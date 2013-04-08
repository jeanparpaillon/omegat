/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.
 
 Copyright (C) 2008 Alex Buloichik (alex73mail@gmail.com)
               2013 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.tokenizer;

import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

/**
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
@Tokenizer(languages = { "el" })
public class LuceneGreekTokenizer extends BaseTokenizer {
    @Override
    protected TokenStream getTokenStream(final String strOrig,
            final boolean stemsAllowed, final boolean stopWordsAllowed) {
        if (stemsAllowed) {
            GreekAnalyzer an;
            if (stopWordsAllowed) {
                an = new GreekAnalyzer(Version.LUCENE_36);
            } else {
                an = new GreekAnalyzer(Version.LUCENE_36,
                        EMPTY_STOP_WORDS_LIST);
            }
            return an.tokenStream("", new StringReader(strOrig));
        } else {
            return new StandardTokenizer(Version.LUCENE_36,
                    new StringReader(strOrig.toLowerCase()));
        }
    }
}
