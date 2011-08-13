package com.inc.gparser;

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

import com.inc.gparser.R;
import com.inc.gparser.data.DbAdapter;
import com.inc.gparser.data.Keyword;
import com.inc.gparser.data.UserProfile;
import com.inc.gparser.util.AsyncDownloader;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

public class MainActivity extends Activity {

	static String TAG = "MY";
	// int num = 100;

	private String spinnerValue;
	private final String emptySpinnerSelection = "Select a profile...";

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.menu_main_activity, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.addnewprofile:
			startActivity(new Intent(getBaseContext(),
					InsertProfileActivity.class));
			return true;
		case R.id.settings:
			startActivity(new Intent(getBaseContext(),
					PreferencesActivity.class));
			return true;
		case R.id.manage_profiles_menu:
			startActivity(new Intent(getBaseContext(),
					ManageProfilesActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity_layout);

		initSpinner();

		bindSpinnerOnClickEvent();
		bindActivateButton();
		// bindAddProfileButton();
		// bindSettingsButton();

		bindMenuBarButtons();

	}

	private void bindMenuBarButtons() {
		((LinearLayout) findViewById(R.id.linearLayoutAddProfile))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						startActivity(new Intent(getBaseContext(),
								InsertProfileActivity.class));

					}
				});

		((LinearLayout) findViewById(R.id.linearLayoutManageProfiles))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {

						startActivity(new Intent(getBaseContext(),
								ManageProfilesActivity.class));

					}
				});

		((LinearLayout) findViewById(R.id.linearLayoutSettings))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {

						startActivity(new Intent(getBaseContext(),
								PreferencesActivity.class));

					}
				});
	}

	public void initSpinner() {

		// new DbAdapter(getBaseContext()).trunkTables();

		ArrayList<UserProfile> data = new DbAdapter(getBaseContext())
				.loadAllProfiles();

		ArrayList<String> spinnerValues = new ArrayList<String>();

		spinnerValues.add(emptySpinnerSelection);

		if (data != null)
			for (UserProfile u : data)
				spinnerValues.add(u.url);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getBaseContext(), android.R.layout.simple_spinner_item,
				spinnerValues);

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

					AsyncDownloader downloader = new AsyncDownloader(
							getBaseContext(),
							(ListView) findViewById(R.id.listview_result),
							spinnerValue);

					if (keywords != null)
						downloader.execute(keywords);
				} else {
					Toast.makeText(getBaseContext(), "Please select a profile",
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	public void bindSpinnerOnClickEvent() {
		// find spinner selected url

		Spinner s = (Spinner) findViewById(R.id.spinner_profile);

		s.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Object o = arg0.getItemAtPosition(arg2);

				spinnerValue = o.toString();
				Log.w("MY", o.toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				Log.w("MY", "NOTHING SELECTED");
			}
		});
	}

}