/*
* This file is part of the Kernel Tuner.
*
* Copyright Predrag Čokulov <predragcokulov@gmail.com>
*
* Kernel Tuner is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Kernel Tuner is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Kernel Tuner. If not, see <http://www.gnu.org/licenses/>.
*/
package com.afstd.sqlitecommander.app.filemanager;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.afstd.sqlitecommander.app.R;

import java.util.List;

public final class FMAdapter extends ArrayAdapter<FMEntry>
{

    public FMAdapter(final Context context, List<FMEntry> list)
    {
        super(context, 0, list);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)
    {
        ViewHolder holder;
        final FMEntry entry = getItem(position);

        if (convertView == null)
        {
            final Context context = getContext();
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.list_item_fm, parent, false);
            holder = new ViewHolder();

            holder.name = (TextView) convertView.findViewById(R.id.tvName);
            holder.summary = (TextView) convertView.findViewById(R.id.tvSummary);
            holder.imageView = (ImageView) convertView.findViewById(R.id.image);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(entry.getName());
        holder.summary.setText(String.format("%s %s %s", entry.getDateHr(), entry.isFolder() ? "" : entry.getSize(), entry.getLink() == null ? "" : entry.getLink()));

        if (entry.isFolder())
        {
            holder.imageView.setImageResource(entry.getType() == FMEntry.TYPE_DIRECTORY_LINK
                    ? R.drawable.fm_folder_link : R.drawable.fm_folder);
        }
        else
        {
            if (entry.getType() == FMEntry.TYPE_LINK)
            {
                holder.imageView.setImageResource(R.drawable.fm_file_link);
            }
            else
            {

                switch (entry.getMimeType())
                {
                    case image:
                        holder.imageView.setImageResource(R.drawable.fm_image);
                        break;
                    case text:
                        holder.imageView.setImageResource(R.drawable.fm_text);
                        break;
                    case video:
                        VideoThumbLoader.getInstance(getContext()).displayImage(new BaseImageLoader.ImageData(entry.getPath(), holder.imageView, R.drawable.fm_video));
                        break;
                    case application:
                        APKImageLoader.getInstance(getContext()).displayImage(new BaseImageLoader.ImageData(entry.getPath(), holder.imageView, R.drawable.fm_apk));
                        break;
                    case database:
                        holder.imageView.setImageResource(R.drawable.fm_db);
                        break;
                    case archive:
                        holder.imageView.setImageResource(R.drawable.fm_archive);
                        break;
                    case web:
                        holder.imageView.setImageResource(R.drawable.fm_web);
                        break;
                    case audio:
                        holder.imageView.setImageResource(R.drawable.fm_audio);
                        break;
                    case doc:
                        holder.imageView.setImageResource(R.drawable.fm_doc);
                        break;
                    case pdf:
                        holder.imageView.setImageResource(R.drawable.fm_pdf);
                        break;
                    case ppt:
                        holder.imageView.setImageResource(R.drawable.fm_ppt);
                        break;
                    case excel:
                        holder.imageView.setImageResource(R.drawable.fm_excel);
                        break;
                    case lib:
                        holder.imageView.setImageResource(R.drawable.fm_lib);
                        break;
                    case script:
                        holder.imageView.setImageResource(R.drawable.fm_script);
                        break;
                    case code:
                        holder.imageView.setImageResource(R.drawable.fm_code);
                        break;
                    case unknown:
                    default:
                        holder.imageView.setImageResource(R.drawable.fm_file);
                }

            }
        }

        return convertView;
    }

    private class ViewHolder
    {
        TextView name;
        TextView summary;
        ImageView imageView;
    }
}
