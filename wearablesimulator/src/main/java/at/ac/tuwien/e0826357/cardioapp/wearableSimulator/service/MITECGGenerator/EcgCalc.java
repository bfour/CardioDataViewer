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
 * EcgCalc.java
 *
 * See EcgLicense.txt for License terms.
 */

/**
 *
 * @author  Mauricio Villarroel (m.villarroel@acm.og)
 */

public class EcgCalc {

    /** Creates a new instance of EcgCalc */
    public EcgCalc(EcgParam parameters, EcgLogWindow logOb) {
        paramOb = parameters;
        ecgLog = logOb;

        /* variables for static function ranq() */
        iy=0;
        iv = new long[NTAB];     
    }
    
    public boolean calculateEcg(){
        boolean RetValue;

//        ecgLog.println("Starting to calculate ECG....");
        
        RetValue = dorun();

//        ecgLog.println("Finished calculating ECG table data.\n");

        return(RetValue);        
    }

    public int getEcgResultNumRows(){
        return ecgResultNumRows;
    }

    public double getEcgResultTimeSec(int index){
        return ecgResultTime[index];
    }

    public double getEcgResultVoltage(int index){
        return ecgResultVoltage[index];
    }

    public int getEcgResultPeak(int index){
        return ecgResultPeak[index];
    }

    /* C defines */
    private final double PI = 2.0*Math.asin(1.0);
    private final int NR_END = 1;
    private final int IA = 16807;
    private final long IM = 2147483647;
    private final double AM = (1.0/IM);
    private final long IQ = 127773;
    private final int IR = 2836;
    private final int NTAB = 32;
    private final double NDIV = (1+(IM-1)/NTAB);
    private final double EPS = 1.2e-7;
    private final double RNMX = (1.0-EPS);

    /*****************************************************************************
     *    DEFINE PARAMETERS AS GLOBAL VARIABLES                                 *
     *****************************************************************************/
    //private String outfile ="ecgsyn.dat";
    // Order of extrema: [P Q R S T]
    private double[] ti = new double[6];  /* ti converted in radians             */
    private double[] ai = new double[6];  /* new calculated a                    */
    private double[] bi = new double[6];  /* new calculated b                    */        

    private int Necg = 0;                 /*  Number of ECG outputs              */
    private int mstate = 3;               /*  System state space dimension       */
    private double xinitial = 1.0;        /*  Initial x co-ordinate value        */
    private double yinitial = 0.0;        /*  Initial y co-ordinate value        */
    private double zinitial = 0.04;       /*  Initial z co-ordinate value        */
    private long rseed; 
    private double h; 
    private double[] rr, rrpc;

    /*
     * Variables for static function ran1()
     */
    private long iy;
    private long[] iv;
    
    /*
     * ECG Result Variables
     */
    /* Result Vectors*/
    private double[] ecgResultTime;
    private double[] ecgResultVoltage;
    private int[] ecgResultPeak;
    
    private int ecgResultNumRows;

    /* Object Variables */
    private EcgParam paramOb;
    private EcgLogWindow ecgLog;    

    /*--------------------------------------------------------------------------*/
    /*    UNIFORM DEVIATES                                                      */
    /*--------------------------------------------------------------------------*/

    private double ran1(){

        int j;
        long k;
        double temp;
        boolean flg;

        if(iy == 0)
            flg = false;
        else
            flg = true;

        if((rseed <= 0) || !flg){
            if (-(rseed) < 1)
                rseed = 1;
            else
                rseed = -rseed;

            for (j=NTAB+7; j>=0; j--) {
                    k=(rseed)/IQ;
                    rseed=IA*(rseed-k*IQ)-IR*k;
                    if (rseed < 0)
                        rseed += IM;
                    if (j < NTAB)
                        iv[j] = rseed;
            }
            iy=iv[0];
        }

        k=(rseed)/IQ;
        rseed=IA*(rseed-k*IQ)-IR*k;
        if (rseed< 0)
            rseed += IM;

        j = (int)(iy/NDIV);
        iy=iv[j];
        iv[j] = rseed;

        if ((temp=AM*iy) > RNMX)
            return RNMX;
        else
            return temp;
    }

