package com.afstd.sqlcmd;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedja on 16.1.16..
 */
public class SQLCMD
{
    private SQLiteDatabase database;

    public SQLCMD(SQLiteDatabase database)
    {
        this.database = database;
    }

    public List<List<KeyValuePair>> executeSql(String sql) throws SQLCMDException
    {
        return executeSql(database, sql);
    }

    public List<List<KeyValuePair>> executeSql(SQLiteDatabase database, String sql) throws SQLCMDException
    {
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
            return result;
        }
        catch (Exception e)
        {
            throw new SQLCMDException(e);
        }
    }

    public String serializeToJson(List<List<KeyValuePair>> result)
    {
        return serializeToJson(database, result);
    }

    public String serializeToJson(SQLiteDatabase database, List<List<KeyValuePair>> result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject();
            JSONArray jResult = new JSONArray();
            for(List<KeyValuePair> row : result)
            {
                JSONObject jRow = new JSONObject();
                for(KeyValuePair pair : row)
                {
                    jRow.put(pair.key, pair.value);
                }
                jResult.put(jRow);
            }
            jsonObject.put("result", jResult);
            return jsonObject.toString();
        }
        catch (JSONException e)
        {
            return e.getMessage();
        }
    }

    public String serializeToJson(String sql) throws SQLCMDException
    {
        return serializeToJson(executeSql(sql));
    }

    public String serializeToJson(SQLiteDatabase database, String sql) throws SQLCMDException
    {
        return serializeToJson(executeSql(database, sql));
    }

    @SuppressWarnings("WeakerAccess")
    public static class KeyValuePair
    {
        public final String key;
        public String value;
        public boolean selected;

        public KeyValuePair(String key, String value)
        {
            this.key = key;
            this.value = value;
        }
    }
}
