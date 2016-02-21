package com.afstd.sqlitecommander.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.af.androidutility.lib.AndroidUtility;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;

import java.util.UUID;


/**
 * Created by pedja on 18.2.16..
 */
public abstract class AddSQLDatabaseActivity extends AppCompatActivity
{
    public static final String INTENT_EXTRA_DATABASE_ID = "database_id";

    protected EditText etDatabaseUrl;
    protected EditText etPort;
    protected EditText etName;
    protected EditText etUsername;
    protected EditText etPassword;

    private DatabaseEntry entry;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sql_connection);

        String id = getIntent().getStringExtra(INTENT_EXTRA_DATABASE_ID);
        if(id != null)
            entry = DatabaseManager.getInstance().getDatabaseEntry("SELECT * FROM _database WHERE id = ?", new String[]{id});

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        etDatabaseUrl = (EditText) findViewById(R.id.etDatabaseUrl);
        etPort = (EditText) findViewById(R.id.etPort);
        etName = (EditText) findViewById(R.id.etName);

        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);

        if(entry != null)
        {
            etDatabaseUrl.setText(entry.databaseUri);
            etPort.setText(entry.databasePort > 0 ? String.valueOf(entry.databasePort) : null);
            etName.setText(entry.databaseName);
            etUsername.setText(entry.databaseUsername);
            etPassword.setText(entry.databasePassword);
        }
    }

    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btnCancel:
                finish();
                break;
            case R.id.btnSave:
                save();
                break;
        }
    }

    private void save()
    {
        if(TextUtils.isEmpty(etName.getText()))
        {
            etName.setError(getString(R.string.database_name_is_required));
            etName.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(etDatabaseUrl.getText()))
        {
            etDatabaseUrl.setError(getString(R.string.database_server_required));
            etDatabaseUrl.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(etUsername.getText()))
        {
            etUsername.setError(getString(R.string.username_required));
            etUsername.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(etPassword.getText()))
        {
            etPassword.setError(getString(R.string.password_is_required));
            etPassword.requestFocus();
            return;
        }

        if(entry == null)
        {
            entry = new DatabaseEntry();
            entry.id = UUID.randomUUID().toString();
        }
        entry.databaseName = etName.getText().toString();
        entry.databaseUri = etDatabaseUrl.getText().toString();
        entry.databasePort = AndroidUtility.parseInt(etPort.getText().toString(), getDefaultDatabasePort());
        entry.type = getDatabaseType();
        entry.databaseUsername = etUsername.getText().toString();
        entry.databasePassword = etPassword.getText().toString();

        DatabaseManager.getInstance().insertDatabaseEntry(entry);

        Intent data = new Intent();
        data.putExtra(INTENT_EXTRA_DATABASE_ID, entry.id);
        setResult(RESULT_OK, data);
        finish();
    }

    protected abstract int getDefaultDatabasePort();
    protected abstract String getDatabaseType();

    public static void start(@NonNull Activity activity, Class<? extends AddSQLDatabaseActivity> _class, @Nullable String databaseId, int requestCode)
    {
        Intent intent = new Intent(activity, _class);
        intent.putExtra(INTENT_EXTRA_DATABASE_ID, databaseId);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void start(@NonNull Fragment fragment, Class<? extends AddSQLDatabaseActivity> _class, @Nullable String databaseId, int requestCode)
    {
        Intent intent = new Intent(fragment.getActivity(), _class);
        intent.putExtra(INTENT_EXTRA_DATABASE_ID, databaseId);
        fragment.startActivityForResult(intent, requestCode);
    }
}
