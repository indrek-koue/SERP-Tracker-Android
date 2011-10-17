package com.inc.im.serptracker.data.access;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.R;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.util.MainActivityHelper;
import com.inc.im.serptracker.util.Parser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

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

			if (!progressDialog.isShowing())
				return null;

			Keyword keyword = keywords[0].get(counter);

			//Elements raw = Download.H3FirstA(keyword);

			Elements raw = Parser.parse(keyword, Download.H3FirstA(keyword));
			
			// find WEBSITE in H3FirstA and save ranking into keyword
			if (raw != null)
				Parser.getRanking(keyword, raw, WEBSITE);

			result.add(keyword);

			// int progress = counter;
			publishProgress(counter + 1);

		}

		flurryLogging(result);

		return result;

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

		MainActivityHelper.bindResultListView(a, lv, input);

		// save new ranks
		for (Keyword k : input)
			new DbAdapter(a).updateKeywordRank(k, k.newRank);

		progressDialog.dismiss();

	}

	@Override
	protected void onProgressUpdate(Integer... values) {

		String loaderValue = a.getString(R.string.keyword_) + " " + values[0]
				+ "/" + itemCount;

		progressDialog.setMessage(loaderValue);

	}

	public void flurryLogging(ArrayList<Keyword> result) {
		String values = "";

		for (Keyword k : result)
			values += k.newRank + ":";

		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("num of keywords", String.valueOf(itemCount));
		parameters.put("total time",
				String.valueOf(System.currentTimeMillis() - start));
		parameters.put("avg time per keyword", String.valueOf((System
				.currentTimeMillis() - start) / itemCount));
		parameters.put("results", String.valueOf(values));

		FlurryAgent.onEvent("download", parameters);
	}

//	public String removePrefix(String searchable) {
//
//		String result = searchable.replace("http://", "").replace("www.", "");
//
//		return result;
//	}

}
