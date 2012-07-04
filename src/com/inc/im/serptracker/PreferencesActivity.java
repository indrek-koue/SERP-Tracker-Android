package com.inc.im.serptracker;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.R;
import com.inc.im.serptracker.adapters.DbAdapter;
import com.inc.im.serptracker.util.Premium;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class PreferencesActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private static final String UA_DEFAULT_CHROME = "Chrome";
	private static final String LOCALE_DEFAULT_VALUE = "Google.com";
	public static final String PREF_TRUNK = "prefTrunk";
	public static final String PREF_LOCALIZE = "prefLocalize";
	public static final String PREF_UA = "prefUa";
	public static final String PREF_MODE = "prefMode";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.app_preferences_layout);

		// bindSelectSearchEngine();
		bindSelectRegion();
		bindSelectUserAgent();
		bindSelectMode();
		bindDeleteAllDataButton();

	}

	private void bindSelectMode() {

		ListPreference prefMode = (ListPreference) getPreferenceManager()
				.findPreference(PREF_MODE);

		prefMode.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {

				Boolean isPrem = isUserPremium();

				if (isPrem
						&& isSelectedModeAccurate(PreferencesActivity.this,
								newValue)) {

					String modeAccurate = getString(R.string.preferences_select_mode_option_accurate);
					String warn = String.format(getString(
							R.string.preference_mode_select_warning,
							modeAccurate));

					AlertDialog.Builder builder = new AlertDialog.Builder(
							PreferencesActivity.this);
					builder.setMessage(warn)
							.setCancelable(true)
							.setPositiveButton(
									PreferencesActivity.this
											.getString(R.string.OK),
									new OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											dialog.dismiss();
										}
									});

					builder.create().show();

				}

				return isPrem;

			}

			private boolean isSelectedModeAccurate(Activity a, Object newValue) {
				// TODO Auto-generated method stub

				String mode = (String) newValue;

				Log.i("MY", "mode: " + mode);

				if (mode.equals(a
						.getString(R.string.preferences_select_mode_option_accurate)))
					return true;
				else
					return false;
			}
		});

	}

	public void bindSelectUserAgent() {

		getPreferenceManager().findPreference(PREF_UA)
				.setOnPreferenceChangeListener(
						new Preference.OnPreferenceChangeListener() {

							@Override
							public boolean onPreferenceChange(
									Preference preference, Object newValue) {

								return isUserPremium();

							}
						});

	}

	public void bindSelectRegion() {
		getPreferenceManager().findPreference(PREF_LOCALIZE)
				.setOnPreferenceChangeListener(
						new Preference.OnPreferenceChangeListener() {

							@Override
							public boolean onPreferenceChange(
									Preference preference, Object newValue) {

								return isUserPremium();

							}
						});
	}

	public void bindDeleteAllDataButton() {
		getPreferenceManager().findPreference(PREF_TRUNK)
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

	private Boolean isUserPremium() {
		if (Boolean.parseBoolean(getString(R.string.isPremium))) {
			return true;
		} else {
			Premium.showBuyPremiumDialog(
					getString(R.string.available_in_the_premium_version_would_you_like_to_buy_the_premium_version_),
					PreferencesActivity.this);
			return false;
		}

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

		findPreference(PREF_LOCALIZE).setSummary(
				PreferenceManager.getDefaultSharedPreferences(getBaseContext())
						.getString(PREF_LOCALIZE, LOCALE_DEFAULT_VALUE));

		findPreference(PREF_MODE)
				.setSummary(
						PreferenceManager
								.getDefaultSharedPreferences(getBaseContext())
								.getString(
										PREF_MODE,
										this.getString(R.string.preferences_select_mode_option_fast)));

		findPreference(PREF_UA).setSummary(
				PreferenceManager.getDefaultSharedPreferences(getBaseContext())
						.getString(PREF_UA, UA_DEFAULT_CHROME));

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

}
