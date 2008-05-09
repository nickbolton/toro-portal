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
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.Sorted;
import net.unicon.academus.apps.rad.XML;
import net.unicon.portal.channels.rad.Screen;
import net.unicon.portal.channels.rad.Servant;
import net.unicon.portal.channels.rad.IdentityComparator;

/**
 * It is contact screen in addressbook project. It allow you create
 * and edit contact.
 */
public class Contact extends AddressBookScreen {
  /* Override from Screen in rad. */
  static public final String SID = "Contact";
  /* Name of before screen. */
  public String m_back = null;
  /* Name of function in m_back screen will be done after choosing ok button. */
  String m_methodName = null;
  /* Flag point at create contact or edit contact. */
  public boolean m_isNew = true;
  /* Save contact data on form. */
  ContactData m_ct = null;
  /* list of folders is not selected of contact in combobox. */
  Sorted m_fd = null;
  /* list of folders is selected of contact. */
  Sorted m_selectedfd = null;
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
    m_fd = new Sorted(new IdentityComparator());
    m_selectedfd = new Sorted(new IdentityComparator());
    Vector vfd = getBo().listFolders(getUserLogon());
    if(vfd!=null&&vfd.size()>0)
      for(int i = 0 ; i < vfd.size() ; i++)
        m_fd.add(vfd.elementAt(i));

    if(m_ct==null)  { // Thach-Mar07
      IdentityData id = (IdentityData)params.get("identity-data");
      m_ct = (id != null)?new ContactData(id):new ContactData();
      params.remove("identity-data");
    }
  }
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public void reinit(Hashtable params) { }
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
    //Folder in Combobox
    if(m_fd!=null&&m_fd.size()>0)
      for(int i = 0; i< m_fd.size() ;i++)
        ((FolderData)m_fd.elementAt(i)).printXML(xml);
    //Folder is selected
    xml.append("<selected-folder>");
    if(m_selectedfd!=null&&m_selectedfd.size()>0)
      for(int i = 0; i< m_selectedfd.size() ;i++)
        ((FolderData)m_selectedfd.elementAt(i)).printXML(xml);
    xml.append("</selected-folder>");
    //ContactData on form
    m_ct.printXML(xml);
  }

  /**
   * Process for add button on screen.
   * note : not add into database.
   */
  public Screen add
    (Hashtable params) throws Exception {
      getContactOnForm(params);
      String folderid = (String)params.get("folderid");
      if(folderid==null||folderid.length()==0)
        return this;
      int i = find(m_fd,folderid);
      m_selectedfd.add((FolderData)m_fd.elementAt(i));
      m_fd.remove(i);
      return this;
    }

  /**
   *  Find position of element.
   *  @param  v   collection for finding.
   *  @param  id  key for finding.
   *  @return -1  Not founded.
   *          id  Position of key in collection.
   */
  public static int find(Sorted v , String id) {
    for (int i = 0 ; i < v.size() ; i++) {
      if (((IdentityData)v.elementAt(i)).getID().equals(id)) {
        return i;
      }
    }
    return -1;
  }

