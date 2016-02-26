package com.afstd.sqlitecommander.app.mysql;

import com.afstd.sqlitecommander.app.JDBCSQLCMD;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;

import java.sql.SQLException;

/**
 * Created by pedja on 27.1.16..
 */
public class MySQLCMD extends JDBCSQLCMD
{
    public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    public static final int DEFAULT_PORT = 3306;

    public MySQLCMD(DatabaseEntry entry, OnJDBCSQLConnectListener listener)
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
        return "mysql";
    }

    @Override
    protected int getDefaultPort()
    {
        return DEFAULT_PORT;
    }

    @Override
    protected boolean isConnectionError(SQLException e)
    {
        return e.getErrorCode() >= 2000;
    }
}
