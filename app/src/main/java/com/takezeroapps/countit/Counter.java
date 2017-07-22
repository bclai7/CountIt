package com.takezeroapps.countit;

import android.app.Fragment;

/**
 * Created by BDC on 5/27/2017.
 */

public class Counter {
    int count; //where the count is at
    String label; //name of count
    String multicounterName; //name of the multicounter it belongs to
    String color; //background color of the counter
    //public Fragment fragment;
    String counterId; //id of counter for when there is two counters from different multicounters that have the same label name. The ID will consist of the Multicounter Name + the counter label
    int index;

    public Counter(String multicounterName, String label, int count, int index, String color)
    {
        this.multicounterName=multicounterName;
        this.label=label;
        this.count=count;
        counterId = this.multicounterName+this.label;
        this.index=index;
        this.color=color;
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

    public void increaseCount(int amount)
    {
        count=count+amount;
    }

    public void subCount()
    {
        count--;
    }

    public void decreaseCount(int amount)
    {
        count = count-amount;
    }

    public void resetCount()
    {
        this.count=0;
    }

    public int getCount()
    {
        return count;
    }

    public void setMulticounterName(String multicounterName)
    {
        this.multicounterName=multicounterName;
        setCounterId();
    }

    public String getMulticounterName()
    {
        return multicounterName;
    }

    public void setCounterId()
    {
        counterId = multicounterName+label;
    }

    public String getCounterId()
    {
        return counterId;
    }

    public String getMultiCounterName()
    {
        return multicounterName;
    }

    public void setIndex(int index)
    {
        this.index=index;
    }

    public int getIndex()
    {
        return index;
    }

    public void setColor(String color)
    {
        this.color = color;
    }

    public String getColor()
    {
        return color;
    }

}
