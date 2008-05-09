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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import net.unicon.academus.apps.calendar.CalendarData;
import net.unicon.academus.apps.calendar.CalendarServer;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.domain.lms.Context;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.academus.service.calendar.CalendarServiceFactory;
import net.unicon.portal.cache.DirtyCacheRequestHandler;
import net.unicon.portal.cache.DirtyCacheRequestHandlerFactory;
import net.unicon.portal.channels.IOfferingSubChannel;
import net.unicon.portal.channels.SuperChannel;
import net.unicon.portal.channels.rad.GroupData;
import net.unicon.portal.channels.rad.Screen;
import net.unicon.portal.common.SubChannelFactory;
import net.unicon.portal.common.cdm.ChannelDataManager;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;


public class CCalendarUnicon extends CCalendar implements IOfferingSubChannel {
  boolean m_renderFocus = true;
  boolean m_log = false;

  /**
   * Default Constructor
   */
  public CCalendarUnicon() {
    super();
        try {
            cacheHandler = DirtyCacheRequestHandlerFactory.getHandler();
        } catch (Exception e) {
            log("CCalendarUnicon: Unable to get DirtyCacheRequestHandler Implementation; DirtyCacheRequestHandlerFactory is throwing errors");
            e.printStackTrace();
        }
  }



  public boolean isRenderingAsPeephole() {
    return !m_renderFocus;
  }

  protected static DirtyCacheRequestHandler cacheHandler = null;

  public void receiveEvent (PortalEvent event) {
    if (event.getEventNumber() == PortalEvent.SESSION_DONE) {
      String uid = m_crd.getParameter(SuperChannel.channelUidKey);
      if (uid != null) {
        ChannelDataManager.removeData(uid);
      }
      super.m_screens = null;
      super.m_lastScreen = null;
      super.m_lastPeephole = null;
      super.m_peParams = null;
      super.m_csd = null;
      super.m_crd = null;
    }
  }

  CRDString m_oldCRD = null;


  public void renderXML(ContentHandler out) throws PortalException {
    try {
      // Check new Channel runtime data
      CRDString crd = new CRDString(m_crd);
      if( m_oldCRD == null || !m_oldCRD.equals(crd)) {
        execute();
        m_oldCRD = crd;
      }

      if(m_lastScreen != null)
        m_lastScreen.processXML(out, null);
    }
    catch (Exception e) {
      e.printStackTrace(System.err);
      String msg = (e==null)?"Object Exception is null":e.getMessage();
      throw new PortalException(msg,e) {
        public int getExceptionCode() {
          return -1;
        }
      };
    }
  }



  /**
   * Set runtime data for channel. This method is called by uPortal.
   * @param crd
   */
  public void setRuntimeData(ChannelRuntimeData crd) {
    super.setRuntimeData(crd);


    // Uncomment to see all parameters...
    // spillParameters(crd);

    // Save channelUidKey
    String channelUidKey = (String)getShared("channelUidKey");
    if( channelUidKey == null) {
      channelUidKey = m_crd.getParameter(SuperChannel.channelUidKey);
      putShared("channelUidKey", channelUidKey);
    }

    // offering in navigation channel
    Offering offering = getOffering(channelUidKey);
    if( offering != null) {
      // Save offering for later usage
      m_csd.put("OfferingCalendar", offering);

      // need to check if user has clicked on different offering
      try {
        if( checkNewOffering(offering)) {
          m_crd.setParameter("killcache", "true");
          m_crd.setParameter("calid", CalendarServiceFactory
                  .getService().getCalendarId(offering));
          m_oldCRD = null;
          Iterator it = m_screens.keySet().iterator();
          while(it.hasNext()){
            String key = (String)it.next();
            Screen scr = (Screen) m_screens.get(key);
            scr.reinit(getParameters());
          }
        }
      } catch (Exception e) {
        log("Exception retrieving User/Offering information. " + e);
      }
      // Get command and check if its a value to dirty the users
      // in the offering. Since we cannot change the code outside
      // of this channel, we have to assume that all commands that
      // that are 'ok' is some sort of modifcation.
      if ("ok".equalsIgnoreCase(getCmdValue("do"))) {
          try {
          cacheHandler.broadcastDirtyChannels(
                                UserFactory.getUser(m_csd.getPerson().getID()),
                                offering,
                                "CCalendarUnicon",
                                false);
          } catch (Exception e) {
              log("CCalendarUnicon: Unable to send dirty request for the offering channels; DirtyCacheRequestHandler is throwing errors");
              e.printStackTrace();
          }
       }
    }

    // Save renderRoot state
    m_crd.setParameter("targetChannel", targetChannelId());
    String up = m_crd.getParameter("uP_root");
    m_renderFocus = (m_crd.getParameter("focusedChannel") != null || (up != null && up.equals("me")));
    log("LCAL.setRuntimeData:m_renderFocus="+m_renderFocus);
    m_crd.remove("focusedChannel");
  }


  /**
   * These methods need to be implemented by all subchannels
   * that want to import/export it's Offering data
   */
  public String exportChannel(Offering offering) throws Exception {
    return "";
  }

  public Map importChannel(Offering offering, Document dom) throws Exception {
    throw new PortalException("importChannel not implemented!");
  }



  /**

   * Get name of class of main screen of calendar.

   * @return

   */

  public String getMain()
  {

    return "net.unicon.portal.channels.calendar.MainLMS";

  }



  // ICacheable

