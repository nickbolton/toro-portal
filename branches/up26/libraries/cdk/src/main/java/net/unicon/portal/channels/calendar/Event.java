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

import java.text.*;

import java.util.*;

import net.unicon.academus.apps.calendar.*;
import net.unicon.academus.apps.calendar.wcap.WCAP;
import net.unicon.academus.apps.notification.Notification;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.portal.channels.rad.*;
import net.unicon.portal.common.service.notification.NotificationService;
import net.unicon.portal.common.service.notification.NotificationServiceFactory;

import org.jasig.portal.security.IPerson;

/**
 * This class presents the even screen.
 */
public class Event extends CalendarScreen
{
  public static String SID = "Event";
  String S_CONTACT = "a";
  EntryData m_entry = null;
  EntryData m_oldEntry = null;
  EntryView m_entryView  = new EntryView();
  XMLData m_view = new XMLData();
  XMLData m_calview = new CalendarData();

  XMLData m_data = new XMLData();

  String m_date = null;
  String m_hour = null;
  String m_back = null;
  boolean m_backRoot = false;
  String m_calid = null;
  Vector m_newUsers = new Vector();
  Vector m_invitedUsers = null;

  // "CNotification.notification"
  Notification m_notification = null;

  // For display pages
  EntryData[] m_entries = null;//array entry repeat
  int m_nPages = -1; // number of pages
  int m_curPage = -1;
  final int MAX_NUM = 10;
  //boolean m_init = true;

  /**
   * Get screen identifer of the screen
   * @return
   */
  public String sid()
  {
    return SID;
  }


  /**
   * Get params of the screen
   * @return
   */
  public Hashtable getXSLTParams()
  {
    Hashtable params = super.getXSLTParams();
    if( m_back != null)
      params.put("back", m_back);
    if( m_backRoot)
      params.put("backRoot", "true");
    if( m_notification != null)
      params.put("notification", "true");
    return params;
  }

  /**
   * Get xml-based data of screen.
   * @return
   * @throws Exception
   */
  public XMLData getData() throws Exception
  {
   //XXX:DPCS
    m_data.putE("user", userList());
    return m_data;
  }

  /**
   * Process action "add new users to attendees"
   * @param v
   */
  public void updateSelectedUsers(Vector v)
  {
    m_newUsers = v;
  }

  //convert vector m_newUsers to array XMLData
  XMLData[] userList() throws Exception
  {
    //Truyen 04/01/2002
    XMLData[] xmlUsers = new XMLData[m_newUsers.size()];
    if(m_newUsers != null) for (int i=0; i < m_newUsers.size(); i++)
    {
      IdentityData idt = (IdentityData)m_newUsers.elementAt(i);
      xmlUsers[i] = new XMLData();
      xmlUsers[i].putA("selected", TRUE);
      xmlUsers[i].putA("cuid",idt.getAlias());
      xmlUsers[i].putA("itype", idt.getType());
      xmlUsers[i].putA("ientity", idt.getEntityType());
      xmlUsers[i].putA("iid", idt.getID());
      xmlUsers[i].put(idt.getName());
    }
    return xmlUsers;
  }

  //Truyen 03/21/2002
  /**
   * Re-initialize the screen. This method is used for refresh the screen.
   * @param params
   * @throws Exception
   */
  public void reinit(Hashtable params) throws Exception
  {
     log("Event.reinit:"+ params);
     m_newUsers.removeAllElements();
     XMLData send = (XMLData)m_view.getE("send");
     if(send != null){
       send.putA("notification","false") ;
       send.putA("email","false") ;
     }
     m_view.putE("send", send);
     init(params);
  }

