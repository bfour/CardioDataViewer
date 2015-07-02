package at.ac.tuwien.e0826357.cardioDataCreater.service.MITECGGenerator;

/*
 * ecgLogWindow.java
 *
 * See EcgLicense.txt for License terms.
 */

/**
 *
 * @author  Mauricio Villarroel (m.villarroel@acm.og)
 */

import java.util.Date;
import java.text.DateFormat;
import javax.swing.JOptionPane;
import javax.swing.JInternalFrame;
import java.io.*;
import javax.swing.JFileChooser;

public class EcgLogWindow extends javax.swing.JInternalFrame {
    
    /** Creates new form ecgLogWindow */
    public EcgLogWindow() {
        initComponents();
        startLog();
        this.setSize(740,460);
    }
    
    public void startLog(){
        txtStatus.append("ECG Application Started on " + getCurDateTime() + "\n");
        
    }

    public void println(String value){
        txtStatus.append(getCurShortTime() + value + "\n");
        
    }

    public void print(String value){
        txtStatus.append(getCurShortTime() + value);
        
    }

    public void clearLog(){
        txtStatus.setText(null);
        
    }

    private DateFormat shorTime = DateFormat.getTimeInstance();
    private DateFormat longDateTime = DateFormat.getDateTimeInstance();

    private String getCurShortTime(){
        return "[" + shorTime.format(new Date()) + "]: ";
    }

    private String getCurDateTime(){
        return "[" + longDateTime.format(new Date()) + "]: ";
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        statusScrollPane = new javax.swing.JScrollPane();
        txtStatus = new javax.swing.JTextArea();
        clearButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("ECG System Log");
        jPanel1.setLayout(null);

        statusScrollPane.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED), "Status and Messages:", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.ABOVE_TOP));
        statusScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        txtStatus.setEditable(false);
        statusScrollPane.setViewportView(txtStatus);

        jPanel1.add(statusScrollPane);
        statusScrollPane.setBounds(0, 0, 730, 390);

        clearButton.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11));
        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        jPanel1.add(clearButton);
        clearButton.setBounds(540, 400, 80, 25);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        jPanel1.add(closeButton);
        closeButton.setBounds(640, 400, 80, 25);

        saveButton.setFont(new java.awt.Font("MS Sans Serif", 0, 10));
        saveButton.setText("Save log");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        jPanel1.add(saveButton);
        saveButton.setBounds(420, 400, 100, 25);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        JFileChooser c = new JFileChooser();
        /* Open "Save" dialog: */
        int rVal = c.showSaveDialog(this);
        if(rVal == JFileChooser.APPROVE_OPTION) {
            File file = c.getSelectedFile();
            try {
                FileWriter fw = new FileWriter(file);
                fw.write("ECG Log:\r\n");
                txtStatus.write(fw);
                fw.close();
                JOptionPane.showMessageDialog(this, "Log was saved successfully!");
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        // TODO add your handling code here:
        clearLog();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // TODO add your handling code here:
        this.setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton saveButton;
    private javax.swing.JScrollPane statusScrollPane;
    private javax.swing.JTextArea txtStatus;
    // End of variables declaration//GEN-END:variables
    
}
