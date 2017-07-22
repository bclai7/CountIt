package com.takezeroapps.countit;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by BDC on 5/25/2017.
 */

public class Multicounter {
    String name; //name of multicounter
    int count; //number of counters
    public ArrayList<Counter> counters;
    String createdDateTime; //string of the date the multicounter is created
    Long createdTimeStamp; //time stamp of when the multicounter was created, uses the amount of milliseconds since epoch (jan 1, 1970). This will be used to sort multicounters by created date.
    String modifiedDateTime; //string of the date the multicounter was last modified
    Long modifiedTimeStamp; //time stamp of when the multicounter was last modified, uses the amount of milliseconds since epoch (jan 1, 1970). This will be used to sort multicounters by last modified date.

    public Multicounter(String name, int count)
    {
        this.name=name;
        this.count=count;
        counters = new ArrayList<Counter>();
        //initialize number of counters to start with using numOfCounters variable. Use a loop?
        for(int i=0; i<count;i++)
        {
            counters.add(new Counter(this.name, "counter"+i, 0, counters.size(), "WHITE"));
        }
        createdDateTime = DateFormat.getDateTimeInstance().format(new Date());
        createdTimeStamp = System.currentTimeMillis();
        modifiedDateTime = DateFormat.getDateTimeInstance().format(new Date());
        modifiedTimeStamp = System.currentTimeMillis();
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

    public String getCreatedDateTime()
    {
        return createdDateTime;
    }

    public Long getCreatedTimeStamp()
    {
        return createdTimeStamp;
    }

    public String getModifiedDateTime()
    {
        return modifiedDateTime;
    }

    public void setModifiedDateTime()
    {
        modifiedDateTime = DateFormat.getDateTimeInstance().format(new Date());
    }

    public Long getModifiedTimeStamp()
    {
        return modifiedTimeStamp;
    }

    public void setModifiedTimeStamp()
    {
        modifiedTimeStamp = System.currentTimeMillis();
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

        //refresh the indexes
        for(int j=0; j<counters.size(); j++)
        {
            counters.get(j).setIndex(j);
        }
    }

    public void resetAllCounters()
    {
        for(Counter c: counters)
        {
            c.setCount(0);
        }
    }

}
