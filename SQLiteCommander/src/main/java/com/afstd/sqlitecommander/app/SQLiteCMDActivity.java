package com.afstd.sqlitecommander.app;


import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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
import com.afstd.syntaxhighlight.Theme;
import com.afstd.syntaxhighlighter.SyntaxHighlighterParser;
import com.afstd.syntaxhighlighter.brush.BrushSql;
import com.afstd.syntaxhighlighter.theme.ThemeDjango;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        if (!shoudVerifyFile)
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
                    if (output != null && output.size() > 1 && AndroidUtility.parseInt(output.get(0).trim(), 0) == 1)
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
        if (Shell.SU.available())
        {
            sqlcmd = new SQLiteCMDRoot(databaseFile.getAbsolutePath());
        }
        else
        {
            if (!databaseFile.canRead())
            {
                AndroidUtility.showToast(this, R.string.cant_read_database_file);
                finish();
                return;
            }

            if (!databaseFile.canWrite())
            {
                AndroidUtility.showToast(this, R.string.cant_write_database_file);
            }
            SQLiteDatabase database = SQLiteDatabase.openDatabase(databaseFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
            sqlcmd = new SQLiteCMDDefault(database);
        }

        DatabaseEntry entry = DatabaseEntry.findWithUri(databaseFile.getAbsolutePath());
        if (entry == null)
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
        final Theme theme = new ThemeDjango();

        etSqlCmd.addTextChangedListener(new TextWatcher()
        {
            boolean calbackDisaabled = false;
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
                if(calbackDisaabled)
                    return;
                calbackDisaabled = true;
                //etSqlCmd.removeTextChangedListener(this);
                //TODO to many for loops for each key type, optimize
                List<ParseResult> results = parser.parse(null, s.toString());
                Map<String, List<ParseResult>> styleList = new HashMap<>();

                for (ParseResult parseResult : results)
                {
                    String styleKeysString = parseResult.getStyleKeysString();
                    List<ParseResult> _styleList = styleList.get(styleKeysString);
                    if (_styleList == null)
                    {
                        _styleList = new ArrayList<>();
                        styleList.put(styleKeysString, _styleList);
                    }
                    _styleList.add(parseResult);
                }

                //s.clearSpans();
                clearSpans(s);
                //SpannableStringBuilder builder = new SpannableStringBuilder(s.toString());
                for (String key : styleList.keySet())
                {
                    List<ParseResult> posList = styleList.get(key);

                    for (ParseResult pos : posList)
                    {
                        List<Object> spans = theme.getStyle(key).newSpans();
                        for(Object span : spans)
                        {
                            s.setSpan(span, pos.getOffset(), pos.getOffset() + pos.getLength(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
                //etSqlCmd.setText(builder);
                calbackDisaabled = false;
                //etSqlCmd.addTextChangedListener(this);
                System.out.println("afterTextChanged");
            }

            private void clearSpans( Editable e )
            {
                // remove foreground color spans
                {
                    ForegroundColorSpan spans[] = e.getSpans(
                            0,
                            e.length(),
                            ForegroundColorSpan.class );

                    for( int n = spans.length; n-- > 0; )
                        e.removeSpan( spans[n] );
                }

                // remove background color spans
                {
                    BackgroundColorSpan spans[] = e.getSpans(
                            0,
                            e.length(),
                            BackgroundColorSpan.class );

                    for( int n = spans.length; n-- > 0; )
                        e.removeSpan( spans[n] );
                }
                // remove style spans
                {
                    StyleSpan spans[] = e.getSpans(
                            0,
                            e.length(),
                            StyleSpan.class );

                    for( int n = spans.length; n-- > 0; )
                        e.removeSpan( spans[n] );
                }
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

    public static void start(Activity activity, String path, boolean verifyDatabase)
    {
        activity.startActivity(new Intent(activity, SQLiteCMDActivity.class)
                .putExtra(INTENT_EXTRA_PATH, path)
                .putExtra(INTENT_EXTRA_VERIFY_DATABASE, verifyDatabase));
    }
}
