package com.inc.im.serptracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);

		bindInfoText("Version 0.9 released!\n\n"
				+ "Version 0.9 adds whole new user interface. \n\n"
				+ "1.6 version of android is not supported anymore due the lack of UI elements and functions\n\n"
				+ "NOTE: If you are migrating from version 0.85 or lower, please reset your data under settings > delete all data");
		bindSendEmailToDevButton();
		bindBackButton();

	}

	private void bindBackButton() {
		((Button) findViewById(R.id.button4))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						startActivity(new Intent(getBaseContext(),
								MainActivity.class));
					}
				});
	}

	private void bindSendEmailToDevButton() {
		((Button) findViewById(R.id.button1))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType("text/plain");
						intent.putExtra(Intent.EXTRA_EMAIL,
								new String[] { "indrek.koue@gmail.com" });

						intent.putExtra(Intent.EXTRA_SUBJECT, "SERP Tracker");
						try {
							startActivity(Intent.createChooser(intent,
									"Send mail"));
						} catch (android.content.ActivityNotFoundException ex) {
							Toast.makeText(getBaseContext(),
									"There are no email clients installed.",
									Toast.LENGTH_SHORT).show();
						}

					}
				});
	}

	private void bindInfoText(String s) {
		((TextView) findViewById(R.id.textView1)).setText(s);
	}

}
