package com.inc.im.serptracker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.bugsense.trace.BugSenseHandler;
import com.flurry.android.FlurryAgent;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.inc.im.serptracker.R;
import com.inc.im.serptracker.adapters.DbAdapter;
import com.inc.im.serptracker.adapters.MainActivityListAdapter;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.data.UserProfile;
import com.inc.im.serptracker.data.access.AsyncDownloader;
import com.inc.im.serptracker.util.MainActivityHelper;
import com.inc.im.serptracker.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ArrayList<UserProfile> data;
	private Boolean menuBarIsVisible = true;
	private AdView adView;

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

		// BugSenseHandler.setup(this, "dd278c2d");

		adView = Util.loadAdmob(this);

		// inhouse notification
		Util.loadInHouseAds(((LinearLayout) findViewById(R.id.inhouseAds)),
				((TextView) findViewById(R.id.inhouseAdsText)),
				MainActivity.this, getString(R.string.ad_text_input_path), true);

		// init spinner + loads data form db
		initSpinner();

		// bind buttons
		bindRunButton();
		MainActivityHelper.bindMenuBarButtons(this);

	}

	@Override
	protected void onDestroy() {
		adView.destroy();
		super.onDestroy();
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

		LinearLayout lv = (LinearLayout) findViewById(R.id.menuBar);

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

							int spinnerSelectedValueNr = getSpinnerSelectedIndex() - 1;

							if (data == null
									|| spinnerSelectedValueNr >= data.size()
									|| spinnerSelectedValueNr < 0)
								return;

							// find keywords by name
							ArrayList<Keyword> keywords = MainActivityHelper
									.findKeywordsByName(data,
											spinnerSelectedValueNr);

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

		ListView lv = (ListView) findViewById(R.id.listview_result);

		if (spinnerSelectedItemIndex > 0 && data != null
				&& spinnerSelectedItemIndex <= data.size()) {

			// -1 because 0 index holds default text
			final UserProfile selectedUser = data
					.get(spinnerSelectedItemIndex - 1);

			lv.setAdapter(new MainActivityListAdapter(getBaseContext(),
					selectedUser.keywords));

			addVerifyUserRankingPremium(lv, selectedUser);

			// MainActivityHelper.bindResultListView(this, listView,
			// selectedUser.keywords);
		} else {

			// clear
			lv.setAdapter(new ArrayAdapter<String>(getBaseContext(),
					R.layout.main_activity_listview_item));
		}

	}

	private void addVerifyUserRankingPremium(ListView lv,
			final UserProfile selectedUser) {
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				Keyword selectedKeyword = selectedUser.keywords.get(arg2);

				Toast.makeText(getBaseContext(),
						"Verify link ranking with web browser",
						Toast.LENGTH_LONG).show();

				try {
					Toast.makeText(
							getBaseContext(),
							"www.google.com/seach?q="
									+ URLEncoder.encode(
											selectedKeyword.keyword,
											"UTF-8"), Toast.LENGTH_LONG)
							.show();
				} catch (UnsupportedEncodingException e) {
					Log.e("MY", e.toString());
					e.printStackTrace();
				}

				// Toast.makeText(
				// getBaseContext(),
				// "Note: "
				// + selectedUser.url
				// + "is highlighted using system default theme",
				// Toast.LENGTH_LONG).show();

				Intent i = new Intent(MainActivity.this,
						VerifyWebView.class);
				i.putExtra("keyword", selectedKeyword.keyword);
				i.putExtra("url", selectedUser.url);

				startActivity(i);

			}
		});
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