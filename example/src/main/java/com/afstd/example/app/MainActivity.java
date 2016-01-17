package com.afstd.example.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.afstd.sqlcmd.SQLCMD;
import com.afstd.sqlcmd.SQLGridView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SQLCMD sqlcmd = new SQLCMD(DatabaseManager.getInstance().getDatabase());

        List<List<SQLCMD.KeyValuePair>> data = sqlcmd.executeSql("SELECT * FROM test_table");
        SQLGridView sqlView = (SQLGridView) findViewById(R.id.sqlView);
        sqlView.setData(data);
    }
}
