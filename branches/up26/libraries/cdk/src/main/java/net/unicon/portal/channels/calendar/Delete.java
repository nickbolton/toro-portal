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
import net.unicon.academus.apps.notification.Notification;
import net.unicon.academus.apps.rad.*;
import net.unicon.portal.channels.rad.*;
import net.unicon.portal.common.service.notification.NotificationService;
import net.unicon.portal.common.service.notification.NotificationServiceFactory;

import org.jasig.portal.security.IPerson;


/**
 * This class represents a confirm, select screen before delete a event/todo/invitation
 */

public class Delete extends CalendarScreen
{
  EntryView m_entryView = null;
  EntryData m_ent = null;
  String m_calid = null;
  String m_back = null;
  boolean m_backRoot = false;
  XMLData m_data = null;
  XMLData m_view = null;

  EntryData[] m_all = null;

  public boolean canRenderAsPeephole()
  {
    return true;
  }

  /**
   * Get screen identifer of the screen
   * @return
   */
  public String sid()
  {
    return "Delete";
  }

  /**
   * Get xml-based data of screen
   * @return
   * @throws Exception
   */
  public XMLData getData() throws Exception
  {
    return m_data;
  }

  public Hashtable getXSLTParams()
  {
    Hashtable params = super.getXSLTParams();
    if( m_backRoot) params.put("backRoot", "true");
    return params;
  }

  /**
   * This method is called when a new instance of the screen was created. It
   * initialize the data for the screen.
   * @param params
   * @throws Exception
   */
  public void init(Hashtable params) throws Exception
  {
    m_data = new XMLData();
    m_view = new XMLData();
    m_entryView = new EntryView();

    m_data.putE("delete", m_entryView);
    m_data.putE("view",m_view);
    log("**Delete.init**"+params);
    super.init(params);
    String ceid = (String)params.get("ceid");
    m_back = (String)params.get("back");
    if( m_back != null)
      m_backRoot = params.containsKey("root")?params.get("root").equals("true"):getScreen(m_back).lastRenderRoot();

    m_calid = (String)params.get("calid");

    CalendarData cal = getServer().getCalendars(new String[]{m_calid})[0];
    m_data.putE("calendar",cal);
    m_view.putA("back",m_back);
    String window =(String)params.get("window");
    m_view.putA("window", window);

		// Fetch by id
    if(window.equals("todo"))

      m_all = getServer().fetchTodosByIds(m_calid,
                      new EntryRange[] {new EntryRange( ceid, EntryRange.ALL)});
    else

      m_all = getServer().fetchEventsByIds(m_calid,
                      new EntryRange[] {new EntryRange( ceid, EntryRange.ALL)});
		m_ent =  EntryData.findEntry(m_all, ceid);
    if (m_ent != null)
    {
      m_entryView.putCeid(ceid);

      if (m_ent.getTitle() != null)
        m_entryView.putA("title", m_ent.getTitle());
      else
       	m_entryView.putA("title", "Untitled") ;

      RecurrenceData[] recr = m_ent.getRecurrence();
      if ((recr != null && recr.length > 0) || m_ent.isRecurrences())
        m_entryView.putA("recur", "true");
      else
        m_entryView.removeA("recur");
      if ( m_ent.isAcceptedInvitation(cal, m_channel.logonUser().getID(), cal.getCalid()) || m_ent.isAcceptedInvitation(cal, cal.getCalid()))
        m_entryView.putA("invitation", "true");
    }
  }

  /**
   * This method is called by RAD.
   * @param params
   * @throws Exception
   */

  public void reinit(Hashtable params) throws Exception
  {
    init(params);
  }