  void initEvent( EntryData[] all, String ceid) throws Exception
  {
    // find this
    m_entry = new EntryData();
    EntryData ent = null;
    if (all != null && all.length != 0){
      for( int i = 0; i < all.length; i++) {
        if( all[i].getCeid().equals(ceid))
          ent = all[i];
      }
    }
    // for unicon only
    m_oldEntry = ent;
    //
    if( ent != null) {
      m_entry.put(ent.get());
      m_entry.putAlarm(ent.getAlarm());
      m_entry.putAttendee(ent.getAttendee());
      m_entry.putCeid(ent.getCeid());
      m_entry.putDuration(ent.getDuration());
      m_entry.putEvent(ent.getEvent());
      m_entry.putLocation(ent.getLocation());
      m_entry.putOrganizer(ent.getOrganizer());
      m_entry.putRecurrence(ent.getRecurrence());
      m_entry.putRelatedTos(ent.getRelatedTos());
      m_entry.putResources(ent.getResources());
      m_entry.putShare(ent.getShare());
      m_entry.putTodo(ent.getTodo());
      m_entry.putEvent(ent.getEvent());
      // Prepare display page
      if(m_entry.isRecurrences())
      {
        m_entries = all;
        m_nPages = m_entries.length / MAX_NUM;
        if (m_entries.length % MAX_NUM > 0) m_nPages++;
        // Truong 0123
        if (m_curPage > 0 && m_curPage < m_nPages) setPage(m_curPage);
        else if (m_curPage > 0 && m_curPage > m_nPages) setPage(m_nPages);
        else setPage(m_nPages > 0? 0: -1);
        //
      }

      m_entryView.putA("ceid", m_entry.getCeid());
      AlarmData[] alrmr = m_entry.getAlarm();
      if (alrmr != null && alrmr.length > 0)
        m_entry.putA("alarm-trigger", new Long((m_entry.getDuration().getStart().getTime()-alrmr[0].getTrigger().getTime())/1000));
      m_view.putE("entry",m_entry);
    }
    // Save invited users
    m_invitedUsers = new Vector();
    IdentityData[] atts = (IdentityData[])m_entry.getAttendee();
    if (atts != null)
    {
      //Finder.findDetails(atts, null); //!!! co the bo
      for (int i = 0; i < atts.length ; i++)
        m_invitedUsers.addElement(atts[i]);
    }
    m_newUsers.removeAllElements();
  }


  /**
   * Initialize the screen. This method is called by RAD
   * @param params
   * @throws Exception
   */
  public void init(Hashtable params) throws Exception
  {
    log("**Event.init:**"+ params);
    super.init(params);
    m_date = null;

    // Input params
    String ceid = (String)params.get("ceid");
    m_calid = params.containsKey("calid")?(String)params.get("calid"):m_calid;
    m_back = params.containsKey("back")?(String)params.get("back"): m_back;
    if (m_back == null)
      m_back = "Peephole";

    m_backRoot = params.containsKey("root")?params.get("root").equals("true"): m_back != null ? getScreen(m_back).lastRenderRoot() : false;

    // Check notification
    m_notification = (Notification)params.get("CNotification.notification");
    if (m_notification != null)
      m_back +="&amp;uP_root=root";

    // Calid and back
    m_calview.putA("calid",m_calid);
    m_view.putA("back",m_back);

    // get calendars
    CalendarData[] cals = getServer().getCalendars(null);
    m_data.putE("view",m_view);
    m_view.putE("calendar",m_calview);

    // Check edit or create
    if (ceid != null)
    {
      CalendarData one = (CalendarData)CalendarData.findCalendar(cals,m_calid);
      m_data.putE("calendar", new CalendarData[] {one});
      EntryData[] all = getServer().fetchEventsByIds(m_calid,
                        new EntryRange[] {new EntryRange( ceid, EntryRange.ALL)});
      one.putEntry(all);
      initEvent(all, ceid);
    }
    // New event
    else {
      m_data.putE("calendar", cals);

      m_entry = new EntryData();

      m_entryView.putE("entry",m_entry);
      m_view.putE("entry",m_entry);

      String date = (String)getParameter("date");
      log("Date " + date);
      String hour = (String)getParameter("hour");
      if (date == null) date = m_date; else m_date = date;
      if (hour == null) date = m_date; else m_date = date;

      // Prepare entry (in view tag)
      if (date != null)
        m_entryView.putDate(date);
      if (hour != null)
        m_entryView.putHour(hour);

      DurationData dur = new DurationData();
      Calendar start = Timetable.getCalendar();
      TimeLength length = new TimeLength();
      log("Date " + date);
      if( date != null)
        start.setTime(XMLData.SDF.parse(date));
      if (hour != null)
        start.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
      length.setLength(3600000);
      dur.putStart(start.getTime());
      dur.putLength(length);
      if (params.get("isAllDay") != null)
        dur.putAllDay();

      m_entry.putDuration(dur);
      m_invitedUsers = new Vector();
    }
  }

