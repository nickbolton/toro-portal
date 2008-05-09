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

import net.unicon.academus.apps.calendar.*;
import net.unicon.academus.apps.notification.Notification;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.portal.channels.rad.*;

/**
 * This class represents the invitation screen
 */
public class Invitations extends CalendarScreen {
  XMLData m_data = new XMLData();

  XMLData m_invitations = new XMLData();

  Notification m_notification = null;
  String m_calid = null;
  String m_ceid = null;
  String m_back = "Peephole";
  boolean m_backRoot = false;

  /**
   * Default constructor
   */
  public Invitations() {
    m_data.putE("invitations", m_invitations);
  }

  /**
   * Get screen id
   * @return
   */
  public String sid() {
    return "Invitations";
  }

  /**
   * Get parameters of XSL.
   * @return
   */
  public Hashtable getXSLTParams() {
    Hashtable params = super.getXSLTParams();
    params.put("notification", m_notification != null?"true":"false");
    params.put("cur-date", curDate());
    if( m_back != null)
      params.put("back", m_back);
    if( m_backRoot)
      params.put("backRoot", "true");
    return params;
  }

  /**
   * Get xml-based data of the screen
   * @return
   * @throws Exception
   */
  public XMLData getData() throws Exception {
    return m_data;
  }

  /**
   * Initialize the screen
   * @param params
   * @throws Exception
   */
  public void init(Hashtable params) throws Exception {
    log("****Invitations.init"+ params);
    super.init(params);
    m_calid = (String)params.get("calid");
    m_ceid = (String)params.get("ceid");
    m_back = (String)params.get("back");
    m_backRoot = params.containsKey("root")?params.get("root").equals("true"):getScreen(m_back).lastRenderRoot();
    m_notification = (Notification)params.get("CNotification.notification");
    refresh();
  }

  /**
   * This method is called when the RAD receipts "make a new screen" request
   * in which the screen have been created before.
   * Instead of make a new instance of the screen, the RAD use existing screen and
   * call this method.
   *
   * @param params
   * @throws Exception
   */
  public void reinit(Hashtable params) throws Exception {
    refresh();
  }

  /**
   * Refresh the screen
   * @throws Exception
   */
  public void refresh() throws Exception {
    log("****Invitations.refresh");
    Date[] range = getRange(0, CCalendar.EVENT_AFTER);
    EntryData[] all = filterRpEn(getServer().fetchInvitations(range[0], range[1]));

    // From notification
    if (m_notification != null) {
      log("****from notification");
      int idx = findEntryEx( all, m_ceid);
      if (idx == -1)
        m_invitations.removeE("entry");
      else
        m_invitations.putE("entry", new EntryData[] {
                             all[idx]
                           }
                          );
    }

    // Normal
    else
      m_invitations.putE("entry", all);
  }

  /**
   * Process "Reply" action from notification channel
   * @param params
   * @return
   * @throws Exception
   */
  public Screen notificationReply(Hashtable params) throws Exception {
    // all invitations
    EntryData[] all = (EntryData[])params.get("Noti.all");
    m_calid = (String)params.get("calid");
    m_ceid = (String)params.get("ceid");
    m_notification = (Notification)params.get("CNotification.notification");

    all = filterRpEn(all);
    int idx = findEntryEx( all, m_ceid);
    if (idx == -1)
      m_invitations.removeE("entry");
    else
      m_invitations.putE("entry", new EntryData[] {
                           all[idx]
                         }
                        );
    return this;
  }

  /**
   * Process "Reply" action
   * @param params
   * @return
   * @throws Exception
   */
  public Screen reply(Hashtable params) throws Exception {
    log("** Invitations.reply:"+ params);
    String ceid = (String)params.get("ceid");
    String reply = (String)params.get("do"+CCalendar.CMD_SEPARATOR+"reply");
    String status = reply.equals("Accept")?"ACCEPTED":"DECLINED";

    EntryData[] invEntries = (EntryData[])m_invitations.getE("entry");

    // Find invitation entry
    int idx = EntryData.indexOf( invEntries, ceid);
    if (idx < 0)
      return error(CCalendar.ERROR_INVITATION_NOT_FOUND);

    // Call server's method
    getServer().replyInvitation( new EntryRange(ceid,EntryRange.ALL), status, null);

    //------ remove from array EntryData --------
    EntryData[] newInv = new EntryData[ invEntries.length - 1];
    for( int j = 0, i = 0; i < invEntries.length; i++)
      if( i != idx)
        newInv[j++] = invEntries[i];
    m_invitations.putE("entry",newInv);

    // Update Noti
    String calUser = getServer().getUser();
    EntryData entry = invEntries[idx];
    Notification.update(CCalendar.class.getName(),
                        CCalendar.buildNotificationParams(calUser, ceid),
                        "Invitation",entry.getTitle(),
                        CCalendar.buildNotificationParams(calUser, ceid));
    return this;
  }

  String getBaseCeid( String ceid) {
    String sign = ".";
    if (ceid.indexOf("_") != -1)
      sign ="_";
    int possign = ceid.indexOf(sign);
    if (possign != -1)
      return ceid.substring(0, possign);
    else
      return ceid;
  }

  int findEntryEx( EntryData[] inEntries, String ceid) {
    String base = getBaseCeid(ceid);
    if( inEntries != null)
      for ( int i = 0; i < inEntries.length ; i++) {
        EntryData ent = inEntries[i];
        if( getBaseCeid( ent.getCeid()).equals( base))
          return i;
      }
    return -1;
  }

  //Truyen 05-04-02
  // get entry that is repeat entry with entry have ceid
  EntryData[] filterRpEn(EntryData[] inEntries) {
    if (inEntries == null)
      return inEntries;

    Vector v = new Vector();
    for(int i = 0; i < inEntries.length; i++)
      v.addElement(inEntries[i]);

    for ( int i = 0; i < v.size() ; i++) {
      EntryData ent = (EntryData)v.elementAt(i);

      XMLData rdate = new XMLData();
      String date = XMLData.SDF.format(ent.getDuration().getStart());
      rdate.putA("rdate", date.substring(0,date.indexOf("_")));

      String ceid = ent.getCeid().split("[_.]")[0];
      for(int j = i+1; j < v.size(); j++) {
          EntryData repEnt = (EntryData)v.elementAt(j);
          String refix = repEnt.getCeid().split("[_.]")[0];

          if (ceid.equalsIgnoreCase(refix)) {
              String repDate = XMLData.SDF.format(repEnt.getDuration().getStart());
              rdate.putA("rdate", rdate.getA("rdate")+", " + repDate.substring(0, repDate.indexOf("_")) );
              v.removeElementAt(j);
              j--;
          }
      }

      ent.putE("repeat", rdate);
    }
    EntryData[]  entries = new EntryData[v.size()];
    for(int i = 0; i < v.size() ; i++)
      entries[i] = (EntryData)v.elementAt(i);
    return entries;
  }
  //----------------------------------------------------------------------//
  /**
   *
   * @param before
   * @param after
   * @return
   */
  public Date[] getRange(int before, int after) {
    Date[] range = new Date[2];
    Calendar cal = Timetable.getCalendar();
    Calendar pre = (Calendar)cal.clone();
    pre.add(Calendar.DATE, - before);
    range[0] = pre.getTime();
    Calendar next = (Calendar)cal.clone();
    next.add(Calendar.DATE, after);
    range[1] = next.getTime();
    return range;
  }

}
