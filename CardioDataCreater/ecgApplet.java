/************************************************************************/
/*                                                                      */
/* ecgApplet.java                                                       */
/*                                                                      */
/* Copyright (C) 2003 Mauricio Villarroel                               */
/*  (mauricio DOT vllarroel AT estudiantes DOT ucb DOT edu DOT bo)      */
/*                                                                      */
/* ecgApplet.java and all its components are free software; you can     */
/* redistribute them and/or modify it under the terms of the            */
/* GNU General Public License as published by the Free Software         */
/* Foundation; either version 2 of the License, or (at your option)     */
/* any later version.                                                   */
/*                                                                      */
/* This file is distributed in the hope that it will be useful, but     */
/* WITHOUT ANY WARRANTY; without even the implied warranty of           */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                 */
/* See the GNU General Public License for more details.                 */
/*                                                                      */
/************************************************************************/
/*                                                                      */
/* This file was created for the ECGSYN Application.                    */
/*                                                                      */
/* ECGSYN: A program for generating a realistic synthetic               */
/* Electrocardiogram Signals.                                           */
/* Copyright (c) 2003 by Patrick McSharry & Gari Clifford.              */
/* All rights reserved.                                                 */
/*                                                                      */
/* See IEEE Transactions On Biomedical Engineering,                     */
/* 50(3), 289-294, March 2003.                                          */
/* Contact:                                                             */
/* P. McSharry (patrick AT mcsharry DOT net)                            */
/* G. Clifford (gari AT mit DOT edu)                                    */
/*                                                                      */
/************************************************************************/
/*                                                                      */
/* Further updates to this software will be published on:               */
/* http://www.physionet.org/                             */
/*                                                                      */
/************************************************************************/

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.*;
import java.util.Vector;
import java.io.File;
import java.io.IOException;
import java.awt.event.*;
import java.lang.Math.*;
import java.util.Timer;
import java.util.TimerTask;

/*
 * This class formats a number in decimal notation or in 
 * scientific notation.
 */
class FormatNumber{
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

/*
 *
 * Public main class
 */
public class ecgApplet extends javax.swing.JApplet implements AdjustmentListener{

    /**************************************
     * Colors for the Plotting Components *
     **************************************/
    protected Color ecgPlotColor            = Color.BLUE;
    protected Color frameLineColor          = Color.BLACK;
    protected Color frameInsideLineColor    = Color.LIGHT_GRAY;
    protected Color frameFillColor          = Color.WHITE;
    protected Color axesNumColor            = Color.GRAY;
    protected Color titleColor              = Color.BLACK;
    protected Color bgColor                 = Color.WHITE;

    /*********************************************
     * These constants used in drawText() method
     * for placement of the text within a given
     * rectangular area.
     *********************************************/ 
    final int CENTER = 0;
    final int LEFT   = 1;
    final int RIGHT  = 2;

    /*******************
     * Frame Dimensions.
     *******************/
    final int posFrameX =0;
    final int posFrameY =1;
    final int frameHeight =290;
    final int frameAmplitude = frameHeight/2;
    //Coordinates Origin
    final int posOriginY = posFrameY + (frameHeight/2);
    //X coordinates
    final int horzScaleY = posFrameY + frameHeight;
    final int horzScaleWidth = 100;
    final int horzScaleHeight = 20;
    final int fScaleNumSize = 9;
    
    /****************************************************
     * Limit below which scale values use decimal format,
     * above which they use scientific format.
     ****************************************************/
    double upLimit = 100.0;
    double loLimit = 0.01;
    
    /******************************
     * Ploting variables
     ******************************/
    boolean readyToPlot;
    int     plotScrollBarValue;
    double  plotZoom = 0.008;
    double  plotZoomInc = 2;
    /* Flag Variable, show if data has been generated. */
    private boolean ecgGenerated = false;    

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
    private double Anoise;          /*  Amplitude of additive uniform noise*/
    private int    period;
    /* Define frequency parameters for rr process 
     * flo and fhi correspond to the Mayer waves and respiratory rate respectively
     */
    private double flo;             /*  Low frequency                      */
    private double fhi;             /*  High frequency                     */
    private double flostd;          /*  Low frequency std                  */
    private double fhistd;          /*  High frequency std                 */
    /* Order of extrema: [P Q R S T]  */
    private double[] theta = new double[6]; /* ti not in radians*/
    private double[] a = new double[6];
    private double[] b = new double[6];
    
    /*******************************
     * Variable for the Data table
     *******************************/
    private String[] peakStr = {"", "P", "Q", "R", "S", "T"};    

    /******************************************
     * Variables to Animate ECG
     ******************************************/
    //Animating in process?
    private boolean ecgAnimateFlg =false;
    Timer ecgAnimateTimer;
    private long ecgAnimateInterval;
    /* Total plotting Data Table Row */
    private int ecgAnimateNumRows;
    /* Current plotting Data Table Row */
    private int ecgAnimateCurRow;
    /* Plot Area Panel width */
    private int ecgAnimatePanelWidth;
    /* Starting X axis value to plot*/
    private int ecgAnimateInitialZero;
    /* For plotting */
    Point ecgAnimateLastPoint = new java.awt.Point(0, 0);

    /** Initializes the applet ecgApplet */
    public void init() {
        initComponents();
        initWindow();
    }

    private void initWindow(){
        /*********************
        *Init the main Window
        *Set maximize
        *********************/
        try{
            ecgWindow.setMaximum(true);        
        } catch(java.beans.PropertyVetoException e){
            txtStatus.append("Exception Error : " + e + "\n");
        }        

        /*********************
        *Init the data Table
        *********************/
        tableValuesModel = new DefaultTableModel(   new Object [][] {},
                                                    new String [] {"Time", "Voltage", "Peak"}){
                                                        Class[] types = new Class [] {
                                                        java.lang.String.class, java.lang.String.class, java.lang.String.class
                                                    };
                                                    public Class getColumnClass(int columnIndex) {
                                                        return types [columnIndex];
                                                    }
                                                };
        tableValues.setModel(tableValuesModel);

        /* Init the ecgFrame */
        ecgFrame = new ecgPanel();
        ecgFrame.setBackground(new java.awt.Color(255, 255, 255));
        ecgPlotArea.setViewportView(ecgFrame);

        /* Set the ScrollBar */
        plotScrollBar.addAdjustmentListener(this);

        /* Set the size of the Dialogs */
        paramDialog.setBounds(80, 80, 570,500);
        alert.setBounds(80, 80, 540,200);
        helpDialog.setBounds(80, 80, 600,500);

        /*************************
         * Reset all Application 
         * to a init state.
         *************************/        
        resetECG();
    }
    
