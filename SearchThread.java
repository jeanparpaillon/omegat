//-------------------------------------------------------------------------
//  
//  SearchThread.java - 
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
//  Build date:  16Sep2003
//  Copyright (C) 2002, Keith Godfrey
//  keithgodfrey@users.sourceforge.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.text.*;
import java.util.*;
import java.lang.*;
import java.io.*;
import javax.swing.*;

// each search window has its own search thread to actually do the 
//	searching.  this prevents lockup of the UI during intensive searches
class SearchThread extends Thread
{
	public SearchThread(TransFrame par, String startText)
	{
		m_window = new SearchWindow(par, this, startText);
		m_stop = false;
		m_searchDir = null;
		m_searchRecursive = false;
		m_searchText = "";
		m_searching = false;
		m_tmSearch = false;

		m_numFinds = 0;
		m_curFileName = "";

		m_extList = new ArrayList();
		m_extMapList = new ArrayList();

		// load mapping table
		// do this check so dialog cna be displayed independently
		StaticUtils.loadFileMappings(m_extList, m_extMapList);
	}

	/////////////////////////////////////////////////////////
	// poublic interface

	// only starts a search if another is not currently running
	// returns 0 on successful start, 1 on failure (i.e. search in progress)
	// to search current project only, set rootDir to null
	public synchronized int requestSearch(String text, String rootDir, 
			boolean recursive, boolean exact, boolean tm)
	{
		if (m_searching == false)
		{
			m_searchDir = rootDir;
			m_searchRecursive = recursive;
			m_searchText = text;
			m_exactSearch = exact;
			m_tmSearch = tm;
			m_searching = true;
			this.interrupt();
			return 0;
		}
		return 1;
	}

	public void haltThread()
	{
		m_stop = true;
		this.interrupt();
	}

	///////////////////////////////////////////////////////////
	// thread main loop
	public void run()
	{
		// have search thread control search window to allow parent 
		//	window to avoid blocking
		// need to spawn subthread so we don't block either
		MDialogThread dlgThread = new MDialogThread(m_window);
		dlgThread.start();

		boolean firstPass = true;
		try 
		{
			while (m_stop == false)
			{
				try { sleep(80); }
				catch (InterruptedException e) { ; }

				if (firstPass == true)
				{
					// on first pass send a request to place cursor in
					//	search field (otherwise search window has no
					//	control with default keyboard focus)
					// this is a hack, but can't find another way to do
					//	this gracefully
					firstPass = false;
					m_window.setSearchControlFocus();
				}
				
				if (m_searching == true)
				{
					// work to be done
					if (m_searchDir == null)
					{
						// if no search directory specified, then we are
						//	searching currnet project only
						searchProject();
					}
					else
					{
						// search specified directory tree
						try
						{
							searchFiles();
						}
						catch (IOException e)
						{
							// something bad happened 
							// alert user to badness
							String msg = OStrings.ST_FILE_SEARCH_ERROR;
							CommandThread.core.displayError(msg, e);
						}
					}
		
					// whatever the states is, error or not, display what's
					//	been found so far
					if (m_numFinds == 0)
					{
						// no match
						m_window.postMessage(OStrings.ST_NOTHING_FOUND);
					}
					m_window.displayResults();
					m_searching = false;
				}
			}
		}
		catch (RuntimeException re)
		{
			String msg = OStrings.ST_FATAL_ERROR;
			CommandThread.core.displayError(msg, re);
			m_window.threadDied();
		}
	}

	//////////////////////////////////////////////////////////////
	// internal functions

	protected void foundString(int entryNum, String intro, String src,
			String target)
	{
		if (m_numFinds++ > OConsts.ST_MAX_SEARCH_RESULTS)
		{
			return;
		}
		
		if (entryNum >= 0)
		{
			// entries are referenced at offset 1 but stored at offset 0
			m_window.addEntry(entryNum+1, null, (entryNum)+"> "+src, target);
		}
		else
		{
			m_window.addEntry(entryNum, intro, src, target);
		}

		if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
		{
			m_window.postMessage(OStrings.SW_MAX_FINDS_REACHED);
		}
	}

