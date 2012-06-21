
package com.inc.im.serptracker;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptrackerpremium.R;
import com.inc.im.serptracker.adapters.DbAdapter;
import com.inc.im.serptracker.util.Premium;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class PreferencesActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    // public static String MODE_ACCURATE = "1";
    // public static String MODE_FAST = "2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_preferences_layout);

        bindDeleteAllDataButton();
        // bindSelectSearchEngine();
        bindSelectRegion();
        bindSelectUserAgent();

        bindMode();

    }

    private void bindMode() {
        ListPreference lp = (ListPreference) getPreferenceManager().findPreference("prefMode");

//        lp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                Toast.makeText(PreferencesActivity.this,
//                        PreferencesActivity.this.getString(R.string.preference_mode_summary),
//                        Toast.LENGTH_LONG).show();
//                return false;
//            }
//        });

        String[] values = new String[] {
                getString(R.string.preferences_select_mode_option_accurate),
                getString(R.string.preferences_select_mode_option_fast)
        };
        lp.setEntries(values);
        lp.setEntryValues(values);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (new Boolean(getString(R.string.isPremium)))
            FlurryAgent.onStartSession(this,
                    getString(R.string.flurry_api_key_premium));
        else
            FlurryAgent
                    .onStartSession(this, getString(R.string.flurry_api_key));
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    @Override
    protected void onResume() {

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        findPreference("prefLocalize").setSummary(
                PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                        .getString("prefLocalize", "Google.com"));

//        findPreference("prefUa").setSummary(
//                PreferenceManager.getDefaultSharedPreferences(getBaseContext())
//                        .getString("prefUa", "Google Chrome"));
//
//        findPreference("prefMode").setSummary(
//                PreferenceManager.getDefaultSharedPreferences(getBaseContext())
//                        .getString("prefMode",
//                                getString(R.string.preferences_select_mode_option_accurate)));

        super.onResume();
    }

    @Override
    protected void onPause() {

        // deregister listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {

        ListPreference pref = (ListPreference) findPreference(key);

        pref.setSummary(pref.getEntry());

        this.onContentChanged();

    }

    public void bindSelectUserAgent() {

        getPreferenceManager().findPreference("prefUa")
                .setOnPreferenceChangeListener(
                        new Preference.OnPreferenceChangeListener() {

                            @Override
                            public boolean onPreferenceChange(
                                    Preference preference, Object newValue) {

                                if (Boolean
                                        .parseBoolean(getString(R.string.isPremium))) {

                                    return true;

                                } else {

                                    Premium.showBuyPremiumDialog(
                                            getString(R.string.selecting_locale_is_available_in_the_premium_version_would_you_like_to_buy_the_premium_version_),
                                            PreferencesActivity.this);

                                    return false;
                                }

                            }
                        });

//        getPreferenceManager().findPreference("prefUa").setOnPreferenceClickListener(
//                new OnPreferenceClickListener() {
//
//                    @Override
//                    public boolean onPreferenceClick(Preference preference) {
//                        Toast.makeText(
//                                PreferencesActivity.this,
//                                PreferencesActivity.this.getString(
//                                        R.string.preferences_browser_summary),
//                                Toast.LENGTH_LONG).show();
//                        return false;
//                    }
//                });

    }

    public void bindSelectRegion() {

        getPreferenceManager().findPreference("prefLocalize")
                .setOnPreferenceChangeListener(
                        new Preference.OnPreferenceChangeListener() {

                            @Override
                            public boolean onPreferenceChange(
                                    Preference preference, Object newValue) {

                                if (Boolean
                                        .parseBoolean(getString(R.string.isPremium))) {

                                    return true;

                                } else {

                                    Premium.showBuyPremiumDialog(
                                            getString(R.string.selecting_locale_is_available_in_the_premium_version_would_you_like_to_buy_the_premium_version_),
                                            PreferencesActivity.this);

                                    return false;
                                }

                            }
                        });
    }

    public void bindDeleteAllDataButton() {
        getPreferenceManager().findPreference("prefTrunk")
                .setOnPreferenceClickListener(
                        new Preference.OnPreferenceClickListener() {

                            @Override
                            public boolean onPreferenceClick(
                                    Preference preference) {
                                new DbAdapter(getBaseContext()).trunkTables();

                                Toast.makeText(
                                        getBaseContext(),
                                        getString(R.string.all_user_data_deleted),
                                        Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(getBaseContext(),
                                        MainActivity.class));

                                return false;
                            }
                        });
    }

}
