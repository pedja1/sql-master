package com.afstd.sqlitecommander.app.mysql;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.NonNull;

import com.afstd.sqlcmd.SQLCMD;
import com.afstd.sqlitecommander.app.App;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.utility.SettingsManager;
import com.mysql.jdbc.Messages;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by pedja on 27.1.16..
 */
public class MySQLCMD extends SQLCMD
{
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final int MYSQL_DEFAULT_PORT = 3306;

    private MySQLThread mThread;
    private boolean mStarted;

    public MySQLCMD(DatabaseEntry entry, OnMySQLConnectListener listener)
    {
        mThread = new MySQLThread(entry, listener);
    }

    public void start()
    {
        mThread.start();
        mStarted = true;
    }

    public void stop()
    {
        mThread.quit();
    }

    @Override
    public void executeSql(String sql, OnResultListener listener)
    {
        if(!mStarted)
            throw new IllegalStateException("You must call start() first");
        if(mThread.mQuit)
            return;
        MySQLThread.Query query = new MySQLThread.Query(sql, listener);
        mThread.mQueue.add(query);
    }

    private static class MySQLThread extends Thread
    {
        private Connection conn = null;
        private boolean mQuit;
        private BlockingQueue<Query> mQueue;
        @NonNull
        private DatabaseEntry databaseEntry;
        @NonNull
        private OnMySQLConnectListener listener;
        private Handler mainThreadHandler;

        /**
         * */
        MySQLThread(@NonNull DatabaseEntry databaseEntry, @NonNull OnMySQLConnectListener listener)
        {
            mQueue = new LinkedBlockingQueue<>();
            this.databaseEntry = databaseEntry;
            this.listener = listener;
            mainThreadHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void run()
        {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            try
            {
                Class.forName(JDBC_DRIVER);
                int port = databaseEntry.databasePort <= 0 ? MYSQL_DEFAULT_PORT : databaseEntry.databasePort;
                conn = DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s",
                        databaseEntry.databaseUri, port, databaseEntry.databaseName),
                        databaseEntry.databaseUsername, databaseEntry.databasePassword);
            }
            catch (SQLException | ClassNotFoundException e)
            {
                if(SettingsManager.DEBUG())e.printStackTrace();
                mainThreadHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        listener.onConnectionFailed(e instanceof SQLException ? ((SQLException)e).getErrorCode() : -1, e.getMessage());
                    }
                });
                return;
            }

            mainThreadHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    listener.onConnected();
                }
            });
            //connected successfully
            while (true)
            {
                final Query query;
                try
                {
                    // Take a request from the queue.
                    query = mQueue.take();
                }
                catch (InterruptedException e)
                {
                    // We may have been interrupted because it was time to quit.
                    if (mQuit)
                    {
                        return;
                    }
                    continue;
                }
                try
                {
                    Statement st = conn.createStatement();

                    try
                    {
                        ResultSet rs = st.executeQuery(query.query);
                        ResultSetMetaData rsmd = rs.getMetaData();

                        final List<List<KeyValuePair>> result = new ArrayList<>();

                        while (rs.next())
                        {
                            List<KeyValuePair> row = new ArrayList<>();
                            for(int i = 1; i <= rsmd.getColumnCount(); i++)
                            {
                                KeyValuePair keyValuePair = new KeyValuePair(rsmd.getColumnName(i), rs.getString(i));
                                row.add(keyValuePair);
                            }
                            result.add(row);
                        }
                        mainThreadHandler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                query.listener.onResult(true, result, null);
                            }
                        });
                    }
                    catch (SQLException e)
                    {
                        if(Messages.getString("Statement.57").equals(e.getMessage()))
                        {
                            final int rowsAffected = st.executeUpdate(query.query);
                            mainThreadHandler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    query.listener.onResult(true, null, App.get().getString(R.string.query_ok, rowsAffected));
                                }
                            });
                        }
                        else
                        {
                            throw e;
                        }
                    }

                }
                catch (final SQLException e)
                {
                    if(SettingsManager.DEBUG())e.printStackTrace();
                    //if code is <= 2000 it means its client error, connection error
                    if (e.getErrorCode() >= 2000)
                    {
                        if(SettingsManager.DEBUG())e.printStackTrace();
                        mainThreadHandler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                listener.onConnectionFailed(e.getErrorCode(), e.getMessage());
                            }
                        });
                        mQuit = true;
                        return;
                    }
                    else
                    {
                        mainThreadHandler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                query.listener.onResult(false, null, e.getMessage());
                            }
                        });
                    }
                }
            }
        }

        void quit()
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    if(SettingsManager.DEBUG())e.printStackTrace();
                }
            }
            mQuit = false;
            interrupt();
        }

        private static class Query
        {
            @NonNull
            String query;
            @NonNull
            OnResultListener listener;

            Query(@NonNull String query, @NonNull OnResultListener listener)
            {
                this.query = query;
                this.listener = listener;
            }
        }
    }

    public interface OnMySQLConnectListener
    {
        void onConnected();
        /**TODO give me reason*/
        void onConnectionFailed(int error, String errorMessage);
    }
}
