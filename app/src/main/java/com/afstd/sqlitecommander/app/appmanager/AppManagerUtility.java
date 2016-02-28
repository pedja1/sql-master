package com.afstd.sqlitecommander.app.appmanager;

import java.util.Comparator;

/**
 * Created by pedja on 27.12.14..
 * <p/>
 * This file is part of Kernel-Tuner
 * Copyright Predrag Čokulov 2014
 */
public class AppManagerUtility
{
    private AppManagerUtility()
    {
    }


    public static class AppSorter implements Comparator<App>
    {

        public AppSorter()
        {
        }

        @Override
        public int compare(App lhs, App rhs)
        {
            return lhs.appLabel.compareTo(rhs.appLabel);
        }
    }
}
