/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Zoltan Bartko
               2011 John Moran
               2012 Alex Buloichik, Jean-Christophe Helary, Didier Briel, Thomas Cordonnier, Aaron Madlon-Kay
               2013 Zoltan Bartko, Aaron Madlon-Kay
               2014 Aaron Madlon-Kay
               2015 Yu Tang
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.matches;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.StringData;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.matching.DiffDriver.TextRun;
import org.omegat.core.matching.NearString;
import org.omegat.gui.common.EntryInfoThreadPane;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.MainWindow;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;
import org.omegat.util.gui.AlwaysVisibleCaret;
import org.omegat.util.gui.DragTargetOverlay;
import org.omegat.util.gui.DragTargetOverlay.FileDropInfo;
import org.omegat.util.gui.Styles;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * This is a Match pane, that displays fuzzy matches.
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Zoltan Bartko
 * @author John Moran
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Jean-Christophe Helary
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 * @author Yu Tang
 */
@SuppressWarnings("serial")
public class MatchesTextArea extends EntryInfoThreadPane<List<NearString>> implements IMatcher {

    private static final String EXPLANATION = OStrings.getString("GUI_MATCHWINDOW_explanation");

    private static final AttributeSet ATTRIBUTES_EMPTY = Styles.createAttributeSet(null, null, null, null);
    private static final AttributeSet ATTRIBUTES_CHANGED = Styles.createAttributeSet(Styles.EditorColor.COLOR_MATCHES_CHANGED.getColor(), null, null,
            null);
    private static final AttributeSet ATTRIBUTES_UNCHANGED = Styles.createAttributeSet(Styles.EditorColor.COLOR_MATCHES_UNCHANGED.getColor(), null, null,
            null);
    private static final AttributeSet ATTRIBUTES_SELECTED = Styles.createAttributeSet(null, null, true, null);
    private static final AttributeSet ATTRIBUTES_DELETED_ACTIVE = Styles.createAttributeSet(Styles.EditorColor.COLOR_MATCHES_DEL_ACTIVE.getColor(), null, true, null, true, null);
    private static final AttributeSet ATTRIBUTES_DELETED_INACTIVE = Styles.createAttributeSet(Styles.EditorColor.COLOR_MATCHES_DEL_INACTIVE.getColor(), null, null, null, true, null);
    private static final AttributeSet ATTRIBUTES_INSERTED_ACTIVE = Styles.createAttributeSet(Styles.EditorColor.COLOR_MATCHES_INS_ACTIVE.getColor(), null, true, null, null, true);
    private static final AttributeSet ATTRIBUTES_INSERTED_INACTIVE = Styles.createAttributeSet(Styles.EditorColor.COLOR_MATCHES_INS_INACTIVE.getColor(), null, null, null, null, true);
    
    private final List<NearString> matches = new ArrayList<NearString>();

    private final List<Integer> delimiters = new ArrayList<Integer>();
    private final List<Integer> sourcePos = new ArrayList<Integer>();
    private final List<Map<Integer, List<TextRun>>> diffInfos = new ArrayList<Map<Integer, List<TextRun>>>();
    private int activeMatch;

    private final MainWindow mw;

    /** Creates new form MatchGlossaryPane */
    public MatchesTextArea(final MainWindow mw) {
        super(true);
        this.mw = mw;

        String title = OStrings.getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_Fuzzy_Matches");
        final DockableScrollPane scrollPane = new DockableScrollPane("MATCHES", title, this, true);
        Core.getMainWindow().addDockable(scrollPane);

        setEditable(false);
        AlwaysVisibleCaret.apply(this);
        this.setText(EXPLANATION);
        setMinimumSize(new Dimension(100, 50));

        addMouseListener(mouseListener);
        
        DragTargetOverlay.apply(this, new FileDropInfo(mw, false) {
            @Override
            public String getImportDestination() {
                return Core.getProject().getProjectProperties().getTMRoot();
            }
            @Override
            public boolean acceptFile(File pathname) {
                return pathname.getName().toLowerCase().endsWith(OConsts.TMX_EXTENSION);
            }
            @Override
            public String getOverlayMessage() {
                return OStrings.getString("DND_ADD_TM_FILE");
            }
            @Override
            public boolean canAcceptDrop() {
                return Core.getProject().isProjectLoaded();
            }
            @Override
            public Component getComponentToOverlay() {
                return scrollPane;
            }
        });
    }

    @Override
    protected void startSearchThread(SourceTextEntry newEntry) {
        new FindMatchesThread(MatchesTextArea.this, Core.getProject(), newEntry).start();
    }

