/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichick
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

package org.omegat.gui.dialogs;

import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.omegat.core.Core;
import org.omegat.core.team.GITRemoteRepository;
import org.omegat.core.team.SVNRemoteRepository;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.OmegaTFileChooser;

/**
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class NewTeamProject extends javax.swing.JDialog {
    
    public enum REPOSITORY_TYPE {
        REPO_UNKNOWN, REPO_SVN, REPO_GIT
    }
    
    public REPOSITORY_TYPE repoType = REPOSITORY_TYPE.REPO_UNKNOWN;
    
    /**
     * Creates new form NewTeamProject
     */
    public NewTeamProject(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        DocumentListener newTeamProjectOkHider = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateButton();
            }

            public void removeUpdate(DocumentEvent e) {
                updateButton();
            }

            public void insertUpdate(DocumentEvent e) {
                updateButton();
            }
        };
        
        txtDirectory.getDocument().addDocumentListener(newTeamProjectOkHider);
        txtRepositoryURL.getDocument().addDocumentListener(newTeamProjectOkHider);
    }
    
    private class RepoTypeChecker extends SwingWorker<REPOSITORY_TYPE, Object> {

        @Override
        protected REPOSITORY_TYPE doInBackground() throws Exception {
            String url = txtRepositoryURL.getText();
            if (StringUtil.isEmpty(url)) {
                    return null;
            }
            detectedRepoLabel.setText(OStrings.getString("TEAM_DETECTING_REPO"));
            if (GITRemoteRepository.isGitRepository(url)) {
                return REPOSITORY_TYPE.REPO_GIT;
            } else if (SVNRemoteRepository.isSVNRepository(url)) {
                return REPOSITORY_TYPE.REPO_SVN;
            }
            return REPOSITORY_TYPE.REPO_UNKNOWN;
        }

        @Override
        protected void done() {
            try {
                REPOSITORY_TYPE result = get();
                if (result == null) {
                	return;
                }
                repoType = result;
                String descKey;
                switch(repoType) {
                    case REPO_GIT:
                        descKey = "TEAM_DETECTED_REPO_GIT";
                        break;
                    case REPO_SVN:
                        descKey = "TEAM_DETECTED_REPO_SVN";
                        break;
                    default:
                        descKey = "TEAM_DETECTED_REPO_UNKNOWN";
                }
                detectedRepoLabel.setText(OStrings.getString(descKey));
            } catch (Exception ex) {
                // Ignore
            }
            updateButton();
        }
        
    };

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        urlLabel = new javax.swing.JLabel();
        txtRepositoryURL = new javax.swing.JTextField();
        detectedRepoLabel = new javax.swing.JLabel();
        localFolderLabel = new javax.swing.JLabel();
        txtDirectory = new javax.swing.JTextField();
        btnDirectory = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        btnOk = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(OStrings.getString("TEAM_NEW_HEADER")); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, OStrings.getString("TEAM_NEW_REPOSITORY_URL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(urlLabel, gridBagConstraints);

        txtRepositoryURL.setColumns(40);
        txtRepositoryURL.setToolTipText("");
        txtRepositoryURL.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtRepositoryURLFocusLost(evt);
            }
        });
        txtRepositoryURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRepositoryURLActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(txtRepositoryURL, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(detectedRepoLabel, " ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(detectedRepoLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(localFolderLabel, OStrings.getString("TEAM_NEW_DIRECTORY")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(localFolderLabel, gridBagConstraints);

        txtDirectory.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        getContentPane().add(txtDirectory, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btnDirectory, "...");
        btnDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDirectoryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        getContentPane().add(btnDirectory, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btnOk, OStrings.getString("BUTTON_OK")); // NOI18N
        btnOk.setEnabled(false);
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });
        jPanel2.add(btnOk);

        org.openide.awt.Mnemonics.setLocalizedText(btnCancel, OStrings.getString("BUTTON_CANCEL")); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        jPanel2.add(btnCancel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(jPanel2, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void updateButton() {
        boolean enabled = repoType != null && repoType != REPOSITORY_TYPE.REPO_UNKNOWN;
        enabled &= !StringUtil.isEmpty(txtRepositoryURL.getText());
        enabled &= !StringUtil.isEmpty(txtDirectory.getText());
        btnOk.setEnabled(enabled);
    }
    
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        repoType = null;
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        dispose();
        ok = true;
    }//GEN-LAST:event_btnOkActionPerformed

    private void btnDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDirectoryActionPerformed
        NewProjectFileChooser ndc = new NewProjectFileChooser();
        int ndcResult = ndc.showSaveDialog(Core.getMainWindow().getApplicationFrame());
        if (ndcResult == OmegaTFileChooser.APPROVE_OPTION) {
            txtDirectory.setText(ndc.getSelectedFile().getPath());
        }
    }//GEN-LAST:event_btnDirectoryActionPerformed

    private void txtRepositoryURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRepositoryURLActionPerformed
        RepoTypeChecker checker = new RepoTypeChecker();
        checker.execute();
    }//GEN-LAST:event_txtRepositoryURLActionPerformed

    private void txtRepositoryURLFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtRepositoryURLFocusLost
        RepoTypeChecker checker = new RepoTypeChecker();
        checker.execute();
    }//GEN-LAST:event_txtRepositoryURLFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton btnCancel;
    public javax.swing.JButton btnDirectory;
    public javax.swing.JButton btnOk;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel detectedRepoLabel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel localFolderLabel;
    public javax.swing.JTextField txtDirectory;
    public javax.swing.JTextField txtRepositoryURL;
    private javax.swing.JLabel urlLabel;
    // End of variables declaration//GEN-END:variables

    public boolean ok;
}
