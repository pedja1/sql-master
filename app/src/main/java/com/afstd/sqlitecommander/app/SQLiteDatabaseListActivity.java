package com.afstd.sqlitecommander.app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afstd.sqlitecommander.app.appmanager.App;
import com.afstd.sqlitecommander.app.filemanager.FMAdapter;
import com.afstd.sqlitecommander.app.filemanager.FMEntry;
import com.afstd.sqlitecommander.app.filemanager.FMUtils;
import com.afstd.sqlitecommander.app.su.ShellInstance;

import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by pedja on 24.1.16..
 */
public class SQLiteDatabaseListActivity extends AppCompatActivity
{
    public static final String INTENT_EXTRA_APP = "app";
    private FMAdapter mAdapter;
    public static final String SQLITE_FILE_HEADER_STRING = "SQLite format 3";

    private String path;
    private LinearLayout llLoading;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sqlite_database_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView lvDatabases = (ListView) findViewById(R.id.lvDatabases);

        llLoading = (LinearLayout) findViewById(R.id.llLoading);
        tvError = (TextView) findViewById(R.id.tvError);

        mAdapter = new FMAdapter(this, new ArrayList<FMEntry>());
        lvDatabases.setAdapter(mAdapter);

        App app = getIntent().getParcelableExtra(INTENT_EXTRA_APP);
        path = String.format("/data/data/%s/databases/", app.packageName);

        setTitle(app.appLabel);
        tvError.setText(getString(R.string.no_databases, app.appLabel));

        Shell.Interactive interactive = ShellInstance.getInstance().getShell();
        String ls = getApplicationInfo().nativeLibraryDir + "/libls.so";
        interactive.addCommand(String.format("%s -f \"%s\" %s", ls, SQLITE_FILE_HEADER_STRING, path), 0, new Shell.OnCommandResultListener()
        {
            @Override
            public void onCommandResult(int commandCode, int exitCode, List<String> output)
            {
                new ATParseOutput(output).execute();
            }
        });

        lvDatabases.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                SQLiteCMDActivity.start(SQLiteDatabaseListActivity.this, mAdapter.getItem(position).getPath(), false);
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

    private class ATParseOutput extends AsyncTask<String, Void, List<FMEntry>>
    {
        private List<String> files;

        ATParseOutput(List<String> files)
        {
            this.files = files;
        }

        @Override
        protected List<FMEntry> doInBackground(String... strings)
        {
            return FMUtils.parseLsOutput(path, files);
        }

        @Override
        protected void onPostExecute(List<FMEntry> e)
        {
            mAdapter.clear();
            mAdapter.addAll(e);
            mAdapter.notifyDataSetChanged();
            llLoading.setVisibility(View.GONE);
            tvError.setVisibility(e.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    public static void start(Activity activity, App app)
    {
        activity.startActivity(new Intent(activity, SQLiteDatabaseListActivity.class).putExtra(INTENT_EXTRA_APP, app));
    }

}
