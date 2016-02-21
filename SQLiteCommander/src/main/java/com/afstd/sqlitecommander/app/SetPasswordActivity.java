package com.afstd.sqlitecommander.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.af.androidutility.lib.AndroidUtility;
import com.afstd.sqlitecommander.app.su.ShellInstance;
import com.afstd.sqlitecommander.app.utility.AesCbcWithIntegrity;
import com.afstd.sqlitecommander.app.utility.SettingsManager;

import java.security.GeneralSecurityException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pedja on 20.2.16..
 */
public class SetPasswordActivity extends AppCompatActivity
{
    private static final int MIN_PASSWORD_LENGTH = 8;
    private EditText etPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        View tvRootWarning = findViewById(R.id.tvRootWarning);
        tvRootWarning.setVisibility(ShellInstance.getInstance().isSu() ? View.VISIBLE : View.GONE);

        etPassword = (EditText) findViewById(R.id.etPassword);
    }

    public void onClick(View view)
    {
        String password = etPassword.getText().toString();
        if(password.length() < MIN_PASSWORD_LENGTH)
        {
            etPassword.setError(getString(R.string.pasword_short));
            etPassword.requestFocus();
            return;
        }
        Pattern digit = Pattern.compile("\\d");
        Matcher matcher = digit.matcher(password);

        if(!matcher.find())
        {
            etPassword.setError(getString(R.string.password_number));
            etPassword.requestFocus();
            return;
        }

        Pattern uppercaseLetter = Pattern.compile("\\p{javaUpperCase}+");
        matcher = uppercaseLetter.matcher(password);

        if(!matcher.find())
        {
            etPassword.setError(getString(R.string.password_uppercase));
            etPassword.requestFocus();
            return;
        }

        Pattern lowecase = Pattern.compile("\\p{javaLowerCase}+");
        matcher = lowecase.matcher(password);

        if(!matcher.find())
        {
            etPassword.setError(getString(R.string.password_lowercase));
            etPassword.requestFocus();
            return;
        }

        try
        {
            AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.generateKeyFromPassword(etPassword.getText().toString(), AesCbcWithIntegrity.generateSalt());
            String keyString = AesCbcWithIntegrity.keyString(keys);
            SettingsManager.setEc(keyString);
            setResult(RESULT_OK);
            finish();
        }
        catch (GeneralSecurityException e)
        {
            if(SettingsManager.DEBUG())e.printStackTrace();
            AndroidUtility.showToast(this, e.getMessage());
        }

    }
}
