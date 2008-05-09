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

import java.util.Map;
import java.util.HashMap;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.jasig.portal.IMimeResponse;
import org.jasig.portal.services.LogService;

import net.unicon.academus.apps.addressbook.AddressBookBoHome;
import net.unicon.academus.apps.addressbook.AddressBookBoRemote;
import net.unicon.academus.apps.addressbook.ContactData;
import net.unicon.academus.apps.addressbook.ExportImport;
import net.unicon.portal.channels.rad.Screen;


/**
 * It is main screen.
 */
abstract public class AddressBookScreen extends Screen implements IMimeResponse {
  /**
   *  Override from <code>IMimeResponse</code> in rad.
   *  @see net.unicon.portal.channels.rad.MimeResponseChannel.
   */
  public Map getHeaders() {
    HashMap attachmentHeaders = new HashMap();
    attachmentHeaders.put("Content-Disposition", "attachment; filename=\"addressbook.txt\"");
    return attachmentHeaders;
  }
  /**
   *  Override from <code>IMimeResponse</code> in rad.
   *  @see net.unicon.portal.channels.rad.MimeResponseChannel.
   */
  public String getName() {
    return null;
  }
  /**
   *  Override from <code>IMimeResponse</code> in rad.
   *  @see net.unicon.portal.channels.rad.MimeResponseChannel.
   */
  public void downloadData(OutputStream out) throws IOException {}
  /**
   *  Override from <code>IMimeResponse</code> in rad.
   *  @see net.unicon.portal.channels.rad.MimeResponseChannel.
   */
  public String getContentType() {
    return null; //Tien 0521
    //return "zip";
  }
  /**
   *  Override from <code>IMimeResponse</code> in rad.
   *  @see net.unicon.portal.channels.rad.MimeResponseChannel.
   */
  public InputStream getInputStream() throws IOException {
    try {
      ContactData[] contacts = (ContactData[])(getBo().listAllContacts(this.getUserLogon()).toArray(new ContactData[0]));
      //return new ByteArrayInputStream(ExportImport.exportContactDataByte(contacts));
      return ExportImport.exportContactDataStream(contacts);
    } catch (Exception e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Let the channel know that there were problems with the download
   * @param e
   */
  public void reportDownloadError(Exception e) {
    LogService.log(LogService.ERROR, getClass().getName()+"::reportDownloadError(): " + e.getMessage());
  }

  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public String getVersion() {
    return CAddressBook.VERSION;
  }
  /**
   *  Get <code>AddressBookBoRemote</code> object to operate on database.
   */
  public AddressBookBoRemote getBo() throws Exception {
    AddressBookBoRemote ejb = (AddressBookBoRemote)m_channel.getEjb("CAddressBook.AddressBookBo");
    if( ejb == null) {
      AddressBookBoHome home = (AddressBookBoHome)m_channel.ejbHome("AddressBookBo");
      ejb = home.create();
      m_channel.putEjb("CAddressBook.AddressBookBo",ejb);
    }
    return ejb;
  }
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public String buildXML() throws Exception {
    StringBuffer xml = new StringBuffer();
    xml.append("<?xml version='1.0'?>");
    xml.append("<addressbook-system>");
    printXML(xml);
    xml.append("</addressbook-system>");
    return xml.toString();
  }
  /**
   * @param xml contains xml data.
   * Get xml data.
   */
  abstract public void printXML(StringBuffer xml) throws Exception;

  /**
   * @return Name of user login in your system.
   * Get portal user login in your system.
   */
  public String getUserLogon() {
    return m_channel.logonUser().getAlias();
  }
}
