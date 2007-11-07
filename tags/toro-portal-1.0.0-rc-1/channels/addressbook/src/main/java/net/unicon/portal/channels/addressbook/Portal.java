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
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import net.unicon.academus.apps.addressbook.ContactData;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.portal.channels.rad.GroupData;
import net.unicon.portal.channels.rad.Screen;
import net.unicon.portal.channels.rad.Finder;

/**
 * Is is Portal screen in addressbook project.It is allow you browse
 * to portal user.
 */
public class Portal extends AddressBookScreen {
  /* Override from Screen in rad. */
  static public final String SID = "Portal";
  // group tree parameters
  Class m_etype = null;
  // others
  String m_openGroup = null;//entities of openGroup are displayed
  int m_nPages = 0;
  int m_curPage = -1;
  int m_rpp = CAddressBook.ROW_PER_PAGE; // rows per page
  // Group tree
  XMLData m_xml = null;
  GroupData m_root = null;
  XMLData m_entities = new XMLData();
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
    // Use when goURL from xsl
    if( params.remove("skip") == null) {
      String etype = (String)params.remove("etype");
      if( etype != null)
        m_etype = GroupData.lookupClass(etype);
      if( m_etype == null)
        m_etype = GroupData.C_USER;
    }

    // Init group tree
    m_root = new GroupData(m_etype, false);//true); Tien 1016
    m_xml = m_root.xml();

