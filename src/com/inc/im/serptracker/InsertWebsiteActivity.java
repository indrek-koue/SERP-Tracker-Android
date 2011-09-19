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

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.data.DbAdapter;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.data.UserProfile;

public class InsertWebsiteActivity extends Activity {

	private int editTextRowCount = 0;

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

						Toast.makeText(
								getBaseContext(),
								getString(R.string.beta_version_doesn_t_have_keyword_limit_enjoy),
								Toast.LENGTH_SHORT).show();
						
//						// find the last break
//						int lastBreakIndex = text.lastIndexOf("\n");
//
//						// compose new text
//						String newText = text.substring(0, lastBreakIndex);
//
//						// add new text - delete old one and append new one
//						// (append because I want the cursor to be at the end)
//						((EditText) v).setText("");
//						((EditText) v).append(newText);

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

						if (keyword == null || keyword.length() < 1) {
							Toast.makeText(getBaseContext(),
									getString(R.string.please_enter_some_keywords),
									Toast.LENGTH_SHORT).show();
							return;
						}

						if (inputSite == null || inputSite.length() < 5) {
							Toast.makeText(getBaseContext(),
									getString(R.string.website_address_is_too_short_or_invalid),
									Toast.LENGTH_SHORT).show();
							return;
						}

						if(new DbAdapter(getBaseContext()).loadAllProfiles().size() >= 3)
							Toast.makeText(getBaseContext(),
									getString(R.string.beta_version_doesn_t_have_website_count_limit_enjoy),
									Toast.LENGTH_SHORT).show();
						
						if (new DbAdapter(getBaseContext()).insertOrUpdate(
								inputSite, keyword, 0)) {
							Toast.makeText(getBaseContext(),
									getString(R.string.website_added_successfuly),
									Toast.LENGTH_SHORT).show();

							startActivity(new Intent(getBaseContext(),
									MainActivity.class));
						} else {
							Toast.makeText(getBaseContext(),
									getString(R.string.website_add_failure), Toast.LENGTH_SHORT)
									.show();
						}
					}
				});
	}

}
