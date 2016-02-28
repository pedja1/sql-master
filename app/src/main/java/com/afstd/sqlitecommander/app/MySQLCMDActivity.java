package com.afstd.sqlitecommander.app;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.afstd.sqlitecommander.app.mysql.MySQLCMD;

/**
 * Created by pedja on 17.1.16..
 */
public class MySQLCMDActivity extends SQLCMDActivity
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
        sqlcmd = new MySQLCMD(entry, new ConnectionListener());
        ((MySQLCMD)sqlcmd).start();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(sqlcmd != null)
            ((MySQLCMD)sqlcmd).stop();
    }

    public static void start(Activity activity, String id)
    {
        activity.startActivity(new Intent(activity, MySQLCMDActivity.class)
                .putExtra(INTENT_EXTRA_ID, id));
    }
}
