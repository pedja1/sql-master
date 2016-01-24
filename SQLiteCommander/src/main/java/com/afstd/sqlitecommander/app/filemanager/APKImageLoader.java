package com.afstd.sqlitecommander.app.filemanager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class APKImageLoader extends BaseImageLoader<Drawable>
{
    private static APKImageLoader instance = null;
    private Context context;

    private APKImageLoader(Context context)
    {
        super();
        this.context = context.getApplicationContext();
    }

    public static APKImageLoader getInstance(Context context)
    {
        if(instance == null)
        {
            instance = new APKImageLoader(context);
        }
        return instance;
    }

    @Override
    public void setImageResource(ImageView imageView, Drawable drawable)
    {
        imageView.setImageDrawable(drawable);
    }

    @Override
    protected Drawable loadCacheItem(ImageData imageData)
    {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = pm.getPackageArchiveInfo(imageData.path, 0);

        pi.applicationInfo.sourceDir = imageData.path;
        pi.applicationInfo.publicSourceDir = imageData.path;

        return pi.applicationInfo.loadIcon(pm);
    }


}