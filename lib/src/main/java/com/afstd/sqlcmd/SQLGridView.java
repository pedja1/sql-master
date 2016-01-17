package com.afstd.sqlcmd;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedja on 16.1.16..
 */
public class SQLGridView extends View
{
    private Rect mClipRect;
    private Rect mDrawingRect;
    private Rect mMeasuringRect;
    private Paint mPaint;

    private Scroller mScroller;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    private int mMaxHorizontalScroll;
    private int mMaxVerticalScroll;

    private int mRowHeight;
    private float mPadding;
    private float mTextSize;
    private float mMaxColumnWidth;

    private int mMaxRowsToDisplay;
    private float separatorSize;

    private List<Column> data;

    public SQLGridView(Context context)
    {
        super(context);
        init();
    }

    public SQLGridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public SQLGridView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SQLGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init()
    {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDrawingRect = new Rect();
        mClipRect = new Rect();
        mMeasuringRect = new Rect();
        mGestureDetector = new GestureDetector(getContext(), new OnGestureListener());
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

        mScroller = new Scroller(getContext());
        mScroller.setFriction(0.2f);

        separatorSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1 , getResources().getDisplayMetrics());
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
        mPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        mMaxColumnWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
        mMaxRowsToDisplay = 5000;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (data != null && !data.isEmpty())
        {
            //canvas.save();
            //canvas.scale(mScaleFactor, mScaleFactor);
            Rect drawingRect = mDrawingRect;
            drawingRect.left = getScrollX();
            drawingRect.top = getScrollY();
            drawingRect.right = drawingRect.left + getWidth();
            drawingRect.bottom = drawingRect.top + getHeight();

            drawEvents(canvas, drawingRect);
            drawTimebar(canvas, drawingRect);

            // If scroller is scrolling/animating do scroll. This applies when doing a fling.
            if (mScroller.computeScrollOffset())
            {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            //canvas.restore();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mScaleDetector.onTouchEvent(event);
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        recalculateAndRedraw(false);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
    }

    private void drawEvents(Canvas canvas, Rect drawingRect)
    {
        int columnOffsetX = 0;
        for (Column column : data)
        {
            //if not visible skip it
            if(columnOffsetX + column.columnWidth < getScrollX())
            {
                columnOffsetX += column.columnWidth + separatorSize;
                continue;
            }
            //if we are past screen width, dont draw any more
            if(columnOffsetX > getScrollX() + getWidth())
                break;
            // Set clip rectangle
            mClipRect.left = getScrollX();
            mClipRect.top = getScrollY() + mRowHeight;
            mClipRect.right = (int) (columnOffsetX + column.columnWidth + separatorSize);
            mClipRect.bottom = mClipRect.top + getHeight();

            canvas.save();
            canvas.clipRect(mClipRect);

            int itemOffsetY = mRowHeight;
            for(SQLCMD.KeyValuePair pair : column.entries)
            {
                if(itemOffsetY < getScrollY() - mRowHeight)
                {
                    itemOffsetY += mRowHeight + separatorSize;
                    continue;
                }
                if (itemOffsetY + mRowHeight > mClipRect.bottom)
                    break;

                drawingRect.top = itemOffsetY;
                drawingRect.left = columnOffsetX;
                drawingRect.right = drawingRect.left + (int) column.columnWidth;
                drawingRect.bottom = drawingRect.top + mRowHeight;
                drawEvent(canvas, pair, drawingRect);

                itemOffsetY += mRowHeight + separatorSize;
            }

            canvas.restore();

            columnOffsetX += column.columnWidth + separatorSize;
        }

    }

    private void drawEvent(final Canvas canvas, final SQLCMD.KeyValuePair item, final Rect drawingRect)
    {
        // Background
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(item.selected ? Color.GRAY : Color.WHITE);
        canvas.drawRect(drawingRect, mPaint);

        // Add left and right inner padding
        drawingRect.left += separatorSize;
        drawingRect.right -= separatorSize;
        drawingRect.top += separatorSize;
        drawingRect.bottom -= separatorSize;

        // Description
        mPaint.setColor(item.selected ? Color.WHITE : Color.GRAY);
        mPaint.setTextSize(mTextSize);

        String desc = item.value;
        // Move drawing.top so text will be centered (text is drawn bottom>up)
        mPaint.getTextBounds(desc, 0, desc.length(), mMeasuringRect);
        drawingRect.top += (((drawingRect.bottom - drawingRect.top) / 2) + (mMeasuringRect.height() / 2));

        desc = desc.substring(0,
                mPaint.breakText(desc, true, drawingRect.right - drawingRect.left, null));
        canvas.drawText(desc, drawingRect.left, drawingRect.top, mPaint);
    }

    private void drawTimebar(Canvas canvas, Rect drawingRect)
    {
        drawingRect.left = getScrollX();
        drawingRect.top = getScrollY();
        drawingRect.right = drawingRect.left + getWidth();
        drawingRect.bottom = drawingRect.top + mRowHeight;

        mClipRect.left = getScrollX();
        mClipRect.top = getScrollY();
        mClipRect.right = getScrollX() + getWidth();
        mClipRect.bottom = mClipRect.top + mRowHeight;

        canvas.save();
        canvas.clipRect(mClipRect);

        // Background
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.CYAN);
        canvas.drawRect(drawingRect, mPaint);

        // Time stamps
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(mTextSize);

        int columnOffsetX = 0;
        for (Column column : data)
        {
            //if not visible skip it
            if(columnOffsetX + column.columnWidth < getScrollX())
            {
                columnOffsetX += column.columnWidth + separatorSize;
                continue;
            }
            //if we are past screen width, dont draw any more
            if(columnOffsetX > getScrollX() + getWidth())
                break;

            canvas.drawText(column.column,
                    columnOffsetX,
                    drawingRect.top + (((drawingRect.bottom - drawingRect.top) / 2) + (mTextSize / 2)), mPaint);
            columnOffsetX += column.columnWidth + separatorSize;
        }

        canvas.restore();
    }