    /**
     * Sets the list of fuzzy matches to show in the pane. Each element of the
     * list should be an instance of {@link NearString}.
     */
    @Override
    protected void setFoundResult(final SourceTextEntry se, List<NearString> newMatches) {
        UIThreadsUtil.mustBeSwingThread();
        
        if (newMatches == null) {
            setText("");
            return;
        }
        
        Collections.sort(newMatches, Collections.reverseOrder(new NearString.NearStringComparator()));

        activeMatch = -1;
        matches.clear();
        delimiters.clear();
        sourcePos.clear();
        diffInfos.clear();

        matches.addAll(newMatches);
        delimiters.add(0);
        StringBuilder displayBuffer = new StringBuilder();

        MatchesVarExpansion template = new MatchesVarExpansion(Preferences.getPreferenceDefault(Preferences.EXT_TMX_MATCH_TEMPLATE, MatchesVarExpansion.DEFAULT_TEMPLATE));
        
        for (int i = 0; i < newMatches.size(); i++) {
            NearString match = newMatches.get(i);
            MatchesVarExpansion.Result result = template.apply(match, i + 1);
            displayBuffer.append(result.text);
            sourcePos.add(result.sourcePos);
            diffInfos.add(result.diffInfo);

            if (i < (newMatches.size() - 1))
                displayBuffer.append("\n\n");
            delimiters.add(displayBuffer.length());
        }

        setText(displayBuffer.toString());
        setActiveMatch(0);

        checkForReplaceTranslation();
    }

    @Override
    protected void onProjectOpen() {
        clear();
    }

