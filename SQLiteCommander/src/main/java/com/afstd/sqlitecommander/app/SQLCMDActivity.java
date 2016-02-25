package com.afstd.sqlitecommander.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afstd.sqlcmd.SQLGridView;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;

/**
 * Created by pedja on 19.2.16..
 */
public class SQLCMDActivity extends AppCompatActivity
{
    protected DatabaseEntry entry;
    protected TextView tvError;
    protected ProgressBar pbLoading;
    protected SQLGridView sqlGridView;
    protected AutoCompleteTextView etSqlCmd;
    protected Button btnExecute;

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
}
