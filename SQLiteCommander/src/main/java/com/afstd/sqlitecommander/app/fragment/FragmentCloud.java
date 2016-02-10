package com.afstd.sqlitecommander.app.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afstd.sqlitecommander.app.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

/**
 * Created by pedja on 21.1.16..
 */
public class FragmentCloud extends Fragment implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener
{
    public static FragmentCloud newInstance()
    {
        FragmentCloud fragment = new FragmentCloud();
        return fragment;
    }

    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView, tvLoginWarning;
    private SignInButton signInButton;
    private static final int RC_SIGN_IN = 9001;
    private ProgressDialog mProgressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_cloud, container, false);

        mStatusTextView = (TextView) view.findViewById(R.id.header);
        tvLoginWarning = (TextView) view.findViewById(R.id.tvLoginWarning);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("770914414372-2923i5l0agg2cjjj20ppo4m7frm2mref.apps.googleusercontent.com")
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton = (SignInButton) view.findViewById(R.id.btnLogin);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(this);

        return view;
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
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone())
        {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        }
        else
        {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>()
            {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult)
                {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
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
        if (result.isSuccess())
        {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            updateUI(true, acct);
            //new ATLogin(this, acct).execute();
        }
        else
        {
            // Signed out, show unauthenticated UI.
            updateUI(false, null);
        }
    }

    private void signIn()
    {
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

    private void updateUI(boolean signedIn, GoogleSignInAccount acct)
    {
        if (signedIn)
        {
            mStatusTextView.setText(Html.fromHtml(getString(R.string.signed_in_fmt, acct.getEmail())));
            signInButton.setVisibility(View.GONE);
            tvLoginWarning.setVisibility(View.GONE);
        }
        else
        {
            mStatusTextView.setText(R.string.signed_out);
            signInButton.setVisibility(View.VISIBLE);
            tvLoginWarning.setVisibility(View.VISIBLE);
        }
    }

    /*private static class ATLogin extends AsyncTask<String, Void, Boolean>
    {
        private WeakReference<FragmentCloud> fragment;
        private GoogleSignInAccount account;

        public ATLogin(FragmentCloud fragment, GoogleSignInAccount account)
        {
            this.fragment = new WeakReference<>(fragment);
            this.account = account;
        }

        @Override
        protected Boolean doInBackground(String... params)
        {
            if(fragment.get() == null)return false;
            String token;
            try
            {
                token = GoogleAuthUtil.getToken(fragment.get().getActivity(), account.getEmail(), "https://www.googleapis.com/auth/userinfo.email");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (UserRecoverableAuthException userAuthEx)
            {
                // Start the user recoverable action using the intent returned by
                // getIntent()
                startActivityForResult(userAuthEx.getIntent(), REQUEST_CODE_GOOGLE_LOGIN);
            }
            catch (GoogleAuthException e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }*/

}
