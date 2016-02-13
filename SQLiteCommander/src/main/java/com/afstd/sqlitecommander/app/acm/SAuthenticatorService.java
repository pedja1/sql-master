package com.afstd.sqlitecommander.app.acm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by pedja on 17.7.14. 09.33.
 * This class is part of the H2D
 * Copyright © 2014 ${OWNER}
 */
public class SAuthenticatorService extends Service
{
    @Override
    public IBinder onBind(Intent intent)
    {
        SAccountAuthenticator authenticator = new SAccountAuthenticator(this);
        return authenticator.getIBinder();
    }
}
