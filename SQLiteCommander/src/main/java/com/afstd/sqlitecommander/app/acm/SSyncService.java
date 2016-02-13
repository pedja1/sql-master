package com.afstd.sqlitecommander.app.acm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.afstd.sqlitecommander.app.bus.CommReceiverLocal;
import com.afstd.sqlitecommander.app.bus.SyncStatusRequestEvent;
import com.afstd.sqlitecommander.app.bus.SyncStatusResponseEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by pedja on 17.7.14. 15.01.
 * This class is part of the AcountManagerTest
 * Copyright © 2014 ${OWNER}
 */
public class SSyncService extends Service
{
    private static final Object sSyncAdapterLock = new Object();
    private static SSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate()
    {
        synchronized (sSyncAdapterLock)
        {
            if (sSyncAdapter == null)
                sSyncAdapter = new SSyncAdapter(getApplicationContext(), true);
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        postStatus(this, SyncStatusResponseEvent.SERVICE_NOT_RUNNING);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return sSyncAdapter.getSyncAdapterBinder();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SyncStatusRequestEvent event)
    {
        SyncStatusResponseEvent status = SyncStatusResponseEvent.IDLE;
        if(sSyncAdapter != null)
        {
            status = sSyncAdapter.getStatus();
        }
        if(status == null)
            status = SyncStatusResponseEvent.UNKNOWN;
        postStatus(this, status);
    }

    public static void postStatus(Context context, SyncStatusResponseEvent status)
    {
        context.sendBroadcast(new Intent(CommReceiverLocal.INTENT_ACTION_STATUS_RESPONSE).putExtra(CommReceiverLocal.INTENT_EXTRA_STATUS, status));
    }
}
