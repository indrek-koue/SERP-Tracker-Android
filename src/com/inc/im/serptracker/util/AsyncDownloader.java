package com.inc.im.serptracker.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.R;
import com.inc.im.serptracker.data.DbAdapter;
import com.inc.im.serptracker.data.Keyword;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AsyncDownloader extends
		AsyncTask<ArrayList<Keyword>, Integer, ArrayList<Keyword>> {

	private final int TIMEOUT = 10000;
	private final int DCOUNT = 100;

	private Activity a;
	public ListView lv;
	private final String WEBSITE;
	private ProgressDialog progressDialog;

	private int itemCount;

	public AsyncDownloader(Activity a, ListView lv, String searchable) {
		this.a = a;
		this.lv = lv;
		this.WEBSITE = removePrefix(searchable);
	}

	@Override
	protected void onPreExecute() {

		progressDialog = ProgressDialog.show(a,
				a.getString(R.string.inspect_dialog_title),
				a.getString(R.string.inspect_dialog_fist_msg), true, true);

	}

	@Override
	protected ArrayList<Keyword> doInBackground(ArrayList<Keyword>... keywords) {

//		try {

			if (keywords == null || keywords.length == 0)
				return null;

			// time logging
			long downloadTime = 0L;
			long start = System.currentTimeMillis();

			// count items for the load screen
			itemCount = keywords[0].size();
			int counter = 0;

			// output to flurry
			String flurryExportParameters = "";

			for (Keyword keyword : keywords[0]) {

				if (!progressDialog.isShowing())
					return null;

				// individual keyword time logging
				long startJsoupParse = System.currentTimeMillis();

				// jsoup download
				Document doc = downloadJsoap(keyword);

				// custom download
				// Document doc = Jsoup.parse(manageDownload(keyword.value));

				if (doc == null) {
					Log.e("MY", "download is null");
					FlurryAgent.onError("EX", "doc download is null", "AsyncDownloader");
					return null;
				}

				// logging
				downloadTime = System.currentTimeMillis() - startJsoupParse;
				Log.i("BENCH", "Download+bind if custom download ("
						+ keyword.value + "): " + downloadTime);

				Elements allResults = doc.select("h3 > a");
				if (allResults == null) {
					Log.e("MY", "allResults h3a is null");
					FlurryAgent.onError("EX", "h3 first a select is null", "AsyncDownloader");
					return null;
				}

				// logging
				// if (allResults.size() == 0) {
				// Log.e("MY", "0 results parsed from doc");
				//
				// for (Element e : doc.getElementsByTag("a"))
				// Log.e("MY", e.text());
				// // -2 == problem getting rank
				// keyword.newRank = -2;
				// flurryExportParameters += -2 + "";
				//
				// } else
				Log.i("MY",
						"results downloaded:"
								+ Integer.toString(allResults.size()));

				// 2nd try - disabled ATM
				// if (allResults.size() == 0) {
				// Log.e("MY", "all results was 0 so all links are: ");
				// for (Element e : doc.getElementsByTag("a"))
				// Log.e("MY", e.attr("href"));
				//
				// // try again
				// allResults = downloadJsoap(keyword).select("h3 > a");
				//
				// if (allResults.size() == 0)
				// keyword.newRank = -2;
				//
				// }

				// remove not valid urls
				for (int i = 0; i < allResults.size(); i++)
					if (allResults.get(i).attr("href").startsWith("/search?q=")) {
						allResults.remove(i);
					}

				// loging remove
				if (allResults.size() != 100)
					Log.w("MY",
							"WARNING: results after delete != 100, instead:"
									+ allResults.size());

				int i = 1;
				for (Element singleResult : allResults) {

					if (singleResult != null) {
						String singleResultAnchor = singleResult.text();
						String singleResultUrl = singleResult.attr("href");

						// if cointains url and is not set yet
						if (singleResultUrl.contains(WEBSITE)
								&& keyword.newRank == 0) {

							keyword.newRank = i;

							// flurry output
							flurryExportParameters += i + "";

							// reset counter
							i = 1;

							keyword.anchorText = singleResultAnchor;
							keyword.url = singleResultUrl;

						}
						i++;
					} // if singelresult!= null
				} // for links in keyword

				flurryExportParameters += ":";

				publishProgress(++counter);

				Log.i("BENCH",
						"Parse time: "
								+ (System.currentTimeMillis() - startJsoupParse - downloadTime));

			} // for keywords

			// Log.i("BENCH", "Parse time: "
			// + (System.currentTimeMillis() - start - ));

			flurryLogging(start, flurryExportParameters);

//		} catch (Exception e) {
//			FlurryAgent.onError("EX", e.toString(), "AsyncDownloader");
//			return null;
//		}

		return keywords[0];

	}

	@Override
	protected void onPostExecute(ArrayList<Keyword> input) {

		if (input == null) {
			progressDialog
					.setMessage("ERROR1: download is null or canceled by the user");
			return;
		}

		// if progressdialog is canceled, dont show results
		if (!progressDialog.isShowing())
			return;

		ArrayList<String> toDisplay = new ArrayList<String>();

		for (Keyword k : input) {

			if (k.newRank == 0) {
				toDisplay.add(k.value + " [not ranked]");
				// } else if (k.newRank == -2) {
				// toDisplay.add(k.value + " [error, try again]");
			} else {

				// save new rank
				if (!new DbAdapter(a).updateKeywordRank(k, k.newRank))
					Log.e("MY", "keyword rank update failed: " + k.value);

				// show user
				int valueChange = k.rank - k.newRank;

				String ready = "";
				String sign = valueChange > 0 ? "+" : "";

				if (k.rank != 0 && valueChange != 0 && k.rank != -1)
					ready = String.format("%s [ %d ] %s%d", k.value, k.newRank,
							sign, valueChange);
				else
					ready = String.format("%s [ %d ]", k.value, k.newRank);

				toDisplay.add(ready);

			}
		}// for

		lv.setAdapter(new ArrayAdapter<String>(a,
				R.layout.main_activity_listview_item, R.id.textView1, toDisplay));

		progressDialog.dismiss();

	}

	@Override
	protected void onProgressUpdate(Integer... values) {

		String loaderValue = "Keyword " + values[0] + "/" + itemCount;

		progressDialog.setMessage(loaderValue);

	}

	public void flurryLogging(long start, String flurryExportParameters) {
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("num of keywords", String.valueOf(itemCount));
		parameters.put("total time",
				String.valueOf(System.currentTimeMillis() - start));
		parameters.put("avg time per keyword", String.valueOf((System
				.currentTimeMillis() - start) / itemCount));
		parameters.put("results", String.valueOf(flurryExportParameters));

		FlurryAgent.onEvent("download", parameters);
	}

	private Document downloadJsoap(Keyword keyword) {

		Document doc = null;
		try {

			// get userAgent
			// String ua = getUserAgentString(a);
			// Log.i("MY", "USERAGENT: " + ua);
			Log.i("MY", "Q-URL:" + generateEscapedQueryString(keyword, false));

			doc = Jsoup.connect(generateEscapedQueryString(keyword, false))
					.userAgent("Apache-HttpClient/UNAVAILABLE (java 1.4)")
					.timeout(TIMEOUT).get();

		} catch (IOException e1) {
			Log.e("MY", e1.toString());
//			FlurryAgent.onError("EX", "JSOUP DOWNLOAD ERROR: " + e1.toString(),
//					"AsyncDownloader");
		}
		return doc;
	}

	// public static String getUserAgentString(Context context) {
	// try {
	// Constructor<WebSettings> constructor = WebSettings.class
	// .getDeclaredConstructor(Context.class, WebView.class);
	// constructor.setAccessible(true);
	// try {
	// WebSettings settings = constructor.newInstance(context, null);
	// return settings.getUserAgentString();
	// } finally {
	// constructor.setAccessible(false);
	// }
	// } catch (Exception e) {
	// FlurryAgent.onError("DOWNLOAD", "get user agent problem",
	// "AsyncDownloader");
	//
	// return "";
	// }
	// }

	// public String manageDownload(String keyword) {
	//
	// String result = null;
	// try {
	// StringBuffer sb = new StringBuffer("");
	//
	// HttpGet request = new HttpGet();
	// request.setURI(generateEscapedQuery(keyword));
	//
	// // logging
	// // long start = System.currentTimeMillis();
	//
	// HttpResponse response = new DefaultHttpClient().execute(request);
	//
	// // logging
	// // long responseTime = (System.currentTimeMillis() - start);
	// // Log.d("MY", "DefaultHttpClient().execute(request): " +
	// // responseTime
	// // + "ms");
	//
	// BufferedReader in = new BufferedReader(new InputStreamReader(
	// response.getEntity().getContent()));
	//
	// // logging
	// // long buffReaderTime = (System.currentTimeMillis() - start -
	// // responseTime);
	// // Log.d("MY", "response.getEntity().getContent(): " +
	// // buffReaderTime
	// // + "ms");
	//
	// String line = "";
	// String NL = System.getProperty("line.separator");
	//
	// while ((line = in.readLine()) != null)
	// sb.append(line + NL);
	//
	// in.close();
	//
	// // logging
	// // Log.d("MY",
	// // "BufferedReader fetch time: "
	// // + (System.currentTimeMillis() - start
	// // - responseTime - buffReaderTime) + "ms");
	// // Log.i("MY",
	// // "download time(" + keyword + "): "
	// // + (System.currentTimeMillis() - start) + "ms");
	//
	// // attach source code to input item
	// result = sb.toString();
	//
	// } catch (Exception e) {
	// Log.e("MY", e.toString());
	// }
	//
	// return result;
	// }

	// public Keyword manageDownload(Keyword k) {
	//
	// // String result = null;
	// try {
	// StringBuffer sb = new StringBuffer("");
	//
	// HttpGet request = new HttpGet();
	// request.setURI(generateEscapedQuery(k));
	//
	// // logging
	// long start = System.currentTimeMillis();
	//
	// HttpResponse response = new DefaultHttpClient().execute(request);
	//
	// // logging
	// long responseTime = (System.currentTimeMillis() - start);
	// Log.d("MY", "DefaultHttpClient().execute(request): " + responseTime
	// + "ms");
	//
	// BufferedReader in = new BufferedReader(new InputStreamReader(
	// response.getEntity().getContent()));
	//
	// // logging
	// long buffReaderTime = (System.currentTimeMillis() - start -
	// responseTime);
	// Log.d("MY", "response.getEntity().getContent(): " + buffReaderTime
	// + "ms");
	//
	// String line = "";
	// String NL = System.getProperty("line.separator");
	//
	// while ((line = in.readLine()) != null)
	// sb.append(line + NL);
	//
	// in.close();
	//
	// // logging
	// Log.d("MY",
	// "BufferedReader fetch time: "
	// + (System.currentTimeMillis() - start
	// - responseTime - buffReaderTime) + "ms");
	// Log.d("MY", "total: " + (System.currentTimeMillis() - start) + "ms");
	//
	// // attach source code to input item
	// k.htmlSourceCode = sb.toString();
	//
	// } catch (Exception e) {
	// Log.e("MY", e.toString());
	// }
	//
	// return k;
	// }

	// public URI generateEscapedQuery(String k) throws URISyntaxException {
	// return new URI("http://www.google.com/search?num=100&q="
	// + URLEncoder.encode(k));
	// }

	public String generateEscapedQueryString(Keyword k, Boolean noMobile) {
		if (noMobile)
			return "http://www.google.com/search?num=" + DCOUNT + "&nomo=1&q="
					+ URLEncoder.encode(k.value);
		else
			return "http://www.google.com/search?num=" + DCOUNT + "&q="
					+ URLEncoder.encode(k.value);
	}

	public String removePrefix(String searchable) {

		String result = searchable.replace("http://", "").replace("www.", "");

		return result;
	}

}
