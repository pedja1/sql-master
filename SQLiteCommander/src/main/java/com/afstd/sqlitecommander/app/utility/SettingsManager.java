package com.afstd.sqlitecommander.app.utility;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.afstd.sqlitecommander.app.App;
import com.afstd.sqlitecommander.app.BuildConfig;
import com.afstd.sqlitecommander.app.R;
import com.afstd.syntaxhighlight.Theme;

import static com.afstd.sqlitecommander.app.SettingsActivity.PrefsFragment.THEME_DEFAULT;
import static com.afstd.sqlitecommander.app.SettingsActivity.PrefsFragment.THEME_DJANGO;


/**
 * Created by pedja on 2/12/14.
 * Handles all reads and writes to SharedPreferences
 *
 * @author Predrag Čokulov
 */
public class SettingsManager
{
    private static final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.get());

    public enum KEY
    {
        DEBUG, active_account, last_sync_time, syntax_highlight_theme;

        private String mValue;

        KEY(String mValue)
        {
            this.mValue = mValue;
        }

        KEY()
        {
        }

        @Override
        public String toString()
        {
            return mValue == null ? super.toString() : mValue;
        }
    }

    public enum SyncKey
    {
        sync_enabled(true, R.id.cbSyncEnabled), sync_history(true, R.id.cbSyncHistory),
        sync_query_history(true, R.id.cbSyncQueryHistory), sync_favorites(true, R.id.cbSyncFavorites),
        sync_mysql(false, R.id.cbSyncMySql), sync_settings(true, R.id.cbSyncSettings);

        private boolean syncDefault;
        private int checkId;

        SyncKey(boolean syncDefault, int checkId)
        {
            this.syncDefault = syncDefault;
            this.checkId = checkId;
        }

        public boolean getSyncDefault()
        {
            return syncDefault;
        }

        public int getCheckId()
        {
            return checkId;
        }

        public static SyncKey fromViewId(int viewId)
        {
            for(SyncKey key : values())
            {
                if(key.checkId == viewId)
                    return key;
            }
            return null;
        }
    }

    public static boolean DEBUG()
    {
        return prefs.getBoolean(KEY.DEBUG.toString(), BuildConfig.DEBUG);
    }

    public static void DEBUG(boolean debug)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY.DEBUG.toString(), debug);
        editor.apply();
    }

    public static boolean getSyncSetting(SyncKey key)
    {
        return prefs.getBoolean(key.toString(), key.getSyncDefault());
    }

    public static void setSyncSetting(SyncKey key, boolean enabled)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key.toString(), enabled);
        editor.apply();
    }

    public static String getActiveAccount()
    {
        return prefs.getString(KEY.active_account.toString(), null);
    }

    public static void setActiveAccount(String account)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY.active_account.toString(), account);
        editor.apply();
    }

    public static long getLastSyncTime()
    {
        return prefs.getLong(KEY.last_sync_time.toString(), 0);
    }

    public static void setLastSyncTime(long lastSyncTime)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY.last_sync_time.toString(), lastSyncTime);
        editor.apply();
    }

    public static String getSyntaxHighlightThemeKey()
    {
        return prefs.getString(KEY.syntax_highlight_theme.toString(), "default");
    }

    public static Theme getSyntaxHighlightTheme()
    {
        String key = getSyntaxHighlightThemeKey();
        switch (key)
        {
            case "default":
                return THEME_DEFAULT;
            case "django":
                return THEME_DJANGO;
        }
        return null;
    }

    public static void clearAllPrefs()
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}
