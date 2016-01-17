package com.afstd.example.app;

import android.app.Application;

/**
 * Created by pedja on 17.1.16..
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
