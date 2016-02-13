package com.afstd.sqlitecommander.app.network;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Log;

import com.af.androidutility.lib.AndroidUtility;
import com.afstd.sqlitecommander.app.fragment.FragmentCloud;
import com.afstd.sqlitecommander.app.model.AuthToken;
import com.afstd.sqlitecommander.app.utility.SettingsManager;
import com.crashlytics.android.Crashlytics;
import com.tehnicomsolutions.http.Internet;
import com.tehnicomsolutions.http.ResponseParser;

import org.json.JSONObject;

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

}

