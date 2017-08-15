package com.takezeroapps.countit;

import android.widget.LinearLayout;

/**
 * Created by Brandon on 1/7/2017.
 */
public class CountSizeHelper {
    //Get number of digits
    private int getDigits(int num)
    {
        int length = String.valueOf(num).length();
        return length;
    }

    //get size of count number
    public int getSize(int num, boolean portraitMode, int screenSize)
    {
        int length = getDigits(num);
        int size=0;

        if(screenSize != 0) { //if screen size is not small
            if (portraitMode) {

                if (length == 1)
                    size = 275;
                else if (length == 2)
                    size = 275;
                else if (length == 3)
                    size = 190;
                else if (length == 4)
                    size = 140;
                else if (length == 5)
                    size = 115;
                else if (length == 6)
                    size = 95;
                else if (length == 7)
                    size = 80;
                else if (length == 8)
                    size = 70;
                else if (length == 9)
                    size = 63;
                else if (length == 10)
                    size = 57;
                else if (length == 11)
                    size = 53;
            } else {
                if (length == 1)
                    size = 200;
                else if (length == 2)
                    size = 200;
                else if (length == 3)
                    size = 200;
                else if (length == 4)
                    size = 200;
                else if (length == 5)
                    size = 180;
                else if (length == 6)
                    size = 165;
                else if (length == 7)
                    size = 140;
                else if (length == 8)
                    size = 120;
                else if (length == 9)
                    size = 110;
                else if (length == 10)
                    size = 100;
                else if (length == 11)
                    size = 90;
            }
        }
        else
        {
            if (portraitMode) {

                if (length == 1)
                    size = 120;
                else if (length == 2)
                    size = 120;
                else if (length == 3)
                    size = 100;
                else if (length == 4)
                    size = 90;
                else if (length == 5)
                    size = 75;
                else if (length == 6)
                    size = 60;
                else if (length == 7)
                    size = 60;
                else if (length == 8)
                    size = 60;
                else if (length == 9)
                    size = 50;
                else if (length == 10)
                    size = 40;
                else if (length == 11)
                    size = 30;
            } else {
                if (length == 1)
                    size = 120;
                else if (length == 2)
                    size = 120;
                else if (length == 3)
                    size = 100;
                else if (length == 4)
                    size = 90;
                else if (length == 5)
                    size = 90;
                else if (length == 6)
                    size = 80;
                else if (length == 7)
                    size = 70;
                else if (length == 8)
                    size = 60;
                else if (length == 9)
                    size = 50;
                else if (length == 10)
                    size = 50;
                else if (length == 11)
                    size = 50;
            }
        }

        return size;
    }

}
