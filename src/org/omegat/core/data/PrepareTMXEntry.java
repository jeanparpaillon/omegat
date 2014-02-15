/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2012 Guido Leenders, Thomas Cordonnier
               2013 Aaron Madlon-Kay
               2014 Alex Buloichik
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.omegat.util.TMXReader2;

/**
 * Class for prepare TMXEntry content before save unchangeable copy in the ProjectTMX. We can't use just
 * parameters in the setTranslation() method since count of parameters is too much. Structure of this class is
 * almost the save like TMXEntry.
 * 
 * Instead, we will set all parameters into this class, then ProjectTMX will convert in into TMXEntry than
 * save internally.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Guido Leenders
 * @author Aaron Madlon-Kay
 */
public class PrepareTMXEntry {
    public String source;
    public String translation;
    public String changer;
    public long changeDate;
    public String creator;
    public long creationDate;
    public String note;
    public boolean defaultTranslation = true;
    public Map<String, String> otherProperties;
    public List<String> xICE;
    public List<String> x100PC;
    public boolean xAUTO;

    public PrepareTMXEntry() {
    }

    public PrepareTMXEntry(TMXEntry e) {
        source = e.source;
        translation = e.translation;
        changer = e.changer;
        changeDate = e.changeDate;
        creator = e.creator;
        creationDate = e.creationDate;
        note = e.note;
        defaultTranslation = e.defaultTranslation;
        otherProperties = e.otherProperties == null ? null : new TreeMap<String, String>(e.otherProperties);
        this.xICE = e.xICE == null ? null : new ArrayList<String>(e.xICE);
        this.x100PC = e.x100PC == null ? null : new ArrayList<String>(e.x100PC);
        this.xAUTO = e.xAUTO;
    }

    public void readFromTU(List<TMXReader2.Prop> props) {
        for (TMXReader2.Prop p : props) {
            if (ProjectTMX.PROP_XICE.equals(p.type)) {
                if (xICE == null) {
                    xICE = new ArrayList<String>();
                }
                xICE.add(p.value);
            } else if (ProjectTMX.PROP_X100PC.equals(p.type)) {
                if (x100PC == null) {
                    x100PC = new ArrayList<String>();
                }
                x100PC.add(p.value);
            } else if (ProjectTMX.PROP_XAUTO.equals(p.type)) {
                xAUTO = true;
            } else {
                if (otherProperties == null) {
                    otherProperties = new TreeMap<String, String>();
                }
                otherProperties.put(p.type, p.value);
            }
        }
    }
}
