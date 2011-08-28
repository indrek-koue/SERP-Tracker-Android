package com.inc.im.serptracker;

import java.util.ArrayList;

import com.inc.im.serptracker.R;
import com.inc.im.serptracker.data.DbAdapter;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.data.UserProfile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class ManageWebsitesActivity extends Activity {

	ArrayList<UserProfile> data;

	int selectedUserProfileId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_profiles_layout);

		initSpinner();
		bindSpinnerItemOnSelectEvent();

		bindAddButton();
		bindBackButton();

	}

	public void initSpinner() {

		data = new DbAdapter(getBaseContext()).loadAllProfiles();

		ArrayList<String> spinnerValues = new ArrayList<String>();

		//spinnerValues.add(MainActivity.emptySpinnerSelection);

		// generate spinner values
		if (data != null)
			for (UserProfile u : data)
				spinnerValues.add(u.url + "[" + u.keywords.size() + "]");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getBaseContext(), R.layout.main_activity_spinner_item,
				R.id.textView1, spinnerValues);

		((Spinner) findViewById(R.id.spinner1)).setAdapter(adapter);

	}

	public void bindBackButton() {
		((Button) findViewById(R.id.button2))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						startActivity(new Intent(getBaseContext(),
								MainActivity.class));

					}
				});
	}

	private void bindAddButton() {

		((Button) findViewById(R.id.button1))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						// load textView values
						String inputSite = ((EditText) findViewById(R.id.editText1))
								.getText().toString();
						String keyword = ((EditText) findViewById(R.id.editText2))
								.getText().toString();

						// validating
						if (keyword == null || keyword.length() < 1) {
							Toast.makeText(getBaseContext(),
									"Please enter some keywords",
									Toast.LENGTH_SHORT).show();
							return;
						}

						if (inputSite == null || inputSite.length() < 5) {
							Toast.makeText(getBaseContext(),
									"Website address is too short or invalid",
									Toast.LENGTH_SHORT).show();
							return;
						}

						// inserting
						Boolean editSuccess = new DbAdapter(getBaseContext())
								.insertOrUpdate(inputSite, keyword,
										selectedUserProfileId);

						if (editSuccess) {
							Toast.makeText(getBaseContext(),
									"Website edited successfuly",
									Toast.LENGTH_SHORT).show();

							startActivity(new Intent(getBaseContext(),
									MainActivity.class));
						} else {
							Toast.makeText(getBaseContext(),
									"Website edit failure", Toast.LENGTH_SHORT)
									.show();
						}

					}
				});
	}

	public void bindSpinnerItemOnSelectEvent() {
		// populate EditTexts

		Spinner s = (Spinner) findViewById(R.id.spinner1);

		s.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapter, View arg1,
					int index, long arg3) {

				populateEditTexts(index);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
	}

	private void populateEditTexts(int index) {

		if (data == null)
			return;

		UserProfile selectedUserProfile = data.get(index);

		selectedUserProfileId = selectedUserProfile.id;

		((EditText) findViewById(R.id.editText1))
				.setText(selectedUserProfile.url);

		ArrayList<Keyword> keywords = selectedUserProfile.keywords;

		if (keywords == null || keywords.size() <= 0)
			return;

		String keyWordsToPopulate = "";

		for (Keyword k : keywords)
			keyWordsToPopulate += k.value + "\n";

		((EditText) findViewById(R.id.editText2)).setText(keyWordsToPopulate);

	}

}
