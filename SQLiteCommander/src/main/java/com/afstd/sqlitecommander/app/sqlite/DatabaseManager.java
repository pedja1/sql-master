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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager
{
    private static DatabaseManager instance;
    private static DatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

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
            reportType.command = cursor.getString(cursor.getColumnIndex("query"));
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
            lis.add(databaseEntry);
        }
        cursor.close();
        return lis;
    }

    public DatabaseEntry getDatabaseEntrie(String query, String[] args)
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

    public void insertCommandHistory(String cmd)
    {
        ContentValues values = new ContentValues();
        values.put("query", cmd);

        long id = mDatabase.insertWithOnConflict("query_history", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1)
        {
            mDatabase.update("query_history", values, "query = ?", new String[]{cmd});
        }
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
                values.put("created", entry.created);
                values.put("accessed", entry.accessed);

                long id = mDatabase.insertWithOnConflict("_database", null, values, SQLiteDatabase.CONFLICT_IGNORE);
                if (id == -1)
                {
                    mDatabase.update("_database", values, "id = ?", new String[]{entry.id});
                }
            }
            mDatabase.setTransactionSuccessful();
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
    public static final int DATABASE_VERSION = 5;
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
