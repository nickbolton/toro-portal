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

/**
 * This class represents Access Control Entries (ACE strings) that is used in
 * "UI layer". It is similar to ACE in <a href="wcap/WCAP.html">WCAP</a> but not the same.
 * The ACEs determine access control for calendar.
 * ACE uses flags to control access. There are 4 flags:
 * <ul>
 *  <li>Read</li>
 *  <li>Write</li>
 *  <li>Freebusy</li>
 *  <li>Schedule</li>
 * </ul>
 * It is xml-based object.
 */
public class ACEData extends IdentityData {
  // <ace cuid='cuid'  iid='iid' type='U/G' read='boolean' write='boolean' freebusy='boolean' schedule='boolean'>name</ace>
  /**
   * Default constructor
   */
  public ACEData() {
    putType(ENTITY);
  }

  /**
   * Constructor with given an instance of IdentityData
   * @param data IdentityData
   * @param write indicates the ACE have right "write"
   */
  public ACEData( IdentityData data, boolean write) {
    super(data);
    putCuid(data.getAlias());
    putFreebusy(new Boolean(true));
    putSchedule(new Boolean(true));
    putRead(new Boolean(true));
    putWrite(new Boolean(write));

    // Refferences: from different sources
    putRef("portal",data.getRef("portal"));
    putRefID("portal",data.getRefID("portal"));
    putRef("campus",data.getRef("campus"));
    putRefID("campus",data.getRefID("campus"));
    putRef("contact",data.getRef("contact"));
    putRefID("contact",data.getRefID("contact"));
  }

  /**
   * Get calendar user identifier.
   * @return
   */
  public String getCuid() {
    return (String)getA("cuid");
  }

  /**
   * Set calendar user identifier.
   */
  public void putCuid(String cuid) {
    putA("cuid", cuid);
  }//; put(cuid);}

  //public String getName() {return (String)get();}
  /**
   * Set name of ACE.
   * @param name
   */
  public void putName(String name) {
    super.putName(name);
    put(name);
  }

  //public String getIid() {return (String)getA("iid");}
  //public void putIid(String iid) {putA("iid", iid);}

  //public String getType() {return (String)getA("itype");}
  //public void putType(String type) {putA("itype", type);}

  //public String getEntityType() { return (String)getA("ientity");}
  //public void putEntityType(String type) { putA("ientity",type);}

  /**
   * Get Read flag of ACE.
   * @return
   */
  public Boolean getRead() {
    return (Boolean)getA("read");
  }

  /**
   * Set Read flag for ACE.
   * @param val
   */
  public void putRead(Boolean val) {
    putA("read", val);
  }

  /**
   * Get Write flag of ACE.
   * @return
   */
  public Boolean getWrite() {
    return (Boolean)getA("write");
  }

  /**
   * Set Write flag for ACE.
   */
  public void putWrite(Boolean val) {
    putA("write", val);
  }

  /**
   * Get Freebusy flag of ACE.
   * @return
   */
  public Boolean getFreebusy() {
    return (Boolean)getA("freebusy");
  }

  /**
   * Set Freebusy flag for ACE.
   * @param val
   */
  public void putFreebusy(Boolean val) {
    putA("freebusy", val);
  }

  /**
   * Get Schedule flag of ACE.
   * @return
   */
  public Boolean getSchedule() {
    return (Boolean)getA("schedule");
  }

  /**
   * Set Schedule flag for ACE.
   * @param val
   */
  public void putSchedule(Boolean val) {
    putA("schedule", val);
  }

  /**
   * Creates a copy of the ACE.
   * @return
   * @throws CloneNotSupportedException
   */
  public Object clone() throws CloneNotSupportedException {
    ACEData c = new ACEData(this,false);
    c.putCuid(getCuid());
    c.putRead(getRead());
    c.putWrite(getWrite());
    c.putFreebusy(getFreebusy());
    c.putSchedule(getSchedule());
    return c;
  }

  public boolean equals(Object obj) {
    if ( obj == null )
      return false;
    if ( obj == this )
      return true;
    if ( ! ( obj instanceof ACEData))
      return false;

    ACEData other = (ACEData) obj;
    if( !getType().equals(other.getType()))
      return false;
    if( !getEntityType().equals( other.getEntityType()))
      return false;
    if( getID() != null ) {
        if (!getID().equals( other.getID()))
            return false;
    } else if (!getName().equals(other.getName()))
        return false;
    if( !getRead().equals( other.getRead()))
      return false;
    if( !getWrite().equals( other.getWrite()))
      return false;
    if( !getFreebusy().equals( other.getFreebusy()))
      return false;
    if( !getSchedule().equals( other.getSchedule()))
      return false;
    return true;
  }

  public static ACEData[] add
    (ACEData[] acl, ACEData ace) {
    // Check null
    if( acl == null || acl.length == 0)
      return new ACEData[]{ace};

    // check duplicate
    for (int i = 0; i < acl.length; i++)
      if( acl[i].equals(ace))
        return acl;

    ACEData[] newAcl = new ACEData[acl.length+1];
    for (int i = 0; i< acl.length; i++) {
      newAcl[i] = acl[i];
    }
    newAcl[newAcl.length-1] = ace;
    return newAcl;
  }
}
