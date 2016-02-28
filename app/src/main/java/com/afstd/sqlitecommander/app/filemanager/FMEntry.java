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

import java.util.Date;

public class FMEntry
{
	/** Entry type: File */
	public static final int TYPE_FILE = 0;
	/** Entry type: Directory */
	public static final int TYPE_DIRECTORY = 1;
	/** Entry type: Directory Link */
	public static final int TYPE_DIRECTORY_LINK = 2;
	/** Entry type: Block */
	public static final int TYPE_BLOCK = 3;
	/** Entry type: Character */
	public static final int TYPE_CHARACTER = 4;
	/** Entry type: Link */
	public static final int TYPE_LINK = 5;
	/** Entry type: Socket */
	public static final int TYPE_SOCKET = 6;
	/** Entry type: FIFO */
	public static final int TYPE_FIFO = 7;
	/** Entry type: Other */
	public static final int TYPE_OTHER = 8;

	private String name;
	private Date date;
    private String dateHr;
	private long size;
	private String sizeHr;
	private String path;
	private int permissions;
	private int type;
    private String link;
    private FMUtils.FileType mimeType = FMUtils.FileType.unknown;

	public FMEntry()
	{
		
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public void setSizeHr(String sizeHr)
	{
		this.sizeHr = sizeHr;
	}

	public String getSizeHr()
	{
		return sizeHr;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public int getType()
	{
		return type;
	}

	public String getName()
	{
		return name;
	}

	public long getSize()
	{
		return size;
	}

	public Date getDate()
	{
		return date;
	}

	public String getPath()
	{
		return path;
	}

	public boolean isFolder()
	{
		return type == TYPE_DIRECTORY || type == TYPE_DIRECTORY_LINK;
	}

    public int getPermissions()
    {
        return permissions;
    }

    public void setPermissions(int permissions)
    {
        this.permissions = permissions;
    }

    public String getDateHr()
    {
        return dateHr;
    }

    public void setDateHr(String dateHr)
    {
        this.dateHr = dateHr;
    }

    public String getLink()
    {
        return link;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    public FMUtils.FileType getMimeType()
    {
        return mimeType;
    }

    public void setMimeType(FMUtils.FileType mimeType)
    {
        this.mimeType = mimeType;
    }
}
