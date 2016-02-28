package com.afstd.sqlitecommander.app.fragment;

import android.os.Bundle;

import com.afstd.sqlitecommander.app.AddSQLDatabaseActivity;
import com.afstd.sqlitecommander.app.MySQLCMDActivity;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.SQLiteCMDActivity;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;

import java.util.List;

/**
 * Created by pedja on 16.2.16..
 */
public class FragmentFavorites extends FragmentDatabaseList
{
    public static FragmentFavorites newInstance()
    {
        Bundle args = new Bundle();

        FragmentFavorites fragment = new FragmentFavorites();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected Class<? extends AddSQLDatabaseActivity> getAddDatabaseActivityClass()
    {
        return null;
    }

    @Override
    protected List<DatabaseEntry> loadDatabases()
    {
        String query = "SELECT * FROM _database WHERE is_favorite = 1  AND deleted != 1 ORDER BY accessed DESC";
        String[] args = new String[0];
        return DatabaseManager.getInstance().getDatabaseEntries(query, args);
    }

    @Override
    protected void onDatabaseClicked(DatabaseEntry item, int position)
    {
        if(DatabaseEntry.TYPE_SQLITE.equals(item.type))
        {
            SQLiteCMDActivity.start(getActivity(), item.databaseUri, true);
        }
        else if(DatabaseEntry.TYPE_MYSQL.equals(item.type))
        {
            MySQLCMDActivity.start(getActivity(), item.id);
        }
    }

    @Override
    protected String getTitle()
    {
        return getString(R.string.favorites);
    }

    @Override
    protected boolean hasAddFab()
    {
        return false;
    }
}
