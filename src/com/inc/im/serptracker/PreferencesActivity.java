package com.inc.im.serptracker;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.R;
import com.inc.im.serptracker.data.DbAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class PreferencesActivity extends PreferenceActivity {

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

		getPreferenceManager().findPreference("prefTrunk")
				.setOnPreferenceClickListener(
						new Preference.OnPreferenceClickListener() {

							@Override
							public boolean onPreferenceClick(
									Preference preference) {
								new DbAdapter(getBaseContext()).trunkTables();

								Toast.makeText(getBaseContext(),
										getString(R.string.all_user_data_deleted),
										Toast.LENGTH_SHORT).show();

								startActivity(new Intent(getBaseContext(),
										MainActivity.class));

								return false;
							}
						});

	}

}
