/**************************************************************************
 * OmegaT - Java based Computer Assisted Translation (CAT) tool
 * Copyright (C) 2002-2004  Keith Godfrey et al
 * keithgodfrey@users.sourceforge.net
 * 907.223.2039
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class automatically detects encoding of an inner file
 * and constructs a Reader with appropriate encoding.
 * <p/>
 * Detecting of encoding is done:
 * <ul>
 * <li>for HTML - by reading a possible 
 *     <code>&lt;META http-equiv="content-type" content="text/html; charset=..."&gt;</code>
 * <li>for XML -  by reading a value from XML header
 *     <code>&lt;?xml version="1.0" encoding="..."?&gt;</code>
 * </ul>
 * If encoding isn't specified, or it is not supported by Java platform,
 * the file is opened in default system encoding (ISO-8859-2 in USA, Windows-1251 on my OS).
 *
 * @author Maxym Mykhalchuk
 */
public class EncodingAwareReader extends Reader
{
	/** Inner reader */
	private Reader reader;
	
	/** This is HTML stream */
	public static final int ST_HTML = 1;
	/** This is XML stream */
	public static final int ST_XML = 2;
	
	/** The type of inner data - HTML or XML */
	private int type;
	
	/**
	 * Creates a new instance of EncodingAwareReader.
	 *
	 * @param fileName - the file to read
	 * @param type - the type of data (HTML or XML)
	 */
	public EncodingAwareReader(String fileName, int type) throws IOException
	{
		this.type = type;
		FileInputStream fis = new FileInputStream(fileName);
		try
		{
			String encoding = fileEncoding(fileName);
			reader = new InputStreamReader(fis, encoding);
		}
		catch( UnsupportedEncodingException uee )
		{
			reader = new InputStreamReader(fis);
		}
	}
	
	/** Return encoding of the file, if defined */
	private String fileEncoding(String fileName) throws IOException
	{
		BufferedReader ereader = new BufferedReader(new FileReader(fileName));
		StringBuffer buffer = new StringBuffer();
		while( ereader.ready() ) {
			buffer.append( ereader.readLine().toLowerCase() );
			switch( type )
			{
				case ST_HTML:
					Matcher matcher_html = PatternConsts.HTML_ENCODING.matcher(buffer);
					if( matcher_html.find() )
						return matcher_html.group(1);
					if( buffer.indexOf("</head>") >= 0 ) // NOI18N
						return "";
					break;
				case ST_XML:
					Matcher matcher_xml = PatternConsts.XML_ENCODING.matcher(buffer);
					if( matcher_xml.find() )
						return matcher_xml.group(1);
					Matcher matcher_xml2 = PatternConsts.XML_HEADER.matcher(buffer);
					if( matcher_xml2.find() )
						return "";
					break;
				default:
					throw new IOException("[EAR] Wrong type of stream specified: Either it's HTML, or XML!");
			}
		}
		ereader.close();
		return ""; // NOI18N
	}
	
	public void close() throws IOException
	{
		reader.close();
	}

	public int read(char[] cbuf, int off, int len) throws IOException
	{
		return reader.read(cbuf, off, len);
	}
	
}
