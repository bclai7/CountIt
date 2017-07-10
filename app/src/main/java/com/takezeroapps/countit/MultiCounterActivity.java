package com.takezeroapps.countit;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static android.graphics.Color.BLACK;
import static com.takezeroapps.countit.MulticounterComparator.CREATED_SORT;
import static com.takezeroapps.countit.MulticounterComparator.MODIFIED_SORT;
import static com.takezeroapps.countit.MulticounterComparator.NAME_SORT;
import static com.takezeroapps.countit.MulticounterComparator.decending;
import static com.takezeroapps.countit.MulticounterComparator.getComparator;

public class MultiCounterActivity extends AppCompatActivity {
    //private ArrayList<Multicounter> multicounterList = new ArrayList<Multicounter>();
    private Multicounter current;
    SingleCounterFragment testFragment = new SingleCounterFragment();
    boolean vibrateSetting, resetconfirmSetting, screenSetting;
    EditText counterEdit;
    int viewOption=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_counter);
        Bundle bundle = getIntent().getExtras();
        final String counterName = bundle.getString(CounterListActivity.MULTICOUNTER_NAME_KEY);
        setTitle(counterName);

        //load data into multi counter list
        File f = new File("/data/data/com.takezeroapps.countit/shared_prefs/MultiCounterList.xml");
        if (f.exists()) {
            SharedPreferences pref = getSharedPreferences("MultiCounterList", Context.MODE_PRIVATE);
            SharedPreferences.Editor e = pref.edit();
            String jsonMC = pref.getString("MultiCounterList", null);
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Multicounter>>() {
            }.getType();
            CounterListActivity.multicounterList = gson.fromJson(jsonMC, type);
        }

        //retrieve counter view option
        SharedPreferences sharedPref = MultiCounterActivity.this.getPreferences(Context.MODE_PRIVATE);
        viewOption = sharedPref.getInt("CounterView", 0);

        for(Multicounter mc: CounterListActivity.multicounterList)
        {
            if(mc.getName().equals(counterName))
            {
                current=mc;
                break;
            }
        }

        int cc = 0;
        //load counter fragments
        try
        {
            for (Counter c : current.counters) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                SingleCounterFragment sc_fragment = SingleCounterFragment.newInstance(current.getName(), c.getLabel(), c.getCount());
                fragmentTransaction.add(R.id.mc_linear_scroll_layout, sc_fragment, c.getCounterId());
                fragmentTransaction.commit();
                fragmentManager.executePendingTransactions();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume()
    {
        super.onResume();
        //get saved "keep screen on" setting from shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        screenSetting = prefs.getBoolean("switch_preference_screen", false);
        resetconfirmSetting = prefs.getBoolean("switch_preference_resetconfirm", true);

        if(screenSetting)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        //retrieve counter view option
        SharedPreferences sharedPref = MultiCounterActivity.this.getPreferences(Context.MODE_PRIVATE);
        viewOption = sharedPref.getInt("CounterView", 0);
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

    // plus sign button (to add counter)
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
                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.max_number_of_counters_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        }
                        else {
                            //create dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(MultiCounterActivity.this);
                            builder.setTitle(R.string.create_single_counter_title);
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
                            final EditText cName = new EditText(context);
                            cName.setHint(R.string.name_hint);
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
                                    }
                                }
                            });

                            //textview to create a space in between fields
                            final TextView space = new TextView(context);
                            space.setText("");
                            space.setTextSize(16);
                            layout.addView(space);

                            //textview telling user to enter counter starting count
                            final TextView sCount = new TextView(context);
                            sCount.setText(R.string.set_starting_count);
                            sCount.setTextSize(16);
                            sCount.setTextColor(BLACK);
                            layout.addView(sCount);

                            //Text input for counter name
                            final EditText cCount = new EditText(context);
                            cCount.setHint(R.string.count_hint);
                            layout.addView(cCount);

                            //bring up number pad
                            cCount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);

                            layout.setPadding(60, 50, 60, 10);

                            builder.setView(layout);

                            builder.create();

                            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        String counterName = cName.getText().toString(); //input text - the user defined counter name

                                        if(cCount.getText().toString().equals("") || cCount.getText().toString().isEmpty() || cCount.getText().toString().length() == 0  || TextUtils.isEmpty(cCount.getText().toString())) throw new IllegalArgumentException();

                                        int startCount = Integer.parseInt(cCount.getText().toString()); // starting count entered by user

                                        if (counterName.isEmpty() || counterName.length() == 0 || counterName.equals("") || TextUtils.isEmpty(counterName)) {
                                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.no_counter_name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                        } else if (inSingleCounterList(counterName)) {
                                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.counter_already_exists, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                        } else if (counterName.length() > 20) {
                                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.sc_title_length_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                        } else if (numberInvalid(startCount)) {
                                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.invalid_number_entered, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                        } else {
                                            //create new counter and add to multicounter
                                            Counter newCounter = new Counter(current.getName(), counterName, startCount);
                                            current.counters.add(newCounter);

                                            //set modified times
                                            current.setModifiedDateTime();
                                            current.setModifiedTimeStamp();

                                            //save multicounter list
                                            saveMultiCounterList();

                                            FragmentManager fragmentManager = getFragmentManager();
                                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                            SingleCounterFragment sc_fragment = SingleCounterFragment.newInstance(current.getName(), counterName, startCount);
                                            fragmentTransaction.add(R.id.mc_linear_scroll_layout, sc_fragment, newCounter.getCounterId());
                                            fragmentTransaction.commit();

                                        }

                                    }
                                    catch (IllegalArgumentException e)
                                    {
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
                                    String counterName = counterEdit.getText().toString(); //input text - the user defined counter name

                                    if (counterName.isEmpty() || counterName.length() == 0 || counterName.equals("") || TextUtils.isEmpty(counterName)) {
                                        Snackbar.make(MultiCounterActivity.this.getWindow().getDecorView().getRootView(), R.string.no_mcounter_name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                        dialog.cancel();
                                        removeKeyboard();
                                    } else if (inCounterList(counterName)) {
                                        Snackbar.make(MultiCounterActivity.this.getWindow().getDecorView().getRootView(), R.string.mcounter_already_exists, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                        dialog.cancel();
                                        removeKeyboard();
                                    } else if (counterName.length() > 40) {
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
                                        CounterListActivity.multicounterNameList.add(it, counterName);
                                        saveCounterList(CounterListActivity.multicounterNameList);

                                        //set the new name in the actual counter object
                                        current.setName(counterName);
                                        current.setModifiedDateTime();
                                        current.setModifiedTimeStamp();

                                        //save multicounter list
                                        saveMultiCounterList();

                                        setTitle(counterName);

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
                                Iterator<Multicounter> a = CounterListActivity.multicounterList.iterator();
                                while (a.hasNext()) {
                                    Multicounter m = a.next();
                                    if(m.getName().equals(current.getName()))
                                    {
                                        a.remove();
                                        break;
                                    }
                                }
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
                                    finish();
                                    startActivity(getIntent());
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
                            finish();
                            startActivity(getIntent());
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
}
