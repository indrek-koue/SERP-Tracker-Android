package com.inc.im.serptracker;

import com.inc.im.serptracker.R;
import android.app.Activity;
import android.os.Bundle;

public class ManageWebsitesActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		 setContentView(R.layout.manage_profiles_layout);

		// TODO: delete and edit here

		// ArrayList<UserProfile> data = new
		// DbAdapter(getBaseContext()).loadAllProfiles();
		//
		//
		// if (data != null){
		//
		// // ArrayAdapter<String> a = new
		// ArrayAdapter<String>(getBaseContext(),
		// android.R.layout.simple_expandable_list_item_1, );
		// //
		// // ListAdapter adapter = null;
		// //
		// //
		// ((ExpandableListView)findViewById(R.id.expandableListView1)).setAdapter(a);
		//
		// }
		//
		// ((ExpandableListView)findViewById(R.id.expandableListView1)).setOnLongClickListener(new
		// View.OnLongClickListener() {
		//
		// @Override
		// public boolean onLongClick(View v) {
		//
		// Toast.makeText(getBaseContext(), "LONG CLICK",
		// Toast.LENGTH_LONG).show();
		//
		// return false;
		// }
		// });

	}

}
