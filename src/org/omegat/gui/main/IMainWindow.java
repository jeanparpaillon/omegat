/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.main;

import java.awt.Font;

import javax.swing.JFrame;

/**
 * Interface for access to main window functionality.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public interface IMainWindow {
    /**
     * Get application frame.
     */
    JFrame getApplicationFrame();

    /**
     * Get main application font.
     */
    Font getApplicationFont();

    /**
     * Show message in status bar.
     * 
     * @param messageText
     *                message text
     */
    void showStatusMessage(String messageText);
    
    /**
     * Show message in progress bar.
     * 
     * @param messageText
     *                message text
     */
    void showProgressMessage(String messageText);

    /**
     * Show message in length label.
     * 
     * @param messageText
     *                message text
     */
    void showLengthMessage(String messageText);

    /**
     * Display error.
     * 
     * @param errorText
     *                error text
     * @param ex
     *                exception to show
     */
    void displayError(String errorText, Throwable ex);
}
