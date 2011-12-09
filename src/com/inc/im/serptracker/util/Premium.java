package com.inc.im.serptracker.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.R;
import com.inc.im.serptracker.VerifyWebView;
import com.inc.im.serptracker.adapters.DbAdapter;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.data.UserProfile;

public class Premium {

	public static void addPremiumOnClick(final Activity a,
			final int spinnerSelectedItemIndex) {

		ListView lv = (ListView) a.findViewById(R.id.listview_result);

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {

				Context con = a.getBaseContext();

				// -1 because first is default text
				final UserProfile selectedUser = new DbAdapter(con)
						.loadAllProfiles().get(spinnerSelectedItemIndex - 1);

				final Keyword k = selectedUser.keywords.get(arg2);

				// display dialog
				final CharSequence[] items = { "View",
						"ANCHOR: " + k.anchorText, "URL: " + k.url };

				AlertDialog.Builder builder = new AlertDialog.Builder(a);

				builder.setTitle("Extra info for ranking");
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						if (item == 0) {

							// first reserved for verify ranking
							try {

								String userSearchEngine = PreferenceManager
										.getDefaultSharedPreferences(a)
										.getString("prefLocalize", "Google.com");

								Toast.makeText(
										a,
										"www."
												+ userSearchEngine
												+ "/seach?q="
												+ URLEncoder.encode(k.keyword,
														"UTF-8"),
										Toast.LENGTH_LONG).show();
							} catch (UnsupportedEncodingException e) {
								Log.e("MY", e.toString());
								e.printStackTrace();
							}

							Intent i = new Intent(a, VerifyWebView.class);
							i.putExtra("keyword", k.keyword);
							i.putExtra("url", selectedUser.url);

							a.startActivity(i);
						} else if (item == 1) {

							// second reserved for show ancho
							Toast.makeText(a, k.anchorText, Toast.LENGTH_SHORT)
									.show();

						} else if (item == 2) {
							// third reserved for visit url
							a.startActivity(new Intent(Intent.ACTION_VIEW, Uri
									.parse(selectedUser.keywords.get(arg2).url)));
						}

					}
				});
				AlertDialog alert = builder.create();

				alert.show();

			}
		});
	}

	public static void showBuyPremiumDialog(String msg, final Activity a) {

		AlertDialog.Builder builder = new AlertDialog.Builder(a);
		builder.setMessage(msg)
				.setCancelable(true)
				.setPositiveButton(a.getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
								Intent intent = new Intent(Intent.ACTION_VIEW);
								intent.setData(Uri.parse(a
										.getString(R.string.market_details_id_com_inc_im_serptrackerpremium)));
								a.startActivity(intent);

							}
						})
				.setNegativeButton(a.getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								dialog.dismiss();

							}
						});
		AlertDialog alert = builder.create();
		alert.show();

	}
}
