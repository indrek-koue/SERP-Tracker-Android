package com.inc.im.serptracker;

import com.flurry.android.FlurryAgent;
import com.google.ads.AdView;
import com.inc.im.serptracker.data.access.AsyncDownloaderNews;
import com.inc.im.serptracker.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {
	
	private AdView adView;

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
	protected void onDestroy() {
		adView.destroy();
		super.onDestroy();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);

		adView = Util.loadAdmob(this);

		AsyncDownloaderNews newsDownloader = new AsyncDownloaderNews(
				((ProgressBar) findViewById(R.id.progressBar1)),
				((TextView) findViewById(R.id.textView1)));

		newsDownloader.execute(getString(R.string.app_news_get_path));

		bindSendEmailToDevButton();
		bindLegalButton();
		bindBackButton();

	}

	private void bindLegalButton() {
		((Button) findViewById(R.id.btn_legal))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						String msg = "Copyright (C) by TheDroidProject.com \n \n "
								+ "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, "
								+ "EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF "
								+ "MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. "
								+ "IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR "
								+ "ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, "
								+ "TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE "
								+ "SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. \n\n"
								+ "Google is trademark of Google inc. This application is built on Google "
								+ "Custom Search API and does not infringe Google Search terms of service.";

						AlertDialog.Builder builder = new Builder(
								AboutActivity.this);

						builder.setTitle(getString(R.string.legal_information));

						builder.setMessage(msg);

						builder.setCancelable(true);
						builder.create().show();

					}
				});
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

						// old send email
						intent.setType("text/plain");
						//
						intent.putExtra(Intent.EXTRA_EMAIL,
								new String[] { getString(R.string.dev_email) });

						intent.putExtra(Intent.EXTRA_SUBJECT,
								getString(R.string.app_name));
						//
						// // in order to not crash with other than emails apps?
						intent.putExtra(android.content.Intent.EXTRA_TEXT, "");

						try {
							startActivity(Intent.createChooser(intent,
									getString(R.string.send_mail)));
						} catch (android.content.ActivityNotFoundException ex) {
							Toast.makeText(
									getBaseContext(),
									R.string.there_are_no_email_clients_installed_,
									Toast.LENGTH_SHORT).show();
						}

					}
				});
	}

}
