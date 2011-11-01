package com.inc.im.serptracker.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Environment;
import android.util.Log;

import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.data.access.Download;

public class Parser {

	private final static int DCOUNT = 100;

	/**
	 * Gets h3 > a from Document and removes not valid urls
	 * 
	 * @param keyword
	 *            - if parse fails save newrank -2
	 * @param doc
	 *            - find h3 > a in here
	 * @return Elements of results
	 */
	public static Elements parse(Keyword keyword) {

		Document doc = Download.H3FirstA(keyword);

		// @added ver 1.3 - exception fix
		if (doc == null)
			return null;

		Elements allResults = doc.select("h3 > a");

		if (allResults == null || allResults.size() == 0) {
			Log.e("MY", "downloaded allResults h3 first a is null");
			keyword.newRank = -2;
			Log.e("MY", doc.text());
			Log.e("MY", doc.text().length() + "");
			Log.e("MY", Environment.getExternalStorageDirectory() + "");

			File f = new File(Environment.getExternalStorageDirectory(),
					"log.txt");

			try {
				FileWriter filenew = new FileWriter(
						Environment.getExternalStorageDirectory() + "log.txt");
				BufferedWriter bw = new BufferedWriter(filenew);
				bw.write(doc.toString());
				bw.close();
			} catch (IOException e) {
				Log.e("MY", e.toString());
			}

			// debug
			// for (Element e : doc.select("h3"))
			// Log.e("MY", e.text());

			// File f = new

			return null;
		}

		removeNotValidUrls(allResults);

		return allResults;
	}

	/**
	 * @param allResults
	 *            where to find
	 * @param WEBSITE
	 *            what to find
	 */
	public static Keyword getRanking(Keyword keyword, Elements allResults,
			String WEBSITE) {

		if (keyword == null || allResults == null)
			return null;

		 //logging
		 for (int i = 0; i < allResults.size(); i++) {
		 Element singleResult = allResults.get(i);
		 Log.i("MY", i + ". " + singleResult.attr("href"));
		 }

		Keyword result = new Keyword(keyword.keyword);
		result.oldRank = keyword.oldRank;
		result.id = keyword.id;

		for (int i = 0; i < allResults.size(); i++) {

			Element singleResult = allResults.get(i);

			if (result.newRank != 0)
				return result;

			if (singleResult != null) {

				String singleResultAnchor = singleResult.text();
				String singleResultUrl = singleResult.attr("href");

				// if cointains url and is not set yet
				if (singleResultUrl.contains(WEBSITE)) {

					if (allResults.size() <= DCOUNT) {

						result.newRank = i + 1;

						// keyword.newRank = i + 1;
					} else {

						// WE HAVE TO JUSTIFY RANK
						// there is a authority link with sub links somewhere
						// probably

						int overTheNormal = allResults.size() - DCOUNT;
						int newRank = i + 1 - overTheNormal;

						if (newRank <= 0)
							newRank = 1;

						result.newRank = newRank;
					}
					result.anchorText = singleResultAnchor;
					result.url = singleResultUrl;

				}

			}
		} // for links in keyword

		// not ranked
		if (result.newRank == 0)
			result.newRank = -1;

		return result;

	}

	/**
	 * Removes advertisements links
	 * 
	 * @param allResults
	 */
	public static void removeNotValidUrls(Elements allResults) {

		// remove ad urls
		for (int i = 0; i < allResults.size(); i++) {

			String temp = allResults.get(i).attr("href");
			// if (temp.startsWith("/search?q=") || temp.startsWith("/aclk?")) {
			if (temp.startsWith("/")) {
				allResults.remove(i);
			}
		}

		// loging - remove
		if (allResults.size() != 100)
			Log.w("MY", "WARNING: results after delete != 100, instead:"
					+ allResults.size());
	}

	public static String generateEscapedQueryString(Keyword k, Boolean noMobile) {
		if (noMobile)
			return "http://www.google.com/search?num=" + DCOUNT + "&nomo=1&q="
					+ URLEncoder.encode(k.keyword);
		else
			return "http://www.google.com/search?num=" + DCOUNT + "&q="
					+ URLEncoder.encode(k.keyword);
	}

	public static String removePrefix(String searchable) {

		String result = searchable.replace("http://", "").replace("www.", "");

		return result;
	}

}
