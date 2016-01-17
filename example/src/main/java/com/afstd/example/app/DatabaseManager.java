package com.afstd.example.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
}


class DatabaseHelper extends SQLiteOpenHelper
{
    // Database Version
    public static final int DATABASE_VERSION = 1;
    // Database Name
    public static final String DATABASE_NAME = "test.db";

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String create = "CREATE TABLE test_table (_id INTEGER PRIMARY KEY, column1 TEXT, column2 TEXT, column3 INTEGER, column4 REAL);";
        db.execSQL(create);

        //insert 100 test rows
        for(int i = 0; i < 100; i++)
        {
            String insert = "INSERT INTO test_table (column1, column2, column3, column4) VALUES ('Android Is The Best OS for mobile devices', 'iOS', '12345546546546546546546456789', '1.23456789')";
            db.execSQL(insert);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2)
    {
        String drop = "DROP TABLE IF NOT EXIST test_table";
        db.execSQL(drop);
        onCreate(db);
    }
}