  /**
   * Process "Reply" action.
   * @param params
   * @return
   * @throws Exception
   */
  public Screen notificationReply(Hashtable params) throws Exception
  {
    log("notificationReply:"+params);
    m_date = null;

    // Input params
    String ceid = (String)params.get("ceid");
    m_calid = (String)params.get("calid");
    String notiCalid = (String)params.get("notiCalid");
    m_notification = (Notification)params.get("CNotification.notification"); //must not null
    EntryData[] all = (EntryData[])params.get("Noti.all");
    CalendarData cal = (CalendarData)params.get("Noti.cal");
    m_back = "Peephole";

    // Calid and back
    m_calview.putA("calid",m_calid);
    m_view.putA("back",m_back);
    m_data.putE("view",m_view);
    m_view.putE("calendar",m_calview);
    m_data.putE("calendar", new CalendarData[] {cal});
    cal.putEntry(all);
    initEvent(all, ceid);

    return this;
  }

  /**
   * Process "delete" action.
   * @param params
   * @return
   * @throws Exception
   */
  public Screen delete(Hashtable params) throws Exception
  {
    String cuid = (String)params.get("user");
    for(int i = 0; i < m_newUsers.size(); i ++)
      if (cuid.equalsIgnoreCase(((IdentityData)m_newUsers.elementAt(i)).getAlias()))
       m_newUsers.removeElementAt(i);
    return this;
  }

  //return old attendee + new user attendee
  AttendeeData[] attendees(AttendeeData[] invited, Vector newUsers) throws Exception
  {
    if (newUsers == null || newUsers.size() == 0) return invited;

    // Add invited users (AttendeeData)
    Vector v = new Vector();
    if (invited != null) for (int i = 0; i < invited.length; i++)
      v.addElement(invited[i]);//add old attendee data to v of DPCS

    // new attendees
    for (int i = 0; i < newUsers.size(); i++)
    {
      IdentityData idt = (IdentityData)newUsers.elementAt(i);
      AttendeeData att = (AttendeeData)idt;
      if( att != null) {
        att.putStatus("NEEDS-ACTION");
        att.status = "Added";
        v.addElement(att);
      }
    }

    // Convert to array
    AttendeeData[] atts = new AttendeeData[ v.size()];
    for (int i = 0; i < v.size(); i++)
      atts[i] = (AttendeeData)v.elementAt(i);

    return atts;
  }

  AttendeeData getAttendeeData( IdentityData idt)
  {
    //truong 07012002

    // iPlanet CS
    if ( CCalendar.SERVER_NAME.equals(CCalendar.IPLANET_SERVER)) {
      // Here can not portal group
      if( GroupData.isPortalUser(idt) || idt.getEntityType().equals(S_CONTACT)) {
        IdentityData campus = idt.getRef("campus");
        if (campus != null)
          idt = campus;
        else if( campus == null && idt.getEmail() == null)
          return null;
      }
    }
    else if (CCalendar.SERVER_NAME.equals(CCalendar.DPCS_SERVER))
    {
      if(idt.getEntityType().equals(S_CONTACT)) {
        IdentityData ref = idt.getRef("campus") != null ? idt.getRef("campus") : idt.getRef("portal");
        if (ref != null)
          idt = ref;
        else if( ref == null && idt.getEmail() == null)
          return null;
      }
    }


    return new AttendeeData( idt);
  }

