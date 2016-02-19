package com.afstd.sqlitecommander.app;


import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.afstd.sqlcmd.SQLiteCMDDefault;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.model.QueryHistory;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;
import com.afstd.sqlitecommander.app.sqlite.SQLiteCMDRoot;
import com.afstd.sqlitecommander.app.su.ShellInstance;
import com.afstd.syntaxhighlight.ParseResult;
import com.afstd.syntaxhighlighter.SyntaxHighlighterParser;
import com.afstd.syntaxhighlighter.brush.BrushSql;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by pedja on 17.1.16..
 */
public class SQLiteCMDActivity extends AppCompatActivity
{
    public static final String INTENT_EXTRA_PATH = "path";
    public static final String INTENT_EXTRA_VERIFY_DATABASE = "verify";
    private File databaseFile;
    private TextView tvError;
    private ProgressBar pbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sqlcmd);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String path = getIntent().getStringExtra(INTENT_EXTRA_PATH);
        databaseFile = new File(path);

        setTitle(databaseFile.getName());
        getSupportActionBar().setSubtitle(databaseFile.getAbsolutePath());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvError = (TextView) findViewById(R.id.tvError);
        pbLoading = (ProgressBar) findViewById(R.id.pbLoading);

        boolean shoudVerifyFile = getIntent().getBooleanExtra(INTENT_EXTRA_VERIFY_DATABASE, true);

        if(!shoudVerifyFile)
        {
            setup();
        }
        else
        {
            ShellInstance su = ShellInstance.getInstance();
            String ver = getApplicationInfo().nativeLibraryDir + "/libsqlite_verify.so";
            su.getShell().addCommand(ver + " " + databaseFile.getAbsolutePath(), 0, new Shell.OnCommandResultListener()
            {
                @Override
                public void onCommandResult(int commandCode, int exitCode, List<String> output)
                {
                    if(output != null && output.size() > 1 && AndroidUtility.parseInt(output.get(0).trim(), 0) == 1)
                    {
                        setup();
                    }
                    else
                    {
                        AndroidUtility.showToast(SQLiteCMDActivity.this, R.string.failed_to_verify_database);
                        finish();
                    }
                }
            });
        }

    }

    private void setup()
    {
        pbLoading.setVisibility(View.GONE);

        final SQLCMD sqlcmd;
        if(Shell.SU.available())
        {
            sqlcmd = new SQLiteCMDRoot(databaseFile.getAbsolutePath());
        }
        else
        {
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
            SQLiteDatabase database = SQLiteDatabase.openDatabase(databaseFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
            sqlcmd = new SQLiteCMDDefault(database);
        }

        DatabaseEntry entry = DatabaseEntry.findWithUri(databaseFile.getAbsolutePath());
        if(entry == null)
        {
            entry = new DatabaseEntry();
            entry.id = UUID.randomUUID().toString();
        }
        entry.databaseUri = databaseFile.getAbsolutePath();
        entry.accessed = System.currentTimeMillis();
        entry.type = DatabaseEntry.TYPE_SQLITE;
        DatabaseManager.getInstance().insertDatabaseEntry(entry);

        final SQLGridView sqlGridView = (SQLGridView) findViewById(R.id.sqlView);
        final AutoCompleteTextView etSqlCmd = (AutoCompleteTextView) findViewById(R.id.etSqlCmd);

        final SyntaxHighlighterParser parser = new SyntaxHighlighterParser(new BrushSql());

        etSqlCmd.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                List<ParseResult> results = parser.parse(null, s.toString());
                System.out.println(Arrays.toString(results.toArray()));
            }
        });

        String query = "SELECT * FROM query_history";
        final List<QueryHistory> list = DatabaseManager.getInstance().getQueryHistory(query, null);

        ArrayAdapter<QueryHistory> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);

        etSqlCmd.setAdapter(mAdapter);

        Button btnExecute = (Button) findViewById(R.id.btnExecute);
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
                        if(error != null)
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

    public static void start(Activity activity, String path, boolean verifyDatabase)
    {
        activity.startActivity(new Intent(activity, SQLiteCMDActivity.class)
                .putExtra(INTENT_EXTRA_PATH, path)
                .putExtra(INTENT_EXTRA_VERIFY_DATABASE, verifyDatabase));
    }
}
