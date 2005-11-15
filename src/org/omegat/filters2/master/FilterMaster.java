/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

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

package org.omegat.filters2.master;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

import org.omegat.core.StringEntry;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.core.threads.CommandThread;
import org.omegat.core.threads.SearchThread;
import org.omegat.filters2.xml.openoffice.OOFilter;
import org.omegat.gui.ProjectProperties;
import org.omegat.util.LFileCopy;
import org.omegat.filters2.*;
import org.omegat.filters2.Instance;
import org.omegat.filters2.html2.HTMLFilter2;
import org.omegat.filters2.text.TextFilter;
import org.omegat.filters2.text.bundles.ResourceBundleFilter;
import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;

/**
 * A master class that registers and handles all the filters.
 * Singleton - there can be only one instance of this class.
 *
 * @author Maxym Mykhalchuk
 */
public class FilterMaster
{
    /** Wrapper around filters storage in an XML file */
    private Filters  filters;
    /** Returns Wrapper around filters storage in an XML file */
    public Filters getFilters()
    {
        return filters;
    }
    
    /**
     * Create a new FilterMaster.
     */
    private FilterMaster()
    {
        loadPlugins();
        if( configFile.exists() )
            loadConfig();
        else
            setupBuiltinFilters();
        loadFilterClassesFromPlugins();
        saveConfig();
    }
    
    private static FilterMaster master = null;
    /**
     * Returns the only instance of this class.
     */
    public static FilterMaster getInstance()
    {
        if( master==null )
            master = new FilterMaster();
        return master;
    }
    

    private boolean memorizing = false;
    /**
     * Call this to turn on/off memorizing passed strings
     * in internal Translation Memory.
     * Typically to be called by Search-in-files functionality.
     */
    private void setMemorizing(boolean value)
    {
        memorizing = value;
    }
    /**
     * Returns whether we're memorizing passed strings
     * in internal Translation Memory.
     */
    private boolean isMemorizing()
    {
        return memorizing && (searchthread==null);
    }
    
    /**
     * Ugly hack to remove Windows Line Feed "\r".
     */
    private String removeLineFeed(String s)
    {
        return s.replaceAll("\r", "");                                          // NOI18N
    }
    
    /**
     * This method is called by filters to:
     * <ul>
     * <li>Instruct OmegaT what source strings are translatable.
     * <li>Get the translation of each source string.
     * </ul>
     *
     * @param entry Translatable source string
     * @return Translation of the source string. If there's no translation, returns the source string itself.
     */
    public String processEntry(String entry)
    {
        // ugly hack, to say the truth
        String src = removeLineFeed(entry);
        
        // some special space handling
        int len = src.length();
        int b = 0;
        StringBuffer bs = new StringBuffer();
        while( b<len && Character.isWhitespace(src.charAt(b)) )
        {
            bs.append(src.charAt(b));
            b++;
        }

        int e = len-1;
        StringBuffer es = new StringBuffer();
        while( e>=b && Character.isWhitespace(src.charAt(e)) )
        {
            es.append(src.charAt(e));
            e--;
        }
        es.reverse();

        src = src.substring(b, e+1);
        
        StringBuffer res = new StringBuffer();
        res.append(bs);
        
        if( CommandThread.core.getProjectProperties().isSentenceSegmentingEnabled() )
        {
            List spaces = new ArrayList();
            List segments = Segmenter.segment(src, spaces);
            for(int i=0; i<segments.size(); i++)
            {
                String onesrc = (String)segments.get(i);
                segments.set(i, processSingleEntry(onesrc));
            }
            res.append(Segmenter.glue(segments, spaces));
        }
        else
            res.append(processSingleEntry(src));
        
        res.append(es);
        return res.toString();
    }
    
