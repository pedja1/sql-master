package com.afstd.sqlitecommander.app;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.af.androidutility.lib.AndroidUtility;
import com.afstd.sqlcmd.SQLCMD;
import com.afstd.sqlcmd.SQLGridView;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.model.QueryHistory;
import com.afstd.sqlitecommander.app.mysql.MySQLCMD;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;

import java.util.List;

/**
 * Created by pedja on 17.1.16..
 */
public class MySQLCMDActivity extends AppCompatActivity
{
    public static final String INTENT_EXTRA_ID = "id";
    private TextView tvError;
    private ProgressBar pbLoading;
    private MySQLCMD sqlcmd;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        String id = getIntent().getStringExtra(INTENT_EXTRA_ID);
        if (TextUtils.isEmpty(id))
        {
            fail();
            return;
        }

        String query = "SELECT * FROM _database WHERE id = ?";
        DatabaseEntry database = DatabaseManager.getInstance().getDatabaseEntrie(query, new String[]{id});

        if (database == null)
        {
            fail();
            return;
        }

        setContentView(R.layout.activity_sqlcmd);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(database.databaseName);
        getSupportActionBar().setSubtitle(database.databaseUri);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvError = (TextView) findViewById(R.id.tvError);
        pbLoading = (ProgressBar) findViewById(R.id.pbLoading);

        sqlcmd = new MySQLCMD(database, new MySQLCMD.OnMySQLConnectListener()
        {
            @Override
            public void onConnected()
            {
                setup();
            }

            @Override
            public void onConnectionFailed(int error, String message)
            {
                ///TODO show error and also interface should return error message/code or something
                AndroidUtility.showToast(MySQLCMDActivity.this, message);
                finish();
            }
        });
        sqlcmd.start();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(sqlcmd != null)
            sqlcmd.stop();
    }

    private void fail()
    {
        //TODO show error
        finish();
    }

    private void setup()
    {
        pbLoading.setVisibility(View.GONE);

        final SQLGridView sqlGridView = (SQLGridView) findViewById(R.id.sqlView);
        final AutoCompleteTextView etSqlCmd = (AutoCompleteTextView) findViewById(R.id.etSqlCmd);

        String query = "SELECT * FROM query_history";
        final List<QueryHistory> list = DatabaseManager.getInstance().getQueryHistory(query, null);

        ArrayAdapter<QueryHistory> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);

        etSqlCmd.setAdapter(mAdapter);

        Button btnExecute = (Button) findViewById(R.id.btnExecute);
        //noinspection Duplicates
        btnExecute.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatabaseManager.getInstance().insertCommandHistory(etSqlCmd.getText().toString());
                list.add(new QueryHistory(etSqlCmd.getText().toString()));
                pbLoading.setVisibility(View.VISIBLE);
                tvError.setVisibility(View.GONE);

                sqlcmd.executeSql(etSqlCmd.getText().toString(), new SQLCMD.OnResultListener()
                {
                    @Override
                    public void onResult(boolean success, List<List<SQLCMD.KeyValuePair>> data, String error)
                    {
                        sqlGridView.setData(data);
                        if (error != null)
                        {
                            tvError.setText(error);
                            tvError.setVisibility(View.VISIBLE);
                        }
                        pbLoading.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void start(Activity activity, String id)
    {
        activity.startActivity(new Intent(activity, MySQLCMDActivity.class)
                .putExtra(INTENT_EXTRA_ID, id));
    }
}
