package com.afstd.sqlitecommander.app.fragment;

import android.os.Bundle;

import com.afstd.sqlitecommander.app.AddMySQLDatabase;
import com.afstd.sqlitecommander.app.AddSQLDatabaseActivity;
import com.afstd.sqlitecommander.app.MySQLCMDActivity;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;

import java.util.List;

/**
 * Created by pedja on 16.2.16..
 */
public class FragmentMySQL extends FragmentDatabaseList
{
    public static FragmentMySQL newInstance()
    {
        Bundle args = new Bundle();

        FragmentMySQL fragment = new FragmentMySQL();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected Class<? extends AddSQLDatabaseActivity> getAddDatabaseActivityClass()
    {
        return AddMySQLDatabase.class;
    }

    @Override
    protected List<DatabaseEntry> loadDatabases()
    {
        String query = "SELECT * FROM _database WHERE type = ? AND deleted != 1";
        String[] args = new String[]{DatabaseEntry.TYPE_MYSQL};
        return DatabaseManager.getInstance().getDatabaseEntries(query, args);
    }

    @Override
    protected void onDatabaseClicked(DatabaseEntry item, int position)
    {
        MySQLCMDActivity.start(getActivity(), item.id);
    }

    @Override
    protected String getTitle()
    {
        return getString(R.string.my_sql);
    }

    @Override
    protected boolean hasAddFab()
    {
        return true;
    }
}
