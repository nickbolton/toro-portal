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

import java.util.*;
import java.text.MessageFormat;
import java.io.*;

import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.provider.NotSoOpaqueCredentials;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.PortalException;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.IServant;
import org.jasig.portal.IChannel;
import org.xml.sax.ContentHandler;
import org.jasig.portal.ChannelCacheKey;

import net.unicon.academus.apps.calendar.*;
import net.unicon.academus.apps.notification.INotified;
import net.unicon.academus.apps.notification.INotifier;
import net.unicon.academus.apps.notification.Notification;
import net.unicon.portal.channels.rad.Screen;
import net.unicon.portal.channels.rad.Info; // Thach-May18
import net.unicon.portal.channels.rad.Channel;
import net.unicon.portal.channels.rad.PrincipalMap;
import net.unicon.portal.channels.rad.MimeResponseChannel;
import net.unicon.portal.channels.notification.CNotification; // Thach-May18
import net.unicon.portal.common.service.notification.INotifyCallback;

import com.interactivebusiness.portal.channel.utils.WalletBase;

/**
 * This class represents channel of calendar.
 */
public class CCalendar extends MimeResponseChannel implements INotified, INotifyCallback
{
  // version
  static final String VERSION = "1.0.0";

  public static final String CMD_SEPARATOR = "~";

  // calendar server name: net.unicon.academus.apps.calendar.wcap.WCAP or net.unicon.academus.apps.calendar.dpcs.DPCS
  public static String SERVER_NAME = getRADProperty("calendar.server");

  // User source from which may be selected
  public static String USER_SOURCE = getRADProperty("calendar.usersource");

  // Calendar security
  public static String SECURITY = getRADProperty("calendar.security");

  //Calendar setup screen
  public static String SETUP = getRADProperty("calendar.setup");

  //Calendar todo before current date
  public static int TODO_BEFORE = (parseInt(getRADProperty("calendar.todos.before")) ==0?7:parseInt(getRADProperty("calendar.todos.before")));

  //Calendar todo after current date
  public static int TODO_AFTER = (parseInt(getRADProperty("calendar.todos.after"))==0?6:parseInt(getRADProperty("calendar.todos.after")));

  //Calendar event after current date
  public static int EVENT_AFTER = (parseInt(getRADProperty("calendar.events.after"))==0?6:parseInt(getRADProperty("calendar.events.after")));

  //Calendar server running
  public static final String IPLANET_SERVER = "net.unicon.academus.apps.calendar.wcap.WCAP";
  public static final String DPCS_SERVER = "net.unicon.academus.apps.calendar.dpcs.DPCS";
  public static final String SOURCE_SELECT ="portal, campus";

  //Errors login user
  public static final String ERROR_NO_USER = "ERROR_NO_USER";
  public static final String ERROR_SECURITY_NOMAP = "ERROR_SECURITY_NOMAP";
  public static final String ERR_LOGIN_FAILED = "ERR_LOGIN_FAILED";
  // Errors in screen "Setup"
  public static final String ERROR_NO_CALENDAR_NAME = "ERROR_NO_CALENDAR_NAME";
  public static final String MSG_INVALID_USERS_ADD = "MSG_INVALID_USERS_ADD";

  // Errors and Messages in screen "Delete"
  public static final String ERROR_FAILED_TO_DELETE = "ERROR_FAILED_TO_DELETE";
  public static final String MSG_DELETE_DELETED = "MSG_DELETE_DELETED";
  public static final String MSG_NO_DELETE_DELETED = "MSG_NO_DELETE_DELETED";
  public static final String MSG_CONFIRM_DELETE_CALENDAR = "MSG_CONFIRM_DELETE_CALENDAR";

  // Errors in screen "Event"
  public static final String ERROR_NO_EVENT_TITLE = "ERROR_NO_EVENT_TITLE";
  public static final String ERROR_INPUT_UNTILDATE = "ERROR_INPUT_UNTILDATE";
  public static final String ERROR_INPUT_LENGTH = "ERROR_INPUT_LENGTH";
  public static final String ERROR_INPUT_NUMBER = "ERROR_INPUT_NUMBER";
  public static final String ERROR_FAILED_TO_CREATE = "ERROR_FAILED_TO_CREATE";
  public static final String ERROR_FAILED_TO_UPDATE = "ERROR_FAILED_TO_UPDATE";
  public static final String MSG_EVENT_ADDED = "MSG_EVENT_ADDED";
  public static final String MSG_EVENT_UPDATED = "MSG_EVENT_UPDATED";
  public static final String MSG_EVENT_DELETED = "MSG_EVENT_DELETED"; // Thach-May18
  public static final String MSG_CALENDAR_NOTFOUND = "MSG_CALENDAR_NOTFOUND"; // Tien 0116

