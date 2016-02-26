package com.afstd.sqlitecommander.app;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.afstd.sqlitecommander.app.mssql.MSSQLCMD;

/**
 * Created by pedja on 17.1.16..
 */
public class MSSQLCMDActivity extends SQLCMDActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(!setDatabaseFromIntent())
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
        sqlcmd = new MSSQLCMD(entry, new ConnectionListener());
        ((MSSQLCMD)sqlcmd).start();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(sqlcmd != null)
            ((MSSQLCMD)sqlcmd).stop();
    }

    public static void start(Activity activity, String id)
    {
        activity.startActivity(new Intent(activity, MSSQLCMDActivity.class)
                .putExtra(INTENT_EXTRA_ID, id));
    }
}
