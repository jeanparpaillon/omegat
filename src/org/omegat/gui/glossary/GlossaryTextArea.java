/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel
               2009-2010 Wildrich Fourie
               2010 Alex Buloichik
               2012 Jean-Christophe Helary
               Home page: http://www.omegat.org/
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.glossary;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.StringEntry;
import org.omegat.gui.common.EntryInfoThreadPane;
import org.omegat.gui.dialogs.CreateGlossaryEntry;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.AlwaysVisibleCaret;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * This is a Glossary pane that displays glossary entries.
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Wildrich Fourie
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Jean-Christophe Helary
 */
@SuppressWarnings("serial")
public class GlossaryTextArea extends EntryInfoThreadPane<List<GlossaryEntry>> {

    private static final String EXPLANATION = OStrings.getString("GUI_GLOSSARYWINDOW_explanation");

    /** Glossary manager instance. */
    protected final GlossaryManager manager = new GlossaryManager(this);

    /**
     * Currently processed entry. Used to detect if user moved into new entry. In this case, new find should
     * be started.
     */
    protected StringEntry processedEntry;

    /**
     * Holds the current GlossaryEntries for the TransTips
     */
    protected static List<GlossaryEntry> nowEntries;

    /**
     * popupmenu
     */
    protected JPopupMenu popup;

    private CreateGlossaryEntry createGlossaryEntryDialog;

    /** Creates new form MatchGlossaryPane */
    public GlossaryTextArea() {
        super(true);

        String title = OStrings.getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_Glossary");
        Core.getMainWindow().addDockable(new DockableScrollPane("GLOSSARY", title, this, true));

        setEditable(false);
        AlwaysVisibleCaret.apply(this);
        this.setText(EXPLANATION);
        setMinimumSize(new Dimension(100, 50));

        //prepare popup menu
        popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem(OStrings.getString("GUI_GLOSSARYWINDOW_addentry"));
        menuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                Core.getGlossary().showCreateGlossaryEntryDialog();
            }
        });
        popup.add(menuItem);

        addMouseListener(mouseListener);

        Core.getEditor().registerPopupMenuConstructors(300, new TransTipsPopup());
    }

    @Override
    protected void onProjectOpen() {
        clear();
        manager.start();
    }

    @Override
    protected void onProjectClose() {
        clear();
        this.setText(EXPLANATION);
        manager.stop();
    }

    @Override
    protected void startSearchThread(SourceTextEntry newEntry) {
        new FindGlossaryThread(GlossaryTextArea.this, newEntry, manager).start();
    }

    /**
     * Refresh content on glossary file changed.
     */
    public void refresh() {
        SourceTextEntry ste = Core.getEditor().getCurrentEntry();
        if (ste != null) {
            startSearchThread(ste);
        }
    }

    /**
     * Sets the list of glossary entries to show in the pane. Each element of the list should be an instance
     * of {@link GlossaryEntry}.
     */
    protected void setFoundResult(SourceTextEntry en, List<GlossaryEntry> entries) {
        UIThreadsUtil.mustBeSwingThread();

        if (entries == null) {
            nowEntries = new ArrayList<GlossaryEntry>();
            setText("");
            return;
        }

        // If the TransTips is enabled then underline all the matched glossary
        // entries
        if (Preferences.isPreference(Preferences.TRANSTIPS)) {
            // TODO move marks construction into search thread
            highlightTransTips(en, entries);
        }

        nowEntries = entries;

        StringBuffer buf = new StringBuffer();
        for (GlossaryEntry entry : entries) {
            buf.append(entry.getSrcText() + " = " + entry.getLocText());
            if (entry.getCommentText().length() > 0)
                buf.append("\n" + entry.getCommentText());
            buf.append("\n\n");
        }
        setText(buf.toString());
    }

    /** Clears up the pane. */
    public void clear() {
        setText("");
    }

    /** {@inheritDoc} */
    public void highlightTransTips(SourceTextEntry en, List<GlossaryEntry> entries) {
        if (!entries.isEmpty()) {
            final List<Mark> marks = new ArrayList<Mark>();
            // Get the index of the current segment in the whole document
            String sourceText = en.getSrcText();
            sourceText = sourceText.toLowerCase();

            TransTips.Search callback = new TransTips.Search() {
                public void found(GlossaryEntry ge, int start, int end) {
                    marks.add(new Mark(Mark.ENTRY_PART.SOURCE, start, end));
                }
            };

            for (GlossaryEntry ent : entries) {
                TransTips.search(en.getSrcText(), ent, callback);
            }
            Core.getEditor().markActiveEntrySource(en, marks, TransTipsMarker.class.getName());
        }
    }

    /**
     * MouseListener for the GlossaryTextArea.
     */
    protected MouseListener mouseListener = new PopupListener(this);

    /**
     * MoueAdapter that knows the GlossaryTextArea. If there is text selected in the Glossary it will be inserted in
     * the Editor upon a right-click. Else a popup is shown to allow to add an entry.
     */
    class PopupListener extends MouseAdapter {

        private GlossaryTextArea glossaryTextArea;

        public PopupListener(GlossaryTextArea gte) {
            super();
            glossaryTextArea = gte;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                String selTxt = glossaryTextArea.getSelectedText();
                if (selTxt == null) {
                    if (Core.getProject().isProjectLoaded()) {
                        popup.show(glossaryTextArea, e.getX(), e.getY());
                    }
                } else {
                    insertTerm(selTxt);
                }
            }
        }
    }

    /**
     * Inserts the given text into the EditorTextArea
     *
     * @param selTxt the text to insert
     */
    private void insertTerm(String selTxt) {
        Core.getEditor().insertText(selTxt);
    }

    public void showCreateGlossaryEntryDialog() {
        CreateGlossaryEntry d = createGlossaryEntryDialog;
        if (d != null) {
            d.requestFocus();
            return;
        }

        ProjectProperties props = Core.getProject().getProjectProperties();
        final File out = new File(props.getWriteableGlossary());

        final CreateGlossaryEntry dialog = new CreateGlossaryEntry(Core.getMainWindow().getApplicationFrame());
        String txt = dialog.getGlossaryFileText().getText();
        txt = MessageFormat.format(txt, out.getAbsolutePath());
        dialog.getGlossaryFileText().setText(txt);
        dialog.setVisible(true);

        dialog.addWindowFocusListener(new WindowFocusListener() {
            public void windowLostFocus(WindowEvent e) {
            }

            public void windowGainedFocus(WindowEvent e) {
                String sel = Core.getEditor().getSelectedText();
                if (!StringUtil.isEmpty(sel)) {
                    if (StringUtil.isEmpty(dialog.getSourceText().getText())) {
                        dialog.getSourceText().setText(sel);
                    } else if (StringUtil.isEmpty(dialog.getTargetText().getText())) {
                        dialog.getTargetText().setText(sel);
                    } else if (StringUtil.isEmpty(dialog.getCommentText().getText())) {
                        dialog.getCommentText().setText(sel);
                    }
                }
            }
        });

        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                createGlossaryEntryDialog = null;
                if (dialog.getReturnStatus() == CreateGlossaryEntry.RET_OK) {
                    String src = dialog.getSourceText().getText();
                    String loc = dialog.getTargetText().getText();
                    String com = dialog.getCommentText().getText();
                    if (!StringUtil.isEmpty(src) && !StringUtil.isEmpty(loc)) {
                        try {
                            GlossaryReaderTSV.append(out, new GlossaryEntry(src, loc, com));
                        } catch (Exception ex) {
                            Log.log(ex);
                        }
                    }
                }
            }
        });
        createGlossaryEntryDialog = dialog;
    }
}