    /**
     * Processes a single entry.
     * This method doesn't perform any changes on the passed string.
     *
     * @param src Translatable source string
     * @return Translation of the source string. If there's no translation, returns the source string itself.
     */
    private String processSingleEntry(String src)
    {
        // if the search thread is non-null, we're searching inside files
        // else we're translating them
        if( searchthread!=null )
        {
            searchthread.searchText(src);
            return src;
        }
        else
        {
            StringEntry se = CommandThread.core.getStringEntry(src);
            if( isMemorizing() )
                CommandThread.core.addEntry(src);
            if( se==null )
            {
                return src;
            }
            else
            {
                String s = se.getTranslation();
                if( s==null || s.length()==0 )
                    s = src;
                return s;
            }
        }
    }
    
    private List old_plugins = null;
    private ClassLoader plugins_cl = null;
    /** Returns the classloader of the filter plugins. */
    private ClassLoader getPluginsClassloader()
    {
        if( !plugins.equals(old_plugins) )
        {
            List jars = new ArrayList(plugins.size());
            for(int i=0; i<plugins.size(); i++)
            {
                List pfilters = (List)plugins.get(i);
                URL jarurl = (URL)pfilters.get(0);
                jars.add(jarurl);
            }
            plugins_cl = new URLClassLoader((URL[])jars.toArray(new URL[plugins.size()]));
            old_plugins = new ArrayList();
            old_plugins.addAll(plugins);
        }
        return plugins_cl;
    }
    
    
    /** Utility Method to instantiate a filter */
    private AbstractFilter instantiateFilter(OneFilter filter)
            throws TranslationException
    {
        AbstractFilter filterObject = null;
        try
        {
            ClassLoader cl = getPluginsClassloader();
            Class filterClass;
            if( filter.isFromPlugin() )
                filterClass = cl.loadClass(filter.getClassName());
            else
                filterClass = Class.forName(filter.getClassName());
            Constructor filterConstructor = filterClass.getConstructor((Class[])null);
            filterObject = (AbstractFilter)filterConstructor.newInstance((Object[])null);
        }
        catch( ClassNotFoundException cnfe )
        {
            throw new TranslationException(cnfe.toString());
        }
        catch( NoSuchMethodException nsme )
        {
            throw new TranslationException(nsme.toString());
        }
        catch( InstantiationException ie )
        {
            throw new TranslationException(ie.toString());
        }
        catch ( IllegalAccessException iae )
        {
            throw new TranslationException(iae.toString());
        }
        catch( InvocationTargetException ite )
        {
            throw new TranslationException(ite.getCause().toString());
        }
        return filterObject;
    }
    
    /**
     * OmegaT core calls this method to load a source file.
     *
     * @param filename  The name of the source file to load.
     * @return          Whether the file was handled by one of OmegaT filters.
     * @see #translateFile(String, String, String)
     */
    public boolean loadFile(String filename)
            throws IOException, TranslationException
    {
        LookupInformation lookup = lookupFilter(filename);
        if( lookup==null )
            return false;
        
        //if()
        setMemorizing(true);
        
        AbstractFilter filterObject = lookup.filterObject;
        BufferedReader reader = lookup.reader;
        
        BufferedWriter writer = new BufferedWriter(new StringWriter());
        
        try
        {
            filterObject.processFile(reader, writer);
            reader.close();
        }
        catch( IOException ioe )
        {
            ioe.printStackTrace();
            throw new IOException(filename + "\n" + ioe);                       // NOI18N
        }
        
        setMemorizing(false);
        return true;
    }
    
    
    private SearchThread searchthread = null;
    /**
     * When mode is set,
     * strings are passed to supplied search thread.
     *
     * @param searchthread The Search Thread supplied.
     */
    private void setSearchMode(SearchThread searchthread)
    {
        setMemorizing(false);
        this.searchthread = searchthread;
    }
    /**
     * Cancels search mode.
     */
    private void cancelSearchMode()
    {
        this.searchthread = null;
    }
    
    
    /**
     * OmegaT core calls this method to search within a source file.
     * (used for source files outside project source dir)
     *
     * @param filename  The name of the source file to search.
     * @see #translateFile(String, String, String)
     */
    public void searchFile(String filename, SearchThread searchthread)
            throws IOException, TranslationException
    {
        setSearchMode(searchthread);
        loadFile(filename);
        cancelSearchMode();
    }
    
