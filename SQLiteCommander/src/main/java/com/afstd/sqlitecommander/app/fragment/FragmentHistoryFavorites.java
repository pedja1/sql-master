package com.afstd.sqlitecommander.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.af.androidutility.lib.RVArrayAdapter;
import com.afstd.sqlitecommander.app.MySQLCMDActivity;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.SQLiteCMDActivity;
import com.afstd.sqlitecommander.app.adapter.DatabaseListAdapter;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;
import com.android.volley.misc.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedja on 16.2.16..
 */
public class FragmentHistoryFavorites extends Fragment
{
    private static final String ARG_TYPE = "type";

    public static final int TYPE_HISTORY = 0;
    public static final int TYPE_FAVORITES = 1;

    public static FragmentHistoryFavorites newInstance(int type)
    {
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);

        FragmentHistoryFavorites fragment = new FragmentHistoryFavorites();
        fragment.setArguments(args);
        return fragment;
    }

    private int type;

    private TextView tvError;
    private ProgressBar pbLoading;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<DatabaseListAdapter.DatabaseListItem> databases;

    private ATLoadDatabases mLoader;
    private DatabaseListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        type = getArguments().getInt(ARG_TYPE);
        View view = inflater.inflate(R.layout.fragment_history_favorites, container, false);

        tvError = (TextView) view.findViewById(R.id.tvError);
        pbLoading = (ProgressBar) view.findViewById(R.id.pbLoading);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);

        mAdapter = new DatabaseListAdapter(this, databases = new ArrayList<>());

        recyclerView.setAdapter(mAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        mLoader = new ATLoadDatabases(this);
        mLoader.execute();

        mAdapter.setOnItemClickListener(new RVArrayAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(Object item, int position)
            {
                DatabaseEntry entry = (DatabaseEntry) item;
                if(DatabaseEntry.TYPE_SQLITE.equals(entry.type))
                {
                    SQLiteCMDActivity.start(getActivity(), entry.databaseUri, true);
                }
                else if(DatabaseEntry.TYPE_MYSQL.equals(entry.type))
                {
                    MySQLCMDActivity.start(getActivity(), ((DatabaseEntry) item).id);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(type == TYPE_HISTORY ? R.string.history : R.string.favorites);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mLoader != null)
            mLoader.cancel(true);
    }

    private static class ATLoadDatabases extends AsyncTask<Void, Void, List<DatabaseEntry>>
    {
        private WeakReference<FragmentHistoryFavorites> reference;

        ATLoadDatabases(FragmentHistoryFavorites adapter)
        {
            this.reference = new WeakReference<>(adapter);
        }

        @Override
        protected List<DatabaseEntry> doInBackground(Void... params)
        {
            if(reference.get() == null)
                return null;
            String query;
            if(reference.get().type == TYPE_FAVORITES)
            {
                query = "SELECT * FROM _database WHERE is_favorite = 1  AND deleted != 1 ORDER BY accessed DESC";
            }
            else
            {
                query = "SELECT * FROM _database WHERE deleted != 1 ORDER BY accessed DESC";
            }
            String[] args = new String[0];
            return DatabaseManager.getInstance().getDatabaseEntries(query, args);
        }

        @Override
        protected void onPreExecute()
        {
            if (reference.get() == null)
                return;
            reference.get().tvError.setVisibility(View.GONE);
            reference.get().pbLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<DatabaseEntry> databaseEntries)
        {
            if (reference.get() == null)
                return;
            reference.get().pbLoading.setVisibility(View.GONE);
            reference.get().tvError.setVisibility(databaseEntries.isEmpty() ? View.VISIBLE : View.GONE);
            reference.get().databases.clear();
            reference.get().databases.addAll(databaseEntries);

            if (!reference.get().databases.isEmpty())
            {
                DatabaseListAdapter.ListItemAd ad = new DatabaseListAdapter.ListItemAd();

                reference.get().databases.add(Math.min(2, reference.get().databases.size()), ad);
            }

            reference.get().mAdapter.notifyDataSetChanged();
        }
    }
}
