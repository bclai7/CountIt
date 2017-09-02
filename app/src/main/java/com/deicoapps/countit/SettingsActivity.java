package com.deicoapps.countit;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;
import java.util.Stack;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

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
        setContentView(R.layout.activity_settings);

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
        text2 = getString(R.string.email);
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

        contactSec.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("*/*");
                        i.putExtra(Intent.EXTRA_EMAIL, new String[] {
                                getString(R.string.email)
                        });
                        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.countit_email_subject));
                        finish();
                        startActivity(createEmailOnlyChooserIntent(i, getString(R.string.send_via_email)));
                    }
                }
        );

        //Top appbar with options, do not remove
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_settings);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
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

        //highlight settings button in drawer
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_settings);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_settings);

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean(vibrateKey, vibrateOn);
        editor.putBoolean(resetKey, resetOn);
        editor.putBoolean(screenKey, screenOn);
        editor.putBoolean(volumeKey, volumeOn);
        editor.commit();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_settings);

            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else {
                finish();
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);

        //return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Go to home/main activity
            finish();
            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
        } else if (id == R.id.nav_multicounter) {
            //go to multicounter
            finish();
            startActivity(new Intent(SettingsActivity.this, CounterListActivity.class));

        } else if (id == R.id.nav_settings) {
            //go to settings

        } else if (id == R.id.nav_share) {
            //let users share app
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String link = "https://goo.gl/TKXVxf"; //app link
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_body) +"\n"+ link);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_title)));

        } else if (id == R.id.nav_rate) {
            //go to app page in google store
            AlertDialog.Builder resetDialog = new AlertDialog.Builder(SettingsActivity.this);

            // Set Dialog Title, message, and other properties
            resetDialog.setMessage(R.string.rate_question)
                    .setTitle(R.string.rate)
            ; // semi-colon only goes after ALL of the properties

            // Add the buttons
            resetDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //go to app page in google store
                    Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    // To count with Play market backstack, After pressing back button,
                    // to taken back to our application, we need to add following flags to intent.
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    try {
                        startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                    }
                }
            });
            resetDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            // Get the AlertDialog from create()
            AlertDialog dialog = resetDialog.create();

            //show dialog when reset button is clicked
            resetDialog.show();

        } else if (id == R.id.nav_contact) {
            //let users contact through email
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("*/*");
            i.putExtra(Intent.EXTRA_EMAIL, new String[] {
                    getString(R.string.email)
            });
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.countit_email_subject));
            startActivity(createEmailOnlyChooserIntent(i, getString(R.string.send_via_email)));

        }
        else if (id == R.id.nav_more) {
            //open link to developer page with the rest of my apps
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/developer?id=Deico+Apps"));
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_settings);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public Intent createEmailOnlyChooserIntent(Intent source,
                                               CharSequence chooserTitle) {
        Stack<Intent> intents = new Stack<Intent>();
        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
                "info@domain.com", null));
        List<ResolveInfo> activities = getPackageManager()
                .queryIntentActivities(i, 0);

        for(ResolveInfo ri : activities) {
            Intent target = new Intent(source);
            target.setPackage(ri.activityInfo.packageName);
            intents.add(target);
        }

        if(!intents.isEmpty()) {
            Intent chooserIntent = Intent.createChooser(intents.remove(0),
                    chooserTitle);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    intents.toArray(new Parcelable[intents.size()]));

            return chooserIntent;
        } else {
            return Intent.createChooser(source, chooserTitle);
        }
    }
}
