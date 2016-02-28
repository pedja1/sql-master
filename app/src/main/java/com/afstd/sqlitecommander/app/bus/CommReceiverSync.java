package com.afstd.sqlitecommander.app.bus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.greenrobot.event.EventBus;

/**
 * Created by pedja on 12.2.16..
 */
public class CommReceiverSync extends BroadcastReceiver
{
    public static final String INTENT_ACTION_STATUS_REQUEST = "com.afstd.sqlcommander.STATUS_REQUEST";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        EventBus.getDefault().post(new SyncStatusRequestEvent());
    }
}
