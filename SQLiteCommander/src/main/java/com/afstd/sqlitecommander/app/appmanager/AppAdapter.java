package com.afstd.sqlitecommander.app.appmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.afstd.sqlitecommander.app.R;

import java.util.List;

/**
 * Created by pedja on 27.12.14..
 * <p/>
 * This file is part of Kernel-Tuner
 * Copyright Predrag Čokulov 2014
 */
public class AppAdapter extends ArrayAdapter<App>
{
    public AppAdapter(Context context, List<App> apps)
    {
        super(context, 0, apps);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        App app = getItem(position);

        if(convertView == null)
        {
            convertView = ((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_app, parent, false);
            holder = new ViewHolder();
            holder.tvLabel = (TextView)convertView.findViewById(R.id.tvLabel);
            holder.tvPackage = (TextView)convertView.findViewById(R.id.tvPackage);
            holder.ivIcon = (ImageView)convertView.findViewById(R.id.ivIcon);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvLabel.setText(app.appLabel);
        holder.tvPackage.setText(app.packageName);
        holder.ivIcon.setImageDrawable(app.icon);

        return convertView;
    }

    private class ViewHolder
    {
        TextView tvLabel, tvPackage;
        ImageView ivIcon;
    }
}
