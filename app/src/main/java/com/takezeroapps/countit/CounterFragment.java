package com.takezeroapps.countit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by scoob on 1/7/2017.
 */
public class CounterFragment extends Fragment{


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sc_counter_fragment, container, false);
        return view;
    }

    public int getCount() //this function is called by the main activity to get the current count
    {
        return 0;
    }
    public void changeCount(int num) //This function is used by the main activity to change the count when the button is held
    {

    }

    public void addCount()
    {

    }
    public void subCount()
    {

    }
    public void resetCount()
    {
        changeCount(0);
    }

}
