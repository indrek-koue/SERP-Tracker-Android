package com.inc.im.serptracker.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.R;
import com.inc.im.serptracker.data.DbAdapter;
import com.inc.im.serptracker.data.Keyword;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AsyncDownloader extends
		AsyncTask<ArrayList<Keyword>, Integer, ArrayList<Keyword>> {

	private final int TIMEOUT = 10000;
	private final int DCOUNT = 100;

	private Activity a;
	public ListView lv;
	private final String WEBSITE;
	private ProgressDialog progressDialog;

	private String flurryExportParameters;
	private int itemCount;
	private long start;

	public AsyncDownloader(Activity a, ListView lv, String searchable) {
		this.a = a;
		this.lv = lv;
		this.WEBSITE = removePrefix(searchable);
	}

	@Override
	protected void onPreExecute() {

		progressDialog = ProgressDialog.show(a,
				a.getString(R.string.inspect_dialog_title),
				a.getString(R.string.inspect_dialog_fist_msg), true, true);

		start = System.currentTimeMillis();

	}

	@Override
	protected ArrayList<Keyword> doInBackground(ArrayList<Keyword>... keywords) {

		if (keywords == null || keywords.length == 0)
			return null;

		// count items for the load screen
		itemCount = keywords[0].size();

		for (int counter = 0; counter < keywords[0].size(); counter++) {
			Keyword keyword = keywords[0].get(counter);

			if (!progressDialog.isShowing())
				return null;

			Elements allResults = downloadJsoapParseH3FirstA(keyword);

			if (allResults != null) {
				removeNotValidUrls(allResults);
				bindIntoCustomElementKeyword(keyword, allResults);
			}

			int progress = counter;
			publishProgress(++progress);

		}

		flurryLogging();

		return keywords[0];

	}

	public void bindIntoCustomElementKeyword(Keyword keyword,
			Elements allResults) {

		if (keyword == null)
			return;

		for (int i = 0; i < allResults.size(); i++) {
			Element singleResult = allResults.get(i);

			if (singleResult != null) {

				String singleResultAnchor = singleResult.text();
				String singleResultUrl = singleResult.attr("href");

				// if cointains url and is not set yet
				if (singleResultUrl.contains(WEBSITE) && keyword.newRank == 0) {

					keyword.newRank = i;
					keyword.anchorText = singleResultAnchor;
					keyword.url = singleResultUrl;

					// save flurry output
					flurryExportParameters += i + "";

				}
			}
		} // for links in keyword

		flurryExportParameters += ":";
	}

	public void removeNotValidUrls(Elements allResults) {

		// remove not valid urls
		for (int i = 0; i < allResults.size(); i++)
			if (allResults.get(i).attr("href").startsWith("/search?q=")) {
				allResults.remove(i);
			}

		// loging - remove
		if (allResults.size() != 100)
			Log.w("MY", "WARNING: results after delete != 100, instead:"
					+ allResults.size());
	}

	@Override
	protected void onPostExecute(ArrayList<Keyword> input) {

		if (input == null) {
			progressDialog.setMessage(a
					.getString(R.string.error1_input_keywords_are_null));
			return;
		}

		// if progressdialog is canceled, dont show results
		if (!progressDialog.isShowing())
			return;

		ArrayList<String> toDisplay = new ArrayList<String>();

		for (Keyword k : input) {

			// not ranked
			if (k.newRank == 0) {
				toDisplay.add(k.value + a.getString(R.string._not_ranked_));
				// ranked
			} else {
				// save new rank
				if (!new DbAdapter(a).updateKeywordRank(k, k.newRank))
					Log.e("MY", "keyword rank update failed: " + k.value);

				// show user
				int valueChange = k.rank - k.newRank;

				// -2 = error
				// -1 = new?
				// 0 = not ranked

				String ready = "";
				String sign = valueChange > 0 ? "+" : "";

				// is ranked and value change exists and is not new show long
				// info
				if (k.rank != 0 && valueChange != 0 && k.rank != -1)
					ready = String.format("%s [ %d ] %s%d", k.value, k.newRank,
							sign, valueChange);
				else
					ready = String.format("%s [ %d ]", k.value, k.newRank);

				toDisplay.add(ready);

			}
		}// for

		lv.setAdapter(new ArrayAdapter<String>(a,
				R.layout.main_activity_listview_item, R.id.textView1, toDisplay));

		progressDialog.dismiss();

	}

	@Override
	protected void onProgressUpdate(Integer... values) {

		String loaderValue = a.getString(R.string.keyword_) + " " + values[0]
				+ "/" + itemCount;

		progressDialog.setMessage(loaderValue);

	}

	public void flurryLogging() {

		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("num of keywords", String.valueOf(itemCount));
		parameters.put("total time",
				String.valueOf(System.currentTimeMillis() - start));
		parameters.put("avg time per keyword", String.valueOf((System
				.currentTimeMillis() - start) / itemCount));
		parameters.put("results", String.valueOf(flurryExportParameters));

		FlurryAgent.onEvent("download", parameters);

	}

	private Elements downloadJsoapParseH3FirstA(Keyword keyword) {

		if (keyword == null)
			return null;

		Document doc = null;
		try {
			// Log.i("MY", "Q-URL:" + generateEscapedQueryString(keyword,
			// false));

			String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30";

			doc = Jsoup.connect(generateEscapedQueryString(keyword, false))
					.userAgent("Apache-HttpClient/UNAVAILABLE (java 1.4)")
					.timeout(TIMEOUT).get();

		} catch (Exception e1) {
			Log.e("MY", e1.toString());

		}

		if (doc == null) {
			Log.w("MY", "download is null");
			FlurryAgent.onError("MINOR-EX", "JSOUP download is null",
					"AsyncDownloader");
			return null;
		}

		Elements allResults = doc.select("h3 > a");
		if (allResults == null) {
			Log.w("MY", "downloaded allResults h3 first a is null");
			FlurryAgent.onError("MINOR-EX",
					"JSOUP download h3 first a select is null",
					"AsyncDownloader");
			return null;
		}

		return allResults;
	}

	public String generateEscapedQueryString(Keyword k, Boolean noMobile) {
		if (noMobile)
			return "http://www.google.com/search?num=" + DCOUNT + "&nomo=1&q="
					+ URLEncoder.encode(k.value);
		else
			return "http://www.google.com/search?num=" + DCOUNT + "&q="
					+ URLEncoder.encode(k.value);
	}

	public String removePrefix(String searchable) {

		String result = searchable.replace("http://", "").replace("www.", "");

		return result;
	}

}
