//-------------------------------------------------------------------------
//  
//  SourceTextEntry.java - 
//  
//  Copyright (C) 2002, Keith Godfrey
//  
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//  
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//  
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//  
//  Build date:  4Dec2002
//  Copyright (C) 2002, Keith Godfrey
//  aurora@coastside.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.util.*;

// a source text entry represents an individual segment for
//  translation pulled directly from the input files
// there can be many SourceTextEntries having identical source
//  language strings
class SourceTextEntry
{
	public void set(StringEntry str, String file, int entryNum)
	{
		m_srcFile = file;
		m_strEntry = str;
		m_strEntry.addParent(this);
		m_entryNum = entryNum;
	}

	public String getSrcFile()		{ return m_srcFile;	}
	public StringEntry getStrEntry()	{ return m_strEntry;	}
	// NOTE: the uncloned reference to m_strEntry is returned on purpose


	public String getTranslation()
	{
		if (m_strEntry != null)
			return m_strEntry.getTrans();
		else
			return "";
	}

	public void setEntryNum(int n)		{ m_entryNum = n;	}
	public int entryNum()			{ return m_entryNum;	}

	private	String m_srcFile;
	private StringEntry m_strEntry = null;
	private int m_entryNum;
}
