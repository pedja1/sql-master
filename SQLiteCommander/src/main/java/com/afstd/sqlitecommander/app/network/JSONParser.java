package com.afstd.sqlitecommander.app.network;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.text.TextUtils;
import android.util.Log;

import com.af.androidutility.lib.AndroidUtility;
import com.afstd.sqlitecommander.app.fragment.FragmentCloud;
import com.afstd.sqlitecommander.app.model.AuthToken;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;
import com.afstd.sqlitecommander.app.utility.AesCbcWithIntegrity;
import com.afstd.sqlitecommander.app.utility.SettingsManager;
import com.crashlytics.android.Crashlytics;
import com.tehnicomsolutions.http.Internet;
import com.tehnicomsolutions.http.ResponseParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedja on 3.7.15. 10.38.
 * This class is part of the Politika
 * Copyright © 2015 ${OWNER}
 */
public class JSONParser extends ResponseParser
{
    private static final String LOG_TAG = JSONParser.class.getName();
    private JSONObject jsonObject;
    private int responseStatusCode;
    private String responseMessage;

    public JSONParser(String stringObject)
    {
        super(stringObject);
        init();
    }

    public JSONParser(Internet.Response serverResponse)
    {
        super(serverResponse);
        init();
    }

    private void init()
    {
        try
        {
            jsonObject = new JSONObject(this.serverResponse.responseData);
            checkErrors();
            if (!serverResponse.isResponseOk())
            {
                jsonObject = null;
            }
        }
        catch (Exception e)
        {
            if (SettingsManager.DEBUG())
                Log.e(LOG_TAG, "JSONUtility " + e.getMessage());
            if (SettingsManager.DEBUG())
                Log.e(LOG_TAG, "JSONUtility :: Failed to parse json");
            Crashlytics.setString("response message", serverResponse.responseMessage);
            Crashlytics.setString("response data", serverResponse.responseData);
            Crashlytics.setInt("response code", serverResponse.code);
            Crashlytics.setString("request url", serverResponse.request);
            Crashlytics.logException(e);
        }
    }

    private void checkErrors()
    {
        if (jsonObject == null) return;
        responseStatusCode = jsonObject.optInt("status", 1) == 1 ? RESPONSE_STATUS_SUCCESS : RESPONSE_STATUS_RESPONSE_ERROR;//assume success if no error
        responseMessage = jsonObject.optString("message", null);
        if (responseStatusCode != RESPONSE_STATUS_SUCCESS)
        {
            jsonObject = null;
        }
    }

    @Override
    public int getResponseStatus()
    {
        int httpResponse = super.getResponseStatus();
        if (httpResponse != RESPONSE_STATUS_SUCCESS)
        {
            return RESPONSE_STATUS_SERVER_ERROR;
        }
        return responseStatusCode;
    }

    @Override
    public String getResponseMessage()
    {
        if (responseMessage == null)
        {
            return serverResponse.responseMessage;
        }
        else
        {
            return responseMessage;
        }
    }

    public void parseLoginResponse()
    {
        if (jsonObject == null) return;
        if (jsonObject.optString("access_token", null) == null)
            return;
        AuthToken token = new AuthToken();

        token.accessToken = jsonObject.optString("access_token");
        token.refreshToken = jsonObject.optString("refresh_token");
        token.accountName = jsonObject.optString("user_email");

        parseObject = token;
    }

    public void parseRefreshTokenResponse(AccountManager am, Account account)
    {
        if (jsonObject == null) return;
        String refreshToken = jsonObject.optString("refresh_token", null);
        if (AndroidUtility.isStringValid(refreshToken))
        {
            am.setUserData(account, FragmentCloud.ARG_REFRESH_TOKEN_KEY, refreshToken);
        }
        String accessToken = jsonObject.optString("access_token", null);
        if (AndroidUtility.isStringValid(accessToken))
        {
            parseObject = accessToken;
        }
    }

