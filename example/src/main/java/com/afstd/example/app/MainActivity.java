package com.afstd.example.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.afstd.sqlcmd.SQLCMD;
import com.afstd.sqlcmd.SQLCMDException;
import com.afstd.sqlcmd.SQLGridView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SQLCMD sqlcmd = new SQLCMD(DatabaseManager.getInstance().getDatabase());
        SQLGridView sqlView = (SQLGridView) findViewById(R.id.sqlView);

        List<List<SQLCMD.KeyValuePair>> data;
        try
        {
            data = sqlcmd.executeSql("SELECT * FROM test-_table");
            sqlView.setData(data);
        }
        catch (SQLCMDException e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
