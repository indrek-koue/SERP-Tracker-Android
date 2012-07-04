package com.inc.im.serptracker.data.access;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptrackerpremium.R;
import com.inc.im.serptracker.PreferencesActivity;
import com.inc.im.serptracker.adapters.DbAdapter;
import com.inc.im.serptracker.adapters.MainActivityListAdapter;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.util.Parser;

import java.util.ArrayList;
import java.util.HashMap;

/**
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
	public static boolean banned = false;

	private int itemCount;
	private long start;

	public AsyncDownloader(Activity a, ListView lv, String searchable) {
		this.a = a;
		this.lv = lv;
		this.WEBSITE = Parser.removePrefix(searchable);
	}

	@Override
	protected void onPreExecute() {

		Toast.makeText(
				a,
				PreferenceManager.getDefaultSharedPreferences(a).getString(
						PreferencesActivity.PREF_LOCALIZE, "Google.com")
						+ " - "
						+ PreferenceManager.getDefaultSharedPreferences(a)
								.getString(PreferencesActivity.PREF_LOCALIZE,
										"Google Chrome"), Toast.LENGTH_LONG)
				.show();

		progressDialog = ProgressDialog.show(a,
				a.getString(R.string.inspect_dialog_title),
				a.getString(R.string.inspect_dialog_fist_msg), true, true);

		start = System.currentTimeMillis();

	}

	@Override
	protected ArrayList<Keyword> doInBackground(ArrayList<Keyword>... keywords) {

		banned = false;

		if (keywords == null || keywords.length == 0)
			return null;

		// count items for the load screen
		itemCount = keywords[0].size();
		ArrayList<Keyword> result = new ArrayList<Keyword>();

		for (int counter = 0; counter < keywords[0].size(); counter++) {

			if (banned == true)
				return null;

			publishProgress(counter + 1);

			if (counter != 0)
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			Keyword keyword = keywords[0].get(counter);

			if (!progressDialog.isShowing())
				return null;

			// download and parse h3first
			result.add(Parser.downloadAndParse(a, keyword, WEBSITE));

		}

		flurryLogging(result);

		return result;

	}

	@Override
	protected void onPostExecute(ArrayList<Keyword> input) {

		// if progressdialog is canceled, dont show results
		if (!progressDialog.isShowing())
			return;

		// show dialog == you banned
		if (banned == true) {

			AlertDialog.Builder builder = new AlertDialog.Builder(a);
			builder.setMessage(
					"You have been temporarily banned from Google. Please try again in 2 hours with less keywords.")
					.setCancelable(true)
					.setPositiveButton(a.getString(R.string.OK),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
								}
							});

			AlertDialog alert = builder.create();
			alert.show();

			progressDialog.dismiss();

			return;
		}

		if (input == null) {
			progressDialog.dismiss();

			Toast.makeText(a, "ERROR", Toast.LENGTH_LONG).show();
			return;
		}

		lv.setAdapter(new MainActivityListAdapter(a, input));

		// save new ranks
		for (Keyword k : input)
			if (!new DbAdapter(a).updateKeywordRank(k, k.newRank))
				Log.e("MY", k.keyword + " save to db update failed");

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

}
