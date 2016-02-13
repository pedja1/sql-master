package com.afstd.sqlitecommander.app.acm;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by pedja on 17.7.14. 15.22.
 * This class is part of the HubToDate
 * Copyright © 2014 ${OWNER}
 *
 * We dont do actual sync but we still need ContentProvider, so we create dummy one
 */
public class DummyProvider extends ContentProvider
{
    @Override
    public boolean onCreate()
    {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        return null;
    }

    @Override
    public String getType(Uri uri)
    {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        return 0;
    }
}
