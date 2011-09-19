package com.inc.im.serptracker.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.inc.im.serptracker.data.InhouseAd;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AsyncDownloaderInhouseAds extends
		AsyncTask<String, Integer, InhouseAd> {

	ProgressBar pb;
	TextView tv;
	LinearLayout ll;
	Activity a;

	public AsyncDownloaderInhouseAds(ProgressBar pb, TextView tv,
			LinearLayout ll, Activity a) {
		super();
		this.pb = pb;
		this.tv = tv;
		this.ll = ll;
		this.a = a;
	}

	@Override
	protected void onPreExecute() {
		pb.setVisibility(View.VISIBLE);
	}

	@Override
	protected InhouseAd doInBackground(String... params) {

		String result = null;
		try {
			StringBuffer sb = new StringBuffer("");

			HttpGet request = new HttpGet();
			request.setURI(URI.create(params[0]));

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

		// parse
		// ##admob## < show admob ads
		// &&link&&
		// %%text%%

		InhouseAd ad = null;

		if (result != null) {

			String text = result.substring(result.indexOf("%%"),
					result.lastIndexOf("%%"));
			String link = result.substring(result.indexOf("&&"),
					result.lastIndexOf("&&"));

			ad = new InhouseAd(text, link);

		}

		return ad;

	}

	@Override
	protected void onPostExecute(final InhouseAd ad) {

		if (ad == null)
			return;

		tv.setText(ad.text);
		ll.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Toast.makeText(a, "GO TO: " + ad.url, Toast.LENGTH_SHORT)
						.show();

			}
		});

		pb.setVisibility(View.GONE);

	}

}