    private void initComponents() {//GEN-BEGIN:initComponents
        paramDialog = new javax.swing.JDialog();
        paramDesktopPane = new javax.swing.JDesktopPane();
        closeParamDialogButton = new javax.swing.JButton();
        resetParamDialogButton = new javax.swing.JButton();
        saveParamDialogButton = new javax.swing.JButton();
        paramTabbedPane = new javax.swing.JTabbedPane();
        generalInterfacePanel = new javax.swing.JPanel();
        txtSf = new javax.swing.JTextField();
        lblSf = new javax.swing.JLabel();
        lblN = new javax.swing.JLabel();
        txtN = new javax.swing.JTextField();
        lblHrmean = new javax.swing.JLabel();
        txtHrmean = new javax.swing.JTextField();
        lblHrstd = new javax.swing.JLabel();
        txtHrstd = new javax.swing.JTextField();
        lblAmplitude = new javax.swing.JLabel();
        txtAmplitude = new javax.swing.JTextField();
        lblGeneralTitle = new javax.swing.JLabel();
        lblAnoise = new javax.swing.JLabel();
        txtAnoise = new javax.swing.JTextField();
        lblSfecg = new javax.swing.JLabel();
        txtSfecg = new javax.swing.JTextField();
        lblSeed = new javax.swing.JLabel();
        txtSeed = new javax.swing.JTextField();
        spectralCharacteristicsPanel = new javax.swing.JPanel();
        lblSpectralTitle = new javax.swing.JLabel();
        lblLfhfratio = new javax.swing.JLabel();
        txtLfhfratio = new javax.swing.JTextField();
        lblFlo = new javax.swing.JLabel();
        txtFlo = new javax.swing.JTextField();
        lblFhi = new javax.swing.JLabel();
        txtFhi = new javax.swing.JTextField();
        lblFlostd = new javax.swing.JLabel();
        lblFhistd = new javax.swing.JLabel();
        txtFhistd = new javax.swing.JTextField();
        txtFlostd = new javax.swing.JTextField();
        extremaPanel = new javax.swing.JPanel();
        lblMorphologyTitle = new javax.swing.JLabel();
        tiScrollPane = new javax.swing.JScrollPane();
        tiTable = new javax.swing.JTable();
        aiScrollPane = new javax.swing.JScrollPane();
        aiTable = new javax.swing.JTable();
        biScrollPane = new javax.swing.JScrollPane();
        biTable = new javax.swing.JTable();
        ExtremaLabelScrollPane = new javax.swing.JScrollPane();
        ExtremaLabelTable = new javax.swing.JTable();
        paramHelpButton = new javax.swing.JButton();
        alert = new javax.swing.JDialog();
        alertDesktopPane = new javax.swing.JDesktopPane();
        alertText = new javax.swing.JTextArea();
        alertButton = new javax.swing.JButton();
        alertTitle = new javax.swing.JLabel();
        exportDialog = new javax.swing.JDialog();
        helpDialog = new javax.swing.JDialog();
        helpInternalFrame = new javax.swing.JInternalFrame();
        helpScrollPane = new javax.swing.JScrollPane();
        helpEditorPane = new javax.swing.JEditorPane();
        ecgWindow = new javax.swing.JInternalFrame();
        desktopPane = new javax.swing.JDesktopPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblMaxAmplitude = new javax.swing.JLabel();
        lblOrigin = new javax.swing.JLabel();
        lblMinAmplitude = new javax.swing.JLabel();
        lblXAxis = new javax.swing.JLabel();
        TableScrollPane = new javax.swing.JScrollPane();
        tableValues = new javax.swing.JTable();
        exportButton = new javax.swing.JButton();
        plotScrollBar = new javax.swing.JScrollBar();
        statusScrollPane = new javax.swing.JScrollPane();
        txtStatus = new javax.swing.JTextArea();
        ecgPlotArea = new javax.swing.JScrollPane();
        animateDesktopPane = new javax.swing.JDesktopPane();
        stopAnimateButton = new javax.swing.JButton();
        startAnimateButton = new javax.swing.JButton();
        calculateDesktopPane = new javax.swing.JDesktopPane();
        generateButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        paramButton = new javax.swing.JButton();
        zommDesktopPane = new javax.swing.JDesktopPane();
        zoomInButton = new javax.swing.JButton();
        zoomOutButton = new javax.swing.JButton();

        paramDialog.setTitle("Set ECG Parameters...");
        paramDialog.setName("paramDialog");
        closeParamDialogButton.setText("Close");
        closeParamDialogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeParamDialogButtonActionPerformed(evt);
            }
        });

        closeParamDialogButton.setBounds(416, 400, 140, 30);
        paramDesktopPane.add(closeParamDialogButton, javax.swing.JLayeredPane.DEFAULT_LAYER);

        resetParamDialogButton.setText("Reset Values");
        resetParamDialogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetParamDialogButtonActionPerformed(evt);
            }
        });

        resetParamDialogButton.setBounds(160, 400, 140, 30);
        paramDesktopPane.add(resetParamDialogButton, javax.swing.JLayeredPane.DEFAULT_LAYER);

        saveParamDialogButton.setText("Save Values");
        saveParamDialogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveParamDialogButtonActionPerformed(evt);
            }
        });

        saveParamDialogButton.setBounds(7, 400, 140, 30);
        paramDesktopPane.add(saveParamDialogButton, javax.swing.JLayeredPane.DEFAULT_LAYER);

        paramTabbedPane.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
        paramTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        generalInterfacePanel.setLayout(null);

        generalInterfacePanel.setName("generalInterface");
        txtSf.setToolTipText("");
        txtSf.setInputVerifier(new sfVerifier());
        generalInterfacePanel.add(txtSf);
        txtSf.setBounds(350, 110, 110, 20);

        lblSf.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSf.setText("Internal Sampling frequency [Hz]");
        generalInterfacePanel.add(lblSf);
        lblSf.setBounds(10, 110, 320, 16);

        lblN.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblN.setText("Approximate number of heart beats");
        generalInterfacePanel.add(lblN);
        lblN.setBounds(10, 50, 320, 16);

        txtN.setToolTipText("");
        txtN.setInputVerifier(new integerVerifier());
        generalInterfacePanel.add(txtN);
        txtN.setBounds(350, 50, 110, 20);

        lblHrmean.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblHrmean.setText("Heart rate mean [bpm]");
        generalInterfacePanel.add(lblHrmean);
        lblHrmean.setBounds(10, 170, 320, 16);

        txtHrmean.setToolTipText("");
        txtHrmean.setInputVerifier(new doubleVerifier());
        generalInterfacePanel.add(txtHrmean);
        txtHrmean.setBounds(350, 170, 110, 20);

        lblHrstd.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblHrstd.setText("Heart rate standard deviation [bpm]");
        generalInterfacePanel.add(lblHrstd);
        lblHrstd.setBounds(10, 200, 320, 16);

        txtHrstd.setToolTipText("");
        txtHrstd.setInputVerifier(new doubleVerifier());
        generalInterfacePanel.add(txtHrstd);
        txtHrstd.setBounds(350, 200, 110, 20);

        lblAmplitude.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAmplitude.setText("Plot area Amplitude");
        generalInterfacePanel.add(lblAmplitude);
        lblAmplitude.setBounds(10, 260, 320, 16);

        txtAmplitude.setToolTipText("");
        txtAmplitude.setInputVerifier(new doubleVerifier());
        generalInterfacePanel.add(txtAmplitude);
        txtAmplitude.setBounds(350, 260, 110, 20);

        lblGeneralTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblGeneralTitle.setText("General Interface Parameters...");
        generalInterfacePanel.add(lblGeneralTitle);
        lblGeneralTitle.setBounds(120, 10, 350, 20);

        lblAnoise.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAnoise.setText("Amplitude of additive uniform noise [mV]");
        lblAnoise.setToolTipText("");
        generalInterfacePanel.add(lblAnoise);
        lblAnoise.setBounds(10, 140, 320, 16);

        txtAnoise.setToolTipText("");
        txtAnoise.setInputVerifier(new doubleVerifier());
        txtAnoise.setName("Anoise");
        generalInterfacePanel.add(txtAnoise);
        txtAnoise.setBounds(350, 140, 110, 20);

        lblSfecg.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSfecg.setText("ECG Sampling Frequency [Hz]");
        generalInterfacePanel.add(lblSfecg);
        lblSfecg.setBounds(10, 80, 320, 16);

        txtSfecg.setToolTipText("");
        txtSfecg.setInputVerifier(new sfecgVerifier());
        generalInterfacePanel.add(txtSfecg);
        txtSfecg.setBounds(350, 80, 110, 20);

        lblSeed.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSeed.setText("Seed");
        generalInterfacePanel.add(lblSeed);
        lblSeed.setBounds(10, 230, 320, 16);

        txtSeed.setToolTipText("");
        txtSeed.setInputVerifier(new integerVerifier());
        generalInterfacePanel.add(txtSeed);
        txtSeed.setBounds(350, 230, 110, 20);

        paramTabbedPane.addTab("General Interface", generalInterfacePanel);

        spectralCharacteristicsPanel.setLayout(null);

        spectralCharacteristicsPanel.setName("spectralCharacteristics");
        lblSpectralTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSpectralTitle.setText("Spectral Characteristics Parameters...");
        spectralCharacteristicsPanel.add(lblSpectralTitle);
        lblSpectralTitle.setBounds(120, 10, 350, 20);

        lblLfhfratio.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblLfhfratio.setText("LF/HF ratio");
        spectralCharacteristicsPanel.add(lblLfhfratio);
        lblLfhfratio.setBounds(10, 170, 320, 16);

        txtLfhfratio.setToolTipText("Low Frequency / High Frequency ratio");
        txtLfhfratio.setInputVerifier(new doubleVerifier());
        spectralCharacteristicsPanel.add(txtLfhfratio);
        txtLfhfratio.setBounds(350, 170, 110, 20);

        lblFlo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblFlo.setText("Low frequency [Hz]");
        spectralCharacteristicsPanel.add(lblFlo);
        lblFlo.setBounds(10, 50, 320, 16);

        txtFlo.setToolTipText("");
        txtFlo.setInputVerifier(new doubleVerifier());
        spectralCharacteristicsPanel.add(txtFlo);
        txtFlo.setBounds(350, 50, 110, 20);

        lblFhi.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblFhi.setText("High frequency [Hz]");
        spectralCharacteristicsPanel.add(lblFhi);
        lblFhi.setBounds(10, 80, 320, 16);

        txtFhi.setToolTipText("");
        txtFhi.setInputVerifier(new doubleVerifier());
        spectralCharacteristicsPanel.add(txtFhi);
        txtFhi.setBounds(350, 80, 110, 20);

        lblFlostd.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblFlostd.setText("Low frequency standard deviation [Hz]");
        spectralCharacteristicsPanel.add(lblFlostd);
        lblFlostd.setBounds(10, 110, 320, 16);

        lblFhistd.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblFhistd.setText("High frequency standard deviation [Hz]");
        lblFhistd.setToolTipText("");
        spectralCharacteristicsPanel.add(lblFhistd);
        lblFhistd.setBounds(10, 140, 320, 16);

        txtFhistd.setToolTipText("");
        txtFhistd.setInputVerifier(new doubleVerifier());
        txtFhistd.setName("Anoise");
        spectralCharacteristicsPanel.add(txtFhistd);
        txtFhistd.setBounds(350, 140, 110, 20);

        txtFlostd.setToolTipText("");
        txtFlostd.setInputVerifier(new doubleVerifier());
        spectralCharacteristicsPanel.add(txtFlostd);
        txtFlostd.setBounds(350, 110, 110, 20);

        paramTabbedPane.addTab("Spectral Characteristics", spectralCharacteristicsPanel);

        extremaPanel.setLayout(null);

        lblMorphologyTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMorphologyTitle.setText("Order of Extrema...");
        extremaPanel.add(lblMorphologyTitle);
        lblMorphologyTitle.setBounds(90, 10, 350, 20);

        tiScrollPane.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
        tiScrollPane.setViewportBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
        tiTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Theta"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tiScrollPane.setViewportView(tiTable);

        extremaPanel.add(tiScrollPane);
        tiScrollPane.setBounds(170, 80, 80, 120);

        aiScrollPane.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
        aiScrollPane.setViewportBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
        aiTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "a"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        aiScrollPane.setViewportView(aiTable);

        extremaPanel.add(aiScrollPane);
        aiScrollPane.setBounds(280, 80, 80, 120);

        biScrollPane.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
        biScrollPane.setViewportBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
        biTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "b"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        biScrollPane.setViewportView(biTable);

        extremaPanel.add(biScrollPane);
        biScrollPane.setBounds(390, 80, 80, 120);

        ExtremaLabelScrollPane.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
        ExtremaLabelScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        ExtremaLabelScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        ExtremaLabelScrollPane.setViewportBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
        ExtremaLabelScrollPane.setEnabled(false);
        ExtremaLabelTable.setBackground((java.awt.Color) javax.swing.UIManager.getDefaults().get("Button.background"));
        ExtremaLabelTable.setFont(new java.awt.Font("Dialog", 1, 12));
        ExtremaLabelTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"          P(1) :"},
                {"          Q(2) :"},
                {"          R(3) :"},
                {"          S(4) :"},
                {"          T(5) :"}
            },
            new String [] {
                "peak label"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        ExtremaLabelTable.setGridColor((java.awt.Color) javax.swing.UIManager.getDefaults().get("Button.background"));
        ExtremaLabelTable.setEnabled(false);
        ExtremaLabelScrollPane.setViewportView(ExtremaLabelTable);

        extremaPanel.add(ExtremaLabelScrollPane);
        ExtremaLabelScrollPane.setBounds(40, 80, 110, 120);

        paramTabbedPane.addTab("ECG Morphology", extremaPanel);

        paramTabbedPane.setBounds(7, 10, 550, 380);
        paramDesktopPane.add(paramTabbedPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

        paramHelpButton.setText("Help");
        paramHelpButton.setEnabled(false);
        paramHelpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paramHelpButtonActionPerformed(evt);
            }
        });

        paramHelpButton.setBounds(310, 400, 100, 30);
        paramDesktopPane.add(paramHelpButton, javax.swing.JLayeredPane.DEFAULT_LAYER);

        paramDialog.getContentPane().add(paramDesktopPane, java.awt.BorderLayout.CENTER);

        alert.setModal(true);
        alertText.setBackground((java.awt.Color) javax.swing.UIManager.getDefaults().get("Desktop.background"));
        alertText.setEditable(false);
        alertText.setLineWrap(true);
        alertText.setRows(10);
        alertText.setWrapStyleWord(true);
        alertText.setBounds(10, 10, 520, 80);
        alertDesktopPane.add(alertText, javax.swing.JLayeredPane.DEFAULT_LAYER);

        alertButton.setText("OK");
        alertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alertButtonActionPerformed(evt);
            }
        });

        alertButton.setBounds(230, 110, 80, -1);
        alertDesktopPane.add(alertButton, javax.swing.JLayeredPane.DEFAULT_LAYER);

        alertTitle.setBounds(0, 0, 350, -1);
        alertDesktopPane.add(alertTitle, javax.swing.JLayeredPane.DEFAULT_LAYER);

        alert.getContentPane().add(alertDesktopPane, java.awt.BorderLayout.CENTER);

        exportDialog.setModal(true);
        helpInternalFrame.setTitle("ECG Help");
        helpInternalFrame.setVisible(true);
        helpEditorPane.setEditable(false);
        helpScrollPane.setViewportView(helpEditorPane);

        helpInternalFrame.getContentPane().add(helpScrollPane, java.awt.BorderLayout.CENTER);

        helpDialog.getContentPane().add(helpInternalFrame, java.awt.BorderLayout.CENTER);

        setFocusCycleRoot(false);
        setName("ecgpanel");
        ecgWindow.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
        ecgWindow.setTitle("Electrocardiogram Signals (ECG)");
        try {
            ecgWindow.setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {
            e1.printStackTrace();
        }
        ecgWindow.setVisible(true);
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("V");
        jLabel1.setBounds(0, 110, 20, 16);
        desktopPane.add(jLabel1, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("o");
        jLabel2.setBounds(0, 130, 20, 16);
        desktopPane.add(jLabel2, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("l");
        jLabel3.setBounds(0, 150, 20, 16);
        desktopPane.add(jLabel3, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("t");
        jLabel4.setBounds(0, 170, 20, 16);
        desktopPane.add(jLabel4, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("a");
        jLabel5.setBounds(0, 190, 20, 16);
        desktopPane.add(jLabel5, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("g");
        jLabel6.setBounds(0, 210, 20, 16);
        desktopPane.add(jLabel6, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("e");
        jLabel7.setBounds(0, 230, 20, 16);
        desktopPane.add(jLabel7, javax.swing.JLayeredPane.DEFAULT_LAYER);

        lblMaxAmplitude.setFont(new java.awt.Font("Dialog", 1, 9));
        lblMaxAmplitude.setText("0.001");
        lblMaxAmplitude.setBounds(30, 25, 40, 12);
        desktopPane.add(lblMaxAmplitude, javax.swing.JLayeredPane.DEFAULT_LAYER);

        lblOrigin.setFont(new java.awt.Font("Dialog", 1, 9));
        lblOrigin.setText("0.00");
        lblOrigin.setBounds(30, 167, 40, 12);
        desktopPane.add(lblOrigin, javax.swing.JLayeredPane.DEFAULT_LAYER);

        lblMinAmplitude.setFont(new java.awt.Font("Dialog", 1, 9));
        lblMinAmplitude.setText("-0.001");
        lblMinAmplitude.setBounds(30, 309, 40, 12);
        desktopPane.add(lblMinAmplitude, javax.swing.JLayeredPane.DEFAULT_LAYER);

        lblXAxis.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblXAxis.setText("Time");
        lblXAxis.setBounds(73, 367, 580, 16);
        desktopPane.add(lblXAxis, javax.swing.JLayeredPane.DEFAULT_LAYER);

        TableScrollPane.setBorder(new javax.swing.border.TitledBorder(null, "Data Table", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.ABOVE_TOP));
        TableScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        TableScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        tableValues.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        TableScrollPane.setViewportView(tableValues);

        TableScrollPane.setBounds(658, 2, 250, 510);
        desktopPane.add(TableScrollPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

        exportButton.setText("Export Table Data...");
        exportButton.setEnabled(false);
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        exportButton.setBounds(658, 515, 250, 20);
        desktopPane.add(exportButton, javax.swing.JLayeredPane.DEFAULT_LAYER);

        plotScrollBar.setMaximum(0);
        plotScrollBar.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        plotScrollBar.setName("timeScroll");
        plotScrollBar.setBounds(73, 352, 580, 17);
        desktopPane.add(plotScrollBar, javax.swing.JLayeredPane.DEFAULT_LAYER);

        statusScrollPane.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED), "Status and Messages:", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.ABOVE_TOP));
        statusScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        txtStatus.setEditable(false);
        statusScrollPane.setViewportView(txtStatus);

        statusScrollPane.setBounds(234, 385, 420, 150);
        desktopPane.add(statusScrollPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

        ecgPlotArea.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED), "Plot Area", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.ABOVE_TOP));
        ecgPlotArea.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        ecgPlotArea.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        ecgPlotArea.setBounds(73, 2, 580, 350);
        desktopPane.add(ecgPlotArea, javax.swing.JLayeredPane.DEFAULT_LAYER);

        animateDesktopPane.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)), "Animate"));
        stopAnimateButton.setFont(new java.awt.Font("Dialog", 1, 11));
        stopAnimateButton.setText("Stop");
        stopAnimateButton.setEnabled(false);
        stopAnimateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopAnimateButtonActionPerformed(evt);
            }
        });

        stopAnimateButton.setBounds(120, 17, 90, 20);
        animateDesktopPane.add(stopAnimateButton, javax.swing.JLayeredPane.DEFAULT_LAYER);

        startAnimateButton.setFont(new java.awt.Font("Dialog", 1, 11));
        startAnimateButton.setText("Start");
        startAnimateButton.setEnabled(false);
        startAnimateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startAnimateButtonActionPerformed(evt);
            }
        });

        startAnimateButton.setBounds(7, 17, 90, 20);
        animateDesktopPane.add(startAnimateButton, javax.swing.JLayeredPane.DEFAULT_LAYER);

        animateDesktopPane.setBounds(7, 492, 220, 45);
        desktopPane.add(animateDesktopPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

        calculateDesktopPane.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)), "Calculate ECG"));
        generateButton.setFont(new java.awt.Font("Dialog", 1, 11));
        generateButton.setText("Generate");
        generateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateButtonActionPerformed(evt);
            }
        });

        generateButton.setBounds(7, 17, 90, 20);
        calculateDesktopPane.add(generateButton, javax.swing.JLayeredPane.DEFAULT_LAYER);

        clearButton.setFont(new java.awt.Font("Dialog", 1, 11));
        clearButton.setText("Clear");
        clearButton.setEnabled(false);
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        clearButton.setBounds(120, 17, 90, 20);
        calculateDesktopPane.add(clearButton, javax.swing.JLayeredPane.DEFAULT_LAYER);

        paramButton.setFont(new java.awt.Font("Dialog", 1, 11));
        paramButton.setText("Set Parameters...");
        paramButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paramButtonActionPerformed(evt);
            }
        });

        paramButton.setBounds(7, 42, 130, 20);
        calculateDesktopPane.add(paramButton, javax.swing.JLayeredPane.DEFAULT_LAYER);

        calculateDesktopPane.setBounds(7, 377, 220, 70);
        desktopPane.add(calculateDesktopPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

        zommDesktopPane.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)), "Zoom plot area"));
        zoomInButton.setFont(new java.awt.Font("Dialog", 1, 14));
        zoomInButton.setText("+");
        zoomInButton.setEnabled(false);
        zoomInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInButtonActionPerformed(evt);
            }
        });

        zoomInButton.setBounds(120, 17, 90, 20);
        zommDesktopPane.add(zoomInButton, javax.swing.JLayeredPane.DEFAULT_LAYER);

        zoomOutButton.setFont(new java.awt.Font("Dialog", 1, 14));
        zoomOutButton.setText("-");
        zoomOutButton.setEnabled(false);
        zoomOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutButtonActionPerformed(evt);
            }
        });

        zoomOutButton.setBounds(7, 17, 90, 20);
        zommDesktopPane.add(zoomOutButton, javax.swing.JLayeredPane.DEFAULT_LAYER);

        zommDesktopPane.setBounds(7, 447, 220, 45);
        desktopPane.add(zommDesktopPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

        ecgWindow.getContentPane().add(desktopPane, java.awt.BorderLayout.CENTER);

        getContentPane().add(ecgWindow, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents

    private void paramHelpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paramHelpButtonActionPerformed
        helpEditorPane.setContentType("text/html");
        helpEditorPane.setText( "<html> <body>" +
                                "<font size='3'>"+
                                "<table width='400' border='0' align='center' cellpadding='0' cellspacing='5'>" +
                                "  <tr> " +
                                "    <td height='50' align='left' valign='middle'><strong><font color='#FF0000' size='4' face='Arial, Helvetica, sans-serif'>ECGSYN " +
                                "      PARAMETER DOCUMENTATION</font></strong></td>" +
                                "  </tr>" +
                                "  <tr> " +
                                "       <td height='30' align='left' valign='top'><strong><font color='#0000FF'>GENERAL " +
                                "           INTERFACE</font></strong></td> "+
                                "  </tr>" +
                                "  <tr> " +
                                "      <td align='left' valign='top'> <table width='100%' border='0' cellspacing='5' cellpadding='5'>" +
                                "    <tr> " +
                                "      <td width='60'>&nbsp;</td>" +
                                "      <td><strong>Approximate number of heart beats:</strong><br>" +
                                "        fjaklsdfjasldjasdjfgfagsdgfasdgfywerwaegsdgfwefasjkgiawegasfkgiawefaklsdgfaiwegfklasegfaiwegaklgawiegfafwgegfauiwegfkasdgfiawegfawiegfasdklgfialweugfasefiuawgefilugfawileugfawilegfaweilgfawilegfasiduflskdgfailwuegfawileusdjklgfawilesdfk</td>" +
                                "    </tr>" +
                                "    <tr>" +
                                "      <td width='60'>&nbsp;</td>" +
                                "      <td><strong><dt>ECG Sampling Frequency</dt></strong>" +
                                "        <dd>definition</dd></td>" +
                                "    </tr>" +
                                "    <tr>" +
                                "      <td width='60'>&nbsp;</td>" +
                                "      <td><strong><dt>Internal Sampling Frequency</dt></strong>" +
                                "        <dd>definition</dd></td>" +
                                "    </tr>" +
                                "    <tr>" +
                                "      <td width='60'>&nbsp;</td>" +
                                "      <td><strong><dt>Amplitude of additive uniform noise</dt></strong>" +
                                "        <dd>definition</dd></td>" +
                                "    </tr>" +
                                "    <tr>" +
                                "      <td width='60'>&nbsp;</td>" +
                                "      <td><strong><dt>Heart rate mean</dt></strong>" +
                                "        <dd>definition</dd></td>" +
                                "    </tr>" +
                                "    <tr>" +
                                "      <td width='60'>&nbsp;</td>" +
                                "      <td><strong><dt>Heart rate standard deviation</dt></strong>" +
                                "        <dd>definition</dd></td>" +
                                "    </tr>" +
                                "    <tr>" +
                                "      <td width='60'>&nbsp;</td>" +
                                "      <td><strong><dt>Seed</dt></strong>" +
                                "        <dd>definition</dd></td>" +
                                "    </tr>" +
                                "  </table></td>" +
                                "  </tr>" +
                                "</table>" +
                                "</font>" +
                                "</body> </html>");
        helpDialog.show();
    }//GEN-LAST:event_paramHelpButtonActionPerformed

    private void alertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alertButtonActionPerformed
        alert.hide();
    }//GEN-LAST:event_alertButtonActionPerformed

    private void zoomInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInButtonActionPerformed
        desktopPane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
 
        paramDialog.hide();
        plotZoom = plotZoom / plotZoomInc;
        ecgFrame.repaint();

        desktopPane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));        
    }//GEN-LAST:event_zoomInButtonActionPerformed

    private void zoomOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutButtonActionPerformed
        desktopPane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

        paramDialog.hide();
        plotZoom = plotZoom * plotZoomInc;
        ecgFrame.repaint();

        desktopPane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_zoomOutButtonActionPerformed

    private void startAnimateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startAnimateButtonActionPerformed
        paramDialog.hide();
        // Disabling automatic plot
        readyToPlot = false;
        ecgFrame.repaint();

        /*
         * Initialize ECG Animate variables
         */
        ecgAnimateFlg = true;
        ecgAnimateNumRows = tableValuesModel.getRowCount();
        ecgAnimateCurRow = 0;
        ecgAnimatePanelWidth = ecgFrame.getBounds().width;
        ecgAnimateInitialZero = 0;
        ecgAnimateLastPoint.setLocation(0, posOriginY - (int)(Double.valueOf(tableValues.getValueAt(0, 1).toString()).doubleValue() * frameAmplitude / amplitude));
        
        /* Create Timer */
        ecgAnimateTimer = new Timer();
        /* Schedule the Animate Plotting Task */
        ecgAnimateTimer.scheduleAtFixedRate(new ECGAnimate(), 0, ecgAnimateInterval);
        
        /* Set the Animate Buttons */
        startECGAnimationSetControls();
    }//GEN-LAST:event_startAnimateButtonActionPerformed

    private void saveParamDialogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveParamDialogButtonActionPerformed
        saveParametersValues();
        paramDialog.hide();        
    }//GEN-LAST:event_saveParamDialogButtonActionPerformed

    private void resetParamDialogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetParamDialogButtonActionPerformed
        clearParameters();
    }//GEN-LAST:event_resetParamDialogButtonActionPerformed

    private void closeParamDialogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeParamDialogButtonActionPerformed
        paramDialog.hide();        
    }//GEN-LAST:event_closeParamDialogButtonActionPerformed

    private void paramButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paramButtonActionPerformed
        paramDialog.show();
    }//GEN-LAST:event_paramButtonActionPerformed

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        paramDialog.hide();
        exportDialog.setBounds(80, 80,500,500);
        exportDialog.show();     
    }//GEN-LAST:event_exportButtonActionPerformed

    private void saveParametersValues(){
        txtStatus.setText(null);
        checkParameters();
    }

    private void clearDataTable(){
        // Delete the DataTable
        tableValuesModel.setRowCount(0);
    }

    /**
     *   Draw a string in the center of a given box.
     *   Reduce the font size if necessary to fit. Can
     *   fix the type size to a value passed as an argument.
     *   The position of the string within the box passed
     *   as LEFT, CENTER or RIGHT constant value.
     *   Don't draw the strings if they do not fit.
     */
    private int drawText(   Graphics g, String msg, int xBox, int yBox, int boxWidth, int boxHeight,
                            int fixedTypeSizeValue, int position){
        boolean fixedTypeSize = false;
        int typeSize = 24;

        // Fixed to a particular type size.
        if(fixedTypeSizeValue > 0) {
            fixedTypeSize = true;
            typeSize = fixedTypeSizeValue;
        }

        int typeSizeMin = 8;
        int x=xBox,y=yBox;
        do {
            // Create the font and pass it to the  Graphics context
            g.setFont(new Font("Monospaced",Font.PLAIN,typeSize));

            // Get measures needed to center the message
            FontMetrics fm = g.getFontMetrics();

            // How many pixels wide is the string
            int msgWidth = fm.stringWidth(msg);

            // How tall is the text?
            int msgHeight = fm.getHeight();

            // See if the text will fit in the allotted
            // vertical limits
            if( msgHeight < boxHeight && msgWidth < boxWidth) {
                y = yBox + boxHeight/2 +(msgHeight/2);
                if( position == CENTER)
                    x = xBox + boxWidth/2 - (msgWidth/2);
                else if(position == RIGHT)
                    x = xBox + boxWidth - msgWidth;
                else
                    x = xBox;

                break;
            }

            // If fixedTypeSize and wouldn't fit, don't draw.
            if( fixedTypeSize) return -1;

            // Try smaller type
            typeSize -= 2;

        } while (typeSize >= typeSizeMin);

        // Don't display the numbers if they did not fit
        if( typeSize < typeSizeMin) return -1;

        // Otherwise, draw and return positive signal.
        g.drawString(msg,x,y);
//                ecgFrame.revalidate();
//                ecgFrame.repaint();        
        return typeSize;
    }

    /*
     * ReInit the Button Parameters' values
     */
    private void clearParameters(){
        /* General Intergace parameters */
        txtN.setText("256");
        N = 256;
        
        txtSfecg.setText("256");
        sfecg = 256;        

        txtSf.setText("512");
        sf = 512;        
 
        txtAnoise.setText("0.1");
        Anoise = 0.1;

        txtHrmean.setText("60.0");
        hrmean = 60.0;

        txtHrstd.setText("1.0");
        hrstd = 1.0;

        txtSeed.setText("1");
        seed = 1;

        txtAmplitude.setText("1.4");
        amplitude = 1.4;
        
        /* Spectral Characteristics parameters */
        txtFlo.setText("0.1");
        flo = 0.1;

        txtFhi.setText("0.25");
        fhi = 0.25;

        txtFlostd.setText("0.01");
        flostd = 0.01;

        txtFhistd.setText("0.01");
        fhistd = 0.01;      

        txtLfhfratio.setText("0.5");
        lfhfratio = 0.5;

        /*
         * ECG morphology: Order of extrema: [P Q R S T]
         */
        theta[1]= -60.0;
        theta[2]= -15.0;
        theta[3]= 0.0;
        theta[4]= 15.0;
        theta[5]= 90.0;

        a[1]= 1.2;
        a[2]= -5.0;
        a[3]= 30.0;
        a[4]= -7.5;
        a[5]= 0.75;
        
        b[1]= 0.25;
        b[2]= 0.1;
        b[3]= 0.1;
        b[4]= 0.1;
        b[5]= 0.4;

        //data tables
        tiTable.getModel().setValueAt(new Double(theta[1]), 0, 0);
        tiTable.getModel().setValueAt(new Double(theta[2]), 1, 0);
        tiTable.getModel().setValueAt(new Double(theta[3]), 2, 0);
        tiTable.getModel().setValueAt(new Double(theta[4]), 3, 0);
        tiTable.getModel().setValueAt(new Double(theta[5]), 4, 0);

        aiTable.getModel().setValueAt(new Double(a[1]), 0, 0);
        aiTable.getModel().setValueAt(new Double(a[2]), 1, 0);
        aiTable.getModel().setValueAt(new Double(a[3]), 2, 0);
        aiTable.getModel().setValueAt(new Double(a[4]), 3, 0);
        aiTable.getModel().setValueAt(new Double(a[5]), 4, 0);

        biTable.getModel().setValueAt(new Double(b[1]), 0, 0);
        biTable.getModel().setValueAt(new Double(b[2]), 1, 0);
        biTable.getModel().setValueAt(new Double(b[3]), 2, 0);
        biTable.getModel().setValueAt(new Double(b[4]), 3, 0);
        biTable.getModel().setValueAt(new Double(b[5]), 4, 0);        
        
        /*
         * ECG Animate parameters
         */
        // convert into miliseconds interval
        ecgAnimateInterval = (long)(1000/(sfecg));
    }

    private void resetECG(){
        ecgGenerated = false;
        clearParameters();
        resetPlotArea();
        resetButtons();
        resetStatusBar();
    }

    /*
     * Set the appropiate state of the controls for start the ECG Animation
     */
    private void startECGAnimationSetControls(){
        stopAnimateButton.setEnabled(true);

        //exportButton.setEnabled(true);
        clearButton.setEnabled(false);
        generateButton.setEnabled(false);
        paramButton.setEnabled(false);

        zoomInButton.setEnabled(false);
        zoomOutButton.setEnabled(false);
        
        startAnimateButton.setEnabled(false);
    }

    /*
     * Set the appropiate state of the controls for stop the ECG Animation
     */
    private void stopECGAnimationSetControls(){
        startAnimateButton.setEnabled(true);

        clearButton.setEnabled(true);
        generateButton.setEnabled(true);
        paramButton.setEnabled(true);

        zoomInButton.setEnabled(true);
        zoomOutButton.setEnabled(true);

        stopAnimateButton.setEnabled(false);
    }    

    /*
     * Enable the buttons after generating the ecg Data.
     */
    private void enableButtons(){
        startAnimateButton.setEnabled(true);
        exportButton.setEnabled(true);
        clearButton.setEnabled(true);
        zoomInButton.setEnabled(true);
        zoomOutButton.setEnabled(true);
    }

    private void resetButtons(){
        stopAnimateButton.setEnabled(false);
        startAnimateButton.setEnabled(false);
        exportButton.setEnabled(false);        
        clearButton.setEnabled(false);
        zoomInButton.setEnabled(false);
        zoomOutButton.setEnabled(false);
    }

    private void resetPlotArea(){
        lblMaxAmplitude.setText("1.4");
        lblMinAmplitude.setText("-1.4");
        readyToPlot = false;
        plotScrollBarValue = 0;
    }

    private void resetStatusBar(){
        txtStatus.setText(null);
        txtStatus.append("**********************************************************\n");
        txtStatus.append("Change desired ECG Parameters\n");
        txtStatus.append("and then click 'Generate' button to generate and plot data\n");
        txtStatus.append("**********************************************************");
    }

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        desktopPane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

        paramDialog.hide();
        // Delete the DataTable
        clearDataTable();
        resetECG();
        ecgFrame.repaint();

        desktopPane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));        
    }//GEN-LAST:event_clearButtonActionPerformed

    private void stopAnimateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopAnimateButtonActionPerformed
        paramDialog.hide();
        /* Stop the Animate Plotting Task */
        ecgAnimateTimer.cancel();
        ecgAnimateFlg =false;
        /* Enable automatic plot */
        readyToPlot = true;
        /* Repaint Plot Area */
        ecgFrame.repaint();

        /* Set the Animate Buttons */
        stopECGAnimationSetControls();
    }//GEN-LAST:event_stopAnimateButtonActionPerformed

    private boolean checkParameters(){
        txtStatus.append("Starting to check ECG parameters entered...\n");

        boolean RetValue = true;
        //ECG Sampling frequency flag
        boolean sfecg_flg = true;
        //Internal Sampling frequency flag
        boolean sf_flg = true;

        /* General Intergace parameters */
        try {
            N = Integer.valueOf(txtN.getText()).intValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'Approximate number of heart beats' entered, please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }

        try {
            sfecg = Integer.valueOf(txtSfecg.getText()).intValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'ECG Sampling Frequency' entered, please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            sfecg_flg = false;
            RetValue = false;
        }

        if(sfecg_flg){
            try {
                sf = Integer.valueOf(txtSf.getText()).intValue();
            } catch(java.lang.NumberFormatException e){
                txtStatus.append("Incorrect 'Internal Sampling Frequency' entered, please correct it!\n");
                txtStatus.append("Exception Error : " + e + "\n");
                sf_flg = false;
                RetValue = false;
            }
        }

        // Check the Internal frequency respect to ECG frequency
        if(sfecg_flg && sf_flg){
            if(((int)Math.IEEEremainder(sf, sfecg)) != 0){
                txtStatus.append("Internal sampling frequency must be an integer multiple of the\n"); 
                txtStatus.append("ECG sampling frequency!, that currently is = " + sfecg + " Hertz\n"); 
                RetValue = false;
            }
        }

        try {
            Anoise = Double.valueOf(txtAnoise.getText()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'Amplitude of additive uniform noise' entered, please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }

        try {
            hrmean = Double.valueOf(txtHrmean.getText()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'Heart rate mean' entered, please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }

        try {
            hrstd = Double.valueOf(txtHrstd.getText()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'Heart rate standard deviation' entered, please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }
        
        try {
            seed = Integer.valueOf(txtSeed.getText()).intValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'seed' entered, please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }
       
        try {
            amplitude = Double.valueOf(txtAmplitude.getText()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'Plot Area Amplitude' entered, please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }

        /* Spectral Characteristics parameters */

        try {
            flo = Double.valueOf(txtFlo.getText()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'Low frequency' entered, please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }

        try {
            fhi = Double.valueOf(txtFhi.getText()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'High frequency' entered, please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }

        try {
            flostd = Double.valueOf(txtFlostd.getText()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'Low frequency standard deviation' entered, please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }

        try {
            fhistd = Double.valueOf(txtFhistd.getText()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'High frequency standard deviation' entered, please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }

        try {
            lfhfratio = Double.valueOf(txtLfhfratio.getText()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'LF/HF ratio' entered, please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }

        /*
         * ECG morphology: Order of extrema: [P Q R S T]
         */
        // theta
        try {
            theta[1] = Double.valueOf(tiTable.getValueAt(0,0).toString()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'theta' value entered (position 1), please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }
        try {
            theta[2] = Double.valueOf(tiTable.getValueAt(1,0).toString()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'theta' value entered (position 2), please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }
        try {
            theta[3] = Double.valueOf(tiTable.getValueAt(2,0).toString()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'theta' value entered (position 3), please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }
        try {
            theta[4] = Double.valueOf(tiTable.getValueAt(3,0).toString()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'theta' value entered (position 4), please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }
        try {
            theta[5] = Double.valueOf(tiTable.getValueAt(4,0).toString()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'theta' value entered (position 5), please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }

        // a
        try {
            a[1] = Double.valueOf(aiTable.getValueAt(0,0).toString()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'a' value entered (position 1), please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }
        try {
            a[2] = Double.valueOf(aiTable.getValueAt(1,0).toString()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'a' value entered (position 2), please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }
        try {
            a[3] = Double.valueOf(aiTable.getValueAt(2,0).toString()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'a' value entered (position 3), please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }
        try {
            a[4] = Double.valueOf(aiTable.getValueAt(3,0).toString()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'a' value entered (position 4), please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }
        try {
            a[5] = Double.valueOf(aiTable.getValueAt(4,0).toString()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'a' value entered (position 5), please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }

        // b
        try {
            b[1] = Double.valueOf(biTable.getValueAt(0,0).toString()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'b' value entered (position 1), please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }
        try {
            b[2] = Double.valueOf(biTable.getValueAt(1,0).toString()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'b' value entered (position 2), please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }
        try {
            b[3] = Double.valueOf(biTable.getValueAt(2,0).toString()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'b' value entered (position 3), please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }
        try {
            b[4] = Double.valueOf(biTable.getValueAt(3,0).toString()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'b' value entered (position 4), please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }
        try {
            b[5] = Double.valueOf(biTable.getValueAt(4,0).toString()).doubleValue();
        } catch(java.lang.NumberFormatException e){
            txtStatus.append("Incorrect 'b' value entered (position 5), please correct it!\n");
            txtStatus.append("Exception Error : " + e + "\n");
            RetValue = false;
        }

        /*
         * ECG Animate parameters
         */
        // convert into miliseconds interval
        ecgAnimateInterval = (long)(1000/(sfecg));        

        if(RetValue){
            txtStatus.append("All parameters are valid!.\n");
        }else{
            txtStatus.append("There were errors in some parameters!.\n");
        }

        txtStatus.append("Finished checking ECG parameters.\n\n");

        return(RetValue);
    }

    /*************************
     * THE ECG FUNCTION
     **************************/
    boolean ecgFunction(){
        boolean RetValue;

        txtStatus.append("Starting to generate ECG table data....\n");
        
        ecgCalc Ecg = new ecgCalc();
        RetValue = Ecg.dorun();

        txtStatus.append("Finished generating ECG table data.\n\n");

        return(RetValue);
    }
    
    private void generateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateButtonActionPerformed
        desktopPane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
        paramDialog.hide();

        /*
         * Clear Status text.
         */
        txtStatus.setText(null);
        txtStatus.append("************************************************************\n");
        txtStatus.append("ECGSYN:\nA program for generating a realistic synthetic ECG\n\n"); 
        txtStatus.append("Copyright (c) 2003 by Patrick McSharry & Gari Clifford.\n");
        txtStatus.append("All rights reserved.\n");
        txtStatus.append("See IEEE Transactions On Biomedical Engineering, 50(3),\n289-294, March 2003.\n\n");
        txtStatus.append("Contact:\nP. McSharry (patrick@mcsharry.net)\nG. Clifford (gari@mit.edu)\n");
        txtStatus.append("************************************************************\n\n");
        txtStatus.append("ECG process started.\n\n");       
        txtStatus.append("Starting to clear table data and widgets values....\n");

        /*
         * Set the Amplitude labels
         */
        lblMaxAmplitude.setText(txtAmplitude.getText());
        lblMinAmplitude.setText("-" + txtAmplitude.getText());

        /*
         * Re init the plot state.
         * Disable repaint for the moment, until we finish the FFT function.
         */
        readyToPlot = false;
        plotScrollBarValue = 0;
        plotScrollBar.setMaximum(0);

        /* Delete any data on the Data Table. */
        clearDataTable();

        txtStatus.append("Finished clearing table data and widgets values.\n\n");
        /*
         * Call the ECG funtion to calculate the data into the Data Table.
         */
        if(ecgFunction()){

            txtStatus.append("Starting to plot ECG table data....\n");
            
            /*
            * if the # Data Table rows is less than the ecgFrame width, we do not
            * need the scrollbar
            */
            int rows = tableValuesModel.getRowCount();
            if(rows > ecgFrame.getBounds().width){
                plotScrollBar.setMaximum(rows - ecgFrame.getBounds().width - 1); 
            }

            /*
            * Only plot if there's data in the table.
            */
            if(rows > 0){
                readyToPlot = true;
                ecgGenerated = true;
                enableButtons();
            }else{
                txtStatus.append("No data to plot!.\n");
            }

            ecgFrame.repaint();
            txtStatus.append("Finished plotting ECG table data.\n\n");

        }
        txtStatus.append("Finsihed ECG process.\n");
        txtStatus.append("************************************************************\n");

        desktopPane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_generateButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane ExtremaLabelScrollPane;
    private javax.swing.JTable ExtremaLabelTable;
    private javax.swing.JScrollPane TableScrollPane;
    private javax.swing.JScrollPane aiScrollPane;
    private javax.swing.JTable aiTable;
    private javax.swing.JDialog alert;
    private javax.swing.JButton alertButton;
    private javax.swing.JDesktopPane alertDesktopPane;
    private javax.swing.JTextArea alertText;
    private javax.swing.JLabel alertTitle;
    private javax.swing.JDesktopPane animateDesktopPane;
    private javax.swing.JScrollPane biScrollPane;
    private javax.swing.JTable biTable;
    private javax.swing.JDesktopPane calculateDesktopPane;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton closeParamDialogButton;
    private javax.swing.JDesktopPane desktopPane;
    private javax.swing.JScrollPane ecgPlotArea;
    private javax.swing.JInternalFrame ecgWindow;
    private javax.swing.JButton exportButton;
    private javax.swing.JDialog exportDialog;
    private javax.swing.JPanel extremaPanel;
    private javax.swing.JPanel generalInterfacePanel;
    private javax.swing.JButton generateButton;
    private javax.swing.JDialog helpDialog;
    private javax.swing.JEditorPane helpEditorPane;
    private javax.swing.JInternalFrame helpInternalFrame;
    private javax.swing.JScrollPane helpScrollPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel lblAmplitude;
    private javax.swing.JLabel lblAnoise;
    private javax.swing.JLabel lblFhi;
    private javax.swing.JLabel lblFhistd;
    private javax.swing.JLabel lblFlo;
    private javax.swing.JLabel lblFlostd;
    private javax.swing.JLabel lblGeneralTitle;
    private javax.swing.JLabel lblHrmean;
    private javax.swing.JLabel lblHrstd;
    private javax.swing.JLabel lblLfhfratio;
    private javax.swing.JLabel lblMaxAmplitude;
    private javax.swing.JLabel lblMinAmplitude;
    private javax.swing.JLabel lblMorphologyTitle;
    private javax.swing.JLabel lblN;
    private javax.swing.JLabel lblOrigin;
    private javax.swing.JLabel lblSeed;
    private javax.swing.JLabel lblSf;
    private javax.swing.JLabel lblSfecg;
    private javax.swing.JLabel lblSpectralTitle;
    private javax.swing.JLabel lblXAxis;
    private javax.swing.JButton paramButton;
    private javax.swing.JDesktopPane paramDesktopPane;
    private javax.swing.JDialog paramDialog;
    private javax.swing.JButton paramHelpButton;
    private javax.swing.JTabbedPane paramTabbedPane;
    private javax.swing.JScrollBar plotScrollBar;
    private javax.swing.JButton resetParamDialogButton;
    private javax.swing.JButton saveParamDialogButton;
    private javax.swing.JPanel spectralCharacteristicsPanel;
    private javax.swing.JButton startAnimateButton;
    private javax.swing.JScrollPane statusScrollPane;
    private javax.swing.JButton stopAnimateButton;
    private javax.swing.JTable tableValues;
    private javax.swing.JScrollPane tiScrollPane;
    private javax.swing.JTable tiTable;
    private javax.swing.JTextField txtAmplitude;
    private javax.swing.JTextField txtAnoise;
    private javax.swing.JTextField txtFhi;
    private javax.swing.JTextField txtFhistd;
    private javax.swing.JTextField txtFlo;
    private javax.swing.JTextField txtFlostd;
    private javax.swing.JTextField txtHrmean;
    private javax.swing.JTextField txtHrstd;
    private javax.swing.JTextField txtLfhfratio;
    private javax.swing.JTextField txtN;
    private javax.swing.JTextField txtSeed;
    private javax.swing.JTextField txtSf;
    private javax.swing.JTextField txtSfecg;
    private javax.swing.JTextArea txtStatus;
    private javax.swing.JDesktopPane zommDesktopPane;
    private javax.swing.JButton zoomInButton;
    private javax.swing.JButton zoomOutButton;
    // End of variables declaration//GEN-END:variables

    private javax.swing.table.DefaultTableModel tableValuesModel;
    
    private ecgPanel ecgFrame;

    /*
    * This class is the AdjustmentListener for the
    * scroll bar. So the events come here when the
    * scroll bar is moved.
    */
    public void adjustmentValueChanged(AdjustmentEvent evt){
    plotScrollBarValue = plotScrollBar.getValue();
    ecgFrame.repaint();
    }

    class ecgPanel extends javax.swing.JPanel{

        public void paintComponent(Graphics g){
            // First call the paintComponent of the
            // superclass, in this case JPanel.
            super.paintComponent(g);

            /* Draw the plot frame. */
            g.setColor(frameLineColor);
            g.drawLine(0, posFrameY, ecgFrame.getBounds().width, posFrameY);
            g.drawLine(0, posOriginY, this.getBounds().width, posOriginY);
            g.drawLine(0, horzScaleY, this.getBounds().width, horzScaleY);

            if(readyToPlot){
                int rows = tableValuesModel.getRowCount();
                int x, y, i;
                int plotLimit;
                int initialZero;
                int curSecond, lastSecond;
                String strValue;

                /*
                 * Set the first point to the current Table row
                 */
                initialZero =  (int)(Double.valueOf(tableValues.getValueAt(plotScrollBarValue, 0).toString()).doubleValue() / plotZoom);
                lastSecond = (int)(Double.valueOf(tableValues.getValueAt(plotScrollBarValue, 0).toString()).doubleValue());
                x = 0;
                y = posOriginY - (int)(Double.valueOf(tableValues.getValueAt(plotScrollBarValue, 1).toString()).doubleValue() * frameAmplitude / amplitude);
                Point lastPoint = new java.awt.Point(x, y);
                i= plotScrollBarValue;

                while((x <= this.getBounds().width)&& (i <=rows)){
                    curSecond = (int)(Double.valueOf(tableValues.getValueAt(i, 0).toString()).doubleValue());
                    if(curSecond > lastSecond){
                        lastSecond = curSecond;
                        // Convert the x value to a string
                        strValue = FormatNumber.toString(Double.valueOf(tableValues.getValueAt(i, 0).toString()).doubleValue(), upLimit, loLimit, 2);
                        /*
                         * Plot the X axes number values (the Time).
                         */
                        g.setColor(axesNumColor);
                        drawText(g, strValue,
                        x, horzScaleY, horzScaleWidth, horzScaleHeight,
                        fScaleNumSize,LEFT);
                        g.setColor(frameInsideLineColor);
                        g.drawLine(x, posFrameY, x, horzScaleY + 5);
                    }

                    /*
                     * Plot a line between the las point and the current point.
                     * This to create a illusion to connect the two points.
                     */
                    g.setColor(ecgPlotColor);
                    g.drawLine(lastPoint.x, lastPoint.y, x, y);

                    /*
                     * Set the current point to be the last, and 
                     * get a new point to plot in the following loop.
                     */
                    lastPoint.setLocation(x, y);
                    i+= 1;
                    x = (int)(Double.valueOf(tableValues.getValueAt(i, 0).toString()).doubleValue() / plotZoom) - initialZero;
                    y = posOriginY - (int)(Double.valueOf(tableValues.getValueAt(i, 1).toString()).doubleValue() * frameAmplitude / amplitude);
                }
            }
        }
    }

    class ecgCalc{

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
        private String outfile ="ecgsyn.dat";
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
         * Variables for static function rand()
         */
        private long iy;
        private long[] iv;

        public ecgCalc(){
            /* variables for static function ranq() */
            iy=0;
            iv = new long[NTAB];                
        }

        /*--------------------------------------------------------------------------*/
        /*    UNIFORM DEVIATES                                                      */
        /*--------------------------------------------------------------------------*/

        private double rand(){

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


            zbase = 0.005* Math.sin(2.0*PI*fhi*t0);

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
               ph0[i] = 2.0*PI*rand();

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
            d = (int)Math.ceil(sfecg/64);
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
        public boolean dorun(){

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
            q = (int) Math.rint(sf/sfecg);
            qd = (double)sf/(double)sfecg;

            /* convert angles from degrees to radians and copy a vector to ai*/
            for(i=1; i <= 5; i++){
                ti[i] = theta[i] * PI/180.0;
                ai[i] = a[i];
            }

            /* adjust extrema parameters for mean heart rate */
            hrfact =  Math.sqrt(hrmean/60);
            hrfact2 = Math.sqrt(hrfact);

            for(i=1; i <= 5; i++)
               bi[i] = b[i] * hrfact;

            ti[1] *= hrfact2;
            ti[2] *= hrfact;
            ti[3] *= 1.0;
            ti[4] *= hrfact;
            ti[5] *= 1.0;

            /* declare state vector */
            //x=dvector(1,mstate);
            x= new double[4];

            txtStatus.append("Approximate number of heart beats: " + N +"\n");
            txtStatus.append("ECG sampling frequency: " + sfecg + " Hertz\n");
            txtStatus.append("Internal sampling frequency: " + sf + " Hertz\n");
            txtStatus.append("Amplitude of additive uniformly distributed noise: " + Anoise + " mV\n");
            txtStatus.append("Heart rate mean: " + hrmean + " beats per minute\n");
            txtStatus.append("Heart rate std: " + hrstd + " beats per minute\n");
            txtStatus.append("Low frequency: " + flo + " Hertz\n");
            txtStatus.append("High frequency std: " + fhistd + " Hertz\n");
            txtStatus.append("Low frequency std: " + flostd + " Hertz\n");
            txtStatus.append("High frequency: " + fhi + " Hertz\n");
            txtStatus.append("LF/HF ratio: " + lfhfratio + "\n");
            txtStatus.append("time step milliseconds: " + ecgAnimateInterval + "\n\n");
            txtStatus.append("Order of Extrema:\n");
            txtStatus.append("      theta(radians)\n");
            txtStatus.append("P: ["+ ti[1] + "\t]\n");
            txtStatus.append("Q: ["+ ti[2] + "\t]\n");
            txtStatus.append("R: ["+ ti[3] + "\t]\n");
            txtStatus.append("S: ["+ ti[4] + "\t]\n");
            txtStatus.append("T: ["+ ti[5] + "\t]\n\n");
            txtStatus.append("      a(calculated)\n");
            txtStatus.append("P: ["+ ai[1] + "\t]\n");
            txtStatus.append("Q: ["+ ai[2] + "\t]\n");
            txtStatus.append("R: ["+ ai[3] + "\t]\n");
            txtStatus.append("S: ["+ ai[4] + "\t]\n");
            txtStatus.append("T: ["+ ai[5] + "\t]\n\n");
            txtStatus.append("      b(calculated)\n");
            txtStatus.append("P: ["+ bi[1] + "\t]\n");
            txtStatus.append("Q: ["+ bi[2] + "\t]\n");
            txtStatus.append("R: ["+ bi[3] + "\t]\n");
            txtStatus.append("S: ["+ bi[4] + "\t]\n");
            txtStatus.append("T: ["+ bi[5] + "\t]\n\n");

            /* Initialise the vector */
            x[1] = xinitial; 
            x[2] = yinitial;
            x[3] = zinitial;

            /* initialise seed */
            rseed = -seed;

            /* calculate time scales */
            h = 1.0/(double)sf;
            tstep = 1.0/(double)sfecg;

            /* calculate length of RR time series */            
            rrmean = (60.0/hrmean);
            Nrr=(int)Math.pow(2.0, Math.ceil(Math.log(N*rrmean*sf)/Math.log(2.0))); 

            txtStatus.append("Using " + Nrr + " = 2^ "+ (int)(Math.log(1.0*Nrr)/Math.log(2.0)) + " samples for calculating RR intervals\n");

            /* create rrprocess with required spectrum */
            rr = new double[Nrr + 1];
            rrprocess(rr, flo, fhi, flostd, fhistd, lfhfratio, hrmean, hrstd, sf, Nrr); 

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
                zts[i] += Anoise*(2.0*rand() - 1.0);

            /*
             * insert into the ECG data table
             */
            txtStatus.append("Inserting rows...\n");
            for(i=1;i<=Nts;i++){
                //fprintf(fp,"%f %f %d\n",(i-1)*tstep,zts[i],(int)ipeak[i]);
                Vector nuevoRow = new Vector(3);
                nuevoRow.addElement(new String(Double.toString((i-1)*tstep)));
                nuevoRow.addElement(new String(Double.toString(zts[i])));
                nuevoRow.addElement(new String(Integer.toString((int)ipeak[i])));
                tableValuesModel.addRow(nuevoRow);                
            }

            txtStatus.append("Finished inserting (" + i + ") rows.\n");

            //RetValue = false;

            return(RetValue);
        }
    }

    /*
     * Class to plot the ECG animation
     */
    class ECGAnimate extends TimerTask{

        int x = 0;
        int y = posOriginY - (int)(Double.valueOf(tableValues.getValueAt(ecgAnimateCurRow, 1).toString()).doubleValue() * frameAmplitude / amplitude);
        int curSecond = 0;
        int lastSecond = 0;
        Graphics ga = ecgFrame.getGraphics();

        public void run(){
            curSecond = (int)(Double.valueOf(tableValues.getValueAt(ecgAnimateCurRow, 0).toString()).doubleValue());
            if(curSecond > lastSecond){
                lastSecond = curSecond;
                /*
                 * Plot the X axes number values (the Time).
                 */
                ga.setColor(axesNumColor);
                drawText(ga, FormatNumber.toString(Double.valueOf(tableValues.getValueAt(ecgAnimateCurRow, 0).toString()).doubleValue(), upLimit, loLimit, 2),
                x, horzScaleY, horzScaleWidth, horzScaleHeight,
                fScaleNumSize,LEFT);

                ga.setColor(frameInsideLineColor);
                ga.drawLine(x, posFrameY, x, horzScaleY + 5);

            }

            ga.setColor(ecgPlotColor);
            ga.drawLine(ecgAnimateLastPoint.x, ecgAnimateLastPoint.y, x, y);
            ecgAnimateCurRow += 1;

            if(ecgAnimateCurRow >= ecgAnimateNumRows){
                /*
                 * If we reach the end of the Data Table, loop again entire table.
                 */
                ecgFrame.repaint();
                ecgAnimateCurRow = 0;
                ecgAnimateInitialZero = 0;
                x = 0;
                y = posOriginY - (int)(Double.valueOf(tableValues.getValueAt(ecgAnimateCurRow, 1).toString()).doubleValue() * frameAmplitude / amplitude);
                ecgAnimateLastPoint.setLocation(x, y);
                curSecond  = 0;
                lastSecond = 0;

            } else if(x > ecgAnimatePanelWidth){
                /*
                 * If we not reached the end of the Data Table, but we reach to the limit of
                 * the Plot Area. so reset the X coordinate to begin again.
                 */
                ecgFrame.repaint();
                x = 0;
                y = posOriginY - (int)(Double.valueOf(tableValues.getValueAt(ecgAnimateCurRow, 1).toString()).doubleValue() * frameAmplitude / amplitude);
                ecgAnimateInitialZero = (int)(Double.valueOf(tableValues.getValueAt(ecgAnimateCurRow, 0).toString()).doubleValue() / plotZoom);
                ecgAnimateLastPoint.setLocation(x, y);
                //curSecond  = 0;
                //lastSecond = 0;
            } else{
                ecgAnimateLastPoint.setLocation(x, y);
                x = (int)(Double.valueOf(tableValues.getValueAt(ecgAnimateCurRow, 0).toString()).doubleValue() / plotZoom) - ecgAnimateInitialZero;
                y = posOriginY - (int)(Double.valueOf(tableValues.getValueAt(ecgAnimateCurRow, 1).toString()).doubleValue() * frameAmplitude / amplitude);                
            }
        }
    }

    /*
     * class for verifying that the value of JtextField is a double number
     */
    class doubleVerifier extends InputVerifier{
        
        public boolean verify(JComponent input) {
            JTextField textF = (JTextField) input;
            double dNumber;
            boolean RetValue = true;

            try {
                dNumber = Double.valueOf(textF.getText()).doubleValue();
            } catch(java.lang.NumberFormatException e){
                getToolkit().beep();
                alert.setTitle("Error!!!");
                alertText.setText("You have to enter a double number, please retry!!!");
                alert.show();
                RetValue = false;
            }
            return(RetValue);
        }
    }

    /*
     * class for verifying that the value of JtextField is an integer number
     */    
    class integerVerifier extends InputVerifier{
        
        public boolean verify(JComponent input) {
            JTextField textF = (JTextField) input;
            int dNumber;
            boolean RetValue = true;

            try {
                dNumber = Integer.valueOf(textF.getText()).intValue();
            } catch(java.lang.NumberFormatException e){
                getToolkit().beep();
                alert.setTitle("Error!!!");
                alertText.setText("You have to enter an integer number, please retry!!!");
                alert.show();
                RetValue = false;
            }
            return(RetValue);
        }
    }

    /*
     * class for verifying the value of the ECG Sampling frequency
     */    
    class sfecgVerifier extends InputVerifier{
        
        public boolean verify(JComponent input) {
            JTextField textF = (JTextField) input;
            int dNumber;
            boolean RetValue = true;

            try {
                dNumber = Integer.valueOf(textF.getText()).intValue();
                txtSf.setText(Integer.toString(dNumber));
            } catch(java.lang.NumberFormatException e){
                getToolkit().beep();
                alert.setTitle("Error!!!");
                alertText.setText("You have to enter an integer number, please retry!!!");
                alert.show();
                RetValue = false;
            }
            return(RetValue);
        }
    }

    /*
     * class for verifying the value of the Internal Sampling frequency
     */    
    class sfVerifier extends InputVerifier{
        
        public boolean verify(JComponent input) {
            JTextField textF = (JTextField) input;
            int dNumber;
            boolean RetValue = true;

            try {
                dNumber = Integer.valueOf(textF.getText()).intValue();
                int sfecgTemp;
                sfecgTemp = Integer.valueOf(txtSfecg.getText()).intValue();
                if(((int)Math.IEEEremainder(dNumber, sfecgTemp)) != 0){
                    getToolkit().beep();
                    alert.setTitle("Error!!!");
                    alertText.setText("Internal sampling frequency must be an integer multiple of the ECG sampling frequency, please retry!");
                    alert.show();
                    RetValue = false;
                }
            } catch(java.lang.NumberFormatException e){
                getToolkit().beep();
                alert.setTitle("Error!!!");
                alertText.setText("You have to enter an integer number, please retry!!!");
                alert.show();
                RetValue = false;
            }
            return(RetValue);
        }
    }
}
