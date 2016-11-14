package at.ac.tuwien.e0826357.cardioapp.wearableSimulator.service.MITECGGenerator;

/*
 * plotWindow.java
 *
 * See EcgLicense.txt for License terms.
 */

/**
 *
 * @author  Mauricio Villarroel (m.villarroel@acm.og)
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JInternalFrame;
import javax.swing.table.DefaultTableModel;

public class EcgPlotWindow extends JInternalFrame implements AdjustmentListener {
    
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
    Point ecgAnimateLastPoint = new Point(0, 0);

    /** Creates new form plotWindow */
    public EcgPlotWindow(EcgParam parameters, EcgLogWindow logOb, EcgApplication application) {
        initComponents();
        paramOb = parameters;
        calcOb = new EcgCalc(paramOb, logOb);
        ecgLog = logOb;
        mainApp = application;
        initWindow();
    }
    
    private void initWindow(){
        this.setSize(850,475);
        /*********************
        *Init the main Window
        *Set maximize
        *********************/
        /*try{
            ecgWindow.setMaximum(true);        
        } catch(java.beans.PropertyVetoException e){
            txtStatus.append("Exception Error : " + e + "\n");
        }*/       

        /*********************
        *Init the data Table
        *********************/
        tableValuesModel = new DefaultTableModel(   new Object [][] {},
                                                    new String [] {"Time", "Voltage", "Peak"}){
                                                        Class[] types = new Class [] {
                                                        String.class, String.class, String.class
                                                    };
                                                    public Class getColumnClass(int columnIndex) {
                                                        return types [columnIndex];
                                                    }
                                                };
        tableValues.setModel(tableValuesModel);

        /* Init the ecgFrame */
        ecgFrame = new ecgPanel();
        ecgFrame.setBackground(new Color(255, 255, 255));
        ecgPlotArea.setViewportView(ecgFrame);

        /* Set the ScrollBar */
        plotScrollBar.addAdjustmentListener(this);

        /*************************
         * Reset all Application 
         * to a init state.
         *************************/        
        resetECG();
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        plotToolBar = new javax.swing.JToolBar();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        generateButton = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        zoomInButton = new javax.swing.JButton();
        zoomOutButton = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        animateStartButton = new javax.swing.JButton();
        animateStopButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        mainPanel = new javax.swing.JPanel();
        ecgPlotArea = new javax.swing.JScrollPane();
        plotScrollBar = new javax.swing.JScrollBar();
        lblMinAmplitude = new javax.swing.JLabel();
        lblOrigin = new javax.swing.JLabel();
        lblMaxAmplitude = new javax.swing.JLabel();
        jTextArea1 = new javax.swing.JTextArea();
        TableScrollPane = new javax.swing.JScrollPane();
        tableValues = new javax.swing.JTable();
        closeButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Plot ECG");
        plotToolBar.setBorder(new javax.swing.border.EtchedBorder(null, Color.lightGray));
        plotToolBar.setRollover(true);
        plotToolBar.setMinimumSize(new Dimension(234, 30));
        plotToolBar.setPreferredSize(new Dimension(234, 30));
        plotToolBar.setAutoscrolls(true);
        jSeparator2.setMaximumSize(new Dimension(1000, 32767));
        plotToolBar.add(jSeparator2);

        jLabel3.setText("Generate:");
        plotToolBar.add(jLabel3);

        //generateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("img/execute2.gif")));
        generateButton.setToolTipText("Generate System");
        generateButton.setMaximumSize(new Dimension(22, 22));
        generateButton.setMinimumSize(new Dimension(22, 22));
        generateButton.setPreferredSize(new Dimension(22, 22));
        generateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                generateButtonActionPerformed(evt);
            }
        });

        plotToolBar.add(generateButton);

        jSeparator4.setMaximumSize(new Dimension(1000, 32767));
        plotToolBar.add(jSeparator4);

        jLabel2.setText("Zoom:");
        plotToolBar.add(jLabel2);

//        zoomInButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("img/tbzoomin.png")));
        zoomInButton.setToolTipText("Zoom In");
        zoomInButton.setMargin(new Insets(0, 0, 0, 0));
        zoomInButton.setMaximumSize(new Dimension(22, 22));
        zoomInButton.setMinimumSize(new Dimension(22, 22));
        zoomInButton.setPreferredSize(new Dimension(22, 22));
        zoomInButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                zoomInButtonActionPerformed(evt);
            }
        });

        plotToolBar.add(zoomInButton);

