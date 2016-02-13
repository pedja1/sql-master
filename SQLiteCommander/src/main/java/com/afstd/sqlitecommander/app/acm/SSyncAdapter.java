package com.afstd.sqlitecommander.app.acm;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

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
            //TODO implement service
            /*RequestBuilder builder = new SRequestBuilder(RequestBuilder.Method.POST, true);
            builder.addParam("sync");
            JSONParser parser = new JSONParser(SInternet.executeHttpRequest(builder));
            parser.parseSyncResponse();*/
        }
        status = SyncStatusResponseEvent.IDLE;
        SettingsManager.setLastSyncTime(System.currentTimeMillis());
        postStatus(getContext(), status);
    }

    public SyncStatusResponseEvent getStatus()
    {
        return status;
    }
}
