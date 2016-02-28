package com.afstd.sqlitecommander.app.fragment;

import android.os.Bundle;

import com.afstd.sqlitecommander.app.AddPostgreSQLDatabase;
import com.afstd.sqlitecommander.app.AddSQLDatabaseActivity;
import com.afstd.sqlitecommander.app.PostgreSQLCMDActivity;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;

import java.util.List;

/**
 * Created by pedja on 16.2.16..
 */
public class FragmentPostgreSQL extends FragmentDatabaseList
{
    public static FragmentPostgreSQL newInstance()
    {
        Bundle args = new Bundle();

        FragmentPostgreSQL fragment = new FragmentPostgreSQL();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected Class<? extends AddSQLDatabaseActivity> getAddDatabaseActivityClass()
    {
        return AddPostgreSQLDatabase.class;
    }

    @Override
    protected List<DatabaseEntry> loadDatabases()
    {
        String query = "SELECT * FROM _database WHERE type = ? AND deleted != 1";
        String[] args = new String[]{DatabaseEntry.TYPE_POSTGRESQL};
        return DatabaseManager.getInstance().getDatabaseEntries(query, args);
    }

    @Override
    protected void onDatabaseClicked(DatabaseEntry item, int position)
    {
        PostgreSQLCMDActivity.start(getActivity(), item.id);
    }

    @Override
    protected String getTitle()
    {
        return getString(R.string.postgresql);
    }

    @Override
    protected boolean hasAddFab()
    {
        return true;
    }
}
