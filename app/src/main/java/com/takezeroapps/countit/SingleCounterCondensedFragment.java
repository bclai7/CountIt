package com.takezeroapps.countit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import Exceptions.NoCountEnteredException;

import static android.graphics.Color.BLACK;


public class SingleCounterCondensedFragment extends Fragment {

    TextView counterName, counterCount;
    Button plusButton, minusButton;
    String mcName, cName;
    int cCount, newNum;
    boolean isNegative, vibrateSetting, resetconfirmSetting, screenSetting;
    Counter currentSC;
    Multicounter currentMC;
    EditText counterEdit, input, incdecInput;
    SingleCounterCondensedFragment currFrag, prevFrag, nextFrag;
    String incdecQ;

    public static SingleCounterCondensedFragment newInstance(String mcName, String cName, int cCount) {
        SingleCounterCondensedFragment myFragment = new SingleCounterCondensedFragment();
        Bundle args = new Bundle();
        args.putString("mcName", mcName);
        args.putString("cName", cName);
        args.putInt("cCount", cCount);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.sc_counter_fragment_condensed, container, false);
        View view = inflater.inflate(R.layout.sc_counter_fragment_condensed, container, false);
        counterName=(TextView)view.findViewById(R.id.scounter_name);
        counterCount=(TextView)view.findViewById(R.id.scounter_count);
        plusButton=(Button)view.findViewById(R.id.scounter_plus);
        minusButton=(Button)view.findViewById(R.id.scounter_minus);

        final String incdecOptions[] = {getActivity().getResources().getString(R.string.increase), getActivity().getResources().getString(R.string.decrease)};

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

        counterCount.setOnLongClickListener(
            new ImageButton.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    final String item = (String) ((TextView) view).getText();
                    //create counterlist_dropdown_menu dialog
                    //option list
                    String names[] ={
                            getResources().getString(R.string.change_count),
                            getResources().getString(R.string.inc_dec_by),
                            getResources().getString(R.string.change_color),
                            getResources().getString(R.string.reset_title),
                            getResources().getString(R.string.rename_counter),
                            getResources().getString(R.string.delete_counter),
                            getResources().getString(R.string.counter_up),
                            getResources().getString(R.string.counter_down)
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
                                Log.d("test", "Option "+position);
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
                                alert.dismiss();
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
                                    final RadioGroup rg = new RadioGroup(getActivity());
                                    rg.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                    for(int p=0; p<2; p++)
                                    {
                                        RadioButton rb = new RadioButton(getActivity());
                                        rb.setText(incdecOptions[p]);
                                        rb.setTextSize(16);
                                        rb.setId(p);
                                        rg.addView(rb);
                                    }

                                    layout.addView(rg);

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
                                    incdecInput = new EditText(context);
                                    incdecInput.setHint(R.string.amount);
                                    incdecInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
                                    incdecInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                                    layout.addView(incdecInput);

                                    layout.setPadding(60, 50, 60, 30);
                                    builder.setView(layout);

                                    builder.create();

                                    rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
                                    {
                                        @Override
                                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                                            // checkedId is the RadioButton selected
                                            View radioButton = rg.findViewById(checkedId);
                                            int index = rg.indexOfChild(radioButton);

                                            if(rg.getCheckedRadioButtonId() == 0)
                                            {
                                                incdecQ = getActivity().getResources().getString(R.string.increase_by);
                                            }
                                            else if(rg.getCheckedRadioButtonId() == 1)
                                            {
                                                incdecQ = getActivity().getResources().getString(R.string.decrease_by);
                                            }
                                            amount.setText(incdecQ);
                                        }
                                    });

                                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            int incdecAmount = 0;
                                            try {
                                                //increase/decrease by required amount
                                                String input_string = incdecText.getText().toString().trim();

                                                if (input_string.isEmpty() || input_string.length() == 0 || input_string.equals("") || TextUtils.isEmpty(input_string)) //check if input is empty
                                                {
                                                    throw new NoCountEnteredException();
                                                }
                                                else //if string is not empty, convert to int
                                                {
                                                    incdecAmount = Integer.parseInt(incdecInput.getText().toString()); //the amount to be decrement/incremented by
                                                }

                                                if (rg.getCheckedRadioButtonId() == 0) //increase
                                                {
                                                    //INCREASE by
                                                    int currCount=getCount();
                                                    if(vibrateSetting)
                                                        vib.vibrate(10);
                                                    increaseCount(incdecAmount);
                                                    //Set modified times
                                                    currentMC.setModifiedDateTime();
                                                    currentMC.setModifiedTimeStamp();

                                                }
                                                else if (rg.getCheckedRadioButtonId() == 1) //decrease
                                                {
                                                    //DECREASE by
                                                    int currCount=getCount();
                                                    if(vibrateSetting)
                                                        vib.vibrate(10);
                                                    decreaseCount(incdecAmount);
                                                    //Set modified times
                                                    currentMC.setModifiedDateTime();
                                                    currentMC.setModifiedTimeStamp();

                                                }

                                                //remove keyboard
                                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                                imm.hideSoftInputFromWindow(incdecInput.getWindowToken(), 0);
                                            }
                                            catch (NoCountEnteredException e1)
                                            {
                                                if(vibrateSetting)
                                                    vib.vibrate(pattern, -1);
                                                Snackbar.make(view, R.string.no_input_message, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                                dialog.cancel();
                                            }
                                            catch (Exception e2)
                                            {
                                                if (vibrateSetting)
                                                    vib.vibrate(pattern, -1);
                                                Snackbar.make(view, R.string.invalid_number_entered, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                                dialog.cancel();

                                                //remove keyboard
                                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                                imm.hideSoftInputFromWindow(incdecInput.getWindowToken(), 0);
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
                                            imm.hideSoftInputFromWindow(incdecInput.getWindowToken(), 0);

                                        }
                                    });