  public Screen ok(Hashtable params) throws Exception
  {
    log("** Event.0k: "+ params);
    String err = cacheData(params);
    if (err != null) return error(err);
    // Check valid input
    String title = ((String)params.get("event")).trim();
    if (title.length() == 0 )
      return error(CCalendar.ERROR_NO_EVENT_TITLE);

    boolean allDay = ((String)params.get("all-day")).equals("1");
    if (allDay == false && ((String)params.get("hours")).equals("0") && ((String)params.get("minutes")).equals("0") )
      return error(CCalendar.ERROR_INPUT_LENGTH);

    if (m_entry.getRecurrence()!=null && m_entry.getRecurrence().length > 0 && m_entry.getRecurrence()[0].getUntil() != null &&
      m_entry.getDuration().getStart().after(
        Timetable.lastDayTime(m_entry.getRecurrence()[0].getUntil()).getTime()))
      return error(CCalendar.ERROR_INPUT_UNTILDATE);

    String ceid = m_entry.getCeid();

    // Emails Attendees 120302
    String emails = (String)params.get("emails");
    if (emails != null && emails.length()>= 100)
      return error("Your email entered too long. It must be less than 100 characters");
    Vector attEmails = new Vector();
    if (emails != null){
      try {
        StringTokenizer tokens = new StringTokenizer(emails, ",;");
        while (tokens.hasMoreTokens()){
          attEmails.addElement(new AttendeeData(GroupData.createEmailEntity( tokens.nextToken())));
        }
      } catch (Exception e) {
          return error("Emails must be delimit by , or ;");
      }
    }


    // Attendees
    // PCAL
    AttendeeData[] atts = null;
    atts = attendees(m_entry.getAttendee(),m_newUsers);
    if (attEmails.size() > 0 ) // Emails Attendees 120302
      atts = attendees(atts, attEmails);

    if (atts != null)
      m_entry.putAttendee(atts);
    else
      m_entry.putAttendee(null);

    EntryData entData = null;

    try {
      if (ceid == null)
        entData = getServer().createEntry(m_calid, m_entry);
      else {
        int mod = 0;
        mod = params.get("future") == null? 0:EntryRange.FUTURE;
        entData = getServer().updateEntry(m_calid, new EntryRange(ceid, mod), m_entry);
      }
    } catch (Exception e) {
      m_channel.log(e);

      if (ceid == null)
        return error(CCalendar.ERROR_FAILED_TO_CREATE, new Object[]
                     {getServer().getError()});
      else
        return error(CCalendar.ERROR_FAILED_TO_UPDATE, new Object[]
                     {getServer().getError()});
    }

    int info = 0;
    if (params.get("notification") != null)
        info |= NotificationService.TYPE_NOTIFICATION;
    if (params.get("email") != null)
        info |= NotificationService.TYPE_EMAIL;

    if ( info != 0 ) {

        NotificationService notifcationService = NotificationServiceFactory.getService();

        String body = null;
        String subject = null;
        String notifyClass = CCalendar.class.getName();
        String notifyParams = CCalendar.buildNotificationParams( m_calid, entData.getCeid());
        boolean useCallback = true;
        String shortmsg = entData.getTitle();

        // Establish the start and end time of the event
        String time = null;
        if (allDay)
            time = "All Day";
        else
            time = getTime(m_entry.getDuration().getStart())+ " - "+ getTime(m_entry.getDuration().getEnd());

        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
        String date = sdf.format(entData.getDuration().getStart());

        String oldDate = null;
        String oldTime = null;

        // Old event, get the original entries start and end times
        if (ceid != null) {
            oldDate = sdf.format(m_oldEntry.getDuration().getStart());

            // Establish the start and end time of the original event
            if (allDay)
                oldTime = "all day";
            else
                oldTime = getTime(m_oldEntry.getDuration().getStart())+ " - "+ getTime(m_oldEntry.getDuration().getEnd());
        }

        if ( m_channel.getClass().getName().endsWith("CCalendar") ) {
          // Send noti in focused calendar
          log(" %%%%%%%%%%% Focus Channel %%%%%%%%% ");

          // Locate the targetted calendar
          CalendarData[] calr = (CalendarData[])m_data.getE("calendar");
          CalendarData cal = CalendarData.findCalendar(calr, m_calid);

          // If the event already existed, this is an update message..
          if (ceid != null) {

              subject = getLocalText(CCalendar.MSG_NOTIFICATION_PCAL_UPDATE,
                        new Object[] {
                                (cal != null ? cal.getCalname() : "")
                        }, null);

              body = getLocalText(CCalendar.MSG_EMAIL_PCAL_BODY_UPDATE,
                        new Object[] {
                                entData.getTitle(),
                                ((IdentityData)m_channel.logonUser()).getName(),
                                date, time, entData.getLocation(),
                                entData.getEvent().getDescription(),
                                m_oldEntry.getTitle(), oldDate, oldTime,
                                m_oldEntry.getLocation(),
                                new Integer(entData.isRecurrent() ? 1 : 0)
                        }, null);

          } else {
              // New event notification
              subject = getLocalText(CCalendar.MSG_NOTIFICATION_PCAL_ADD,
                            new Object[] {
                                (cal != null ? cal.getCalname() : "")
                            }, null);

              body = getLocalText(CCalendar.MSG_EMAIL_PCAL_BODY_ADD,
                            new Object[] {
                                entData.getTitle(),
                                ((IdentityData)m_channel.logonUser()).getName(),
                                date, time, entData.getLocation(),
                                entData.getEvent().getDescription(),
                                new Integer(entData.isRecurrent() ? 1 : 0)
                            }, null);

            }

        } else {
            // send Noti in LMS
            log(" %%%%%%%%%%% LMS Channel %%%%%%%%% ");

            // Obtains the members of the Groupware Offering which are the 
            // attendees. None are specified explicitly during the Groupware 
            // Calendar event creation. 
            atts = (AttendeeData[])getMembers();
            log("########### invite member ####### ");

            // If the event already existed, this is an update message..
            if (ceid != null) {

                subject = getLocalText(CCalendar.MSG_NOTIFICATION_LCAL_UPDATE,
                        new Object[] {
                            getTopicName(), getOfferingName()
                        }, null);

                body = getLocalText(CCalendar.MSG_EMAIL_LCAL_BODY_UPDATE,
                        new Object[] {
                            getTopicName(), getOfferingName(),
                            entData.getTitle(),date, time,
                            entData.getLocation(),
                            entData.getEvent().getDescription(),
                            m_oldEntry.getTitle(), oldDate, oldTime,
                            m_oldEntry.getLocation(),
                            new Integer(entData.isRecurrent() ? 1 : 0)
                        }, null);

            } else {
                // New Event notification

                subject = getLocalText(CCalendar.MSG_NOTIFICATION_LCAL_ADD,
                                new Object[] {
                                    getTopicName(), getOfferingName()
                                }, null);

                body = getLocalText(CCalendar.MSG_EMAIL_LCAL_BODY_ADD,
                        new Object[] {
                            getTopicName(), getOfferingName(),
                            entData.getTitle(), date, time,
                            entData.getLocation(),
                            entData.getEvent().getDescription(),
                            new Integer(entData.isRecurrent() ? 1 : 0)
                        }, null);
            }
            useCallback = false;

        }

        // Send off the notification (or email)
        if ( atts != null && atts.length > 0) {
            try {
                notifcationService.sendNotifications(
                                    atts, m_channel.logonUser(), subject, body,
                                    shortmsg, (useCallback ? notifyClass : null),
                                    (useCallback ? notifyParams : null), info);
            } catch(Exception ex) {
                log("***Failed to send notification/email: " + ex.toString());
                ex.printStackTrace(System.err);
            }

        }
    }

    // Give information
    if (ceid == null)
        return info(CCalendar.MSG_EVENT_ADDED, new Object[]
                {m_entry.getTitle()},m_back, true);
    else if (m_notification != null)
        return this;
    else
        return info(CCalendar.MSG_EVENT_UPDATED, new Object[]
                {m_entry.getTitle()},m_back, true);
  }

