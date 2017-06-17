package Exceptions;

/**
 * Created by BDC on 6/17/2017.
 */

public class NoCountEnteredException extends Exception {
    // Parameterless Constructor
    public NoCountEnteredException() {}

    // Constructor that accepts a message
    public NoCountEnteredException(String message)
    {
        super(message);
    }
}
