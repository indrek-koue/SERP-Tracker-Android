package com.inc.im.serptracker.data.access;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Downloads news on about page asyncronosly from my persnal server through http
 */

public class AsyncDownloaderNews extends AsyncTask<String, Integer, String> {

	ProgressBar pb;
	TextView tv;

	public AsyncDownloaderNews(ProgressBar pb, TextView tv) {
		super();
		this.pb = pb;
		this.tv = tv;
	}

	@Override
	protected String doInBackground(String... params) {
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

		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		tv.setText(result);
		pb.setVisibility(View.GONE);
	}

	@Override
	protected void onPreExecute() {
		pb.setVisibility(View.VISIBLE);
	}

}
