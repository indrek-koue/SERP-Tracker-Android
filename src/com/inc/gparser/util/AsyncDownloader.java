package com.inc.gparser.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.inc.gparser.MainActivity;
import com.inc.gparser.R;
import com.inc.gparser.data.Keyword;

import dalvik.system.TemporaryDirectory;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AsyncDownloader extends
		AsyncTask<ArrayList<Keyword>, Integer, ArrayList<String>> {

	String TAG = "MY";
	private Context con;
	private ListView lv;
	private final String searchable;

	// Params, the type of the parameters sent to the task upon execution.
	// Progress, the type of the progress units published during the background
	// computation.
	// Result, the type of the result of the background computation.
	//

	public AsyncDownloader(Context con, ListView lv, String searchable) {
		this.con = con;
		this.lv = lv;

		// remove http:// and www

		searchable.replace("http://", "");
		searchable.replace("www", "");

		Log.d("MY", "remover" + searchable);

		this.searchable = searchable;
	}

	@Override
	protected ArrayList<String> doInBackground(ArrayList<Keyword>... params) {

		Log.w("MY", "doInBackground");
		long start = System.currentTimeMillis();

		ArrayList<String> results = new ArrayList<String>();
		String result = null;

		ArrayList<Keyword> input = params[0];

		int counter = 1;
		
		for (Keyword k : input) {
			try {
				
				publishProgress(counter);
				counter++;
				
				StringBuffer sb = new StringBuffer("");
				// HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet();
				// URI uri = new URI(params[0]);

				request.setURI(new URI("http://www.google.com/search?q=" + URLEncoder.encode(k.value)));

				HttpResponse response;

				response = new DefaultHttpClient().execute(request);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));

				String line = "";
				String NL = System.getProperty("line.separator");

				while ((line = in.readLine()) != null)
					sb.append(line + NL);

				in.close();

				result = sb.toString();

			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
			// catch (ClientProtocolException e) {
			// Log.e(TAG, e.toString());
			// } catch (IOException e) {
			// Log.e(TAG, e.toString());
			// } catch (IllegalStateException e) {
			// Log.e(TAG, e.toString());
			// }
			

			Log.w(TAG, "DOWNLOAD TIME: " + (System.currentTimeMillis() - start)
					+ "ms");

			Log.w("MY", "downloaded chars: " + result.length());

			results.add(result);
		}
		return results;

	}

	@Override
	protected void onPostExecute(ArrayList<String> input) {
		// Log.w("MY", "onPostExecute");

		long start = System.currentTimeMillis();
		if (input == null) {
			// TODO: show warning here
			Log.w("MY", "download result is null");
			return;
		}

		ArrayList<String> temp = new ArrayList<String>();

		for (String s : input) {

			ArrayList<String> links = new GoogleParser().parse(s);

			int rank = -1;

			// search
			for (int i = 0; i < links.size(); i++) {
				Log.w("MY", links.get(i));
				if (links.get(i).contains(searchable)) {
					// hurray, found!
					rank = i++;
				}
			}

			Log.w("MY", "RANK NR:" + rank);

			temp.add(Integer.toString(rank));

		}
		ArrayAdapter<String> aa = new ArrayAdapter<String>(con,
				android.R.layout.simple_list_item_1, temp);

		lv.setAdapter(aa);

		Log.w(TAG, "PARSE TIME: " + (System.currentTimeMillis() - start) + "ms");

	}

	@Override
	protected void onProgressUpdate(Integer... values) {

		Log.w("MY", "onProgressUpdate: " + values[0]);

		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}

}
