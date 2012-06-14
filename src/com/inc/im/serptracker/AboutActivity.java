
package com.inc.im.serptracker;

import com.flurry.android.FlurryAgent;
import com.google.ads.AdView;
import com.inc.im.serptrackerpremium.R;
import com.inc.im.serptracker.data.access.AsyncDownloaderNews;
import com.inc.im.serptracker.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {

    private AdView adView;

    @Override
    public void onStart() {
        super.onStart();

        Button buy = (Button) findViewById(R.id.btnBuy);

        buy.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.inc.im.serptrackerpremium"
                        ));
                startActivity(intent);
            }
        });

        if (new Boolean(getString(R.string.isPremium))) {
            FlurryAgent.onStartSession(this,
                    getString(R.string.flurry_api_key_premium));

            buy.setVisibility(View.GONE);

        } else {

            FlurryAgent
                    .onStartSession(this, getString(R.string.flurry_api_key));

            buy.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    @Override
    protected void onDestroy() {

        if (adView != null)
            adView.destroy();

        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        adView = Util.loadAdmob(this);

        AsyncDownloaderNews newsDownloader = new AsyncDownloaderNews(
                ((ProgressBar) findViewById(R.id.progressBar1)),
                ((TextView) findViewById(R.id.textView1)));

        newsDownloader.execute(getString(R.string.app_news_get_path));

        bindSendEmailToDevButton();
        bindLegalButton();
        bindBackButton();

    }

    private void bindLegalButton() {
        ((Button) findViewById(R.id.btn_legal))
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        String msg = getString(R.string.copyright_c_by_thedroidproject_com_the_software_is_provided_as_is_without_warranty_of_any_kind);

                        String msg2 = AboutActivity.this
                                .getString(R.string.google_is_trademark_of_google_inc_this_application_is_built_on_google_custom_search_api_and_does_not_infringe_google_search_terms_of_service_);

                        AlertDialog.Builder builder = new Builder(
                                AboutActivity.this);

                        builder.setTitle(getString(R.string.legal_information));

                        builder.setMessage(msg2 + "\n\n" + msg);

                        builder.setCancelable(true);
                        builder.create().show();

                    }
                });
    }

    private void bindBackButton() {
        ((Button) findViewById(R.id.button4))
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        startActivity(new Intent(getBaseContext(),
                                MainActivity.class));
                    }
                });
    }

    private void bindSendEmailToDevButton() {

        OnClickListener onClick = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL,
                        new String[] {
                            getString(R.string.dev_email)
                        });
                intent.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.app_name));

                // in order to not crash with other than emails apps?
                intent.putExtra(android.content.Intent.EXTRA_TEXT, "");

                try {
                    startActivity(Intent.createChooser(intent,
                            getString(R.string.send_email_to_dev)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getBaseContext(),
                            R.string.there_are_no_email_clients_installed_,
                            Toast.LENGTH_SHORT).show();
                }

            }
        };

        ((Button) findViewById(R.id.button1)).setOnClickListener(onClick);
        // ((Button) findViewById(R.id.btnReportWrongRanking))
        // .setOnClickListener(onClick);

    }

}
