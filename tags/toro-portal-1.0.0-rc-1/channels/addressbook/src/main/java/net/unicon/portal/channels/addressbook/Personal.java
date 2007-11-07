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

import net.unicon.academus.apps.addressbook.ContactData;
import net.unicon.academus.apps.addressbook.FolderData;
import net.unicon.portal.channels.rad.Screen;


/**
 * Is is Personal screen in addressbook project.It is allow you browse
 * to find, contact, folder and it lists all of folders and contact of user.
 */
public class Personal extends AddressBookScreen {
  /* Override from Screen in rad. */
  static public final String SID = "Personal";
  /* Number of records on a page */
  int m_rpp = CAddressBook.ROW_PER_PAGE;
  /* Folder is openning. */
  String m_openFolder = null;
  /* sum of page */
  int m_nPages = 0;
  /* current page */
  int m_curPage = -1;
  /* List of ContactData on right. */
  Vector m_contacts = null;
  /* List of folder FolderData on left. */
  Vector m_folders = null;
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
    try {
      m_folders = getBo().listFolders(this.getUserLogon());
    } catch(Exception e) {}
    //Check opening folder is exists
    if(m_openFolder==null||!getBo().folderExistId(m_openFolder))
      m_openFolder = CAddressBook.ALL_FOLDER;
    open(m_openFolder);
  }

  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public void reinit(Hashtable params) throws Exception {
    String where = (String)params.get("where");
    if(where==null) {
      //return;
      init(params);
    } else if(where.equals(Portal.SID))
      open(m_openFolder);
    else if(where.equals(Search.SID))
      init(params);
  }

  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public Hashtable getXSLTParams() {
    Hashtable params = new Hashtable();
    params.put("nPages",new Integer(m_nPages));
    params.put("curPage",new Integer(m_curPage));
    params.put("openFolder",m_openFolder);

    return params;
  }

  /**
   *  Override from <code>AddressBookScreen</code>.
   *  @see net.unicon.portal.channels.addressbook.AddressBookScreen.
   */
  public void printXML(StringBuffer xml) throws Exception {
    //List of Folder
    if(m_folders!=null&&m_folders.size()>0)
      for(int i = 0; i < m_folders.size(); i++)
        ((FolderData)m_folders.elementAt(i)).printXML(xml);
    //List of Contact
    if( m_curPage >= 0 && m_curPage < m_nPages) {
      int start = m_curPage * m_rpp;
      int len = (start + m_rpp >= m_contacts.size())? m_contacts.size() - start:m_rpp;
      if( len > 0)
        for( int i = 0; i < len; i++)
          ((ContactData)m_contacts.elementAt(start+i)).printXML(xml);
    }
  }
  /**
   * Open folder.
   * @param params Noop at here. It is used by framework.
   */
  public Screen open(Hashtable params) throws Exception {
    String folderID = (String)params.get("folderid");
    if(folderID!=null)
      open(folderID);
    return this;
  }
  /**
   * Go to contact screen for editting contact.
   * @param params Noop at here. It is used by framework.
   */
  public Screen editContact(Hashtable params) throws Exception {
    String contactid = (String)params.get("contactid");

    Contact screen = (Contact)m_channel.makeScreen("Contact");
    screen.m_isNew = false;
    screen.m_back = sid();
    screen.m_ct = getBo().getContact(contactid);
    screen.init(params);
    screen.m_methodName = "refreshContactOnScreen";
    screen.getFolderOfContact(contactid);

    return screen;
  }
  /**
   * Delete contact.
   * @param params Noop at here. It is used by framework.
   */
  public Screen deleteContact(Hashtable params) throws Exception {
    if(m_openFolder.equals(CAddressBook.ALL_FOLDER))
      getBo().deleteContact(null,(String)params.get("contactid"));
    else
      getBo().deleteContact(m_openFolder,(String)params.get("contactid"));
    return refreshContactOnScreen(params);
  }
  /**
   * Confirm before delete contact.
   * @param params Noop at here. It is used by framework.
   */
  public Screen confirmDelContact(Hashtable params) throws Exception {
    String contactid = (String)params.get("contactid");
    String[] mfps = new String[1];
    for(int i = 0 ; i < m_contacts.size(); i++)
      if(((ContactData)m_contacts.elementAt(i)).getID().equals(contactid)) {
        mfps[0] = ((ContactData)m_contacts.elementAt(i)).getName();
        break;
      }
    params.put("methodName","deleteContact");
    params.put("back", sid());

    DeleteConfirm confirm = (DeleteConfirm) super.makeScreen("DeleteConfirm");
    confirm.init(params);
    return confirm;
//    return confirm(CAddressBook.CONFIRM_DELETE_CONTACT,mfps,params,sid());
  }
  /**
   * Go to contact screen for creating a new contact.
   * @param params Noop at here. It is used by framework.
   */
  public Screen newContact(Hashtable params) throws Exception {
    Contact screen = (Contact)m_channel.makeScreen("Contact");
    screen.m_isNew = true;
    screen.m_back = sid();
    screen.init(params);
    screen.m_methodName = "refreshContactOnScreen";

    return screen;
  }
  /**
   * Go to folder screen for editting folder.
   * @param params Noop at here. It is used by framework.
   */
  public Screen editFolder(Hashtable params) throws Exception {
    String folderid = (String)params.get("folderid");

    Folder screen = (Folder)m_channel.makeScreen("Folder");
    screen.m_isNew = false;
    screen.m_back = sid();
    screen.m_folder = getBo().getFolder(folderid);
    screen.m_methodName = "refreshFolderOnScreen";

    return screen;
  }
  /**
   * Delete Folder.
   * @param params Noop at here. It is used by framework.
   */
  public Screen deleteFolder(Hashtable params) throws Exception {
    boolean ret = getBo().deleteFolder((String)params.get("folderid"));
    if(ret==false)
      return info(getLocalText(CAddressBook.ERROR_DELETE_FOLDER,CAddressBook.class),sid(), true);
    try {
      m_folders.clear();
      m_folders = getBo().listFolders(this.getUserLogon());
    } catch(Exception e) {}

    m_openFolder = CAddressBook.ALL_FOLDER;
    open(m_openFolder);
    return this;
  }
  /**
   * Confirm before deleting folder.
   * @param params Noop at here. It is used by framework.
   */
  public Screen confirmDelFolder(Hashtable params) throws Exception {
    String folderid = (String)params.get("folderid");
    String[] mfps = new String[1];
    for(int i = 0 ; i < m_folders.size(); i++)
      if(((FolderData)m_folders.elementAt(i)).getID().equals(folderid)) {
        mfps[0] = ((FolderData)m_folders.elementAt(i)).getName();
        break;
      }
    params.put("methodName","deleteFolder");
    params.put("back", sid());

    DeleteConfirm confirm = (DeleteConfirm) super.makeScreen("DeleteConfirm");
    confirm.init(params);
    return confirm;

//    return confirm(CAddressBook.CONFIRM_DELETE_FOLDER,mfps,params,sid());
  }
  /**
   * Go to folder screen for creating a new folder.
   * @param params Noop at here. It is used by framework.
   */
  public Screen newFolder(Hashtable params) throws Exception {
    Folder screen = (Folder)m_channel.makeScreen("Folder");
    screen.m_isNew = true;
    screen.m_back = sid();
    screen.m_methodName = "refreshFolderOnScreen";

    return screen;
  }
  /**
   * Go to the next page if screen have many pages.
   * @param params Noop at here. It is used by framework.
   */
  public Screen next(Hashtable params) throws Exception {
    if(m_openFolder!=null)
      m_curPage = m_curPage + 1;
    return this;
  }
  /**
   * Go to the previous page if screen have many pages.
   * @param params Noop at here. It is used by framework.
   */
  public Screen prev(Hashtable params) throws Exception {
    if(m_openFolder!=null)
      m_curPage = m_curPage -1;
    return this;
  }
  /**
   * Get contact data of the specified folder.
   * Init m_contacts and init all parameter for operating on pages.
   * @param fdID the specified folder.
   */
  private void open( String fdID) throws Exception {
    m_openFolder = new String(fdID);
    m_nPages = 0;
    if(m_contacts!=null)
      m_contacts.clear();
    if(m_openFolder.equals(CAddressBook.ALL_FOLDER))
      m_contacts = getBo().listAllContacts(this.getUserLogon());
    else
      m_contacts = getBo().listContacts(this.getUserLogon(),m_openFolder);
    int len;
    if(m_contacts==null)
      len = 0;
    else
      len = m_contacts.size();
    m_nPages = len / m_rpp;
    if( (len % m_rpp) != 0)
      m_nPages++;
    m_curPage = (m_nPages > 0) ? 0 : -1;
  }
  /**
   * Refresh contain of folder on screen.
   * @param params it is used by framework.
   * @return screen is a currently screen.
   */
  public Screen refreshFolderOnScreen(Hashtable params) throws Exception {
    try {
      m_folders.clear();
      m_folders = getBo().listFolders(this.getUserLogon());
    } catch(Exception e) {}
    return this;
  }
  /**
   * Refresh contain of contact on screen.
   * @param params it is used by framework.
   * @return screen is a currently screen.
   */
  public Screen refreshContactOnScreen(Hashtable params) throws Exception {
    int curPage = m_curPage;
    open(m_openFolder);
    if(curPage == -1)
      m_curPage = 0;
    else if(curPage<=m_nPages -1)
      m_curPage = curPage;
    else
      m_curPage = m_nPages - 1;
    return this;
  }
}
