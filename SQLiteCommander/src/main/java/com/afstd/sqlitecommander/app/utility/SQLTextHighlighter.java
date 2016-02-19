package com.afstd.sqlitecommander.app.utility;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

import com.afstd.syntaxhighlight.ParseResult;
import com.afstd.syntaxhighlight.Theme;
import com.afstd.syntaxhighlighter.SyntaxHighlighterParser;
import com.afstd.syntaxhighlighter.brush.BrushSql;
import com.afstd.syntaxhighlighter.theme.ThemeDefault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pedja on 19.2.16..
 */
public class SQLTextHighlighter
{
    @Nullable
    private final TextView mTextView;
    @NonNull
    private final SyntaxHighlighterParser mParser;
    @NonNull
    private final Theme mTheme;

    public SQLTextHighlighter()
    {
        this(null, null);
    }

    public SQLTextHighlighter(@Nullable TextView textView)
    {
        this(textView, null);
    }

    public SQLTextHighlighter(@Nullable TextView textView, @Nullable Theme theme)
    {
        this.mTextView = textView;
        mParser = new SyntaxHighlighterParser(new BrushSql());
        mTheme = theme == null ? new ThemeDefault() : theme;
    }

    public Spannable highlight(String text)
    {
        return highlight(text, mTheme);
    }

    public Spannable highlight(String text, Theme theme)
    {
        if(theme == null)
            theme = new ThemeDefault();
        SpannableString spannableString = new SpannableString(text);
        _highlight(spannableString, text, theme);
        return spannableString;
    }

    public void highlight()
    {
        assertTextView();
        //noinspection ConstantConditions
        Editable e = mTextView.getEditableText();
        _highlight(e, e.toString(), mTheme);
    }

    private void _highlight(Spannable e, String text, Theme theme)
    {
        List<ParseResult> results = mParser.parse(null, text);
        Map<String, List<ParseResult>> styleList = new HashMap<>();

        for (ParseResult parseResult : results)
        {
            String styleKeysString = parseResult.getStyleKeysString();
            List<ParseResult> _styleList = styleList.get(styleKeysString);
            if (_styleList == null)
            {
                _styleList = new ArrayList<>();
                styleList.put(styleKeysString, _styleList);
            }
            _styleList.add(parseResult);
        }

        clearSpans(e);
        for (String key : styleList.keySet())
        {
            List<ParseResult> posList = styleList.get(key);

            for (ParseResult pos : posList)
            {
                List<Object> spans = theme.getStyle(key).newSpans();
                for (Object span : spans)
                {
                    e.setSpan(span, pos.getOffset(), pos.getOffset() + pos.getLength(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }

    private void assertTextView()
    {
        if(mTextView == null)
            throw new IllegalStateException("TextView must not be null to perform this action");
    }

    public void highlightTextChanges()
    {
        assertTextView();
        //noinspection ConstantConditions
        mTextView.addTextChangedListener(mWatcher);
    }

    public void stopHiglightingChanges()
    {
        assertTextView();
        //noinspection ConstantConditions
        mTextView.removeTextChangedListener(mWatcher);
    }

    private TextWatcher mWatcher = new TextWatcher()
    {
        boolean calbackDisaabled = false;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {

        }

        @Override
        public void afterTextChanged(Editable s)
        {
            if (calbackDisaabled)
                return;
            calbackDisaabled = true;

            highlight();

            calbackDisaabled = false;
        }
    };

    private void clearSpans(Spannable e)
    {
        // remove foreground color spans
        {
            ForegroundColorSpan spans[] = e.getSpans(0, e.length(), ForegroundColorSpan.class);

            for (int n = spans.length; n-- > 0; )
                e.removeSpan(spans[n]);
        }

        // remove background color spans
        {
            BackgroundColorSpan spans[] = e.getSpans(0, e.length(), BackgroundColorSpan.class);

            for (int n = spans.length; n-- > 0; )
                e.removeSpan(spans[n]);
        }
        // remove style spans
        {
            StyleSpan spans[] = e.getSpans(0, e.length(), StyleSpan.class);

            for (int n = spans.length; n-- > 0; )
                e.removeSpan(spans[n]);
        }
    }
}
