package com.afstd.sqlitecommander.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.List;

/**
 * Created by pedja on 17.2.16..
 */
public class DatabaseListAdapter extends RVArrayAdapter<DatabaseListAdapter.DatabaseListItem>
{
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_ADD = 1;

    public static final int REQUEST_CODE_EDIT_DATABASE = 9002;
    private Fragment fragment;

    public DatabaseListAdapter(@NonNull Context context, @NonNull List<DatabaseListAdapter.DatabaseListItem> items)
    {
        super(context, items);
    }

    public DatabaseListAdapter(@NonNull Fragment fragment, @NonNull List<DatabaseListAdapter.DatabaseListItem> items)
    {
        super(fragment.getActivity(), items);

        this.fragment = fragment;
    }

    @Override
    public int getItemViewType(int position)
    {
        return getItem(position).isAd() ? VIEW_TYPE_ADD : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        if(viewType == VIEW_TYPE_ADD)
        {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_ad, parent, false), true);
        }
        else
        {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_database_entry, parent, false), false);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position)
    {
        if(getItemViewType(position) == VIEW_TYPE_ADD)
        {
            AdView mAdView = ((ViewHolder)viewHolder).adView;
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
        else
        {
            final DatabaseEntry databaseEntry = (DatabaseEntry) getItem(position);

            ViewHolder holder = (ViewHolder) viewHolder;

            if (DatabaseEntry.TYPE_SQLITE.equals(databaseEntry.type))
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

            holder.ivDelete.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    removeItemDialog(position);
                }
            });

            holder.ivFavorite.setVisibility(databaseEntry.isFavorite ? View.VISIBLE : View.GONE);

            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (onItemClickListener != null)
                        onItemClickListener.onItemClick(databaseEntry, position);
                }
            });

            holder.ivEdit.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (!DatabaseEntry.TYPE_SQLITE.equals(databaseEntry.type))
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
    }

    public void removeItemDialog(final int position)
    {
        final boolean[] canceled = {true};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete);
        builder.setMessage(R.string.delete_database);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                canceled[0] = false;
                DatabaseEntry databaseEntry = (DatabaseEntry) getItem(position);
                databaseEntry.deleted = true;
                DatabaseManager.getInstance().insertDatabaseEntry(databaseEntry);
                remove(databaseEntry);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                if(canceled[0])notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
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
        ImageView ivIcon, ivFavorite, ivEdit, ivCredWarning, ivDelete;
        TextView text1, text2;
        AdView adView;

        ViewHolder(View itemView, boolean isAd)
        {
            super(itemView);
            if(isAd)
            {
                adView = (AdView) itemView.findViewById(R.id.adView);
            }
            else
            {
                ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon);
                ivFavorite = (ImageView) itemView.findViewById(R.id.ivFavorite);
                ivEdit = (ImageView) itemView.findViewById(R.id.ivEdit);
                ivCredWarning = (ImageView) itemView.findViewById(R.id.ivCredWarning);
                ivDelete = (ImageView) itemView.findViewById(R.id.ivDelete);
                text1 = (TextView) itemView.findViewById(R.id.text1);
                text2 = (TextView) itemView.findViewById(R.id.text2);
            }
        }
    }

    public interface DatabaseListItem
    {
        boolean isAd();
    }

    public static class ListItemAd implements DatabaseListItem
    {
        @Override
        public boolean isAd()
        {
            return true;
        }
    }
}
