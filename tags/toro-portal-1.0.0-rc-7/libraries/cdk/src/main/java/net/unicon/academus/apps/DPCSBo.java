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

package net.unicon.academus.apps.dpcs;

import java.io.InputStream;
import java.io.OutputStream;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import net.unicon.academus.apps.calendar.ACEData;
import net.unicon.academus.apps.calendar.AttendeeData;
import net.unicon.academus.apps.calendar.CalendarData;
import net.unicon.academus.apps.calendar.CalendarServer;
import net.unicon.academus.apps.calendar.CompletionData;
import net.unicon.academus.apps.calendar.DurationData;
import net.unicon.academus.apps.calendar.EntryData;
import net.unicon.academus.apps.calendar.EntryRange;
import net.unicon.academus.apps.calendar.EventData;
import net.unicon.academus.apps.calendar.OrganizerData;
import net.unicon.academus.apps.calendar.RecurrenceData;
import net.unicon.academus.apps.calendar.TimeLength;
import net.unicon.academus.apps.calendar.TodoData;
import net.unicon.academus.apps.calendar.dpcs.DPCS;
import net.unicon.academus.apps.rad.DBService;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.SQL;
import net.unicon.academus.apps.rad.StdBo;
import net.unicon.portal.channels.rad.GroupData;
import net.unicon.sdk.log.*;

/**
 * DPCS Business Object
 * <p>Title: Dan Phong Calendar Server</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: IBS-DP</p>
 * @author
 * @version 1.0.c
 */

public class DPCSBo extends StdBo {
  //- Members
  public String m_user = null;
  public IdentityData m_person = null;
  public static Boolean TRUE = new Boolean(true);
  public static Boolean FALSE = new Boolean(false);
  private static int m_debug = 0;

  //- only contain calendardata[] without entries
  CalendarData[] m_calr = null;

  //- Data Source
  public static final String DS = "DPCS";
  static int MAXTIMES = -1;

  //- Constants
  static public final String ERROR_DPCS = "[DPCSBo] Dan Phong Calendar Server error.";
  static public final String ERROR_RDBM = "[DPCSBo] Database error.";
  static public final String ERROR_CAL_NULL = "[DPCSBo] Input CalendarData null.";
  static public final String ERROR_CEIDR_NULL = "[DPCSBo] Param EntryRange ceidr null.";
  static public final String ERROR_USEINWRONGWAY = "[DPCSBo] Use DPCS in wrong way. Must Login first!";
  static public final String ERROR_INVALID_CEID = "Invalid ceid.";
  static public final String ERROR_INVALID_CALENDAR = "Invalid Calendar.";
  static public final String ERROR_CACHE = "[DPCSBo] Cache error.";
  static final String EVENT = "EVENT";
  static final String TODO = "TODO";
  //--------------------------------------------------------------------------//

  /**
   * Constructor
   */

  public DPCSBo() {
    super("DPCSBo");
  }

  //--------------------------------------------------------------------------//

  /**
   * Create business object
   */

  public void ejbCreate() {
    try {
      String debug = (String)getProperty("DPCSDebug");
      if (debug != null && debug.equals("true"))
        m_debug = 1;
    } catch (Exception e) {}
    try {
      if (MAXTIMES == -1) {
        String maxtimes = (String)getProperty("maxtimes");
        if (maxtimes != null && (maxtimes = maxtimes.trim()).length() > 0)
          MAXTIMES = Integer.parseInt(maxtimes);
      }
    } catch (Exception e) {
      MAXTIMES = -1;
    }
    trace("=== EJB DPCSBo Created ===");
    trace("MAXTIMES = " + MAXTIMES);
  }

  /**
   * Reomve business object
   */

  public void ejbRemove() {
    trace("### EJB DPCSBo Removed ###");
  }

  //--------------------------------------------------------------------------//

  /**
   * Login to Dan Phong canlendar server
   * @param person IdentityData of logon user
   * @param calidr array of calid
   * @return true array of calid that logon user can access
   * @throws Exception
   */

