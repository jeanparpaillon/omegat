//-------------------------------------------------------------------------
//  
//  StaticUtils.java - 
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
//  Build date:  16Apr2003
//  Copyright (C) 2002, Keith Godfrey
//  keithgodfrey@users.sourceforge.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.util.*;
import java.util.zip.*;
import java.io.*;
import java.awt.*;
import java.text.*;
import java.awt.event.*;
import java.lang.Math.*;
import java.lang.reflect.*;
import javax.swing.*;
import java.util.*;

// static functions and generic object taken from 
//	CommandThread to reduce file size

// keeps track of specific attributes for project files
class ProjectFileData 
{
	String	name;
	int		firstEntry;
	int		lastEntry;
//	int		totalWords;
//	int		currentWords;
};


////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////

class StaticUtils
{
	public static void initPrefFileMappings()
	{
		PreferenceManager pref = CommandThread.core.getPrefManager();

		pref.setPreference(OConsts.PREF_FILE_MAPPING_N + "1", "htm html");
		// common alternatives here are 'txt txt1' and 'txt txt2' for
		//	Latin1 and Latin2 encodings
		pref.setPreference(OConsts.PREF_FILE_MAPPING_N + "2", "txt utf8");

		///////////////////////////////////////////////
		// OOo family mappings
		// spreadsheet
		pref.setPreference(OConsts.PREF_FILE_MAPPING_N + "3", "sxc sxw");
		// master doc
		pref.setPreference(OConsts.PREF_FILE_MAPPING_N + "4", "sxg sxw");
		// formula
		pref.setPreference(OConsts.PREF_FILE_MAPPING_N + "5", "sxm sxw");
		// drawing
		pref.setPreference(OConsts.PREF_FILE_MAPPING_N + "6", "sxd sxw");
		// presentation
		pref.setPreference(OConsts.PREF_FILE_MAPPING_N + "7", "sxi sxw");

		pref.setPreference(OConsts.PREF_NUM_FILE_MAPPINGS, "7");

		pref.save();
	}
	////////////////////////

	// load file mappings into specified handler master
	public static void loadFileMappings(ArrayList mapFrom, ArrayList mapTo)
	{
		PreferenceManager pref = CommandThread.core.getPrefManager();

		if ((mapFrom == null) || (mapTo == null))
			return;

		String str = pref.getPreference(OConsts.PREF_NUM_FILE_MAPPINGS);
		if ((str == null) || (str.equals("")))
		{
			// probably an old pref file - update it
			initPrefFileMappings();

			// now try to read it again
			str = pref.getPreference(OConsts.PREF_NUM_FILE_MAPPINGS);
			if ((str == null) || (str.equals("")))
			{
				// we've got problems here
				String msg = OStrings.CT_PREF_LOAD_ERROR_MAPPINGS;
				MessageRelay.uiMessageDisplayError(
						CommandThread.core.getTransFrame(), msg, null);
				// just returning will cause trouble later, but at 
				//	least we're preparing the user
				// (this really shouldn't happen, but trap for it just
				//	in case)
				return;
			}
		}
		int num = Integer.decode(str).intValue();

		if (num <= 0)
			initPrefFileMappings();

		int pos;
		String mapn;
		for (int i=1; i<=num; i++)
		{
			mapn = OConsts.PREF_FILE_MAPPING_N + i;
			str = pref.getPreference(mapn);
			pos = str.indexOf(' ');
			mapFrom.add(str.substring(0, pos));
			mapTo.add(str.substring(pos+1));
System.out.println("mapping extension '"+str.substring(0,pos)+"' to '"+str.substring(pos+1)+"'");	
		}
	}

	// builds a list of format tags within the supplied string
	// format tags are HTML style <xxx> tags
	public static void buildTagList(String str, ArrayList tagList)
	{
		String tag = "";
		char c;
		int state = 1;
		// extract tags from source string
		for (int j=0; j<str.length(); j++)
		{
			c = str.charAt(j);
			switch (state)
			{
				// 'normal' mode
				case 1:
					if (c == '<')
						state = 2;
					break;

				// found < - see if double or single
				case 2:
					if (c == '<')
					{
						// "<<" - let it slide
						state = 1;
					}
					else
					{
						tag = "";
						tag += c;
						state = 3;
					}
					break;

				// copy tag
				case 3:
					if (c == '>')
					{
						tagList.add(tag);
						state = 1;
						tag = "";
					}
					else
						tag += c;
					break;
			};
		}
	}

