/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
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

package org.omegat.gui.editor.filter;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.search.SearchMatch;
import org.omegat.core.search.Searcher;
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.editor.IEditorFilter;

/**
 * Editor filter implementation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ReplaceFilter implements IEditorFilter {
    private final Map<Integer, SourceTextEntry> entries = new HashMap<Integer, SourceTextEntry>();
    private FilterBarReplace controlComponent;
    private Searcher searcher;
    private String replacement;
    private int minEntryNum, maxEntryNum;

    public ReplaceFilter(List<Integer> entriesList, Searcher searcher, String replacement) {
        this.searcher = searcher;
        this.replacement = replacement;

        minEntryNum = Integer.MAX_VALUE;
        maxEntryNum = Integer.MIN_VALUE;
        Set<Integer> display = new HashSet<Integer>(entriesList);
        for (SourceTextEntry ste : Core.getProject().getAllEntries()) {
            minEntryNum = Math.min(minEntryNum, ste.entryNum());
            maxEntryNum = Math.max(maxEntryNum, ste.entryNum());
            if (display.contains(ste.entryNum())) {
                entries.put(ste.entryNum(), ste);
            }
        }

        controlComponent = new FilterBarReplace();

        controlComponent.btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Core.getEditor().removeFilter();
            }
        });
        controlComponent.btnSkip.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                skip();
            }
        });
        controlComponent.btnReplaceNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                replace();
            }
        });
    }

    /**
     * Replace all occurrences in all entries.
     */
    public void replaceAll() {
        for (SourceTextEntry ste : entries.values()) {
            TMXEntry en = Core.getProject().getTranslationInfo(ste);
            String trans = en != null ? en.translation : ste.getSourceTranslation();
            List<SearchMatch> found = getReplacementsForEntry(trans);
            if (found != null) {
                int off = 0;
                StringBuilder o = new StringBuilder(trans);
                for (SearchMatch m : found) {
                    o.replace(m.start + off, m.end + off, replacement);
                    off += replacement.length() - (m.end - m.start);
                }
                if (en != null) {
                    Core.getProject().setTranslation(ste, o.toString(), en.note, en.defaultTranslation);
                } else {
                    // new translation - set as default
                    Core.getProject().setTranslation(ste, o.toString(), null, true);
                }
            }
        }
        EditorController ec = (EditorController) Core.getEditor();
        ec.refreshEntries(entries.keySet());
    }

    @Override
    public boolean allowed(SourceTextEntry ste) {
        return entries.containsKey(ste.entryNum());
    }

    @Override
    public Component getControlComponent() {
        return controlComponent;
    }

    public List<SearchMatch> getReplacementsForEntry(String translationText) {
        if (searcher.searchString(translationText)) {
            return searcher.getFoundMatches();
        } else {
            return null;
        }
    }

    private void skip() {
        EditorController ec = (EditorController) Core.getEditor();

        // try to find in current entry
        int pos = ec.getCurrentPositionInEntryTranslation();
        String str = ec.getCurrentTranslation();
        List<SearchMatch> found = getReplacementsForEntry(str);
        if (found != null) {
            for (SearchMatch m : found) {
                if (m.getStart() > pos) {
                    ec.setCaretPosition(new EditorController.CaretPosition(m.getStart(), m.getEnd()));
                    ec.requestFocus();
                    return;
                }
            }
        }
        // not found in current entry - find next entry
        int currentEntryNumber = ec.getCurrentEntryNumber();
        ec.commitAndDeactivate();

        // find to the end of project
        for (int i = currentEntryNumber + 1; i <= maxEntryNum; i++) {
            SourceTextEntry ste = entries.get(i);
            if (ste == null) {
                continue; // entry not filtered
            }
            TMXEntry trans = Core.getProject().getTranslationInfo(ste);
            if (trans == null) {
                continue; // there is no translation
            }
            found = getReplacementsForEntry(trans.translation);
            if (found == null) {
                continue; // no replacements
            }
            for (SearchMatch m : found) {
                ec.gotoEntry(i, new EditorController.CaretPosition(m.start, m.end));
                ec.requestFocus();
                return;
            }
        }
        // find from the beginning of project
        for (int i = minEntryNum; i <= currentEntryNumber; i++) {
            SourceTextEntry ste = entries.get(i);
            if (ste == null) {
                continue; // entry not filtered
            }
            TMXEntry trans = Core.getProject().getTranslationInfo(ste);
            if (trans == null) {
                continue; // there is no translation
            }
            found = getReplacementsForEntry(trans.translation);
            if (found == null) {
                continue; // no replacements
            }
            for (SearchMatch m : found) {
                ec.gotoEntry(i, new EditorController.CaretPosition(m.start, m.end));
                ec.requestFocus();
                return;
            }
        }
    }

    private void replace() {
        EditorController ec = (EditorController) Core.getEditor();

        // is caret inside match ?
        int pos = ec.getCurrentPositionInEntryTranslation();
        String str = ec.getCurrentTranslation();
        for (SearchMatch m : getReplacementsForEntry(str)) {
            if (m.getStart() <= pos && pos <= m.getEnd()) {
                // yes - replace
                ec.replacePartOfTextAndMark(replacement, m.start, m.end);
                // then skip to next
                skip();
                return;
            }
        }
    }
}