  public Screen addAttendee(Hashtable params) throws Exception
  {
    log("**addattendee**"+ params);
    //Truyen 03/29/2002
    String err = cacheData(params);
    params.put("back", sid());
    return select( CCalendar.USER_SOURCE, null, "updateUsers", params);
  }

  //--------------------------------------------------------------------------//

  //Truong 011702
  public Screen updateUsers(Hashtable params) throws Exception
  {
    //Truyen 03/29/2002
    log("**Event.updateSelectedUsers"+params);
    IdentityData[] new_users = (IdentityData[]) params.get("selected");
    if( new_users == null) return this;

    // iPlanet CS
    if ( CCalendar.SERVER_NAME.equals(CCalendar.IPLANET_SERVER)) {
      new_users = GroupData.expandGroups(new_users,true);
      Finder.findRefferences(m_channel.logonUser(),new_users);
    }
    log("**Event.updateSelectedUsers 1");
    // save distinct
    HashSet errors = new HashSet();
    for ( int i = 0; i < new_users.length; i++)
    {
      String err = accept(new_users[i]);
      log("*error*" +err);
      if( err == null) {
        log("**update selected user** " + new_users[i].toString());
        if (!attendeeContains( m_newUsers, new_users[i]) && !attendeeContains( m_invitedUsers, new_users[i]))
          m_newUsers.addElement(getAttendeeData(new_users[i]));
      }
      else{
        log("**Event.updateSelectedUsers 2");
        errors.add(err);
      }
    }

    // Info message
    if( errors.size() > 0)
      return warningMulti((String[])errors.toArray(new String[0]), sid(), false);
    else
      return this;
  }

