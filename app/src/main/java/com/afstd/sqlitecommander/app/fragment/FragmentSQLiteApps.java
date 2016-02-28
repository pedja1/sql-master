package com.afstd.sqlitecommander.app.fragment;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.SQLiteDatabaseListActivity;
import com.afstd.sqlitecommander.app.appmanager.App;
import com.afstd.sqlitecommander.app.appmanager.AppAdapter;
import com.afstd.sqlitecommander.app.appmanager.AppManagerUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by pedja on 21.1.16..
 */
public class FragmentSQLiteApps extends Fragment
{
    private AppAdapter mAdapter;

    private ProgressBar pbLoading;
    private LinearLayout llLoading;

    private ATLoadApps atLoadApps;

    private static final List<App> cache = new ArrayList<>();

    public static FragmentSQLiteApps newInstance()
    {
        FragmentSQLiteApps fragment = new FragmentSQLiteApps();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_sqlite_apps, container, false);
        pbLoading = (ProgressBar) view.findViewById(R.id.pbLoading);
        llLoading = (LinearLayout) view.findViewById(R.id.llLoading);

        ListView lvApps = (ListView) view.findViewById(R.id.lvApps);
        mAdapter = new AppAdapter(getActivity(), new ArrayList<App>());
        lvApps.setAdapter(mAdapter);

        atLoadApps = new ATLoadApps();
        atLoadApps.execute();

        lvApps.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                App app = mAdapter.getItem(position);
                SQLiteDatabaseListActivity.start(getActivity(), app);
            }
        });
        return view;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (atLoadApps != null)
        {
            atLoadApps.cancel(true);
            atLoadApps.canceled = true;
        }
    }

    private class ATLoadApps extends AsyncTask<String, Integer, Void>
    {
        ArrayList<App> apps;

        static final int PROGRESS_SET_MAX = 0;
        static final int PROGRESS_SET_PROGRESS = 1;
        static final int PROGRESS_SHOW = 2;
        static final int PROGRESS_UPDATE = 3;

        boolean canceled = false;

        @Override
        protected Void doInBackground(String... params)
        {
            if (!cache.isEmpty())
            {
                apps = new ArrayList<>(cache);
                return null;
            }
            apps = new ArrayList<>();
            PackageManager pm = getActivity().getPackageManager();
            List<PackageInfo> packs = pm.getInstalledPackages(/*GET_APPS_FLAGS*/0);
            publishProgress(PROGRESS_SET_MAX, packs.size());
            int offset = 0;
            for (PackageInfo pi : packs)
            {
                if (canceled) return null;

                App app = new App();
                app.appLabel = pi.applicationInfo.loadLabel(pm).toString();
                app.packageName = pi.packageName;
                app.icon = pi.applicationInfo.loadIcon(pm);
                app.versionName = pi.versionName;
                app.pi = pi;
                apps.add(app);

                publishProgress(PROGRESS_SET_PROGRESS, offset);
                offset++;
            }
            publishProgress(PROGRESS_UPDATE, 0);

            //calculate size for each app
            cache.clear();
            cache.addAll(apps);
            Collections.sort(apps, new AppManagerUtility.AppSorter());
            return null;
        }

        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected void onPostExecute(Void res)
        {
            mAdapter.clear();
            mAdapter.addAll(apps);
            mAdapter.notifyDataSetChanged();
            llLoading.setVisibility(View.GONE);
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            if (values == null || values.length < 2) return;
            switch (values[0])
            {
                case PROGRESS_SET_MAX:
                    pbLoading.setMax(values[1]);
                    break;
                case PROGRESS_SET_PROGRESS:
                    pbLoading.setProgress(values[1]);
                    break;
                case PROGRESS_UPDATE:
                    mAdapter.clear();
                    mAdapter.addAll(apps);
                    mAdapter.notifyDataSetChanged();
                case PROGRESS_SHOW:
                    llLoading.setVisibility(View.GONE);
                    mAdapter.clear();
                    mAdapter.addAll(apps);
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }
}
