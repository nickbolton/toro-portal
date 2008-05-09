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

package net.unicon.portal.channels.rad;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import net.unicon.academus.apps.rad.IdentityData;

/**
 * Finder is used to find all the informations and all the refferences of several IdentityData objects.
 * The last one can be portal users/groups or campus users which have the information in the LDAP server or
 * any contacts from personal address book. The finder has a name and is registered as RAD property with the name,
 * started by "finder." and follows the name of finder. The value of this property is the name of finder class. Ex.:
 *  finder.contact=net.unicon.portal.channels.addressbook.ContactFinder
 *  finder.portal=net.unicon.portal.channels.rad.PortalFinder
 *  finder.ldap=net.unicon.academus.apps.campus.LdapFinder
 *
 * Normally each finder will find informations in the specific person source.
 */
abstract public class Finder {
  //----- Find information -----
  /**
   * Check whether the finder can find for given IdentityData.
   * @param id The IdentityData to check
   * @param attrs The attribtute names to find.
   * @return true if the Finder can do, else - false.
   */
  abstract public boolean canFind( IdentityData id, String[] attrs);

  /**
   * Fill the detail of given IdentityData.
   * @param id The IdentityData to fill detail
   * @param attrs What attributes to fill. If null - all the attributes finder can find.
   */
  abstract public void fillDetail( IdentityData id, String[] attrs);

  /**
   * Fill the details for a set of IdentityData.
   * @param ids The array of IdentityData to fill detail
   * @param attrs What attributes to fill. If null - all the attributes finder can find.
   */
  abstract public void fillDetails( IdentityData[] ids, String[] attrs);

  //--------- Find refferences----
  /**
   * Check whether the IdentityData can belong to this Finder.
   */
  abstract public boolean canRef( IdentityData org, IdentityData id);

  /**
   * For each element in the array ids, the finder will find its reference and put it in that element.
   * @param org The logon user.
   * @param ids The array of IdentityData to get refference for.
   */
  abstract public void putRef( IdentityData org, IdentityData[] ids);

  /**
   * Find and put the found refference to given IdentityData (with key name is the name of finder)
   * @param org The logon user.
   * @param id The IdentityData to get refference for.
   */
  abstract public void putRef( IdentityData org, IdentityData id);

  //--------- Static usage --------------------------------//

  /**
   * All the finders.
   */
  protected static Hashtable m_finders = new Hashtable();

  /**
   * Get the Finder with the given name.
   * @param name The name of finder.
   */
  public static Finder getFinder( String name) {
    return (Finder)m_finders.get(name);
  }

  /**
   * Find the details for a set of IdentityData.
   * @param ids The array of IdentityData to find detail.
   * @param attrs What attributes to find. If null - all the attributes finder can find.
   */
  public static void findDetails( IdentityData[] ids, String[] attrs) {
    //------ Classify first -------//
    Hashtable activeFinders = new Hashtable();//map finder <--> Vector
    for( int i = 0; i < ids.length; i++) {
      Vector fv = getFinder(ids[i], attrs);
      for( int j = 0; j < fv.size(); j++) {
        Finder finder = (Finder)fv.elementAt(j);
        Vector v = (Vector)activeFinders.get(finder);
        if( v == null) {
          v = new Vector();
          activeFinders.put(finder,v);
        }
        v.addElement( new Integer(i));
      }
    }

    //--- Now fill details ------//
    Enumeration e = activeFinders.keys();
    while( e.hasMoreElements()) {
      Finder finder = (Finder)e.nextElement();

      // Create input data for finder
      Vector v = (Vector)activeFinders.get(finder);
      IdentityData[] in = new IdentityData[v.size()];
      for( int i = 0; i < v.size(); i++)
        in[i] = ids[ ((Integer)v.elementAt(i)).intValue()];

      // Fill detail of attributes
      finder.fillDetails(in, attrs);
    }
  }

