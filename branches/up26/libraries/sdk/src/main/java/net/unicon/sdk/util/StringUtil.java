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

import java.text.*;
import java.util.*;
import java.io.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A static class that provides some useful methods for String manipulation,
 * specifically as relates to translation between encoding schemes
 *
 * @author Unicon, Inc.
 * @version 3.0 */
public class StringUtil
{
    private static final Log log = LogFactory.getLog(StringUtil.class);

	/**
	 * Returns a specified String in UTF8 encoding
	 *
	 * @deprecated All strings should be read in with a UTF8 Reader.
	 * @param str the String to be transformed to UTF8
	 *
	 * @return a UTF8 version of <code>str</code> */
	private static String toUTF8(String str)
	{
		String UTFString = "";
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			OutputStreamWriter    osw = new OutputStreamWriter(bos, "UTF-8");
			PrintWriter			  pw  = new PrintWriter(osw);
			pw.print(str);
			pw.flush();
			
			byte[] UTF = bos.toByteArray();
			UTFString = new String(UTF);
		} catch (UnsupportedEncodingException e) {
			log.error(e);
		}

		return UTFString;
	}

	/**
	 * Transforms a given Unicode string so that all nonprintable characters
	 * are represented in a printable manner
	 *
	 * @param str the string to be translated
	 *
	 * @return a translated version of <code>str</code>, with each backslash
	 * character (for example, <code>\n</code>) replaced by an escaped version
	 * (for axample, <code>\\n</code>), and each Unicode-specific character
	 * replaced by <code>\\u</code> followed by the character's hexidecimal
	 * representation */
	public static String toUnicodeEscapeString(String str)
	{
		StringBuffer buf = new StringBuffer();
		int len = str.length();
		char ch;

		for (int i = 0; i < len; i++) {
			ch = str.charAt(i);

			switch (ch) {
			case '\\': 
				buf.append("\\\\"); 
				break;
			case '\t': 
				buf.append("\\t"); 
				break;
			case '\n': 
				buf.append("\\n"); 
				break;
			case '\r': 
				buf.append("\\r"); 
				break;				
			default:
				if (ch >= ' ' && ch <= 127) {
					buf.append(ch);
				}
				else {
					buf.append('\\');
					buf.append('u');
					buf.append(toHex((ch >> 12) & 0xF));
					buf.append(toHex((ch >>	8) & 0xF));
					buf.append(toHex((ch >>	4) & 0xF));
					buf.append(toHex((ch >>	0) & 0xF));
				}
			}
		}
		return buf.toString();
	}

	private static char[] hexDigit =
	{ '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f' };

	private static char toHex(int nibble)
	{
		return hexDigit[(nibble & 0xF)];
	}

	/**
	 * Returns a 2-byte Java Unicode-encoded version of a given UTF8-encoded
	 * string
	 *
	 * @param in a UTF8-encoded String
	 *
	 * @return a Unicode-encoded String translated from <code>in</code>
	 *
	 * @exception java.io.UnsupportedEncodingException if UTF8 is not
	 * supported */
	public static String fromUTF8(String in)
		throws java.io.UnsupportedEncodingException
	{
		return translate(in, "UTF-8");
	}

	/**
	 * Returns a 2-byte Java Unicode-encoded version of a String in a
	 * specified encoding
	 *
	 * @param in       the String to be translated to Unicode
	 * @param encoding the encoding schema of <code>in</code>
	 *
	 * @return the translation of <code>in</code> from <code>encoding</code>
	 * to Unicode
	 *
	 * @exception java.io.UnsupportedEncodingException if the specified
	 * encoding is not supported 
	 *
	 * @see #fromUTF8(String) */
	public static String translate(String in, String encoding)
		throws java.io.UnsupportedEncodingException
	{
		return new String(in.getBytes(), encoding);
	}
	
	/**
	 * Creates a comma-separated list of Strings from a specified array
	 *
	 * @param list an array of Strings to be listed
	 *
	 * @return a String consisting of the elements of <code>list</code>,
	 * concatenated and separated by commas */
	public static String commaSeparated(String[] list)
	{
		if (list == null || list.length <= 0)
			return "";

		StringBuffer result = new StringBuffer();

		result.append(list[0]);
		for (int i=1; i < list.length; i++) {
			result.append(", ");
			result.append(list[i]);
		}

		return result.toString();
	}


	/**
	 * Creates a list of Strings from a specified Collection of Strings
	 *
	 * @param list an array of Strings to be listed
     * @param a separator to be used in th elist
	 *
	 * @return a String consisting of the elements of <code>list</code>,
	 * concatenated and separated by  <code>separator</code>
     */
	public static String concatStrings(Collection list, String separator)
	{
		StringBuffer result = new StringBuffer("");
        Iterator it = list.iterator();
        if (it.hasNext()) {
            result.append(it.next());
        }
        while (it.hasNext()) {
			result.append(separator);
			result.append(it.next());
		}

		return result.toString();
	}

	/**
	 * Creates a comma-separated list of Strings from a specified Collection of Strings
	 *
	 * @param list an array of Strings to be listed
	 *
	 * @return a String consisting of the elements of <code>list</code>,
	 * concatenated and separated by commas
     */
    public static String commaSeparated(Collection list) {
        return concatStrings(list, ",");
    }

	/**
	 * Replace all occurances of <i>old</i> with <i>replace</i> inside
	 * the String <i>in</i> - only because java.lang.String doesn't
	 * have one!
	 * 
	 * @param in original String
	 * @param old String to be replaced
	 * @param replace replacement String
	 */
	public static String replace(String in, String old, String replace) {
	
		if (in == null) {
			return null;
		}
	
		StringBuffer result = new StringBuffer();


		int lastIndex = 0;
		int index = in.indexOf(old);

		while (index != -1) {

			result.append(in.substring(lastIndex, index));
			result.append(replace);

			lastIndex = index + old.length();
			index = in.indexOf(old, index+1);
		}
			result.append(in.substring(lastIndex));

		return result.toString();

	}
	
	/**
	 * Wrap input String at maxLineWidth.
	 *
	 * @param body 		String to be wrapped
	 * @param maxLineWidth  The line width of the properly formatted line in the input string (message body)
	 *
	 * Note: This function is meant to wrap the lines in which the parameters (in curly brackets) have
	 *       just been replaced.  This function will keep the rest of the string exactly the same as the
	 *       original text. (chen)
	 */
	public static String wrap(String body, int maxLineWidth) {

		StringBuffer result = new StringBuffer();
		int indexWrap 		= 0;
		int currentIndex 	= 0;
		int indexLastBlank 	= 0;
		boolean isPrevBlank 	= false;
		boolean isNewLineReplaced = false;
		boolean isWrapped = false;

		for(int i=0; i < body.length(); i++)
		{
			switch (body.charAt(i))
			{	
				case '\n': 
					if(!isWrapped)
					{
						if(!isNewLineReplaced)
						{
							result.append('\n');
							indexWrap = 0;
							currentIndex++;
						}
						else
						{
							result.append("\n\n");
							indexWrap = 0;
							currentIndex+=2;
							isNewLineReplaced = false;
						}
					}
					else
					{
						if(!isPrevBlank)
						{
							result.append(' ');
							indexLastBlank = currentIndex;
							indexWrap++;
							currentIndex++;
						}
						
						isNewLineReplaced = true;
						isWrapped = false;
					}
					break;

				case ' ':
					result.append(' ');
					indexLastBlank = currentIndex;
					
					isPrevBlank = true;
					indexWrap++;
					currentIndex++;
					break;

				case '\t': 
					result.append("     ");
					indexLastBlank = currentIndex+4;
	
					isPrevBlank = true;
					indexWrap+=5;
					currentIndex+=5;
					break;

				default:
					result.append(body.charAt(i));
					
					isPrevBlank = false;
					isNewLineReplaced = false;
					indexWrap++;
					currentIndex++;
					break;
			}

			if (indexWrap >= maxLineWidth)
			{
				if(indexLastBlank == 0)
				{
					result.setCharAt(currentIndex, '\n');
					indexWrap = 0;
				}
				else
				{
					result.setCharAt(indexLastBlank, '\n');
					indexWrap = currentIndex-indexLastBlank;
				}

				isWrapped = true;
			}
		}

		return result.toString();
	}

	public static List listFromDelimitedString(String x, String delimiters) {

		List result = new ArrayList();
		
		StringTokenizer tok = new StringTokenizer(x, delimiters);
		while (tok.hasMoreElements()) {
			result.add(tok.nextToken());
		}

		return result;
	}

	public static void main(String args[]) {

		System.out.println(listFromDelimitedString("AB,CD,  ,DEF, GHIJ,,,    KJ", ", "));
	}
}
