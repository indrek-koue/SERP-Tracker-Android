package com.inc.im.serptracker.util;

import com.inc.im.serptracker.data.access.AsyncDownloaderInhouseAds;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Util {

	public static void setKeywordLimit(final int limit, EditText et,
			final String messageOnLimit, final Context con) {

		// if limit == -1, then disabled
		if (limit == -1)
			return;

		et.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				// if enter is selected and on release start calculating
				if (keyCode == KeyEvent.KEYCODE_ENTER
						&& event.getAction() == KeyEvent.ACTION_UP) {

					// get EditText text
					String text = ((EditText) v).getText().toString();

					// find how many rows it cointains
					int editTextRowCount = text.split("\\n").length;

					// user has input more than limited - lets do something
					// about that
					if (editTextRowCount >= limit) {

						Toast.makeText(con, messageOnLimit, Toast.LENGTH_LONG)
								.show();

						// find the last break
						int lastBreakIndex = text.lastIndexOf("\n");

						// compose new text
						String newText = text.substring(0, lastBreakIndex);

						// add new text - delete old one and append new one
						// (append because I want the cursor to be at the end)
						((EditText) v).setText("");
						((EditText) v).append(newText);

					}

				}

				return false;
			}
		});
	}

	public static boolean internetConnectionExists(Context con) {

		ConnectivityManager cm = (ConnectivityManager) con
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm != null) {
			NetworkInfo netInfo = cm.getActiveNetworkInfo();

			if (netInfo != null && netInfo.isConnectedOrConnecting()) {
				return true;
			} else {
				return false;
			}
		} else
			return false;

	}

	public static void loadInHouseAds(LinearLayout adBox, TextView adText,
			Activity a, String sourcePath, Boolean enabled) {

		if (enabled)
			new AsyncDownloaderInhouseAds(adBox, a).execute(sourcePath);

		// ads.execute(sourcePath);

		// ads.execute(a.getString(R.string.ad_text_input_path));
	}

}
