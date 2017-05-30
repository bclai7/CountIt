package com.takezeroapps.countit;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
    private ArrayList<Multicounter> multicounterList = new ArrayList<Multicounter>();
    private Multicounter current;
    SingleCounterFragment testFragment = new SingleCounterFragment();
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
            multicounterList = gson.fromJson(jsonMC, type);
        }

        for(Multicounter mc: multicounterList)
        {
            if(mc.getName().equals(counterName))
            {
                current=mc;
                break;
            }
        }

        //load counter fragments
        for(Counter c: current.counters)
        {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            SingleCounterFragment sc_fragment = new SingleCounterFragment();
            //SingleCounterFragment sc_fragment = (SingleCounterFragment) getFragmentManager().findFragmentById(R.id.default_fragment);
            Log.d("test", "new 1");
            //sc_fragment.setLabel(c.getLabel());
            //sc_fragment.setCount(c.getCount());
            Log.d("test", "new 2");
            fragmentTransaction.add(R.id.mc_linear_layout, sc_fragment);
            fragmentTransaction.commit();
        }

    }

    @Override
    public void onResume()
    {
        super.onResume();
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

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            SingleCounterFragment fragment = new SingleCounterFragment();
            fragmentTransaction.add(R.id.mc_linear_layout, fragment);
            fragmentTransaction.commit();

        }

        return super.onOptionsItemSelected(item);
    }
}
