/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008-2013 Alex Buloichik
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

package org.omegat.gui.tagvalidation;

import java.util.List;

import org.omegat.core.data.SourceTextEntry;

/**
 * Interface for tag validation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public interface ITagValidation {
    /**
     * Get invalid tags entries.
     * 
     * @return list of entries with invalid tags, or null if all entries are
     *         valid
     */
    List<SourceTextEntry> listInvalidTags();

    /**
     * Checks invalid tags for one entry.
     * 
     * @param ste
     *            entry
     * @return true if all tags are valid
     */
    boolean checkInvalidTags(SourceTextEntry ste);

    /**
     * Show invalid tags entries.
     * 
     * @param invalidTagsEntries
     *            list of invalid tags entries(from listInvalidTags() method)
     */
    void displayTagValidationErrors(List<SourceTextEntry> invalidTagsEntries);
}