  public String[] login(IdentityData person, String[] calidr) throws Exception {
    DBService db = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
      // store current user
      m_user = person.getAlias();
      m_person = person;
      db = getDBService(DS);

      // if personal calendar of user not found, create a new one
      String sql = "SELECT COUNT(CALID) FROM DPCS_CALENDAR WHERE CALID = '" + quot(m_user) + "'";
      if (count(db, sql) == 0) {
        CalendarData cal = new CalendarData();
        cal.putCalid(m_user);
        cal.putCalname(m_user);
        cal.putOwner(m_user);
        cal.putA("created", getCurrentDate());
        cal.putA("last-modified", getCurrentDate());

        createCalendar(cal);
      }
      //

      sql = "SELECT id, calid, calname, owner, created, last_modified FROM dpcs_calendar"
            + " WHERE calid IN " + atostring(calidr);
      ps = db.prepareStatement(sql);
      rs = ps.executeQuery();
      Vector calv = new Vector();
      while (rs.next()) {
        CalendarData cal = new CalendarData();
        cal.putCalid(rs.getString("CALID"));
        cal.putCalname(rs.getString("CALNAME"));
        cal.putOwner(rs.getString("OWNER"));
        cal.putA("created", rs.getTimestamp("CREATED"));
        cal.putA("last-modified", rs.getTimestamp("LAST_MODIFIED"));
        calv.addElement(cal);
      }
      m_calr = new CalendarData[calv.size()];
      for (int i = 0; i < calv.size(); i++) {
        m_calr[i] = (CalendarData)calv.elementAt(i);
      }
      //log
      trace("method login: end");

      return calidr;
    } catch (Exception e) {
      trace(ERROR_RDBM);
      e.printStackTrace(System.err);
      throw e;
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        if (ps != null) ps.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (db != null) db.release();
    }
  }

  /**
   * Not use
   */

  public void logout() throws Exception {
    // not implement
  }

  /**
   * Not use
   */

  public void changePassword(String user, String oldPassword, String newPassword) throws Exception {
    // not implement
  }

  /**
   * Create a calendar
   * @param cal <code>net.unicon.academus.apps.calendar.CalendarData</code> object
   * @return <code>net.unicon.academus.apps.calendar.CalendarData</code> object
   * @throws Exception
   */

  public CalendarData createCalendar(CalendarData cal) throws Exception {
    DBService db = null;
    PreparedStatement ps = null;
    String sql = "";
    int idMax = -1;
    try {
      db = getDBService(DS);
      db.begin();
      idMax = getMaxInt(db, "DPCS_CALENDAR", "ID", null);
      sql = "INSERT INTO DPCS_CALENDAR (ID,CALID,CALNAME,OWNER,CREATED," +
            "LAST_MODIFIED) VALUES (?,?,?,?,?,?)";
      ps = db.prepareStatement(sql);
      if (cal.getCalid() == null) {
        cal.putOwner(m_user);
        cal.putCalid(cal.getOwner()+"_"+Integer.toString(idMax+1));
      }
      ps.setInt(1, idMax+1);
      ps.setString(2, cal.getCalid());
      ps.setString(3, cal.getCalname());
      ps.setString(4, cal.getOwner());
      if (cal.getCreated() != null)
        ps.setTimestamp(5, new Timestamp(cal.getCreated().getTime()));
      else
        ps.setTimestamp(5, new Timestamp(getCurrentDate().getTime()));
      if (cal.getLastModified() != null)
        ps.setTimestamp(6, new Timestamp(cal.getLastModified().getTime()));
      else
        ps.setTimestamp(6, new Timestamp(getCurrentDate().getTime()));
      ps.executeUpdate();
   
      db.commit();
    } catch( Exception e) {
      // de nhieu channel cung tao cal cung luc khong bi exception
      LogServiceFactory.instance().log(
          ILogService.WARN, "DPCSBo::createCalendar(): " + e.getMessage());
      db.commit();
      return cal;
    } finally {
      try {
        if (ps != null) ps.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (db != null) db.release();
    }

    // update m_calr
    if (m_calr != null) {
      m_calr = CalendarData.addCalendar(m_calr, cal);
    } else
      m_calr = new CalendarData[] {
                 cal
               };
    // end update m_calr

    //log
    trace("method createCalendar: complete");
    return cal;
  }

  /**
   * Update a calendar
   * @param cal object of <code>net.unicon.academus.apps.calendar.CalendarData</code>
   * @return An object of <code>net.unicon.academus.apps.calendar.CalendarData</code>
   * @throws Exception
   */

  public CalendarData updateCalendar(CalendarData cal) throws Exception {
    if (cal == null)
      throw new Exception(ERROR_CAL_NULL);

    PreparedStatement ps = null;
    DBService db = null;
    try {
      db = getDBService(DS);
      db.begin();
      String sql = "UPDATE DPCS_CALENDAR SET CALNAME = ?, OWNER = ?, LAST_MODIFIED = ? WHERE CALID = ?";
      ps = db.prepareStatement(sql);
      ps.setString(1, cal.getCalname());
      ps.setString(2, cal.getOwner());
      ps.setTimestamp(3, new Timestamp(getCurrentDate().getTime()));
      ps.setString(4, cal.getCalid());
      ps.executeUpdate();

      db.commit();
    } catch( Exception e) {
      db.rollback(e.getMessage());
      e.printStackTrace(System.err);
      throw e;
    } finally {
      try {
        if (ps != null) ps.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (db != null) db.release();
    }

    // update cache
    if (m_calr != null) {
      CalendarData old = CalendarData.findCalendar(m_calr, cal.getCalid());
      if (old != null)
        old.updateProps(cal, true);
    }
    //

    return cal;
  }

  /**
   * Delete a Calendar
   * @param calid java.lang.String
   * @throws Exception
   */

  public void deleteCalendar(String calid) throws Exception {
    DBService db = null;
    try {
      PreparedStatement ps = null;
      db = getDBService(DS);
      db.begin();

      String sql = "UPDATE DPCS_ENTRY SET REFS = REFS-1 WHERE DPCS_ENTRY.CEID IN (SELECT DPCS_CAL_X_ENTRY.CEID FROM DPCS_CAL_X_ENTRY WHERE CALID = '" + quot(calid) + "')";
      try {
        ps = db.prepareStatement(sql);
        ps.executeUpdate();
      } finally {
        if (ps != null) ps.close();
      }

      sql = "DELETE FROM DPCS_RECURRENCE WHERE DPCS_RECURRENCE.CEID IN (SELECT DPCS_ENTRY.CEID FROM DPCS_ENTRY WHERE REFS = 0)";
      try {
        ps = db.prepareStatement(sql);
        ps.executeUpdate();
      } finally {
        if (ps != null) ps.close();
      }

      sql = "DELETE FROM DPCS_ATTENDEE WHERE DPCS_ATTENDEE.CEID IN (SELECT DPCS_ENTRY.CEID FROM DPCS_ENTRY WHERE REFS = 0)";
      try {
        ps = db.prepareStatement(sql);
        ps.executeUpdate();
      } finally {
        if (ps != null) ps.close();
      }

      sql = "DELETE FROM DPCS_ENTRY WHERE REFS = 0";
      try {
        ps = db.prepareStatement(sql);
        ps.executeUpdate();
      } finally {
        if (ps != null) ps.close();
      }

      sql = "DELETE FROM DPCS_CAL_X_ENTRY WHERE CALID = '" + quot(calid) + "'";
      try {
        ps = db.prepareStatement(sql);
        ps.executeUpdate();
      } finally {
        if (ps != null) ps.close();
      }

      sql = "DELETE FROM DPCS_CALENDAR WHERE CALID = '" + quot(calid) + "'";
      try {
        ps = db.prepareStatement(sql);
        ps.executeUpdate();
      } finally {
        if (ps != null) ps.close();
      }

      db.commit();
    } catch( Exception e) {
      db.rollback(e.getMessage());
      e.printStackTrace(System.err);
      throw e;
    } finally {
      if (db != null) db.release();
    }

    // update m_calr
    if (m_calr != null) {
      m_calr = CalendarData.removeCalendar(m_calr, calid);
    }
    //

  }

  /**
   * Get calendars
   * @param calidr Array of calids
   * @return <code>net.unicon.academus.apps.calendar.CalendarData</code> objects
   * @throws Exception
   */

  public CalendarData[] getCalendars(String[] calidr) throws Exception {
    //trace("method: getCalendar m_calr.length " + m_calr.length);
    if (calidr == null)
      return cloneCalrProps(m_calr);
    //return m_calr;
    else {
      CalendarData[] ret = new CalendarData[calidr.length];
      for (int i=0; i < calidr.length; i++) {
        //ret[i] = CalendarData.findCalendar(m_calr, calidr[i]);
        CalendarData cal = CalendarData.findCalendar(m_calr, calidr[i]);
        ret[i] = new CalendarData();
        if (cal != null)
          ret[i].updateProps(cal, true);
      }
      return ret;
    }
  }

  /**
   * Fetch Entries of calendars in one period
   * @param array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects not include entries
   * @param from java.util.Date
   * @param to java.util.Date
   * @return array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects include entries
   * @throws Exception
   */

  public CalendarData[] fetchEntries(CalendarData[] calr, Date from, Date to) throws Exception {
    return fetchComponentsByRange(calr, from, to, null);
  }

  /**
   * Fetch Events of calendars in one period
   * @param array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects not include events
   * @param from java.util.Date
   * @param to java.util.Date
   * @return array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects include events
   * @throws Exception
   */

  public CalendarData[] fetchEvents(CalendarData[] calr, Date from, Date to) throws Exception {
    //return fetchComponentsByRange(calr, from, to, "DUE is null");
    return fetchComponentsByRange(calr, from, to, "LENGTH != 0");
  }

  /**
   * Fetch Todos of calendars in one period
   * @param array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects not include todos
   * @param from java.util.Date
   * @param to java.util.Date
   * @return array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects include todos
   * @throws Exception
   */

  public CalendarData[] fetchTodos(CalendarData[] calr, Date from, Date to) throws Exception {
    return fetchComponentsByRange(calr, from, to, "DUE is not null");
  }

  /**
   * Fetch Entries
   * @param calr Array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects
   * @param from java.util.Date
   * @param to java.util.Date
   * @param filter String "DUE is null" for event or "DUE is not null" for todo
   * @return Array of Calendar objects
   * @throws Exception
   */

  CalendarData[] fetchComponentsByRange(CalendarData[] calr, Date from, Date to, String filter) throws Exception {
    if (calr == null || calr.length == 0 || calr[0] == null)
      return null;
    trace("method fetchEntries: caldr.length m_calr.length " + calr.length +" --- "+ m_calr.length);
    trace("method fetchEntries " + calr[0]);

    String sql = "SELECT DPCS_ENTRY.*, DPCS_CAL_X_ENTRY.CALID FROM DPCS_CAL_X_ENTRY, DPCS_ENTRY WHERE CALID IN " + atostring(calr)
                 + " AND DPCS_ENTRY.CEID = DPCS_CAL_X_ENTRY.CEID AND DPCS_ENTRY.CESEQ = DPCS_CAL_X_ENTRY.CESEQ";
    if (to != null) {
      //
      Calendar toCal = Calendar.getInstance();
      toCal.setTime(to);
      toCal.add(Calendar.DATE, +1);
      Date to1 = toCal.getTime();
      //
      sql += " AND ENTRY_DAY <= {ts '" + new Timestamp(to1.getTime()) + "'}";
    }
    if (from != null)
      sql += " AND EOR >= {ts '" + new Timestamp(from.getTime()) + "'}";
    if (filter != null)
      sql += " AND " + filter;

    calr = buildCalr(calr, sql, from, to);

    trace("method: fetchEntries end update cache calr[0].getCalid() calr[0].getEntry() " + calr[0].getCalid() +" "+ calr[0].getEntry());

    return calr;
  }

  /**
   *
   * @param calid
   * @param ceid
   * @return
   * @throws Exception
   */

  EntryData[] fetchAllEntriesByIds(String calid, String ceid) throws Exception {
    CalendarData[] cals = getCalendars( new String[] {calid});
    cals = fetchEntries( cals, null, null);
    if( cals != null && cals.length > 0) {
      EntryData[] ents = cals[0].getEntry();
      Vector v = new Vector();
      String baseCeid = getBaseCeid( ceid);
      if( ents != null)
        for( int i = 0; i < ents.length; i++) {
          String otherBase = getBaseCeid( ents[i].getCeid());
          if( otherBase != null && otherBase.equals(baseCeid))
            v.addElement(ents[i]);
        }

      return (EntryData[])v.toArray(new EntryData[]{});
    }
    return null;
  }

  boolean isRecurrentCeid( String ceid) {
    return (ceid.indexOf(".") != -1);
  }

  String getBaseCeid( String ceid) {
    int dot = ceid.indexOf("_");
    if( dot != -1)
      return ceid.substring(0,dot);
    else
      return null;
  }

  /**
   * Fetch Entries by id
   * @param calid java.lang.String
   * @param array of <code>net.unicon.academus.apps.calendar.EntryRange</code> objects contain id of entries
   * @return array of <code>net.unicon.academus.apps.calendar.EntryData</code> objects
   * @throws Exception
   */

  public EntryData[] fetchEntriesByIds(String calid, EntryRange[] ceidr) throws Exception {
    if (ceidr == null)
      return null;

    String[] ceids = new String[ceidr.length];
    for (int i = 0; i < ceidr.length; i++) {
      StringTokenizer token = new StringTokenizer(ceidr[i].m_ceid, "_.");
      ceids[i] = (String)token.nextToken();
      trace("ceids[i] " + ceids[i]);
    }
    String sql = "SELECT DPCS_ENTRY.*, DPCS_CAL_X_ENTRY.CALID FROM DPCS_CAL_X_ENTRY, DPCS_ENTRY WHERE CALID='"+quot(calid)+"' AND DPCS_CAL_X_ENTRY.CEID = DPCS_ENTRY.CEID AND DPCS_ENTRY.CESEQ = DPCS_CAL_X_ENTRY.CESEQ  AND  DPCS_ENTRY.CEID IN " + atostring(ceids);

    if(ceidr[0].m_mod == 0)  // mod = 0
    {
      EntryData[] entr = new EntryData[ceidr.length];
      EntryData[] entr1 = buildEntr(sql, false, null, null, true);
      for (int i = 0; i < ceidr.length; i++) {
        entr[i] = EntryData.findEntry(entr1, ceidr[i].m_ceid);
      }

      return entr;
    } else // mod = all
      return buildEntr(sql, false, null, null, true);

  }

  /**
   *  Fetch Events by id
    * @param calid
    * @param array of <code>net.unicon.academus.apps.calendar.EntryRange</code> objects contain id of events
    * @return array of <code>net.unicon.academus.apps.calendar.EntryData</code> objects contain Events
    * @throws Exception
   */

  public EntryData[] fetchEventsByIds(String calid, EntryRange[] ceidr) throws Exception {
    return fetchEntriesByIds(calid, ceidr);
  }

  /**
   * Fetch Todos by id
   * @param calid
   * @param array of <code>net.unicon.academus.apps.calendar.EntryRange</code> objects contain id of todos
   * @return array of <code>net.unicon.academus.apps.calendar.EntryData</code> objects contain Todos
   * @throws Exception
  */

  public EntryData[] fetchTodosByIds(String calid, EntryRange[] ceidr) throws Exception {
    return fetchEntriesByIds(calid, ceidr);
  }

  /**   * Search Event by category, look in title, notes, place   * @param array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects   * @param text java.lang.String   * @param category java.lang.String   * @param lookin integer, lookin have value CalendarServer.LOOK_IN_TITLE, CalendarServer.LOOK_IN_NOTES, CalendarServer.LOOK_IN_PLACE   * @param from java.util.Date   * @param to java.util.Date   * @return array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects include event found   * @throws Exception   */
  public CalendarData[] searchEvent(CalendarData[] calr, String text, String category, int lookin, Date from, Date to) throws Exception {
    if (calr == null)      return null;
    text = quot(text.trim());
    trace("method searchEvent calidr.length " + calr.length);
    String sql = "SELECT DPCS_ENTRY.*, DPCS_CAL_X_ENTRY.CALID FROM DPCS_CAL_X_ENTRY, DPCS_ENTRY WHERE"                 + " CALID IN " + atostring(calr) + " AND"                 + " DPCS_ENTRY.CEID = DPCS_CAL_X_ENTRY.CEID"                 + " AND DPCS_ENTRY.CESEQ = DPCS_CAL_X_ENTRY.CESEQ";
    if (to != null)      sql += " AND ENTRY_DAY <= {ts '" + new Timestamp(to.getTime()) + "'}";
    if (from != null)      sql += " AND EOR >= {ts '" + new Timestamp(from.getTime()) + "'}";
    if (category != null && !category.equals("all-categogy"))      sql += " AND DPCS_ENTRY.CATEGORIES = '" + category + "'";
    if (lookin == CalendarServer.LOOK_IN_TITLE)      sql += " AND lower(TITLE) LIKE lower('%" + text + "%')";
    if (lookin == CalendarServer.LOOK_IN_NOTES)      sql += " AND lower(DESCRIPTION) LIKE lower('%" + text + "%')";
    if (lookin == CalendarServer.LOOK_IN_PLACE)      sql += " AND lower(LOCATION) LIKE lower('%" + text + "%')";
    if (lookin == CalendarServer.LOOK_IN_TITLE + CalendarServer.LOOK_IN_NOTES)      sql += " AND (lower(TITLE) LIKE lower('%" + text + "%') OR lower(DESCRIPTION) LIKE lower('%" + text + "%'))";
    if (lookin == CalendarServer.LOOK_IN_TITLE + CalendarServer.LOOK_IN_PLACE)      sql += " AND (lower(TITLE) LIKE lower('%" + text + "%') OR lower(LOCATION) LIKE lower('%" + text + "%'))";
    if (lookin == CalendarServer.LOOK_IN_NOTES + CalendarServer.LOOK_IN_PLACE)      sql += " AND (lower(DESCRIPTION) LIKE lower('%" + text + "%') OR lower(LOCATION) LIKE lower('%" + text + "%'))";
    if (lookin == CalendarServer.LOOK_IN_TITLE+CalendarServer.LOOK_IN_NOTES+CalendarServer.LOOK_IN_PLACE)      sql += " AND (lower(TITLE) LIKE lower('%" + text + "%') OR lower(DESCRIPTION) LIKE lower('%" + text + "%') OR lower(LOCATION) LIKE lower('%" + text + "%'))";
    calr = buildCalr(calr, sql, from, to);
    return calr;  }
  /**
   * Get Entry
   * @param calid java.lang.String
   * @param ceid java.lang.String - id of entry
   * @return <code>net.unicon.academus.apps.calendar.CalendarData</code> object
   * @throws Exception
   */

  public EntryData getEntry(String calid, String ceid) throws Exception {
    if (ceid == null || calid == null)
      return null;

    trace("method: getEntry : ceid " + ceid);

    if (isRecurrence(ceid)) {
      trace("method: getEntry : step 0");
      EntryData[] entr = fetchEntriesByIds(calid, new EntryRange[]{new EntryRange(ceid, EntryRange.ALL)});
      if (entr != null && entr.length >0) {
        for (int j = 0; j < entr.length; j++) {
          trace("method: getEntry : step 1.1 entr[j].getCeid() " + entr[j].getCeid());
          if (entr[j].getCeid().equals(ceid))
            return entr[j];
        }
        return null;
      } else
        return null;
    }

    // not reccurence
    StringTokenizer tokens = new StringTokenizer(ceid, "_.");
    String ceseq;
    try {
      ceid = tokens.nextToken();
      ceseq = tokens.nextToken();
    } catch (Exception e) {
      throw new Exception(ERROR_INVALID_CEID);
    }
    String sql = "SELECT e.id, e.ceid, e.ceseq, e.created, e.last_modified, e.entry_scope, e.refs, e.cuid, e.entry_day, " +    				"e.eor, e.length, e.blocked, e.due, e.percentage, e.completed, e.title, e.status, " +    				"e.priority, e.description, e.categories, e.location, x.calid, x.ceid, x.ceseq " +    				"FROM dpcs_entry e, dpcs_cal_x_entry x " +    				"WHERE e.ceid = '" + ceid + "' " +    				"AND e.CESEQ = " + ceseq + " " +    				"AND x.CALID = '"+ quot(calid) + "' " +    				"AND x.CEID = '" + ceid + "' " +    				"AND x.CESEQ = " + ceseq;    				;
    DBService db = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    Vector entv = new Vector();

    // RecurrenceData
    Vector recurv = new Vector();

    // AttendeeData
    Vector attv = new Vector();

    try {

    db = getDBService(DS);

    try {
    ps = db.prepareStatement(sql);
    rs = ps.executeQuery();
    //ps.close();
    while (rs.next()) {
      EntryData ent = new EntryData();
      // TodoData
      if (rs.getLong("LENGTH") == 0 && rs.getTimestamp("DUE") != null) {
        TodoData todo = new TodoData();
        if (rs.getTimestamp("COMPLETED") != null && rs.getInt("PERCENTAGE") != 0) {
          CompletionData comp = new CompletionData(rs.getTimestamp("COMPLETED"), new Integer(rs.getInt("PERCENTAGE")));
          todo.putCompletion(comp);
        }
        Timestamp due = rs.getTimestamp("DUE"); // fix nano on SQL 7
        due.setNanos(0);
        todo.putDue(due);
        if (rs.getString("CATEGORIES") != null)
          todo.putCategory(new String[]{rs.getString("CATEGORIES")});
        if (rs.getString("DESCRIPTION") != null)
          todo.putDescription(rs.getString("DESCRIPTION"));
        todo.putPriority(new Integer(rs.getInt("PRIORITY")));
        if (rs.getString("STATUS") != null)
          todo.putStatus(rs.getString("STATUS"));
        if (rs.getString("TITLE") != null)
          todo.put(rs.getString("TITLE"));

        ent.putTodo(todo);
      } else {
        // EventData
        EventData ev = new EventData();
        if (rs.getString("CATEGORIES") != null)
          ev.putCategory(new String[]{rs.getString("CATEGORIES")});
        if (rs.getString("DESCRIPTION") != null)
          ev.putDescription(rs.getString("DESCRIPTION"));
        ev.putPriority(new Integer(rs.getInt("PRIORITY")));
        if (rs.getString("STATUS") != null)
          ev.putStatus(rs.getString("STATUS"));
        if (rs.getString("TITLE") != null)
          ev.put(rs.getString("TITLE"));

        ent.putEvent(ev);
      }

      // DurationData
      DurationData dur = new DurationData();
      Timestamp day = rs.getTimestamp("ENTRY_DAY");
      day.setNanos(0);
      dur.putStart(day);

      if (rs.getLong("LENGTH") == -1)
        dur.putAllDay();
      else
        dur.putLength(new TimeLength(rs.getLong("LENGTH")));

      ent.putDuration(dur);

      // OrganizerData
      OrganizerData organizer = new OrganizerData();
      organizer.putCuid(rs.getString("CUID"));
      organizer.put(rs.getString("CUID"));
      ent.putOrganizer(organizer);

      ent.putCeid(rs.getString("CEID"));
      // temporary put CESEQ in entrydata
      ent.putA("CESEQ", new Integer(rs.getInt("CESEQ")));

      if (rs.getString("LOCATION") != null)
        ent.putLocation(rs.getString("LOCATION"));
      if (rs.getString("ENTRY_SCOPE") != null)
        ent.putShare(rs.getString("ENTRY_SCOPE"));

      ent.putA("created", rs.getTimestamp("CREATED"));
      ent.putA("last-modified",rs.getTimestamp("LAST_MODIFIED"));

      entv.addElement(ent);
    }

    } finally {
      try {
        if (rs != null) rs.close();
        rs = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    try {
    sql = "SELECT ceid, ceseq, exclude, recur_day, frequency, byday, intrvl, repetition, until FROM dpcs_recurrence WHERE ceid = '" + ceid + "' AND ceseq = '" + ceseq + "'";
    ps = db.prepareStatement(sql);
    rs = ps.executeQuery();
    //ps.close();
    while (rs.next()) {
      RecurrenceData rec = new RecurrenceData();
      if (rs.getTimestamp("RECUR_DAY") != null) {
        Timestamp day = rs.getTimestamp("RECUR_DAY");
        day.setNanos(0);
        rec.putDate(day);
      }
      if (rs.getString("BYDAY") != null)
        rec.putByDay(rs.getString("BYDAY"));
      if (rs.getInt("EXCLUDE") == 1)
        rec.putExclude(TRUE);
      if (rs.getInt("REPETITION") != 0)
        rec.putRule(rs.getString("FREQUENCY"), new Integer(rs.getInt("INTRVL")), new Integer(rs.getInt("REPETITION")));
      else if (rs.getTimestamp("UNTIL") != null)
        rec.putRule(rs.getString("FREQUENCY"), new Integer(rs.getInt("INTRVL")), rs.getTimestamp("UNTIL"));

      // temporary add two field CEID and CESEQ
      rec.putA("CEID", rs.getString("CEID"));
      rec.putA("CESEQ", new Integer(rs.getInt("CESEQ")));

      recurv.addElement(rec);
    }
    } finally {
      try { 
        if (rs != null) rs.close();
        rs = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
      try { 
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    try {
    sql = "SELECT ceid, ceseq, cuid, role, status, rsvp, comments FROM dpcs_attendee WHERE ceid = '" + ceid + "' AND ceseq = " + ceseq + "";
    rs = db.select(sql);
    while (rs.next()) {
      AttendeeData att = new AttendeeData();
      initAttendeeData( att, rs.getString("CUID"));

      att.putRole(rs.getString("ROLE"));
      att.putStatus(rs.getString("STATUS"));
      //att.putRSVP(new Boolean(rs.getBoolean("RSVP")));
      att.putRSVP(new Boolean(rs.getInt("RSVP")==1? true:false));

      // temporary add two field CEID and CESEQ
      att.putA("CEID", rs.getString("CEID"));
      att.putA("CESEQ", new Integer(rs.getInt("CESEQ")));

      attv.addElement(att);
    }
    } finally {
      if (rs != null) rs.close();
    }

    } finally {
      if (db != null) db.release();
    }

    // Build entries data with detail (alarmdata, recurrencedata, attendeedata)
    for (int i = 0; i < entv.size(); i++) {
      for (int j = 0; j < recurv.size(); j++) {
        if ( ((RecurrenceData)recurv.elementAt(j)).getA("CEID").equals(((EntryData)entv.elementAt(i)).getCeid()) ) {
          EntryData ent = (EntryData)entv.elementAt(i);
          ent.putRecurrence(new RecurrenceData[]{(RecurrenceData)recurv.elementAt(j)});
          entv.setElementAt(ent, i);
        }
      }
      for (int k = 0; k < attv.size(); k++) {
        if ( ((AttendeeData)attv.elementAt(k)).getA("CEID").equals(((EntryData)entv.elementAt(i)).getCeid())
             && ((AttendeeData)attv.elementAt(k)).getA("CESEQ").equals(((EntryData)entv.elementAt(i)).getA("CESEQ")) ) {
          EntryData ent = (EntryData)entv.elementAt(i);
          AttendeeData[] attr = ent.getAttendee();
          if (attr != null && attr.length > 0) {
            AttendeeData[] attr2 = new AttendeeData[attr.length + 1];
            for (int m = 0; m < attr2.length-1; m++)
              attr2[m] = attr[m];
            attr2[attr2.length-1] = (AttendeeData)attv.elementAt(k);
            ent.putAttendee(attr2);
          } else
            ent.putAttendee(new AttendeeData[] {
                              (AttendeeData)attv.elementAt(k)
                            }
                           );

          entv.setElementAt(ent, i);
        }
      }
    }

    EntryData[] entr = new EntryData[entv.size()];
    if (entv != null)
      for (int i = 0; i < entv.size(); i++) {
        entr[i] = (EntryData)entv.elementAt(i);
        entr[i].putCeid(entr[i].getCeid()+"_"+entr[i].getA("CESEQ"));
      }

    return (entr != null && entr.length > 0 ? entr[0] : null);
  }

  /**
   * Fetch all invitations
   * @return Array of <code>net.unicon.academus.apps.calendar.EntryData</code> object
   * @throws Exception
   */

  public EntryData[] fetchInvitations() throws Exception {
    return fetchInvitations(null, null);
  }

  /**
   * Fetch invitations in a period
   * @param from java.util.Date
   * @param to java.util.Date
   * @return Array of <code>net.unicon.academus.apps.calendar.EntryData</code> object
   * @throws Exception
   */

  public EntryData[] fetchInvitations(Date from, Date to) throws Exception {
    String sql = "SELECT DPCS_ENTRY.* FROM DPCS_ATTENDEE, DPCS_ENTRY WHERE "
                 + "(DPCS_ATTENDEE.CUID LIKE '"
                 + quot(m_person.getIdentifier())+IdentityData.SEPARATOR+ "%'";
    if (m_person.getRef("campus") != null)
      sql += " OR DPCS_ATTENDEE.CUID LIKE '" +quot(m_person.getRef("campus").getIdentifier())+IdentityData.SEPARATOR+"%'";
    if (m_person.getRef("contact") != null)
      sql += " OR DPCS_ATTENDEE.CUID LIKE '" +quot(m_person.getRef("contact").getIdentifier())+IdentityData.SEPARATOR+"%'";
    String grList = (String)m_person.getA("dpcs_grlist");
    if (grList != null && grList.length() > 0)
      sql += " OR DPCS_ATTENDEE.CUID IN " + grList;

    sql +=     ") AND DPCS_ATTENDEE.STATUS = 'NEEDS-ACTION' "
               + " AND DPCS_ENTRY.CEID = DPCS_ATTENDEE.CEID"
               + " AND DPCS_ENTRY.CESEQ = DPCS_ATTENDEE.CESEQ"
               + " AND DPCS_ENTRY.CUID != '" + quot(m_user) + "'";

    /*
     + " AND DPCS_ENTRY.CEID NOT IN (SELECT DPCS_ENTRY.CEID FROM DPCS_ENTRY, DPCS_ATTENDEE WHERE DPCS_ENTRY.CEID = DPCS_ATTENDEE.CEID AND DPCS_ENTRY.CESEQ = DPCS_ATTENDEE.CESEQ "
     + " AND (DPCS_ATTENDEE.STATUS = 'ACCEPTED' OR DPCS_ATTENDEE.STATUS = 'DECLINED') AND "
     + " (DPCS_ATTENDEE.CUID LIKE '"
     + quot(m_person.getIdentifier())+IdentityData.SEPARATOR + "%'";
    if (m_person.getRef("campus") != null)
    sql += " OR DPCS_ATTENDEE.CUID LIKE '" +quot(m_person.getRef("campus").getIdentifier())+IdentityData.SEPARATOR+"%'";
    if (m_person.getRef("contact") != null)
    sql += " OR DPCS_ATTENDEE.CUID LIKE '" +quot(m_person.getRef("contact").getIdentifier())+IdentityData.SEPARATOR+"%'";
    sql         += "))";
    */

    if (to != null) {
      //
      Calendar toCal = Calendar.getInstance();
      toCal.setTime(to);
      toCal.add(Calendar.DATE, +1);
      Date to1 = toCal.getTime();
      //
      sql += " AND ENTRY_DAY <= {ts '" + new Timestamp(to1.getTime()) + "'}";
    }
    if (from != null)
      sql += " AND EOR >= {ts '" + new Timestamp(from.getTime()) + "'}";

    return filterInviEnt(buildEntr(sql, true, from, to, false));
    //return buildEntr(sql, true, from, to, false);
  }

  /**
   * Accept or Decline an invitation entry
   * @param calid Id of calendar contain this invitation entry
   * @param ceid Id of invitation entry
   * @param status ACCEPTED or DECLINE
   * @param comment Any comment
   * @throws Exception
   */

  public void replyInvitation(String calid, EntryRange ceid, String status, String comment) throws Exception {
    DBService db = null;

    try {
      db = getDBService(DS);
      db.begin();
      PreparedStatement ps = null; 

      String ceid1 = null;
      int ceseq = 0;
      StringTokenizer token = new StringTokenizer(ceid.m_ceid, "_.");
      ceid1 = token.nextToken();
      ceseq = Integer.parseInt(token.nextToken());
      //
      EntryData[]  invr = fetchInvitations();
      HashMap seqmap = new HashMap();
      if (invr != null) {
        for (int i = 0; i < invr.length; i++) {
          if (invr[i].getCeid().startsWith(ceid1+"_")) {
            StringTokenizer token1 = new StringTokenizer(invr[i].getCeid(), "_.");
            String id = token1.nextToken();
            String seq = token1.nextToken();
            seqmap.put(seq, seq);
          }
        }
      }

      Vector ceseqv = new Vector();
      ceseqv.addAll(seqmap.values());
      for (int i = 0; i < ceseqv.size(); i++) {
        ceseq = Integer.parseInt((String)ceseqv.elementAt(i));
        String sql = "UPDATE DPCS_ATTENDEE SET STATUS=?, COMMENTS=? WHERE CEID=? AND CESEQ=? AND (CUID LIKE ?";
        if (m_person.getRef("campus") != null)
          sql += " OR CUID LIKE '" +m_person.getRef("campus").getIdentifier()+IdentityData.SEPARATOR+"%'";
        if (m_person.getRef("contact") != null)
          sql += " OR CUID LIKE '" +m_person.getRef("contact").getIdentifier()+IdentityData.SEPARATOR+"%'";
        sql += ")";

        int ret = 0;
        try {
        ps = db.prepareStatement(sql);
        ps.setString(1, status);
        ps.setString(2, comment);
        ps.setString(3, ceid1);
        ps.setInt(4, ceseq);
        ps.setString(5, m_person.getIdentifier()+IdentityData.SEPARATOR+"%");
        ret = ps.executeUpdate();
        } finally {
          try {
            if (ps != null) ps.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        trace("method replyinvitation: ret " + ret );
        if (ret == 0) // id not found -> have group in here
        {
          sql = "INSERT INTO DPCS_ATTENDEE VALUES (?,?,?,?,?,?,?)";

          try {
          ps = db.prepareStatement(sql);
          ps.setString(1, ceid1);
          ps.setInt(2, ceseq);
          //ps.setString(3, IdentityData.ENTITY + IdentityData.SEPARATOR + GroupData.S_USER
          //           + IdentityData.SEPARATOR + m_person.getID() + IdentityData.SEPARATOR
          //           + m_person.getAttribute("username") + IdentityData.SEPARATOR
          //           + m_person.getFullName() != null ? m_person.getFullName() : m_user);
          //           + m_user );
          ps.setString(3, m_person.toString());
          ps.setString(4, "REQ_PARTICIPANT");
          ps.setString(5, status);
          ps.setInt(6, 1);
          ps.setString(7, comment);
          ps.executeUpdate();
          } finally {
            try {
              if (ps != null) ps.close();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }

        if (status.equals("ACCEPTED")) {
          sql = "INSERT INTO DPCS_CAL_X_ENTRY VALUES (?,?,?)";

          try {
          ps = db.prepareStatement(sql);
          ps.setString(1, calid);
          ps.setString(2, ceid1);
          ps.setInt(3, ceseq);
          ps.executeUpdate();
          } finally {
            try {
              if (ps != null) ps.close();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          sql = "UPDATE DPCS_ENTRY SET REFS = REFS+1 WHERE CEID=? AND CESEQ=?";
          try {
          ps = db.prepareStatement(sql);
          ps.setString(1, ceid1);
          ps.setInt(2, ceseq);
          ps.executeUpdate();
          } finally {
            try {
              if (ps != null) ps.close();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      }

      db.commit();
    } catch (Exception e) {
      db.rollback(e.getMessage());
      e.printStackTrace(System.err);
      throw e; //
    } finally {
      if (db != null) db.release();
    }

    trace("method replyinvitation: end");
  }

  /**
   * Create entry
   * @param calid
   * @param ent <code>net.unicon.academus.apps.calendar.EntryData<code> object
   * @param ceseq integer - sequence id
   * @return <code>net.unicon.academus.apps.calendar.EntryData<code> object
   * @throws Exception
   */

  public EntryData createEntry(String calid, EntryData ent, int ceseq) throws Exception {
    DBService db = getDBService(DS);
    db.begin();
    try {
      createEntry(calid, ent, ceseq, db);
      db.commit();
    } catch (Exception e) {
      e.printStackTrace(System.err);
      db.rollback(e.getMessage());
      throw e;
    } finally {
      db.release();
    }

    return ent;
  }

  /**
   *
   * @param calid
   * @param ent
   * @param ceseq
   * @param db
   * @return
   * @throws Exception
   */

  EntryData createEntry(String calid, EntryData ent, int ceseq, DBService db) throws Exception {
    int ceid = 0;
    // initialize ceid
    if (ent.getCeid() != null) {
      try {
        ceid = Integer.parseInt(ent.getCeid());
      } catch (Exception e) {
        throw new Exception(ERROR_INVALID_CEID);
      }
    } else {
      int maxCeid = getMaxInt(db, "DPCS_ENTRY", "ID", null);
      ceid = maxCeid + 1;
    }
    // build return ceid
    if (ent.getRecurrence() != null) {
      if (ent.getDuration() != null && ent.getDuration().getStart() != null) {
        Timestamp start = null;
        if (ent.getDuration().getLength()!= null && ent.getDuration().getLength().getAllDay()) {
          Calendar startCal = Calendar.getInstance();
          startCal.setTime(ent.getDuration().getStart());
          startCal.setTimeZone(TimeZone.getTimeZone("GMT"));
          //startCal.add(Calendar.HOUR, +12);
          startCal.set(Calendar.HOUR, +12);
          start = new Timestamp(startCal.getTime().getTime());
        } else {
          start = new Timestamp(ent.getDuration().getStart().getTime());
        }
        start.setNanos(0);
        ent.putCeid(Integer.toString(ceid)+"_"+ceseq+"."+ start.getTime());
      } else if (ent.getTodo() != null && ent.getTodo().getDue() != null) {
        ent.putCeid(Integer.toString(ceid)+"_"+ceseq+"."+ent.getTodo().getDue().getTime());
      }
    } else
      ent.putCeid(Integer.toString(ceid)+"_"+ceseq);
    // Write to database
    String sql = "INSERT INTO DPCS_ENTRY VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    PreparedStatement ps = null;
    CalendarData cal = null;
    Timestamp eor = null;

    try {
    ps = db.prepareStatement(sql);
    ps.setInt(1, ceid);
    ps.setString(2, Integer.toString(ceid));
    trace("method: createEntry : step -3");
    ps.setInt(3, ceseq);
    ent.putA("created", getCurrentDate());
    ent.putA("last-modified", Calendar.getInstance().getTime());
    ps.setTimestamp(4, new Timestamp(ent.getCreated().getTime()));
    ps.setTimestamp(5, new Timestamp(ent.getLastModified().getTime()));
    ps.setString(6, ent.getShare());
    ps.setInt(7, 1);
    cal = CalendarData.findCalendar(m_calr, calid);
    if (cal == null)
      throw new Exception(ERROR_INVALID_CALENDAR);
    ps.setString(8, cal.getOwner());
    //ps.setString(8, m_user);
    if (ent.getDuration() != null && ent.getDuration().getStart() != null) {
      Timestamp start = null;
      if (ent.getDuration().getLength()!= null && ent.getDuration().getLength().getAllDay()) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(ent.getDuration().getStart());
        startCal.setTimeZone(TimeZone.getTimeZone("GMT"));
        //startCal.add(Calendar.HOUR, +12);
        startCal.set(Calendar.HOUR, +12);
        start = new Timestamp(startCal.getTime().getTime());
      } else {
        start = new Timestamp(ent.getDuration().getStart().getTime());
      }
      start.setNanos(0);
      ps.setTimestamp(9, start);
    } else if (ent.getTodo() != null) {
      Date d = ent.getTodo().getDue();
      if( d == null)
        d = getCurrentDate();
      Timestamp due = new Timestamp(d.getTime());
      due.setNanos(0);
      ps.setTimestamp(9, due);
    } else {
      Timestamp due = new Timestamp(getCurrentDate().getTime());
      due.setNanos(0);
      ps.setTimestamp(9, due);
    }

    //log
    trace("method: createEntry : step -41");
    eor = new Timestamp(generateEOR(ent).getTime());
    eor.setNanos(0);
    ps.setTimestamp(10, eor);
    //log
    trace("method: createEntry: step -4");

    if (ent.getDuration() != null && ent.getDuration().getLength() != null && ent.getDuration().getLength().getLength() > 0)
      ps.setInt(11, (int)ent.getDuration().getLength().getLength());
    else if (ent.getTodo() != null)
      ps.setInt(11, 0);
    else
      ps.setInt(11, -1);

    //log
    trace("method: createEntry: step -42");

    if (ent.getDuration()!= null && ent.getDuration().getBlocked() != null)
      ps.setInt(12, 1);
    else
      ps.setInt(12, 0);

    //log
    trace("method: createEntry: step -43");
    TodoData td = ent.getTodo();
    if (td != null) {
      Date d = td.getDue();
      if( d == null)
        d = getCurrentDate();
      Timestamp due = new Timestamp(d.getTime());
      due.setNanos(0);
      ps.setTimestamp(13, due);
    } else
      ps.setTimestamp(13, null);
    ps.setInt(14, ent.getTodo() != null && ent.getTodo().getCompletion() != null && ent.getTodo().getCompletion().getPercent() != null ? ent.getTodo().getCompletion().getPercent().intValue() : 0);
    ps.setTimestamp(15, ent.getTodo() != null && ent.getTodo().getCompletion() != null && ent.getTodo().getCompletion().getCompleted() != null ? new Timestamp(ent.getTodo().getCompletion().getCompleted().getTime()) : null);
    ps.setString(16, ent.getTitle());
    ps.setString(17, ent.getEvent() != null ? ent.getEvent().getStatus() : ent.getTodo().getStatus());
    ps.setInt(18, (ent.getEvent() != null && ent.getEvent().getPriority() != null) ? ent.getEvent().getPriority().intValue() : (ent.getTodo() != null && ent.getTodo().getPriority() != null) ? ent.getTodo().getPriority().intValue() : 3);
    ps.setString(19, (ent.getEvent() != null && ent.getEvent().getDescription() != null)? ent.getEvent().getDescription() : (ent.getTodo() != null && ent.getTodo().getDescription() != null) ? ent.getTodo().getDescription() : null);
    ps.setString(20, (ent.getEvent() != null && ent.getEvent().getCategory() != null) ? ent.getEvent().getCategory()[0] : (ent.getTodo() != null && ent.getTodo().getCategory() != null) ? ent.getTodo().getCategory()[0] : null);
    ps.setString(21, ent.getLocation());
    ps.executeUpdate();
    } finally {
          try {
            if (ps != null) ps.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
    }

    sql = "INSERT INTO DPCS_CAL_X_ENTRY VALUES ('" + quot(calid) + "','" + Integer.toString(ceid) + "'," + ceseq +")";
    //log
    trace("method: createEntry: sql " + sql);

    try {
    ps = db.prepareStatement(sql);
    ps.executeUpdate();
    } finally {
          try {
            if (ps != null) ps.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
    }

    //log
    trace("method: createEntry: sql " + sql);

    AttendeeData[] attr = ent.getAttendee();
    if (attr != null)
      for (int i = 0; i < attr.length; i++) {
        sql = "INSERT INTO DPCS_ATTENDEE VALUES (?,?,?,?,?,?,?)";

        try {
        ps = db.prepareStatement(sql);
        ps.setString(1, new Integer(ceid).toString());
        ps.setInt(2, ceseq);
        trace("IdentityData.SEPARATOR " + IdentityData.SEPARATOR);
        ps.setString(3, attr[i].getType()+IdentityData.SEPARATOR+attr[i].getEntityType()
                     +IdentityData.SEPARATOR+attr[i].getID()+IdentityData.SEPARATOR
                     +attr[i].getCuid()+IdentityData.SEPARATOR+attr[i].get());
        ps.setString(4, attr[i].getRole());
        if (attr[i].getCuid()!= null && attr[i].getCuid().equals(cal.getOwner()))
          ps.setString(5, "ACCEPTED");
        else
          ps.setString(5, attr[i].getStatus());
        ps.setInt(6, 1);
        ps.setString(7, attr[i].getComment());

        ps.executeUpdate();
        } finally {
          try {
            if (ps != null) ps.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }

    RecurrenceData[] recr = ent.getRecurrence();
    if (recr != null)
      for (int i = 0; i < recr.length; i++) {
        sql = "INSERT INTO DPCS_RECURRENCE VALUES (?,?,?,?,?,?,?,?,?)";

        try {
        ps = db.prepareStatement(sql);
        ps.setString(1, Integer.toString(ceid));
        ps.setInt(2, ceseq);
ps.setInt(3, recr[i].getExclude() != null ? (recr[i].getExclude().booleanValue() == true ? 1 : 0) : 0);
        if (recr[i].getDate() != null) {
          Timestamp day = new Timestamp(recr[i].getDate().getTime());
          day.setNanos(0);
          ps.setTimestamp(4, day);
        } else
          ps.setTimestamp(4, null);
        ps.setString(5, recr[i].getFrequency());
        ps.setString(6, recr[i].getByDay());
        ps.setInt(7, recr[i].getInterval() != null ? recr[i].getInterval().intValue() : -1);
        if (MAXTIMES >= 1)
          ps.setInt(8, recr[i].getCount() != null ? (int)Math.min(recr[i].getCount().intValue(),MAXTIMES) : -1);
        else
          ps.setInt(8, recr[i].getCount() != null ? recr[i].getCount().intValue() : -1);
        ps.setTimestamp(9, recr[i].getUntil() != null ? eor : null);

        ps.executeUpdate();
        } finally {
          try {
            if (ps != null) ps.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }

    return ent;
  }

  /**
   * Complete a todo
   * @param calid id of Calendar contains todo - java.lang.String
   * @param ceid <code>net.unicon.academus.apps.calendar.EntryRange</code> object contains todo's id
   * @param complete <code>net.unicon.academus.apps.calendar.CompletionData</code> object
   * @throws Exception
   */

  public void completeTodo(String calid, EntryRange ceid, CompletionData complete) throws Exception {
    ceid.m_mod = EntryRange.ALL;
    EntryData[] entr = fetchTodosByIds(calid, new EntryRange[]{ceid});
    EntryData ent = EntryData.findEntry(entr, ceid.m_ceid);
    if (ent != null) {
      TodoData td = ent.getTodo();
      if (td != null) {
        td.putCompletion(complete);
        ent.putTodo(td);
        updateEntry(calid, ceid, ent);
      }
    }
  }

  /**
   * Update Entry
   * @param calid id of Calendar contains entry - java.lang.String
   * @param ceid <code>net.unicon.academus.apps.calendar.EntryRange</code> object contains entry's id
   * @param ent <code>net.unicon.academus.apps.calendar.EntryData</code> object
   * @return <cdoe>EntryData</code> object had been updated
   * @throws Exception
   */

  public EntryData updateEntry(String calid, EntryRange ceid, EntryData ent) throws Exception {
    DBService db = null;

    try {
    db = getDBService(DS);
    db.begin();

    if (ent.getCeid() == null)
      if (ceid.m_ceid != null)
        ent.putCeid(ceid.m_ceid);

    if (!isRecurrence(ent.getCeid())) {
      try {
        trace("method updateEntry step 0 ent.getCeid() " + ent.getCeid());
        StringTokenizer token = new StringTokenizer(ceid.m_ceid, "_.");
        String ceid1 = token.nextToken();
        int ceseq = Integer.parseInt(token.nextToken());
        //log
        trace("method updateEntry ceid ceseq " +ceid1 + " " + ceseq);

        String sql = "UPDATE DPCS_ENTRY SET LAST_MODIFIED=?, ENTRY_DAY=?, EOR=?, LENGTH=?, DUE=?, PERCENTAGE=?, COMPLETED=?, TITLE=?,"
                     + " STATUS=?, PRIORITY=?, DESCRIPTION=?, CATEGORIES=?, LOCATION=?"
                     + " WHERE CEID=? AND CESEQ=?";
        PreparedStatement ps = null;

        try {
        ps = db.prepareStatement(sql);
        ps.setTimestamp(1, new Timestamp(getCurrentDate().getTime()));
        if (ent.getDuration() != null && ent.getDuration().getStart() != null) {
          Timestamp start = null;
          if (ent.getDuration().getLength()!= null && ent.getDuration().getLength().getAllDay()) {
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(ent.getDuration().getStart());
            startCal.setTimeZone(TimeZone.getTimeZone("GMT"));
            //startCal.add(Calendar.HOUR, +12);
            startCal.set(Calendar.HOUR, +12);
            start = new Timestamp(startCal.getTime().getTime());
          } else {
            start = new Timestamp(ent.getDuration().getStart().getTime());
          }
          start.setNanos(0);
          ps.setTimestamp(2, start);
        } else if (ent.getTodo()!= null && ent.getTodo().getDue() != null) {
          Timestamp due = new Timestamp(ent.getTodo().getDue().getTime());
          due.setNanos(0);
          ps.setTimestamp(2, due);
        }

        //log
        trace("method updateEntry step1");
        Timestamp eor = new Timestamp(generateEOR(ent).getTime());
        eor.setNanos(0);
        ps.setTimestamp(3, eor);
        //log
        trace("method updateEntry step1.1");

        if (ent.getDuration() != null && ent.getDuration().getLength() != null && ent.getDuration().getLength().getLength() > 0)
          ps.setInt(4, (int)ent.getDuration().getLength().getLength());
        else if (ent.getTodo() != null)
          ps.setInt(4, 0);
        else
          ps.setInt(4, -1);

        //log
        trace("method updateEntry step1.2");
        if(ent.getTodo() != null) {
          Timestamp due = new Timestamp(ent.getTodo().getDue().getTime());
          due.setNanos(0);
          ps.setTimestamp(5, due);
        } else
          ps.setTimestamp(5, null);

        ps.setInt(6, ent.getTodo() != null && ent.getTodo().getCompletion() != null && ent.getTodo().getCompletion().getPercent() != null ? ent.getTodo().getCompletion().getPercent().intValue() : 0);
        ps.setTimestamp(7, ent.getTodo() != null && ent.getTodo().getCompletion() != null && ent.getTodo().getCompletion().getCompleted() != null ? new Timestamp(ent.getTodo().getCompletion().getCompleted().getTime()) : null);
        ps.setString(8, ent.getTitle());

        //log
        trace("method updateEntry step1.3");

        ps.setString(9, ent.getEvent() != null ? ent.getEvent().getStatus() : ent.getTodo().getStatus());
        ps.setInt(10, ent.getEvent() != null ? ent.getEvent().getPriority().intValue() : ent.getTodo().getPriority().intValue());
        ps.setString(11, (ent.getEvent() != null && ent.getEvent().getDescription() != null)? ent.getEvent().getDescription() : (ent.getTodo() != null && ent.getTodo().getDescription() != null) ? ent.getTodo().getDescription() : null);

        //log
        trace("method updateEntry step1.4");

        ps.setString(12, (ent.getEvent() != null && ent.getEvent().getCategory() != null) ? ent.getEvent().getCategory()[0] : (ent.getTodo() != null && ent.getTodo().getCategory() != null) ? ent.getTodo().getCategory()[0] : null);

        //log
        trace("method updateEntry step1.5");

        ps.setString(13, ent.getLocation());

        //log
        trace("method updateEntry step1.6   ent.getCeid() + ceid.m_ceid " + ent.getCeid()+ " " + ceid.m_ceid);

        ps.setString(14, ceid1);
        ps.setInt(15, ceseq);

        //log
        trace("method updateEntry step2");

        ps.executeUpdate();
        } finally {
          try {
            if (ps != null) ps.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        //log
        trace("method updateEntry end update entry");

        //log
        trace("method updateEntry step3");
        CalendarData cal = CalendarData.findCalendar(m_calr, calid);
        AttendeeData[] attr = ent.getAttendee();
        if (attr != null )
          for (int i = 0; i < attr.length; i++)

            if (attr[i].status.equals("Added")) {
              sql = "INSERT INTO DPCS_ATTENDEE VALUES (?,?,?,?,?,?,?)";

              try {
              ps = db.prepareStatement(sql);

              /*
              if (ent.getCeid().indexOf(".") != -1)
              {
                StringTokenizer token = new StringTokenizer(ent.getCeid(), ".");
                String ceid1 = token.nextToken();
                int ceseq = Integer.parseInt(token.nextToken());

                ps.setString(1, ceid1);
                ps.setInt(2, ceseq);
              }
              else
              {
                ps.setString(1, ent.getCeid());
                ps.setInt(2, 0);
              }
              */
              ps.setString(1, ceid1);
              ps.setInt(2, ceseq);

              ps.setString(3, attr[i].getType()+IdentityData.SEPARATOR+attr[i].getEntityType()
                           +IdentityData.SEPARATOR+attr[i].getID()+IdentityData.SEPARATOR
                           +attr[i].getCuid()+IdentityData.SEPARATOR+attr[i].get());

              ps.setString(4, attr[i].getRole());
              if (attr[i].getCuid() != null && attr[i].getCuid().equals(cal.getOwner()))
                ps.setString(5, "ACCEPTED");
              else
                ps.setString(5, attr[i].getStatus());
              ps.setInt(6, 1);
              ps.setString(7, attr[i].getComment());
              ps.executeUpdate();
              } finally {
                try {
                  if (ps != null) ps.close();
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            }

        //log
        trace("method updateEntry step4");

        db.commit();
      } catch (Exception e) {
        //db.rollback(ERROR_RDBM);
        db.rollback(e.getMessage());
        e.printStackTrace(System.err);
        throw e; //
      }

      //log
      trace("method updateEntry step5");
    }
    // Entry repeat update 1 occurrences
    else {
      //db = getDBService(DS);
      try {
        StringTokenizer token = new StringTokenizer(ceid.m_ceid, "_.");
        String ceid1 = token.nextToken();
        int ceseq = Integer.parseInt(token.nextToken());
        long startDate = Long.parseLong(token.nextToken());

        int ceseqMax = getMaxInt(db, "DPCS_ENTRY", "CESEQ", "CEID='"+ceid1+"'");

        String sql = "INSERT INTO DPCS_RECURRENCE (CEID, CESEQ, EXCLUDE, RECUR_DAY) VALUES (?,?,?,?)";
        PreparedStatement ps = null;

        try {
        ps = db.prepareStatement(sql);

        //log
        trace("method updateEntry ceid1 ceseq " +ceid1 + " " + ceseq);

        ps.setString(1, ceid1);
        ps.setInt(2, ceseq);
        ps.setInt(3, 1);

        //log
        trace("method updateEntry step6");

        Timestamp start = new Timestamp(startDate);
        trace(" start ***  "+ start);
        start.setNanos(0);
        ps.setTimestamp(4, start);
        /*
        if (m_calr == null) throw new Exception(ERROR_CACHE);
        else
        {
          //CalendarData cal = CalendarData.findCalendar(m_calr, calid);
          //if (cal == null) throw new Exception(ERROR_INVALID_CALENDAR);
          //EntryData[] entr = cal.getEntry();
          //EntryData ent1 = EntryData.findEntry(entr, ent.getCeid());
          //Timestamp start = new Timestamp(ent1.getDuration().getStart().getTime());
          Timestamp start = new Timestamp(startDate);
          trace(" start***  "+ start);
          start.setNanos(0);
          ps.setTimestamp(4, start);
        }
        */
        //ps.setTimestamp(4, new Timestamp(ent.getDuration().getStart().getTime()));
        /*
        ps.setString(5, null);
        ps.setString(6, null);
        ps.setInt(7, -1);
        ps.setInt(8, -1);
        ps.setTime(9, null);
        */
        //log
        trace("method updateEntry step7");

        ps.executeUpdate();
        } finally {
          try {
            if (ps != null) ps.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        ent.putCeid(ceid1);
        /*
        if (ent.getAttendee() != null)
          for (int i = 0; i < ent.getAttendee().length; i++)
          {
            if (ent.getAttendee()[i].getCuid() != m_user)
              ent.getAttendee()[i].putStatus("NEEDS-ACTION");
          }
        */
        if (ent.getAttendee() != null)
          for (int i = 0; i < ent.getAttendee().length; i++) {
            if (ent.getAttendee()[i].getCuid()!= null && !ent.getAttendee()[i].getCuid().equals(m_user) && ent.getAttendee()[i].getStatus().equals("ACCEPTED")) {
              sql = "INSERT INTO DPCS_CAL_X_ENTRY VALUES (?,?,?)";

              try {
              ps = db.prepareStatement(sql);
              ps.setString(1, ent.getAttendee()[i].getCuid());
              ps.setString(2, ceid1);
              ps.setInt(3, ceseqMax+1);
              ps.executeUpdate();
              } finally {
                try {
                  if (ps != null) ps.close();
                  ps = null;
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            }
          }

        ent.removeE("recurrence");

        //log
        trace("method updateEntry step7.1");

        RecurrenceData rec = new RecurrenceData();
        if (ent.getDuration() != null)
          rec.putDate(ent.getDuration().getStart());
        else if (ent.getTodo() != null)
          rec.putDate(ent.getTodo().getDue());
        ent.putRecurrence(new RecurrenceData[]{rec});

        //log
        trace("method updateEntry step8");

        ent = createEntry(calid, ent, ceseqMax+1, db);

        //log
        trace("method updateEntry step9 ent.getCeid() " + ent.getCeid());

        db.commit();
      } catch (Exception e) {
        //db.rollback(ERROR_RDBM);
        db.rollback(e.getMessage());
        e.printStackTrace(System.err);
        throw e;//
      }
    }

    } finally {
      if (db != null) db.release();
    }

    //log
    trace("method updateEntry step6");

    return ent;
  }

  /**
   *
   * @param calidr
   * @param from
   * @param to
   * @param filter
   * @throws Exception
   */

  void deleteEntries(String[] calidr, Date from, Date to, String filter) throws Exception {
    if (calidr == null)
      throw new Exception(ERROR_CAL_NULL);

    DBService db = null;

    try {
      db = getDBService(DS);
      db.begin();
      PreparedStatement ps = null;

      for (int i = 0; i < calidr.length; i++) {
        String sql = "UPDATE DPCS_ENTRY SET REFS = REFS-1"
                     + " WHERE CEID IN (SELECT CEID FROM DPCS_CAL_X_ENTRY, DPCS_ENTRY"
                     + " WHERE CALID = ? AND DPCS_ENTRY.CEID = DPCS_CAL_X_ENTRY.CEID"
                     + " AND DPCS_ENTRY.CESEQ = DPCS_CAL_X_ENTRY.CESEQ"
                     + " AND ENTRY_DAY <= ? AND EOR >= ? AND " + filter +")";

        try {
        ps = db.prepareStatement(sql);
        ps.setString(1, calidr[i]);
        ps.setTimestamp(2, new Timestamp(from.getTime()));
        ps.setTimestamp(3, new Timestamp(to.getTime()));
        ps.executeUpdate();
        } finally {
          try {
            if (ps != null) ps.close();
            ps = null;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }

      String sql = "DELETE FROM DPCS_RECURRENCE WHERE CEID IN (SELECT CEID FROM DPCS_ENTRY WHERE REFS=0)";

      try {
        ps = db.prepareStatement(sql);
        ps.executeUpdate();
      } finally {
        try {
          if (ps != null) ps.close();
          ps = null;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      sql = "DELETE FROM DPCS_ATTENDEE WHERE CEID IN (SELECT CEID FROM DPCS_ENTRY WHERE REFS=0)";

      try {
        ps = db.prepareStatement(sql);
        ps.executeUpdate();
      } finally {
        try {
          if (ps != null) ps.close();
          ps = null;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      sql = "DELETE FROM DPCS_ENTRY WHERE CEID IN (SELECT CEID FROM DPCS_ENTRY WHERE REFS=0)";

      try {
        ps = db.prepareStatement(sql);
        ps.executeUpdate();
      } finally {
        try {
          if (ps != null) ps.close();
          ps = null;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      sql = "DELETE FROM DPCS_CAL_X_ENTRY WHERE CEID IN (SELECT CEID FROM DPCS_ENTRY WHERE REFS=0)";
      try {
        ps = db.prepareStatement(sql);
        ps.executeUpdate();
      } finally {
        try {
          if (ps != null) ps.close();
          ps = null;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      db.commit();
    } catch (Exception e) {
      db.rollback(ERROR_RDBM);
      e.printStackTrace(System.err);
      throw e;//
    } finally {
      if (db != null) db.release();
    }
  }

  /**
   *
   * @param calid
   * @param ceidr
   * @param filter
   * @throws Exception
   */

  void deleteEntries(String calid, EntryRange[] ceidr, String filter) throws Exception {
    if (ceidr == null)
      throw new Exception(ERROR_CEIDR_NULL);

    DBService db = getDBService(DS);
    db.begin();

    try {
      for (int i = 0; i < ceidr.length; i++) {
        // prepare ceid, ceseq
        StringTokenizer token = new StringTokenizer(ceidr[i].m_ceid, "_.");
        String ceid = token.nextToken();
        int ceseq = Integer.parseInt(token.nextToken());

        // delete entries no repeate
        if (!isRecurrence(ceidr[i].m_ceid)) // && ceidr[i].m_mod == 0)
        {
          //log
          trace("method: deleteEntries step -1");
          deleteOneEntry(db, calid, ceid, ceseq, filter);
        } else // delete entries have repeat
        {
          // delete all past + future entries
          if (ceidr[i].m_mod == 3)
            deleteAllOccurences(db, calid, ceid, ceseq, filter);
          else // delete past entries or future entries or one occurence
          {
            //<*****>//
            //log
            trace("method: deleteEntries step 33 ceid va ceseq " + ceid + " " + ceseq);
            //log
            trace("method: deleteEntries step 4.0  ceid " + ceidr[i].m_ceid);
            Date day = null;
            try {
              day = new Date(Long.parseLong(token.nextToken()));
            } catch (Exception e) {}
            Date nextDay = null;
            int nextCount = 0;
            Date prevDay = null;
            //log
            trace("method: deleteEntries step 4");
            // searching prevday & nextday
            /* remove update cache
            CalendarData cal = CalendarData.findCalendar(m_calr, calid);
            EntryData[] entr = cal.getEntry();
            */
            EntryData[] entr = fetchEntriesByIds(calid, new EntryRange[]{new EntryRange(ceidr[i].m_ceid, EntryRange.ALL)});
            if (entr == null)
              trace("cache error") ;
            for (int k = 0; k < entr.length; k++) {
              trace("method: deleteEntries step 4.1.0");
              if (entr[k].getCeid().equals(ceidr[i].m_ceid)) {
                //trace("method: deleteEntries step 4.1.1 " + entr[k].isAcceptedInvitation(cal, m_user));
                //if (k+1 < entr.length && entr[k].getCreated().equals(entr[k+1].getCreated()))
                for (int k1 = k+1; k1 < entr.length; k1++) {
                  //if ( (k+1 < entr.length) && entr[k+1].getCeid().startsWith(ceid+"_") )
                  if ( entr[k1].getCeid().startsWith(ceid+"_") && entr[k1].getDuration().getStart().after(day) ) {
                    nextDay = entr[k1].getDuration().getStart();
                    //14/4
                    /*
                    if (entr[k].getRecurrence()[0].getUntil() == null)
                      for (int l = k+1; l < entr.length; l++)
                        //if (entr[k].getCreated().equals(entr[l].getCreated()))
                        if (entr[k].getCeid().startsWith(entr[l].getCeid().substring(0, entr[l].getCeid().indexOf("_")+1)) )
                          nextCount++;
                    */
                    for (int l = k1; l < entr.length; l++) {
                      //if (entr[k].getCreated().equals(entr[l].getCreated()))
                      if (entr[l].getCeid().startsWith(ceid+"_") )
                        nextCount++;
                    }
                    //
                    break;
                  }
                }

                for (int k2 = k-1; k2 >= 0; k2--) {
                  //if (k-1 >= 0  && entr[k].getCreated().equals(entr[k-1].getCreated()))
                  //if ( (k-1 >= 0) && entr[k-1].getCeid().startsWith(ceid+"_") )
                  if ( entr[k2].getCeid().startsWith(ceid+"_")  && entr[k2].getDuration().getStart().before(day)) {
                    prevDay = entr[k2].getDuration().getStart();
                    trace("delete entries entr[k2].getCeid prevDay " +entr[k2].getCeid() +"***" + prevDay);
                    break;
                  }
                }

              }
            }
            //log
            trace("method: deleteEntries step 4.2");

            //
            //searchEntPos(calid, ceidr[i].m_ceid, nextDay, prevDay, nextCount);
            trace ("method: deleteEntries nextDay + prevDay + nextCount "+ nextDay +" ** "+ prevDay + " ** "+nextCount);

            // mod = 1 delete past occurences
            if (ceidr[i].m_mod == 1) {
              if (nextDay != null) // have future ocurences
                deletePastOccurences(db, nextDay, nextCount, calid, ceid, ceseq, filter);
              else // not have future ocurrences -> delete entry
                deleteAllOccurences(db, calid, ceid, ceseq, filter);
            }
            // mod = 2 delete to future
            else if (ceidr[i].m_mod == 2) {
              if (prevDay != null) // have past ocurrence -> modify until
                deleteFutureOccurences(db, prevDay, calid, ceid, ceseq, filter);
              else // not have past ocurrence -> delete entry
                deleteAllOccurences(db, calid, ceid, ceseq, filter);
            }
            // delete one ocurrence
            else if (ceidr[i].m_mod == 0) {
              if (prevDay == null && nextDay == null) // not have past and future -> delete entry
                deleteAllOccurences(db, calid, ceid, ceseq, filter);
              else if (prevDay == null && nextDay != null) // delete past occurence
                deletePastOccurences(db, nextDay, nextCount, calid, ceid, ceseq, filter);
              else if (nextDay == null && prevDay != null) // delete future occurence
                deleteFutureOccurences(db, prevDay, calid, ceid, ceseq, filter);
              else if (nextDay != null && prevDay != null) // exclude one
              {
                deleteOneOccurences(db, day, calid, ceid, ceseq, filter);
              }
            }
            //<*****>//
          }
        }
      }
      db.commit();
    } catch (Exception e) {
      db.rollback(ERROR_RDBM);
      e.printStackTrace(System.err);
      throw e;
    } finally {
      db.release();
    }

    /*remove update cache
    // update cache
    m_calr = fetchEntries(m_calr, null, null);
    */
    //log
    trace("method: deleteEntries end");
  }

  //--------------------------------------------------------------------------//

  /**
   *
   * @param db
   * @param calid
   * @param ceid
   * @param ceseq
   * @param filter
   * @throws Exception
   */

  void deleteOneEntry(DBService db, String calid, String ceid, int ceseq, String filter) throws Exception {
    // mod = 0
    String sql = "UPDATE DPCS_ENTRY SET REFS = REFS-1"
                 + " WHERE CEID = ? AND CESEQ = ? AND "
                 + filter;
    PreparedStatement ps = null;

    try {
    ps = db.prepareStatement(sql);
    ps.setString(1, ceid);
    ps.setInt(2, ceseq);
    ps.executeUpdate();
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    sql = "DELETE FROM DPCS_CAL_X_ENTRY WHERE DPCS_CAL_X_ENTRY.CEID = ?"
          + " AND DPCS_CAL_X_ENTRY.CESEQ = ? AND CALID = ?";

    try {
    ps = db.prepareStatement(sql);
    ps.setString(1, ceid);
    ps.setInt(2, ceseq);
    ps.setString(3, calid);
    ps.executeUpdate();
    } finally {
	try {
	    if (ps != null) 
			ps.close();
	    ps = null;
	} catch (Exception e) {
	    e.printStackTrace();
	}	
    }

    //log
    trace("method: deleteEntries step 1");

    sql = "DELETE FROM DPCS_RECURRENCE WHERE DPCS_RECURRENCE.CEID IN (SELECT DPCS_ENTRY.CEID FROM DPCS_ENTRY WHERE REFS=0)"
          + " AND DPCS_RECURRENCE.CESEQ IN (SELECT DPCS_ENTRY.CESEQ FROM DPCS_ENTRY WHERE REFS=0)";

    try {
      ps = db.prepareStatement(sql);
      ps.executeUpdate();
    } finally {
	try {
	    if (ps != null) 
			ps.close();
	    ps = null;
	} catch (Exception e) {
	    e.printStackTrace();
	}	
     }
    

    sql = "DELETE FROM DPCS_ATTENDEE WHERE DPCS_ATTENDEE.CEID IN (SELECT DPCS_ENTRY.CEID FROM DPCS_ENTRY WHERE REFS=0)"
          + " AND DPCS_ATTENDEE.CESEQ IN (SELECT DPCS_ENTRY.CESEQ FROM DPCS_ENTRY WHERE REFS=0)";

    try {
      ps = db.prepareStatement(sql);
      ps.executeUpdate();
    } finally {
	try {
	    if (ps != null) 
			ps.close();
	    ps = null;
	} catch (Exception e) {
	    e.printStackTrace();
	}	
     }

    sql = "DELETE FROM DPCS_ENTRY WHERE REFS=0";

    try {
      ps = db.prepareStatement(sql);
      ps.executeUpdate();
    } finally {
	try {
	    if (ps != null) 
			ps.close();
	    ps = null;
	} catch (Exception e) {
	    e.printStackTrace();
	}	
     }
  }

  /**
   *
   * @param db
   * @param calid
   * @param ceid
   * @param ceseq
   * @param filter
   * @throws Exception
   */

  void deleteAllOccurences(DBService db, String calid, String ceid, int ceseq, String filter) throws Exception {
    String sql = "UPDATE DPCS_ENTRY SET REFS = REFS-1"
                 + " WHERE CEID = ?"
                 + " AND " +  filter;
    PreparedStatement ps = null;

    try {
    ps = db.prepareStatement(sql);
    ps.setString(1, ceid);
    ps.executeUpdate();
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    sql = "DELETE FROM DPCS_CAL_X_ENTRY WHERE DPCS_CAL_X_ENTRY.CEID = ?"
          + " AND CALID = ?";

    try {
    ps = db.prepareStatement(sql);
    ps.setString(1, ceid);
    ps.setString(2, calid);
    ps.executeUpdate();
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    sql = "DELETE FROM DPCS_RECURRENCE WHERE DPCS_RECURRENCE.CEID IN (SELECT DPCS_ENTRY.CEID FROM DPCS_ENTRY WHERE REFS=0)";
    try {
    ps = db.prepareStatement(sql);
    ps.executeUpdate();
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    sql = "DELETE FROM DPCS_ATTENDEE WHERE DPCS_ATTENDEE.CEID IN (SELECT DPCS_ENTRY.CEID FROM DPCS_ENTRY WHERE REFS=0)";
    try {
    ps = db.prepareStatement(sql);
    ps.executeUpdate();
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    sql = "DELETE FROM DPCS_ENTRY WHERE REFS=0";
    try {
    ps = db.prepareStatement(sql);
    ps.executeUpdate();
        } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   *
   * @param db
   * @param prevDay
   * @param calid
   * @param ceid
   * @param ceseq
   * @param filter
   * @throws Exception
   */

  void deleteFutureOccurences(DBService db, Date prevDay, String calid, String ceid, int ceseq, String filter) throws Exception {
    String sql = "UPDATE DPCS_RECURRENCE SET UNTIL=?, REPETITION=NULL WHERE CEID=? AND CESEQ=? AND EXCLUDE=?";
    PreparedStatement ps = null;

    try {
    ps = db.prepareStatement(sql);
    ps.setTimestamp(1, new Timestamp(prevDay.getTime()));
    ps.setString(2, ceid);
    trace("deleteFutureOccurences ceid " + ceid + " prevDay " +  prevDay);
    ps.setInt(3, 0);
    ps.setInt(4, 0);
    ps.executeUpdate();
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    Timestamp pastDay = new Timestamp(prevDay.getTime());
    pastDay.setNanos(0);

    sql = "DELETE FROM DPCS_ATTENDEE WHERE DPCS_ATTENDEE.CEID IN (SELECT DPCS_ENTRY.CEID FROM DPCS_ENTRY WHERE CEID=? AND ENTRY_DAY >?)"
          + " AND DPCS_ATTENDEE.CESEQ IN (SELECT DPCS_ENTRY.CESEQ FROM DPCS_ENTRY WHERE CEID=? AND ENTRY_DAY >?)";

    try {
    ps = db.prepareStatement(sql);
    ps.setString(1, ceid);
    ps.setTimestamp(2, pastDay);
    ps.setString(3, ceid);
    ps.setTimestamp(4, pastDay);
    ps.executeUpdate();
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    sql = "DELETE FROM DPCS_RECURRENCE WHERE DPCS_RECURRENCE.CEID IN (SELECT DPCS_ENTRY.CEID FROM DPCS_ENTRY WHERE CEID=? AND ENTRY_DAY >?)"
          + " AND DPCS_RECURRENCE.CESEQ IN (SELECT DPCS_ENTRY.CESEQ FROM DPCS_ENTRY WHERE CEID=? AND ENTRY_DAY >?)";
    try {
    ps = db.prepareStatement(sql);
    ps.setString(1, ceid);
    ps.setTimestamp(2, pastDay);
    ps.setString(3, ceid);
    ps.setTimestamp(4, pastDay);
    ps.executeUpdate();
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    sql = "DELETE FROM DPCS_CAL_X_ENTRY WHERE DPCS_CAL_X_ENTRY.CEID IN (SELECT DPCS_ENTRY.CEID FROM DPCS_ENTRY WHERE CEID=? AND ENTRY_DAY >?)"
          + " AND DPCS_CAL_X_ENTRY.CESEQ IN (SELECT DPCS_ENTRY.CESEQ FROM DPCS_ENTRY WHERE CEID=? AND ENTRY_DAY >?)";
    try {
    ps = db.prepareStatement(sql);
    ps.setString(1, ceid);
    ps.setTimestamp(2, pastDay);
    ps.setString(3, ceid);
    ps.setTimestamp(4, pastDay);
    ps.executeUpdate();
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }


    sql = "DELETE FROM DPCS_ENTRY WHERE CEID=? AND ENTRY_DAY >?";
    try {
    ps = db.prepareStatement(sql);
    ps.setString(1, ceid);
    ps.setTimestamp(2, pastDay);
    ps.executeUpdate();
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   *
   * @param db
   * @param nextDay
   * @param nextCount
   * @param calid
   * @param ceid
   * @param ceseq
   * @param filter
   * @throws Exception
   */

  void deletePastOccurences(DBService db, Date nextDay, int nextCount, String calid, String ceid, int ceseq, String filter) throws Exception {
    String sql = "UPDATE DPCS_ENTRY SET ENTRY_DAY=? WHERE CEID=? AND CESEQ=?";
    PreparedStatement ps = null;

    try {
    ps = db.prepareStatement(sql);
    ps.setTimestamp(1, new Timestamp(nextDay.getTime()));
    ps.setString(2, ceid);
    ps.setInt(3, 0);
    ps.executeUpdate();
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (nextCount > 0) {
      sql = "UPDATE DPCS_RECURRENCE SET REPETITION=?, UNTIL=NULL WHERE CEID=? AND CESEQ=? AND EXCLUDE!=?";
      try {
      ps = db.prepareStatement(sql);
      ps.setInt(1, nextCount);
      ps.setString(2, ceid);
      ps.setInt(3, 0);
      ps.setInt(4, 1);
      ps.executeUpdate();
      } finally {
        try {
          if (ps != null) ps.close();
          ps = null;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    sql = "DELETE FROM DPCS_ATTENDEE WHERE DPCS_ATTENDEE.CEID IN (SELECT DPCS_ENTRY.CEID FROM DPCS_ENTRY WHERE CEID=? AND ENTRY_DAY < {ts '" + new Timestamp(nextDay.getTime()) + "'} )"
          + " AND DPCS_ATTENDEE.CESEQ IN (SELECT DPCS_ENTRY.CESEQ FROM DPCS_ENTRY WHERE CEID=? AND ENTRY_DAY <{ts '" + new Timestamp(nextDay.getTime()) + "'} )";

    try {
    ps = db.prepareStatement(sql);
    ps.setString(1, ceid);
    ps.setString(2, ceid);
    ps.executeUpdate();
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    sql = "DELETE FROM DPCS_RECURRENCE WHERE DPCS_RECURRENCE.CEID IN (SELECT DPCS_ENTRY.CEID FROM DPCS_ENTRY WHERE CEID=? AND ENTRY_DAY < {ts '" + new Timestamp(nextDay.getTime()) + "'} )"
          + " AND DPCS_RECURRENCE.CESEQ IN (SELECT DPCS_ENTRY.CESEQ FROM DPCS_ENTRY WHERE CEID=? AND ENTRY_DAY <{ts '" + new Timestamp(nextDay.getTime()) + "'} )";

    try {
    ps = db.prepareStatement(sql);
    ps.setString(1, ceid);
    ps.setString(2, ceid);
    ps.executeUpdate();
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    sql = "DELETE FROM DPCS_CAL_X_ENTRY WHERE DPCS_CAL_X_ENTRY.CESEQ IN (SELECT DPCS_ENTRY.CESEQ FROM DPCS_ENTRY WHERE CEID=? AND ENTRY_DAY <{ts '" + new Timestamp(nextDay.getTime()) + "'} )";
    try {
    ps = db.prepareStatement(sql);
    ps.setString(1, ceid);
    ps.executeUpdate();
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    sql = "DELETE FROM DPCS_ENTRY WHERE CEID=? AND ENTRY_DAY <?";
    try {
    ps = db.prepareStatement(sql);
    ps.setString(1, ceid);
    ps.setTimestamp(2, new Timestamp(nextDay.getTime()));
    ps.executeUpdate();
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

}

  /**
   *
   * @param db
   * @param day
   * @param calid
   * @param ceid
   * @param ceseq
   * @param filter
   * @throws Exception
   */

  void deleteOneOccurences(DBService db, Date day, String calid, String ceid, int ceseq, String filter) throws Exception {
    String sql = "INSERT INTO DPCS_RECURRENCE (CEID, CESEQ, EXCLUDE, RECUR_DAY) VALUES (?,?,?,?)";
    PreparedStatement ps = null;

    try {
    ps = db.prepareStatement(sql);
    ps.setString(1, ceid);
    ps.setInt(2, ceseq);
    ps.setInt(3, 1);
    //log
    trace("method deleteEntries step 51 day " + day.toString());
    ps.setTimestamp(4, new Timestamp(day.getTime()));
    ps.executeUpdate();
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  void searchEntPos(String calid, String ceid, Date nextDay, Date prevDay, int nextCount) throws Exception {
    // searching prevday & nextday & nextcount
    for (int j = 0; j < m_calr.length; j++) {
      //log
      trace("method: deleteEntries step 4.0  ceid " + ceid);
      if (m_calr[j].getCalid().equals(calid)) {
        //log
        trace("method: deleteEntries step 4.1 m_calr[j] " + m_calr[j]);
        EntryData[] entr = m_calr[j].getEntry();
        if (entr == null)
          trace("cache error") ;
        for (int k = 0; k < entr.length; k++) {
          trace("method: deleteEntries step 4.1.0");
          if (entr[k].getCeid().equals(ceid)) {
            trace("method: deleteEntries step 4.1.1");
            if (k+1 < entr.length && entr[k].getCreated().equals(entr[k+1].getCreated())) {
              nextDay = entr[k+1].getDuration().getStart();
              if (entr[k].getRecurrence()[0].getUntil() == null)
                for (int l = k+1; l < entr.length; l++)
                  if (entr[k].getCreated().equals(entr[l].getCreated()))
                    nextCount++;
            }
            if (k-1 >= 0  && entr[k].getCreated().equals(entr[k-1].getCreated()))
              prevDay = entr[k-1].getDuration().getStart();
          }
        }
        //log
        trace("method: deleteEntries step 4.2");
      }
    }

    trace ("method: search pos nextDay + prevDay + nextCount "+ nextDay +" ** "+ prevDay + " ** "+nextCount);
  }

  //--------------------------------------------------------------------------//

  /**
   * Delete all Events in Calendars
   * @param calidr Array of java.lang.String contains calendar ids
   * @param from java.util.Date
   * @param to java.util.Date
   * @throws Exception
   */

  public void deleteEvents(String[] calidr, Date from, Date to) throws Exception {
    //deleteEntries(calidr, from, to, "DUE is null");
    deleteEntries(calidr, from, to, "LENGTH != 0");
  }

  /**
   * Delete all Todos in Calendars
   * @param calidr Array of java.lang.String contains calendar ids
   * @param from java.util.Date
   * @param to java.util.Date
   * @throws Exception
   */

  public void deleteTodos(String[] calidr, Date from, Date to) throws Exception {
    deleteEntries(calidr, from, to, "DUE is not null");
  }

  /**
   * Delete events. In case recurrent event It can delete one or past or future occurences
   * @param calid java.lang.String - id of calendar contains event
   * @param ceidr Array of <code>net.unicon.academus.apps.calendar.EntryRange</code> objects
   * @throws Exception
   */

  public void deleteEvents(String calid, EntryRange[] ceidr) throws Exception {
    deleteEntries(calid, ceidr, "LENGTH != 0");
  }

  /**
   * Delete todos. In case recurrent todo It can delete one or past or future occurences
   * @param calid java.lang.String - id of calendar contains todo
   * @param ceidr Array of <code>net.unicon.academus.apps.calendar.EntryRange</code> objects
   * @throws Exception
   */

  public void deleteTodos(String calid, EntryRange[] ceidr) throws Exception {
    deleteEntries(calid, ceidr, "DUE is not null");
  }

  //--------------------------------------------------------------------------//

  /**
   * Import entries from Tab delimited file
   * @param is <code>java.io.InputStream</code>
   * @param format "TODO" or "EVENT"
   * @param calid Calendar ID
   * @param map <code>Hashtable</code>
   * @return <code>java.lang.String</code> Error message return "" if no error
   * @throws Exception
   */

  public String importEntries(InputStream is, String format, String calid, Hashtable map) throws Exception {
    //System.out.println("importEntries 1");
    String error = "";
    Hashtable data[] = net.unicon.academus.apps.calendar.ImExUtil.loadDataFromTabDelimitedFile(is);
    //System.out.println("importEntries 2 data " + data);
    EntryData[] entr = net.unicon.academus.apps.calendar.ImExUtil.importEntry(data, map, format, error);
    //System.out.println("importEntries 3 entr " + entr);
    if (entr != null && entr.length != 0) {
      //System.out.println("importEntries 4 ");
      for (int i = 0; i < entr.length; i++) {
        //System.out.println("importEntries create ");
        createEntry(calid, entr[i], 0);
      }
    }
    return error;
  }

  /**
   * Import entries from Tab delimited file
   * @param is <code>java.io.InputStream</code>
   * @param format "TODO" or "EVENT"
   * @param calid Calendar ID
   * @param map <code>Hashtable</code>
   * @return <code>java.lang.String</code> Error message return "" if no error
   * @throws Exception
   */

  public InputStream exportEntries(String format, String[] calids, Date from, Date to) throws Exception {
    if (calids == null)
      return null;
    CalendarData[] calr = new CalendarData[calids.length];
    for (int i = 0; i < calids.length; i++) {
      calr[i] = CalendarData.findCalendar(m_calr, calids[i]);
      trace("calids[i] " + calids[i] +" calr[i] " + calr[i]);
    }
    if (format != null && format.equals(EVENT))
      calr = fetchEvents(calr, from, to);
    else if (format != null && format.equals(TODO))
      calr = fetchTodos(calr, from, to);
    if (calr != null && calr.length != 0) {
      Vector v = new Vector();
      for (int i = 0; i < calr.length; i++) {
        EntryData[] entr = calr[i] != null ? calr[i].getEntry() : null;
        if (entr != null)
          for (int j = 0; j < entr.length; j++)
            if (entr[j] != null)
              v.addElement(entr[j]);
      }
      if (v.size() != 0)
        return net.unicon.academus.apps.calendar.ImExUtil.exportEntryStream((EntryData[])v.toArray(new EntryData[]{new EntryData()}));
    }

    return null;
  }

  //--------------------------------------------------------------------------//

  void generateEntOccurences(Vector entv, EntryData ent) throws Exception {
    if (ent.getRecurrence() != null) {
      //log
      trace("generateEntOccurences step1 ent.getRecurrence().length " + ent.getRecurrence().length);
      for (int i = 0; i < ent.getRecurrence().length; i++) {
        if (ent.getRecurrence()[i] != null && ent.getRecurrence()[i].getUntil() != null && (ent.getRecurrence()[i].getExclude() == null || ent.getRecurrence()[i].getExclude().equals(FALSE)) ) {
          //log
          trace("method generateEntOccurences repeat until");
          if (ent.getRecurrence()[i].getFrequency().equals("DAILY") && ent.getRecurrence()[i].getInterval() != null)
            generateEntUntilDaily(entv, ent, i);
          else if (ent.getRecurrence()[i].getFrequency().equals("WEEKLY") && ent.getRecurrence()[i].getInterval()!= null) {
            if (ent.getRecurrence()[i].getByDay() == null )
              generateEntUntilWeekly(entv, ent, i);
            else
              generateEntUntilWeeklyByDay(entv, ent, i);
          } else if (ent.getRecurrence()[i].getFrequency().equals("MONTHLY") && ent.getRecurrence()[i].getInterval() != null)
            generateEntUntilMonthly(entv, ent, i);
          else if (ent.getRecurrence()[i].getFrequency().equals("YEARLY") && ent.getRecurrence()[i].getInterval() != null)
            generateEntUntilYearly(entv, ent, i);
        } else if (ent.getRecurrence()[i] != null && ent.getRecurrence()[i].getCount() != null && ent.getRecurrence()[i].getCount().intValue() > 0 && (ent.getRecurrence()[i].getExclude() == FALSE || ent.getRecurrence()[i].getExclude() == null)) {
          //log
          trace("method generateEntOccurences repeat count");
          if (ent.getRecurrence()[i].getFrequency().equals("DAILY") && ent.getRecurrence()[i].getInterval() != null)
            generateEntCountDaily(entv, ent, i);
          else if (ent.getRecurrence()[i].getFrequency().equals("WEEKLY") && ent.getRecurrence()[i].getInterval()!= null) {
            if (ent.getRecurrence()[i].getByDay() == null )
              generateEntCountWeekly(entv, ent, i);
            else
              generateEntCountWeeklyByDay(entv, ent, i);
          } else if (ent.getRecurrence()[i].getFrequency().equals("MONTHLY") && ent.getRecurrence()[i].getInterval() != null)
            generateEntCountMonthly(entv, ent, i);
          else if (ent.getRecurrence()[i].getFrequency().equals("YEARLY") && ent.getRecurrence()[i].getInterval() != null)
            generateEntCountYearly(entv, ent, i);
        } else if (ent.getRecurrence()[i] != null && (ent.getRecurrence()[i].getExclude() == null || ent.getRecurrence()[i].getExclude().equals(FALSE) ) && ent.getRecurrence()[i].getDate() != null
                   && ent.getRecurrence()[i].getFrequency() == null //&& (ent.getRecurrence()[i].getInterval() == null || ent.getRecurrence()[i].getInterval() == -1)
                   && ent.getRecurrence()[i].getUntil() == null) //&& ent.getRecurrence()[i].getCount() == null)
          generateEntByDay(entv, ent, i);

        //else if (ent.getRecurrence()[i] != null && ent.getRecurrence()[i].getExclude() != null && ent.getRecurrence()[i].getExclude().equals(TRUE) )//&& ent.getRecurrence()[i].getUntil() == null && ent.getRecurrence()[i].getCount() == null)
        //  exclude(entv, ent, i);

      }
      for (int i = 0; i < ent.getRecurrence().length; i++) {
        if (ent.getRecurrence()[i] != null && ent.getRecurrence()[i].getExclude() != null && ent.getRecurrence()[i].getExclude().equals(TRUE) ) //&& ent.getRecurrence()[i].getUntil() == null && ent.getRecurrence()[i].getCount() == null)
          exclude(entv, ent, i);
      }

    }
  }

  //--------------------------------------------------------------------------//

  void generateEntUntilDaily(Vector entv, EntryData ent, int i) throws Exception {
    Date start = ent.getDuration() != null ? ent.getDuration().getStart() : ent.getTodo() != null ? ent.getTodo().getDue() : null;
    int interval = ent.getRecurrence()[i].getInterval().intValue();
    long next = start.getTime() + 24*3600000*interval;
    long until = ent.getRecurrence()[i].getUntil().getTime();
    if (MAXTIMES >= 1) {
      int n = 1;
      while ( next <= until && n < MAXTIMES) {
        entv.addElement(generateEnt(ent, new Date(next)));
        next += 24*3600000*interval;
        n++;
      }
    } else
      while ( next <= until) {
        entv.addElement(generateEnt(ent, new Date(next)));
        next += 24*3600000*interval;
      }
  }

  void generateEntUntilWeekly(Vector entv, EntryData ent, int i) throws Exception {
    Date start = ent.getDuration() != null ? ent.getDuration().getStart() : ent.getTodo() != null ? ent.getTodo().getDue() : null;
    int interval = ent.getRecurrence()[i].getInterval().intValue();
    Calendar next = Calendar.getInstance();
    next.setTime(start);
    next.add(next.WEEK_OF_MONTH, +interval);
    Date until = ent.getRecurrence()[i].getUntil();
    if (MAXTIMES >= 1) {
      int n = 1;
      while ((next.getTime().before(until) || next.getTime().equals(until)) && n < MAXTIMES) {
        entv.addElement(generateEnt(ent, next.getTime()));
        next.add(next.WEEK_OF_MONTH, +interval);
        n++;
      }
    } else
      while (next.getTime().before(until) || next.getTime().equals(until)) {
        entv.addElement(generateEnt(ent, next.getTime()));
        next.add(next.WEEK_OF_MONTH, +interval);
      }
  }

  void generateEntUntilWeeklyByDay(Vector entv, EntryData ent, int i) throws Exception {
    Date start = ent.getDuration() != null ? ent.getDuration().getStart() : ent.getTodo() != null ? ent.getTodo().getDue() : null;
    Date until = ent.getRecurrence()[i].getUntil();
    Calendar next = Calendar.getInstance();
    next.setTime(start);
    int interval = ent.getRecurrence()[i].getInterval().intValue();
    //log
    trace("next.DAY_OF_WEEK step 0 " + next.get(Calendar.DAY_OF_WEEK));
    boolean found = false;
    int byDayIdx = 0;
    String byDay = ent.getRecurrence()[i].getByDay();
    for (int q = 0; q < byDay.length(); q++) {
      if (next.get(Calendar.DAY_OF_WEEK) < Integer.parseInt( String.valueOf(byDay.charAt(q))) ) {
        //log
        trace("Integer.parseInt( String.valueOf(byDay.charAt(q))) " + Integer.parseInt( String.valueOf(byDay.charAt(q))));

        next.set(Calendar.DAY_OF_WEEK, Integer.parseInt( String.valueOf(byDay.charAt(q))) );
        byDayIdx = q;
        found = true;
        break;
      }
    }

    //log
    trace("next.DAY_OF_WEEK 2 " + next.get(Calendar.DAY_OF_WEEK));

    if (!found) {
      next.add(Calendar.WEEK_OF_MONTH, +1*interval);
      next.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
      for (int q = 0; q < byDay.length(); q++) {
        if (next.get(Calendar.DAY_OF_WEEK) <= Integer.parseInt(String.valueOf(byDay.charAt(q))) ) {
          next.set(Calendar.DAY_OF_WEEK, Integer.parseInt(String.valueOf(byDay.charAt(q))) );
          byDayIdx = q;
          break;
        }
      }
    }

    //log
    trace("next.DAY_OF_WEEK 3 " + next.get(Calendar.DAY_OF_WEEK));

    if (MAXTIMES >= 1) {
      int n = 1;
      while ( (next.getTime().before(until) || next.getTime().equals(until)) && n < MAXTIMES) {
        entv.addElement(generateEnt(ent, next.getTime()));
        n++;
        if (byDayIdx+1 < byDay.length()) {
          next.set(Calendar.DAY_OF_WEEK, Integer.parseInt(String.valueOf(byDay.charAt(byDayIdx+1))) );
          ++byDayIdx;
        } else {
          next.add(Calendar.WEEK_OF_MONTH, +1*interval);
          next.set(Calendar.DAY_OF_WEEK, +Calendar.SUNDAY);
          for (int q = 0; q < byDay.length(); q++) {
            if (next.get(Calendar.DAY_OF_WEEK) <= Integer.parseInt(String.valueOf(byDay.charAt(q)))) {
              next.set(Calendar.DAY_OF_WEEK, Integer.parseInt(String.valueOf(byDay.charAt(q))) );
              byDayIdx = q;
              break;
            }
          }
        }
      }
    } else {
      while (next.getTime().before(until) || next.getTime().equals(until)) {
        entv.addElement(generateEnt(ent, next.getTime()));
        if (byDayIdx+1 < byDay.length()) {
          next.set(Calendar.DAY_OF_WEEK, Integer.parseInt(String.valueOf(byDay.charAt(byDayIdx+1))) );
          ++byDayIdx;
        } else {
          next.add(Calendar.WEEK_OF_MONTH, +1*interval);
          next.set(Calendar.DAY_OF_WEEK, +Calendar.SUNDAY);
          for (int q = 0; q < byDay.length(); q++) {
            if (next.get(Calendar.DAY_OF_WEEK) <= Integer.parseInt(String.valueOf(byDay.charAt(q)))) {
              next.set(Calendar.DAY_OF_WEEK, Integer.parseInt(String.valueOf(byDay.charAt(q))) );
              byDayIdx = q;
              break;
            }
          }
        }
      }
    }

    //log
    trace("next.DAY_OF_WEEK 4 " + next.get(Calendar.DAY_OF_WEEK));
  }

  void generateEntUntilMonthly(Vector entv, EntryData ent, int i)
            throws Exception {
        Date start = ent.getDuration() != null ? ent.getDuration().getStart()
                : ent.getTodo() != null ? ent.getTodo().getDue() : null;
        int interval = ent.getRecurrence()[i].getInterval().intValue();
        Calendar next = Calendar.getInstance();
        next.setTime(start);
        next.add(next.MONTH, +interval);
        Date until = ent.getRecurrence()[i].getUntil();
        
        // Starts at 2 to avoid duplicating the first recurring month
        int multiplier = 2; 
        if (MAXTIMES >= 1) {
            int n = 1;
            while ((next.getTime().before(until) || next.getTime()
                    .equals(until))
                    && n < MAXTIMES) {
                entv.addElement(generateEnt(ent, next.getTime()));

                // TT 04724 - BSS -->
                // Reset so event will be set based on the start
                // day, not the day of the current iteration month.
                next.setTime(start);
                next.add(Calendar.MONTH, (interval * multiplier));

                multiplier++;
                n++;
            }
        } else {
            while (next.getTime().before(until) || next.getTime().equals(until)) {
                entv.addElement(generateEnt(ent, next.getTime()));

                next.setTime(start); // TT 04724 - BSS - See above
                next.add(Calendar.MONTH, (interval * multiplier));

                multiplier++;
            }
        }
    }

  void generateEntUntilYearly(Vector entv, EntryData ent, int i) throws Exception {
    Date start = ent.getDuration() != null ? ent.getDuration().getStart() : ent.getTodo() != null ? ent.getTodo().getDue() : null;
    int interval = ent.getRecurrence()[i].getInterval().intValue();
    Calendar next = Calendar.getInstance();
    next.setTime(start);
    next.add(next.YEAR, +interval);
    Date until = ent.getRecurrence()[i].getUntil();
    if (MAXTIMES >= 1) {
      int n = 1;
      while ((next.getTime().before(until) || next.getTime().equals(until)) && n < MAXTIMES) {
        entv.addElement(generateEnt(ent, next.getTime()));
        next.add(next.YEAR, +interval);
        n++;
      }
    } else {
      while (next.getTime().before(until) || next.getTime().equals(until)) {
        entv.addElement(generateEnt(ent, next.getTime()));
        next.add(next.YEAR, +interval);
      }
    }
  }

  void generateEntCountDaily(Vector entv, EntryData ent, int i) throws Exception {
    Date start = ent.getDuration() != null ? ent.getDuration().getStart() : ent.getTodo() != null ? ent.getTodo().getDue() : null;
    int interval = ent.getRecurrence()[i].getInterval().intValue();
    long next =  start.getTime() + 24*3600000*interval;
    int count = 1;
    int maxCount = ent.getRecurrence()[i].getCount().intValue();
    if (MAXTIMES >= 1 && maxCount > MAXTIMES)
      maxCount = MAXTIMES;
    while (count < maxCount) {
      entv.addElement(generateEnt(ent, new Date(next)));
      next += 24*3600000*interval;
      ++count;
    }
  }

  void generateEntCountWeekly(Vector entv, EntryData ent, int i) throws Exception {
    Date start = ent.getDuration() != null ? ent.getDuration().getStart() : ent.getTodo() != null ? ent.getTodo().getDue() : null;
    int interval = ent.getRecurrence()[i].getInterval().intValue();
    Calendar next = Calendar.getInstance();
    next.setTime(start);
    next.add(next.WEEK_OF_MONTH, +interval);
    int count = 1;
    int maxCount = ent.getRecurrence()[i].getCount().intValue();
    if (MAXTIMES >= 1 && maxCount > MAXTIMES)
      maxCount = MAXTIMES;
    while (count < maxCount) {
      entv.addElement(generateEnt(ent, next.getTime()));
      next.add(next.WEEK_OF_MONTH, +interval);
      ++count;
    }
  }

  void generateEntCountWeeklyByDay(Vector entv, EntryData ent, int i) throws Exception {
    Date start = ent.getDuration() != null ? ent.getDuration().getStart() : ent.getTodo() != null ? ent.getTodo().getDue() : null;
    Calendar next = Calendar.getInstance();
    next.setTime(start);
    int interval = ent.getRecurrence()[i].getInterval().intValue();
    //log
    trace("next.DAY_OF_WEEK step 0" + next.get(Calendar.DAY_OF_WEEK));
    boolean found = false;
    int byDayIdx = 0;
    String byDay = ent.getRecurrence()[i].getByDay();
    for (int q = 0; q < byDay.length(); q++) {
      if (next.get(Calendar.DAY_OF_WEEK) < Integer.parseInt( String.valueOf(byDay.charAt(q))) ) {
        //log
        trace("Integer.parseInt( String.valueOf(byDay.charAt(q))) " + Integer.parseInt( String.valueOf(byDay.charAt(q))));

        next.set(Calendar.DAY_OF_WEEK, Integer.parseInt( String.valueOf(byDay.charAt(q))) );
        byDayIdx = q;
        found = true;
        break;
      }
    }

    //log
    trace("next.DAY_OF_WEEK 2 " + next.get(Calendar.DAY_OF_WEEK));

    if (!found) {
      next.add(Calendar.WEEK_OF_MONTH, +1*interval);
      next.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
      for (int q = 0; q < byDay.length(); q++) {
        if (next.get(Calendar.DAY_OF_WEEK) <= Integer.parseInt(String.valueOf(byDay.charAt(q))) ) {
          next.set(Calendar.DAY_OF_WEEK, Integer.parseInt(String.valueOf(byDay.charAt(q))) );
          byDayIdx = q;
          break;
        }
      }
    }

    //log
    trace("next.DAY_OF_WEEK 3 " + next.get(Calendar.DAY_OF_WEEK));

    int count = 1;
    int maxCount = ent.getRecurrence()[i].getCount().intValue();
    if (MAXTIMES >= 1 && maxCount > MAXTIMES)
      maxCount = MAXTIMES;
    while (count < maxCount) {
      entv.addElement(generateEnt(ent, next.getTime()));
      ++count;
      if (byDayIdx+1 < byDay.length()) {
        next.set(Calendar.DAY_OF_WEEK, Integer.parseInt(String.valueOf(byDay.charAt(byDayIdx+1))) );
        ++byDayIdx;
      } else {
        next.add(Calendar.WEEK_OF_MONTH, +1*interval);
        next.set(Calendar.DAY_OF_WEEK, +Calendar.SUNDAY);
        for (int q = 0; q < byDay.length(); q++) {
          if (next.get(Calendar.DAY_OF_WEEK) <= Integer.parseInt(String.valueOf(byDay.charAt(q)))) {
            next.set(Calendar.DAY_OF_WEEK, Integer.parseInt(String.valueOf(byDay.charAt(q))) );
            byDayIdx = q;
            break;
          }
        }
      }

    }

    //log
    trace("next.DAY_OF_WEEK 4 " + next.get(Calendar.DAY_OF_WEEK));

  }

  void generateEntCountMonthly(Vector entv, EntryData ent, int i) throws Exception {
    Date start = ent.getDuration() != null ? ent.getDuration().getStart() : ent.getTodo() != null ? ent.getTodo().getDue() : null;
    int interval = ent.getRecurrence()[i].getInterval().intValue();
    Calendar next = Calendar.getInstance();
    next.setTime(start);
    next.add(next.MONTH, +interval);
    int count = 1;
    int maxCount = ent.getRecurrence()[i].getCount().intValue();
    if (MAXTIMES >= 1 && maxCount > MAXTIMES)
      maxCount = MAXTIMES;
    while (count < maxCount) {
      entv.addElement(generateEnt(ent, next.getTime()));
      next.add(next.MONTH, +interval);
      ++count;
    }
  }

  void generateEntCountYearly(Vector entv, EntryData ent, int i) throws Exception {
    Date start = ent.getDuration() != null ? ent.getDuration().getStart() : ent.getTodo() != null ? ent.getTodo().getDue() : null;
    int interval = ent.getRecurrence()[i].getInterval().intValue();
    Calendar next = Calendar.getInstance();
    next.setTime(start);
    next.add(next.YEAR, +interval);
    int count = 1;
    int maxCount = ent.getRecurrence()[i].getCount().intValue();
    if (MAXTIMES >= 1 && maxCount > MAXTIMES)
      maxCount = MAXTIMES;
    while (count < maxCount) {
      entv.addElement(generateEnt(ent, next.getTime()));
      next.add(next.YEAR, +interval);
      ++count;
    }
  }

  void generateEntByDay(Vector entv, EntryData ent, int i) throws Exception {
    //entv.addElement(ent);
    trace(" generateEntByDay ");
  }

  void exclude (Vector entv, EntryData ent, int i) throws Exception {
    Date testCond = ent.getRecurrence()[i].getDate();
    for (int j = 0; j < entv.size(); j++) {
      if (((EntryData)entv.elementAt(j)).getDuration().getStart().equals(testCond)) {
        entv.removeElementAt(j);
      }
    }
  }

  //--------------------------------------------------------------------------//

  EntryData generateEnt(EntryData ent, Date newStart) throws Exception {
    EntryData ent2 = new EntryData();
    ent2.putA("CALID", ent.getA("CALID"));
    //ent2.putA("CESEQ", ent.getA("CESEQ"));
    ent2.putA("created", ent.getCreated());
    ent2.putA("last-modified", ent.getLastModified());
    if (ent.isInvitation())
      ent2.setIsInvitation(true);
    if (ent.getAlarm() != null)
      ent2.putAlarm(ent.getAlarm());
    if (ent.getAttendee() != null)
      ent2.putAttendee(ent.getAttendee());
    if (ent.getEvent() != null)
      ent2.putEvent(ent.getEvent());
    else {
      TodoData todo = new TodoData();
      todo.put(ent.getTodo().get());
      todo.putDue(newStart);
      if (ent.getTodo().getCategory() != null)
        todo.putCategory(ent.getTodo().getCategory());
      if (ent.getTodo().getCompletion() != null)
        todo.putCompletion(ent.getTodo().getCompletion());
      if (ent.getTodo().getDescription() != null)
        todo.putDescription(ent.getTodo().getDescription());
      if (ent.getTodo().getPriority() != null)
        todo.putPriority(ent.getTodo().getPriority());
      ent2.putTodo(todo);
    }
    // new start date / due date
    if (ent.getDuration() != null) {
      DurationData dur = new DurationData();
      dur.putStart(newStart);
      dur.putDOW(net.unicon.portal.channels.calendar.CalendarView.dayOfWeek(dur.getStart()));
      if (ent.getDuration().getBlocked() != null)
        dur.putBlocked(ent.getDuration().getBlocked());
      if (ent.getDuration().getLength() != null)
        dur.putLength(ent.getDuration().getLength());
      ent2.putDuration(dur);
    }

    if (ent.getCeid() != null)
      ent2.putCeid(ent.getCeid().substring(0, ent.getCeid().indexOf(".")) + "." + newStart.getTime());
    //

    if (ent.getLocation() != null)
      ent2.putLocation(ent.getLocation());
    if (ent.getOrganizer() != null)
      ent2.putOrganizer(ent.getOrganizer());
    if (ent.getRecurrence() != null)
      ent2.putRecurrence(ent.getRecurrence());
    if (ent.getShare() != null)
      ent2.putShare(ent.getShare());

    return ent2;
  }

  Date generateEOR(EntryData ent) throws Exception {
    trace("method generateEOR start");
    Date eor = getCurrentDate();

    if (ent.getDuration() != null && ent.getDuration().getStart() != null)
      eor = new Date (ent.getDuration().getStart().getTime() + ( ent.getDuration() != null && ent.getDuration().getLength() != null ? ent.getDuration().getLength().getLength() : 0 ) );
    if (ent.getTodo() != null && ent.getTodo().getDue() != null)
      eor = ent.getTodo().getDue();

    // Calculate EOR
    if (ent.getRecurrence() != null) {
      Vector entv = new Vector();
      generateEntOccurences(entv, ent);
      if (entv.size() > 0) {
        EntryData lastEnt = (EntryData)entv.lastElement();
        Date start = lastEnt.getDuration() != null ? lastEnt.getDuration().getStart() : lastEnt.getTodo() != null ? lastEnt.getTodo().getDue() : null;
        long length = lastEnt.getDuration() != null ? lastEnt.getDuration().getLength().getLength() : 0;
        eor = new Date(start != null ? start.getTime() + length : 0);

        if (ent.getDuration()!= null && ent.getDuration().getLength()!= null && ent.getDuration().getLength().getAllDay()) {
          Calendar eorCal = Calendar.getInstance();
          eorCal.setTime(eor);
          eorCal.setTimeZone(TimeZone.getTimeZone("GMT"));
          //eorCal.add(Calendar.HOUR, +12);
          eorCal.set(Calendar.HOUR, +12);
          eor = eorCal.getTime();
        }

        return eor;
      }
    }
    trace("method generateEOR end");

    if (ent.getDuration()!= null && ent.getDuration().getLength()!= null && ent.getDuration().getLength().getAllDay()) {
      Calendar eorCal = Calendar.getInstance();
      eorCal.setTime(eor);
      eorCal.setTimeZone(TimeZone.getTimeZone("GMT"));
      //eorCal.add(Calendar.HOUR, +12);
      eorCal.set(Calendar.HOUR, +12);
      eor = eorCal.getTime();
    }

    return eor;
  }

  CalendarData[] buildCalr(CalendarData[] calr, String sql, Date from, Date to) throws Exception {
    if( calr == null)
      return null;

    // remove old entries
    for (int i = 0; i < calr.length; i++)
      if (calr[i] != null)
        calr[i].removeE("entry");

    EntryData[] entr = buildEntr(sql, false, from, to, true);
    trace("method: buildCalr step 1");
    if (entr == null)
      return calr;
    trace("method: buildCalr, entr.length: " + entr.length);

    // insert EntryData[] to CalendarData[]
    HashMap mapEnt = new HashMap();
    for (int i = 0; i < entr.length; i++) {
      String calid = (String)entr[i].getA("CALID");
      if (!mapEnt.containsKey(calid)) {
        Vector temp = new Vector();
        temp.addElement(entr[i]);
        mapEnt.put(calid, temp);
      } else {
        Vector temp = (Vector)mapEnt.get(calid);
        temp.addElement(entr[i]);
        mapEnt.put(calid, temp);
      }
    }
    for (int i = 0; i < calr.length; i++) {
      String calid = calr[i].getCalid();
      trace("method buildCalr calid " + calid);
      Vector tempv = (Vector)mapEnt.get(calid);
      if (tempv != null) {
        EntryData[] temp = (EntryData[])tempv.toArray(new EntryData[]{});
        calr[i].putEntry(temp);
      }
    }

    trace("method: buildCalr end build cal ");

    return calr;
  }

  EntryData[] buildEntr(String sql, boolean invitation, Date from, Date to, boolean sortUp) throws Exception {
    DBService db = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    Vector entv = new Vector();

    // AttendeeData
    HashMap mapAtt = new HashMap();

    // RecurrenceData
    HashMap mapRec = new HashMap();

    try {
    db = getDBService(DS);

    try {
    ps = db.prepareStatement(sql);
    rs = ps.executeQuery();
    //ps.close();
    while (rs.next()) {
      EntryData ent = new EntryData();
      if (invitation)
        ent.setIsInvitation(true);

      // TodoData
      if (rs.getLong("LENGTH") == 0 && rs.getTimestamp("DUE") != null) {
        TodoData todo = new TodoData();
        // completion data
        if (rs.getTimestamp("COMPLETED") != null && rs.getInt("PERCENTAGE") > 0) {
          CompletionData comp = new CompletionData(rs.getTimestamp("COMPLETED"), new Integer(rs.getInt("PERCENTAGE")));
          todo.putCompletion(comp);
        }
        Timestamp due = rs.getTimestamp("DUE");
        due.setNanos(0);
        todo.putDue(due);
        if (rs.getString("CATEGORIES") != null)
          todo.putCategory(new String[]{rs.getString("CATEGORIES")});
        if (rs.getString("DESCRIPTION") != null)
          todo.putDescription(rs.getString("DESCRIPTION"));
        if (rs.getInt("PRIORITY") > -1)
          todo.putPriority(new Integer(rs.getInt("PRIORITY")));
        if (rs.getString("STATUS") != null)
          todo.putStatus(rs.getString("STATUS"));
        if (rs.getString("TITLE") != null)
          todo.put(rs.getString("TITLE"));

        ent.putTodo(todo);
      } else {
        // EventData
        EventData ev = new EventData();
        if (rs.getString("CATEGORIES") != null)
          ev.putCategory(new String[]{rs.getString("CATEGORIES")});
        if (rs.getString("DESCRIPTION") != null)
          ev.putDescription(rs.getString("DESCRIPTION"));
        if (rs.getInt("PRIORITY") > -1)
          ev.putPriority(new Integer(rs.getInt("PRIORITY")));
        if (rs.getString("STATUS") != null)
          ev.putStatus(rs.getString("STATUS"));
        if (rs.getString("TITLE") != null)
          ev.put(rs.getString("TITLE"));

        ent.putEvent(ev);
      }

      // DurationData
      DurationData dur = new DurationData();
      Timestamp day =  rs.getTimestamp("ENTRY_DAY");
      day.setNanos(0);
      dur.putStart(day);
      dur.putDOW(dayOfWeek(dur.getStart()));
      if (rs.getLong("LENGTH") == -1)
        dur.putAllDay();
      else {
        dur.putLength(new TimeLength(rs.getLong("LENGTH")));
      }
      ent.putDuration(dur);

      // OrganizerData
      OrganizerData organizer = new OrganizerData();
      organizer.putCuid(rs.getString("CUID"));
      organizer.put(rs.getString("CUID"));
      ent.putOrganizer(organizer);

      ent.putCeid(rs.getString("CEID")+"_"+rs.getInt("CESEQ"));
      // temporary put in entrydata CESEQ & CALID
      //ent.putA("CESEQ", new Integer(rs.getInt("CESEQ")));
      try{
        ent.putA("CALID", rs.getString("CALID"));
      } catch(Exception e){}


      if (rs.getString("LOCATION") != null)
        ent.putLocation(rs.getString("LOCATION"));
      if (rs.getString("ENTRY_SCOPE") != null)
        ent.putShare(rs.getString("ENTRY_SCOPE"));
      ent.putA("created", rs.getTimestamp("CREATED"));
      ent.putA("last-modified",rs.getTimestamp("LAST_MODIFIED"));

      entv.addElement(ent);

    }
      
    } finally {
      try {
        if (rs != null) rs.close();
        rs = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (entv.size() == 0)
      return null;

    String ceids = atostring(entv); // ceids = "(1,2,3,4,5)"
    sql = "SELECT ceid, ceseq, exclude, recur_day, frequency, byday, intrvl, repetition, until FROM dpcs_recurrence WHERE ceid IN " + ceids;
    try {
    ps = db.prepareStatement(sql);
    rs = ps.executeQuery();
    while (rs.next()) {
      RecurrenceData rec = new RecurrenceData();
      if (rs.getTimestamp("RECUR_DAY") != null) {
        Timestamp day =  rs.getTimestamp("RECUR_DAY");
        day.setNanos(0);
        rec.putDate(day);
      }
      if (rs.getString("BYDAY") != null)
        rec.putByDay(rs.getString("BYDAY"));
      //if (rs.getBoolean("EXCLUDE") == true)
      //  rec.putExclude(new Boolean(rs.getBoolean("EXCLUDE")));
      if (rs.getInt("EXCLUDE") == 1)
        rec.putExclude(TRUE);
      if (rs.getInt("REPETITION") > 0)
        rec.putRule(rs.getString("FREQUENCY"), new Integer(rs.getInt("INTRVL")), new Integer(rs.getInt("REPETITION")));
      else if (rs.getTimestamp("UNTIL") != null) {
        rec.putRule(rs.getString("FREQUENCY"), new Integer(rs.getInt("INTRVL")), rs.getTimestamp("UNTIL"));
      }

      String ceid_ceseq = rs.getString("CEID")+"_"+rs.getInt("CESEQ");
      if (!mapRec.containsKey(ceid_ceseq)) {
        Vector temp = new Vector();
        temp.addElement(rec);
        mapRec.put(ceid_ceseq, temp);
      } else {
        Vector temp = (Vector)mapRec.get(ceid_ceseq);
        temp.addElement(rec);
        mapRec.put(ceid_ceseq, temp);
      }
    }
    } finally {
      try {
        if (rs != null) rs.close();
        rs = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    sql = "SELECT ceid, ceseq, cuid, role, status, rsvp, comments FROM dpcs_attendee WHERE ceid IN " + ceids;

    try {
    ps = db.prepareStatement(sql);
    rs = ps.executeQuery();
    while (rs.next()) {
      AttendeeData att = new AttendeeData();
      initAttendeeData( att, rs.getString("CUID"));

      att.putRole(rs.getString("ROLE"));
      att.putStatus(rs.getString("STATUS"));
      att.putRSVP(new Boolean(rs.getInt("RSVP")==1? true:false));

      String ceid_ceseq = rs.getString("CEID")+"_"+rs.getInt("CESEQ");
      if (!mapAtt.containsKey(ceid_ceseq)) {
        Vector temp = new Vector();
        temp.addElement(att);
        mapAtt.put(ceid_ceseq, temp);
      } else {
        Vector temp = (Vector)mapAtt.get(ceid_ceseq);
        temp.addElement(att);
        mapAtt.put(ceid_ceseq, temp);
      }
    }
    } finally {
      try {
        if (rs != null) rs.close();
        rs = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

 
    } finally {
      if (db != null) db.release();
    }

    // Build entries data with detail (alarmdata, recurrencedata, attendeedata)
    int size = entv.size();
    for (int i = 0; i < size; i++) {
      EntryData ent = (EntryData)entv.elementAt(i);
      String ceid_ceseq = ent.getCeid();
      Vector attv = (Vector)mapAtt.get(ceid_ceseq);
      Vector recv = (Vector)mapRec.get(ceid_ceseq);
      if (attv != null)
        ent.putAttendee((AttendeeData[])attv.toArray(new AttendeeData[]{}));
      if (recv != null) {
        ent.putRecurrence((RecurrenceData[])recv.toArray(new RecurrenceData[]{}));
        ent.putCeid(ent.getCeid()+"."+ent.getDuration().getStart().getTime());
        entv.removeElementAt(i);
        i--;
        size--;
        Vector entv1 = new Vector();
        entv1.addElement(ent);
        generateEntOccurences(entv1, ent);
        entv.addAll(entv1);
      }
    }

    Comparator comp = null;
    if (sortUp)
        comp = new EntryAscComparator();
    else
        comp = new EntryDescComparator();
    Collections.sort(entv, comp);

    return vtoEntr(entv, from, to);
  }

  EntryData[] filterInviEnt(EntryData[] entr) {
    if (entr == null )
      return null;
    else {
      Vector v = new Vector();
      for (int i = 0; i < entr.length; i++ ) {
        AttendeeData[] attr = entr[i].getAttendee();
        for (int j = 0; j < attr.length; j++) {
          if (attr[j].getCuid().equals(m_user) && !attr[j].getStatus().equals("NEEDS-ACTION")) {
            entr[i] = null;
          }
        }
        if (entr[i] != null)
          v.addElement(entr[i]);
      }
      if (v.size() != 0)
        return (EntryData[])v.toArray(new EntryData[]{new EntryData()});
      else
        return null;
    }
  }

  //--------------------------------------------------------------------------//

  /**
   * Use for special situation only. Update cache - array of CalendarData
   * @param calr Array of <code>CaledarData</code>
   * @throws Exception
   */

  public void setCalr(CalendarData[] calr) throws Exception {
    // update cache
    if (m_calr != null && calr != null) {
      for (int i = 0; i < calr.length; i++) {
        CalendarData.replaceCal(m_calr, calr[i]);
        //CalendarData old = CalendarData.findCalendar(m_calr, calr[i].getCalid());
        //if (old != null) old.updateProps(calr[i], true);
      }
    } else {
      m_calr = calr;
    }
    //
  }

  //- Utils ------------------------------------------------------------------//

  static public void trace(String trace) {
    if(m_debug != 0)
      System.out.println(trace);
  }

  int getMaxInt(DBService db, String table, String field, String where) throws Exception {
    String sql = "SELECT MAX(" + field + ") FROM " + table;
    if (where != null)
      sql += " WHERE " + where;
    ResultSet rs = null;
    int ret = 0;

    try {
      rs = db.select(sql);
      if (rs.next())
        ret = rs.getInt(1);
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      db.closeStatement();
    }

    return ret;
  }

  int count(DBService db, String sql) throws Exception {
    ResultSet rs = null;
    int ret = 0;

    try {
      rs = db.select(sql);
      if (rs.next())
        ret = rs.getInt(1);
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      db.closeStatement();
    }

    return ret;
  }

  String quot(String s) {
    // Double single quot character
    if (s == null)
      return s;
    int k = -1;
    int beg = -1;
    while ((k = s.indexOf("'", ++beg)) > -1) {
      s = s.substring(0, k) + "'" + s.substring(k);
      beg = ++k;
    }
    return s;
  }

  Vector atov(Object[] ar) {
    Vector v = new Vector();
    if (ar == null)
      return v;
    for (int i=0; i < ar.length; i++)
      v.addElement(ar[i]);
    return v;
  }

  EntryData[] vtoEntr(Vector entv, Date from, Date to) {
    if (entv == null || entv.size( )== 0 )
      return null;
    trace("entv.size() "+ entv.size() +"**" +from +"**"+ to + "**" );
    Vector v = new Vector();
    for (int i = 0; i < entv.size(); i++) {
        EntryData ent = (EntryData)entv.elementAt(i);

        if (from != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(from);
            cal.add(Calendar.SECOND, -1);
            from = cal.getTime();
        }

        if (from != null && to != null) {
            // NOTE: You *cannot* use getStart()'s result directly, as it
            // cannot be guaranteed to be a Date (it might be a Timestamp).
            Date start = new Date(ent.getDuration().getStart().getTime());
            if ((start.compareTo(from) >= 0) && (start.before(to) ||
                        start.equals(to))) {
                v.addElement(ent);
                        }
        } else {
            v.addElement(ent);
        }
    }
    EntryData[] entr = (EntryData[])v.toArray(new EntryData[]{});

    return entr;
  }

  String atostring(CalendarData[] calr) throws Exception {
    if (calr == null || calr.length == 0)
      return "";
    String s = "(";
    for (int i = 0; i < calr.length; i++) {
      if (calr[i] != null) {
        trace("calr[i].getCalid().lenght " + calr[i].getCalid().length());
        if (i < calr.length-1)
          s += "'" + quot(calr[i].getCalid()) + "',";
        else
          s+= "'" + quot(calr[i].getCalid()) + "')";
      }
    }

    return s;
  }

  String atostring(Vector entv) {
    if (entv == null || entv.size() == 0)
      return "";
    String s = "(";
    for (int i = 0; i < entv.size(); i++) {
      String ceid = ((EntryData)entv.elementAt(i)).getCeid();
      if (i < entv.size()-1)
        s += "'" + ceid.substring(0, ceid.indexOf("_")) + "',";
      else
        s+= "'" + ceid.substring(0, ceid.indexOf("_")) + "')";
    }

    return s;
  }

  String atostring(String[] ar) {
    if (ar == null || ar.length == 0)
      return "";
    String s = "(";
    for (int i = 0; i < ar.length; i++) {
      if (i < ar.length-1)
        s += "'" + quot(ar[i]) + "',";
      else
        s+= "'" + quot(ar[i]) + "')";
    }

    return s;
  }

  boolean isRecurrence(String ceid) {
    if (ceid.indexOf(".") != -1)
      return true;

    return false;
  }

  String dayOfWeek(Date date) throws Exception {
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

  //--------------------------------------------------------------------------//

  CalendarData[] cloneCalrProps(CalendarData[] calr) throws Exception {
    if( calr == null)
      return null;
    CalendarData[] ret = new CalendarData[calr.length];
    for (int i = 0; i < calr.length; i++) {
      ret[i] = new CalendarData();
      ret[i].updateProps(calr[i],true);
    }
    return ret;
  }


  // Tien 1126: beg
  static void initAttendeeData( AttendeeData att, String cuidDesc) {
    StringTokenizer token = new StringTokenizer(cuidDesc, IdentityData.SEPARATOR);
    if (token.hasMoreTokens())
      att.putType(token.nextToken());
    if (token.hasMoreTokens())
      att.putEntityType(token.nextToken());
    if (token.hasMoreTokens())
      att.putOID(token.nextToken());
    if (token.hasMoreTokens()) {
      String cuid = token.nextToken();
      att.putCuid(cuid);
      att.put(cuid);
    }
    if (token.hasMoreTokens())
      att.put(token.nextToken());
  }
  // Tien 1126: end
}
