package com.takezeroapps.countit;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
//import android.support.v4.app.Fragment;
import android.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;

import Exceptions.NoCountEnteredException;

import static android.graphics.Color.BLACK;

/**
 * Created by scoob on 1/7/2017.
 */
public class SingleCounterFragment extends Fragment{

    TextView counterName, counterCount;
    Button plusButton, minusButton;
    ImageButton resetButton, editCounterName, deleteCounter, arrowUp, arrowDown;
    String mcName, cName;
    int cCount, newNum;
    boolean isNegative, vibrateSetting, resetconfirmSetting, screenSetting;
    Counter currentSC;
    Multicounter currentMC;
    EditText counterEdit, input;
    LinearLayout lin;
    Counter tempCounter;
    SingleCounterFragment prevFrag, currFrag, nextFrag;
    String incdecQ; //string that stores whether the selected option is "increase" or "decrease" when selecting the corresponding option in the longClick menu of a counter
    public boolean increaseBool;

    public static SingleCounterFragment newInstance(String mcName, String cName, int cCount) {
        SingleCounterFragment myFragment = new SingleCounterFragment();
        Bundle args = new Bundle();
        args.putString("mcName", mcName);
        args.putString("cName", cName);
        args.putInt("cunt", cCount);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try
        {
            mcName=getArguments().getString("mcName");
            cName = getArguments().getString("cName");
            cCount = getArguments().getInt("cCount");
            counterName.setText(cName);
            counterCount.setText(Integer.toString(cCount));

            for(Multicounter m: CounterListActivity.multicounterList)
            {
                if(m.getName().equals(mcName))
                {
                    currentMC=m;
                    for(Counter c: m.counters)
                    {
                        if(c.getLabel().equals(cName))
                        {
                            currentSC=c;
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sc_counter_fragment, container, false);
        counterName=(TextView)view.findViewById(R.id.scounter_name);
        counterCount=(TextView)view.findViewById(R.id.scounter_count);
        plusButton=(Button)view.findViewById(R.id.scounter_plus);
        minusButton=(Button)view.findViewById(R.id.scounter_minus);
        resetButton=(ImageButton)view.findViewById(R.id.scounter_reset);
        editCounterName=(ImageButton)view.findViewById(R.id.edit_counter_name);
        deleteCounter=(ImageButton)view.findViewById(R.id.delete_counter);

        final String incdecOptions[] = {getActivity().getResources().getString(R.string.increase), getActivity().getResources().getString(R.string.decrease)};

        arrowUp=(ImageButton)view.findViewById(R.id.arrow_up);
        arrowDown=(ImageButton)view.findViewById(R.id.arrow_down);

        final Vibrator vib = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        final long[] pattern = {0, 20, 150, 20}; //double vibration pattern for errors

        //Addition Button
        plusButton.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        int currAddCount=getCount();
                        if((currAddCount + 1)>2147483646) //if next number would be higher than max print error
                        {
                            if(vibrateSetting)
                                vib.vibrate(pattern, -1);
                            Snackbar.make(v, R.string.max_num, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                        }
                        else //else add to count
                        {
                            if(vibrateSetting)
                                vib.vibrate(10);
                            addCount();
                            //Set modified times
                            currentMC.setModifiedDateTime();
                            currentMC.setModifiedTimeStamp();
                        }
                    }
                }
        );

        //Subtraction button
        minusButton.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        int currSubCount=getCount();
                        if((currSubCount - 1) < -2147483647) //if next number would be lower than min print error
                        {
                            if(vibrateSetting)
                                vib.vibrate(pattern, -1);
                            Snackbar.make(v, R.string.min_num, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                        }
                        else //else decrement
                        {
                            if(vibrateSetting)
                                vib.vibrate(10);
                            subCount();
                            //Set modified times
                            currentMC.setModifiedDateTime();
                            currentMC.setModifiedTimeStamp();
                        }
                    }
                }
        );
        resetButton.setOnClickListener(
                new ImageButton.OnClickListener(){
                    public void onClick(View v){
                        if(vibrateSetting)
                            vib.vibrate(10);
                        if(resetconfirmSetting) {
                            // Instantiate an AlertDialog.Builder with its constructor
                            AlertDialog.Builder resetDialog = new AlertDialog.Builder(getActivity());

                            // Set Dialog Title, message, and other properties
                            resetDialog.setMessage(R.string.reset_question)
                                    .setTitle(R.string.reset_title)
                            ; // semi-colon only goes after ALL of the properties

                            // Add the buttons
                            resetDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // reset count if "yes" is clicked
                                    resetCount();
                                    //Set modified times
                                    currentMC.setModifiedDateTime();
                                    currentMC.setModifiedTimeStamp();
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
                            resetCount();
                            //Set modified times
                            currentMC.setModifiedDateTime();
                            currentMC.setModifiedTimeStamp();
                        }
                    }
                }
        );


        counterCount.setOnLongClickListener(
                new ImageButton.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(final View view) {
                        final String item = (String) ((TextView) view).getText();
                        //create counterlist_dropdown_menu dialog
                        String names[] ={
                                getResources().getString(R.string.change_count),
                                "addby filler",
                                "color filler",
                                getResources().getString(R.string.rename_counter),
                                getResources().getString(R.string.delete_counter),
                        };
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View convertView = (View) inflater.inflate(R.layout.options_popup_list, null);
                        alertDialog.setView(convertView);
                        alertDialog.setTitle(R.string.counter_options_title);
                        ListView lv = (ListView) convertView.findViewById(R.id.listView1);
                        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,names);
                        lv.setAdapter(adapter1);
                        final AlertDialog alert = alertDialog.create();
                        alert.show();

                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View viewIn, int position, long id) {
                                if(position == 0) //change count
                                {
                                    alert.dismiss();
                                    final int currcount = getCount();

                                    final CharSequence[] negOptions = {getActivity().getResources().getString(R.string.make_negative_num)}; //choices to select from, only one choice so it only has one element
                                    final ArrayList selectedItems=new ArrayList();

                                    final AlertDialog.Builder counterChanger = new AlertDialog.Builder(getActivity());
                                    counterChanger.setTitle(R.string.change_count); //set title

                                    // Set up the input
                                    input = new EditText(getActivity());

                                    // Specify the type of input expected; this, for example, sets the input as a number, and will use the numpad
                                    input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                                    input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(10)});
                                    counterChanger.setView(input);

                                    // Set up the buttons
                                    counterChanger.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                String input_string = input.getText().toString().trim();

                                                if (input_string.isEmpty() || input_string.length() == 0 || input_string.equals("") || TextUtils.isEmpty(input_string)) //check if input is empty
                                                {
                                                    throw new NoCountEnteredException();
                                                } else //if string is not empty, convert to int
                                                {
                                                    newNum = Integer.valueOf(input.getText().toString());//get integer value of new number
                                                }

                                                if (isNegative) {
                                                    setCount(-1 * newNum); //if isNegative checkbox is checked, make the number negative
                                                }
                                                else {
                                                    setCount(newNum); //if checkbox is not checked, keep number the same
                                                }

                                                removeInputKeyboard();
                                            }
                                            catch (NoCountEnteredException e1)
                                            {
                                                if(vibrateSetting)
                                                    vib.vibrate(pattern, -1);
                                                Snackbar.make(view, R.string.no_input_message, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                                newNum = getCount(); //set new count back to old count (or else manually setting a real number > resetting count > entering blank input = count being the original real number instead of 0 after the reset)
                                                dialog.cancel();
                                                removeInputKeyboard();
                                            }
                                            catch (Exception e2)
                                            {
                                                if (vibrateSetting)
                                                    vib.vibrate(pattern, -1);
                                                Snackbar.make(view, R.string.invalid_number_entered, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                                dialog.cancel();

                                                removeInputKeyboard();
                                            }
                                        }
                                    });
                                    counterChanger.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel(); //cancel dialog and do not save changes when "cancel" button is clicked
                                        }
                                    });
                                    counterChanger.setMultiChoiceItems(negOptions, null, new DialogInterface.OnMultiChoiceClickListener() { //checkbox for negative number
                                        @Override
                                        public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                                            if (isChecked) {
                                                isNegative=true; //if checkbox is checked, set the boolean value for negative number as true
                                            }
                                            else
                                                isNegative=false; //otherwise if checkbox is not checked, then keep value positive
                                        }
                                    });

                                    counterChanger.setOnCancelListener(new DialogInterface.OnCancelListener() {

                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            //removes keyboard from screen when user clicks outside of dialog box so it is not stuck on the screen
                                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

                                        }
                                    });

                                    counterChanger.show(); //show dialog
                                    isNegative=false; //sets negative flag back to false after dialog is closed. This is so the input doesn't stay negative on each new change by the user

                                    input.requestFocus();
                                    InputMethodManager imm2 = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                }
                                else if(position==1) //increase/decrease by
                                {
                                    try {
                                        alert.dismiss();
                                        incdecQ = getActivity().getResources().getString(R.string.increase_by);
                                        //create dialog
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setTitle(R.string.inc_dec_by);
                                        Context context = getActivity(); //store context in a variable
                                        LinearLayout layout = new LinearLayout(context);
                                        layout.setOrientation(LinearLayout.VERTICAL);

                                        //textview telling user to select "increase or decrease"
                                        final TextView incdecText = new TextView(context);
                                        incdecText.setText(R.string.select);
                                        incdecText.setTextSize(16);
                                        incdecText.setTextColor(BLACK);
                                        layout.addView(incdecText);

                                        //dropdown menu with increase/decrease
                                        //dropdown for initial number of counters
                                        final ArrayAdapter<String> adp = new ArrayAdapter<String>(getActivity(),
                                                R.layout.spinner_item_custom, incdecOptions);
                                        final Spinner sp = new Spinner(getActivity());
                                        sp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                        sp.setAdapter(adp);
                                        layout.addView(sp);

                                        //textview to create a space in between fields
                                        final TextView space = new TextView(context);
                                        space.setText("");
                                        space.setTextSize(16);
                                        layout.addView(space);

                                        //textview telling user to enter amount
                                        final TextView amount = new TextView(context);
                                        amount.setText(incdecQ);
                                        amount.setTextSize(16);
                                        amount.setTextColor(BLACK);
                                        layout.addView(amount);

                                        //Text input for inc/dec amount
                                        final EditText incdecInput = new EditText(context);
                                        incdecInput.setHint(R.string.amount);
                                        incdecInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
                                        incdecInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                                        layout.addView(incdecInput);

                                        layout.setPadding(60, 50, 60, 70);
                                        builder.setView(layout);

                                        builder.create();

                                        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                                // your code here
                                                if (sp.getSelectedItem().toString().equals(getActivity().getResources().getString(R.string.increase))) {
                                                    incdecQ = getActivity().getResources().getString(R.string.increase_by);
                                                    increaseBool = true;

                                                } else if (sp.getSelectedItem().toString().equals(getActivity().getResources().getString(R.string.decrease))) {
                                                    incdecQ = getActivity().getResources().getString(R.string.decrease_by);
                                                    increaseBool = false;
                                                }

                                                amount.setText(incdecQ);
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parentView) {
                                                // your code here
                                            }

                                        });

                                        builder.show();

                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                                else if(position==2) //color
                                {
                                    alert.dismiss();
                                }
                                else if(position==3) //rename
                                {
                                    alert.dismiss();
                                    //create dialog
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setTitle(R.string.rename_single_counter);
                                    Context context = getActivity(); //store context in a variable
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
                                    counterEdit.setHint(cName);
                                    counterEdit.setFilters(new InputFilter[] {new InputFilter.LengthFilter(20)});
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
                                                    if(vibrateSetting)
                                                        vib.vibrate(pattern, -1);
                                                    Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), R.string.no_counter_name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                                    dialog.cancel();
                                                    removeCounterEditKeyboard();
                                                } else if (inSingleCounterList(counterName)) {
                                                    if(vibrateSetting)
                                                        vib.vibrate(pattern, -1);
                                                    Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), R.string.counter_already_exists, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                                    dialog.cancel();
                                                    removeCounterEditKeyboard();
                                                } else if (counterName.length() > 20) {
                                                    if(vibrateSetting)
                                                        vib.vibrate(pattern, -1);
                                                    Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), R.string.sc_title_length_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                                    dialog.cancel();
                                                    removeCounterEditKeyboard();
                                                } else {
                                                    //find current counter and set the name to the new name
                                                    setNewCounterName(counterName); //sets new countername in the counter object as well as the textview

                                                    //Set modified times
                                                    currentMC.setModifiedDateTime();
                                                    currentMC.setModifiedTimeStamp();

                                                    //save multicounter list
                                                    saveMultiCounterList();
                                                    removeCounterEditKeyboard();
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

                                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            //removes keyboard from screen when user clicks outside of dialog box so it is not stuck on the screen
                                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                                            imm.hideSoftInputFromWindow(counterEdit.getWindowToken(), 0);

                                        }
                                    });

                                    builder.show();

                                    counterEdit.requestFocus();
                                    InputMethodManager imm2 = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                }
                                else if(position==4) //delete
                                {
                                    try {
                                        alert.dismiss();
                                        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getActivity());

                                        deleteDialog.setMessage(R.string.delete_question)
                                                .setTitle(R.string.delete_title);
                                        deleteDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                // delete counter if "yes" is clicked
                                                deleteCounterFromMC();
                                                saveMultiCounterList();
                                                deleteFragment();

                                                //Set modified times
                                                currentMC.setModifiedDateTime();
                                                currentMC.setModifiedTimeStamp();
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
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                        return true;
                    }
                }
        );

        counterName.setOnLongClickListener(
                new ImageButton.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(final View view) {
                        counterCount.performLongClick();
                        return true;
                    }
                }
        );

        arrowUp.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){

                        for(Counter cn: currentMC.counters)
                        {
                            if(currentSC.getIndex()-1 < 0) //if index out of bounds
                            {
                                Toast.makeText(getActivity(), R.string.already_at_top, Toast.LENGTH_SHORT).show();
                                break;
                            }
                            else if(cn.getIndex() == (currentSC.getIndex()-1)) //if the index of the counter is the one before current
                            {
                                try {

                                    //prev counter info
                                    int prevIndex = cn.getIndex();
                                    String prevName = cn.getLabel();
                                    int prevCount = cn.getCount();
                                    String prevFragId = MultiCounterActivity.fragTagList.get(prevIndex);
                                    prevFrag = (SingleCounterFragment) getFragmentManager().findFragmentByTag(prevFragId);

                                    //curr counter info
                                    int currIndex = currentSC.getIndex();
                                    String currName = currentSC.getLabel();
                                    int currCount = currentSC.getCount();
                                    String currFragId = MultiCounterActivity.fragTagList.get(currIndex);
                                    currFrag = (SingleCounterFragment) getFragmentManager().findFragmentByTag(currFragId);

                                    //Collections.swap(MultiCounterActivity.fragTagList, prevIndex, currIndex);

                                    //set prev info to curr info
                                    currentSC.setLabel(prevName);
                                    currentSC.setCount(prevCount);
                                    currentSC.setIndex(currIndex);
                                    //set curr fragment
                                    currFrag.setCount(prevCount);
                                    currFrag.setNewCounterName(prevName);

                                    //copy prev counter info into curr counter variable
                                    cn.setLabel(currName);
                                    cn.setCount(currCount);
                                    cn.setIndex(prevIndex);
                                    //store fragment info
                                    prevFrag.setNewCounterName(currName);
                                    prevFrag.setCount(currCount);

                                    saveMultiCounterList();

                                    break;
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
        );

        arrowDown.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        for(Counter cn: currentMC.counters)
                        {
                            if(currentSC.getIndex()+1 > (currentMC.counters.size()-1)) //if index out of bounds
                            {
                                Toast.makeText(getActivity(), R.string.already_at_bottom, Toast.LENGTH_SHORT).show();
                                break;
                            }
                            else if(cn.getIndex() == (currentSC.getIndex()+1)) //if the index of the counter is the one after current
                            {
                                try {

                                    //prev counter info
                                    int nextIndex = cn.getIndex();
                                    String nextName = cn.getLabel();
                                    int nextCount = cn.getCount();
                                    String nextFragId = MultiCounterActivity.fragTagList.get(nextIndex);
                                    nextFrag = (SingleCounterFragment) getFragmentManager().findFragmentByTag(nextFragId);

                                    //curr counter info
                                    int currIndex = currentSC.getIndex();
                                    String currName = currentSC.getLabel();
                                    int currCount = currentSC.getCount();
                                    String currFragId = MultiCounterActivity.fragTagList.get(currIndex);
                                    currFrag = (SingleCounterFragment) getFragmentManager().findFragmentByTag(currFragId);

                                    //Collections.swap(MultiCounterActivity.fragTagList, prevIndex, currIndex);

                                    //set prev info to curr info
                                    currentSC.setLabel(nextName);
                                    currentSC.setCount(nextCount);
                                    currentSC.setIndex(currIndex);
                                    //set curr fragment
                                    currFrag.setCount(nextCount);
                                    currFrag.setNewCounterName(nextName);

                                    //copy prev counter info into curr counter variable
                                    cn.setLabel(currName);
                                    cn.setCount(currCount);
                                    cn.setIndex(nextIndex);
                                    //store fragment info
                                    nextFrag.setNewCounterName(currName);
                                    nextFrag.setCount(currCount);

                                    saveMultiCounterList();

                                    break;
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }

                                saveMultiCounterList();

                                break;
                            }
                        }
                    }
                }
        );

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        currentSC.setCount(currentSC.getCount());
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(counterEdit != null)
            imm.hideSoftInputFromWindow(counterEdit.getWindowToken(), 0);
        if(input != null)
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        //get saved settings from stored preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        vibrateSetting = prefs.getBoolean("switch_preference_vibrate", true);
        resetconfirmSetting = prefs.getBoolean("switch_preference_resetconfirm", true);
        screenSetting = prefs.getBoolean("switch_preference_screen", false);

        if(screenSetting)
        {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    public void setNewCounterName(String n)
    {
        currentSC.setLabel(n);
        counterName.setText(n);
        cName=n;
    }

    public boolean inSingleCounterList(String countN)
    {
        for(Counter c: currentMC.counters)
        {
            if(c.getLabel().equals(countN))
            {
                return true;
            }
        }

        return false;
    }

    public void deleteCounterFromMC()
    {
        MultiCounterActivity.fragTagList.remove(currentSC.getIndex());
        currentMC.deleteCounter(cName);
    }

    public void setCount(int num)
    {
        currentSC.setCount(num);
        counterCount.setText(Integer.toString(currentSC.getCount()));
        cCount=num;
    }

    public int getCount()
    {
        return Integer.parseInt(counterCount.getText().toString());
    }

    public void addCount()
    {
        //int num = Integer.valueOf(counterCount.getText().toString());
        //num++;
        currentSC.addCount();
        counterCount.setText(Integer.toString(currentSC.getCount()));;
        cCount=currentSC.getCount();

    }
    public void subCount()
    {
        //int num = Integer.valueOf(counterCount.getText().toString());
        //num--;
        currentSC.subCount();
        counterCount.setText(Integer.toString(currentSC.getCount()));
        cCount=currentSC.getCount();
    }

    public void resetCount()
    {
        setCount(0);
    }

    public void deleteFragment() //fragment self-destructs
    {
        getActivity().getFragmentManager().beginTransaction().remove(SingleCounterFragment.this).commit();
    }

    public void saveMultiCounterList()
    {
        //save multicounter list
        SharedPreferences sharedPref = getActivity().getSharedPreferences("MultiCounterList", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String jsonMC = gson.toJson(CounterListActivity.multicounterList);
        editor.putString("MultiCounterList", jsonMC);
        editor.commit();
    }

    public void removeCounterEditKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(counterEdit.getWindowToken(), 0);
    }

    public void removeInputKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

}
