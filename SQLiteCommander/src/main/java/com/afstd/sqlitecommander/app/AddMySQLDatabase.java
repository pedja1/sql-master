package com.afstd.sqlitecommander.app;

import com.afstd.sqlitecommander.app.model.DatabaseEntry;

/**
 * Created by pedja on 18.2.16..
 */
public class AddMySQLDatabase extends AddSQLDatabaseActivity
{
    @Override
    protected int getDefaultDatabasePort()
    {
        return DatabaseEntry.MYSQL_DEFAULT_PORT;
    }

    @Override
    protected String getDatabaseType()
    {
        return DatabaseEntry.TYPE_MYSQL;
    }
}
