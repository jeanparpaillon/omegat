/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Didier Briel
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.dialogs;

import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;

/**
 * The dialog to change the font of OmegaT windows.
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
@SuppressWarnings("serial")
public class FontSelectionDialog extends javax.swing.JDialog {
    /**
     * A return status code - returned if Cancel button has been pressed or User
     * haven't changed the font
     */
    public static final int RET_CANCEL_OR_UNCHANGED = 0;
    /**
     * A return status code - returned if OK button has been pressed and User
     * have changed the font
     */
    public static final int RET_OK_CHANGED = 1;

    /** The old font, passed to this Dialog */
    private Font oldFont;

    /** New Font, selected by the user */
    public Font getSelectedFont() {
        return new Font((String) fontComboBox.getSelectedItem(), Font.PLAIN,
                ((Number) sizeSpinner.getValue()).intValue());
    }

    /** Creates new form FontSelectionDialog2 */
    public FontSelectionDialog(Frame parent, Font font) {
        super(parent, true);

        // HP
        // Handle escape key to close the window
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
        // END HP

        initComponents();

        getRootPane().setDefaultButton(okButton);

        oldFont = font;
        previewTextArea.setFont(oldFont);
        fontComboBox.setSelectedItem(oldFont.getName());
        sizeSpinner.setValue(new Integer(oldFont.getSize()));
        applyToProjectFilesCheckBox.setSelected(Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT));

    }

    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed"
    // desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        fontComboBox = new javax.swing.JComboBox(StaticUtils.getFontNames());
        fontLabel = new javax.swing.JLabel();
        sizeSpinner = new javax.swing.JSpinner();
        sizeLabel = new javax.swing.JLabel();
        previewTextArea = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        applyToProjectFilesCheckBox = new javax.swing.JCheckBox();
        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setTitle(OStrings.getString("TF_SELECT_FONTS_TITLE"));
        setModal(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        fontComboBox.setMaximumRowCount(20);
        fontComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(fontComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(fontLabel, OStrings.getString("TF_SELECT_SOURCE_FONT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(fontLabel, gridBagConstraints);

        sizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sizeSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(sizeSpinner, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(sizeLabel, OStrings.getString("TF_SELECT_FONTSIZE"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(sizeLabel, gridBagConstraints);

        previewTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        previewTextArea.setEditable(false);
        previewTextArea.setLineWrap(true);
        previewTextArea.setText(OStrings.getString("TF_FONT_SAMPLE_TEXT"));
        previewTextArea.setWrapStyleWord(true);
        previewTextArea.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
                OStrings.getString("TF_FONT_SAMPLE_TITLE"),
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION, fontLabel.getFont()));
        previewTextArea.setPreferredSize(new java.awt.Dimension(116, 100));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(previewTextArea, gridBagConstraints);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        org.openide.awt.Mnemonics.setLocalizedText(applyToProjectFilesCheckBox,
                OStrings.getString("TF_APPLY_TO_PROJECT_FILES"));
        applyToProjectFilesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyToProjectFilesCheckBoxActionPerformed(evt);
            }
        });
        jPanel1.add(applyToProjectFilesCheckBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanel1, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        org.openide.awt.Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(okButton);

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(cancelButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        getContentPane().add(buttonPanel, gridBagConstraints);

        pack();
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension dialogSize = getSize();
        setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);
    }// </editor-fold>//GEN-END:initComponents

    private void fontComboBoxActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_fontComboBoxActionPerformed
    {// GEN-HEADEREND:event_fontComboBoxActionPerformed
        previewTextArea.setFont(getSelectedFont());
    }// GEN-LAST:event_fontComboBoxActionPerformed

    private void sizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt)// GEN-FIRST:event_sizeSpinnerStateChanged
    {// GEN-HEADEREND:event_sizeSpinnerStateChanged
        previewTextArea.setFont(getSelectedFont());
    }// GEN-LAST:event_sizeSpinnerStateChanged

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_okButtonActionPerformed
    {
        if ((!getSelectedFont().equals(oldFont))
                || (applyToProjectFilesCheckBox.isSelected() != Preferences
                        .isPreference(Preferences.PROJECT_FILES_USE_FONT))) {
            Preferences.setPreference(Preferences.PROJECT_FILES_USE_FONT,
                    applyToProjectFilesCheckBox.isSelected());
            doClose(RET_OK_CHANGED);
        } else
            doClose(RET_CANCEL_OR_UNCHANGED);
    }// GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_cancelButtonActionPerformed
    {
        doClose(RET_CANCEL_OR_UNCHANGED);
    }// GEN-LAST:event_cancelButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt)// GEN-FIRST:event_closeDialog
    {
        doClose(RET_CANCEL_OR_UNCHANGED);
    }// GEN-LAST:event_closeDialog

    private void applyToProjectFilesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_applyToProjectFilesCheckBoxActionPerformed
    // TODO add your handling code here:
    }// GEN-LAST:event_applyToProjectFilesCheckBoxActionPerformed

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox applyToProjectFilesCheckBox;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox fontComboBox;
    private javax.swing.JLabel fontLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton okButton;
    private javax.swing.JTextArea previewTextArea;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JSpinner sizeSpinner;
    // End of variables declaration//GEN-END:variables

    private int returnStatus = RET_CANCEL_OR_UNCHANGED;
}
