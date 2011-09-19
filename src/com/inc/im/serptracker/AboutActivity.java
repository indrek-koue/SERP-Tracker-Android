package com.inc.im.serptracker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.util.AsyncDownloaderNews;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {

	/** Called when the activity is first created. */

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

//		if (internetConnectionExists())
//			bindInfoText(manageDownload("http://www.thedroidproject.com/_app/SERPTracker/apptext.txt"));
//		else
//			bindInfoText("ERROR: no internet connection exists");

		// bindInfoText("Version 0.9 released!\n\n"
		// + "Version 0.9 adds whole new user interface. \n\n"
		// +
		// "1.6 version of android is not supported anymore due the lack of UI elements and functions\n\n"
		// +
		// "NOTE: If you are migrating from version 0.85 or lower, please reset your data under settings > delete all data");

		bindSendEmailToDevButton();
		bindBackButton();

		Toast.makeText(
				getBaseContext(),
				"Please send any questions and bug reports to my email by clicking \"send email button\"",
				Toast.LENGTH_LONG).show();

	}

	public boolean internetConnectionExists() {

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		} else {
			return false;
		}

	}

	public String manageDownload(String url) {

		String result = null;
		try {
			StringBuffer sb = new StringBuffer("");

			HttpGet request = new HttpGet();
			request.setURI(URI.create(url));

			HttpResponse response = new DefaultHttpClient().execute(request);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			String line = "";
			String NL = System.getProperty("line.separator");

			while ((line = in.readLine()) != null)
				sb.append(line + NL);

			in.close();

			result = sb.toString();

		} catch (Exception e) {
			Log.e("MY", e.toString());
		}

		return result;
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

	private void bindInfoText(String s) {
		((TextView) findViewById(R.id.textView1)).setText(s);
	}

}