  boolean attendeeContains( Vector v, IdentityData user)
  {
    for( int i = 0; i < v.size(); i++) {
      if( equalsRef( (IdentityData)v.elementAt(i), user))
        return true;
    }

    return false;
  }

  boolean equalsRef(IdentityData user1, IdentityData user2)
  {
    if( user1.equals( user2))
      return true;

    // portal
    IdentityData ref1 = GroupData.isPortalUser(user1)? user1: user1.getRef("portal");
    IdentityData ref2 = GroupData.isPortalUser(user2)? user2: user2.getRef("portal");
    if( ref1 != null && ref2 != null && ref1.equals( ref2))
      return true;

    // campus
    ref1 = user1.getEntityType().equals("p")? user1: user1.getRef("campus");
    ref2 = user2.getEntityType().equals("p")? user2: user2.getRef("campus");
    if( ref1 != null && ref2 != null && ref1.equals( ref2))
      return true;

    // contact
    ref1 = user1.getEntityType().equals("a")? user1: user1.getRef("contact");
    ref2 = user2.getEntityType().equals("a")? user2: user2.getRef("contact");
    if( ref1 != null && ref2 != null && ref1.equals( ref2))
      return true;

    return false;
  }

  public Screen viewRepeat(Hashtable params) throws Exception
  {
    // Input params
    String ceid = (String)params.get("ceid");
    // get calendar
    CalendarData[] cals = (CalendarData[])m_data.getE("calendar");
    EntryData[] all = cals[0].getEntry();
    initEvent(all, ceid);
    return this;
  }

  public Screen previous(Hashtable params) throws Exception
  {
    String p = (String)params.get("p");
    if( p != null) try {
      setPage( Integer.parseInt(p) - 1);
    } catch( Exception e) {
      log(e);
    }
    return this;
  }

  public Screen next(Hashtable params) throws Exception
  {
    String p = (String)params.get("p");
    if( p != null) try {
      setPage( Integer.parseInt(p) + 1);
    } catch( Exception e) {
      log(e);
    }
    return this;
  }

  public void setPage(int p) throws Exception
  {
    // Save current page number
    m_curPage = p;
    m_view.putA("prev",new Boolean(m_curPage > 0));
    m_view.putA("next",new Boolean(m_curPage < m_nPages - 1));
    m_view.putA("pages",new Integer(m_nPages));
    m_view.putA("page",new Integer(m_curPage));
    if (m_curPage < 0 || m_curPage >= m_nPages || m_nPages < 0) return;
    // Start index of event and len of page display
    int start = p * MAX_NUM;
    int len = (start + MAX_NUM > m_entries.length)? m_entries.length - start: MAX_NUM;

    // Extract entries to calendar page
    XMLData[] viewData = new XMLData[len];
    for (int i = 0; i < len; i++)
    {
      EntryData viewEn = m_entries[start+i];
      viewData[i] = new XMLData();
      // put ceid to "view" once if there are many
      viewData[i].putA("ceid", viewEn.getCeid());
    }
    m_calview.putE("entry",viewData);

  }

