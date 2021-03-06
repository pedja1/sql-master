package com.afstd.sqlitecommander.app.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.af.androidutility.lib.FileUtility;
import com.afstd.sqlitecommander.app.App;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.model.QueryHistory;
import com.afstd.sqlitecommander.app.utility.SettingsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseManager
{
    private static DatabaseManager instance;
    private static DatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    static final String MYSQL_TEST_DATABASE_ID = "1";
    static final String SQLITE_TEST_DATABASE_ID = "2";
    public final static Set<String> EXCLUDE_DATABASE_IDS;

    static
    {
        EXCLUDE_DATABASE_IDS = new HashSet<>();
        EXCLUDE_DATABASE_IDS.add(MYSQL_TEST_DATABASE_ID);
        EXCLUDE_DATABASE_IDS.add(SQLITE_TEST_DATABASE_ID);
    }

    public static synchronized DatabaseManager getInstance()
    {
        if (instance == null)
        {
            instance = new DatabaseManager();
            mDatabaseHelper = new DatabaseHelper(App.get());
            instance.mDatabase = mDatabaseHelper.getWritableDatabase();
        }

        return instance;
    }

    public SQLiteDatabase getDatabase()
    {
        return mDatabase;
    }

    public void rawQueryNoResult(String query, String[] args)
    {
        Cursor cursor = mDatabase.rawQuery(query, args);
        cursor.moveToNext();
        cursor.close();
    }

    public Cursor rawQuery(String query, String[] args)
    {
        return mDatabase.rawQuery(query, args);
    }

    public int delete(String table, String whereClause, String[] whereArgs)
    {
        return mDatabase.delete(table, whereClause, whereArgs);
    }

    public List<QueryHistory> getQueryHistory(String query, String[] args)
    {
        List<QueryHistory> his;

        Cursor cursor = mDatabase.rawQuery(query, args);
        his = new ArrayList<>();
        while (cursor.moveToNext())
        {
            QueryHistory reportType = new QueryHistory();
            reportType.query = cursor.getString(cursor.getColumnIndex("query"));
            his.add(reportType);
        }
        cursor.close();
        return his;
    }

    public List<DatabaseEntry> getDatabaseEntries(String query, String[] args)
    {
        List<DatabaseEntry> lis;

        Cursor cursor = mDatabase.rawQuery(query, args);
        lis = new ArrayList<>();
        while (cursor.moveToNext())
        {
            DatabaseEntry databaseEntry = new DatabaseEntry();
            databaseEntry.id = cursor.getString(cursor.getColumnIndex("id"));
            databaseEntry.type = cursor.getString(cursor.getColumnIndex("type"));
            databaseEntry.databaseUri = cursor.getString(cursor.getColumnIndex("database_uri"));
            databaseEntry.databaseName = cursor.getString(cursor.getColumnIndex("database_name"));
            databaseEntry.databaseUsername = cursor.getString(cursor.getColumnIndex("database_username"));
            databaseEntry.databasePassword = cursor.getString(cursor.getColumnIndex("database_password"));
            databaseEntry.databasePort = cursor.getInt(cursor.getColumnIndex("database_port"));
            databaseEntry.isFavorite = cursor.getInt(cursor.getColumnIndex("is_favorite")) == 1;
            databaseEntry.created = cursor.getLong(cursor.getColumnIndex("created"));
            databaseEntry.accessed = cursor.getLong(cursor.getColumnIndex("accessed"));
            databaseEntry.deleted = cursor.getInt(cursor.getColumnIndex("deleted")) == 1;
            lis.add(databaseEntry);
        }
        cursor.close();
        return lis;
    }

    public DatabaseEntry getDatabaseEntry(String query, String[] args)
    {
        Cursor cursor = mDatabase.rawQuery(query, args);
        DatabaseEntry databaseEntry = null;
        if (cursor.moveToNext())
        {
            databaseEntry = new DatabaseEntry();
            databaseEntry.id = cursor.getString(cursor.getColumnIndex("id"));
            databaseEntry.type = cursor.getString(cursor.getColumnIndex("type"));
            databaseEntry.databaseUri = cursor.getString(cursor.getColumnIndex("database_uri"));
            databaseEntry.databaseName = cursor.getString(cursor.getColumnIndex("database_name"));
            databaseEntry.databaseUsername = cursor.getString(cursor.getColumnIndex("database_username"));
            databaseEntry.databasePassword = cursor.getString(cursor.getColumnIndex("database_password"));
            databaseEntry.databasePort = cursor.getInt(cursor.getColumnIndex("database_port"));
            databaseEntry.isFavorite = cursor.getInt(cursor.getColumnIndex("is_favorite")) == 1;
            databaseEntry.created = cursor.getLong(cursor.getColumnIndex("created"));
            databaseEntry.accessed = cursor.getLong(cursor.getColumnIndex("accessed"));
            databaseEntry.deleted = cursor.getInt(cursor.getColumnIndex("deleted")) == 1;
        }
        cursor.close();
        return databaseEntry;
    }

    public int getCount(String query, String[] args)
    {
        Cursor cursor = mDatabase.rawQuery(query, args);
        while (cursor.moveToNext())
        {
            if (cursor.getColumnCount() > 0)
                return cursor.getInt(0);
        }
        cursor.close();
        return 0;
    }

    public void insertQueryHistory(String cmd)
    {
        ContentValues values = new ContentValues();
        values.put("query", cmd);

        long id = mDatabase.insertWithOnConflict("query_history", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1)
        {
            mDatabase.update("query_history", values, "query = ?", new String[]{cmd});
        }
    }

    public void insertDatabaseEntry(List<DatabaseEntry> databaseEntries)
    {
        if(databaseEntries == null)
            return;
        insertDatabaseEntry(databaseEntries.toArray(new DatabaseEntry[databaseEntries.size()]));
    }

    public void insertDatabaseEntry(DatabaseEntry... databaseEntries)
    {
        if(databaseEntries == null)
            return;
        mDatabase.beginTransaction();
        try
        {
            for (DatabaseEntry entry : databaseEntries)
            {
                ContentValues values = new ContentValues();
                values.put("id", entry.id);
                values.put("type", entry.type);
                values.put("database_uri", entry.databaseUri);
                values.put("database_name", entry.databaseName);
                values.put("database_username", entry.databaseUsername);
                values.put("database_password", entry.databasePassword);
                values.put("database_port", entry.databasePort);
                values.put("is_favorite", entry.isFavorite);
                values.put("deleted", entry.deleted);
                if(entry.created == null)
                    entry.created = System.currentTimeMillis();
                values.put("created", entry.created);
                if(entry.accessed == null)
                    entry.accessed = System.currentTimeMillis();
                values.put("accessed", entry.accessed);

                long id = mDatabase.insertWithOnConflict("_database", null, values, SQLiteDatabase.CONFLICT_IGNORE);
                if (id == -1)
                {
                    mDatabase.update("_database", values, "id = ?", new String[]{entry.id});
                }
            }
            mDatabase.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            if(SettingsManager.DEBUG())
                e.printStackTrace();
        }
        finally
        {
            mDatabase.endTransaction();
        }
    }

    public void insertQueryHistory(List<String> queryHistory)
    {
        if(queryHistory == null)
            return;
        insertQueryHistory(queryHistory.toArray(new String[queryHistory.size()]));
    }

    public void insertQueryHistory(String... queryHistory)
    {
        if(queryHistory == null)
            return;
        mDatabase.beginTransaction();
        try
        {
            for (String query : queryHistory)
            {
                ContentValues values = new ContentValues();
                values.put("query", query);

                long id = mDatabase.insertWithOnConflict("query_history", null, values, SQLiteDatabase.CONFLICT_IGNORE);
                if (id == -1)
                {
                    mDatabase.update("query_history", values, "query = ?", new String[]{query});
                }
            }
            mDatabase.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            if(SettingsManager.DEBUG())
                e.printStackTrace();
        }
        finally
        {
            mDatabase.endTransaction();
        }
    }

    public void clearDatabase()
    {
        mDatabaseHelper.onUpgrade(mDatabase, DatabaseHelper.DATABASE_VERSION, DatabaseHelper.DATABASE_VERSION);
    }
}


