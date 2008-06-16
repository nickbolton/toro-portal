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

package net.unicon.academus.apps.calendar;

import java.util.*;

import net.unicon.academus.apps.rad.XMLData;

/**
 * This class represents Calendar. It's xml-based object.
 * For each user, there is always a default calendar called personal calendar.
 * A user can create his/her own calendars and can share these ones to others.
 */
public class CalendarData extends XMLData {
  // <calendar calid='calid' owner='owner' created='created' last-modified='last-modified'>
  //   <ace>...<\ace>
  //   <entry>...<\entry>
  //   <freebusy>...<\freebusy>
  // <\calendar>

  /**
   * Get identifer of calendar.
   * @return
   */
  public String getCalid() {
    return (String)getA("calid");
  }

  /**
   * Set identifer to calendar.
   * @param calid
   */
  public void putCalid(String calid) {
    putA("calid", calid);
  }

  /**
   * Get owner of calendar. Owner is creator of calendar.
   * @return
   */
  public String getOwner() {
    return (String)getA("owner");
  }

  /**
   * Set owner of calendar.
   * @param owner
   */
  public void putOwner(String owner) {
    putA("owner", owner);
  }

  /**
   * Get name of Calendar
   * @return
   */
  public String getCalname() {
    return (String)getA("calname");
  }

  /**
   * Set name of Calendar.
   * @param calname
   */
  public void putCalname(String calname) {
    putA("calname", calname);
  }

  /**
   * Get created-date of Calendar.
   * @return
   */
  public Date getCreated() {
    return (Date)getA("created");
  }

  /**
   * Get lastest mofified-date of Calendar.
   */
  public Date getLastModified() {
    return (Date)getA("last-modified");
  }

  /**
   * Get list of Access Control Entries of Calendar.
   * @return
   */
  public ACEData[] getACE() {
    return (ACEData[])getE("ace");
  }

  /**
   * Set list of ACE for Calendar.
   * @param ace
   */
  public void putACE(ACEData[] ace) {
    putE("ace", ace);
  }

  /**
   * Get list of entries of calendar.
   * @return
   */
  public EntryData[] getEntry() {
    return (EntryData[])getE("entry");
  }

  /**
   * Set entries for calendar.
   * @param ace
   */
  public void putEntry(EntryData[] ace) {
    putE("entry", ace);
  }

  /**
   * Get list of free durations.
   * @return
   */
  public DurationData[] getFreebusy() {
    return (DurationData[])getE("freebusy");
  }

  /**
   * Set list of free durations.
   * @param ace
   */
  public void putFreebusy(DurationData[] ace) {
    putE("freebusy", ace);
  }

  /**
   * Check whether this is own user logon's calendar or not.
   * @return
   */
  public boolean isPersonal() {
    String owner = getOwner(), calid = getCalid();
    return (owner != null && calid != null && owner.equals(calid));
  }

  /**
   * Check whether given user is owner of the calendar.
   * @param user
   * @return
   */

  public boolean isOwner(String user) {
    return user.equals(getOwner());
  }
  /**
   * Only clone properties of calendar. Not clone entries
   */
  /*  public Object clone() throws CloneNotSupportedException
    {
      CalendarData cal = new CalendarData();
      if (getCalid() != null) cal.putCalid(getCalid());
      if (getCalname() != null) cal.putCalname(getCalname());
      if (getOwner() != null) cal.putOwner(getOwner());
      if (getCreated() != null) cal.putA("created", getCreated());
      if (getLastModified() != null) cal.putA("last-modified", getLastModified());
      //Truyen 04-25-02
      if (getEntry() != null) cal.putEntry(getEntry());

      ACEData[] aces = getACE();
      if (aces != null && aces.length > 0)
      {
        ACEData[] to = new ACEData[aces.length];
        for (int i = 0; i < aces.length; i++)
          to[i] = (ACEData)aces[i].clone();
        cal.putACE(to);
      }
      return cal;
    }
  */

