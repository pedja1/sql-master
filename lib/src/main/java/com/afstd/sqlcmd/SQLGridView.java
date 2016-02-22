package com.afstd.sqlcmd;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Scroller;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.x;

/**
 * Created by pedja on 16.1.16..
 */
public class SQLGridView extends View
{

    private static final float DEFAULT_DIVIDER_SIZE = 1;//dp
    private static final float DEFAULT_PADDING = 5;//dp
    private static final float DEFAULT_TEXT_SIZE = 14;//dp
    private static final float DEFAULT_MAX_COLUMN_WIDTH = 150;//dp

    private static final int DEFAULT_MAX_ROWS_TO_DISPLAY = 5000;
    private static final boolean DEFAULT_SHOW_LIMIT_EXCEEDED_MESSAGE = true;
    private static final int DEFAULT_DIVIDER_COLOR = Color.GRAY;
    private static final int DEFAULT_ITEM_TEXT_COLOR = Color.GRAY;
    private static final int DEFAULT_ITEM_TEXT_COLOR_SELECTED = Color.WHITE;
    private static final int DEFAULT_COLUMN_TEXT_COLOR = Color.WHITE;
    private static final int DEFAULT_COLUMNS_BACKGROUND_COLOR = Color.CYAN;
    private static final int DEFAULT_ITEM_SELECTED_BACKGROUND = Color.GRAY;

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
    private float mItemPaddingLeft;
    private float mItemPaddingRight;
    private float mItemPaddingTop;
    private float mItemPaddingBottom;
    private float mTextSize;
    private float mMaxColumnWidth;

    private int mMaxRowsToDisplay;
    private float mVerticalDividerSize;
    private float mHorizontalDividerSize;

    private int mDividerColor, mItemTextColor, mItemTextColorSelected, mColumnTextColor;
    private Drawable mColumnsBackground, mSelectedItemBackground;

    private boolean mShowMessageIfDisplayLimitIsExceeded;

    private List<Column> data;

    public SQLGridView(Context context)
    {
        super(context);
        init(null, 0, 0);
    }