  /**
   * Find the details for an IdentityData.
   * @param id The IdentityData to find.
   * @param attrs What attributes to find. If null - all the attributes finder can find.
   */
  public static void findDetail( IdentityData id, String[] attrs) {
    Vector fv = getFinder(id, attrs);
    for( int j = 0; j < fv.size(); j++) {
      Finder finder = (Finder)fv.elementAt(j);
      finder.fillDetail(id, attrs);
    }
  }

  //----------------------------------------------------------------------//

  /**
   * For each element in the array ids, the finder will find its refferences
   * (in the person source specific for this finder) and put them in that element
   * with the key name is the name of finder.
   * @param org The logon user.
   * @param ids The array of IdentityData to find refferences for.
   */
  public static void findRefferences( IdentityData org, IdentityData[] ids) {
    //------ Classify first -------//
    Hashtable activeFinders = new Hashtable();//map finder <--> Vector
    for( int i = 0; i < ids.length; i++) {
      Vector fv = getRefFinder(org, ids[i]);
      for( int j = 0; j < fv.size(); j++) {
        Finder finder = (Finder)fv.elementAt(j);
        Vector v = (Vector)activeFinders.get(finder);
        if( v == null) {
          v = new Vector();
          activeFinders.put(finder,v);
        }
        v.addElement( new Integer(i));
      }
    }

    //--- Now put the refferences ------//
    Enumeration e = activeFinders.keys();
    while( e.hasMoreElements()) {
      Finder finder = (Finder)e.nextElement();

      // Create input data for finder
      Vector v = (Vector)activeFinders.get(finder);
      IdentityData[] in = new IdentityData[v.size()];
      for( int i = 0; i < v.size(); i++)
        in[i] = ids[ ((Integer)v.elementAt(i)).intValue()];

      // put the refferences...
      finder.putRef(org, in);
    }
  }

  /**
   * The finder will find the refferences for given IdentityData
   * (in the person source specific for this finder) and put them in that element
   * with the key name is the name of finder.
   * @param org The logon user.
   * @param id The IdentityData to find refferences for.
   */
  public static void findRefferences( IdentityData org, IdentityData id) {
    Vector fv = getRefFinder(org, id);
    for( int j = 0; j < fv.size(); j++) {
      Finder finder = (Finder)fv.elementAt(j);
      finder.putRef(org,id);
    }
  }

  //-------------------------------------------------------//

  /**
   * Get the email of IdentityData, looking for all its refferences.
   * @param id The IdentityData to get email.
   * @return the email address.
   */
  public static String getEmail( IdentityData id) {
    String email = null;

    // portal user
    if( GroupData.isPortalUser(id)) {
      // Personal data
      IdentityData personal = id.getRef("contact");
      if( personal != null)
        email = personal.getEmail();

      // from Ldap data
      if( email == null) {
        IdentityData campus = id.getRef("campus");
        if( campus != null)
          email = campus.getEmail();
      }
    }

    return (email != null? email: id.getEmail());
  }

  /**
   * Get the name of given IdentityData.
   * @param id The IdentityData to get name
   * @return the name of given IdentityData
   */
  public static String getName( IdentityData id) {
    String name = null;

    // portal user
    if( GroupData.isPortalUser(id)) {
      // from Ldap data
      IdentityData campus = id.getRef("campus");
      if( campus != null)
        name = campus.getName();
    }

    return (name != null? name: id.getName());
  }


  //-------------------------------------------------------//
  /**
   * @return Vector of Finder
   */
  static Vector getFinder(IdentityData id, String[] attrs) {
    Vector v = new Vector();
    Enumeration e = m_finders.keys();
    while( e.hasMoreElements()) {
      Finder finder = (Finder)m_finders.get( (String)e.nextElement());
      if( finder.canFind(id, attrs))
        v.addElement(finder);
    }
    return v;
  }

  /**
   * @return Vector of Finder
   */
  static Vector getRefFinder(IdentityData org, IdentityData id) {
    Vector v = new Vector();
    Enumeration e = m_finders.keys();
    while( e.hasMoreElements()) {
      Finder finder = (Finder)m_finders.get( (String)e.nextElement());
      if( finder.canRef(org, id))
        v.addElement(finder);
    }
    return v;
  }
}
