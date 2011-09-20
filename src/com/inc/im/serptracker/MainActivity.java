package com.inc.im.serptracker;

import java.util.ArrayList;

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

		Util.loadInHouseAds(((LinearLayout) findViewById(R.id.inhouseAds)),
				((TextView) findViewById(R.id.inhouseAdsText)),
				MainActivity.this, getString(R.string.ad_text_input_path),
				false);

		// init spinner + loads data form db
		initSpinner();

		bindRunButton();
		bindMenuBarButtons();

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

	private void bindMenuBarButtons() {

		// insert website
		View.OnClickListener insertProfileListener = new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(getBaseContext(),
						InsertWebsiteActivity.class));

			}
		};

		((LinearLayout) findViewById(R.id.linearLayoutAddProfile))
				.setOnClickListener(insertProfileListener);
		((ImageButton) findViewById(R.id.imageButton1))
				.setOnClickListener(insertProfileListener);
		((TextView) findViewById(R.id.textView1))
				.setOnClickListener(insertProfileListener);

		// manage websites
		View.OnClickListener manageProfilesListener = new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (new DbAdapter(getBaseContext()).loadAllProfiles() == null) {
					Toast.makeText(
							getBaseContext(),
							getString(R.string.no_websites_to_manage_please_add_a_website),
							Toast.LENGTH_SHORT).show();
				} else {
					startActivity(new Intent(getBaseContext(),
							ManageWebsitesActivity.class));
				}

			}
		};

		((LinearLayout) findViewById(R.id.linearLayoutManageProfiles))
				.setOnClickListener(manageProfilesListener);
		((ImageButton) findViewById(R.id.imageButton2))
				.setOnClickListener(manageProfilesListener);
		((TextView) findViewById(R.id.textView2))
				.setOnClickListener(manageProfilesListener);

		// settings
		View.OnClickListener settingsListener = new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				startActivity(new Intent(getBaseContext(),
						PreferencesActivity.class));

			}
		};

		((LinearLayout) findViewById(R.id.linearLayoutSettings))
				.setOnClickListener(settingsListener);
		((ImageButton) findViewById(R.id.imageButton3))
				.setOnClickListener(settingsListener);
		((TextView) findViewById(R.id.textView3))
				.setOnClickListener(settingsListener);

		// about
		View.OnClickListener aboutListener = new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				startActivity(new Intent(getBaseContext(), AboutActivity.class));

			}
		};

		((LinearLayout) findViewById(R.id.linearLayoutAbout))
				.setOnClickListener(aboutListener);
		((ImageButton) findViewById(R.id.imageButton4))
				.setOnClickListener(aboutListener);
		((TextView) findViewById(R.id.textView4))
				.setOnClickListener(aboutListener);

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
							if (rateUsDialog(15, 50))
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

	private Boolean rateUsDialog(int firstCap, int secondCap) {

		// read
		SharedPreferences settings = getSharedPreferences("minuPref", 0);
		int loadedCountFromPref = settings.getInt("minuMuutuja", 0);
		int newNumber = loadedCountFromPref + 1;

		Log.d("MY", loadedCountFromPref + "");
		// write
		SharedPreferences settings2 = getSharedPreferences("minuPref", 0);
		SharedPreferences.Editor editor = settings2.edit();
		editor.putInt("minuMuutuja", newNumber);
		editor.commit();

		Boolean displayed = false;
		if (loadedCountFromPref == firstCap || loadedCountFromPref == secondCap) {
			displayed = true;
			AlertDialog.Builder builder = new AlertDialog.Builder(
					MainActivity.this);
			builder.setMessage(
					getString(R.string.like_this_app_rate_us_on_android_market_))
					.setCancelable(false)
					.setPositiveButton(getString(R.string.sure_),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
									Intent intent = new Intent(
											Intent.ACTION_VIEW);
									intent.setData(Uri
											.parse(getString(R.string.market_details_id_com_inc_im_serptracker)));
									startActivity(intent);

								}
							})
					.setNegativeButton(getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									dialog.dismiss();

								}
							});
			AlertDialog alert = builder.create();
			alert.show();

		}

		return displayed;
	}

	public void bindListViewItems(int spinnerSelectedItemIndex) {

		ListView listView = (ListView) findViewById(R.id.listview_result);

		if (spinnerSelectedItemIndex > 0 && data != null
				&& spinnerSelectedItemIndex <= data.size()) {

			// -1 because 0 index holds default text
			UserProfile selectedUser = data.get(spinnerSelectedItemIndex - 1);

			ArrayList<String> keywordsToBind = new ArrayList<String>();

			if (selectedUser.keywords != null) {
				for (Keyword k : selectedUser.keywords) {

					// 0 - empty field in DB
					// -1 - not ranked
					// -2 - error

					if (k.rank == -1 || k.rank == -2 || k.rank == 0)
						keywordsToBind.add(k.value + " ["
								+ getString(R.string.not_ranked) + "]");
					else
						keywordsToBind.add(k.value + " [" + k.rank + "] ");
				}

				listView.setAdapter(new ArrayAdapter<String>(getBaseContext(),
						R.layout.main_activity_listview_item, R.id.textView1,
						keywordsToBind));

			}
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