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

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import net.unicon.academus.apps.addressbook.AddressBookBoRemote;
import net.unicon.academus.apps.addressbook.ContactData;
import net.unicon.academus.apps.addressbook.FolderData;
import net.unicon.academus.apps.rad.SQL;
import net.unicon.academus.apps.rad.XML;
import net.unicon.portal.channels.rad.Screen;


public class SPersonal {
  /**List of variables, used in processing on page.*/
  int m_rpp = CAddressBook.ROW_PER_PAGE;
  String m_openFolder = null;
  int m_nPages = 0;
  int m_curPage = -1;
  /**Contains results find out.*/
  ContactData[] m_contacts = null;//List of contact show on screen
  Vector m_contactAlls = null;//List of contact ContactData
  Vector m_folders = null;//List of folder FolderData
  Screen m_screen = null;
  AddressBookBoRemote m_bo;
  String m_userLogon;
  Hashtable m_contactCheck = new Hashtable();

  /**
   * Constructor of <code>SPersonal</code> object.
   */
  public SPersonal(Screen screen, String openFolder,AddressBookBoRemote bo, String userLogon) throws Exception {
    m_screen = screen;
    m_openFolder = CAddressBook.ALL_FOLDER;
    m_bo = bo;
    m_userLogon = userLogon;
  }
  /**
   * Init variable of object.
   * @param params Not use, only used for override.
   */
  public void init(Hashtable params) throws Exception {
    try {
      m_folders = m_bo.listFolders(m_userLogon);
    } catch(Exception e) {}
    m_openFolder = new String(CAddressBook.ALL_FOLDER);
    open(m_openFolder);
  }
  /**
   * Get xml data.
   * @param xml output data.
   */
  public void printXML(StringBuffer xml) throws Exception {
    if(m_folders!=null&&m_folders.size()>0)
      for(int i = 0; i < m_folders.size(); i++)
        ((FolderData)m_folders.elementAt(i)).printXML(xml);
    //List of Contact
    m_contacts = null;
    if( m_curPage >= 0 && m_curPage < m_nPages) {
      int start = m_curPage * m_rpp;
      int len = (start + m_rpp >= m_contactAlls.size())? m_contactAlls.size() - start:m_rpp;
      if( len > 0) {
        m_contacts = new ContactData[len];
        for( int i = 0; i < len; i++) {
          m_contacts[i] = (ContactData)m_contactAlls.elementAt(start+i);
          m_contacts[i].printXML(xml);
        }
      }
    }
    printSelected(xml);
  }
  /**
   * Open a folder.
   */
  public Screen open(Hashtable params) throws Exception {
    String folderID = (String)params.get("folderid");
    getSelected(params);
    if(folderID!=null)
      open(folderID);
    return m_screen;
  }
  /**
   * Go to next page.
   */
  public Screen next(Hashtable params) throws Exception {
    int p = Integer.parseInt((String)params.get("p"));
    getSelected(params);
    if(m_openFolder!=null)
      m_curPage = p + 1;
    return m_screen;
  }
  /**
   * Go to previous page.
   */
  public Screen prev(Hashtable params) throws Exception {
    int p = Integer.parseInt((String)params.get("p"));
    getSelected(params);
    if(m_openFolder!=null)
      m_curPage = p - 1;
    return m_screen;
  }
  /**
   * Init list of contacs of the specified folderID.
   * @param fdID the specified folderID.
   */
  void open( String fdID) throws Exception {
    m_openFolder = new String(fdID);
    m_nPages = 0;
    if(m_contactAlls!=null)
      m_contactAlls.clear();
    if(m_openFolder.equals(CAddressBook.ALL_FOLDER))
      m_contactAlls = m_bo.listAllContacts(m_userLogon);
    else
      m_contactAlls = m_bo.listContacts(m_userLogon,m_openFolder);
    int len;
    if(m_contactAlls==null)
      len = 0;
    else
      len = m_contactAlls.size();
    m_nPages = len / m_rpp;
    if( (len % m_rpp) != 0)
      m_nPages++;
    m_curPage = (m_nPages > 0)?0:-1;
  }
  /**
   * Get a list of contact is checked.
   * @param params input data.
   */
  void getSelected(Hashtable params) throws Exception {
    //Clear contact has choiced on screen
    if(m_contacts!=null) {
      //for(int i = 0; i < m_contacts.length ; i++)
        //m_contactCheck.remove(m_contacts[i].getID());

      // Set selected for ckecked items of submitted form
      for(Enumeration en = params.keys();en.hasMoreElements();) {
        String key = (String)en.nextElement();
        if( key.startsWith("contact")) {
          key = key.substring(7);//pass over "contact"
          for(int i = 0; i < m_contacts.length ; i++)
            if(m_contacts[i].getID().equals(key)) {
              m_contactCheck.put(key,m_contacts[i]);
              break;
            }
        }
      }
    }
  }

  void deselect(Hashtable params) throws Exception {
    for(Enumeration en = params.keys();en.hasMoreElements();) {
      String key = (String)en.nextElement();
      if( key.startsWith("_contact"))
        m_contactCheck.remove(key.substring(8));
    }
  }
  /**
   * Convert list of contact is checked to XML data.
   * <selected iid="" itype="P"/>
   * @param xml contains content of XML data.
   */
  private void printSelected(StringBuffer xml) throws Exception {
    if(m_contactCheck!=null&&m_contactCheck.size()>0) {
      for(Enumeration en = m_contactCheck.keys();en.hasMoreElements();) {
        String iid = (String)en.nextElement();
        ContactData id = (ContactData)m_contactCheck.get(iid);
        //xml.append("<selected iid='" + iid + "' iname='" + XML.esc(id.getName())+ "' itype='P'/>");
        xml.append("<selected iid='"+id.getID()+"' iname='"+ XML.esc(id.getName())+"' itype='"+id.getType()+"' ientity='"+ id.getEntityType()+ "'/>");
      }
    }
  }
}
