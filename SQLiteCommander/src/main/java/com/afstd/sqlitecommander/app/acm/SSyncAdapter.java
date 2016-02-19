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

import com.afstd.sqlitecommander.app.App;
import com.afstd.sqlitecommander.app.MainActivity;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.bus.SyncStatusResponseEvent;
import com.afstd.sqlitecommander.app.utility.SettingsManager;

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
            showSyncNotification();
            try
            {
                Thread.sleep(30000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            //TODO implement service
            /*RequestBuilder builder = new SRequestBuilder(RequestBuilder.Method.POST, true);
            builder.addParam("sync");
            JSONParser parser = new JSONParser(SInternet.executeHttpRequest(builder));
            parser.parseSyncResponse();*/
            hideSyncNotification();
        }
        status = SyncStatusResponseEvent.IDLE;
        SettingsManager.setLastSyncTime(System.currentTimeMillis());
        postStatus(getContext(), status);
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

        //mBuilder.setLargeIcon(BitmapFactory.decodeResource(App.get().getResources(), R.drawable.ic_stat_sync));

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