  public static final String ERROR_INPUT_REPEAT = "ERROR_INPUT_REPEAT";
  public static final String MSG_INVALID_USERS_ATTENDEE = "MSG_INVALID_USERS_ATTENDEE";

  // Errors and messages in screen "Todo"
  public static final String ERROR_NO_TODO_TITLE = "ERROR_NO_TODO_TITLE";
  public static final String ERROR_TODO_FAILED_TO_CREATE = "ERROR_TODO_FAILED_TO_CREATE";
  public static final String ERROR_TODO_FAILED_TO_UPDATE = "ERROR_TODO_FAILED_TO_UPDATE";
  public static final String MSG_TODO_ADDED = "MSG_TODO_ADDED";
  public static final String MSG_TODO_UPDATED = "MSG_TODO_UPDATED";

  // Errors and messages in screen "Peephole"
  public static final String MSG_PEEPHOLE_NO_CALENDAR = "MSG_PEEPHOLE_NO_CALENDAR";
  public static final String MSG_PEEPHOLE_NOTFOUND_TODO = "MSG_PEEPHOLE_NOTFOUND_TODO";

  // Errors
  public static final String ERROR_PERSONAL_CALENDAR_NOT_FOUND = "ERROR_PERSONAL_CALENDAR_NOT_FOUND";
  public static final String ERROR_INVITATION_NOT_FOUND = "ERROR_INVITATION_NOT_FOUND";
  public static final String ERROR_EVENTS_NOT_SORTED = "ERROR_EVENTS_NOT_SORTED";

  // Notification
  public static final String MSG_INVITATION_DECLINED = "MSG_INVITATION_DECLINED"; // Thach-May18
  public static final String ERROR_NOTIFICATION_INVALID_PARAMS = "ERROR_NOTIFICATION_INVALID_PARAMS";
  public static final String ERROR_NOTIFICATION_INVITATION_NOT_FOUND = "ERROR_NOTIFICATION_INVITATION_NOT_FOUND";
  public static final String STATUS_ACCEPTED = "ACCEPTED";//do not modify this
  public static final String STATUS_DECLINED = "DECLINED";//do not modify this
  public static final String MSG_INVITAITON_UPDATE = "MSG_INVITAITON_UPDATE";
  public static final String MSG_NOTIFICATION_TITLE_CANCELED = "MSG_NOTIFICATION_TITLE_CANCELED";
  public static final String MSG_NOTIFICATION_CANCELED = "MSG_NOTIFICATION_CANCELED";
  // for Unicon
  public static final String MSG_NOTIFICATION_PCAL_ADD = "MSG_NOTIFICATION_PCAL_ADD";
  public static final String MSG_NOTIFICATION_LCAL_ADD = "MSG_NOTIFICATION_LCAL_ADD";
  public static final String MSG_NOTIFICATION_PCAL_UPDATE = "MSG_NOTIFICATION_PCAL_UPDATE";
  public static final String MSG_NOTIFICATION_LCAL_UPDATE = "MSG_NOTIFICATION_LCAL_UPDATE";
  public static final String MSG_NOTIFICATION_LCAL_DELETE = "MSG_NOTIFICATION_LCAL_DELETE";
  public static final String MSG_NOTIFICATION_PCAL_DELETE = "MSG_NOTIFICATION_PCAL_DELETE";
  // Email
  public static final String MSG_EMAIL_LCAL_BODY_ADD = "MSG_EMAIL_LCAL_BODY_ADD";
  public static final String MSG_EMAIL_PCAL_BODY_ADD = "MSG_EMAIL_PCAL_BODY_ADD";
  public static final String MSG_EMAIL_LCAL_BODY_UPDATE = "MSG_EMAIL_LCAL_BODY_UPDATE";
  public static final String MSG_EMAIL_PCAL_BODY_UPDATE = "MSG_EMAIL_PCAL_BODY_UPDATE";
  public static final String MSG_EMAIL_LCAL_BODY_DELETE = "MSG_EMAIL_LCAL_BODY_DELETE";
  public static final String MSG_EMAIL_PCAL_BODY_DELETE = "MSG_EMAIL_PCAL_BODY_DELETE";

  public static final String ERROR_INVALID_FILE = "ERROR_INVALID_FILE";

  public static final String USERDATA_KEY = "net.unicon.portal.channels.calendar.CCalendar";

  private IServant servant;