  /**
   * Update properties of this calendar from given one.
   * @param from
   * @param acl
   * @throws Exception
   */
  public void updateProps(CalendarData from, boolean acl) throws Exception {
    putCalid(from.getCalid());
    putCalname(from.getCalname());
    putOwner(from.getOwner());
    putA("created", from.getCreated());
    putA("last-modified", from.getLastModified());
    if (acl) {
      ACEData[] aces = from.getACE();
      if (aces != null && aces.length > 0) {
        ACEData[] to = new ACEData[aces.length];
        for (int i = 0; i < aces.length; i++)
          to[i] = (ACEData)aces[i].clone();
        putACE(to);
      } else
        putE("ace",null);
    }
  }
  /**
   * Find the personal calendar in given array of calendars.
   * @param cals
   * @param user
   * @return
   */
  public static CalendarData findPersonal(CalendarData[] cals, String user) {
    if (cals != null)
      for (int i = 0; i < cals.length; i++)
        if (cals[i].isPersonal() && cals[i].isOwner(user))
          return cals[i];
    return null;
  }

  /**
   * Append a calendar into a array of given calendars.
   * @param cals
   * @param cal
   * @return
   */
  public static CalendarData[] addCalendar(CalendarData[] cals, CalendarData cal) {
    if (cals != null && cal != null) {
      CalendarData[] newCals = new CalendarData[cals.length + 1];
      for (int i = 0; i < cals.length; i++)
        newCals[i] = cals[i];

      newCals[cals.length] = cal;
      return newCals;
    } else
      return cals;
  }

  /**
   * Remove a calendar by id from array of calendars.
   * @param cals
   * @param calid
   * @return
   */

  /*
  public static CalendarData[] removeCalendar(CalendarData[] cals, String calid) {
    if (cals == null || cals.length == 0)
      return cals;
    CalendarData newCals[] = new CalendarData[cals.length - 1];
    for (int i = 0, k = 0; i < cals.length; i++)
      if (calid.equals(cals[i].getCalid()) == false)
        newCals[k++] = cals[i];

    return newCals;
  }
  */
  public static CalendarData[] removeCalendar(CalendarData[] cals, String calid) {
    if (cals == null || cals.length == 0)
      return cals;
    Vector vcal = new Vector();
    for (int i = 0; i < cals.length; i++)
      if (calid.equals(cals[i].getCalid()) == false) {
        vcal.addElement(cals[i]);
      }

    if (vcal != null)
      return (CalendarData[]) vcal.toArray(new CalendarData[] {});

    return null;
  }

  /**
   * Find a calendar by id from array of calendars.
   * @param cals
   * @param calid
   * @return
   */

  public static CalendarData findCalendar(CalendarData[] cals, String calid) {

    if (cals != null && calid != null)
      for (int i = 0; i < cals.length; i++)
        if (calid.equals(cals[i].getCalid()))
          return cals[i];
    return null;
  }
  /**
   * Get array calendar id from array calendars.
   * @param cals
   * @return
   */

  public static String[] calids(CalendarData[] cals) {
    if (cals == null)
      return null;
    String[] ids = new String[cals.length];
    for (int i = 0; i < cals.length; i++)
      if (cals[i] != null)
        ids[i] = cals[i].getCalid();
    return ids;
  }
  //truyen 6/6/02
  /**
   * Create a new copy of calendars from given array of one.
   * @param cals
   * @return
   * @throws Exception
   */
  public static CalendarData[] cloneCalProps(CalendarData[] cals) throws Exception {
    if (cals == null)
      return null;
    CalendarData[] ret = new CalendarData[cals.length];
    for (int i = 0; i < cals.length; i++) {
      ret[i] = new CalendarData();
      ret[i].updateProps(cals[i],true);
    }
    return ret;
  }

  /**
   * Replace a calendar in given array (of calendars).
   * @param cals
   * @param cal
   * @return
   * @throws Exception
   */
  public static CalendarData[] replaceCal(CalendarData[] cals, CalendarData cal) throws Exception {
    if (cals == null)
      return null;
    for (int i = 0; i < cals.length; i++)
      if (cals[i]!= null && cal != null && (cals[i].getCalid()).equals(cal.getCalid()))
        cals[i] = cal;
    return cals;
  }


}
