package com.afstd.sqlitecommander.app.model;

import android.support.annotation.NonNull;

import com.afstd.sqlitecommander.app.App;
import com.quinny898.library.persistentsearch.SearchResult;

import java.io.File;

/**
 * Created by pedja on 19.2.16..
 */
public class DatabaseSearchResult extends SearchResult
{
    @NonNull
    private final DatabaseEntry entry;

    public DatabaseSearchResult(@NonNull DatabaseEntry entry)
    {
        super(entry.databaseName != null ? entry.databaseName : new File(entry.databaseUri).getName(),
                App.get().getResources().getDrawable(entry.getIconResource()));
        this.entry = entry;
    }

    @NonNull
    public DatabaseEntry getEntry()
    {
        return entry;
    }
}