    /**
     * OmegaT core calls this method to translate a source file.
     * <ul>
     * <li>OmegaT first looks through registered filter instances
     *     to find filter(s) that can handle this file.
     * <li>Opens the file and tests if filter(s) want to handle it.
     * <li>If the filter accepts the file, the appropriate target file is opened.
     * <li>Filter is asked to process the file.
     * <li>Target writer is closed.
     * </ul>
     * If no filter is found, that processes this file,
     * we simply copy it to target folder.
     *
     * @param sourcedir The folder of the source file.
     * @param filename  The name of the source file to process (only the part, relative to source folder).
     * @param targetdir The folder to place the translated file to.
     */
    public void translateFile(String sourcedir, String filename, String targetdir)
            throws IOException, TranslationException
    {
        setMemorizing(false);
        
        LookupInformation lookup = lookupFilter(sourcedir+File.separatorChar+filename);
        if( lookup==null )
        {
            // The file is not supported by any of the filters.
            // Copying it
            LFileCopy.copy(sourcedir+File.separator+filename,
                    targetdir+File.separator+filename);
            return;
        }
        
        File file = new File(sourcedir+File.separatorChar+filename);
        String name = file.getName();
        String path = filename.substring(0, filename.length()-name.length());
        
        AbstractFilter filterObject = lookup.filterObject;
        BufferedReader reader = lookup.reader;
        Instance instance = lookup.instance;
        File outfile =
                new File(
                targetdir + File.separatorChar +
                path + File.separatorChar +
                constructTargetFilename(
                instance.getSourceFilenameMask(),
                name,
                instance.getTargetFilenamePattern()));
        
        BufferedWriter writer = filterObject.createWriter(outfile, instance.getTargetEncoding());
        
        filterObject.processFile(reader, writer);
        reader.close();
        writer.close();
    }
    
    class LookupInformation
    {
        public OneFilter filter;
        public Instance instance;
        public AbstractFilter filterObject;
        public BufferedReader reader;
        
        public LookupInformation(OneFilter filter, Instance instance,
                AbstractFilter filterObject, BufferedReader reader)
        {
            this.filter = filter;
            this.instance = instance;
            this.filterObject = filterObject;
            this.reader = reader;
        }
    }
    
    /**
     * Gets the filter according to the source
     * filename provided.
     * In case of failing to find a filter to handle the file
     * returns <code>null</code>.
     *
     * In case of finding an appropriate filter it
     * <ul>
     * <li>Creates the filter (use <code>OneFilter.getFilter()</code> to get it)
     * <li>Creates a reader (use <code>OneFilter.getReader()</code> to get it)
     * <li>Checks whether the filter supports the file.
     * </ul>
     * It <b>does not</b> check whether the filter supports the file,
     * i.e. it doesn't call <code>isFileSupported</code>
     *
     * @param filename    The source filename.
     * @return            The filter to handle the file.
     */
    private LookupInformation lookupFilter(String filename)
            throws TranslationException, IOException
    {
        File file = new File(filename);
        String name = file.getName();
        String path = file.getParent();
        if( path==null )
            path = "";                                                          // NOI18N
        
        for(int i=0; i<getFilters().getFilter().length; i++)
        {
            OneFilter filter = getFilters().getFilter(i);
            if( !filter.isOn() )
                continue;
            for(int j=0; j<filter.getInstance().length; j++)
            {
                Instance instance = filter.getInstance(j);
                if( matchesMask(name, instance.getSourceFilenameMask()) )
                {
                    AbstractFilter filterObject;
                    filterObject = instantiateFilter(filter);
                    
                    BufferedReader reader = filterObject.createReader(file, instance.getSourceEncoding());
                    
                    reader.mark(OConsts.READ_AHEAD_LIMIT);
                    if( !filterObject.isFileSupported(reader) )
                    {
                        break;
                    }
                    
                    try
                    {
                        reader.reset();
                    }
                    catch( IOException e )
                    {
                        // it means that isFileSupported() have read more than the buffer was
                        StaticUtils.log(filter.getClassName()+
                                ".isFileSupported() violated the contract:\n " +        // NOI18N
                                "It have read more than "+OConsts.READ_AHEAD_LIMIT+     // NOI18N
                                " bytes from the reader.");                             // NOI18N
                        // we need to reopen the reader
                        reader = filterObject.createReader(file, instance.getSourceEncoding());
                    }
                    
                    return new LookupInformation(filter, instance, filterObject, reader);
                }
            }
        }
        return null;
    }
    
    
    private static List supportedEncodings = null;
    /**
     * Queries JRE for the list of supported encodings.
     * Also adds the human name for no/automatic encoding.
     *
     * @return names of all the encodings in an array
     */
    public static List getSupportedEncodings()
    {
        if( supportedEncodings==null )
        {
            supportedEncodings = new ArrayList();
            supportedEncodings.add(AbstractFilter.ENCODING_AUTO_HUMAN);
            supportedEncodings.addAll(Charset.availableCharsets().keySet());
        }
        return supportedEncodings;
    }
    
