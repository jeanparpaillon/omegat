/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2004  Keith Godfrey et al
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

package org.omegat.core;

import java.util.TreeMap;

/**
 * An entry in the index
 *
 * @author Keith Godfrey
 */
public class IndexEntry extends Object
{
	public IndexEntry(String wrd)
	{
        m_refTree = new TreeMap();
	}

	public TreeMap getTreeMap()	
			{ return (TreeMap) m_refTree.clone();	}

	public void addReference(StringEntry ref)
	{
		// make sure reference doesn't already exist
		// (from repeated words)
		String s = String.valueOf(ref.digest());
		if (!m_refTree.containsKey(s))
			m_refTree.put(s, ref);
	}

    private TreeMap		m_refTree;
}
