package com.afstd.sqlitecommander.app.utility;

import com.af.androidutility.lib.RandomString;
import com.afstd.sqlitecommander.app.BuildConfig;
import com.afstd.sqlitecommander.app.R;
import com.afstd.syntaxhighlight.Theme;

import net.grandcentrix.tray.AppPreferences;

import static com.afstd.sqlitecommander.app.SettingsActivity.PrefsFragment.THEME_DEFAULT;
import static com.afstd.sqlitecommander.app.SettingsActivity.PrefsFragment.THEME_DJANGO;
import static com.tehnicomsolutions.http.TSHttp.getContext;


/**
 * Created by pedja on 2/12/14.
 * Handles all reads and writes to SharedPreferences
 *
 * @author Predrag Čokulov
 */
public class SettingsManager
{
    /**
     * Tray preferences
     * multi-process is supported
     */
    private static final AppPreferences appPreferences = new AppPreferences(getContext());
    private static RandomString rs = new RandomString(32);

    public enum Key
    {
        DEBUG(true, boolean.class, BuildConfig.DEBUG), active_account(false, String.class, null),
        last_sync_time(true, long.class, 0L), syntax_highlight_theme(true, String.class, "default"),
        sync_notification(true, boolean.class, true), ec("T+Ieae0g1mHEcFa+tXGc/VnE9V47gKiqthrcVbLJfQo=", false, String.class, null),
        is_pro("7ZnSRoaMZ7iBmfDB6s5Y", false, String.class, rs.nextString());

        private final String mValue;
        private final boolean mSyncable;
        private final Class mClass;
        private final Object mDefault;

        Key(boolean syncable, Class _class, Object defaultValue)
        {
            this(null, syncable, _class, defaultValue);
        }

        Key(String mValue, boolean syncable, Class _class, Object defaultValue)
        {
            this.mValue = mValue;
            this.mSyncable = syncable;
            mClass = _class;
            this.mDefault = defaultValue;
        }

        public boolean isSyncable()
        {
            return mSyncable;
        }

        public Class getKeyClass()
        {
            return mClass;
        }

        public Object getDefault()
        {
            return mDefault;
        }

        @Override
        public String toString()
        {
            return mValue == null ? super.toString() : mValue;
        }
    }

    public enum SyncKey
    {
        sync_enabled(true, R.id.cbSyncEnabled, false), sync_databases(true, R.id.cbSyncDatabases, true),
        sync_query_history(true, R.id.cbSyncQueryHistory, true), sync_credentials(true, R.id.cbSyncCredentials, true),
        sync_settings(true, R.id.cbSyncSettings, true);

        private final boolean syncDefault;
        private final int checkId;
        private final boolean mSyncable;

        SyncKey(boolean syncDefault, int checkId, boolean syncable)
        {
            this.syncDefault = syncDefault;
            this.checkId = checkId;
            this.mSyncable = syncable;
        }

        public boolean getSyncDefault()
        {
            return syncDefault;
        }

        public int getCheckId()
        {
            return checkId;
        }

        public boolean isSyncable()
        {
            return mSyncable;
        }

        public static SyncKey fromViewId(int viewId)
        {
            for (SyncKey key : values())
            {
                if (key.checkId == viewId)
                    return key;
            }
            return null;
        }
    }

    public static boolean DEBUG()
    {
        return appPreferences.getBoolean(Key.DEBUG.toString(), BuildConfig.DEBUG);
    }

    public static void DEBUG(boolean debug)
    {
        appPreferences.put(Key.DEBUG.toString(), debug);
    }

    public static boolean getSyncSetting(SyncKey key)
    {
        return appPreferences.getBoolean(key.toString(), key.getSyncDefault());
    }

    public static void setSyncSetting(SyncKey key, boolean enabled)
    {
        appPreferences.put(key.toString(), enabled);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getSetting(Key key)
    {
        if(key == null)
            return null;
        if(key.getKeyClass() == boolean.class)
        {
            return (T)(Boolean)appPreferences.getBoolean(key.toString(), (boolean) key.getDefault());
        }
        else if(key.getKeyClass() == String.class)
        {
            return (T)appPreferences.getString(key.toString(), (String) key.getDefault());
        }
        else if(key.getKeyClass() == long.class)
        {
            return (T)(Long)appPreferences.getLong(key.toString(), (long) key.getDefault());
        }
        else if(key.getKeyClass() == int.class)
        {
            return (T)(Integer)appPreferences.getInt(key.toString(), (int) key.getDefault());
        }
        else if(key.getKeyClass() == float.class)
        {
            return (T)(Float)appPreferences.getFloat(key.toString(), (float) key.getDefault());
        }
        throw new IllegalStateException(String.format("key type '%s' is not supported", key.getDefault()));
    }

    @SuppressWarnings("ConstantConditions")
    public static void setSetting(Key key, Object value)
    {
        if(key.getKeyClass() == boolean.class)
        {
            appPreferences.put(key.toString(), (boolean) value);
        }
        else if(key.getKeyClass() == String.class)
        {
            appPreferences.put(key.toString(), (String) value);
        }
        else if(key.getKeyClass() == long.class)
        {
            appPreferences.put(key.toString(), (long) value);
        }
        else if(key.getKeyClass() == float.class)
        {
            appPreferences.put(key.toString(), (float) value);
        }
        else if(key.getKeyClass() == int.class)
        {
            appPreferences.put(key.toString(), (int) value);
        }
        //throw new IllegalStateException(String.format("key type '%s' is not supported", key.getDefault()));
    }

    public static String getActiveAccount()
    {
        return appPreferences.getString(Key.active_account.toString(), (String) Key.active_account.getDefault());
    }

    public static void setActiveAccount(String account)
    {
        appPreferences.put(Key.active_account.toString(), account);
    }

    public static long getLastSyncTime()
    {
        return appPreferences.getLong(Key.last_sync_time.toString(), (long) Key.last_sync_time.getDefault());
    }

    public static void setLastSyncTime(long lastSyncTime)
    {
        appPreferences.put(Key.last_sync_time.toString(), lastSyncTime);
    }

    public static String getSyntaxHighlightThemeKey()
    {
        return appPreferences.getString(Key.syntax_highlight_theme.toString(), (String) Key.syntax_highlight_theme.getDefault());
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

    public static boolean isShowSyncNotification()
    {
        return appPreferences.getBoolean(Key.sync_notification.toString(), (boolean) Key.sync_notification.getDefault());
    }

    public static void setShowSyncNotification(boolean show)
    {
        appPreferences.put(Key.sync_notification.toString(), show);
    }

    public static String getEc()
    {
        return appPreferences.getString(Key.ec.toString(), (String) Key.ec.getDefault());
    }

    public static void setEc(String ec)
    {
        appPreferences.put(Key.ec.toString(), ec);
    }

    public static final String LICENCE_IS_PRO = "EVvCZ7W7sNvaayDs6dyS";
    public static boolean isPro()
    {
        return LICENCE_IS_PRO.equals(appPreferences.getString(Key.is_pro.toString(), rs.nextString()));
    }

    public static void setPro(boolean pro)
    {
        appPreferences.put(Key.is_pro.toString(), pro ? LICENCE_IS_PRO : rs.nextString());
    }

    public static void clearAllPrefs()
    {
        appPreferences.clear();
    }
}
