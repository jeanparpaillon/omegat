/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.core.data;

import java.util.ArrayList;
import java.util.List;

import org.omegat.core.segmentation.Rule;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.ITranslateCallback;
import org.omegat.util.Language;
import org.omegat.util.StringUtil;

/**
 * Base class for entry translation.
 * 
 * This class collects all segments which should be translated, for ability to link prev/next segments for the
 * seconds pass.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public abstract class TranslateEntry implements ITranslateCallback {

    private final ProjectProperties m_config;
    
    private int pass;
    
    /** Collected segments. */
    private List<TranslateEntryQueueItem> translateQueue = new ArrayList<TranslateEntryQueueItem>();
    
    /**
     * Index of currently processed segment. It required for multiple translation for use right segment.
     */
    private int currentlyProcessedSegment;

    public TranslateEntry(final ProjectProperties m_config) {
        this.m_config = m_config;
    }

    /**
     * Set current pass number, i.e. 1 or 2.
     */
    public void setPass(int pass) {
        this.pass = pass;
        currentlyProcessedSegment = 0;
    }
    
    protected void fileFinished() {
        if (currentlyProcessedSegment != translateQueue.size()) {
            throw new RuntimeException("Invalid two-pass processing: number of segments are not equals");
        }
        translateQueue.clear();
    }
    
    /**
     * Get translation for specified entry to write output file.
     * 
     * @param entry
     *            entry ID
     * @param source
     *            source text
     */
    public String getTranslation(final String id, final String origSource) {
        ParseEntry.ParseEntryResult spr = new ParseEntry.ParseEntryResult();

        final String source = ParseEntry.stripSomeChars(origSource, spr);

        StringBuffer res = new StringBuffer();

        if (m_config.isSentenceSegmentingEnabled()) {
            List<StringBuffer> spaces = new ArrayList<StringBuffer>();
            List<Rule> brules = new ArrayList<Rule>();
            Language sourceLang = m_config.getSourceLanguage();
            Language targetLang = m_config.getTargetLanguage();
            List<String> segments = Segmenter.segment(sourceLang, source, spaces, brules);
            for (int i = 0; i < segments.size(); i++) {
                String onesrc = segments.get(i);
                segments.set(i, internalGetSegmentTranslation(id, i, onesrc));
            }
            res.append(Segmenter.glue(sourceLang, targetLang, segments, spaces, brules));
        } else {
            res.append(internalGetSegmentTranslation(id, 0, source));
        }

        // replacing all occurrences of LF (\n) by either single CR (\r) or CRLF
        // (\r\n)
        // this is a reversal of the process at the beginning of this method
        // fix for bug 1462566
        String r = res.toString();
        if (spr.crlf) {
            r = r.replace("\n", "\r\n");
        } else if (spr.cr) {
            r = r.replace("\n", "\r");
        }

        if (spr.spacesAtBegin > 0) {
            r = origSource.substring(0, spr.spacesAtBegin) + r;
        }

        if (spr.spacesAtEnd > 0) {
            r = r + origSource.substring(origSource.length() - spr.spacesAtEnd);
        }

        return r;
    }

    /**
     * {@inheritDoc}
     */
    public void linkPrevNextSegments() {
        for (int i = 0; i < translateQueue.size(); i++) {
            TranslateEntryQueueItem item = translateQueue.get(i);
            try {
                item.prevSegment = translateQueue.get(i - 1).segmentSource;
            } catch (IndexOutOfBoundsException ex) {
                // first entry - previous will be empty
                item.prevSegment = "";
            }
            try {
                item.nextSegment = translateQueue.get(i + 1).segmentSource;
            } catch (IndexOutOfBoundsException ex) {
                // last entry - next will be empty
                item.nextSegment = "";
            }
        }
    }
    
    /**
     * This method calls real method for empty prev/next on the first pass, then with real prev/next on the
     * second pass.
     * 
     * @param id
     * @param segmentIndex
     * @param segmentSource
     * @return
     */
    private String internalGetSegmentTranslation(String id, int segmentIndex, String segmentSource) {
        TranslateEntryQueueItem item;
        switch (pass) {
        case 1:
            item = new TranslateEntryQueueItem();
            item.id = id;
            item.segmentIndex = segmentIndex;
            item.segmentSource = segmentSource;
            translateQueue.add(item);
            currentlyProcessedSegment++;
            return getSegmentTranslation(id, segmentIndex, segmentSource, null, null);
        case 2:
            item = translateQueue.get(currentlyProcessedSegment);
            if (!StringUtil.equalsWithNulls(id, item.id) || segmentIndex != item.segmentIndex
                    || !StringUtil.equalsWithNulls(segmentSource, item.segmentSource)) {
                throw new RuntimeException("Invalid two-pass processing: not equals fields");
            }
            currentlyProcessedSegment++;
            return getSegmentTranslation(id, segmentIndex, segmentSource, item.prevSegment, item.nextSegment);
        default:
            throw new RuntimeException("Invalid pass number: " + pass);
        }
    }
    
    protected abstract String getSegmentTranslation(String id, int segmentIndex, String segmentSource,
            String prevSegment, String nextSegment);
    
    /**
     * Storage for cached segments.
     */
    protected static class TranslateEntryQueueItem {
        String id;
        int segmentIndex;
        String segmentSource;
        String prevSegment;
        String nextSegment;
    }
}
