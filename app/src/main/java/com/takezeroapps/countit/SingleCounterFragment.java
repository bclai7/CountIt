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
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import static android.graphics.Color.BLACK;

/**
 * Created by scoob on 1/7/2017.
 */
public class SingleCounterFragment extends Fragment{

    TextView counterName, counterCount;
    Button plusButton, minusButton;
    ImageButton resetButton, editCounterName, deleteCounter;
    String mcName, cName;
    int cCount;
    boolean isNegative, vibrateSetting, resetconfirmSetting, screenSetting;

    public static SingleCounterFragment newInstance(String mcName, String cName, int cCount) {
        SingleCounterFragment myFragment = new SingleCounterFragment();
        Bundle args = new Bundle();
        args.putString("mcName", mcName);
        args.putString("cName", cName);
        args.putInt("cCount", cCount);
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
                        }
                    }
                }
        );

        editCounterName.setOnClickListener(
                new ImageButton.OnClickListener(){
                    public void onClick(View v){

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
                            final EditText counterEdit = new EditText(context);
                            counterEdit.setHint(cName);
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
                                            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), R.string.no_counter_name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                        } else if (inSingleCounterList(counterName)) {
                                            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), R.string.counter_already_exists, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                        } else if (counterName.length() > 20) {
                                            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), R.string.sc_title_length_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            dialog.cancel();
                                        } else {
                                            //find current counter and set the name to the new name
                                            setNewCounterName(counterName); //sets new countername in the counter object as well as the textview

                                            //save multicounter list
                                            saveMultiCounterList();
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
                }
        );

        deleteCounter.setOnClickListener(
                new ImageButton.OnClickListener(){
                    public void onClick(View v){
                        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getActivity());

                        deleteDialog.setMessage(R.string.delete_question)
                                .setTitle(R.string.delete_title);
                        deleteDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // delete counter if "yes" is clicked
                                deleteCounterFromMC();
                                saveMultiCounterList();
                                deleteFragment();
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
                }
        );

        counterCount.setOnLongClickListener(
                new ImageButton.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(final View view) {
                        final int currcount = getCount();

                        final CharSequence[] negOptions = {getActivity().getResources().getString(R.string.make_negative_num)}; //choices to select from, only one choice so it only has one element
                        final ArrayList selectedItems=new ArrayList();

                        final AlertDialog.Builder counterChanger = new AlertDialog.Builder(getActivity());
                        counterChanger.setTitle(R.string.change_count); //set title

                        // Set up the input
                        final EditText input = new EditText(getActivity());

                        // Specify the type of input expected; this, for example, sets the input as a number, and will use the numpad
                        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                        counterChanger.setView(input);

                        // Set up the buttons
                        counterChanger.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String input_string = input.getText().toString().trim();
                                int newNum;

                                if(input_string.isEmpty() || input_string.length() == 0 || input_string.equals("") || TextUtils.isEmpty(input_string)) //check if input is empty
                                {
                                    Snackbar.make(view, R.string.no_input_message, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                    newNum=getCount(); //set new count back to old count (or else manually setting a real number > resetting count > entering blank input = count being the original real number instead of 0 after the reset)
                                    dialog.cancel();
                                }
                                else //if string is not empty, convert to int
                                    newNum = Integer.valueOf(input.getText().toString());//get integer value of new number

                                if(newNum > 2147483646) //if entered is too high, print error and return to original number
                                {
                                    if(vibrateSetting)
                                        vib.vibrate(pattern, -1);
                                    Snackbar.make(view, R.string.too_high_message, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                    dialog.cancel();
                                }
                                else if(newNum < -2147483646) //if number entered is too low, print error and return to original number
                                {
                                    if(vibrateSetting)
                                        vib.vibrate(pattern, -1);
                                    Snackbar.make(view, R.string.too_high_message, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                    dialog.cancel();
                                }
                                else //else change number
                                {
                                    if(isNegative)
                                        setCount(-1*newNum); //if isNegative checkbox is checked, make the number negative
                                    else
                                        setCount(newNum); //if checkbox is not checked, keep number the same
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

                        counterChanger.show(); //show dialog
                        isNegative=false; //sets negative flag back to false after dialog is closed. This is so the input doesn't stay negative on each new change by the user

                        return true;
                    }
                }
        );




        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        for(Multicounter m: CounterListActivity.multicounterList)
        {
            if(m.getName().equals(mcName))
            {
                for(Counter c: m.counters)
                {
                    if(c.getLabel().equals(cName))
                    {
                        c.setCount(getCount());
                        break;
                    }
                }
            }
        }

        //save multicounter list
        saveMultiCounterList();
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
        for(Multicounter m: CounterListActivity.multicounterList)
        {
            if(m.getName().equals(mcName))
            {
                for(Counter c: m.counters)
                {
                    if(c.getLabel().equals(cName))
                    {
                        c.setLabel(n);
                        counterName.setText(n);
                        cName=n;
                        break;
                    }
                }
            }
        }
    }

    public boolean inSingleCounterList(String countN)
    {
        for(Multicounter m: CounterListActivity.multicounterList)
        {
            if(m.getName().equals(mcName))
            {
                for(Counter c: m.counters)
                {
                    if(c.getLabel().equals(countN))
                    {
                        return true;
                    }
                }
                break;
            }
        }
        return false;
    }

    public void deleteCounterFromMC()
    {
        for(Multicounter m: CounterListActivity.multicounterList)
        {
            if(m.getName().equals(mcName))
            {

                m.deleteCounter(cName);
                break;
            }
        }
    }

    public int getCount()
    {
        return Integer.parseInt(counterCount.getText().toString());
    }
    public void setCount(int num)
    {
        counterCount.setText(Integer.toString(num));
    }

    public void addCount()
    {
        int num = Integer.valueOf(counterCount.getText().toString());
        num++;
        counterCount.setText(Integer.toString(num));
    }
    public void subCount()
    {
        int num = Integer.valueOf(counterCount.getText().toString());
        num--;
        counterCount.setText(Integer.toString(num));
    }
    public void resetCount()
    {
        setCount(0);
    }

    public void setLabel(String name)
    {
        counterName.setText(name);
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

}
