package com.google.android.gms.ads;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by pedja on 24.2.16..
 */
public class AdView extends View
{
    public AdView(Context context)
    {
        super(context);
    }

    public AdView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public AdView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public AdView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(0, 0);
    }

    public void loadAd(AdRequest adRequest)
    {

    }
}
