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

package org.omegat.filters3.xml.opendoc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.LFileCopy;

/**
 * Filter for Open Document file format.
 * 
 * @author Maxym Mykhalchuk
 */
public class OpenDocFilter extends AbstractFilter
{
    private static final String CONTENT_XML = "content.xml";                    // NOI18N
    private static final String STYLES_XML = "styles.xml";                      // NOI18N
    private static final HashSet TRANSLATABLE = new HashSet(
            Arrays.asList(new String[] { CONTENT_XML, STYLES_XML }));
    
    /** Creates a new instance of OpenDocFilter */
    public OpenDocFilter()
    {
    }

    /** Returns true if it's OpenDocument file. */
    public boolean isFileSupported(File inFile, String inEncoding)
    {
        try
        {
            ZipFile file = new ZipFile(inFile);
            Enumeration entries = file.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (TRANSLATABLE.contains(entry.getName()))
                    return true;
            }
        } catch (IOException e) {}
        return false;
    }

    OpenDocXMLFilter xmlfilter = null;
    private OpenDocXMLFilter getXMLFilter()
    {
        if (xmlfilter==null)
            xmlfilter = new OpenDocXMLFilter();
        return xmlfilter;
    }
    
    /** Makes all path separators UNIX-like: '/' */
    private File tmp() throws IOException
    {
        return File.createTempFile("ot-oo-", ".xml");
    }
    
    /**
     * Processes a single OpenDocument file,
     * which is actually a ZIP file consisting of many XML files, 
     * some of which should be translated.
     */
    public List processFile(File inFile, String inEncoding, File outFile, String outEncoding) throws IOException, TranslationException
    {
        ZipFile zipfile = new ZipFile(inFile);
        ZipOutputStream zipout = null;
        if (outFile!=null)
            zipout = new ZipOutputStream(new FileOutputStream(outFile));
        Enumeration zipcontents = zipfile.entries();
        while (zipcontents.hasMoreElements())
        {
            ZipEntry zipentry = (ZipEntry) zipcontents.nextElement();
            if (TRANSLATABLE.contains(zipentry.getName()))
            {
                File tmpin = tmp();
                LFileCopy.copy(zipfile.getInputStream(zipentry), tmpin);
                File tmpout = null;
                if (zipout!=null)
                    tmpout = tmp();
                
                try
                {
                    getXMLFilter().processFile(tmpin, null, tmpout, null);
                }
                catch (Exception e)
                {
                    throw new TranslationException(e.getLocalizedMessage() +
                            "\n" +
                            "Error in file "+inFile);
                }
                
                if (zipout!=null)
                {
                    ZipEntry outentry = new ZipEntry(zipentry.getName());
                    outentry.setMethod(ZipEntry.DEFLATED);
                    zipout.putNextEntry(outentry);
                    LFileCopy.copy(tmpout, zipout);
                    zipout.closeEntry();
                }
                if (!tmpin.delete())
                    tmpin.deleteOnExit();
                if (tmpout!=null)
                {
                    if (!tmpout.delete())
                        tmpout.deleteOnExit();
                }
            }
            else
            {
                if (zipout!=null)
                {
                    zipout.putNextEntry(zipentry);
                    LFileCopy.copy(zipfile.getInputStream(zipentry), zipout);
                    zipout.closeEntry();
                }
            }
        }
        if (zipout!=null)
            zipout.close();
        return null;
    }

    /** Human-readable OpenDocument filter name. */
    public String getFileFormatName()
    {
        return "OpenDocument files";
    }

    /** Extensions... */
    public Instance[] getDefaultInstances()
    {
        return new Instance[] 
        {
                new Instance("*.sx?"),                                          // NOI18N
                new Instance("*.st?"),                                          // NOI18N
                new Instance("*.od?"),                                          // NOI18N
                new Instance("*.ot?"),                                          // NOI18N
        };
    }

    /** Source encoding can not be varied by the user. */
    public boolean isSourceEncodingVariable()
    {
        return false;
    }

    /** Target encoding can not be varied by the user. */
    public boolean isTargetEncodingVariable()
    {
        return false;
    }

    /** Not implemented. */
    protected void processFile(BufferedReader inFile, BufferedWriter outFile) throws IOException, TranslationException
    {
        throw new IOException("Not Implemented!");                              // NOI18N
    }
    
}
