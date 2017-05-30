package com.takezeroapps.countit;

import android.app.Fragment;

/**
 * Created by BDC on 5/27/2017.
 */

public class Counter {
    int count; //where the count is at
    String label; //name of count
    String multicounterName; //name of the multicounter it belongs to
    //public Fragment fragment;
    String counterId; //id of counter for when there is two counters from different multicounters that have the same label name. The ID will consist of the Multicounter Name + the counter label

    public Counter(String multicounterName, String label, int count)
    {
        this.multicounterName=multicounterName;
        this.label=label;
        this.count=count;
        counterId = this.multicounterName+this.label;
    }

    public void setLabel(String label)
    {
        this.label=label;
        setCounterId();
    }

    public String getLabel()
    {
        return label;
    }

    public void setCount(int count)
    {
        this.count=count;
    }

    public void addCount()
    {
        count++;
    }

    public void subCount()
    {
        count--;
    }

    public void resetCount()
    {
        this.count=0;
    }

    public int getCount()
    {
        return count;
    }

    public String getMulticounterName()
    {
        return multicounterName;
    }

    public void setCounterId()
    {
        counterId = multicounterName+label;
    }

    public void setMulticounterName(String multicounterName)
    {
        this.multicounterName=multicounterName;
        setCounterId();
    }

    public String getMultiCounterName()
    {
        return multicounterName;
    }

}
