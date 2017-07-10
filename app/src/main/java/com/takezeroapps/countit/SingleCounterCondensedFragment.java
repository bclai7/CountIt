package com.takezeroapps.countit;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;


public class SingleCounterCondensedFragment extends Fragment {

    TextView counterName, counterCount;
    Button plusButton, minusButton;
    String mcName, cName;
    int cCount, newNum;
    boolean isNegative, vibrateSetting, resetconfirmSetting, screenSetting;
    Counter currentSC;
    Multicounter currentMC;
    EditText counterEdit, input;

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
}
