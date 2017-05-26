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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static android.graphics.Color.BLACK;

public class CounterListActivity extends AppCompatActivity {

    private ListView listView;
    private String[] multicounterNames;
    private ArrayList<String> multicounterNameList = new ArrayList<String>();
    TextView tx;
    String[] c = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

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
            builder.setTitle(R.string.create_counter_title);
            Context context = CounterListActivity.this; //store context in a variable
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);

            //textview telling user to enter counter name
            final TextView name = new TextView(context);
            name.setText(R.string.set_counter_name);
            name.setTextSize(16);
            name.setTextColor(BLACK);
            layout.addView(name);

            //Text input for counter name
            final EditText input = new EditText(context);
            input.setHint(R.string.name_hint);
            layout.addView(input);

            //textview to create a space in between fields
            final TextView space = new TextView(context);
            space.setText("");
            space.setTextSize(16);
            layout.addView(space);

            //textview telling user to select initial count
            final TextView countTv = new TextView(context);
            countTv.setText(R.string.init_num_counters);
            countTv.setTextSize(16);
            countTv.setTextColor(BLACK);
            layout.addView(countTv);

            //dropdown for initial number of counters
            final ArrayAdapter<String> adp = new ArrayAdapter<String>(CounterListActivity.this,
                    R.layout.spinner_item_custom, c);
            final Spinner sp = new Spinner(CounterListActivity.this);
            sp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            sp.setAdapter(adp);
            layout.addView(sp);
            layout.setPadding(60,50,60,10);

            builder.setView(layout);

            builder.create();

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String it = input.getText().toString(); //input text
                    multicounterNameList.add(it);
                    saveCounterList(multicounterNameList);
                    finish();
                    startActivity(getIntent());

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
        //for the actual counter activity, for each count in the counter

        //save move list
        SharedPreferences sharedPref = getSharedPreferences(filename, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        Gson gson = new Gson();
        String jsonMoves = gson.toJson(moveList);

        editor.putString(filename, jsonMoves);
        editor.commit();
    */

    public class OnSpinnerItemClicked implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent,
                                   View view, int pos, long id) {
            Toast.makeText(parent.getContext(), "Clicked : " +
                    parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();


        }

        @Override
        public void onNothingSelected(AdapterView parent) {
            // Do nothing.
        }
    }
}
