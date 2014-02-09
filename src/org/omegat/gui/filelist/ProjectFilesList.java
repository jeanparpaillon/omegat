/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.filelist;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import org.omegat.util.OStrings;
import org.openide.awt.Mnemonics;

/**
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ProjectFilesList extends javax.swing.JFrame {

    /**
     * Creates new form ProjectFilesList
     */
    public ProjectFilesList() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        scrollFiles = new JScrollPane();
        tableFiles = new JTable();
        tableTotal = new JTable();
        statLabel = new JTextArea();
        jPanel1 = new JPanel();
        filler1 = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(32767, 0));
        m_addNewFileButton = new JButton();
        filler3 = new Box.Filler(new Dimension(5, 0), new Dimension(5, 0), new Dimension(5, 32767));
        m_wikiImportButton = new JButton();
        filler4 = new Box.Filler(new Dimension(5, 0), new Dimension(5, 0), new Dimension(5, 32767));
        m_closeButton = new JButton();
        filler2 = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(32767, 0));
        jPanel2 = new JPanel();
        filler6 = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 32767));
        btnFirst = new JButton();
        filler8 = new Box.Filler(new Dimension(0, 5), new Dimension(0, 5), new Dimension(32767, 5));
        btnUp = new JButton();
        filler7 = new Box.Filler(new Dimension(0, 20), new Dimension(0, 20), new Dimension(32767, 20));
        btnDown = new JButton();
        filler9 = new Box.Filler(new Dimension(0, 5), new Dimension(0, 5), new Dimension(32767, 5));
        btnLast = new JButton();
        filler5 = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 32767));

        getContentPane().setLayout(new GridBagLayout());

        scrollFiles.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollFiles.setViewportView(tableFiles);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(scrollFiles, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(50, 5, 10, 5);
        getContentPane().add(tableTotal, gridBagConstraints);

        statLabel.setEditable(false);
        statLabel.setColumns(20);
        statLabel.setLineWrap(true);
        statLabel.setRows(5);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(10, 0, 0, 0);
        getContentPane().add(statLabel, gridBagConstraints);

        jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.LINE_AXIS));
        jPanel1.add(filler1);

        Mnemonics.setLocalizedText(m_addNewFileButton, OStrings.getString("TF_MENU_FILE_IMPORT")); // NOI18N
        jPanel1.add(m_addNewFileButton);
        jPanel1.add(filler3);

        Mnemonics.setLocalizedText(m_wikiImportButton, OStrings.getString("TF_MENU_WIKI_IMPORT")); // NOI18N
        jPanel1.add(m_wikiImportButton);
        jPanel1.add(filler4);

        Mnemonics.setLocalizedText(m_closeButton, OStrings.getString("BUTTON_CLOSE")); // NOI18N
        jPanel1.add(m_closeButton);
        jPanel1.add(filler2);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(10, 0, 0, 0);
        getContentPane().add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new BoxLayout(jPanel2, BoxLayout.Y_AXIS));
        jPanel2.add(filler6);

        Mnemonics.setLocalizedText(btnFirst, OStrings.getString("PF_MOVE_FIRST")); // NOI18N
        jPanel2.add(btnFirst);
        jPanel2.add(filler8);

        Mnemonics.setLocalizedText(btnUp, OStrings.getString("PF_MOVE_UP")); // NOI18N
        jPanel2.add(btnUp);
        jPanel2.add(filler7);

        Mnemonics.setLocalizedText(btnDown, OStrings.getString("PF_MOVE_DOWN")); // NOI18N
        jPanel2.add(btnDown);
        jPanel2.add(filler9);

        Mnemonics.setLocalizedText(btnLast, OStrings.getString("PF_MOVE_LAST")); // NOI18N
        jPanel2.add(btnLast);
        jPanel2.add(filler5);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(jPanel2, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ProjectFilesList.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ProjectFilesList.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ProjectFilesList.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ProjectFilesList.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ProjectFilesList().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public JButton btnDown;
    public JButton btnFirst;
    public JButton btnLast;
    public JButton btnUp;
    public Box.Filler filler1;
    public Box.Filler filler2;
    public Box.Filler filler3;
    public Box.Filler filler4;
    public Box.Filler filler5;
    public Box.Filler filler6;
    public Box.Filler filler7;
    public Box.Filler filler8;
    public Box.Filler filler9;
    public JPanel jPanel1;
    public JPanel jPanel2;
    public JButton m_addNewFileButton;
    public JButton m_closeButton;
    public JButton m_wikiImportButton;
    public JScrollPane scrollFiles;
    public JTextArea statLabel;
    public JTable tableFiles;
    public JTable tableTotal;
    // End of variables declaration//GEN-END:variables
}
