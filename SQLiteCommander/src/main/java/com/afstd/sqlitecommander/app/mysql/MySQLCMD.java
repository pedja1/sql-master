package com.afstd.sqlitecommander.app.mysql;

import com.afstd.sqlcmd.SQLCMD;
import com.android.volley.misc.AsyncTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by pedja on 27.1.16..
 */
public class MySQLCMD extends SQLCMD
{
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    Connection conn = null;

    public MySQLCMD()
    {
        try
        {
            Class.forName(JDBC_DRIVER);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void connect()
    {
        new ATConnect().execute();
    }

    @Override
    public void executeSql(String sql, OnResultListener listener)
    {
        try
        {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from scores limit 1");
            ResultSetMetaData rsmd = rs.getMetaData();
            System.out.println(rs);
            System.out.println(rsmd);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        new MySQLCMD().executeSql(null, null);
    }

    public class ATConnect extends AsyncTask<String, Void, Boolean>
    {
        private String error;
        @Override
        protected Boolean doInBackground(String... params)
        {
            try
            {
                conn = DriverManager.getConnection("jdbc:mysql://5.9.43.104:3306/linpack", "linpack", "Jop9~6c9");
                executeSql(null, null);
            }
            catch (SQLException e)
            {
                error = e.getMessage();
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean)
        {

        }

        @Override
        protected void onPreExecute()
        {

        }
    }

}
