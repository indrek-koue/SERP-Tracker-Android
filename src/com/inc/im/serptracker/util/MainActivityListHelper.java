package com.inc.im.serptracker.util;

import java.util.ArrayList;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.inc.im.serptracker.R;
import com.inc.im.serptracker.adapters.MainActivityListAdapter;
import com.inc.im.serptracker.data.UserProfile;

public class MainActivityListHelper {

	public static void bindListViewItems(Activity a,
			ArrayList<UserProfile> data, int spinnerSelectedItemIndex) {

		ListView lv = (ListView) a.findViewById(R.id.listview_result);

		if (spinnerSelectedItemIndex > 0 && data != null
				&& spinnerSelectedItemIndex <= data.size()) {

			// -1 because 0 index holds default text
			final UserProfile selectedUser = data
					.get(spinnerSelectedItemIndex - 1);

			lv.setAdapter(new MainActivityListAdapter(a.getBaseContext(),
					selectedUser.keywords));

			if (Boolean.parseBoolean(a.getString(R.string.isPremium)))
				Premium.addPremiumOnClick(a, spinnerSelectedItemIndex);

		} else {

			// clear
			lv.setAdapter(new ArrayAdapter<String>(a,
					R.layout.main_activity_listview_item));
		}

	}

}
