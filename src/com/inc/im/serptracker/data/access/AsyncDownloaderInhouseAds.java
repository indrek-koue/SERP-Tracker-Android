package com.inc.im.serptracker.data.access;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.inc.im.serptracker.R;
import com.inc.im.serptracker.data.InhouseAd;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AsyncDownloaderInhouseAds extends
		AsyncTask<String, Integer, InhouseAd> {

	TextView tv;
	LinearLayout ll;
	Activity a;

	public AsyncDownloaderInhouseAds(LinearLayout ll, Activity a) {
		super();
		tv = (TextView) ll.findViewById(R.id.inhouseAdsText);
		this.ll = ll;
		this.a = a;
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

		// TODO: make xml based ad parser
		// &&ENTER URL HERE&&
		// %%ENTER TEXT HERE%%

		InhouseAd ad = null;

		if (result != null && result.length() > 5 && result.contains("%%")) {

			String text = result.substring(result.indexOf("%%") + 2,
					result.lastIndexOf("%%"));

			ad = new InhouseAd("", text);

			if (result.contains("&&")) {
				String link = result.substring(result.indexOf("&&") + 2,
						result.lastIndexOf("&&"));
				ad = new InhouseAd(link, text);
			}

		}

		return ad;

	}
	

	@Override
	protected void onPostExecute(final InhouseAd ad) {

		if (ad == null) {
			return;
		}

		ll.setVisibility(View.VISIBLE);

		tv.setText(ad.text);

		if (!ad.url.equals(""))
			ll.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(ad.url));
					a.startActivity(intent);

				}
			});

	}

}
