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

import java.sql.*;
import java.text.*;

import java.util.Date;

/**
 * The helper class, provides the static methods used when create the SQL queries.
 */
public class SQL {
  //------ Some useful constants --------------------------------//
  public static final String EMPTY = "";
  public static final String NULL = "NULL";
  public static final String TRUE = "Y";
  public static final String FALSE = "N";

  static final DateFormat m_time = new SimpleDateFormat("HH:mm:ss");
  static final DateFormat m_date = new SimpleDateFormat("yyyy-MM-dd");

  public static String CONCAT = "||";

  //-------------------------------------------------------------//

  /**
   * Convert string to used with the LIKE operator of SQL.
   * @param s String to convert
   * @return String that can be used with the LIKE.
   */
  public static String like(String s) {
    return "'%" + quot(s) + "%'";
  }

  /**
   * Convert a byte to String used in the SQL query.
   * @param b byte to convert
   * @return String that can be used in the SQL query.
   */
  public static String esc(byte b) {
    return Byte.toString(b);
  }

  /**
   * Convert an int number to String used in the SQL query.
   * @param i int number to convert
   * @return String that can be used in the SQL query.
   */
  public static String esc(int i) {
    return Integer.toString(i);
  }

  /**
   * Convert a long number to String used in the SQL query.
   * @param l long number to convert
   * @return String that can be used in the SQL query.
   */
  public static String esc(long l) {
    return Long.toString(l);
  }

  /**
   * Convert a float number to String used in the SQL query.
   * @param f float number to convert
   * @return String that can be used in the SQL query.
   */
  public static String esc(float f) {
    return Float.toString(f);
  }

  /**
   * Convert a double number to String used in the SQL query.
   * @param d double number to convert
   * @return String that can be used in the SQL query.
   */
  public static String esc(double d) {
    return Double.toString(d);
  }

  /**
   * Convert a boolean value to String used in the SQL query.
   * @param b boolean value to convert
   * @return String that can be used in the SQL query.
   */
  public static String esc(boolean b) {
    return b?esc(TRUE):esc(FALSE);
  }

  /**
   * Escapes a string so that it can be used in the SQL query.
   * @param s string to be escape.
   * @return String that can be used in the SQL query.
   */
  public static String esc(String s) {
    if( s == null || s.length() == 0)
      return NULL;
    else
      return "'" + quot(s) + "'";
  }

  /**
   * Convert a date value to String used in the SQL query.
   * @param d date value to convert
   * @return String that can be used in the SQL query.
   */
  public static String esc(java.util.Date d) {
    if( d == null)
      return NULL;
    else
      return "{d '" + m_date.format(d) + "'}";
  }

  /**
   * Convert a Timestamp value to String used in the SQL query.
   * @param d Timestamp value to convert
   * @return String that can be used in the SQL query.
   */
  public static String esc(Timestamp d) {
    if( d == null)
      return NULL;
    else
      return "{ts '" + d.toString() + "'}";
  }

  /**
   * Convert a Time value to String used in the SQL query.
   * @param d Time value to convert
   * @return String that can be used in the SQL query.
   */
  public static String esc(Time d) {
    if( d == null)
      return NULL;
    else
      return "{t '" + m_time.format(d) + "'}";
  }

  /**
   * Create a list of Strings separeted by comma. It is intended to used in the IN clause of SQL.
   * @param items an array of String to be create a list.
   * @return String in the format of list of strings separeted by comma.
   */
  public static String list( String[] items) {
    String ret = "";
    for( int i = 0; i < items.length; i++)
      ret += (i==0?"":",") + esc(items[i]);
    return ret;
  }

  /**
   * Create a list of Identifier Strings separeted by comma. It is intended to used in the IN clause of SQL.
   * @param items an array of IdentityData to be create a list.
   * @return String in the format of list of identifier strings separeted by comma.
   */
  public static String listIds( IdentityData[] items) {
    String ret = "";
    for( int i = 0; i < items.length; i++)
      ret += (i==0?"":",") + esc(items[i].getIdentifier());
    return ret;
  }

  //-------------------------------------------------------------//

  /**
   * Double quote character.
   */
  public static String quot(String s) {
    if (s == null || s.trim().equals(""))
      return s;
    // Double single quot character
    int k = -1;
    int beg = -1;
    String str1="",str2="";
    while ((k = s.indexOf("'", ++beg)) > -1) {
      s = s.substring(0, k) + "'" + s.substring(k);
      beg = ++k;
    }
    return s;
  }
}
