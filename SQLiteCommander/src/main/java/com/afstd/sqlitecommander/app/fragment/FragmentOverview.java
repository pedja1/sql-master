package com.afstd.sqlitecommander.app.fragment;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afstd.sqlitecommander.app.MainActivity;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;

/**
 * Created by pedja on 21.1.16..
 */
public class FragmentOverview extends Fragment implements View.OnClickListener
{
    public static FragmentOverview newInstance()
    {
        FragmentOverview fragment = new FragmentOverview();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        TextView tvAppCount = (TextView) view.findViewById(R.id.tvAppCount);
        TextView tvFavCount = (TextView) view.findViewById(R.id.tvFavCount);
        TextView tvHistCount = (TextView) view.findViewById(R.id.tvHistCount);

        PackageManager pm = getActivity().getPackageManager();
        tvAppCount.setText(getString(R.string.installed_applications, pm.getInstalledPackages(0).size()));

        tvFavCount.setText(getString(R.string.favorite_databases, DatabaseManager.getInstance().getCount("SELECT COUNT(*) FROM _database WHERE is_favorite = 1", null)));
        tvHistCount.setText(getString(R.string.viewed_databases, DatabaseManager.getInstance().getCount("SELECT COUNT(*) FROM  _database", null)));

        view.findViewById(R.id.btnSqlite).setOnClickListener(this);
        view.findViewById(R.id.btnMySql).setOnClickListener(this);
        view.findViewById(R.id.btnFavorites).setOnClickListener(this);
        view.findViewById(R.id.btnHistory).setOnClickListener(this);
        view.findViewById(R.id.btnCloud).setOnClickListener(this);
        view.findViewById(R.id.btnPostgreSql).setOnClickListener(this);
        view.findViewById(R.id.btnMSSql).setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.app_name);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnSqlite:
                ((MainActivity)getActivity()).onNavigationItemSelected(R.id.nav_sqlite);
                break;
            case R.id.btnMySql:
                ((MainActivity)getActivity()).onNavigationItemSelected(R.id.nav_mysql);
                break;
            case R.id.btnFavorites:
                ((MainActivity)getActivity()).onNavigationItemSelected(R.id.nav_favorites);
                break;
            case R.id.btnHistory:
                ((MainActivity)getActivity()).onNavigationItemSelected(R.id.nav_history);
                break;
            case R.id.btnCloud:
                ((MainActivity)getActivity()).onNavigationItemSelected(R.id.nav_cloud);
                break;
            case R.id.btnPostgreSql:
                ((MainActivity)getActivity()).onNavigationItemSelected(R.id.nav_postgresql);
                break;
            case R.id.btnMSSql:
                ((MainActivity)getActivity()).onNavigationItemSelected(R.id.nav_mssql);
                break;
        }
    }
}
