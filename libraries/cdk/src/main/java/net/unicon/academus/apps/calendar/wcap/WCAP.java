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

package net.unicon.academus.apps.calendar.wcap;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

import org.w3c.dom.*;
import org.apache.xerces.parsers.*;
import org.xml.sax.*;
import org.jasig.portal.security.IPerson;

import net.unicon.academus.apps.calendar.*;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XMLData;

/**
 * This class implements a calendar server to communicate with iPlanet Calendar
 * Server (iCS). It uses the Web Calendar Access Protocol which is a high leve command-based
 * protocol.
 * @version 1.1
 * @date 3/26/2003
 */
public class WCAP extends net.unicon.academus.apps.calendar.CalendarServer {
  String m_host = null;
  int m_port = 0;
  boolean m_p2 = true;
  String m_version = null;

  /**
   * Default format of output is xml.
   */
  public String m_fmt_out = "text/xml";

  String m_error = null;
  // only have calendardata without entry
  CalendarData[] m_cals = null;

  static final String ERR_UNKNOWN = "ERR_UNKNOWN";
  static final String ERR_UNKNOWN_HOST = "ERR_UNKNOWN_HOST";
  static final String ERR_CONNECT_FAILED = "ERR_CONNECT_FAILED";
  static final String ERR_LOGIN_FAILED = "ERR_LOGIN_FAILED";

  static public final SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

  // Filter for m_cals
  Date m_from = null;
  Date m_to = null;