	protected void searchProject()
	{
		SourceTextEntry ste;
		int numEntries = CommandThread.core.numEntries();
		m_numFinds = 0;
		String srcText;
		String locText;
		if (m_exactSearch == true)
		{
			int i;
			for (i=0; i<numEntries; i++)
			{
				ste = CommandThread.core.getSTE(i);
				srcText = ste.getSrcText();
				locText = ste.getTranslation();
				if ((searchString(srcText, m_searchText) == true) ||
						(searchString(locText, m_searchText) == true))
				{
					// found a match - relay source and trans text
					foundString(i, null, srcText, locText);
					if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
					{
						break;
					}
				}
			}
			if (m_tmSearch)
			{
				ArrayList tmList = CommandThread.core.getTransMemory();
				TransMemory tm;
				for (i=0; i<tmList.size(); i++)
				{
					tm = (TransMemory) tmList.get(i);
					srcText = tm.source;
					locText = tm.target;
					if ((searchString(srcText, m_searchText) == true) ||
							(searchString(locText, m_searchText) == true))
					{
						// found a match - relay source and trans text
						foundString(-1, tm.file, srcText, locText);
						if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
						{
							break;
						}
					}
				}
			}
		}
		else
		{
			// keyword search - recycling TransFrame search code
			TreeMap foundList = CommandThread.core.findAll(m_searchText);
			if (foundList != null)
			{
				int entryNum;
				ListIterator it;
				TreeMap list = (TreeMap) foundList.clone();
				TreeMap queryMap = new TreeMap(new MQueryComparator());
				LinkedList parentList;
				StringEntry strEntry;
				SourceTextEntry srcTextEntry;
				MQueryData qd;
				while (list.size() > 0)
				{
					strEntry = (StringEntry) list.remove(list.firstKey());
					if (strEntry == null)
					{
						continue;
					}
					parentList = strEntry.getParentList();
					if (parentList == null)
					{
						continue;
					}

					it = parentList.listIterator();
					while (it.hasNext())
					{
						srcTextEntry = (SourceTextEntry) it.next();
						foundString(srcTextEntry.entryNum(), null, 
								strEntry.getSrcText(), strEntry.getTrans());
						if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
						{
							break;
						}
					}
				}
			}
		}
	}
	
	protected void searchFiles() throws IOException
	{
		int i;
		int j;

		FileHandler fh = null;
		HandlerMaster hm = new HandlerMaster();
		ArrayList fileList = new ArrayList(256);
		if (m_searchDir.endsWith(File.separator) == false)
			m_searchDir += File.separator;
		StaticUtils.buildFileList(fileList, new File(m_searchDir), 
				m_searchRecursive);
		//TODO m_window.numberSearchFiles(fileList.size());
		int namePos = m_searchDir.length();

		boolean ignore;
		String shortName;
		String filename;
		for (i=0; i<fileList.size(); i++)
		{
			filename = (String) fileList.get(i);
			// determine actual file name w/ no root path info
			m_curFileName = filename.substring(namePos);

			int extPos = filename.lastIndexOf('.');
			String ext = filename.substring(extPos+1);
			// look for mapping of this extension
			for (j=0; j<m_extList.size(); j++)
			{
				if (ext.equals(m_extList.get(j)) == true)
				{
					ext = (String) m_extMapList.get(j);
					break;
				}
			}

			// if file has handler, use it, otherwise do a binary search
			fh = hm.findPreferredHandler(ext);
			if (fh != null)
			{
				// make sure file hander in correct mode
				fh.setSearchMode(true, this);

				// don't bother to tell handler what we're looking for - 
				//	the search data is already known here (and the 
				//	handler is in the same thread, so info is not volatile)
				fh.load(filename);
			}
			else
			{
				// do binary search -- TODO
				System.out.println("Don't recognize file extension for '" +
						filename + "' - omitting file from search");
			}
		}
	}

	// extract 'strings' from current file.  Use newline and non-display
	// char strings w/ length > 5 for searches.
	//protected void examineBinary()
	//{
	//}

