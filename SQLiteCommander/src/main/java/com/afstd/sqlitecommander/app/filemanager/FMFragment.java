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

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.afstd.sqlitecommander.app.App;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.su.SUInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;


public class FMFragment extends Fragment
{
    private static final String CURR_DIR = "curr_dir";
    private static final String BACKSTACK = "backstack";

    private static final int SU_COMMAND_CODE = 1;

	private String path;
	private FMAdapter fAdapter;
	private ListView fListView;
	private HashMap<String, Parcelable> listScrollStates = new HashMap<>();
    private LinkedList<String> backstack = new LinkedList<>();
    private TextView tvPath;

    public static FMFragment newInstance()
    {
        FMFragment fragment = new FMFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        View view = inflater.inflate(R.layout.fragment_fm, container, false);
		fListView = (ListView) view.findViewById(R.id.list);

        tvPath = (TextView) view.findViewById(R.id.tvPath);

		path = savedInstanceState != null ? savedInstanceState.getString(CURR_DIR) : FMUtils.FILE_SEPARATOR;//Environment.getExternalStorageDirectory().toString();
        tvPath.setText(path);

        fListView.setDrawingCacheEnabled(true);
		fAdapter = new FMAdapter(getActivity(), new ArrayList<FMEntry>());

		fListView.setAdapter(fAdapter);

		ls(path, false);

		fListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
										long arg3)
				{
                    FMEntry entry = fAdapter.getItem(pos);
                    if(entry.getType() == FMEntry.TYPE_DIRECTORY || entry.getType() == FMEntry.TYPE_DIRECTORY_LINK)
                    {
                        Parcelable state = fListView.onSaveInstanceState();
                        listScrollStates.put(path, state);
                        backstack.add(path);
                        path = entry.getType() == FMEntry.TYPE_DIRECTORY_LINK ? entry.getLink() : entry.getPath();
                        validatePath();
                        ls(path, false);
                    }
                    else if(entry.getType() == FMEntry.TYPE_FILE || entry.getType() == FMEntry.TYPE_LINK)
                    {
                        //TODO
                    }
				}
			});
		if(savedInstanceState != null)
		{
            backstack = (LinkedList<String>) savedInstanceState.getSerializable(BACKSTACK);
			Parcelable listState = savedInstanceState.getParcelable("list_position");
			if(listState != null)fListView.post(new RestoreListStateRunnable(listState));
		}
        return view;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        APKImageLoader.getInstance(getActivity()).cleanup();
        System.gc();
    }

    private void validatePath()
    {
        if(path != null)
        {
            if(path.endsWith(FMUtils.FILE_SEPARATOR))return;
            path = path + FMUtils.FILE_SEPARATOR;
        }
    }

    @Override
	public void onSaveInstanceState(Bundle outState)
	{
		// Serialize the current dropdown position.
		Parcelable state = fListView.onSaveInstanceState();
		outState.putParcelable("list_position", state);
        outState.putString(CURR_DIR, path);
        outState.putSerializable(BACKSTACK, backstack);
	}

	private void ls(final String path, final boolean back)
	{
        Shell.Interactive interactive = SUInstance.getInstance().getShell();
        String ls = App.get().getApplicationInfo().nativeLibraryDir + "/libls.so";
        interactive.addCommand(ls + " " + path, SU_COMMAND_CODE, new Shell.OnCommandResultListener()
        {
            @Override
            public void onCommandResult(int commandCode, int exitCode, List<String> output)
            {
                new ATParseOutput(back, output).execute();
            }
        });
	}


	/*@Override
	public void onBackPressed()
	{
        String bsPath = backstack.pollLast();
		if (bsPath == null)
		{
			super.onBackPressed();
		}
		else
		{

			path = bsPath;//path.substring(0, path.lastIndexOf("/"));

			ls(path, true);
		}
	}*/

	private class RestoreListStateRunnable implements Runnable
	{
        Parcelable state;

		public RestoreListStateRunnable(Parcelable state)
		{
			this.state = state;
		}
		
		@Override
		public void run()
		{
			fListView.onRestoreInstanceState(state);
			System.out.println("restoring list state");
		}
	}

    private class ATParseOutput extends AsyncTask<String, Void, List<FMEntry>>
    {
        boolean back;
        private List<String> files;

        ATParseOutput(boolean back, List<String> files)
        {
            this.files = files;
            this.back = back;
        }

        @Override
        protected List<FMEntry> doInBackground(String... strings)
        {
            return FMUtils.parseLsOutput(path, files);
        }

        @Override
        protected void onPostExecute(List<FMEntry> e)
        {
            fAdapter.clear();
            fAdapter.addAll(e);
            fAdapter.notifyDataSetChanged();
            if(back)
            {
                Parcelable state = listScrollStates.get(path);
                if(state != null)fListView.post(new RestoreListStateRunnable(state));
            }
			else
			{
				fListView.post(new Runnable()
				{
					@Override
					public void run()
					{
						fListView.setSelection(0);
					}
				});
			}
            tvPath.setText(path);
            //setSupportProgressBarIndeterminateVisibility(false);
            //getSupportActionBar().setSubtitle(path);
        }
    }
}