    /*
     * FFT
     */
    private void ifft(double[] data, long nn, int isign){

        long n, mmax, m, istep, i, j;
        double wtemp,wr,wpr,wpi,wi,theta;
        double tempr,tempi;
        double swap;

        n=nn << 1;
        j=1;
        for (i=1; i< n; i+=2) {
                if (j > i) {
                    //SWAP(data[j],data[i]);
                    swap = data[(int) j];
                    data[(int)j] = data[(int)i];
                    data[(int)i] = swap;
                    //SWAP(data[j+1],data[i+1]);
                    swap = data[(int)j+1];
                    data[(int)j+1] = data[(int)i+1];
                    data[(int)i+1] = swap;
                }
                m=n >> 1;
                while (m >= 2 && j > m) {
                        j -= m;
                        m >>= 1;
                }
                j += m;
        }
        mmax=2;
        while (n > mmax) {
                istep=mmax << 1;
                theta=isign*(6.28318530717959/mmax);
                wtemp=Math.sin(0.5*theta);
                wpr = -2.0*wtemp*wtemp;
                wpi=Math.sin(theta);
                wr=1.0;
                wi=0.0;
                for (m=1; m<mmax; m+=2) {
                        for (i=m; i<=n; i+=istep) {
                                j= i + mmax;
                                tempr=wr * data[(int)j] - wi * data[(int)j+1];
                                tempi=wr * data[(int)j+1] + wi * data[(int)j];
                                data[(int)j]= data[(int)i] - tempr;
                                data[(int)j+1]= data[(int)i+1] - tempi;
                                data[(int)i] += tempr;
                                data[(int)i+1] += tempi;
                        }
                        wr=(wtemp=wr)*wpr-wi*wpi+wr;
                        wi=wi*wpr+wtemp*wpi+wi;
                }
                mmax=istep;
        }
    }

    /*
     * STANDARD DEVIATION CALCULATOR
     */
    /* n-by-1 vector, calculate standard deviation */
    private double stdev(double[] x, int n){
            int j;
            double add,mean,diff,total;

            add = 0.0;
            for(j=1;j<=n;j++)
                add += x[j];

            mean = add/n;

            total = 0.0;
            for(j=1;j<=n;j++){
               diff = x[j] - mean;
               total += diff*diff;
            } 
            return (Math.sqrt(total/((double)n-1)));
    }        

    /*
     * THE ANGULAR FREQUENCY
     */
    private double angfreq(double t){
       int i = 1 + (int)Math.floor(t/h);
       return(2.0*PI/rrpc[i]);
    }

    /*--------------------------------------------------------------------------*/
    /*    THE EXACT NONLINEAR DERIVATIVES                                       */
    /*--------------------------------------------------------------------------*/
    private void derivspqrst(double t0,double[] x, double[] dxdt){

        int i,k;
        double a0,w0,r0,x0,y0,z0;
        double t,dt,dt2,zbase;
        double[] xi, yi;

        k = 5; 
        xi = new double[k + 1];
        yi = new double[k + 1];
        w0 = angfreq(t0);
        r0 = 1.0; x0 = 0.0;  y0 = 0.0;  z0 = 0.0;
        a0 = 1.0 - Math.sqrt((x[1]-x0)*(x[1]-x0) + (x[2]-y0)*(x[2]-y0))/r0;

        for(i=1; i<=k; i++)
            xi[i] = Math.cos(ti[i]);
        for(i=1; i<=k; i++)
            yi[i] = Math.sin(ti[i]);   


        zbase = 0.005* Math.sin(2.0*PI*paramOb.getFHi()*t0);

        t = Math.atan2(x[2],x[1]);
        dxdt[1] = a0*(x[1] - x0) - w0*(x[2] - y0);
        dxdt[2] = a0*(x[2] - y0) + w0*(x[1] - x0); 
        dxdt[3] = 0.0;  

        for(i=1; i<=k; i++){
          dt = Math.IEEEremainder(t-ti[i], 2.0*PI);
          dt2 = dt*dt;
          dxdt[3] += -ai[i] * dt * Math.exp(-0.5*dt2/(bi[i]*bi[i])); 
        }
        dxdt[3] += -1.0*(x[3] - zbase);
    }

