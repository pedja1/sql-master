package com.afstd.sqlitecommander.app.appmanager;

import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by pedja on 27.12.14..
 * <p/>
 * This file is part of Kernel-Tuner
 * Copyright Predrag Čokulov 2014
 */
public class App implements Parcelable
{
    public String appLabel, packageName, versionName;
    public Drawable icon;
    public PackageInfo pi;


    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.appLabel);
        dest.writeString(this.packageName);
        dest.writeString(this.versionName);
        dest.writeParcelable(this.pi, 0);
    }

    public App()
    {
    }

    protected App(Parcel in)
    {
        this.appLabel = in.readString();
        this.packageName = in.readString();
        this.versionName = in.readString();
        this.pi = in.readParcelable(PackageInfo.class.getClassLoader());
    }

    public static final Parcelable.Creator<App> CREATOR = new Parcelable.Creator<App>()
    {
        public App createFromParcel(Parcel source)
        {
            return new App(source);
        }

        public App[] newArray(int size)
        {
            return new App[size];
        }
    };
}
