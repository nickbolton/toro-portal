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

import java.io.*;
import java.util.*;
import java.text.*;

import net.unicon.academus.apps.calendar.*;
import net.unicon.academus.apps.rad.XMLData;

/**
 * This class represents a table of time to make a layout for calendar
 */
public class Timetable {
  static public final int BOD = 8;
  static public final int EOD = 18;

  /**
   * Get an instance of calendar
   * @return
   */
  static public Calendar getCalendar() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    return cal;
  }

  /**
   * Get an instance of calendar from given date
   * @param d
   * @return Calendar that is the end of the day.
   */
  static public Calendar lastDayTime(Date d) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    return cal;
  }

  Vector m_allday = new Vector();
  Vector m_timed = new Vector(); // Vector of columns
  int m_first = -1;
  int m_last = -1;

  /**
   * Make layout for entries
   * @param date
   * @param entries
   * @throws Exception
   */
  public void layout(Date date, EntryData[] entries) throws Exception {
    // truong DPCS 4/4/02
    if (entries != null)
      for (int i = 0; i < entries.length; i++)
        layout(date, entries[i]);
  }

  /**
   * Make layout for calendars
   * @param date
   * @param cals
   * @throws Exception
   */
  public void layout(Date date, CalendarData[] cals) throws Exception {
    //Truong 11_8

    //build vector contains entries of all calendars
    Vector entv = new Vector();
    //truong DPCS 4/08/02
    if (cals != null && cals.length > 0)
      for (int i = 0; i < cals.length; i++) {
        EntryData[] entr = cals[i].getEntry();
        if (entr != null && entr.length > 0)
          for (int j = 0; j < entr.length; j++) {
            if (entr[j] != null)
              entv.addElement(entr[j]);
          }
      }

    //if have entries that have one ceid, we only keep one
    for (int i = 0; i < entv.size(); i++) {
      for (int j = 0; j < entv.size(); j++) {
        if (i!=j && ((EntryData)entv.elementAt(i)).getCeid().equals(((EntryData)entv.elementAt(j)).getCeid()) )
          entv.removeElementAt(j);
      }
    }
    // Sort
    CalendarServer.sort(entv);

    EntryData[] entr = new EntryData[entv.size()];
    for (int i = 0; i < entv.size(); i++)
      entr[i] = (EntryData)entv.elementAt(i);

    //build slot
    layout(date, entr);
  }

  void layout(Date date, EntryData entry) throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy");
    if (!sdf.format(date).equals(sdf.format(entry.getDuration().getStart())))
      return;

    // Entry la Todo
    if (entry.isTodo()) {
      int col = 0;
      while (!layout(entry, col++))
        ;
    }

    // Event data
    else {
      if (entry.getDuration().getLength().getAllDay())
        m_allday.addElement(entry);
      else {
        int col = 0;
        while (!layout(entry, col++))
          ;
      }
    }
  }

  Vector[] getTrack(int col) {
    Vector[] track = null;

    if (col < m_timed.size())
      track = (Vector[])m_timed.elementAt(col);
    else // add new column
    {
      track = new Vector[24];
      for (int i = 0; i < 24; i++)
        track[i] = null;
      m_timed.addElement(track);
    }
    return track;
  }

  boolean layout(EntryData entry, int col) throws Exception {
    // Get track, grow if needed
    Vector[] track = getTrack(col);

    // get start time of entry
    Date start = entry.getDuration().getStart();
    Calendar timeStart = getCalendar();
    timeStart.setTime(start);
    int hourStart = timeStart.get(Calendar.HOUR_OF_DAY);

    // End time of entry
    Calendar timeEnd = getCalendar();
    timeEnd.setTime(entry.getDuration().getEnd());
    int hourEnd = timeEnd.get(Calendar.HOUR_OF_DAY);
    hourEnd = timeEnd.after(lastDayTime(start))? 23:
              (timeEnd.get(Calendar.MINUTE) == 0)? hourEnd-1:hourEnd;

    // layout
    if (layout(entry, track, hourStart)) {
      for (int i = hourStart + 1; i <= hourEnd; i++)
        if (!layout(entry, track, i))
          throw new Exception(CCalendar.ERROR_EVENTS_NOT_SORTED);
      return true;
    }

    // Conflict, add to next column
    return layout(entry, col+1);
  }

  boolean layout(EntryData entry, Vector[] track, int row) {
    Vector slot = track[row];

    // Grow track range
    if (m_first == -1 || m_first > row)
      m_first = row;
    if (m_last == -1 || m_last < row)
      m_last = row;

    // if there are no entries, OK
    if (slot == null) {
      slot = new Vector();
      slot.add(entry);
      track[row] = slot;
      return true;
    }

    // Check conflict
    EntryData last = (EntryData)slot.lastElement();
    Calendar t1 = getCalendar();
    t1.setTime(last.getDuration().getEnd()); // End time of last entry
    Calendar t2 = getCalendar();
    t2.setTime(entry.getDuration().getStart()); // Start time of entry
    if (t1.before(t2) || t1.equals(t2)) {
      slot.add(entry);
      return true;
    } else
      return false;
  }

  int extended(int i, int j) {
    Vector slot0 = ((Vector[])m_timed.elementAt(j))[i-1];
    if (slot0 == null)
      return 0;
    String e0 = (String)((EntryData)slot0.lastElement()).getA("ceid");
    Vector slot1 = ((Vector[])m_timed.elementAt(j))[i];
    for (int k = 0; k < slot1.size(); k++) {
      String e1 = (String)((EntryData)slot1.elementAt(k)).getA("ceid");
      if (!e1.equals(e0))
        return k;
    }
    return -1;
  }

  int extending(int i, int j) {
    Vector slot1 = ((Vector[])m_timed.elementAt(j))[i];
    String e1 = (String)((EntryData)slot1.lastElement()).getA("ceid");
    for (int k = 1;; k++) {
      if (((Vector[])m_timed.elementAt(j)).length <= i+k) // Thach Aug 11
        return k;

      Vector slot2 = ((Vector[])m_timed.elementAt(j))[i+k];
      if (slot2 == null)
        return k;

      for (int m = 0; m < slot2.size(); m++) {
        String e2 = (String)((EntryData)slot2.elementAt(m)).getA("ceid");
        if (!e1.equals(e2))
          return k;
      }
    }
  }

  void build0(Vector slotv, int i, int j) {
    if (j == 0 || ((Vector[])m_timed.elementAt(j-1))[i] != null) {
      int width = 1;
      for (; j+width < m_timed.size() && ((Vector[])m_timed.elementAt(j+width))[i] == null; width++)
        ;
      XMLData slot = new XMLData();
      slot.putA("row", "1");
      slot.putA("col", ""+width);
      slotv.addElement(slot);
    }
  }

  void build1(Vector slotv, int i, int j) {
    int m = 0;
    if (i== 0 || (m=extended(i, j)) >= 0) // Thach Aug 11
    {
      Vector list = ((Vector[])m_timed.elementAt(j))[i];
      XMLData[] entr = new XMLData[list.size() - m];
      for (int k = m; k < list.size(); k++) {
        entr[k - m] = new XMLData();
        entr[k - m].putA("ceid", ((EntryData)list.elementAt(k)).getA("ceid"));
      }

      XMLData slot = new XMLData();
      slot.putA("row", ""+extending(i, j));
      slot.putA("col", "1");
      slot.putE("entry", entr);
      slotv.addElement(slot);
    }
  }

  public void build(XMLData data) {
    data.putE("all-day",null);
    int width = m_timed.size();
    if (m_allday.size() > 0) {
      XMLData allday = new XMLData();
      allday.putA("col", new Integer(width > 0? width:1));
      XMLData entry[] = new XMLData[m_allday.size()];
      for (int i = 0; i < m_allday.size(); i++) {
        entry[i] = new XMLData();
        entry[i].putA("ceid", ((EntryData)m_allday.elementAt(i)).getA("ceid"));
      }
      allday.putE("entry",entry);
      data.putE("all-day", allday);
    }

    int first = (m_first == -1 || m_first > BOD)? BOD:m_first;
    int last = (m_last == -1 || m_last < EOD)? EOD:m_last;
    XMLData[] hourr = new XMLData[last-first+1];
    for (int i = first; i <= last; i++) {
      hourr[i-first] = new XMLData();
      hourr[i-first].putA("hid", ""+i);
      Vector slotv = new Vector();
      if (width == 0) {
        XMLData slot = new XMLData();
        slot.putA("row", "1");
        slot.putA("col", "1");
        slotv.addElement(slot);
      } else {
        for (int j = 0; j < width; j++) {
          if (((Vector[])m_timed.elementAt(j))[i] == null)
            build0(slotv, i, j);
          else
            build1(slotv, i, j);
        }
      }
      XMLData[] slotr= new XMLData[slotv.size()];
      for (int j = 0; j < slotr.length; j++)
        slotr[j] = (XMLData)slotv.elementAt(j);
      hourr[i-first].putE("slot", slotr);
    }
    data.putE("hour", hourr);
  }
}