    /**
     * This will recalculate boundaries, maximal scroll and scroll to start position which is current time.
     * To be used on device rotation etc since the device height and width will change.
     *
     * @param withAnimation true if scroll to current position should be animated.
     */
    public void recalculateAndRedraw(boolean withAnimation)
    {
        if (data != null && !data.isEmpty())
        {
            calculateMaxVerticalScroll();
            calculateMaxHorizontalScroll();

            mScroller.startScroll(getScrollX(), getScrollY(),
                    0,
                    0, withAnimation ? 600 : 0);

            redraw();
        }
    }

    private void calculateMaxHorizontalScroll()
    {
        mMaxHorizontalScroll = 0;
        for(Column column : data)
        {
            mMaxHorizontalScroll += column.columnWidth + separatorSize;
        }
        mMaxHorizontalScroll = mMaxHorizontalScroll < getWidth() ? 0 : mMaxHorizontalScroll - getWidth();
    }

    private void calculateMaxVerticalScroll()
    {
        mPaint.setTextSize(mTextSize);
        mPaint.getTextBounds("A", 0, 1, mMeasuringRect);
        int height = mMeasuringRect.height();
        mRowHeight = (int) (height + mPadding * 2);

        mMaxVerticalScroll = (int) ((mRowHeight + separatorSize) * (data.get(0).entries.size() + 1));

        mMaxVerticalScroll = mMaxVerticalScroll < getHeight() ? 0 : mMaxVerticalScroll - getHeight();
    }

    /**
     * Does a invalidate() and requestLayout() which causes a redraw of screen.
     */
    private void redraw()
    {
        invalidate();
        requestLayout();
    }

    public void setData(List<List<SQLCMD.KeyValuePair>> newData)
    {
        if(newData == null || newData.isEmpty())
            return;
        if(data == null)
            data = new ArrayList<>();
        mPaint.setTextSize(mTextSize);
        data.clear();
        int offset = 0;
        for(List<SQLCMD.KeyValuePair> row : newData)
        {
            int columnIndex = 0;
            for(SQLCMD.KeyValuePair pair : row)
            {
                if(pair.value == null)
                    pair.value = "NULL";
                if(data.size() == columnIndex)
                {
                    Column column = new Column();
                    mPaint.getTextBounds(pair.key, 0, pair.key.length(), mMeasuringRect);
                    column.columnWidth = Math.min(mMaxColumnWidth, (mMeasuringRect.width() + 2 * mPadding));
                    data.add(column);
                }
                Column column = data.get(columnIndex);
                column.column = pair.key;
                column.entries.add(pair);

                mPaint.getTextBounds(pair.value, 0, pair.value.length(), mMeasuringRect);
                float width = Math.min(mMaxColumnWidth, (mMeasuringRect.width() + 2 * mPadding));
                if(width > column.columnWidth)
                    column.columnWidth = width;

                columnIndex++;
            }
            offset++;
            if(offset >= mMaxRowsToDisplay)
                break;
        }
        recalculateAndRedraw(false);
    }

    private class OnGestureListener extends GestureDetector.SimpleOnGestureListener
    {

        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            // This is absolute coordinate on screen not taking scroll into account.
            int x = (int) e.getX();
            int y = (int) e.getY();

            // Adding scroll to clicked coordinate
            int scrollX = getScrollX() + x;
            int scrollY = getScrollY() + y;

            int offsetX = 0;
            int selectedColumnPosition = -1;
            int columnPositionOffset = 0;
            for(Column column : data)
            {
                offsetX += column.columnWidth + separatorSize;
                if(offsetX < scrollX)
                {
                    deselectColumn(column);
                    continue;
                }

                if(selectedColumnPosition == -1)
                {
                    int position = (int) Math.floor((scrollY - (mRowHeight + separatorSize)) / (mRowHeight + separatorSize));
                    int offset = 0;
                    for(SQLCMD.KeyValuePair pair : column.entries)
                    {
                        if(position == offset)
                        {
                            pair.selected = true;
                            redraw();
                        }
                        else
                        {
                            pair.selected = false;
                        }
                        offset++;
                    }
                    selectedColumnPosition = columnPositionOffset;

                }
                else
                {
                    deselectColumn(column);
                }

                columnPositionOffset++;
            }

            return true;
        }

        private void deselectColumn(Column column)
        {
            for(SQLCMD.KeyValuePair pair : column.entries)
            {
                pair.selected = false;
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
        {
            int dx = (int) distanceX;
            int dy = (int) distanceY;
            int x = getScrollX();
            int y = getScrollY();


            // Avoid over scrolling
            if (x + dx < 0)
            {
                dx = 0 - x;
            }
            if (y + dy < 0)
            {
                dy = 0 - y;
            }
            if (x + dx > mMaxHorizontalScroll)
            {
                dx = mMaxHorizontalScroll - x;
            }
            if (y + dy > mMaxVerticalScroll)
            {
                dy = mMaxVerticalScroll - y;
            }

            scrollBy(dx, dy);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float vX, float vY)
        {

            mScroller.fling(getScrollX(), getScrollY(), -(int) vX,
                    -(int) vY, 0, mMaxHorizontalScroll, 0, mMaxVerticalScroll);

            redraw();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e)
        {
            if (!mScroller.isFinished())
            {
                mScroller.forceFinished(true);
                return true;
            }
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            redraw();
            return true;
        }
    }

}
