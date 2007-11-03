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
import java.text.*;

import net.unicon.academus.apps.calendar.*;
import net.unicon.academus.apps.rad.XMLData;

/**
 * This class provides information to determine how the calendar is showed.
 * The calendar is showed by daily, monthly or weekly
 */
public class CalendarView extends XMLData implements Cloneable {
  String m_mode = "monthly";
  Calendar m_cal = Timetable.getCalendar();

  /**
   * Constructor with given mode.
   * @param mode Mode of view. One of these values is accepted:<br/>
   * daily, monthly, weekly.
   */
  public CalendarView(String mode) {
    m_mode = mode;
    build();
  }

    public int getMonth() {
        // NB:  The java.util.Calendar class numbers months 0-11, but RAD
        // Calendar uses 1-12.  We therefore need to increment by one when
        // we read the month.
        return m_cal.get(Calendar.MONTH) + 1;
    }

    public int getDay() {
        return m_cal.get(Calendar.DAY_OF_MONTH);
    }

    public int getYear() {
        return m_cal.get(Calendar.YEAR);
    }

  /**
   * Make a copy of this class.
   * @return CalendarView object
   * @throws CloneNotSupportedException
   */
  public Object clone() throws CloneNotSupportedException {
    CalendarView clone = (CalendarView)super.clone();
    clone.m_cal = (Calendar)m_cal.clone();
    clone.m_mode = new String(m_mode);
    clone.build();
    return clone;
  }

  /**
   * Set mode for view of calendar.
   * @param mode Mode of view. One of these values is accepted:<br/>
   * daily, monthly, weekly.
   */
  public void setMode(String mode) {
    m_mode = mode;
    build();
  }

  /**
   * Get mode of view.
   * @return Mode of view
   */
  public String getMode() {
    return m_mode;
  }

  /**
   * Add a pair field/value to the view.
   * @param field
   * @param value
   */
  public void add
    (int field, int value) {
    m_cal.add(field, value);
    build();
  }

  /**
   * Set value of field.
   * @param field
   * @param value
   */
  public void set
    (int field, int value) {
    m_cal.set(field, value);
    build();
  }

  /**
   * Get time of view.
   * @return
   */
  public Date getTime() {
    return m_cal.getTime();
  }

  /**
   * Set time for view.
   * @param date
   */
  public void setTime(Date date) {
    m_cal.setTime(date);
    build();
  }

  /**
   * Set time for view.
   * @param date String of date
   * @throws Exception
   */
  public void setTime(String date) throws Exception {
    setTime(XMLData.SDF.parse(date));
  }

  /**
   * Get value of field.
   * @param field
   * @return
   */
  public int get
    (int field) {
    return m_cal.get(field);
  }

  /**
   * Get range of date of view.
   * @return Array of dates to view.
   */
  public Date[] getRange() {
    Date[] range = new Date[2];
    if (m_mode.equals("daily")) {
      range[0] = m_cal.getTime();
      Calendar next = (Calendar)m_cal.clone();
      next.add(Calendar.DATE, 1);
      range[1] = next.getTime();
    }
    if (m_mode.equals("weekly")) {
      Date[] layout = wod(m_cal.getTime());
      range[0] = layout[0];
      Calendar next = Timetable.getCalendar();
      next.setTime(layout[6]);
      next.add(Calendar.DATE, 1);
      range[1] = next.getTime();
    }
    if (m_mode.equals("monthly")) {
      Calendar cal = (Calendar)m_cal.clone();
      cal.set(Calendar.DATE, 1);
      range[0] = cal.getTime();
      cal.add(Calendar.MONTH, 1);
      range[1] = cal.getTime();
    }
    return range;
  }

