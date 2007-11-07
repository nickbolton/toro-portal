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
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.portal.channels.rad.*;
import net.unicon.portal.common.service.notification.NotificationService;
import net.unicon.portal.common.service.notification.NotificationServiceFactory;

import org.jasig.portal.security.IPerson;

/**
 *
 */
public class DeleteAll extends CalendarScreen {
  EntryData m_ent = null;
  String m_calid = null;
  String m_back = null;
  String m_window = new String();
  XMLData m_data = null;
  XMLData m_view = null;
  XMLData[] m_delete = null;

  CalendarData[] m_save = null;

  public boolean canRenderAsPeephole() {
    return true;
  }

  public String sid() {
    return "DeleteAll";
  }

  public XMLData getData() throws Exception {
    return m_data;
  }

  public void init(Hashtable params) throws Exception {
    log("delete**" + params);
    m_data = new XMLData();
    ;
    m_view = new XMLData();
    m_data.putE("view",m_view);
    super.init(params);
    String ceid = (String)params.get("ceid");
    m_back = (String)params.get("back");
    m_calid = (String)params.get("calid");
    m_view.putA("back",m_back);
    m_window =(String)params.get("window");
    m_view.putA("window", m_window);

    m_delete = buildDelEntr(getServer().getCalendars(null), ceid);
    m_data.putE("delete",m_delete);
  }

  public void reinit(Hashtable params) throws Exception {
    init(params);
  }

  public Screen ok(Hashtable params) throws Exception {
    log("*del-all-ok**"+params);
    String back = (String)getParameter("back");
    int index = 0;
    int deleteCount = 0;

    if (m_delete == null)
        m_delete = new XMLData[0];

    int mod = 0;
    if (params.get("past") != null)
        mod += EntryRange.PAST;
    if (params.get("future") != null)
        mod += EntryRange.FUTURE;

    try {
        for (int i = 0; i < m_delete.length; i++) {
            index = i;

            String calid = (String)m_delete[i].getA("calid");
            if (params.get(calid) == null)
                continue;

            deleteCount++;
            if (m_delete[i].getA("event") != null) {
                // Delete event(s)
                String ceid = (String)m_delete[i].getA("ceid");

                EntryRange[] entr = null;
                String[] allparams = null;
                EntryData ent = null;
                CalendarData cal = CalendarData.findCalendar(m_save, calid);

                if (m_delete[i].getA("invitation") != null) {
                    entr = new EntryRange[] { new EntryRange(ceid, EntryRange.PAST+EntryRange.FUTURE) };

                    EntryData[] all = cal.getEntry();
                    if (all != null && all.length > 0) {
                        ent = m_ent = all[0];
                    }

                    allparams = new String[all.length];
                    for (int k=0;k<all.length;k++)
                        allparams[k] = CCalendar.buildNotificationParams(calid, all[k].getCeid());

                } else {
                    entr = new EntryRange[] { new EntryRange(ceid, mod) };

                    EntryData[] all = cal.getEntry();
                    ent = EntryData.findEntry(all, ceid);

                    Date start = ent.getDuration().getStart();
                    HashSet delParams = new HashSet();
                    delParams.add(CCalendar.buildNotificationParams( calid, ent.getCeid()));
                    for (int k=0; all != null && k < all.length; k++) {
                        switch( mod) {
                            case EntryRange.PAST:
                                if( all[k].getDuration().getStart().before(start))
                                    delParams.add(CCalendar.buildNotificationParams( calid, all[k].getCeid()));
                                break;
                            case EntryRange.FUTURE:
                                if( all[k].getDuration().getStart().after(start))
                                    delParams.add(CCalendar.buildNotificationParams( calid, all[k].getCeid()));
                                break;
                            case EntryRange.ALL:
                                delParams.add(CCalendar.buildNotificationParams( calid, all[k].getCeid()));
                                break;
                        }
                    }

                    allparams = (String[])delParams.toArray(new String[0]);
                }

                // Delete the selected events
                getServer().deleteEvents(calid, entr);

                // Update old notifications
                if (allparams != null && allparams.length > 0)
                    Notification.delete(CCalendar.class.getName(), allparams,
                            getLocalText(CCalendar.MSG_NOTIFICATION_TITLE_CANCELED, new Object[]{cal.getCalname()}, null),
                            getLocalText(CCalendar.MSG_NOTIFICATION_CANCELED, new Object[]{m_delete[i].getA("title")}, null));

                int info = 0;
                if (params.get("notification") != null)
                    info |= NotificationService.TYPE_NOTIFICATION;
                if (params.get("email") != null)
                    info |= NotificationService.TYPE_EMAIL;

                if (info != 0) {
                    // Prepare notification...
                    NotificationService notifcationService =
                            NotificationServiceFactory.getService();

                    String notifyClass = CCalendar.class.getName();
                    String notifyParams =
                            CCalendar.buildNotificationParams(m_calid, ceid);

                    SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
                    String date = sdf.format(m_ent.getDuration().getStart());

                    String time = null;
                    if (m_ent.getDuration().getLength().getAllDay())
                        time = "All Day";
                    else
                        time = getTime(m_ent.getDuration().getStart())+ " - "+ getTime(m_ent.getDuration().getEnd());

                    AttendeeData[] members = ent.getAttendee();

                    String subject = getLocalText(CCalendar.MSG_NOTIFICATION_PCAL_DELETE,
                                new Object[] {
                                    cal.getCalname()
                                }, null);

                    String body = getLocalText(CCalendar.MSG_EMAIL_PCAL_BODY_DELETE,
                                new Object[] {
                                    cal.getCalname(),
                                    ent.getTitle(), date, time,
                                    ent.getEvent().getDescription()
                                }, null);

                    String shortmsg = cal.getCalname();

                    // Get logon user
                    IPerson p = m_channel.m_csd.getPerson();
                    IdentityData logon =
                        new IdentityData(IdentityData.ENTITY, GroupData.S_USER,
                                ""+p.getID(),
                                (String)p.getAttribute("username"),
                                p.getFullName());
                    String email = (String)p.getAttribute("mail");
                    if( email != null) logon.putEmail(email);

                    // Send the notification
                    try {
                        notifcationService.sendNotifications(
                                members, logon, subject, body, shortmsg,
                                notifyClass, notifyParams, info);
                    } catch(Exception ex) {
                        log("***Failed to send notification/email: " + ex.toString());
                        ex.printStackTrace(System.err);
                    }
                }
            } else {
                // Delete TODOs
                String ceid = (String)m_delete[i].getA("ceid");
                EntryRange[] entr = { new EntryRange(ceid, mod) };
                getServer().deleteTodos(calid, entr);
            }
        }
    } catch (Exception e) {
      m_channel.log(e);
      return error(CCalendar.ERROR_FAILED_TO_DELETE,new Object[]
                   {new Integer( m_delete[index].getA("invitation") != null ? 0:
                                 m_delete[index].getA("event") != null ? 1:2), e.getMessage()},
                   back != null? back:"Main");
    }

    if (deleteCount == 0)
      return info(CCalendar.MSG_NO_DELETE_DELETED, back != null? back:"Main", true);

    return info(CCalendar.MSG_DELETE_DELETED, new Object[]
                { new Integer(m_delete[0].getA("invitation") != null ? 0:
                              m_delete[0].getA("event") != null ?1:2), m_delete[0].getA("title")}, back != null? back:"Main", true);
  }

