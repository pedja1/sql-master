package com.afstd.sqlitecommander.app.acm;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.afstd.sqlitecommander.app.App;
import com.afstd.sqlitecommander.app.MainActivity;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.bus.SyncStatusResponseEvent;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.model.QueryHistory;
import com.afstd.sqlitecommander.app.network.JSONParser;
import com.afstd.sqlitecommander.app.network.SInternet;
import com.afstd.sqlitecommander.app.network.SRequestBuilder;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;
import com.afstd.sqlitecommander.app.utility.AesCbcWithIntegrity;
import com.afstd.sqlitecommander.app.utility.SettingsManager;
import com.tehnicomsolutions.http.RequestBuilder;
import com.tehnicomsolutions.http.ResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;

import static com.afstd.sqlitecommander.app.acm.SSyncService.postStatus;

/**
 * Created by pedja on 17.7.14. 14.54.
 * This class is part of the AcountManagerTest
 * Copyright © 2014 ${OWNER}
 */
public class SSyncAdapter extends AbstractThreadedSyncAdapter
{
    private static final int NOTIFICATION_ID = 3452134;
    public static final long SYNC_ADAPTER_INTERVAL = 60 * 60 * 1;//in seconds - 1 hours
    private final AccountManager mAccountManager;
    private SyncStatusResponseEvent status;

    /*
     * Database Sync Schema
     * */
    /*
    _id: {type: String, required: true},
    user_id: {type: String, required: true},
    databases: [
        {
            _id: {type: String, required: true},
            type: { type: String, required: true},
            database_uri: String,
            database_username: String,
            database_password: String
        }
    ],
    favorites: [String],
    history: [String],
    ac_history: [String],
    settings: {

    }
    */

    public SSyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
        status = SyncStatusResponseEvent.IDLE;
        postStatus(getContext(), status);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
    {
        status = SyncStatusResponseEvent.IN_SYNC;
        postStatus(getContext(), status);
        if(account.name.equals(SettingsManager.getActiveAccount()) && SettingsManager.getSyncSetting(SettingsManager.SyncKey.sync_enabled))
        {
            if(SettingsManager.DEBUG()) Log.d("sync", "sync started");
            showSyncNotification();
            RequestBuilder builder = new SRequestBuilder(RequestBuilder.Method.POST, true);
            builder.addParam("sync");
            builder.setPostMethod(RequestBuilder.PostMethod.BODY);
            builder.setContentType("application/json");

            builder.setResponseMessagePolicy(new ResponseHandler.ResponseMessagePolicy().showErrorMessages(false).showSuccessMessages(false));

            try
            {
                if(SettingsManager.DEBUG()) Log.d("sync", "generating json");
                JSONObject jsonObject = new JSONObject();

                if (SettingsManager.getSyncSetting(SettingsManager.SyncKey.sync_databases))
                {
                    if(SettingsManager.DEBUG()) Log.d("sync", "adding databases for sync");
                    JSONArray jDatabases = new JSONArray();
                    List<DatabaseEntry> databases = DatabaseManager.getInstance().getDatabaseEntries("SELECT * FROM _database", null);

                    for(DatabaseEntry entry : databases)
                    {
                        JSONObject jDatabase = new JSONObject();

                        jDatabase.put("_id", entry.id);
                        if(entry.deleted)
                        {
                            jDatabase.put("deleted", true);
                        }
                        else
                        {
                            encryptCredentials(entry, jDatabase);
                            jDatabase.put("type", entry.type);
                            jDatabase.put("database_uri", entry.databaseUri);
                            jDatabase.put("database_name", entry.databaseName);
                            jDatabase.put("database_port", entry.databasePort);
                            jDatabase.put("is_favorite", entry.isFavorite);
                            jDatabase.put("created", entry.created);
                            jDatabase.put("accessed", entry.accessed);
                        }

                        jDatabases.put(jDatabase);
                    }
                    jsonObject.put("databases", jDatabases);
                }

                if(SettingsManager.getSyncSetting(SettingsManager.SyncKey.sync_query_history))
                {
                    if(SettingsManager.DEBUG()) Log.d("sync", "adding query history");
                    JSONArray jQueryHistory = new JSONArray();
                    List<QueryHistory> queryHistory = DatabaseManager.getInstance().getQueryHistory("SELECT * FROM query_history", null);
                    for(QueryHistory history : queryHistory)
                    {
                        jQueryHistory.put(history.query);
                    }
                    jsonObject.put("query_history", jQueryHistory);
                }

                if(SettingsManager.getSyncSetting(SettingsManager.SyncKey.sync_settings))
                {
                    if(SettingsManager.DEBUG()) Log.d("sync", "adding settings");
                    JSONObject jSettings = new JSONObject();
                    for(SettingsManager.SyncKey key : SettingsManager.SyncKey.values())
                    {
                        if(!key.isSyncable())
                            continue;
                        jSettings.put(key.toString(), SettingsManager.getSyncSetting(key));
                    }

                    for(SettingsManager.Key key : SettingsManager.Key.values())
                    {
                        if(!key.isSyncable())
                            continue;
                        jSettings.put(key.toString(), SettingsManager.getSetting(key));
                    }

                    jsonObject.put("settings", jSettings);
                }
                builder.setRequestBody(jsonObject.toString());

                if(SettingsManager.DEBUG()) Log.d("sync", "sending request");
                if(SettingsManager.DEBUG()) Log.d("sync", "json: " + jsonObject.toString());
                JSONParser parser = new JSONParser(SInternet.executeHttpRequest(builder));
                parser.parseSyncResponse();
            }
            catch (JSONException e)
            {
                if(SettingsManager.DEBUG())e.printStackTrace();
            }

            hideSyncNotification();
        }
        status = SyncStatusResponseEvent.IDLE;
        SettingsManager.setLastSyncTime(System.currentTimeMillis());
        postStatus(getContext(), status);
    }

