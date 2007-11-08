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
package net.unicon.academus.apps.addressbook;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.Vector;
import java.util.Hashtable;


import net.unicon.academus.apps.addressbook.ContactData;
import net.unicon.academus.apps.addressbook.FolderData;
import net.unicon.academus.apps.rad.DBService;
import net.unicon.academus.apps.rad.SQL;
import net.unicon.academus.apps.rad.StdBo;
/**
 *  A <code>AddressBookBo</code> object is a component for operating on
 *  database.
 *
 *@author
 *@created    April 9, 2003
 *@version
 */
public class AddressBookBo extends StdBo {
  private final static String DS = "AddressBook";

  /**
   *  Creates a new <code>AddressBookBo</code> instance.
   */
  public AddressBookBo() {
    super("AddressBookBo");
  }

  /**
   *@param  contacts       The feature to be added to the Contacts attribute
   *@return                Description of the Return Value
   *@exception  Exception  Description of the Exception
   */
  public boolean addContacts(ContactData[] contacts) throws Exception {
    DBService ds = null;
    int ret = 0;
    PreparedStatement pstmt = null;
    try {
      // Start transaction
      ds = getDBService(DS);
      ds.begin();
      pstmt = ds.prepareStatement(
                                  "INSERT INTO UPC_AB_CONTACT(LOGON_ID,CONTACT_ID,CONTACT_NAME," +
                                  "EMAIL,MOBILE,TITLE,COMPANY,DEPARTMENT,BUSINESSPHONE,FAX," +
                                  "OFFICEADDRESS,HOMEPHONE,HOMEADDRESS,NOTES,REF) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      if (contacts != null && contacts.length > 0) {
        for (int i = 0; i < contacts.length; i++) {
          ContactData cdata = contacts[i];
          //int contactId = getMax(ds,"UPC_AB_CONTACT","CONTACT_ID", null) + 1;
          int contactId = ds.getNextValue("UPC_AB_CONTACT", "CONTACT_ID");
          // Thach-May13
          cdata.putOID(Integer.toString(contactId));
          pstmt.setString(1, cdata.getOwnerId());
          pstmt.setInt(2, Integer.parseInt(cdata.getID()));
          pstmt.setString(3, cdata.getName());
          pstmt.setString(4, cdata.getEmail());
          pstmt.setString(5, cdata.getCellPhone());
          pstmt.setString(6, cdata.getTitle());
          pstmt.setString(7, cdata.getCompany());
          pstmt.setString(8, cdata.getDepartment());
          pstmt.setString(9, cdata.getBusinessPhone());
          pstmt.setString(10, cdata.getFax());
          pstmt.setString(11, cdata.getOfficeAddress());
          pstmt.setString(12, cdata.getHomePhone());
          pstmt.setString(13, cdata.getHomeAddress());
          pstmt.setString(14, cdata.getNotes());
          pstmt.setString(15, cdata.getRef());
          ds.executePreparedStatement(pstmt);
        }
      }
      // End transation
      ds.commit();
    } catch (Exception e) {
      ds.rollback(e.toString());
      return false;
    } finally {
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      ds.release();
    }
    return true;
  }

