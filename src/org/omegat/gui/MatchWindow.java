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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.omegat.core.StringData;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.Token;

/**
 * This is a combined Match + Glossary window, that displays
 * fuzzy matches and glossary entries.
 *
 * @author Keith Godfrey
 * @author Raymond Martin
 */
public class MatchWindow extends JFrame
{ 
    public MatchWindow()
    {
        setTitle(OStrings.TF_MATCH_VIEWER_TITLE);
        // KBG - set screen size based on saved values or guesses about
        //    screen size (18may04)
        initScreenLayout();
        
        //  Top level container
        Container cntMatch = getContentPane();
        
        ///////////////////////////////////////////////////////////////////
        //  Panes
        //
        
        //  Match Pane
        m_matchPane = new JTextPane();
        //  Embed text pane within scrollable pane - RM
        JScrollPane scpnMatch = new JScrollPane( m_matchPane );
        
        //  Glossary Pane
        m_glosPane = new JTextPane();
        //  Embed text pane within scrollable pane - RM
        JScrollPane scpnGlossary = new JScrollPane( m_glosPane );
        
        ///////////////////////////////////////////////////////////////////
        //  Split Pane with Two Scroll Panes - RM
        JSplitPane sppnMatchGlos =
                new JSplitPane( JSplitPane.VERTICAL_SPLIT, scpnMatch, scpnGlossary );
        
        sppnMatchGlos.setResizeWeight( 0.5 );
        sppnMatchGlos.setContinuousLayout( true );
        sppnMatchGlos.setOneTouchExpandable( true );
        
        //  Provide minimum sizes for the two components in the split pane
        Dimension minimumSize = new Dimension( 100, 50 );
        m_matchPane.setMinimumSize( minimumSize );
        m_glosPane.setMinimumSize( minimumSize );
        
        cntMatch.add( sppnMatchGlos );
        
        m_matchPane.setEditable(false);
        m_glosPane.setEditable(false);
        
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                setVisible(false);
            }
        });
    }
    
    /**
     * Sets the text of the glossary window to the found glossary entries
     */
	public void updateGlossaryText()
	{
		m_glosPane.setText(m_glosDisplay);
		m_glosDisplay = "";														// NOI18N
	}

    /**
     * Format the currently selected Near String (fuzzy match)
     * according to the tokens and their attributes.
     *
     * @param tokenList - the list of tokens to highlight
     * @param attrList - the attributes to color tokens accordingly
     */
	public void formatNearText(List tokenList, byte[] attrList)
	{
		int start;
		int end;
		JTextPane pane = m_matchPane;

		// reset color of text to default value
		int numTokens = tokenList.size();
		for (int i=0; i<numTokens; i++)
		{
			start = m_hiliteStart + ((Token) tokenList.get(i)).offset + 4;
			end = m_hiliteStart + ((Token) tokenList.get(i)).offset + 4 + 
										((Token) tokenList.get(i)).text.length();

			pane.select(start, end);
			SimpleAttributeSet mattr = new SimpleAttributeSet();
			if ((attrList[i] & StringData.UNIQ) != 0)
				StyleConstants.setForeground(mattr, Color.blue);
			else if ((attrList[i] & StringData.PAIR) != 0)
				StyleConstants.setForeground(mattr, Color.green);
			pane.setCharacterAttributes(mattr, false);
		}
		pane.select(0, 0);
		SimpleAttributeSet mattr = new SimpleAttributeSet();
		pane.setCharacterAttributes(mattr, false);
	}

    /**
     * Sets the text of match window to the list of found matches (near strings),
     * and select the currently selected match in bold.
     */
	public void updateMatchText()
	{
		// Cancelling all the attributes
		m_matchPane.setCharacterAttributes(new SimpleAttributeSet(), true);
		
		m_matchPane.setText(m_matchDisplay);

		if ( m_hiliteStart >= 0 && m_matchDisplay.length()>0 )
		{
			m_matchPane.select(m_hiliteStart, m_hiliteEnd);
			MutableAttributeSet mattr;
			mattr = new SimpleAttributeSet();
			StyleConstants.setBold(mattr, true);
			m_matchPane.setCharacterAttributes(mattr, false);

			m_matchPane.setCaretPosition(m_hiliteStart);
		}

		m_matchDisplay = "";													// NOI18N
		m_matchCount = 0;
	}

	public void reset()
	{
		m_matchDisplay = "";													// NOI18N
		m_glosDisplay = "";														// NOI18N
		m_matchCount = 0;
		m_matchPane.setText("");												// NOI18N
		m_glosPane.setText("");													// NOI18N
	}

	public void setFont(Font f)
	{
		m_matchPane.setFont(f);
		m_glosPane.setFont(f);
	}

	public void addGlosTerm(String src, String loc, String comments)
	{
		String glos = "'" + src + "'  =  '" + loc + "'\n";						// NOI18N
		if (comments.length() > 0)
			glos += comments + "\n\n";											// NOI18N
		else
			glos += "\n";														// NOI18N
		m_glosDisplay += glos;
	}

	// returns offset in display of the start of this term
	public int addMatchTerm(String src, String loc, int score, String proj)
	{
		String entry = ++m_matchCount + ")  " + src + "\n" + loc + "\n< " +		// NOI18N
					score + "% " + proj + " >\n\n";								// NOI18N
		int size = m_matchDisplay.length();
		m_matchDisplay += entry;
		return size;
	}

	public void hiliteRange(int start, int end)
	{
		int len = m_matchDisplay.length();
		if (start < 0 || start > len)
		{
			m_hiliteStart = -1;
			m_hiliteEnd = -1;
			return;
		}

		if (end < 0)
			end = len;

		m_hiliteStart = start;
		m_hiliteEnd = end;
	}

	public void storeScreenLayout()
	{
		int w = getWidth();
		int h = getHeight();
		int x = getX();
		int y = getY();
		Preferences.setPreference(Preferences.MATCHWINDOW_WIDTH, w);
		Preferences.setPreference(Preferences.MATCHWINDOW_HEIGHT, h);
		Preferences.setPreference(Preferences.MATCHWINDOW_X, x);
		Preferences.setPreference(Preferences.MATCHWINDOW_Y, y);
	}
	
	private void initScreenLayout()
	{
		// KBG - assume screen size is 800x600 if width less than 900, and
		//		1024x768 if larger.  assume task bar at bottom of screen.
		//		if screen size saved, recover that and use instead
		//	(18may04)
		String dw, dh, dx, dy;
		dw = Preferences.getPreference(Preferences.MATCHWINDOW_WIDTH);
		dh = Preferences.getPreference(Preferences.MATCHWINDOW_HEIGHT);
		dx = Preferences.getPreference(Preferences.MATCHWINDOW_X);
		dy = Preferences.getPreference(Preferences.MATCHWINDOW_Y);
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
				setSize(200, 536);
				setLocation(590, 0);
			}
			else
			{
				// assume 1024x768 or larger
				setSize(320, 700);
				setLocation(680, 0);
			}
		}
		else
		{
			setSize(w, h);
			setLocation(x, y);
		}
	}

	private String		m_matchDisplay = "";								// NOI18N
	private String		m_glosDisplay = "";									// NOI18N
	private JTextPane		m_matchPane;
	private JTextPane		m_glosPane;
	private int			m_matchCount;
	private int			m_hiliteStart = -1;
	private int			m_hiliteEnd = -1;

}