    public SQLGridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(attrs, 0, 0);
    }

    public SQLGridView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SQLGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDrawingRect = new Rect();
        mClipRect = new Rect();
        mMeasuringRect = new Rect();
        mGestureDetector = new GestureDetector(getContext(), new OnGestureListener());
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

        mScroller = new Scroller(getContext());
        mScroller.setFriction(0.2f);

        if (attrs != null)
        {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SQLGridView, defStyleAttr, defStyleRes);

            mVerticalDividerSize = a.getDimension(R.styleable.SQLGridView_sqlview_verticalDividerSize, DEFAULT_DIVIDER_SIZE);
            mHorizontalDividerSize = a.getDimension(R.styleable.SQLGridView_sqlview_horizontalDividerSize, DEFAULT_DIVIDER_SIZE);

            mTextSize = a.getDimension(R.styleable.SQLGridView_android_textSize, dpToPixels(DEFAULT_TEXT_SIZE));

            mItemPaddingTop = a.getDimension(R.styleable.SQLGridView_sqlview_itemPaddingTop, dpToPixels(DEFAULT_PADDING));
            mItemPaddingBottom = a.getDimension(R.styleable.SQLGridView_sqlview_itemPaddingBottom, dpToPixels(DEFAULT_PADDING));
            mItemPaddingLeft = a.getDimension(R.styleable.SQLGridView_sqlview_itemPaddingLeft, dpToPixels(DEFAULT_PADDING));
            mItemPaddingRight = a.getDimension(R.styleable.SQLGridView_sqlview_itemPaddingRight, dpToPixels(DEFAULT_PADDING));

            mMaxColumnWidth = a.getDimension(R.styleable.SQLGridView_sqlview_maxColumnWidth, dpToPixels(DEFAULT_MAX_COLUMN_WIDTH));

            mMaxRowsToDisplay = a.getInteger(R.styleable.SQLGridView_sqlview_maxDisplayRows, DEFAULT_MAX_ROWS_TO_DISPLAY);
            mShowMessageIfDisplayLimitIsExceeded = a.getBoolean(R.styleable.SQLGridView_sqlview_showMessageIfDisplayLimitIsExceeded, DEFAULT_SHOW_LIMIT_EXCEEDED_MESSAGE);

            mDividerColor = a.getColor(R.styleable.SQLGridView_sqlview_dividerColor, DEFAULT_DIVIDER_COLOR);

            mItemTextColor = a.getColor(R.styleable.SQLGridView_sqlview_itemTextColor, DEFAULT_ITEM_TEXT_COLOR);
            mItemTextColorSelected = a.getColor(R.styleable.SQLGridView_sqlview_itemTextColorSelected, DEFAULT_ITEM_TEXT_COLOR_SELECTED);

            mColumnTextColor = a.getColor(R.styleable.SQLGridView_sqlview_columnTextColor, DEFAULT_COLUMN_TEXT_COLOR);

            if (a.hasValue(R.styleable.SQLGridView_sqlview_columnsBackground))
            {
                mColumnsBackground = a.getDrawable(R.styleable.SQLGridView_sqlview_columnsBackground);
            }
            else
            {
                mColumnsBackground = new ColorDrawable(DEFAULT_COLUMNS_BACKGROUND_COLOR);
            }
            if (a.hasValue(R.styleable.SQLGridView_sqlview_selectedItemBackground))
            {
                mSelectedItemBackground = a.getDrawable(R.styleable.SQLGridView_sqlview_selectedItemBackground);
            }
            else
            {
                mSelectedItemBackground = new ColorDrawable(DEFAULT_ITEM_SELECTED_BACKGROUND);
            }

            a.recycle();
        }
        else
        {
            mVerticalDividerSize = mHorizontalDividerSize = dpToPixels(DEFAULT_DIVIDER_SIZE);
            mTextSize = dpToPixels(DEFAULT_TEXT_SIZE);
            mItemPaddingBottom = mItemPaddingLeft = mItemPaddingRight = mItemPaddingTop = dpToPixels(DEFAULT_PADDING);
            mMaxColumnWidth = dpToPixels(DEFAULT_MAX_COLUMN_WIDTH);
            mMaxRowsToDisplay = DEFAULT_MAX_ROWS_TO_DISPLAY;
            mShowMessageIfDisplayLimitIsExceeded = DEFAULT_SHOW_LIMIT_EXCEEDED_MESSAGE;
            mDividerColor = DEFAULT_DIVIDER_COLOR;
            mItemTextColor = DEFAULT_ITEM_TEXT_COLOR;
            mItemTextColorSelected = DEFAULT_ITEM_TEXT_COLOR_SELECTED;
            mColumnTextColor = DEFAULT_COLUMN_TEXT_COLOR;
            mColumnsBackground = new ColorDrawable(DEFAULT_COLUMNS_BACKGROUND_COLOR);
            mSelectedItemBackground = new ColorDrawable(DEFAULT_COLUMNS_BACKGROUND_COLOR);
        }

    }

    private float dpToPixels(float dps)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, getResources().getDisplayMetrics());
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
            drawColumns(canvas, drawingRect);

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
            if (columnOffsetX + column.columnWidth < getScrollX())
            {
                columnOffsetX += column.columnWidth + mHorizontalDividerSize;
                continue;
            }
            //if we are past screen width, dont draw any more
            if (columnOffsetX > getScrollX() + getWidth())
                break;
            // Set clip rectangle
            mClipRect.left = getScrollX();
            mClipRect.top = getScrollY() + mRowHeight;
            mClipRect.right = (int) (columnOffsetX + column.columnWidth + mHorizontalDividerSize);
            mClipRect.bottom = mClipRect.top + getHeight();

            canvas.save();
            canvas.clipRect(mClipRect);

            int itemOffsetY = mRowHeight;
            for (SQLCMD.KeyValuePair pair : column.entries)
            {
                if (itemOffsetY < getScrollY() - mRowHeight)
                {
                    itemOffsetY += mRowHeight + mVerticalDividerSize;
                    continue;
                }
                if (itemOffsetY + mRowHeight > mClipRect.bottom)
                    break;

                drawingRect.top = itemOffsetY;
                drawingRect.left = columnOffsetX;
                drawingRect.right = drawingRect.left + (int) column.columnWidth;
                drawingRect.bottom = drawingRect.top + mRowHeight;
                drawEvent(canvas, pair, drawingRect);

                drawingRect.top = (int) (itemOffsetY + mRowHeight - mVerticalDividerSize);
                drawingRect.left = columnOffsetX;
                drawingRect.right = drawingRect.left + (int) column.columnWidth;
                drawingRect.bottom = (int) (drawingRect.top + mVerticalDividerSize);
                drawDivider(canvas, drawingRect);

                itemOffsetY += mRowHeight + mVerticalDividerSize;
            }

            drawingRect.top = getScrollY() + mRowHeight;
            drawingRect.left = (int) (columnOffsetX + column.columnWidth - mHorizontalDividerSize);
            drawingRect.right = (int) (drawingRect.left + mHorizontalDividerSize);
            drawingRect.bottom = drawingRect.top + getHeight();
            drawDivider(canvas, drawingRect);

            canvas.restore();

            columnOffsetX += column.columnWidth + mHorizontalDividerSize;
        }

    }

    private void drawDivider(Canvas canvas, Rect drawingRect)
    {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mDividerColor);
        canvas.drawRect(drawingRect, mPaint);
    }

    private void drawEvent(final Canvas canvas, final SQLCMD.KeyValuePair item, final Rect drawingRect)
    {
        // Background
        if (item.selected && mSelectedItemBackground != null)
        {
            mSelectedItemBackground.setBounds(drawingRect.left, drawingRect.top, drawingRect.right, drawingRect.bottom);
            mSelectedItemBackground.draw(canvas);
        }

        // Add left and right inner padding
        drawingRect.left += mHorizontalDividerSize;
        drawingRect.right -= mHorizontalDividerSize;
        drawingRect.top += mVerticalDividerSize;
        drawingRect.bottom -= mVerticalDividerSize;

        // Description
        mPaint.setColor(item.selected ? mItemTextColorSelected : mItemTextColor);
        mPaint.setTextSize(mTextSize);

        String desc = item.value;
        // Move drawing.top so text will be centered (text is drawn bottom>up)
        mPaint.getTextBounds(desc, 0, desc.length(), mMeasuringRect);
        drawingRect.top += (((drawingRect.bottom - drawingRect.top) / 2) + (mMeasuringRect.height() / 2));

        desc = desc.substring(0,
                mPaint.breakText(desc, true, drawingRect.right - drawingRect.left, null));
        canvas.drawText(desc, drawingRect.left, drawingRect.top, mPaint);
    }

    private void drawColumns(Canvas canvas, Rect drawingRect)
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
        if (mColumnsBackground != null)
        {
            mColumnsBackground.setBounds(drawingRect.left, drawingRect.top, drawingRect.right, drawingRect.bottom);
            mColumnsBackground.draw(canvas);
        }

        // Time stamps
        mPaint.setColor(mColumnTextColor);
        mPaint.setTextSize(mTextSize);

        int columnOffsetX = 0;
        for (Column column : data)
        {
            //if not visible skip it
            if (columnOffsetX + column.columnWidth < getScrollX())
            {
                columnOffsetX += column.columnWidth + mHorizontalDividerSize;
                continue;
            }
            //if we are past screen width, dont draw any more
            if (columnOffsetX > getScrollX() + getWidth())
                break;

            canvas.drawText(column.column,
                    columnOffsetX,
                    drawingRect.top + (((drawingRect.bottom - drawingRect.top) / 2) + (mTextSize / 2)), mPaint);
            columnOffsetX += column.columnWidth + mHorizontalDividerSize;
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
        for (Column column : data)
        {
            mMaxHorizontalScroll += column.columnWidth + mHorizontalDividerSize;
        }
        mMaxHorizontalScroll = mMaxHorizontalScroll < getWidth() ? 0 : mMaxHorizontalScroll - getWidth();
    }

    private void calculateMaxVerticalScroll()
    {
        mPaint.setTextSize(mTextSize);
        mPaint.getTextBounds("A", 0, 1, mMeasuringRect);
        int height = mMeasuringRect.height();
        mRowHeight = (int) (height + mItemPaddingTop + mItemPaddingBottom);

        mMaxVerticalScroll = (int) ((mRowHeight + mVerticalDividerSize) * (data.get(0).entries.size() + 1));

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
        if (data == null)
            data = new ArrayList<>();
        data.clear();
        if (newData == null)
        {
            recalculateAndRedraw(false);
            return;
        }
        mPaint.setTextSize(mTextSize);
        int offset = 0;
        for (List<SQLCMD.KeyValuePair> row : newData)
        {
            int columnIndex = 0;
            for (SQLCMD.KeyValuePair pair : row)
            {
                if (pair.value == null)
                    pair.value = "NULL";
                if (data.size() == columnIndex)
                {
                    Column column = new Column();
                    mPaint.getTextBounds(pair.key, 0, pair.key.length(), mMeasuringRect);
                    column.columnWidth = Math.min(mMaxColumnWidth, (mMeasuringRect.width() + mItemPaddingLeft + mItemPaddingRight));
                    data.add(column);
                }
                Column column = data.get(columnIndex);
                column.column = pair.key;
                column.entries.add(pair);

                mPaint.getTextBounds(pair.value, 0, pair.value.length(), mMeasuringRect);
                float width = Math.min(mMaxColumnWidth, (mMeasuringRect.width() + mItemPaddingLeft + mItemPaddingRight));
                if (width > column.columnWidth)
                    column.columnWidth = width;

                columnIndex++;
            }
            offset++;
            if (offset >= mMaxRowsToDisplay)
            {
                if (mShowMessageIfDisplayLimitIsExceeded)
                {
                    Toast.makeText(getContext(), getResources().getString(R.string.display_limit_exceeded, mMaxRowsToDisplay), Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
        recalculateAndRedraw(false);
    }

    private class OnGestureListener extends GestureDetector.SimpleOnGestureListener
    {

        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            if(data == null)
                return false;
            // This is absolute coordinate on screen not taking scroll into account.
            int x = (int) e.getX();
            int y = (int) e.getY();

            // Adding scroll to clicked coordinate
            int scrollX = getScrollX() + x;
            int scrollY = getScrollY() + y;

            int offsetX = 0;
            int selectedColumnPosition = -1;
            int columnPositionOffset = 0;
            for (Column column : data)
            {
                offsetX += column.columnWidth + mHorizontalDividerSize;
                if (offsetX < scrollX)
                {
                    deselectColumn(column);
                    continue;
                }

                if (selectedColumnPosition == -1)
                {
                    int position = (int) Math.floor((scrollY - (mRowHeight + mVerticalDividerSize)) / (mRowHeight + mVerticalDividerSize));
                    int offset = 0;
                    for (SQLCMD.KeyValuePair pair : column.entries)
                    {
                        if (position == offset)
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
            for (SQLCMD.KeyValuePair pair : column.entries)
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
