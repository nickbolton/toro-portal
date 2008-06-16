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
import java.util.Enumeration;

import net.unicon.academus.apps.addressbook.AddressBookBoRemote;
import net.unicon.academus.apps.addressbook.ContactData;
import net.unicon.academus.apps.campus.CampusData;
import net.unicon.academus.apps.campus.Ldap;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.Sorted;
import net.unicon.academus.apps.rad.XML;
import net.unicon.portal.channels.rad.Screen;
import net.unicon.portal.channels.rad.Channel;
import net.unicon.portal.channels.rad.GroupData;
import net.unicon.portal.channels.rad.IdentityComparator;
import net.unicon.portal.channels.rad.IdentityDataSearcher;


/**
 * This is a screen in Select screen. It allow you search contact.
 */
public class SSearch {
  private static boolean useIdentityDataSearcher = true;

  static {
    useIdentityDataSearcher = "true".equalsIgnoreCase(Channel.getRADProperty("addressbook.useIdentityDataSearcher"));
  }

  boolean m_portal = true;
  boolean m_campus = false;
  boolean m_contact = false;

  /**list of variables, used in processing on page.*/
  int m_nPages = 0;
  int m_curPage = -1;
  int m_start = 0;
  int m_end = 0;
  int m_sum = 0;
  int m_rpp = CAddressBook.ROW_PER_PAGE; // rows per page

  /**Info for Search.*/
  String m_name = null;
  String m_title = null;
  String m_department = null;
  String m_email = null;

  /**Contains results find out.*/
  IdentityData[] m_contacts =null;
  Vector m_vnonPortalCam = null;
  Vector m_vPortal = null;
  Vector m_vnonPortalCon = null;
  String m_result = "";
  Screen m_screen = null;
  AddressBookBoRemote m_bo;
  String m_userLogon;
  Hashtable m_contactCheck = new Hashtable();
  int m_countLimit = 50;
  Sorted m_vresult = null;
  /**
   * Constructor of <code>SSearch</code> object.
   */
  public SSearch(Screen screen, AddressBookBoRemote bo, String userLogon) throws Exception {
    m_screen = screen;
    m_bo = bo;
    m_userLogon = userLogon;
  }