  XMLData[] buildDelEntr(CalendarData[] cals, String ceid) throws Exception {
    if (cals == null)
      return null;
    Vector calClones = new Vector();
    Vector delv = new Vector();
    for (int i = 0; i < cals.length; i++) {
      // Fetch by id
      EntryData[] all = null;
      // Fetch by id
      if(m_window.equals("todo"))
        all = getServer().fetchTodosByIds(cals[i].getCalid(),
                                          new EntryRange[] {new EntryRange( ceid, EntryRange.ALL)});
      else
        all = getServer().fetchEventsByIds(cals[i].getCalid(),
                                           new EntryRange[] {
                                             new EntryRange( ceid, EntryRange.ALL)
                                           }
                                          );

      if( all != null && all.length > 0) {
        CalendarData cal = (CalendarData)cals[i];
        cal.putEntry(all);
        calClones.addElement(cal);

        // check some attributes
        EntryData ent =  EntryData.findEntry(all, ceid);
        if (ent != null) {
          XMLData del = new XMLData();
          del.putA("ceid", ceid);
          del.putA("title", ent.getTitle());
          if (ent.isRecurrences())
            del.putA("recur", "true");
          if ( ent.isAcceptedInvitation(cals[i], m_channel.logonUser().getID(), cals[i].getCalid() ) || ent.isAcceptedInvitation(cals[i], cals[i].getCalid()))
            del.putA("invitation", "true");
          if (ent.getEvent() != null)
            del.putA("event", "true");
          del.putA("calid", cals[i].getCalid());
          del.putA("calname", cals[i].getCalname());
          if (!getWriteAccess(cals[i]))
            del.putA("right", "read-only");
          delv.addElement(del);
        }
      }
    }

    // Save all calendars
    m_save = (CalendarData[])calClones.toArray(new CalendarData[0]);

    // Deleted entries
    XMLData[] delr = (XMLData[])delv.toArray(new XMLData[0]);
    log("delr.length "+ delr.length);
    return delr;
  }

  boolean getWriteAccess(CalendarData cal) throws Exception {
    if (cal.getOwner().equals(getServer().getUser()))
      return true;
    GroupData[] gr = GroupData.getAncestors(m_channel.logonUser(), false);
    String GRS = atoString(gr);
    boolean write = false;
    ACEData[] acer = cal.getACE();
    if (acer != null) {
      for (int i = 0; i < acer.length; i++) {
        if (acer[i] != null && (acer[i].getCuid().equals(getServer().getUser()) || (GRS.indexOf(acer[i].getIdentifier())!=-1)) &&  (acer[i].getWrite()!= null && acer[i].getWrite().booleanValue()))
          return true;
      }
    }

    return false;
  }

  String atoString(GroupData[] gr) {
    if (gr == null || gr.length == 0)
      return "";
    String s = "(";
    for (int i = 0; i < gr.length; i++) {
      if (i < gr.length-1)
        s += "'" + gr[i].getIdentifier() + "',";
      else
        s+= "'" + gr[i].toString() + "')";
    }
    return s;
  }

}
