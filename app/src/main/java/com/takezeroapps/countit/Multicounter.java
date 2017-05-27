package com.takezeroapps.countit;

import java.util.ArrayList;

/**
 * Created by BDC on 5/25/2017.
 */

public class Multicounter {
    String name;
    int count;
    ArrayList<Counter> counters = new ArrayList<Counter>();

    //initialize starting counters with generic label names and a starting count of 0
    //addCounter() method
    //removeCounter() method
    //eraseAllCounters() method
    //renameCounter() method??? or have it in regular Counter object

    public Multicounter(String name, int count)
    {
        this.name=name;
        this.count=count;
    }

    public void setName(String name)
    {
        this.name=name;
    }

    public String getName()
    {
        return name;
    }

    public void setCount(int count)
    {
        this.count=count;
    }

    public int getCount()
    {
        return count;
    }
}