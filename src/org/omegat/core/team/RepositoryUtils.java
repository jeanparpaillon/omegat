/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
               2014 Aaron Madlon-Kay
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
package org.omegat.core.team;

import org.omegat.core.Core;
import org.omegat.gui.dialogs.TeamUserPassDialog;
import org.omegat.util.OStrings;
import org.omegat.util.gui.DockingUI;

/**
 * Some utility methods for working with remote repository.
 *  
 * @author Alex Buloichik <alex73mail@gmail.com>
 * @author Aaron Madlon-Kay
 */
public class RepositoryUtils {
    /**
     * Display dialog for credentials.
     * 
     * @return true if user entered credentials, otherwise - false
     */
    public static boolean askForCredentials(IRemoteRepository repository, String message) {
        TeamUserPassDialog userPassDialog = new TeamUserPassDialog(Core.getMainWindow().getApplicationFrame());
        DockingUI.displayCentered(userPassDialog);
        userPassDialog.descriptionTextArea.setText(message);
        userPassDialog.setVisible(true);
        if (userPassDialog.getReturnStatus() == TeamUserPassDialog.RET_OK) {
            repository.setCredentials(userPassDialog.userText.getText(),
                    new String(userPassDialog.passwordField.getPassword()),
                    userPassDialog.cbForceSavePlainPassword.isSelected());
            repository.setReadOnly(userPassDialog.cbReadOnly.isSelected());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Class to execute a repository command that can throw a IRemoteRepository.AuthenticationException.
     * In that case, a username/password dialog will be shown.
     */
    public static abstract class AskCredentials {

       /**
         * wrapper around callRepository to execute some repository command. 
         * On IRemoteRepository.AuthenticationException, show username/password dialog and try again.
         * @param repository
         * @throws Exception when no credentials entered.
         */
        public void execute(IRemoteRepository repository) throws Exception {
            boolean firstPass = true;
            while (true) {
                try {
                    callRepository();
                    break;
                } catch (IRemoteRepository.AuthenticationException ex) {
                    boolean entered = RepositoryUtils.askForCredentials(repository,
                            OStrings.getString(firstPass ? "TEAM_USERPASS_FIRST" : "TEAM_USERPASS_WRONG"));
                    if (!entered) {
                        throw ex;
                    }
                    firstPass = false;
                }
            }
        }

        /**
         * Implement here a function to execute, which can throw an IRemoteRepository.AuthenticationException.
         * It is called by the execute() function which will show an username/password dialog on this exeption.
         * Other execptions are thrown.
         * @throws Exception Any exception;
         * can even be IRemoteRepository.AuthenticationException in case user did not enter his credentials.
         */
        abstract protected void callRepository() throws Exception;
    }

    
    /**
     * Detect the type of the remote repository. Returns the class object representing the
     * repository type (one of the OmegaT classes extending {@link IRemoteRepository}), or
     * null if the detection fails for whatever reason.
     * 
     * @param url The URL of the remote repository
     * @return The class representing the repository type or null if detection failed
     */
    public static Class<? extends IRemoteRepository> detectRemoteRepoType(String url) {
        if (GITRemoteRepository.isGitRepository(url)) {
            return GITRemoteRepository.class;
        } else if (SVNRemoteRepository.isSVNRepository(url)) {
            return SVNRemoteRepository.class;
        }
        return null;
    }
}
