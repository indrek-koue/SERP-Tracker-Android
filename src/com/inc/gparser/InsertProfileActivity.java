package com.inc.gparser;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

import com.inc.gparser.data.DbAdapter;
import com.inc.gparser.data.Keyword;
import com.inc.gparser.data.UserProfile;
import com.inc.gparser.util.AsyncDownloader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class InsertProfileActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.insertprofile);

		// new DbAdapter(getBaseContext()).trunkTables();

		bindAddButton();

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

		Button btn = (Button) findViewById(R.id.button1);

		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// new DbAdapter(getBaseContext()).trunkTables();

				// load textView values
				String inputSite = ((EditText) findViewById(R.id.editText1))
						.getText().toString();
				String keyword = ((EditText) findViewById(R.id.editText2))
						.getText().toString();

				insertIntoIndernalDatabase(inputSite, keyword);

				// ArrayList<UserProfile> data = new
				// DbAdapter(getBaseContext()).loadAllProfiles();
				//
				// data.size();

			}
		});
	}

	public void insertIntoIndernalDatabase(String inputSite, String keyword) {
		String[] keywords = null;

		if (keyword.contains("\n"))
			keywords = keyword.split("\\n");
		else if (keyword.contains(","))
			keywords = keyword.split(",");
		else
			keywords = new String[] { keyword };

		// generate array list
		ArrayList<Keyword> keywordsArrayList = new ArrayList<Keyword>();
		for (String s : keywords)
			keywordsArrayList.add(new Keyword(s));

		UserProfile profile = new UserProfile(inputSite, keywordsArrayList);

		if (new DbAdapter(getBaseContext()).insertProfile(profile))
			Toast.makeText(getBaseContext(), "Profile insertion succesful",
					Toast.LENGTH_SHORT);
		else
			Toast.makeText(getBaseContext(), "Profile insertion failed",
					Toast.LENGTH_SHORT);
	}

}
