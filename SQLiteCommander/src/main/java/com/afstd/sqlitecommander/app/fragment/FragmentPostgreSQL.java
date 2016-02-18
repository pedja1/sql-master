package com.afstd.sqlitecommander.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afstd.sqlitecommander.app.R;

/**
 * Created by pedja on 16.2.16..
 */
public class FragmentPostgreSQL extends Fragment
{
    public static FragmentPostgreSQL newInstance()
    {
        Bundle args = new Bundle();

        FragmentPostgreSQL fragment = new FragmentPostgreSQL();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_postgresql, container, false);


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.postgresql);
    }
}
