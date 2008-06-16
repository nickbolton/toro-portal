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
/*
 *  This is Remote interface for AddressBookBo. It is generated by Skeler by IBS-DP
 *  DO NOT MODIFY
 */
package net.unicon.academus.apps.addressbook;

/**
 *  Description of the Interface
 *
 *@author     nvvu
 *@created    April 9, 2003
 */
public interface AddressBookBoRemote extends javax.ejb.EJBObject, java.io.Serializable {

  /**
   *  Description of the Method
   *
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public void ejbActivate() throws java.rmi.RemoteException;

  /**
   *  Description of the Method
   *
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public void ejbPassivate() throws java.rmi.RemoteException;

  /**
   *  Sets the sessionContext attribute of the AddressBookBoRemote object
   *
   *@param  arg0                          The new sessionContext value
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public void setSessionContext(javax.ejb.SessionContext arg0) throws java.rmi.RemoteException;

  //Contact
  /**
   *  Adds a feature to the Contacts attribute of the AddressBookBoRemote object
   *
   *@param  arg0                          The feature to be added to the
   *      Contacts attribute
   *@return                               Description of the Return Value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public boolean addContacts(net.unicon.academus.apps.addressbook.ContactData[] arg0) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Adds a feature to the Contact attribute of the AddressBookBoRemote object
   *
   *@param  arg0                          The feature to be added to the Contact
   *      attribute
   *@param  arg1                          The feature to be added to the Contact
   *      attribute
   *@return                               Description of the Return Value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public int addContact(net.unicon.academus.apps.addressbook.ContactData arg0, net.unicon.academus.apps.addressbook.FolderData[] arg1) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Description of the Method
   *
   *@param  arg0                          Description of the Parameter
   *@param  arg1                          Description of the Parameter
   *@return                               Description of the Return Value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public java.util.Vector listContacts(java.lang.String arg0, java.lang.String arg1) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Description of the Method
   *
   *@param  arg0                          Description of the Parameter
   *@return                               Description of the Return Value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public java.util.Vector listAllContacts(java.lang.String arg0) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Description of the Method
   *
   *@param  arg0                          Description of the Parameter
   *@param  arg1                          Description of the Parameter
   *@param  arg2                          Description of the Parameter
   *@param  arg3                          Description of the Parameter
   *@param  arg4                          Description of the Parameter
   *@param  arg5                          Description of the Parameter
   *@return                               Description of the Return Value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public java.util.Vector searchContacts(java.lang.String arg0, java.lang.String arg1, java.lang.String arg2, java.lang.String arg3, java.lang.String arg4, int arg5) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Description of the Method
   *
   *@param  arg0                          Description of the Parameter
   *@param  arg1                          Description of the Parameter
   *@param  arg2                          Description of the Parameter
   *@param  arg3                          Description of the Parameter
   *@param  arg4                          Description of the Parameter
   *@param  arg5                          Description of the Parameter
   *@param  arg6                          Description of the Parameter
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public void searchContacts(java.util.Vector arg0, java.lang.String arg1, java.lang.String arg2, java.lang.String arg3, java.lang.String arg4, java.lang.String arg5, int arg6) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Gets the contact attribute of the AddressBookBoRemote object
   *
   *@param  arg0                          Description of the Parameter
   *@return                               The contact value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public net.unicon.academus.apps.addressbook.ContactData getContact(java.lang.String arg0) throws java.lang.Exception, java.rmi.RemoteException;

  public net.unicon.academus.apps.addressbook.ContactData getContactByName(java.lang.String arg0,java.lang.String arg1) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Gets the contacts attribute of the AddressBookBoRemote object
   *
   *@param  arg0                          Description of the Parameter
   *@return                               The contacts value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public net.unicon.academus.apps.addressbook.ContactData[] getContacts(java.lang.String arg0[]) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Gets the contactByRef attribute of the AddressBookBoRemote object
   *
   *@param  arg0                          Description of the Parameter
   *@return                               The contactByRef value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public net.unicon.academus.apps.addressbook.ContactData getContactByRef(java.lang.String arg0) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Gets the contactByRef attribute of the AddressBookBoRemote object
   *
   *@param  arg0                          Description of the Parameter
   *@param  arg1                          Description of the Parameter
   *@return                               The contactByRef value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public net.unicon.academus.apps.addressbook.ContactData getContactByRef(java.lang.String arg0, java.lang.String arg1) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Gets the contactByRefs attribute of the AddressBookBoRemote object
   *
   *@param  arg0                          Description of the Parameter
   *@return                               The contactByRefs value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public net.unicon.academus.apps.addressbook.ContactData[] getContactByRefs(java.lang.String arg0[]) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Description of the Method
   *
   *@param  arg0                          Description of the Parameter
   *@param  arg1                          Description of the Parameter
   *@param  arg2                          Description of the Parameter
   *@return                               Description of the Return Value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public java.util.Vector searchFolders(String arg0, String arg1, int arg2) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Description of the Method
   *
   *@param  arg0                          Description of the Parameter
   *@param  arg1                          Description of the Parameter
   *@return                               Description of the Return Value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public boolean deleteContact(java.lang.String arg0, java.lang.String arg1) throws java.lang.Exception, java.rmi.RemoteException;

  public void deleteContacts(net.unicon.academus.apps.addressbook.ContactData[] arg0) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Description of the Method
   *
   *@param  arg0                          Description of the Parameter
   *@param  arg1                          Description of the Parameter
   *@return                               Description of the Return Value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public boolean updateContact(net.unicon.academus.apps.addressbook.ContactData arg0, net.unicon.academus.apps.addressbook.FolderData[] arg1) throws java.lang.Exception, java.rmi.RemoteException;

  //Folder or group
  /**
   *  Adds a feature to the Folder attribute of the AddressBookBoRemote object
   *
   *@param  arg0                          The feature to be added to the Folder
   *      attribute
   *@return                               Description of the Return Value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public int addFolder(net.unicon.academus.apps.addressbook.FolderData arg0) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Description of the Method
   *
   *@param  arg0                          Description of the Parameter
   *@param  arg1                          Description of the Parameter
   *@return                               Description of the Return Value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public boolean updateFolder(java.lang.String arg0, java.lang.String arg1) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Description of the Method
   *
   *@param  arg0                          Description of the Parameter
   *@return                               Description of the Return Value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public boolean deleteFolder(java.lang.String arg0) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Description of the Method
   *
   *@param  arg0                          Description of the Parameter
   *@return                               Description of the Return Value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public java.util.Vector listFolders(java.lang.String arg0) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Gets the folder attribute of the AddressBookBoRemote object
   *
   *@param  arg0                          Description of the Parameter
   *@return                               The folder value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public net.unicon.academus.apps.addressbook.FolderData getFolder(java.lang.String arg0) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Gets the folders attribute of the AddressBookBoRemote object
   *
   *@param  arg0                          Description of the Parameter
   *@return                               The folders value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public java.util.Vector getFolders(java.lang.String arg0) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Description of the Method
   *
   *@param  arg0                          Description of the Parameter
   *@param  arg1                          Description of the Parameter
   *@return                               Description of the Return Value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public boolean folderExistName(java.lang.String arg0, java.lang.String arg1) throws java.lang.Exception, java.rmi.RemoteException;

  /**
   *  Description of the Method
   *
   *@param  arg0                          Description of the Parameter
   *@return                               Description of the Return Value
   *@exception  java.lang.Exception       Description of the Exception
   *@exception  java.rmi.RemoteException  Description of the Exception
   */
  public boolean folderExistId(java.lang.String arg0) throws java.lang.Exception, java.rmi.RemoteException;
}
