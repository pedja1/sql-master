package com.afstd.sqlitecommander.app;


import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.af.androidutility.lib.AndroidUtility;
import com.afstd.sqlcmd.SQLiteCMDDefault;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;
import com.afstd.sqlitecommander.app.sqlite.SQLiteCMDRoot;
import com.afstd.sqlitecommander.app.su.ShellInstance;

import java.io.File;
import java.util.List;
import java.util.UUID;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by pedja on 17.1.16..
 */
public class SQLiteCMDActivity extends SQLCMDActivity
{
    public static final String INTENT_EXTRA_PATH = "path";
    public static final String INTENT_EXTRA_VERIFY_DATABASE = "verify";
    private File databaseFile;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        String path = getIntent().getStringExtra(INTENT_EXTRA_PATH);
        databaseFile = new File(path);

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
                    if (output != null && output.size() >= 1 && AndroidUtility.parseInt(output.get(0).trim(), 0) == 1)
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

    @Override
    protected void setup()
    {
        createSQLCMD();
        super.setup();
    }

    @Override
    protected CharSequence title()
    {
        return databaseFile.getName();
    }

    @Override
    protected CharSequence subtitle()
    {
        return databaseFile.getAbsolutePath();
    }

    @Override
    protected void createSQLCMD()
    {
        if (ShellInstance.getInstance().isSu())
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

        entry = DatabaseEntry.findWithUri(databaseFile.getAbsolutePath());
        if (entry == null)
        {
            entry = new DatabaseEntry();
            entry.id = UUID.randomUUID().toString();
        }
        entry.databaseUri = databaseFile.getAbsolutePath();
        entry.accessed = System.currentTimeMillis();
        entry.type = DatabaseEntry.TYPE_SQLITE;
        DatabaseManager.getInstance().insertDatabaseEntry(entry);
    }

    public static void start(Activity activity, String path, boolean verifyDatabase)
    {
        activity.startActivity(new Intent(activity, SQLiteCMDActivity.class)
                .putExtra(INTENT_EXTRA_PATH, path)
                .putExtra(INTENT_EXTRA_VERIFY_DATABASE, verifyDatabase));
    }
}