                                    builder.show();
                                    rg.check(rg.getChildAt(0).getId());
                                    incdecInput.requestFocus();
                                    InputMethodManager imm2 = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }

                            }
                            else if(position==2) //change color
                            {
                                alert.dismiss();
                                //SingleCounterFragment.this.getView().setBackgroundColor(Color.RED);
                                final String item = (String) ((TextView) view).getText();
                                //create counterlist_dropdown_menu dialog
                                final AlertDialog.Builder alertDialogB = new AlertDialog.Builder(getActivity());
                                LayoutInflater inflater = getActivity().getLayoutInflater();
                                View convertView = (View) inflater.inflate(R.layout.options_popup_list, null);
                                alertDialogB.setView(convertView);
                                alertDialogB.setTitle(getResources().getString(R.string.colors_title));
                                ListView lv = (ListView) convertView.findViewById(R.id.listView1);
                                ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.colors_array));
                                lv.setAdapter(adapter1);
                                final AlertDialog alertInside = alertDialogB.create();
                                alertInside.show();

                                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View viewIn, int position, long id) {
                                        alertInside.dismiss();
                                        if(position == 0) //WHITE
                                        {
                                            currentSC.setColor("WHITE");
                                        }
                                        else if(position == 1) //RED
                                        {
                                            currentSC.setColor("RED");
                                        }
                                        else if(position==2) //BLUE
                                        {
                                            currentSC.setColor("BLUE");
                                        }
                                        else if(position==3) //GREEN
                                        {
                                            currentSC.setColor("GREEN");
                                        }
                                        else if(position==4) //YELLOW
                                        {
                                            currentSC.setColor("YELLOW");
                                        }
                                        else if(position==5) //ORANGE
                                        {
                                            currentSC.setColor("ORANGE");
                                        }
                                        else if(position==6) //PURPLE
                                        {
                                            currentSC.setColor("PURPLE");
                                        }
                                        else if(position==7) //PINK
                                        {
                                            currentSC.setColor("PINK");
                                        }
                                        else if(position==8) //GRAY
                                        {
                                            currentSC.setColor("GRAY");
                                        }

                                        changeFragmentColor(SingleCounterCondensedFragment.this, currentSC, currentSC.getColor());

                                        saveMultiCounterList();
                                    }
                                });

                            }
                            else if(position == 3) //reset count
                            {
                                alert.dismiss();
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
                            else if(position==4) //rename
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
                            else if(position==5) //delete
                            {
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
                            else if(position==6) //move up
                            {
                                alert.dismiss();
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
                                            String prevColor = cn.getColor();
                                            String prevFragId = MultiCounterActivity.fragTagList.get(prevIndex);
                                            prevFrag = (SingleCounterCondensedFragment) getFragmentManager().findFragmentByTag(prevFragId);

                                            //curr counter info
                                            int currIndex = currentSC.getIndex();
                                            String currName = currentSC.getLabel();
                                            int currCount = currentSC.getCount();
                                            String currColor = currentSC.getColor();
                                            String currFragId = MultiCounterActivity.fragTagList.get(currIndex);
                                            currFrag = (SingleCounterCondensedFragment) getFragmentManager().findFragmentByTag(currFragId);

                                            //set prev info to curr info
                                            currentSC.setLabel(prevName);
                                            currentSC.setCount(prevCount);
                                            currentSC.setIndex(currIndex);
                                            currentSC.setColor(prevColor);
                                            //set curr fragment
                                            currFrag.setCount(prevCount);
                                            currFrag.setNewCounterName(prevName);

                                            //copy prev counter info into curr counter variable
                                            cn.setLabel(currName);
                                            cn.setCount(currCount);
                                            cn.setIndex(prevIndex);
                                            cn.setColor(currColor);
                                            //store fragment info
                                            prevFrag.setNewCounterName(currName);
                                            prevFrag.setCount(currCount);

                                            //SET CURRENT COLOR BACKGROUND
                                            changeFragmentColor(currFrag, currentSC, currentSC.getColor());

                                            //SET PREV COLOR BACKGROUND
                                            changeFragmentColor(prevFrag, cn, cn.getColor());

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
                            else if(position==7) //move down
                            {
                                alert.dismiss();
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
                                            String nextColor = cn.getColor();
                                            String nextFragId = MultiCounterActivity.fragTagList.get(nextIndex);
                                            nextFrag = (SingleCounterCondensedFragment) getFragmentManager().findFragmentByTag(nextFragId);

                                            //curr counter info
                                            int currIndex = currentSC.getIndex();
                                            String currName = currentSC.getLabel();
                                            int currCount = currentSC.getCount();
                                            String currColor = currentSC.getColor();
                                            String currFragId = MultiCounterActivity.fragTagList.get(currIndex);
                                            currFrag = (SingleCounterCondensedFragment) getFragmentManager().findFragmentByTag(currFragId);

                                            //Collections.swap(MultiCounterActivity.fragTagList, prevIndex, currIndex);

                                            //set prev info to curr info
                                            currentSC.setLabel(nextName);
                                            currentSC.setCount(nextCount);
                                            currentSC.setIndex(currIndex);
                                            currentSC.setColor(nextColor);
                                            //set curr fragment
                                            currFrag.setCount(nextCount);
                                            currFrag.setNewCounterName(nextName);

                                            //copy prev counter info into curr counter variable
                                            cn.setLabel(currName);
                                            cn.setCount(currCount);
                                            cn.setIndex(nextIndex);
                                            cn.setColor(currColor);
                                            //store fragment info
                                            nextFrag.setNewCounterName(currName);
                                            nextFrag.setCount(currCount);

                                            //SET CURRENT COLOR BACKGROUND
                                            changeFragmentColor(currFrag, currentSC, currentSC.getColor());

                                            //SET NEXT COLOR BACKGROUND
                                            changeFragmentColor(nextFrag, cn, cn.getColor());


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
                    });

                    return true;
                }
            });

        counterName.setOnLongClickListener(
                new ImageButton.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View view) {
                        counterCount.performLongClick();
                        return true;
                    }
                });

        return view;
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
            //SET COLOR BACKGROUND

            changeFragmentColor(SingleCounterCondensedFragment.this, currentSC, currentSC.getColor());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
        if(incdecInput != null)
            imm.hideSoftInputFromWindow(incdecInput.getWindowToken(), 0);
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

    //HELPER METHODS
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

    public int getCount()
    {
        return Integer.parseInt(counterCount.getText().toString());
    }
    public void setCount(int num)
    {
        currentSC.setCount(num);
        counterCount.setText(Integer.toString(currentSC.getCount()));
        cCount=num;
    }

    public void addCount()
    {
        //int num = Integer.valueOf(counterCount.getText().toString());
        //num++;
        currentSC.addCount();
        counterCount.setText(Integer.toString(currentSC.getCount()));;
    }
    public void subCount()
    {
        //int num = Integer.valueOf(counterCount.getText().toString());
        //num--;
        currentSC.subCount();
        counterCount.setText(Integer.toString(currentSC.getCount()));
    }

    public void increaseCount(int amount)
    {
        currentSC.increaseCount(amount);
        counterCount.setText(Integer.toString(currentSC.getCount()));
        cCount=currentSC.getCount();
    }

    public void decreaseCount(int amount)
    {
        currentSC.decreaseCount(amount);
        counterCount.setText(Integer.toString(currentSC.getCount()));
        cCount=currentSC.getCount();
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
        getActivity().getFragmentManager().beginTransaction().remove(SingleCounterCondensedFragment.this).commit();
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

    public void changeFragmentColor(SingleCounterCondensedFragment myFragment, Counter counter, String color)
    {
        counter.setColor(color);
        if(color.equalsIgnoreCase("WHITE")) //WHITE
        {
            myFragment.getView().setBackgroundColor(Color.WHITE);
        }
        else if(color.equalsIgnoreCase("RED")) //RED
        {
            myFragment.getView().setBackgroundColor(Color.RED);
        }
        else if(color.equalsIgnoreCase("BLUE")) //BLUE
        {
            myFragment.getView().setBackgroundColor(Color.BLUE);
        }
        else if(color.equalsIgnoreCase("GREEN")) //GREEN
        {
            myFragment.getView().setBackgroundColor(Color.GREEN);
        }
        else if(color.equalsIgnoreCase("YELLOW")) //YELLOW
        {
            myFragment.getView().setBackgroundColor(Color.YELLOW);
        }
        else if(color.equalsIgnoreCase("ORANGE")) //ORANGE
        {
            myFragment.getView().setBackgroundColor(Color.parseColor(ColorParse.COLOR_ORANGE));
        }
        else if(color.equalsIgnoreCase("PURPLE")) //PURPLE
        {
            myFragment.getView().setBackgroundColor(Color.parseColor(ColorParse.COLOR_PURPLE));
        }
        else if(color.equalsIgnoreCase("PINK")) //PINK
        {
            myFragment.getView().setBackgroundColor(Color.parseColor(ColorParse.COLOR_PINK));
        }
        else if(color.equalsIgnoreCase("GRAY")) //GRAY
        {
            myFragment.getView().setBackgroundColor(Color.GRAY);
        }
    }
}
