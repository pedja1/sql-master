package com.afstd.sqlitecommander.app.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.af.androidutility.lib.AndroidUtility;
import com.afstd.sqlitecommander.app.App;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.acm.AMUtility;
import com.afstd.sqlitecommander.app.acm.SAccountAuthenticator;
import com.afstd.sqlitecommander.app.bus.CommReceiverSync;
import com.afstd.sqlitecommander.app.bus.SyncStatusResponseEvent;
import com.afstd.sqlitecommander.app.model.AuthToken;
import com.afstd.sqlitecommander.app.network.SRequestBuilder;
import com.afstd.sqlitecommander.app.network.SRequestHandler;
import com.afstd.sqlitecommander.app.utility.SettingsManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.tehnicomsolutions.http.RequestBuilder;
import com.tehnicomsolutions.http.ResponseHandler;
import com.tehnicomsolutions.http.ResponseParser;
import com.tehnicomsolutions.http.TSRequestManager;

import java.text.SimpleDateFormat;
import java.util.Locale;

import de.greenrobot.event.EventBus;

/**
 * Created by pedja on 21.1.16..
 */
public class FragmentCloud extends Fragment implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
    private SimpleDateFormat LAST_SYNC_FORMAT = new SimpleDateFormat("HH:mm dd.MM, yyyy", Locale.US);
    public static final String ARG_REFRESH_TOKEN_KEY = "refresh_token";
    public static final String ACCOUNT_TYPE = App.get().getString(R.string.account_type);
    public static final String AUTH_TOKEN_TYPE = ACCOUNT_TYPE + ".LOGIN";
    public static final String GRANT_TYPE = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_CLIENT_ID = "770914414372-2923i5l0agg2cjjj20ppo4m7frm2mref.apps.googleusercontent.com";

    public static FragmentCloud newInstance()
    {
        FragmentCloud fragment = new FragmentCloud();
        return fragment;
    }

    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView, tvLoginWarning;
    private ScrollView svSettings;
    private SignInButton signInButton;
    private static final int RC_SIGN_IN = 9001;
    private ProgressDialog mProgressDialog;

    private AccountManager mAccountManager;

    private TSRequestManager mRequestManager;

    private boolean mCheckChangeEnabled;
    private CheckBox cbSyncDatabases;
    private CheckBox cbSyncQueryHistory;
    private CheckBox cbSyncCredentials;
    private CheckBox cbSyncSettings;

    private Button btnDelete, btnSyncNow;

    private TextView tvLastSyncTime, tvSyncStatus;

    private LoadingAnimation loadingAnimation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        mAccountManager = (AccountManager) getActivity().getSystemService(Context.ACCOUNT_SERVICE);
        View view = inflater.inflate(R.layout.fragment_cloud, container, false);

        mStatusTextView = (TextView) view.findViewById(R.id.header);
        tvLoginWarning = (TextView) view.findViewById(R.id.tvLoginWarning);
        svSettings = (ScrollView) view.findViewById(R.id.svSettings);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton = (SignInButton) view.findViewById(R.id.btnLogin);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(this);

        btnDelete = (Button) view.findViewById(R.id.btnDelete);
        btnSyncNow = (Button) view.findViewById(R.id.btnSyncNow);
        btnDelete.setOnClickListener(this);
        btnSyncNow.setOnClickListener(this);

        tvLastSyncTime = (TextView) view.findViewById(R.id.tvLastSyncTime);
        setLastSyncData();

        tvSyncStatus = (TextView) view.findViewById(R.id.tvSyncStatus);
        tvSyncStatus.setText(Html.fromHtml(getString(R.string.sync_status, getString(R.string.checking_1))));

        loadingAnimation = new LoadingAnimation();
        loadingAnimation.execute();

        getActivity().sendBroadcast(new Intent(CommReceiverSync.INTENT_ACTION_STATUS_REQUEST));

        //if no response from service within 5 seconds, show error
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if(loadingAnimation != null)
                    loadingAnimation.cancel(true);
                tvSyncStatus.setText(Html.fromHtml(getString(R.string.sync_status, SyncStatusResponseEvent.SERVICE_NOT_RUNNING)));
            }
        }, 5000);

        setupSettingsViews(view);

        updateUI();

        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.cloud);
    }

    private void setLastSyncData()
    {
        //TODO sync service sets this pref. Since sync service is running in separate process, changes wont reflect here
        long lastSyncTime = SettingsManager.getLastSyncTime();
        tvLastSyncTime.setText(Html.fromHtml(getString(R.string.last_sync, lastSyncTime <= 0 ? getString(R.string.never) : LAST_SYNC_FORMAT.format(lastSyncTime))));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SyncStatusResponseEvent event)
    {
        if(loadingAnimation != null)
            loadingAnimation.cancel(true);
        tvSyncStatus.setText(Html.fromHtml(getString(R.string.sync_status, event)));
        setLastSyncData();
    }

    private void setupSettingsViews(View view)
    {
        Switch sSwitch = (Switch) view.findViewById(R.id.cbSyncEnabled);
        cbSyncDatabases = (CheckBox) view.findViewById(R.id.cbSyncDatabases);
        cbSyncQueryHistory = (CheckBox) view.findViewById(R.id.cbSyncQueryHistory);
        cbSyncCredentials = (CheckBox) view.findViewById(R.id.cbSyncCredentials);
        cbSyncSettings = (CheckBox) view.findViewById(R.id.cbSyncSettings);

        mCheckChangeEnabled = false;

        sSwitch.setChecked(SettingsManager.getSyncSetting(SettingsManager.SyncKey.fromViewId(R.id.cbSyncEnabled)));
        cbSyncDatabases.setChecked(SettingsManager.getSyncSetting(SettingsManager.SyncKey.fromViewId(R.id.cbSyncDatabases)));
        cbSyncQueryHistory.setChecked(SettingsManager.getSyncSetting(SettingsManager.SyncKey.fromViewId(R.id.cbSyncQueryHistory)));
        cbSyncCredentials.setChecked(SettingsManager.getSyncSetting(SettingsManager.SyncKey.fromViewId(R.id.cbSyncCredentials)));
        cbSyncSettings.setChecked(SettingsManager.getSyncSetting(SettingsManager.SyncKey.fromViewId(R.id.cbSyncSettings)));

        mCheckChangeEnabled = true;

        sSwitch.setOnCheckedChangeListener(this);
        cbSyncDatabases.setOnCheckedChangeListener(this);
        cbSyncQueryHistory.setOnCheckedChangeListener(this);
        cbSyncCredentials.setOnCheckedChangeListener(this);
        cbSyncSettings.setOnCheckedChangeListener(this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btnLogin:
                signIn();
                break;
            case R.id.btnSyncNow:
                Account account = AMUtility.getAccount(SettingsManager.getActiveAccount());
                if(account != null)
                {
                    Bundle settingsBundle = new Bundle();
                    settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                    settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, false);
                    ContentResolver.requestSync(account, getString(R.string.content_authority), settingsBundle);
                }
                break;
            case R.id.btnCloud:
                initRequestManager();
                RequestBuilder builder = new SRequestBuilder(RequestBuilder.Method.DELETE, true);
                builder.addParam("sync");
                mRequestManager.execute(SRequestHandler.REQUEST_CODE_DELETE, builder);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result)
    {
        hideProgressDialog();
        if (result.isSuccess())
        {
            GoogleSignInAccount acct = result.getSignInAccount();
            initRequestManager();
            RequestBuilder builder = new SRequestBuilder(RequestBuilder.Method.POST, false);
            builder.addParam("oauth").addParam("token");
            builder.addParam("grant_type", GRANT_TYPE);
            builder.addParam("client_id", SAccountAuthenticator.CLIENT_ID);
            builder.addParam("client_secret", SAccountAuthenticator.CLIENT_SECRET);
            builder.addParam("id_token", acct.getIdToken());
            mRequestManager.execute(SRequestHandler.REQUEST_CODE_LOGIN, builder);
        }
        else
        {
            AndroidUtility.showToast(getActivity(), result.getStatus().getStatusMessage());
        }
    }

    private void initRequestManager()
    {
        if (mRequestManager == null)
        {
            mRequestManager = new TSRequestManager(getActivity(), false);
            mRequestManager.setRequestHandler(new SRequestHandler(getActivity()));
            mRequestManager.addResponseHandler(new ResponseHandler()
            {
                @Override
                public void onResponse(int requestCode, int responseStatus, ResponseParser responseParser)
                {
                    switch (requestCode)
                    {
                        case SRequestHandler.REQUEST_CODE_LOGIN:
                            if (responseStatus == ResponseParser.RESPONSE_STATUS_SUCCESS && responseParser.getParseObject() != null)
                            {
                                finishLogin(responseParser.getParseObject(AuthToken.class));
                            }
                            break;
                    }
                }
            });
        }
    }

    private void signIn()
    {
        showProgressDialog();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void showProgressDialog()
    {
        if (mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog()
    {
        if (mProgressDialog != null && mProgressDialog.isShowing())
        {
            mProgressDialog.hide();
        }
    }

    private void updateUI()
    {
        String accountName = SettingsManager.getActiveAccount();
        if (accountName != null)
        {
            mStatusTextView.setText(Html.fromHtml(getString(R.string.signed_in_fmt, accountName)));
            signInButton.setVisibility(View.GONE);
            tvLoginWarning.setVisibility(View.GONE);
            svSettings.setVisibility(View.VISIBLE);
        }
        else
        {
            mStatusTextView.setText(R.string.signed_out);
            signInButton.setVisibility(View.VISIBLE);
            tvLoginWarning.setVisibility(View.VISIBLE);
            svSettings.setVisibility(View.GONE);
        }
    }

    private void finishLogin(AuthToken token)
    {
        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
        boolean addingNewAccount = true;
        for (Account account : accounts)
        {
            if (account.name.equals(token.accountName))
            {
                addingNewAccount = false;
                break;
            }
        }
        final Account account = new Account(token.accountName, ACCOUNT_TYPE);
        if (addingNewAccount)
        {
            String authtokenType = AUTH_TOKEN_TYPE;
            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, null, null);
            mAccountManager.setAuthToken(account, authtokenType, token.accessToken);
            mAccountManager.setUserData(account, ARG_REFRESH_TOKEN_KEY, token.refreshToken);
        }
        else
        {
            mAccountManager.setPassword(account, null);
            mAccountManager.setAuthToken(account, AUTH_TOKEN_TYPE, token.accessToken);
            mAccountManager.setUserData(account, ARG_REFRESH_TOKEN_KEY, token.refreshToken);
        }
        SettingsManager.setActiveAccount(token.accountName);
        //setAccountAuthenticatorResult(intent.getExtras());
        updateUI();
    }

    @Override
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked)
    {
        if (!mCheckChangeEnabled)
            return;
        final SettingsManager.SyncKey key = SettingsManager.SyncKey.fromViewId(buttonView.getId());
        if (key != null)
        {
            SettingsManager.setSyncSetting(key, isChecked);
        }
        if (key == SettingsManager.SyncKey.sync_enabled)
        {
            if (isChecked)
            {
                cbSyncDatabases.setEnabled(true);
                cbSyncQueryHistory.setEnabled(true);
                cbSyncCredentials.setEnabled(true);
                cbSyncSettings.setEnabled(true);
                btnSyncNow.setEnabled(true);
            }
            else
            {
                cbSyncDatabases.setEnabled(false);
                cbSyncQueryHistory.setEnabled(false);
                cbSyncCredentials.setEnabled(false);
                cbSyncSettings.setEnabled(false);
                btnSyncNow.setEnabled(false);
            }
        }
    }

    private class LoadingAnimation extends AsyncTask<Void, Integer, Void>
    {
        private boolean canceled;

        @Override
        protected Void doInBackground(Void... params)
        {
            int offset = 0;
            while (true)
            {
                if(canceled)
                    return null;
                publishProgress(offset);
                try
                {
                    Thread.sleep(700);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                offset++;
            }
        }


        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
            if(canceled || !isAdded())
                return;

            if (values[0] % 3 == 0)
            {
                tvSyncStatus.setText(Html.fromHtml(getString(R.string.sync_status, getString(R.string.checking_1))));
            }
            else if (values[0] % 3 == 1)
            {
                tvSyncStatus.setText(Html.fromHtml(getString(R.string.sync_status, getString(R.string.checking_2))));
            }
            else if (values[0] % 3 == 2)
            {
                tvSyncStatus.setText(Html.fromHtml(getString(R.string.sync_status, getString(R.string.checking_3))));
            }
        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();
            canceled = true;
        }
    }
}