	// returns true if file starts w/ "<?xml"
	public static boolean isXMLFile(String filename)
	{
		try 
		{
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String s = in.readLine();
			if ((s.charAt(0) == '<')		&& 
					(s.charAt(1) == '?')	&&
					(s.charAt(2) == 'x')	&&
					(s.charAt(3) == 'm')	&&
					(s.charAt(4) == 'l'))
			{
				// got a match
				// for now, assume version 1.0 and utf-8 encoding
				in.close();
				return true;
			}
			in.close();
		}
		catch (IOException e)
		{
			// if an exception occured, it must not be an xml file 
		}
		return false;
	}

	// returns a list of all files under the root directory
	//  by absolute path
	public static void buildFileList(ArrayList lst, File rootDir, 
			boolean recursive)
	{
		int i;
		// read all files in current directory, recurse into subdirs
		// append files to supplied list
		File [] flist = rootDir.listFiles();
		for (i=0; i<Array.getLength(flist); i++)
		{
			if (flist[i].isDirectory())
			{
				continue;	// recurse into directories later
			}
			lst.add(flist[i].getAbsolutePath());
		}
		if (recursive == true)
		{
			for (i=0; i<Array.getLength(flist); i++)
			{
				if (flist[i].isDirectory())
				{
					// now recurse into subdirectories
					buildFileList(lst, flist[i], true);
				}
			}
		}
	}