  /**
   * catch data on form
   */
  String cacheData(Hashtable params) throws Exception
  {
    String err = null;
    DurationData dur = new DurationData();
    Calendar start = Timetable.getCalendar();
    TimeLength length = new TimeLength();
    m_calid = params.get("calid") != null?(String)params.get("calid"): m_calid;
    m_calview.putA("calid",m_calid);
    m_view.putE("calendar",m_calview);
    boolean allDay = ((String)params.get("all-day")).equals("1");
    if(allDay)
    {
      //start.setTimeZone(TimeZone.getTimeZone("GMT"));
      //start.add(Calendar.HOUR, +12);
      length.setAllDay();
    }

    start.set(Calendar.MONTH, Integer.parseInt((String)params.get("month"))-1);
    start.set(Calendar.DATE, Integer.parseInt((String)params.get("day")));
    start.set(Calendar.YEAR, Integer.parseInt((String)params.get("year")));

    if( allDay == false)
    {
      start.set(Calendar.HOUR_OF_DAY, Integer.parseInt((String)params.get("hour")));
      start.set(Calendar.MINUTE, Integer.parseInt((String)params.get("minute")));
      length.setLength((Integer.parseInt((String)params.get("hours"))*3600+Integer.parseInt((String)params.get("minutes"))*60)*1000);
    }

    dur.putStart(start.getTime());
    dur.putLength(length);
    m_entry.putDuration(dur);
    String loc = enc(((String)params.get("place")).trim());

    if (loc != null)
      m_entry.putLocation(loc);

    m_entry.putShare((String)params.get("share"));

    EventData ev = new EventData();
    String title = ((String)params.get("event")).trim();
    if (title.length() != 0)
      ev.put(enc(title));
    String[] cats = {(String)params.get("category")};
    if (!cats[0].equals("None"))
      ev.putCategory(cats);
    ev.putPriority(new Integer((String)params.get("priority")));
    String desc = enc(((String)params.get("description")).trim());

    if (desc != null)
      ev.putDescription(desc);
    m_entry.putEvent(ev);
    //Truyen 04/05/2002
    //cache notification, email, day of week
    if(params.get("freq")!= null)
    {
      XMLData week = new XMLData();
      week.putA("mon",params.containsKey("mon")?"yes":"no");
      week.putA("tue",params.containsKey("tue")?"yes":"no");
      week.putA("wed",params.containsKey("wed")?"yes":"no");
      week.putA("thu",params.containsKey("thu")?"yes":"no");
      week.putA("fri",params.containsKey("fri")?"yes":"no");
      week.putA("sat",params.containsKey("sat")?"yes":"no");
      week.putA("sun",params.containsKey("sun")?"yes":"no");
      m_view.putE("week", week);
    }
     XMLData send = new XMLData();
     send.putA("notification",params.containsKey("notification")?"true":"false") ;
     send.putA("email",params.containsKey("email")?"true":"false") ;
     m_view.putE("send", send);
    //repeating
    try{
    if (params.get("freq")!=null && !((String)params.get("freq")).equals("0"))
    {
      EntryData oldEnt = null;
      if (m_entry.getCeid() != null)
        oldEnt = getServer().fetchEventsByIds(m_calid,new EntryRange[]{new EntryRange(m_entry.getCeid())})[0];
//        oldEnt = getServer().getEntry(m_calid, m_entry.getCeid());

      if (m_entry.getCeid() != null && oldEnt.getRecurrence() != null &&
          oldEnt.getRecurrence().length > 0)
        m_entry.putRecurrence(oldEnt.getRecurrence());
      else
      {
        int intr = 1;
        int times = 1;
        String byDate = "";
        String freq = null;
        switch(Integer.parseInt((String)params.get("freq")))
        {
          case 1: // repeat every .. days
            freq = "DAILY";
            intr = Integer.parseInt((String)params.get("days"));
            break;
          case 2: // repeat every .. weeks
            freq = "WEEKLY";
            intr = Integer.parseInt((String)params.get("weeks"));
            if (CCalendar.SERVER_NAME.equals(CCalendar.DPCS_SERVER))
            {
              if(params.containsKey("sun"))
                byDate += "1";
              if(params.containsKey("mon"))
                byDate += "2";
              if(params.containsKey("tue"))
                byDate += "3";
              if(params.containsKey("wed"))
                byDate += "4";
              if(params.containsKey("thu"))
                byDate += "5";
              if(params.containsKey("fri"))
                byDate += "6";
              if(params.containsKey("sat"))
                byDate += "7";
            }
            else
            {
              if(params.containsKey("mon"))
                byDate += "MO,";
              if(params.containsKey("tue"))
                byDate += "TU,";
              if(params.containsKey("wed"))
                byDate += "WE,";
              if(params.containsKey("thu"))
                byDate += "TH,";
              if(params.containsKey("fri"))
                byDate += "FR,";
              if(params.containsKey("sat"))
                byDate += "SA,";
              if(params.containsKey("sun"))
                byDate += "SU,";
              if (byDate.endsWith(","))
                byDate = byDate.substring(0, byDate.length()-1) ;
            }
            break;
          case 3: // repeat every .. months
            freq = "MONTHLY";
            intr = Integer.parseInt((String)params.get("months"));
            break;
          case 4: // repeat every year
            freq = "YEARLY";
            intr = 1;
        }
        if (intr <= 0)
          err = new String(CCalendar.ERROR_INPUT_REPEAT);

        RecurrenceData rec = new RecurrenceData();
        if (params.get("until")!=null)
        {
          if(((String)params.get("until")).equals("0"))
          {
            int tms = Integer.parseInt((String)params.get("times"));
            if (tms <= 0)
              err = new String(CCalendar.ERROR_INPUT_REPEAT);

            rec.putRule(freq, new Integer(intr), new Integer(tms));
          }
          else
          {
            Calendar until = (Calendar)start.clone();
            until.set(Calendar.MONTH, Integer.parseInt((String)params.get("month2"))-1);
            until.set(Calendar.DATE, Integer.parseInt((String)params.get("day2")));
            until.set(Calendar.YEAR, Integer.parseInt((String)params.get("year2")));
            rec.putRule(freq, new Integer(intr), until.getTime());
          }
          if (freq.equals("WEEKLY") && byDate != null && byDate.length() > 0)
            rec.putByDay(byDate);
        }

        RecurrenceData[] recr = {rec};
        m_entry.putRecurrence(recr);
      }
    }
    else
      m_entry.putRecurrence(null);
    }catch(Exception ex){
      err = new String(CCalendar.ERROR_INPUT_REPEAT);
    }

    // send email and notification
     return err;
  }
  //--------------------------------------------------------------------------//

