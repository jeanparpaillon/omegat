//-------------------------------------------------------------------------
//  
//  LBuffer.java - 
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

import java.text.*;

class LBuffer {
	public LBuffer(int size)
	{
		int sz = PowerOfTwo(size);
		if (sz < 4)
			sz = 4;
		m_buf = new char[sz];
		m_size = 0;
	}

	public LBuffer(char[] str)
	{
		int sz = PowerOfTwo(str.length);
		if (sz < 4)
			sz = 4;
		m_buf = new char[sz];
		m_size = str.length;
		System.arraycopy(str, 0, m_buf, 0, str.length);
	}

	public LBuffer(String str)
	{
		int sz = PowerOfTwo(str.length());
		if (sz < 4)
			sz = 4;
		m_buf = new char[sz];
		m_size = str.length();
		str.getChars(0, str.length(), m_buf, 0);
	}

	public void append(LBuffer buf)
	{
		if (buf.length() + m_size > m_buf.length)
			setSize(buf.length() + m_size);
		char[] b = buf.getBuf();
		System.arraycopy(b, 0, m_buf, m_size, buf.length());
		m_size += buf.length();
	}

	public void append(String str)
	{
		if (str.length() + m_size > m_buf.length)
			setSize(str.length() + m_size);
		str.getChars(0, str.length(), m_buf, m_size);
		m_size += str.length();
	}
	
	public void append(char[] str)
	{
		if (str.length + m_size > m_buf.length)
			setSize(str.length + m_size);
		System.arraycopy(str, 0, m_buf, m_size, str.length);
		m_size += str.length;
	}

	public void append(char c)
	{
		if (m_size + 1 > m_buf.length)
			setSize(m_size + 1);
		m_buf[m_size] = c;
		m_size++;
	}
	
	public char getChar(int pos)
	{
		if ((pos >= m_size) || (pos < 0))
			return 0;
		else
			return m_buf[pos];
	}

	public boolean isEqual(LBuffer buf)
	{
		if (buf.length() != m_size)
			return false;
		for (int i=0; i<m_size; i++)
		{
			if (m_buf[i] != buf.m_buf[i])
				return false;
		}
		return true;
	}

	public boolean isEqual(String str)
	{
		if (str.length() != m_size)
			return false;
		else 
		{
			String s = new String(m_buf, 0, m_size);
			return (str.compareTo(s) == 0);
		}
	}

	public boolean isEqualIgnoreCase(String str)
	{
		if (str.length() != m_size)
			return false;
		else 
		{
			String s = new String(m_buf, 0, m_size);
			return (str.compareToIgnoreCase(s) == 0);
		}
	}

	public String string()
	{
		return new String(m_buf, 0, m_size);
	}

	public void appendInt(int x)
	{
		append(Integer.toString(x));
	}

	public void appendDouble(double x, int digits)
	{
		DecimalFormat nf = new DecimalFormat();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		append(nf.format(x));
	}
	
	public void setSize(int newSize)
	{
		char[] buf = null;
		if (newSize < 0)
			newSize = 0;

		// make sure buffer is large enough
		if (newSize > m_buf.length)
		{
			int logSize = m_buf.length;
			while (newSize > logSize)
				logSize *= 2;
			buf = new char[logSize];
			System.arraycopy(m_buf, 0, buf, 0, m_buf.length);
			m_buf = buf;
		}
		// don't grow size (len) pointer - only shrink it
		if (m_size > newSize)
			m_size = newSize;
	}

	public void reset()
	{
		m_size = 0;
	}

	public static int PowerOfTwo(int x)
	{
		int retVal = 0;
		int initVal = x;
		int ctr = 0;
		int bits = 0;

		if (x > 0)
		{
			// no support for negative numbers
			while (x != 0)
			{
				ctr++;
				if ((x & 1) != 0)
					bits++;
				x >>= 1;
			}
			if (bits == 1)
				retVal = initVal;
			else
				retVal = 1 << ctr;
		}
		return retVal;

	}

	public int length()	{ return m_size;	}
	public int size()	{ return m_size;	}
	public char[] getBuf()	{ return m_buf;		}

	public void report()
	{
		System.out.println("size:         " + m_size);
		System.out.println("logical size: " + m_buf.length);
		System.out.println(new String(m_buf, 0, m_size));
	}

	private char[]	m_buf;
	private int	m_size;
};
