/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2012 Guido Leenders, Thomas Cordonnier
               2013 Aaron Madlon-Kay
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

package org.omegat.core.data;

import org.omegat.util.StringUtil;

import java.util.Map;
 
/**
 * Storage for TMX entry.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Guido Leenders
 * @author Aaron Madlon-Kay
 */
public class TMXEntry {
    public final String source;
    public final String translation;
    public final String changer;
    public final long changeDate;
    public final String creator;
    public final long creationDate;
    public final String note;
    public final boolean defaultTranslation;
    public final Map<String,String> properties;

    public TMXEntry(String source, String translation, String changer, long changeDate,
            String creator, long creationDate, String note, boolean defaultTranslation,
            Map<String,String> properties) {
        this.source = source;
        this.translation = translation;
        this.changer = changer;
        this.changeDate = changeDate;
        this.creator = creator;
        this.creationDate = creationDate;
        this.note = note;
        this.defaultTranslation = defaultTranslation;
        this.properties = properties;
    }

    public TMXEntry(String source, String translation, boolean defaultTranslation) {
        this(source, translation, null, 0, null, 0, null, defaultTranslation, null);
    }
	
    public boolean isTranslated() {
        return translation != null;
    }
    
    public boolean hasNote() {
        return note != null;
    }

    public boolean equalsTranslation(TMXEntry other) {
        if (other == null) {
            return false;
        }
        /*
         * Dates can't be just checked for equals since date stored in memory with 1 milliseconds accuracy,
         * but written to file with 1 second accuracy.
         */
        if (!StringUtil.equalsWithNulls(translation, other.translation)) {
            return false;
        }
        if (!StringUtil.equalsWithNulls(note, other.note)) {
            return false;
        }
        return true;
    }
}
