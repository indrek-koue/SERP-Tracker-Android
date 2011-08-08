package com.inc.gparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.inc.gparser.R;
import com.inc.gparser.data.DbAdapter;
import com.inc.gparser.data.Keyword;
import com.inc.gparser.data.UserProfile;
import com.inc.gparser.util.AsyncDownloader;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class MainActivity extends Activity {

	static String TAG = "MY";
	int num = 10;
	String query = "make money online";
	String siteToLookFor = "www.carlocab.com";

	String spinnerValue;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		ArrayList<UserProfile> data = new DbAdapter(getBaseContext())
				.loadAllProfiles();

		ArrayList<String> spinnerValues = new ArrayList<String>();

		for (UserProfile u : data)
			spinnerValues.add(u.url);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getBaseContext(), android.R.layout.simple_spinner_item,
				spinnerValues);

		((Spinner) findViewById(R.id.spinner1)).setAdapter(adapter);

		// startActivity(new Intent(MainActivity.this,
		// InsertProfileActivity.class));

		initSpinner();

		bindActivateButton();

		bindAddProfileButton();
		
		
		((Button)findViewById(R.id.settings)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			startActivity(new Intent(getBaseContext(), SettingsActivity.class));	
			}
		});

	}

	public void bindAddProfileButton() {
		Button btn = (Button) findViewById(R.id.addProfile);

		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivity(new Intent(getBaseContext(),
						InsertProfileActivity.class));

			}
		});
	}

	private void bindActivateButton() {

		Button btn = (Button) findViewById(R.id.button1);

		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				ArrayList<UserProfile> data = new DbAdapter(getBaseContext())
						.loadAllProfiles();

				ArrayList<Keyword> keywords = null;

				// find keywords by name
				for (UserProfile u : data) {
					if (u.url.equals(spinnerValue))
						keywords = u.keywords;

				}

				AsyncDownloader downloader = new AsyncDownloader(
						getBaseContext(),
						(ListView) findViewById(R.id.listView1), spinnerValue);

				if (keywords != null)
					downloader.execute(keywords);

				// load values from db

				// load textView values
				// String siteToLookFor2 = ((EditText)
				// findViewById(R.id.editText1))
				// .getText().toString();
				// String keyword = ((EditText) findViewById(R.id.editText2))
				// .getText().toString();
				//

			}
		});
	}

	public void initSpinner() {
		// find spinner selected url

		Spinner s = (Spinner) findViewById(R.id.spinner1);

		s.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Object o = arg0.getItemAtPosition(arg2);

				spinnerValue = o.toString();
				Log.w("MY", o.toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				Log.w("MY", "NOTHING SELECTED");
			}
		});
	}

	// private void doWork() {
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	//
	// long start = System.currentTimeMillis();
	//
	// String sourceText = myRequest("http://www.google.ee/search?q="
	// + URLEncoder.encode(query) + "&num=" + num);
	//
	// Log.w(TAG, "CHAR RECEIVED: " + sourceText.length());
	//
	// long downloadTime = System.currentTimeMillis() - start;
	//
	// Log.w(TAG, "DOWNLOAD TIME: " + downloadTime + "ms");
	//
	// final ArrayList<String> links = parse(sourceText);
	//
	// Log.w(TAG, "PARSE TIME: "
	// + (System.currentTimeMillis() - start - downloadTime)
	// + "ms");
	//
	// Log.w(TAG, "LINKS COUNT: " + links.size());
	//
	// runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	//
	// bindListView(links);
	//
	// }
	// });
	//
	// if (links.size() != 0)
	// for (String s : links)
	// Log.d(TAG, s);
	//
	// }
	// }).run();
	// }

	// public static ArrayList<String> parse(String sourceText) {
	//
	// ArrayList<String> allLinks = new ArrayList<String>();
	// String startTag = "<a href=";
	// String endTag = "</a>";
	//
	// int i = sourceText.indexOf("<h3");
	// // int j = sourceText.indexOf("</head>");
	// // int k = sourceText.indexOf("</head>");
	//
	// // remove beginning
	// String sourceText2 = sourceText.substring(i,
	// sourceText.lastIndexOf("</h3>"));
	//
	// Log.w(TAG, "NEW CHAR AMOUNT: " + sourceText2.length());
	//
	// while (sourceText2.contains(startTag)) {
	//
	// int startIndex = sourceText2.indexOf(startTag);
	// int endIndex = sourceText2.indexOf(endTag) + 4;
	//
	// if (startIndex < endIndex) {
	// String link = sourceText2.substring(startIndex, endIndex);
	//
	// final boolean b = !link
	// .contains("webcache.googleusercontent.com");
	// final boolean c = !link.contains("<a href=\"/search");
	// final boolean d = !link.contains("translate.google.");
	// final boolean e = !link.contains("<a href=\"/url?q=");
	// final boolean f = !link
	// .contains("<a href=\"http://docs.google.com/viewer?");
	//
	// final boolean g = !link.contains("<a href=\"/aclk?sa=");
	//
	// if (b && c && d && e && f && g)
	// allLinks.add(link);
	// }
	//
	// sourceText2 = sourceText2.substring(endIndex);
	// }
	//
	// return allLinks;
	// }

	// public String myRequest(String url) {
	//
	// BufferedReader in = null;
	// HttpClient client = new DefaultHttpClient();
	// HttpGet request = new HttpGet();
	//
	// try {
	//
	// URI uri = new URI(url);
	//
	// Log.e(TAG, uri.toString());
	//
	// request.setURI(uri);
	//
	// HttpResponse response;
	//
	// response = client.execute(request);
	// in = new BufferedReader(new InputStreamReader(response.getEntity()
	// .getContent()));
	//
	// } catch (URISyntaxException e) {
	// Log.e(TAG, e.toString());
	// } catch (ClientProtocolException e) {
	// Log.e(TAG, e.toString());
	// } catch (IOException e) {
	// Log.e(TAG, e.toString());
	// } catch (IllegalStateException e) {
	// Log.e(TAG, e.toString());
	// }
	//
	// StringBuffer sb = new StringBuffer("");
	// String line = "";
	// String NL = System.getProperty("line.separator");
	//
	// try {
	// while ((line = in.readLine()) != null)
	// sb.append(line + NL);
	//
	// in.close();
	// } catch (IOException e) {
	// Log.e(TAG, e.toString());
	// }
	//
	// return sb.toString();
	//
	// }

	// public void bindListView(final ArrayList<String> links) {
	//
	// Log.w("MY", "start bind");
	//
	// //Context con = getApplicationContext();
	//
	// Context con2 = MainActivity.this;
	//
	// ArrayAdapter<String> aa = new ArrayAdapter<String>(con2,
	// android.R.layout.simple_list_item_1, links);
	//
	// ListView lv = (ListView) findViewById(R.id.listView1);
	//
	// Log.w("MY", Boolean.toString((lv == null)));
	//
	//
	// lv.setAdapter(aa);
	// }

}