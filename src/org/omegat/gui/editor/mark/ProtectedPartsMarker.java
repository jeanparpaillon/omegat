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

package org.omegat.gui.editor.mark;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.Core;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.Document3;
import org.omegat.gui.editor.EditorController;
import org.omegat.util.gui.Styles;

/**
 * Marker for SourceTextEntry.protectedParts and tags.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ProtectedPartsMarker implements IMarker {
    protected static final HighlightPainter PAINTERrtl = new TransparentHighlightPainter(Styles.COLOR_PLACEHOLDER, 0.2F);
    protected static final AttributeSet ATTRIBUTESltr = Styles.createAttributeSet(Styles.COLOR_PLACEHOLDER, null, null,
            null);

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText, boolean isActive)
            throws Exception {
        HighlightPainter painter;
        AttributeSet attrs;
        if (((EditorController) Core.getEditor()).getOrientation() == Document3.ORIENTATION.ALL_LTR) {
            attrs = ATTRIBUTESltr;
            painter = null;
        } else {
            attrs = null;
            painter = PAINTERrtl;
        }

        List<Mark> r = new ArrayList<Mark>();

        // find protected parts
        for (ProtectedPart pp : ste.getProtectedParts()) {
            int pos = -1;
            while ((pos = sourceText.indexOf(pp.getTextInSourceSegment(), pos + 1)) >= 0) {
                Mark m = new Mark(Mark.ENTRY_PART.SOURCE, pos, pos + pp.getTextInSourceSegment().length());
                m.painter = painter;
                m.attributes = attrs;
                m.toolTipText = escapeHtml(pp.getDetailsFromSourceFile());
                r.add(m);
            }
            if (translationText != null) {
                pos = -1;
                while ((pos = translationText.indexOf(pp.getTextInSourceSegment(), pos + 1)) >= 0) {
                    Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, pos, pos + pp.getTextInSourceSegment().length());
                    m.painter = painter;
                    m.attributes = attrs;
                    m.toolTipText = escapeHtml(pp.getDetailsFromSourceFile());
                    r.add(m);
                }
            }
        }

        return r;
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
