package com.afstd.sqlitecommander.app.fragment;

import android.os.Bundle;

import com.afstd.sqlitecommander.app.AddMSSQLDatabase;
import com.afstd.sqlitecommander.app.AddSQLDatabaseActivity;
import com.afstd.sqlitecommander.app.MSSQLCMDActivity;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;

import java.util.List;

/**
 * Created by pedja on 16.2.16..
 */
public class FragmentMSSQL extends FragmentDatabaseList
{
    public static FragmentMSSQL newInstance()
    {
        Bundle args = new Bundle();

        FragmentMSSQL fragment = new FragmentMSSQL();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected Class<? extends AddSQLDatabaseActivity> getAddDatabaseActivityClass()
    {
        return AddMSSQLDatabase.class;
    }

    @Override
    protected List<DatabaseEntry> loadDatabases()
    {
        String query = "SELECT * FROM _database WHERE type = ? AND deleted != 1";
        String[] args = new String[]{DatabaseEntry.TYPE_MSSQL};
        return DatabaseManager.getInstance().getDatabaseEntries(query, args);
    }

    @Override
    protected void onDatabaseClicked(DatabaseEntry item, int position)
    {
        MSSQLCMDActivity.start(getActivity(), item.id);
    }

    @Override
    protected String getTitle()
    {
        return getString(R.string.mssql);
    }

    @Override
    protected boolean hasAddFab()
    {
        return true;
    }
}
