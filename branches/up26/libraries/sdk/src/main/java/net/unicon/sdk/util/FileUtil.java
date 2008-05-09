/*
 * Copyright (C) 2007 Unicon, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this distribution.  It is also available here:
 * http://www.fsf.org/licensing/licenses/gpl.html
 *
 * As a special exception to the terms and conditions of version 
 * 2 of the GPL, you may redistribute this Program in connection 
 * with Free/Libre and Open Source Software ("FLOSS") applications 
 * as described in the GPL FLOSS exception.  You should have received
 * a copy of the text describing the FLOSS exception along with this
 * distribution.
 */
package net.unicon.sdk.util;

import java.io.*;
import java.util.*;

import org.apache.oro.text.perl.*;
import org.apache.oro.text.regex.*;


/**
 * A static class that provides some useful file manipulation methods. Please
 * note that some methods only work in Unix environments (these methods are
 * marked as such).<p>
 *
 * @author UNICON, Inc.
 * @version 3.1 */
public class FileUtil
{

	// Singleton class
	private FileUtil() {}

	/**
	 * Reads in the file whose name is specified, in UTF8 format. Same
	 * behavior as <code>readFile(new File(filename))</code>.
	 *
	 * @param filename the name of the file to be read
	 *
	 * @return a buffer containing the contents of the file named
	 * <code>filename</code>
	 *
	 * @exception IOException if an error occurs while reading the file
	 *
	 * @see #readFileInBytes(File)
	 * @see #readFile(File)
	 * @see #readFileAndChop(String)*/
	public static StringBuffer readFile(String filename)
		throws IOException
	{
		return readFile(new File(filename));
	}

	/**
	 * Reads in a specified file using UTF8 encoding
	 *
	 * @param file the file to be read
	 *
	 * @return a buffer containing the contents of <code>file</code>
	 *
	 * @exception IOException if an error occurs while reading the file
	 *
	 * @see #readFileInBytes(File)
	 * @see #readFile(String)
	 * @see #readFile(Reader)
	 * @see #readFileAndChop(String) */
	public static StringBuffer readFile(File file)
			throws IOException
	{
        FileInputStream		f = new FileInputStream (file);
        InputStreamReader 	s = new InputStreamReader (f, "UTF-8");
		return (readFile(s));
	}

	/**
	 * Reads in the contents of a given reader
	 *
	 * @param reader the reader to be read from
	 *
	 * @return a buffer containing the contents of <code>reader</code>
	 *
	 * @exception IOException if an error occurs while reading the file
	 *
	 * @see #readFileInBytes(File)
	 * @see #readFile(String)
	 * @see #readFile(File)
	 * @see #readFileAndChop(String) */
	public static StringBuffer readFile(Reader reader)
			throws IOException
	{
		StringBuffer	buff = new StringBuffer();

		int numRead;
		char[] b = new char[4096];

		while ((numRead = reader.read(b, 0, 4096)) != -1) {
			buff.append(b, 0, numRead);
		}

		reader.close();
		return buff;
	}

}