  /**
   * Default constructor. The WCAP uses GMT time zone
   */
  public WCAP() {
    m_sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  /**
   * Initalize the properties of iPlanet Calendar Server
   * @param props
   */
  public void init(Properties props) {
    m_host = (String)props.get("calendar.host");
    m_port = Integer.parseInt((String)props.get("calendar.port"));
    m_version = (String)props.get("calendar.server.version");
    if( m_version == null)
      m_version = "v5.0p2";
    //m_p2 = (m_version.indexOf("p2") != -1 || m_version.indexOf("v5.1.1") != -1);
    m_p2 = (m_version.indexOf("p2") != -1);
  }

  //--------------------------------------------------------------------------//

  public String m_session = "NULL";
  public String m_user = null;
  public String m_password = null;

  /**
   * Get user
   * @return
   */
  public String getUser() {
    return m_user;
  }

  XMLData m_logon = new XMLData();

  /**
   * Get information of logon user.
   * @return
   */
  public XMLData getLogonData() {
    XMLData logonData = new XMLData();
    logonData.putE("logon", m_logon);
    return logonData;
  }

  /**
   * Get calendar user identifer.
   * @param cuid
   * @return
   */
  public int getCuidCode(String cuid) {
    return getCuidType(cuid);
  }

  static int getCuidType(String cuid) {
    if (cuid == null)
      return 0;

    if (cuid.equals("@"))
      return CUID_ALL;
    else if (cuid.equals("@@p"))
      return CUID_OWNER;
    else if (cuid.equals("@@o"))
      return CUID_SHARE;
    else if (cuid.equals("@@n"))
      return CUID_NOSHARE;
    else
      return 0;
  }

  /**
   * Login to iCS. This starts a session to working with the iCS
   * @param person
   * @param password
   * @return
   * @throws Exception
   */
  public CalendarData[] login(IdentityData person, String password) throws Exception {
    //m_user = user;
    m_user = (String)person.getA("calendar-logon");
    m_password = password;
    //System.out.println("****** muser, mpass " + m_user + "  " + m_password);
    m_logon.putA("user", m_user);

    // Read all calendars (including shared)
    //String query = "search_calprops.wcap?fmt-out="+m_fmt_out;
    String query = "fetchcomponents_by_range.wcap?fmt-out="+m_fmt_out;
    if (m_p2)
      query += "&primaryOwner=1";

    // Try to connect to iPlanet Calendar server
    try {
      m_cals = exec(query, XMLOutput.OUT_LOGIN);
      //System.out.println("m_cals.length login "+ m_cals.length);
      //System.out.println("m_cals[0].getCalid() login " + m_cals[0].getCalid());


      /*   truyen 6/6/02
            return CalendarData.calids(m_cals);*/
      return m_cals;
    } catch (java.net.UnknownHostException uhe) {
      m_error = "-1";
      throw uhe;
    } catch (java.net.ConnectException ce) {
      m_error = "-2";
      throw ce;
    } catch( Exception e) {
      m_error = e.getMessage();
      throw e;
    }
  }

  void login() throws Exception {
    String query = "login.wcap?fmt-out="+m_fmt_out+"&user="+m_user+"&password="+m_password;
    //System.out.println("query " + query);
    read(call(query), XMLOutput.OUT_AUTHENTICATE);
  }

  /**
   * Logout from the iCS. It ends the session with iCS
   * @throws Exception
   */
  public void logout() throws Exception {
    String query = "logout.wcap?fmt-out="+m_fmt_out;
    call(query+"&id="+m_session);
    m_session = "NULL";
  }

  /**
   * Change password of user. This version does nothing.
   * @param user
   * @param oldPassword
   * @param newPassword
   * @throws Exception
   */
  public void changePassword(String user, String oldPassword, String newPassword) throws Exception {
    // change_password
  }
  //truong 22/5
  /**
   * Authenticate to iCS
   * @param person
   * @param password
   * @return
   * @throws Exception
   */
  public boolean authenticate(IdentityData person, String password) throws Exception {
    m_user = (String)person.getA("calendar-logon");
    m_password = password;

    try {
      login();
      return (m_session != null && !m_session.equals("NULL"));
    } catch (java.net.UnknownHostException uhe) {
      m_error = "-1";
      throw uhe;
    } catch (java.net.ConnectException ce) {
      m_error = "-2";
      throw ce;
    } catch( Exception e) {
      m_error = e.getMessage();
      throw e;
    }
  }
  //--------------------------------------------------------------------------//

  /**
   * Update properties of calendar
   * @param cal
   * @return
   * @throws Exception
   */
  public CalendarData updateCalendar(CalendarData cal) throws Exception {
    // set_calprops
    String query = "set_calprops.wcap?fmt-out=" + m_fmt_out;
    query += "&calid=" + cal.getCalid() + "&name=" + XMLData.replace(cal.getCalname()," ","%20");
    query += "&acl=" + ACE.acl(cal.getACE());

    // Execute and update m_cals
    CalendarData[] cals = exec(query, XMLOutput.OUT_CALENDAR_UPDATE);

    if (cals != null && cals.length > 0) {
      CalendarData old = CalendarData.findCalendar(m_cals, cals[0].getCalid());
      if (old != null)
        old.updateProps(cals[0], true);
    }

    return cals[0];
  }

  /**
   * Delete a calendar by id
   * @param calid
   * @throws Exception
   */
  public void deleteCalendar(String calid) throws Exception {
    String query = "deletecalendar.wcap?fmt-out="+m_fmt_out+"&calid="+calid;
    exec(query, XMLOutput.OUT_CALENDAR_DELETE);

    // Remove from m_cals
    m_cals = CalendarData.removeCalendar(m_cals, calid);
  }

  /**
   * Create a new calendar
   * @param cal contains information of new calendar
   * @return a calendar has just been created.
   * @throws Exception
   */
  public CalendarData createCalendar(CalendarData cal) throws Exception {
    // createcalendar
    String calid = getGUID();
    String query = "createcalendar.wcap?fmt-out="+m_fmt_out;

    if (cal != null)
      query += "&calid="+calid+"&set_calprops=1&name="+XMLData.replace(cal.getCalname()," ","%20")+"&acl="+ACE.acl(cal.getACE());
    else
      query += "&calid="+calid;

    // execute
    CalendarData[] cals = exec(query, XMLOutput.OUT_CALENDAR_CREATE);
    if (cals != null && cals.length > 0) {
      m_cals = CalendarData.addCalendar(m_cals, cals[0]);
      return cals[0];
    } else
      return null;
  }

  CalendarData getCalprops(String calid) throws Exception {
    String query = "get_calprops.wcap?fmt-out=" + m_fmt_out + "&calid=" + calid;
    return CalendarData.findCalendar(exec(query, XMLOutput.OUT_CALENDAR_GETPROPS), calid);
  }

  /**
   * Get a list of calendar with given identifers
   * @param calidr array of calendar id.
   * @return Array of calendars
   */
  public CalendarData[] getCalendars(String[] calidr) {
    try {

      String[] idr = null;
      if (calidr == null) {
        //System.out.println("m_cals.length truoc clone"+ m_cals.length);
        //System.out.println("m_cals[0].getCalid() truoc clone" + m_cals[0].getCalid());
        return cloneCalrProps(m_cals);
      } else {
        CalendarData[] ret = new CalendarData[calidr.length];
        for (int i=0; i < calidr.length; i++) {
          CalendarData cal = CalendarData.findCalendar(m_cals, calidr[i]);
          ret[i] = new CalendarData();
          if (cal != null)
            ret[i].updateProps(cal, true);
        }
        return ret;
      }

    } catch(Exception e) {
      return null;
    }
  }

  //--------------------------------------------------------------------------//

  public String importEntries(InputStream is, String format, String calid, Hashtable map) throws Exception {
    // not implement
    return null;
  }

  public InputStream exportEntries(String format, String[] calids, Date from, Date to) throws Exception {
    // not implment
    return null;
  }

  /**
   * Get an entry with given calid and entry id
   * @param calid
   * @param ceid
   * @return
   * @throws Exception
   */
  public EntryData getEntry(String calid, String ceid) throws Exception {
    CalendarData cal = CalendarData.findCalendar(m_cals,calid);

    if (cal != null) {
      EntryData ent = EntryData.findEntry(cal.getEntry(), ceid);
      if (ent != null)
        return ent;
    }

    //return fetchEventsByIds(calid,new EntryRange[]{new EntryRange(ceid)})[0]; // Thach-May21
    EntryData[] ents = fetchEventsByIds(calid,new EntryRange[]{new EntryRange(ceid)});
    return (ents != null && ents.length > 0?ents[0]:null);
  }

  /**
   * Get an entry with given id
   * @param ceid
   * @return
   */

  public EntryData getEntry(String ceid) {
    for (int i = 0; i < m_cals.length; i++) {
      EntryData data = EntryData.findEntry(m_cals[i].getEntry(), ceid);
      if (data != null)
        return data;
    }
    return null;
  }

  /**
   * Delete events
   * @param calid
   * @param ceidr
   * @throws Exception
   */
  public void deleteEvents(String calid, EntryRange[] ceidr) throws Exception {
    deleteEntries(calid,ceidr,"events");
  }

  /**
   * Delete todos
   * @param calid
   * @param ceidr
   * @throws Exception
   */
  public void deleteTodos(String calid, EntryRange[] ceidr) throws Exception {
    deleteEntries(calid,ceidr,"todos");
  }

  void deleteEntries(String calid, EntryRange[] ceidr, String type) throws Exception {
    String query = null;
    if (type.equals("events"))
      query = "deleteevents_by_id.wcap?fmt-out="+m_fmt_out;
    else if (type.equals("todos"))
      query = "deletetodos_by_id.wcap?fmt-out="+m_fmt_out;
    else
      return;

    query += "&calid="+calid;
    String uids = "";
    String rids = "";
    String mods = "";
    for (int i = 0; i < ceidr.length; i++) {
      if (i > 0) {
        uids += ";";
        rids += ";";
        mods += ";";
      }
      uids += uid(ceidr[i]);
      rids += rid(ceidr[i]);
      mods += mod(ceidr[i]);
    }
    query += "&uid="+uids;
    query += "&rid="+rids;
    query += "&mod="+mods;
    exec(query, type.equals("events")?XMLOutput.OUT_EVENT_DELETE:XMLOutput.OUT_TODO_DELETE);

    // Refresh data
    if (m_from != null && m_to != null)
      fetchEntries(new String[]{calid}, null, m_from, m_to);
  }

  /**
   * Create a new entry
   * @param calid
   * @param ent
   * @return
   * @throws Exception
   */
  public EntryData createEntry(String calid, EntryData ent) throws Exception {
    return updateEntry(calid, null, ent);
  }

  /**
   * Reply an invitation
   * @param ceid
   * @param status
   * @param comment
   * @throws Exception
   */
  public void replyInvitation( EntryRange ceid, String status, String comment) throws Exception {
    String calid = m_user; // Personal calendar
    String query = "storeevents.wcap";
    query += "?method=4&fetch=1&fmt-out=" + m_fmt_out + "&calid="+calid;

    if (ceid != null) {
      query += "&uid="+uid(ceid);
      query += "&rid="+rid(ceid);
      if (!rid(ceid).equals("0"))
        query += "&mod="+mod(ceid);
      else
        query += "&mod=1";
    }

    // Find attendees
    AttendeeData[] attr = null;
    EntryData[] inv = fetchInvitationByIds(new EntryRange[] {
                                             ceid
                                           }
                                          );
    if( inv != null)
      for( int i = 0; i < inv.length; i++) {
        if( inv[i].getCeid().equals(ceid.m_ceid)) {
          attr = inv[i].getAttendee();
          break;
        }
      }

    if (attr != null && attr.length > 0) {
      String atts = "";
      for (int i = 0; i < attr.length; i++) {
        String cuid = attr[i].getCuid();
        String sta = cuid.equals(m_user)? status:attr[i].getStatus();
        String rsvp = attr[i].getRSVP().toString();
        String role = attr[i].getRole();

        atts += (i>0?";":"") +(sta != null? "PARTSTAT=" + sta:"PARTSTAT=NEEDS-ACTION");
        atts += ( rsvp != null?"^RSVP=" + rsvp.toUpperCase():"");
        atts += ( role != null?"^ROLE=" + role:"");
        atts += "^" + attr[i].getCuid();
      }
      query += "&attendees="+atts;

      // Execute
      CalendarData[] cals = exec(query,XMLOutput.OUT_INVITATION_REPLY);
    }
  }

  /**
   * Update an entry
   * @param calid
   * @param ceid
   * @param ent
   * @return
   * @throws Exception
   */
  public EntryData updateEntry(String calid, EntryRange ceid, EntryData ent) throws Exception {
    CalendarData[] cals = null;
    if (ent.isEvent()) {
      String query = formEntryQuery(calid, ceid, ent);
      cals = exec(query,XMLOutput.OUT_EVENT_UPDATE);
    } else if (ent.isTodo()) {
      if (ceid != null && ceid.m_mod == EntryRange.FUTURE) {
        RecurrenceData[] recr = ent.getRecurrence();
        CompletionData comp = ent.getTodo().getCompletion();
        ent.getTodo().putCompletion(null);
        // Update all except completion
        String query = formEntryQuery(calid, ceid, ent);
        cals = exec(query,XMLOutput.OUT_TODO_UPDATE);

        // Update only complete
        if (comp != null) {
          EntryData ent1 = new EntryData();
          ent1.putCeid(ent.getCeid());
          ent1.putTodo(new TodoData());
          ent1.getTodo().putCompletion(comp);
          ent1.putRecurrence(ent.getRecurrence()); // Thach Sep 31
          query = formEntryQuery(calid, new EntryRange(cals[0].getEntry()[0].getCeid()), ent1); // Thach Sep 31
          cals = exec(query, XMLOutput.OUT_TODO_UPDATE);
        }
      } else // Create new Todo or update with not check "Apply for future date"
      {
        String query = formEntryQuery(calid, ceid, ent);
        cals = exec(query,XMLOutput.OUT_TODO_UPDATE);
      }
    } else if (ent.isInvitation()) {
      //String query = formEntryQuery(calid, new EntryRange(ceid.m_ceid), ent);
      String query = formEntryQuery(calid, ceid, ent);
      cals = exec(query,XMLOutput.OUT_INVITATION_UPDATE);
    }

    // updated entry
    if (cals != null && cals.length > 0 &&
        cals[0].getEntry() != null && cals[0].getEntry().length > 0) {
      /*
      boolean fetch = true;
      CalendarData cal = CalendarData.findCalendar(m_cals, calid);

      // re-fetch
      fetchEntries(new String[]{calid},null,m_from,m_to);
      */

      return cals[0].getEntry()[0];
    }

    return ent;
  }

  /**
   * Fetch events from iCS
   * @param calid
   * @param ceidr
   * @return
   * @throws Exception
   */
  public EntryData[] fetchEventsByIds(String calid, EntryRange[] ceidr) throws Exception {
    return fetchEntriesByIds(calid,ceidr,"event");
  }

  /**
   * Fetch todos from iCS
   * @param calid
   * @param ceidr
   * @return
   * @throws Exception
   */
  public EntryData[] fetchTodosByIds(String calid, EntryRange[] ceidr) throws Exception {
    return fetchEntriesByIds(calid,ceidr,"todo");
  }

  /**
   * Fetch invitation from iCS
   * @param ceidr
   * @return
   * @throws Exception
   */
  public EntryData[] fetchInvitationByIds( EntryRange[] ceidr) throws Exception {
    return fetchEntriesByIds(m_user,ceidr,"invitation");
  }

  /**
   * Fetch events from iCS
   * @param calr
   * @param from
   * @param to
   * @return
   * @throws Exception
   */
  public CalendarData[] fetchEvents(CalendarData[] calr, Date from, Date to) throws Exception {
    String[] calidr = CalendarData.calids(calr);
    CalendarData[] cals = fetchEntries(calidr,"event",from,to);

    // update calr
    if (calidr != null) {
      for (int i = 0; i < calidr.length; i++) {
        CalendarData newCal = CalendarData.findCalendar(cals, calidr[i]);
        CalendarData cal = CalendarData.findCalendar(calr,calidr[i]);
        if (cal != null && newCal != null) {
          if (newCal.getEntry() != null)
            cal.putEntry(newCal.getEntry());
          else
            cal.putEntry(null);
        }
      }
    }

    // Save last fetch range
    m_from = from;
    m_to = to;

    return calr;
  }

  /**
   * Fetch todos from iCS
   * @param calr
   * @param from
   * @param to
   * @return
   * @throws Exception
   */
  public CalendarData[] fetchTodos(CalendarData[] calr, Date from, Date to) throws Exception {
    String[] calidr = CalendarData.calids(calr);
    CalendarData[] cals = fetchEntries(calidr,"todo",from,to);

    // update calr
    if (calidr != null) {
      for (int i = 0; i < calidr.length; i++) {
        CalendarData newCal = CalendarData.findCalendar(cals, calidr[i]);
        CalendarData cal = CalendarData.findCalendar(calr,calidr[i]);
        if (cal != null && newCal != null) {
          if (newCal.getEntry() != null)
            cal.putEntry(newCal.getEntry());
          else
            cal.putEntry(null);
        }
      }
    }

    // Save last fetch range
    m_from = from;
    m_to = to;

    return calr;
  }

  /**
   * Fetch entries
   * @param calr (input) Array of CalendarData object
   * @param from
   * @param to
   * @return
   * @throws Exception
   */
  public CalendarData[] fetchEntries(CalendarData[] calr, Date from, Date to) throws Exception {
    String[] calidr = new String[calr.length];
    for (int i=0; i < calr.length; i++)
      calidr[i] = calr[i].getCalid();

    CalendarData[] cals = fetchEntries(calidr,null,from,to);

    // update calr
    if (calidr != null) {
      for (int i = 0; i < calidr.length; i++) {
        CalendarData newCal = CalendarData.findCalendar(cals, calidr[i]);
        CalendarData cal = CalendarData.findCalendar(calr,calidr[i]);
        if (cal != null && newCal != null) {
          if (newCal.getEntry() != null)
            cal.putEntry(newCal.getEntry());
          else
            cal.putEntry(null);
        }
      }
    }

    // Save last fetch range
    m_from = from;
    m_to = to;

    return calr;
  }

  /**
   * Fetch invitations
   * @param from
   * @param to
   * @return
   * @throws Exception
   */
  public EntryData[] fetchInvitations(Date from, Date to) throws Exception {
    // Form query
    String query = "fetchcomponents_by_range.wcap?fmt-out="+m_fmt_out;
    if( m_version.equals("v5.1"))
      query += "&compressed=0";
    query += "&component-type=event&compstate=REQUEST_NEEDS-ACTION";
    query += "&calid=" + m_user;

    if (from != null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(from);
      cal.add(Calendar.SECOND, -1);
      query += "&dtstart="+ m_sdf.format(cal.getTime());
    }
    if (to != null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(to);
      cal.add(Calendar.SECOND, 1);
      query += "&dtend="+ m_sdf.format(cal.getTime());
    }

    // excutive query
    XMLOutput out = doQuery(query, XMLOutput.OUT_INVITATION_FETCH);

    if (out.getSession() != null)
      m_session = out.getSession();
    m_error = out.getError();

    return out.readInvitations();
  }

  /**
   * Fetch all invitations
   * @return
   * @throws Exception
   */
  public EntryData[] fetchInvitations() throws Exception {
    return fetchInvitations(null, null);
  }

  /**
   * Count number of invitations
   * @param from
   * @param to
   * @return
   * @throws Exception
   */
  public int countInvitations(Date from, Date to) throws Exception {
    // Form query
    String query = "fetchcomponents_by_range.wcap?fmt-out="+m_fmt_out;
    if( m_version.equals("v5.1"))
      query += "&compressed=0";
    query += "&component-type=event&compstate=REQUEST_NEEDS-ACTION";
    query += "&calid=" + m_user;
    //truyen 6/6/02
    if (from != null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(from);
      cal.add(Calendar.SECOND, -1);
      query += "&dtstart="+ m_sdf.format(cal.getTime());
    }
    if (to != null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(to);
      cal.add(Calendar.SECOND, 1);
      query += "&dtend="+ m_sdf.format(cal.getTime());
    }

    // excutive query
    XMLOutput out = doQuery(query, XMLOutput.OUT_INVITATION_COUNT);
    if (out.getSession() != null)
      m_session = out.getSession();
    m_error = out.getError();

    return out.countInvitations();
  }

  /**
   * Search events
   * @param cals
   * @param text
   * @param category
   * @param lookin
   * @param from
   * @param to
   * @return
   * @throws Exception
   */
  public CalendarData[] searchEvent(CalendarData[] cals, String text, String category, int lookin, Date from, Date to) throws Exception {
    for (int i = 0; i < cals.length; i++)
      cals[i].putEntry(null);

    // Create query for fetching all components with given parameters
    String query = "fetchcomponents_by_range.wcap?fmt-out="+m_fmt_out;
    if( m_version.equals("v5.1"))
      query += "&compressed=0";
    query += "&component-type=event";
    if (cals != null && cals.length > 0)
      query += "&calid="+joinCalid(cals, ';');
    if (from != null) {
      //Truong 31_8
      Calendar cal = Calendar.getInstance();
      cal.setTime(from);
      cal.add(Calendar.SECOND, -1);
      query += "&dtstart="+ m_sdf.format(cal.getTime());
      //query += "&dtstart="+m_sdf.format(from);
    }
    if (to != null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(to);
      cal.add(Calendar.SECOND, 1);
      query += "&dtend="+ m_sdf.format(cal.getTime());
      //query += "&dtend="+m_sdf.format(to);
    }

    // execute query and parse to Node
    XMLOutput out = doQuery(query, XMLOutput.OUT_EVENT_FETCH);
    out.searchEvent(cals, text, category, lookin);
    if (out.getSession() != null)
      m_session = out.getSession();
    m_error = out.getError();

    return cals;
  }

  /**
   * Delete events
   * @param calidr
   * @param from
   * @param to
   * @throws Exception
   */
  public void deleteEvents(String[] calidr, Date from, Date to) throws Exception {
    deleteEntries(calidr, from, to, "events");
  }

  /**
   * Delete todos
   * @param calidr
   * @param from
   * @param to
   * @throws Exception
   */
  public void deleteTodos(String[] calidr, Date from, Date to) throws Exception {
    deleteEntries(calidr, from, to, "todos");
  }

  /**
   * Add links for entries
   * @param calto
   * @param calfrom
   * @param ceidr
   * @throws Exception
   */
  public void linkEntries(String calto, String calfrom, EntryRange[] ceidr) throws Exception {
    // addlink
    String query = "addlink.wcap?fmt-out="+m_fmt_out;
    query += "&destCal="+calto + "&srcCal=" + calfrom;
    ;
    String uids = "";
    String rids = "";
    String mods = "";
    if (ceidr != null) {
      for (int i = 0; i < ceidr.length; i++) {
        if (i > 0) {
          uids += ";";
          rids += ";";
        }
        uids += uid(ceidr[i]);
        rids += rid(ceidr[i]);
        mods += mod(ceidr[i]);
      }
      query += "&uid="+uids;
      query += "&rid="+rids;
    }
    call(query+"&id="+m_session);
  }

  //--------------------------------------------------------------------------//

  /**
   * Fetch "Freebusy". This version always returns null.
   * @param calid
   * @param start
   * @param end
   * @return
   * @throws Exception
   */
  public DurationData[] fetchFreebusy(String[] calid, Date start, Date end) throws Exception {
    // get_freebusy
    return null;
  }

  /**
   * Generate a globally unique identifier
   * @return
   * @throws Exception
   */
  public String getGUID() throws Exception {
    XMLOutput out = doQuery("get_guids.wcap?fmt-out="+m_fmt_out, XMLOutput.OUT_NONE);
    return out.getGUID();
  }

  //--------------------------------------------------------------------------//

  CalendarData[] exec(String query, int outFlags) throws Exception {
    CalendarData[] calr = null;
    login();
    if (query != null)
      calr = read(call(query+"&id="+m_session),outFlags);
    logout();
    return calr;
  }

  XMLOutput doQuery(String query, int outputFlags) throws Exception {
    login();
    XMLOutput xmlout = new XMLOutput(call(query+"&id="+m_session), m_user, outputFlags);
    logout();
    return xmlout;
  }

  InputStream call(String query) throws Exception {
    //System.out.println("query="+query);
    URL url = new URL("http://"+m_host+":"+m_port+"/"+query);
    URLConnection conn = url.openConnection();
    InputStream is = url.openStream();
    return m_p2? p2top3(is): is;
  }

  InputStream p2top3(InputStream stream) throws Exception {
    DataInputStream inStream = new DataInputStream(stream);
    StringBuffer sb = new StringBuffer();
    try {
      byte ch;
      while ((ch = inStream.readByte()) != -1) {
        sb.append((char)ch);
      }
    } catch(EOFException eof) {}
    //String buff = sb.toString();
    // Remove all "<?xml ...?> except the first
    int idx1=0;

    while ((idx1 = sb.toString().lastIndexOf("<?xml")) > 1) {
      int idx2 = sb.toString().indexOf("?>",idx1);
      sb.delete(idx1,idx2 + 2);
    }
    // Remove all </iCalendar><iCalendar>
    String st = "</iCalendar>\n\n<iCalendar>";
    while ((idx1 = sb.toString().lastIndexOf(st)) > -1) {
      sb.delete(idx1,idx1 + st.length());
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream(sb.length());
    for (int i=0; i < sb.length(); i++) {
      int ch = sb.charAt(i);
      baos.write(ch);
    }
    //System.out.println(baos.toString());
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    return bais;
  }

  CalendarData[] read(InputStream stream, int outFlags) throws Exception {
    m_error = null;
    XMLOutput out = new XMLOutput(stream, m_user, outFlags);
    CalendarData[] ret = out.readCalendars();
    if (out.getSession() != null)
      m_session = out.getSession();
    m_error = out.getError();
    return ret;
  }

  //--------------------------------------------------------------------------//

  static String enc(String freq, Integer interval, Integer count, Date until, String byDay) {
    String rule = "\"FREQ%3D"+freq;
    if (interval != null)
      rule += "%3BINTERVAL%3D"+interval;
    if (count != null)
      rule += "%3BCOUNT%3D"+count;
    if (until != null) {
      // truong 25/5/02
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T120000Z'");
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      rule += "%3BUNTIL%3D"+ sdf.format(until);
    }
    if (byDay != null)
      rule += "%3BBYDAY%3D"+ byDay;
    rule += "\"";

    return rule;
  }

  static String enc(String s) {
    s = XMLData.replace(s, " ", "%20");
    s = XMLData.replace(s, "=", "%3D");
    s = XMLData.replace(s, "&", "%26");
    s = XMLData.replace(s, "\n", "%0A");
    s = XMLData.replace(s, "#", "%23");
    //s = XMLData.replace(s, "\"", "&quot;");
    return s;
  }

  static String join(String[] szr, char sep) {
    String sz = "";
    for (int i = 0; i < szr.length; i++) {
      if (i > 0)
        sz += sep;
      sz += enc(szr[i]);
    }
    return sz;
  }

  static String joinCalid(CalendarData[] szr, char sep) {
    String sz = "";
    for (int i = 0; i < szr.length; i++) {
      if (i > 0)
        sz += sep;
      sz += enc(szr[i].getCalid());
    }
    return sz;
  }

  static String iso8601(long time) {
    int hours = (int)(time/3600000);
    int minutes = (int)((time%3600000)/60000);
    int seconds = (int)(((time%3600000)%60000)/1000);
    return "PT"+hours+"H"+minutes+"M"+seconds+"S";
  }

  static String uid(EntryRange ceid) {
    int dot = ceid.m_ceid.indexOf(".");
    return dot == -1? ceid.m_ceid:ceid.m_ceid.substring(0, dot);
  }

  static String rid(EntryRange ceid) {
    int dot = ceid.m_ceid.indexOf(".");
    return dot == -1? "0":ceid.m_ceid.substring(dot+1);
  }

  static String mod(EntryRange ceid) {
    if (ceid.m_mod == 0)
      return "1";
    if (ceid.m_mod == EntryRange.FUTURE)
      return "2";
    if (ceid.m_mod == EntryRange.PAST)
      return "3";
    if (ceid.m_mod == EntryRange.FUTURE+EntryRange.PAST)
      return "4";
    return "0";
  }

  //=====================================//
  static boolean contains(String[] arr, String s) {
    for (int i = 0; i < arr.length; i++)
      if (arr[i].equals(s))
        return true;
    return false;
  }

  String formEntryQuery(String calid, EntryRange ceid, EntryData ent) throws Exception {
    String query = ent.isTodo()?"storetodos.wcap":"storeevents.wcap";
    query += "?fmt-out=" + m_fmt_out + "&calid="+calid;

    // Ceid
    if (ceid != null) {
      query += "&uid="+uid(ceid);
      query += "&rid="+rid(ceid);
      if (!rid(ceid).equals("0") )
        query += "&mod="+mod(ceid);
      else if (ent.getRecurrence() != null && ent.getRecurrence().length > 0)
        query += "&mod=2";
      else
        query += "&mod=1";
    }

    // Share
    String share = ent.getShare();
    if (share != null)
      query += "&icsClass="+share;

    // Duration
    DurationData dur = ent.getDuration();
    if (dur != null)
      if (dur.getLength()!= null && dur.getLength().getAllDay()) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T120000Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        query += "&dtstart="+sdf.format(dur.getStart());
        query += "&duration=PT0H0M0S";
        //if (!m_p2) truong 21/5/02
        //  query += "&isAllDay=1";
      } else {
        if (dur.getStart() != null)
          query += "&dtstart="+m_sdf.format(dur.getStart());

        if (dur.getLength() != null)
          query += "&duration="+iso8601(dur.getLength().getLength());
        //if (!m_p2) truong 21/5/02
        //  query += "&isAllDay=0";
      }

    // Location
    String loc = ent.getLocation();
    if (loc != null && loc.length() == 0)
      loc = "<NONE>";
    if (loc != null)
      query += "&location="+enc(loc);

    // Organizer
    OrganizerData org = ent.getOrganizer();
    if (org != null) {
      if (org.getSentby() != null)
        query += "&orgEmail="+org.getSentby();
      if (org.getCuid() != null)
        query += "&orgUID=" + org.getCuid();
    }

    // RelatatedTo
    String[] relatedTos = ent.getRelatedTos();
    if (relatedTos != null && relatedTos.length > 0)
      query += "&relatedTos=" + join(relatedTos,';');

    // Resources
    String[] resources = ent.getResources();
    if (resources != null && resources.length > 0)
      query += "&resources=" + join(resources,';');

    // Attendees
    if (!ent.isTodo()) {
      AttendeeData[] attr = ent.getAttendee();
      if (attr != null && attr.length > 0) {
        String atts = "";
        for (int i = 0; i < attr.length; i++) {
          if (i > 0)
            atts += ";";
          if (attr[i].getStatus() != null)
            atts += "PARTSTAT=" + attr[i].getStatus();
          else
            atts += "PARTSTAT=NEEDS-ACTION";

          if (attr[i].getRSVP() != null)
            atts += "^RSVP=" + attr[i].getRSVP().toString().toUpperCase();
          if (attr[i].getRole() != null)
            atts += "^ROLE=" + attr[i].getRole();

          //atts += "^" + attr[i].get();
          atts += "^" + attr[i].getCuid();
        }
        query += "&attendees="+atts;
      }
    }

    // Reminders
    AlarmData[] alr = ent.getAlarm();
    if (alr != null) {
      if (alr.length > 0) {
        query += "&alarmStart="+m_sdf.format(alr[0].getTrigger());
        query += "&alarmEmails="+join(alr[0].getRecipient(), ';');
      } else // remove alarm
      {
        query += "&alarmStart=0";
        query += "&alarmEmails=0";
      }
    }

    // recurrences
    RecurrenceData[] recr = ent.getRecurrence();
    if (recr != null && recr.length > 0) {
      String rdates = null;
      String rrules = null;
      String exdates = null;
      String exrules = null;
      for (int i = 0; i < recr.length; i++) {
        boolean ex = recr[i].getExclude() != null && recr[i].getExclude().booleanValue();
        Date date = recr[i].getDate();
        if (date != null) {
          if (ex)
            exdates = exdates == null? m_sdf.format(date):exdates+";"+m_sdf.format(date);
          else
            rdates = rdates == null? m_sdf.format(date):rdates+";"+m_sdf.format(date);
        }
        String freq = recr[i].getFrequency();
        if (freq != null) {
          String rule = enc(freq, recr[i].getInterval(), recr[i].getCount(), recr[i].getUntil(), recr[i].getByDay());
          if (ex)
            exrules = exrules == null? rule:exrules+";"+rule;
          else
            rrules = rrules == null? rule:rrules+";"+rule;
        }
      }

      if (rdates != null)
        query += "&rdates="+rdates;
      if (rrules != null) {
        query += "&rrules="+rrules;
        query += "&rchange=1";
      }
      if (exdates != null)
        query += "&exdates="+exdates;
      if (exrules != null)
        query += "&exrules="+exrules;
    }

    // Todo/Event properties
    if (ent.isTodo()) {
      TodoData td = ent.getTodo();
      Integer pri = td.getPriority();
      if (pri != null)
        query += "&priority="+pri;
      String stat = td.getStatus();
      if (stat != null)
        query += "&status="+stat;
      String[] catr = td.getCategory();
      if (catr != null && catr.length > 0)
        query += "&categories="+join(catr, ';');
      String sum = td.get();
      if (sum != null && sum.length() == 0)
        sum = "Untitled";
      if (sum != null)
        query += "&summary="+enc(sum);
      String desc = td.getDescription();
      if (desc != null && desc.length() == 0)
        desc = "<NONE>";
      if (desc != null)
        query += "&desc="+enc(desc);

      Date due = td.getDue();
      if (due != null)
        query += "&due="+ m_sdf.format(due);

      if (td.getCompletion() != null) {
        if (td.getCompletion().getCompleted() != null &&
            td.getCompletion().getPercent() != null &&
            td.getCompletion().getPercent().intValue() == 100) {
          query += "&completed=" + m_sdf.format(td.getCompletion().getCompleted());
          query += "&percent=" + td.getCompletion().getPercent();
        } else if (td.getCompletion().getPercent() != null &&
                   td.getCompletion().getPercent().intValue() == 0)
          query += "&completed=0&percent=0";
      }

    } else {
      EventData ev = ent.getEvent();
      if (ev != null) {
        Integer pri = ev.getPriority();
        if (pri != null)
          query += "&priority="+pri;
        String stat = ev.getStatus();
        if (stat != null)
          query += "&status="+stat;
        String[] catr = ev.getCategory();
        if (catr != null && catr.length > 0)
          query += "&categories="+join(catr, ';');
        String sum = ev.get();
        if (sum != null)
          query += "&summary="+enc(sum);
        String desc = ev.getDescription();
        if (desc != null && desc.length() == 0)
          desc = "<NONE>";
        if (desc != null)
          query += "&desc="+enc(desc);
      }
    }

    return query + "&method=" + (ent.isInvitation()? "4": "2") + "&fetch=1";
  }

  //--------------------------------------------------------------------------//

  /**
   * Do not update m_cals, instead this method return array of entries
   * @param type = "event", fetchevents
   * type= "todo", fetchevents
   */
  EntryData[] fetchEntriesByIds(String calid, EntryRange[] ceidr, String type) throws Exception {
    // Form query: fetchevents_by_id, fetchtodos_by_id
    String query = type.equals("todo")?"fetchtodos_by_id.wcap":"fetchevents_by_id.wcap";
    query += "?fmt-out=" + m_fmt_out;
    if (calid != null)
      query += "&calid=" + calid;

    String uids = "";
    String rids = "";
    String mods = "";
    for (int i = 0; i < ceidr.length; i++) {
      if (i > 0) {
        uids += ";";
        rids += ";";
        mods += ";";
      }
      uids += uid(ceidr[i]);
      rids += rid(ceidr[i]);
      mods += mod(ceidr[i]);
    }
    query += "&uid="+uids;
    query += "&rid="+rids;
    query += "&mod="+mods;

    // Execute query and update m_cals
    if( type.equals("invitation")) {
      XMLOutput out = doQuery(query, XMLOutput.OUT_INVITATION_FETCH);
      if (out.getSession() != null)
        m_session = out.getSession();
      m_error = out.getError();
      return out.readInvitations();
    }

    // Todos or events
    int flag = type.equals("event")?XMLOutput.OUT_EVENT_FETCH:XMLOutput.OUT_TODO_FETCH;
    CalendarData newCal = CalendarData.findCalendar(exec(query,flag), calid);
    if (newCal != null)
      return newCal.getEntry();
    else
      return null;
  }

  /**
   * @param type is one of "event", "todo", null
   */
  CalendarData fetchEntries(String calid, String type, Date from, Date to) throws Exception {
    //    String query = "fetchcomponents_by_range.wcap?fmt-out="+m_fmt_out + "&calid="+calid;
    String query = "fetchcomponents_by_range.wcap?fmt-out="+m_fmt_out;
    if (calid != null)
      query += "&calid="+calid;
    if( type != null)
      query += "&component-type=" + type;
    if( m_version.equals("v5.1"))
      query += "&compressed=0";
    //System.out.println("query 1 "+ query);
    if (from != null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(from);
      cal.add(Calendar.SECOND, -1);
      query += "&dtstart="+ m_sdf.format(cal.getTime());
    }
    if (to != null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(to);
      cal.add(Calendar.SECOND, 1);
      query += "&dtend="+ m_sdf.format(cal.getTime());
    }

    // Execute query and update m_cals
    CalendarData[] cals = exec(query, XMLOutput.OUT_ENTRY_FETCH);
    return cals[0];
  }

  CalendarData[] fetchEntries(String[] calidr, String type, Date from, Date to) throws Exception {
    // Execute query
    CalendarData[] cals = null;
    if (m_p2) {
      cals = new CalendarData[calidr.length];
      for (int i=0; i< calidr.length; i++)
        cals[i] = fetchEntries(calidr[i], type, from, to);
    } else {
      String query = "fetchcomponents_by_range.wcap?fmt-out="+m_fmt_out;
      if( type != null)
        query += "&component-type=" + type;
      if( m_version.equals("v5.1"))
        query += "&compressed=0";
      if (calidr != null && calidr.length > 0 && calidr[0] != null)
        query += "&calid="+join(calidr, ';');
      if (from != null)
        query += "&dtstart="+m_sdf.format(from);
      if (to != null)
        query += "&dtend="+m_sdf.format(to);
      //System.out.println("query 2 "+ query);
      cals = exec(query, XMLOutput.OUT_ENTRY_FETCH);
    }
    /*
    // update m_cals
    for (int i = 0; i < calidr.length; i++)
    {
      CalendarData newCal = CalendarData.findCalendar(cals, calidr[i]);
      CalendarData cal = CalendarData.findCalendar(m_cals,calidr[i]);
      if (cal != null && newCal != null)
      {
        if (newCal.getEntry() != null)
          cal.putEntry(newCal.getEntry());
        else
          cal.putEntry(null);
      }
    }
    */

    return cals;
  }

  void deleteEntries(String[] calidr, Date from, Date to, String type) throws Exception {
    String query = null;
    if (type.equals("events"))
      query = "deleteevents_by_range.wcap?fmt-out="+m_fmt_out;
    else if (type.equals("todos"))
      query = "deletetodos_by_range.wcap?fmt-out="+m_fmt_out;
    else
      return;

    if (calidr != null && calidr.length > 0)
      query += "&calid="+join(calidr, ';');
    if (from != null)
      query += "&dtstart="+ m_sdf.format(from);
    if (to != null)
      query += "&dtend="+ m_sdf.format(to);
    exec(query, type.equals("events")? XMLOutput.OUT_EVENT_DELETE:XMLOutput.OUT_TODO_DELETE);

    // Refresh data
    if (m_from != null && m_to != null)
      fetchEntries(calidr, null,m_from, m_to);
  }

  public String getError() {
    if( m_error == null)
      return ERR_UNKNOWN;

    // Error code
    try {
      int code = Integer.parseInt(m_error);
      return decodeError( code);
    } catch( NumberFormatException ne) {
      // m_error is description string
      return "[WCAP] " + m_error;
    }
  }

  static String decodeError( int code) {
    switch( code) {
    case -1: // unknown host
      return ERR_UNKNOWN_HOST;
    case -2: // connect failed
      return ERR_CONNECT_FAILED;
    case 1: // login failed
      return ERR_LOGIN_FAILED;
    default:
      return "[WCAP] Transaction failed: "+code;
    }
  }

  CalendarData[] cloneCalrProps(CalendarData[] cals) throws Exception {
    CalendarData[] ret = new CalendarData[cals.length];
    for (int i = 0; i < cals.length; i++) {
      ret[i] = new CalendarData();
      ret[i].updateProps(cals[i],true);
    }
    return ret;
  }


  //--------------------------------------------------------------------//

  /**
   * Comlete a todo
   * @param calid
   * @param ceid
   * @param complete
   * @throws Exception
   */
  public void completeTodo(String calid, EntryRange ceid, CompletionData complete) throws Exception {
    String query = "storetodos.wcap?fmt-out=" + m_fmt_out + "&calid="+calid;
    query += "&uid="+uid(ceid) + "&rid="+rid(ceid) + "&mod=1";
    if (complete != null) {
      if (complete.getPercent().intValue()== 0)
        query += "&percent="+complete.getPercent().intValue()+"&completed=0";
      else
        query += "&percent="+complete.getPercent().intValue()+"&completed=" + m_sdf.format(complete.getCompleted());
    }
    query += "&method=2&fetch=1";
    exec(query, XMLOutput.OUT_TODO_UPDATE);
  }
}
