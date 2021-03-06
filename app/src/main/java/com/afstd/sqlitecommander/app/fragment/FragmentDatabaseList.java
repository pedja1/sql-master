package com.afstd.sqlitecommander.app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.af.androidutility.lib.RVArrayAdapter;
import com.afstd.sqlitecommander.app.AddMySQLDatabase;
import com.afstd.sqlitecommander.app.AddSQLDatabaseActivity;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.adapter.DatabaseListAdapter;
import com.afstd.sqlitecommander.app.bus.RemoveAdsEvent;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;
import com.afstd.sqlitecommander.app.utility.SAsyncTask;
import com.afstd.sqlitecommander.app.utility.SettingsManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by pedja on 16.2.16..
 */
public abstract class FragmentDatabaseList extends Fragment implements View.OnClickListener
{
    private static final int REQUEST_CODE_ADD_DATABASE = 9001;

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
        View view = inflater.inflate(R.layout.fragment_database_list, container, false);

        tvError = (TextView) view.findViewById(R.id.tvError);
        pbLoading = (ProgressBar) view.findViewById(R.id.pbLoading);

        View fab = view.findViewById(R.id.fabAdd);
        fab.setOnClickListener(this);

        if(!hasAddFab())
            fab.setVisibility(View.GONE);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);

        mAdapter = new DatabaseListAdapter(this, databases = new ArrayList<>());

        recyclerView.setAdapter(mAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
        {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
            {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir)
            {
                int position = viewHolder.getAdapterPosition();
                if(mAdapter.getItem(position).isAd())
                    return;
                mAdapter.removeItemDialog(position);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        mLoader = new ATLoadDatabases(this);
        mLoader.execute();

        mAdapter.setOnItemClickListener(new RVArrayAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(Object item, int position)
            {
                onDatabaseClicked((DatabaseEntry)item, position);
            }
        });

        EventBus.getDefault().register(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getTitle());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mLoader != null)
            mLoader.cancel(true);
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(RemoveAdsEvent event)
    {
        if(mLoader != null)
            mLoader.cancel(true);
        mLoader = new ATLoadDatabases(this);
        mLoader.execute();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.fabAdd:
                AddSQLDatabaseActivity.start(this, getAddDatabaseActivityClass(), null, REQUEST_CODE_ADD_DATABASE);
                break;
        }
    }

    private static class ATLoadDatabases extends SAsyncTask<Void, Void, List<DatabaseEntry>>
    {
        private WeakReference<FragmentDatabaseList> reference;

        ATLoadDatabases(FragmentDatabaseList adapter)
        {
            this.reference = new WeakReference<>(adapter);
        }

        @Override
        protected List<DatabaseEntry> doInBackground(Void... params)
        {
            if(reference.get() == null)
                return null;
            return reference.get().loadDatabases();
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

            if (!reference.get().databases.isEmpty() && !SettingsManager.isPro())
            {
                DatabaseListAdapter.ListItemAd ad = new DatabaseListAdapter.ListItemAd();

                reference.get().databases.add(Math.min(2, reference.get().databases.size()), ad);
            }

            reference.get().mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_CODE_ADD_DATABASE:
                if (resultCode == Activity.RESULT_OK)
                {
                    String id = data.getStringExtra(AddMySQLDatabase.INTENT_EXTRA_DATABASE_ID);
                    if (id != null)
                    {
                        DatabaseEntry entry = DatabaseManager.getInstance().getDatabaseEntry("SELECT * FROM _database WHERE id = ?", new String[]{id});
                        databases.add(entry);
                        mAdapter.notifyItemInserted(databases.size() - 1);
                    }
                }
                break;
        }
    }

    protected abstract Class<? extends AddSQLDatabaseActivity> getAddDatabaseActivityClass();
    protected abstract List<DatabaseEntry> loadDatabases();
    protected abstract void onDatabaseClicked(DatabaseEntry item, int position);
    protected abstract String getTitle();
    protected abstract boolean hasAddFab();
}
