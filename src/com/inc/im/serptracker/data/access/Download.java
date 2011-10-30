package com.inc.im.serptracker.data.access;

import java.io.IOException;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.util.Parser;

public class Download {

	private final static int TIMEOUT = 10000;
	private final static int PAUSE = 500;
	private final static String ua = "Apache-HttpClient/UNAVAILABLE (java 1.4)";

	/**
	 * 
	 * @param keyword
	 *            used to generate URL for download
	 * @return downloaded document from Google
	 */
	public static Document H3FirstA(Keyword keyword) {

		if (keyword == null)
			return null;

		Document doc = null;

		// try1
		try {

			doc = download(keyword, ua);

		} catch (Exception e1) {
			Log.e("MY", e1.toString());
		}

		// try 2
		if (doc == null)
			try {

				Thread.sleep(PAUSE);
				doc = download(keyword, ua);

			} catch (Exception e1) {
				Log.e("MY", e1.toString());

			}

		// try 3
		if (doc == null)
			try {

				Thread.sleep(PAUSE);
				doc = download(keyword, ua);

			} catch (Exception e1) {
				Log.e("MY", e1.toString());

			}

		if (doc == null) {
			Log.w("MY", "download is null");
			FlurryAgent.onEvent("NO RANK after 3 tries");

			keyword.newRank = -2;

			return null;
		}
		return doc;
		// return Parser.parse(keyword, doc);
	}

	private static Document download(Keyword keyword, String ua)
			throws IOException {

		return Jsoup.connect(Parser.generateEscapedQueryString(keyword, false))
				.userAgent(ua).header("Accept", "text/plain").timeout(TIMEOUT)
				.get();

	}

}
