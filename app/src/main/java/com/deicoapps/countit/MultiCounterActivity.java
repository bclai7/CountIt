package com.deicoapps.countit;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static android.graphics.Color.BLACK;

public class MultiCounterActivity extends AppCompatActivity {
    //private ArrayList<Multicounter> multicounterList = new ArrayList<Multicounter>();
    private Multicounter current;
    boolean vibrateSetting, resetconfirmSetting, screenSetting;
    EditText counterEdit;
    int viewOption=0;
    public static ArrayList<String> fragTagList;
    boolean countIsNegative=false;
    Vibrator vib;
    long[] pattern = new long[4];
    boolean tutorialComplete; //boolean storing whether or not user has completed the tutorial/tip for this particular activity
    ShowcaseView tut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_counter);
        Bundle bundle = getIntent().getExtras();
        final String counterName = bundle.getString(CounterListActivity.MULTICOUNTER_NAME_KEY);
        setTitle(counterName);
        fragTagList=new ArrayList<String>();

        //get tutorial sharedpref
        SharedPreferences sharedPrefB = PreferenceManager.getDefaultSharedPreferences(this);
        tutorialComplete=sharedPrefB.getBoolean("MulticounterTutorial", false);

        vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        pattern[0]=0;
        pattern[1]=20;
        pattern[2]=150;
        pattern[3]=20;

        //load data into multi counter list
        File f = new File("/data/data/com.deicoapps.countit/shared_prefs/MultiCounterList.xml");
        if (f.exists()) {
            SharedPreferences pref = getSharedPreferences("MultiCounterList", Context.MODE_PRIVATE);
            SharedPreferences.Editor e = pref.edit();
            String jsonMC = pref.getString("MultiCounterList", null);
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, Multicounter>>() {
            }.getType();
            CounterListActivity.multicounterList = gson.fromJson(jsonMC, type);
        }

        //retrieve counter view option
        SharedPreferences sharedPref = MultiCounterActivity.this.getPreferences(Context.MODE_PRIVATE);
        viewOption = sharedPref.getInt("CounterView", 0);

        current = CounterListActivity.multicounterList.get(counterName);

        ViewGroup linLay = (ViewGroup)findViewById(R.id.mc_linear_scroll_layout);

        //load counter fragments
        for (Counter c : current.counters) {
            if(viewOption==0) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                SingleCounterFragment sc_fragment = SingleCounterFragment.newInstance(current.getName(), c.getLabel(), c.getCount());
                fragmentTransaction.add(R.id.mc_linear_scroll_layout, sc_fragment, c.getCounterId());
                fragmentTransaction.commit();
                fragmentManager.executePendingTransactions();
            }
            else if(viewOption==1) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                SingleCounterCondensedFragment sc_condensed_fragment = SingleCounterCondensedFragment.newInstance(current.getName(), c.getLabel(), c.getCount());
                fragmentTransaction.add(R.id.mc_linear_scroll_layout, sc_condensed_fragment, c.getCounterId());
                fragmentTransaction.commit();
                fragmentManager.executePendingTransactions();
            }

            fragTagList.add(c.getCounterId()); //add to list of fragment tags

        }

        if(!tutorialComplete) {
            showcaseDialogTutorial();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED); //lock orientation so tutorial glitch in landscape
        }

    }

    @Override
    public void onResume()
    {
        super.onResume();
        //get settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        vibrateSetting = sharedPref.getBoolean(SettingsActivity.vibrateKey, true);
        resetconfirmSetting = sharedPref.getBoolean(SettingsActivity.resetKey, true);
        screenSetting = sharedPref.getBoolean(SettingsActivity.screenKey, false);

        if(screenSetting)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        //retrieve counter view option
        SharedPreferences sharedPrefA = MultiCounterActivity.this.getPreferences(Context.MODE_PRIVATE);
        viewOption = sharedPrefA.getInt("CounterView", 0);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //save multicounter list
        saveMultiCounterList();

        //save orientation mode
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("orientation_key", MainActivity.portraitMode);
        editor.commit();

        //remove keyboard so its not stuck on screen when activity is pauses
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(counterEdit != null)
            imm.hideSoftInputFromWindow(counterEdit.getWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.multicounter_drawer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.multicounter_options) { //options button (3 dots)

            //creates dropdown list when option button is clicked
            View menuItemView = findViewById(R.id.multicounter_options);
            PopupMenu popupMenu = new PopupMenu(MultiCounterActivity.this, menuItemView);
            popupMenu.getMenuInflater().inflate(R.menu.multicounter_dropdown_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.add_c) {
                        if(current.counters.size() + 1 > 20) //maximum number of counters set to 50
                        {
                            if (vibrateSetting)
                                vib.vibrate(pattern, -1);
                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.max_number_of_counters_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        }
                        else {
                            //create dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(MultiCounterActivity.this);
                            builder.setTitle(R.string.create_single_counter_title);
                            Context context = MultiCounterActivity.this; //store context in a variable
                            LinearLayout layout = new LinearLayout(context);
                            layout.setOrientation(LinearLayout.VERTICAL);

                            final CharSequence[] negOptions = {MultiCounterActivity.this.getResources().getString(R.string.make_count_negative_num)}; //choices to select from, only one choice so it only has one element
                            builder.setMultiChoiceItems(negOptions, null, new DialogInterface.OnMultiChoiceClickListener() { //checkbox for negative number
                                @Override
                                public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                                    if (isChecked) {
                                        countIsNegative=true; //if checkbox is checked, set the boolean value for negative number as true
                                    }
                                    else {
                                        countIsNegative = false; //otherwise if checkbox is not checked, then keep value positive
                                    }
                                }
                            });

                            //textview telling user to enter counter name
                            final TextView name = new TextView(context);
                            name.setText(R.string.set_counter_name);
                            name.setTextSize(14);
                            name.setTextColor(BLACK);
                            layout.addView(name);

                            //Text input for counter name
                            final EditText cName = new EditText(context);
                            cName.setHint(R.string.name_hint);
                            cName.setFilters(new InputFilter[] {new InputFilter.LengthFilter(20)});
                            cName.setTextSize(14);
                            layout.addView(cName);

                            //code below sets it so user cannot enter more than 1 line (the "return" button on the keyboard now turns into the "done" button)
                            cName.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                                @Override
                                public void onFocusChange(View v, boolean hasFocus) {
                                    // TODO Auto-generated method stub
                                    if (hasFocus) {
                                        cName.setSingleLine(true);
                                        cName.setMaxLines(1);
                                        cName.setLines(1);
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.showSoftInput(cName, InputMethodManager.SHOW_IMPLICIT);
                                    }
                                }
                            });


                            //textview to create a space in between fields
                            final TextView space = new TextView(context);
                            space.setText("");
                            space.setTextSize(14);
                            //layout.addView(space);

                            //textview telling user to enter counter starting count
                            final TextView sCount = new TextView(context);
                            sCount.setText(R.string.set_starting_count);
                            sCount.setTextSize(14);
                            sCount.setTextColor(BLACK);
                            layout.addView(sCount);

                            //Text input for counter name
                            final EditText cCount = new EditText(context);
                            cCount.setHint(R.string.count_hint);
                            cCount.setFilters(new InputFilter[] {new InputFilter.LengthFilter(10)});
                            cCount.setTextSize(14);
                            layout.addView(cCount);

                            //textview to create a space in between fields
                            final TextView spaceA = new TextView(context);
                            spaceA.setText("");
                            spaceA.setTextSize(14);
                            //layout.addView(spaceA);

                            //textview for color dropdown
                            final TextView colorTv = new TextView(context);
                            colorTv.setText(R.string.colors_sub);
                            colorTv.setTextSize(14);
                            colorTv.setTextColor(BLACK);
                            layout.addView(colorTv);

                            //dropdown for initial number of counters
                            final ArrayAdapter<String> adp = new ArrayAdapter<String>(MultiCounterActivity.this,
                                    R.layout.spinner_item_custom_smaller, getResources().getStringArray(R.array.colors_array));
                            final Spinner sp = new Spinner(MultiCounterActivity.this);
                            sp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            sp.setAdapter(adp);
                            layout.addView(sp);

                            //bring up number pad
                            cCount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);

                            layout.setPadding(60, 0, 60, 0);

                            builder.setView(layout);

                            builder.create();

                            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        String counterName = cName.getText().toString(); //input text - the user defined counter name
                                        String selectedColor = sp.getSelectedItem().toString();

                                        if(cCount.getText().toString().equals("") || cCount.getText().toString().isEmpty() || cCount.getText().toString().length() == 0  || TextUtils.isEmpty(cCount.getText().toString())) throw new IllegalArgumentException();

                                        int startCount = Integer.parseInt(cCount.getText().toString()); // starting count entered by user
                                        if(countIsNegative)
                                            startCount=startCount*-1;

                                        if (counterName.isEmpty() || counterName.length() == 0 || counterName.equals("") || TextUtils.isEmpty(counterName)) {
                                            if (vibrateSetting)
                                                vib.vibrate(pattern, -1);
                                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.no_counter_name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                        } else if (inSingleCounterList(counterName)) {
                                            if (vibrateSetting)
                                                vib.vibrate(pattern, -1);
                                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.counter_already_exists, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                        } else if (counterName.length() > 20) {
                                            if (vibrateSetting)
                                                vib.vibrate(pattern, -1);
                                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.sc_title_length_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                        } else if (numberInvalid(startCount)) {
                                            if (vibrateSetting)
                                                vib.vibrate(pattern, -1);
                                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.invalid_number_entered, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                        } else {
                                            //create new counter and add to multicounter
                                            Counter newCounter = new Counter(current.getName(), counterName, startCount, current.counters.size(), selectedColor);
                                            current.counters.add(newCounter);

                                            //set modified times
                                            current.setModifiedDateTime();
                                            current.setModifiedTimeStamp();

                                            //save multicounter list
                                            saveMultiCounterList();

                                            if(viewOption==0) {
                                                FragmentManager fragmentManager = getFragmentManager();
                                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                SingleCounterFragment sc_fragment = SingleCounterFragment.newInstance(current.getName(), counterName, startCount);
                                                fragmentTransaction.add(R.id.mc_linear_scroll_layout, sc_fragment, newCounter.getCounterId());
                                                fragmentTransaction.commit();
                                            }
                                            if(viewOption==1) {
                                                FragmentManager fragmentManager = getFragmentManager();
                                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                SingleCounterCondensedFragment sc__condensed_fragment = SingleCounterCondensedFragment.newInstance(current.getName(), counterName, startCount);
                                                fragmentTransaction.add(R.id.mc_linear_scroll_layout, sc__condensed_fragment, newCounter.getCounterId());
                                                fragmentTransaction.commit();
                                            }
                                            fragTagList.add(newCounter.getCounterId());

                                            finish();
                                            startActivity(getIntent());

                                        }

                                    }
                                    catch (IllegalArgumentException e)
                                    {
                                        if (vibrateSetting)
                                            vib.vibrate(pattern, -1);
                                        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.invalid_starting_count, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                        dialog.cancel();
                                    }
                                }
                            });
                            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            builder.show();
                            countIsNegative=false;
                        }
                    }
                    if (item.getItemId() == R.id.rename_mc) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MultiCounterActivity.this);
                        builder.setTitle(R.string.rename_multi_counter);
                        Context context = MultiCounterActivity.this; //store context in a variable
                        LinearLayout layout = new LinearLayout(context);
                        layout.setOrientation(LinearLayout.VERTICAL);

                        //textview telling user to enter counter name
                        final TextView name = new TextView(context);
                        name.setText(R.string.set_counter_name);
                        name.setTextSize(16);
                        name.setTextColor(BLACK);
                        layout.addView(name);

                        //Text input for counter name
                        counterEdit = new EditText(context);
                        counterEdit.setHint(current.getName());
                        counterEdit.setFilters(new InputFilter[] {new InputFilter.LengthFilter(40)});
                        layout.addView(counterEdit);
                        //code below sets it so user cannot enter more than 1 line (the "return" button on the keyboard now turns into the "done" button)
                        counterEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                // TODO Auto-generated method stub
                                if (hasFocus) {
                                    counterEdit.setSingleLine(true);
                                    counterEdit.setMaxLines(1);
                                    counterEdit.setLines(1);
                                }
                            }
                        });

                        layout.setPadding(60, 50, 60, 10);
                        builder.setView(layout);
                        builder.create();

                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    String newCounterName = counterEdit.getText().toString(); //input text - the user defined counter name

                                    if (newCounterName.isEmpty() || newCounterName.length() == 0 || newCounterName.equals("") || TextUtils.isEmpty(newCounterName)) {
                                        if (vibrateSetting)
                                            vib.vibrate(pattern, -1);
                                        Snackbar.make(MultiCounterActivity.this.getWindow().getDecorView().getRootView(), R.string.no_mcounter_name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                        dialog.cancel();
                                        removeKeyboard();
                                    } else if (inCounterList(newCounterName)) {
                                        if (vibrateSetting)
                                            vib.vibrate(pattern, -1);
                                        Snackbar.make(MultiCounterActivity.this.getWindow().getDecorView().getRootView(), R.string.mcounter_already_exists, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                        dialog.cancel();
                                        removeKeyboard();
                                    } else if (newCounterName.length() > 40) {
                                        if (vibrateSetting)
                                            vib.vibrate(pattern, -1);
                                        Snackbar.make(MultiCounterActivity.this.getWindow().getDecorView().getRootView(), R.string.mc_title_length_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                        dialog.cancel();
                                        removeKeyboard();
                                    } else {

                                        int it=0;
                                        for(String st: CounterListActivity.multicounterNameList)
                                        {
                                            if(st.equals(current.getName()))
                                            {
                                                break;
                                            }
                                            it++;
                                        }

                                        //remove old name from multicounterNameList (list of strings)
                                        Iterator<String> i = CounterListActivity.multicounterNameList.iterator();
                                        while (i.hasNext()) {
                                            String s = i.next(); // must be called before you can call i.remove()
                                            if(s.equals(current.getName()))
                                            {
                                                i.remove();
                                                break;
                                            }
                                        }
                                        //add new name to String list and save
                                        CounterListActivity.multicounterNameList.add(it, newCounterName);
                                        saveCounterList(CounterListActivity.multicounterNameList);

                                        //remove current from MC List to remove old key
                                        CounterListActivity.multicounterList.remove(current.getName());

                                        //set the new name in the actual counter object
                                        current.setName(newCounterName);
                                        current.setModifiedDateTime();
                                        current.setModifiedTimeStamp();

                                        //update key value with new name
                                        CounterListActivity.multicounterList.put(current.getName(), current);

                                        //save multicounter list
                                        saveMultiCounterList();

                                        setTitle(newCounterName);

                                        removeKeyboard();
                                    }

                                }
                                catch (IllegalArgumentException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        });
                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                removeKeyboard();
                            }
                        });

                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                //removes keyboard from screen when user clicks outside of dialog box so it is not stuck on the screen
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                                imm.hideSoftInputFromWindow(counterEdit.getWindowToken(), 0);

                            }
                        });

                        builder.show();

                        counterEdit.requestFocus();
                        InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                    if (item.getItemId() == R.id.delete_mc) {
                        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(MultiCounterActivity.this);

                        deleteDialog.setMessage(R.string.delete_mc_question)
                                .setTitle(R.string.delete_mc_title);
                        deleteDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //find multicounter and delete from list
                                //first find multicounter in multicounterList and remove it
                                CounterListActivity.multicounterList.remove(current.getName());

                                //save multiCounterList
                                saveMultiCounterList();

                                //remove name from multicounterNameList (list of strings)
                                Iterator<String> b = CounterListActivity.multicounterNameList.iterator();
                                while (b.hasNext()) {
                                    String s = b.next(); // must be called before you can call i.remove()
                                    if(s.equals(current.getName()))
                                    {
                                        b.remove();
                                        break;
                                    }
                                }
                                //save string list
                                saveCounterList(CounterListActivity.multicounterNameList);

                                //go back to counter list activity
                                startActivity(new Intent(MultiCounterActivity.this, CounterListActivity.class));

                            }
                        });
                        deleteDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //cancel dialog if "no" is clicked
                                dialog.cancel();
                            }
                        });

                        AlertDialog dialog = deleteDialog.create();
                        deleteDialog.show();
                    }
                    if (item.getItemId() == R.id.reset_all_c) {
                        if(resetconfirmSetting) {
                            // Instantiate an AlertDialog.Builder with its constructor
                            AlertDialog.Builder resetDialog = new AlertDialog.Builder(MultiCounterActivity.this);

                            // Set Dialog Title, message, and other properties
                            resetDialog.setMessage(R.string.reset_all_question)
                                    .setTitle(R.string.reset_all_title)
                            ; // semi-colon only goes after ALL of the properties

                            // Add the buttons
                            resetDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // reset count if "yes" is clicked
                                    current.resetAllCounters();

                                    //set modified times
                                    current.setModifiedDateTime();
                                    current.setModifiedTimeStamp();

                                    saveMultiCounterList();

                                    Bundle bundle = new Bundle();
                                    bundle.putString(CounterListActivity.MULTICOUNTER_NAME_KEY, current.getName());
                                    Intent intent = new Intent(MultiCounterActivity.this, MultiCounterActivity.class);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
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
                            current.resetAllCounters();
                            saveMultiCounterList();

                            Bundle bundle = new Bundle();
                            bundle.putString(CounterListActivity.MULTICOUNTER_NAME_KEY, current.getName());
                            Intent intent = new Intent(MultiCounterActivity.this, MultiCounterActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    }

                    if (item.getItemId() == R.id.switch_view) {
                        String names[] ={getResources().getString(R.string.full_view), getResources().getString(R.string.condensed_view)};
                        AlertDialog.Builder builder = new AlertDialog.Builder(MultiCounterActivity.this);
                        //Log.d("test", "ViewOption: "+viewOption);
                        // Set the dialog title
                        builder.setTitle(R.string.switch_view)
                                // Specify the list array, the items to be selected by default (null for none),
                                // and the listener through which to receive callbacks when items are selected
                                .setSingleChoiceItems(names, viewOption,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //Log.d("test", "selection "+which+" was clicked");
                                                viewOption=which;
                                            }

                                        })
                                // Set the action buttons
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User clicked OK, so save the mSelectedItems results somewhere
                                        // or return them to the component that opened the dialog
                                        //Log.d("test", "selection "+viewOption+" was accepted");

                                        //save the counter view preference
                                        SharedPreferences sharedPref = MultiCounterActivity.this.getPreferences(Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putInt("CounterView", viewOption);
                                        editor.commit();

                                        //change the counter view
                                        saveMultiCounterList();

                                        Bundle bundle = new Bundle();
                                        bundle.putString(CounterListActivity.MULTICOUNTER_NAME_KEY, current.getName());
                                        Intent intent = new Intent(MultiCounterActivity.this, MultiCounterActivity.class);
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });

                        builder.create().show();
                    }

                    return false;
                }

            });
            popupMenu.show();

        }

        return super.onOptionsItemSelected(item);
    }

    public boolean inSingleCounterList(String counterName)
    {
        for(Counter co: current.counters)
        {
            if(co.getLabel().equals(counterName)) {
                return true;
            }
        }
        return false;
    }

    public boolean numberInvalid(int num)
    {
        if(num > 2147483646)
            return true;
        if(num < -2147483646)
            return true;
        return false;
    }

    public void saveMultiCounterList()
    {
        //save multicounter list
        SharedPreferences sharedPref = getSharedPreferences("MultiCounterList", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String jsonMC = gson.toJson(CounterListActivity.multicounterList);
        editor.putString("MultiCounterList", jsonMC);
        editor.commit();
    }

    private void saveCounterList(ArrayList<String> counterList) {
        try {
            FileOutputStream fileOutputStream = openFileOutput("MultiCounterNames.txt", Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
            out.writeObject(counterList);
            out.close();
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean inCounterList(String counterName)
    {
        for(String s: CounterListActivity.multicounterNameList)
        {
            if(counterName.equals(s))
            {
                return true;
            }
        }
        return false;
    }

    //Save orientation

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        // Check for the rotation
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Log.d("test", "Landscape");
            //Toast.makeText(this, "LANDSCAPE", Toast.LENGTH_SHORT).show();
            MainActivity.portraitMode=false;
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("orientation_key", MainActivity.portraitMode);
            editor.commit();
        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Log.d("test", "Portrait");
            //Toast.makeText(this, "PORTRAIT", Toast.LENGTH_SHORT).show();
            MainActivity.portraitMode=true;
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("orientation_key", MainActivity.portraitMode);
            editor.commit();
        }
    }

    public void removeKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(counterEdit.getWindowToken(), 0);
    }

    public String getColorString(String color)
    {
        if(color.equalsIgnoreCase("White"))
        {
            return "WHITE";
        }
        else if(color.equalsIgnoreCase("Red"))
        {
            return "RED";
        }

        else if(color.equalsIgnoreCase("Blue"))
        {
            return "BLUE";
        }

        else if(color.equalsIgnoreCase("Green"))
        {
            return "GREEN";
        }

        else if(color.equalsIgnoreCase("Yellow"))
        {
            return "YELLOW";
        }

        else if(color.equalsIgnoreCase("Orange"))
        {
            return "ORANGE";
        }

        else if(color.equalsIgnoreCase("Purple"))
        {
            return "PURPLE";
        }

        else if(color.equalsIgnoreCase("Pink"))
        {
            return "PINK";
        }

        else if(color.equalsIgnoreCase("Gray"))
        {
            return "GRAY";
        }

        return "WHITE";
    }

    private void showcaseDialogTutorial(){

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
                .setTarget(Target.NONE)
                .setContentTitle(getString(R.string.tutorial_multicounter_title))
                .setContentText(getString(R.string.tutorial_multicounter_text))
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
                        tut.setTarget(Target.NONE);
                        tut.setContentTitle(getString(R.string.tutorial_multicounter2_title));
                        tut.setContentText(getString(R.string.tutorial_multicounter2_text));
                        tut.setButtonText(getString(R.string.next));
                        break;
                    case 2:
                        tut.setTarget(Target.NONE);
                        tut.setContentTitle(getString(R.string.tutorial_multicounterview_title));
                        tut.setContentText(getString(R.string.tutorial_multicounterview_text));
                        tut.setButtonText(getString(R.string.done));
                        break;
                    case 3:
                        tut.hide();
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR); //unlock orientation
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MultiCounterActivity.this);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("MulticounterTutorial", true);
                        editor.commit();
                        break;

                }
            }
            });
    }
}
