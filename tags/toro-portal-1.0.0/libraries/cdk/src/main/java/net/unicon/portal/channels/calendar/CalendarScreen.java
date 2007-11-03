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
import java.text.*;

import org.jasig.portal.IMimeResponse;

import net.unicon.academus.apps.calendar.*;
import net.unicon.academus.apps.notification.Notification;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.academus.domain.lms.Memberships;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.channels.rad.Screen;
import net.unicon.portal.channels.rad.PrincipalMap;
import net.unicon.portal.channels.rad.GroupData;
import net.unicon.portal.channels.rad.Finder;


import net.unicon.portal.channels.SuperChannel;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.permissions.IPermissions;


/**
 * This is a base class for screens of Calendar Channel.
 */

abstract public class CalendarScreen extends Screen // implements IMimeResponse
{
  // Some constants
  /**
   * Current calendar identifer.
   */
  public static final String CURCALID = "curcalid";

  //--------------------------------------------------------------//

  public boolean canRenderAsPeephole() {
    return false;
    /*
    Hashtable p;
    if ((p = getXSLTParams()) != null && m_channel.m_crd.getParameter("focusedChannel") != null){
      String sMain = (String) p.get("main");
      if (sMain != null && !sMain.equals("daily"))
        return false;
    }
    return ( getClass().getName().equals(m_channel.getMain()));
    */
  }

  public Hashtable getXSLTParams() {
    Hashtable params = new Hashtable();

    // Focus and Target channel id
    String targetChannel = m_channel.m_crd.getParameter("targetChannel");
    if (targetChannel != null)
      params.put("targetChannel", targetChannel); // Unicon
    else
      params.put("targetChannel", m_channel.m_csd.getChannelSubscribeId());

    // cur-date
    params.put("cur-date", XMLData.SDF.format(m_channel.getCurrentDate()));

    // will need to send all activities
    String channelUidKey = (String)getShared("channelUidKey");
    if( channelUidKey != null) {
      IPermissions pm = ChannelDataManager.getPermissions(channelUidKey);
      if (pm != null)
        try {
          String name = null;
          String value = null;
          User user = ChannelDataManager.getDomainUser(channelUidKey);
          params.putAll(pm.getEnumeratedActivities(user.getUsername()));
        } catch( Exception e) {
          log(e);
        }
    }

    return params;
  }


  /**
   * Get string of version of Calendar channel.
   * @return String version
   */
  public String getVersion() {
    return CCalendar.VERSION;
  }

  //--------------------------------------------------------------//

  /**
   * Get instance server of calendar.
   * @return an instance of CalendarServer
   * @throws Exception
   */
  public CalendarServer getServer() throws Exception {
    return ((CCalendar)m_channel).getServer();
  }

  //--------------------------------------------------------------//

  /**
   * Get xml-based data of the screen. This method returns only data of screen.
   * It's called by buildXML method.
   * @return xml-based XMLData object
   * @throws Exception
   */
  abstract public XMLData getData() throws Exception;

  /**
   * The buildXML overrides the method in RAD. This return well-formed xml-data.
   * @return String of xml-based data.
   * @throws Exception
   */
  public String buildXML() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    ps.println("<?xml version=\"1.0\"?>");
    ps.println("<calendar-system>");
    XMLData setup = new XMLData();
    setup.putA("setup", CCalendar.SETUP);
    XMLData config = new XMLData();
    config.putE("config",setup);
    config.print(ps,1);
    XMLData udata = m_channel.getUserProfile(CCalendar.USERDATA_KEY);
    XMLData pref = new XMLData();
    if (udata != null)
      pref.putE("preference", udata);
    pref.print(ps,1);
    XMLData data = getData();
    data.print(ps, 1);
    XMLData logonData = getServer().getLogonData();
    logonData.print(ps, 1);
    ps.println("</calendar-system>");
    return baos.toString();
  }

  //--------------------------------------------------------------//
  /**
   * Get all calendars
   * @return Array of calendars
   * @throws Exception
   */
  CalendarData[] getAllCalendars() throws Exception {
    return CalendarData.cloneCalProps(getServer().getCalendars(null));
  }
  //--------------------------------------------------------------//
  /**
   * When user click refresh on browser
   */
  /**
   * Refesh the screen.
   * @throws Exception
   */
  public void refresh() throws Exception {
    removeShared("CCalendar.CalendarServer");
    getServer();
  }

  public String curDate() {
    Date date = m_channel.getCurrentDate();
    return XMLData.SDF.format(date);
  }

  public String getTopicName() {
    Offering offering = getOffering();
    return (offering == null || offering.getTopic() == null)? null: offering.getTopic().getName();
  }

  public Offering getOffering() {
    return (Offering)m_channel.m_csd.get("OfferingCalendar");
  }

  public String getOfferingName() {
    Offering offering = getOffering();
    return (offering == null)? null: offering.getName();
  }

  public IdentityData[] getMembers() throws Exception {
    Offering offering = getOffering();
    if (offering == null)
      return null;
    List list = Memberships.getMembers(offering);
    if (list.isEmpty())
      return null;

    log("********* ###### " + list);
    Iterator it = list.iterator();
    Vector v = new Vector();
    while (it.hasNext())
      v.addElement( member((User)it.next()));

    AttendeeData[] idr = (AttendeeData[])v.toArray(new AttendeeData[0]);
    Finder.findDetails(idr, null);
    Finder.findRefferences(m_channel.logonUser(), idr);

    // log
    for (int i=0; i < idr.length; i++) {
      log("****** getMembers " + idr[i]);
    }
    //
    return idr;
  }

  //--------------------------------------------------------------------------//

  /**
   *  Override from <code>IMimeResponse</code> in rad.
   *  @see net.unicon.portal.channels.rad.MimeResponseChannel.
   */
  public Map getHeaders() {
    return null;
  }

  public Map getHeaders(String s) {
    return null;
  }

  /**
   *  Override from <code>IMimeResponse</code> in rad.
   *  @see net.unicon.portal.channels.rad.MimeResponseChannel.
   */
  public String getName() {
    return null;
  }

  public String getName(String s) {
    return s;
  }

  /**
   *  Override from <code>IMimeResponse</code> in rad.
   *  @see net.unicon.portal.channels.rad.MimeResponseChannel.
   */
  public void downloadData(OutputStream out) throws IOException {}

  public void downloadData(OutputStream out, String s) throws IOException {}

  /**
   *  Override from <code>IMimeResponse</code> in rad.
   *  @see net.unicon.portal.channels.rad.MimeResponseChannel.
   */
  public String getContentType() {
    return "zip";
  }

  public String getContentType(String s) {
    return s;
  }

  /**
   *  Override from <code>IMimeResponse</code> in rad.
   *  @see net.unicon.portal.channels.rad.MimeResponseChannel.
   */
  public InputStream getInputStream() throws IOException {
    return null;
  }

  public InputStream getInputStream(String s) throws IOException {
    return null;
  }

  //-----------------//

  protected static AttendeeData member( User user) {
    AttendeeData id = new AttendeeData();
    id.putEntityType(GroupData.S_USER);
    id.putType(IdentityData.ENTITY);
    id.putAlias(user.getUsername());
    id.putEmail(user.getEmail());
    id.putName(user.getUsername());
    return id;
  }

  String getTime(Date start)
  {
    if (start == null) return "";
    Calendar date = Timetable.getCalendar();
    date.setTime(start);
    int hour = date.get(date.HOUR_OF_DAY);
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

}
