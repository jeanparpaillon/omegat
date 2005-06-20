/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

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

package org.omegat.gui;

import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.PreferenceManager;

/**
 * Basic File Chooser for OmegaT, showing the icon for OmegaT projects
 * and customizing the description for OmegaT project directories.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class OmegaTFileChooser extends JFileChooser
{
 
    /**
     * Constructs an <code>OmegaTFileChooser</code> pointing to OmegaT's
     * current directory.
     */
    public OmegaTFileChooser()
    {
        super(PreferenceManager.pref.getPreference(OConsts.PREF_CUR_DIR));
    }
    /**
     * Constructs an <code>OmegaTFileChooser</code> using the given path.
     */
    public OmegaTFileChooser(String path)
    {
        super(path);
    }
    
    /** OmegaT project icon */
    private static ImageIcon omegatIcon = null;

    /** Redefines the icon for OmegaT projects. */
    public Icon getIcon(File f)
    {
        if( omegatIcon==null )
            omegatIcon = new ImageIcon("images" + File.separator + "OmegaT_small.gif");	// NOI18N
		if( omegatIcon!=null && OpenProjectFileChooser.isProjectDir(f) )
            return omegatIcon;
		else	
			return super.getIcon(f);
    }

    /** Redefines the file type for OmegaT projects. */
    public String getTypeDescription(File f)
    {
		if (OpenProjectFileChooser.isProjectDir(f))
			return OStrings.PFC_OMEGAT_PROJECT;
		else
			return null;
    }
    
}