    // open root group
    m_rpp = countExpanded( m_root);
    m_xml.putE("entities", m_entities);
    open(m_root.getID());
  }
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public Hashtable getXSLTParams() {
    Hashtable params = new Hashtable();

    params.put("openGroup", m_openGroup);
    params.put("nPages", new Integer(m_nPages));
    params.put("curPage", new Integer(m_curPage));
    return params;
  }
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public String buildXML() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    ps.println("<?xml version='1.0'?>");
    ps.println("<addressbook-system>");
    m_xml.print(ps, 1);
    ps.println("</addressbook-system>");
    return baos.toString();
  }
  /**
   *  Override from <code>AddressBookScreen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public void printXML(StringBuffer xml) throws Exception { }
  /**
   * Expand one node on tree.
   */
  public Screen expand(Hashtable params) throws Exception {
    //<a href="{$doURL}=expand&amp;key={@iid}">
    //m_xml.putE("expanded", m_expanded);
    String key = (String)params.get("key");
    if( key != null) {
      GroupData gr = m_root.getGroup( key);
      if( gr != null) {
        // expand group
        GroupData[] ch = gr.getChildGroups();
        if( ch != null)
          for( int i = 0; i < ch.length; i++)
            ch[i].initChildren();

        // save state
        String[] old = (String[])m_xml.getE("expanded");
        int len = (old==null)?0:old.length;
        String[] n = new String[len+1];
        if( old != null)
          for( int i = 0; i < old.length; i++)
            n[i] = old[i];
        n[len] = key;
        m_xml.putE("expanded", n);
        // Update page info
        updatePage();
      }
    }
    return this;
  }
  /**
   * Collapse a node on tree.
   */
  public Screen collapse(Hashtable params) throws Exception {
    //<a href="{$doURL}=collapse&amp;key={@iid}">
    String key = (String)params.get("key");
    if( key != null) {
      String[] old = (String[])m_xml.getE("expanded");
      if( old != null && old.length > 0) {
        String[] n = new String[old.length-1];
        for( int i = 0, j = 0; i < old.length && j < old.length - 1; i++)
          if( !old[i].equals(key))
            n[j++] = old[i];
        m_xml.putE("expanded", n);
      }
      // Update page info
      updatePage();
    }
    return this;
  }
  /**
   * Open a node on tree.
   */
  public Screen open(Hashtable params) throws Exception {
    //<a class='uportal-navigation-channel' href="{$doURL}=open&amp;key={@iid}">
    String key = (String)params.get("key");
    if( key != null)
      open(key);
    return this;
  }
  /**
   * Go to the previous page.
   */
  public Screen prev(Hashtable params) throws Exception {
    GroupData gr = m_root.getGroup( m_openGroup);
    if( gr != null)
      setCurPage( gr, m_curPage - 1);

    return this;
  }
  /**
   * Go to the next page.
   */
  public Screen next(Hashtable params) throws Exception {
    GroupData gr = m_root.getGroup( m_openGroup);
    if( gr != null)
      setCurPage( gr, m_curPage + 1);

    return this;
  }
  /**
   * Go to contact screen for creating a new contact.
   */
  public Screen copyContact(Hashtable params) throws Exception {
    ContactData ct = null;
    String contactid = (String)params.get("contactid"); //id of portal ( not alias)

    // Get IdentityData with getID == contactid
    IdentityData data = null;
    IdentityData[] entities = (IdentityData[])m_entities.getE("entity");
    if( entities != null)
      for( int i = 0; i < entities.length; i++) {
        if( entities[i].getID().equals(contactid)) {
          data = entities[i];
          break;
        }
      }
    // Check
    if( data == null) { // Should not go there
      log("Portal.copyContact: can not find with contactid="+contactid);
      return this;
    }

    String ref = "u:" + data.getAlias();
    ct = getBo().getContactByRef(ref , getUserLogon());
    if(ct==null) {
      Finder.findRefferences(m_channel.logonUser(),data);
      ref = "p:" + data.getRefID("campus");
      ct = getBo().getContactByRef(ref , getUserLogon());
    }
    // go to screen Contact
    Contact screen = (Contact)m_channel.makeScreen("Contact");
    screen.m_back = sid();
    screen.init(params);
    screen.m_methodName = null;
    if(ct == null) {
      ct = new ContactData();
      if(data.getName()==null)
        ct.putName(data.getAlias());
      else
        ct.putName(data.getName());
      ct.putEmail(data.getEmail());
      ct.putBusinessPhone((String)data.getA("telephoneNumber"));
      ct.putRefID("portal", data.getAlias());
      screen.m_isNew = true;
    } else {
      screen.m_isNew = false;
      screen.getFolderOfContact(ct.getID());
    }
    screen.m_ct = ct;
    return screen;
  }
  /**
   *
   */
  void open( String grID) throws Exception {
    // get group data associated with this id
    GroupData gr = m_root.getGroup( grID);
    m_openGroup = grID;
    m_entities.removeA("group");
    if( gr != null) {
      // Save open group id
      m_entities.putA("group", grID);

      // get all entities
      IdentityData[] children = gr.getChildEntities();
      // Count page
      int length = 0;
      if( children != null)
        length = length + children.length;

      m_nPages = 0;
      if( length >0 ) {
        m_nPages = length / m_rpp;
        if( (length % m_rpp) != 0)
          m_nPages++;
      }

      // current page
      setCurPage( gr, (m_nPages > 0)?0:-1);
    }
  }
  /**
   *
   */
  void setCurPage( GroupData gr, int curPage) throws Exception {
    m_curPage = curPage;
    IdentityData[] entities = null;

    if( curPage >= 0 && curPage < m_nPages) {
      Vector ventities = new Vector();
      IdentityData[] children = gr.getChildEntities();
      int start = m_curPage * m_rpp;
      int count = m_rpp;

      for(int k = start; k < children.length && count > 0; k++ ,count--)
        ventities.addElement(children[k]);

      //Find details Tien 1011
      entities = (IdentityData[])ventities.toArray(new IdentityData[0]);
      Finder.findDetails(entities,null);
    }

    m_entities.putE("entity", entities);
  }
  /**
   *
   */
  int countExpanded( GroupData gr) throws Exception {
    // This
    int ret = 1;

    // check expanded
    if( isExpanded( gr.getID())) {
      GroupData[] ch = gr.getChildGroups();
      if( ch != null)
        for( int j = 0; j < ch.length; j++)
          ret += countExpanded( ch[j]);
    }

    return ret;
  }
  /**
   *
   */
  boolean isExpanded( String key) {
    String[] expanded = (String[])m_xml.getE("expanded");
    if( expanded == null)
      return false;
    for( int i = 0; i < expanded.length; i++)
      if( expanded[i].equals( key))
        return true;
    return false;
  }
  /**
   *
   */
  void updatePage() throws Exception {
    m_rpp = countExpanded( m_root);
    if(m_rpp < 2 * CAddressBook.ROW_PER_PAGE) {
      m_rpp = 2 * CAddressBook.ROW_PER_PAGE;
    }
    if( m_openGroup != null)
      open( m_openGroup);
  }
}
