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

import java.io.*;
import java.util.*;

import net.unicon.academus.apps.rad.IdentityData;
/**
 * This class provides abstract methods for Calendar Server. These methods are separated
 * into 4 main groups:
 * <ul>
 *   <li>Methods of Calendar</li>
 *   <li>Methods of Event</li>
 *   <li>Methods of Todo</li>
 *   <li>Methods of Entry</li>
 * </ul>
 * A Entry is defined an event or a todo.
 * Additional, there are some methods to create a new instance of Calendar Servers,
 * initialize, login/logout.
 */
abstract public class CalendarServer {
  // Search event flags
  public final static int LOOK_IN_TITLE = 1;
  public final static int LOOK_IN_NOTES = 2;
  public final static int LOOK_IN_PLACE = 4;

  // Calendar user codes
  public static final int CUID_ALL = 1;
  public static final int CUID_OWNER = 2;
  public static final int CUID_SHARE = 3;
  public static final int CUID_NOSHARE = 4;

  public static final String EVENT = "EVENT";
  public static final String TODO = "TODO";

  /**
   * Get an instance of CalendarServer object through a Properties object. The properties
   * contains "calendar.server" property that is name of derived class from CalendarServer.
   * @param props
   * @return
   * @throws Exception
   */
  public static CalendarServer getInstance(Properties props) throws Exception {
    CalendarServer server = (CalendarServer)Class.forName(props.getProperty("calendar.server")).newInstance();
    server.init(props);
    return server;
  }

  /**
   * This method will be called when create a new instance CalendarServer.
   * In this class, the method does nothing.
   * The extended class should re-implement this method to initialize.
   */
  public void init(Properties props) {}

  //--------------------------------------------------------------------------//
  //truyen 6/6/02
  //abstract public String[] login(IdentityData person, String password) throws Exception;
  /**
   * Login to Calendar Server to begin a working session.
   * @param person
   * @param password
   * @return
   * @throws Exception
   */
  abstract public CalendarData[] login(IdentityData person, String password) throws Exception;

  /**
   * Get login identifier of user.
   * @return
   * @throws Exception
   */
  abstract public String getUser();
  /**
   * Get information of user.
   * @return
   */
  abstract public net.unicon.academus.apps.rad.XMLData getLogonData();
  /**
   * Get Calendar User Identifier of user. For each Calendar Server, a user has
   * a private identifier that differs from user logon.
   * @return
   */
  abstract public int getCuidCode(String cuid);

  /**
   * Logout from CalendarServer. End the working session.
   */
  abstract public void logout() throws Exception;

  /**
   * Change password of user.
   * @param user
   * @param oldPassword
   * @param newPassword
   * @throws Exception
   */
  abstract public void changePassword(String user, String oldPassword, String newPassword) throws Exception;

  /**
   * Check whether a person have right to authenticate to CalendarServer or not.
   * @param person
   * @param password
   * @throws Exception
   */
  abstract public boolean authenticate(IdentityData person, String password) throws Exception;

  //--------------------------------------------------------------------------//
  /**
   * Create a new calendar.
   * @param cal
   * @return
   * @throws Exception
   */
  abstract public CalendarData createCalendar(CalendarData cal) throws Exception;

  /**
   * Update information for calendar.
   * @param cal
   * @return
   * @throws Exception
   */
  abstract public CalendarData updateCalendar(CalendarData cal) throws Exception;

  /**
   * Delete a calendar with given calid.
   * @param calid Identifier of Calendar
   * @return
   * @throws Exception
   */
  abstract public void deleteCalendar(String calid) throws Exception;
  /**
   * Get all information of array of calendars.
   * @param calidr Array of calid
   * @return
   * @throws Exception
   */
  abstract public CalendarData[] getCalendars(String[] calidr);

  /**
   * Get globally unique identifier.
   * @return
   * @throws Exception
   */
  abstract public String getGUID() throws Exception;
  //--------------------------------------------------------------------------//

  /**
   * Get entries of calendars between two points of time.
   * @param calr
   * @param from
   * @param to
   * @return
   * @throws Exception
   */
  abstract public CalendarData[] fetchEntries(CalendarData[] calr, Date from, Date to) throws Exception;

  /**
   * Get events of calendars between two points of time.
   * @param calr
   * @param from
   * @param to
   * @return
   * @throws Exception
   */
  abstract public CalendarData[] fetchEvents(CalendarData[] calr, Date from, Date to) throws Exception;

  /**
   * Get todos of calendars between two points of time.
   * @param calr
   * @param from
   * @param to
   * @return
   * @throws Exception
   */
  abstract public CalendarData[] fetchTodos(CalendarData[] calr, Date from, Date to) throws Exception;

  /**
   * Get events of calendars with given id.
   * @param calid
   * @param ceidr
   * @return
   * @throws Exception
   */
  abstract public EntryData[] fetchEventsByIds(String calid, EntryRange[] ceidr) throws Exception;

  /**
   * Get todos of calendars with given id.
   * @param calid
   * @param ceidr
   * @return
   * @throws Exception
   */
  abstract public EntryData[] fetchTodosByIds(String calid, EntryRange[] ceidr) throws Exception;

