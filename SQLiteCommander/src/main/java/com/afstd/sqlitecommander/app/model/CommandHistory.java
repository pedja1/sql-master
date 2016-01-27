package com.afstd.sqlitecommander.app.model;

/**
 * Created by pedja on 26.1.16..
 */
public class CommandHistory
{
    public String command;

    public CommandHistory(String command)
    {
        this.command = command;
    }

    public CommandHistory()
    {
    }

    @Override
    public String toString()
    {
        return command;//used for autocomplete, DON'T edit
    }
}
