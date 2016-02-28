package com.afstd.sqlitecommander.app;


import android.accounts.Account;
import android.accounts.AccountManager;

import com.afstd.sqlitecommander.app.fragment.FragmentCloud;
import com.afstd.sqlitecommander.app.network.SInternet;
import com.afstd.sqlitecommander.app.utility.SettingsManager;
import com.tehnicomsolutions.http.RequestBuilder;
import com.tehnicomsolutions.http.TSHttp;

import eu.chainfire.libsuperuser.Application;

/**
 * Created by pedja on 24.1.16..
 */
public class App extends Application
{
    private static App instance;

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;

        TSHttp.init(this);
        TSHttp.LOGGING = SettingsManager.DEBUG();
        RequestBuilder.setDefaultRequestUrl(SInternet.API_REQUEST_URL);
        restoreAccount();
    }

    public static App get()
    {
        return instance;
    }

    /**
     * When user clears data from app (from settings) accounts aren't deleted
     * This method finds accounts and set first found account as active
     */
    private void restoreAccount()
    {
        AccountManager am = AccountManager.get(this);
        Account[] accounts = am.getAccountsByType(FragmentCloud.ACCOUNT_TYPE);

        String activeUser = SettingsManager.getActiveAccount();
        if (null == activeUser && accounts.length != 0)//if there is no active account and there are accounts on device
        {
            Account account = accounts[0];
            SettingsManager.setActiveAccount(account.name);
        }
    }
}
