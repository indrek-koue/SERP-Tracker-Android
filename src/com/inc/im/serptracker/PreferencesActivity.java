package com.inc.im.serptracker;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.R;
import com.inc.im.serptracker.adapters.DbAdapter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class PreferencesActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@Override
	public void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, getString(R.string.flurry_api_key));
	}

	@Override
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.app_preferences_layout);

		bindDeleteAllDataButton();
		bindSelectSearchEngine();
		bindSelectRegion();

	}

	@Override
	protected void onResume() {

		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

		findPreference("prefLocalize").setSummary(
				PreferenceManager.getDefaultSharedPreferences(getBaseContext())
						.getString("prefLocalize", "Google.com"));

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

		pref.setSummary(pref.getValue());

		Toast.makeText(getBaseContext(), "SELECTED: " + pref.getValue(),
				Toast.LENGTH_SHORT).show();

		this.onContentChanged();

	}

	public void bindSelectSearchEngine() {
		getPreferenceManager().findPreference("prefSearchEngine")
				.setOnPreferenceClickListener(
						new Preference.OnPreferenceClickListener() {

							@Override
							public boolean onPreferenceClick(
									Preference preference) {
								Toast.makeText(
										getBaseContext(),
										getString(R.string.google_is_trademark_of_google_inc_this_application_is_built_on_google_custom_search_api_and_does_not_infringe_google_search_terms_of_service_),
										Toast.LENGTH_LONG).show();

								return false;
							}
						});
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

									Toast.makeText(
											getBaseContext(),
											"is premium, value saved: "
													+ newValue.toString(),
											Toast.LENGTH_SHORT).show();

									return true;

								} else {
									Toast.makeText(
											getBaseContext(),
											"This feature is available in the premium version. Would you like to buy the premium version?",
											Toast.LENGTH_SHORT).show();
									return false;
								}

							}
						});
	}

	// getPreferenceManager().findPreference("prefLocalize")
	// .setOnPreferenceClickListener(
	// new Preference.OnPreferenceClickListener() {
	//
	// @Override
	// public boolean onPreferenceClick(
	// Preference preference) {
	//
	// if (Boolean
	// .parseBoolean(getString(R.string.isPremium))) {
	// // allow selecting
	//
	// Toast.makeText(
	// getBaseContext(),
	// getString(R.string.google_is_trademark_of_google_inc_this_application_is_built_on_google_custom_search_api_and_does_not_infringe_google_search_terms_of_service_),
	// Toast.LENGTH_LONG).show();
	//
	// } else {
	// Toast.makeText(
	// getBaseContext(),
	// "This feature is available in the premium version. Would you like to buy the premium version?",
	// Toast.LENGTH_SHORT).show();
	// }
	//
	// return false;
	// }
	// });
	// }

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
