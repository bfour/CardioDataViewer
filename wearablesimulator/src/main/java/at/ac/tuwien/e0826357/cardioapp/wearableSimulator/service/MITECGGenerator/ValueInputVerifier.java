/*
 * Java ECG Generator
 * © Java Code Copyright(C) 2004 Mauricio Villarroel (m.villarroel@acm.org)
 *
 * Java ECG Generator was developed for ECGSYN:
 *
 * © ECGSYN Copyright by Patrick E. McSharry and Gari D. Clifford.
 * For the Mathematical Model, see:
 *       IEEE Transactions On Biomedical Engineering, 50(3), 289-294, March 2003
 *
 * Contact:
 *      - Patrck McSharry (patrick@mcsharry.net)
 *      - Gari Clifford (gari@mit.edu)
 *
 *
 *
 * Java ECG Generator and all its components are free software. You can redistribute
 * them or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. (See the GNU General Public License
 * for more details.)
 *
 * This file is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

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
