package com.inc.im.serptracker;

import java.util.ArrayList;

import com.bugsense.trace.BugSenseHandler;
import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.R;
import com.inc.im.serptracker.data.DbAdapter;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.data.UserProfile;
import com.inc.im.serptracker.util.AsyncDownloader;
import com.inc.im.serptracker.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ArrayList<UserProfile> data;
	private Boolean menuBarIsVisible = true;

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity_layout);

		// bugsense error tracking
		// TODO: temp remove BugSenseHandler.setup(this, "dd278c2d");

		Util.loadInHouseAds(((LinearLayout) findViewById(R.id.inhouseAds)),
				((TextView) findViewById(R.id.inhouseAdsText)),
				MainActivity.this, getString(R.string.ad_text_input_path),
				false);

		// init spinner + loads data form db
		initSpinner();

		bindRunButton();
		MainActivityHelper.bindMenuBarButtons(this);

	}

	@Override
	protected void onResume() {
		super.onResume();

		// load default value on spinner
		initSpinner();

		// clear listview
		((ListView) findViewById(R.id.listview_result))
				.setAdapter(new ArrayAdapter<String>(getBaseContext(),
						R.layout.main_activity_listview_item));

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		LinearLayout lv = (LinearLayout) findViewById(R.id.linearLayout2);

		if (keyCode == KeyEvent.KEYCODE_MENU)
			if (menuBarIsVisible) {
				lv.setVisibility(View.GONE);
				menuBarIsVisible = false;
			} else {
				lv.setVisibility(View.VISIBLE);
				menuBarIsVisible = true;
			}

		return super.onKeyDown(keyCode, event);
	}

	public void initSpinner() {

		data = new DbAdapter(getBaseContext()).loadAllProfiles();
		Spinner spinner = (Spinner) findViewById(R.id.spinner_profile);

		ArrayList<String> spinnerValues = new ArrayList<String>();
		spinnerValues.add(getString(R.string.spinner_default_selection));

		// generate spinner values
		if (data != null)
			for (UserProfile u : data)
				spinnerValues.add(u.url + "[" + u.keywords.size() + "]");

		spinner.setAdapter(new ArrayAdapter<String>(getBaseContext(),
				R.layout.main_activity_spinner_item, R.id.textView1,
				spinnerValues));

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapter, View arg1,
					int index, long arg3) {

				bindListViewItems(index);

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
	}

	private void bindRunButton() {

		((Button) findViewById(R.id.button_run))
				.setOnClickListener(new View.OnClickListener() {

					@SuppressWarnings("unchecked")
					@Override
					public void onClick(View v) {

						if (!Util.internetConnectionExists(getBaseContext())) {
							Toast.makeText(
									getBaseContext(),
									getString(R.string.no_active_internet_connection_please_check_your_settings),
									Toast.LENGTH_LONG).show();

							return;
						}

						// default not selected
						if (!getSpinnerSelectedValue().equals(
								getString(R.string.spinner_default_selection))) {

							// counter for rate-us-dialog
							if (MainActivityHelper.rateUsDialog(
									MainActivity.this, 15, 50))
								return;

							ArrayList<Keyword> keywords = null;

							int spinnerSelectedValueNr = getSpinnerSelectedIndex() - 1;

							if (data == null
									|| spinnerSelectedValueNr >= data.size()
									|| spinnerSelectedValueNr < 0)
								return;

							// find keywords by name
							for (UserProfile u : data) {
								if (data.get(spinnerSelectedValueNr) != null)
									if (u.url.equals(data
											.get(spinnerSelectedValueNr).url)
											&& u.id == data
													.get(spinnerSelectedValueNr).id)
										keywords = u.keywords;
							}

							if (keywords != null) {

								// start download
								new AsyncDownloader(
										MainActivity.this,
										(ListView) findViewById(R.id.listview_result),
										data.get(spinnerSelectedValueNr).url)
										.execute(keywords);
							}

							else {
								Toast.makeText(
										getBaseContext(),
										getString(R.string.profile_keywords_are_missing_how_s_thats_possible_),
										Toast.LENGTH_SHORT).show();
							}

						} else {

							Toast.makeText(
									getBaseContext(),
									getString(R.string.please_select_a_profile),
									Toast.LENGTH_SHORT).show();

						} // if empty selected
					}
				});
	}

	public void bindListViewItems(int spinnerSelectedItemIndex) {

		ListView listView = (ListView) findViewById(R.id.listview_result);

		if (spinnerSelectedItemIndex > 0 && data != null
				&& spinnerSelectedItemIndex <= data.size()) {

			// -1 because 0 index holds default text
			UserProfile selectedUser = data.get(spinnerSelectedItemIndex - 1);

			MainActivityHelper.bindResultListView(this, listView,
					selectedUser.keywords);
		} else {
			listView.setAdapter(new ArrayAdapter<String>(getBaseContext(),
					R.layout.main_activity_listview_item));
		}

	}

	public String getSpinnerSelectedValue() {

		Object o = ((Spinner) findViewById(R.id.spinner_profile))
				.getSelectedItem();
		if (o != null)
			return o.toString();
		else
			return "";

	}

	public int getSpinnerSelectedIndex() {

		return ((Spinner) findViewById(R.id.spinner_profile))
				.getSelectedItemPosition();

	}

}