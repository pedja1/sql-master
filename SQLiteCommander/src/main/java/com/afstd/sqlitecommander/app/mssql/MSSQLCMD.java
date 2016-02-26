package com.afstd.sqlitecommander.app.mssql;

import com.afstd.sqlitecommander.app.JDBCSQLCMD;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pedja on 27.1.16..
 */
public class MSSQLCMD extends JDBCSQLCMD
{
    private static List<String> CONNECTION_ERROR_CODES = new ArrayList<>(Arrays.asList(new String[]{
            "08001", "08002", "08003", "08004", "08007", "08S07"}));

    public static final String JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static final int DEFAULT_PORT = 1433;

    public MSSQLCMD(DatabaseEntry entry, OnJDBCSQLConnectListener listener)
    {
        super(entry, listener);
    }

    @Override
    protected String getDriverClassName()
    {
        return JDBC_DRIVER;
    }

    @Override
    protected String getDriverName()
    {
        return "sqlserver";
    }

    @Override
    protected int getDefaultPort()
    {
        return DEFAULT_PORT;
    }

    @Override
    protected boolean isConnectionError(SQLException e)
    {
        return CONNECTION_ERROR_CODES.contains(e.getSQLState());
    }

    @Override
    protected String getConnectionUrl(String driver, String host, int port, String databaseName)
    {
        return String.format("jdbc:%s://%s:%d;databaseName=%s", driver,
                host, port, databaseName);
    }
}
