package com.takezeroapps.countit;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;

public class SettingsActivity extends AppCompatActivity {

    Switch vibrateSwitch, resetSwitch, screenSwitch, volumeSwitch;
    TextView contactSec, versionSec;
    String text1, text2;
    SpannableString span1, span2;
    CharSequence finalText;
    boolean vibrateOn, resetOn, screenOn, volumeOn; //booleans storing the status of each switch
    public static final String vibrateKey="VibrateSetting";
    public static final String resetKey="ResetSettings";
    public static final String screenKey="ScreenSetting";
    public static final String volumeKey="VolumeSetting";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_drawer_settings);
        setTitle(getString(R.string.settings));

        vibrateSwitch = (Switch) findViewById(R.id.setting_vibrate_switch);
        resetSwitch = (Switch) findViewById(R.id.settings_reset_switch);
        screenSwitch = (Switch) findViewById(R.id.settings_screen_switch);
        volumeSwitch = (Switch) findViewById(R.id.settings_volume_switch);
        contactSec = (TextView) findViewById(R.id.contact_section);
        versionSec = (TextView) findViewById(R.id.version_section);

        int textSize1 = getResources().getDimensionPixelSize(R.dimen.header_text);
        int textSize2 = getResources().getDimensionPixelSize(R.dimen.sub_text);

        //Vibration Settings text
        text1 = getString(R.string.pref_title_vibrate);
        text2 = getString(R.string.pref_description_vibrate);
        span1 = new SpannableString(text1);
        span1.setSpan(new AbsoluteSizeSpan(textSize1), 0, text1.length(), SPAN_INCLUSIVE_INCLUSIVE);
        span2 = new SpannableString(text2);
        span2.setSpan(new AbsoluteSizeSpan(textSize2), 0, text2.length(), SPAN_INCLUSIVE_INCLUSIVE);
        span2.setSpan(new ForegroundColorSpan(Color.GRAY), 0, span2.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        finalText = TextUtils.concat(span1, "\n", span2);
        vibrateSwitch.setText(finalText);

        //Reset Text
        text1 = getString(R.string.pref_title_resetconfirm);
        text2 = getString(R.string.pref_description_resetconfirm);
        span1 = new SpannableString(text1);
        span1.setSpan(new AbsoluteSizeSpan(textSize1), 0, text1.length(), SPAN_INCLUSIVE_INCLUSIVE);
        span2 = new SpannableString(text2);
        span2.setSpan(new AbsoluteSizeSpan(textSize2), 0, text2.length(), SPAN_INCLUSIVE_INCLUSIVE);
        span2.setSpan(new ForegroundColorSpan(Color.GRAY), 0, span2.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        finalText = TextUtils.concat(span1, "\n", span2);
        resetSwitch.setText(finalText);

        //Screen Text
        text1 = getString(R.string.pref_title_screen);
        text2 = getString(R.string.pref_description_screen);
        span1 = new SpannableString(text1);
        span1.setSpan(new AbsoluteSizeSpan(textSize1), 0, text1.length(), SPAN_INCLUSIVE_INCLUSIVE);
        span2 = new SpannableString(text2);
        span2.setSpan(new AbsoluteSizeSpan(textSize2), 0, text2.length(), SPAN_INCLUSIVE_INCLUSIVE);
        span2.setSpan(new ForegroundColorSpan(Color.GRAY), 0, span2.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        finalText = TextUtils.concat(span1, "\n", span2);
        screenSwitch.setText(finalText);

        //Volume Text
        text1 = getString(R.string.pref_title_volume);
        text2 = getString(R.string.pref_description_volume);
        span1 = new SpannableString(text1);
        span1.setSpan(new AbsoluteSizeSpan(textSize1), 0, text1.length(), SPAN_INCLUSIVE_INCLUSIVE);
        span2 = new SpannableString(text2);
        span2.setSpan(new AbsoluteSizeSpan(textSize2), 0, text2.length(), SPAN_INCLUSIVE_INCLUSIVE);
        span2.setSpan(new ForegroundColorSpan(Color.GRAY), 0, span2.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        finalText = TextUtils.concat(span1, "\n", span2);
        volumeSwitch.setText(finalText);

        //Contact Text
        text1 = getString(R.string.pref_title_contact);
        text2 = getString(R.string.pref_description_contact);
        span1 = new SpannableString(text1);
        span1.setSpan(new AbsoluteSizeSpan(textSize1), 0, text1.length(), SPAN_INCLUSIVE_INCLUSIVE);
        span1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, span1.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        span2 = new SpannableString(text2);
        span2.setSpan(new AbsoluteSizeSpan(textSize2), 0, text2.length(), SPAN_INCLUSIVE_INCLUSIVE);
        span2.setSpan(new ForegroundColorSpan(Color.GRAY), 0, span2.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        finalText = TextUtils.concat(span1, "\n", span2);
        contactSec.setText(finalText);

        //Version Text
        text1 = getString(R.string.pref_title_version);
        text2 = getString(R.string.pref_description_version);
        span1 = new SpannableString(text1);
        span1.setSpan(new AbsoluteSizeSpan(textSize1), 0, text1.length(), SPAN_INCLUSIVE_INCLUSIVE);
        span1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, span1.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        span2 = new SpannableString(text2);
        span2.setSpan(new AbsoluteSizeSpan(textSize2), 0, text2.length(), SPAN_INCLUSIVE_INCLUSIVE);
        span2.setSpan(new ForegroundColorSpan(Color.GRAY), 0, span2.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        finalText = TextUtils.concat(span1, "\n", span2);
        versionSec.setText(finalText);

        //Listeners for switches
        vibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                vibrateOn=isChecked;
            }
        });
        resetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                resetOn=isChecked;
            }
        });
        screenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                screenOn=isChecked;
            }
        });
        volumeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                volumeOn=isChecked;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        vibrateOn=sharedPref.getBoolean(vibrateKey, true);
        resetOn=sharedPref.getBoolean(resetKey, true);
        screenOn=sharedPref.getBoolean(screenKey, false);
        volumeOn=sharedPref.getBoolean(volumeKey, false);

        vibrateSwitch.setChecked(vibrateOn);
        resetSwitch.setChecked(resetOn);
        screenSwitch.setChecked(screenOn);
        volumeSwitch.setChecked(volumeOn);

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();

        Log.d("test", "bool:"+vibrateOn);
        Log.d("test", "bool:"+resetOn);
        Log.d("test", "bool:"+screenOn);
        Log.d("test", "bool:"+volumeOn);

        editor.putBoolean(vibrateKey, vibrateOn);
        editor.putBoolean(resetKey, resetOn);
        editor.putBoolean(screenKey, screenOn);
        editor.putBoolean(volumeKey, volumeOn);
        editor.commit();

    }
}
