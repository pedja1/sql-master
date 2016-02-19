// Copyright (c) 2012 Chan Wai Shing
//
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package com.afstd.syntaxhighlight;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.text.SimpleAttributeSet;

/**
 * Theme for the {@link SyntaxHighlighterPane} and
 * {@link JTextComponentRowHeader}.
 * <p>
 * To make a new theme, either extending this class or initiate this class and
 * set parameters using setters. For the default value, find the comment of the
 * constructor.
 *
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class Theme
{

    private static final Logger LOG = Logger.getLogger(Theme.class.getName());

    /**
     * The background color of the highlighted line of script text.
     */
    protected int highlightedBackground;
    /**
     * Text area.
     */
    /**
     * The default style. When the style requested by {@link #getStyle(String)}
     * not exist, this will be returned.
     */
    protected Style plain;
    /**
     * The styles of this theme.
     */
    protected Map<String, Style> styles;

    /**
     * Constructor.<br />
     * <p>
     * <b>Default value:</b><br />
     * <ul>
     * <li>font: Consolas 12pt</li>
     * <li>background: white</li>
     * <li>gutter text: black</li>
     * <li>gutter border: R: 184, G: 184, B: 184</li>
     * <li>gutter border width: 3px</li>
     * <li>gutter text font: Consolas 12pt</li>
     * <li>gutter text padding-left: 7px</li>
     * <li>gutter text padding-right: 7px</li>
     * </ul>
     * </p>
     */
    public Theme()
    {

        highlightedBackground = Color.GRAY;

        plain = new Style();

        styles = new HashMap<String, Style>();
    }

    /**
     * Set the default style.
     *
     * @param plain the style
     */
    public void setPlain(Style plain)
    {
        if (plain == null)
        {
            throw new NullPointerException("argument 'plain' cannot be null");
        }
        this.plain = plain;
    }

    /**
     * Get the default style.
     *
     * @return the style
     */
    public Style getPlain()
    {
        return plain;
    }

    /**
     * Get the {@link AttributeSet} of {@code styleKeys}. For more than one
     * styles, separate the styles by space, e.g. 'plain comments'.
     *
     * @param styleKeys the style keys with keys separated by space
     * @return the combined {@link AttributeSet}
     */
    public SimpleAttributeSet getStylesAttributeSet(String styleKeys)
    {
        if (styleKeys.indexOf(' ') != -1)
        {
            SimpleAttributeSet returnAttributeSet = new SimpleAttributeSet();
            String[] _keys = styleKeys.split(" ");
            for (String _key : _keys)
            {
                returnAttributeSet.addAttributes(getStyle(_key).getAttributeSet());
            }
            return returnAttributeSet;
        }
        else
        {
            return getStyle(styleKeys).getAttributeSet();
        }
    }

    /**
     * Add style.
     *
     * @param styleKey the keyword of the style
     * @param style    the style
     * @return see the return value of {@link Map#put(Object, Object)}
     */
    public Style addStyle(String styleKey, Style style)
    {
        return styles.put(styleKey, style);
    }

    /**
     * Remove style by keyword.
     *
     * @param styleKey the keyword of the style
     * @return see the return value of {@link Map#remove(Object)}
     */
    public Style removeStyle(String styleKey)
    {
        return styles.remove(styleKey);
    }

    /**
     * Get the style by keyword.
     *
     * @param key the keyword
     * @return the {@link syntaxhighlighter.theme.Style} related to the
     * {@code key}; if the style related to the {@code key} not exist, the
     * style of 'plain' will return.
     */
    public Style getStyle(String key)
    {
        Style returnStyle = styles.get(key);
        return returnStyle != null ? returnStyle : plain;
    }

    /**
     * Get all styles.
     *
     * @return the styles
     */
    public Map<String, Style> getStyles()
    {
        return new HashMap<String, Style>(styles);
    }

    /**
     * Clear all styles.
     */
    public void clearStyles()
    {
        styles.clear();
    }

    /**
     * The background color of the highlighted line of script text.
     *
     * @return the color
     */
    public int getHighlightedBackground()
    {
        return highlightedBackground;
    }

    /**
     * The background color of the highlighted line of script text.
     *
     * @param highlightedBackground the color
     */
    public void setHighlightedBackground(int highlightedBackground)
    {
        this.highlightedBackground = highlightedBackground;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Theme clone()
    {
        Theme object = null;
        try
        {
            object = (Theme) super.clone();
            object.styles = new HashMap<String, Style>();
            for (String key : styles.keySet())
            {
                object.styles.put(key, styles.get(key).clone());
            }
        }
        catch (CloneNotSupportedException ex)
        {
        }
        return object;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append(getClass().getName());
        sb.append(": ");
        sb.append("highlightedBackground: ").append(getHighlightedBackground());
        sb.append("; ");
        sb.append("styles: ");
        for (String _key : styles.keySet())
        {
            sb.append(_key).append(":").append(styles.get(_key));
        }
        sb.append("]");

        return sb.toString();
    }
}
