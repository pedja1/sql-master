package com.afstd.sqlitecommander.app.acm;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.NetworkErrorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.afstd.sqlitecommander.app.App;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.fragment.FragmentCloud;
import com.afstd.sqlitecommander.app.network.JSONParser;
import com.afstd.sqlitecommander.app.network.SInternet;
import com.afstd.sqlitecommander.app.network.SRequestBuilder;
import com.afstd.sqlitecommander.app.utility.SettingsManager;
import com.tehnicomsolutions.http.Internet;
import com.tehnicomsolutions.http.RequestBuilder;
import com.tehnicomsolutions.http.ResponseParser;

import java.io.IOException;

import static com.afstd.sqlitecommander.app.fragment.FragmentCloud.ARG_REFRESH_TOKEN_KEY;

/**
 * Created by pedja on 17.7.14. 09.07.
 * This class is part of the H2D
 * Copyright © 2014 ${OWNER}
 */
public class SAccountAuthenticator extends AbstractAccountAuthenticator
{
    public static final String CLIENT_ID = "e3ec69ccfa6045d70fc8910469831d82";
    public static final String CLIENT_SECRET = "52ccbf34bfdd023a649bcf14187a2c48b660dfc95af3c0685c5bd93ec2ac61b6";
    private static final int ERROR_TOKEN_INVALID = -1;

    private Context mContext;

