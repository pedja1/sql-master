package com.afstd.sqlitecommander.app;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.af.androidutility.lib.DisplayManager;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;
import com.android.volley.misc.AsyncTask;

import java.lang.ref.WeakReference;
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
        if (id != null)
            entry = DatabaseManager.getInstance().getDatabaseEntry("SELECT * FROM _database WHERE id = ?", new String[]{id});

        int width = ViewGroup.LayoutParams.MATCH_PARENT;

        DisplayManager displayManager = new DisplayManager(this);
        if (AndroidUtility.isLandscape(this))
        {
            //noinspection SuspiciousNameCombination
            width = displayManager.screenHeight;
        }
        else
        {
            if (AndroidUtility.isTablet(this))
            {
                width = (int) (displayManager.screenWidth * 0.8f);
            }
        }

        getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

        etDatabaseUrl = (EditText) findViewById(R.id.etDatabaseUrl);
        etPort = (EditText) findViewById(R.id.etPort);
        etName = (EditText) findViewById(R.id.etName);

        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);

        if (entry != null)
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
            case R.id.btnTest:
                if (!informationIsValid())
                    break;
                initDatabaseEntry();
                new ATTestConnection(this, entry).execute();
                break;
        }
    }

    private static class ATTestConnection extends AsyncTask<Void, Void, Boolean>
    {
        private WeakReference<AddSQLDatabaseActivity> reference;
        private ProgressDialog progressDialog;
        private DatabaseEntry entry;

        ATTestConnection(AddSQLDatabaseActivity activity, DatabaseEntry entry)
        {
            reference = new WeakReference<>(activity);
            this.entry = entry;
        }

        @Override
        protected void onPreExecute()
        {
            if (reference.get() == null)
                return;
            progressDialog = new ProgressDialog(reference.get());
            progressDialog.setMessage(reference.get().getString(R.string.please_wait));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            if (reference.get() == null)
                return null;
            return reference.get().testConnection(entry);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean)
        {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            if (reference.get() == null || aBoolean == null)
                return;
            AndroidUtility.showMessageAlertDialog(reference.get(), aBoolean ? R.string.connection_successfull : R.string.connection_failed, 0, null);
        }
    }

    private void save()
    {
        if (!informationIsValid())
            return;

        initDatabaseEntry();

        DatabaseManager.getInstance().insertDatabaseEntry(entry);

        Intent data = new Intent();
        data.putExtra(INTENT_EXTRA_DATABASE_ID, entry.id);
        setResult(RESULT_OK, data);
        finish();
    }

    private boolean informationIsValid()
    {
        if (TextUtils.isEmpty(etDatabaseUrl.getText()))
        {
            etDatabaseUrl.setError(getString(R.string.database_server_required));
            etDatabaseUrl.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etName.getText()))
        {
            etName.setError(getString(R.string.database_name_is_required));
            etName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etUsername.getText()))
        {
            etUsername.setError(getString(R.string.username_required));
            etUsername.requestFocus();
            return false;
        }
        /*if(TextUtils.isEmpty(etPassword.getText()))
        {
            etPassword.setError(getString(R.string.password_is_required));
            etPassword.requestFocus();
            return false;
        }*/
        return true;
    }

    private void initDatabaseEntry()
    {
        if (entry == null)
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
    }

    protected abstract int getDefaultDatabasePort();

    protected abstract String getDatabaseType();

    /**
     * This method is called on worker thread, you must return if connection is valid
     */
    protected abstract boolean testConnection(DatabaseEntry entry);

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
