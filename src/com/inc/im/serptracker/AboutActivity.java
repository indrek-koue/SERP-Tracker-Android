package com.inc.im.serptracker;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.util.AsyncDownloaderNews;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {

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
		setContentView(R.layout.about_layout);

		AsyncDownloaderNews newsDownloader = new AsyncDownloaderNews(
				((ProgressBar) findViewById(R.id.progressBar1)),
				((TextView) findViewById(R.id.textView1)));
		
		newsDownloader.execute(getString(R.string.app_news_get_path));

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
								new String[] { getString(R.string.dev_email) });

						intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));

						try {
							startActivity(Intent.createChooser(intent,
									getString(R.string.send_mail)));
						} catch (android.content.ActivityNotFoundException ex) {
							Toast.makeText(getBaseContext(),
									R.string.there_are_no_email_clients_installed_,
									Toast.LENGTH_SHORT).show();
						}

					}
				});
	}

}
