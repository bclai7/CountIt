package com.deicoapps.countit;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionItemTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import static android.graphics.Color.BLACK;
import static android.widget.AbsListView.CHOICE_MODE_NONE;
import static com.deicoapps.countit.MulticounterComparator.CREATED_SORT;
import static com.deicoapps.countit.MulticounterComparator.MODIFIED_SORT;
import static com.deicoapps.countit.MulticounterComparator.NAME_SORT;
import static com.deicoapps.countit.MulticounterComparator.decending;
import static com.deicoapps.countit.MulticounterComparator.getComparator;

public class CounterListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private ListView listView;
    private String[] multicounterNamesArray;
    public static ArrayList<String> multicounterNameList = new ArrayList<String>();
    public static HashMap<String, Multicounter> multicounterList = new HashMap<String, Multicounter>();
    String[] c = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"};
    public static final String MULTICOUNTER_NAME_KEY = "multicounter_name";
    MultiCounterListViewAdapter adapter;
    EditText counterEdit;
    boolean vibrateSetting;
    List<String> mcList;
    ActionMode mActionMode;
    MenuItem itm;
    String yesText, noText, selectCheck, deselectCheck, selectedTitle, sure_delete_1, sure_delete_2, confirmationTitle;
    Vibrator vib;
    long[] pattern = new long[4];
    int sortOrder;
    int selectedO; //selected order
    EditText inputName;
    Toolbar toolbar;
    boolean tutorialComplete; //boolean storing whether or not user has completed the tutorial/tip for this particular activity
    boolean counterListVisited; //boolean tells whether or not its their first time visiting this activity (so if the multicounterlist is empty, it creates a default multicounter)
    ShowcaseView tut;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter_list);

        //get tutorial sharedpref
        SharedPreferences sharedPrefA = PreferenceManager.getDefaultSharedPreferences(this);
        tutorialComplete=sharedPrefA.getBoolean("CounterlistTutorial", false);

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

                            //Text input for multicounter counter name
                            counterEdit = new EditText(context);
                            counterEdit.setHint(item);
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
                                            //find reference to multicounter
                                            Multicounter tempM = multicounterList.get(item);
                                            //set data
                                            tempM.setName(counterName);
                                            tempM.setModifiedDateTime();
                                            tempM.setModifiedTimeStamp();
                                            //remove old MC from hashmap
                                            multicounterList.remove(item);
                                            //add updated MC to hashmap
                                            multicounterList.put(tempM.getName(), tempM);

                                            //save multicounter list
                                            saveMultiCounterList();

                                            ((TextView)view).setText(counterName);
                                            loadSortOrder();
                                            sortCounterList();
                                            removeKeyboard();//remove keyboard from screen

                                            int currentOne=0;

                                            for(int h=0; h<multicounterList.size(); h++)
                                            {
                                                if(listView.getItemAtPosition(h).equals(counterName))
                                                {
                                                    currentOne=h;
                                                    break;
                                                }
                                            }

                                            //scroll to new item
                                            //listView.setSelection(currentOne);
                                            final int scrollLocation = currentOne;
                                            listView.post(new Runnable() {
                                                @Override
                                                public void run() {

                                                    //listView.smoothScrollToPosition(scrollLocation);
                                                    listView.smoothScrollToPositionFromTop(scrollLocation,0);
                                                }
                                            });
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
                                    multicounterList.remove(item);

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

                            Multicounter tempM = multicounterList.get(item);
                            numOfCounters=tempM.getCount();
                            dateCreated=tempM.getCreatedDateTime();
                            dateModified=tempM.getModifiedDateTime();

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

        //Top appbar with options, do not remove
        toolbar = (Toolbar) findViewById(R.id.toolbar_counterlist);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_counterlist);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        if(!tutorialComplete) {
            showcaseDialogTutorial();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED); //lock orientation so tutorial glitch in landscape
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        File f = new File("/data/data/com.deicoapps.countit/shared_prefs/MultiCounterList.xml");
        if (f.exists())
        {
            SharedPreferences pref = getSharedPreferences("MultiCounterList", Context.MODE_PRIVATE);
            SharedPreferences.Editor e = pref.edit();
            String jsonMC = pref.getString("MultiCounterList", null);
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, Multicounter>>(){}.getType();
            multicounterList = gson.fromJson(jsonMC, type);
        }

        SharedPreferences sharedPrefA = PreferenceManager.getDefaultSharedPreferences(this);
        counterListVisited=sharedPrefA.getBoolean("CounterListVisited", false);

        //TEST BEGIN
        if(multicounterList.isEmpty() && !counterListVisited) {
            multicounterList.put("Sample Multicounter", new Multicounter("Sample Multicounter", 4));
            multicounterNameList.add(0, "Sample Multicounter");

            saveMultiCounterList();
            saveCounterList(multicounterNameList);

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            counterListVisited=true;
            editor.putBoolean("CounterListVisited", counterListVisited);
            editor.commit();
        }

        //get settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        vibrateSetting = sharedPref.getBoolean(SettingsActivity.vibrateKey, true);

        //sort list
        loadSortOrder();
        sortCounterList();

        //highlight multicounter button in drawer
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_counterlist);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_multicounter);

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
                }
                return false;
            }

        });

        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                optionsIcon.setVisible(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_counterlist);

            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                Log.d("test", "isOpen");
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else {
                Log.d("test", "isClosed");
                finish();
                startActivity(new Intent(CounterListActivity.this, MainActivity.class));
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);

        //return true;
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

                            //Text input for multicounter name
                            inputName = new EditText(context);
                            inputName.setHint(R.string.name_hint);
                            inputName.setFilters(new InputFilter[] {new InputFilter.LengthFilter(40)});
                            layout.addView(inputName);
                            //code below sets it so user cannot enter more than 1 line (the "return" button on the keyboard now turns into the "done" button)
                            inputName.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                                @Override
                                public void onFocusChange(View v, boolean hasFocus) {
                                    // TODO Auto-generated method stub
                                    if (hasFocus) {
                                        inputName.setSingleLine(true);
                                        inputName.setMaxLines(1);
                                        inputName.setLines(1);
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

                                    String mcName = inputName.getText().toString(); //input text - the user defined multi-counter name
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
                                        multicounterList.put(mcName, new Multicounter(mcName, initCount));

                                        saveMultiCounterList();

                                        multicounterNameList.add(0, mcName);
                                        saveCounterList(multicounterNameList);

                                        multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                        mcList= Arrays.asList(multicounterNamesArray);
                                        adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                        listView.setAdapter(adapter);

                                        sortCounterList();

                                        int currentOne=0;

                                        for(int h=0; h<multicounterList.size(); h++)
                                        {
                                            if(listView.getItemAtPosition(h).equals(mcName))
                                            {
                                                currentOne=h;
                                                break;
                                            }
                                        }

                                        //scroll to new item
                                        //listView.setSelection(currentOne);
                                        final int scrollLocation = currentOne;
                                        listView.post(new Runnable() {
                                            @Override
                                            public void run() {

                                                //listView.smoothScrollToPosition(scrollLocation);
                                                listView.smoothScrollToPositionFromTop(scrollLocation,0);
                                            }
                                        });

                                        //cancel keyboard
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(inputName.getWindowToken(), 0);
                                    }

                                }
                            });
                            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    //cancel keyboard
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(inputName.getWindowToken(), 0);
                                }
                            });
                            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    //removes keyboard from screen when user clicks outside of dialog box so it is not stuck on the screen
                                    InputMethodManager inputmm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    inputmm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                                    inputmm.hideSoftInputFromWindow(inputName.getWindowToken(), 0);

                                }
                            });

                            builder.show();

                            inputName.requestFocus();
                            InputMethodManager inputmm = (InputMethodManager) CounterListActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputmm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        }

                    }
                    if (item.getItemId() == R.id.sort_mc) {

                        loadSortOrder();
                        AlertDialog.Builder builder = new AlertDialog.Builder(CounterListActivity.this);

                        // Set the dialog title
                        builder.setTitle(R.string.sort_by)
                                // Specify the list array, the items to be selected by default (null for none),
                                // and the listener through which to receive callbacks when items are selected
                                .setSingleChoiceItems(R.array.sort_menu_options_array, sortOrder,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //Log.d("test", "selection "+which+" was clicked");
                                                selectedO=which;
                                            }

                                        })
                                // Set the action buttons
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User clicked OK, so save the mSelectedItems results somewhere
                                        // or return them to the component that opened the dialog
                                        //Log.d("test", "selection "+viewOption+" was accepted");
                                        //convert HashMap to ArrayList
                                        //Getting Collection of values from HashMap
                                        Collection<Multicounter> values = multicounterList.values();
                                        //Creating an ArrayList of values
                                        ArrayList<Multicounter> mcArrayList = new ArrayList<Multicounter>(values);

                                        ArrayList<String> newOrder = new ArrayList<String>(); //temp arraylist to hold new order of strings

                                        if(selectedO == 0) //Name (Ascending)
                                        {
                                            Collections.sort(mcArrayList, decending(getComparator(NAME_SORT, CREATED_SORT, MODIFIED_SORT)));
                                            Collections.reverse(mcArrayList);
                                            for(Multicounter m: mcArrayList)
                                            {
                                                newOrder.add(m.getName());
                                            }

                                            multicounterNameList=newOrder; //set string list to new sorted order
                                            saveCounterList(multicounterNameList); //save new ordered string list

                                            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                            mcList= Arrays.asList(multicounterNamesArray);
                                            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                            listView.setAdapter(adapter);
                                            saveSortOrder(0);
                                        }
                                        else if(selectedO == 1) //Name (Descending)
                                        {
                                            Collections.sort(mcArrayList, decending(getComparator(NAME_SORT, CREATED_SORT, MODIFIED_SORT)));
                                            for(Multicounter m: mcArrayList)
                                            {
                                                newOrder.add(m.getName());
                                            }

                                            multicounterNameList=newOrder; //set string list to new sorted order
                                            saveCounterList(multicounterNameList); //save new ordered string list

                                            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                            mcList= Arrays.asList(multicounterNamesArray);
                                            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                            listView.setAdapter(adapter);
                                            saveSortOrder(1);
                                        }
                                        else if(selectedO == 2) //Date Created (Recent First)
                                        {
                                            Collections.sort(mcArrayList, decending(getComparator(CREATED_SORT, NAME_SORT, MODIFIED_SORT)));
                                            for(Multicounter m: mcArrayList)
                                            {
                                                newOrder.add(m.getName());
                                            }

                                            multicounterNameList=newOrder; //set string list to new sorted order
                                            saveCounterList(multicounterNameList); //save new ordered string list

                                            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                            mcList= Arrays.asList(multicounterNamesArray);
                                            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                            listView.setAdapter(adapter);
                                            saveSortOrder(2);
                                        }
                                        else if(selectedO == 3) //Date Created (Oldest First)
                                        {
                                            Collections.sort(mcArrayList, decending(getComparator(CREATED_SORT, NAME_SORT, MODIFIED_SORT)));
                                            Collections.reverse(mcArrayList);
                                            for(Multicounter m: mcArrayList)
                                            {
                                                newOrder.add(m.getName());
                                            }

                                            multicounterNameList=newOrder; //set string list to new sorted order
                                            saveCounterList(multicounterNameList); //save new ordered string list

                                            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                            mcList= Arrays.asList(multicounterNamesArray);
                                            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                            listView.setAdapter(adapter);
                                            saveSortOrder(3);
                                        }
                                        else if(selectedO == 4) //Date Modified (Recent First)
                                        {
                                            Collections.sort(mcArrayList, decending(getComparator(MODIFIED_SORT, CREATED_SORT, NAME_SORT)));
                                            for(Multicounter m: mcArrayList)
                                            {
                                                newOrder.add(m.getName());
                                            }

                                            multicounterNameList=newOrder; //set string list to new sorted order
                                            saveCounterList(multicounterNameList); //save new ordered string list

                                            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                            mcList= Arrays.asList(multicounterNamesArray);
                                            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                            listView.setAdapter(adapter);
                                            saveSortOrder(4);
                                        }
                                        else if(selectedO == 5) //Date Modified (Oldest First)
                                        {
                                            Collections.sort(mcArrayList, decending(getComparator(MODIFIED_SORT, CREATED_SORT, NAME_SORT)));
                                            Collections.reverse(mcArrayList);
                                            for(Multicounter m: mcArrayList)
                                            {
                                                newOrder.add(m.getName());
                                            }

                                            multicounterNameList=newOrder; //set string list to new sorted order
                                            saveCounterList(multicounterNameList); //save new ordered string list

                                            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                            mcList= Arrays.asList(multicounterNamesArray);
                                            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                            listView.setAdapter(adapter);
                                            saveSortOrder(5);
                                        }

                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });

                        builder.create().show();
                    }
                    if(item.getItemId() == R.id.multiselect_mc)
                    {
                        toolbar.setVisibility(View.GONE);
                        mcList= Arrays.asList(multicounterNamesArray);
                        adapter = new MultiCounterListViewAdapter(CounterListActivity.this, android.R.layout.simple_list_item_multiple_choice, mcList);

                        listView.setAdapter(adapter);
                        listView.setClickable(false);
                        listView.setLongClickable(false);
                        //listView.setOnItemClickListener(null);
                        //listView.setOnItemLongClickListener(null);

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
        if (id == R.id.multicounter_search)
        {

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
            findViewById(R.id.action_mode_bar).setVisibility(View.GONE); //makes it so theres no animation when closing action bar, prevents it from looking weird
            toolbar.setVisibility(View.VISIBLE);

            //LISTENER for each item in ListView (Each multicounter)
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        String selected = (String) (listView.getItemAtPosition(position));
                        Bundle bundle = new Bundle();
                        bundle.putString(MULTICOUNTER_NAME_KEY, selected);
                        Intent intent = new Intent(CounterListActivity.this, MultiCounterActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });

            listView.setClickable(true);
            listView.setLongClickable(true);

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
                            SparseBooleanArray checkedItemPositions = listView.getCheckedItemPositions();

                            if(listView.getCheckedItemCount() == listView.getCount())
                            {
                                //if the number of checked items equal the size of the list, just clear the list instead of iterating
                                multicounterList.clear();
                                saveMultiCounterList();
                                //same with name list
                                multicounterNameList.clear();
                                saveCounterList(multicounterNameList);

                                //set adapter accordingly
                                multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                mcList= Arrays.asList(multicounterNamesArray);
                                adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                listView.setAdapter(adapter);

                            }
                            else {
                                for (int i = (checkedItemPositions.size() - 1); i >= 0; i--) {
                                    if (checkedItemPositions.valueAt(i)) {
                                        String selecteditem = adapter.getItem(checkedItemPositions.keyAt(i));

                                        multicounterList.remove(selecteditem);

                                        //save multiCounterList
                                        saveMultiCounterList();
                                        //remove name from multicounterNameList (list of strings)
                                        Iterator<String> b = multicounterNameList.iterator();
                                        while (b.hasNext()) {
                                            String s = b.next(); // must be called before you can call i.remove()
                                            if (s.equals(selecteditem)) {
                                                b.remove();
                                                break;
                                            }
                                        }
                                        //save string list
                                        saveCounterList(multicounterNameList);

                                        multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
                                        mcList = Arrays.asList(multicounterNamesArray);
                                        adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
                                        listView.setAdapter(adapter);
                                    }
                                }
                            }
                            // Close CAB
                            mode.finish();
                            checkedItemPositions.clear();

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Go to home/main activity
            finish();
            startActivity(new Intent(CounterListActivity.this, MainActivity.class));

        } else if (id == R.id.nav_multicounter) {
            //go to multicounter

        } else if (id == R.id.nav_settings) {
            //go to settings
            finish();
            startActivity(new Intent(CounterListActivity.this, SettingsActivity.class));

        } else if (id == R.id.nav_share) {
            //let users share app
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String link = "https://goo.gl/TKXVxf"; //app link
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_body) +"\n"+ link);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_title)));

        } else if (id == R.id.nav_rate) {
            //go to app page in google store
            AlertDialog.Builder resetDialog = new AlertDialog.Builder(CounterListActivity.this);

            // Set Dialog Title, message, and other properties
            resetDialog.setMessage(R.string.rate_question)
                    .setTitle(R.string.rate)
            ; // semi-colon only goes after ALL of the properties

            // Add the buttons
            resetDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //go to app page in google store
                    Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    // To count with Play market backstack, After pressing back button,
                    // to taken back to our application, we need to add following flags to intent.
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    try {
                        startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                    }
                }
            });
            resetDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            // Get the AlertDialog from create()
            AlertDialog dialog = resetDialog.create();

            //show dialog when reset button is clicked
            resetDialog.show();

        } else if (id == R.id.nav_contact) {
            //let users contact through email
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("*/*");
            i.putExtra(Intent.EXTRA_EMAIL, new String[] {
                    getString(R.string.email)
            });
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.countit_email_subject));
            startActivity(createEmailOnlyChooserIntent(i, getString(R.string.send_via_email)));
        }
        else if (id == R.id.nav_more) {
            //open link to developer page with the rest of my apps
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/developer?id=Deico+Apps"));
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_counterlist);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public Intent createEmailOnlyChooserIntent(Intent source,
                                               CharSequence chooserTitle) {
        Stack<Intent> intents = new Stack<Intent>();
        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
                "info@domain.com", null));
        List<ResolveInfo> activities = getPackageManager()
                .queryIntentActivities(i, 0);

        for(ResolveInfo ri : activities) {
            Intent target = new Intent(source);
            target.setPackage(ri.activityInfo.packageName);
            intents.add(target);
        }

        if(!intents.isEmpty()) {
            Intent chooserIntent = Intent.createChooser(intents.remove(0),
                    chooserTitle);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    intents.toArray(new Parcelable[intents.size()]));

            return chooserIntent;
        } else {
            return Intent.createChooser(source, chooserTitle);
        }
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
        InputMethodManager inputmm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(inputName != null)
            inputmm.hideSoftInputFromWindow(inputName.getWindowToken(), 0);
    }

    public void removeKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(counterEdit.getWindowToken(), 0);
    }

    public void saveSortOrder(int sortNumber) //saves order of list in sharedpref
    {
        sortOrder=sortNumber;
        SharedPreferences sharedPref = CounterListActivity.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("SortOrder", sortNumber);
        editor.commit();
    }

    public void loadSortOrder()
    {
        SharedPreferences sharedPref = CounterListActivity.this.getPreferences(Context.MODE_PRIVATE);
        sortOrder = sharedPref.getInt("SortOrder", 2);
    }

    public void sortCounterList()
    {
        //convert HashMap to ArrayList
        //Getting Collection of values from HashMap
        Collection<Multicounter> values = multicounterList.values();
        //Creating an ArrayList of values
        ArrayList<Multicounter> mcArrayList = new ArrayList<Multicounter>(values);

        ArrayList<String> newOrder = new ArrayList<String>(); //temp arraylist to hold new order of strings

        if(sortOrder == 0) //Name (Ascending)
        {
            Collections.sort(mcArrayList, decending(getComparator(NAME_SORT, CREATED_SORT, MODIFIED_SORT)));
            Collections.reverse(mcArrayList);
            for(Multicounter m: mcArrayList)
            {
                newOrder.add(m.getName());
            }

            multicounterNameList=newOrder; //set string list to new sorted order
            saveCounterList(multicounterNameList); //save new ordered string list

            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
            mcList= Arrays.asList(multicounterNamesArray);
            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
            listView.setAdapter(adapter);
            saveSortOrder(0);
        }
        else if(sortOrder == 1) //Name (Descending)
        {
            Collections.sort(mcArrayList, decending(getComparator(NAME_SORT, CREATED_SORT, MODIFIED_SORT)));
            for(Multicounter m: mcArrayList)
            {
                newOrder.add(m.getName());
            }

            multicounterNameList=newOrder; //set string list to new sorted order
            saveCounterList(multicounterNameList); //save new ordered string list

            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
            mcList= Arrays.asList(multicounterNamesArray);
            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
            listView.setAdapter(adapter);
            saveSortOrder(1);
        }
        else if(sortOrder == 2) //Date Created (Recent First)
        {
            Collections.sort(mcArrayList, decending(getComparator(CREATED_SORT, NAME_SORT, MODIFIED_SORT)));
            for(Multicounter m: mcArrayList)
            {
                newOrder.add(m.getName());
            }

            multicounterNameList=newOrder; //set string list to new sorted order
            saveCounterList(multicounterNameList); //save new ordered string list

            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
            mcList= Arrays.asList(multicounterNamesArray);
            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
            listView.setAdapter(adapter);
            saveSortOrder(2);
        }
        else if(sortOrder == 3) //Date Created (Oldest First)
        {
            Collections.sort(mcArrayList, decending(getComparator(CREATED_SORT, NAME_SORT, MODIFIED_SORT)));
            Collections.reverse(mcArrayList);
            for(Multicounter m: mcArrayList)
            {
                newOrder.add(m.getName());
            }

            multicounterNameList=newOrder; //set string list to new sorted order
            saveCounterList(multicounterNameList); //save new ordered string list

            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
            mcList= Arrays.asList(multicounterNamesArray);
            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
            listView.setAdapter(adapter);
            saveSortOrder(3);
        }
        else if(sortOrder == 4) //Date Modified (Recent First)
        {
            Collections.sort(mcArrayList, decending(getComparator(MODIFIED_SORT, CREATED_SORT, NAME_SORT)));
            for(Multicounter m: mcArrayList)
            {
                newOrder.add(m.getName());
            }

            multicounterNameList=newOrder; //set string list to new sorted order
            saveCounterList(multicounterNameList); //save new ordered string list

            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
            mcList= Arrays.asList(multicounterNamesArray);
            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
            listView.setAdapter(adapter);
            saveSortOrder(4);
        }
        else if(sortOrder == 5) //Date Modified (Oldest First)
        {
            Collections.sort(mcArrayList, decending(getComparator(MODIFIED_SORT, CREATED_SORT, NAME_SORT)));
            Collections.reverse(mcArrayList);
            for(Multicounter m: mcArrayList)
            {
                newOrder.add(m.getName());
            }

            multicounterNameList=newOrder; //set string list to new sorted order
            saveCounterList(multicounterNameList); //save new ordered string list

            multicounterNamesArray = multicounterNameList.toArray(new String[multicounterNameList.size()]);
            mcList= Arrays.asList(multicounterNamesArray);
            adapter = new MultiCounterListViewAdapter(CounterListActivity.this, R.layout.mcounters_text_format, mcList);
            listView.setAdapter(adapter);
            saveSortOrder(5);
        }
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
                .setContentTitle(getString(R.string.tutorial_counterlist_title))
                .setContentText(getString(R.string.tutorial_counterlist_text))
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
                        tut.setContentTitle(getString(R.string.tutorial_search_title));
                        tut.setContentText(getString(R.string.tutorial_search_text));
                        tut.setButtonText(getString(R.string.done));
                        break;
                    case 2:
                        tut.hide();
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR); //unlock orientation
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(CounterListActivity.this);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("CounterlistTutorial", true);
                        editor.commit();
                        break;

                }
            }
        });
    }

}
