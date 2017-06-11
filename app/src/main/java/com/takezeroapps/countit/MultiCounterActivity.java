package com.takezeroapps.countit;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
import java.util.Iterator;

import static android.graphics.Color.BLACK;

public class MultiCounterActivity extends AppCompatActivity {
    //private ArrayList<Multicounter> multicounterList = new ArrayList<Multicounter>();
    private Multicounter current;
    SingleCounterFragment testFragment = new SingleCounterFragment();
    boolean vibrateSetting, resetconfirmSetting, screenSetting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_counter);
        Bundle bundle = getIntent().getExtras();
        String counterName = bundle.getString(CounterListActivity.MULTICOUNTER_NAME_KEY);
        setTitle(counterName);

        //CounterListActivity.multicounterNameList = getCounterList();

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

        if(screenSetting)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        //set new modified time every time the activity is paused
        for(Multicounter m: CounterListActivity.multicounterList)
        {
            if(current.getName().equals(m.getName()))
            {
                m.setModifiedDateTime();
                m.setModifiedTimeStamp();
                break;
            }
        }

        //save multicounter list
        saveMultiCounterList();
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

        if (id == R.id.multicounter_add) { //add counter button
            if(current.counters.size() + 1 > 10) //maximum number of multicounters set to 50
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
                                saveCountersToMulticounter(current.counters);
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
                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.no_counter_count, Snackbar.LENGTH_LONG).setAction("Action", null).show();
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

        else if (id == R.id.multicounter_delete) { //delete multicounter button
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

        else if (id == R.id.multicounter_edit) { //edit/rename multicounter button

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
            final EditText counterEdit = new EditText(context);
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
                        } else if (inCounterList(counterName)) {
                            Snackbar.make(MultiCounterActivity.this.getWindow().getDecorView().getRootView(), R.string.mcounter_already_exists, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            dialog.cancel();
                        } else if (counterName.length() > 40) {
                            Snackbar.make(MultiCounterActivity.this.getWindow().getDecorView().getRootView(), R.string.mc_title_length_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            dialog.cancel();
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
                            for(Multicounter m: CounterListActivity.multicounterList)
                            {
                                if(m.getName().equals(current.getName()))
                                {
                                    m.setName(counterName);
                                    m.setModifiedDateTime();
                                    m.setModifiedTimeStamp();
                                    break;
                                }
                            }

                            //save multicounter list
                            saveMultiCounterList();

                            setTitle(counterName);
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
                }
            });

            builder.show();

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

    public void saveCountersToMulticounter(ArrayList<Counter> counterList) {
        for(Multicounter m: CounterListActivity.multicounterList)
        {
            if(current.getName().equals(m.getName()))
            {
                m.counters=counterList;
                break;
            }
        }
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
}
