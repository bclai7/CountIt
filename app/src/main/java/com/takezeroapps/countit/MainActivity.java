package com.takezeroapps.countit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.io.File;
import java.util.ArrayList;

import Exceptions.NoCountEnteredException;

import static android.graphics.Color.BLACK;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    boolean isNegative, vibrateSetting, resetconfirmSetting, screenSetting, volumeSetting;
    int newNum;
    OperatorFragment opf = new OperatorFragment();
    Button addButton, subButton;
    ImageButton resetButton, counterButton;
    public static boolean portraitMode=true;
    int count;
    View counterChangeView;
    EditText input, incdecInput;
    ShowcaseView tut;
    boolean tutorialComplete; //boolean storing whether or not user has completed the tutorial/tip for this particular activity
    String incdecQ;

    @Override
    public void onResume()
    {
        //get saved settings from stored preferences
        super.onResume();

        //get settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        vibrateSetting = sharedPref.getBoolean(SettingsActivity.vibrateKey, true);
        resetconfirmSetting = sharedPref.getBoolean(SettingsActivity.resetKey, true);
        screenSetting = sharedPref.getBoolean(SettingsActivity.screenKey, false);
        volumeSetting = sharedPref.getBoolean(SettingsActivity.volumeKey, false);

        if(screenSetting)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        portraitMode=true;

        //create sharedpref for counter view if it doesn't already exist
        File f = new File("/data/data/com.takezeroapps.countit/shared_prefs/CounterView.xml");
        if(!f.exists())
        {
            SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("CounterView", 0);
            editor.commit();
        }

        SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sp.edit();
        editor1.putBoolean("orientation_key", portraitMode);
        editor1.commit();


        // Check for the rotation
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            portraitMode=false;
            opf.changeCount(count, portraitMode);

            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("orientation_key", portraitMode);
            editor.commit();

            finish();
            startActivity(getIntent());
        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
            portraitMode=true;
            opf.changeCount(count, portraitMode);

            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("orientation_key", portraitMode);
            editor.commit();

            finish();
            startActivity(getIntent());
        }
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

        setTitle("");

        final String incdecOptions[] = {MainActivity.this.getResources().getString(R.string.increase), MainActivity.this.getResources().getString(R.string.decrease)};

        //get tutorial sharedpref
        SharedPreferences sharedPrefA = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        tutorialComplete=sharedPrefA.getBoolean("MainTutorial", false);
        Log.d("test", "OnCreate bool: "+tutorialComplete);

        //fixes issue where loading into landscape uses the wrong font size
        Configuration newConfig = getResources().getConfiguration();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
            portraitMode=false;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
            portraitMode=true;
        }

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        count = sharedPref.getInt("count_key", 0);

        opf.changeCount(count, portraitMode);

        final Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        final long[] pattern = {0, 20, 150, 20}; //double vibration pattern for errors

        addButton = (Button) findViewById(R.id.plusButton);
        subButton = (Button) findViewById(R.id.minusButton);
        resetButton = (ImageButton) findViewById(R.id.resetButton);
        counterButton = (ImageButton) findViewById(R.id.countButton);

        //long click on actual number count, this is to manually enter a count
        counterButton.setOnLongClickListener(
                new ImageButton.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(final View view) {

                        //create counterlist_dropdown_menu dialog
                        String names[] ={
                                getResources().getString(R.string.change_count),
                                getResources().getString(R.string.inc_dec_by),
                        };
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                        View convertView = (View) inflater.inflate(R.layout.options_popup_list, null);
                        alertDialog.setView(convertView);
                        alertDialog.setTitle(R.string.counter_options_title);
                        ListView lv = (ListView) convertView.findViewById(R.id.listView1);
                        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,names);
                        lv.setAdapter(adapter1);
                        final AlertDialog alert = alertDialog.create();
                        alert.show();

                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View viewIn, int position, long id) {
                                if(position == 0) //change count
                                {
                                    alert.dismiss();
                                    counterChangeView =view;
                                    final int currcount = opf.getCount();

                                    final CharSequence[] negOptions = {MainActivity.this.getResources().getString(R.string.make_negative_num)}; //choices to select from, only one choice so it only has one element
                                    final ArrayList selectedItems=new ArrayList();

                                    final AlertDialog.Builder counterChanger = new AlertDialog.Builder(MainActivity.this);
                                    counterChanger.setTitle(R.string.change_count); //set title

                                    // Set up the input
                                    input = new EditText(MainActivity.this);

                                    // Specify the type of input expected; this, for example, sets the input as a number, and will use the numpad
                                    input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                                    input.setHint(R.string.enter_new_count);
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(9)});
                                    counterChanger.setView(input);

                                    // Set up the buttons
                                    counterChanger.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                String input_string = input.getText().toString().trim();

                                                if (input_string.isEmpty() || input_string.length() == 0 || input_string.equals("") || TextUtils.isEmpty(input_string)) //check if input is empty
                                                {
                                                    throw new NoCountEnteredException();
                                                } else //if string is not empty, convert to int
                                                    newNum = Integer.valueOf(input.getText().toString());//get integer value of new number

                                                {
                                                    if (isNegative) {
                                                        opf.changeCount(-1 * newNum, portraitMode); //if isNegative checkbox is checked, make the number negative
                                                        count = newNum * -1;
                                                    } else {
                                                        opf.changeCount(newNum, portraitMode); //if checkbox is not checked, keep number the same
                                                        count = newNum;
                                                    }
                                                }

                                                //removes keyboard from screen when user clicks ok so it is not stuck on the screen
                                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                            }
                                            catch (NoCountEnteredException e1)
                                            {
                                                if (vibrateSetting)
                                                    vib.vibrate(pattern, -1);
                                                Snackbar.make(view, R.string.no_input_message, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                                newNum = opf.getCount(); //set new count back to old count (or else manually setting a real number > resetting count > entering blank input = count being the original real number instead of 0 after the reset)
                                                dialog.cancel();

                                                //removes keyboard from screen when user clicks ok so it is not stuck on the screen
                                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                            }
                                            catch(Exception e2)
                                            {
                                                //checking if the number is higher than maximum is no longer needed because the program will throw an exception if its too high anyway, this is where it is caught
                                                if (vibrateSetting)
                                                    vib.vibrate(pattern, -1);
                                                Snackbar.make(view, R.string.invalid_number_entered, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                                dialog.cancel();

                                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

                                            }
                                        }
                                    });
                                    counterChanger.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel(); //cancel dialog and do not save changes when "cancel" button is clicked

                                            //removes keyboard from screen when user clicks cancel so it is not stuck on the screen
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
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

                                    counterChanger.setOnCancelListener(new DialogInterface.OnCancelListener() {

                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            //removes keyboard from screen when user clicks outside of dialog box so it is not stuck on the screen
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

                                        }
                                    });


                                    counterChanger.show(); //show dialog
                                    input.requestFocus();
                                    InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                                    isNegative=false; //sets negative flag back to false after dialog is closed. This is so the input doesn't stay negative on each new change by the user
                                }
                                else if(position==1) //increase/decrease by
                                {

                                    try {
                                        alert.dismiss();
                                        incdecQ = MainActivity.this.getResources().getString(R.string.increase_by);
                                        //create dialog
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle(R.string.inc_dec_by);
                                        Context context = MainActivity.this; //store context in a variable
                                        LinearLayout layout = new LinearLayout(context);
                                        layout.setOrientation(LinearLayout.VERTICAL);

                                        //textview telling user to select "increase or decrease"
                                        final TextView incdecText = new TextView(context);
                                        incdecText.setText(R.string.select);
                                        incdecText.setTextSize(16);
                                        incdecText.setTextColor(BLACK);
                                        layout.addView(incdecText);

                                        //dropdown menu with increase/decrease
                                        //dropdown for initial number of counters
                                        final RadioGroup rg = new RadioGroup(MainActivity.this);
                                        rg.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                        for(int p=0; p<2; p++)
                                        {
                                            RadioButton rb = new RadioButton(MainActivity.this);
                                            rb.setText(incdecOptions[p]);
                                            rb.setTextSize(16);
                                            rb.setId(p);
                                            rg.addView(rb);
                                        }

                                        layout.addView(rg);

                                        //textview to create a space in between fields
                                        final TextView space = new TextView(context);
                                        space.setText("");
                                        space.setTextSize(16);
                                        layout.addView(space);

                                        //textview telling user to enter amount
                                        final TextView amount = new TextView(context);
                                        amount.setText(incdecQ);
                                        amount.setTextSize(16);
                                        amount.setTextColor(BLACK);
                                        layout.addView(amount);

                                        //Text input for inc/dec amount
                                        incdecInput = new EditText(context);
                                        incdecInput.setHint(R.string.amount);
                                        incdecInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
                                        incdecInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                                        layout.addView(incdecInput);

                                        layout.setPadding(60, 50, 60, 30);
                                        builder.setView(layout);

                                        builder.create();

                                        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
                                        {
                                            @Override
                                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                                // checkedId is the RadioButton selected
                                                View radioButton = rg.findViewById(checkedId);
                                                int index = rg.indexOfChild(radioButton);

                                                if(rg.getCheckedRadioButtonId() == 0)
                                                {
                                                    incdecQ = MainActivity.this.getResources().getString(R.string.increase_by);
                                                }
                                                else if(rg.getCheckedRadioButtonId() == 1)
                                                {
                                                    incdecQ = MainActivity.this.getResources().getString(R.string.decrease_by);
                                                }
                                                amount.setText(incdecQ);
                                            }
                                        });

                                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                int incdecAmount = 0;
                                                try {
                                                    //increase/decrease by required amount
                                                    String input_string = incdecText.getText().toString().trim();

                                                    if (input_string.isEmpty() || input_string.length() == 0 || input_string.equals("") || TextUtils.isEmpty(input_string)) //check if input is empty
                                                    {
                                                        throw new NoCountEnteredException();
                                                    }
                                                    else //if string is not empty, convert to int
                                                    {
                                                        incdecAmount = Integer.parseInt(incdecInput.getText().toString()); //the amount to be decrement/incremented by
                                                    }

                                                    if (rg.getCheckedRadioButtonId() == 0) //increase
                                                    {
                                                        //INCREASE by
                                                        int currCount=opf.getCount();
                                                        if(vibrateSetting)
                                                            vib.vibrate(10);
                                                        opf.increaseCount(portraitMode, incdecAmount);

                                                    }
                                                    else if (rg.getCheckedRadioButtonId() == 1) //decrease
                                                    {
                                                        //DECREASE by
                                                        int currCount=opf.getCount();
                                                        if(vibrateSetting)
                                                            vib.vibrate(10);
                                                        opf.decreaseCount(portraitMode, incdecAmount);

                                                    }

                                                    //remove keyboard
                                                    InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                    imm.hideSoftInputFromWindow(incdecInput.getWindowToken(), 0);
                                                }
                                                catch (NoCountEnteredException e1)
                                                {
                                                    if(vibrateSetting)
                                                        vib.vibrate(pattern, -1);
                                                    Snackbar.make(view, R.string.no_input_message, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                                    dialog.cancel();
                                                }
                                                catch (Exception e2)
                                                {
                                                    if (vibrateSetting)
                                                        vib.vibrate(pattern, -1);
                                                    Snackbar.make(view, R.string.invalid_number_entered, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                                    dialog.cancel();

                                                    //remove keyboard
                                                    InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                    imm.hideSoftInputFromWindow(incdecInput.getWindowToken(), 0);
                                                }


                                            }
                                        });
                                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });

                                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                                //removes keyboard from screen when user clicks outside of dialog box so it is not stuck on the screen
                                                InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                                                imm.hideSoftInputFromWindow(incdecInput.getWindowToken(), 0);

                                            }
                                        });

                                        builder.show();
                                        rg.check(rg.getChildAt(0).getId());
                                        incdecInput.requestFocus();
                                        InputMethodManager imm2 = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }

                                }

                            }
                        });

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
                            count++;
                            opf.addCount(portraitMode);
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
                            opf.subCount(portraitMode);
                            count--;
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
                                    opf.resetCount(portraitMode);
                                    count=0;
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
                            opf.resetCount(portraitMode);
                            count=0;
                        }
                    }
                }
        );

        //Top appbar with options, do not remove
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_main);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_home);

        //Display Tutorial
        if(!tutorialComplete) {
            showcaseDialogTutorial();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED); //lock orientation so tutorial doesn't restart on orientation change
        }

    }

    @Override
    public void onPause()
    {
        super.onPause();

        //save count
        int c = opf.getCount();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("count_key", c);
        editor.commit();

        //save orientation mode
        editor.putBoolean("orientation_key", portraitMode);
        editor.commit();

        //remove keyboard so its not stuck on screen when activity is pauses
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        if(input != null)
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

        if(counterChangeView != null)
            imm.hideSoftInputFromWindow(counterChangeView.getWindowToken(), 0);

        if(incdecInput != null)
            imm.hideSoftInputFromWindow(incdecInput.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
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
        else if (id == R.id.nav_more) {
            //open link to developer page with the rest of my apps

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showcaseDialogTutorial(){
        final SharedPreferences tutorialShowcases = getSharedPreferences("showcaseTutorial", MODE_PRIVATE);

        boolean run;

        run = tutorialShowcases.getBoolean("run?", true);

        if(run){//If the user already went through the showcases it won't do it again.
            final ViewTarget plus = new ViewTarget(R.id.plusButton , this);//Variable holds the item that the showcase will focus on.
            final ViewTarget minus = new ViewTarget(R.id.minusButton , this);
            final ViewTarget reset = new ViewTarget(R.id.resetButton , this);
            final ViewTarget count = new ViewTarget(R.id.count , this);

            //This code creates a new layout parameter so the button in the showcase can move to a new spot.
            final RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // This aligns button to the bottom left side of screen
            lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lps.addRule(RelativeLayout.CENTER_HORIZONTAL);
            // Set margins to the button, we add 16dp margins here
            int margin = ((Number) (getResources().getDisplayMetrics().density * 16)).intValue();
            lps.setMargins(margin, margin, margin, margin);

            //This creates the first showcase.
            ShowcaseView.Builder res = new ShowcaseView.Builder(this, true)
                    .setTarget(plus)
                    .setContentTitle(getString(R.string.tutorial_plus_title))
                    .setContentText(getString(R.string.tutorial_plus_text))
                    .setStyle(R.style.CustomShowcaseTheme);
            tut = res.build();
            tut.setButtonText(getString(R.string.next));

            //When the button is clicked then the switch statement will check the counter and make the new showcase.
            tut.overrideButtonClick(new View.OnClickListener() {
                int index = 0;

                @Override
                public void onClick(View v) {
                    index++;
                    switch (index) {
                        case 1:
                            tut.setTarget(minus);
                            tut.setContentTitle(getString(R.string.tutorial_minus_title));
                            tut.setContentText(getString(R.string.tutorial_minus_text));
                            tut.setButtonText(getString(R.string.next));
                            break;

                        case 2:
                            tut.setTarget(reset);
                            tut.setContentTitle(getString(R.string.tutorial_reset_title));
                            tut.setContentText(getString(R.string.tutorial_reset_text));
                            tut.setButtonText(getString(R.string.next));
                            break;

                        case 3:
                            tut.setTarget(count);
                            tut.setContentTitle(getString(R.string.tutorial_count_title));
                            tut.setContentText(getString(R.string.tutorial_count_text));
                            tut.setButtonText(getString(R.string.done));
                            break;

                        case 4:
                            tut.hide();
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR); //unlock orientation
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("MainTutorial", true);
                            editor.commit();

                            break;

                    }
                }
            });
        }
    }
}
