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

package org.omegat.gui.main;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.omegat.core.StringEntry;
import org.omegat.core.glossary.GlossaryEntry;
import org.omegat.core.matching.NearString;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.core.threads.CommandThread;
import org.omegat.core.threads.SearchThread;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.ContextFrame;
import org.omegat.gui.HelpFrame;
import org.omegat.gui.MatchWindow;
import org.omegat.gui.ProjectFrame;
import org.omegat.gui.dialogs.AboutDialog;
import org.omegat.util.StaticUtils;
import org.omegat.gui.dialogs.FontSelectionDialog;
import org.omegat.gui.filters2.FiltersCustomizer;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.PreferenceManager;
import org.omegat.util.RequestPacket;

/**
 * The main window of OmegaT application.
 * <p>
 * Currently prototype.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class MainWindow extends JFrame implements org.omegat.gui.TransFrameInterface, java.awt.event.ActionListener, java.awt.event.WindowListener
{
    
    /** Creates new form MainWindow */
    public MainWindow()
    {
        initComponents();
		additionalUIInit();
        oldInit();
        
        try
        {
            URL resource = getClass().getResource("/org/omegat/gui/resources/omegat-small.gif");  // NOI18N
            ImageIcon imageicon = new ImageIcon(resource);
            Image image = imageicon.getImage();
            setIconImage(image);
        }
        catch( Exception e )
        {
            e.printStackTrace(StaticUtils.getLogStream());
        }
    }
    
	private void additionalUIInit()
	{
		updateTitle();
        
		xlPane = new XLPane(this);
        mainScroller.setViewportView(xlPane);
        
		m_projWin = new ProjectFrame(this);
		m_matchViewer = new MatchWindow();
	}
	private void updateTitle()
	{
		String s = OStrings.VERSION;
		if( m_activeProj!=null && m_activeProj.length()>0 )									// NOI18N
		{
			String file = m_activeFile.substring(
					CommandThread.core.sourceRoot().length());
			s += " - " + m_activeProj + " :: " + file;							// NOI18N
		}
		setTitle(s);
	}
    
	/**
     * Old Initialization.
     */
	public void oldInit()
	{
        m_curEntryNum = -1;
		m_curNear = null;
        m_activeProj = "";														// NOI18N
		m_activeFile = "";														// NOI18N
		m_docSegList = new ArrayList();
		
		////////////////////////////////
		loadDisplayPrefs();

		enableEvents(0);

		initScreenLayout();

		String fontName = PreferenceManager.pref.getPreferenceDefault(OConsts.TF_SRC_FONT_NAME, OConsts.TF_FONT_DEFAULT);
		String fontSize = PreferenceManager.pref.getPreferenceDefault(OConsts.TF_SRC_FONT_SIZE, OConsts.TF_FONT_SIZE_DEFAULT);
        int fontSizeInt = 12;
		try 
        {
			fontSizeInt = Integer.parseInt(fontSize);
		}
		catch (NumberFormatException nfe) 
        {
        }
        m_font = new Font(fontName, Font.PLAIN, fontSizeInt);
		xlPane.setFont(m_font);
		m_matchViewer.setFont(m_font);
		
		// check this only once as it can be changed only at compile time
		// should be OK, but customization might have messed it up
		String start = OStrings.TF_CUR_SEGMENT_START;
		int zero = start.lastIndexOf('0');
        m_segmentTagHasNumber = (zero > 4) && // 4 to reserve room for 10000 digit
                (start.charAt(zero - 1) == '0') &&
                (start.charAt(zero - 2) == '0') &&
                (start.charAt(zero - 3) == '0');
	}

	private void loadDisplayPrefs()
	{
		String tab = PreferenceManager.pref.getPreference(OConsts.PREF_TAB);
		if (tab != null && tab.equals("true"))								// NOI18N
		{
			optionsTabAdvanceCheckBoxMenuItem.setSelected(true);
			m_advancer = KeyEvent.VK_TAB;
		}
		else
			m_advancer = KeyEvent.VK_ENTER;
	}

	private void initScreenLayout()
	{
		// KBG - assume screen size is 800x600 if width less than 900, and
		//		1024x768 if larger.  assume task bar at bottom of screen.
		//		if screen size saved, recover that and use instead
		//	(18may04)
		String dw, dh, dx, dy;
		dw = PreferenceManager.pref.getPreference(OConsts.PREF_DISPLAY_W);
		dh = PreferenceManager.pref.getPreference(OConsts.PREF_DISPLAY_H);
		dx = PreferenceManager.pref.getPreference(OConsts.PREF_DISPLAY_X);
		dy = PreferenceManager.pref.getPreference(OConsts.PREF_DISPLAY_Y);
		int x=0;
		int y=0;
		int w=0;
		int h=0;
		boolean badSize = false;
		if (dw == null || dw.equals("")	|| dh == null			||			// NOI18N
                dh.equals("")	|| dx == null || dx.equals("")	||			// NOI18N
                dy == null || dy.equals(""))								// NOI18N
		{
			badSize = true;
		}
		else
		{
			try 
			{
				x = Integer.parseInt(dx);
				y = Integer.parseInt(dy);
				w = Integer.parseInt(dw);
				h = Integer.parseInt(dh);
			}
			catch (NumberFormatException nfe)
			{
				badSize = true;
			}
		}
		if (badSize)
		{
			// size info missing - put window in default position
			GraphicsEnvironment env = 
					GraphicsEnvironment.getLocalGraphicsEnvironment();
			Rectangle scrSize = env.getMaximumWindowBounds();
			if (scrSize.width < 900)
			{
				// assume 800x600
				setSize(585, 536);
				setLocation(0, 0);
			}
			else
			{
				// assume 1024x768 or larger
				setSize(675, 700);
				setLocation(0, 0);
			}
		}
		else
		{
			setSize(w, h);
			setLocation(x, y);
		}
	}

	private void storeScreenLayout()
	{
		int w = getWidth();
		int h = getHeight();
		int x = getX();
		int y = getY();
		PreferenceManager.pref.setPreference(OConsts.PREF_DISPLAY_W, "" + w);		// NOI18N
		PreferenceManager.pref.setPreference(OConsts.PREF_DISPLAY_H, "" + h);		// NOI18N
		PreferenceManager.pref.setPreference(OConsts.PREF_DISPLAY_X, "" + x);		// NOI18N
		PreferenceManager.pref.setPreference(OConsts.PREF_DISPLAY_Y, "" + y);		// NOI18N
	}

    ///////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////
	// command handling
	
	private void doQuit()
	{
		// shutdown
		if (m_projectLoaded)
		{
			commitEntry();
			doSave();
		}

		storeScreenLayout();
		m_matchViewer.storeScreenLayout();
		PreferenceManager.pref.save();

		CommandThread.core.signalStop();
		for (int i=0; i<25; i++)
		{
			while (CommandThread.core != null)
			{
				try { Thread.sleep(10); }
				catch (InterruptedException e) {
                }
			}
			if (CommandThread.core == null)
				break;
		}

		System.exit(0);
	}

	private void doValidateTags()
	{
		ArrayList suspects = CommandThread.core.validateTags();
		if (suspects.size() > 0)
		{
			// create list of suspect strings - use org.omegat.gui.ContextFrame for now
			ContextFrame cf = new ContextFrame(this);
			cf.setVisible(true);
			cf.displayStringList(suspects);
		}
		else
		{
			// show dialog saying all is OK
			JOptionPane.showMessageDialog(this, 
						OStrings.TF_NOTICE_OK_TAGS,
						OStrings.TF_NOTICE_TITLE_TAGS,
						JOptionPane.INFORMATION_MESSAGE);
		}
	}

    public void doNextEntry()
	{
		if (!m_projectLoaded)
			return;
		
		commitEntry();

		m_curEntryNum++;
		if (m_curEntryNum > m_xlLastEntry)
		{
			if (m_curEntryNum >= CommandThread.core.numEntries())
				m_curEntryNum = 0;
			loadDocument();
		}
		activateEntry();
	}

	public void doPrevEntry()
	{
		if (!m_projectLoaded)
			return;
		
		commitEntry();

		m_curEntryNum--;
		if (m_curEntryNum < m_xlFirstEntry)
		{
			if (m_curEntryNum < 0)
				m_curEntryNum = CommandThread.core.numEntries() - 1;
			loadDocument();
		}
		activateEntry();
	}

	/**
	 * Finds the next untranslated entry in the document.
     * 
     * @author Henry Pjiffers
     * @author Maxym Mykhalchuk
	 */	
	public void doNextUntranslatedEntry()
	{
		// check if a document is loaded
		if (m_projectLoaded == false)
			return;
		
		// save the current entry
		commitEntry();
	
		// get the current entry number and the total number of entries
		int curEntryNum = m_curEntryNum;
		int numEntries = CommandThread.core.numEntries();
		
		// iterate through the list of entries,
		// starting at the current entry,
		// until an entry with no translation is found
        //
		// P.S. going to the next entry anyway, even if it's not translated
		curEntryNum++;
        
		SourceTextEntry entry = null;
		while (curEntryNum < numEntries)
		{
			// get the next entry
			entry = CommandThread.core.getSTE(curEntryNum);
			
			// check if the entry is not null, and whether it contains a translation
			if (   (entry != null)
			    && (entry.getTranslation().length() == 0))
			{
				// mark the entry
				m_curEntryNum = curEntryNum;
				
				// load the document, if the segment is not in the current document
				if (m_curEntryNum > m_xlLastEntry)
					loadDocument();
				
				// stop searching
				break;
			}
			
			// next entry
			curEntryNum++;
		}
        
        // activate the entry
        activateEntry();
	}

	// insert current fuzzy match at cursor position
    private void doInsertTrans()
	{
		if (!m_projectLoaded)
			return;
		
		if (m_curNear == null)
			return;

		StringEntry se = m_curNear.str;
		String s = se.getTrans();
		int pos = xlPane.getCaretPosition();
		xlPane.select(pos, pos);
		xlPane.replaceSelection(s);
	}

	// replace entire edit area with active fuzzy match
	public void doRecycleTrans()
	{
		if (!m_projectLoaded)
			return;
		
		if (m_curNear == null)
			return;

		StringEntry se = m_curNear.str;
		doReplaceEditText(se.getTrans());
	}

    private void doReplaceEditText(String text)
	{
		if (!m_projectLoaded)
			return;
		
		if (m_curNear == null)
			return;

		if (text != null)
		{
			// remove current text
			// build local offsets
			int start = m_segmentStartOffset + m_sourceDisplayLength +
				OStrings.TF_CUR_SEGMENT_START.length() + 1;
			int end = xlPane.getText().length() - m_segmentEndInset -
				OStrings.TF_CUR_SEGMENT_END.length();

			// remove text
//StaticUtils.log("removing text "+start+" -> "+end+" length:"+(end-start));
			xlPane.select(start, end);
			xlPane.replaceSelection(text);
		}
	}

	public void doCompareN(int nearNum)
	{
		if (!m_projectLoaded)
			return;
		
		updateFuzzyInfo(nearNum);
	}

	public void doUnloadProject()
	{
		if (m_projectLoaded)
		{
			commitEntry();
			doSave();
		}
		m_projWin.reset();
		m_projectLoaded = false;
		xlPane.setText("");													// NOI18N
        
        m_matchViewer.reset();

        projectOpenMenuItem.setEnabled(true);
		projectNewMenuItem.setEnabled(true);
		optionsSetupFileFiltersMenuItem.setEnabled(true);
	}

    /**
     * Displays the font dialog to allow selecting
     * the font for source, target text (in main window) 
     * and for match and glossary windows.
     */
    private void doFont()
	{
		FontSelectionDialog dlg = new FontSelectionDialog(this, m_font);
		dlg.setVisible(true);
		if( dlg.getReturnStatus()==FontSelectionDialog.RET_OK_CHANGED )
		{
			// fonts have changed  
			// first commit current translation
			commitEntry();
            m_font = dlg.getSelectedFont();
			xlPane.setFont(m_font);
			m_matchViewer.setFont(m_font);
            PreferenceManager.pref.setPreference(OConsts.TF_SRC_FONT_NAME, m_font.getName());
            PreferenceManager.pref.setPreference(OConsts.TF_SRC_FONT_SIZE, String.valueOf(m_font.getSize()));
			activateEntry();
		}
	}
	
    /**
     * Displays the filters setup dialog to allow 
     * customizing file filters in detail.
     */
    private void doFilters()
    {
        FiltersCustomizer dlg = new FiltersCustomizer(this);
        dlg.setVisible(true);
        if( dlg.getReturnStatus()==FiltersCustomizer.RET_OK )
        {
            // saving config
            FilterMaster.getInstance().saveConfig();
        }
        else
        {
            // reloading config from disk
            FilterMaster.getInstance().loadConfig();
        }
    }
    
    private void doSave()
	{
		if (!m_projectLoaded)
			return;
		
		RequestPacket pack;
		pack = new RequestPacket(RequestPacket.SAVE, this);
		CommandThread.core.messageBoardPost(pack);
	}

	private void doLoadProject()
	{
		doUnloadProject();
		m_matchViewer.reset();
		
		RequestPacket load;
		load = new RequestPacket(RequestPacket.LOAD, this);
		CommandThread.core.messageBoardPost(load);
	}

	public void doGotoEntry(int entryNum)
	{
		if (!m_projectLoaded)
			return;
		
		commitEntry();

		m_curEntryNum = entryNum - 1;
		if (m_curEntryNum < m_xlFirstEntry)
		{
			if (m_curEntryNum < 0)
				m_curEntryNum = CommandThread.core.numEntries();
			loadDocument();
		}
		else if (m_curEntryNum > m_xlLastEntry)
		{
			if (m_curEntryNum >= CommandThread.core.numEntries())
				m_curEntryNum = 0;
			loadDocument();
		}
		activateEntry();
	}

	public void doGotoEntry(String str)
	{
		int num;
		try
		{
			num = Integer.parseInt(str);
			doGotoEntry(num);
		}
		catch (NumberFormatException e) {
        }
	}

	public void finishLoadProject()
	{
		m_activeProj = CommandThread.core.projName();
		m_activeFile = "";														// NOI18N
		m_curEntryNum = 0;
		loadDocument();
		m_projectLoaded = true;
        
        projectOpenMenuItem.setEnabled(false);
		projectNewMenuItem.setEnabled(false);
		optionsSetupFileFiltersMenuItem.setEnabled(false);
	}

	private void doCompileProject()
	{
		if (!m_projectLoaded)
			return;
		try 
		{
			CommandThread.core.compileProject();
		}
		catch(IOException e)
		{
			displayError(OStrings.TF_COMPILE_ERROR, e);
		}
		catch(TranslationException te)
		{
			displayError(OStrings.TF_COMPILE_ERROR, te);
		}
	}

	private void doFind()
	{
		String selection = xlPane.getSelectedText();
		if (selection != null)
		{
			selection.trim();
			if (selection.length() < 3)
			{
				selection = null;
			}
		}

		SearchThread srch = new SearchThread(this, selection);
		srch.start();
	}

	/* updates status label */
	public void setMessageText(String str)
	{
		if( str.equals("") )													// NOI18N
			str = " ";															// NOI18N
		statusLabel.setText(str);
	}

	/////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	// internal routines 

	// displays all segments in current document
	// displays translation for each segment if it's available (in dark gray)
	// otherwise displays source text (in black)
	// stores length of each displayed segment plus its starting offset
    private void loadDocument()
	{
		m_docReady = false;

		// clear old text
		xlPane.setText("");													// NOI18N
		m_docSegList.clear();
		m_curEntry = CommandThread.core.getSTE(m_curEntryNum);
		setMessageText(OStrings.TF_LOADING_FILE + 
								m_curEntry.getSrcFile().name);
		Thread.yield();	// let UI update
		m_xlFirstEntry = m_curEntry.getFirstInFile();
		m_xlLastEntry = m_curEntry.getLastInFile();

		DocumentSegment docSeg;
		StringBuffer textBuf = new StringBuffer();
		
		for (int entryNum=m_xlFirstEntry; entryNum<=m_xlLastEntry; entryNum++)
		{
			docSeg = new DocumentSegment();
			
			SourceTextEntry ste = CommandThread.core.getSTE(entryNum);
			String text = ste.getTranslation();
			// set text and font
			if( text.length()==0 ) 
			{
				// no translation available - use source text
				text = ste.getSrcText(); 
			}
			text += "\n\n";														// NOI18N
			
			textBuf.append(text);

			docSeg.length = text.length();
			m_docSegList.add(docSeg);
		}
		xlPane.setText(textBuf.toString());
		
		setMessageText("");														// NOI18N
		Thread.yield();
	}

	///////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////
	// display oriented code

	// display fuzzy matching info if it's available
	// don't call this directly - should only be called through doCompareN
    private void updateFuzzyInfo(int nearNum)
	{
		if (!m_projectLoaded)
			return;
		
		StringEntry curEntry = m_curEntry.getStrEntry();
		List nearList = curEntry.getNearListTranslated();
		// see if there are any matches
		if( nearList.size()<=0 ) 
		{
			m_curNear = null;
			m_matchViewer.updateMatchText();
			return;
		}
		
		m_curNear = (NearString) nearList.get(nearNum);
		String str = null;
		
		NearString ns;
		int ctr = 0;
		int offset;
		int start = -1;
		int end = -1;
		ListIterator li = nearList.listIterator();
		
		while( li.hasNext() ) 
		{
			ns = (NearString) li.next();
			
			String oldStr = ns.str.getSrcText();
			String locStr = ns.str.getTrans();
			String proj = ns.proj;
			offset = m_matchViewer.addMatchTerm(oldStr, locStr,	(int)(ns.score*100), proj);
			
			if( ctr==nearNum ) {
				start = offset;
				str = oldStr;
			} else if( ctr==nearNum + 1 ) {
				end = offset;
			}
			
			ctr++;
		}
		
		m_matchViewer.hiliteRange(start, end);
		m_matchViewer.updateMatchText();
		m_matchViewer.formatNearText(m_curNear.str.getSrcTokenList(), m_curNear.attr);
	}
	
	private void commitEntry()
	{
		if (!m_projectLoaded)
		{
			return;
		}
		// read current entry text and commit it to memory if it's changed
		// clear out segment markers while we're at it

		// +1 is for newline between source text and SEGMENT 
		int start = m_segmentStartOffset + m_sourceDisplayLength + 
					OStrings.TF_CUR_SEGMENT_START.length() + 1;
		int end = xlPane.getText().length() - m_segmentEndInset - 
					OStrings.TF_CUR_SEGMENT_END.length();
		String s;
		if (start == end)
		{
			s = m_curEntry.getSrcText();
			xlPane.select(start, end);
			xlPane.replaceSelection(s);
			end += s.length();
		}
		else
			s = xlPane.getText().substring(start, end);

		xlPane.select(start, end);
		MutableAttributeSet mattr;
		mattr = new SimpleAttributeSet();
		StyleConstants.setForeground(mattr, Color.darkGray);
		xlPane.setCharacterAttributes(mattr, true);
		
		xlPane.select(end, xlPane.getText().length() - m_segmentEndInset);
		xlPane.replaceSelection("");											// NOI18N
		xlPane.select(m_segmentStartOffset, start);
		xlPane.replaceSelection("");											// NOI18N
			
		// update memory
		m_curEntry.setTranslation(s);
		
		DocumentSegment docSeg = (DocumentSegment) 
							m_docSegList.get(m_curEntryNum - m_xlFirstEntry);
		docSeg.length = s.length() + "\n\n".length();							// NOI18N	
		
		// update the length parameters of all changed segments
		// update strings in display
		if (!s.equals(m_curTrans))
		{
			// update display
			// find all identical strings and redraw them
			SourceTextEntry ste = CommandThread.core.getSTE(m_curEntryNum);
			StringEntry se = ste.getStrEntry();
			ListIterator it = se.getParentList().listIterator();
			int entry;
			int offset;
			int i;
			while (it.hasNext())
			{
				ste = (SourceTextEntry) it.next();
				entry = ste.entryNum();
				if (entry >= m_xlFirstEntry && entry <= m_xlLastEntry)
				{
					// found something to update
					// find offset to this segment, remove it and
					//	replace the updated text
					offset = 0;
					// current entry is already handled 
					if (entry == m_curEntryNum)
						continue;

					// build offset
					for (i=m_xlFirstEntry; i<entry; i++)
					{
						docSeg = (DocumentSegment) m_docSegList.get(
								i-m_xlFirstEntry);
						offset += docSeg.length;
					}
					// replace old text w/ new
					docSeg = (DocumentSegment) m_docSegList.get(
								entry - m_xlFirstEntry);
					xlPane.select(offset, offset+docSeg.length);
					xlPane.replaceSelection(s + "\n\n");						// NOI18N
					docSeg.length = s.length() + "\n\n".length();				// NOI18N
				}
			}
		}
        xlPane.cancelUndo();
	}

	// activate current entry by displaying source text and imbedding
	//	displaying text in markers
	// move document focus to current entry
	// make sure fuzzy info displayed if available and wanted
	public void activateEntry() 
	{
		if (!m_projectLoaded)
			return;

		int i;
		DocumentSegment docSeg;

		// recover data about current entry
		m_curEntry = CommandThread.core.getSTE(m_curEntryNum);
		String srcText = m_curEntry.getSrcText();

		m_sourceDisplayLength = srcText.length();
		
		// sum up total character offset to current segment start
		m_segmentStartOffset = 0;
		for (i=m_xlFirstEntry; i<m_curEntryNum; i++)
		{
			docSeg = (DocumentSegment) m_docSegList.get(i-m_xlFirstEntry);
			m_segmentStartOffset += docSeg.length; // length includes \n
		}

		docSeg = (DocumentSegment) m_docSegList.get(
					m_curEntryNum - m_xlFirstEntry);
		// -2 to move inside newlines at end of segment
		int paneLen = xlPane.getText().length();
		m_segmentEndInset = paneLen - (m_segmentStartOffset + docSeg.length-2);

		// get label tags
		String startStr = "\n" + OStrings.TF_CUR_SEGMENT_START;					// NOI18N
		String endStr = OStrings.TF_CUR_SEGMENT_END;
		if (m_segmentTagHasNumber)
		{
			// put entry number in first tag
			int num = m_curEntryNum + 1;
			int ones;
			
			// do it digit by digit - there's a better way (like sprintf)
			//	but this works just fine
			String disp = "";													// NOI18N
			while (num > 0)
			{
				ones = num % 10;
				num /= 10;
				disp = ones + disp;
			}
			int zero = startStr.lastIndexOf('0');
			startStr = startStr.substring(0, zero-disp.length()+1) + 
				disp + startStr.substring(zero+1);
		}

		MutableAttributeSet mattr;
		// append to end of segment first because this operation is done
		//	by reference to end of file which will change after insert
		xlPane.select(paneLen-m_segmentEndInset, paneLen-m_segmentEndInset);
		xlPane.replaceSelection(endStr);
		xlPane.select(paneLen-m_segmentEndInset + 1, 
				paneLen-m_segmentEndInset + endStr.length());
		mattr = new SimpleAttributeSet();
		StyleConstants.setBold(mattr, true);
		xlPane.setCharacterAttributes(mattr, true);

		xlPane.select(m_segmentStartOffset, m_segmentStartOffset);
		String insertText = srcText + startStr;
		xlPane.replaceSelection(insertText);
		xlPane.select(m_segmentStartOffset, m_segmentStartOffset + 
				insertText.length() - 1);
		xlPane.setCharacterAttributes(mattr, true);

		// background color
		Color background = new Color(192, 255, 192);
		// other color options
		xlPane.select(m_segmentStartOffset, m_segmentStartOffset + 
				insertText.length() - startStr.length());
		mattr = new SimpleAttributeSet();
		StyleConstants.setBackground(mattr, background);
		xlPane.setCharacterAttributes(mattr, false);

		// TODO XXX format source text if there is near match
		
		if (m_curEntry.getSrcFile().name.compareTo(m_activeFile) != 0)
		{
			m_activeFile = m_curEntry.getSrcFile().name;
			updateTitle();
		}
		
		// TODO set word counts

		doCompareN(0);

		// add glossary terms and fuzzy match info to match window
		StringEntry curEntry = m_curEntry.getStrEntry();
		if (curEntry.getGlossaryEntries().size() > 0)
		{
			// TODO do something with glossary terms
			m_glossaryLength = curEntry.getGlossaryEntries().size();
			ListIterator li = curEntry.getGlossaryEntries().listIterator();
			while (li.hasNext())
			{
				GlossaryEntry glos = (GlossaryEntry) li.next();
				m_matchViewer.addGlosTerm(glos.getSrcText(), glos.getLocText(),
						glos.getCommentText());
			}
		
		}
		else
			m_glossaryLength = 0;
		m_matchViewer.updateGlossaryText();

		int nearLength = curEntry.getNearListTranslated().size();
		
		if (nearLength > 0 && m_glossaryLength > 0)
		{
			// display text indicating both categories exist
			Object obj[] = { 
					new Integer(nearLength), 
					new Integer(m_glossaryLength) };
			setMessageText(MessageFormat.format(
							OStrings.TF_NUM_NEAR_AND_GLOSSARY, obj));
		}
		else if (nearLength > 0)
		{
			Object obj[] = { new Integer(nearLength) };
			setMessageText(MessageFormat.format(
							OStrings.TF_NUM_NEAR, obj));
		}
		else if (m_glossaryLength > 0)
		{
			Object obj[] = { new Integer(m_glossaryLength) };
			setMessageText(MessageFormat.format(
							OStrings.TF_NUM_GLOSSARY, obj));
		}
		else
			setMessageText("");													// NOI18N

		// TODO - hilite translation area in yellow

		// set caret position

		// try to scroll so next 3 entries are displayed after current entry
		//	or first entry ending after the 500 characters mark
		// to do this, set cursor 3 entries down, then reset it to 
		//	begging of source text in current entry, then finnaly into
		//	editing region
		int padding = 0;
		int j;
		for (i=m_curEntryNum+1-m_xlFirstEntry, j=0; 
				i<=m_xlLastEntry-m_xlFirstEntry; 
				i++, j++)
		{
			docSeg = (DocumentSegment) m_docSegList.get(i);
			padding += docSeg.length;
			if (j > 2 || padding > 500)
				break;
		}
		// don't try to set caret after end of document
		if (padding > m_segmentEndInset)
			padding = m_segmentEndInset;
		xlPane.setCaretPosition(xlPane.getText().length() - 
				m_segmentEndInset + padding);

		// try to make sure entire segment displays
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				// make sure 2 newlines above current segment are visible
				int loc = m_segmentStartOffset - 3;
				if (loc < 0)
					loc = 0;
				xlPane.setCaretPosition(loc);
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						checkCaret();
					}
				});

			}
		});
