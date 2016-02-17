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

import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.adapter.DatabaseListAdapter;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;
import com.android.volley.misc.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by pedja on 16.2.16..
 */
public class FragmentMySQL extends Fragment implements View.OnClickListener
{
    public static FragmentMySQL newInstance()
    {
        Bundle args = new Bundle();

        FragmentMySQL fragment = new FragmentMySQL();
        fragment.setArguments(args);
        return fragment;
    }

    private TextView tvError;
    private ProgressBar pbLoading;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<DatabaseEntry> databases;

    private ATLoadDatabases mLoader;
    private DatabaseListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_mysql, container, false);

        tvError = (TextView) view.findViewById(R.id.tvError);
        pbLoading = (ProgressBar) view.findViewById(R.id.pbLoading);

        view.findViewById(R.id.fabAdd).setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);

        mAdapter = new DatabaseListAdapter(getActivity(), databases = new ArrayList<>());

        recyclerView.setAdapter(mAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        mLoader = new ATLoadDatabases(this);
        mLoader.execute();

        return view;
    }

    @Override
    public void onDestroy()
    {
        if(mLoader != null)
            mLoader.cancel(true);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.fabAdd:
                addDummyData();
                break;
        }
    }

    private void addDummyData()
    {
        Random random = new Random();
        DatabaseEntry entry = new DatabaseEntry();
        entry.created = System.currentTimeMillis();
        entry.accessed = entry.created;
        entry.isFavorite = random.nextInt() % 2 == 0;
        entry.databaseName = "test_database " + random.nextInt();
        entry.databaseUri = "localhost";
        entry.id = UUID.randomUUID().toString();
        entry.type = DatabaseEntry.TYPE_MYSQL;
        databases.add(entry);
        DatabaseManager.getInstance().insertDatabaseEntry(entry);
        mAdapter.notifyItemInserted(databases.size() - 1);
    }

    private static class ATLoadDatabases extends AsyncTask<Void, Void, List<DatabaseEntry>>
    {
        private WeakReference<FragmentMySQL> reference;

        ATLoadDatabases(FragmentMySQL adapter)
        {
            this.reference = new WeakReference<>(adapter);
        }

        @Override
        protected List<DatabaseEntry> doInBackground(Void... params)
        {
            String query = "SELECT * FROM _database WHERE type = ?";
            String[] args = new String[]{DatabaseEntry.TYPE_MYSQL};
            return DatabaseManager.getInstance().getDatabaseEntries(query, args);
        }

        @Override
        protected void onPreExecute()
        {
            if(reference.get() == null)
                return;
            reference.get().tvError.setVisibility(View.GONE);
            reference.get().pbLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<DatabaseEntry> databaseEntries)
        {
            if(reference.get() == null)
                return;
            reference.get().pbLoading.setVisibility(View.GONE);
            reference.get().tvError.setVisibility(databaseEntries.isEmpty() ? View.VISIBLE : View.GONE);
            reference.get().databases.clear();
            reference.get().databases.addAll(databaseEntries);
            reference.get().mAdapter.notifyDataSetChanged();
        }
    }
}
