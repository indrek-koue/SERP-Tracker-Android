package com.inc.im.serptracker.util;

import java.net.URLEncoder;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import com.inc.im.serptracker.data.Keyword;

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
	public static Elements parse(Keyword keyword, Document doc) {

		Elements allResults = doc.select("h3 > a");

		if (allResults == null || allResults.size() == 0) {
			Log.w("MY", "downloaded allResults h3 first a is null");
			keyword.newRank = -2;

			return null;
		}

		removeNotValidUrls(allResults);

		return allResults;
	}

	/**
	 * Calculates ranking from allResults by WEBSITE and saves to keyword
	 * 
	 * @param keyword
	 *            results saved here
	 * @param allResults
	 *            where to find user ranking
	 * @param WEBSITE
	 *            by what find user ranking
	 */
	public static void getRanking(Keyword keyword, Elements allResults,
			String WEBSITE) {

		if (keyword == null)
			return;

		for (int i = 0; i < allResults.size(); i++) {
			Element singleResult = allResults.get(i);

			if (keyword.newRank != 0)
				return;

			if (singleResult != null) {

				String singleResultAnchor = singleResult.text();
				String singleResultUrl = singleResult.attr("href");

				// if cointains url and is not set yet
				if (singleResultUrl.contains(WEBSITE)) {

					keyword.newRank = i + 1;
					keyword.anchorText = singleResultAnchor;
					keyword.url = singleResultUrl;

				}

			}
		} // for links in keyword

		// not ranked
		if (keyword.newRank == 0)
			keyword.newRank = -1;

	}

	/**
	 * Removes advertisements links
	 * 
	 * @param allResults
	 */
	public static void removeNotValidUrls(Elements allResults) {

		// remove ad urls
		for (int i = 0; i < allResults.size(); i++)
			if (allResults.get(i).attr("href").startsWith("/search?q=")
					|| allResults.get(i).attr("href").startsWith("/aclk?")) {
				allResults.remove(i);
			}

		// TODO: remove first place multible combos ex. keyword:adobe
		String first = removePrefix(allResults.get(0).attr("href"));

		String temp = first.substring(0, first.indexOf("/"));
		Log.d("MY", temp);

		// if first 7 are same, then remove 6
		// if x links with same domain are in row, remove all except one

		// loging - remove
		if (allResults.size() != 100)
			Log.w("MY", "WARNING: results after delete != 100, instead:"
					+ allResults.size());
	}

	public static String generateEscapedQueryString(Keyword k, Boolean noMobile) {
		if (noMobile)
			return "http://www.google.com/search?num=" + DCOUNT + "&nomo=1&q="
					+ URLEncoder.encode(k.value);
		else
			return "http://www.google.com/search?num=" + DCOUNT + "&q="
					+ URLEncoder.encode(k.value);
	}

	public static String removePrefix(String searchable) {

		String result = searchable.replace("http://", "").replace("www.", "");

		return result;
	}

}
