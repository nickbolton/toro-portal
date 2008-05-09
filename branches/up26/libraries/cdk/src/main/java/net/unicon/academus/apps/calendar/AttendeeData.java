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

package net.unicon.academus.apps.calendar;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XMLData;

/**
 * This class represents Attendee.
 * It is xml-based object.
 */
public class AttendeeData extends IdentityData {
  // members, optimized usage
  public String status = "";

  /**
   * Default constructor
   */
  public AttendeeData() {
    putType(ENTITY);
  }

  /**
   * Constructor of Attendee with given description.
   * @param desc
   */
  public AttendeeData(String desc) {
    super(desc);
  }

  /**
   * Constructor of Attendee with given IdentityData.
   * @param data
   */
  public AttendeeData( IdentityData data) {
    super(data);
    putCuid(data.getAlias());
    putRSVP(new Boolean(true));
    putRole("REQ_PARTICIPANT");
    put(data.getName());

    // Refferences: from different sources
    putRef("portal",data.getRef("portal"));
    putRefID("portal",data.getRefID("portal"));
    putRef("campus",data.getRef("campus"));
    putRefID("campus",data.getRefID("campus"));
    putRef("contact",data.getRef("contact"));
    putRefID("contact",data.getRefID("contact"));
  }

  //public AttendeeData()
  //{
  //  putEntityType(CampusData.S_CAMPUS);
  //  putType(IdentityData.ENTITY);
  //}
  // <attendee cuid='cuid' iid='iid' itype='U/G' ientity='o/u/g/c' role='role' status='status' rsvp='rsvp'>name</attendee>

  /**
   * Get calendar user identifier
   * @return
   */
  public String getCuid() {
    return cuid((String)getA("cuid"));
  }

  /**
   * Set calendar user identifier.
   */
  public void putCuid(String cuid) {
    putA("cuid", cuid);
  }

  /**
   * Get name of attendee.
   * @return
   */
  public String getName() {
    return (String)get
             ();
  }

  /**
   * Set name of attendee.
   * @param name
   */
  public void putName(String name) {
    put(name);
  }

  //  public String get() {return (String)getName();}
  //  public void put(String name) {put(name);}
  /*27/04/02*/
  //public String getIid() {return (String)getA("iid");}
  //public void putIid(String iid) {putA("iid", iid);}

  //public String getType() {return (String)getA("itype");}
  //public void putType(String type) {putA("itype", type);}

  //public String getEntityType() { return (String)getA("ientity");}
  //public void putEntityType(String type) { putA("ientity",type);}

  /**
   * Get role of attendee
   * @return
   */
  public String getRole() {
    return (String)getA("role");
  }

  /**
   * Set role for attendee.
   * @param role
   */
  public void putRole(String role) {
    putA("role", role);
  }

  public String getStatus() {
    return (String)getA("status");
  }
  public void putStatus(String status) {
    putA("status", status);
  }

  public Boolean getRSVP() {
    return (Boolean)getA("rsvp");
  }
  public void putRSVP(Boolean rsvp) {
    putA("rsvp", rsvp);
  }

  public String getComment() {
    return (String)getA("comment");
  }
  public void putComment(String status) {
    putA("comment", status);
  }

  //--------------------------------------------------------------------------//
  /**
   * Find a user by name in array of Attendee.
   * @param atts
   * @param user
   * @return
   */
  public static AttendeeData findAttendeeByCuid(AttendeeData[] atts, String user) {
    if (atts != null)
      for (int i = 0; i < atts.length; i++) {
        if (user.equals(atts[i].getCuid()))
          return atts[i];
      }

    return null;
  }
  /**
   * Find a user by id in array of Attendee.
   * @param atts
   * @param iid
   * @return
   */
  public static AttendeeData findAttendeeById(AttendeeData[] atts, String iid) {
    if (atts != null)
      for (int i = 0; i < atts.length; i++) {
        if (iid.equals(atts[i].getID()))
          return atts[i];
      }

    return null;
  }
  //Truyen 04/24/02
  /**
   * @param attendeeId either user id or maito:...
   */
  String cuid(String attendeeId) {
    if (attendeeId == null)
      return attendeeId;
    if (attendeeId.startsWith("mailto:") && attendeeId.indexOf("@") != -1)
      return attendeeId.substring(7, attendeeId.indexOf('@'));
    else
      return attendeeId;
  }


}
