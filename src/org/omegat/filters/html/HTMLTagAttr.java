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

package org.omegat.filters.html;

import org.omegat.filters.LBuffer;

public class HTMLTagAttr
{
	public HTMLTagAttr()
	{
		attr = null;
		val = null;
	}

	public void attrAppend(char c)
	{
		if (attr == null)
			attr = new LBuffer(8);
		attr.append(c);
	}

	public void valAppend(char c)
	{
		if (val == null)
			val = new LBuffer(8);
		val.append(c);
	}

	LBuffer attr;
	LBuffer val;
}

