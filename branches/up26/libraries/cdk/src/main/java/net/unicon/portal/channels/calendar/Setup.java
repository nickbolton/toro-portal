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

import net.unicon.academus.apps.calendar.*;
import net.unicon.academus.apps.calendar.wcap.WCAP;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.portal.channels.rad.Screen;
import net.unicon.portal.channels.rad.Select;
import net.unicon.portal.channels.rad.GroupData;
import net.unicon.portal.channels.rad.Finder;

/**
 * This class represents setup screen of calendars
 */
public class Setup extends CalendarScreen {

  public static String EVERYONE = "\"everyone\"";
  String S_CONTACT = "a";
  XMLData m_data = new XMLData();
  XMLData m_view = new XMLData();

  /**
   * Get identifier of the screen
   * @return
   */
  public String sid() {
    return "Setup";
  }

  /**
   * Initialize the screen
   * @param params
   * @throws Exception
   */
  public void init(Hashtable params) throws Exception {
    super.init(params);

    // current calendar
    String calid = (String)params.get("calid");
    if (calid == null)
      calid = (String)getShared(CURCALID);

    m_data.putE("view", m_view);
    m_view.putE("calendar", new CalendarData());

    // Calendar to view
    gotoCalendar(calid);
  }

  /**
   * Re-initialize the screen
   * @param params
   * @throws Exception
   */
  public void reinit(Hashtable params) throws Exception {
    gotoCalendar(params);
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
   * Get parameter of XSL
   * @return
   */
  public Hashtable getXSLTParams() {
    Hashtable params = super.getXSLTParams();
    params.put("cur-date", curDate());
    return params;
  }

  public Screen createCalendar(Hashtable params) throws Exception {
    CalendarData cal = new CalendarData();
    m_view.putE("calendar", cal);
    return this;
  }

  /**
   * Process "Create calendar" action
   * @param params
   * @return
   * @throws Exception
   */
  public Screen gotoCalendar(Hashtable params) throws Exception {
    // Calendar to view
    gotoCalendar((String)params.get("calid"));
    return this;
  }

  /**
   * Process "delete user from calendar" action
   * @param params
   * @return
   * @throws Exception
   */
  public Screen deleteUser(Hashtable params) throws Exception {
    // Current editing calendar
    CalendarData editCal = (CalendarData)m_view.getE("calendar");
    ACEData[] newAcl = deleteACE(editCal.getACE(), (String)params.get("user"));
    if (newAcl != null)
      editCal.putACE(newAcl);
    else
      editCal.putACE(null);
    return this;
  }

  /**
   * Process "delete calendar" action
   * @param params
   * @return
   * @throws Exception
   */
  public Screen deleteCalendar(Hashtable params) throws Exception {
    params.put("methodName","delete");

    String calid =(String)params.get("calid");
    CalendarData[] cals = (CalendarData[])m_data.getE("calendar");
    CalendarData curCal = CalendarData.findCalendar(cals, calid);
    String calname = curCal.getCalname();
    //    calname = WCAP.enc(calname);
    return confirm(CCalendar.MSG_CONFIRM_DELETE_CALENDAR, new Object[]{calname},params,sid());
  }

  public Screen delete(Hashtable params) throws Exception {
    // Calendar to delete
    String calid =(String)params.get("calid");
    if (calid != null) {
      // Delete by CS
      getServer().deleteCalendar(calid);

      // Refresh data
      gotoCalendar((String)null);
    }

    return this;
  }

  /**
   * Process "Save" action
   * @param params
   * @return
   * @throws Exception
   */
  public Screen save(Hashtable params) throws Exception {
    // Current editing calendar
    CalendarData editCal = (CalendarData)m_view.getE("calendar");

    //--- Calendar data to save
    String calid = (String)params.get("calid"); // This is the same as view/calendar
    String calname = ((String)params.get("calname")).trim();

    //--- Check input data
    if (calname.length() == 0)
      return error(CCalendar.ERROR_NO_CALENDAR_NAME);
    //truong DPCS 4/10/02
    editCal.putCalname(calname);

    //--- Write flags --------
    updateWriteAccess(editCal.getACE(), params);
    //--- Create/Modify calendar properties
    if (calid == null)
      calid = getServer().createCalendar(editCal).getCalid();
    else if (calid.equals(editCal.getCalid()))
      getServer().updateCalendar(editCal);
    //-- Refesh
    gotoCalendar(calid);

    return this;
  }

  /**
   * Process "Add user to calendar" action
   * @param params
   * @return
   * @throws Exception
   */
  public Screen addUser(Hashtable params) throws Exception {
    //Truyen 03/30/2002
    //catche calendar name
    CalendarData editCal = (CalendarData)m_view.getE("calendar");
    String calname = ((String)params.get("calname")).trim();
    editCal.putCalname(calname);
    //--- Write flags --------
    updateWriteAccess(editCal.getACE(), params);
    params.put("back", sid());
    IdentityData[] users = null;
    //return select( CCalendar.USER_SOURCE, users, "updateSelectedUsers", params);
    return select( "campus,portal", users, "updateSelectedUsers", params);
  }

  //-------------------------------------------------------------------------//
  void gotoCalendar(String calid) throws Exception {
    // Reset parameters
    CalendarData[] cals = getServer().getCalendars(null);
    m_data.putE("calendar", cals);
    // Validate calid
    if (calid == null || CalendarData.findCalendar(cals, calid) == null ||
        !getServer().getUser().equals(CalendarData.findCalendar(cals,calid).getOwner()))
      calid = CalendarData.findPersonal(cals,getServer().getUser()).getCalid();
    //calid = cals[0].getCalid();

    // Save given calendar as current
    CalendarData curCal = CalendarData.findCalendar(cals, calid);
    CalendarData viewCal = (CalendarData)m_view.getE("calendar");
    if (curCal != null) // Edit existing calendar
      viewCal.updateProps(curCal,true);
    else // Create new calendar
      m_view.putE("calendar", new CalendarData());
  }

  ACEData getACEData( IdentityData idt) {
    // Here can not portal group
    if (CCalendar.SERVER_NAME.equals(CCalendar.IPLANET_SERVER)) {
      if( GroupData.isPortalUser(idt) || idt.getEntityType().equals(S_CONTACT)) {
        IdentityData campus = idt.getRef("campus");
        if( campus == null)
          return null;
        else
          idt = campus;
      }
    } else if (CCalendar.SERVER_NAME.equals(CCalendar.DPCS_SERVER)) {
      if(idt.getEntityType().equals(S_CONTACT)) {
        IdentityData ref = idt.getRef("campus") != null ? idt.getRef("campus") : idt.getRef("portal");
        if( ref == null)
          return null;
        else
          idt = ref;
      }
    }

    return new ACEData(idt, false);
  }

  /**
   * This method intended for screen "Select user"
   * @param params
   * @return
   * @throws Exception
   */
  public Screen updateSelectedUsers(Hashtable params)throws Exception {
    //Truyen 03/29/2002
    //new user add
    IdentityData[] new_users = (IdentityData[]) params.get("selected");
    Vector v = new Vector();
    // iPlanet CS
    if ( CCalendar.SERVER_NAME.equals(CCalendar.IPLANET_SERVER)) {
      new_users = GroupData.expandGroups(new_users, true);
      Finder.findRefferences(m_channel.logonUser(), new_users);
    }

    CalendarData cal = (CalendarData)m_view.getE("calendar");
    ACEData[] acl = cal.getACE();
    Vector old_users = new Vector();
    if (acl != null)
      for (int i = 0; i < acl.length ; i++)
        old_users.addElement((IdentityData)acl[i]);

    // save distinct
    Vector users = new Vector();
    HashSet errors = new HashSet();
    for ( int i = 0; i < new_users.length; i++) {
      String err = accept( new_users[i]);
      if( err == null)
        if (!attendeeContains( old_users, new_users[i]))
          users.addElement(getACEData(new_users[i]));
        else
          errors.add(err);
    }

    // New Acl
    // To array new curAcl = acl +  newAcl
    ACEData[] curAcl = new ACEData[(acl != null?acl.length:0) + (users != null?users.size():0)];

    int i = 0;
    if (acl != null)
      for (i = 0; i < acl.length; i++)
        curAcl[i] = acl[i];

    if (users != null)
      for (int j = 0; j < users.size(); j++)
        curAcl[i+j] =  new ACEData((IdentityData)users.elementAt(j), false);

    // Save
    cal.putACE(curAcl);
    // Info message
    if( errors.size() > 0)
      return warningMulti((String[])errors.toArray(new String[0]), sid(), false);
    else
      return this;
  }


  boolean attendeeContains( Vector v, IdentityData user) {
    for( int i = 0; i < v.size(); i++) {
      if( equalsRef( (IdentityData)v.elementAt(i), user))
        return true;
    }

    return false;
  }

  boolean equalsRef(IdentityData user1, IdentityData user2) {
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

  void updateWriteAccess(ACEData[] acl, Hashtable params) throws Exception {
    if (acl != null)
      for (int i = 0; i < acl.length; i++) {
        String user = acl[i].getCuid();
        if (user == null)
          continue;
        int cuid = getServer().getCuidCode(user);
        if (cuid != CalendarServer.CUID_SHARE && cuid != CalendarServer.CUID_OWNER) {
          //log("***"+ i +" * "+ user);
          String write = (String)params.get(user);
          if (write != null && write.equalsIgnoreCase("ON")) {
            acl[i].putFreebusy(TRUE);
            acl[i].putSchedule(TRUE);
            acl[i].putRead(TRUE);
            acl[i].putWrite(TRUE);
          } else if (acl[i].getRead() != null && acl[i].getRead().booleanValue())
            acl[i].putWrite(FALSE);
        }
      }
  }

  ACEData[] deleteACE(ACEData[] acl, String cuid) {
    Vector v = new Vector();
    if (acl != null)
      for (int i = 0; i < acl.length; i++)
        if (cuid.equals(acl[i].getCuid()) == false)
          v.addElement(acl[i]);

    // Allocate new array
    if (v.size() == 0)
      return null;
    ACEData[] newArr = new ACEData[v.size()];
    for (int i = 0; i < v.size(); i++)
      newArr[i] = (ACEData)v.elementAt(i);

    return newArr;
  }

  //--------------------------------------------------------------------------//
  String accept( IdentityData id) {
    if (CCalendar.SERVER_NAME.equals(CCalendar.IPLANET_SERVER)) {
      if( GroupData.isPortalUser(id) || id.getEntityType().equals(S_CONTACT))
        return (( id.getRef("campus") != null)? null:CCalendar.MSG_INVALID_USERS_ATTENDEE);
      else
        return null;
    } else if (CCalendar.SERVER_NAME.equals(CCalendar.DPCS_SERVER)) {
      if(id.getEntityType().equals(S_CONTACT))
        return id.getRef("campus") != null ? null : id.getRef("portal")!= null ? null : CCalendar.MSG_INVALID_USERS_ATTENDEE;
      else
        return null;
    }

    return null;
  }

}
