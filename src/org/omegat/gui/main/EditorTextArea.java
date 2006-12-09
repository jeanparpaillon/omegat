/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
               Support center: http://groups.yahoo.com/group/OmegaT/

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

import java.awt.ComponentOrientation;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTextPane;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleContext;
import javax.swing.undo.UndoManager;

import org.omegat.util.OStrings;


/**
 * The main panel, where all the translation happens.
 *
 * @author Maxym Mykhalchuk
 */
public class EditorTextArea extends JTextPane implements MouseListener, DocumentListener
{
    private MainWindow mw;
    
    /** Creates new form BeanForm */
    public EditorTextArea(MainWindow mainwindow)
    {
        this.mw = mainwindow;
        DefaultStyledDocument doc = new DefaultStyledDocument(new StyleContext());
        doc.addDocumentListener(this);

        undoManager = new UndoManager();
        doc.addUndoableEditListener(undoManager);
        setDocument(doc);
        setText(OStrings.getString("TF_INTRO_MESSAGE"));

        addMouseListener(this);
    }

    ////////////////////////////////////////////////////////////////////////
    // Managing Undo operations
    ////////////////////////////////////////////////////////////////////////
    
    /** Undo Manager to store edits */
    private UndoManager	undoManager;
    
    /** Orders to cancel all Undoable edits. */
    public void cancelUndo()
    {
        undoManager.die();
    }
    /** Orders to undo a single edit. */
    public void undoOneEdit()
    {
        if (undoManager.canUndo())
            undoManager.undo();
    }
    /** Orders to redo a single edit. */
    public void redoOneEdit()
    {
        if (undoManager.canRedo())
            undoManager.redo();
    }
    
    ////////////////////////////////////////////////////////////////////////
    // Mouse reaction
    ////////////////////////////////////////////////////////////////////////
    
    /**
     * Reacts to double mouse clicks.
     */
    public void mouseClicked(MouseEvent e)
    {
        // design-time
        if (mw==null)
            return;
        
        // ignore mouse clicks until document is ready
        if (!mw.m_docReady)
            return;
        
        if (e.getClickCount() == 2)
        {
            // user double clicked on view pane - goto entry
            // that was clicked
            int pos = getCaretPosition();
            DocumentSegment docSeg;
            int i;
            if (pos < mw.m_segmentStartOffset)
            {
                // before current entry
                int offset = 0;
                for (i=mw.m_xlFirstEntry; i<mw.m_curEntryNum; i++)
                {
                    docSeg = mw.m_docSegList[i-mw.m_xlFirstEntry];
                    offset += docSeg.length;
                    if (pos < offset)
                    {
                        mw.doGotoEntry(i+1);
                        return;
                    }
                }
            }
            else if (pos > getTextLength() - mw.m_segmentEndInset)
            {
                // after current entry
                int inset = getTextLength() - mw.m_segmentEndInset;
                for (i=mw.m_curEntryNum+1; i<=mw.m_xlLastEntry; i++)
                {
                    docSeg = mw.m_docSegList[i-mw.m_xlFirstEntry];
                    inset += docSeg.length;
                    if (pos <= inset)
                    {
                        mw.doGotoEntry(i+1);
                        return;
                    }
                }
            }
        }
    }    
    // not used now
    public void mouseReleased(MouseEvent e) { }
    public void mousePressed(MouseEvent e)  { }
    public void mouseExited(MouseEvent e)   { }
    public void mouseEntered(MouseEvent e)  { }
    
    
    /** Ctrl key mask. On MacOSX it's CMD key. */
    private static final int CTRL_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    

    ////////////////////////////////////////////////////////////////////////
    // Keyborad handling to protect text areas
    ////////////////////////////////////////////////////////////////////////
    