	///////////////////////////////////////////////////////////////////////
	// search algorithm
	
	// look for the search text in the specified text
	// search supports wildcards * and ?
	protected boolean searchString(String text, String search)
	{
		if ((text == null) || (search == null))
			return false;

		char c, t;
		int searchLen = search.length();
		int textLen = text.length();
		int spos = 0;
		int pos = 0;
		int mark = 0;
		boolean inSeq = false;

		// handle the edge cases here
		if (textLen == 0)
		{	
			if (searchLen == 0)
				return true;
			else
			{
				// if only * remain in search string, that's OK
				while (spos < searchLen)
				{
					c = search.charAt(spos++);
					if (c != '*')
						return false;
				}
				return true;
			}
		}
		else if (searchLen == 0)
		{
			// text and no search string - must be OK
			return true;
		}

		// now do the search
		while (spos < searchLen)
		{
			c = search.charAt(spos);
			switch (c)
			{
				case '*':	// match zero or more characters
					spos++;
					if (inSeq == true)
					{
						// currently in a match sequence - recurse to check
						//	substring
						return searchString(text.substring(pos), 
									search.substring(spos));
					}
					// otherwise ignore
					break;

				case '?':	// match exactly one character
					spos++;
					if (++pos >= textLen)
					{
						// end of text encountered
						// we have match if search string finished or
						// if search string contains only *
						while (spos < searchLen)
						{
							c = search.charAt(spos++);
							if (c != '*')
								return false;
						}
						return true;
					}
					if (inSeq == false)
					{
						// if not in a sequence, simply skip first text
						//	character (there is implied * at head of 
						//	each search string)
						return searchString(text.substring(pos), 
									search.substring(spos));
					}
					break;

				default:
					// regular text
					t = text.charAt(pos++);
					if (inSeq == false)
					{
						// TODO case insensitive compare change goes here
						if (c == t)
						{
							inSeq = true;
							mark = pos;
							spos++;
						}
					}
					else 
					{
						if (c != t)
						{
							pos = mark;
							spos = 0;
							inSeq = false;
						}
						else
							spos++;
					}

					if (pos >= textLen)
					{
						if (inSeq == true)
						{
							// text overwith - if search string also done, 
							//	or if only has *s left, then a match
							//	otherwise not
							while (spos < searchLen)
							{
								c = search.charAt(spos++);
								if (c != '*')
									return false;
							}
							return true;
						}
						else
							return false;
					}
					break;
			};
		}

		// search string finished - if we're still in sequence, we must 
		//	have a successful match
		// otherwise, text ran out while searching and so no match
		return inSeq;
	}

	class MDialogThread extends Thread
	{
		public MDialogThread(JFrame win)
		{
			m_win = win;
		}
		
		public void run()
		{
			m_win.show();
		}

		protected JFrame m_win;
	};
	
	class MQueryComparator implements Comparator
	{
		public int compare(Object o1, Object o2)
		{
			int q1 = ((MQueryData) o1).entryNum;
			int q2 = ((MQueryData) o2).entryNum;
			return (q1 - q2);
		}
	}
	
	class MQueryData 
	{
		public String src;
		public String xl;
		public int entryNum;
	}
	
	/////////////////////////////////////////////////////////////////
	// interface used by FileHandlers
	
	public void searchText(String seg)
	{
		if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
		{
			return;
		}

		if (searchString(seg, m_searchText) == true)
		{
			// found a match - do something about it
			foundString(-1, m_curFileName, seg, null);
		}
	}

	protected SearchWindow	m_window;
	protected boolean		m_stop;
	protected boolean		m_searching;
	protected String		m_searchText;
	protected String		m_searchDir;
	protected boolean		m_searchRecursive;
	protected String		m_curFileName;
	protected boolean		m_exactSearch;
	protected boolean		m_tmSearch;

	protected int			m_numFinds;

	// this contains the results of the current search
	protected StringBuffer	m_results;

	protected ArrayList		m_extList;
	protected ArrayList		m_extMapList;
};

