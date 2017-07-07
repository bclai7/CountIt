package com.takezeroapps.countit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static android.graphics.Color.BLACK;
import static android.widget.AbsListView.CHOICE_MODE_NONE;
import static com.takezeroapps.countit.MulticounterComparator.CREATED_SORT;
import static com.takezeroapps.countit.MulticounterComparator.MODIFIED_SORT;
import static com.takezeroapps.countit.MulticounterComparator.NAME_SORT;
import static com.takezeroapps.countit.MulticounterComparator.decending;
import static com.takezeroapps.countit.MulticounterComparator.getComparator;

public class CounterListActivity extends AppCompatActivity {

    private ListView listView;
    private String[] multicounterNamesArray;
    public static ArrayList<String> multicounterNameList = new ArrayList<String>();
    public static ArrayList<Multicounter> multicounterList = new ArrayList<Multicounter>();
    TextView tx;
    String[] c = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"};
    public static final String MULTICOUNTER_NAME_KEY = "multicounter_name";
    MultiCounterListViewAdapter adapter;
    int searchListStartPos;
    EditText counterEdit;
    boolean vibrateSetting;
    boolean editMode=false;
    MenuItem searchIcon, optionsIcon;
    List<String> mcList;
    ArrayAdapter<String> adapterA;
    ActionMode mActionMode;
    MenuItem itm;
    String yesText, noText, selectCheck, deselectCheck, selectedTitle, sure_delete_1, sure_delete_2, confirmationTitle;
    long[] checkedItems;
    Vibrator vib;
    long[] pattern = new long[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter_list);

        yesText = getResources().getString(R.string.yes);
        noText = getResources().getString(R.string.no);
        selectCheck = getResources().getString(R.string.select_all);
        deselectCheck = getResources().getString(R.string.deselect_all);
        selectedTitle = getResources().getString(R.string.selected_title);
        sure_delete_1 = getResources().getString(R.string.sure_delete_1);
        sure_delete_2 = getResources().getString(R.string.sure_delete_2);
        confirmationTitle = getResources().getString(R.string.confirmation_title);

         vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
         pattern[0]=0;
         pattern[1]=20;
         pattern[2]=150;
         pattern[3]=20;
         //= {0, 20, 150, 20}; //double vibration pattern for errors

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
        mcList= Arrays.asList(multicounterNamesArray);
        adapter = new MultiCounterListViewAdapter(this, R.layout.mcounters_text_format, mcList);

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
                //create counterlist_dropdown_menu dialog
                String names[] ={getResources().getString(R.string.open_counter), getResources().getString(R.string.rename_counter), getResources().getString(R.string.delete_counter), getResources().getString(R.string.details_counter), };
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(CounterListActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View convertView = (View) inflater.inflate(R.layout.options_popup_list, null);
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
                            counterEdit = new EditText(context);
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
                                            if (vibrateSetting)
                                                vib.vibrate(pattern, -1);
                                            Snackbar.make(CounterListActivity.this.getWindow().getDecorView().getRootView(), R.string.no_mcounter_name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                            removeKeyboard();//remove keyboard from screen
                                        } else if (inCounterList(counterName)) {
                                            if (vibrateSetting)
                                                vib.vibrate(pattern, -1);
                                            Snackbar.make(CounterListActivity.this.getWindow().getDecorView().getRootView(), R.string.mcounter_already_exists, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                            removeKeyboard();//remove keyboard from screen
                                        } else if (counterName.length() > 40) {
                                            if (vibrateSetting)
                                                vib.vibrate(pattern, -1);
                                            Snackbar.make(CounterListActivity.this.getWindow().getDecorView().getRootView(), R.string.mc_title_length_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                            removeKeyboard();//remove keyboard from screen
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
                                            mcList= Arrays.asList(multicounterNamesArray);
                                            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                            listView.setAdapter(adapter);
                                            removeKeyboard();//remove keyboard from screen
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
                                    removeKeyboard();//remove keyboard from screen
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
                                    mcList= Arrays.asList(multicounterNamesArray);
                                    adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
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

        //TEST BEGIN
        if(multicounterList.isEmpty()) {
            for (int i = 1; i < 50; i++) {
                multicounterList.add(new Multicounter("mc" + i, 20));
                multicounterNameList.add(0, "mc" + i);
            }

            saveMultiCounterList();
            saveCounterList(multicounterNameList);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        vibrateSetting = prefs.getBoolean("switch_preference_vibrate", true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.multicounter_list_drawer, menu);
        MenuItem item = menu.findItem(R.id.multicounter_search);
        final SearchView searchView = (SearchView)item.getActionView();
        final MenuItem optionsIcon = menu.findItem(R.id.multicounterlist_options);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    adapter.getFilter().filter(newText);
                    return false;
                }catch(Exception e)
                {
                    e.printStackTrace();
                    Log.d("test", Log.getStackTraceString(new Exception()));
                }
                return false;
            }

        });

        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Log.d("test", "search opened");
                optionsIcon.setVisible(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.d("test", "search closed");
                optionsIcon.setVisible(true);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    // plus sign button (to add multi-counter)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.multicounterlist_options) { //options button (3 dots)

            //creates dropdown list when option button is clicked
            View menuItemView = findViewById(R.id.multicounterlist_options);
            PopupMenu popupMenu = new PopupMenu(CounterListActivity.this, menuItemView);
            popupMenu.getMenuInflater().inflate(R.menu.counterlist_dropdown_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.add_mc) {
                        if(multicounterNameList.size() + 1 > 100) //maximum number of multicounters set to 100
                        {
                            if (vibrateSetting)
                                vib.vibrate(pattern, -1);
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
                                        if (vibrateSetting)
                                            vib.vibrate(pattern, -1);
                                        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.no_mcounter_name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                        dialog.cancel();
                                    } else if (inCounterList(mcName)) {
                                        if (vibrateSetting)
                                            vib.vibrate(pattern, -1);
                                        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.mcounter_already_exists, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                        dialog.cancel();
                                    } else if (mcName.length() > 40) {
                                        if (vibrateSetting)
                                            vib.vibrate(pattern, -1);
                                        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.mc_title_length_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                        dialog.cancel();
                                    } else {

                                        //create new multicounter and add to list
                                        multicounterList.add(new Multicounter(mcName, initCount));

                                        saveMultiCounterList();

                                        multicounterNameList.add(0, mcName);
                                        saveCounterList(multicounterNameList);

                                        multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                        mcList= Arrays.asList(multicounterNamesArray);
                                        adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
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
                    if (item.getItemId() == R.id.sort_mc) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(CounterListActivity.this);
                        builder.setTitle(R.string.sort_by)
                                .setItems(R.array.sort_menu_options_array, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // The 'which' argument contains the index position
                                        // of the selected item

                                        ArrayList<String> newOrder = new ArrayList<String>(); //temp arraylist to hold new order of strings

                                        if(which == 0) //Name (Ascending)
                                        {
                                            Collections.sort(multicounterList, decending(getComparator(NAME_SORT, CREATED_SORT, MODIFIED_SORT)));
                                            Collections.reverse(multicounterList);
                                            for(Multicounter m: multicounterList)
                                            {
                                                newOrder.add(m.getName());
                                            }

                                            multicounterNameList=newOrder; //set string list to new sorted order
                                            saveCounterList(multicounterNameList); //save new ordered string list

                                            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                            mcList= Arrays.asList(multicounterNamesArray);
                                            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                            listView.setAdapter(adapter);
                                        }
                                        else if(which == 1) //Name (Descending)
                                        {
                                            Collections.sort(multicounterList, decending(getComparator(NAME_SORT, CREATED_SORT, MODIFIED_SORT)));
                                            for(Multicounter m: multicounterList)
                                            {
                                                newOrder.add(m.getName());
                                            }

                                            multicounterNameList=newOrder; //set string list to new sorted order
                                            saveCounterList(multicounterNameList); //save new ordered string list

                                            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                            mcList= Arrays.asList(multicounterNamesArray);
                                            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                            listView.setAdapter(adapter);
                                        }
                                        else if(which == 2) //Date Created (Recent First)
                                        {
                                            Collections.sort(multicounterList, decending(getComparator(CREATED_SORT, NAME_SORT, MODIFIED_SORT)));
                                            for(Multicounter m: multicounterList)
                                            {
                                                newOrder.add(m.getName());
                                            }

                                            multicounterNameList=newOrder; //set string list to new sorted order
                                            saveCounterList(multicounterNameList); //save new ordered string list

                                            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                            mcList= Arrays.asList(multicounterNamesArray);
                                            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                            listView.setAdapter(adapter);
                                        }
                                        else if(which == 3) //Date Created (Oldest First)
                                        {
                                            Collections.sort(multicounterList, decending(getComparator(CREATED_SORT, NAME_SORT, MODIFIED_SORT)));
                                            Collections.reverse(multicounterList);
                                            for(Multicounter m: multicounterList)
                                            {
                                                newOrder.add(m.getName());
                                            }

                                            multicounterNameList=newOrder; //set string list to new sorted order
                                            saveCounterList(multicounterNameList); //save new ordered string list

                                            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                            mcList= Arrays.asList(multicounterNamesArray);
                                            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                            listView.setAdapter(adapter);
                                        }
                                        else if(which == 4) //Date Modified (Recent First)
                                        {
                                            Collections.sort(multicounterList, decending(getComparator(MODIFIED_SORT, CREATED_SORT, NAME_SORT)));
                                            for(Multicounter m: multicounterList)
                                            {
                                                newOrder.add(m.getName());
                                            }

                                            multicounterNameList=newOrder; //set string list to new sorted order
                                            saveCounterList(multicounterNameList); //save new ordered string list

                                            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                            mcList= Arrays.asList(multicounterNamesArray);
                                            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                            listView.setAdapter(adapter);
                                        }
                                        else if(which == 5) //Date Modified (Oldest First)
                                        {
                                            Collections.sort(multicounterList, decending(getComparator(MODIFIED_SORT, CREATED_SORT, NAME_SORT)));
                                            Collections.reverse(multicounterList);
                                            for(Multicounter m: multicounterList)
                                            {
                                                newOrder.add(m.getName());
                                            }

                                            multicounterNameList=newOrder; //set string list to new sorted order
                                            saveCounterList(multicounterNameList); //save new ordered string list

                                            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                            mcList= Arrays.asList(multicounterNamesArray);
                                            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                            listView.setAdapter(adapter);
                                        }

                                    }
                                });
                        builder.create().show();
                    }
                    if(item.getItemId() == R.id.multiselect_mc)
                    {
                        mcList= Arrays.asList(multicounterNamesArray);
                        adapter = new MultiCounterListViewAdapter(CounterListActivity.this, android.R.layout.simple_list_item_multiple_choice, mcList);
                        //listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(null);
                        listView.setOnItemLongClickListener(null);

                        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                        //mActionMode = CounterListActivity.this.startActionMode(mActionModeCallback);

                        if (mActionMode != null) {
                            return false;
                        }
                        // Start the CAB using the ActionMode.Callback defined above
                        mActionMode = CounterListActivity.this.startActionMode(mActionModeCallback);
                    }

                    return false;
                }

            });
            popupMenu.show();

        }

        return super.onOptionsItemSelected(item);
    }
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {


        @Override
        public boolean  onPrepareActionMode(ActionMode mode, Menu menu) {
            // TODO  Auto-generated method stub
            adapter.removeSelection();
            listView.clearChoices();
            final int checkedCountA  = listView.getCheckedItemCount();
            mode.setTitle(checkedCountA  + " "+selectedTitle);

            return false;
        }

        @Override
        public void  onDestroyActionMode(ActionMode mode) {
            // TODO  Auto-generated method stub
            mActionMode = null;

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
                    //create counterlist_dropdown_menu dialog
                    String names[] ={getResources().getString(R.string.open_counter), getResources().getString(R.string.rename_counter), getResources().getString(R.string.delete_counter), getResources().getString(R.string.details_counter), };
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(CounterListActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View convertView = (View) inflater.inflate(R.layout.options_popup_list, null);
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
                                counterEdit = new EditText(context);
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
                                                if (vibrateSetting)
                                                    vib.vibrate(pattern, -1);
                                                Snackbar.make(CounterListActivity.this.getWindow().getDecorView().getRootView(), R.string.no_mcounter_name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                                dialog.cancel();
                                                removeKeyboard();//remove keyboard from screen
                                            } else if (inCounterList(counterName)) {
                                                if (vibrateSetting)
                                                    vib.vibrate(pattern, -1);
                                                Snackbar.make(CounterListActivity.this.getWindow().getDecorView().getRootView(), R.string.mcounter_already_exists, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                                dialog.cancel();
                                                removeKeyboard();//remove keyboard from screen
                                            } else if (counterName.length() > 40) {
                                                if (vibrateSetting)
                                                    vib.vibrate(pattern, -1);
                                                Snackbar.make(CounterListActivity.this.getWindow().getDecorView().getRootView(), R.string.mc_title_length_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                                dialog.cancel();
                                                removeKeyboard();//remove keyboard from screen
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
                                                mcList= Arrays.asList(multicounterNamesArray);
                                                adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                                listView.setAdapter(adapter);
                                                removeKeyboard();//remove keyboard from screen
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
                                        removeKeyboard();//remove keyboard from screen
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
                                        mcList= Arrays.asList(multicounterNamesArray);
                                        adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
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

            listView.setChoiceMode(CHOICE_MODE_NONE);
            adapter.removeSelection();
            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
            listView.setAdapter(adapter);
            listView.clearChoices();

        }

        @Override
        public boolean  onCreateActionMode(final ActionMode mode, final Menu menu) {
            // TODO  Auto-generated method stub
            mode.getMenuInflater().inflate(R.menu.contextual_action_bar, menu);

            itm = (MenuItem) menu.findItem(R.id.selectAll);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final int checkedCountA  = listView.getCheckedItemCount();
                    mode.setTitle(checkedCountA  + " "+selectedTitle);

                    if(listView.getCheckedItemCount() == mcList.size())
                    {
                        itm.setTitle(deselectCheck);
                    }
                    else if(listView.getCheckedItemCount() < mcList.size())
                    {
                        itm.setTitle(selectCheck);
                    }

                }
            });

            return true;

        }

        @Override
        public boolean  onActionItemClicked(final ActionMode mode,
                                            MenuItem item) {
            // TODO  Auto-generated method stub

            switch  (item.getItemId()) {
                case R.id.selectAll:
                    if(listView.getCheckedItemCount() == mcList.size()) //if all items are already selected, un-select them
                    {
                        adapter.removeSelection();
                        listView.clearChoices();
                        final int checkedCountA  = listView.getCheckedItemCount();
                        mode.setTitle(checkedCountA  + " "+selectedTitle);
                        itm.setTitle(selectCheck);
                    }
                    else {
                        //
                        final int checkedCount = mcList.size();
                        // If item  is already selected or checked then remove or
                        // unchecked  and again select all
                        adapter.removeSelection();
                        for (int i = 0; i < checkedCount; i++) {
                            listView.setItemChecked(i, true);
                            //  listviewadapter.toggleSelection(i);
                        }

                        checkedItems = listView.getCheckedItemIds();

                        // Count no.  of selected item and print it
                        mode.setTitle(checkedCount + " "+selectedTitle);
                        itm.setTitle(deselectCheck);
                    }
                    return true;
                case R.id.delete:
                    // Add  dialog for confirmation to delete selected item
                    // record.
                    AlertDialog.Builder  builder = new AlertDialog.Builder(
                            CounterListActivity.this);
                    builder.setMessage(sure_delete_1+" "+listView.getCheckedItemCount()+" "+sure_delete_2);

                    builder.setNegativeButton(noText, new  DialogInterface.OnClickListener() {

                        @Override
                        public void  onClick(DialogInterface dialog, int which) {
                            // TODO  Auto-generated method stub

                        }
                    });
                    builder.setPositiveButton(yesText, new  DialogInterface.OnClickListener() {

                        @Override
                        public void  onClick(DialogInterface dialog, int which) {
                            // TODO  Auto-generated method stub
                            SparseBooleanArray selected = adapter
                                    .getSelectedIds();
                            for (int i =  (selected.size() - 1); i >= 0; i--) {
                                if  (selected.valueAt(i)) {
                                    String  selecteditem = adapter
                                            .getItem(selected.keyAt(i));
                                    // Remove  selected items following the ids
                                    adapter.remove(selecteditem);
                                }
                            }

                            // Close CAB
                            mode.finish();
                            selected.clear();

                        }
                    });
                    AlertDialog alert =  builder.create();
                    alert.setIcon(R.drawable.ic_action_delete);// dialog  Icon
                    alert.setTitle(confirmationTitle); // dialog  Title
                    alert.show();
                    return true;
                default:
                    return false;
            }

        }

    };

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

    private void saveCounterList(ArrayList<String> counterList) { //saves multicounter NAME list to text file
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

    @Override
    protected void onPause() {
        super.onPause();
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

    public void removeKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(counterEdit.getWindowToken(), 0);
    }

}
