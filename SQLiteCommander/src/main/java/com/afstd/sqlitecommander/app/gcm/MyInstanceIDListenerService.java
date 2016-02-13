package com.afstd.sqlitecommander.app.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by pedja on 27.10.15. 14.36.
 * This class is part of the Politika
 * Copyright © 2015 ${OWNER}
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService
{
    @Override
    public void onTokenRefresh()
    {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
