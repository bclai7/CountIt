package com.takezeroapps.countit;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
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
import java.lang.reflect.Type;
import java.util.ArrayList;

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
        SharedPreferences sharedPref = getSharedPreferences("MultiCounterList", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String jsonMC = gson.toJson(CounterListActivity.multicounterList);
        editor.putString("MultiCounterList", jsonMC);
        editor.commit();
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
                                SharedPreferences sharedPref = getSharedPreferences("MultiCounterList", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                Gson gson = new Gson();
                                String jsonMC = gson.toJson(CounterListActivity.multicounterList);
                                editor.putString("MultiCounterList", jsonMC);
                                editor.commit();

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
}
