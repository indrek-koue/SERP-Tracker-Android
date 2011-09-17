package com.inc.im.serptracker;

import java.util.ArrayList;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.R;
import com.inc.im.serptracker.data.DbAdapter;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.data.UserProfile;
import com.inc.im.serptracker.util.AsyncDownloader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
	public final static String EMPTY_SPINNER_TEXT = "Select a website...";
	private Boolean menuBarIsVisible = true;
	private AsyncDownloader downloader;

	@Override
	public void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, "LCFV3QMWQDCW9VRBU14R");
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
		if (data == null)
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
					Toast.makeText(getBaseContext(),
							"No websites to manage. Please add a website",
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
							"No active internet connection. Please check your settings",
							Toast.LENGTH_LONG).show();

					return;
				}

				// default not selected
				if (!getSpinnerSelectedValue().equals(EMPTY_SPINNER_TEXT)) {

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
								MainActivity.this, "Inspecting keywords",
								"Starting, please wait.", true, true);

						// start download
						downloader = new AsyncDownloader(getBaseContext(),
								(ListView) findViewById(R.id.listview_result),
								data.get(spinnerSelectedValueNr).url, dialog);

						downloader.execute(keywords);
					}

					else {
						Toast.makeText(
								getBaseContext(),
								"Profile keywords are missing - how's thats possible?",
								Toast.LENGTH_SHORT).show();
					}

				} else {

					Toast.makeText(getBaseContext(), "Please select a profile",
							Toast.LENGTH_SHORT).show();

				}
			}
		});
	}

	public void bindSpinnerItemOnSelectEvent() {
		// find spinner selected url

		Spinner s = (Spinner) findViewById(R.id.spinner_profile);

		s.setOnItemSelectedListener(new OnItemSelectedListener() {

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

	public void bindListViewItems(int spinnerSelectedItemIndex) {

		// index 0 is reserved for "Select a item..."
		if (spinnerSelectedItemIndex > 0) {
			UserProfile selectedUser = data.get(spinnerSelectedItemIndex - 1);

			ArrayList<String> keywordsToBind = new ArrayList<String>();

			if (selectedUser.keywords != null) {
				for (Keyword k : selectedUser.keywords) {

					if (k.rank == -1)
						keywordsToBind.add(k.value + " [ - ] ");
					else
						keywordsToBind.add(k.value + " [ " + k.rank + " ] ");

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