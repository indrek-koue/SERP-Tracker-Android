package com.inc.im.serptracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.inc.im.serptracker.R;
import com.inc.im.serptracker.data.DbAdapter;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.data.UserProfile;
import com.inc.im.serptracker.util.AsyncDownloader;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	static String TAG = "MY";
	// int num = 100;

	private String spinnerValue;
	private final String emptySpinnerSelection = "Select a website...";
	private Boolean menuBarIsVisible = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity_layout);

		initSpinner();

		bindSpinnerItemOnSelectEvent();
		bindActivateButton();
		bindMenuBarButtons();

	}

	// @Override
	// protected void onResume() {
	//
	// // user is back - load fresh data to spinner
	// initSpinner();
	//
	// }

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	//
	// LinearLayout lv = (LinearLayout) findViewById(R.id.linearLayout2);
	//
	// if (menuBarIsVisible) {
	// lv.setVisibility(View.GONE);
	// menuBarIsVisible = false;
	// } else {
	// lv.setVisibility(View.VISIBLE);
	// menuBarIsVisible = true;
	// }
	// // getMenuInflater().inflate(R.menu.menu_main_activity, menu);
	//
	// return false;
	//
	// }

	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	//
	// switch (item.getItemId()) {
	// case R.id.addnewprofile:
	// startActivity(new Intent(getBaseContext(),
	// InsertWebsiteActivity.class));
	// return true;
	// case R.id.settings:
	// startActivity(new Intent(getBaseContext(),
	// PreferencesActivity.class));
	// return true;
	// case R.id.manage_profiles_menu:
	// startActivity(new Intent(getBaseContext(),
	// ManageWebsitesActivity.class));
	// return true;
	// default:
	// return super.onOptionsItemSelected(item);
	//
	// }
	//
	// }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		LinearLayout lv = (LinearLayout) findViewById(R.id.linearLayout2);

		if (keyCode == 82)
			if (menuBarIsVisible) {
				lv.setVisibility(View.GONE);
				menuBarIsVisible = false;
			} else {
				lv.setVisibility(View.VISIBLE);
				menuBarIsVisible = true;
			}

		// KeyEvent myKeyEvent = event;
		// int myKeyCode = keyCode;

		// TODO Auto-generated method stub
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

				startActivity(new Intent(getBaseContext(),
						ManageWebsitesActivity.class));

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

	}

	public void initSpinner() {

		// new DbAdapter(getBaseContext()).trunkTables();

		final ArrayList<UserProfile> data = new DbAdapter(getBaseContext())
				.loadAllProfiles();

		ArrayList<String> spinnerValues = new ArrayList<String>();

		spinnerValues.add(emptySpinnerSelection);

		if (data != null)
			for (UserProfile u : data)
				spinnerValues.add(u.url);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getBaseContext(), R.layout.main_activity_spinner_item,
				R.id.textView1, spinnerValues);

		((Spinner) findViewById(R.id.spinner_profile)).setAdapter(adapter);

	}

	private void bindActivateButton() {

		Button btn = (Button) findViewById(R.id.button_run);

		btn.setOnClickListener(new View.OnClickListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void onClick(View v) {

				if (!spinnerValue.equals(emptySpinnerSelection)) {
					ArrayList<UserProfile> data = new DbAdapter(
							getBaseContext()).loadAllProfiles();
					ArrayList<Keyword> keywords = null;

					// find keywords by name
					for (UserProfile u : data)
						if (u.url.equals(spinnerValue))
							keywords = u.keywords;

					if (keywords != null) {

						// start dialog
						ProgressDialog dialog = ProgressDialog.show(
								MainActivity.this, "Inspecting keywords",
								"Starting, please wait.", true, true);
						// dialog.setCancelable(true);

						AsyncDownloader downloader = new AsyncDownloader(
								getBaseContext(),
								(ListView) findViewById(R.id.listview_result),
								spinnerValue, dialog);

						downloader.execute(keywords);
					}
				} else {
					Toast.makeText(getBaseContext(), "Please select a profile",
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	public void bindSpinnerItemOnSelectEvent() {
		// find spinner selected url

		Spinner s = (Spinner) findViewById(R.id.spinner_profile);

		s.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				bindListViewItems(arg0, arg2);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				Log.w("MY", "NOTHING SELECTED");
			}
		});
	}

	public void bindListViewItems(AdapterView<?> arg0, int arg2) {

		final ArrayList<UserProfile> data = new DbAdapter(getBaseContext())
				.loadAllProfiles();

		spinnerValue = arg0.getItemAtPosition(arg2).toString();

		if (!spinnerValue.equals(emptySpinnerSelection)) {

			ArrayList<Keyword> keywords = null;

			// find keywords by name
			for (UserProfile u : data)
				if (u.url.equals(spinnerValue))
					keywords = u.keywords;

			ArrayList<String> keywordsToBind = new ArrayList<String>();

			if (keywords != null) {
				for (Keyword k : keywords)
					keywordsToBind.add(k.value + " [ - ] ");

				ArrayAdapter<String> a = new ArrayAdapter<String>(
						getBaseContext(), R.layout.main_activity_listview_item,
						R.id.textView1, keywordsToBind);

				((ListView) findViewById(R.id.listview_result)).setAdapter(a);
			}
		}
	}

}