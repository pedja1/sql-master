package com.afstd.sqlcmd;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedja on 16.1.16..
 */
public class Column
{
    String column;
    List<SQLCMD.KeyValuePair> entries;
    float columnWidth;

    public Column()
    {
        entries = new ArrayList<>();
    }
}