  /**
   * Set runtime data for channel. This method is called by RAD and uPortal.
   * @param crd
   */
  public void setRuntimeData(ChannelRuntimeData crd)
  {
    super.setRuntimeData(crd);

    try
    {
      // check to see if Credentials have been located
      if (SECURITY.equals("wallet"))
      {
        if( m_csd.get("CalendarCredentials") == null) {
          // Create communication with Security Wallet
          servant = getWalletServant (m_crd);

          // take a look at the wallet to see if credentials exist.
          Map myWallet = ((WalletBase)servant).getWalletCredentials("Calendar");
          //Map myWallet = null;
          if (myWallet == null || myWallet.get("username") == null)
          {
            // wallet not found...call Wallet Base and prompt user
            ((IChannel) servant).setRuntimeData(m_crd);
            // check if user has finished entering credentials
            checkIfFinished ();
          }
          else
          {
            // wallet found for user, now check credentials inside wallet!!!
            boolean successful = checkCredentials ((HashMap) myWallet);
            if (successful)
            {
              m_csd.remove("WalletServant");
              m_csd.put("CalendarCredentials", (HashMap) myWallet);
            }
            else
            {
              // wallet data not correct..call Wallet Base and prompt user
              ((WalletBase)servant).removeWalletCredentials ("Calendar");
              ((IChannel) servant).setRuntimeData(m_crd);

              m_csd.setParameter("WalletServantFinished","false");
            }
          }
        }
        else {
          IPerson person = m_csd.getPerson();
          logonUser().putA("calendar-logon", person.getAttribute("logon"));
        }
      }

      // Table
      else if(SECURITY.equals("table") && logonUser().getA("calendar-logon") == null) {
        tableSecurity();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace(System.err);
      log("Exception occurred while trying to get wallet credentials: " + e);
    }
  }
/*
  // ICacheable
  public boolean isCacheValid(Object validity){return false;}

  public ChannelCacheKey generateKey(){return null;}
  //
*/

  private void checkIfFinished ()
  {
    if (servant.isFinished())
    {
      Object[] results = servant.getResults();
      if (results != null && results.length > 0)
      {
        boolean successful = checkCredentials ((HashMap) results[0]);
        if (successful)
        {
          m_csd.remove("WalletServant");
          m_csd.put("CalendarCredentials", (HashMap) results[0]);
          // create wallet for future use
          boolean saved = ((WalletBase)servant).storeWalletCredentials("Calendar", (HashMap) results[0]);
        }
        else
        {
          ((WalletBase)servant).setLoginError(true);
          m_csd.setParameter("WalletServantFinished","false");
          m_csd.remove("Credentials");
        }
      }
      else
      {
        ((WalletBase)servant).setLoginError(true);
        m_csd.setParameter("WalletServantFinished","false");
      }
    }

  }

  private IServant getWalletServant (ChannelRuntimeData crd)
  {
    try
    {
      if (m_csd.get("WalletServant") == null)
      {
        servant = (IServant) new WalletBase ();
        ChannelStaticData servantStatic = (ChannelStaticData) m_csd.clone();
        ((IChannel)servant).setStaticData(servantStatic);
        m_csd.put("WalletServant", servant);
      }
      else
      {
        servant = (IServant)  m_csd.get("WalletServant");
      }
    }
    catch (Exception e)
    {
     return null;
    }

    return servant;
  }

  private boolean checkCredentials (HashMap credentials)
  {
    try
    {
      String username = (String) credentials.get("username");
      String password = (String) credentials.get("password");
      CalendarServer cServer = CalendarServer.getInstance(m_radProps);
      logonUser().putA("calendar-logon", username);
      boolean au = cServer.authenticate(logonUser(), password);
      if (!au) {
        logonUser().removeA("calendar-logon");
        return false;
      }

      // if login successfull
      IPerson person = m_csd.getPerson();
      person.setAttribute("logon", username);
      person.setAttribute("password", password);
      m_csd.setPerson(person);
    }
    catch (Exception e)
    {
      log ("Invalid Calendar Credentials:" + e);
      logonUser().removeA("calendar-logon");
      return false;
    }
    return true;
  }

  private void tableSecurity()
  {
    String err = null;
    PrincipalMap map = PrincipalMap.map(m_csd.getPerson(), "Calendar", SECURITY);
    if( map == null)
      err = ERROR_SECURITY_NOMAP;
    else {
      CalendarServer server = null;
      try {
        server = CalendarServer.getInstance(m_radProps);
        logonUser().putA("calendar-logon", map.logon);
        boolean au = server.authenticate(logonUser(), map.password);
        if (!au) {
          logonUser().removeA("calendar-logon");
          err = ERR_LOGIN_FAILED;
        }
        else {
          // if login successfull
          IPerson person = m_csd.getPerson();
          person.setAttribute("logon", map.logon);
          person.setAttribute("password", map.password);
          m_csd.setPerson(person);
        }
      } catch( Exception e) {
        if( server != null)
          err = server.getError();
        else
          err = e.getMessage();
        logonUser().removeA("calendar-logon");
      }
    }

    // Check error
    if( err != null)
      m_crd.setParameter("security-error", getLocalText(err));
    else
      m_crd.setParameter("go", getMain());
  }


  protected void finalize()
  {
    CalendarServer server = (CalendarServer)getShared("CCalendar.CalendarServer");
    if (server != null)
    {
      try
      {
        server.logout();
        server = null;
      }
      catch (Exception e)
      {
        log(e);
      }
      removeShared("CCalendar.CalendarServer");
    }
  }

  /**
   * Get main class name of screen. This class name is used for create Peephole
   * screen for calendar.
   * @return
   */
  public String getMain()
  {
    return "net.unicon.portal.channels.calendar.Peephole";
  }

  /**
   * Get an instance of calendar server
   * @return
   * @throws Exception
   */
  public CalendarServer getServer() throws Exception
  {
    CalendarServer server = (CalendarServer)getShared("CCalendar.CalendarServer");
    // always get new data
    //CalendarServer server = null;
    if (server == null)
    {
      // Create server instance
      server = CalendarServer.getInstance(m_radProps);

      // try to login to CalendarServer
      PrincipalMap map = PrincipalMap.map(m_csd.getPerson(), "Calendar", SECURITY);
      if( map == null)
        throw new Exception(getLocalText(CCalendar.ERROR_NO_USER));
      logonUser().putA("calendar-logon", map.logon); // NLe 0626
      try
      {
        //truong DPCS 5/4/02
        //String[] calidr = server.login(logonUser(), map.password);
        putShared("CCalendar.CalendarServer", server);
        //truyen 6/6/02
        //System.out.println("map.password " + map.password);
        server.login(logonUser(), map.password);
        //putShared("CCalendar.Allcalendars", cals);
      }
      catch(Exception e)
      {
        String err = server.getError();
        throw new Exception(getLocalText(err));
      }
    }
    return server;
  }

  //----------- INotified ----------------------------------------------//
  public INotifier m_notifier = null;
  Notification m_noti = null;

  /**
   * This method is called when user open a notification entry, which is event of calendar.
   * @param notifier is CNotification channel object
   */
  public void openNotification( Notification notification, INotifier notifier, ContentHandler out) throws Exception
  {
    // Check param
    log("m_notificationId="+notification.m_notificationId);
    log("recip="+notification.m_recipient);
    log("m_type="+notification.m_type);
    log("m_params="+notification.m_params);
    log("m_status="+notification.m_status);

    if( notification.m_params.indexOf(";ceid=") == -1) throw new Exception(ERROR_NOTIFICATION_INVALID_PARAMS);

    // Valid param
    m_notifier = notifier;
    m_noti = notification;
    renderXML(out);
  }


  /**
   * Check whether can go back to the CNotification channel.
   * m_notifier must be not null
   */
  public boolean isNotificationClosed()
  {
    String doAct = getCmdValue("do");
    String sid = m_crd.getParameter("sid");
    if (doAct != null && ( (doAct.equals("ok") && sid != null && (sid.equals(Event.SID) || sid.equals(Info.SID))) ||
        doAct.equals("reply") || doAct.equals("Close")))
      return true;
    String goAct = getCmdValue("go");
    if( goAct != null)
      return true;

    return ( goAct == null && doAct == null);
  }

  //---------------------------------------------------------------//

  void notiMessage(String id) throws Exception
  {
    m_crd.setParameter("icon", Info.INFO);
    String msg = getLocalText(id);
    m_crd.setParameter("message",msg);
    throw new Exception(msg);
  }

  void prepareNoti() throws Exception
  {
    // Parse params: ceid
    int idx = m_noti.m_params.indexOf(";ceid=");
    String ceid = m_noti.m_params.substring(idx+6);
    String notiCalid = m_noti.m_params.substring(6,idx);
    String calid = getServer().getUser();

    // Find entry data with given ceid
    boolean invi = false;
    EntryData[] ents = getServer().fetchEventsByIds(calid, new EntryRange[] {new EntryRange(ceid,3)});
    EntryData ent = EntryData.findEntry(ents, ceid);

    if (ent == null) {
      ents = getServer().fetchInvitations();
      ent = EntryData.findEntry(ents, ceid);
      if( ent != null)
        invi = true;
    }

    // Event in noticalid
    if (ent == null) {
      ents = getServer().fetchEventsByIds(notiCalid, new EntryRange[] {new EntryRange(ceid,3)});
      ent = EntryData.findEntry(ents, ceid);
      if( ent != null)
        calid = notiCalid;
    }

    // Check calendar
    CalendarData[] cals = getServer().getCalendars(null);
   //dump
   //for (int i = 0; i < cals.length; i++)
   //  System.out.println("cals[i].getCalid() "+ cals[i].getCalid());
   //
    CalendarData one = (CalendarData)CalendarData.findCalendar(cals,calid);
    if( one == null && invi == false) // Tien 0119
      notiMessage( MessageFormat.format( getLocalText(MSG_CALENDAR_NOTFOUND), new Object[] {calid}));

    // Event is deleted
    else if (ent == null)
      notiMessage(MSG_EVENT_DELETED);

    // Event was declined
    else if (ent.isDeclined())
      notiMessage(MSG_INVITATION_DECLINED);

    // Event is Invitation or Accepted invitation or Own event
    else {
      String sid = invi? "Invitations": Event.SID;
      Screen scr = this.getScreen(sid);
      if( scr == null)
       scr = makeScreen(sid);
      m_crd.put("calid",calid);
      m_crd.put("notiCalid",notiCalid);
      m_crd.put("ceid",ceid);
      m_crd.put("Noti.all", ents);
      m_crd.put("Noti.cal", one);
      m_crd.setParameter("sid",scr.sid());
      m_crd.remove("go");
      m_crd.setParameter("do","notificationReply");
    }
  }

  boolean m_errInfo = false;

  /**
   * Render XML. This overrides the renderXML in RAD.
   * @param out
   * @throws PortalException
   */
  public void renderXML(ContentHandler out) throws PortalException
  {
    IChannel servant = (IChannel) m_csd.get("WalletServant");
    if (servant != null)
      servant.renderXML(out);
    else if(SECURITY.equals("table") && logonUser().getA("calendar-logon") == null) {
      try {
        // Check noti return
        if (m_errInfo && m_notifier != null && isNotificationClosed())
          m_notifier.goBack(out);
        else {
          Screen info = getScreen(Info.SID);
          if (info == null)
            info = makeScreen("net.unicon.portal.channels.rad.Info");
          info.putParameter("icon", Info.INFO);
          info.putParameter("text", m_crd.getParameter("security-error"));
          info.processXML(out,null);
          m_errInfo = true;
        }
      }
      catch (Exception e) {
        log(e);
        String msg = (e==null)?"Object Exception is null":e.getMessage();
        throw new PortalException(msg,e) {public int getExceptionCode() { return -1;}};
      }
    }
    else {
      try {
        // Notification
        if( m_noti != null) {
          prepareNoti();
          m_noti = null;
        }

        // Do something
        execute();
        if (m_notifier != null && isNotificationClosed())
          m_notifier.goBack(out);
        else if(m_lastScreen != null) {
          /*
          IServant serv = m_lastScreen.getServant();
          if( serv != null)
            ((IChannel)serv).renderXML(out);
          else
          */
            m_lastScreen.processXML(out, null);
        }
      }
      catch (Exception e) {
        //log(e);
        e.printStackTrace(System.err);
        String msg = (e==null)?"Object Exception is null":e.getMessage();
        throw new PortalException(msg,e) {public int getExceptionCode() { return -1;}};
      }
    }
  }
  //--------------------------------------------------------------------//

  static String buildNotificationParams( String calid, String ceid)
  {
    return "calid=" + calid + ";ceid=" + ceid;
  }
  //--------------------------------------------------------------------//
  static int parseInt(String number){

    try{
      return Integer.parseInt(number);
    }catch(Exception ex){
      return 0;
    }

  }

  public static String prepareNotificationCallback(String params) {
      StringBuffer rslt = new StringBuffer();
      int idx = params.indexOf(";ceid=");
      if (idx > 0) {
          String ceid = params.substring(idx+6);

          rslt.append("<external-action ref=\"calendar\">")
              .append(  "<param name=\"username\" value=\"%USERNAME%\" />")
              .append(  "<param name=\"ceid\" value=\"").append(ceid).append("\" />")
              .append(  "<param name=\"action\" value=\"noti\" />")
              .append("</external-action>");
      }

      return rslt.toString();
  }

}
