//-------------------------------------------------------------------------
//  
//  FileHandler.java - 
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

import java.io.*;
import java.util.*;

abstract class FileHandler 
{
	public FileHandler(String type, String extension)
	{
		m_type = new String(type);
		m_preferredExtension = new String(extension);
		m_testMode = false;
		m_outputMode = false;
//		m_lang = "";
		m_outFile = null;
	}

	public String type()
	{
		return m_type;
	}

	public String preferredExtension()
	{
		return m_preferredExtension;
	}

	public void setTestMode(boolean state)
	{
		m_testMode = state;
	}

	public void fileWriteError(IOException e) throws IOException
	{
		String str = OStrings.FH_ERROR_WRITING_FILE;
		throw new IOException(str + " - " + e);
	}

	public String formatString(String text) 
	{
		// override in subclasses when formatting important
		return text;
	}

	protected void processEntry(LBuffer buf, String file)
					throws IOException
	{
		processEntry(buf.string(), file);
	}
	
	protected void processEntry(String srcText, String file)
					throws IOException
	{
		if ((m_testMode) && (!m_outputMode))
			CommandThread.core.dumpEntry(srcText, file);
		else if (m_outputMode)
		{
			// fetch translation and write it to outfile
			String s;
			if (m_testMode)
			{
				s = srcText;
				s = formatString(s);
				m_outFile.write(s + "-trans");
			}
			else
			{
				s = CommandThread.core.getTranslation(srcText);
				if ((s == null) || (s.equals("")))
					s = srcText;
				s = formatString(s);
				m_outFile.write(s);
			}
		}
		else
		{
			CommandThread.core.addEntry(srcText, file);
		}
	}

	public void write(String infile, String outfile) throws IOException
	{
		m_file = infile;
//		m_lang = lang;
		m_outputMode = true;
		File of = new File(outfile);
		File pd;
		try
		{
			if (!m_testMode)
			{
				pd = of.getParentFile();
				if (pd == null)
					throw new IOException(
						"invalid project tree");
				if ((pd.isDirectory() == false) &&
						(pd.mkdirs() == false))
				{
					throw new IOException(
						"cannot create target " +
						"language directory tree (" +
						pd.getAbsolutePath() + ")");
				}
			}
			m_outFile = createOutputStream(infile, outfile);
			load(infile);
			m_outFile.close();
		}
		catch(IOException e)
		{
			m_outputMode = false;
			m_outFile = null;
//			m_lang = "";
			throw e;
		}
		m_outFile = null;
		m_outputMode = false;
//		m_lang = "";
	}

	// create output stream - allow stream to have access to source file
	//  if necessary
	public BufferedReader createInputStream(String infile)
			throws IOException
	{
		FileInputStream fis = new FileInputStream(infile);
		//InputStreamReader isr = new InputStreamReader(fis);
		InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-1");
		BufferedReader br = new BufferedReader(isr);
		return br;
	}

	public BufferedWriter createOutputStream(String infile, String outfile)
			throws IOException
	{
		FileOutputStream fos = new FileOutputStream(outfile);
		//OutputStreamWriter osw = new OutputStreamWriter(fos);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "ISO-8859-1");
		BufferedWriter bw = new BufferedWriter(osw);
		return bw;
	}

	public abstract void doLoad() throws IOException;
	public void load(String file) throws IOException
	{
		reset();
		m_file = file;
		//m_in = new DataInputStream(new FileReader(file));
		
		try 
		{
			m_in = createInputStream(file);
	
			if (m_in == null)
			{
				throw new IOException("Can't open input file '" + file + "'");
			}
			doLoad();
			m_in.close();
		}
		catch (IOException e)
		{
			try				{ if (m_in != null) m_in.close(); }
			catch (IOException e2)		{ ; }
			m_in = null;
			throw e;
		}
		m_in = null;
	}

	// push a character in front of the active stream
	protected void pushNextChar(char c)
	{
		m_pushChar = c;
	}

	public int getNextChar() throws IOException
	{
		int i;
		if (m_pushChar != 0)
		{
			i = m_pushChar;
			m_pushChar = 0;
		}
		else
		{
			i = m_in.read();
			if (i == 10)
			{
				// don't increment counter again on /r/m
				if (m_cr == false)
					m_line++;
				m_cr = false;
			}
			else if (i == 13)
			{
				m_cr = true;
				m_line++;
			}
			else
				m_cr = false;
		}
		return i;
	}

	protected String getNextLine() throws IOException
	{
		return m_in.readLine();
	}

	protected void markStream() throws IOException
	{
		m_in.mark(16);
	}

	protected void resetToMark() throws IOException
	{
		m_in.reset();
	}

	public void reset()
	{
		m_line = 0;
		m_cr = false;
		m_file = "";
	}

	public int	line()	{ return m_line;	}

	private String m_type;
	private String m_preferredExtension;
	//private DataInputStream m_in;
	protected BufferedReader m_in;
	protected boolean m_testMode;
	protected boolean m_outputMode;
	//protected DataOutputStream	m_outFile;
	protected BufferedWriter	m_outFile;
//	protected String m_lang = "";

	private int		m_line = 0;
	private boolean		m_cr = false;
	private int		m_pushChar = 0;

	protected String	m_file = "";
}