    //////////////////////////////////////////////////////////////////////////
    // Plugins
    //////////////////////////////////////////////////////////////////////////
    
    /**
     * The list of plugins.
     * <p>
     * The format is simple:
     * <ul>
     * <li>each element of this list is a List itself
     * <li>the first (0th) element of each sublist is an URL of JAR file
     * <li>further elements are names of filter classes
     * </ul>
     */
    private List plugins;
    
    /**
     * Loads filter plugins.
     * <p>
     * Filter plugins should be situated in &lt;OmegaT-install-dir&gt;/plugins,
     * and be packed as JAR files with manifest stating
     * <pre> OmegaT-Plugin: true </pre>
     * and then for each filter
     * <pre> Name: the.package.name.TheFilterName
     * OmegaT-Filter: true</pre> for each filter in a plugin
     * (plugin may have more than one filter).
     */
    private void loadPlugins()
    {
        plugins = new ArrayList();
        File pluginsDir = new File("plugins/");                                 // NOI18N
        if( pluginsDir.exists() && pluginsDir.isDirectory() )
            loadPluginsFrom(pluginsDir);
    }
    
    /**
     * Loads filter plugins from a single directory.
     */
    private void loadPluginsFrom(File dir)
    {
        File[] filters = dir.listFiles(new FileFilter()
        // File filter that accepts JAR files or directories
        {
            public boolean accept(File file)
            {
                return
                        (
                        file.isFile() &&
                        file.getName().toLowerCase().endsWith(".jar")       // NOI18N
                        )
                        ||
                        (
                        file.isDirectory() &&
                        !file.getName().equals(".") &&                      // NOI18N
                        !file.getName().equals("..")                        // NOI18N
                        );
            }
        });
        for(int i=0; i<filters.length; i++)
        {
            if( filters[i].isFile() )
            {
                try
                {
                    URL jar = filters[i].toURL();
                    loadOnePlugin(jar);
                }
                catch( MalformedURLException mue )
                {
                    // nothing is really wrong
                    // strange exception
                    StaticUtils.log("Couldn't access local file system " +      // NOI18N
                            "to get '"+filters[i]+"' !");                       // NOI18N
                    mue.printStackTrace(StaticUtils.getLogStream());
                }
                catch( IOException ioe )
                {
                    // nothing is really wrong
                    // we just couldn't load one JAR
                    StaticUtils.log("Couldn't load plugin JAR '"+               // NOI18N
                            filters[i]+"' !");                                  // NOI18N
                }
            }
            else
                loadPluginsFrom(filters[i]);
        }
    }
    