    /*
     * RUNGA-KUTTA FOURTH ORDER INTEGRATION
     */
    private void Rk4(double[] y, int n, double x, double h, double[] yout){
        int i;
        double xh,hh,h6;
        double[] dydx, dym, dyt, yt;

        dydx=  new double[n + 1];
        dym =  new double[n + 1];
        dyt =  new double[n + 1];
        yt  =  new double[n + 1];

        hh= h * 0.5;
        h6= h/6.0;
        xh= x + hh;

        derivspqrst(x,y,dydx);
        for (i=1; i<=n; i++)
            yt[i]=y[i]+hh*dydx[i];

        derivspqrst(xh,yt,dyt);
        for (i=1; i<=n; i++)
            yt[i]=y[i] + hh * dyt[i];

        derivspqrst(xh,yt,dym);
        for (i=1; i<=n; i++){
                yt[i]=y[i] + h * dym[i];
                dym[i] += dyt[i];
        }

        derivspqrst(x+h,yt,dyt);
        for (i=1; i<=n; i++)
                yout[i]=y[i] + h6 * (dydx[i]+dyt[i]+2.0*dym[i]);
    }

    /*
     * GENERATE RR PROCESS
     */
    private void rrprocess(double[] rr, double flo, double fhi, 
                          double flostd, double fhistd, double lfhfratio,  
                          double hrmean, double hrstd, double sf, int n)
    {
        int i,j;
        double c1,c2,w1,w2,sig1,sig2,rrmean,rrstd,xstd,ratio;
        double df;//,dw1,dw2;
        double[] w, Hw, Sw, ph0, ph, SwC;

        w =  new double[n+1];
        Hw = new double[n+1];
        Sw = new double[n+1];
        ph0= new double[(int)(n/2-1 +1)];
        ph = new double[n+1];
        SwC= new double[(2*n)+1];

        w1 = 2.0*PI*flo;
        w2 = 2.0*PI*fhi;
        c1 = 2.0*PI*flostd;
        c2 = 2.0*PI*fhistd;
        sig2 = 1.0;
        sig1 = lfhfratio;
        rrmean = 60.0/hrmean;
        rrstd = 60.0*hrstd/(hrmean*hrmean);

        df = sf/(double)n;
        for(i=1; i<=n; i++)
           w[i] = (i-1)*2.0*PI*df;

        for(i=1; i<=n; i++){
          //dw1 = w[i]-w1;
          //dw2 = w[i]-w2;
          Hw[i] = (sig1*Math.exp(-0.5*(Math.pow(w[i]-w1,2)/Math.pow(c1,2))) / Math.sqrt(2*PI*c1*c1))
                + (sig2*Math.exp(-0.5*(Math.pow(w[i]-w2,2)/Math.pow(c2,2))) / Math.sqrt(2*PI*c2*c2));
        }

        for(i=1; i<=n/2; i++)
           Sw[i] = (sf/2.0)* Math.sqrt(Hw[i]);

        for(i=n/2+1; i<=n; i++)
           Sw[i] = (sf/2.0)* Math.sqrt(Hw[n-i+1]);

        /* randomise the phases */
        for(i=1; i<=n/2-1; i++)
           ph0[i] = 2.0*PI*ran1();

        ph[1] = 0.0;
        for(i=1; i<=n/2-1; i++)
           ph[i+1] = ph0[i];

        ph[n/2+1] = 0.0;
        for(i=1; i<=n/2-1; i++)
           ph[n-i+1] = - ph0[i]; 

        /* make complex spectrum */
        for(i=1; i<=n; i++)
           SwC[2*i-1] = Sw[i]* Math.cos(ph[i]);

        for(i=1; i<=n; i++)
           SwC[2*i] = Sw[i]* Math.sin(ph[i]);

        /* calculate inverse fft */
        ifft(SwC,n,-1);

        /* extract real part */
        for(i=1; i<=n; i++)
           rr[i] = (1.0/(double)n)*SwC[2*i-1];

        xstd = stdev(rr,n);
        ratio = rrstd/xstd; 

        for(i=1; i<=n; i++)
           rr[i] *= ratio;

        for(i=1; i<=n; i++)
           rr[i] += rrmean;

    }

