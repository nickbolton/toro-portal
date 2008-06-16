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
package net.unicon.academus.apps.rad;

import java.io.*;
import java.util.*;

public class MSVLine extends Vector {
  public MSVLine() {
    super();
  }

  public MSVLine( String msvLine, char separator, boolean quotes) throws Exception {
    super();

    // remove '0D'

    int len = msvLine.length();
    //msvLine = msvLine + "  ";
    char[] line = msvLine.toCharArray();

    int i = 0;
    int startField = 0;
    int endField = startField;
    int comma = -1;
    boolean quote = false;
    while( i < len) {
      if (line[i] == '"') {
        if( line[i+1] != '"')
          quote = !quote;
        else
          i++; // pass over following quote
      }

      if (line[i] == separator && quote == false) {
        endField = i;
        if (quotes)
          addElement(new String(line, startField, endField - startField));
        else
          addElement(toInternal(line, startField, endField - startField));

        // Prepare for next field
        startField = i + 1; // pass over comma
        endField = startField;
      }

      i++;
    }

    // i >= len
    /*
    if (separator != '\n'){
      endField = len;
      addElement(toInternal(line, startField, endField - startField));
    }
    */
    //Dump
    /*
    for( i = 0; i < size(); i++)
    {
      String s = (String)elementAt(i);
      System.out.println(s);
    }
    */
  }

  public static String encodeOutlook(String s) {
    if (s == null || s.length() == 0)
      return s;
    //s.replaceAll("&quot;", "\"");
    s = XMLData.replace(s, "&quot;", "\"");
    if( s.indexOf('"') >= 0)
      s = "\"" + addQuote( s) + "\"";
    else if( s.indexOf('\n') >= 0 || s.indexOf('\t') >= 0)
      s = "\"" + s + "\"";


    return s;
  }

  public String toCsv() {
    String res = "";
    for( int i = 0; i < size(); i++) {
      String s = (String)elementAt(i);
      if( s.indexOf('"') >= 0)
        s = "\"" + addQuote( s) + "\"";
      else if( s.indexOf(',') >= 0)
        s = "\"" + s + "\"";

      if( i > 0)
        res += ",";
      res += s;
    }

    return res;
  }

  protected static String addQuote( String s) {
    String res = "";
    String remain = s;
    int index = remain.indexOf('"');
    while( index >= 0) {
      res += remain.substring(0, index) + "\"\"";
      remain = remain.substring( index + 1);
      index = remain.indexOf('"');
    }

    if( remain != null)
      res += remain;
    return res;
  }

  protected String toInternal( char[] line, int start, int count) {
    if( count == 0)
      return "";

    int outIndex = 0;
    char[] out = new char[count + 1];
    for( int i = 0; i < count; i++) {
      if( line[ start + i] != '"')
        out[outIndex++] = line[ start + i];
      else if( line[ start + i + 1] == '"') {
        out[outIndex++] = '"';
        i++; // pass over next '"'
      }
    }

    String s = new String( out, 0, outIndex);
    return s.trim();
  }
}