//        zoomOutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("img/tbzoomout.png")));
        zoomOutButton.setToolTipText("Zoom Out");
        zoomOutButton.setMaximumSize(new Dimension(22, 22));
        zoomOutButton.setMinimumSize(new Dimension(22, 22));
        zoomOutButton.setPreferredSize(new Dimension(22, 22));
        zoomOutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                zoomOutButtonActionPerformed(evt);
            }
        });

        plotToolBar.add(zoomOutButton);

        jSeparator3.setMaximumSize(new Dimension(1000, 32767));
        plotToolBar.add(jSeparator3);

        jLabel1.setText("Animate:");
        plotToolBar.add(jLabel1);

//        animateStartButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("img/ball_green.gif")));
        animateStartButton.setToolTipText("Start Animation");
        animateStartButton.setMaximumSize(new Dimension(22, 22));
        animateStartButton.setMinimumSize(new Dimension(22, 22));
        animateStartButton.setPreferredSize(new Dimension(22, 22));
        animateStartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                animateStartButtonActionPerformed(evt);
            }
        });

        plotToolBar.add(animateStartButton);

//        animateStopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("img/ball_red.gif")));
        animateStopButton.setToolTipText("Stop Animation");
        animateStopButton.setMaximumSize(new Dimension(22, 22));
        animateStopButton.setMinimumSize(new Dimension(22, 22));
        animateStopButton.setPreferredSize(new Dimension(22, 22));
        animateStopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                animateStopButtonActionPerformed(evt);
            }
        });

        plotToolBar.add(animateStopButton);

        plotToolBar.add(jSeparator1);

        getContentPane().add(plotToolBar, BorderLayout.NORTH);

        mainPanel.setLayout(null);

        ecgPlotArea.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED), "Plot Area", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.ABOVE_TOP));
        ecgPlotArea.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        ecgPlotArea.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        mainPanel.add(ecgPlotArea);
        ecgPlotArea.setBounds(73, 2, 580, 350);

        plotScrollBar.setMaximum(0);
        plotScrollBar.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        plotScrollBar.setName("timeScroll");
        mainPanel.add(plotScrollBar);
        plotScrollBar.setBounds(73, 352, 580, 16);

        lblMinAmplitude.setFont(new Font("Dialog", 1, 9));
        lblMinAmplitude.setText("-0.001");
        mainPanel.add(lblMinAmplitude);
        lblMinAmplitude.setBounds(30, 309, 40, 12);

        lblOrigin.setFont(new Font("Dialog", 1, 9));
        lblOrigin.setText("0.00");
        mainPanel.add(lblOrigin);
        lblOrigin.setBounds(30, 167, 40, 12);

        lblMaxAmplitude.setFont(new Font("Dialog", 1, 9));
        lblMaxAmplitude.setText("0.001");
        mainPanel.add(lblMaxAmplitude);
        lblMaxAmplitude.setBounds(30, 25, 40, 12);

        jTextArea1.setBackground(new Color(212, 208, 200));
        jTextArea1.setLineWrap(true);
        jTextArea1.setText("Voltage");
        mainPanel.add(jTextArea1);
        jTextArea1.setBounds(5, 120, 10, 130);

        TableScrollPane.setBorder(new javax.swing.border.TitledBorder(null, "Data Table", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.ABOVE_TOP));
        TableScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        TableScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        tableValues.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        TableScrollPane.setViewportView(tableValues);

        mainPanel.add(TableScrollPane);
        TableScrollPane.setBounds(660, 0, 180, 340);

        closeButton.setText("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        mainPanel.add(closeButton);
        closeButton.setBounds(750, 380, 80, 25);

//        exportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("img/disk.gif")));
        exportButton.setText("Export Data...");
        exportButton.setToolTipText("Export Table Data to a File");
        exportButton.setIconTextGap(15);
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        mainPanel.add(exportButton);
        exportButton.setBounds(660, 343, 180, 25);

        clearButton.setText("Clear");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        mainPanel.add(clearButton);
        clearButton.setBounds(630, 380, 80, 25);

        getContentPane().add(mainPanel, BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void clearButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // Delete the DataTable
        clearDataTable();
        resetECG();
        ecgFrame.repaint();

        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_clearButtonActionPerformed

    private void exportButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        EcgExportWindow exportWin = new EcgExportWindow(null, true, paramOb, calcOb, ecgLog);
        exportWin.show();
    }//GEN-LAST:event_exportButtonActionPerformed

    private void animateStopButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_animateStopButtonActionPerformed
        /* Stop the Animate Plotting Task */
        ecgAnimateTimer.cancel();
        ecgAnimateFlg =false;
        /* Enable automatic plot */
        readyToPlot = true;
        /* Repaint Plot Area */
        ecgFrame.repaint();

        /* Set the Animate Buttons */
        stopECGAnimationSetControls();
    }//GEN-LAST:event_animateStopButtonActionPerformed

    private void animateStartButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_animateStartButtonActionPerformed
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
        ecgAnimateLastPoint.setLocation(0, posOriginY - (int)(Double.valueOf(tableValues.getValueAt(0, 1).toString()).doubleValue() * frameAmplitude / paramOb.getAmplitude()));
        
        /* Create Timer */
        ecgAnimateTimer = new Timer();
        /* Schedule the Animate Plotting Task */
        ecgAnimateTimer.scheduleAtFixedRate(new ECGAnimate(), 0, paramOb.getEcgAnimateInterval());
        
        /* Set the Animate Buttons */
        startECGAnimationSetControls();
    }//GEN-LAST:event_animateStartButtonActionPerformed

    private void zoomInButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_zoomInButtonActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
        plotZoom = plotZoom / plotZoomInc;
        ecgFrame.repaint();

        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_zoomInButtonActionPerformed

    private void zoomOutButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_zoomOutButtonActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        plotZoom = plotZoom * plotZoomInc;
        ecgFrame.repaint();

        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_zoomOutButtonActionPerformed

    private void closeButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        //this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void generateButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_generateButtonActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        /*
         * Clear Status text.
         */
        ecgLog.println("************************************************************");
        ecgLog.println("ECGSYN:\nA program for generating a realistic synthetic ECG\n"); 
        ecgLog.println("Copyright (c) 2003 by Patrick McSharry & Gari Clifford.");
        ecgLog.println("All rights reserved.");
        ecgLog.println("See IEEE Transactions On Biomedical Engineering, 50(3),\n289-294, March 2003.\n");
        ecgLog.println("Contact:\nP. McSharry (patrick@mcsharry.net)\nG. Clifford (gari@mit.edu)");
        ecgLog.println("************************************************************\n");
        ecgLog.println("ECG process started.\n");       
        ecgLog.println("Starting to clear table data and widgets values....");

        /*
         * Set the Amplitude labels
         */
        lblMaxAmplitude.setText(Double.toString(paramOb.getAmplitude()));
        lblMinAmplitude.setText("-" + Double.toString(paramOb.getAmplitude()));

        /*
         * Re init the plot state.
         * Disable repaint for the moment, until we finish the FFT function.
         */
        readyToPlot = false;
        plotScrollBarValue = 0;
        plotScrollBar.setMaximum(0);

        /* Delete any data on the Data Table. */
        clearDataTable();

        ecgLog.println("Finished clearing table data and widgets values.\n");
        /*
         * Call the ECG funtion to calculate the data into the Data Table.
         */
        if(calcOb.calculateEcg()){

            fillDataTable();
            ecgLog.println("Starting to plot ECG table data....");
            
            /*
            * if the # Data Table rows is less than the ecgFrame width, we do not
            * need the scrollbar
            */
            int rows = tableValuesModel.getRowCount();
            if(rows > ecgFrame.getBounds().width){
                //JOptionPane.showMessageDialog(this, "Entro a: rows > ecgFrame.getBounds().width");
                plotScrollBar.setMaximum(rows - ecgFrame.getBounds().width - 1); 
            }

            /*
            * Only plot if there's data in the table.
            */
            if(rows > 0){
                //JOptionPane.showMessageDialog(this, "Entro a: rows > 0");
                readyToPlot = true;
                ecgGenerated = true;
                enableButtons();
            }else{
                ecgLog.println("No data to plot!.");
            }

            ecgFrame.repaint();
            ecgLog.println("Finished plotting ECG table data.\n");

        }
        ecgLog.println("Finsihed ECG process.");
        ecgLog.println("************************************************************");

        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_generateButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane TableScrollPane;
    private javax.swing.JButton animateStartButton;
    private javax.swing.JButton animateStopButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JScrollPane ecgPlotArea;
    private javax.swing.JButton exportButton;
    private javax.swing.JButton generateButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel lblMaxAmplitude;
    private javax.swing.JLabel lblMinAmplitude;
    private javax.swing.JLabel lblOrigin;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JScrollBar plotScrollBar;
    private javax.swing.JToolBar plotToolBar;
    private javax.swing.JTable tableValues;
    private javax.swing.JButton zoomInButton;
    private javax.swing.JButton zoomOutButton;
    // End of variables declaration//GEN-END:variables
    
    private EcgCalc calcOb;
    private EcgParam paramOb;
    private EcgLogWindow ecgLog;  
    private DefaultTableModel tableValuesModel;
    private ecgPanel ecgFrame;
    private EcgApplication mainApp;
  
    private void resetECG(){
        ecgGenerated = false;
        //clearParameters();
        resetPlotArea();
        resetButtons();
    }

    private void clearDataTable(){
        // Delete the DataTable
        tableValuesModel.setRowCount(0);
    }

    private void resetButtons(){
        animateStopButton.setEnabled(false);
        animateStartButton.setEnabled(false);
        exportButton.setEnabled(false);        
        clearButton.setEnabled(false);
        zoomInButton.setEnabled(false);
        zoomOutButton.setEnabled(false);
    }

    /* Enable the buttons after generating the ecg Data. */
    private void enableButtons(){
        animateStartButton.setEnabled(true);
        exportButton.setEnabled(true);
        clearButton.setEnabled(true);
        zoomInButton.setEnabled(true);
        zoomOutButton.setEnabled(true);
    }

    private void resetPlotArea(){
        lblMaxAmplitude.setText("1.4");
        lblMinAmplitude.setText("-1.4");
        readyToPlot = false;
        plotScrollBarValue = 0;
    }

    /*
     * Set the appropiate state of the controls for start the ECG Animation
     */
    private void startECGAnimationSetControls(){
        animateStopButton.setEnabled(true);

        //exportButton.setEnabled(true);
        clearButton.setEnabled(false);
        generateButton.setEnabled(false);

        zoomInButton.setEnabled(false);
        zoomOutButton.setEnabled(false);
        
        animateStartButton.setEnabled(false);
    }

    /*
     * Set the appropiate state of the controls for stop the ECG Animation
     */
    private void stopECGAnimationSetControls(){
        animateStartButton.setEnabled(true);

        clearButton.setEnabled(true);
        generateButton.setEnabled(true);
        zoomInButton.setEnabled(true);
        zoomOutButton.setEnabled(true);

        animateStopButton.setEnabled(false);
    }

    private void fillDataTable(){
        // Delete the DataTable
        for(int i=0;i<calcOb.getEcgResultNumRows();i++){
            Vector nuevoRow = new Vector(3);
            nuevoRow.addElement(new String(Double.toString(calcOb.getEcgResultTime(i))));
            nuevoRow.addElement(new String(Double.toString(calcOb.getEcgResultVoltage(i))));
            nuevoRow.addElement(new String(Integer.toString(calcOb.getEcgResultPeak(i))));
            tableValuesModel.addRow(nuevoRow);
        }
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
                //JOptionPane.showMessageDialog(EcgPlotWindow.this, "Entro en la funcion paint");
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
                y = posOriginY - (int)(Double.valueOf(tableValues.getValueAt(plotScrollBarValue, 1).toString()).doubleValue() * frameAmplitude / paramOb.getAmplitude());
                Point lastPoint = new Point(x, y);
                i= plotScrollBarValue;

                while((x <= this.getBounds().width)&& (i <=rows)){
                    //JOptionPane.showMessageDialog(EcgPlotWindow.this, "Entro al while de paint");
                    curSecond = (int)(Double.valueOf(tableValues.getValueAt(i, 0).toString()).doubleValue());
                    if(curSecond > lastSecond){
                        lastSecond = curSecond;
                        // Convert the x value to a string
                        strValue = EcgFormatNumber.toString(Double.valueOf(tableValues.getValueAt(i, 0).toString()).doubleValue(), upLimit, loLimit, 2);
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
                    y = posOriginY - (int)(Double.valueOf(tableValues.getValueAt(i, 1).toString()).doubleValue() * frameAmplitude / paramOb.getAmplitude());
                }
            }
        }
    }

    /*
     * Class to plot the ECG animation
     */
    class ECGAnimate extends TimerTask{

        int x = 0;
        int y = posOriginY - (int)(Double.valueOf(tableValues.getValueAt(ecgAnimateCurRow, 1).toString()).doubleValue() * frameAmplitude / paramOb.getAmplitude());
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
                drawText(ga, EcgFormatNumber.toString(Double.valueOf(tableValues.getValueAt(ecgAnimateCurRow, 0).toString()).doubleValue(), upLimit, loLimit, 2),
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
                y = posOriginY - (int)(Double.valueOf(tableValues.getValueAt(ecgAnimateCurRow, 1).toString()).doubleValue() * frameAmplitude / paramOb.getAmplitude());
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
                y = posOriginY - (int)(Double.valueOf(tableValues.getValueAt(ecgAnimateCurRow, 1).toString()).doubleValue() * frameAmplitude / paramOb.getAmplitude());
                ecgAnimateInitialZero = (int)(Double.valueOf(tableValues.getValueAt(ecgAnimateCurRow, 0).toString()).doubleValue() / plotZoom);
                ecgAnimateLastPoint.setLocation(x, y);
                //curSecond  = 0;
                //lastSecond = 0;
            } else{
                ecgAnimateLastPoint.setLocation(x, y);
                x = (int)(Double.valueOf(tableValues.getValueAt(ecgAnimateCurRow, 0).toString()).doubleValue() / plotZoom) - ecgAnimateInitialZero;
                y = posOriginY - (int)(Double.valueOf(tableValues.getValueAt(ecgAnimateCurRow, 1).toString()).doubleValue() * frameAmplitude / paramOb.getAmplitude());                
            }
        }
    }
}
