package Exceptions;

/**
 * Created by BDC on 6/17/2017.
 */

public class CountTooLowException extends Exception {
    // Parameterless Constructor
    public CountTooLowException() {}

    // Constructor that accepts a message
    public CountTooLowException(String message)
    {
        super(message);
    }
}
