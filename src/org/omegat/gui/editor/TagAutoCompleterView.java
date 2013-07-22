/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Aaron Madlon-Kay, Zoltan Bartko
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

package org.omegat.gui.editor;

import java.util.ArrayList;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.gui.editor.autocompleter.AutoCompleter;
import org.omegat.gui.editor.autocompleter.AutoCompleterView;
import org.omegat.util.OStrings;
import org.omegat.util.TagUtil;

/**
 * An AutoCompleterView for inserting missing tags.
 * 
 * @author Aaron Madlon-Kay
 */
public class TagAutoCompleterView extends AutoCompleterView {

    public TagAutoCompleterView(AutoCompleter completer) {
        super(OStrings.getString("AC_TAG_VIEW"), completer);
    }

    @Override
    public List<String> computeListData(String wordChunk) {
        
        List<String> missingGroups = TagUtil.getGroupedMissingTagsFromTarget();
        
        // Check for partial matches among missing tag groups.
        List<String> matchGroups = new ArrayList<String>();
        for (String g : missingGroups) {
            if (g.startsWith(wordChunk)) matchGroups.add(g);
        }
        
        // For non-space-delimited languages, show all missing tags as
        // suggestions for the case of no matching tags.
        if (!Core.getProject().getProjectProperties().getTargetLanguage().isSpaceDelimited()
                && matchGroups.size() == 0
                && missingGroups.size() > 0) {
            completer.adjustInsertionPoint(wordChunk.length());
            return missingGroups;
        }
        
        return missingGroups;
    }

    @Override
    public ITokenizer getTokenizer() {
        return new DefaultTokenizer();
    }
}
