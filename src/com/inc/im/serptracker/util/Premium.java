
package com.inc.im.serptracker.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.inc.im.serptrackerpremium.R;
import com.inc.im.serptracker.adapters.DbAdapter;
import com.inc.im.serptracker.data.Keyword;
import com.inc.im.serptracker.data.UserProfile;

/**
 * Holds functions for premium version.
 */

public class Premium {

    /**
     * Adds ranking result keyword on click premium features
     */
    public static void addPremiumOnClick(final Activity a,
            final int spinnerSelectedItemIndex) {

        ListView lv = (ListView) a.findViewById(R.id.listview_result);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                    final int arg2, long arg3) {

                Context con = a.getBaseContext();

                // -1 because first is default text
                if (new DbAdapter(con)
                        .loadAllProfiles().size() <= spinnerSelectedItemIndex - 1)
                    return;

                final UserProfile selectedUser = new DbAdapter(con)
                        .loadAllProfiles().get(spinnerSelectedItemIndex - 1);

                final Keyword k = selectedUser.keywords.get(arg2);

                // if there isn't anchor/url to show, don't open dialog
                if (k.url.equals("error getExtraUrlById")) {
                    Toast.makeText(a, R.string.no_extra_data_to_display_for_selected_keyword,
                            Toast.LENGTH_LONG).show();
                    return;

                }

                // new minimalistic look
                final CharSequence[] items = {
                        "Title: " + k.anchorText,
                        a.getString(R.string.premium_dialog_go_to_address), "Raw ranking data"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(a);

                builder.setTitle(a.getString(R.string.premium_dialog_title));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                        if (item == 0) {
                            // start custom dialog with textbox with anchor
                            // inside
                            textboxToShowAnchor(a, k.anchorText);

                        } else if (item == 1) {
                            // third reserved for visit url
                            a.startActivity(new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://www.google.com/url?q="
                                            + selectedUser.keywords.get(arg2).url)));
                        }
                        else if (item == 2) {
                            textboxToShowRawData(k.id, a);
                        }

                    }

                });
                AlertDialog alert = builder.create();

                alert.show();

            }
        });
    }

    private static void textboxToShowAnchor(Activity a,
            String anchorText) {

        Dialog dialog = new Dialog(a);

        dialog.setContentView(R.layout.premium_dialog_textbox);
        dialog.setTitle(a
                .getString(R.string.premium_show_anchor_dialog_title));

        EditText et = (EditText) dialog
                .findViewById(R.id.editText1);

        et.setText(anchorText);

        dialog.show();

    }

    private static void textboxToShowRawData(int parentId, final Activity a) {

        Dialog dialog = new Dialog(a);

        dialog.setContentView(R.layout.premium_dialog_listview);
        dialog.setTitle(R.string.raw_ranking_data_title);

        // EditText et = (EditText) dialog
        // .findViewById(R.id.editText1);

        String rawData = new DbAdapter(a).getPremiumRawData(parentId);

        // et.setText(rawData);

        final String[] rawDataList = rawData.split("\n\n");

        ListView lv = (ListView) dialog.findViewById(R.id.listView1);
        lv.setAdapter(new ArrayAdapter<String>(a, R.layout.raw_data_list_item, R.id.TextView,
                rawDataList));

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                try {
                    if (arg2 < rawDataList.length)
                        a.startActivity(new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(rawDataList[arg2])));
                } catch (Exception e) {
                    Log.e("MY", e.toString());
                }

            }
        });

        dialog.show();

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

                                Boolean isAndroidMarketInstalled = isMarketInstalled();

                                if (isAndroidMarketInstalled) {
                                    String marketlink = a
                                            .getString(R.string.market_details_id_com_inc_im_serptrackerpremium);

                                    intent.setData(Uri.parse(marketlink));

                                    a.startActivity(intent);
                                } else {
                                    Toast.makeText(
                                            a,
                                            "Android market is not found on your device. Aborting",
                                            Toast.LENGTH_LONG).show();
                                }

                            }

                            private Boolean isMarketInstalled() {

                                try {
                                    a.getPackageManager().getApplicationInfo(
                                            "com.android.vending", 0);
                                } catch (NameNotFoundException e) {
                                    return false;
                                }

                                return true;
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
