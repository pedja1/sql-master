package com.afstd.sqlitecommander.app.model;

/**
 * Created by pedja on 13.2.16..
 */
public class DatabaseEntry
{
    public static final String TYPE_SQLITE = "sqlite";
    public static final String TYPE_MYSQL = "mysql";
    public static final Object DEFAULT_PORT = 3306;

    /**
     * UUID of this database, used for sync*/
    public String id;

    /**
     * Type of the database, either {@link #TYPE_SQLITE} or {@link #TYPE_MYSQL}*/
    public String type;

    /**For SQLite this is file path of the database file
     * For MySQL this is actually ip/domain name for mysql server WITHOUT protocol*/
    public String databaseUri;
    /**
     * Used only for MySQL, name of the database*/
    public String databaseName;
    /**
     * Used only for MySQL, database port, <= 0 for default port: 3306*/
    public int databasePort;

    /**
     * Used only for MySQL, username for the database*/
    public String databaseUsername;

    /**
     * Used only for MySQL, password for database*/
    public String databasePassword;

    public boolean isFavorite;
    public Long created;
    public Long accessed;
}
