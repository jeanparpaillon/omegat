/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2015 Hiroshi Miura
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

package org.omegat.core.dictionaries;

import java.io.File;
import java.util.Map;

import org.junit.Test;

import org.omegat.core.TestCore;

/**
 * Dictionary test
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Hiroshi Miura
 */
public class StarDictTest extends TestCore {
    /**
     * Test of readArticle method, of class StarDict.
     */
    @Test
    public void testStarDictReadArticle() throws Exception {
        System.out.println("readArticle from latin-francais stardict dictionary.");
        String word = "testudo";
        String expResult = "dinis, f. : tortue";
        StarDict s = new StarDict(new File("test/data/dicts/latin-francais.ifo"));
        Map<String, Object> map = s.readHeader();
        Object data = map.get(word);
        String result = s.readArticle(word, data);
        assertEquals(expResult, result);
    }
}
