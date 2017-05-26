package com.takezeroapps.countit;

/**
 * Created by BDC on 5/25/2017.
 */

public class Multicounter {
    String name;
    int count;

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
