package com.afstd.sqlitecommander.app.su;

import android.os.Handler;
import android.os.Looper;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by pedja on 23.1.16..
 */
public class SUInstance
{
    private Shell.Interactive shell;
    private static SUInstance instance;

    public static synchronized SUInstance getInstance()
    {
        if(instance == null)
        {
            instance = new SUInstance();
        }
        return instance;
    }

    private SUInstance()
    {
        Handler handler = new Handler(Looper.getMainLooper());
        Shell.Builder builder = new Shell.Builder();
        builder.setHandler(handler);
        builder.useSU();
        shell = builder.open(new Shell.OnCommandResultListener()
        {
            @Override
            public void onCommandResult(int commandCode, int exitCode, List<String> output)
            {
                System.out.println();
            }
        });
    }

    public Shell.Interactive getShell()
    {
        return shell;
    }
}
