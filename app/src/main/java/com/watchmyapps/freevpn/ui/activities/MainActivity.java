package com.watchmyapps.freevpn.ui.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAdListener;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.watchmyapps.freevpn.R;
import com.watchmyapps.freevpn.localdata.model.Server;
import com.watchmyapps.freevpn.ui.adapters.CountryListAdapter;
import com.watchmyapps.freevpn.utils.PropertiesService;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    public static final String EXTRA_COUNTRY = "country";
    public static final String EXTRA_COUNTRY_CODE = "country_code";
    DrawerLayout drawer_layout;
    ImageView shieldImage, drawerMenu;
    TextView statusText, secureText, serverWarning;
    private PopupWindow popupWindow;
    private RelativeLayout homeContextRL;
    private List<Server> countryList;
    // Using deprecated methods makes you look way cool
    SharedPreferences prefs;

    private com.facebook.ads.AdView mAdView;
    private com.facebook.ads.InterstitialAd interstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("firstTime2", true)) {
            startTapTargetView();
        }
     /*
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        AdView mAdView = findViewById(R.id.adVBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
*/

        AudienceNetworkAds.initialize(this);
        mAdView = new com.facebook.ads.AdView(this, "578194492683763_620688755101003", AdSize.BANNER_HEIGHT_50);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);
        adContainer.addView(mAdView);
        //AdSettings.addTestDevice("bbb4aa7e-e2c9-42b5-ad7f-8f00a414b74d");
        mAdView.loadAd();


