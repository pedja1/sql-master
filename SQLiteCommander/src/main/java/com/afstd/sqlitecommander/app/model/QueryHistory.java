package com.afstd.sqlitecommander.app.model;

/**
 * Created by pedja on 26.1.16..
 */
public class QueryHistory
{
    public String query;

    public QueryHistory(String query)
    {
        this.query = query;
    }

    public QueryHistory()
    {
    }

    @Override
    public String toString()
    {
        return query;//used for autocomplete, DON'T edit
    }
}
