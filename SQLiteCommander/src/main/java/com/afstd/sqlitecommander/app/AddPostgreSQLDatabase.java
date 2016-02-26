package com.afstd.sqlitecommander.app;

import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.postgresql.PostgreSQLCMD;
import com.afstd.sqlitecommander.app.utility.SettingsManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Created by pedja on 18.2.16..
 */
public class AddPostgreSQLDatabase extends AddSQLDatabaseActivity
{
    @Override
    protected int getDefaultDatabasePort()
    {
        return PostgreSQLCMD.DEFAULT_PORT;
    }

    @Override
    protected String getDatabaseType()
    {
        return DatabaseEntry.TYPE_POSTGRESQL;
    }

    @Override
    protected boolean testConnection(DatabaseEntry databaseEntry)
    {
        try
        {
            Class.forName(PostgreSQLCMD.JDBC_DRIVER);
            int port = databaseEntry.databasePort <= 0 ? PostgreSQLCMD.DEFAULT_PORT : databaseEntry.databasePort;
            Connection conn = DriverManager.getConnection(String.format("jdbc:postgresql://%s:%d/%s",
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
