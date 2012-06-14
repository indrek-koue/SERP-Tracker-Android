
package com.inc.im.serptracker.data.access;

import java.io.IOException;
import java.net.URLEncoder;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection.Response;
import org.jsoup.nodes.Document;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.R;
import com.inc.im.serptracker.data.Keyword;

/**
 * All data is download through this class. Uses JSoup based on java networking
 * to get the data.
 */

public class Download {

    private final static int TIMEOUT = 10000;
    private final static int PAUSE1 = 500;
    private final static int PAUSE2 = 2000;

    /**
     * @param keyword used to generate URL for download
     * @return downloaded document from Google
     */
    public static Document downloadAndGetH3FirstA(Activity a, Keyword keyword, int i, String ua) {

        if (keyword == null)
            return null;

        if (AsyncDownloader.banned)
            return null;

        Document doc = null;

        // try1
        try {
            Log.i("MY", "try1");
            doc = download(a, keyword, ua, i);
        } catch (Exception e1) {
            Log.e("MY", e1.toString());
        }

        // try 2
        if (doc == null)
            try {
                Log.i("MY", "try2");
                Thread.sleep(PAUSE1);
                doc = download(a, keyword, ua, i);

            } catch (Exception e1) {
                Log.e("MY", e1.toString());

            }

        // try 3
        if (doc == null)
            try {
                Log.i("MY", "try3");
                Thread.sleep(PAUSE2);
                doc = download(a, keyword, ua, i);

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

    private static Document download(Activity a, Keyword keyword, String ua, int pageNum)
            throws Exception {

        String httpQuery = "";

        if (pageNum == 0) {
            httpQuery = generateEscapedQueryString(a, keyword);
        } else {
            httpQuery = generateEscapedQueryString(a, keyword) + "&start=" + pageNum * 10;
        }
        Log.i("MY", "QUERY: " + httpQuery);

        // v. 2.11 fix
        Connection con = Jsoup.connect(httpQuery)
                .userAgent(ua).header("Accept", "text/plain").timeout(TIMEOUT);

        if (con != null) {

            Document doc = null;
            try {
                doc = con.get();

            } catch (Exception e) {

                if (e.getMessage().startsWith("503"))
                    AsyncDownloader.banned = true;

                e.printStackTrace();
            }

            return doc;

        } else {
            return null;
        }
    }

    public static String generateEscapedQueryString(Activity a, Keyword k) {

        String userSearchEngine;

        if (new Boolean(a.getString(R.string.isPremium))) {
            // get value from preference
            userSearchEngine = PreferenceManager.getDefaultSharedPreferences(a)
                    .getString("prefLocalize", "Google.com");
        } else {
            userSearchEngine = "google.com";
        }

        return "http://www." + userSearchEngine + "/search?q="
                + URLEncoder.encode(k.keyword);
    }

}