/*
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.admob_intersitial_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
*/

        interstitialAd = new com.facebook.ads.InterstitialAd(this, "578194492683763_621699521666593");
        interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {

            }

            @Override
            public void onInterstitialDismissed(Ad ad) {

                interstitialAd.loadAd();

            }

            @Override
            public void onError(Ad ad, AdError adError) {

            }

            @Override
            public void onAdLoaded(Ad ad) {

            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });
        interstitialAd.loadAd();



        homeContextRL = findViewById(R.id.homeContextRL);
        countryList = dataBaseHelper.getCountriesByServers();

        shieldImage = findViewById(R.id.shieldImage);
        drawer_layout = findViewById(R.id.drawer_layout);
        drawerMenu = findViewById(R.id.drawerMenu);
        statusText = findViewById(R.id.statusText);
        secureText = findViewById(R.id.secureText);
        serverWarning = findViewById(R.id.serverWarning);
        serverWarning.setText(R.string.note_text);

        drawerMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer_layout.openDrawer(Gravity.START, true);
            }
        });

        if (connectedServer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                shieldImage.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_shield));
            }
            statusText.setText(getString(R.string.state_connected));
            secureText.setText(R.string.you_secure);
            secureText.setTextColor(Color.parseColor("#00DD80"));
            statusText.setTextColor(Color.parseColor("#00DD80"));
            Button button = findViewById(R.id.homeBtnRandomConnection);
            button.setText(getString(R.string.server_btn_disconnect));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                button.setBackground(this.getDrawable(R.drawable.disconnect_button_background));
            }
        }
        if (connectedServer == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                shieldImage.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_shield_red));
            }
            statusText.setText(getString(R.string.state_disconnected));
            secureText.setText(R.string.not_secure);
            secureText.setTextColor(getResources().getColor(R.color.lightRed));
            statusText.setTextColor(getResources().getColor(R.color.lightRed));
            Button button = findViewById(R.id.homeBtnRandomConnection);
            button.setText(R.string.optimal_connect);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                button.setBackground(this.getDrawable(R.drawable.connect_button_background));
            }
        }

        final NavigationView navigationView = findViewById(R.id.nav_view);
        final String appPackageName = getPackageName();
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case android.R.id.home:
                                onBackPressed();
                                return true;
                            case R.id.actionRefresh:
                                startActivity(new Intent(getApplicationContext(), LoadingActivity.class));
                                finish();
                                return true;

                            case R.id.actionCurrentServer:
                                if (connectedServer != null) {
                                    startActivity(new Intent(MainActivity.this, ServerActivity.class));
                                } else {
                                    Toast.makeText(MainActivity.this, "No Current Connection", Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            case R.id.actionSpeedTest:
//                                startActivity(new Intent(MainActivity.this, SpeedTestActivity.class));
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=www.speedcheck.com.pk.androidapp")));
                                } catch (ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=www.speedcheck.com.pk.androidapp" )));
                                }
                                return true;

                            case R.id.action_settings:
                                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                                return true;

                            case R.id.actionRate:
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                                return true;

                            case R.id.actionLike:
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                                return true;

                            case R.id.actionAbout:
                                startAboutDialog("About Us", getString(R.string.aboutApp));
                                drawer_layout.closeDrawer(Gravity.START);
                                return true;

                        }
                        drawer_layout.closeDrawer(Gravity.START);
                        return true;
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (connectedServer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                shieldImage.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_shield));
            }
            statusText.setText(getString(R.string.state_connected));
            secureText.setText(R.string.secure);
            secureText.setTextColor(Color.parseColor("#00DD80"));
            statusText.setTextColor(Color.parseColor("#00DD80"));
            Button button = findViewById(R.id.homeBtnRandomConnection);
            button.setText(getString(R.string.server_btn_disconnect));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                button.setBackground(this.getDrawable(R.drawable.disconnect_button_background));
            }
        }
        if (connectedServer == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                shieldImage.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_shield_red));
            }
            statusText.setText(getString(R.string.state_disconnected));
            secureText.setText(R.string.not_secure);
            secureText.setTextColor(getResources().getColor(R.color.lightRed));
            statusText.setTextColor(getResources().getColor(R.color.lightRed));
            Button button = findViewById(R.id.homeBtnRandomConnection);
            button.setText(R.string.optimal_connect);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                button.setBackground(this.getDrawable(R.drawable.connect_button_background));
            }
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    public void homeOnClick(View view) {
        if(interstitialAd.isAdLoaded()) {
            interstitialAd.show();
        }

        switch (view.getId()) {
            case R.id.homeBtnChooseCountry:
                chooseCountry();
                break;
            case R.id.homeBtnRandomConnection:
                if (connectedServer != null) {
                    startActivity(new Intent(MainActivity.this, ServerActivity.class));
                } else {

                    Server randomServer = getRandomServer();
                    if (randomServer != null) {
                        newConnecting(randomServer, true, true);
                    } else {
                        String randomError = String.format(getResources().getString(R.string.error_random_country), PropertiesService.getSelectedCountry());
                        Toast.makeText(this, randomError, Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }

    }

    private void chooseCountry() {
        View view = initPopUp();

        final List<String> countryListName = new ArrayList<String>();
        final List<String> countryListCode = new ArrayList<String>();
        for (Server server : countryList) {
            String localeCountryName = localeCountries.get(server.getCountryShort()) != null ?
                    localeCountries.get(server.getCountryShort()) : server.getCountryLong();
            String localeCountryCode = server.getCountryShort();
            countryListName.add(localeCountryName);
            countryListCode.add(localeCountryCode);
        }

        ListView lvCountry = view.findViewById(R.id.homeCountryList);
        CountryListAdapter countryListAdapter = new CountryListAdapter(getApplicationContext(), countryListName, countryListCode);
        lvCountry.setAdapter(countryListAdapter);
        lvCountry.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                popupWindow.dismiss();
                onSelectCountry(countryList.get(position));
            }
        });

        popupWindow.showAtLocation(homeContextRL, Gravity.CENTER, 0, 0);
//        interstitialAd.show();
    }

    private View initPopUp() {

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view = inflater.inflate(R.layout.pop_up_choose_country, null);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            popupWindow = new PopupWindow(view, widthWindow, heightWindow);
        } else {
            popupWindow = new PopupWindow(view, widthWindow, heightWindow);
        }
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        return view;
    }

    private void onSelectCountry(Server server) {

        //-----------------ME----------------
        String countryCode = server.getCountryShort();
        final List<Server> serverList = dataBaseHelper.getServersFromCountry(countryCode);

        if (serverList.size() > 0) {

            Intent intent = new Intent(this, ServerActivity.class);
            intent.putExtra(Server.class.getCanonicalName(), server);
            startActivity(intent);

        } else {
            Toast.makeText(this, "No server available from this country", Toast.LENGTH_SHORT).show();
        }

        //----------------------------------------
//        Intent intent = new Intent(getApplicationContext(), ServersListActivity.class);
//        intent.putExtra(EXTRA_COUNTRY, server.getCountryLong());
//        intent.putExtra(EXTRA_COUNTRY_CODE, server.getCountryShort());
//        startActivity(intent);
    }

    private void startAboutDialog(String title, String message) {
        final LovelyStandardDialog lovelyStandardDialog = new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.VERTICAL)
                .setTopColorRes(R.color.lightRed)
                .setButtonsColorRes(R.color.colorAccent)
                .setIcon(R.drawable.ic_info_outline_black_24dp)
                .setTitle(title)
                .setMessage(message);
        lovelyStandardDialog
                .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lovelyStandardDialog.dismiss();
                    }
                });
        lovelyStandardDialog.show();
    }

    private void startTapTargetView() {
        final TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(findViewById(R.id.homeBtnChooseCountry), "Click to choose country", "Select your desired country from the list and select any server"),
                        TapTarget.forView(findViewById(R.id.homeBtnRandomConnection), "Click for best connection", "Clicking this button will connect to random best servers available")
                                .textColor(android.R.color.white),
                        TapTarget.forView(findViewById(R.id.drawerMenu), "Click here for more options", "More options include settings speedtest and others"))
                .listener(new TapTargetSequence.Listener() {

                    @Override
                    public void onSequenceFinish() {
                        // Yay
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {

                    }
                });
        sequence.start();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstTime2", false);
        editor.apply();
    }
}
