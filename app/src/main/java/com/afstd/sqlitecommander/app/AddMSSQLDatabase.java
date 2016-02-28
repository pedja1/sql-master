package com.afstd.sqlitecommander.app;

import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.mssql.MSSQLCMD;
import com.afstd.sqlitecommander.app.utility.SettingsManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by pedja on 18.2.16..
 */
public class AddMSSQLDatabase extends AddSQLDatabaseActivity
{
    @Override
    protected int getDefaultDatabasePort()
    {
        return MSSQLCMD.DEFAULT_PORT;
    }

    @Override
    protected String getDatabaseType()
    {
        return DatabaseEntry.TYPE_MSSQL;
    }

    @Override
    protected boolean testConnection(DatabaseEntry databaseEntry)
    {
        try
        {
            Class.forName(MSSQLCMD.JDBC_DRIVER);
            int port = databaseEntry.databasePort <= 0 ? MSSQLCMD.DEFAULT_PORT : databaseEntry.databasePort;
            Connection conn = DriverManager.getConnection(String.format("jdbc:sqlserver://%s:%d;databaseName=%s",
                    databaseEntry.databaseUri, port, databaseEntry.databaseName),
                    databaseEntry.databaseUsername, databaseEntry.databasePassword);
            conn.close();
        }
        catch (ClassNotFoundException | SQLException e)
        {
            if(SettingsManager.DEBUG())e.printStackTrace();
            return false;
        }
        return true;
    }
}