    /**
     * Monitors key events - need to prevent text insertion
     * outside of edit zone while maintaining normal functionality
     * across jvm versions.
     */
    protected void processKeyEvent(KeyEvent e)
    {
        // design-time
        if (mw==null)
            return;
        
        if (!mw.m_projectLoaded)
        {
            if( (e.getModifiers()&CTRL_KEY_MASK)==CTRL_KEY_MASK ||
                    (e.getModifiers()&InputEvent.ALT_MASK)==InputEvent.ALT_MASK )
                super.processKeyEvent(e);
            return;
        }
        
        int keyCode = e.getKeyCode();
        char keyChar = e.getKeyChar();
        
        // let released keys go straight to parent - they should
        //	have no effect on UI and so don't need processing
        if (e.getID() == KeyEvent.KEY_RELEASED)
        {
            // let key releases pass through as well
            // no events should be happening here
            super.processKeyEvent(e);
            return;
        }
        
        // let control keypresses pass through unscathed
        if (e.getID() == KeyEvent.KEY_PRESSED	&&
                (keyCode == KeyEvent.VK_CONTROL	||
                keyCode == KeyEvent.VK_ALT		||
                keyCode == KeyEvent.VK_META		||
                keyCode == KeyEvent.VK_SHIFT))
        {
            super.processKeyEvent(e);
            return;
        }
        
        // for now, force all key presses to reset the cursor to
        //	the editing region unless it's a ctrl-c (copy)
        if( e.getID()==KeyEvent.KEY_PRESSED && e.getModifiers()==CTRL_KEY_MASK && keyCode == KeyEvent.VK_C
            || e.getID() == KeyEvent.KEY_TYPED && e.getModifiers() == CTRL_KEY_MASK && keyChar=='\u0003' )
        {
            // control-c pressed or typed
            super.processKeyEvent(e);
            return;
        }

        // if we've made it here, we have a keypressed or
        //	key-typed event of a (presumably) valid key
        //	and we're in an open project
        // it could still be a keyboard shortcut though
        
        // look for delete/backspace events and make sure they're
        //	in an acceptable area
        switch (keyChar)
        {
            case KeyEvent.VK_BACK_SPACE:
                if (mw.checkCaretForDelete(false))
                {
                    super.processKeyEvent(e);
                }
                return;
            case KeyEvent.VK_DELETE:
                if (mw.checkCaretForDelete(true))
                {
                    super.processKeyEvent(e);
                }
                return;
        }
        
        // handling Ctrl+Shift+Home / End manually
        // in order to select only to beginning / to end
        // of the current segment
        // Also hadling HOME and END manually
        // BUGFIX FOR: HOME and END key issues
        //             http://sourceforge.net/support/tracker.php?aid=1228296 
        if( keyCode==KeyEvent.VK_HOME || keyCode==KeyEvent.VK_END )
        {
            // letting parent do the handling
            super.processKeyEvent(e);
            
            // and then "refining" the selection
            mw.checkCaret();
            
            return;
        }
        
        // handling Ctrl+A manually
        // BUGFIX FOR: Select all in the editing field shifts focus
        //             http://sourceforge.net/support/tracker.php?aid=1211826
        // BUGFIX FOR: AltGr+A in Irish deletes all the segment text
        //             http://sourceforge.net/support/tracker.php?aid=1256094
        //             changed the condition to work on ONLY! the Ctrl key pressed
        if( e.getModifiers()==CTRL_KEY_MASK &&
                e.getKeyCode()==KeyEvent.VK_A )
        {
            int start = mw.m_segmentStartOffset + mw.m_sourceDisplayLength +
                    OStrings.getSegmentStartMarker().length();
            int end = getTextLength() - mw.m_segmentEndInset -
                    OStrings.getSegmentEndMarker().length();
            
            // selecting
            setSelectionStart(start);
            setSelectionEnd(end);
            
            return;
        }
        
        // every other key press should be within the editing zone
        //	so make sure the caret is there
        mw.checkCaret();
        
        // if user pressed enter, see what modifiers were pressed
        //	so determine what to do
        // 18may04 KBG - enable user to hit TAB to advance to next
        //	entry.  some asian IMEs don't swallow enter key resulting
        //	in non-usability
        if (keyCode == KeyEvent.VK_ENTER)
        {
            if (e.isShiftDown())
            {
                // convert key event to straight enter key
                KeyEvent ke = new KeyEvent(e.getComponent(), e.getID(),
                        e.getWhen(), 0, KeyEvent.VK_ENTER, '\n');
                super.processKeyEvent(ke);
            }
            else if (mw.m_advancer != keyCode)
            {
                return;	// swallow event - hopefully IME still works
            }
        }
        
        if (keyCode == mw.m_advancer)
        {
            if (mw.m_advancer == KeyEvent.VK_ENTER)
            {
                // Previous segment shortcut should be CMD+Enter on MacOSX
                // http://sourceforge.net/support/tracker.php?aid=1468315
                if( (e.getModifiers() & CTRL_KEY_MASK)==CTRL_KEY_MASK )
                {
                    // go backwards on control return
                    if (e.getID() == KeyEvent.KEY_PRESSED)
                        mw.doPrevEntry();
                }
                else if (!e.isShiftDown())
                {
                    // return w/o modifiers - swallow event and move on to
                    //  next segment
                    if (e.getID() == KeyEvent.KEY_PRESSED)
                        mw.doNextEntry();
                }
            }
            else if (mw.m_advancer == KeyEvent.VK_TAB)
            {
                // ctrl-tab not caught
                if (e.isShiftDown())
                {
                    // go backwards on control return
                    if (e.getID() == KeyEvent.KEY_PRESSED)
                        mw.doPrevEntry();
                }
                else
                {
                    // return w/o modifiers - swallow event and move on to
                    //  next segment
                    if (e.getID() == KeyEvent.KEY_PRESSED)
                        mw.doNextEntry();
                }
            }
            return;
        }
        
        // need to over-ride default text hiliting procedures because
        //	we're managing caret placement manually
        if (e.isShiftDown())
        {
            // if navigation control, make sure things are hilited
            if (keyCode == KeyEvent.VK_UP				||
                    keyCode == KeyEvent.VK_LEFT		||
                    keyCode == KeyEvent.VK_KP_UP		||
                    keyCode == KeyEvent.VK_KP_LEFT)
            {
                super.processKeyEvent(e);
                if (e.getID() == KeyEvent.KEY_PRESSED)
                {
                    int pos = getCaretPosition();
                    int start = mw.m_segmentStartOffset +
                            mw.m_sourceDisplayLength +
                            OStrings.getSegmentStartMarker().length();
                    if (pos < start)
                        moveCaretPosition(start);
                }
            }
            else if (keyCode == KeyEvent.VK_DOWN		||
                    keyCode == KeyEvent.VK_RIGHT		||
                    keyCode == KeyEvent.VK_KP_DOWN	||
                    keyCode == KeyEvent.VK_KP_RIGHT)
            {
                super.processKeyEvent(e);
                if (e.getID() == KeyEvent.KEY_PRESSED)
                {
                    int pos = getCaretPosition();
                    // -1 for space before tag, -2 for newlines
                    int end = getTextLength() - mw.m_segmentEndInset -
                            OStrings.getSegmentEndMarker().length();
                    if (pos > end)
                        moveCaretPosition(end);
                }
            }
        }
        
        // shift key is not down
        // if arrow key pressed, make sure caret moves to correct side
        //	of hilite (if text hilited)
        if( !e.isShiftDown() )
        {
            if( keyCode == KeyEvent.VK_UP      ||
                    keyCode == KeyEvent.VK_LEFT    ||
                    keyCode == KeyEvent.VK_KP_UP   ||
                    keyCode == KeyEvent.VK_KP_LEFT )
            {
                int end = getSelectionEnd();
                int start = getSelectionStart();
                if (end != start)
                    setCaretPosition(start);
                else
                    super.processKeyEvent(e);
                mw.checkCaret();
                return;
            }
            else if (keyCode == KeyEvent.VK_DOWN		||
                    keyCode == KeyEvent.VK_RIGHT		||
                    keyCode == KeyEvent.VK_KP_DOWN	||
                    keyCode == KeyEvent.VK_KP_RIGHT)
            {
                int end = getSelectionEnd();
                int start = getSelectionStart();
                if (end != start)
                    setCaretPosition(end);
                else
                    super.processKeyEvent(e);
                mw.checkCaret();
                return;
            }
        }
        
        // no more special handling required
        super.processKeyEvent(e);
    }


    ////////////////////////////////////////////////////////////////////////
    // getText().length() caching
    ////////////////////////////////////////////////////////////////////////

    /** Holds the length of the text in the underlying document. */
    int textLength = 0;
    
    /** 
     * Returns the length of the text in the underlying document.
     * <p>
     * This should replace all <code>getText().length()</code> calls,
     * because this method does not count the length (costly operation),
     * instead it accounts document length by listening to document updates.
     */
    public int getTextLength()
    {
        return textLength;
    }
    
    /** Accounting text length. */
    public void removeUpdate(javax.swing.event.DocumentEvent e)
    {
        textLength -= e.getLength();
    }

    /** Accounting text length. */
    public void insertUpdate(javax.swing.event.DocumentEvent e)
    {
        textLength += e.getLength();
    }

    /** Attribute changes do not result in document length changes. Doing nothing. */
    public void changedUpdate(javax.swing.event.DocumentEvent e) { }

}