    /*
     * DETECT PEAKS
     */
    private void detectpeaks(double[] ipeak, double[] x, double[] y, double[] z, int n){
        int i, j, j1, j2, jmin, jmax, d;
        double thetap1, thetap2, thetap3, thetap4, thetap5;
        double theta1, theta2, d1, d2, zmin, zmax;

        thetap1 = ti[1];
        thetap2 = ti[2];
        thetap3 = ti[3];
        thetap4 = ti[4];
        thetap5 = ti[5];

        for(i=1; i<=n; i++)
           ipeak[i] = 0.0;

        theta1 = Math.atan2(y[1],x[1]);

        for(i=1; i<n; i++){
          theta2 = Math.atan2(y[i+1], x[i+1]);

          if( (theta1 <= thetap1) && (thetap1 <= theta2) ){
            d1 = thetap1 - theta1;
            d2 = theta2 - thetap1;
            if(d1 < d2)
                ipeak[i] = 1.0;
            else
                ipeak[i+1] = 1.0;
          }else if( (theta1 <= thetap2) && (thetap2 <= theta2) ){
            d1 = thetap2 - theta1;
            d2 = theta2 - thetap2;
            if(d1 < d2)
                ipeak[i] = 2.0;
            else
                ipeak[i+1] = 2.0;
          }else if( (theta1 <= thetap3) && (thetap3 <= theta2) ){
            d1 = thetap3 - theta1;
            d2 = theta2 - thetap3;
            if(d1 < d2)
                ipeak[i] = 3.0;
            else
                ipeak[i+1] = 3.0;
          }else if( (theta1 <= thetap4) && (thetap4 <= theta2) ){
            d1 = thetap4 - theta1;
            d2 = theta2 - thetap4;
            if(d1 < d2)
                ipeak[i] = 4.0;
            else
                ipeak[i+1] = 4.0;
          }else if( (theta1 <= thetap5) && (thetap5 <= theta2) ){
            d1 = thetap5 - theta1;
            d2 = theta2 - thetap5;
            if(d1 < d2)
                ipeak[i] = 5.0;
            else
                ipeak[i+1] = 5.0;
          }
          theta1 = theta2; 
        }

        /* correct the peaks */
        d = (int)Math.ceil(paramOb.getSfEcg()/64);
        for(i=1; i<=n; i++){ 
            if( ipeak[i]==1 || ipeak[i]==3 || ipeak[i]==5 ){

                j1 = (1 > (i-d) ? 1 : (i-d)); //MAX(1,i-d);
                j2 = (n < (i+d) ? n : (i+d)); //MIN(n,i+d);
                jmax = j1;
                zmax = z[j1];
                for(j=j1+1;j<=j2;j++){ 
                    if(z[j] > zmax){
                        jmax = j;
                        zmax = z[j];
                    }
                }
                if(jmax != i){
                    ipeak[jmax] = ipeak[i];
                    ipeak[i] = 0;
                }
            } else if( ipeak[i]==2 || ipeak[i]==4 ){
                j1 = (1 > (i-d) ? 1 : (i-d));//MAX(1,i-d);
                j2 = (n < (i+d) ? n : (i+d)); //MIN(n,i+d);
                jmin = j1;
                zmin = z[j1];
                for(j=j1+1;j<=j2;j++){
                    if(z[j] < zmin){
                        jmin = j;
                        zmin = z[j];
                    }
                }
                if(jmin != i){
                    ipeak[jmin] = ipeak[i];
                    ipeak[i] = 0;
                }
            }
        }
    }

