
package com.inc.im.serptracker.data.access;

import java.io.IOException;
import java.net.URLEncoder;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
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

    private final static String CHROME = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1092.0 Safari/536.6";
    private final static String FIREFOX = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:15.0) Gecko/20120427 Firefox/15.0a1";
    private final static String EXPLORER = "Mozilla/5.0 (compatible; MSIE 10.6; Windows NT 6.1; Trident/5.0; InfoPath.2; SLCC1; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 2.0.50727) 3gpp-gba UNTRUSTED/1.0";
    private final static String OPERA = "Opera/9.80 (Windows NT 6.1; U; es-ES) Presto/2.9.181 Version/12.00";
    private final static String SAFARI = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.55.3 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10";
    private final static String UNAVAILABLE = "Apache-HttpClient/UNAVAILABLE (java 1.4)";
    private final static String EMPTY = "";

    /**
     * @param keyword used to generate URL for download
     * @return downloaded document from Google
     */
    public static Document H3FirstA(Activity a, Keyword keyword) {

        if (keyword == null)
            return null;

        Document doc = null;

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

        
        // try1
        try {
            Log.i("MY", "try1");
            doc = download(a, keyword, ua);

        } catch (Exception e1) {
            Log.e("MY", e1.toString());
        }

        // try 2
        if (doc == null)
            try {
                Log.i("MY", "try2");
                Thread.sleep(PAUSE1);
                doc = download(a, keyword, ua);

            } catch (Exception e1) {
                Log.e("MY", e1.toString());

            }

        // try 3
        if (doc == null)
            try {
                Log.i("MY", "try3");
                Thread.sleep(PAUSE2);
                doc = download(a, keyword, ua);

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
            throws Exception {

        String httpQuery = generateEscapedQueryString(a, keyword);

        Log.i("MY", "QUERY: " + httpQuery);
        Log.i("MY", "UA: " + ua);

        // v. 2.11 fix
        Connection con = Jsoup.connect(httpQuery)
                .userAgent(ua).header("Accept", "text/plain").timeout(TIMEOUT);

        if (con != null) {

            Document doc = null;
            try {
                doc = con.get();
            } catch (Exception e) {
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

        return "http://www." + userSearchEngine + "/search?num=100&q="
                + URLEncoder.encode(k.keyword);
    }

}