  /**
   * Init variable of object.
   * @param params Not use, only used for override.
   */
  public void init(Hashtable params) throws Exception {
    m_name = "";
    m_title = "";
    m_department = "";
    m_email = "";

    m_vnonPortalCam = new Vector();
    m_vnonPortalCon = new Vector();
    m_vPortal = new Vector();
  }
  /**
   * Get xml data.
   * @param xml output data.
   */
  public void printXML(StringBuffer xml) throws Exception {
    // VU - begin (5/21/2003)	 
	  
//    System.out.println("portal [ " + m_portal + " ]");
//    System.out.println("contact [ " + m_contact + " ]");
//    System.out.println("campus [ " + m_campus + " ]");

    String value = null;
    xml.append("<search");
    
    xml.append(" portal=");
    value = m_portal ? "\"y\"" : "\"n\"";
    xml.append(value);
    
    xml.append(" contact=");
    value = m_contact ? "\"y\"" : "\"n\"";
    xml.append(value);
    
    xml.append(" campus=");
    value = m_campus ? "\"y\"" : "\"n\"";
    xml.append(value);
    
    xml.append(" />");    
    // VU - end (5/21/2003)
    xml.append(m_result);
    printSelected(xml);
  }
  /**
   * Process for search button.
   */
  public Screen search(Hashtable params) throws Exception {
    //recieve data from form
    getForm(params);

    //John Bodily 9/2/2004 - It's not necessary to clean out the "selected" users here..
    //we're performing the search to add to this collection not start it from scratch.
    //m_contactCheck.clear();
    //search

    m_vnonPortalCam.clear();
    if (m_campus) {
      m_vnonPortalCam = Ldap.searchCampusContacts(m_name,m_email,m_title,m_department,m_countLimit);
    }
    
    m_vnonPortalCon.clear();
    if (m_contact) {
      m_vnonPortalCon = m_bo.searchContacts(m_userLogon,m_name,m_email,m_title,m_department,m_countLimit);
    }
    
    m_vPortal.clear();
    if(m_portal) {
      if (useIdentityDataSearcher) {
        m_vPortal = IdentityDataSearcher.portalSearch(m_name, m_title, m_department, m_email, m_countLimit);
      } else {
        m_vPortal = GroupData.search(GroupData.S_USER, new SearchFilter(m_name,m_title,m_department,m_email) , GroupData.FILTER_ALL , new IdentityComparator(),m_countLimit, true);
      }
    }
    
    Search.doFilter(m_vnonPortalCon,m_vnonPortalCam,m_vPortal);

    //encodeID(m_vnonPortalCon);
    //encodeID(m_vnonPortalCam);
    //encodeID(m_vPortal);

    m_vresult = new Sorted(new IdentityComparator());
    m_vresult.addAll(m_vnonPortalCon);
    m_vresult.addAll(m_vnonPortalCam);
    m_vresult.addAll(m_vPortal);

    //update page
    if(m_vresult.size()>m_countLimit) {
      m_vresult.setSize(m_countLimit);
      m_sum = m_countLimit;
    } else
      m_sum = m_vresult.size();
    m_nPages = 0;
    if( m_sum >0 ) {
      m_nPages = m_sum / m_rpp;
      if( (m_sum % m_rpp) != 0)
        m_nPages++;
    }
    m_result = setCurPage((m_nPages > 0)?0:-1);
    return m_screen;
  }
  /**
   * Encode list of ids.
   * @param v list of ids need to encode.
   */
  //private void encodeID(Vector v) {
  //  if(v!=null&&v.size()>0)
  //    for(int i = 0 ; i < v.size() ; i++) {
  //      IdentityData data = (IdentityData)v.elementAt(i);
  //     data.putOID(data.getIdentifier());
  //    }
  //}
  /**
   * Decode list of ids.
   */
  //public void decodeID() {
  //  for(Enumeration enum = m_contactCheck.keys(); enum.hasMoreElements();) {
  //    Object key = enum.nextElement();
  //    IdentityData value = (IdentityData)m_contactCheck.get(key);
  //    IdentityData realkey = new IdentityData((String)key);
  //    value.putOID(realkey.getID());
  //  }
  //}
  /**
   * Go to next page.
   */
  public Screen next(Hashtable params) throws Exception {
    int p = Integer.parseInt((String)params.get("p"));
    getSelected(params);
    m_result = setCurPage( p + 1);
    return m_screen;
  }
  /**
   * Go to previous page.
   */
  public Screen prev(Hashtable params) throws Exception {
    int p = Integer.parseInt((String)params.get("p"));
    getSelected(params);
    m_result = setCurPage( p - 1);
    return m_screen;
  }
  /**
   * Get data from form input.
   * @param params input data.
   */
  private void getForm(Hashtable params) {
    m_countLimit = Integer.parseInt((String)params.get("max-results"));
    m_name = ((String)params.get("name")).trim();
    m_title = ((String)params.get("title")).trim();
    m_department = ((String)params.get("department")).trim();
    m_email = ((String)params.get("email")).trim();
    m_portal = (params.get("portal") != null) ? true : false;
    m_campus = (params.get("campus") != null) ? true : false;
    m_contact = (params.get("contact") != null) ? true : false;
  }
  /**
   * Get list of contact in the specified page.
   * @param curPage the specified page.
   * @return Get list of contact in the specified page as <code>String</code>.
   */
  String setCurPage( int curPage) throws Exception {
    m_curPage = curPage;
    m_contacts = null;
    StringBuffer xml = new StringBuffer();
    Vector vContacts = new Vector();
    if( curPage >= 0 && curPage < m_nPages) {
      int start = m_curPage * m_rpp;
      int end = start + m_rpp;
      if( end > m_vresult.size())
        end = m_vresult.size();
      m_start = start;
      m_end = end;
      for(int i = start ; i < end; i++) {
        Object idDataObj = m_vresult.get(i);
        if(idDataObj instanceof CampusData)
          ((CampusData)idDataObj).printXML(xml);
        else if(idDataObj instanceof ContactData)
          ((ContactData)idDataObj).printXML(xml);
        else if(idDataObj instanceof IdentityData)
          xml.append(Search.printPortalXML((IdentityData)idDataObj));
        vContacts.addElement((IdentityData)idDataObj);
      }
      m_contacts = (IdentityData[])vContacts.toArray(new IdentityData[0]);
    }
    return xml.toString();
  }
  /**
   * Get a list of contact is checked.
   * @param params input data.
   */
  void getSelected(Hashtable params) throws Exception {
    if(m_contacts!=null) {
      //clear
      //for(int i = 0; i < m_contacts.length ; i++)
        //m_contactCheck.remove(((IdentityData)m_contacts[i]).getID());
      //add
      for(Enumeration en = params.keys();en.hasMoreElements();) {
        String key = (String)en.nextElement();//start with: contact, campus, group, entity
        for(int i = 0; i < m_contacts.length ; i++) {
          String idKey = getUIKey(m_contacts[i]);
          if( idKey.equals(key))
            m_contactCheck.put(key,m_contacts[i]);
        }
      }
    }
  }

  void deselect(Hashtable params) throws Exception {
    if(m_contacts!=null) {
      for(Enumeration en = params.keys();en.hasMoreElements();) {
        String key = (String)en.nextElement();//start with: contact, campus, group, entity
        if( key.startsWith("_"))
          m_contactCheck.remove(key.substring(1));
      }
    }
  }

  String getUIKey(IdentityData id) {
    String etype = id.getEntityType();
    String prefix = etype.equals(CampusData.S_CAMPUS)?"campus": etype.equals(ContactData.S_CONTACT)?"contact":
             id.getType().equals(id.GROUP)?"group":"entity";
    return prefix + id.getID();
  }

  /**
   * Convert list of contact is checked to XML data.
   * <selected iid="" itype="P"/>
   * @param xml contains content of XML data.
   */
  void printSelected(StringBuffer xml) throws Exception {
    if(m_contactCheck!=null&&m_contactCheck.size()>0) {
      for(Enumeration en = m_contactCheck.keys();en.hasMoreElements();) {
        IdentityData id = (IdentityData)m_contactCheck.get((String)en.nextElement());
        xml.append("<selected iid='"+id.getID()+"' iname='"+ XML.esc(id.getName())+"' itype='"+id.getType()+"' ientity='"+ id.getEntityType()+ "'/>");
      }
    }
  }
}
