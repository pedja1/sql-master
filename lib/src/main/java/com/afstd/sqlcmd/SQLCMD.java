package com.afstd.sqlcmd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by pedja on 16.1.16..
 */
public abstract class SQLCMD
{
    public abstract void executeSql(String sql, OnResultListener listener);

    public String serializeToJson(List<List<KeyValuePair>> result)
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

    public interface OnResultListener
    {
        void onResult(boolean success, List<List<SQLCMD.KeyValuePair>> data, String error);
    }

}