    /**
     * Loads plugins from a single JAR.
     *
     * @param jar name of the plugin JAR file to load filters from.
     */
    private void loadOnePlugin(URL jar) throws IOException
    {
        JarFile filter_jar = new JarFile(jar.getFile());
        Manifest manifest = filter_jar.getManifest();
        
        Attributes mainattribs = manifest.getMainAttributes();
        if( mainattribs.getValue("OmegaT-Plugin")==null )                       // NOI18N
            return; // it's not OmegaT plugin
        
        List filterList = new ArrayList();
        filterList.add(jar);
        
        Map entries = manifest.getEntries();
        String[] keys = (String[])entries.keySet().toArray(new String[]{});
        for(int i=0; i<keys.length; i++)
        {
            Attributes attrs = (Attributes)entries.get(keys[i]);
            String name = attrs.getValue("Name");                               // NOI18N
            String isfilter = attrs.getValue("OmegaT-Filter");                  // NOI18N
            if( isfilter!=null && isfilter.equals("true") )                     // NOI18N
            {
                filterList.add(keys[i]);
                try
                {
                    URLClassLoader.newInstance(new URL[] {jar}).loadClass(keys[i]);
                }
                catch( ClassNotFoundException e )
                {
                    // we just don't load the plugin
                    throw new IOException(e.getLocalizedMessage());
                }
            }
        }
        
        if( filterList.size()>1 )
        {
            plugins.add(filterList);
        }
    }
    
    //////////////////////////////////////////////////////////////////////////
    // Filters
    //////////////////////////////////////////////////////////////////////////
    
    /**
     * Reverts Filter Configuration to Default values.
     * Basically
     * <ul>
     * <li>Sets up built-in filters
     * <li>Reloads the plugins
     * <li>Loads filters from plugins
     * <li>Saves the configuration
     * </ul>
     */
    public void revertFiltersConfigToDefaults()
    {
        setupBuiltinFilters();
        loadPlugins();
        loadFilterClassesFromPlugins();
        saveConfig();
    }
    
    /** XML file with filters configuration */
    private File configFile = new File("filters.conf");                         // NOI18N
    
    /**
     * My Own Class to listen to exceptions,
     * occured while loading filters configuration.
     */
    class MyExceptionListener implements ExceptionListener
    {
        private List exceptionsList = new ArrayList();
        private boolean exceptionOccured = false;
        public void exceptionThrown(Exception e)
        {
            exceptionOccured = true;
            exceptionsList.add(e);
        }
        
        /**
         * Returns whether any exceptions occured.
         */
        public boolean isExceptionOccured()
        {
            return exceptionOccured;
        }
        /**
         * Returns the list of occured exceptions.
         */
        public List getExceptionsList()
        {
            return exceptionsList;
        }
    }
    
    /**
     * Loads information about the filters from an XML file.
     * If there's an error loading a file, it calls <code>setupDefaultFilters</code>.
     */
    public void loadConfig()
    {
        try
        {
            MyExceptionListener myel = new MyExceptionListener();
            XMLDecoder xmldec = new XMLDecoder(new FileInputStream(configFile), this, myel);
            filters = (Filters)xmldec.readObject();
            xmldec.close();
            
            if( myel.isExceptionOccured() )
            {
                StringBuffer sb = new StringBuffer();
                List exceptions = myel.getExceptionsList();
                for(int i=0; i<exceptions.size(); i++)
                {
                    sb.append("    ");                                          // NOI18N
                    sb.append(exceptions.get(i));
                    sb.append("\n");                                            // NOI18N
                }
                throw new Exception("Exceptions occured while loading file filters:\n"+sb.toString()); // NOI18N
            }
            
            checkIfAllFilterPluginsAreAvailable();
        }
        catch( Exception e )
        {
            StaticUtils.log(OStrings.getString("FILTERMASTER_ERROR_LOADING_FILTERS_CONFIG") + e);
            setupBuiltinFilters();
        }
    }
    
    /**
     * Goes through the list of loaded plugins to see if
     * all the filters that are there in config file are present.
     */
    private void checkIfAllFilterPluginsAreAvailable()
    {
        ClassLoader cl = getPluginsClassloader();
        
        int k=0;
        while( k<filters.filtersSize() )
        {
            OneFilter onefilter = filters.getFilter(k);
            if( !onefilter.isFromPlugin() )
            {
                k++;
                continue;
            }
            
            for(int i=0; i<plugins.size(); i++)
            {
                List filterList = (List)plugins.get(i);
                for(int j=1; j<filterList.size(); j++)
                {
                    String classname = (String)filterList.get(j);
                    if( onefilter.getClass().getName().equals(classname) )
                    {
                        // trying to create
                        try
                        {
                            Class filter_class = cl.loadClass(classname);
                            Constructor filter_constructor = filter_class.getConstructor((Class[])null);
                            Object filter = filter_constructor.newInstance((Object[])null);
                            if( filter instanceof AbstractFilter )
                            {
                                // OK
                                k++;
                                continue;
                            }
                        }
                        catch( Exception e )
                        {
                            // couldn't load one of filters
                            // removing it
                            filters.removeFilter(k);
                            continue;
                        }
                    }
                }
            }
            
            // if we are here, it means that there's no such filter class
            // in all the plugins currently present
            // removing it
            filters.removeFilter(k);
        }
    }
    
