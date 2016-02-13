package com.afstd.sqlitecommander.app.bus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.greenrobot.event.EventBus;

/**
 * Created by pedja on 12.2.16..
 */
public class CommReceiverLocal extends BroadcastReceiver
{
    public static final String INTENT_EXTRA_STATUS = "com.afstd.sqlcommander.STATUS";
    public static final String INTENT_ACTION_STATUS_RESPONSE = "com.afstd.sqlcommander.STATUS_RESPONSE";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        SyncStatusResponseEvent status = (SyncStatusResponseEvent) intent.getSerializableExtra(INTENT_EXTRA_STATUS);
        EventBus.getDefault().post(status);
    }
}
