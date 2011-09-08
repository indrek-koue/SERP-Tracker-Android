package com.inc.im.serptracker.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.inc.im.serptracker.R;
import com.inc.im.serptracker.data.DbAdapter;
import com.inc.im.serptracker.data.Keyword;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AsyncDownloader extends
		AsyncTask<ArrayList<Keyword>, Integer, ArrayList<Keyword>> {

	private Context con;
	public ListView lv;
	private final String searchable;
	private ProgressDialog progressDialog;

	private int itemCount;

	public AsyncDownloader(Context con, ListView lv, String searchable,
			ProgressDialog progress) {
		this.con = con;
		this.lv = lv;
		this.searchable = removePrefix(searchable);
		this.progressDialog = progress;
	}

	@Override
	protected ArrayList<Keyword> doInBackground(ArrayList<Keyword>... keywords) {

		if (keywords == null || keywords.length == 0)
			return null;

		ArrayList<Keyword> result = new ArrayList<Keyword>();

		long start = System.currentTimeMillis();

		// count items for the load screen
		itemCount = keywords[0].size();

		int counter = 0;

		for (Keyword k : keywords[0]) {

			try {
				Document doc = Jsoup.connect(generateEscapedQueryString(k))
						.get();

				Log.d("MY", "downloading: " + generateEscapedQueryString(k));

				// Element divSearch = doc.select("div#search").first();

				Log.d("MY", "downloaded chars: " + doc.html().length());

				Elements allResults = doc.select("h3 > a");

				Log.d("MY",
						"results downloaded:"
								+ Integer.toString(allResults.size()));

				Log.d("MY",
						"results downloaded:"
								+ Integer.toString(doc.getElementsByTag("h3")
										.size()));

				Log.d("MY",
						"all links: "
								+ Integer.toString(doc.getElementsByTag("a")
										.size()));

				for (Element e : allResults) {
					k.searchEngineResults.add(e.attr("href"));

				}

				// result.add(k);

			} catch (IOException e) {
				Log.d("MY", "download error");
			}

			// Keyword keywordWithHtmlSource = manageDownload(k);
			// result.add(keywordWithHtmlSource);

			publishProgress(++counter);

		}

		Log.d("MY", "TOTAL TIME: " + (System.currentTimeMillis() - start));

		return keywords[0];

	}

	@Override
	protected void onPostExecute(ArrayList<Keyword> input) {

		// if progressdialog is canceled, dont show results
		if (!progressDialog.isShowing())
			return;

		ArrayList<String> toDisplay = new ArrayList<String>();

		for (Keyword k : input) {

			// find position
			int rank = -1;
			for (int i = 0; i < k.searchEngineResults.size(); i++)
				if (k.searchEngineResults.get(i).contains(searchable))
					rank = i;

			if (rank == -1) {
				toDisplay.add(k.value + " [not ranked]");
			} else {

				DbAdapter db = new DbAdapter(con);

				// load old rank
				int oldRank = k.rank;

				// save new rank
				if (!db.updateKeywordRank(k, rank))
					Log.e("MY", "keyword rank update failed: " + k.value);

				// show user
				toDisplay.add(k.value + " new:" + Integer.toString(rank + 1)
						+ " old:" + oldRank);

			}
		}// for

		lv.setAdapter(new ArrayAdapter<String>(con,
				R.layout.main_activity_listview_item, R.id.textView1, toDisplay));

		progressDialog.dismiss();

	}

	@Override
	protected void onProgressUpdate(Integer... values) {

		String loaderValue = "Keyword " + values[0] + "/" + itemCount;

		progressDialog.setMessage(loaderValue);

	}

	// public Keyword manageDownload(Keyword k) {
	//
	// // String result = null;
	// try {
	// StringBuffer sb = new StringBuffer("");
	//
	// HttpGet request = new HttpGet();
	// request.setURI(generateEscapedQuery(k));
	//
	// // logging
	// long start = System.currentTimeMillis();
	//
	// HttpResponse response = new DefaultHttpClient().execute(request);
	//
	// // logging
	// long responseTime = (System.currentTimeMillis() - start);
	// Log.d("MY", "DefaultHttpClient().execute(request): " + responseTime
	// + "ms");
	//
	// BufferedReader in = new BufferedReader(new InputStreamReader(
	// response.getEntity().getContent()));
	//
	// // logging
	// long buffReaderTime = (System.currentTimeMillis() - start -
	// responseTime);
	// Log.d("MY", "response.getEntity().getContent(): " + buffReaderTime
	// + "ms");
	//
	// String line = "";
	// String NL = System.getProperty("line.separator");
	//
	// while ((line = in.readLine()) != null)
	// sb.append(line + NL);
	//
	// in.close();
	//
	// // logging
	// Log.d("MY",
	// "BufferedReader fetch time: "
	// + (System.currentTimeMillis() - start
	// - responseTime - buffReaderTime) + "ms");
	// Log.d("MY", "total: " + (System.currentTimeMillis() - start) + "ms");
	//
	// // attach source code to input item
	// k.htmlSourceCode = sb.toString();
	//
	// } catch (Exception e) {
	// Log.e("MY", e.toString());
	// }
	//
	// return k;
	// }

	// public URI generateEscapedQuery(Keyword k) throws URISyntaxException {
	// return new URI("http://www.google.com/search?num=100&q="
	// + URLEncoder.encode(k.value));
	// }

	public String generateEscapedQueryString(Keyword k) {

		return "http://www.google.com/search?num=100&q="
				+ URLEncoder.encode(k.value);
	}

	public String removePrefix(String searchable) {
		searchable.replace("http://", "");
		searchable.replace("www", "");

		return searchable;
	}

}
