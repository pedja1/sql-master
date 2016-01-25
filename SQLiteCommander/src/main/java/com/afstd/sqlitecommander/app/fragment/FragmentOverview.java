package com.afstd.sqlitecommander.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afstd.sqlitecommander.app.R;

/**
 * Created by pedja on 21.1.16..
 */
public class FragmentOverview extends Fragment
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

        return view;
    }
}