    public void parseSyncResponse()
    {
        if(SettingsManager.DEBUG()) Log.d("sync", "parse sync response");
        if (jsonObject == null) return;

        JSONObject jData = jsonObject.optJSONObject("data");
        if(jData != null)
        {
            //query history
            if(SettingsManager.getSyncSetting(SettingsManager.SyncKey.sync_query_history))
            {
                JSONArray jQueryHistory = jData.optJSONArray("query_history");
                if (jQueryHistory != null)
                {
                    List<String> queryHistory = new ArrayList<>();
                    for(int i = 0; i < jQueryHistory.length(); i++)
                    {
                        queryHistory.add(jQueryHistory.optString(i));
                    }
                    DatabaseManager.getInstance().insertQueryHistory(queryHistory);
                }
            }

            //databases
            if(SettingsManager.getSyncSetting(SettingsManager.SyncKey.sync_databases))
            {
                JSONArray jDatabases = jsonObject.optJSONArray("databases");
                if(jDatabases != null)
                {
                    List<DatabaseEntry> entries = new ArrayList<>();

                    for(int i = 0; i < entries.size(); i++)
                    {
                        JSONObject jDatabase = jDatabases.optJSONObject(i);
                        DatabaseEntry databaseEntry = new DatabaseEntry();
                        databaseEntry.id = jDatabase.optString("_id");
                        databaseEntry.type = jDatabase.optString("type");
                        databaseEntry.databaseUri = jDatabase.optString("database_uri");

                        if(TextUtils.isEmpty(databaseEntry.id) || TextUtils.isEmpty(databaseEntry.type) || TextUtils.isEmpty(databaseEntry.databaseUri))
                        {
                            continue;
                        }
                        databaseEntry.databasePort = jDatabase.optInt("database_port");
                        decryptCredentials(databaseEntry, jDatabase);
                        databaseEntry.isFavorite = jDatabase.optBoolean("is_favorite");
                        databaseEntry.created = jDatabase.optLong("created");
                        databaseEntry.accessed = jDatabase.optLong("accessed");

                        entries.add(databaseEntry);
                    }

                    DatabaseManager.getInstance().insertDatabaseEntry(entries);
                    DatabaseManager.getInstance().rawQueryNoResult("DELETE FROM _database WHERE deleted = 1", null);
                }
            }
        }

        //settings
        if(SettingsManager.getSyncSetting(SettingsManager.SyncKey.sync_settings))
        {
            JSONObject jSettings = jsonObject.optJSONObject("settings");
            if(jSettings != null)
            {
                for(SettingsManager.SyncKey key : SettingsManager.SyncKey.values())
                {
                    if(!key.isSyncable() && jSettings.has(key.toString()))
                        continue;
                    boolean enabled = jSettings.optBoolean(key.toString());
                    SettingsManager.setSyncSetting(key, enabled);
                }

                for(SettingsManager.Key key : SettingsManager.Key.values())
                {
                    if(!key.isSyncable() && jSettings.has(key.toString()))
                        continue;
                    SettingsManager.setSetting(key, jSettings.opt(key.toString()));
                }
            }
        }

    }

    private void decryptCredentials(DatabaseEntry entry, JSONObject jDatabase)
    {
        String databaseUsername, databasePassword = null;
        //if no encryption key or both username and password are empty, skip encryption
        if(!SettingsManager.getSyncSetting(SettingsManager.SyncKey.sync_credentials)
                || TextUtils.isEmpty(SettingsManager.getEc())
                || (TextUtils.isEmpty(databaseUsername = jDatabase.optString("database_username")) && TextUtils.isEmpty(databasePassword = jDatabase.optString("database_password"))))
            return;
        try
        {
            AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.keys(SettingsManager.getEc());

            if(!TextUtils.isEmpty(databaseUsername))
            {
                AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(databaseUsername);
                entry.databaseUsername = AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys);
            }
            if(!TextUtils.isEmpty(databasePassword))
            {
                AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(databasePassword);
                entry.databasePassword = AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys);
            }
        }
        catch (GeneralSecurityException | UnsupportedEncodingException e)
        {
            if(SettingsManager.DEBUG())e.printStackTrace();
            //fail silently on any kind of error
            //user will see warning on database entry when restored without username/password
        }
    }

}

