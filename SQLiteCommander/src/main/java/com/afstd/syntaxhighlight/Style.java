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

/**
 * The style used by {@link syntaxhiglight.Theme} as those of CSS styles.
 *
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class Style implements Cloneable
{

    /**
     * It indicate whether the {@link #attributeSet} has updated after any style
     * changes.
     */
    protected boolean changed;
    /**
     * The {@link AttributeSet} representation of this style.
     */
    protected SimpleAttributeSet attributeSet;
    /**
     * Font bold.
     */
    protected boolean bold;
    /**
     * Font color.
     */
    protected int color;
    /**
     * The background color, null means no background color is set.
     */
    protected int background;
    /**
     * Font underline.
     */
    protected boolean underline;
    /**
     * Font italic.
     */
    protected boolean italic;

    /**
     * Constructor.
     * <p>
     * <b>Default values:</b><br />
     * <ul>
     * <li>bold: false;</li>
     * <li>color: black;</li>
     * <li>background: null;</li>
     * <li>underline: false;</li>
     * <li>italic: false;</li>
     * </ul>
     * </p>
     */
    public Style()
    {
        changed = true;
        attributeSet = null;

        bold = false;
        color = Color.BLACK;
        background = -1;
        underline = false;
        italic = false;
    }

    /**
     * Get the {@link AttributeSet} of this style.
     *
     * @return the {@link AttributeSet}
     */
    public SimpleAttributeSet getAttributeSet()
    {
        if (changed)
        {
            attributeSet = new SimpleAttributeSet();
            StyleConstants.setBold(attributeSet, bold);
            StyleConstants.setForeground(attributeSet, color);
            if (background != null)
            {
                StyleConstants.setBackground(attributeSet, background);
            }
            StyleConstants.setUnderline(attributeSet, underline);
            StyleConstants.setItalic(attributeSet, italic);
            changed = false;
        }
        return attributeSet;
    }

    /**
     * Get the background color.
     *
     * @return the background color or null if no color is set
     */
    public int getBackground()
    {
        return background;
    }

    /**
     * Set the background color.
     *
     * @param background null means do not set the background
     */
    public void setBackground(int background)
    {
        changed = true;
        this.background = background;
    }

    public boolean isBold()
    {
        return bold;
    }

    public void setBold(boolean bold)
    {
        changed = true;
        this.bold = bold;
    }

    public int getColor()
    {
        return color;
    }

    public void setColor(int color)
    {
        changed = true;
        this.color = color;
    }

    public boolean isItalic()
    {
        return italic;
    }

    public void setItalic(boolean italic)
    {
        changed = true;
        this.italic = italic;
    }

    public boolean isUnderline()
    {
        return underline;
    }

    public void setUnderline(boolean underline)
    {
        changed = true;
        this.underline = underline;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Style style = (Style) o;

        if (bold != style.bold) return false;
        if (color != style.color) return false;
        if (background != style.background) return false;
        if (underline != style.underline) return false;
        return italic == style.italic;

    }

    @Override
    public int hashCode()
    {
        int result = (bold ? 1 : 0);
        result = 31 * result + color;
        result = 31 * result + background;
        result = 31 * result + (underline ? 1 : 0);
        result = 31 * result + (italic ? 1 : 0);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Style clone()
    {
        Style object = null;
        try
        {
            object = (Style) super.clone();
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
        sb.append("bold: ").append(bold);
        sb.append(", ");
        sb.append("color: ").append(color);
        sb.append(", ");
        sb.append("background: ").append(background);
        sb.append(", ");
        sb.append("underline: ").append(underline);
        sb.append(", ");
        sb.append("italic: ").append(italic);
        sb.append("]");

        return sb.toString();
    }
}
