package com.watchmyapps.freevpn.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.watchmyapps.freevpn.R;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragmentBuilder;

public class IntroActivity extends MaterialIntroActivity {
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("firstTime", true)) {
            onFinish();
        }
        else {
            addSlide(new SlideFragmentBuilder()
                    .backgroundColor(R.color.colorPrimaryDark)
                    .buttonsColor(R.color.colorPrimary)
                    .image(R.drawable.splash1)
                    .title("Easy and Secure")
                    .description("Using FreeVPN is very easy, and it is more secure too! People wont be able to track your online activity")
                    .build());
            addSlide(new SlideFragmentBuilder()
                    .backgroundColor(R.color.colorPrimaryDark)
                    .buttonsColor(R.color.colorPrimary)
                    .image(R.drawable.splash2)
                    .title("Many Servers")
                    .description("Choose from a lot of servers across the globe. The servers are provided by many Volunteers through VPN Hub public server.")
                    .build());
            addSlide(new SlideFragmentBuilder()
                    .backgroundColor(R.color.colorPrimaryDark)
                    .buttonsColor(R.color.colorPrimary)
                    .image(R.drawable.splash3)
                    .title("Start Now")
                    .description("So, what are you waiting? Start now and unblock the sites that are blocked in your country!")
                    .build());
        } }

    @Override
    public void onFinish() {
        startActivity(new Intent(getApplicationContext(), LoadingActivity.class));
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstTime", false);
        editor.apply();
        super.onFinish();
    }
}
