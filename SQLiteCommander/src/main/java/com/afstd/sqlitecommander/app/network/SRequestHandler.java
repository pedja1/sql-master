package com.afstd.sqlitecommander.app.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afstd.sqlitecommander.app.R;
import com.tehnicomsolutions.http.RequestBuilder;
import com.tehnicomsolutions.http.RequestHandler;
import com.tehnicomsolutions.http.ResponseParser;

/**
 * Created by pedja on 6.7.15. 08.58.
 * This class is part of the Politika
 * Copyright © 2015 ${OWNER}
 */
public class SRequestHandler implements RequestHandler
{
    public static final int REQUEST_CODE_LOGIN = 1002;

    private ProgressDialog dialog;
    private Activity activity;
    boolean showProgressDialog;

    public SRequestHandler(@Nullable Activity activity)
    {
        this(activity, true);
    }

    public SRequestHandler(@Nullable Activity activity, boolean showProgressDialog)
    {
        this.activity = activity;
        this.showProgressDialog = showProgressDialog;
    }

    @Override
    public ResponseParser handleRequest(int requestCode, @NonNull RequestBuilder requestBuilder, boolean sync)
    {
        JSONParser parser = new JSONParser(SInternet.executeHttpRequest(activity, requestBuilder));
        switch (requestCode)
        {
            case REQUEST_CODE_LOGIN:
                parser.parseLoginResponse();
                break;
        }
        return parser;
    }

    public void handlePreRequest(int requestCode, boolean sync)
    {
        if (showProgressDialog && !sync && this.activity != null)
        {
            this.dialog = new ProgressDialog(this.activity);
            this.dialog.setCancelable(false);
            this.dialog.setMessage(this.activity.getString(R.string.please_wait));
            this.dialog.show();
        }

    }

    public void handlePostRequest(int requestCode, @NonNull RequestBuilder builder, ResponseParser responseParser, boolean sync)
    {
        if (showProgressDialog && !sync && this.activity != null && this.dialog != null)
        {
            this.dialog.dismiss();
        }

    }

    public void handleRequestCancelled(int requestCode, @NonNull ResponseParser parser, boolean sync)
    {
    }
}