/*
  public Screen confirmDeleteFolder(Hashtable params) throws Exception {
//    getContactOnForm(params);
    params.put("back", sid());
    params.put("methodName", "deleteFolder");

    DeleteConfirm confirm = (DeleteConfirm) super.makeScreen("DeleteConfirm");
    confirm.init(params);
    return confirm;

//    return this;
  }
*/
  /**
   * Process for delete image on screen.
   * note : not delete into database.
   */
  public Screen deleteFolder(Hashtable params) throws Exception {
    getContactOnForm(params);
    String folderid = (String)params.get("folderid");
    int i = find(m_selectedfd,folderid);
    m_fd.add(m_selectedfd.elementAt(i));
    m_selectedfd.remove(i);
    return this;
  }

  /**
   * Process OK button on contact screen, go back before screen.
   */
  public Screen ok(Hashtable params) throws Exception {
    getContactOnForm(params);
    Screen scrret = checkValue();
    if(scrret!=null)
      return scrret;

    m_ct.putOwnerId(getUserLogon());
    if(m_isNew)//New
    {
      try {
	if (getBo().getContactByName(m_ct.getName(), getUserLogon()) != null) {
	  return error(CAddressBook.DUPLICATE_CREATE_CONTACT);
	}      
        int contactid = getBo().addContact(m_ct,(FolderData[])m_selectedfd.toArray(new FolderData[0]));
        m_ct.putOID(Integer.toString(contactid));
      } catch(Exception e) {
        return error(CAddressBook.ERROR_CREATE_CONTACT);
      }
    }
    else//Update
    {
      boolean ret = getBo().updateContact(m_ct,(FolderData[])m_selectedfd.toArray(new FolderData[0]));
      if(!ret) {
        return info(getLocalText(CAddressBook.ERROR_UPDATE_CONTACT,CAddressBook.class), true);
      }
    }

    // Thach 0506
    if (m_channel instanceof Servant) {
      ((Servant)m_channel).finish((Object[])new ContactData[]{m_ct});
      return null;
    }

    params.put("data",m_ct);

    if(m_methodName==null)
      return getScreen(m_back);
    else
      return getScreen(m_back).invoke(m_methodName,params);
  }

  /**
   * Process Cancel button on contact screen, go back before screen.
   */
  public Screen cancel(Hashtable params) throws Exception {
    // Thach 0506
    if (m_channel instanceof Servant) {
      ((Servant)m_channel).finish(null);
      return null;
    }

    return getScreen(m_back);
  }

  /**
   * Receive data from contact form and init for m_ct.
   */
  private void getContactOnForm(Hashtable params) {
    m_ct.putName(params.get("name")==null ? "" : ((String)params.get("name")).trim());
    m_ct.putEmail(params.get("email")==null ? "" : ((String)params.get("email")).trim());
    m_ct.putCellPhone(params.get("mobile")==null ? "" : ((String)params.get("mobile")).trim());
    m_ct.putTitle(params.get("title")==null ? "" : ((String)params.get("title")).trim());
    m_ct.putCompany(params.get("company")==null ? "" : ((String)params.get("company")).trim());
    m_ct.putDepartment(params.get("department")==null ? "" : ((String)params.get("department")).trim());
    m_ct.putBusinessPhone(params.get("business-phone")==null ? "" : ((String)params.get("business-phone")).trim());
    m_ct.putFax(params.get("fax")==null?"":((String)params.get("fax")).trim());
    m_ct.putOfficeAddress(params.get("office-address")==null? "" : ((String)params.get("office-address")).trim());
    m_ct.putHomePhone(params.get("home-phone")==null? "" : ((String)params.get("home-phone")).trim());
    m_ct.putHomeAddress(params.get("home-address")==null? "" : ((String)params.get("home-address")).trim());
    m_ct.putNotes(params.get("notes")==null? "" :((String)params.get("notes")).trim());
  }
  /**
   * Check data input on <code>Contact</code> screen.
   * @return  null      : correct value.
   *          not null  : incorrect value.
   */
  private Screen checkValue() {
    String[] obj = new String[2];
    String tmp = m_ct.getName();
    //Name
    if (tmp == null || tmp.length() == 0)
      return error(CAddressBook.ERROR_EMPTY_CONTACT_NAME);
    if (tmp.length() > ContactData.MAX_NAME_LENGTH)
      return error(CAddressBook.ERROR_CONTACT_NAME_TOO_LONG);
    //Email
    tmp = m_ct.getEmail();
    if (!CAddressBook.isValidEmail(tmp)) {
      return error(CAddressBook.ERROR_INVALID_EMAIL);
    }
    if(tmp.length() > ContactData.MAX_EMAIL_LENGTH) {
      obj[0] = "email";
      obj[1] = new Integer(ContactData.MAX_EMAIL_LENGTH).toString();
      return error(CAddressBook.ERROR_OTHER_TOO_LONG,obj);
    }
    //Mobile
    tmp = m_ct.getCellPhone();
    if(tmp.length() > ContactData.MAX_MOBILE_LENGTH) {
      obj[0] = "mobile";
      obj[1] = new Integer(ContactData.MAX_MOBILE_LENGTH).toString();
      return error(CAddressBook.ERROR_OTHER_TOO_LONG,obj);
    }
    //Title
    tmp = m_ct.getTitle();
    if(tmp.length() > ContactData.MAX_TITLE_LENGTH) {
      obj[0] = "title";
      obj[1] = new Integer(ContactData.MAX_TITLE_LENGTH).toString();
      return error(CAddressBook.ERROR_OTHER_TOO_LONG,obj);
    }
    //Company
    tmp = m_ct.getCompany();
    if(tmp.length() > ContactData.MAX_COMPANY_LENGTH) {
      obj[0] = "company";
      obj[1] = new Integer(ContactData.MAX_COMPANY_LENGTH).toString();
      return error(CAddressBook.ERROR_OTHER_TOO_LONG,obj);
    }
    //Department
    tmp = m_ct.getDepartment();
    if(tmp.length() > ContactData.MAX_DEPARTMENT_LENGTH) {
      obj[0] = "department";
      obj[1] = new Integer(ContactData.MAX_DEPARTMENT_LENGTH).toString();
      return error(CAddressBook.ERROR_OTHER_TOO_LONG,obj);
    }
    //Business Phone
    tmp = m_ct.getBusinessPhone();
    if(tmp.length() > ContactData.MAX_BUSINESS_PHONE_LENGTH) {
      obj[0] = "business phone";
      obj[1] = new Integer(ContactData.MAX_BUSINESS_PHONE_LENGTH).toString();
      return error(CAddressBook.ERROR_OTHER_TOO_LONG,obj);
    }
    //Fax
    tmp = m_ct.getFax();
    if(tmp.length() > ContactData.MAX_FAX_LENGTH) {
      obj[0] = "fax";
      obj[1] = new Integer(ContactData.MAX_FAX_LENGTH).toString();
      return error(CAddressBook.ERROR_OTHER_TOO_LONG,obj);
    }
    //Office Address
    tmp = m_ct.getOfficeAddress();
    if(tmp.length() > ContactData.MAX_OFFICE_ADDRESS_LENGTH) {
      obj[0] = "office address";
      obj[1] = new Integer(ContactData.MAX_OFFICE_ADDRESS_LENGTH).toString();
      return error(CAddressBook.ERROR_OTHER_TOO_LONG,obj);
    }
    //Home Phone
    tmp = m_ct.getHomePhone();
    if(tmp.length() > ContactData.MAX_HOME_PHONE_LENGTH) {
      obj[0] = "home phone";
      obj[1] = new Integer(ContactData.MAX_HOME_PHONE_LENGTH).toString();
      return error(CAddressBook.ERROR_OTHER_TOO_LONG,obj);
    }
    //Home Address
    tmp = m_ct.getHomeAddress();
    if(tmp.length() > ContactData.MAX_HOME_ADDRESS_LENGTH) {
      obj[0] = "home address";
      obj[1] = new Integer(ContactData.MAX_HOME_ADDRESS_LENGTH).toString();
      return error(CAddressBook.ERROR_OTHER_TOO_LONG,obj);
    }
    //Notes
    tmp = m_ct.getNotes();
    if(tmp.length() > ContactData.MAX_NOTES_LENGTH) {
      obj[0] = "notes";
      obj[1] = new Integer(ContactData.MAX_NOTES_LENGTH).toString();
      return error(CAddressBook.ERROR_OTHER_TOO_LONG,obj);
    }
    return null;
  }

  /**
   * To build folders(combobox) and selected folders(deleted) for Contact Screen
   * used by Search Screen and Personal Screen.
   * @param contactid
   */
  public void getFolderOfContact(String contactid) throws Exception {
    m_selectedfd.clear();
    Vector foldersels = getBo().getFolders(contactid);
    if(foldersels!=null&&foldersels.size()>0)
      for(int i=0; i< foldersels.size() ; i++) {
        m_selectedfd.add(foldersels.elementAt(i));
        m_fd.remove(find(m_fd,((FolderData)foldersels.elementAt(i)).getID()));
      }
  }
}