    public SAccountAuthenticator(Context context)
    {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s)
    {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException
    {
        /*final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(FragmentCloud.ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(LoginActivity.ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(LoginActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;*/
        //TODO is null ok
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException
    {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException
    {
        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        final AccountManager am = AccountManager.get(mContext);

        String authToken = am.peekAuthToken(account, authTokenType);
        String refreshToken = am.getUserData(account, ARG_REFRESH_TOKEN_KEY);

        // Lets give another try to authenticate the user
        if (TextUtils.isEmpty(authToken))
        {
            //if authToken is empty(or null) try to refresh it
            if (!TextUtils.isEmpty(refreshToken))
            {
                //try to request new access token with refresh token
                authToken = refreshToken(am, account, refreshToken);
            }
            if (TextUtils.isEmpty(authToken))//token still empty, try re-login
            {
                //no refresh token, try to login with user/pass, social
                authToken = requestAccessToken(am, account);
            }

        }

        // If we get an authToken - we return it
        if (!TextUtils.isEmpty(authToken))
        {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        final Bundle bundle = new Bundle();
        bundle.putInt(AccountManager.KEY_ERROR_CODE, ERROR_TOKEN_INVALID);
        bundle.putString(AccountManager.KEY_ERROR_MESSAGE, mContext.getString(R.string.invalid_token));
        return bundle;
    }

    private String refreshToken(AccountManager am, Account account, String refreshToken)
    {
        RequestBuilder builder = new SRequestBuilder(RequestBuilder.Method.POST, false);
        builder.setRequestUrl(SInternet.API_REQUEST_URL);

        builder.addParam("auth").addParam("token");
        builder.addParam("client_id", CLIENT_ID);
        builder.addParam("client_secret", CLIENT_SECRET);
        builder.addParam("username", account.name);
        builder.addParam("refresh_token", refreshToken);
        builder.addParam("grant_type", "refresh_token");

        JSONParser jsonParser = new JSONParser(Internet.executeHttpRequest(builder));
        jsonParser.parseRefreshTokenResponse(am, account);

        if (jsonParser.getResponseStatus() == ResponseParser.RESPONSE_STATUS_SUCCESS && jsonParser.getParseObject() != null)
        {
            return jsonParser.getParseObject();
        }
        return null;
    }

    private String requestAccessToken(AccountManager am, Account account)
    {
        /*String token = null;
        switch (service)
        {
            case LandingPageActivity.LOGIN_SERVICE_FACEBOOK:
                token = AccessToken.getCurrentAccessToken().getToken();
                break;
            case LandingPageActivity.LOGIN_SERVICE_TWITTER:
                token = TwitterCore.getInstance().getSessionManager().getActiveSession().getAuthToken().toString();//twitter token newer expires
                break;
            case LandingPageActivity.LOGIN_SERVICE_GOOGLE:
                int maxRetries = 3;
                while (maxRetries > 0)
                {
                    try
                    {
                        token = GoogleAuthUtil.getToken(mContext, account.name,
                                "oauth2:https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email");
                        break;
                    }
                    catch (UserRecoverableAuthException userAuthEx)
                    {
                        if (App.get().getCurrentActivity() instanceof AuthenticatorActivity)
                        {
                            App.get().getCurrentActivity().startActivityForResult(userAuthEx.getIntent(), AuthenticatorActivity.REQUEST_CODE_GOOGLE_LOGIN);
                        }
                        synchronized (AuthenticatorActivity.authenticatorLock)
                        {
                            try
                            {
                                AuthenticatorActivity.authenticatorLock.wait();
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                                break;
                            }
                        }
                        maxRetries--;
                    }
                    catch (IOException | GoogleAuthException e)
                    {
                        e.printStackTrace();
                        break;
                    }
                }
                break;
        }
        if (token == null) return null;
        RequestBuilder builder = new TulfieRequestBuilder(RequestBuilder.Method.POST, false);
        builder.setRequestUrl(Constants.API_REQUEST_URL);
        builder.addParam("members").addParam("social-login");
        builder.addParam("service", service);
        builder.addParam("token", token);
        JSONParser jsonParser = new JSONParser(Internet.executeHttpRequest(builder));
        jsonParser.parseLoginResponse();

        if (jsonParser.getResponseStatus() == JSONParser.RESPONSE_STATUS_SUCCESS && jsonParser.getParseObject() != null)
        {
            return jsonParser.getParseObject(Intent.class).getStringExtra(AccountManager.KEY_AUTHTOKEN);
        }*/

        return null;
    }


    @Override
    public String getAuthTokenLabel(String s)
    {
        return "getAuthTokenLabel";
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException
    {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException
    {
        return null;
    }

    @NonNull
    @Override
    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account)
    {
        Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);//allow account removal
        return result;
    }

    public static String getAccessToken(@NonNull Activity activity)
    {
        AccountManager mAccountManager = AccountManager.get(App.get());
        //String authToken = mAccountManager.blockingGetAuthToken(account, LoginActivity.mAuthTokenType, true);

        //AccountManagerFuture<Bundle> future = mAccountManager.getAuthTokenByFeatures(LoginActivity.ACCOUNT_TYPE, LoginActivity.mAuthTokenType, null, activity, null, null, null, null);

        //new testing
        Account[] accounts = mAccountManager.getAccountsByType(FragmentCloud.ACCOUNT_TYPE);

        //find active account
        Account activeAccount = null;
        for (Account account : accounts)
        {
            if (account.name.equals(SettingsManager.getActiveAccount()))
            {
                activeAccount = account;
                break;
            }
        }


        AccountManagerFuture<Bundle> future;
        if (accounts.length == 0)
        {
            //no account found on device, ask user to add new one
            future = mAccountManager.getAuthTokenByFeatures(FragmentCloud.ACCOUNT_TYPE, FragmentCloud.AUTH_TOKEN_TYPE, null, activity, null, null, null, null);
        }
        else
        {
            //there are accounts on a device, first check if current active account exists
            if (activeAccount != null)
            {
                //current account active found in am, get token for it
                future = mAccountManager.getAuthToken(activeAccount, FragmentCloud.AUTH_TOKEN_TYPE, null, activity, null, null);
            }
            else
            {
                // there are other accounts on a device but our active account wasn't found(probably removed)
                // ask user to select account and get token for it

                if (accounts.length == 1)
                {
                    //if there is only one account on a device, try to get token for it
                    future = mAccountManager.getAuthToken(accounts[0], FragmentCloud.AUTH_TOKEN_TYPE, null, activity, null, null);
                }
                else
                {
                    //if there is more then one account, ask user to select one, and get token for it
                    future = mAccountManager.getAuthTokenByFeatures(FragmentCloud.ACCOUNT_TYPE, FragmentCloud.AUTH_TOKEN_TYPE, null, activity, null, null, null, null);
                }
            }
        }
        //new testing end
        try
        {
            Bundle result = future.getResult();
            SettingsManager.setActiveAccount(result.getString(AccountManager.KEY_ACCOUNT_NAME));//save account name
            return result.getString(AccountManager.KEY_AUTHTOKEN);
        }
        catch (OperationCanceledException | IOException e)
        {
            e.printStackTrace();
        }
        catch (AuthenticatorException e)
        {
            mAccountManager.invalidateAuthToken(FragmentCloud.ACCOUNT_TYPE, null);
        }
        return null;
    }
}
