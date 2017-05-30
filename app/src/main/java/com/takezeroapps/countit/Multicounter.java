package com.takezeroapps.countit;

import java.util.ArrayList;

/**
 * Created by BDC on 5/25/2017.
 */

public class Multicounter {
    String name; //name of multicounter
    int count; //number of counters
    public ArrayList<Counter> counters;

    //initialize starting counters with generic label names and a starting count of 0 (in the constructor). Also set fragments to the correct fragments.
    //addCounter() method
    //removeCounter() method
    //eraseAllCounters() method ???

    public Multicounter(String name, int count)
    {
        this.name=name;
        this.count=count;
        counters = new ArrayList<Counter>();
        //initialize number of counters to start with using numOfCounters variable. Use a loop?
        for(int i=0; i<count;i++)
        {
            counters.add(new Counter(this.name, "counter"+i, 0));
        }
    }

    public void setName(String name)
    {
        this.name=name;
        for(Counter c: counters) //any counters that belong to this multicounter will also need their "multiCounterName" field updated so it shows the correct corresponding counter ID
        {
            c.setMulticounterName(name);
        }
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
