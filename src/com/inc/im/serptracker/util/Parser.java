
package com.inc.im.serptracker.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.R;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.data.access.Download;

/**
 * Parses raw document into objects using Jsoup parsing engine.
 * 
 * @author indrek
 */

public class Parser {

    // private static ArrayList<String> allAnchors;
    // private static ArrayList<String> allResults;

    private final static String CHROME = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1092.0 Safari/536.6";
    private final static String FIREFOX = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:15.0) Gecko/20120427 Firefox/15.0a1";
    private final static String EXPLORER = "Mozilla/5.0 (compatible; MSIE 10.6; Windows NT 6.1; Trident/5.0; InfoPath.2; SLCC1; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 2.0.50727) 3gpp-gba UNTRUSTED/1.0";
    private final static String OPERA = "Opera/9.80 (Windows NT 6.1; U; es-ES) Presto/2.9.181 Version/12.00";
    private final static String SAFARI = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.55.3 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10";
    private final static String UNAVAILABLE = "Apache-HttpClient/UNAVAILABLE (java 1.4)";
    private final static String EMPTY = "";

    private final static int DCOUNT = 100;

    /**
     * Gets h3 > a from Document and removes not valid urls
     * 
     * @param keyword - if parse fails save newrank -2
     * @param doc - find h3 > a in here
     * @return
     * @return Elements of results
     */
    public static Keyword downloadAndParse(Activity a, Keyword keyword, String WEBSITE) {

        String ua = getUserAgentStringFromPref(a);

        ArrayList<String> allAnchors = new ArrayList<String>();
        ArrayList<String> allResults = new ArrayList<String>();

        for (int j = 0; j < 10; j++) {
            Log.i("MY", "LOOP NR: " + j);

            Document doc = Download.downloadAndGetH3FirstA(a, keyword, j, ua);

            if (doc != null) {
                Elements allResultsE = doc.select(a
                        .getString(R.string.googleResultParseRule));

                for (int i = 0; i < allResultsE.size(); i++) {
                    Element e = allResultsE.get(i);

                    // Log.i("MY", e.attr("href").replace("/url?q=", ""));
                    allResults.add(e.attr("href").replace("/url?q=", ""));
                    allAnchors.add(e.text());

                }
            }
        }

        allResults = removeNotValidUrls(allResults);

        // print out for logging
        int i = 0;
        for (String s : allResults)
            Log.i("MY", i++ + ". " + s);
        Log.i("MY", keyword.keyword + " RESULTS: " + allResults.size());

        if (allResults.size() == 0) {
            Log.e("MY", "downloaded allResults h3 first a is null");
            keyword.newRank = -2;
            return keyword;
        }

        // removeNotValidUrls();

        Keyword updatedKeyword = Parser.getRanking(keyword, WEBSITE,
                allResults, allAnchors);

        if (updatedKeyword != null) {
            return updatedKeyword;
        } else {
            // nothing found = add old back
            return keyword;
        }

    }

    private static String getUserAgentStringFromPref(Activity a) {

        String uaSelector = PreferenceManager.getDefaultSharedPreferences(a)
                .getString("prefUa", "Google Chrome");

        String ua = CHROME;
        if (uaSelector.equals("Firefox"))
            ua = FIREFOX;
        else if (uaSelector.equals("Internet Explorer"))
            ua = EXPLORER;
        else if (uaSelector.equals("Opera"))
            ua = OPERA;
        else if (uaSelector.equals("Safari"))
            ua = SAFARI;
        else if (uaSelector.equals("Unavailable"))
            ua = UNAVAILABLE;
        else if (uaSelector.equals("Empty"))
            ua = EMPTY;

        Log.i("MY", "UA: " + ua);

        return ua;

    }

    /**
     * @param allResults where to find
     * @param WEBSITE what to find
     */
    public static Keyword getRanking(Keyword keyword, String WEBSITE, ArrayList<String> allResults,
            ArrayList<String> allAnchors) {

        if (keyword == null || allResults == null)
            return null;

        int numOfResults = allResults.size();

        Keyword result = new Keyword(keyword.keyword);
        result.oldRank = keyword.oldRank;
        result.id = keyword.id;

        for (int i = 0; i < numOfResults; i++) {

            if (result.newRank != 0)
                return result;

            String singleResultUrlModified = removePrefix(allResults.get(i));

            // second boolean is for subdomains. For example when person
            // searches for wikipedia he probably wants to get the
            // en.wikipedia.org etc
            if (singleResultUrlModified.startsWith(WEBSITE + "/")
                    || singleResultUrlModified.contains("." + WEBSITE + "/")) {

                if (numOfResults <= DCOUNT) {
                    result.newRank = i + 1;
                } else {

                    // WE HAVE TO JUSTIFY RANK
                    // there is a authority site with sub links somewhere
                    // probably

                    int overTheNormal = numOfResults - DCOUNT;
                    int newRank = i + 1 - overTheNormal;

                    if (newRank <= 0)
                        newRank = 1;

                    result.newRank = newRank;
                }

                result.anchorText = allAnchors.get(i);
                result.url = allResults.get(i);

            }

            // flurry loging
            // if (numOfResults <= 95 || numOfResults >= 105) {
            //
            // Map<String, Integer> data = new HashMap<String, Integer>();
            //
            // data.put("result count", numOfResults);
            //
            // FlurryAgent.logEvent("DEBUG: parse count after invalid delete",
            // data);
            // }

        } // for links in keyword

        // not ranked
        if (result.newRank == 0)
            result.newRank = -1;

        return result;

    }

    /**
     * Removes advertisements links, meaning all local links meaning all links
     * starting with /
     * 
     * @param allResults
     */
    public static ArrayList<String> removeNotValidUrls(ArrayList<String> allResults) {

        for (int i = 0; i < allResults.size(); i++) {

            if (allResults.get(i).startsWith("/")) {
                Log.i("MY", "removed: " + allResults.get(i));

                allResults.remove(i);
                i--;
            }

        }

        if (allResults.size() != 100)
            Log.w("MY",
                    "WARNING: results after internal link delete != 100, instead:"
                            + allResults.size());

        return allResults;

    }

    /**
     * Removes http, https and www. from the beginning and converts to lowercase
     * 
     * @param searchable
     * @return
     */
    public static String removePrefix(String searchable) {

        if (searchable == null)
            return "";

        String result = searchable.replace("https://", "").replace("http://",
                "");

        if (result.startsWith("www."))
            result = result.replace("www.", "");

        return result.toLowerCase();
    }

}
