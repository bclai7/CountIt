package com.takezeroapps.countit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
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
import java.util.Iterator;

import static android.graphics.Color.BLACK;

public class CounterListActivity extends AppCompatActivity {

    private ListView listView;
    private String[] multicounterNamesArray;
    public static ArrayList<String> multicounterNameList = new ArrayList<String>();
    public static ArrayList<Multicounter> multicounterList = new ArrayList<Multicounter>();
    TextView tx;
    String[] c = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    public static final String MULTICOUNTER_NAME_KEY = "multicounter_name";
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter_list);

        if(getCounterList() == null)
        {
            saveCounterList(multicounterNameList);
        }

        //load text file storing the multicounter names
        multicounterNameList=getCounterList();

        //convert to array so it can be read by adapter
        multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
        //initialize listView to the listview object in the xml file
        listView = (ListView)findViewById(R.id.counter_list);
        //add array of counter names to adapter
        adapter = new ArrayAdapter<String>(this, R.layout.mcounters_text_format, multicounterNamesArray);

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
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                //on long click

                final String item = (String) ((TextView) view).getText();
                //create popup dialog
                String names[] ={getResources().getString(R.string.open_counter), getResources().getString(R.string.rename_counter), getResources().getString(R.string.delete_counter), getResources().getString(R.string.details_counter), };
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(CounterListActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View convertView = (View) inflater.inflate(R.layout.popup_list, null);
                alertDialog.setView(convertView);
                alertDialog.setTitle(item);
                ListView lv = (ListView) convertView.findViewById(R.id.listView1);
                ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(CounterListActivity.this,android.R.layout.simple_list_item_1,names);
                lv.setAdapter(adapter1);
                final AlertDialog alert = alertDialog.create();
                alert.show();

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View viewIn, int position, long id) {
                        //Log.d("test","Item "+ item +": " + ((TextView) viewIn).getText());
                        if(position == 0) //open button
                        {
                            Bundle bundle = new Bundle();
                            bundle.putString(MULTICOUNTER_NAME_KEY, item);
                            Intent intent = new Intent(CounterListActivity.this, MultiCounterActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);

                        }
                        else if(position == 1) //rename button
                        {
                            alert.dismiss();
                            //create dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(CounterListActivity.this);
                            builder.setTitle(R.string.rename_multi_counter);
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
                            final EditText counterEdit = new EditText(context);
                            counterEdit.setHint(item);
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
                                            Snackbar.make(CounterListActivity.this.getWindow().getDecorView().getRootView(), R.string.no_mcounter_name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                        } else if (inCounterList(counterName)) {
                                            Snackbar.make(CounterListActivity.this.getWindow().getDecorView().getRootView(), R.string.mcounter_already_exists, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                        } else if (counterName.length() > 40) {
                                            Snackbar.make(CounterListActivity.this.getWindow().getDecorView().getRootView(), R.string.mc_title_length_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                        } else {

                                            int it=0;
                                            for(String st: multicounterNameList)
                                            {
                                                if(st.equals(item))
                                                {
                                                    break;
                                                }
                                                it++;
                                            }

                                            //remove old name from multicounterNameList (list of strings)
                                            Iterator<String> i = multicounterNameList.iterator();
                                            while (i.hasNext()) {
                                                String s = i.next(); // must be called before you can call i.remove()
                                                if(s.equals(item))
                                                {
                                                    i.remove();
                                                    break;
                                                }
                                            }
                                            //add new name to String list and save
                                            multicounterNameList.add(it, counterName);
                                            saveCounterList(multicounterNameList);

                                            //set the new name in the actual counter object
                                            for(Multicounter m: multicounterList)
                                            {
                                                if(m.getName().equals(item))
                                                {
                                                    m.setName(counterName);
                                                    m.setModifiedDateTime();
                                                    m.setModifiedTimeStamp();
                                                    break;
                                                }
                                            }

                                            //save multicounter list
                                            saveMultiCounterList();

                                            ((TextView)view).setText(counterName);
                                            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                            adapter = new ArrayAdapter<String>(CounterListActivity.this, R.layout.mcounters_text_format, multicounterNamesArray);
                                            listView.setAdapter(adapter);
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
                        else if(position==2) //delete button
                        {
                            //dismiss the previous dialog with the list of options
                            alert.dismiss();
                            AlertDialog.Builder deleteDialog = new AlertDialog.Builder(CounterListActivity.this);

                            deleteDialog.setMessage(R.string.delete_mc_question)
                                    .setTitle(R.string.delete_mc_title);
                            deleteDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //find multicounter and delete from list
                                    //first find multicounter in multicounterList and remove it
                                    Iterator<Multicounter> a = multicounterList.iterator();
                                    while (a.hasNext()) {
                                        Multicounter m = a.next();
                                        if(m.getName().equals(item))
                                        {
                                            a.remove();
                                            break;
                                        }
                                    }
                                    //save multiCounterList
                                    saveMultiCounterList();
                                    //remove name from multicounterNameList (list of strings)
                                    Iterator<String> b = multicounterNameList.iterator();
                                    while (b.hasNext()) {
                                        String s = b.next(); // must be called before you can call i.remove()
                                        if(s.equals(item))
                                        {
                                            b.remove();
                                            break;
                                        }
                                    }
                                    //save string list
                                    saveCounterList(multicounterNameList);

                                    multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                    adapter = new ArrayAdapter<String>(CounterListActivity.this, R.layout.mcounters_text_format, multicounterNamesArray);
                                    listView.setAdapter(adapter);

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
                        else if(position==3) //details button
                        {
                            alert.dismiss();
                            //find number of counters in the multicounter and store it in variable. Find the date the MC was created and store it in a variable
                            int numOfCounters=0;
                            String dateCreated="";
                            String dateModified="";
                            for(Multicounter mco: multicounterList)
                            {
                                if(mco.getName().equals(item))
                                {
                                    numOfCounters=mco.getCount();
                                    dateCreated=mco.getCreatedDateTime();
                                    dateModified=mco.getModifiedDateTime();
                                    break;
                                }
                            }

                            //create dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(CounterListActivity.this);
                            builder.setTitle(R.string.counter_details_title);
                            Context context = CounterListActivity.this; //store context in a variable
                            LinearLayout layout = new LinearLayout(context);
                            layout.setOrientation(LinearLayout.VERTICAL);

                            //formatting for displaying of info
                            Spanned nT; //name field text
                            Spanned cT; //counter count field text
                            Spanned dT; //date field text
                            Spanned mT; //date modifed text

                            //set bold field labels
                            String nameFirst = "<B>"+getResources().getString(R.string.counter_details_name)+"</B>";
                            String countFirst = "<B>"+getResources().getString(R.string.counter_details_number_counters)+"</B>";
                            String dateFirst = "<B>"+getResources().getString(R.string.created)+"</B>";
                            String modFirst = "<B>"+getResources().getString(R.string.last_modified)+"</B>";

                            if (Build.VERSION.SDK_INT >= 24) {
                                nT = Html.fromHtml(nameFirst +" "+ item, Html.FROM_HTML_MODE_LEGACY); // for 24 api and more
                                cT = Html.fromHtml(countFirst +" "+ numOfCounters, Html.FROM_HTML_MODE_LEGACY); // for 24 api and more
                                dT = Html.fromHtml(dateFirst +" "+ dateCreated, Html.FROM_HTML_MODE_LEGACY); // for 24 api and more
                                mT = Html.fromHtml(modFirst +" "+ dateModified, Html.FROM_HTML_MODE_LEGACY); // for 24 api and more
                            } else {
                                nT = Html.fromHtml(nameFirst +" "+ item);
                                cT = Html.fromHtml(countFirst +" "+ numOfCounters);
                                dT = Html.fromHtml(dateFirst +" "+ dateCreated);
                                mT = Html.fromHtml(modFirst +" "+ dateModified);
                            }

                            //textview Showing the counter's name
                            final TextView name = new TextView(context);
                            name.setText(nT);
                            name.setTextSize(16);
                            name.setTextColor(BLACK);
                            layout.addView(name);


                            //textview to create a space in between fields
                            final TextView space1 = new TextView(context);
                            space1.setText("");
                            space1.setTextSize(10);
                            layout.addView(space1);

                            //textview showing the number of counters in the multicounter
                            final TextView numCountTv = new TextView(context);
                            numCountTv.setText(cT);
                            numCountTv.setTextSize(16);
                            numCountTv.setTextColor(BLACK);
                            layout.addView(numCountTv);

                            //textview to create a space in between fields
                            final TextView space2 = new TextView(context);
                            space2.setText("");
                            space2.setTextSize(10);
                            layout.addView(space2);

                            //textview showing the date and time the counter was created
                            final TextView dateCreatedTv = new TextView(context);
                            dateCreatedTv.setText(dT);
                            dateCreatedTv.setTextSize(16);
                            dateCreatedTv.setTextColor(BLACK);
                            layout.addView(dateCreatedTv);

                            //textview to create a space in between fields
                            final TextView space3 = new TextView(context);
                            space3.setText("");
                            space3.setTextSize(10);
                            layout.addView(space3);

                            //textview showing the date and time the counter was created
                            final TextView dateModifiedTv = new TextView(context);
                            dateModifiedTv.setText(mT);
                            dateModifiedTv.setTextSize(16);
                            dateModifiedTv.setTextColor(BLACK);
                            layout.addView(dateModifiedTv);

                            //set layout margins and show
                            layout.setPadding(60, 50, 60, 10);
                            builder.setView(layout);
                            builder.create();

                            builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            builder.show();
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

                        if (mcName.isEmpty() || mcName.length() == 0 || mcName.equals("") || TextUtils.isEmpty(mcName)) {
                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.no_mcounter_name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            dialog.cancel();
                        } else if (inCounterList(mcName)) {
                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.mcounter_already_exists, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            dialog.cancel();
                        } else if (mcName.length() > 40) {
                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.mc_title_length_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            dialog.cancel();
                        } else {

                            //create new multicounter and add to list
                            multicounterList.add(new Multicounter(mcName, initCount));

                            saveMultiCounterList();

                            multicounterNameList.add(mcName);
                            saveCounterList(multicounterNameList);

                            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                            adapter = new ArrayAdapter<String>(CounterListActivity.this, R.layout.mcounters_text_format, multicounterNamesArray);
                            listView.setAdapter(adapter);
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

    public void saveMultiCounterList()
    {
        //save multicounter list
        SharedPreferences sharedPref = CounterListActivity.this.getSharedPreferences("MultiCounterList", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String jsonMC = gson.toJson(multicounterList);
        editor.putString("MultiCounterList", jsonMC);
        editor.commit();
    }

    //SEARCH FUNCTIONS


}
