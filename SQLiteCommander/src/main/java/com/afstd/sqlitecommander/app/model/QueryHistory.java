package com.afstd.sqlitecommander.app.model;

/**
 * Created by pedja on 26.1.16..
 */
public class QueryHistory
{
    public String command;

    public QueryHistory(String command)
    {
        this.command = command;
    }

    public QueryHistory()
    {
    }

    @Override
    public String toString()
    {
        return command;//used for autocomplete, DON'T edit
    }
}