    /*
     * DORUN PART OF PROGRAM
     */
    private boolean dorun(){

        boolean RetValue = true;

        int i, j, k, Nrr, Nt, Nts;
        int q;
        double[] x;
        double tstep, tecg, rrmean, hrfact, hrfact2;
        double qd;
        double[] xt, yt, zt, xts, yts, zts;
        double timev, zmin, zmax, zrange;
        double[] ipeak;

        // perform some checks on input values
        q = (int) Math.rint(paramOb.getSf()/paramOb.getSfEcg());
        qd = (double)paramOb.getSf()/(double)paramOb.getSfEcg();

        /* convert angles from degrees to radians and copy a vector to ai*/
        for(i=1; i <= 5; i++){
            ti[i] = paramOb.getTheta(i-1) * PI/180.0;
            ai[i] = paramOb.getA(i-1);
        }

        /* adjust extrema parameters for mean heart rate */
        hrfact =  Math.sqrt(paramOb.getHrMean()/60);
        hrfact2 = Math.sqrt(hrfact);

        for(i=1; i <= 5; i++)
           bi[i] = paramOb.getB(i-1) * hrfact;

        ti[1] *= hrfact2;
        ti[2] *= hrfact;
        ti[3] *= 1.0;
        ti[4] *= hrfact;
        ti[5] *= 1.0;

        /* declare state vector */
        //x=dvector(1,mstate);
        x= new double[4];

        ecgLog.println("Approximate number of heart beats: " + paramOb.getN());
        ecgLog.println("ECG sampling frequency: " + paramOb.getSfEcg() + " Hertz");
        ecgLog.println("Internal sampling frequency: " + paramOb.getSf() + " Hertz");
        ecgLog.println("Amplitude of additive uniformly distributed noise: " + paramOb.getANoise() + " mV");
        ecgLog.println("Heart rate mean: " + paramOb.getHrMean() + " beats per minute");
        ecgLog.println("Heart rate std: " + paramOb.getHrStd() + " beats per minute");
        ecgLog.println("Low frequency: " + paramOb.getFLo() + " Hertz");
        ecgLog.println("High frequency std: " + paramOb.getFHiStd() + " Hertz");
        ecgLog.println("Low frequency std: " + paramOb.getFLoStd() + " Hertz");
        ecgLog.println("High frequency: " + paramOb.getFHi() + " Hertz");
        ecgLog.println("LF/HF ratio: " + paramOb.getLfHfRatio());
        ecgLog.println("time step milliseconds: " + paramOb.getEcgAnimateInterval() + "\n");
        ecgLog.println("Order of Extrema:");
        ecgLog.println("      theta(radians)");
        ecgLog.println("P: ["+ ti[1] + "\t]");
        ecgLog.println("Q: ["+ ti[2] + "\t]");
        ecgLog.println("R: ["+ ti[3] + "\t]");
        ecgLog.println("S: ["+ ti[4] + "\t]");
        ecgLog.println("T: ["+ ti[5] + "\t]\n");
        ecgLog.println("      a(calculated)");
        ecgLog.println("P: ["+ ai[1] + "\t]");
        ecgLog.println("Q: ["+ ai[2] + "\t]");
        ecgLog.println("R: ["+ ai[3] + "\t]");
        ecgLog.println("S: ["+ ai[4] + "\t]");
        ecgLog.println("T: ["+ ai[5] + "\t]\n");
        ecgLog.println("      b(calculated)");
        ecgLog.println("P: ["+ bi[1] + "\t]");
        ecgLog.println("Q: ["+ bi[2] + "\t]");
        ecgLog.println("R: ["+ bi[3] + "\t]");
        ecgLog.println("S: ["+ bi[4] + "\t]");
        ecgLog.println("T: ["+ bi[5] + "\t]\n");

        /* Initialise the vector */
        x[1] = xinitial; 
        x[2] = yinitial;
        x[3] = zinitial;

        /* initialise seed */
        rseed = -paramOb.getSeed();

        /* calculate time scales */
        h = 1.0/(double)paramOb.getSf();
        tstep = 1.0/(double)paramOb.getSfEcg();

        /* calculate length of RR time series */            
        rrmean = (60.0/paramOb.getHrMean());
        Nrr=(int)Math.pow(2.0, Math.ceil(Math.log(paramOb.getN()*rrmean*paramOb.getSf())/Math.log(2.0))); 

//        ecgLog.println("Using " + Nrr + " = 2^ "+ (int)(Math.log(1.0*Nrr)/Math.log(2.0)) + " samples for calculating RR intervals");

        /* create rrprocess with required spectrum */
        rr = new double[Nrr + 1];
        rrprocess(rr, paramOb.getFLo(), paramOb.getFHi(), paramOb.getFLoStd(),
                  paramOb.getFHiStd(), paramOb.getLfHfRatio(), paramOb.getHrMean(),
                  paramOb.getHrStd(), paramOb.getSf(), Nrr); 

        /* create piecewise constant rr */
        rrpc = new double[(2*Nrr) + 1];
        tecg = 0.0;
        i = 1;
        j = 1;
        while(i <= Nrr){  
          tecg += rr[j];
          j = (int) Math.rint(tecg/h);
          for(k=i; k<=j; k++)
              rrpc[k] = rr[i];
          i = j+1;
        }
        Nt = j;

        /* integrate dynamical system using fourth order Runge-Kutta*/
        xt = new double[Nt + 1];
        yt = new double[Nt + 1];
        zt = new double[Nt + 1];
        timev = 0.0;
        for(i=1; i<=Nt; i++){
            xt[i] = x[1];
            yt[i] = x[2];
            zt[i] = x[3];
            Rk4(x, mstate, timev, h, x);
            timev += h;
        }

        /* downsample to ECG sampling frequency */
        xts = new double[Nt + 1];
        yts = new double[Nt + 1];
        zts = new double[Nt + 1];

        j=0;
        for(i=1; i<=Nt; i+=q){ 
          j++;
          xts[j] = xt[i];
          yts[j] = yt[i];
          zts[j] = zt[i];
        }
        Nts = j;

        /* do peak detection using angle */
        ipeak = new double[Nts + 1];
        detectpeaks(ipeak, xts, yts, zts, Nts);

        /* scale signal to lie between -0.4 and 1.2 mV */
        zmin = zts[1];
        zmax = zts[1];
        for(i=2; i<=Nts; i++){
            if(zts[i] < zmin)
                zmin = zts[i];
            else if(zts[i] > zmax)
                    zmax = zts[i];
        }
        zrange = zmax-zmin;
        for(i=1; i<=Nts; i++)
            zts[i] = (zts[i]-zmin)*(1.6)/zrange - 0.4;

        /* include additive uniformly distributed measurement noise */
        for(i=1; i<=Nts; i++)
            zts[i] += paramOb.getANoise()*(2.0*ran1() - 1.0);

        /*
         * insert into the ECG data table
         */
//        ecgLog.println("Generating result matrix...");
        
        ecgResultNumRows = Nts;
        
        ecgResultTime = new double[ecgResultNumRows];
        ecgResultVoltage = new double[ecgResultNumRows];
        ecgResultPeak = new int[ecgResultNumRows];
        
        for(i=1;i<=Nts;i++){
            ecgResultTime[i-1] = (i-1)*tstep;
            ecgResultVoltage[i-1] = zts[i];
            ecgResultPeak[i-1] = (int)ipeak[i];
            /*
            Vector nuevoRow = new Vector(3);
            nuevoRow.addElement(new String(Double.toString((i-1)*tstep)));
            nuevoRow.addElement(new String(Double.toString(zts[i])));
            nuevoRow.addElement(new String(Integer.toString((int)ipeak[i])));
            tableValuesModel.addRow(nuevoRow);
            */
        }

//        ecgLog.println("Finished generating result matrix.");

        return(RetValue);
    }
}