  /**
   * Get events of calendars with given id.
   * @param calid
   * @param ceidr
   * @return
   * @throws Exception
   */
  abstract public CalendarData[] searchEvent(CalendarData[] calr, String text, String category, int lookin, Date from, Date to) throws Exception;
  /**
   * Create a new entry for calendar
   * @param calid
   * @param ent
   * @throws Exception
   */
  abstract public EntryData createEntry(String calid, EntryData ent) throws Exception;

  // Truong Feb-4-02
  /**
   * Get invitation between two points of time.
   * @param from
   * @param to
   * @return
   * @throws Exception
   */
  abstract public EntryData[] fetchInvitations(Date from, Date to) throws Exception;

  /**
   * Get all inviations.
   * @return
   * @throws Exception
   */
  abstract public EntryData[] fetchInvitations() throws Exception;

  /**
   * Count number of invitations between two points of time.
   * @param from
   * @param to
   * @return
   * @throws Exception
   */
  abstract public int countInvitations(Date from, Date to) throws Exception;

  /**
   * Reply a invitation.
   * @param ceid
   * @param status
   * @param comment
   * @throws Exception
   */
  abstract public void replyInvitation(EntryRange ceid, String status, String comment) throws Exception;

  /**
   * Update information for entry given id.
   * @param calid
   * @param ceid
   * @param ent
   * @throws Exception
   */
  abstract public EntryData updateEntry(String calid, EntryRange ceid, EntryData ent) throws Exception;

  /**
   * Remove events of Calendars between two points of time.
   * @param calidr
   * @param from
   * @throws Exception
   */
  abstract public void deleteEvents(String[] calidr, Date from, Date to) throws Exception;

  /**
   * Remove todos of Calendars between two points of time
   * @param calidr
   * @param from
   * @throws Exception
   */
  abstract public void deleteTodos(String[] calidr, Date from, Date to) throws Exception;

  /**
   * Remove events of Calendars with given ids.
   * @param calid
   * @param ceidr
   * @throws Exception
   */
  abstract public void deleteEvents(String calid, EntryRange[] ceidr) throws Exception;

  /**
   * Remove todos of Calendars with given ids.
   * @param calid
   * @param ceidr
   * @throws Exception
   */
  abstract public void deleteTodos(String calid, EntryRange[] ceidr) throws Exception;
  //--------------------------------------------------------------------------//
  //truyen 6/6/02
  /**
   * Remove events of Calendars with given ids.
   * @param calid
   * @param ceid
   * @param complete
   * @throws Exception
   */
  abstract public void completeTodo(String calid, EntryRange ceid, CompletionData complete) throws Exception;

  /**
   * Import entries from Tab delimited file
   * @param is <code>java.io.InputStream</code>
   * @param format "TODO" or "EVENT"
   * @param calid Calendar ID
   * @param map <code>Hashtable</code>
   * @return <code>java.lang.String</code> Error message return "" if no error
   * @throws Exception
   */

  abstract public String importEntries(InputStream is, String format, String calid, Hashtable map) throws Exception;

  /**
   * Export entries of Calendar to Tab delimited file
   * @param format "TODO" or "EVENT"
   * @param calids Array of Calendar ID
   * @param from <code>Date</code>
   * @param to <code>Date</code>
   * @return <code>InputStream</code> Contain entries as Tab delimited format
   * @throws Exception
   */

  abstract public InputStream exportEntries(String format, String[] calids, Date from, Date to) throws Exception;


  //--------------------------------------------------------------------------//
  /**
   * Get string of error of last command.
   * @return
   */
  abstract public String getError();

  //--------------------------------------------------------------------------//
  /**
   * Get entry with given id.
   * @param calid
   * @param ceid
   * @return
   * @throws Exception
   */
  abstract public EntryData getEntry(String calid, String ceid) throws Exception;

  /**
   * Sort vector of EntryData.
   * @param vec
   */
  static public void sort(Vector vec) {
    for (int i = 0; i < vec.size()-1; i++) {
      EntryData oi = (EntryData)vec.elementAt(i);
      if (oi.getDuration() == null || oi.getDuration().getLength() == null)
        vec.removeElementAt(i);
      else {
        Date si = oi.getDuration().getStart();
        TimeLength li = oi.getDuration().getLength();
        if (si == null || li == null)
          return;
        EntryData ov = oi;
        int v = i;
        for (int j = i+1; j < vec.size(); j++) {
          EntryData oj = (EntryData)vec.elementAt(j);
          if (oj.getDuration() == null || oj.getDuration().getLength() == null)
            vec.removeElementAt(j);
          else {
            Date sj = oj.getDuration().getStart();
            TimeLength lj = oj.getDuration().getLength();

            if (sj.before(ov.getDuration().getStart()) ||
                (sj.equals(ov.getDuration().getStart()) && lj.getLength() < ov.getDuration().getLength().getLength())) {
              ov = oj;
              v = j;
            }
          }
        }
        if (v != i) {
          vec.setElementAt(ov,i);
          vec.setElementAt(oi,v);
        }
      }
    }
  }
}