  /**
   * Process OK action
   * @param params
   * @return
   * @throws Exception
   */
  public Screen ok(Hashtable params) throws Exception
  {
    //System.out.println(" delete params " + params);
    String back = (String)getParameter("back");
    String ceid = m_entryView.getCeid();
    CalendarData cal = getServer().getCalendars(new String[]{m_calid})[0];

    try
    {
      int mod = 0;
      if (params.get("past") != null)
        mod += EntryRange.PAST;
      if (params.get("future") != null)
        mod += EntryRange.FUTURE;
      EntryRange[] entr = {new EntryRange(ceid, mod)};

      if (m_ent.isEvent()) {
        getServer().deleteEvents(m_calid, entr);

        // Update Noti
        Date start = m_ent.getDuration().getStart();
        HashSet delParams = new HashSet();
        delParams.add(CCalendar.buildNotificationParams( m_calid, m_ent.getCeid()));
        for (int k=0;k<m_all.length;k++){
          switch( mod) {
                  case EntryRange.PAST:
                          if( m_all[k].getDuration().getStart().before(start))
                                  delParams.add(CCalendar.buildNotificationParams( m_calid, m_all[k].getCeid()));
                          break;
                  case EntryRange.FUTURE:
                          if( m_all[k].getDuration().getStart().after(start))
                                  delParams.add(CCalendar.buildNotificationParams( m_calid, m_all[k].getCeid()));
                          break;
                  case EntryRange.ALL:
                          delParams.add(CCalendar.buildNotificationParams( m_calid, m_all[k].getCeid()));
                          break;
          }
        }


        String[] allparams = (String[])delParams.toArray(new String[0]);
        CalendarData cal1 = (CalendarData)m_data.getE("calendar");

        // Old Notifications: Delete (well, update) the notification text.
        if ( m_channel.getClass().getName().endsWith("CCalendar") ) {
            Notification.delete(CCalendar.class.getName(), allparams,
                    getLocalText(CCalendar.MSG_NOTIFICATION_TITLE_CANCELED,
                        new Object[]{cal1.getCalname()}, null),
                    getLocalText(CCalendar.MSG_NOTIFICATION_CANCELED,
                        new Object[]{m_ent.getTitle()}, null));
        } else {
            // LCAL
            Notification.delete(CCalendar.class.getName(), allparams,
                    getLocalText(CCalendar.MSG_NOTIFICATION_LCAL_DELETE,
                        new Object[]{getTopicName(),getOfferingName()},null),
                    getLocalText(CCalendar.MSG_NOTIFICATION_CANCELED,
                        new Object[]{m_ent.getTitle()}, null));
        }

        int info = 0;
        if (params.get("notification") != null)
            info |= NotificationService.TYPE_NOTIFICATION;
        if (params.get("email") != null)
            info |= NotificationService.TYPE_EMAIL;

        if (info != 0) {

            NotificationService notifcationService =
                    NotificationServiceFactory.getService();

            String body = null;
            String subject = null;
            //String notifyClass = CCalendar.class.getName();
            //String notifyParams =
            //        CCalendar.buildNotificationParams(m_calid, m_ent.getCeid());
            String notifyClass = null;
            String notifyParams =
                    getLocalText(CCalendar.MSG_NOTIFICATION_CANCELED,
                            new Object[] { m_ent.getTitle() }, null);
            String shortmsg = m_ent.getTitle();

            SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
            String date = sdf.format(m_ent.getDuration().getStart());
            String time = null;
            if (m_ent.getDuration().getLength().getAllDay())
                time = "All Day";
            else
                time = getTime(m_ent.getDuration().getStart())+ " - "+ getTime(m_ent.getDuration().getEnd());

            AttendeeData[] members = null;

            // PCAL
            if ( m_channel.getClass().getName().endsWith("CCalendar") ) {

                members = m_ent.getAttendee();

                subject = getLocalText(CCalendar.MSG_NOTIFICATION_PCAL_DELETE,
                            new Object[] {
                                cal1.getCalname()
                            }, null);

                body = getLocalText(CCalendar.MSG_EMAIL_PCAL_BODY_DELETE,
                            new Object[] {
                                cal1.getCalname(), m_ent.getTitle(),
                                date, time, m_ent.getEvent().getDescription()
                            }, null);

            } else {

                members = (AttendeeData[])getMembers();

                subject = getLocalText(CCalendar.MSG_NOTIFICATION_LCAL_DELETE,
                            new Object[] {
                                getTopicName(), getOfferingName()
                            }, null);

                body = getLocalText(CCalendar.MSG_EMAIL_LCAL_BODY_DELETE,
                            new Object[] {
                                getTopicName(), getOfferingName(),
                                m_ent.getTitle(), date, time,
                                m_ent.getEvent().getDescription()
                            }, null);
            }

            // get logon user
            IPerson p = m_channel.m_csd.getPerson();
            IdentityData logon =
                    new IdentityData(IdentityData.ENTITY, GroupData.S_USER,
                                     ""+p.getID(),
                                     (String)p.getAttribute("username"),
                                     p.getFullName());
            String email = (String)p.getAttribute("mail");
            if( email != null) logon.putEmail(email);

            try {
                notifcationService.sendNotifications(
                        members, logon, subject, body, shortmsg, notifyClass,
                        notifyParams, info);
            } catch(Exception ex) {
                log("***Failed to send notification/email: " + ex.toString());
                ex.printStackTrace(System.err);
            }
        }
      } else if (m_ent.isTodo())
        getServer().deleteTodos(m_calid, entr);
    }
    catch (Exception e)
    {
      e.printStackTrace(System.err);
      m_channel.log(e);
      /*
      return error(CCalendar.ERROR_FAILED_TO_DELETE,new Object[]
                   {new Integer(m_ent.isAcceptedInvitation(cal,cal.getCalid())?0:
                                m_ent.isEvent()?1:2), getServer().getError()},
                   back != null? back:"Main");
      */
      return error(CCalendar.ERROR_FAILED_TO_DELETE,e.getMessage(),
                   back != null? back:"Main");
    }

    return info(CCalendar.MSG_DELETE_DELETED, new Object[]
                { new Integer(m_ent.isAcceptedInvitation(cal,cal.getCalid())?0:
                              m_ent.isEvent()?1:2), m_ent.getTitle()}, back != null? back:"Main", true);
  }

}
