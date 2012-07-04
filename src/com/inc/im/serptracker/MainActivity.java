
package com.inc.im.serptracker;

/* Switching between premium - free
 * 
 * Rename package
 * change config: name and isPremium
 * 
 */

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bugsense.trace.BugSenseHandler;
import com.flurry.android.FlurryAgent;
import com.google.ads.AdView;
import com.inc.im.serptracker.util.MainActivityHelper;
import com.inc.im.serptracker.util.Util;
import com.inc.im.serptracker.R;

public class MainActivity extends Activity {

    private Boolean menuBarIsVisible = true;
    private AdView adView;

    @Override
    public void onStart() {
        super.onStart();

        if (new Boolean(getString(R.string.isPremium)))
            FlurryAgent.onStartSession(this,
                    getString(R.string.flurry_api_key_premium));
        else
            FlurryAgent
                    .onStartSession(this, getString(R.string.flurry_api_key));
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

        BugSenseHandler.setup(this, "dd278c2d");

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

    // @Override
    // protected void onResume() {
    // super.onResume();
    //
    // // load default value on spinner
    // MainActivityHelper.initSpinner(this);
    //
    // // clear listview
    // ((ListView) findViewById(R.id.listview_result))
    // .setAdapter(new ArrayAdapter<String>(getBaseContext(),
    // R.layout.main_activity_listview_item));
    //
    // }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // menu key hide/show menubar

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