    @Override
    protected void onProjectClose() {
        clear();
        this.setText(EXPLANATION);
        // We clean the ATTRIBUTE_SELECTED style set by the last displayed match
        StyledDocument doc = (StyledDocument) getDocument();
        doc.setCharacterAttributes(0, doc.getLength(), ATTRIBUTES_EMPTY, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NearString getActiveMatch() {
        UIThreadsUtil.mustBeSwingThread();

        if (activeMatch < 0 || activeMatch >= matches.size()) {
            return null;
        } else {
            return matches.get(activeMatch);
        }
    }

    /**
     * Attemps to subtitute numbers in a match with numbers from the source segment.
     * For substitution to be done, the number of numbers must be the same between source and matches, and
     * the numbers must be the same between the source match and the target match. The order of the numbers
     * can be different between the source match and the target match. Numbers will be substituted at the 
     * correct location.
     * @param source The source segment
     * @param sourceMatch The source of the match
     * @param targetMatch The target of the match
     * @return The target match with numbers possibly substituted
     */
    @Override
    public String substituteNumbers(String source, String sourceMatch, String targetMatch) {

        ITokenizer sourceTok = Core.getProject().getSourceTokenizer();
        ITokenizer targetTok = Core.getProject().getTargetTokenizer();

        Token[] sourceMatchStrTokensAll = sourceTok.tokenizeVerbatim(sourceMatch);
        List<String> sourceMatchNumbers = getNumberList(sourceMatchStrTokensAll, sourceMatch);

        Token[] targetMatchStrTokensAll = targetTok.tokenizeVerbatim(targetMatch);
        List<String> targetMatchNumbers = getNumberList(targetMatchStrTokensAll, targetMatch);

        Token[] sourceStrTokensAll = sourceTok.tokenizeVerbatim(source);
        List <String> sourceNumbers = getNumberList(sourceStrTokensAll, source);

        if (sourceMatchNumbers.size() != targetMatchNumbers.size() || //Not the same number of numbers
            sourceMatchNumbers.size() != sourceNumbers.size()) {
            return targetMatch; 
        }

        List<Integer> matchingNumbers = new ArrayList<Integer>();
        List<Integer> foundLocation = new ArrayList<Integer>();

        // Compute the location of numbers in the target match
        for (String oneNumber : sourceMatchNumbers) {
            int pos = -1;
            for (Token oneToken : targetMatchStrTokensAll) {
                pos ++;
                if (oneNumber.equals(oneToken.getTextFromString(targetMatch)) && !foundLocation.contains(pos)) {
                   matchingNumbers.add(pos);
                   foundLocation.add(pos);
                }
            }
            if (pos == -1) { // One of the number in source is not in target
                return targetMatch; 
            } 
        }

        // Substitute new numbers in the target match
        String finalString = "";
        int pos = -1;
        boolean replaced;
        for (Token oneToken : targetMatchStrTokensAll) {
            pos ++;
            replaced = false;
            for (int numberRank = 0; numberRank < matchingNumbers.size(); numberRank++){
                if (matchingNumbers.get(numberRank) == pos) {
                    finalString += sourceNumbers.get(numberRank);
                    replaced = true;
                }
            }
            if (!replaced) {// No subtitution was done
                finalString += oneToken.getTextFromString(targetMatch);
            }
        }

        return finalString;
    }

    /**
     * Compute a list of numerals inside a string. Integers and simple doubles (not localized) are recognized.
     * @param strTokenAll A list of tokens from a string
     * @param text A string
     * @return A list of strings of tokens which can be considered being numerals
     */
    private List<String> getNumberList(Token[] strTokenAll, String text) {
        List<String> numberList = new ArrayList<String>();
        for (Token oneToken : strTokenAll) {
            try {
                Integer.parseInt(oneToken.getTextFromString(text));
                numberList.add(oneToken.getTextFromString(text));
            } catch (NumberFormatException nfe) {
                try {
                    Double.parseDouble(oneToken.getTextFromString(text));
                    numberList.add(oneToken.getTextFromString(text));
                } catch (NumberFormatException nfe2) {
                } // Eat exception silently
            } // Eat exception silently
        }
        return numberList;
    }

    /**
     * if WORKFLOW_OPTION "Insert best fuzzy match into target field" is set
     * 
     * RFE "Option: Insert best match (80%+) into target field"
     * 
     * http://sourceforge.net/support/tracker.php?aid=1075976
     */
    private void checkForReplaceTranslation() {
        if (matches.isEmpty()) {
            return;
        }
        if (Preferences.isPreference(Preferences.BEST_MATCH_INSERT)) {
            String percentage_s = Preferences.getPreferenceDefault(Preferences.BEST_MATCH_MINIMAL_SIMILARITY,
                    Preferences.BEST_MATCH_MINIMAL_SIMILARITY_DEFAULT);
            // <HP-experiment>
            int percentage;
            try {
                // int
                percentage = Integer.parseInt(percentage_s);
            } catch (Exception exception) {
                Log.log("ERROR: exception while parsing percentage:");
                Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                Log.log(exception);
                return; // deliberately breaking, to simulate previous behaviour
                // FIX: unknown, but expect number parsing errors
            }
            // </HP-experiment>
            NearString thebest = matches.get(0);
            if (thebest.scores[0].score >= percentage) {
                SourceTextEntry currentEntry = Core.getEditor().getCurrentEntry();
                TMXEntry te = Core.getProject().getTranslationInfo(currentEntry);
                if (!te.isTranslated()) {
                    String prefix = "";

                    if (!Preferences.getPreferenceDefaultAllowEmptyString(Preferences.BEST_MATCH_EXPLANATORY_TEXT)
                            .equals("")) {
                        prefix = Preferences.getPreferenceDefault(Preferences.BEST_MATCH_EXPLANATORY_TEXT,
                                OStrings.getString("WF_DEFAULT_PREFIX"));
                    }

                    String translation = thebest.translation;
                    if (Preferences.isPreference(Preferences.CONVERT_NUMBERS)) {
                        translation = 
                            substituteNumbers(currentEntry.getSrcText(), thebest.source, thebest.translation);
                    }
                    Core.getEditor().replaceEditText(prefix + translation);
                }
            }
        }
    }

    /**
     * Sets the index of an active match. It basically highlights the fuzzy
     * match string selected. (numbers start from 0)
     */
    @Override
    public void setActiveMatch(int activeMatch) {
        UIThreadsUtil.mustBeSwingThread();

        if (activeMatch < 0 || activeMatch >= matches.size() || this.activeMatch == activeMatch) {
            return;
        }

        this.activeMatch = activeMatch;

        StyledDocument doc = (StyledDocument) getDocument();
        doc.setCharacterAttributes(0, doc.getLength(), ATTRIBUTES_EMPTY, true);

        int start = delimiters.get(activeMatch);
        int end = delimiters.get(activeMatch + 1);

        NearString match = matches.get(activeMatch);
        // List tokens = match.str.getSrcTokenList();
        ITokenizer tokenizer = Core.getProject().getSourceTokenizer();
        if (tokenizer == null) {
            return;
        }
        
        // Apply sourceText styling
        if (sourcePos.get(activeMatch) != -1) {
            Token[] tokens = tokenizer.tokenizeVerbatim(match.source);
            // fix for bug 1586397
            byte[] attributes = match.attr;
            for (int i = 0; i < tokens.length; i++) {
                Token token = tokens[i];
                int tokstart = start + sourcePos.get(activeMatch) + token.getOffset();
                int toklength = token.getLength();
                if ((attributes[i] & StringData.UNIQ) != 0) {
                    doc.setCharacterAttributes(tokstart, toklength, ATTRIBUTES_CHANGED, false);
                } else if ((attributes[i] & StringData.PAIR) != 0) {
                    doc.setCharacterAttributes(tokstart, toklength, ATTRIBUTES_UNCHANGED, false);
                }
            }
        }
        
        // Apply diff styling to ALL diffs, with colors only for activeMatch
        // Iterate through (up to) 5 fuzzy matches
        for (int i = 0; i < diffInfos.size(); i++) {
            Map<Integer, List<TextRun>> diffInfo = diffInfos.get(i);
            // Iterate through each diff variant (${diff}, ${diffReversed}, ...)
            for (Entry<Integer, List<TextRun>> e : diffInfo.entrySet()) {
                int diffPos = e.getKey();
                if (diffPos != -1) {
                    // Iterate through each style chunk (added or deleted)
                    for (TextRun r : e.getValue()) {
                        int tokstart = delimiters.get(i) + diffPos + r.start;
                        switch (r.type) {
                        case DELETE:
                            doc.setCharacterAttributes(
                                tokstart,
                                r.length,
                                i == activeMatch ? ATTRIBUTES_DELETED_ACTIVE : ATTRIBUTES_DELETED_INACTIVE,
                                false);
                            break;
                        case INSERT:
                            doc.setCharacterAttributes(
                                tokstart,
                                r.length,
                                i == activeMatch ? ATTRIBUTES_INSERTED_ACTIVE : ATTRIBUTES_INSERTED_INACTIVE,
                                false);
                        case NOCHANGE:
                            // Nothing
                        }
                    }
                }
            }
        }

        doc.setCharacterAttributes(start, end - start, ATTRIBUTES_SELECTED, false);
        setCaretPosition(end - 2); // two newlines
        final int fstart = start;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setCaretPosition(fstart);
            }
        });
    }

    /** Clears up the pane. */
    public void clear() {
        UIThreadsUtil.mustBeSwingThread();

        setFoundResult(null, new ArrayList<NearString>());
    }

    protected MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            // is there anything?
            if (matches == null || matches.isEmpty())
                return;

            // find out the clicked item
            int clickedItem = -1;

            // where did we click?
            int mousepos = MatchesTextArea.this.viewToModel(e.getPoint());

            int i;
            for (i = 0; i < delimiters.size() - 1; i++) {
                int start = delimiters.get(i);
                int end = delimiters.get(i + 1);

                if (mousepos >= start && mousepos < end) {
                    clickedItem = i;
                    break;
                }
            }

            if (clickedItem == -1)
                clickedItem = delimiters.size() - 1;

            if (clickedItem >= matches.size())
                return;

            // set up the menu
            if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                mouseRightClick(clickedItem, e.getPoint());
            }
            
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1)
                setActiveMatch(clickedItem);
        }
    };

    private void mouseOneClick(final int clickedItem, final Point clickedPoint) {
        // show colored source segment
    }

    private void mouseRightClick(final int clickedItem, final Point clickedPoint) {
        // create the menu
        JPopupMenu popup = new JPopupMenu();

        NearString m = matches.get(clickedItem);
        if (m.projs.length > 1) {
            JMenuItem item = popup.add(OStrings.getString("MATCHES_PROJECTS"));
            item.setEnabled(false);
            for (int i = 0; i < m.projs.length; i++) {
                String proj = m.projs[i];
                StringBuilder b = new StringBuilder();
                if (proj.equals("")) {
                    b.append(OStrings.getString("MATCHES_THIS_PROJECT"));
                } else {
                    b.append(proj);
                }
                b.append(" ");
                b.append(m.scores[i].toString());
                JMenuItem pItem = popup.add(b.toString());
                pItem.setEnabled(false);
            }
            popup.addSeparator();
        }
        
        JMenuItem item = popup.add(OStrings.getString("MATCHES_INSERT"));
        item.addActionListener(new ActionListener() {
            // the action: insert this match
            @Override
            public void actionPerformed(ActionEvent e) {
                if (StringUtil.isEmpty(getSelectedText())) {
                    setActiveMatch(clickedItem);
                }
                mw.doInsertTrans();
            }
        });

        item = popup.add(OStrings.getString("MATCHES_REPLACE"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (StringUtil.isEmpty(getSelectedText())) {
                    setActiveMatch(clickedItem);
                }
                mw.doRecycleTrans();
            }
        });

        popup.addSeparator();

        final NearString ns = matches.get(clickedItem);
        String proj = ns.projs[0];

        item = popup.add(OStrings.getString("MATCHES_GO_TO_SEGMENT_SOURCE"));

        if (StringUtil.isEmpty(proj)) {
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Core.getEditor().gotoEntry(ns.source, ns.key);
                }
            });
        } else {
            item.setEnabled(false);
        }

        popup.show(this, clickedPoint.x, clickedPoint.y);
    }
    
    /**
     * Make the next match active
     */
    @Override
    public void setNextActiveMatch() {
        if (activeMatch < matches.size()-1) {
            setActiveMatch(activeMatch+1);
}
    }

    /**
     * Make the previous match active
     */
    @Override
    public void setPrevActiveMatch() {
        if (activeMatch > 0) {
            setActiveMatch(activeMatch-1);
        }
    }
}
