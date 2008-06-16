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

package net.unicon.portal.channels.addressbook;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Map;

import net.unicon.academus.apps.addressbook.AddressBookBoHome;
import net.unicon.academus.apps.addressbook.AddressBookBoRemote;
import net.unicon.academus.apps.addressbook.ContactData;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.portal.channels.rad.Channel;
import net.unicon.portal.channels.rad.Finder;
import net.unicon.portal.channels.rad.GroupData;

/**
 * It's class that is used for reference between sources.
 */
public class ContactFinder extends Finder {
  /**
   * Have or don't have to find in id.
   * @param id IdentityData.
   * @param attrs noop, use for overide operate.
   * @return  true  can find in id.
   *          false can't find in id.
   */
  public boolean canFind( IdentityData id, String[] attrs) {
    return ( id.getType().equals(GroupData.ENTITY) && id.getEntityType().equals(ContactData.S_CONTACT));
  }
  /**
   * Fill info into list of IdentityData.
   * @param ids   list of IdentityData.
   * @param attrs data is used to fill in ids.
   */
  public void fillDetails( IdentityData[] ids, String[] attrs) {
    for( int i = 0; i < ids.length; i++)
      fillDetail( ids[i],attrs);
  }
  /**
   * Fill info into the specify IdentityData.
   * @param id    IndentityData.
   * @attrs attrs data is used to fill in id.
   */
  public void fillDetail( IdentityData id, String[] attrs) {
    try {
      ContactData data = getBo().getContact(id.getID());

      if( data != null && attrs != null)
        for( int i = 0; i < attrs.length; i++)
          id.putA(attrs[i], data.getA(attrs[i]));
      else if( data != null && attrs == null) { // copy all attributes to output
        id.putA("business-phone", data.getBusinessPhone());
        id.putA("notes", data.getNotes());
        id.putA("home-address", data.getHomeAddress());
        id.putA("home-phone", data.getHomePhone());
        id.putA("fax", data.getFax());
        id.putA("office-address", data.getOfficeAddress());
        id.putA("cell-phone", data.getCellPhone());
        id.putA("title", data.getTitle());
        id.putA("company", data.getCompany());
        id.putA("department", data.getDepartment());
        id.putA("ownerid", data.getOwnerId());
      }
    } catch( Exception e) {}
  }

  //------------------------------------------------------------------------//
  // Find refferences
  /**
   * Can Ref.
   * @param org
   * @param id
   */
  public boolean canRef( IdentityData org, IdentityData id) {
    return ( id.getRef("contact") == null &&
             (id.getRefID("contact") != null || GroupData.isPortalUser(id)));
  }
  /**
   * Put Ref in list of IdentityData.
   * @param org
   * @param ids
   */
  public void putRef( IdentityData org, IdentityData[] ids) {
    for( int i = 0; i < ids.length; i++)
      putRef(org, ids[i]);
  }
  /**
   * Put Ref in the specify IdentityData.
   * @param org
   * @param id
   */
  public void putRef( IdentityData org, IdentityData id) {
    try {
      String cid = id.getRefID("contact");
      if( cid != null)
        id.putRef("contact", getBo().getContact(cid));
      else { // Find contact by logon
        String logon = id.getAlias();
        if( logon != null) {
          id.putRef("contact", getBo().getContactByRef("u:"+logon, org.getAlias()));
        }
      }
    } catch( Exception e) {
      Channel.log(e);
    }
  }

  //------------------------------------------------------------------------//
  /** Bo for processing */
  AddressBookBoRemote m_privateBo = null;
  /**
   * Get Bo for processing
   */
  AddressBookBoRemote getBo() throws Exception {
    if( m_privateBo == null)
      m_privateBo = ((AddressBookBoHome)Channel.ejbHome("AddressBookBo")).create();
    return m_privateBo;
  }
  /**
   * Finalize object.
   */
  protected void finalize() throws Throwable {
    if( m_privateBo != null)
      m_privateBo.remove();
  }
}
