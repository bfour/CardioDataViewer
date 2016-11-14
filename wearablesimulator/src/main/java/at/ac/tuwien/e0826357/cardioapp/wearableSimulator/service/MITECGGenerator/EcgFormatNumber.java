package at.ac.tuwien.e0826357.cardioapp.wearableSimulator.service.MITECGGenerator;

/*
 * EcgFormatNumber.java
 *
 * See EcgLicense.txt for License terms.
 */

/**
 *
 * @author  Mauricio Villarroel (m.villarroel@acm.og)
 * Part of this code was taken from "somewhere" on the Internet.
 */

import java.text.DecimalFormat;

public class EcgFormatNumber {
    
    final static DecimalFormat dec1 = new DecimalFormat("0.0");
    final static DecimalFormat dec2 = new DecimalFormat("0.00");

    final static DecimalFormat sci1 = new DecimalFormat("0.0E0");
    final static DecimalFormat sci2 = new DecimalFormat("0.00E0");

    /*
     * Formats the 'number' parameter and returns it as a String.
     * precision = number of decimal places in the output.
     */
    public static String toString( double number,
                                   double upLimit,
                                   double loLimit,
                                   int precision)
    {
        // If number less than decimalLimit, or equal to zero, use decimal style
        if( number == 0.0 ||
            (Math.abs(number) <= upLimit && Math.abs(number) > loLimit) )
        {
            switch (precision){
                case 1 : return dec1.format(number);
                case 2 : return dec2.format(number);
                default: return dec1.format(number);
            }

        } else{
            // Create the format for Scientific Notation with E
            switch (precision){
                case 1 : return sci1.format(number);
                case 2 : return sci2.format(number);
                default: return sci1.format(number);
            }
        }
    }    
}
