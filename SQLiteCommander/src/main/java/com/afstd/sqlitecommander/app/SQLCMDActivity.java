package com.afstd.sqlitecommander.app;


import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.af.androidutility.lib.AndroidUtility;
import com.afstd.sqlcmd.SQLCMD;
import com.afstd.sqlcmd.SQLCMDException;
import com.afstd.sqlcmd.SQLGridView;

import java.io.File;

/**
 * Created by pedja on 17.1.16..
 */
public class SQLCMDActivity extends AppCompatActivity
{
    public static final String INTENT_EXTRA_PATH = "path";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sqlcmd);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String path = getIntent().getStringExtra(INTENT_EXTRA_PATH);
        File databaseFile = new File(path);

        setTitle(databaseFile.getName());
        getSupportActionBar().setSubtitle(databaseFile.getAbsolutePath());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final TextView tvError = (TextView) findViewById(R.id.tvError);

        if(!databaseFile.canRead())
        {
            AndroidUtility.showToast(this, R.string.cant_read_database_file);
            finish();
            return;
        }

        if(!databaseFile.canWrite())
        {
            AndroidUtility.showToast(this, R.string.cant_write_database_file);
        }

        SQLiteDatabase database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);

        final SQLCMD sqlcmd = new SQLCMD(database);

        final SQLGridView sqlGridView = (SQLGridView) findViewById(R.id.sqlView);
        final EditText etSqlCmd = (EditText) findViewById(R.id.etSqlCmd);

        Button btnExecute = (Button) findViewById(R.id.btnExecute);
        btnExecute.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                tvError.setVisibility(View.GONE);
                try
                {
                    sqlGridView.setData(sqlcmd.executeSql(etSqlCmd.getText().toString()));
                }
                catch (SQLCMDException e)
                {
                    tvError.setText(e.getMessage());
                    tvError.setVisibility(View.VISIBLE);
                }
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

    public static void start(Activity activity, String path)
    {
        activity.startActivity(new Intent(activity, SQLCMDActivity.class).putExtra(INTENT_EXTRA_PATH, path));
    }
}
