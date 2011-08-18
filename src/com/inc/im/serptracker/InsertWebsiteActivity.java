package com.inc.im.serptracker;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.inc.im.serptracker.data.DbAdapter;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.data.UserProfile;

public class InsertWebsiteActivity extends Activity {

	private int editTextRowCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.insertprofile_activity_layout);

		// new DbAdapter(getBaseContext()).trunkTables();

		bindAddButton();
		bindBackButton();

		final EditText et = (EditText) findViewById(R.id.editText2);

		et.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				// String text = et.getText().toString();
				// enterCount = text.split("\\n").length;
				//

				// if enter is selected and on release start calculating
				if (keyCode == KeyEvent.KEYCODE_ENTER
						&& event.getAction() == KeyEvent.ACTION_UP) {

					// get EditText text
					String text = ((EditText) v).getText().toString();

					// find how many rows it cointains
					editTextRowCount = text.split("\\n").length;

					// user has input more than limited - lets do something
					// about that
					if (editTextRowCount >= 7) {

						// find the last break
						int lastBreakIndex = text.lastIndexOf("\n");

						// compose new text
						String newText = text.substring(0, lastBreakIndex);

						// add new text - delete old one and append new one
						// (append because I want the cursor to be at the end)
						((EditText) v).setText("");
						((EditText) v).append(newText);

					}
				}

				return false;
			}
		});

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
			Toast.makeText(getBaseContext(),
					"Website address is too short or invalid",
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
