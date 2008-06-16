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



public class TDLine extends Vector {

  public TDLine() {}



  public TDLine(String[] inputs) {

    if (inputs != null)

      for (int i = 0; i < inputs.length; i++)

        addElement( inputs[i]);

  }



  public TDLine(InputStream is, int separator, int skipChar/*, boolean quotes*/) throws

    Exception {


    StringBuffer sb = new StringBuffer();

    boolean quote = false;

    int b = is.read();
    while ( b != -1) {
      // read next
      int next = is.read();

      // quote character
      if (b == '"') {
        // Check double quote: is single quote
        if( next == '"') {
          sb.append( (char)b);
          next = is.read();
        }
        else
          quote = !quote;
      }

      // Separator
      else if (b == separator && quote == false) {
        addElement(sb.toString());
        sb = new StringBuffer();
      }

      // Other characters
      else if (b != skipChar)
        sb.append( (char) b);

      // Check next
      b = next;
    }

    // Last field
    addElement(sb.toString());
  }

  public static String encodeOutlook(String s) {

    if (s == null || s.length() == 0)

      return s;

    s = XMLData.replace(s, "&quot;", "\"");

    if (s.indexOf('"') >= 0)

      s = "\"" + addQuote(s) + "\"";

    else if (s.indexOf('\n') >= 0 || s.indexOf('\t') >= 0)

      s = "\"" + s + "\"";



    return s;

  }



  public String toCsv(char separator) {

    String res = "";

    for (int i = 0; i < size(); i++) {

      String s = (String) elementAt(i);

      if (s == null)

        ;

      else if (s.indexOf('"') >= 0)

        s = "\"" + addQuote(s) + "\"";

      else if (s.indexOf(separator) >= 0)

        s = "\"" + s + "\"";



      if (i > 0)

        res += separator;

      res += s;

    }



    return res;

  }



  protected static String addQuote(String s) {

    String res = "";

    String remain = s;

    int index = remain.indexOf('"');

    while (index >= 0) {

      res += remain.substring(0, index) + "\"\"";

      remain = remain.substring(index + 1);

      index = remain.indexOf('"');

    }



    if (remain != null)

      res += remain;

    return res;

  }



  protected String toInternal(char[] line, int start, int count) {

    if (count == 0)

      return "";



    int outIndex = 0;

    char[] out = new char[count + 1];

    for (int i = 0; i < count; i++) {

      if (line[start + i] != '"')

        out[outIndex++] = line[start + i];

      else if (line[start + i + 1] == '"') {

        out[outIndex++] = '"';

        i++; // pass over next '"'

      }

    }



    String s = new String(out, 0, outIndex);

    return s.trim();

  }



  //--------------------------------------------------//

  /**

   * @param <code>InputStream</code>

   * @return <code>Hashtable</code>

   * @throws Exception

   */



  public static TDLine[] loadDataFromDelimitedFile(InputStream in,

      int separator) throws Exception {

    TDLine lines = new TDLine(in, '\n', '\r'/*, true*/);

    TDLine[] ret = new TDLine[lines.size()];

    for (int j = 0; j < lines.size(); j++) {
      String line = (String) lines.elementAt(j);
      ret[j] = (new TDLine(new ByteArrayInputStream(line.getBytes()), separator, -1/*, false*/));
    }

    return ret;

  }



  static public void main(String args[]) {

    String fn = "D:\\temp\\jason.txt";

    try {

      InputStream in = new FileInputStream(fn);

      TDLine[] all = loadDataFromDelimitedFile(in, '\t');

      for (int i = 0; i < all.length; i++) {

        System.out.println(all[i].toCsv('\t'));

      }

    } catch (Exception e) {

      e.printStackTrace();

    }

  }

}

