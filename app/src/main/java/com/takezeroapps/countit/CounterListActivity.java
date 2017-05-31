package com.takezeroapps.countit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import static android.graphics.Color.BLACK;

public class CounterListActivity extends AppCompatActivity {

    private ListView listView;
    private String[] multicounterNamesArray;
    private ArrayList<String> multicounterNameList = new ArrayList<String>();
    public static ArrayList<Multicounter> multicounterList = new ArrayList<Multicounter>();
    TextView tx;
    String[] c = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    public static final String MULTICOUNTER_NAME_KEY = "multicounter_name";

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
        multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
        //initialize listView to the listview object in the xml file
        listView = (ListView)findViewById(R.id.counter_list);
        //add array of counter names to adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.mcounters_text_format, multicounterNamesArray);

        //set adapter to list view
        listView.setAdapter(adapter);

        //LISTENER for each item in ListView (Each multicounter)
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) (listView.getItemAtPosition(position));
                Bundle bundle = new Bundle();
                bundle.putString(MULTICOUNTER_NAME_KEY, selected);
                Intent intent = new Intent(CounterListActivity.this, MultiCounterActivity.class);
                intent.putExtras(bundle);
                Log.d("test", "Selected: "+selected);
                startActivity(intent);

                //startActivity(new Intent(CounterListActivity.this, MultiCounterActivity.class));
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //on long click

                final String item = (String) ((TextView) view).getText();

                //create popup dialog
                String names[] ={getResources().getString(R.string.open_counter), getResources().getString(R.string.rename_counter), getResources().getString(R.string.delete_counter), getResources().getString(R.string.details_counter), };
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(CounterListActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View convertView = (View) inflater.inflate(R.layout.popup_list, null);
                alertDialog.setView(convertView);
                alertDialog.setTitle(item);
                ListView lv = (ListView) convertView.findViewById(R.id.listView1);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(CounterListActivity.this,android.R.layout.simple_list_item_1,names);
                lv.setAdapter(adapter);
                alertDialog.show();

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View viewIn, int position, long id) {
                        //Log.d("test","Item "+ item +": " + ((TextView) viewIn).getText());
                        if(position == 0) //open button
                        {

                        }
                        else if(position == 1) //rename button
                        {

                        }
                        else if(position==2) //delete button
                        {

                        }
                        else if(position==3) //details button
                        {

                        }
                    }
                });

                return true;
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        File f = new File("/data/data/com.takezeroapps.countit/shared_prefs/MultiCounterList.xml");
        if (f.exists())
        {
            SharedPreferences pref = getSharedPreferences("MultiCounterList", Context.MODE_PRIVATE);
            SharedPreferences.Editor e = pref.edit();
            String jsonMC = pref.getString("MultiCounterList", null);
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Multicounter>>(){}.getType();
            multicounterList = gson.fromJson(jsonMC, type);
        }
        else
        {
            Log.d("test", "sharedpref doesn't exist");
        }
        
        for(Multicounter m: multicounterList)
        {
            Log.d("test", "MC Name: "+m.getName());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.multicounter_list_drawer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // plus sign button (to add multi-counter)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.multicounter_add) { //add counter button

            if(multicounterNameList.size() + 1 > 50) //maximum number of multicounters set to 50
            {
                Snackbar.make(getWindow().getDecorView().getRootView(), R.string.max_number_of_multicounters_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
            else {
                //create dialog
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
                //code below sets it so user cannot enter more than 1 line (the "return" button on the keyboard now turns into the "done" button)
                input.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if (hasFocus) {
                            input.setSingleLine(true);
                            input.setMaxLines(1);
                            input.setLines(1);
                        }
                    }
                });

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
                layout.setPadding(60, 50, 60, 10);

                builder.setView(layout);

                builder.create();

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String mcName = input.getText().toString(); //input text - the user defined multi-counter name
                        int initCount = Integer.parseInt(sp.getSelectedItem().toString()); // initial count entered by user

                        if (mcName.equals("")) {
                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.no_counter_name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            dialog.cancel();
                        } else if (inCounterList(mcName)) {
                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.counter_already_exists, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            dialog.cancel();
                        } else if (mcName.length() > 40) {
                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.mc_title_length_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            dialog.cancel();
                        } else {

                            //create new multicounter and add to list
                            multicounterList.add(new Multicounter(mcName, initCount));

                            //save multicounter list
                            SharedPreferences sharedPref = getSharedPreferences("MultiCounterList", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            Gson gson = new Gson();
                            String jsonMC = gson.toJson(multicounterList);
                            editor.putString("MultiCounterList", jsonMC);
                            editor.commit();

                            multicounterNameList.add(mcName);
                            saveCounterList(multicounterNameList);
                            finish();
                            startActivity(getIntent());
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
        else if (id == R.id.multicounter_search) //search button
        {

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

   public boolean inCounterList(String counterName)
   {
       for(String s: multicounterNameList)
       {
           if(counterName.equals(s))
           {
               return true;
           }
       }
       return false;
   }

    public class OnSpinnerItemClicked implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent,
                                   View view, int pos, long id) {
            Toast.makeText(parent.getContext(), "Clicked : " +
                    parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
            Log.d("test", "option: "+parent.getItemAtPosition(pos).toString());


        }

        @Override
        public void onNothingSelected(AdapterView parent) {
            // Do nothing.
        }
    }

    //SEARCH FUNCTIONS


}
