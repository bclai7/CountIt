package com.takezeroapps.countit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.takezeroapps.countit.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    boolean isNegative;
    int newNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addButton = (Button) findViewById(R.id.plusButton);
        Button subButton = (Button) findViewById(R.id.minusButton);
        ImageButton resetButton = (ImageButton) findViewById(R.id.resetButton);
        ImageButton counterButton = (ImageButton) findViewById(R.id.countButton);
        final OperatorFragment opf = new OperatorFragment();

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
                        //input.setText(Integer.toString(opf.getCount())); //sets default text to the current number
                        //input.setSelection((input.getText().toString().length())); //sets index at end of text
                        //input.selectAll(); //selects all text entered
                        counterChanger.setView(input);
                        //newNum=opf.getCount();

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
                                    Snackbar.make(view, R.string.too_high_message, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                    dialog.cancel();
                                }
                                else if(newNum < -2147483646) //if number entered is too low, print error and return to original number
                                {
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
                            Snackbar.make(v, R.string.max_num, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                        }
                        else //else add to count
                            opf.addCount();
                    }
                }
        );
        subButton.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        int currSubCount=opf.getCount();
                        if((currSubCount - 1) < -2147483647) //if next number would be lower than min print error
                        {
                            Snackbar.make(v, R.string.min_num, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                        }
                        else //else decrement
                            opf.subCount();
                    }
                }
        );
        resetButton.setOnClickListener(
                new ImageButton.OnClickListener(){
                    public void onClick(View v){
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
        if (id == R.id.action_settings) {
            Log.d("test", "Setting dots Pressed");
            return true;
        }

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