//
//		checkCaret();
		if (!m_docReady)
		{
			m_docReady = true;
		}
		xlPane.cancelUndo();
	}

	public void displayWarning(String msg, Throwable e)
	{
		setMessageText(msg);
		String str = OStrings.TF_WARNING;
		JOptionPane.showMessageDialog(this, msg + "\n" + e.toString(),			// NOI18N
					str, JOptionPane.WARNING_MESSAGE);
	}

	public void displayError(String msg, Throwable e)
	{
		setMessageText(msg);
		String str = OStrings.TF_ERROR;
		JOptionPane.showMessageDialog(this, msg + "\n" + e.toString(),			// NOI18N 
					str, JOptionPane.ERROR_MESSAGE);
	}

    /**
     * Make sure there's one character in the direction indicated for
     * delete operation.
     *
     * @param forward 
     * @return true if space is available
     */
    public boolean checkCaretForDelete(boolean forward)
	{
		int pos = xlPane.getCaretPosition();
		
		// make sure range doesn't overlap boundaries
		checkCaret();

		if (forward)
		{
			// make sure we're not at end of segment
			// -1 for space before tag, -2 for newlines
			int end = xlPane.getText().length() - m_segmentEndInset -
				OStrings.TF_CUR_SEGMENT_END.length();
       		int spos = xlPane.getSelectionStart();
    		int epos = xlPane.getSelectionEnd();
			if( pos>=end && spos>=end && epos>=end )
				return false;
		}
		else
		{
			// make sure we're not at start of segment
			int start = m_segmentStartOffset + m_sourceDisplayLength +
				OStrings.TF_CUR_SEGMENT_START.length() + 1;
       		int spos = xlPane.getSelectionStart();
    		int epos = xlPane.getSelectionEnd();
			if( pos<=start && epos<=start && spos<=start )
				return false;
		}
		return true;
	}
	
    /**
     * Checks whether the selection & caret is inside editable text,
     * and changes their positions accordingly if not.
     */
    public void checkCaret()
	{
		//int pos = m_xlPane.getCaretPosition();
		int spos = xlPane.getSelectionStart();
		int epos = xlPane.getSelectionEnd();
		int start = m_segmentStartOffset + m_sourceDisplayLength +
			OStrings.TF_CUR_SEGMENT_START.length() + 1;
		// -1 for space before tag, -2 for newlines
		int end = xlPane.getText().length() - m_segmentEndInset -
			OStrings.TF_CUR_SEGMENT_END.length();

		if (spos != epos)
		{
			// dealing with a selection here - make sure it's w/in bounds
			if (spos < start)
			{
				xlPane.setSelectionStart(start);
			}
			else if (spos > end)
			{
				xlPane.setSelectionStart(end);
			}
			if (epos > end)
			{
				xlPane.setSelectionEnd(end);
			}
			else if (epos < start)
			{
				xlPane.setSelectionStart(start);
			}
		}
		else
		{
			// non selected text 
			if (spos < start)
			{
				xlPane.setCaretPosition(start);
			}
			else if (spos > end)
			{
				xlPane.setCaretPosition(end);
			}
		}
    }

	public void fatalError(String msg, Throwable re)
	{
		StaticUtils.log(msg);
		if (re != null)
        {
			re.printStackTrace(StaticUtils.getLogStream());
            re.printStackTrace();
        }

		// try to shutdown gracefully
		CommandThread.core.signalStop();
		for (int i=0; i<25; i++)
		{
			while (CommandThread.core != null)
			{
				try { Thread.sleep(10); }
				catch (InterruptedException e) {
                }
			}
			if (CommandThread.core == null)
				break;
		}
        Runtime.getRuntime().halt(1);
	}
	
	/**
	 * Overrides parent method to show Match/Glossary viewer 
	 * simultaneously with the main frame.
	 */
	public void setVisible(boolean b)
	{
		super.setVisible(b);
		m_matchViewer.setVisible(b);
        m_matchViewer.setFont(m_font);
		toFront();
	}

	public boolean	isProjectLoaded()	{ return m_projectLoaded;		}

	/** The font for main window (source and target text) and for match and glossary windows */
    private Font m_font;

	// first and last entry numbers in current file
    public int		m_xlFirstEntry;
	public int		m_xlLastEntry;

	// starting offset and length of source lang in current segment
    public int		m_segmentStartOffset;
	public int		m_sourceDisplayLength;
	public int		m_segmentEndInset;
	// text length of glossary, if displayed
    private int		m_glossaryLength;

	// boolean set after safety check that org.omegat.OStrings.TF_CUR_SEGMENT_START
	//	contains empty "0000" for segment number
    private boolean	m_segmentTagHasNumber;

	// indicates the document is loaded and ready for processing
    public boolean	m_docReady;

    // list of text segments in current doc
    public ArrayList	m_docSegList;

	public char	m_advancer;

    /** Main panel with source and target strings */
	private XLPane		xlPane;

	private SourceTextEntry		m_curEntry;

	private String	m_activeFile;
	private String	m_activeProj;
	public int m_curEntryNum;
	private NearString m_curNear;
    private String	m_curTrans = "";										// NOI18N

	private ProjectFrame	m_projWin;
    public ProjectFrame getProjectFrame()
    {
        return m_projWin;
    }
    
	private MatchWindow	m_matchViewer;

	public boolean m_projectLoaded;

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        statusLabel = new javax.swing.JLabel();
        mainArea = new javax.swing.JPanel();
        mainScroller = new javax.swing.JScrollPane();
        mainMenu = new javax.swing.JMenuBar();
        projectMenu = new javax.swing.JMenu();
        projectNewMenuItem = new javax.swing.JMenuItem();
        projectOpenMenuItem = new javax.swing.JMenuItem();
        projectCloseMenuItem = new javax.swing.JMenuItem();
        projectSaveMenuItem = new javax.swing.JMenuItem();
        separator1inProjectMenu = new javax.swing.JSeparator();
        projectCompileMenuItem = new javax.swing.JMenuItem();
        separator2inProjectMenu = new javax.swing.JSeparator();
        projectExitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        editUndoMenuItem = new javax.swing.JMenuItem();
        editRedoMenuItem = new javax.swing.JMenuItem();
        separator1inEditMenu = new javax.swing.JSeparator();
        editOverwriteTranslationMenuItem = new javax.swing.JMenuItem();
        editInsertTranslationMenuItem = new javax.swing.JMenuItem();
        separator2inEditMenu = new javax.swing.JSeparator();
        editFindMenuItem = new javax.swing.JMenuItem();
        editFindInProjectMenuItem = new javax.swing.JMenuItem();
        separator3inEditMenu = new javax.swing.JSeparator();
        editSelectFuzzy1MenuItem = new javax.swing.JMenuItem();
        editSelectFuzzy2MenuItem = new javax.swing.JMenuItem();
        editSelectFuzzy3MenuItem = new javax.swing.JMenuItem();
        editSelectFuzzy4MenuItem = new javax.swing.JMenuItem();
        editSelectFuzzy5MenuItem = new javax.swing.JMenuItem();
        gotoMenu = new javax.swing.JMenu();
        gotoNextUntranslatedMenuItem = new javax.swing.JMenuItem();
        gotoNextSegmentMenuItem = new javax.swing.JMenuItem();
        gotoPreviousSegmentMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        viewMatchWindowCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        viewFileListCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        toolsMenu = new javax.swing.JMenu();
        toolsValidateTagsMenuItem = new javax.swing.JMenuItem();
        optionsMenu = new javax.swing.JMenu();
        optionsTabAdvanceCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        separator1inOptionsMenu = new javax.swing.JSeparator();
        optionsFontSelectionMenuItem = new javax.swing.JMenuItem();
        optionsSetupFileFiltersMenuItem = new javax.swing.JMenuItem();
        optionsKeyboardShortcutsMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        helpContentsMenuItem = new javax.swing.JMenuItem();
        helpAboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(this);

        getContentPane().add(statusLabel, java.awt.BorderLayout.SOUTH);

        mainArea.setLayout(new java.awt.BorderLayout());

        mainScroller.setBorder(null);
        mainArea.add(mainScroller, java.awt.BorderLayout.CENTER);

        getContentPane().add(mainArea, java.awt.BorderLayout.CENTER);

        org.openide.awt.Mnemonics.setLocalizedText(projectMenu, OStrings.getString("TF_MENU_FILE"));
        org.openide.awt.Mnemonics.setLocalizedText(projectNewMenuItem, OStrings.getString("TF_MENU_FILE_CREATE"));
        projectNewMenuItem.addActionListener(this);

        projectMenu.add(projectNewMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(projectOpenMenuItem, OStrings.getString("TF_MENU_FILE_OPEN"));
        projectOpenMenuItem.addActionListener(this);

        projectMenu.add(projectOpenMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(projectCloseMenuItem, OStrings.getString("TF_MENU_FILE_CLOSE"));
        projectCloseMenuItem.addActionListener(this);

        projectMenu.add(projectCloseMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(projectSaveMenuItem, OStrings.getString("TF_MENU_FILE_SAVE"));
        projectSaveMenuItem.addActionListener(this);

        projectMenu.add(projectSaveMenuItem);

        projectMenu.add(separator1inProjectMenu);

        org.openide.awt.Mnemonics.setLocalizedText(projectCompileMenuItem, OStrings.getString("TF_MENU_FILE_COMPILE"));
        projectCompileMenuItem.addActionListener(this);

        projectMenu.add(projectCompileMenuItem);

        projectMenu.add(separator2inProjectMenu);

        org.openide.awt.Mnemonics.setLocalizedText(projectExitMenuItem, OStrings.getString("TF_MENU_FILE_QUIT"));
        projectExitMenuItem.addActionListener(this);

        projectMenu.add(projectExitMenuItem);

        mainMenu.add(projectMenu);

        org.openide.awt.Mnemonics.setLocalizedText(editMenu, OStrings.getString("TF_MENU_EDIT"));
        org.openide.awt.Mnemonics.setLocalizedText(editUndoMenuItem, OStrings.getString("TF_MENU_EDIT_UNDO"));
        editUndoMenuItem.addActionListener(this);

        editMenu.add(editUndoMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(editRedoMenuItem, OStrings.getString("TF_MENU_EDIT_REDO"));
        editRedoMenuItem.addActionListener(this);

        editMenu.add(editRedoMenuItem);

        editMenu.add(separator1inEditMenu);

        org.openide.awt.Mnemonics.setLocalizedText(editOverwriteTranslationMenuItem, OStrings.getString("TF_MENU_EDIT_RECYCLE"));
        editOverwriteTranslationMenuItem.addActionListener(this);

        editMenu.add(editOverwriteTranslationMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(editInsertTranslationMenuItem, OStrings.getString("TF_MENU_EDIT_INSERT"));
        editInsertTranslationMenuItem.addActionListener(this);

        editMenu.add(editInsertTranslationMenuItem);

        editMenu.add(separator2inEditMenu);

        org.openide.awt.Mnemonics.setLocalizedText(editFindMenuItem, "&Find...");
        editFindMenuItem.addActionListener(this);

        editMenu.add(editFindMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(editFindInProjectMenuItem, OStrings.getString("TF_MENU_EDIT_FIND"));
        editFindInProjectMenuItem.addActionListener(this);

        editMenu.add(editFindInProjectMenuItem);

        editMenu.add(separator3inEditMenu);

        org.openide.awt.Mnemonics.setLocalizedText(editSelectFuzzy1MenuItem, OStrings.getString("TF_MENU_EDIT_COMPARE_1"));
        editSelectFuzzy1MenuItem.addActionListener(this);

        editMenu.add(editSelectFuzzy1MenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(editSelectFuzzy2MenuItem, OStrings.getString("TF_MENU_EDIT_COMPARE_2"));
        editSelectFuzzy2MenuItem.addActionListener(this);

        editMenu.add(editSelectFuzzy2MenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(editSelectFuzzy3MenuItem, OStrings.getString("TF_MENU_EDIT_COMPARE_3"));
        editSelectFuzzy3MenuItem.addActionListener(this);

        editMenu.add(editSelectFuzzy3MenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(editSelectFuzzy4MenuItem, OStrings.getString("TF_MENU_EDIT_COMPARE_4"));
        editSelectFuzzy4MenuItem.addActionListener(this);

        editMenu.add(editSelectFuzzy4MenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(editSelectFuzzy5MenuItem, OStrings.getString("TF_MENU_EDIT_COMPARE_5"));
        editSelectFuzzy5MenuItem.addActionListener(this);

        editMenu.add(editSelectFuzzy5MenuItem);

        mainMenu.add(editMenu);

        org.openide.awt.Mnemonics.setLocalizedText(gotoMenu, "&Go To");
        org.openide.awt.Mnemonics.setLocalizedText(gotoNextUntranslatedMenuItem, OStrings.getString("TF_MENU_EDIT_UNTRANS"));
        gotoNextUntranslatedMenuItem.addActionListener(this);

        gotoMenu.add(gotoNextUntranslatedMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(gotoNextSegmentMenuItem, OStrings.getString("TF_MENU_EDIT_NEXT"));
        gotoNextSegmentMenuItem.addActionListener(this);

        gotoMenu.add(gotoNextSegmentMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(gotoPreviousSegmentMenuItem, OStrings.getString("TF_MENU_EDIT_PREV"));
        gotoPreviousSegmentMenuItem.addActionListener(this);

        gotoMenu.add(gotoPreviousSegmentMenuItem);

        mainMenu.add(gotoMenu);

        org.openide.awt.Mnemonics.setLocalizedText(viewMenu, "&View");
        org.openide.awt.Mnemonics.setLocalizedText(viewMatchWindowCheckBoxMenuItem, OStrings.getString("TF_MENU_FILE_MATCHWIN"));
        viewMatchWindowCheckBoxMenuItem.addActionListener(this);

        viewMenu.add(viewMatchWindowCheckBoxMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(viewFileListCheckBoxMenuItem, OStrings.getString("TF_MENU_FILE_PROJWIN"));
        viewFileListCheckBoxMenuItem.addActionListener(this);

        viewMenu.add(viewFileListCheckBoxMenuItem);

        mainMenu.add(viewMenu);

        org.openide.awt.Mnemonics.setLocalizedText(toolsMenu, OStrings.getString("TF_MENU_TOOLS"));
        org.openide.awt.Mnemonics.setLocalizedText(toolsValidateTagsMenuItem, OStrings.getString("TF_MENU_TOOLS_VALIDATE"));
        toolsValidateTagsMenuItem.addActionListener(this);

        toolsMenu.add(toolsValidateTagsMenuItem);

        mainMenu.add(toolsMenu);

        org.openide.awt.Mnemonics.setLocalizedText(optionsMenu, "&Options");
        org.openide.awt.Mnemonics.setLocalizedText(optionsTabAdvanceCheckBoxMenuItem, OStrings.getString("TF_MENU_DISPLAY_ADVANCE"));
        optionsTabAdvanceCheckBoxMenuItem.addActionListener(this);

        optionsMenu.add(optionsTabAdvanceCheckBoxMenuItem);

        optionsMenu.add(separator1inOptionsMenu);

        org.openide.awt.Mnemonics.setLocalizedText(optionsFontSelectionMenuItem, OStrings.getString("TF_MENU_DISPLAY_FONT"));
        optionsFontSelectionMenuItem.addActionListener(this);

        optionsMenu.add(optionsFontSelectionMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(optionsSetupFileFiltersMenuItem, OStrings.getString("TF_MENU_DISPLAY_FILTERS"));
        optionsSetupFileFiltersMenuItem.addActionListener(this);

        optionsMenu.add(optionsSetupFileFiltersMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(optionsKeyboardShortcutsMenuItem, "&Keyboard Shortcuts");
        optionsKeyboardShortcutsMenuItem.addActionListener(this);

        optionsMenu.add(optionsKeyboardShortcutsMenuItem);

        mainMenu.add(optionsMenu);

        org.openide.awt.Mnemonics.setLocalizedText(helpMenu, OStrings.getString("TF_MENU_HELP"));
        org.openide.awt.Mnemonics.setLocalizedText(helpContentsMenuItem, OStrings.getString("TF_MENU_HELP_CONTENTS"));
        helpContentsMenuItem.addActionListener(this);

        helpMenu.add(helpContentsMenuItem);

        org.openide.awt.Mnemonics.setLocalizedText(helpAboutMenuItem, OStrings.getString("TF_MENU_HELP_ABOUT"));
        helpAboutMenuItem.addActionListener(this);

        helpMenu.add(helpAboutMenuItem);

        mainMenu.add(helpMenu);

        setJMenuBar(mainMenu);

        pack();
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt)
    {
        if (evt.getSource() == projectNewMenuItem)
        {
            MainWindow.this.projectNewMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectOpenMenuItem)
        {
            MainWindow.this.projectOpenMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectCloseMenuItem)
        {
            MainWindow.this.projectCloseMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectSaveMenuItem)
        {
            MainWindow.this.projectSaveMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectCompileMenuItem)
        {
            MainWindow.this.projectCompileMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == projectExitMenuItem)
        {
            MainWindow.this.projectExitMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editUndoMenuItem)
        {
            MainWindow.this.editUndoMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editRedoMenuItem)
        {
            MainWindow.this.editRedoMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editOverwriteTranslationMenuItem)
        {
            MainWindow.this.editOverwriteTranslationMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editInsertTranslationMenuItem)
        {
            MainWindow.this.editInsertTranslationMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editFindMenuItem)
        {
            MainWindow.this.editFindMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editFindInProjectMenuItem)
        {
            MainWindow.this.editFindInProjectMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editSelectFuzzy1MenuItem)
        {
            MainWindow.this.editSelectFuzzy1MenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editSelectFuzzy2MenuItem)
        {
            MainWindow.this.editSelectFuzzy2MenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editSelectFuzzy3MenuItem)
        {
            MainWindow.this.editSelectFuzzy3MenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editSelectFuzzy4MenuItem)
        {
            MainWindow.this.editSelectFuzzy4MenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == editSelectFuzzy5MenuItem)
        {
            MainWindow.this.editSelectFuzzy5MenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == gotoNextUntranslatedMenuItem)
        {
            MainWindow.this.gotoNextUntranslatedMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == gotoNextSegmentMenuItem)
        {
            MainWindow.this.gotoNextSegmentMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == gotoPreviousSegmentMenuItem)
        {
            MainWindow.this.gotoPreviousSegmentMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == viewMatchWindowCheckBoxMenuItem)
        {
            MainWindow.this.viewMatchWindowCheckBoxMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == viewFileListCheckBoxMenuItem)
        {
            MainWindow.this.viewFileListCheckBoxMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == toolsValidateTagsMenuItem)
        {
            MainWindow.this.toolsValidateTagsMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsFontSelectionMenuItem)
        {
            MainWindow.this.optionsFontSelectionMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsSetupFileFiltersMenuItem)
        {
            MainWindow.this.optionsSetupFileFiltersMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsKeyboardShortcutsMenuItem)
        {
            MainWindow.this.optionsKeyboardShortcutsMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == helpContentsMenuItem)
        {
            MainWindow.this.helpContentsMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == helpAboutMenuItem)
        {
            MainWindow.this.helpAboutMenuItemActionPerformed(evt);
        }
        else if (evt.getSource() == optionsTabAdvanceCheckBoxMenuItem)
        {
            MainWindow.this.optionsTabAdvanceCheckBoxMenuItemActionPerformed(evt);
        }
    }

    public void windowActivated(java.awt.event.WindowEvent evt)
    {
    }

    public void windowClosed(java.awt.event.WindowEvent evt)
    {
    }

    public void windowClosing(java.awt.event.WindowEvent evt)
    {
        if (evt.getSource() == MainWindow.this)
        {
            MainWindow.this.formWindowClosing(evt);
        }
    }

    public void windowDeactivated(java.awt.event.WindowEvent evt)
    {
    }

    public void windowDeiconified(java.awt.event.WindowEvent evt)
    {
    }

    public void windowIconified(java.awt.event.WindowEvent evt)
    {
    }

    public void windowOpened(java.awt.event.WindowEvent evt)
    {
    }
    // </editor-fold>//GEN-END:initComponents

    private void optionsTabAdvanceCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_optionsTabAdvanceCheckBoxMenuItemActionPerformed
    {//GEN-HEADEREND:event_optionsTabAdvanceCheckBoxMenuItemActionPerformed

        PreferenceManager.pref.setPreference(OConsts.PREF_TAB, 
                optionsTabAdvanceCheckBoxMenuItem.isSelected());
        if( optionsTabAdvanceCheckBoxMenuItem.isSelected() )
			m_advancer = KeyEvent.VK_TAB;
		else
			m_advancer = KeyEvent.VK_ENTER;
    }//GEN-LAST:event_optionsTabAdvanceCheckBoxMenuItemActionPerformed

    private void helpContentsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_helpContentsMenuItemActionPerformed
    {//GEN-HEADEREND:event_helpContentsMenuItemActionPerformed
        HelpFrame hf = HelpFrame.getInstance();
        hf.setVisible(true);
        hf.toFront();
    }//GEN-LAST:event_helpContentsMenuItemActionPerformed

    private void optionsKeyboardShortcutsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_optionsKeyboardShortcutsMenuItemActionPerformed
    {//GEN-HEADEREND:event_optionsKeyboardShortcutsMenuItemActionPerformed
        JOptionPane.showMessageDialog(this, "Not yet implemented", "Error", JOptionPane.ERROR_MESSAGE); // NOI18N
    }//GEN-LAST:event_optionsKeyboardShortcutsMenuItemActionPerformed

    private void optionsSetupFileFiltersMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_optionsSetupFileFiltersMenuItemActionPerformed
    {//GEN-HEADEREND:event_optionsSetupFileFiltersMenuItemActionPerformed
        doFilters();
    }//GEN-LAST:event_optionsSetupFileFiltersMenuItemActionPerformed

    private void optionsFontSelectionMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_optionsFontSelectionMenuItemActionPerformed
    {//GEN-HEADEREND:event_optionsFontSelectionMenuItemActionPerformed
        doFont();
    }//GEN-LAST:event_optionsFontSelectionMenuItemActionPerformed

    private void toolsValidateTagsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_toolsValidateTagsMenuItemActionPerformed
    {//GEN-HEADEREND:event_toolsValidateTagsMenuItemActionPerformed
        doValidateTags();
    }//GEN-LAST:event_toolsValidateTagsMenuItemActionPerformed

    private void editSelectFuzzy5MenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editSelectFuzzy5MenuItemActionPerformed
    {//GEN-HEADEREND:event_editSelectFuzzy5MenuItemActionPerformed
        doCompareN(4);
    }//GEN-LAST:event_editSelectFuzzy5MenuItemActionPerformed

    private void editSelectFuzzy4MenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editSelectFuzzy4MenuItemActionPerformed
    {//GEN-HEADEREND:event_editSelectFuzzy4MenuItemActionPerformed
        doCompareN(3);
    }//GEN-LAST:event_editSelectFuzzy4MenuItemActionPerformed

    private void editSelectFuzzy3MenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editSelectFuzzy3MenuItemActionPerformed
    {//GEN-HEADEREND:event_editSelectFuzzy3MenuItemActionPerformed
        doCompareN(2);
    }//GEN-LAST:event_editSelectFuzzy3MenuItemActionPerformed

    private void editSelectFuzzy2MenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editSelectFuzzy2MenuItemActionPerformed
    {//GEN-HEADEREND:event_editSelectFuzzy2MenuItemActionPerformed
        doCompareN(1);
    }//GEN-LAST:event_editSelectFuzzy2MenuItemActionPerformed

    private void editSelectFuzzy1MenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editSelectFuzzy1MenuItemActionPerformed
    {//GEN-HEADEREND:event_editSelectFuzzy1MenuItemActionPerformed
        doCompareN(0);
    }//GEN-LAST:event_editSelectFuzzy1MenuItemActionPerformed

    private void editFindMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editFindMenuItemActionPerformed
    {//GEN-HEADEREND:event_editFindMenuItemActionPerformed
        JOptionPane.showMessageDialog(this, "Not yet implemented", "Error", JOptionPane.ERROR_MESSAGE); // NOI18N
    }//GEN-LAST:event_editFindMenuItemActionPerformed

    private void editFindInProjectMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editFindInProjectMenuItemActionPerformed
    {//GEN-HEADEREND:event_editFindInProjectMenuItemActionPerformed
        doFind();
    }//GEN-LAST:event_editFindInProjectMenuItemActionPerformed

    private void editInsertTranslationMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editInsertTranslationMenuItemActionPerformed
    {//GEN-HEADEREND:event_editInsertTranslationMenuItemActionPerformed
        doInsertTrans();
    }//GEN-LAST:event_editInsertTranslationMenuItemActionPerformed

    private void editOverwriteTranslationMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editOverwriteTranslationMenuItemActionPerformed
    {//GEN-HEADEREND:event_editOverwriteTranslationMenuItemActionPerformed
        doRecycleTrans();
    }//GEN-LAST:event_editOverwriteTranslationMenuItemActionPerformed

    private void gotoNextUntranslatedMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_gotoNextUntranslatedMenuItemActionPerformed
    {//GEN-HEADEREND:event_gotoNextUntranslatedMenuItemActionPerformed
        doNextUntranslatedEntry();
    }//GEN-LAST:event_gotoNextUntranslatedMenuItemActionPerformed

    private void gotoPreviousSegmentMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_gotoPreviousSegmentMenuItemActionPerformed
    {//GEN-HEADEREND:event_gotoPreviousSegmentMenuItemActionPerformed
        doPrevEntry();
    }//GEN-LAST:event_gotoPreviousSegmentMenuItemActionPerformed

    private void gotoNextSegmentMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_gotoNextSegmentMenuItemActionPerformed
    {//GEN-HEADEREND:event_gotoNextSegmentMenuItemActionPerformed
        doNextEntry();
    }//GEN-LAST:event_gotoNextSegmentMenuItemActionPerformed

    private void editRedoMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editRedoMenuItemActionPerformed
    {//GEN-HEADEREND:event_editRedoMenuItemActionPerformed
        try 
        {
            xlPane.redoOneEdit();
        }
        catch (CannotRedoException cue)	{ }
    }//GEN-LAST:event_editRedoMenuItemActionPerformed

    private void editUndoMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editUndoMenuItemActionPerformed
    {//GEN-HEADEREND:event_editUndoMenuItemActionPerformed
        try
        {
            xlPane.undoOneEdit();
        }
        catch( CannotUndoException cue ) { }
    }//GEN-LAST:event_editUndoMenuItemActionPerformed

    private void viewFileListCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_viewFileListCheckBoxMenuItemActionPerformed
    {//GEN-HEADEREND:event_viewFileListCheckBoxMenuItemActionPerformed
        if( m_projWin==null )
        {
            viewFileListCheckBoxMenuItem.setSelected(false);
            return;
        }
            
        if( viewFileListCheckBoxMenuItem.isSelected() )
        {
            m_projWin.setVisible(true);
            m_projWin.toFront();
        }
        else
        {
            m_projWin.setVisible(false);
        }
    }//GEN-LAST:event_viewFileListCheckBoxMenuItemActionPerformed

    private void viewMatchWindowCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_viewMatchWindowCheckBoxMenuItemActionPerformed
    {//GEN-HEADEREND:event_viewMatchWindowCheckBoxMenuItemActionPerformed
        if( viewMatchWindowCheckBoxMenuItem.isSelected() )
        {
            m_matchViewer.setVisible(true);
            toFront();
        }
        else
        {
            m_matchViewer.setVisible(false);
        }
    }//GEN-LAST:event_viewMatchWindowCheckBoxMenuItemActionPerformed

    private void projectCompileMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectCompileMenuItemActionPerformed
    {//GEN-HEADEREND:event_projectCompileMenuItemActionPerformed
        doCompileProject();
    }//GEN-LAST:event_projectCompileMenuItemActionPerformed

    private void projectCloseMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectCloseMenuItemActionPerformed
    {//GEN-HEADEREND:event_projectCloseMenuItemActionPerformed
        doUnloadProject();
        CommandThread.core.requestUnload();
    }//GEN-LAST:event_projectCloseMenuItemActionPerformed

    private void projectSaveMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectSaveMenuItemActionPerformed
    {//GEN-HEADEREND:event_projectSaveMenuItemActionPerformed
        doSave();
    }//GEN-LAST:event_projectSaveMenuItemActionPerformed

    private void projectOpenMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectOpenMenuItemActionPerformed
    {//GEN-HEADEREND:event_projectOpenMenuItemActionPerformed
        doLoadProject();
    }//GEN-LAST:event_projectOpenMenuItemActionPerformed

    private void projectNewMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectNewMenuItemActionPerformed
    {//GEN-HEADEREND:event_projectNewMenuItemActionPerformed
        CommandThread.core.createProject();
    }//GEN-LAST:event_projectNewMenuItemActionPerformed

    private void projectExitMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectExitMenuItemActionPerformed
    {//GEN-HEADEREND:event_projectExitMenuItemActionPerformed
        doQuit();
    }//GEN-LAST:event_projectExitMenuItemActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
    {//GEN-HEADEREND:event_formWindowClosing
        doQuit();
    }//GEN-LAST:event_formWindowClosing

    private void helpAboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpAboutMenuItemActionPerformed
        new AboutDialog(this).setVisible(true);
    }//GEN-LAST:event_helpAboutMenuItemActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem editFindInProjectMenuItem;
    private javax.swing.JMenuItem editFindMenuItem;
    private javax.swing.JMenuItem editInsertTranslationMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem editOverwriteTranslationMenuItem;
    private javax.swing.JMenuItem editRedoMenuItem;
    private javax.swing.JMenuItem editSelectFuzzy1MenuItem;
    private javax.swing.JMenuItem editSelectFuzzy2MenuItem;
    private javax.swing.JMenuItem editSelectFuzzy3MenuItem;
    private javax.swing.JMenuItem editSelectFuzzy4MenuItem;
    private javax.swing.JMenuItem editSelectFuzzy5MenuItem;
    private javax.swing.JMenuItem editUndoMenuItem;
    private javax.swing.JMenu gotoMenu;
    private javax.swing.JMenuItem gotoNextSegmentMenuItem;
    private javax.swing.JMenuItem gotoNextUntranslatedMenuItem;
    private javax.swing.JMenuItem gotoPreviousSegmentMenuItem;
    private javax.swing.JMenuItem helpAboutMenuItem;
    private javax.swing.JMenuItem helpContentsMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPanel mainArea;
    private javax.swing.JMenuBar mainMenu;
    private javax.swing.JScrollPane mainScroller;
    private javax.swing.JMenuItem optionsFontSelectionMenuItem;
    private javax.swing.JMenuItem optionsKeyboardShortcutsMenuItem;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JMenuItem optionsSetupFileFiltersMenuItem;
    private javax.swing.JCheckBoxMenuItem optionsTabAdvanceCheckBoxMenuItem;
    private javax.swing.JMenuItem projectCloseMenuItem;
    private javax.swing.JMenuItem projectCompileMenuItem;
    private javax.swing.JMenuItem projectExitMenuItem;
    private javax.swing.JMenu projectMenu;
    private javax.swing.JMenuItem projectNewMenuItem;
    private javax.swing.JMenuItem projectOpenMenuItem;
    private javax.swing.JMenuItem projectSaveMenuItem;
    private javax.swing.JSeparator separator1inEditMenu;
    private javax.swing.JSeparator separator1inOptionsMenu;
    private javax.swing.JSeparator separator1inProjectMenu;
    private javax.swing.JSeparator separator2inEditMenu;
    private javax.swing.JSeparator separator2inProjectMenu;
    private javax.swing.JSeparator separator3inEditMenu;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenuItem toolsValidateTagsMenuItem;
    private javax.swing.JCheckBoxMenuItem viewFileListCheckBoxMenuItem;
    private javax.swing.JCheckBoxMenuItem viewMatchWindowCheckBoxMenuItem;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables
    
}