    private void encryptCredentials(DatabaseEntry entry, JSONObject jDatabase)
    {
        //if no encryption key or both username and password are empty, skip encryption
        if(!SettingsManager.getSyncSetting(SettingsManager.SyncKey.sync_credentials) || TextUtils.isEmpty(SettingsManager.getEc()) || (TextUtils.isEmpty(entry.databaseUsername) && TextUtils.isEmpty(entry.databasePassword)))
            return;
        try
        {
            AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.keys(SettingsManager.getEc());

            if(!TextUtils.isEmpty(entry.databaseUsername))
            {
                jDatabase.put("database_username", AesCbcWithIntegrity.encrypt(entry.databaseUsername, keys));
            }
            if(!TextUtils.isEmpty(entry.databasePassword))
            {
                jDatabase.put("database_password", AesCbcWithIntegrity.encrypt(entry.databasePassword, keys));
            }
        }
        catch (GeneralSecurityException | UnsupportedEncodingException | JSONException e)
        {
            if(SettingsManager.DEBUG())e.printStackTrace();
            //fail silently on any kind of error
            //user will see warning on database entry when restored without username/password
        }
    }

    private void hideSyncNotification()
    {
        NotificationManager mNotificationManager = (NotificationManager) App.get().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    private void showSyncNotification()
    {
        if(!SettingsManager.isShowSyncNotification())
        {
            return;
        }

        NotificationManager mNotificationManager = (NotificationManager) App.get().getSystemService(Context.NOTIFICATION_SERVICE);

        Intent dataIntent = new Intent(getContext(), MainActivity.class);
        dataIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(getContext(), 0,
                dataIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext());

        mBuilder.setSmallIcon(R.drawable.ic_stat_sync);

        mBuilder.setContentTitle(App.get().getString(R.string.sync_notification_title));
        mBuilder.setContentText(App.get().getString(R.string.sync_notification_text));

        mBuilder.setContentIntent(resultPendingIntent);

        mBuilder.setOngoing(true);

        Notification notification = mBuilder.build();

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    public SyncStatusResponseEvent getStatus()
    {
        return status;
    }
}