    /**
     * Initializes Filter Master defaults
     * by re-creating all information about built-in file filters.
     */
    private void setupBuiltinFilters()
    {
        filters = new Filters();
        filters.addFilter(new OneFilter(new TextFilter(), false));
        filters.addFilter(new OneFilter(new ResourceBundleFilter(), false));
        filters.addFilter(new OneFilter(new HTMLFilter2(), false));
        filters.addFilter(new OneFilter(new OOFilter(), false));
    }
    
    /**
     * Loads filter classes from plugins.
     * <p>
     * Filter plugins should be situated in &lt;OmegaT-install-dir&gt;/plugins,
     * and be packed as JAR files with manifest stating
     * <pre> OmegaT-Plugin: true </pre>
     * and then for each filter
     * <pre> Name: the.package.name.TheFilterName
     * OmegaT-Filter: true</pre> for each filter in a plugin
     * (plugin may have more than one filter).
     */
    private void loadFilterClassesFromPlugins()
    {
        ClassLoader cl = getPluginsClassloader();
        for(int i=0; i<plugins.size(); i++)
        {
            List filterList = (List)plugins.get(i);
            for(int j=1; j<filterList.size(); j++)
            {
                try
                {
                    Class filter_class = cl.loadClass((String)filterList.get(j));
                    Constructor filter_constructor = filter_class.getConstructor((Class[])null);
                    Object filter = filter_constructor.newInstance((Object[])null);
                    if( filter instanceof AbstractFilter )
                    {
                        OneFilter one_filter = new OneFilter((AbstractFilter)filter, true);
                        filters.addFilter(one_filter);
                    }
                }
                catch( Exception e )
                {
                    // couldn't load one of filters
                    // eat (almost) silently
                    StaticUtils.log("Filter '"+(String)filterList.get(j)+       // NOI18N
                            "' from '"+((URL)filterList.get(0)).getFile()+"'"+  // NOI18N
                            " cannot be loaded");                               // NOI18N
                }
            }
        }
    }
    
