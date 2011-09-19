package com.inc.im.serptracker;

import java.util.ArrayList;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.R;
import com.inc.im.serptracker.data.DbAdapter;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.data.UserProfile;
import com.inc.im.serptracker.util.AsyncDownloader;
import com.inc.im.serptracker.util.AsyncDownloaderInhouseAds;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ArrayList<UserProfile> data;
	public static String EMPTY_SPINNER_TEXT;
	private Boolean menuBarIsVisible = true;
	private AsyncDownloader downloader;
	private Long counterForExtensiveClickLimit = 0L;

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

		// load in house ads
		AsyncDownloaderInhouseAds ads = new AsyncDownloaderInhouseAds(
				(ProgressBar) findViewById(R.id.progressBar1),
				((TextView) findViewById(R.id.inhouseAdsText)),
				((LinearLayout) findViewById(R.id.inhouseAds)),
				MainActivity.this);

		ads.execute(getString(R.string.ad_text_input_path));

		EMPTY_SPINNER_TEXT = getString(R.string.spinner_default_selection);

		// init spinner + loads data form db
		initSpinner();

		bindSpinnerItemOnSelectEvent();
		bindRunButton();
		bindMenuBarButtons();

	}

	@Override
	protected void onResume() {
		super.onResume();

		initSpinner();

		// clear listview
		// if (data == null)
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

//	@Override
//	public void onBackPressed() {
//		downloader.cancel(true);
//		super.onBackPressed();
//	}

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

		ArrayList<String> spinnerValues = new ArrayList<String>();

		spinnerValues.add(EMPTY_SPINNER_TEXT);

		// generate spinner values
		if (data != null)
			for (UserProfile u : data)
				spinnerValues.add(u.url + "[" + u.keywords.size() + "]");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getBaseContext(), R.layout.main_activity_spinner_item,
				R.id.textView1, spinnerValues);

		((Spinner) findViewById(R.id.spinner_profile)).setAdapter(adapter);

	}

	private void bindRunButton() {

		Button btn = (Button) findViewById(R.id.button_run);

		btn.setOnClickListener(new View.OnClickListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void onClick(View v) {

				if (!internetConnectionExists()) {
					Toast.makeText(
							getBaseContext(),
							getString(R.string.no_active_internet_connection_please_check_your_settings),
							Toast.LENGTH_LONG).show();

					return;
				}

				// default not selected
				if (!getSpinnerSelectedValue().equals(EMPTY_SPINNER_TEXT)) {
					// limit requests
					// if (counterForExtensiveClickLimit == 0l) {
					// counterForExtensiveClickLimit = System
					// .currentTimeMillis() / 1000;
					// } else {
					// Long currentTime = System.currentTimeMillis() / 1000;
					// if (currentTime - counterForExtensiveClickLimit < 60)
					// Toast.makeText(getBaseContext(),
					// "Results are already up to date",
					// Toast.LENGTH_SHORT).show();
					// }

					// counter for rate-us-dialog
					if (rateUsDialog(rateDialogCounter()))
						return;

					ArrayList<Keyword> keywords = null;

					int spinnerSelectedValueNr = getSpinnerSelectedIndex() - 1;

					// find keywords by name
					for (UserProfile u : data) {
						if (u.url.equals(data.get(spinnerSelectedValueNr).url)
								&& u.id == data.get(spinnerSelectedValueNr).id)
							keywords = u.keywords;
					}

					if (keywords != null) {

						// start dialog
						ProgressDialog dialog = ProgressDialog.show(
								MainActivity.this,
								getString(R.string.inspect_dialog_title),
								getString(R.string.inspect_dialog_fist_msg),
								true, true);

						// start download
						downloader = new AsyncDownloader(getBaseContext(),
								(ListView) findViewById(R.id.listview_result),
								data.get(spinnerSelectedValueNr).url, dialog);

						downloader.execute(keywords);
					}

					else {
						Toast.makeText(
								getBaseContext(),
								getString(R.string.profile_keywords_are_missing_how_s_thats_possible_),
								Toast.LENGTH_SHORT).show();
					}

				} else {

					Toast.makeText(getBaseContext(),
							getString(R.string.please_select_a_profile),
							Toast.LENGTH_SHORT).show();

				} // if empty selected
			}
		});
	}

	public int rateDialogCounter() {

		// read
		SharedPreferences settings = getSharedPreferences("minuPref", 0);
		int number = settings.getInt("minuMuutuja", 0);
		int newNumber = number + 1;

		Log.d("MY", number + "");
		// write
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings2 = getSharedPreferences("minuPref", 0);
		SharedPreferences.Editor editor = settings2.edit();
		editor.putInt("minuMuutuja", newNumber);

		// Commit the edits!
		editor.commit();

		return number;
		// final String PREFS_NAME = "RateDialogCounter2";
		//
		// SharedPreferences counter = PreferenceManager
		// .getDefaultSharedPreferences(getBaseContext());
		//
		// int oldValue = counter.getInt(PREFS_NAME, 0);
		// Log.d("MY", "old value" + oldValue+"");
		// int newValue = oldValue++;
		//
		// // SharedPreferences.Editor editor = PreferenceManager
		// // .getDefaultSharedPreferences(getBaseContext()).edit();
		// SharedPreferences.Editor editor = counter.edit();
		// editor.putInt(PREFS_NAME, newValue);
		// editor.commit();
		//
		// Log.d("MY", newValue+"");
		//
		// return newValue;
	}

	private Boolean rateUsDialog(int timesClicked) {

		Boolean displayed = false;
		if (timesClicked == 15 || timesClicked == 50) {
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

	public void bindSpinnerItemOnSelectEvent() {
		// find spinner selected url

		Spinner s = (Spinner) findViewById(R.id.spinner_profile);

		s.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapter, View arg1,
					int index, long arg3) {

				// if deafult selected - clear listview
				if (index == 0) {
					((ListView) findViewById(R.id.listview_result))
							.setAdapter(new ArrayAdapter<String>(
									getBaseContext(),
									R.layout.main_activity_listview_item));
				}

				bindListViewItems(index);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
	}

	public void bindListViewItems(int spinnerSelectedItemIndex) {

		// index 0 is reserved for "Select a item..."
		if (spinnerSelectedItemIndex > 0) {
			UserProfile selectedUser = data.get(spinnerSelectedItemIndex - 1);

			ArrayList<String> keywordsToBind = new ArrayList<String>();

			if (selectedUser.keywords != null) {
				for (Keyword k : selectedUser.keywords) {

					if (k.rank == -1)
						keywordsToBind.add(k.value + " [not ranked] ");
					else
						keywordsToBind.add(k.value + " [" + k.rank + "] ");

				}

				ArrayAdapter<String> a = new ArrayAdapter<String>(
						getBaseContext(), R.layout.main_activity_listview_item,
						R.id.textView1, keywordsToBind);

				((ListView) findViewById(R.id.listview_result)).setAdapter(a);
			}
		}

	}

	public String getSpinnerSelectedValue() {
		return ((Spinner) findViewById(R.id.spinner_profile)).getSelectedItem()
				.toString();

	}

	public int getSpinnerSelectedIndex() {
		return ((Spinner) findViewById(R.id.spinner_profile))
				.getSelectedItemPosition();

	}

	public boolean internetConnectionExists() {

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm == null)
			return false;

		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		} else {
			return false;
		}

	}

}