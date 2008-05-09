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

import java.lang.*;
import java.io.*;
import java.util.*;

import net.unicon.academus.apps.calendar.*;
import net.unicon.academus.apps.rad.XMLData;

/**
 * This class provides information to show a entry
 */
public class EntryView extends XMLData implements Cloneable {

  /**
   * Default constructor
   */
  public EntryView() {
    putA("date", Timetable.getCalendar().getTime());
  }

  /**
   * Make a copy of EntryView
   * @return
   * @throws CloneNotSupportedException
   */
  public Object clone() throws CloneNotSupportedException {
    EntryView clone = (EntryView)super.clone();
    clone.putA("date", getA("date"));
    return clone;
  }

  /**
   * Set date to view entry
   * @param date Date Object
   */
  public void putDate(Date date) {
    putA("date", date);
  }

  /**
   * Set date to view entry
   * @param date String of Date
   * @throws Exception
   */

  public void putDate(String date) throws Exception {
    Calendar cal = Timetable.getCalendar();
    cal.setTime(XMLData.SDF.parse(date));
    putA("date", cal.getTime());
  }

  /**
   * Set hour to view entry
   * @param hour
   */
  public void putHour(String hour) {
    Date date = (Date)getA("date");
    if (date == null)
      return;
    Calendar cal = Timetable.getCalendar();
    cal.setTime(date);
    cal.set(Calendar.HOUR, Integer.parseInt(hour.trim()));
    putA("date", cal.getTime());
  }

  /**
   * Set calendar entry identifier
   * @param ceid
   */
  public void putCeid(String ceid) {
    putA("ceid", ceid);
  }

  /**
   * Get calendar entry identifer
   * @return
   */
  public String getCeid() {
    return (String)getA("ceid");
  }
}
