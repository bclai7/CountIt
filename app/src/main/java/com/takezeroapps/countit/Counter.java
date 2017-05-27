package com.takezeroapps.countit;

import android.app.Fragment;

/**
 * Created by BDC on 5/27/2017.
 */

public class Counter {
    int count; //where the count is at
    String label; //name of count
    Fragment fragment;

    public Counter(String label, int count)
    {
        this.label=label;
        this.count=count;
    }

    public void setLabel(String label)
    {
        this.label=label;
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

    public void setFragment(Fragment fragment)
    {
        this.fragment=fragment;
    }

    public Fragment getFragment()
    {
        return fragment;
    }

}
