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

import org.apache.oro.text.perl.*;
import org.apache.oro.text.regex.*;

import java.util.*;
import java.io.File;
import java.io.IOException;

/**
 * Provides a template with named or numbered variables that can be replaced
 * by given arguments. An argument set may be in the form of an
 * <code>Object</code> array, a <code>Map</code>, a <code>List</code>, a
 * <code>ResultSet</code>, or a <code>Map</code> combined with a
 * <code>ResultSet</code>. Any argument not specified in the argument set will
 * be replaced with a default value. Also, instead of an argument set, a single
 * <code>Object</code> may be inserted in place of all arguments in the
 * template.<p>
 *
 * Static methods are provided for formatting given a template
 * <code>String</code> and an argument set.<p>
 *
 * @author Unicon, Inc.
 * @version 3.0
 *
 * @see java.text.MessageFormat */
public class PrintFormat 
    // Henri questions use of String vs. StringBuffer
{
    private String pattern = null;
    private Perl5Util perl = new Perl5Util();
    private String defaultValueName = "DEFAULT_VALUE";

    private ArrayList elements;
    private int size;
    private String[] names;
    private String defaultValue = "";
    // added 4/29/02 by KG,AC to ensure a decent guess at SB size
    private int formatLength = 10;
             
    /**
     * Creates a new <code>PrintFormat</code> with a specified format and
     * curly-brace delimiters
     *
     * @param format a buffer containing the template for this
     * <code>PrintFormat</code> */
    public PrintFormat(StringBuffer format)
    {
        this(format.toString(), "{", "}");
    }

    /**
     * Creates a new <code>PrintFormat</code> with a specified format and
     * curly-brace delimiters
     *
     * @param format the template for this <code>PrintFormat</code> */
    public PrintFormat(String format)
    {
        this(format, "{", "}");
    }

    /**
     * Creates a new <code>PrintFormat</code> with the format contained in a
     * specified file and curly-brace delimiters
     *
     * @param format a file containing a template
     *
     * @exception IOException if an error occurs while reading the file */
    public PrintFormat(File format)
        throws IOException
    {
            this(FileUtil.readFile(format).toString());
    }

    /**
     * Creates a new <code>PrintFormat</code> with a specified format and
     * specified delimiters indicating the beginning and end of argument names
     * (by default, these delimiters are curly braces: {})
     *
     * @param format a buffer containing the template for this
     * <code>PrintFormat</code>
     * @param delimiterStart the character used to indicate the beginning of
     * an argument name
     * @param delimiterEnd the character used to undicate the end of an
     * argument name */
    public PrintFormat(    StringBuffer format,
                        String delimiterStart,
                        String delimiterEnd)
    {
        this(format.toString(), delimiterStart, delimiterEnd);
    }

    /**
     * Creates a new <code>PrintFormat</code> with a specified format and
     * specified delimiters indicating the beginning and end of argument names
     * (by default, these delimiters are curly braces: {})
     *
     * @param format the template for this <code>PrintFormat</code>
     * @param delimiterStart the character used to indicate the beginning of
     * an argument name
     * @param delimiterEnd the character used to indicate the end of an 
     * argument name */
    public PrintFormat(    String format,
                        String delimiterStart,
                        String delimiterEnd)
    {
        final int SIZE_GUESS = 10;

        formatLength = format.length();

        //escape the delimiters, just in case
        StringBuffer patternBuffer = new StringBuffer(30);
        patternBuffer.append("/(^|[^");
        patternBuffer.append(delimiterStart);
        patternBuffer.append("])");
        patternBuffer.append(delimiterStart);
        patternBuffer.append("([^\\s");
        patternBuffer.append(delimiterEnd);
        patternBuffer.append(delimiterStart);
        patternBuffer.append("]*)");
        patternBuffer.append(delimiterEnd);
        patternBuffer.append("(?!");
        patternBuffer.append(delimiterEnd);
        patternBuffer.append(")/m");
        pattern = patternBuffer.toString();

        //We have to replace pairs of delimiters by single delimiters
        Perl5Util subber = new Perl5Util();

        patternBuffer = new StringBuffer(10);
        patternBuffer.append("s/");
        patternBuffer.append(delimiterStart);
        patternBuffer.append(delimiterStart);
        patternBuffer.append("/");
        patternBuffer.append(delimiterStart);
        patternBuffer.append("/g");
        String startSub = patternBuffer.toString();

        patternBuffer = new StringBuffer(10);
        patternBuffer.append("s/");
        patternBuffer.append(delimiterEnd);
        patternBuffer.append(delimiterEnd);
        patternBuffer.append("/");
        patternBuffer.append(delimiterEnd);
        patternBuffer.append("/g");
        String endSub = patternBuffer.toString();

        //String startSub = "s/" + delimiterStart + delimiterStart + "/" + delimiterStart + "/g";
        //String endSub = "s/" + delimiterEnd + delimiterEnd + "/" + delimiterEnd + "/g";
        
        elements = new ArrayList(SIZE_GUESS);
        
        //Attempts to use PatternMatcher to hold state, rather than resetting the input
        //to postMatch on each iteration, seem fail on two consecutive delimited strings
        //(e.g "{NAME}{NAME}".  
        boolean matched = false;
        for (String input=format;  perl.match(pattern,input);  input=perl.postMatch()) {
            matched = true;
            final int GROUP_NUMBER = 2;  //number of group containing name/number to be replaced
            //substring *include* the first (non-startDelimiter) character in the match
            String between = input.substring(0,perl.beginOffset(GROUP_NUMBER)-1);
            between = subber.substitute(startSub, between);
            between = subber.substitute(endSub, between);
            elements.add(between);
            elements.add(perl.group(GROUP_NUMBER));
        }

        String postMatch = (matched ? perl.postMatch() : format);
        postMatch = subber.substitute(startSub, postMatch);
        postMatch = subber.substitute(endSub, postMatch);
        elements.add(postMatch);
        
        size = elements.size();
        names = new String[size];

        for (int i=0; i < size; i++) {
            if (i%2 == 1){
                names[i] = (String)elements.get(i);
            }
        }
    }

    /**
     * Sets the default value for an argument in case its value is not
     * specified in the list sent to <code>format</code>
     *
     * @param value the new default argument value. The initial default value
     * is the empty String (""). */
    public void setDefaultValue(String value)
    {
        defaultValue = value;
    }

    /**
     * Returns the default value used for an argument whose value is not
     * specified
     *
     * @return the default argument value */
    public String getDefaultValue()
    {
        return defaultValue;
    }

    /**
     * Populates this <code>PrintFormat</code>'s template with a given set of
     * values. Each argument is replaced by its corresponding value in the
     * given <code>Map</code>. If any argument is missing from
     * <code>args</code>, it is replaced by the value from the map whose key
     * is <code>DEFAULT_VALUE</code>. If no such entry exists, the missing
     * argument is replaced by the default value of this
     * <code>PrintFormat</code>.
     *
     * @param args the arguments to be entered into the template
     *
     * @return the template, filled in with the arguments in <code>args</code>
     * 
     * @see #format(Object[])
     * @see #format(List)
     * @see #format(Map)
     * @see #format(Map, java.sql.ResultSet)
     * @see #format(Object)
     * @see #format(String, Object[])
     * @see #format(String, List)
     * @see #format(String, Map)
     * @see #format(String, Object) 
     *
     * @see #setDefaultValue(String)
     * @see #getDefaultValue() */
    public String format(Map args)
    {
        String localDefault = (String) args.get(defaultValueName);
        if (localDefault == null)
            localDefault = defaultValue;

        StringBuffer result = new StringBuffer(formatLength + formatLength / 4); // legacy guess
        
        for (int i=0; i < size; i++)
        {
            if (i%2 == 0)
                result.append(elements.get(i));
            else
            {
                Object value = args.get(names[i]);
                if (value == null)
                    result.append(localDefault);
                else
                    result.append(value.toString());
            }
        }
        return result.toString();
    }

    /**
     * Populates the template with values from a given <code>Map</code> and
     * <code>ResultSet</code>. If an argument is in the map, <code>format(Map,
     * ResultSet)</code> will <b>not</b> look for it in the result
     * set.
     *
     * @param args a <code>Map</code> of arguments to be entered into the
     *             template 
     * @param rs   a set of arguments to be entered into the template if not
     *             found in <code>args</code>
     *
     * @return the template, filled in with the arguments in <code>args</code>
     * snd <code>rs</code>
     *
     * @exception java.sql.SQLException if an error occurs while reading from
     * the database
     * 
     * @see #format(Object[])
     * @see #format(List)
     * @see #format(Map)
     * @see #format(Map, java.sql.ResultSet)
     * @see #format(Object)
     * @see #format(String, Object[])
     * @see #format(String, List)
     * @see #format(String, Map)
     * @see #format(String, Object) */
    public String format(Map args, java.sql.ResultSet rs) throws java.sql.SQLException
    {
        StringBuffer result = new StringBuffer(formatLength + formatLength / 4); // legacy guess
        
        for (int i=0; i < size; i++)
        {

            if (i%2 == 0) {
                result.append(elements.get(i));
            } else {
                
                //result.append(args.get(elements.get(i)).toString());
                String element = null;
                
                Object value = args.get(names[i]);
                if (value != null)
                    element = value.toString();

                if (element == null)
                    element = rs.getString(names[i]);

                if (element == null)
                    element = defaultValue;

                result.append(element);
            } 
            
        }
        return result.toString();
    }

    /**
     * Replaces all arguments in the template with a single Object, including
     * all occurrences of the asterisk (<code>*</code>)
     *
     * @param arg the value to be entered into the template
     *
     * @return the template, with all variables replaced by <code>arg</code>
     * 
     * @see #format(Object[])
     * @see #format(List)
     * @see #format(Map)
     * @see #format(Map, java.sql.ResultSet)
     * @see #format(Object)
     * @see #format(String, Object[])
     * @see #format(String, List)
     * @see #format(String, Map)
     * @see #format(String, Object) */
    public String format(Object arg)
    {
        StringBuffer result = new StringBuffer(formatLength + formatLength / 4); // legacy guess
        
        for (int i=0; i < size; i++)
        {
            if (i%2 == 0)
                result.append(elements.get(i));
            else
                result.append(arg.toString());
        }
        return result.toString();
    }

    /**
     * A static version of {@link #format(Map)}, where the template to be
     * populated is sent as a parameter
     *
     * @param format the template to be populated
     * @param args the arguments to be entered into the template
     *
     * @return <code>format</code>, filled in with the arguments in <code>args</code>
     * 
     * @see #format(Object[])
     * @see #format(List)
     * @see #format(Map)
     * @see #format(Map, java.sql.ResultSet)
     * @see #format(Object)
     * @see #format(String, Object[])
     * @see #format(String, List)
     * @see #format(String, Map)
     * @see #format(String, Object) */
    public static String format(String format, Map args)
    {
        return (new PrintFormat(format)).format(args);
    }

    /**
     * A static version of {@link #format(Object)}, where the template to be
     * populated is sent as a parameter
     *
     * @param format the template to be populated
     * @param arg the value to be entered into the template
     *
     * @return <code>format</code>, with all variables replaced by <code>arg</code>
     * 
     * @see #format(Object[])
     * @see #format(List)
     * @see #format(Map)
     * @see #format(Map, java.sql.ResultSet)
     * @see #format(Object)
     * @see #format(String, Object[])
     * @see #format(String, List)
     * @see #format(String, Map)
     * @see #format(String, Object) */
    public static String format(String format, Object arg)
    {
        return (new PrintFormat(format)).format(arg);
    }

    // Return this PrintFormat as a string, with any occurance of {<argname>} 
    // unchanged
    public String format()
    {
        StringBuffer result = new StringBuffer(formatLength + formatLength / 4); // legacy guess

        for (int i=0; i < size; i++) {
            if (i%2 == 0)
                result.append(elements.get(i));
            else {
                // The curly braces got stripped out above, so put them back in
                result.append("{");
                result.append(elements.get(i));
                result.append("}");
            }
        }
        return result.toString();
    }

    public static void main(String[] args) throws Exception
    {
        String formatFileName = args[0];
        int num = (args.length-1) / 2;
        String[] keys = new String[num];
        String[] values = new String[num];

        for (int i=1; i < args.length; i+=2)
        {
            keys[(i-1) / 2] = args[i];
            values[((i-1) / 2)] = args[i+1];
        }

        Map map = new HashMap(10);
        for (int i=0; i < keys.length; i++)
        {
            map.put(keys[i],values[i]);
        }

        PrintFormat pf = new PrintFormat(new File(formatFileName));
        System.out.println(pf.format(map));
    }
}
