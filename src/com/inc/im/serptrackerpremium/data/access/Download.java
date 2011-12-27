package com.inc.im.serptrackerpremium.data.access;

import java.io.IOException;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptrackerpremium.R;
import com.inc.im.serptrackerpremium.data.Keyword;

/**
 * All data is download through this class. Uses JSoup based on java networking
 * to get the data.
 */

public class Download {

	private final static int TIMEOUT = 10000;
	private final static int PAUSE1 = 500;
	private final static int PAUSE2 = 2000;
	//private final static String ua = "Apache-HttpClient/UNAVAILABLE (java 1.4)";

	/**
	 * 
	 * @param keyword
	 *            used to generate URL for download
	 * @return downloaded document from Google
	 */
	public static Document H3FirstA(Activity a, Keyword keyword) {

		if (keyword == null)
			return null;

		Document doc = null;

		// try1
		try {
			Log.i("MY", "try1");
			doc = download(a, keyword, a.getString(R.string.userAgent));

		} catch (Exception e1) {
			Log.e("MY", e1.toString());
		}

		// try 2
		if (doc == null)
			try {
				Log.i("MY", "try2");
				Thread.sleep(PAUSE1);
				doc = download(a, keyword, a.getString(R.string.userAgent));

			} catch (Exception e1) {
				Log.e("MY", e1.toString());

			}

		// try 3
		if (doc == null)
			try {
				Log.i("MY", "try3");
				Thread.sleep(PAUSE2);
				doc = download(a, keyword, a.getString(R.string.userAgent));

			} catch (Exception e1) {
				Log.e("MY", e1.toString());

			}

		if (doc == null) {
			Log.e("MY", "download is null FAILED DOWNLOAD (after 3 tries)");
			FlurryAgent.onEvent("FAILED DOWNLOAD (after 3 tries)");

			keyword.newRank = -2;

			return null;
		}
		return doc;
	}

	private static Document download(Activity a, Keyword keyword, String ua)
			throws IOException {

		return Jsoup.connect(generateEscapedQueryString(a, keyword))
				.userAgent(ua).header("Accept", "text/plain").timeout(TIMEOUT)
				.get();

	}

	public static String generateEscapedQueryString(Activity a, Keyword k) {

		// get value from preference
		String userSearchEngine = PreferenceManager
				.getDefaultSharedPreferences(a).getString("prefLocalize",
						"Google.com");

		return "http://www." + userSearchEngine + "/search?num=100&q="
				+ URLEncoder.encode(k.keyword);
	}

}
