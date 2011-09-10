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
import android.webkit.WebSettings;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AsyncDownloader extends
		AsyncTask<ArrayList<Keyword>, Integer, ArrayList<Keyword>> {

	private Context con;
	public ListView lv;
	private final String WEBSITE;
	private ProgressDialog progressDialog;

	private int itemCount;

	public AsyncDownloader(Context con, ListView lv, String searchable,
			ProgressDialog progress) {
		this.con = con;
		this.lv = lv;
		this.WEBSITE = removePrefix(searchable);
		this.progressDialog = progress;
	}

	@Override
	protected ArrayList<Keyword> doInBackground(ArrayList<Keyword>... keywords) {

		if (keywords == null || keywords.length == 0)
			return null;

		// ArrayList<Keyword> result = new ArrayList<Keyword>();

		long start = System.currentTimeMillis();

		// count items for the load screen
		itemCount = keywords[0].size();

		int counter = 0;

		for (Keyword k : keywords[0]) {

			int mode = 2;

			Document doc = null;

			// custom download + jsoap parse
			if (mode == 1) {

				String html = manageDownload(k);
				long startJsoupParse = System.currentTimeMillis();
				doc = Jsoup.parse(html);
				Log.i("MY",
						"Jsoup parse ("
								+ k.value
								+ "): "
								+ (System.currentTimeMillis() - startJsoupParse));

			}
			// jsoup download + jsoup parse
			else {

				long startJsoupParse = System.currentTimeMillis();

				doc = null;
				try {

					// String authUser = new WebSettings().getUserAgentString();

					String userAgent = "Mozilla/5.0 (Linux; U; Android 2.3.3; en-us; sdk Build/GRI34) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
					doc = Jsoup.connect(generateEscapedQueryString(k))
							.userAgent("???").get();
				} catch (IOException e1) {
					Log.e("MY", e1.toString());
				}

				if (doc == null)
					return null;

				Log.i("MY",
						"Jsoup download ("
								+ k.value
								+ "): "
								+ (System.currentTimeMillis() - startJsoupParse));

			}

			// jsoup download + parse

			// Log.d("MY", "downloading: " + generateEscapedQueryString(k));

			if (doc == null) {
				Log.e("MY", "doc is null");
				return null;
			}

//			Log.e("MY", Integer.toString(doc.getElementsByTag("div").size()));
//			Log.e("MY", Integer.toString(doc.select("a").size()));
//			Log.e("MY", Integer.toString(doc.select("div#search").size()));
//			Log.e("MY", Integer.toString(doc.select("#search").size()));
//			Log.e("MY", Integer.toString(doc.select("#main").size()));
//			Log.e("MY", Boolean.toString(doc.getElementById("main") == null));
//
//			for(Element e : doc.getElementsByTag("div"))
//				Log.e("MY", e.id());
//			
//			Log.e("MY", doc.html().substring(3000));

			Element divSearch = doc.getElementById("ires");

			//Element divSearch = doc.getElementById("div#search");
			
			// div#search
			// Log.d("MY", "downloaded chars: " + doc.html().length());

			if (divSearch == null) {
				Log.e("MY", "div#search is null");
				return null;
			}

			Elements allResults = divSearch.select("h3 > a");

			Log.d("MY",
					"results downloaded:" + Integer.toString(allResults.size()));

			for (Element e : allResults)
				k.searchEngineResults.add(e.attr("href"));

			publishProgress(++counter);

		}

		Log.i("MY", "Total: " + (System.currentTimeMillis() - start));

		return keywords[0];

	}

	@Override
	protected void onPostExecute(ArrayList<Keyword> input) {

		if (input == null) {
			progressDialog.dismiss();
			return;

		}

		// if progressdialog is canceled, dont show results
		if (!progressDialog.isShowing())
			return;

		ArrayList<String> toDisplay = new ArrayList<String>();

		for (Keyword k : input) {

			// find position
			int rank = -1;
			for (int i = 0; i < k.searchEngineResults.size(); i++)
				if (k.searchEngineResults.get(i).contains(WEBSITE))
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

	public String manageDownload(Keyword k) {

		String result = null;
		try {
			StringBuffer sb = new StringBuffer("");

			HttpGet request = new HttpGet();
			request.setURI(generateEscapedQuery(k));

			// logging
			long start = System.currentTimeMillis();

			HttpResponse response = new DefaultHttpClient().execute(request);

			// logging
			long responseTime = (System.currentTimeMillis() - start);
			Log.d("MY", "DefaultHttpClient().execute(request): " + responseTime
					+ "ms");

			BufferedReader in = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			// logging
			long buffReaderTime = (System.currentTimeMillis() - start - responseTime);
			Log.d("MY", "response.getEntity().getContent(): " + buffReaderTime
					+ "ms");

			String line = "";
			String NL = System.getProperty("line.separator");

			while ((line = in.readLine()) != null)
				sb.append(line + NL);

			in.close();

			// logging
			Log.d("MY",
					"BufferedReader fetch time: "
							+ (System.currentTimeMillis() - start
									- responseTime - buffReaderTime) + "ms");
			Log.i("MY",
					"download time(" + k.value + "): "
							+ (System.currentTimeMillis() - start) + "ms");

			// attach source code to input item
			result = sb.toString();

		} catch (Exception e) {
			Log.e("MY", e.toString());
		}

		return result;
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

	public URI generateEscapedQuery(Keyword k) throws URISyntaxException {
		return new URI("http://www.google.com/search?num=100&q="
				+ URLEncoder.encode(k.value));
	}

	public String generateEscapedQueryString(Keyword k) {

		return "http://www.google.com/search?num=100&q="
				+ URLEncoder.encode(k.value);
	}

	public String removePrefix(String searchable) {

		String result = searchable.replace("http://", "").replace("www.", "");

		return result;
	}

}
