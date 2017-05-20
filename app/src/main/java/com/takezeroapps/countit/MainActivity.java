package com.takezeroapps.countit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.takezeroapps.countit.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    boolean isNegative, vibrateSetting, resetconfirmSetting, screenSetting, volumeSetting;
    int newNum;
    OperatorFragment opf = new OperatorFragment();
    Button addButton, subButton;
    ImageButton resetButton;

    @Override
    public void onResume()
    {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        vibrateSetting = prefs.getBoolean("switch_preference_vibrate", true);
        resetconfirmSetting = prefs.getBoolean("switch_preference_resetconfirm", true);
        screenSetting = prefs.getBoolean("switch_preference_screen", false);
        volumeSetting = prefs.getBoolean("switch_preference_volume", false);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        int count = sharedPref.getInt("count_key", 0);
        opf.changeCount(count);

        if(screenSetting)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        //resetButton.setImageResource(R.drawable.ic_reset_gray);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            if(volumeSetting) {
                addButton.performClick();
                return true;
            }
            else return false;
        } else if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            if(volumeSetting) {
                subButton.performClick();
                return true;
            }
            else return false;

        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        final long[] pattern = {0, 20, 150, 20}; //double vibration pattern for errors
        
        addButton = (Button) findViewById(R.id.plusButton);
        subButton = (Button) findViewById(R.id.minusButton);
        resetButton = (ImageButton) findViewById(R.id.resetButton);
        ImageButton counterButton = (ImageButton) findViewById(R.id.countButton);

        //long click on actual number count, this is to manually enter a count
        counterButton.setOnLongClickListener(
                new ImageButton.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(final View view) {
                        final int currcount = opf.getCount();

                        final CharSequence[] negOptions = {MainActivity.this.getResources().getString(R.string.make_negative_num)}; //choices to select from, only one choice so it only has one element
                        final ArrayList selectedItems=new ArrayList();

                        final AlertDialog.Builder counterChanger = new AlertDialog.Builder(MainActivity.this);
                        counterChanger.setTitle(R.string.change_count); //set title

                        // Set up the input
                        final EditText input = new EditText(MainActivity.this);

                        // Specify the type of input expected; this, for example, sets the input as a number, and will use the numpad
                        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                        counterChanger.setView(input);

                        // Set up the buttons
                        counterChanger.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String input_string = input.getText().toString().trim();

                                if(input_string.isEmpty() || input_string.length() == 0 || input_string.equals("") || TextUtils.isEmpty(input_string)) //check if input is empty
                                {
                                    Snackbar.make(view, R.string.no_input_message, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                    newNum=opf.getCount(); //set new count back to old count (or else manually setting a real number > resetting count > entering blank input = count being the original real number instead of 0 after the reset)
                                    dialog.cancel();
                                }
                                else //if string is not empty, convert to int
                                    newNum = Integer.valueOf(input.getText().toString());//get integer value of new number

                                if(newNum > 2147483646) //if entered is too high, print error and return to original number
                                {
                                    if(vibrateSetting)
                                        vib.vibrate(pattern, -1);
                                    Snackbar.make(view, R.string.too_high_message, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                    dialog.cancel();
                                }
                                else if(newNum < -2147483646) //if number entered is too low, print error and return to original number
                                {
                                    if(vibrateSetting)
                                        vib.vibrate(pattern, -1);
                                    Snackbar.make(view, R.string.too_high_message, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                    dialog.cancel();
                                }
                                else //else change number
                                {
                                    if(isNegative)
                                        opf.changeCount(-1*newNum); //if isNegative checkbox is checked, make the number negative
                                    else
                                        opf.changeCount(newNum); //if checkbox is not checked, keep number the same
                                }
                            }
                        });
                        counterChanger.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel(); //cancel dialog and do not save changes when "cancel" button is clicked
                            }
                        });
                        counterChanger.setMultiChoiceItems(negOptions, null, new DialogInterface.OnMultiChoiceClickListener() { //checkbox for negative number
                            @Override
                            public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                                if (isChecked) {
                                    isNegative=true; //if checkbox is checked, set the boolean value for negative number as true
                                }
                                else
                                    isNegative=false; //otherwise if checkbox is not checked, then keep value positive
                            }
                        });

                        counterChanger.show(); //show dialog
                        isNegative=false; //sets negative flag back to false after dialog is closed. This is so the input doesn't stay negative on each new change by the user

                        return true;
                    }
                }
        );

        //When the PLUS button is pressed, increment by 1
        addButton.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        int currAddCount=opf.getCount();
                        if((currAddCount + 1)>2147483646) //if next number would be higher than max print error
                        {
                            if(vibrateSetting)
                                vib.vibrate(pattern, -1);
                            Snackbar.make(v, R.string.max_num, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                        }
                        else //else add to count
                        {
                            if(vibrateSetting)
                                vib.vibrate(10);
                            opf.addCount();
                        }
                    }
                }
        );

        //Subtraction button
        subButton.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        int currSubCount=opf.getCount();
                        if((currSubCount - 1) < -2147483647) //if next number would be lower than min print error
                        {
                            if(vibrateSetting)
                                vib.vibrate(pattern, -1);
                            Snackbar.make(v, R.string.min_num, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                        }
                        else //else decrement
                        {
                            if(vibrateSetting)
                                vib.vibrate(10);
                            opf.subCount();
                        }
                    }
                }
        );
        resetButton.setOnClickListener(
                new ImageButton.OnClickListener(){
                    public void onClick(View v){
                        if(vibrateSetting)
                            vib.vibrate(10);
                        if(resetconfirmSetting) {
                            // Instantiate an AlertDialog.Builder with its constructor
                            AlertDialog.Builder resetDialog = new AlertDialog.Builder(MainActivity.this);

                            // Set Dialog Title, message, and other properties
                            resetDialog.setMessage(R.string.reset_question)
                                    .setTitle(R.string.reset_title)
                            ; // semi-colon only goes after ALL of the properties

                            // Add the buttons
                            resetDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // reset count if "yes" is clicked
                                    opf.resetCount();
                                }
                            });
                            resetDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //cancel dialog if "no" is clicked
                                    dialog.cancel();
                                }
                            });

                            // Get the AlertDialog from create()
                            AlertDialog dialog = resetDialog.create();

                            //show dialog when reset button is clicked
                            resetDialog.show();
                        }
                        else{
                            opf.resetCount();
                        }
                    }
                }
        );

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        int c = opf.getCount();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("count_key", c);
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.action_settings) {
            Log.d("test", "Setting dots Pressed");
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Go to home/main activity

        } else if (id == R.id.nav_multicounter) {
            //go to multicounter
            startActivity(new Intent(MainActivity.this, CounterListActivity.class));

        } else if (id == R.id.nav_settings) {
            //go to settings
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));

        } else if (id == R.id.nav_share) {
            //let users share app

        } else if (id == R.id.nav_rate) {
            //go to app page in google store

        } else if (id == R.id.nav_contact) {
            //let users contact through email

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
