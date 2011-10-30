package com.inc.im.serptracker.util;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.AboutActivity;
import com.inc.im.serptracker.InsertWebsiteActivity;
import com.inc.im.serptracker.ManageWebsitesActivity;
import com.inc.im.serptracker.PreferencesActivity;
import com.inc.im.serptracker.R;
import com.inc.im.serptracker.adapters.DbAdapter;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.data.UserProfile;

public class MainActivityHelper {

//	public static ArrayList<String> bindResultListView(Activity a,
//			ListView listView, ArrayList<Keyword> keywords) {
//
//		ArrayList<String> keywordsToBind = new ArrayList<String>();
//
//		if (keywords != null) {
//
//			for (Keyword k : keywords) {
//
//				// 0 - empty field in DB - new
//				// -1 - not ranked in top 100
//				// -2 - error getting data
//
//				if ((k.newRank == -1 && k.oldRank == 0)
//						|| (k.newRank == 0 && k.oldRank == -1)
//						|| k.newRank == -1) {
//					// not ranked
//					keywordsToBind.add(k.keyword + " ["
//							+ a.getString(R.string.not_ranked) + "]");
//				} else if (k.newRank == -2) {
//					// error
//					keywordsToBind.add(k.keyword + " ["
//							+ a.getString(R.string.error_please_try_again)
//							+ "]");
//				} else if (k.newRank == 0 && k.oldRank == 0) {
//					// new keyword
//					keywordsToBind.add(k.keyword);
//				} else {
//
//					// keyword has a rank - is it new or old?
//					int valueChange = k.oldRank - k.newRank;
//					String ready = "";
//
//					if (valueChange != 0 && k.newRank != 0) {
//
//						// keyword first run
//						if (k.oldRank == 0) {
//							ready = String.format("%s [ %d ]", k.keyword,
//									k.newRank);
//						} else {
//							// show with value change
//							String sign = valueChange > 0 ? "+" : "";
//							ready = String.format("%s [ %d ] %s%d", k.keyword,
//									k.newRank, sign, valueChange);
//						}
//
//					} else {
//						// value change is 0 - no point to show it with +/-
//						ready = String.format("%s [ %d ]", k.keyword, k.oldRank);
//					}
//
//					keywordsToBind.add(ready);
//					// keywordsToBind.add(k.value + " [" + k.rank + "] ");
//				}
//			}
//
//			listView.setAdapter(new ArrayAdapter<String>(a,
//					R.layout.main_activity_listview_item, R.id.textView1,
//					keywordsToBind));
//
//		}
//
//		return keywordsToBind;
//
//	}

	public static ArrayList<Keyword> findKeywordsByName(
			ArrayList<UserProfile> data, int spinnerSelectedValueNr) {
		ArrayList<Keyword> keywords = null;

		for (UserProfile u : data) {
			if (data.get(spinnerSelectedValueNr) != null)
				if (u.url.equals(data.get(spinnerSelectedValueNr).url)
						&& u.id == data.get(spinnerSelectedValueNr).id)
					keywords = u.keywords;
		}
		return keywords;
	}

	public static void bindMenuBarButtons(final Activity a) {

		final Context con = a.getBaseContext();

		// insert website
		View.OnClickListener insertProfileListener = new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				a.startActivity(new Intent(con, InsertWebsiteActivity.class));

			}
		};

		((LinearLayout) a.findViewById(R.id.linearLayoutAddProfile))
				.setOnClickListener(insertProfileListener);
		((ImageButton) a.findViewById(R.id.imageButton1))
				.setOnClickListener(insertProfileListener);
		((TextView) a.findViewById(R.id.textView1))
				.setOnClickListener(insertProfileListener);

		// manage websites
		View.OnClickListener manageProfilesListener = new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (new DbAdapter(con).loadAllProfiles() == null) {
					Toast.makeText(
							con,
							a.getString(R.string.no_websites_to_manage_please_add_a_website),
							Toast.LENGTH_SHORT).show();
				} else {
					a.startActivity(new Intent(con,
							ManageWebsitesActivity.class));
				}

			}
		};

		((LinearLayout) a.findViewById(R.id.linearLayoutManageProfiles))
				.setOnClickListener(manageProfilesListener);
		((ImageButton) a.findViewById(R.id.imageButton2))
				.setOnClickListener(manageProfilesListener);
		((TextView) a.findViewById(R.id.textView2))
				.setOnClickListener(manageProfilesListener);

		// settings
		View.OnClickListener settingsListener = new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				a.startActivity(new Intent(con, PreferencesActivity.class));

			}
		};

		((LinearLayout) a.findViewById(R.id.linearLayoutSettings))
				.setOnClickListener(settingsListener);
		((ImageButton) a.findViewById(R.id.imageButton3))
				.setOnClickListener(settingsListener);
		((TextView) a.findViewById(R.id.textView3))
				.setOnClickListener(settingsListener);

		// about
		View.OnClickListener aboutListener = new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				a.startActivity(new Intent(con, AboutActivity.class));

			}
		};

		((LinearLayout) a.findViewById(R.id.linearLayoutAbout))
				.setOnClickListener(aboutListener);
		((ImageButton) a.findViewById(R.id.imageButton4))
				.setOnClickListener(aboutListener);
		((TextView) a.findViewById(R.id.textView4))
				.setOnClickListener(aboutListener);

	}

	public static Boolean rateUsDialog(final Activity a, int firstCap,
			int secondCap) {

		// read
		SharedPreferences settings = a.getSharedPreferences("minuPref", 0);
		int loadedCountFromPref = settings.getInt("minuMuutuja", 0);
		int newNumber = loadedCountFromPref + 1;

		Log.d("MY", loadedCountFromPref + "");
		// write
		SharedPreferences settings2 = a.getSharedPreferences("minuPref", 0);
		SharedPreferences.Editor editor = settings2.edit();
		editor.putInt("minuMuutuja", newNumber);
		editor.commit();

		Boolean displayed = false;
		if (loadedCountFromPref == firstCap || loadedCountFromPref == secondCap) {
			displayed = true;
			AlertDialog.Builder builder = new AlertDialog.Builder(a);
			builder.setMessage(
					a.getString(R.string.like_this_app_rate_us_on_android_market_))
					.setCancelable(false)
					.setPositiveButton(a.getString(R.string.sure_),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
									Intent intent = new Intent(
											Intent.ACTION_VIEW);
									intent.setData(Uri.parse(a
											.getString(R.string.market_details_id_com_inc_im_serptracker)));
									a.startActivity(intent);

									FlurryAgent.onEvent("rate us dialog: YES");

								}
							})
					.setNegativeButton(a.getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									dialog.dismiss();

									FlurryAgent.onEvent("rate us dialog: NO");

								}
							});
			AlertDialog alert = builder.create();
			alert.show();

		}

		return displayed;
	}

}
