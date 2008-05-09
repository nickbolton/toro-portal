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

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Helper class for escaping a String or a Date so that they are valid in the xml stream.
 */
public class XML {
  /**
   * General date format in the xml stream
   */
  public static SimpleDateFormat m_dateFormat = new java.text.SimpleDateFormat("MM/dd/yy hh:mm a");

  /**
   * Escapes a Date object
   * @param d Date to be escaped
   * @return an escaped String
   */
  public static String esc(Date d) {
    if( d == null)
      return "";
    else
      return m_dateFormat.format(d).toLowerCase();
  }

  /**
   * Escapes an XML string
   * @param source a String to be escaped
   * @return an escaped String
   */
  public static String esc(String source) {
    if( source == null)
      return "";

    StringBuffer sb = new StringBuffer (source.length () + 256);
    for (int i = 0 ; i < source.length() ; i++) {
      char ch = source.charAt (i);
      switch (ch) {
      case '<':
        sb.append ("&lt;");
        break;
      case '>':
        sb.append ("&gt;");
        break;
      case '"':
        sb.append ("&quot;");
        break;
      case '\'':
        sb.append ("&apos;");
        break;
      case '&':
        sb.append ("&amp;");
        break;
      default:
        if (( ch >= ' ' && ch <= 0x7E && ch != 0xF7 ) ||
            ch == '\n' || ch == '\r' || ch == '\t' )
          sb.append (ch);
        else if (ch<' ')
          sb.append(' ');
        else
          sb.append ("&#" + Integer.toString (ch) + ";");
        break;
      }
    }

    return sb.toString();
  }
}
