package com.afstd.sqlitecommander.app.model;

import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.adapter.DatabaseListAdapter;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;

/**
 * Created by pedja on 13.2.16..
 */
public class DatabaseEntry implements DatabaseListAdapter.DatabaseListItem
{
    public static final String TYPE_SQLITE = "sqlite";
    public static final String TYPE_MYSQL = "mysql";
    public static final String TYPE_MSSQL = "mssql";
    public static final String TYPE_POSTGRESQL = "postgresql";

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

    /**
     * if this database has been deleted*/
    public boolean deleted;

    public boolean isFavorite;
    public Long created;
    public Long accessed;

    public static DatabaseEntry findWithUri(String uri)
    {
        String query = "SELECT * FROM _database WHERE database_uri = ? AND deleted != 1";
        String[] args = new String[]{uri};

        return DatabaseManager.getInstance().getDatabaseEntry(query, args);
    }

    @DrawableRes
    public int getIconResource()
    {
        if(TextUtils.isEmpty(type))
            return R.drawable.ic_menu_sql;

        switch (type)
        {
            case TYPE_MYSQL:
                return R.drawable.ic_menu_mysql;
            case TYPE_SQLITE:
                return R.drawable.ic_menu_sqlite;
            case TYPE_POSTGRESQL:
                return R.drawable.ic_menu_postgressql;
            case TYPE_MSSQL:
                return R.drawable.ic_action_mssql;
            default:
                return R.drawable.ic_menu_sql;
        }
    }

    @Override
    public boolean isAd()
    {
        return false;
    }

    /*{
        try
        {
            AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.keys(SettingsManager.getEc());
            String encryptedUsername = cursor.getString(cursor.getColumnIndex("database_username"));
            String encryptedPassword = cursor.getString(cursor.getColumnIndex("database_password"));
            if (!TextUtils.isEmpty(encryptedUsername))
            {
                AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(encryptedUsername);
                databaseEntry.databaseUsername = AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys);
            }
            if (!TextUtils.isEmpty(encryptedPassword))
            {
                AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(encryptedPassword);
                databaseEntry.databasePassword = AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys);
            }
        }
        catch (GeneralSecurityException | UnsupportedEncodingException e)
        {
            if(SettingsManager.DEBUG())e.printStackTrace();
            //fail silently
        }
    }*/
}
