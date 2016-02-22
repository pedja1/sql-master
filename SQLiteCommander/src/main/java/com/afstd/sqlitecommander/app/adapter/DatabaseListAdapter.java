package com.afstd.sqlitecommander.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.af.androidutility.lib.RVArrayAdapter;
import com.afstd.sqlitecommander.app.AddMySQLDatabase;
import com.afstd.sqlitecommander.app.AddSQLDatabaseActivity;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;

import java.io.File;
import java.util.List;

/**
 * Created by pedja on 17.2.16..
 */
public class DatabaseListAdapter extends RVArrayAdapter<DatabaseEntry>
{
    public static final int REQUEST_CODE_EDIT_DATABASE = 9002;
    private Fragment fragment;

    public DatabaseListAdapter(@NonNull Context context, @NonNull List<DatabaseEntry> items)
    {
        super(context, items);
    }

    public DatabaseListAdapter(@NonNull Fragment fragment, @NonNull List<DatabaseEntry> items)
    {
        super(fragment.getActivity(), items);
        this.fragment = fragment;
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

        if(DatabaseEntry.TYPE_SQLITE.equals(databaseEntry.type))
        {
            holder.ivIcon.setImageResource(R.drawable.ic_menu_sqlite);
            holder.text1.setText(new File(databaseEntry.databaseUri).getName());//todo new file each time?
            holder.text2.setText(databaseEntry.databaseUri);
            holder.ivEdit.setVisibility(View.GONE);
            holder.ivCredWarning.setVisibility(View.GONE);
        }
        else
        {
            holder.ivEdit.setVisibility(View.VISIBLE);
            holder.ivIcon.setImageResource(databaseEntry.getIconResource());
            holder.text1.setText(DatabaseEntry.TYPE_MYSQL.equals(databaseEntry.type) ? databaseEntry.databaseName : new File(databaseEntry.databaseUri).getName());
            holder.text2.setText(String.format("%s:%s", databaseEntry.databaseUri, databaseEntry.databasePort <= 0 ? DatabaseEntry.MYSQL_DEFAULT_PORT : databaseEntry.databasePort));

            holder.ivCredWarning.setVisibility(hasCredentials(databaseEntry) ? View.GONE : View.VISIBLE);
        }

        holder.ivCredWarning.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.warning);
                builder.setMessage(R.string.empty_credentials_warning);
                builder.setNegativeButton(R.string.ok, null);
                builder.show();
            }
        });

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

        holder.ivEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!DatabaseEntry.TYPE_SQLITE.equals(databaseEntry.type))
                {
                    if (fragment != null)
                    {
                        AddSQLDatabaseActivity.start(fragment, getClassForType(databaseEntry.type), databaseEntry.id, REQUEST_CODE_EDIT_DATABASE);
                    }
                    else
                    {
                        AddSQLDatabaseActivity.start((Activity) context, getClassForType(databaseEntry.type), databaseEntry.id, REQUEST_CODE_EDIT_DATABASE);
                    }
                }
            }
        });
    }

    private boolean hasCredentials(DatabaseEntry databaseEntry)
    {
        return !TextUtils.isEmpty(databaseEntry.databaseUsername);
    }

    private Class<? extends AddSQLDatabaseActivity> getClassForType(String type)
    {
        if(type == null)
            return null;
        switch (type)
        {
            case DatabaseEntry.TYPE_MYSQL:
                return AddMySQLDatabase.class;
        }
        //TODO other types
        return null;//die, horribly
    }

    private class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView ivIcon, ivFavorite, ivEdit, ivCredWarning;
        TextView text1, text2;

        ViewHolder(View itemView)
        {
            super(itemView);
            ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon);
            ivFavorite = (ImageView) itemView.findViewById(R.id.ivFavorite);
            ivEdit = (ImageView) itemView.findViewById(R.id.ivEdit);
            ivCredWarning = (ImageView) itemView.findViewById(R.id.ivCredWarning);
            text1 = (TextView) itemView.findViewById(R.id.text1);
            text2 = (TextView) itemView.findViewById(R.id.text2);
        }
    }
}
