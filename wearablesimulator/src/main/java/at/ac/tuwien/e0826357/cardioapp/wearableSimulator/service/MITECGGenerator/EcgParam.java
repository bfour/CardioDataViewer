package at.ac.tuwien.e0826357.cardioapp.wearableSimulator.service.MITECGGenerator;

/*
 * EcgParam.java
 *
 * See EcgLicense.txt for License terms.
 */

/**
 *
 * @author  Mauricio Villarroel (m.villarroel@acm.og)
 * Part of of this code was taken from the ECG C version 
 * @Copyright Patrick McSharry and Gari Clifford.
 */

public class EcgParam {
    
    /****************************************************************
     *  GLOBAL ECG PARAMETERS:                                                  
     ****************************************************************/
    private int    N;               /*  Number of heart beats              */
    private double hrstd;           /*  Heart rate std                     */
    private double hrmean;          /*  Heart rate mean                    */
    private double lfhfratio;       /*  LF/HF ratio                        */
    private int    sfecg;           /*  ECG sampling frequency             */        
    private int    sf;              /*  Internal sampling frequency        */
    private double amplitude;       /*  Amplitude for the plot area        */
    private int seed;               /*  Seed                               */    
    private double anoise;          /*  Amplitude of additive uniform noise*/
    private int    period;
    /* Define frequency parameters for rr process 
     * flo and fhi correspond to the Mayer waves and respiratory rate respectively
     */
    private double flo;             /*  Low frequency                      */
    private double fhi;             /*  High frequency                     */
    private double flostd;          /*  Low frequency std                  */
    private double fhistd;          /*  High frequency std                 */
    /* Order of extrema: [P Q R S T]  */
    private double[] theta = new double[5]; /* ti not in radians*/
    private double[] a = new double[5];
    private double[] b = new double[5];

    /* Animation variables */
    private long ecgAnimateInterval;

    /* Flag to know if all parameters are valid */
    private boolean allParametersValid;

    /** Creates a new instance of EcgParam */
    public EcgParam() {
        resetParameters();
    }

    /****************************************************************
     *  GLOBAL Set/Get Parameter Functions:
     ****************************************************************/
    public void setN(int value){
        N = value;
        allParametersValid = false;
    }
    public int getN(){
        return N;
    }
    
    public void setHrStd(double value){
        hrstd = value;
        allParametersValid = false;
    }
    public double getHrStd(){
        return hrstd;
    }

    public void setHrMean(double value){
        hrmean = value;
        allParametersValid = false;
    }
    public double getHrMean(){
        return hrmean;
    }

    public void setLfHfRatio(double value){
        lfhfratio = value;
        allParametersValid = false;
    }
    public double getLfHfRatio(){
        return lfhfratio;
    }

    public void setSfEcg(int value){
        sfecg = value;
        ecgAnimateInterval = (long)(1000/(sfecg));
        allParametersValid = false;
    }
    public int getSfEcg(){
        return sfecg;
    }

    public void setSf(int value){
        sf = value;
        allParametersValid = false;
    }
    public int getSf(){
        return sf;
    }

    public void setAmplitude(double value){
        amplitude = value;
        allParametersValid = false;
    }
    public double getAmplitude(){
        return amplitude;
    }

    public void setSeed(int value){
        seed = value;
        allParametersValid = false;
    }
    public int getSeed(){
        return seed;
    }

    public void setANoise(double value){
        anoise = value;
        allParametersValid = false;
    }
    public double getANoise(){
        return anoise;
    }

    public void setPeriod(int value){
        period = value;
        allParametersValid = false;
    }
    public int getPeriod(){
        return period;
    }

    public void setFLo(double value){
        flo = value;
        allParametersValid = false;
    }
    public double getFLo(){
        return flo;
    }

    public void setFHi(double value){
        fhi = value;
        allParametersValid = false;
    }
    public double getFHi(){
        return fhi;
    }

    public void setFLoStd(double value){
        flostd = value;
        allParametersValid = false;
    }
    public double getFLoStd(){
        return flostd;
    }

    public void setFHiStd(double value){
        fhistd = value;
        allParametersValid = false;
    }
    public double getFHiStd(){
        return fhistd;
    }

    public void setTheta(int index, double value){
        theta[index] = value;
        allParametersValid = false;
    }
    public double getTheta(int index){
        return theta[index];
    }

    public void setA(int index, double value){
        a[index] = value;
        allParametersValid = false;
    }
    public double getA(int index){
        return a[index];
    }

    public void setB(int index, double value){
        b[index] = value;
        allParametersValid = false;
    }
    public double getB(int index){
        return b[index];
    }

    public void setEcgAnimateInterval(long value){
        ecgAnimateInterval = value;
        allParametersValid = false;
    }
    public long getEcgAnimateInterval(){
        return ecgAnimateInterval;
    }
    /****************************************************************
     *  Functions:
     ****************************************************************/

    /*
     * check to see if all parameters are valid
     */
    public boolean isValid(){
        return allParametersValid;
    }

    /*
     * In this function, it can be enforced
     * adicional rules.
     */
    public boolean checkParameters(){

        boolean RetValue = true;
        allParametersValid = true;

        // Check the Internal frequency respect to ECG frequency
        if(((int)Math.IEEEremainder(sf, sfecg)) != 0){
                RetValue = false;
                allParametersValid = false;
        }

        return RetValue;
    }
    /*
     * ReInit the Button Parameters' values
     */
    public void resetParameters(){
        /* General Intergace parameters */
        N = 256;
        sfecg = 256;        
        sf = 512;        
        anoise = 0.1;
        hrmean = 60.0;
        hrstd = 1.0;
        seed = 1;
        amplitude = 1.4;
        flo = 0.1;
        fhi = 0.25;
        flostd = 0.01;
        fhistd = 0.01;      
        lfhfratio = 0.5;

        /* ECG morphology: Order of extrema: [P Q R S T] */
        theta[0]= -60.0;
        theta[1]= -15.0;
        theta[2]= 0.0;
        theta[3]= 15.0;
        theta[4]= 90.0;

        a[0]= 1.2;
        a[1]= -5.0;
        a[2]= 30.0;
        a[3]= -7.5;
        a[4]= 0.75;
        
        b[0]= 0.25;
        b[1]= 0.1;
        b[2]= 0.1;
        b[3]= 0.1;
        b[4]= 0.4;
        
        ecgAnimateInterval = (long)(1000/(sfecg));

        allParametersValid = true;
    }
}
