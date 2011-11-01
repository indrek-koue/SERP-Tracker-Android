package com.inc.im.serptracker;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;

import com.inc.im.serptracker.util.Parser;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class VerifyWebView extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getIntent() == null && getIntent().getExtras() == null)
			return;

		String keywordEncoded = "";
		final String myUrl = Parser.removePrefix(getIntent().getExtras()
				.getString("url"));

		Toast.makeText(getBaseContext(), "highlight: " + myUrl,
				Toast.LENGTH_LONG).show();

		try {
			keywordEncoded = URLEncoder.encode(getIntent().getExtras()
					.getString("keyword"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WebView wv = new WebView(getBaseContext());

		wv.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {

				view.findAll(myUrl);

				try {
					Method m = WebView.class.getMethod("setFindIsUp",
							Boolean.TYPE);
					
					m.invoke(view, true);

				} catch (Throwable ignored) {
				}

				super.onPageFinished(view, url);
			}

		});

		wv.loadUrl("http://www.google.com/search?num=100&q=" + keywordEncoded);

		wv.getSettings().setUserAgentString(
				"Apache-HttpClient/UNAVAILABLE (java 1.4)");

		setContentView(wv);
	}
}
