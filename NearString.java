//-------------------------------------------------------------------------
//  
//  NearString.java - 
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

class NearString implements Comparable
{
	public NearString(StringEntry strEntry, 
				double nearScore, 
				byte[] parData,
				byte[] nearData,
				String projName)
	{
		int i;
		str = strEntry;
		score = nearScore;
		parAttr = parData;
		attr = nearData;
		proj = projName;
	}

	public int compareTo(Object obj)
	{
		NearString visitor = (NearString) obj;
		Double homeScore = new Double(score);
		Double visitorScore = new Double(visitor.score);
		return homeScore.compareTo(visitorScore);
	}

	public StringEntry str;
	public double score;
	public byte[] parAttr;	// matching attributes of parent strEntry
	public byte[] attr;	// matching attributes of near strEntry
	public String proj;
}
