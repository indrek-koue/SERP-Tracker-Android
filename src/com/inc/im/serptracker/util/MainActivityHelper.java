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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptracker.AboutActivity;
import com.inc.im.serptracker.InsertWebsiteActivity;
import com.inc.im.serptracker.MainActivity;
import com.inc.im.serptracker.ManageWebsitesActivity;
import com.inc.im.serptracker.PreferencesActivity;
import com.inc.im.serptracker.R;
import com.inc.im.serptracker.adapters.DbAdapter;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.data.UserProfile;
import com.inc.im.serptracker.data.access.AsyncDownloader;

public class MainActivityHelper {

	// public static ArrayList<String> bindResultListView(Activity a,
	// ListView listView, ArrayList<Keyword> keywords) {
	//
	// ArrayList<String> keywordsToBind = new ArrayList<String>();
	//
	// if (keywords != null) {
	//
	// for (Keyword k : keywords) {
	//
	// // 0 - empty field in DB - new
	// // -1 - not ranked in top 100
	// // -2 - error getting data
	//
	// if ((k.newRank == -1 && k.oldRank == 0)
	// || (k.newRank == 0 && k.oldRank == -1)
	// || k.newRank == -1) {
	// // not ranked
	// keywordsToBind.add(k.keyword + " ["
	// + a.getString(R.string.not_ranked) + "]");
	// } else if (k.newRank == -2) {
	// // error
	// keywordsToBind.add(k.keyword + " ["
	// + a.getString(R.string.error_please_try_again)
	// + "]");
	// } else if (k.newRank == 0 && k.oldRank == 0) {
	// // new keyword
	// keywordsToBind.add(k.keyword);
	// } else {
	//
	// // keyword has a rank - is it new or old?
	// int valueChange = k.oldRank - k.newRank;
	// String ready = "";
	//
	// if (valueChange != 0 && k.newRank != 0) {
	//
	// // keyword first run
	// if (k.oldRank == 0) {
	// ready = String.format("%s [ %d ]", k.keyword,
	// k.newRank);
	// } else {
	// // show with value change
	// String sign = valueChange > 0 ? "+" : "";
	// ready = String.format("%s [ %d ] %s%d", k.keyword,
	// k.newRank, sign, valueChange);
	// }
	//
	// } else {
	// // value change is 0 - no point to show it with +/-
	// ready = String.format("%s [ %d ]", k.keyword, k.oldRank);
	// }
	//
	// keywordsToBind.add(ready);
	// // keywordsToBind.add(k.value + " [" + k.rank + "] ");
	// }
	// }
	//
	// listView.setAdapter(new ArrayAdapter<String>(a,
	// R.layout.main_activity_listview_item, R.id.textView1,
	// keywordsToBind));
	//
	// }
	//
	// return keywordsToBind;
	//
	// }

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

	public static String getSpinnerSelectedValue(Activity a) {

		Object o = ((Spinner) a.findViewById(R.id.spinner_profile))
				.getSelectedItem();
		if (o != null)
			return o.toString();
		else
			return "";

	}

	public static int getSpinnerSelectedIndex(Activity a) {

		return ((Spinner) a.findViewById(R.id.spinner_profile))
				.getSelectedItemPosition();

	}

	public static void initSpinner(final Activity a) {

		final ArrayList<UserProfile> data = new DbAdapter(a).loadAllProfiles();
		Spinner spinner = (Spinner) a.findViewById(R.id.spinner_profile);

		ArrayList<String> spinnerValues = new ArrayList<String>();
		spinnerValues.add(a.getString(R.string.spinner_default_selection));

		// generate spinner values
		if (data != null)
			for (UserProfile u : data)
				spinnerValues.add(u.url + "[" + u.keywords.size() + "]");

		spinner.setAdapter(new ArrayAdapter<String>(a.getBaseContext(),
				R.layout.main_activity_spinner_item, R.id.textView1,
				spinnerValues));

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapter, View arg1,
					int index, long arg3) {

				MainActivityListHelper.bindListViewItems(a, data, index);

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
	}

	@SuppressWarnings("unchecked")
	public static void runButtonLogic(Activity a) {
		if (!Util.internetConnectionExists(a)) {
			Toast.makeText(
					a,
					a.getString(R.string.no_active_internet_connection_please_check_your_settings),
					Toast.LENGTH_LONG).show();

			return;
		}

		// default not selected
		if (!MainActivityHelper.getSpinnerSelectedValue(a).equals(
				a.getString(R.string.spinner_default_selection))) {

			ArrayList<UserProfile> data = new DbAdapter(a).loadAllProfiles();

			// counter for rate-us-dialog
			if (MainActivityHelper.rateUsDialog(a, 15, 50))
				return;

			int spinnerSelectedValueNr = MainActivityHelper
					.getSpinnerSelectedIndex(a) - 1;

			if (data == null || spinnerSelectedValueNr >= data.size()
					|| spinnerSelectedValueNr < 0)
				return;

			// find keywords by name
			ArrayList<Keyword> keywords = MainActivityHelper
					.findKeywordsByName(data, spinnerSelectedValueNr);

			if (keywords != null) {

				// start download
				new AsyncDownloader(a,
						(ListView) a.findViewById(R.id.listview_result),
						data.get(spinnerSelectedValueNr).url).execute(keywords);
			}

			else {
				Toast.makeText(
						a,
						a.getString(R.string.profile_keywords_are_missing_how_s_thats_possible_),
						Toast.LENGTH_SHORT).show();
			}

		} else {

			Toast.makeText(a, a.getString(R.string.please_select_a_profile),
					Toast.LENGTH_SHORT).show();

		} // if empty selected
	}

}
