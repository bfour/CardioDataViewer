package at.ac.tuwien.e0826357.cardioapp.wearableSimulator.service.MITECGGenerator;

/*
 * ValueInputVerifier.java
 *
 * See EcgLicense.txt for License terms.
 */

/**
 *
 * @author  Mauricio Villarroel (m.villarroel@acm.og)
 */

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JComponent;

public class ValueInputVerifier {
    
    /** Creates a new instance of ValueInputVerifier */
    public ValueInputVerifier() {
    }

    public static boolean verify(JComponent input, EcgString str, String verifyType) {
        JTextField textF = (JTextField) input;

        boolean RetValue = true;

        
        try {
            if(verifyType == "Integer"){
                int dNumber;
                dNumber = Integer.valueOf(textF.getText()).intValue();
            } else if(verifyType == "Double"){
                double dNumber;
                dNumber = Double.valueOf(textF.getText()).doubleValue();
            }    
        } catch(NumberFormatException e){
            str.setString("You have to enter a(n) " + verifyType + " number, please retry!!!");
            RetValue = false;
        }

        return(RetValue);
    }
}
