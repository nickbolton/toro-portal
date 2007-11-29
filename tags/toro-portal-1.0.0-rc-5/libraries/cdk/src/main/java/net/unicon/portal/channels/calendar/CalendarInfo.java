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
package net.unicon.portal.channels.calendar;
import net.unicon.portal.channels.rad.Screen;
public class CalendarInfo extends Screen {
  static public final String INFO = "INFO";
  static public final String ERROR = "ERROR";
  static public final String EXCEPTION = "EXCEPTION";
  static public final String SID = "INFO";
  public String sid() {
    return SID;
  }
  public String buildXML() throws Exception {
    StringBuffer xml = new StringBuffer();
    xml.append("<?xml version=\"1.0\"?>");
    xml.append("<info>");
    xml.append("  <icon>" + getParameter("icon") + "</icon>");
    xml.append("  <text>" + getParameter("text") + "</text>");
    xml.append("  <back>" + getParameter("back") + "</back>");
    xml.append("</info>");
    return xml.toString();
  }
  public String getVersion() {
    return CCalendar.VERSION;
  }
  public String getType() {
    return "CCalendar";
  }
}