class DatabaseHelper extends SQLiteOpenHelper
{
    // Database Version
    public static final int DATABASE_VERSION = 9;
    // Database Name
    public static final String DATABASE_NAME = "internal.db";

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        try
        {
            String create = FileUtility.readRawFile(App.get(), R.raw.create_schema);
            String[] statements = create.split(";");
            for (String statement : statements)
            {
                db.execSQL(statement);
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unable to create database. Error reading create schema file. Error message: " + e.getMessage());
        }

        addDefaultHistory(db);
        addTestDatabase(db);
    }

    private void addTestDatabase(SQLiteDatabase db)
    {
        db.beginTransaction();
        try
        {
            String cmd = "INSERT INTO _database " +
                    "(id, type, database_uri, database_name, database_username, database_password, is_favorite) " +
                    "VALUES('" + DatabaseManager.MYSQL_TEST_DATABASE_ID + "', 'mysql', 'pedjaapps.net', 'sqlcmd_public_test', 'guest', 'guest', 1)";
            db.execSQL(cmd);

            cmd = "INSERT INTO _database " +
                    "(id, type, database_uri, is_favorite) " +
                    "VALUES('" + DatabaseManager.SQLITE_TEST_DATABASE_ID + "', 'sqlite', '" + db.getPath() + "', 1)";
            db.execSQL(cmd);

            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }
    }

    private void addDefaultHistory(SQLiteDatabase db)
    {
        String[] cmds = new String[]{
                "SELECT name FROM sqlite_master WHERE type='table';",
                "SELECT * FROM ",
                "INSERT INTO",
                "DELETE FROM",
        };
        db.beginTransaction();

        for (String cmd : cmds)
        {
            ContentValues values = new ContentValues();
            values.put("query", cmd);
            db.insert("query_history", null, values);
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2)
    {
        //TODO cant just delete all data
        try
        {
            String drop = FileUtility.readRawFile(App.get(), R.raw.drop_schema);
            String[] statements = drop.split(";");
            for (String statement : statements)
            {
                db.execSQL(statement);
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unable to update database. Error reading create schema file. Error message: " + e.getMessage());
        }
        onCreate(db);

    }
}
