package com.afstd.sqlitecommander.app.mysql;

public class SQLCMDException extends Exception
{
    public SQLCMDException()
    {
    }

    public SQLCMDException(String detailMessage)
    {
        super(detailMessage);
    }

    public SQLCMDException(String detailMessage, Throwable throwable)
    {
        super(detailMessage, throwable);
    }

    public SQLCMDException(Throwable throwable)
    {
        super(throwable);
    }
}
