package com.inc.im.serptracker;

import com.flurry.android.FlurryAgent;
import com.google.ads.AdView;
import com.inc.im.serptracker.R;
import com.inc.im.serptracker.util.MainActivityHelper;
import com.inc.im.serptracker.util.Util;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

	private Boolean menuBarIsVisible = true;
	private AdView adView;

//	@Override
//	public void onAttachedToWindow() {
//		super.onAttachedToWindow();
//		Window window = getWindow();
//		window.setFormat(PixelFormat.RGB_888);
//	}
//	
	@Override
	public void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, getString(R.string.flurry_api_key));
	}

	@Override
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity_layout);

		// BugSenseHandler.setup(this, "dd278c2d");

		adView = Util.loadAdmob(this);

		// init spinner + loads data form db
		MainActivityHelper.initSpinner(this);

		// bind buttons
		bindRunButton();
		MainActivityHelper.bindMenuBarButtons(this);

	}

	@Override
	protected void onDestroy() {
		if (adView != null)
			adView.destroy();
		super.onDestroy();
	}

//	@Override
//	protected void onResume() {
//		super.onResume();
//
//		// load default value on spinner
//		MainActivityHelper.initSpinner(this);
//
//		// clear listview
//		((ListView) findViewById(R.id.listview_result))
//				.setAdapter(new ArrayAdapter<String>(getBaseContext(),
//						R.layout.main_activity_listview_item));
//
//	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		//menu key hide/show menubar
		
		LinearLayout lv = (LinearLayout) findViewById(R.id.menuBar);

		if (keyCode == KeyEvent.KEYCODE_MENU)
			if (menuBarIsVisible) {
				lv.setVisibility(View.GONE);
				menuBarIsVisible = false;
			} else {
				lv.setVisibility(View.VISIBLE);
				menuBarIsVisible = true;
			}

		return super.onKeyDown(keyCode, event);
	}


	private void bindRunButton() {

		((Button) findViewById(R.id.button_run))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						MainActivityHelper.runButtonLogic(MainActivity.this);
					}
				});
	}

}