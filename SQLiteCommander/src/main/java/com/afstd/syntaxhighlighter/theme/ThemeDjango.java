// Copyright (c) 2011 Chan Wai Shing
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
package com.afstd.syntaxhighlighter.theme;


import com.afstd.syntaxhighlight.Style;
import com.afstd.syntaxhighlight.Theme;


/**
 * Django theme.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class ThemeDjango extends Theme
{

  public ThemeDjango() {
    super();

    setHighlightedBackground(0xff233729);

    Style style = new Style();
    style.setBold(true);
    addStyle("bold", style);

    style = new Style();
    style.setColor(0xfff8f8f8);
    addStyle("plain", style);
    setPlain(style);

    style = new Style();
    style.setItalic(true);
    style.setColor(0xff336442);
    addStyle("comments", style);

    style = new Style();
    style.setColor(0xff9df39f);
    addStyle("string", style);

    style = new Style();
    style.setBold(true);
    style.setColor(0xff96dd3b);
    addStyle("keyword", style);

    style = new Style();
    style.setColor(0xff91bb9e);
    addStyle("preprocessor", style);

    style = new Style();
    style.setColor(0xffffaa3e);
    addStyle("variable", style);

    style = new Style();
    style.setColor(0xfff7e741);
    addStyle("value", style);

    style = new Style();
    style.setColor(0xffffaa3e);
    addStyle("functions", style);

    style = new Style();
    style.setColor(0xffe0e8ff);
    addStyle("constants", style);

    style = new Style();
    style.setBold(true);
    style.setColor(0xff96dd3b);
    addStyle("script", style);

    style = new Style();
    addStyle("scriptBackground", style);

    style = new Style();
    style.setColor(0xffeb939a);
    addStyle("color3", style);

    style = new Style();
    style.setColor(0xff91bb9e);
    addStyle("color2", style);

    style = new Style();
    style.setColor(0xffedef7d);
    addStyle("color3", style);
  }
}
