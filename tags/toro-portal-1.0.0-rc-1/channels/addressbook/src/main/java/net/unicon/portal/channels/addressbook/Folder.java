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

import net.unicon.academus.apps.addressbook.FolderData;
import net.unicon.portal.channels.rad.Screen;

/**
 * It is folder screen in addressbook project. It allow you create
 * and edit folder.
 */
public class Folder extends AddressBookScreen {
  /* Override from Screen in rad. */
  static public final String SID = "Folder";
  /* Name of before screen. */
  String m_back = null;
  /* Name of function in m_back screen will be done after choosing ok button. */
  String m_methodName = null;
  /* Flag point at create contact or edit contact. */
  boolean m_isNew = true;
  /* Save folder data on form. */
  FolderData m_folder = new FolderData();
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
  public Hashtable getXSLTParams() {
    Hashtable params = new Hashtable();
    params.put("isNew",(new Boolean(m_isNew)).toString());
    return params;
  }
  /**
   *  Override from <code>AddressBookScreen</code>.
   *  @see net.unicon.portal.channels.addressbook.AddressBookScreen.
   */
  public void printXML(StringBuffer xml) throws Exception {
    m_folder.printXML(xml);
  }
  /**
   * Process OK button on contact screen, go back before screen.
   */
  public Screen ok(Hashtable params) throws Exception {
    String folname = (String)params.get("foldername");
    folname = (folname==null) ? "" : folname.trim();
    if (folname.length() == 0)
      return error(CAddressBook.ERROR_EMPTY_FOLDER_NAME);
    if (folname.length() > FolderData.MAX_NAME_LENGTH)
      return error(CAddressBook.ERROR_FOLDER_NAME_TOO_LONG);
    if (!folname.equals(m_folder.getName())||m_isNew) {
      m_folder.putOwnerId(getUserLogon());
      if(getBo().folderExistName(folname,getUserLogon()))
        return error(CAddressBook.ERROR_DUPLICATE_FOLDER_NAME );
      m_folder.putName(folname);
      if(m_isNew)//New
      {
        try {
          int folid = getBo().addFolder(m_folder);
          m_folder.putOID(Integer.toString(folid));
        } catch(Exception e) {
          return info(getLocalText(CAddressBook.ERROR_CREATE_FOLDER,CAddressBook.class), true);
        }
      }
      else//Update
      {
        try {
          boolean ret = getBo().updateFolder(m_folder.getID() ,folname);
          if(!ret)
            return info(getLocalText(CAddressBook.ERROR_UPDATE_FOLDER,CAddressBook.class), true);
        } catch(Exception e) {
          return info(CAddressBook.ERROR_UPDATE_FOLDER, true);
        }
      }
    }
    params.remove("foldername");
    params.put("data",m_folder);
    if(m_methodName==null)
      return getScreen(m_back);
    else
      return getScreen(m_back).invoke(m_methodName,params);
  }
  /**
   * Process Cancel button on contact screen, go back before screen.
   */
  public Screen cancel(Hashtable params) throws Exception {
    return getScreen(m_back);
  }
}
