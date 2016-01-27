package com.afstd.sqlcmd;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedja on 16.1.16..
 */
public class SQLCMDDefault extends SQLCMD
{
    private SQLiteDatabase database;

    public SQLCMDDefault(SQLiteDatabase database)
    {
        this.database = database;
    }

    @Override
    public void executeSql(String sql, OnResultListener listener)
    {
        if(listener == null)
            throw new IllegalStateException("Listener cannot be null");
        try
        {
            List<List<KeyValuePair>> result = new ArrayList<>();
            Cursor cursor = database.rawQuery(sql, null);

            while (cursor.moveToNext())
            {
                List<KeyValuePair> row = new ArrayList<>();
                for(int i = 0; i < cursor.getColumnCount(); i++)
                {
                    KeyValuePair keyValuePair = new KeyValuePair(cursor.getColumnName(i), cursor.getString(i));
                    row.add(keyValuePair);
                }
                result.add(row);
            }

            cursor.close();
            listener.onResult(true, result, null);
        }
        catch (Exception e)
        {
            listener.onResult(false, null, e.getMessage());
        }
    }
}
