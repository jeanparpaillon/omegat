/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
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

package org.omegat.core.matching;

import java.text.MessageFormat;
import java.util.List;

import org.omegat.core.StringData;
import org.omegat.core.StringEntry;
import org.omegat.core.threads.CommandThread;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.messages.MessageRelay;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Token;

/**
 * The class, responsible for building the list of fuzzy matches
 * between the source text strings.
 *
 * @author  Maxym Mykhalchuk
 */
public class FuzzyMatcher
{
    private String statusTemplate;
    private MainWindow tf;
    private CommandThread core;
    
    private void updateStatus(int index, int total)
    {
        Object[] obj = { new Integer(index), new Integer(total) };
        MessageRelay.uiMessageSetMessageText(tf, MessageFormat.format(statusTemplate, obj));
        Thread.yield();
    }
    
    /**
     * Creates a new instance of FuzzyMatcher
     */
    public FuzzyMatcher(MainWindow tf, CommandThread core)
    {
        this.tf = tf;
        this.core = core;
    }
    
    /**
     * Builds the similarity data for color highlight in match window.
     */
    private byte[] buildSimilarityData(List sourceTokens, List matchTokens)
    {
        int len = matchTokens.size();
        byte[] result = new byte[len];
        
        boolean leftfound = true;
        for(int i=0; i<len; i++)
        {
            result[i]=0;
            
            Token righttoken = null;
            if( i+1<len )
                righttoken = (Token)matchTokens.get(i+1);
            boolean rightfound = (i+1==len) || sourceTokens.contains(righttoken);
            
            Token token;
            token = (Token)matchTokens.get(i);
            boolean found = sourceTokens.contains(token);
            
            if( found && (!leftfound || !rightfound) )
                result[i] = StringData.PAIR;
            else if( !found )
                result[i] = StringData.UNIQ;
            
            leftfound = found;
        }
        return result;
    }
    
    /**
     * Builds the list of fuzzy matches
     * between the strings of the source text(s).
     *
     * @param strings - the list of the source text strings.
     */
    public void match(List strings) throws InterruptedException
    {
        int total = strings.size();
        
        statusTemplate = OStrings.CT_FUZZY_X_OF_Y;
        updateStatus(0, total);
        
        for(int i=0; i<total; i++)
        {
            if( i%20==0 )
            {
                if( core.isInterrupted() )
                    throw new InterruptedException("Stopping on demand");       // NOI18N
                updateStatus(i, total);
            }
            
            StringEntry strEntry = (StringEntry) strings.get(i);
            List strTokens = strEntry.getSrcTokenList();
            int strTokensSize = strTokens.size();
            if( strTokensSize==0 ) // HP: maybe also test on strTokensComplete.size(), if strTokensSize is 0
                continue;          // HP: perhaps that would result in better number/non-word matching too
            List strTokensAll = strEntry.getSrcTokenListAll(); // HP: includes non-word tokens
            
            for(int j=i+1; j<total; j++)
            {
                StringEntry candEntry = (StringEntry) strings.get(j);
                // don't know why, but it happened once
                if( candEntry==null )
                    continue;
                List candTokens = candEntry.getSrcTokenList();
                int candTokensSize = candTokens.size();
                if( candTokensSize==0 )
                    continue;
                
                int ld = LevenshteinDistance.compute(strTokens, candTokens);
                int similarity = (100 * (Math.max(strTokensSize, candTokensSize) - ld)) /
                        Math.max(strTokensSize, candTokensSize);
                
                if( similarity<OConsts.FUZZY_MATCH_THRESHOLD )
                    continue;

                // determine Levenshtein distance/adjusted similarity across the complete
                // list of tokens, including numbers, tags, and other non-word tokens
                // fix for bug 1449988
                List candTokensAll = candEntry.getSrcTokenListAll();
                int ldAll = LevenshteinDistance.compute(strTokensAll, candTokensAll);
                int simAdjusted = (100 * (Math.max(strTokensAll.size(), candTokensAll.size()) - ldAll)) /
                        Math.max(strTokensAll.size(), candTokensAll.size());
                // end fix 1449988

                //byte[] similarityData = buildSimilarityData(strTokens, candTokens);
                byte[] similarityData = buildSimilarityData(strTokensAll, candTokensAll); // fix for bug 1586397
                strEntry.addNearString(candEntry, similarity, simAdjusted, similarityData, null);

                //similarityData = buildSimilarityData(candTokens, strTokens);
                similarityData = buildSimilarityData(candTokensAll, strTokensAll); // fix for bug 1586397
                candEntry.addNearString(strEntry, similarity, simAdjusted, similarityData, null);
            }
        }
        updateStatus(total, total);
    }
    
    
    /**
     * Builds the list of fuzzy matches
     * of legacy TMs and the strings of the source text(s).
     *
     * @param strings    list of the source text strings
     * @param tmxname    name of legacy TMX file
     * @param tmstrings  the strings of legacy TMX file
     */
    public void match(List strings, String tmxname, List tmstrings) throws InterruptedException
    {
        int tmtotal = tmstrings.size();
        int total = strings.size();
        
        statusTemplate = OStrings.CT_FUZZY_X_OF_Y + " (" + tmxname + ")";       // NOI18N
        updateStatus(0, tmtotal);
        
        for(int i=0; i<tmtotal; i++)
        {
            if( i%20==0 )
            {
                if( core.isInterrupted() )
                    throw new InterruptedException("Stopping on demand");       // NOI18N
                updateStatus(i, tmtotal);
            }
            
            StringEntry strEntry = (StringEntry) tmstrings.get(i);
            List strTokens = strEntry.getSrcTokenList();
            int strTokensSize = strTokens.size();
            if( strTokensSize==0 ) // HP: maybe also test on strTokensComplete.size(), if strTokensSize is 0
                continue;          // HP: perhaps that would result in better number/non-word matching too
            List strTokensAll = strEntry.getSrcTokenListAll(); // HP: includes non-word tokens
            
            for(int j=0; j<total; j++)
            {
                StringEntry candEntry = (StringEntry) strings.get(j);
                List candTokens = candEntry.getSrcTokenList();
                int candTokensSize = candTokens.size();
                if( candTokensSize==0 )
                    continue;
                
                int ld = LevenshteinDistance.compute(strTokens, candTokens);
                int similarity = (100 * (Math.max(strTokensSize, candTokensSize) - ld)) /
                        Math.max(strTokensSize, candTokensSize);
                
                if( similarity<OConsts.FUZZY_MATCH_THRESHOLD )
                    continue;

                // determine Levenshtein distance/adjusted similarity across the complete
                // list of tokens, including numbers, tags, and other non-word tokens
                // fix for bug 1449988
                List candTokensAll = candEntry.getSrcTokenListAll();
                int ldAll = LevenshteinDistance.compute(strTokensAll, candTokensAll);
                int simAdjusted = (100 * (Math.max(strTokensAll.size(), candTokensAll.size()) - ldAll)) /
                        Math.max(strTokensAll.size(), candTokensAll.size());
                // end fix 1449988

                //byte[] similarityData = buildSimilarityData(candTokens, strTokens);
                byte[] similarityData = buildSimilarityData(candTokensAll, strTokensAll); // fix for bug 1586397
                candEntry.addNearString(strEntry, similarity, simAdjusted, similarityData, tmxname);
            }
        }
        updateStatus(tmtotal, tmtotal);
    }
    
    
}
