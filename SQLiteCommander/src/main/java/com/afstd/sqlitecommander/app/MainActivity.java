package com.afstd.sqlitecommander.app;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.af.androidutility.lib.AndroidUtility;
import com.afstd.sqlitecommander.app.acm.AMUtility;
import com.afstd.sqlitecommander.app.acm.SSyncAdapter;
import com.afstd.sqlitecommander.app.fragment.FragmentCloud;
import com.afstd.sqlitecommander.app.fragment.FragmentHistoryFavorites;
import com.afstd.sqlitecommander.app.fragment.FragmentMySQL;
import com.afstd.sqlitecommander.app.fragment.FragmentOverview;
import com.afstd.sqlitecommander.app.fragment.FragmentPostgreSQL;
import com.afstd.sqlitecommander.app.fragment.FragmentSQLite;
import com.afstd.sqlitecommander.app.utility.SettingsManager;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.post(new Runnable()
        {
            @Override
            public void run()
            {
                TextView tvVersion = (TextView) findViewById(R.id.tvVersion);
                tvVersion.setText(getString(R.string.version, AndroidUtility.getVersionName(MainActivity.this.getApplicationContext())));
            }
        });
        navigationView.setCheckedItem(R.id.nav_overview);
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_overview));

        Account account = AMUtility.getAccount(SettingsManager.getActiveAccount());
        if(account != null)
        {
            ContentResolver.addPeriodicSync(account, getString(R.string.content_authority), Bundle.EMPTY, SSyncAdapter.SYNC_ADAPTER_INTERVAL);
            ContentResolver.setSyncAutomatically(account, getString(R.string.content_authority), true);
        }
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings)
        {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    public boolean onNavigationItemSelected(@IdRes int menuId)
    {
        navigationView.setCheckedItem(menuId);
        return onNavigationItemSelected(navigationView.getMenu().findItem(menuId));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_sqlite)
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, FragmentSQLite.newInstance());
            transaction.commit();
        }
        else if (id == R.id.nav_overview)
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, FragmentOverview.newInstance());
            transaction.commit();
        }
        else if (id == R.id.nav_cloud)
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, FragmentCloud.newInstance());
            transaction.commit();
        }
        else if (id == R.id.nav_mysql)
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, FragmentMySQL.newInstance());
            transaction.commit();
        }
        else if (id == R.id.nav_postgresql)
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, FragmentPostgreSQL.newInstance());
            transaction.commit();
        }
        else if (id == R.id.nav_history)
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, FragmentHistoryFavorites.newInstance(FragmentHistoryFavorites.TYPE_HISTORY));
            transaction.commit();
        }
        else if (id == R.id.nav_favorites)
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, FragmentHistoryFavorites.newInstance(FragmentHistoryFavorites.TYPE_FAVORITES));
            transaction.commit();
        }
        else if (id == R.id.nav_settings)
        {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        /*else if (id == R.id.nav_manage)
        {

        }
        else if (id == R.id.nav_share)
        {

        }
        else if (id == R.id.nav_send)
        {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
