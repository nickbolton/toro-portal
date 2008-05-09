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
import java.net.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.ContentHandler;
import org.jasig.portal.*;
import org.apache.xalan.xslt.*;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.security.IPerson;

import net.unicon.academus.apps.notification.*;
import net.unicon.portal.channels.rad.Channel;
import net.unicon.portal.channels.rad.Screen;
import net.unicon.portal.channels.rad.Info;


public class CNotification extends Channel
  implements INotifier, IPrivilegedChannel {
  // version
  static final String VERSION = "1.0.0";

  // Error message
  public static final String ERROR_CANNOT_OPEN = "ERROR_CANNOT_OPEN";
  public static final String CONFIRM_DELETE = "CONFIRM_DELETE";

  PortalControlStructures m_pcs = null;
  String m_notiId = null;

  // Indicates whether this Notification channel instance has been swapped out
  // and replaced with the Survey channel instance
  private boolean channelSwap = false;

  public void goBack(ContentHandler out) throws PortalException {
    // get the chammel manager and swap CNotification back in, with the same channel id
    ChannelManager cm = m_pcs.getChannelManager();
    cm.setChannelInstance(m_csd.getChannelSubscribeId(),this);
    //Clear params do,go,.. were saved before
    m_crd.remove("CNotification.notification");
    m_crd.setParameter("do",(String)null);
    m_crd.setParameter("go",(String)null);
    renderXML(out);
  }

  public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
    m_pcs = pcs;
  }


  // Overriding method of Channel class
  // TTrack: 03745  Notification - survey producing undesirable caching
  public boolean isCacheValid(Object validity) {

	boolean cacheValid = super.isCacheValid(validity);
	
	
	// If the notification channel was swapped out and replaced with the
	// survey channel the wrong content got cached as the notifications
	// content cache
	if (channelSwap)
	{
		// Invalidate the cache and reset flag
		cacheValid = false;
		channelSwap = false;
	}
	
	return cacheValid;
  }

  public void renderXML(ContentHandler out) throws PortalException {
    // if user clicks on a notification link
    String doAct = m_crd.getParameter("do");
    if (doAct!= null && doAct.equals("open")) {
      m_crd.remove("do");

      Hashtable params  = getParameters();
      String noti  = (String)params.get("notiId");
      String recipient = (String)params.get("recipient");

      // build the notification object and swap channel
      Notification note = null;
      try { // Thach May-28
        note = Notification.getNotification(Integer.parseInt(noti), recipient);

        System.out.println("[CNotification]renderXML:note.m_notified="+note.m_notified+":note.m_params="+note.m_params);

        // Normal entries
        if (note.m_notified != null && note.m_params != null) {
          openNotification( note, out);
        }

        // Deleted/Modified entries
        else if (note.m_notified == null && note.m_params != null) {
          Screen info = getScreen(Info.SID);
          if (info == null)
            info = makeScreen("net.unicon.portal.channels.rad.Info");
          info.putParameter("icon", Info.INFO);
          info.putParameter("text", note.m_params);
          info.putParameter("back", Peephole.SID);
          info.processXML(out,null);
        } else {
          super.renderXML(out);
        }
      } catch (Exception e) {
        log(e);
        String msg = (e==null)?"Object Exception is null":e.getMessage();
        throw new PortalException(msg) {
          public int getExceptionCode() {
            return -1;
          }
        };
      }
    } else {
      super.renderXML(out);
    }
  }

  public Hashtable getParameters() {
    Hashtable params = new Hashtable();
    for (Enumeration e = m_crd.keys(); e.hasMoreElements();) {
      Object key = e.nextElement();
      Object value = m_crd.get(key);

      if (value instanceof String[]) {
        if (((String[])value)[0]!= null) // additional
          params.put(key, ((String[])value)[0]);
      } else
        params.put(key, value);
    }
    return params;
  }

  public String getMain() {
    return "net.unicon.portal.channels.notification.Peephole";
  }

  //---------------------------------------------------------------------//
  void openNotification( Notification note, ContentHandler out) throws Exception {
    // for now, instantiate callback channel directly, note.m_notified is IChannel
    IChannel notified = (IChannel)Class.forName(note.m_notified).newInstance();

    // get the channel manager and swap CNotification with the notified channel
    ChannelManager cm = m_pcs.getChannelManager();
    cm.setChannelInstance(m_csd.getChannelSubscribeId(),(IChannel)notified);

    try {
      // Prepare to swap channels
      m_crd.put("CNotification.notification", note);
      notified.setStaticData(m_csd);
      notified.setRuntimeData(m_crd);

      // Open survey channel and set channel swap flag
	  channelSwap = true;
	  ((INotified)notified).openNotification( note, this, out); // Open Notification
	} catch( Exception e) {
	  e.printStackTrace(System.err);
	  cm.setChannelInstance(m_csd.getChannelSubscribeId(),this);
	  m_crd.remove("CNotification.notification");
	  m_crd.setParameter("go",(String)null);
	  m_crd.setParameter("do","errorOpen");
	  m_crd.setParameter("sid",Peephole.SID);

	  // Reset flag since channel didnt get swapped out successfully
	  channelSwap = false;

	  super.renderXML(out);
    }
  }
}
