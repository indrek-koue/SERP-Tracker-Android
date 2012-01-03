package com.inc.im.serptrackerpremium.data.access;

import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.select.Elements;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptrackerpremium.R;
import com.inc.im.serptrackerpremium.adapters.DbAdapter;
import com.inc.im.serptrackerpremium.adapters.MainActivityListAdapter;
import com.inc.im.serptrackerpremium.data.Keyword;
import com.inc.im.serptrackerpremium.util.Parser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

/**
 * 
 * Downloads results from search engine asyncronosly and bind to elements. Uses
 * Download.java and Jsoup parsing engine.
 */

// 0 - empty field in DB - new
// -1 - not ranked in top 100
// -2 - error getting data

public class AsyncDownloader extends
		AsyncTask<ArrayList<Keyword>, Integer, ArrayList<Keyword>> {

	private Activity a;
	public ListView lv;
	private final String WEBSITE;
	private ProgressDialog progressDialog;

	private int itemCount;
	private long start;

	public AsyncDownloader(Activity a, ListView lv, String searchable) {
		this.a = a;
		this.lv = lv;
		this.WEBSITE = Parser.removePrefix(searchable);
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
		ArrayList<Keyword> result = new ArrayList<Keyword>();

		for (int counter = 0; counter < keywords[0].size(); counter++) {

			Keyword keyword = keywords[0].get(counter);

			if (!progressDialog.isShowing())
				return null;

			// download and parse h3first
			Elements raw = Parser.parse(a, keyword);

			// find WEBSITE ranking in raw
			if (raw != null) {

				// NB! has to return keyword because i need the anchor/link also
				// for premium

				Keyword updatedKeyword = Parser.getRanking(keyword, raw,
						WEBSITE);

				// keyword.newRank = newRank;
				if (updatedKeyword != null) {
					result.add(updatedKeyword);
				} else {
					// nothing found = add old back
					result.add(keyword);
				}

			} else {
				// nothing downloaded = add old back
				result.add(keyword);
			}

			// int progress = counter;
			publishProgress(counter + 1);

		}

		flurryLogging(result);

		return result;

	}

	@Override
	protected void onPostExecute(ArrayList<Keyword> input) {

		if (input == null)
			return;

		// if progressdialog is canceled, dont show results
		if (!progressDialog.isShowing())
			return;

		lv.setAdapter(new MainActivityListAdapter(a, input));

		// save new ranks
		for (Keyword k : input)
			if (!new DbAdapter(a).updateKeywordRank(k, k.newRank))
				Log.e("MY", k.keyword + " save to db update failed");

		progressDialog.dismiss();

	}

	@Override
	protected void onProgressUpdate(Integer... values) {

		String loaderValue = a.getString(R.string.keyword_) + " " + values[0]
				+ "/" + itemCount;

		progressDialog.setMessage(loaderValue);

	}

	public void flurryLogging(ArrayList<Keyword> result) {

		// ver 1.31 fix
		if (result == null)
			return;

		String values = "";
		for (Keyword k : result)
			values += k.newRank + ":";

		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("num of keywords", String.valueOf(itemCount));
		parameters.put("total time",
				String.valueOf(System.currentTimeMillis() - start));

		// ver 1.31 fix
		if ((System.currentTimeMillis() - start) != 0 && itemCount != 0)
			parameters.put(
					"avg time per keyword",
					String.valueOf((System.currentTimeMillis() - start)
							/ itemCount));

		parameters.put("results", values);

		FlurryAgent.onEvent("download", parameters);
	}

	// public String removePrefix(String searchable) {
	//
	// String result = searchable.replace("http://", "").replace("www.", "");
	//
	// return result;
	// }

}
