package com.afstd.sqlitecommander.app.su;

import android.os.Handler;
import android.os.Looper;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by pedja on 23.1.16..
 */
public class ShellInstance
{
    private Shell.Interactive shell;
    private static ShellInstance instance;

    private boolean isSu;

    public static synchronized ShellInstance getInstance()
    {
        if(instance == null)
        {
            instance = new ShellInstance();
        }
        return instance;
    }

    private ShellInstance()
    {
        Handler handler = new Handler(Looper.getMainLooper());
        final Shell.Builder builder = new Shell.Builder();
        builder.setHandler(handler);
        builder.setWantSTDERR(true);
        builder.useSU();
        shell = builder.open(new Shell.OnCommandResultListener()
        {
            @Override
            public void onCommandResult(int commandCode, int exitCode, List<String> output)
            {
                if (exitCode != Shell.OnCommandResultListener.SHELL_RUNNING)
                {
                    builder.useSH();
                    shell = builder.open();
                    isSu = false;
                }
                else
                {
                    isSu = true;
                }
            }
        });
    }

    public Shell.Interactive getShell()
    {
        return shell;
    }

    public boolean isSu()
    {
        return isSu;
    }
}
