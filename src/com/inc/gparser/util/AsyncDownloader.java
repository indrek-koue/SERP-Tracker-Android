package com.inc.gparser.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.inc.gparser.MainActivity;
import com.inc.gparser.R;
import com.inc.gparser.data.DbAdapter;
import com.inc.gparser.data.Keyword;

import dalvik.system.TemporaryDirectory;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class AsyncDownloader extends
		AsyncTask<ArrayList<Keyword>, Integer, ArrayList<Keyword>> {

	String TAG = "MY";
	private Context con;
	public ListView lv;
	private ProgressBar progressBar;
	private final String searchable;

	int itemCount;

	// int counterEnd;

	// Params, the type of the parameters sent to the task upon execution.
	// Progress, the type of the progress units published during the background
	// computation.
	// Result, the type of the result of the background computation.
	//

	public AsyncDownloader(Context con, ListView lv, ProgressBar pb,
			String searchable) {
		this.con = con;
		this.lv = lv;
		this.searchable = removePrefix(searchable);
		this.progressBar = pb;
	}

	// public AsyncDownloader(Context con, ListView lv, int itemCount,
	// String searchable) {
	// this.con = con;
	// this.lv = lv;
	// this.searchable = removePrefix(searchable);
	// this.itemCount = itemCount;
	// }

	@Override
	protected ArrayList<Keyword> doInBackground(ArrayList<Keyword>... params) {

		itemCount = params[0].size();

		// Log.w("MY", "doInBackground");
		// long start = System.currentTimeMillis();

		ArrayList<Keyword> allDownloadedKeywordsWithHtmlSource = new ArrayList<Keyword>();

		// String result = null;

		// ArrayList<Keyword> input = params[0];

		int counter = 0;
		if (params.length > 0)
			for (Keyword k : params[0]) {

				// publishProgress(counter);
				// counter++;

				

				Keyword keywordWithHtmlSource = manageDownload(k);

				publishProgress(++counter);
				
				// Log.w(TAG, "DOWNLOAD TIME: " + (System.currentTimeMillis() -
				// start)
				// + "ms");
				//
				// Log.w("MY", "downloaded chars: " + result.length());

				allDownloadedKeywordsWithHtmlSource.add(keywordWithHtmlSource);
			}
		return allDownloadedKeywordsWithHtmlSource;

	}

	@Override
	protected void onPostExecute(ArrayList<Keyword> keywords) {

		ArrayList<String> downloadAndParseResult = new ArrayList<String>();

		// Log.w("MY", "onPostExecute");

		// long start = System.currentTimeMillis();
		// if (input == null) {
		// // TODO: show warning here
		// Log.w("MY", "download result is null");
		// return;
		// }

		// input of source codes

		for (Keyword keyword : keywords) {

			ArrayList<String> links = new GoogleParser()
					.parse(keyword.htmlSourceCode);

			int rank = -1;
			for (int i = 0; i < links.size(); i++)
				if (links.get(i).contains(searchable))
					rank = i;
			if (rank == -1)
				downloadAndParseResult.add(keyword.value + " [not ranked]");
			else
				downloadAndParseResult.add(keyword.value + " ["
						+ Integer.toString(rank) + "]");
		}

		// search
		// for (int i = 0; i < links.size(); i++) {
		// // Log.w("MY", links.get(i));
		// if (links.get(i).contains(searchable)) {
		// // hurray, found!
		// rank = i;
		// }
		// }

		// Log.w("MY", "RANK NR:" + rank);

		// keyword = keyword.substring(keyword.indexOf("q="), end)

		// ArrayAdapter<String> aa = new ArrayAdapter<String>(con,
		// android.R.layout.simple_list_item_1, downloadAndParseResult);

		lv.setAdapter(new ArrayAdapter<String>(con,
				android.R.layout.simple_list_item_1, downloadAndParseResult));

		// Log.w(TAG, "PARSE TIME: " + (System.currentTimeMillis() - start) +
		// "ms");

	}

	@Override
	protected void onProgressUpdate(Integer... values) {

		// Log.w("MY", "PROGRESS: " + values[0] + "/" + counterEnd);

		// super.onProgressUpdate(values);
		// progressBar.setProgress(values[0]);

		Toast.makeText(con, values[0] + "/" + itemCount + "processed",
				Toast.LENGTH_SHORT).show();

	}

	public Keyword manageDownload(Keyword k) {

		// String result = null;

		try {
			StringBuffer sb = new StringBuffer("");

			HttpGet request = new HttpGet();
			request.setURI(generateEscapedQuery(k));

			HttpResponse response = new DefaultHttpClient().execute(request);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			String line = "";
			String NL = System.getProperty("line.separator");

			while ((line = in.readLine()) != null)
				sb.append(line + NL);

			in.close();

			// attach source code to input item
			k.htmlSourceCode = sb.toString();

		} catch (Exception e) {
			Log.e("MY", e.toString());
		}

		return k;
	}

	public URI generateEscapedQuery(Keyword k) throws URISyntaxException {
		return new URI("http://www.google.com/search?num=100&q="
				+ URLEncoder.encode(k.value));
	}

	public String removePrefix(String searchable) {
		searchable.replace("http://", "");
		searchable.replace("www", "");

		return searchable;
	}

}
