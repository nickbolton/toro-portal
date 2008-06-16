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

package net.unicon.academus.apps.calendar.dpcs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Vector;
import java.util.Properties;
import java.util.TimeZone;
import java.util.HashMap;
import java.util.Hashtable;
import java.io.InputStream;
import java.io.OutputStream;

import net.unicon.academus.apps.calendar.ACEData;
import net.unicon.academus.apps.calendar.AlarmData;
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
import net.unicon.academus.apps.calendar.TodoData;
import net.unicon.academus.apps.dpcs.DPCSBo;
import net.unicon.academus.apps.dpcs.DPCSBoHome;
import net.unicon.academus.apps.dpcs.DPCSBoRemote;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.SQL;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.portal.channels.rad.Security;
import net.unicon.portal.channels.rad.GroupData;
import net.unicon.portal.channels.rad.Channel;
import net.unicon.portal.channels.rad.Finder;
import net.unicon.sdk.log.*;

import org.jasig.portal.security.IPerson;

/**
 * DP Caledar server API
 * <p>Title: Dan Phong Calendar Server</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: IBS-DP</p>
 * @author
 * @version 1.0.c
 */

public class DPCS extends net.unicon.academus.apps.calendar.CalendarServer{

  // DPCS errors
  static final String ERR_UNKNOWN = "[DPCS] Unknown error.";
  static final String ERR_DPCSBO = "[DPCS] DPCSBo error.";
  static final String ERR_CANNOT_CREATE_DPCSBO = "[DPCS] Can not create DPCSBo.";

  // Constants
  static final String GRANT = "GRANT";
  static final String DENY = "DENY";
  static final String RWFS = "RWFS";
  static final String RFS = "RFS";
  static final String CALENDAR = "net.unicon.academus.apps.calendar.DPCS";
  static Boolean TRUE = new Boolean(true);
  static Boolean FALSE = new Boolean(false);
  static public final SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

  // Filter for m_cals
  Date m_from = null;
  Date m_to = null;

  XMLData m_logon = new XMLData();
  IdentityData m_person = new IdentityData();
  DPCSBoRemote m_ejb = null;

  /**
   * Contructor
   * Initial time format, time zone of server
   */

