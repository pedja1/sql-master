package com.afstd.sqlitecommander.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
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
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;
import com.afstd.sqlitecommander.app.utility.SQLTextHighlighter;
import com.afstd.sqlitecommander.app.utility.SettingsManager;

import java.util.List;

/**
 * Created by pedja on 19.2.16..
 */
public abstract class SQLCMDActivity extends AppCompatActivity
{
    public static final String INTENT_EXTRA_ID = "id";
    protected DatabaseEntry entry;
    protected TextView tvError;
    protected ProgressBar pbLoading;
    protected SQLGridView sqlGridView;
    protected AutoCompleteTextView etSqlCmd;
    protected Button btnExecute;

    protected SQLCMD sqlcmd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sqlcmd);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvError = (TextView) findViewById(R.id.tvError);
        pbLoading = (ProgressBar) findViewById(R.id.pbLoading);

        sqlGridView = (SQLGridView) findViewById(R.id.sqlView);
        etSqlCmd = (AutoCompleteTextView) findViewById(R.id.etSqlCmd);
        btnExecute = (Button) findViewById(R.id.btnExecute);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        setTitle(title());
        getSupportActionBar().setSubtitle(subtitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.sql_cmd_menu, menu);
        menu.findItem(R.id.action_favorite).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        if(entry != null)
        {
            MenuItem fav = menu.findItem(R.id.action_favorite);
            fav.setVisible(true);
            fav.setIcon(entry.isFavorite ? R.drawable.ic_action_favorite_white : R.drawable.ic_action_favorite_outline_white);
        }
        return super.onPrepareOptionsMenu(menu);
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
            case R.id.action_favorite:
                entry.isFavorite = !entry.isFavorite;
                DatabaseManager.getInstance().insertDatabaseEntry(entry);
                invalidateOptionsMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setup()
    {
        invalidateOptionsMenu();
        pbLoading.setVisibility(View.GONE);

        etSqlCmd.setEnabled(true);
        btnExecute.setEnabled(true);

        SQLTextHighlighter highlighter = new SQLTextHighlighter(etSqlCmd, SettingsManager.getSyntaxHighlightTheme());
        highlighter.highlightTextChanges();

        String query = "SELECT * FROM query_history";
        final List<QueryHistory> list = DatabaseManager.getInstance().getQueryHistory(query, null);

        ArrayAdapter<QueryHistory> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);

        etSqlCmd.setAdapter(mAdapter);

        btnExecute.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatabaseManager.getInstance().insertQueryHistory(etSqlCmd.getText().toString());
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

    protected boolean setDatabaseFromIntent()
    {
        String id = getIntent().getStringExtra(INTENT_EXTRA_ID);
        if (TextUtils.isEmpty(id))
        {
            fail();
            return false;
        }

        String query = "SELECT * FROM _database WHERE id = ?";
        entry = DatabaseManager.getInstance().getDatabaseEntry(query, new String[]{id});

        if (entry == null)
        {
            fail();
            return false;
        }
        return true;
    }


    private void fail()
    {
        //TODO show error
        finish();
    }

    protected class ConnectionListener implements JDBCSQLCMD.OnJDBCSQLConnectListener
    {
        @Override
        public void onConnected()
        {
            setup();
            SQLCMDActivity.this.onConnected();
        }

        @Override
        public void onConnectionFailed(int error, String message)
        {
            AndroidUtility.showToast(SQLCMDActivity.this, message);
            finish();
            SQLCMDActivity.this.onConnectionFailed(error, message);
        }

    }

    protected void onConnected()
    {
        //do nothing
    }

    public void onConnectionFailed(int error, String message)
    {
        //do nothing
    }

    protected abstract CharSequence title();
    protected abstract CharSequence subtitle();
    protected abstract void createSQLCMD();
}
