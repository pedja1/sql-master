package com.afstd.sqlitecommander.app.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.af.androidutility.lib.RVArrayAdapter;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;

import java.io.File;
import java.util.List;

/**
 * Created by pedja on 17.2.16..
 */
public class DatabaseListAdapter extends RVArrayAdapter<DatabaseEntry>
{
    public DatabaseListAdapter(@NonNull Context context, @NonNull List<DatabaseEntry> items)
    {
        super(context, items);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_database_entry, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position)
    {
        final DatabaseEntry databaseEntry = getItem(position);

        ViewHolder holder = (ViewHolder) viewHolder;

        holder.ivIcon.setImageResource(DatabaseEntry.TYPE_MYSQL.equals(databaseEntry.type) ? R.drawable.ic_menu_mysql : R.drawable.ic_menu_sqlite);
        holder.text1.setText(DatabaseEntry.TYPE_MYSQL.equals(databaseEntry.type) ? databaseEntry.databaseName : new File(databaseEntry.databaseUri).getName());
        if (DatabaseEntry.TYPE_MYSQL.equals(databaseEntry.type))
        {
            holder.text1.setText(String.format("%s:%s", databaseEntry.databaseUri, databaseEntry.databasePort <= 0 ? DatabaseEntry.DEFAULT_PORT : databaseEntry.databasePort));
        }
        else
        {
            holder.text1.setText(databaseEntry.databaseUri);
        }

        holder.ivFavorite.setVisibility(databaseEntry.isFavorite ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(onItemClickListener != null)
                    onItemClickListener.onItemClick(databaseEntry, position);
            }
        });
    }

    private class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView ivIcon, ivFavorite;
        TextView text1, text2;

        ViewHolder(View itemView)
        {
            super(itemView);
            ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon);
            ivFavorite = (ImageView) itemView.findViewById(R.id.ivFavorite);
            text1 = (TextView) itemView.findViewById(R.id.text1);
            text2 = (TextView) itemView.findViewById(R.id.text2);
        }
    }
}
