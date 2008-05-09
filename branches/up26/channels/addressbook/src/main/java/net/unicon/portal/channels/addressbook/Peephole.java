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

import java.util.Hashtable;
import java.util.Vector;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.portal.channels.rad.Screen;

/**
 * It is main screen in addressbook project. It allow you browse
 * to personal, find, portal screen.
 */
public class Peephole extends AddressBookScreen {
  /* Override from Screen in rad. */
  static public final String SID = "Peephole";
  String m_criteria = null;


  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public String sid() {
    return SID;
  }
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public void init(Hashtable params) throws Exception {
    m_criteria = "";
  }
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public Hashtable getXSLTParams() {
    Hashtable params = new Hashtable();
    params.put("criteria",m_criteria);
    return params;
  }

  public Screen testSelect(Hashtable params) throws Exception {
    return select( "portal,campus,contact", null, "selection", params);
  }

  public Screen selection(Hashtable params) throws Exception {
    IdentityData[] sels = (IdentityData[]) params.get("selected");
    return this;
  }

  /**
   *  Override from <code>AddressBookScreen</code>.
   *  @see net.unicon.portal.channels.addressbook.AddressBookScreen.
   */
  public void printXML(StringBuffer xml) throws Exception {}
  /**
   * Go to search screen.
   */
  public Screen search(Hashtable params) throws Exception {

//    System.out.println("AB.Peephole.search:"+params);

    //    m_criteria = ((String)params.get("criteria")).trim();
    Search screen = (Search)m_channel.makeScreen("Search");
    params.put("from",sid());
    screen.init(params);

    params.put("Portal","true");
    params.put("Campus","true");
    params.put("Personal","true");

    if (m_criteria != null && !m_criteria.equals("")) {
      params.put("name",m_criteria);
      m_criteria = "";
      screen.search(params);
    }
    return screen;
  }
}
