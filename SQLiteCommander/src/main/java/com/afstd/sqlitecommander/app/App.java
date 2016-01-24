package com.afstd.sqlitecommander.app;


import eu.chainfire.libsuperuser.Application;

/**
 * Created by pedja on 24.1.16..
 */
public class App extends Application
{
    private static App instance;

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
    }

    public static App get()
    {
        return instance;
    }
}