  public ChannelCacheKey generateKey() {

    // Offering.
    Offering o = (Offering) m_csd.get("OfferingCalendar");

    // Key part.
    StringBuffer sbKey = new StringBuffer(1024);
    sbKey.append(getClass().getName()).append(": ");
    sbKey.append("userId=").append(m_csd.getPerson().getID()).append(", ");
    sbKey.append("authenticated=").append(m_csd.getPerson().getSecurityContext().isAuthenticated()).append(", ");
    if (o != null) sbKey.append("offeringId=").append(o.getId());

    // Generate channel key
    ChannelCacheKey k = new ChannelCacheKey();
    k.setKeyScope(m_csd.getPerson().isGuest()?ChannelCacheKey.SYSTEM_KEY_SCOPE:ChannelCacheKey.INSTANCE_KEY_SCOPE);
    k.setKey(sbKey.toString());
    k.setKeyValidity("key");
    return k;

  }

  public boolean isCacheValid(Object validity) {

    boolean cacheValid = true;

    // Invalidate if this channel has been set dirty.
    String upId = (String)getShared("channelUidKey");
    if( upId == null) {
      upId = m_crd.getParameter(SuperChannel.channelUidKey);
      putShared("channelUidKey", upId);
    }
    if (ChannelDataManager.isDirty(upId)) {
        return false;
    }

    // Invalidate if this channel is the target of user activity.
    String targetChannel = m_crd.getParameter(SuperChannel.SC_TARGET_HANDLE);
    if (targetChannel != null && !"".equals(targetChannel) &&
        m_csd.getChannelSubscribeId().indexOf(targetChannel) >= 0) {
        cacheValid = false;
    }

    return cacheValid;

  }

  // End of ICacheable

  IdentityData m_logonOffering = null;



  private boolean checkNewOffering( Offering offering) throws Exception
  {

    // Check changing of offering

    boolean ret = false;

    if( m_logonOffering != null) {

      Offering old = (Offering)m_logonOffering.getA("offering");

      if( old.getId() != offering.getId())

        m_logonOffering = null;

    }



    // Create logon offering

    if( m_logonOffering == null) {

      ret = true;

      String calendarId = CalendarServiceFactory.getService().
        getCalendarId(offering);

      m_logonOffering = new IdentityData(IdentityData.ENTITY, GroupData.S_OBJECT, ""+offering.getId(), calendarId, calendarId);

      m_logonOffering.putA("offering", offering);

      sop("*** logonOffering()=" + m_logonOffering);



      // Create the offering calendar

      CalendarServer server = null;

      m_lastScreen = null;

      try {

        server = getServer();

        CalendarData[] cals = server.getCalendars(null);

        if (CalendarData.findCalendar(cals, calendarId) == null) {
            CalendarServiceFactory.getService().createCalendar(offering);
        }
      }
      catch(Exception e) {

        throw new Exception(getLocalText(server.getError()));

      }

    }

    return ret;

  }

  public CalendarServer getServer() throws Exception
  {
    if (m_logonOffering == null) {
      throw new Exception(
        "CCalendarUnicon::getServerForOffering : No offering given");
    }

    // keep a map of server connections, one for each offering

    Map calendarServers = (Map)getShared("CCalendarUnicon.CalendarServers");
    if (calendarServers == null) {
      calendarServers = new HashMap();
      putShared("CCalendarUnicon.CalendarServers", calendarServers);
    }

    CalendarServer server =
      (CalendarServer)calendarServers.get(m_logonOffering.getID());


    if (server == null) {
      // Create server instance
      server = CalendarServer.getInstance(m_radProps);
      calendarServers.put(m_logonOffering.getID(), server);
      server.login(m_logonOffering, null);
    }
    return server;
  }


  //-----------//



  void dumpRuntime(String msg) {

    sop("###---- Log runtime data:"+msg);

    Enumeration e = m_crd.getParameterNames();

    while(e.hasMoreElements()) {

      String key = (String)e.nextElement();

      String val = m_crd.getParameter(key);

      sop(key+"="+val);

    }

    sop("###---- End of Log runtime data--------");

  }



  void sop(String msg) {
  }



  String targetChannelId() {

    return m_crd.getParameter(SuperChannel.parentIdKey) + SubChannelFactory.subChannelIdSeparator + "CCalendarUnicon";

  }



  Offering getOffering(String channelUidKey) {

    User user = ChannelDataManager.getDomainUser(channelUidKey);

    Context context = user.getContext();

    return context.getCurrentOffering(TopicType.ACADEMICS);

  }



  class CRDString {

    Properties m_props = new Properties();

    public CRDString(ChannelRuntimeData crd) {

      Enumeration e = crd.getParameterNames();

      while(e.hasMoreElements()) {

        String key = (String)e.nextElement();

        String val = crd.getParameter(key);

        if (val != null)

          m_props.setProperty(key,val);

      }

    }



    public boolean equals(Object o) {

      if( o instanceof CRDString) {

        Properties props = ((CRDString)o).m_props;

        if( m_props.size() != props.size())

          return false;



        Enumeration e = m_props.propertyNames();

        while(e.hasMoreElements()) {

          String key = (String)e.nextElement();

          String val = m_props.getProperty(key);

          String otherVal = props.getProperty(key);

          if( (otherVal == null && val != null) || (otherVal!= null && val == null))

            return false;

          if( otherVal != null && val != null && !val.equals(otherVal))

            return false;

        }

        return true;

      }
      else

        return false;

    }

  }

    private static void spillParameters(ChannelRuntimeData crd) {
        StringBuffer rslt = new StringBuffer();
        java.util.Enumeration keys = crd.getParameterNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) crd.getParameter(key);
            rslt.append("\t" + key + "=" + value + "\n");
        }
        System.out.println("\n" + rslt.toString());
    }

}

