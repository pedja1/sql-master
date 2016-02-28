package com.afstd.sqlitecommander.app;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.afstd.sqlitecommander.app.postgresql.PostgreSQLCMD;

/**
 * Created by pedja on 17.1.16..
 */
public class PostgreSQLCMDActivity extends SQLCMDActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (!setDatabaseFromIntent())
            return;
        createSQLCMD();
    }

    @Override
    protected CharSequence title()
    {
        return entry.databaseName;
    }

    @Override
    protected CharSequence subtitle()
    {
        return entry.databaseUri;
    }

    @Override
    protected void createSQLCMD()
    {
            sqlcmd = new PostgreSQLCMD(entry, new ConnectionListener());
        ((PostgreSQLCMD) sqlcmd).start();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (sqlcmd != null)
            ((PostgreSQLCMD) sqlcmd).stop();
    }

    public static void start(Activity activity, String id)
    {
        activity.startActivity(new Intent(activity, PostgreSQLCMDActivity.class)
                .putExtra(INTENT_EXTRA_ID, id));
    }
}