  String accept( IdentityData id)
  {
    if (CCalendar.SERVER_NAME.equals(CCalendar.IPLANET_SERVER))
    {
      if( GroupData.isPortalUser(id) || id.getEntityType().equals(S_CONTACT))
        //return (( id.getRef("campus") != null)? null:CCalendar.MSG_INVALID_USERS_ATTENDEE);
      {
        //truong 070102
        if (id.getRef("campus") != null)
          return null;
        else if (id.getEmail() != null)
        {
          id.putAlias(id.getEmail());
          return null;
        }
        else
          return CCalendar.MSG_INVALID_USERS_ATTENDEE;
      }
    }
    else if (CCalendar.SERVER_NAME.equals(CCalendar.DPCS_SERVER))
    {
      if(id.getEntityType().equals(S_CONTACT))
        //return id.getRef("campus") != null ? null : id.getRef("portal")!= null ? null : CCalendar.MSG_INVALID_USERS_ATTENDEE;
      {
        //truong 070102
        if (id.getRef("campus") != null || id.getRef("portal")!= null)
          return null;
        else if (id.getEmail() != null)
        {
          id.putAlias(id.getEmail());
          return null;
        }
        else
          return CCalendar.MSG_INVALID_USERS_ATTENDEE;
      }
    }

    return null;
  }

  String getTime(Date start)
  {
    if (start == null) return "";
    Calendar date = Timetable.getCalendar();
    date.setTime(start);
    int hour = date.get(date.HOUR_OF_DAY);
    //log("$$$$$ " + " start " + start + " date " + date + " hour " + hour);
    int minute = date.get(date.MINUTE);
    String minutes = String.valueOf(minute).length() == 1 ? "0"+String.valueOf(minute) : String.valueOf(minute);
    String time = new String();

    if (hour == 0 )
      time = "12:" + minutes +" am";
    else if ( 0 < hour && hour < 12)
      time = String.valueOf(hour) + ":" + minutes + " am";
    else if ( hour == 12)
      time = "12:" + minutes + " pm";
    else if ( hour > 12 )
      time = String.valueOf(hour - 12) + ":" + minutes + " pm";

    return time;
  }

  static String enc(String s)
  {
    //s = XMLData.replace(s, "\"", "&quot;");
    s = XMLData.replace(s, "&#34;", "\"");
    return s;
  }

}
