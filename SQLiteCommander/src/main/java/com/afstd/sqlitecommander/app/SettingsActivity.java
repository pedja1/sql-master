package com.afstd.sqlitecommander.app;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;

import com.afstd.sqlitecommander.app.utility.SQLTextHighlighter;
import com.afstd.sqlitecommander.app.utility.SettingsManager;
import com.afstd.sqlitecommander.app.view.SpannableListPreference;
import com.afstd.syntaxhighlight.Theme;
import com.afstd.syntaxhighlighter.theme.ThemeDefault;
import com.afstd.syntaxhighlighter.theme.ThemeDjango;

/**
 * Created by pedja on 17.7.15. 15.12.
 * This class is part of the SQL
 * Copyright © 2015 ${OWNER}
 */
public class SettingsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction().replace(R.id.flContent, new PrefsFragment()).commit();
    }

    public static class PrefsFragment extends PreferenceFragment
    {
        SQLTextHighlighter mHighlighter;

        private static final String DISPLAY_QUERY = "SELECT * FROM photo WHERE photo.id = 234;";
        public static final Theme THEME_DEFAULT = new ThemeDefault();
        public static final Theme THEME_DJANGO = new ThemeDjango();

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            mHighlighter = new SQLTextHighlighter();

            //final CheckBoxPreference cbpCyr = (CheckBoxPreference) findPreference("lang_cyr");
            //final CheckBoxPreference cbpLat = (CheckBoxPreference) findPreference("lang_lat");
            final SpannableListPreference lpFont = (SpannableListPreference) findPreference("syntax_highlight_theme");
            lpFont.setSummary(getSyntaxHighlightThemeDisplay(SettingsManager.getSyntaxHighlightThemeKey()));

            /*cbpCyr.setChecked(Language.SERBIAN_CYRILLIC.equals(SettingsManager.getLanguage()));
            cbpLat.setChecked(Language.SERBIAN_LATIN.equals(SettingsManager.getLanguage()));

            cbpCyr.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    if ((boolean) newValue)
                    {
                        SettingsManager.setLanguage(Language.SERBIAN_CYRILLIC);
                        cbpLat.setChecked(false);
                        cbpCyr.setChecked(true);
                        askToRestart();
                    }
                    return false;
                }
            });

            cbpLat.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    if ((boolean) newValue)
                    {
                        SettingsManager.setLanguage(Language.SERBIAN_LATIN);
                        cbpCyr.setChecked(false);
                        cbpLat.setChecked(true);
                        askToRestart();
                    }
                    return false;
                }
            });*/

            lpFont.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    lpFont.setSummary(getSyntaxHighlightThemeDisplay((String) newValue));
                    return true;
                }
            });
        }

        private Spannable getSyntaxHighlightThemeDisplay(String key)
        {
            if(key == null)
                return null;
            switch (key)
            {
                case "default":
                    return mHighlighter.highlight(DISPLAY_QUERY, THEME_DEFAULT);
                case "django":
                    return mHighlighter.highlight(DISPLAY_QUERY, THEME_DJANGO);
            }
            return new SpannableString(DISPLAY_QUERY);
        }
    }

}
