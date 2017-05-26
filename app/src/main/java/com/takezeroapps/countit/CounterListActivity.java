package com.takezeroapps.countit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CounterListActivity extends AppCompatActivity {

    private ListView listView;
    private String[] multicounterNames;
    private ArrayList<String> multicounterNameList = new ArrayList<String>();
    int[] c = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter_list);

        if(getCounterList() == null)
        {
            Log.d("test", "counterlist null");
            saveCounterList(multicounterNameList);
        }

        //load text file storing the multicounter names
        multicounterNameList=getCounterList();
        Log.d("test", "Size: "+multicounterNameList.size());

        //convert to array so it can be read by adapter
        multicounterNames = multicounterNameList.toArray(new String[multicounterNameList.size()]);
        //initialize listView to the listview object in the xml file
        listView = (ListView)findViewById(R.id.counter_list);
        //add array of counter names to adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.mcounters, multicounterNames);

        for(int i=0; i<adapter.getCount(); i++)
        {
            Log.d("test", "Adapter item "+i+": "+adapter.getItem(i));
            Log.d("test", "Array item "+i+": "+multicounterNames[i]);
        }

        //set adapter to list view
        listView.setAdapter(adapter);

        //LISTENER
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //showRoute(position);
                Log.d("test", "item clicked");
            }
        });
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

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.multicounter_add) { //add counter button
            AlertDialog.Builder builder = new AlertDialog.Builder(CounterListActivity.this);
            builder.setTitle("Name this counter");
            builder.setCancelable(false);

            final EditText input = new EditText(CounterListActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String it = input.getText().toString(); //input text
                    //createCounter(it);
                    multicounterNameList.add(it);
                    saveCounterList(multicounterNameList);
                    finish();
                    startActivity(getIntent());
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<String> getCounterList() {
        ArrayList<String> counterList = null;

        try {
            FileInputStream inputStream = openFileInput("MultiCounterNames.txt");
            ObjectInputStream in = new ObjectInputStream(inputStream);
            counterList = (ArrayList<String>) in.readObject();
            in.close();
            inputStream.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return counterList;
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

    /*
    public void createCounter(String counterName)
    {
        //save file name
        SharedPreferences sp = getSharedPreferences("MulticounterList", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        multicounterNameList.add(counterName);
        Set<String> set = new HashSet<String>();
        set.addAll(multicounterNameList);
        ed.putStringSet("MulticounterList", set);
        ed.commit();


        //for the actual counter activity, for each count in the counter

        //save move list
        SharedPreferences sharedPref = getSharedPreferences(filename, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        Gson gson = new Gson();
        String jsonMoves = gson.toJson(moveList);

        editor.putString(filename, jsonMoves);
        editor.commit();


    }
    */
}
