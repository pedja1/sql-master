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

import android.graphics.Color;

import com.afstd.syntaxhighlight.Style;
import com.afstd.syntaxhighlight.Theme;

/**
 * RDark theme.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class ThemeRDark extends Theme
{

  public ThemeRDark() {
    super();

    // RDark SyntaxHighlighter theme based on theme by Radu Dineiu
    // http://www.vim.org/scripts/script.php?script_id=1732

    setHighlightedBackground(0x323E41);

    Style style = new Style();
    style.setBold(true);
    addStyle("bold", style);

    style = new Style();
    style.setColor(0xb9bdb6);
    addStyle("plain", style);
    setPlain(style);

    style = new Style();
    style.setColor(0x878a85);
    addStyle("comments", style);

    style = new Style();
    style.setColor(0x5ce638);
    addStyle("string", style);

    style = new Style();
    style.setColor(0x5ba1cf);
    addStyle("keyword", style);

    style = new Style();
    style.setColor(0x435a5f);
    addStyle("preprocessor", style);

    style = new Style();
    style.setColor(0xffaa3e);
    addStyle("variable", style);

    style = new Style();
    style.setColor(0x009900);
    addStyle("value", style);

    style = new Style();
    style.setColor(0xffaa3e);
    addStyle("functions", style);

    style = new Style();
    style.setColor(0xe0e8ff);
    addStyle("constants", style);

    style = new Style();
    style.setBold(true);
    style.setColor(0x5ba1cf);
    addStyle("script", style);

    style = new Style();
    addStyle("scriptBackground", style);

    style = new Style();
    style.setColor(0xe0e8ff);
    addStyle("color3", style);

    style = new Style();
    style.setColor(Color.WHITE);
    addStyle("color2", style);

    style = new Style();
    style.setColor(0xffaa3e);
    addStyle("color3", style);
  }
}