  public DPCS()
  {
    m_sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  /**
   * Initial claendar server. Create business object
   * @param props
   */

  public void init(Properties props)
  {
    try
    {
      if (m_ejb == null)
      {
        DPCSBoHome home =  (DPCSBoHome)net.unicon.portal.channels.rad.Channel.ejbHome("DPCSBo");
        m_ejb = home.create();
      }
    }
    catch (Exception e)
    {
      Channel.log(ERR_CANNOT_CREATE_DPCSBO);
      e.printStackTrace(System.err);
    }
  }

  //--------------------------------------------------------------------------//

  public String m_user = null;
  public String m_password = null;

  /**
   * Get logon user
   * @return java.lang.String - username
   */

  public String getUser()
  {
    return m_user;
  }

  /**
   * Get logon data as private xml format.
   * Example:
   * <pre>
   *   &lt;logon user='demo'&gt;
   *    &lt;access calid='demo_4' rights='RWFS'/&gt;
   *    &lt;access calid='demo_3' rights='RWFS'/&gt;
   *    &lt;access calid='demo' rights='RWFS'/&gt;
   *    &lt;access calid='1_2' rights='RFS'/&gt;
   *   &lt;/logon&gt;
   * </pre>
   *
   * @return XMLData containing logon user and access rights. 
   */

  public XMLData getLogonData()
  {
    XMLData logonData = new XMLData();
    logonData.putE("logon", m_logon);
    return logonData;
  }

  //--------------------------------------------------------------------------//

  /**
   * Not use
   */

  public int getCuidCode(String cuid) {return getCuidType(cuid);}

  static int getCuidType(String cuid)
  {
    if (cuid == null) return 0;

    if (cuid.equals("@")) return CUID_ALL;
    else if (cuid.equals("@@p")) return CUID_OWNER;
    else if (cuid.equals("@@o")) return CUID_SHARE;
    else if (cuid.equals("@@n")) return CUID_NOSHARE;
    else return 0;
  }

  //--------------------------------------------------------------------------//

  /**
   * Login to Dan Phong canlendar server
   * @param person IdentityData of logon user
   * @param calidr array of calid
   * @return true array of <code>CalendarData</code> that logon user can access
   * @throws Exception
   */

  public CalendarData[] login(IdentityData person, String password) throws Exception
  {
    try
    {
      m_person = person;
      m_user = (String)m_person.getAlias();
      m_password = password;
      //log
      //Channel.log("method: login call security m_user " + m_user);
      Security sec = new Security(CALENDAR);
      String[] calidr = sec.getPermissionTargets(m_person, null);
      buildLogonData(sec);
      //log
      //Channel.log("method: login end call security");

      if (calidr == null)
      {
        calidr = new String[]{m_user};
        // create personal calendar
        CalendarData cal = new CalendarData();
        cal.putCalid(m_user);
        cal.putCalname(m_user);
        cal.putOwner(m_user);
        cal.putA("created", Channel.getCurrentDate());
        cal.putA("last-modified", Channel.getCurrentDate());

        createCalendar(cal);
      }
      else
      {
        boolean foundPerCal =  false;
        for (int i = 0; i < calidr.length; i++)
        {
          if (calidr[i].equals(m_user))
            foundPerCal = true;
        }
        if (!foundPerCal)
        {
          // build new calidr
          String[] calidr1 = new String[calidr.length + 1];
          for (int i = 0; i < calidr.length; i++)
          {
            calidr1[i] = calidr[i];
          }
          calidr1[calidr1.length-1] = m_user;
          calidr = calidr1;

          // create personal calendar
          CalendarData cal = new CalendarData();
          cal.putCalid(m_user);
          cal.putCalname(m_user);
          cal.putOwner(m_user);
          cal.putA("created", Channel.getCurrentDate());
          cal.putA("last-modified", Channel.getCurrentDate());

          createCalendar(cal);
        }
      }

      // call DPCSBo
      m_ejb.login(m_person, calidr);
      //log
      //Channel.log("method: login - calidr.length " + calidr.length);

      //return calidr;
      //return cloneCalrProps(getCalendars(null));
      return getCalendars(null);
    }
    catch (Exception e){
      m_error = e.getMessage();
      e.printStackTrace(System.err);
      throw e;
    }
  }

  /**
   * Not use
   */

  public void logout() throws Exception
  {
    // not implement
  }

  /**
   * Not use
   */
  public void changePassword(String user, String oldPassword, String newPassword) throws Exception
  {
    // change_password
  }

  /**
   * Not use
   */

  public boolean authenticate(IdentityData person, String password) throws Exception
  {
    return true;
  }

  //--------------------------------------------------------------------------//

  /**
   * Update a calendar
   * @param cal object of <code>net.unicon.academus.apps.calendar.CalendarData</code>
   * @return object of <code>net.unicon.academus.apps.calendar.CalendarData</code>
   * @throws Exception
   */

  public CalendarData updateCalendar(CalendarData cal) throws Exception
  {
    m_ejb.updateCalendar(cal);
    removeCalPermissionProps(cal);
    addCalPermissionProps(cal);
    m_ejb.setCalr(new CalendarData[]{cal});

    return cal;
  }

  /**
   * Delete a Calendar
   * @param calid java.lang.String
   * @throws Exception
   */

  public void deleteCalendar(String calid) throws Exception
  {
    m_ejb.deleteCalendar(calid);
    removeCalPermissionProps(calid);
  }

   /**
   * Create a calendar
   * @param cal <code>net.unicon.academus.apps.calendar.CalendarData</code> object
   * @return <code>net.unicon.academus.apps.calendar.CalendarData</code> object
   * @throws Exception
   * @see net.unicon.academus.apps.calendar.CalendarData
   */

  public CalendarData createCalendar(CalendarData cal) throws Exception
  {
    // create calendar
    cal = m_ejb.createCalendar(cal);
    cal = addCalPermissionProps(cal);
    m_ejb.setCalr(new CalendarData[]{cal});

    return cal;
  }

  /**
   * Get calendars
   * @param calidr Array of calids
   * @return <code>net.unicon.academus.apps.calendar.CalendarData</code> objects
   * @throws Exception
   */

  public CalendarData[] getCalendars(String[] calidr)
  {
    try{
      CalendarData[] calr = m_ejb.getCalendars(calidr);
      calr = buildACE(calr);
      m_ejb.setCalr(calr);

      return calr;
    }
    catch (Exception e)
    {
      Channel.log(ERR_DPCSBO);
      e.printStackTrace(System.err);
    }

    return null;
  }

  /**
   * Not use
   */

  public String getGUID() throws Exception
  {
    return null;
  }

  //--------------------------------------------------------------------------//

  /**
   * Get Entry
   * @param calid java.lang.String
   * @param ceid java.lang.String - id of entry
   * @return <code>net.unicon.academus.apps.calendar.CalendarData</code> object
   * @throws Exception
   */

  public EntryData getEntry(String calid, String ceid) throws Exception
  {
    return m_ejb.getEntry(calid, ceid);
  }

  /**
   * Delete events. In case recurrent event It can delete one or past or future occurences
   * @param calid java.lang.String - id of calendar contains event
   * @param ceidr Array of <code>net.unicon.academus.apps.calendar.EntryRange</code> objects
   * @throws Exception
   */

  public void deleteEvents(String calid, EntryRange[] ceidr) throws Exception
  {
    try{
      m_ejb.deleteEvents(calid, ceidr);
    }
    catch (Exception e){
      m_error = e.getMessage();
      e.printStackTrace(System.err);
      throw e;
    }
  }

  /**
   * Delete todos. In case recurrent todo It can delete one or past or future occurences
   * @param calid java.lang.String - id of calendar contains todo
   * @param ceidr Array of <code>net.unicon.academus.apps.calendar.EntryRange</code> objects
   * @throws Exception
   */

  public void deleteTodos(String calid, EntryRange[] ceidr) throws Exception
  {
    try{
      m_ejb.deleteTodos(calid, ceidr);
    }
    catch (Exception e){
      m_error = e.getMessage();
      e.printStackTrace(System.err);
      throw e;
    }
  }

  /**
   * Create entry
   * @param calid
   * @param ent <code>net.unicon.academus.apps.calendar.EntryData<code> object
   * @param ceseq integer - sequence id
   * @return <code>net.unicon.academus.apps.calendar.EntryData<code> object
   * @throws Exception
   */

  public EntryData createEntry(String calid, EntryData ent) throws Exception
  {
    try{
      return m_ejb.createEntry(calid, ent, 0);
    }
    catch (Exception e){
      m_error = e.getMessage();
      e.printStackTrace(System.err);
      throw e;
    }
  }

  /**
   * Accept or Decline an invitation entry
   * @param calid Id of calendar contain this invitation entry
   * @param ceid Id of invitation entry
   * @param status ACCEPTED or DECLINE
   * @param comment Any comment
   * @throws Exception
   */

  public void replyInvitation(String calid, EntryRange ceid, String status, String comment) throws Exception
  {
    m_ejb.replyInvitation(calid, ceid, status, comment);
  }

  /**
   * Accept or Decline an invitation entry to personal calendar
   * @param ceid Id of invitation entry
   * @param status ACCEPTED or DECLINE
   * @param comment Any comment
   * @throws Exception
   */

  public void replyInvitation(EntryRange ceid, String status, String comment) throws Exception
  {
    m_ejb.replyInvitation(m_user, ceid, status, comment);
  }

  /**
   * Complete a todo
   * @param calid id of Calendar contains todo - java.lang.String
   * @param ceid <code>net.unicon.academus.apps.calendar.EntryRange</code> object contains todo's id
   * @param complete <code>net.unicon.academus.apps.calendar.CompletionData</code> object
   * @throws Exception
   */

  public void completeTodo(String calid, EntryRange ceid, CompletionData complete) throws Exception
  {
    m_ejb.completeTodo(calid, ceid, complete);
  }

  /**
   * Update Entry
   * @param calid id of Calendar contains entry - java.lang.String
   * @param ceid <code>net.unicon.academus.apps.calendar.EntryRange</code> object contains entry's id
   * @param ent <code>net.unicon.academus.apps.calendar.EntryData</code> object
   * @return <code>net.unicon.academus.apps.calendar.EntryData</code> object had been updated
   * @throws Exception
   */

  public EntryData updateEntry(String calid, EntryRange ceid, EntryData ent) throws Exception
  {
    try
    {
      //log
      //Channel.log("DPCS method updateEntry start ");
      if (ent.isInvitation())
      {
        //log
        //Channel.log("DPCS method updateEntry end reply invitation ent.findAttendee(m_user).getStatus() " + ent.findAttendeeById(m_person.getID()).getStatus());
        replyInvitation(calid, ceid, ent.findAttendeeById(m_person.getID()).getStatus(), null);
      }
      else
      {
        //log
        //Channel.log("DPCS method updateEntry starting ");
        return m_ejb.updateEntry(calid, ceid, ent);
      }
      //log
      //Channel.log("DPCS method updateEntry end ");

      return ent;
    }
    catch (Exception e){
      m_error = e.getMessage();
      e.printStackTrace(System.err);
      throw e;
    }
  }

  /**
  *  Fetch Events by id
   * @param calid java.lang.String
   * @param ceidr array of <code>net.unicon.academus.apps.calendar.EntryRange</code> objects contain id of events
   * @return array of <code>net.unicon.academus.apps.calendar.EntryData</code> objects contain Events
   * @throws Exception
  */

  public EntryData[] fetchEventsByIds(String calid, EntryRange[] ceidr) throws Exception
  {
    return m_ejb.fetchEventsByIds(calid, ceidr);
  }

  /**
   * Fetch Todos by id
   * @param calid java.lang.String
   * @param ceidr array of <code>net.unicon.academus.apps.calendar.EntryRange</code> objects contain id of todos
   * @return array of <code>net.unicon.academus.apps.calendar.EntryData</code> objects contain Todos
   * @throws Exception
  */

  public EntryData[] fetchTodosByIds(String calid, EntryRange[] ceidr) throws Exception
  {
    return m_ejb.fetchTodosByIds(calid, ceidr);
  }

  /**
   * Fetch Events of calendars in one period
   * @param array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects not include events
   * @param from java.util.Date
   * @param to java.util.Date
   * @return array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects include events
   * @throws Exception
   */

  public CalendarData[] fetchEvents(CalendarData[] calr, Date from, Date to) throws Exception
  {
    return m_ejb.fetchEvents(calr, from, to);
  }

  /**
   * Fetch Todos of calendars in one period
   * @param calr array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects not include todos
   * @param from java.util.Date
   * @param to java.util.Date
   * @return array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects include todos
   * @throws Exception
   */

  public CalendarData[] fetchTodos(CalendarData[] calr, Date from, Date to) throws Exception
  {
    return m_ejb.fetchTodos(calr, from, to);
  }

  /**
   * Fetch Entries of calendars in one period
   * @param array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects not include entries
   * @param from java.util.Date
   * @param to java.util.Date
   * @return array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects include entries
   * @throws Exception
   */
  public CalendarData[] fetchEntries(CalendarData[] calr, Date from, Date to) throws Exception
  {
    return m_ejb.fetchEntries(calr, from, to);
  }

  /**
   * Count all invitations
   * @return integer - number of invitations
   * @throws Exception
   */

  public int countInvitations() throws Exception
  {
    return countInvitations(null, null);
  }

  /**
   * Count invitations in period
   * @param from java.util.Date
   * @param to java.util.Date
   * @return integer - number of invitations
   * @throws Exception
   */

  public int countInvitations(Date from, Date to) throws Exception
  {
    EntryData[] iv = fetchInvitations(from, to);
    return (iv != null?iv.length:0);
  }

  /**
   * Fetch all invitaions
   * @return Array of <code>EntryData</code> objects - invitations
   * @throws Exception
   */

  public EntryData[] fetchInvitations() throws Exception
  {
    return fetchInvitations(null, null);
  }

  /**
   * Fetch invitations in a period
   * @param from java.util.Date
   * @param to java.util.Date
   * @return Array of <code>net.unicon.academus.apps.calendar.EntryData</code> object
   * @throws Exception
   */

  public EntryData[] fetchInvitations(Date from, Date to) throws Exception
  {
    GroupData[] gr = GroupData.getAncestors(m_person, false);
    String grs = "";
    grs = atoString(gr);
    m_person.putA("dpcs_grlist",grs);

    return m_ejb.fetchInvitations(from, to);
  }

  /**
   * Search Event by category, look in title, notes, place
   * @param array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects
   * @param text java.lang.String
   * @param category java.lang.String
   * @param lookin integer, lookin have value CalendarServer.LOOK_IN_TITLE, CalendarServer.LOOK_IN_NOTES, CalendarServer.LOOK_IN_PLACE
   * @param from java.util.Date
   * @param to java.util.Date
   * @return array of <code>net.unicon.academus.apps.calendar.CalendarData</code> objects include event found
   * @throws Exception
   */

  public CalendarData[] searchEvent(CalendarData[] cals, String text, String category, int lookin, Date from, Date to) throws Exception
  {
    return m_ejb.searchEvent(cals, text, category, lookin, from ,to);
  }

  /**
   * Delete all Events in Calendars
   * @param calidr Array of java.lang.String contains calendar ids
   * @param from java.util.Date
   * @param to java.util.Date
   * @throws Exception
   */

  public void deleteEvents(String[] calidr, Date from, Date to) throws Exception
  {
    m_ejb.deleteEvents(calidr, from, to);
  }

  /**
   * Delete all Todos in Calendars
   * @param calidr Array of java.lang.String contains calendar ids
   * @param from java.util.Date
   * @param to java.util.Date
   * @throws Exception
   */

  public void deleteTodos(String[] calidr, Date from, Date to) throws Exception
  {
    m_ejb.deleteTodos(calidr, from, to);
  }

  //--------------------------------------------------------------------------//
  public String m_error = "unknown error";

  /**
   * Get server error when server crash
   * @return java.lang.String
   */

  public String getError()
  {
    return m_error;
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

  public String importEntries(InputStream is, String format, String calid, Hashtable map) throws Exception{
    return m_ejb.importEntries(is, format, calid, map);
  }

  /**
   * Export entries of Calendar to Tab delimited file
   * @param format "TODO" or "EVENT"
   * @param calids Array of Calendar ID
   * @param from <code>Date</code>
   * @param to <code>Date</code>
   * @return <code>InputStream</code> Contain entries as Tab delimited format
   * @throws Exception
   */

  public InputStream exportEntries(String format, String[] calids, Date from, Date to) throws Exception{
     return m_ejb.exportEntries(format, calids, from, to);
  }

  //--------------------------------------------------------------------------//

  void removeCalPermissionProps(CalendarData cal) throws Exception
  {
    removeCalPermissionProps(cal.getCalid());
  }

  void removeCalPermissionProps(String calid) throws Exception
  {
    Security sec = new Security(CALENDAR);
    sec.removePermissions(null, quot(calid));
    //sec.removePermissions(null, calid);
  }

  CalendarData addCalPermissionProps(CalendarData cal) throws Exception
  {
    // Ensure to add owner to ACL
    ACEData[] acl = ACEData.add( cal.getACE(), new ACEData(m_person,true));
    Finder.findDetails(acl,null);
    cal.putACE(acl);

    IdentityData id;
    Security sec = new Security(CALENDAR);
    try {
        for (int i = 0; i < acl.length; i++)
        {
          if (acl[i].getType().endsWith(IdentityData.ENTITY))
            id = new IdentityData(IdentityData.ENTITY, acl[i].getEntityType(), acl[i].getID(), acl[i].getCuid(), acl[i].getName());
          else if (acl[i].getType().endsWith(IdentityData.GROUP))
            id = new IdentityData(IdentityData.GROUP, acl[i].getEntityType(), acl[i].getID(), acl[i].getCuid(), acl[i].getName());
          else
            id = new IdentityData(IdentityData.ENTITY, GroupData.S_USER, acl[i].getCuid(), acl[i].getCuid(), acl[i].getName());
          //sec.addPermission(id, getActivity(acl[i]), quot(cal.getCalid()), GRANT);
          sec.addPermission(id, getActivity(acl[i]), cal.getCalid(), GRANT);
        }
    } catch (Exception e) {
        LogServiceFactory.instance().log(
              ILogService.WARN, "DPCS::addCalPermissionProps(): " + 
                  e.getMessage());
    }
    return cal;
  }

  String getActivity(ACEData ace) {
    String s = "";
    Boolean b = ace.getRead();
    if( b != null && b.booleanValue()) s += "R";
    b = ace.getWrite();
    if( b != null && b.booleanValue()) s += "W";
    b = ace.getFreebusy();
    if( b != null && b.booleanValue()) s += "F";
    b = ace.getSchedule();
    if( b != null && b.booleanValue()) s += "S";
    return s;
  }

  void buildLogonData(Security sec) throws Exception
  {
    // put to logon
    m_logon.putA("user", m_user);
    HashMap mapCalid = new HashMap();
    String[] calidrRFS = sec.getPermissionTargets(m_person, DPCS.RFS);
    String[] calidrRWFS = sec.getPermissionTargets(m_person, DPCS.RWFS);
    //log
    //Channel.log("method: buildLogonData call security 0.0.0");
    if (calidrRFS != null)
    {
      for (int i = 0; i < calidrRFS.length; i++)
      {
        XMLData right = new XMLData();
        right.putA("calid", calidrRFS[i]);
        right.putA("rights", DPCS.RFS);
        mapCalid.put(calidrRFS[i], right);
      }

    }
    //log
    //Channel.log("method: buildLogonData call security 0.1");
    if (calidrRWFS != null)
    {
      //log
      //Channel.log("method: buildLogonData call security 0.1.1");
      for (int i = 0; i < calidrRWFS.length; i++)
      {
        XMLData right = new XMLData();
        right.putA("calid", calidrRWFS[i]);
        right.putA("rights", DPCS.RWFS);
        mapCalid.put(calidrRWFS[i], right);
      }
    }
    // logon user
    XMLData right = new XMLData();
    right.putA("calid", m_user);
    right.putA("rights", DPCS.RWFS);
    mapCalid.put(m_user, right);
    //

    m_logon.putE("access", (XMLData[])mapCalid.values().toArray(new XMLData[]{}));

  }

  CalendarData[] buildACE(CalendarData[] calr) throws Exception
  {
    // build ACEData[]
    String[] calidr = new String[calr.length];
    //log
    //Channel.log("method: buildACE call security 2 calidr " + calidr.length);
    Security sec = new Security(CALENDAR);
    for (int i = 0; i < calr.length; i++)
    {
      calr[i].removeE("ace");
      HashMap mapACE = new HashMap();
      IdentityData[] idr1 = sec.getPermissionPrincipals(DPCS.RFS, quot(calr[i].getCalid()), false);
      for (int k = 0; idr1 != null && k < idr1.length; k++)
        if (!idr1[k].getType().equals(IdentityData.ENTITY))
            Finder.findDetail(idr1[k], null);
      IdentityData[] idr2 = sec.getPermissionPrincipals(DPCS.RWFS, quot(calr[i].getCalid()), false);
      for (int k = 0; idr2 != null && k < idr2.length; k++)
        if (!idr2[k].getType().equals(IdentityData.ENTITY))
            Finder.findDetail(idr2[k], null);

      if (idr1 != null && idr1.length > 0)
      {
        for (int j = 0; j < idr1.length; j++)
        {
          //log
          //Channel.log("method: buildACE step 2 " + idr1[j]);
          ACEData ace = new ACEData();
          if (idr1[j].getType().equals(IdentityData.ENTITY))
            ace.putCuid(idr1[j].getAlias());
          else
            ace.putCuid(idr1[j].getName());
          if (idr1[j].getName() != null)
            ace.putName(idr1[j].getName());
          else
            ace.putName(idr1[j].getAlias());
          ace.putType(idr1[j].getType());
          ace.putEntityType(idr1[j].getEntityType());
          ace.putOID(idr1[j].getID());
          ace.putFreebusy(TRUE);
          ace.putRead(TRUE);
          ace.putSchedule(TRUE);
          mapACE.put(idr1[j].toString(), ace);
        }
      }
      if (idr2 != null && idr2.length > 0)
      {
        for (int j = 0; j < idr2.length; j++)
        {
          //log
          //Channel.log("method: buildACE step 2 " + idr2[j]);
          ACEData ace = new ACEData();
          if (idr2[j].getType().equals(IdentityData.ENTITY))
            ace.putCuid(idr2[j].getAlias());
          else
            ace.putCuid(idr2[j].getName());
          if (idr2[j].getName() != null)
            ace.putName(idr2[j].getName());
          else
            ace.putName(idr2[j].getAlias());
          ace.putType(idr2[j].getType());
          ace.putEntityType(idr2[j].getEntityType());
          ace.putOID(idr2[j].getID());
          ace.putFreebusy(TRUE);
          ace.putRead(TRUE);
          ace.putSchedule(TRUE);
          ace.putWrite(TRUE);
          mapACE.put(idr2[j].toString(), ace);
        }
      }

      calr[i].putACE((ACEData[])mapACE.values().toArray(new ACEData[]{}));
    }
    //log
    //Channel.log("method: buildACE end call security 2 calr.length " + calr.length);

    return calr;
  }

  CalendarData[] cloneCalrProps(CalendarData[] calr) throws Exception
  {
    CalendarData[] ret = new CalendarData[calr.length];
    for (int i = 0; i < calr.length; i++)
    {
      ret[i] = new CalendarData();
      ret[i].updateProps(calr[i],true);
    }
    return ret;
  }

  //- Utils ------------------------------------------------------------------//

  String quot(String s)
  {
    // Double single quot character
    if (s == null) return s;
    int k = -1;
    int beg = -1;
    while ((k = s.indexOf("'", ++beg)) > -1)
    {
      s = s.substring(0, k) + "'" + s.substring(k);
      beg = ++k;
    }
    return s;
  }

  String atoString(GroupData[] gr)
  {
    if (gr == null || gr.length == 0) return "";
    String s = "(";
    for (int i = 0; i < gr.length; i++)
    {
      if (i < gr.length-1) s += "'" + quot(gr[i].toString()) + "',";
      else s+= "'" + quot(gr[i].toString()) + "')";
    }
    return s;
  }

}
