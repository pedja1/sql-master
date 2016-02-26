package com.afstd.sqlitecommander.app;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.NonNull;

import com.afstd.sqlcmd.SQLCMD;
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
public abstract class JDBCSQLCMD extends SQLCMD
{
    private JDBCConnectionThread mThread;
    private boolean mStarted;

    public JDBCSQLCMD(DatabaseEntry entry, OnJDBCSQLConnectListener listener)
    {
        mThread = new JDBCConnectionThread(entry, listener);
    }

    public final void start()
    {
        mThread.start();
        mStarted = true;
    }

    public final void stop()
    {
        mThread.quit();
    }

    @Override
    public final void executeSql(String sql, OnResultListener listener)
    {
        if(!mStarted)
            throw new IllegalStateException("You must call start() first");
        if(mThread.mQuit)
            return;
        Query query = new Query(sql, listener);
        mThread.mQueue.add(query);
    }

    private class JDBCConnectionThread extends Thread
    {
        private Connection conn = null;
        private boolean mQuit;
        private BlockingQueue<Query> mQueue;
        @NonNull
        private DatabaseEntry databaseEntry;
        @NonNull
        private OnJDBCSQLConnectListener listener;
        private Handler mainThreadHandler;

        /**
         * */
        JDBCConnectionThread(@NonNull DatabaseEntry databaseEntry, @NonNull OnJDBCSQLConnectListener listener)
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
                Class.forName(getDriverClassName());
                int port = databaseEntry.databasePort <= 0 ? getDefaultPort() : databaseEntry.databasePort;
                conn = DriverManager.getConnection(getConnectionUrl(getDriverName(),
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
                    if (isConnectionError(e))
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

    public interface OnJDBCSQLConnectListener
    {
        void onConnected();
        void onConnectionFailed(int error, String errorMessage);
    }

    protected abstract String getDriverClassName();
    protected abstract String getDriverName();
    protected abstract int getDefaultPort();
    protected abstract boolean isConnectionError(SQLException e);
    protected abstract String getConnectionUrl(String driver, String host, int port, String databaseName);
}