  /**
   *  Create and add the specified contact to a list of specified folders. If
   *  cdata is null, exception is thrown and no action is performed. If
   *  folders is null or number of folders is zero, only contact is created.
   *
   *@param  cdata          the <code>ContactData</code> object contains data
   *      of contact.
   *@param  folders        the list of <code>FolderData</code> objects
   *      indicating where contact to add.
   *@return                0 create/add failure. ID ID of contact has just
   *      created.
   *@exception  Exception  Description of the Exception
   *@see                   net.unicon.academus.apps.addressbook.ContactData
   *@see                   net.unicon.academus.apps.addressbook.FolderData
   */
  public int addContact(ContactData cdata, FolderData[] folders) throws
    Exception {
    DBService ds = null;
    PreparedStatement pstmt = null;
    int ret = 0;
    try {
      ds = getDBService(DS);
      // Start transaction
      ds.begin();
      //int contactId = getMax(ds,"UPC_AB_CONTACT","CONTACT_ID", null) + 1;
      int contactId = ds.getNextValue("UPC_AB_CONTACT", "CONTACT_ID");
      // Thach-May13
      cdata.putOID(Integer.toString(contactId));

      pstmt = ds.prepareStatement(
                                  "INSERT INTO UPC_AB_CONTACT(LOGON_ID,CONTACT_ID,CONTACT_NAME," +
                                  "EMAIL,MOBILE,TITLE,COMPANY,DEPARTMENT,BUSINESSPHONE,FAX," +
                                  "OFFICEADDRESS,HOMEPHONE,HOMEADDRESS,NOTES,REF) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      pstmt.setString(1, cdata.getOwnerId());
      pstmt.setInt(2, Integer.parseInt(cdata.getID()));
      pstmt.setString(3, cdata.getName());
      pstmt.setString(4, cdata.getEmail());
      pstmt.setString(5, cdata.getCellPhone());
      pstmt.setString(6, cdata.getTitle());
      pstmt.setString(7, cdata.getCompany());
      pstmt.setString(8, cdata.getDepartment());
      pstmt.setString(9, cdata.getBusinessPhone());
      pstmt.setString(10, cdata.getFax());
      pstmt.setString(11, cdata.getOfficeAddress());
      pstmt.setString(12, cdata.getHomePhone());
      pstmt.setString(13, cdata.getHomeAddress());
      pstmt.setString(14, cdata.getNotes());
      pstmt.setString(15, cdata.getRef());
      ds.executePreparedStatement(pstmt);

      // Insert new contact
      if (folders != null && folders.length > 0) {
        pstmt = ds.prepareStatement(
                  "INSERT INTO UPC_AB_FOLDCON(LOGON_ID,CONTACT_ID,FOLDER_ID) VALUES(" +
                  SQL.esc(cdata.getOwnerId()) + "," + contactId + ",?)");
        for (int i = 0; i < folders.length; i++) {
          String folderid = folders[i].getID();
          pstmt.setInt(1, Integer.parseInt(folderid));
          ds.executePreparedStatement(pstmt);
        }
      }
      // End transation
      ds.commit();
      ret = contactId;
    } catch (Exception e) {
      ds.rollback(e.toString());
    } finally {
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      ds.release();
    }
    return ret;
  }

  /**
   *  List of contacts are in the specified folder and the specified user. If
   *  userLogon and folderId is null, exception is thrown and no action is
   *  performed.
   *
   *@param  userLogon      the specified user.
   *@param  folderId       the specified folder.
   *@return                List of contacts.
   *@exception  Exception  Description of the Exception
   */
  public Vector listContacts(String userLogon, String folderId) throws
    Exception {
    DBService ds = null;
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    Vector v = new Vector();
    String query = "SELECT CT.LOGON_ID,CT.CONTACT_ID," +
                   "CT.CONTACT_NAME,CT.EMAIL,CT.MOBILE,CT.TITLE," +
                   "CT.COMPANY,CT.DEPARTMENT,CT.BUSINESSPHONE,CT.FAX,CT.OFFICEADDRESS," +
                   "CT.HOMEPHONE,CT.HOMEADDRESS,CT.NOTES,CT.REF FROM UPC_AB_CONTACT CT,UPC_AB_FOLDCON FC" +
                   " WHERE FC.LOGON_ID=? AND CT.CONTACT_ID = FC.CONTACT_ID" +
                   " AND FC.FOLDER_ID=? ORDER BY CONTACT_NAME ASC";
    try {
      ds = getDBService(DS);
      pstmt = ds.prepareStatement(query);
      pstmt.setString(1, userLogon);
      pstmt.setInt(2, Integer.parseInt(folderId));
      rs = pstmt.executeQuery();
      //ResultSet rs = ds.executeQueryPreparedStatement(pstmt);
      while (rs.next()) {
        v.addElement(newContact(rs));
      }
      ds.closeStatement();
    } catch (Exception e) {
      throw e;
    } finally {
      try { 
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try { 
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      ds.release();
    }
    return v;
  }

  /**
   *  List of contacts are in all of folders of the specified user. If
   *  userLogon is null, exception is thrown and no action is performed.
   *
   *@param  userLogon      the specified user.
   *@return                List of contacts. (ContactData)
   *@exception  Exception  Description of the Exception
   */
  public Vector listAllContacts(String userLogon) throws Exception {
    DBService ds = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Vector v = new Vector();
    try {
      ds = getDBService(DS);
      String query =
        "SELECT logon_id, contact_id, contact_name, email, mobile, title, company, department, businessphone, fax, officeaddress, homephone, homeaddress, notes, ref FROM UPC_AB_CONTACT WHERE LOGON_ID = ? ORDER BY CONTACT_NAME ASC";
      pstmt = ds.prepareStatement(query);
      pstmt.setString(1, userLogon);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        v.addElement(newContact(rs));
      }
      ds.closeStatement();
    } catch (Exception e) {
      throw e;
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      ds.release();
    }
    return v;
  }

  /**
   *  List of contacts of specified user are suitable to the specified
   *  conditions. Results find out is approximation. Examples ... name =
   *  "Phuoc Nguyen Thanh" name filter = "Ng" result Phuoc Nguyen Thanh record
   *  is found out. If userLogon is null, exception is thrown and no action is
   *  performed.
   *
   *@param  result         list of contacts find out is added to result
   *      vector.
   *@param  userLogon      the specified user.
   *@param  name           the name filter.
   *@param  email          the email filter.
   *@param  title          the title filter.
   *@param  department     the department filter.
   *@param  countLimit     limit number of contacts find out.
   *@exception  Exception  Description of the Exception
   */
  public void searchContacts(Vector result, String userLogon, String name,
                             String email, String title, String department,
                             int countLimit) throws Exception {
    DBService ds = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    String sql = "SELECT logon_id, contact_id, contact_name, email, mobile, title, company, department, businessphone, fax, officeaddress, homephone, homeaddress, notes, ref FROM upc_ab_contact WHERE logon_id=" +
                 SQL.esc(userLogon);
    if (name != null && !name.equals("")) {
      sql += " AND lower(contact_name) LIKE " + SQL.like(name.toLowerCase());
    }
    if (email != null && !email.equals("")) {
      sql += " AND lower(email) LIKE " + SQL.like(email.toLowerCase());
    }
    if (title != null && !title.equals("")) {
      sql += " AND lower(title) LIKE " + SQL.like(title.toLowerCase());
    }
    if (department != null && !department.equals("")) {
      sql += " AND lower(department) LIKE " + SQL.like(department.toLowerCase());
    }
    sql += " ORDER BY contact_name ASC";
    try {
      ds = getDBService(DS);
      pstmt = ds.prepareStatement(sql);
      rs = pstmt.executeQuery();
      //ResultSet rs = ds.select(sql);
      while (rs.next() && countLimit-- > 0) {
        result.addElement(newContact(rs));
      }
      ds.closeStatement();
    } catch (Exception e) {
      throw e;
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      ds.release();
    }
  }

  /**
   *  List of contacts of specified user are suitable to the specified
   *  conditions. Results find out is approximation. Examples ... name =
   *  "Phuoc Nguyen Thanh" name filter = "Ng" result Phuoc Nguyen Thanh record
   *  is found out. If userLogon is null, exception is thrown and no action is
   *  performed.
   *
   *@param  userLogon      the specified user.
   *@param  name           the name filter.
   *@param  email          the email filter.
   *@param  title          the title filter.
   *@param  department     the department filter.
   *@param  countLimit     limit number of contacts find out.
   *@return                List of contacts.
   *@exception  Exception  Description of the Exception
   */
  public Vector searchContacts(String userLogon, String name,
                               String email, String title, String department,
                               int countLimit) throws Exception {
    Vector v = new Vector();
    searchContacts(v, userLogon, name, email, title, department, countLimit);
    return v;
  }

  /**
   *  List of folders of specified user are suitable to the specified
   *  conditions. Results find out is approximation. Examples ... name =
   *  "Phuoc Nguyen Thanh" name filter = "Ng" result Phuoc Nguyen Thanh record
   *  is found out. If userLogon is null, exception is thrown and no action is
   *  performed.
   *
   *@param  userLogon      the specified user.
   *@param  name           the name filter.
   *@param  countLimit     limit number of contacts find out.
   *@return                List of folders.
   *@exception  Exception  Description of the Exception
   */
  public Vector searchFolders(String userLogon, String name, int countLimit) throws
    Exception {
    Vector v = new Vector();
    DBService ds = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    String sql =
      "SELECT FOLDER_ID,FOLDER_NAME FROM UPC_AB_FOLDER WHERE LOGON_ID=" +
      SQL.esc(userLogon);
    if (name != null && !name.equals("")) {
      sql += " AND lower(FOLDER_NAME) LIKE " + SQL.like(name.toLowerCase());
    }
    sql += " ORDER BY FOLDER_NAME ASC";
    try {
      ds = getDBService(DS);
      pstmt = ds.prepareStatement(sql);
      rs = pstmt.executeQuery();
      //ResultSet rs = ds.select(sql);
      while (rs.next() && countLimit-- > 0) {
        v.addElement(newFolder(rs));
      }
      ds.closeStatement();
    } catch (Exception e) {
      throw e;
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      ds.release();
    }
    return v;
  }

  /**
   *  VU
   *
   *@param  contactId                Description of the Parameter
   *@return                          The duplicated value
   *@exception  Exception            Description of the Exception
   */
  /*
     *  public boolean isDuplicated(ContactData cdata) throws java.lang.Exception {
   *  DBService ds = getDBService(DS);
   *  ContactData cdata = null;
   *  try {
   *  PreparedStatement pstmt = ds.prepareStatement("SELECT CONTACT_NAME FROM UPC_AB_CONTACT");
   *  pstmt.setString(1, cdata.ID_UNKNOWNindent);
   *  ResultSet rs = pstmt.executeQuery();
   *  /ResultSet rs = ds.executeQueryPreparedStatement(pstmt);
   *  if (rs.next()) {
   *  cdata = newContact(rs);
   *  }
   *  ds.closeStatement();
   *  } catch (Exception e) {
   *  throw e;
   *  } finally {
   *  ds.release();
   *  }
   *  return cdata;
   *  }
   */
  /**
   *  Get detailed information of the specificed contact. If contactId is
   *  null, exception is thrown and no action is performed.
   *
   *@param  contactId      Id of contact.
   *@return                Detailed information of the specificed contact.
   *@exception  Exception  Description of the Exception
   *@see                   net.unicon.academus.apps.addressbook.ContactData.
   */
  public ContactData getContact(String contactId) throws Exception {
    DBService ds = null;
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    ContactData cdata = null;
    try {
      ds = getDBService(DS);
      pstmt = ds.prepareStatement("SELECT logon_id, contact_id, contact_name, email, mobile, title, company, department, businessphone, fax, officeaddress, homephone, homeaddress, notes, ref FROM upc_ab_contact WHERE contact_id=?");
      pstmt.setInt(1, Integer.parseInt(contactId));
      rs = pstmt.executeQuery();
      if (rs.next()) {
        cdata = newContact(rs);
      }
      ds.closeStatement();
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      ds.release();
    }
    return cdata;
  }

  public ContactData getContactByName(String contactName, String logonUser) throws Exception {
    DBService ds = null;
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    ContactData cdata = null;
    try {
      ds = getDBService(DS);
      pstmt = ds.prepareStatement("SELECT logon_id, contact_id, contact_name, email, mobile, title, company, department, businessphone, fax, officeaddress, homephone, homeaddress, notes, ref FROM upc_ab_contact WHERE contact_name=? AND logon_id=?");
      pstmt.setString(1, contactName);
      pstmt.setString(2, logonUser);
      rs = pstmt.executeQuery();
      if (rs.next())
        cdata = newContact(rs);
      ds.closeStatement();
    } finally {
      try {   
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {     
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace(); 
      }
      ds.release();
    }
    return cdata;
  }

  /**
   *  Get detailed information of the specificed list of contacts. If
   *  contactIds is null, no action is performed.
   *
   *@param  contactIds     List of Ids.
   *@return                list of detailed information contacts.
   *@exception  Exception  Description of the Exception
   *@see                   net.unicon.academus.apps.addressbook.ContactData.
   */
  public ContactData[] getContacts(String contactIds[]) throws Exception {
    String contactId = convertIN(contactIds);
    Hashtable hs = new Hashtable();
    ContactData[] ret = null;
    DBService ds = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    if (contactId == null) {
      return null;
    }
    try {
      ds = getDBService(DS);
      pstmt = ds.prepareStatement(
                                  "SELECT logon_id, contact_id, contact_name, email, mobile, title, company, department, businessphone, fax, officeaddress, homephone, homeaddress, notes, ref FROM upc_ab_contact WHERE contact_id IN (?)");
      pstmt.setInt(1, Integer.parseInt(contactId));
      rs = pstmt.executeQuery();
      //ResultSet rs = ds.executeQueryPreparedStatement(pstmt);
      while (rs.next()) {
        ContactData ct = newContact(rs);
        hs.put(ct.getID(), ct);
      }
      ret = getData(contactIds, hs);
      ds.closeStatement();
    } catch (Exception e) {
      throw e;
    } finally {
      try {   
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {     
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace(); 
      }
      ds.release();
    }
    return ret;
  }

  /**
   *@param  ref
   *@return
   *@exception  Exception  Description of the Exception
   *@see                   net.unicon.academus.apps.addressbook.ContactData.
   */
  public ContactData getContactByRef(String ref) throws Exception {
    DBService ds = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    ContactData cdata = null;
    try {
      ds = getDBService(DS);
      pstmt = ds.prepareStatement(
                                  "SELECT logon_id, contact_id, contact_name, email, mobile, title, company, department, businessphone, fax, officeaddress, homephone, homeaddress, notes, ref FROM upc_ab_contact WHERE ref=?");
      pstmt.setString(1, ref);
      rs = pstmt.executeQuery();
      //ResultSet rs = ds.executeQueryPreparedStatement(pstmt);
      if (rs.next()) {
        cdata = newContact(rs);
      }
      ds.closeStatement();
    } catch (Exception e) {
      throw e;
    } finally {
      try {   
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {     
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace(); 
      }
      ds.release();
    }
    return cdata;
  }

  /**
   *@param  ref
   *@param  userLogon      the specified user.
   *@return
   *@exception  Exception  Description of the Exception
   *@see                   net.unicon.academus.apps.addressbook.ContactData.
   */
  public ContactData getContactByRef(String ref, String userLogon) throws
    Exception {
    DBService ds = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    ContactData cdata = null;
    try {
      ds = getDBService(DS);
      pstmt = ds.prepareStatement(
                                  "SELECT logon_id, contact_id, contact_name, email, mobile, title, company, department, businessphone, fax, officeaddress, homephone, homeaddress, notes, ref FROM upc_ab_contact WHERE ref=? AND logon_id=?");
      pstmt.setString(1, ref);
      pstmt.setString(2, userLogon);
      rs = pstmt.executeQuery();
      //ResultSet rs = ds.executeQueryPreparedStatement(pstmt);
      if (rs.next()) {
        cdata = newContact(rs);
      }
      ds.closeStatement();
    } catch (Exception e) {
      throw e;
    } finally {
      try {   
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {     
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace(); 
      }
      ds.release();
    }
    return cdata;
  }

  /**
   *@param  refs
   *@return
   *@exception  Exception  Description of the Exception
   *@see                   net.unicon.academus.apps.addressbook.ContactData.
   */
  public ContactData[] getContactByRefs(String refs[]) throws Exception {
    String ref = convertIN(refs);
    Hashtable hs = new Hashtable();
    ContactData[] ret = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    DBService ds = null;
    try {
      ds = getDBService(DS);
      pstmt = ds.prepareStatement(
                                  "SELECT logon_id, contact_id, contact_name, email, mobile, title, company, department, businessphone, fax, officeaddress, homephone, homeaddress, notes, ref FROM upc_ab_contact WHERE ref IN (?)");
      pstmt.setString(1, ref);
      rs = pstmt.executeQuery();
      //ResultSet rs = ds.executeQueryPreparedStatement(pstmt);
      while (rs.next()) {
        ContactData ct = newContact(rs);
        hs.put(ct.getRef(), ct);
      }
      ret = getData(refs, hs);
      ds.closeStatement();
    } catch (Exception e) {
      throw e;
    } finally {
      try {   
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {     
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace(); 
      }
      ds.release();
    }
    return ret;
  }

  //Hashtable data : ( key , ContactData )
  /**
   *  Gets the data attribute of the AddressBookBo object
   *
   *@param  keys  Description of the Parameter
   *@param  data  Description of the Parameter
   *@return       The data value
   */
  private ContactData[] getData(String keys[], Hashtable data) {
    if (keys == null) {
      return null;
    }
    if (keys.length == 0) {
      return null;
    }
    ContactData datas[] = new ContactData[keys.length];
    if (data == null) {
      return datas;
    }
    if (data.size() == 0) {
      return datas;
    }
    for (int i = 0; i < datas.length; i++) {
      datas[i] = (ContactData) data.remove(datas[i]);
    }
    return datas;
  }

  /**
   *  Delete contact.
   *
   *@param  folderId       the specified folder.
   *@param  contactId      the specified contact.
   *@return                Success : true. Fail : false.
   *@exception  Exception  Description of the Exception
   */

  public boolean deleteContact(String folderId, String contactId) throws
    Exception {
    //folderId == null : ALL_FOLDER

    DBService ds = null;
    PreparedStatement pstmt = null;
    int iRet = -1;
    try {
      ds = getDBService(DS);
      ds.begin();
      if (folderId == null) {
        pstmt = ds.prepareStatement(
                  "DELETE FROM UPC_AB_FOLDCON WHERE CONTACT_ID=?");
        pstmt.setInt(1, Integer.parseInt(contactId));
        iRet = ds.executePreparedStatement(pstmt);
      } else {
        pstmt = ds.prepareStatement(
                  "DELETE FROM UPC_AB_FOLDCON WHERE CONTACT_ID=? AND FOLDER_ID= ?");
        pstmt.setInt(1, Integer.parseInt(contactId));
        pstmt.setInt(2, Integer.parseInt(folderId));
        iRet = ds.executePreparedStatement(pstmt);
      }
	  pstmt.close();
	  pstmt = null;

      pstmt = ds.prepareStatement("DELETE FROM UPC_AB_CONTACT WHERE CONTACT_ID=? AND CONTACT_ID NOT IN (SELECT CONTACT_ID FROM UPC_AB_FOLDCON)");
      pstmt.setInt(1, Integer.parseInt(contactId));
      ds.executePreparedStatement(pstmt);
      ds.commit();
    } catch (Exception e) {
      ds.rollback(e.toString());
    } finally {
      try {     
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace(); 
      }
      ds.release();
    }
    return (iRet > 0);
  }

  /**
   *  Delete contact.
   *
   *@param  folderId       the specified folder.
   *@param  contactId      the specified contact.
   *@return                Success : true. Fail : false.
   *@exception  Exception  Description of the Exception
   */

  // VU
  public void deleteContacts(ContactData[] cdata) throws Exception {
    if (cdata != null && cdata.length > 0) {
      DBService ds = null;
      try {
        PreparedStatement pstmt = null;
        ds = getDBService(DS);
        ds.begin();
        String sql = "DELETE FROM UPC_AB_CONTACT WHERE LOGON_ID=? AND CONTACT_NAME=?";
        for (int i = 0; i < cdata.length; i++) {
          try {
            pstmt = ds.prepareStatement(sql);
            pstmt.setString(1, cdata[i].getOwnerId());
            pstmt.setString(2, cdata[i].getName());
            ds.executePreparedStatement(pstmt);
          } finally {
            try {
              if (pstmt != null) pstmt.close();
              pstmt = null;
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
        ds.commit();
      } catch (Exception e) {
        ds.rollback(e.toString());
      } finally {
        ds.release();
      }
    }
  }


  /**
   *  Update infomation of the specified contact. If ct is null, exception is
   *  thrown and no action is performed.
   *
   *@param  ct             the specified contact.
   *@param  folders        the specified folders.
   *@return                Success : true Fail : false
   *@exception  Exception  Description of the Exception
   *@see                   net.unicon.academus.apps.addressbook.ContactData.
   *@see                   net.unicon.academus.apps.addressbook.FolderData.
   */
  public boolean updateContact(ContactData ct, FolderData[] folders) throws
    Exception {
    DBService ds = null;
    PreparedStatement pstmt = null;
    boolean ret = false;
    try {
      ds = getDBService(DS);
      ds.begin();

      try {
      String prequery = "UPDATE UPC_AB_CONTACT SET CONTACT_NAME=? " +
                        ",EMAIL=? ,MOBILE=? ,TITLE=? ,COMPANY=? " +
                        ",DEPARTMENT=? ,BUSINESSPHONE=? ,FAX=? " +
                        ",OFFICEADDRESS=? ,HOMEPHONE=? ,HOMEADDRESS=? " +
                        ",NOTES=? WHERE CONTACT_ID=?";
      pstmt = ds.prepareStatement(prequery);
      pstmt.setString(1, ct.getName());
      pstmt.setString(2, ct.getEmail());
      pstmt.setString(3, ct.getCellPhone());
      pstmt.setString(4, ct.getTitle());
      pstmt.setString(5, ct.getCompany());
      pstmt.setString(6, ct.getDepartment());
      pstmt.setString(7, ct.getBusinessPhone());
      pstmt.setString(8, ct.getFax());
      pstmt.setString(9, ct.getOfficeAddress());
      pstmt.setString(10, ct.getHomePhone());
      pstmt.setString(11, ct.getHomeAddress());
      pstmt.setString(12, ct.getNotes());
      pstmt.setInt(13, Integer.parseInt(ct.getID()));

      //Update Contact
      ds.executePreparedStatement(pstmt);
      } finally {
        try {     
          if (pstmt != null) pstmt.close();
        } catch (Exception e) {
          e.printStackTrace(); 
        }
      }

      /* VU - begin (05/05/2003) */

      // Folders
      if (folders != null) {
        //Delete Folder
        try {
          pstmt = ds.prepareStatement("DELETE FROM UPC_AB_FOLDCON WHERE CONTACT_ID=?");
          pstmt.setInt(1, Integer.parseInt(ct.getID()));
          ds.executePreparedStatement(pstmt);
        } finally {
          try {     
            if (pstmt != null) pstmt.close();
          } catch (Exception e) {
            e.printStackTrace(); 
          }
        }

        //Insert Folder
        if (folders.length > 0) {
          try {

          pstmt = ds.prepareStatement( "INSERT INTO UPC_AB_FOLDCON(LOGON_ID,CONTACT_ID,FOLDER_ID) VALUES(?,?,?)");
          for (int i = 0; i < folders.length; i++) {
            String folderid = folders[i].getID();
            pstmt.setString(1, ct.getOwnerId());
            pstmt.setInt(2, Integer.parseInt(ct.getID()));
            pstmt.setInt(3, Integer.parseInt(folderid));
            ds.executePreparedStatement(pstmt);
          }

          } finally {
            try {     
              if (pstmt != null) pstmt.close();
            } catch (Exception e) {
              e.printStackTrace(); 
            }
          }
        }
      }
      /* VU - end (05/05/2003) */


      /*
            // Folders
            if (folders != null && folders.length > 0) {
              //Delete Folder
              pstmt = ds.prepareStatement("DELETE FROM UPC_AB_FOLDCON WHERE CONTACT_ID=?");
              pstmt.setInt(1, Integer.parseInt(ct.getID()));
              ds.executePreparedStatement(pstmt);

              //Insert Folder
              pstmt = ds.prepareStatement( "INSERT INTO UPC_AB_FOLDCON(LOGON_ID,CONTACT_ID,FOLDER_ID) VALUES(?,?,?)");
              for (int i = 0; i < folders.length; i++) {
                String folderid = folders[i].getID();
                pstmt.setString(1, ct.getOwnerId());
                pstmt.setInt(2, Integer.parseInt(ct.getID()));
                pstmt.setInt(3, Integer.parseInt(folderid));
                ds.executePreparedStatement(pstmt);
              }
            }
      */
      ds.commit();
      return true;
    } catch (Exception e) {
      ds.rollback(e.toString());
      return false;
    } finally {
      ds.release();
    }
  }

  /**
   *  Create a new folder. If fdata is null, exception is thrown and no action
   *  is performed.
   *
   *@param  fdata          the specified folder.
   *@return                success : Id of the folder has just created. fail :
   *      -1
   *@exception  Exception  Description of the Exception
   *@see                   net.unicon.academus.apps.addressbook.FolderData.
   */
  public int addFolder(FolderData fdata) throws Exception {
    DBService ds = null;
    PreparedStatement pstmt = null;
    int folderId = -1;
    try {
      ds = getDBService(DS);
      ds.begin();
      //folderId = getMax(ds,"UPC_AB_FOLDER","FOLDER_ID",null) + 1;
      folderId = ds.getNextValue("UPC_AB_FOLDER", "FOLDER_ID");
      // Thach-May13
      pstmt = ds.prepareStatement(
                                  "INSERT INTO UPC_AB_FOLDER(LOGON_ID,FOLDER_ID,FOLDER_NAME) VALUES(?,?,?)");
      pstmt.setString(1, fdata.getOwnerId());
      pstmt.setInt(2, folderId);
      pstmt.setString(3, fdata.getName());
      ds.executePreparedStatement(pstmt);
      ds.commit();
    } catch (Exception e) {
      ds.rollback(e.toString());
    } finally {
      try {     
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace(); 
      }
      ds.release();
    }
    return folderId;
  }

  /**
   *  Change the specified folder name. If folderId or newName is null,
   *  exception is thrown and no action is performed.
   *
   *@param  folderId       the specified folder.
   *@param  newName        the new folder name.
   *@return                true : success. false : fail.
   *@exception  Exception  Description of the Exception
   */
  public boolean updateFolder(String folderId, String newName) throws Exception {
    DBService ds = null;
    int iRet = 0;
    PreparedStatement pstmt = null;
    try {
      ds = getDBService(DS);
      ds.begin();
      pstmt = ds.prepareStatement(
          "UPDATE UPC_AB_FOLDER SET FOLDER_NAME=? WHERE FOLDER_ID=?");
      pstmt.setString(1, newName);
      pstmt.setInt(2, Integer.parseInt(folderId));
      iRet = ds.executePreparedStatement(pstmt);
      ds.commit();
    } catch (Exception e) {
      ds.rollback(e.toString());
    } finally {
      try {     
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace(); 
      }
      ds.release();
    }
    return (iRet > 0);
  }

  /**
   *  Delete the specified folder. If folderId is null, exception is thrown
   *  and no action is performed.
   *
   *@param  folderId       the specified folder.
   *@return                true success. false fail.
   *@exception  Exception  Description of the Exception
   */
  public boolean deleteFolder(String folderId) throws Exception {
    DBService ds = null;
    boolean ret = false;
    String ids = "";
    try {
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      ds = getDBService(DS);
      ds.begin();

      try {
        pstmt = ds.prepareStatement(
            "SELECT CONTACT_ID FROM UPC_AB_FOLDCON WHERE FOLDER_ID=?");
        pstmt.setInt(1, Integer.parseInt(folderId));
        rs = pstmt.executeQuery();
        //ResultSet rs = ds.executeQueryPreparedStatement(pstmt);
        while (rs.next()) {
          ids = ids + "," + rs.getString("CONTACT_ID");
        }
      } finally {
        try {   
          if (rs != null) rs.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        try {     
          if (pstmt != null) pstmt.close();
        } catch (Exception e) {
          e.printStackTrace(); 
        }
      }

      try {
      if (ids.equals("")) {
        pstmt = ds.prepareStatement(
                  "DELETE FROM UPC_AB_FOLDER WHERE FOLDER_ID=?");
        pstmt.setInt(1, Integer.parseInt(folderId));
        ds.executePreparedStatement(pstmt);
      } else {
        pstmt = ds.prepareStatement(
                  "DELETE FROM UPC_AB_FOLDCON WHERE FOLDER_ID=?");
        pstmt.setInt(1, Integer.parseInt(folderId));
        ds.executePreparedStatement(pstmt);
        pstmt = ds.prepareStatement(
                  "DELETE FROM UPC_AB_FOLDER WHERE FOLDER_ID=?");
        pstmt.setInt(1, Integer.parseInt(folderId));
        ds.executePreparedStatement(pstmt);
        //ds.update("DELETE FROM UPC_AB_CONTACT WHERE CONTACT_ID IN ("+ ids.substring(1)+")"
        //+ " AND CONTACT_ID NOT IN (SELECT CONTACT_ID FROM UPC_AB_FOLDCON)");
      }
      } finally {
        try {     
          if (pstmt != null) pstmt.close();
        } catch (Exception e) {
          e.printStackTrace(); 
        }
      }
      ds.commit();
      ret = true;
    } catch (Exception e) {
      ds.rollback(e.toString());
    } finally {
      ds.release();
    }
    return ret;
  }

  /**
   *  Get list of folders of the specified users. If userLogon is null,
   *  exception is thrown and no action is performed.
   *
   *@param  userLogon      the specified user.
   *@return                - vector of <code>FolderData<code> object.
   *          - list of folders of the specified users.
   *
   *
   *
   *
   *
   *
   *
   *
   *@exception  Exception  Description of the Exception
   *@see                   net.unicon.academus.apps.addressbook.FolderData.
   */
  public Vector listFolders(String userLogon) throws Exception {
    DBService ds = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Vector v = new Vector();
    try {
      ds = getDBService(DS);
      pstmt = ds.prepareStatement(
          "SELECT FOLDER_ID,FOLDER_NAME FROM UPC_AB_FOLDER" +
          " WHERE LOGON_ID=? ORDER BY FOLDER_NAME ASC");
      pstmt.setString(1, userLogon);
      rs = pstmt.executeQuery();
      //ResultSet rs = ds.executeQueryPreparedStatement(pstmt);
      while (rs.next()) {
        v.addElement(newFolder(rs));
      }
      ds.closeStatement();
    } catch (Exception e) {
      throw e;
    } finally {
      try {   
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {     
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace(); 
      }
      ds.release();
    }
    return v;
  }

  /**
   *  Get detailed information of the specified folder. If folderId is null,
   *  exception is thrown and no action is performed.
   *
   *@param  folderId       the specified folder.
   *@return                detailed information of the specified folder.
   *@exception  Exception  Description of the Exception
   *@see                   net.unicon.academus.apps.addressbook.FolderData.
   */
  public FolderData getFolder(String folderId) throws Exception {
    DBService ds = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    FolderData fdata = null;
    try {
      ds = getDBService(DS);
      pstmt = ds.prepareStatement(
                                  "SELECT FOLDER_ID,FOLDER_NAME FROM UPC_AB_FOLDER WHERE FOLDER_ID=?");
      pstmt.setInt(1, Integer.parseInt(folderId));
      rs = pstmt.executeQuery();
      //ResultSet rs = ds.executeQueryPreparedStatement(pstmt);
      if (rs.next()) {
        fdata = newFolder(rs);
      }
      ds.closeStatement();
    } catch (Exception e) {
      throw e;
    } finally {
      try {   
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {     
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace(); 
      }
      ds.release();
    }
    return fdata;
  }

  /**
   *  Get list of folders which belong to specified contact. If contactId is
   *  null, exception is thrown and no action is performed.
   *
   *@param  contactId      the specified contact.
   *@return                - vector of <code>FolderData</code> object. - list
   *      of folders.
   *@exception  Exception  Description of the Exception
   *@see                   net.unicon.academus.apps.addressbook.FolderData.
   */
  public Vector getFolders(String contactId) throws Exception {
    Vector v = new Vector();
    DBService ds = null;
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    try {
      ds = getDBService(DS);
      pstmt = ds.prepareStatement("SELECT UPC_AB_FOLDER.FOLDER_ID,UPC_AB_FOLDER.FOLDER_NAME FROM UPC_AB_FOLDER,UPC_AB_FOLDCON WHERE UPC_AB_FOLDCON.CONTACT_ID=?"
                                + " AND UPC_AB_FOLDCON.FOLDER_ID=UPC_AB_FOLDER.FOLDER_ID");
      pstmt.setInt(1, Integer.parseInt(contactId));
      rs = pstmt.executeQuery();
      //ResultSet rs = ds.executeQueryPreparedStatement(pstmt);
      while (rs.next()) {
        v.addElement(newFolder(rs));
      }
      ds.closeStatement();
    } catch (Exception e) {
      throw e;
    } finally {
      try {   
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {     
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace(); 
      }
      ds.release();
    }
    return v;
  }

  /**
   *  Check the specified folderName exist in the specified user. If
   *  folderName or userLogon is null, exception is thrown and no action is
   *  performed.
   *
   *@param  folderName     the specified folder.
   *@param  userLogon      the specified user.
   *@return                true : existence. false : not existence.
   *@exception  Exception  Description of the Exception
   */
  public boolean folderExistName(String folderName, String userLogon) throws
    Exception {
    boolean ret = false;
    DBService ds = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      ds = getDBService(DS);
      pstmt = ds.prepareStatement(
                                  "SELECT FOLDER_NAME FROM UPC_AB_FOLDER WHERE FOLDER_NAME=? AND LOGON_ID=?");
      pstmt.setString(1, folderName);
      pstmt.setString(2, userLogon);
      rs = pstmt.executeQuery();
      //ResultSet rs = ds.executeQueryPreparedStatement(pstmt);
      ret = rs.next();
      ds.closeStatement();
    } catch (Exception e) {
      throw e;
    } finally {
      try {   
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {     
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace(); 
      }
      ds.release();
    }
    return ret;
  }

  /**
   *  Check the specified folderID exist. If folderId is null, exception is
   *  thrown and no action is performed.
   *
   *@param  folderId       the specified folder.
   *@return                true : existence. false : not existence.
   *@exception  Exception  Description of the Exception
   *@see                   net.unicon.academus.apps.addressbook.FolderData.
   */
  public boolean folderExistId(String folderId) throws Exception {
    boolean ret = false;
    DBService ds = null;
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    try {
      ds = getDBService(DS);
      pstmt = ds.prepareStatement(
                                  "SELECT FOLDER_ID FROM UPC_AB_FOLDER WHERE FOLDER_ID=?");
      pstmt.setInt(1, Integer.parseInt(folderId));
      rs = pstmt.executeQuery();
      //ResultSet rs = ds.executeQueryPreparedStatement(pstmt);
      ret = rs.next();
      ds.closeStatement();
    } catch (Exception e) {
      throw e;
    } finally {
      try {   
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {     
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace(); 
      }
      ds.release();
    }
    return ret;
  }

  /**
   *  Create a new instance of <code>ContactData</code> and data get from
   *  database.
   *
   *@param  rs             contains data.
   *@return                a new instance of <code>ContactData</code>.
   *@exception  Exception  Description of the Exception
   *@see                   net.unicon.academus.apps.addressbook.ContactData.
   */
  private ContactData newContact(ResultSet rs) throws Exception {
    ContactData cdata = new ContactData();

    cdata.putOwnerId(rs.getString("logon_id"));
    //LOGON_ID
    cdata.putOID(rs.getString("contact_id"));
    //CONTACT_ID
    cdata.putName(rs.getString("contact_name"));
    //CONTACT_NAME
    cdata.putEmail(rs.getString("email"));
    //EMAIL
    cdata.putCellPhone(rs.getString("mobile"));
    //MOBILE
    cdata.putTitle(rs.getString("title"));
    //TITLE
    cdata.putCompany(rs.getString("company"));
    //COMPANY
    cdata.putDepartment(rs.getString("department"));
    //DEPARTMENT
    cdata.putBusinessPhone(rs.getString("businessphone"));
    //BUSINESSPHONE
    cdata.putFax(rs.getString("fax"));
    //FAX
    cdata.putOfficeAddress(rs.getString("officeaddress"));
    //OFFICEADDRESS
    cdata.putHomePhone(rs.getString("homephone"));
    //HOMEPHONE
    cdata.putHomeAddress(rs.getString("homeaddress"));
    //HOMEADDRESS
    cdata.putNotes(rs.getString("notes"));
    //NOTES
    cdata.putRef(rs.getString("ref"));
    //REF
    return cdata;
  }

  /**
   *  Create a new instance of FolderData and data get from database.
   *
   *@param  rs             contains data.
   *@return                a new instance of FolderData.
   *@exception  Exception  Description of the Exception
   *@see                   net.unicon.academus.apps.addressbook.FolderData.
   */
  private FolderData newFolder(ResultSet rs) throws Exception {
    FolderData fd = new FolderData();
    fd.putOID(rs.getString(1));
    // FOLDER_ID
    fd.putName(rs.getString(2));
    // FOLDER_NAME
    return fd;
  }

  /**
   *  Generate a number is a next number in specified field and specified
   *  table with specified condition.
   *
   *@param  ds             it is used to operate on database.
   *@param  table          a specified table.
   *@param  field          a specified field.
   *@param  where          a specified condition.
   *@return                A number is next number.
   *@exception  Exception  Description of the Exception
   *@see                   net.unicon.academus.apps.rad.DBService.
   */
  private int getMax(DBService ds, String table, String field, String where) throws
    Exception {
    int ret = 0;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      //ResultSet rs = (where==null)? ds.select("SELECT MAX("+field+") FROM "+table):
      //ds.select("SELECT MAX ("+field+") FROM "+table+" WHERE "+where);
      String sql = "";
      if (where == null) {
        sql = "SELECT MAX(" + field + ") FROM " + table;
      } else {
        sql = "SELECT MAX (" + field + ") FROM " + table + " WHERE " + where;
      }
      pstmt = ds.prepareStatement(sql);
      rs = pstmt.executeQuery();
      if (rs.next()) {
        ret = rs.getInt(1);
      }
      ds.closeStatement();
    } catch (Exception e) {
      throw e;
    } finally {
      try {   
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {     
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        e.printStackTrace(); 
      }
      //ds.release(); it is release on function used it
    }
    return (ret < 0 ? 0 : ret);
  }

  /**
   *  Convert a array of args to string. Each arg Example : args(1,2,4,5,6,7);
   *  convertIN(args); Result : "1,2,3,4,5,6,7"
   *
   *@param  args  list of arguments
   *@return       string of args
   */
  private String convertIN(String args[]) {
    if (args == null) {
      return null;
    }
    if (args.length == 0) {
      return null;
    }
    String ret = "";
    for (int i = 0; i < args.length; i++) {
      ret = ret + "," + args[i];
    }
    ret = ret.substring(1);
    return ret;
  }
}
