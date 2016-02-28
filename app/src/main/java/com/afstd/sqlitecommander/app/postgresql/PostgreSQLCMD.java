package com.afstd.sqlitecommander.app.postgresql;

import com.afstd.sqlitecommander.app.JDBCSQLCMD;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pedja on 27.1.16..
 */
public class PostgreSQLCMD extends JDBCSQLCMD
{
    private static List<String> CONNECTION_ERROR_CODES = new ArrayList<>(Arrays.asList(new String[]{
            "08000", "08003", "08006", "08001", "08004", "08007", "08P01"}));
    public static final String JDBC_DRIVER = "org.postgresql.Driver";
    public static final int DEFAULT_PORT = 5432;

    public PostgreSQLCMD(DatabaseEntry entry, OnJDBCSQLConnectListener listener)
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
        return "postgresql";
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
        return String.format("jdbc:%s://%s:%d/%s", driver,
                host, port, databaseName);
    }
}
