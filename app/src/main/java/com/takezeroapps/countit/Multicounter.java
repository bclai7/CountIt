package com.takezeroapps.countit;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by BDC on 5/25/2017.
 */

public class Multicounter {
    String name; //name of multicounter
    int count; //number of counters
    public ArrayList<Counter> counters;

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

    public int getCount()
    {
        count = counters.size();
        return count;
    }

    public void deleteCounter(String counter)
    {
        Iterator<Counter> i = counters.iterator();
        while (i.hasNext()) {
            Counter c = i.next(); // must be called before you can call i.remove()
            // Do something
            if(c.getLabel().equals(counter))
            {
                i.remove();
                break;
            }
        }
    }

}
