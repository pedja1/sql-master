package com.afstd.sqlitecommander.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.filemanager.FMFragment;

/**
 * Created by pedja on 21.1.16..
 */
public class FragmentSQLite extends Fragment
{
    public static FragmentSQLite newInstance()
    {
        FragmentSQLite fragment = new FragmentSQLite();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_sqlite, container, false);

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    private class PagerAdapter extends FragmentPagerAdapter
    {
        private static final int PAGE_INDEX_APPS = 0;
        private static final int PAGE_INDEX_FILES = 1;

        PagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public int getCount()
        {
            return 2;
        }

        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                case PAGE_INDEX_APPS:
                    return FragmentSQLiteApps.newInstance();
                case PAGE_INDEX_FILES:
                    return FMFragment.newInstance();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case PAGE_INDEX_APPS:
                    return getString(R.string.apps);
                case PAGE_INDEX_FILES:
                    return getString(R.string.files);
            }
            return null;
        }
    }
}
