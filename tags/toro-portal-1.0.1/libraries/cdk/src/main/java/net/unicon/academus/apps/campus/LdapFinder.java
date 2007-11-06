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

package net.unicon.academus.apps.campus;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Map;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.portal.channels.rad.Finder;
import net.unicon.portal.channels.rad.Channel;//For loggin only
import net.unicon.portal.channels.rad.GroupData;

public class LdapFinder extends Finder {
  Map m_caches = new HashMap();

  public boolean canFind( IdentityData id, String[] attrs) {
    return ( id.getType().equals(GroupData.ENTITY) && id.getEntityType().equals(CampusData.S_CAMPUS));
  }

  public void fillDetails( IdentityData[] ids, String[] attrs) {
    for( int i = 0; i < ids.length; i++)
      fillDetail( ids[i],attrs);
  }

  public void fillDetail( IdentityData id, String[] attrs) {
    // Check caches
    String key = id.getID();
    CampusData data = (CampusData)m_caches.get(key);
    try {
      if( data == null) { // Get from Ldap server
        data = Ldap.getCampusContact(key);
        if( data != null)
          m_caches.put( key, data);
      }

      if( data != null && attrs != null)
        for( int i = 0; i < attrs.length; i++)
          id.putA(attrs[i], data.getA(attrs[i]));
      else if( data != null && attrs == null) { // copy all attributes to output
        id.putAlias( data.getAlias());
        id.putA("telephonenumber", data.getTelephoneNumber());
        id.putA("department", data.getDepartment());
        id.putA("title", data.getTitle());
        id.putRefID("portal", data.getRefID("portal"));
      }
    } catch( Exception e) {}
  }

  //--------------------------------------------------------------------//
  // Find refferences
  public boolean canRef( IdentityData org, IdentityData id) {
    return ( id.getRef("campus") == null &&
             (id.getRefID("campus") != null || GroupData.isPortalUser(id)));
  }

  public void putRef( IdentityData org, IdentityData[] ids) {
    for( int i = 0; i < ids.length; i++)
      putRef(org, ids[i]);
  }

  public void putRef( IdentityData org, IdentityData id) {
    try {
      String uid = id.getRefID("campus");
      CampusData campus = ( uid != null?
                            Ldap.getCampusContact(uid):
                            Ldap.getCampusFromPortal(id.getAlias()));
      id.putRef("campus", campus);
      if( campus != null)
        id.putRefID("campus", campus.getID());
    } catch( Exception e) {
      Channel.log(e);
    }
  }
}
