package com.afstd.sqlitecommander.app.sqlite;

import android.util.Log;

import com.afstd.sqlcmd.SQLCMD;
import com.afstd.sqlitecommander.app.App;
import com.afstd.sqlitecommander.app.su.SUInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by pedja on 16.1.16..
 */
public class SQLCMDRoot extends SQLCMD
{
    private static final String LOG_TAG = SQLCMDRoot.class.getName();
    private static final String HEADER_START = "header:";
    private static final String MESSAGE_START = "message:";
    private static final String ROW_START = "row:";

    private String databasePath;

    public SQLCMDRoot(String databasePath)
    {
        this.databasePath = databasePath;
    }

    @Override
    public void executeSql(String sql, final OnResultListener listener)
    {
        if(listener == null)
            throw new IllegalStateException("Listener cannot be null");
        Shell.Interactive shell = SUInstance.getInstance().getShell();
        String ver = App.get().getApplicationInfo().nativeLibraryDir + "/libsqlite_cmd.so";
        shell.addCommand(ver + " " + databasePath + " \"" + sql + "\"", 0, new Shell.OnCommandResultListener()
        {
            @Override
            public void onCommandResult(int commandCode, int exitCode, List<String> output)
            {
                if(exitCode != 0)
                {
                    listener.onResult(false, null, output != null ? Arrays.toString(output.toArray(new String[output.size()])) : null);
                }
                else
                {
                    List<String> messages = new ArrayList<>();
                    List<List<SQLCMD.KeyValuePair>> result = new ArrayList<>();
                    String[] columns = null;

                    for(String line : output)
                    {
                        if(line.startsWith(MESSAGE_START))
                        {
                            line = line.replace(MESSAGE_START, "");
                            messages.add(line);
                            continue;
                        }

                        if(columns == null)
                        {
                            if(!line.startsWith(HEADER_START))
                            {
                                continue;
                            }

                            line = line.replace(HEADER_START, "");

                            columns = line.split("\\|");
                            if(columns.length == 0)
                            {
                                listener.onResult(true, result, null);
                                return;
                            }
                        }
                        if(!line.startsWith(ROW_START))
                        {
                            continue;
                        }
                        line = line.replace(ROW_START, "");
                        String[] data = line.split("\\|");
                        if(data.length != columns.length)
                        {
                            Log.w(LOG_TAG, "warning. number of columns in row doesn't match actual number of columns, skipping...");
                            continue;
                        }
                        List<SQLCMD.KeyValuePair> row = new ArrayList<>();
                        for(int i = 0; i < columns.length; i++)
                        {
                            SQLCMD.KeyValuePair keyValuePair = new SQLCMD.KeyValuePair(columns[i], data[i]);
                            row.add(keyValuePair);
                        }
                        result.add(row);
                    }
                    listener.onResult(true, result, messages.isEmpty() ? null : Arrays.toString(messages.toArray(new String[messages.size()])));
                }
            }
        });
    }

}
