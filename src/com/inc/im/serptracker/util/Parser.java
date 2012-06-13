
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

    private static ArrayList<String> allAnchors;
    private static ArrayList<String> allResults;

    private final static int DCOUNT = 100;

    /**
     * Gets h3 > a from Document and removes not valid urls
     * 
     * @param keyword - if parse fails save newrank -2
     * @param doc - find h3 > a in here
     * @return Elements of results
     */
    public static void parse(Activity a, Keyword keyword) {

        Document doc = Download.H3FirstA(a, keyword);

        allAnchors = new ArrayList<String>();
        allResults = new ArrayList<String>();

        // @added ver 1.3 - exception fix
        if (doc == null)
            return;

        Elements allResultsE = doc.select(a
                .getString(R.string.googleResultParseRule));

        // 14.02.2012 - ver 2.15 - google muutis oma urle

        Log.i("MY", "ALL RESULT SIZE: " + allResultsE.size());

        for (int i = 0; i < allResultsE.size(); i++) {
            // for (Element e : allResultsE) {
            Element e = allResultsE.get(i);

            Log.i("MY", e.attr("href").replace("/url?q=", ""));
            allResults.add(e.attr("href").replace("/url?q=", ""));
            allAnchors.add(e.text());

        }

        if (allResults.size() == 0) {
            Log.e("MY", "downloaded allResults h3 first a is null");
            keyword.newRank = -2;
            return;
        }

        removeNotValidUrls();

    }

    /**
     * @param allResults where to find
     * @param WEBSITE what to find
     */
    public static Keyword getRanking(Keyword keyword, String WEBSITE) {

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
            if (numOfResults <= 95 || numOfResults >= 105) {

                Map<String, Integer> data = new HashMap<String, Integer>();

                data.put("result count", numOfResults);

                FlurryAgent.logEvent("DEBUG: parse count after invalid delete",
                        data);
            }

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
    public static void removeNotValidUrls() {

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
