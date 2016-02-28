package com.afstd.sqlitecommander.app;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.af.androidutility.lib.AndroidUtility;
import com.afstd.sqlitecommander.app.acm.AMUtility;
import com.afstd.sqlitecommander.app.acm.SSyncAdapter;
import com.afstd.sqlitecommander.app.bus.RemoveAdsEvent;
import com.afstd.sqlitecommander.app.fragment.FragmentCloud;
import com.afstd.sqlitecommander.app.fragment.FragmentFavorites;
import com.afstd.sqlitecommander.app.fragment.FragmentHistory;
import com.afstd.sqlitecommander.app.fragment.FragmentMSSQL;
import com.afstd.sqlitecommander.app.fragment.FragmentMySQL;
import com.afstd.sqlitecommander.app.fragment.FragmentOverview;
import com.afstd.sqlitecommander.app.fragment.FragmentPostgreSQL;
import com.afstd.sqlitecommander.app.fragment.FragmentSQLite;
import com.afstd.sqlitecommander.app.model.DatabaseEntry;
import com.afstd.sqlitecommander.app.model.DatabaseSearchResult;
import com.afstd.sqlitecommander.app.sqlite.DatabaseManager;
import com.afstd.sqlitecommander.app.su.ShellInstance;
import com.afstd.sqlitecommander.app.utility.SettingsManager;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.crashlytics.android.Crashlytics;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import eu.chainfire.libsuperuser.Shell;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        SearchBox.SearchListener, BillingProcessor.IBillingHandler
{
    private static final String IAB_PRODUCT_ID = "remove_ads";
    private static final String STATE_NAVIGATION_SELECTED = "selected_navigation";
    private static final int REQUEST_CODE_SET_PASSWORD = 1004;
    private NavigationView navigationView;

    private SearchBox mSearchBox;
    private boolean passwordSet;

    private int checkedNavigationItem;

    private BillingProcessor mBillingProcessor;
    private final Object billingInitLock = new Object();
    private boolean iabSetupFinished;

    private ATGetPro atGetPro;
    private ATCheckRoot atCheckRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSearchBox = (SearchBox) findViewById(R.id.searchbox);
        mSearchBox.setSearchListener(this);

        List<DatabaseEntry> entries = DatabaseManager.getInstance().getDatabaseEntries("SELECT * FROM _database WHERE deleted != 1", new String[0]);
        for (DatabaseEntry entry : entries)
        {
            DatabaseSearchResult option = new DatabaseSearchResult(entry);
            mSearchBox.addSearchable(option);
        }

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
        if(savedInstanceState == null)
        {
            checkedNavigationItem = R.id.nav_overview;
        }
        else
        {
            checkedNavigationItem = savedInstanceState.getInt(STATE_NAVIGATION_SELECTED);
        }
        navigationView.setCheckedItem(checkedNavigationItem);
        onNavigationItemSelected(navigationView.getMenu().findItem(checkedNavigationItem));

        Account account = AMUtility.getAccount(SettingsManager.getActiveAccount());
        if (account != null)
        {
            ContentResolver.addPeriodicSync(account, getString(R.string.content_authority), Bundle.EMPTY, SSyncAdapter.SYNC_ADAPTER_INTERVAL);
            ContentResolver.setSyncAutomatically(account, getString(R.string.content_authority), true);
        }

        atCheckRoot = new ATCheckRoot(this);
        atCheckRoot.execute();

        boolean isAvailable = BillingProcessor.isIabServiceAvailable(this);
        if(isAvailable && !SettingsManager.isPro())
        {
            mBillingProcessor = new BillingProcessor(this, BuildConfig.BASE64_PUBLIC_KEY, this);
            iabSetupFinished = false;
            //every iab action will synchronize on billingInitLock
            // we lock it down so none can use iab before it is initialized
            lockIab();
        }
        else
        {
            navigationView.getMenu().findItem(R.id.nav_pro).setVisible(false);
        }
    }

    private void lockIab()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (!iabSetupFinished)
                {
                    synchronized (billingInitLock)
                    {
                        try
                        {
                            billingInitLock.wait();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(STATE_NAVIGATION_SELECTED, checkedNavigationItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy()
    {
        if(mBillingProcessor != null)
            mBillingProcessor.release();
        if(atGetPro != null)
            atGetPro.cancel(true);
        if(atCheckRoot != null)
            atCheckRoot.cancel(true);
        super.onDestroy();
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
        if (id == R.id.action_search)
        {
            mSearchBox.revealFromMenuItem(R.id.action_search, this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onNavigationItemSelected(@IdRes int menuId)
    {
        boolean ret = onNavigationItemSelected(navigationView.getMenu().findItem(menuId));
        if (ret)
            navigationView.setCheckedItem(menuId);
        return ret;
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
            if (TextUtils.isEmpty(SettingsManager.getEc()))
            {
                showSetPasswordDialog();
                return false;
            }
            else
            {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content, FragmentCloud.newInstance());
                transaction.commit();
            }
        }
        else if (id == R.id.nav_mysql)
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, FragmentMySQL.newInstance());
            transaction.commit();
        }
        else if (id == R.id.nav_mssql)
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, FragmentMSSQL.newInstance());
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
            transaction.replace(R.id.content, FragmentHistory.newInstance());
            transaction.commit();
        }
        else if (id == R.id.nav_favorites)
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, FragmentFavorites.newInstance());
            transaction.commit();
        }
        else if (id == R.id.nav_settings)
        {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        else if (id == R.id.nav_pro)
        {
            if (mBillingProcessor != null)
            {
                atGetPro = new ATGetPro(this);
                atGetPro.execute();
            }
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
        checkedNavigationItem = id;
        return true;
    }

    private void showSetPasswordDialog()
    {
        startActivityForResult(new Intent(this, SetPasswordActivity.class), REQUEST_CODE_SET_PASSWORD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == RESULT_OK)
        {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mSearchBox.populateEditText(matches.get(0));
        }
        if (requestCode == REQUEST_CODE_SET_PASSWORD && resultCode == RESULT_OK)
        {
            passwordSet = true;
        }
        if (mBillingProcessor == null || !mBillingProcessor.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSearchOpened()
    {

    }

    @Override
    public void onSearchCleared()
    {

    }

    @Override
    public void onSearchClosed()
    {
        mSearchBox.hideCircularly(this);
    }

    @Override
    public void onSearchTermChanged(String s)
    {

    }

    @Override
    public void onSearch(String s)
    {

    }

    @Override
    public void onResultClick(SearchResult searchResult)
    {
        DatabaseEntry entry = ((DatabaseSearchResult) searchResult).getEntry();
        if (DatabaseEntry.TYPE_SQLITE.equals(entry.type))
        {
            SQLiteCMDActivity.start(this, entry.databaseUri, true);
        }
        else if (DatabaseEntry.TYPE_MYSQL.equals(entry.type))
        {
            MySQLCMDActivity.start(this, entry.id);
        }
        else if (DatabaseEntry.TYPE_POSTGRESQL.equals(entry.type))
        {
            PostgreSQLCMDActivity.start(this, entry.id);
        }
        else if (DatabaseEntry.TYPE_MSSQL.equals(entry.type))
        {
            MSSQLCMDActivity.start(this, entry.id);
        }
        //TODO other types
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details)
    {
        if(IAB_PRODUCT_ID.equals(productId))
        {
            SettingsManager.setPro(true);
            EventBus.getDefault().post(new RemoveAdsEvent());
            navigationView.getMenu().findItem(R.id.nav_pro).setVisible(false);
        }
    }

    @Override
    public void onPurchaseHistoryRestored()
    {
        if(mBillingProcessor.isPurchased(IAB_PRODUCT_ID))
        {
            SettingsManager.setPro(true);
            EventBus.getDefault().post(new RemoveAdsEvent());
            navigationView.getMenu().findItem(R.id.nav_pro).setVisible(false);
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error)
    {
        //if not already initialized
        if (!mBillingProcessor.isInitialized())
        {
            //unlock iab, allow others to use it
            synchronized (billingInitLock)
            {
                iabSetupFinished = true;
                billingInitLock.notify();
            }
        }
        showError(errorCode);
    }

    private void showError(int response)
    {

        int errorMessage = -1;
        switch (response)
        {
            case 2:
                errorMessage = R.string.iab_unknown_error;
                break;
            case 5:
                errorMessage = R.string.iab_developer_error;
                break;
            case 6:
            case -1006:
            case -1008:
                errorMessage = R.string.iab_unknown_error;
                break;
            case 7:
                errorMessage = R.string.iab_item_owned;
                break;
            case 8:
                errorMessage = R.string.iab_item_not_owned;
                break;
            case 3:
                errorMessage = R.string.billing_unavailable;
                break;
            case 4:
                errorMessage = R.string.item_not_available;
                break;
            case -1001:
                errorMessage = R.string.iab_remote_error;
                break;
            case -1002:
                errorMessage = R.string.iab_bad_response;
                break;
            case -1003:
                errorMessage = R.string.iab_signature_error;
                break;
            case -1004:
                errorMessage = R.string.iab_send_intent_failed;
                break;
            case -1007:
                errorMessage = R.string.iab_missing_token;
                break;
            case -1009:
                errorMessage = R.string.iab_subscriptions_not_available;
                break;
            case -1010:
                errorMessage = R.string.iab_invalid_consumption_attempt;
                break;
        }
        if(errorMessage != -1)AndroidUtility.showToast(this, errorMessage);
    }


    @Override
    public void onBillingInitialized()
    {
        //unlock iab, allow others to use it
        synchronized (billingInitLock)
        {
            iabSetupFinished = true;
            billingInitLock.notify();
        }
        mBillingProcessor.loadOwnedPurchasesFromGoogle();
    }

    private static class ATCheckRoot extends AsyncTask<Void, Void, Boolean>
    {
        ProgressDialog progressDialog;
        private WeakReference<MainActivity> reference;

        ATCheckRoot(MainActivity activity)
        {
            this.reference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute()
        {
            if (reference.get() == null)
                return;
            progressDialog = new ProgressDialog(reference.get());
            progressDialog.setMessage(reference.get().getString(R.string.please_wait));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            Shell.SU.available();
            ShellInstance.getInstance();//this checks if su is available an creates su shell
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean)
        {
            if (reference.get() != null && progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    private static class ATGetPro extends AsyncTask<Void, Void, Boolean>
    {
        ProgressDialog progressDialog;
        private WeakReference<MainActivity> reference;

        ATGetPro(MainActivity activity)
        {
            this.reference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute()
        {
            if (reference.get() == null)
                return;
            progressDialog = new ProgressDialog(reference.get());
            progressDialog.setMessage(reference.get().getString(R.string.please_wait));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            if(reference.get() == null)
                return null;
            synchronized (reference.get().billingInitLock)
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean)
        {
            if (reference.get() != null && progressDialog.isShowing())
                progressDialog.dismiss();
            reference.get().mBillingProcessor.purchase(reference.get(), IAB_PRODUCT_ID);
        }
    }

    @Override
    protected void onResumeFragments()
    {
        if (passwordSet)
        {
            onNavigationItemSelected(R.id.nav_cloud);
            passwordSet = false;
        }
        super.onResumeFragments();

    }
}
