package com.afstd.sqlitecommander.app.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.afstd.sqlitecommander.app.App;
import com.afstd.sqlitecommander.app.model.CommandHistory;

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

    public List<CommandHistory> getCommandHistory(String query, String[] args)
    {
        List<CommandHistory> his;

        Cursor cursor = mDatabase.rawQuery(query, args);
        his = new ArrayList<>();
        while (cursor.moveToNext())
        {
            CommandHistory reportType = new CommandHistory();
            reportType.command = cursor.getString(cursor.getColumnIndex("cmd"));
            his.add(reportType);
        }
        cursor.close();
        return his;
    }

    public void insertCommandHistory(String cmd)
    {
        ContentValues values = new ContentValues();
        values.put("cmd", cmd);

        long id = mDatabase.insertWithOnConflict("cmd_history", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1)
        {
            mDatabase.update("cmd_history", values, "cmd = ?", new String[]{cmd});
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
    public static final int DATABASE_VERSION = 1;
    // Database Name
    public static final String DATABASE_NAME = "internal.db";

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String createCmdHistory = "CREATE TABLE cmd_history (cmd TEXT PRIMARY KEY)";
        db.execSQL(createCmdHistory);

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
            values.put("cmd", cmd);
            db.insert("cmd_history", null, values);
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2)
    {
        db.execSQL("DROP TABLE IF NOT EXIST cmd_history");
        onCreate(db);
    }
}