	public static void mergeTmxFiles(File outFile, File rootDir,
			String srcLang, String tarLang) throws IOException, ParseException
	{
		// first, write preamble
		FileOutputStream fos = new FileOutputStream(outFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
		BufferedWriter out = new BufferedWriter(osw);

		String s;
		String t;
		int cnt;
		StringEntry se;
		
		String str = "<?xml version=\"1.0\"?>\n";
		str += "<!DOCTYPE tmx SYSTEM \"tmx11.dtd\">\n";
		str += "<tmx version=\"1.1\">\n";
		str += "  <header\n";
		str += "    creationtool=\"OmegaT\"\n";
		str += "    creationtoolversion=\"1\"\n";
		str += "    segtype=\"paragraph\"\n";
		str += "    o-tmf=\"OmegaT TMX\"\n";
		str += "    adminlang=\"EN-US\"\n";
		str += "    srclang=\"" + srcLang + "\"\n";
		str += "    datatype=\"plaintext\"\n";
		str += "  >\n";
		str += "  </header>\n";
		str += "  <body>\n";
		out.write(str, 0, str.length());
	
		// write meat of file
		TMXReader tmx = new TMXReader();
		ArrayList lst = new ArrayList();
		buildFileList(lst, rootDir, true);
		for (int i=0; i<lst.size(); i++)
		{
			tmx.loadFile((String) lst.get(i), srcLang, tarLang);
			cnt = tmx.numSegments();
			for (int j=0; j<cnt; j++)
			{
				s = tmx.getSourceSegment(j);
				t = tmx.getTargetSegment(j);
				se = CommandThread.core.getStringEntry(j);
				s = XMLStreamReader.makeValidXML(se.getSrcText(), null);
				t = XMLStreamReader.makeValidXML(se.getTrans(), null);
				if ((s.equals("") == false) && (t.equals("") == false))
				{
					s = XMLStreamReader.makeValidXML(s, null);
					t = XMLStreamReader.makeValidXML(t, null);
					str =  "    <tu>\n";
					str += "      <tuv lang=\"" + srcLang + "\">\n";
					str += "        <seg>" + s + "</seg>\n";
					str += "      </tuv>\n";
					str += "      <tuv lang=\"" + tarLang + "\">\n";
					str += "        <seg>" + t + "</seg>\n";
					str += "      </tuv>\n";
					str += "    </tu>\n";
					out.write(str, 0, str.length());
				}
			}
		}

		// finish off file
		str =  "  </body>\n";
		str += "</tmx>\n";
		out.write(str, 0, str.length());
		out.close();
	}

	// returns a list of all files under the root directory
	//  by absolute path
	public static void buildDirList(ArrayList lst, File rootDir)
	{
		int i;
		// read all files in current directory, recurse into subdirs
		// append files to supplied list
		File [] flist = rootDir.listFiles();
		for (i=0; i<Array.getLength(flist); i++)
		{
			if (flist[i].isDirectory())
			{
				// now recurse into subdirectories
				lst.add(flist[i].getAbsolutePath());
				buildDirList(lst, flist[i]);
			}
		}
	}

	// builds a list of tokens and a list of their offsets w/in a file
	// returns number of "words" in file (this may be less than the
	//	number of tokens)
	public static int tokenizeText(String str, ArrayList tokenList)
	{
		if (tokenList != null)
			tokenList.clear();
		int numWords = 0;
		StringBuffer tokenBuf = new StringBuffer();
		int len = str.length();
		boolean hasText = true;
		// process everything as lower case
		str = str.toLowerCase();

		char c;
		int type;
		int offset = 0;
		boolean closeToken = false;
		for (int i=0; i<len; i++)
		{
			c = str.charAt(i);
			type = Character.getType(c);

			if ((type == Character.UPPERCASE_LETTER)		||
					(type == Character.LOWERCASE_LETTER)	||
					(type == Character.TITLECASE_LETTER)	||
					(type == Character.MODIFIER_LETTER)		||
					(type == Character.OTHER_LETTER))
			{
				// make sure we're not in a non-text token
				if (hasText == true)
				{
					tokenBuf.append(c);
				}
				else
				{
					// in a non-text token.  close token and push char
					closeToken = true;
					i--;
				}
			}
			else if ((type == Character.NON_SPACING_MARK)	||
					(type == Character.ENCLOSING_MARK)		||
					(type == Character.COMBINING_SPACING_MARK))
			{
				// modifies preceding character - add it to existing token
				//	but don't increase position counter
				tokenBuf.append(c);
			}
			else if ((type == Character.SPACE_SEPARATOR)	||
					(type == Character.CONTROL)				||
					(type == Character.PARAGRAPH_SEPARATOR)	||
					(type == Character.FORMAT))
			{
				// close token and swallow character
				closeToken = true;
			}
			else if (type == Character.SURROGATE)
			{
				tokenBuf.append(c);
				// if one surrogate is found, it's almost guaranteed
				//	there should be a second, but check just in case
				if (i < (len-1))
					tokenBuf.append(str.charAt(++i));
			}
			else 
			{
				// punctuation of some sort 
				// end token and return char to stack
				if ((tokenBuf.length() > 0) && (hasText == true))
				{
					// don't end if char is in {@.'} and followed by
					// text
					if (((c == '@') || (c == '.')) && (i < (len-1)))
					{
						// take a peek at next char
						char c2 = str.charAt(i+1);
						int t2 = Character.getType(c2);
						if ((t2 == Character.UPPERCASE_LETTER)		||
								(t2 == Character.LOWERCASE_LETTER)	||
								(t2 == Character.TITLECASE_LETTER)	||
								(t2 == Character.MODIFIER_LETTER)		||
								(t2 == Character.OTHER_LETTER))
						{
							// followed by text 
							// probably filename or email address - either 
							//	it's not a word
							tokenBuf.append(c);
							tokenBuf.append(c2);
							i++;
						}
						else
						{
							closeToken = true;
							i--;
						}
					}
					else if ((c == '\'') && (i < (len-1)))
					{
						// see if it's an imbedded apostrophe
						char c2 = str.charAt(i+1);
						int t2 = Character.getType(c2);
						if ((t2 == Character.UPPERCASE_LETTER)		||
								(t2 == Character.LOWERCASE_LETTER)	||
								(t2 == Character.TITLECASE_LETTER)	||
								(t2 == Character.MODIFIER_LETTER)		||
								(t2 == Character.OTHER_LETTER))
						{
							// apostrophe surrounded by text
							// fits pattern of a word
							tokenBuf.append(c);
							tokenBuf.append(c2);
							i++;
						}
						else
						{
							closeToken = true;
							i--;
						}
					}
					else
					{
						// normal punctuation - break token and push char
						closeToken = true;
						i--;
					}
				}
				else
				{
					// empty buffer or non text token - append char
					tokenBuf.append(c);
					hasText = false;
				}
			}


			if (((closeToken == true) || (i == (len-1))) &&
					(tokenBuf.length() > 0))
			{
				if (hasText == true)
					numWords++;
				if (tokenList != null)
					tokenList.add(new Token(tokenBuf.toString(), 
								hasText, offset));
				offset = i+1;
				tokenBuf.setLength(0);

				hasText = true;	// start off assuming it's text
				closeToken = false;
			}
		}

		return numWords;
	}

	// for FileHandlers in test mode
	public static void dumpEntry(String val, String file)
	{
		System.out.println(" val: " + val);
		System.out.println("file: " + file);
		System.out.println("");
	}

	public static String[] getFontNames()
	{
		GraphicsEnvironment graphics;
		graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
		return graphics.getAvailableFontFamilyNames();
	}
};

// offset marks the display offset of character - this might be
//	different than the characters position in the char array
//	due existence of multi-char characters
class Token
{
	public Token(String _text, boolean _hasText, int _offset)
	{
		text = _text;
		hasText = _hasText;
		offset = _offset;
	}
	public String text;
	public boolean hasText;
	public int offset;
};