    /**
     * Saves information about the filters to an XML file.
     */
    public void saveConfig()
    {
        try
        {
            XMLEncoder xmlenc = new XMLEncoder(new FileOutputStream(configFile));
            xmlenc.writeObject(filters);
            xmlenc.close();
        }
        catch( FileNotFoundException fnfe )
        {
            StaticUtils.log(OStrings.getString("FILTERMASTER_ERROR_SAVING_FILTERS_CONFIG") + fnfe);
            JOptionPane.showMessageDialog(null,
                    OStrings.getString("FILTERMASTER_ERROR_SAVING_FILTERS_CONFIG") + fnfe,
                    OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    //////////////////////////////////////////////////////////////////////////
    // Static Utility Methods
    //////////////////////////////////////////////////////////////////////////
    
    /**
     * Whether the mask matches the filename.
     * Filename should be "name.ext", without path.
     *
     * @param filename The filename to check
     * @param mask The mask, against which the filename is tested
     * @return Whether the mask matches the filename.
     */
    private boolean matchesMask(String filename, String mask)
    {
        mask = mask.replaceAll("\\.", "\\\\.");                                   // NOI18N
        mask = mask.replaceAll("\\*", ".*");                                    // NOI18N
        mask = mask.replaceAll("\\?", ".");                                     // NOI18N
        return filename.matches("(?iu)"+mask);                                  // NOI18N
    }
    
    /**
     * Construct a target filename according to pattern from a file's name.
     * Filename should be "name.ext", without path.
     * <p>
     * Output filename pattern is pretty complex.
     * <br>
     * It may consist of normal characters and some substituted variables.
     * They have the format <code>${variableName}</code> and are case insensitive.
     * <br>
     * There're such variables:
     * <ul>
     * <li><code>${filename}</code> - full filename of the input file, both name and extension (default)
     * <li><code>${nameOnly}</code> - only the name of the input file without extension part
     * <li><code>${extension}</code> - the extension of the input file
     * <li><code>${targetLocale}</code> - target locale code (of a form "xx_YY")
     * <li><code>${targetLanguage}</code> - the target language and country code together (of a form "XX-YY")
     * <li><code>${targetLanguageCode}</code> - the target language only ("XX")
     * <li><code>${targetCoutryCode}</code> - the target country only ("YY")
     * </ul>
     * <p>
     * Most file filters will use default "<code>${filename}</code>,
     * that leads to the name of translated file being the same as
     * the name of source file. But for example the Java(TM) Resource Bundles file filter
     * will have the pattern equal to
     * "<code>${nameonly}_${targetlanguage}.${extension}</code>".
     * <p>
     * E.g. if you have
     * <ul>
     * <li>a source filename mask "*.ext1.ext2"
     * <li>file name "thisisfile.ext1.ext2"
     * </ul>
     * Then
     * <ul>
     * <li><code>${nameOnly}</code> will be equal to "thisisfile"
     * <li>and <code>${extension}</code> - "ext1.ext2"
     * </ul>
     *
     * @param filename Filename to change
     * @param pattern Pattern, according to which we change the filename
     * @return The changed filename
     */
    private String constructTargetFilename(String sourceMask, String filename, String pattern)
    {
        int lastStarPos = sourceMask.lastIndexOf('*');
        int dot = 0;
        if( lastStarPos>=0 )
        {
            // bugfix #1204740
            // so where's the dot next to the star
            int lastDotPos=sourceMask.indexOf('.', lastStarPos);
            // counting chars after the dot
            int extlength=sourceMask.length()-lastDotPos;
            // going forward this many chars
            // and finding the dot we looked for
            dot = filename.length()-extlength;
        }
        else
        {
            dot = filename.lastIndexOf('.');
        }
        
        String nameOnly = filename;
        String extension = "";                                                  // NOI18N
        if( dot>=0 )
        {
            nameOnly = filename.substring(0, dot);
            extension = filename.substring(dot+1);
        }
        
        String res = pattern;
        res = res.replaceAll(targetRegexer(AbstractFilter.TFP_FILENAME),
                filename);
        res = res.replaceAll(targetRegexer(AbstractFilter.TFP_NAMEONLY),
                nameOnly);
        res = res.replaceAll(targetRegexer(AbstractFilter.TFP_EXTENSION),
                extension);
        
        Language targetLang = new Language(
                Preferences.getPreference(Preferences.TARGET_LOCALE));
        
        res = res.replaceAll(targetRegexer(AbstractFilter.TFP_TARGET_LOCALE),
                targetLang.getLocale());
        res = res.replaceAll(targetRegexer(AbstractFilter.TFP_TARGET_LANGUAGE),
                targetLang.getLanguage());
        res = res.replaceAll(targetRegexer(AbstractFilter.TFP_TARGET_LANG_CODE),
                targetLang.getLanguageCode());
        res = res.replaceAll(targetRegexer(AbstractFilter.TFP_TARGET_COUNTRY_CODE),
                targetLang.getCountryCode());
        
        return res;
    }
    
    private String targetRegexer(String tfp)
    {
        String pattern = tfp;
        pattern = pattern.replaceAll("\\$", "\\\\\\$");                         // NOI18N
        pattern = pattern.replaceAll("\\{", "\\\\{");                           // NOI18N
        pattern = pattern.replaceAll("\\}", "\\\\}");                           // NOI18N
        pattern = "(?i)"+pattern;                                               // NOI18N
        return pattern;
    }
}
