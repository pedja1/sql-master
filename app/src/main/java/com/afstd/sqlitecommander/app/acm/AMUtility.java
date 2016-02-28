package com.afstd.sqlitecommander.app.acm;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;

import com.afstd.sqlitecommander.app.App;
import com.afstd.sqlitecommander.app.fragment.FragmentCloud;
import com.afstd.sqlitecommander.app.utility.SettingsManager;

/**
 * Created by pedja on 28.11.14. 15.35.
 * This class is part of the Hub2Date
 * Copyright © 2014 ${OWNER}
 */
public class AMUtility
{
    public static void invalidateToken(boolean clearPassword)
    {
        AccountManager am = AccountManager.get(App.get());

        Account[] accounts = am.getAccountsByType(FragmentCloud.ACCOUNT_TYPE);
        Account activeAccount = null;
        for(Account account : accounts)
        {
            if(account.name.equals(SettingsManager.getActiveAccount()))
                activeAccount = account;
        }

        if(activeAccount != null)
        {
            String authToken = am.peekAuthToken(activeAccount, FragmentCloud.AUTH_TOKEN_TYPE);
            am.invalidateAuthToken(FragmentCloud.ACCOUNT_TYPE, authToken);
            am.setUserData(activeAccount, FragmentCloud.ARG_REFRESH_TOKEN_KEY, null);
            if(clearPassword)am.setPassword(activeAccount, null);
        }
    }

    public static void removeAccount(String accountName, AccountManagerCallback<Boolean> callback)
    {
        AccountManager am = AccountManager.get(App.get());

        Account[] accounts = am.getAccountsByType(FragmentCloud.ACCOUNT_TYPE);
        Account activeAccount = null;
        for(Account account : accounts)
        {
            if(account.name.equals(accountName))
                activeAccount = account;
        }
        if(activeAccount != null)am.removeAccount(activeAccount, callback, null);
    }

    public static Account getAccount(String accountName)
    {
        AccountManager am = AccountManager.get(App.get());
        Account[] accounts = am.getAccountsByType(FragmentCloud.ACCOUNT_TYPE);
        Account activeAccount = null;
        for(Account account : accounts)
        {
            if(account.name.equals(accountName))
                activeAccount = account;
        }
        return activeAccount;
    }
}
