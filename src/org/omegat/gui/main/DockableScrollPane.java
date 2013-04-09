/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.gui.main;

import java.awt.Component;

import javax.swing.JScrollPane;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingConstants;

/**
 * Dockable ScrollPane for a docking library.
 * 
 * @author Maxym Mykhalchuk
 */
@SuppressWarnings("serial")
public class DockableScrollPane extends JScrollPane implements Dockable {
    DockKey dockKey;

    /** Updates the name of the docking pane. */
    public void setName(String name) {
        dockKey.setName(name);
    }

    /** Creates a new instance of DockableScrollBox */
    public DockableScrollPane(String key, String name, Component view, boolean detouchable) {
        super(view);
        dockKey = new DockKey(key, name, null, null, DockingConstants.HIDE_BOTTOM);
        dockKey.setFloatEnabled(detouchable);
    }

    public DockKey getDockKey() {
        return dockKey;
    }

    public Component getComponent() {
        return this;
    }

}
