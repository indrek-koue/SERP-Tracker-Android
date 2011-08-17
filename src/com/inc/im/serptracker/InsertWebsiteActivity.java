package com.inc.im.serptracker;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

import com.inc.im.serptracker.R;
import com.inc.im.serptracker.data.DbAdapter;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.data.UserProfile;
import com.inc.im.serptracker.util.AsyncDownloader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class InsertWebsiteActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.insertprofile_activity_layout);

		// new DbAdapter(getBaseContext()).trunkTables();

		bindAddButton();

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

				if (insertIntoIndernalDatabase(inputSite, keyword))
					startActivity(new Intent(getBaseContext(),
							MainActivity.class));

			}
		});
	}

	public Boolean insertIntoIndernalDatabase(String inputSite, String keyword) {

		// stress test
		// keyword = "";
		// for(int i = 0; i < 100; i++)
		// keyword = keyword+"a\n";

		if (keyword == null || keyword.length() < 1) {
			Toast.makeText(getBaseContext(), "Please enter some keywords",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if (inputSite == null || inputSite.length() < 5) {
			Toast.makeText(getBaseContext(), "Website address is too short or invalid",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		
		String[] keywords = null;
		if (keyword.contains("\n"))
			keywords = keyword.split("\\n");
		else if (keyword.contains(","))
			keywords = keyword.split(",");
		else if (keyword.contains(";"))
			keywords = keyword.split(";");
		else
			keywords = new String[] { keyword };

		// generate array list
		ArrayList<Keyword> keywordsArrayList = new ArrayList<Keyword>();
		for (String s : keywords)
			keywordsArrayList.add(new Keyword(s));

		UserProfile profile = new UserProfile(inputSite, keywordsArrayList);

		if (new DbAdapter(getBaseContext()).insertProfile(profile)) {
			Toast.makeText(getBaseContext(), "Website added successfuly",
					Toast.LENGTH_SHORT).show();
			return true;
		} else {
			Toast.makeText(getBaseContext(), "Website add failure",
					Toast.LENGTH_SHORT).show();
			return false;
		}
	}

}
