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
package net.unicon.portal.channels.notification;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;

import net.unicon.academus.apps.notification.*;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XML;
import net.unicon.portal.channels.rad.*;
import net.unicon.portal.channels.notification.*;

public class Peephole extends Screen {
  static public final String SID = "Peephole";

  Vector m_notifications = new Vector();


  public String sid() {
    return SID;
  }

  public boolean isCacheValid( long oldMilisTime) {
    return false;
  }

  public void init(Hashtable params) throws Exception {
    getNotificationList ();
  }

  // Unicon
  public void getNotificationList () throws Exception {
    m_notifications = Notification.listNotifications(m_channel.logonUser());
  }
  // Unicon

  public String buildXML() throws Exception {
    StringBuffer xml = new StringBuffer();
    xml.append("<?xml version='1.0'?>");
    xml.append("<notification-system>");
    printXML(xml);
    xml.append("</notification-system>");
    return xml.toString();
  }

  public void printXML(StringBuffer xml) throws Exception {

    SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy HH:mm");
    for (int i = 0; i < m_notifications.size(); i ++) {

        Notification noti = (Notification)m_notifications.elementAt(i);
        xml.append("<notification id='").append(noti.m_notificationId);
        if (noti.m_date != null) {
            xml.append("' date-milis='").append(noti.m_date.getTime());
            xml.append("' date='").append(sdf.format(noti.m_date));
        }
        if (noti.m_type != null) {
            xml.append("' type='").append(XML.esc(noti.m_type));
        }
        if (noti.m_sender != null) {
            xml.append("' sender='").append(XML.esc(noti.m_sender.getName()));
        }
        if (noti.m_recipient != null) {
            xml.append("' recipient='").append(XML.esc(noti.m_recipient));
        }
        if (noti.m_notified != null) {
            xml.append("' notified='").append(XML.esc(noti.m_notified));
        }
        if (noti.m_status != null) {
            xml.append("' status='").append(XML.esc(noti.m_status));
        }
        if (noti.m_params != null) {
            xml.append("' params='").append(XML.esc(noti.m_params));
        }
        xml.append("'>")
           .append(XML.esc(noti.m_text))
           .append("</notification>");
/* OLD (INEXCUSABLE) IMPL -- Left for reference...
        xml.append("<notification id='"+noti.m_notificationId
                 + (noti.m_date!= null?"' date-milis='"+noti.m_date.getTime():null)
                 + (noti.m_date!= null?"' date='"+sdf.format(noti.m_date):null)
                 + (noti.m_type!= null?"' type='"+XML.esc(noti.m_type):"")
                 + (noti.m_sender!= null?"' sender='"+XML.esc(noti.m_sender.getName()):"")
                 + (noti.m_recipient!= null?"' recipient='"+XML.esc(noti.m_recipient):"")
                 + (noti.m_notified!= null?"' notified='"+XML.esc(noti.m_notified):"")
                 + (noti.m_status!= null?"' status='"+XML.esc(noti.m_status):"")
                 + (noti.m_params!= null?"' params='"+XML.esc(noti.m_params):"")+"'>"
                 + XML.esc(noti.m_text)
                 + "</notification>");
*/
      }
  }

  public Screen errorOpen(Hashtable params) throws Exception {
    String icon = (String)params.get("icon");
    String msg = (String)params.get("message");
    if( msg == null)
      msg = CNotification.ERROR_CANNOT_OPEN;
    Channel.log("Info:icon=" + icon + " msg=" + msg);
    return error(icon==null?Info.ERROR:icon, msg,null);
  }

  public Screen confirmDelete(Hashtable params) throws Exception {
    params.put("methodName","delete");
    return confirm(CNotification.CONFIRM_DELETE,null,params,sid());
  }

  public Screen delete(Hashtable params) throws Exception {
    int notiId = Integer.parseInt(((String)params.get("notiId")).trim());
    String recipient = (String)params.get("recipient");
    Notification.delete(notiId, recipient, m_channel.logonUser());
    updateNotificationList();
    return this;
  }


  public Screen save(Hashtable params) throws Exception {
    int notiId = Integer.parseInt(((String)params.get("notiId")).trim());
    String recipient = (String)params.get("recipient");
    Notification.save(notiId, recipient, m_channel.logonUser());
    updateNotificationList();
    return this;
  }

  public Screen refresh(Hashtable params) throws Exception {
    updateNotificationList();

    Notification.cleanup();
    return this;
  }

  public void refresh() throws Exception {
    updateNotificationList();
  }

  public String getVersion() {
    return CNotification.VERSION;
  }

  private void updateNotificationList() throws Exception {
    getNotificationList ();
  }
}