  void build() {
    clear();
    if (m_mode.equals("monthly")) {
      // <tag date='...' wdo1='...' ldom='...' ldopm='...' title='MMMM yyyy'/>
      putA("date", m_cal.getTime());
      putA("title", (new SimpleDateFormat("MMMM yyyy")).format(m_cal.getTime()));
      int year = m_cal.get(Calendar.YEAR);
      int month = m_cal.get(Calendar.MONTH);
      putA("wdo1", ""+wdo1(year, month));
      putA("ldom", ""+ldom(year, month));
      putA("ldopm", ""+ldopm(year, month));
    } else if (m_mode.equals("weekly")) {
      //<tag date='...' ldom='...' title='MMMM d - MMMM d, yyyy'/>
      SimpleDateFormat sdf1 = new SimpleDateFormat("MMMM d");
      SimpleDateFormat sdf2 = new SimpleDateFormat("MMMM d, yyyy");
      Date[] layout = wod(m_cal.getTime());
      if (yeqy(layout[0], layout[6]))
        putA("title", sdf1.format(layout[0])+" - "+sdf2.format(layout[6]));
      else
        putA("title", sdf2.format(layout[0])+" - "+sdf2.format(layout[6]));
      putA("date", layout[0]);
      putA("ldom", ""+ldom(layout[0]));
    } else if (m_mode.equals("daily")) {
      //<tag date='...' title='EEEE, M/d/yy'/>
      putA("date", m_cal.getTime());
      putA("title", (new SimpleDateFormat("EEEE, M/d/yy")).format(m_cal.getTime()));
    }
  }

  //--------------------------------------------------------------------------//

  static int wdo1(int year, int month) // weekday of the 1st
  {
    Calendar cal = Timetable.getCalendar();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month);
    cal.set(Calendar.DATE, 1);
    return ((cal.get(Calendar.DAY_OF_WEEK)-1)+7)%7;  // change SUNDAY from 1 to 0
  }

  static int ldom(int year, int month) // last date of month
  {
    Calendar cal = Timetable.getCalendar();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month);
    cal.set(Calendar.DAY_OF_MONTH,1);
    cal.setLenient(false);
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
  }

  static int ldom(Date date) // last date of month
  {
    Calendar cal = Timetable.getCalendar();
    cal.setTime(date);
    return ldom(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
  }

  static int ldopm(int year, int month) // last date of previous month
  {
    Calendar cal = Timetable.getCalendar();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month);
    cal.set(Calendar.DATE, 1);
    cal.add(Calendar.DATE, -1);
    return cal.get(Calendar.DATE);
  }

  static int ldopm(Date date) // last date of previous month
  {
    Calendar cal = Timetable.getCalendar();
    cal.setTime(date);
    return ldopm(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
  }

  static boolean yeqy(Date date1, Date date2) // year equals year
  {
    Calendar cal = Timetable.getCalendar();
    cal.setTime(date1);
    int y1 = cal.get(Calendar.YEAR);
    cal.setTime(date2);
    int y2 = cal.get(Calendar.YEAR);
    return y1 == y2;
  }

  static Date[] wod(Date date) // week of date
  {
    Date[] layout = new Date[7];
    Calendar cal = Timetable.getCalendar();
    cal.setTime(date);
    int day = cal.get(Calendar.DAY_OF_WEEK);
    day = ((day-1)+7)%7;  // change SUNDAY from 1 to 0
    layout[day] = date;
    for (int i = 1; day+i < 7; i++) {
      cal.add(Calendar.DATE, 1);
      layout[day+i] = cal.getTime();
    }
    cal.setTime(date);
    for (int i = -1; day+i >= 0; i--) {
      cal.add(Calendar.DATE, -1);
      layout[day+i] = cal.getTime();
    }
    return layout;
  }

  //--------------------------------------------------------------------------//

  /**
   * Get String of day (of Week) from Date object.
   * @param date
   * @return (0 = Sunday, 1 = Monday, 2 = Tuesday, 3 = Wednesday, 4 = Thursday, 5 = Friday, 6 = Saturday)
   * @throws Exception
   */
  public static String dayOfWeek(Date date) throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? "Sunday" : cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY ? "Monday"
           : cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY ? "Tuesday"
           : cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY ? "Wednesday"
           : cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY ? "Wednesday"
           : cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY ? "Thursday"
           : cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY ? "Friday"
           : cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ? "Saturday"
           : "error in find DOW";


  }


  /**
   * Get the first day of given week, month and year. It's Sunday.
   * @param weekOfMonth
   * @param month
   * @param year
   * @return
   * @throws Exception
   */
  public static Date dayOfWeek(int weekOfMonth, int month, int year) throws Exception {
    //Calendar cal = Calendar.getInstance();
    Calendar cal = Timetable.getCalendar();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month-1);
    cal.set(Calendar.WEEK_OF_MONTH, weekOfMonth);
    cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

    return cal.getTime();
  }

  /**
   * Get week of date.
   * @param date
   * @return
   * @throws Exception
   */
  public static int weekOfDate(Date date) throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);

    return cal.get(Calendar.WEEK_OF_MONTH);
  }

}
