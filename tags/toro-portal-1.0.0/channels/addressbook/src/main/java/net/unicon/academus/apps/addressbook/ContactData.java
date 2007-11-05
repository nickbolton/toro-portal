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

import java.io.Serializable;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XML;
import net.unicon.academus.apps.rad.XMLData;
/**
 * A <code>ContactData</code> object contains data of contact.
 *  type : "E"
 *  entityType : "a"
 *  id : contactID
 *  name : fullname
 * @version
 *@author
 *@created    April 9, 2003
 *@version
 */

public class ContactData extends IdentityData implements Serializable {
  //,Cloneable

  /**
   *  Description of the Field
   */
  public final static String S_CONTACT = "a";
  //Constant
  /**
   *  Description of the Field
   */
  public final static int MAX_NAME_LENGTH = 64;
  /**
   *  Description of the Field
   */
  public final static int MAX_EMAIL_LENGTH = 64;
  /**
   *  Description of the Field
   */
  public final static int MAX_MOBILE_LENGTH = 24;
  /**
   *  Description of the Field
   */
  public final static int MAX_TITLE_LENGTH = 46;
  /**
   *  Description of the Field
   */
  public final static int MAX_COMPANY_LENGTH = 64;
  /**
   *  Description of the Field
   */
  public final static int MAX_DEPARTMENT_LENGTH = 64;
  /**
   *  Description of the Field
   */
  public final static int MAX_BUSINESS_PHONE_LENGTH = 24;
  /**
   *  Description of the Field
   */
  public final static int MAX_FAX_LENGTH = 24;
  /**
   *  Description of the Field
   */
  public final static int MAX_OFFICE_ADDRESS_LENGTH = 128;
  /**
   *  Description of the Field
   */
  public final static int MAX_HOME_PHONE_LENGTH = 24;
  /**
   *  Description of the Field
   */
  public final static int MAX_HOME_ADDRESS_LENGTH = 128;
  /**
   *  Description of the Field
   */
  public final static int MAX_NOTES_LENGTH = 128;

  /**
   *  Create a new instance of <code>ContactData</code> with empty data.
   */
  public ContactData() {
    putType(ENTITY);
    putEntityType(S_CONTACT);
  }

  /**
   *  Create a new instance of <code>ContactData</code> with <code>IdentityData</code>
   *  data.
   *
   *@param  id  IdentityData data.
   *@see        net.unicon.academus.apps.rad.IdentityData.
   */
  public ContactData(IdentityData id) {
    this.putName(id.getName());
    this.putEmail(id.getEmail());
  }

  /**
   * Create a new instance of <code>ContactData</code> with <code>IdentityData</code> data.
   *@param  contactId  Id of contact.
   * @param fullName    Name of contact.
   */
  public ContactData(String contactId, String fullname) {
    super(ENTITY, S_CONTACT, contactId, fullname);
  }

  public void copyFrom(ContactData other) {
    this.putName( other.getName());
    this.putEmail(other.getEmail());
    this.putCellPhone(other.getCellPhone());
    this.putTitle(other.getTitle());
    this.putCompany(other.getCompany());
    this.putDepartment(other.getDepartment());
    this.putBusinessPhone(other.getBusinessPhone());
    this.putFax(other.getFax());
    this.putOfficeAddress(other.getOfficeAddress());
    this.putHomePhone(other.getHomePhone());
    this.putHomeAddress(other.getHomeAddress());
    this.putNotes(other.getNotes());
  }

  /**
   *  Get BusinessPhone from contact.
   *
   *@return    The businessPhone value
   */
  public String getBusinessPhone() {
    return (String)getE("business-phone");
  }
  /**
   *  Set BusinessPhone from contact.
   *
   *@param  businessPhone  Description of the Parameter
   */
  public void putBusinessPhone(String businessPhone) {
    putE("business-phone",businessPhone);
  }
  /**
   *  Get Notes from contact.
   *
   *@return    The notes value
   */
  public String getNotes() {
    return (String)getE("notes");
  }
  /**
   *  Set Notes from contact.
   *
   *@param  notes  Description of the Parameter
   */
  public void putNotes(String notes) {
    putE("notes",notes);
  }
  /**
   *  Get HomeAddress from contact.
   *
   *@return    The homeAddress value
   */
  public String getHomeAddress() {
    return (String)getE("home-address");
  }
  /**
   *  Set HomeAddress from contact.
   *
   *@param  homeaddress  Description of the Parameter
   */
  public void putHomeAddress(String homeaddress) {
    putE("home-address",homeaddress);
  }
  /**
   *  Get HomePhone from contact.
   *
   *@return    The homePhone value
   */
  public String getHomePhone() {
    return (String)getE("home-phone");
  }
  /**
   *  Set HomePhone from contact.
   *
   *@param  homephone  Description of the Parameter
   */
  public void putHomePhone(String homephone) {
    putE("home-phone",homephone);
  }
  /**
   *  Get Fax from contact.
   *
   *@return    The fax value
   */
  public String getFax() {
    return (String)getE("fax");
  }
  /**
   *  Set Fax from contact.
   *
   *@param  fax  Description of the Parameter
   */
  public void putFax(String fax) {
    putE("fax",fax);
  }
  /**
   *  Get OfficeAddress from contact.
   *
   *@return    The officeAddress value
   */
  public String getOfficeAddress() {
    return (String)getE("office-address");
  }
  /**
   *  Set OfficeAddress from contact.
   *
   *@param  officeaddress  Description of the Parameter
   */
  public void putOfficeAddress(String officeaddress) {
    putE("office-address",officeaddress);
  }
  /**
   *  Get CellPhone from contact.
   *
   *@return    The cellPhone value
   */
  public String getCellPhone() {
    return (String)getE("cell-phone");
  }
  /**
   *  Set CellPhone from contact.
   *
   *@param  cellPhone  Description of the Parameter
   */
  public void putCellPhone(String cellPhone) {
    putE("cell-phone",cellPhone);
  }
  /**
   *  Get Title from contact.
   *
   *@return    The title value
   */
  public String getTitle() {
    return (String)getE("title");
  }
  /**
   *  Set Title from contact.
   *
   *@param  title  Description of the Parameter
   */
  public void putTitle(String title) {
    putE("title",title);
  }
  /**
   *  Get Company from contact.
   *
   *@return    The company value
   */
  public String getCompany() {
    return (String)getE("company");
  }
  /**
   *  Set Company from contact.
   *
   *@param  company  Description of the Parameter
   */
  public void putCompany(String company) {
    putE("company",company);
  }
  /**
   *  Get Department from contact.
   *
   *@return    The department value
   */
  public String getDepartment() {
    return (String)getE("department");
  }
  /**
   *  Set Department from contact.
   *
   *@param  department  Description of the Parameter
   */
  public void putDepartment(String department) {
    putE("department",department);
  }
  /**
   *  Get Owner from contact.
   *
   *@return    The ownerId value
   */
  public String getOwnerId() {
    return (String)getE("ownerid");
  }
  /**
   *  Set Owner from contact.
   *
   *@param  ownerId  Description of the Parameter
   */
  public void putOwnerId(String ownerId) {
    putE("ownerid",ownerId);
  }
  /**
   *  Get Ref from contact.
   *
   *@return    The ref value
   */
  public String getRef() {
    String portal = getRefID("portal");
    portal = (portal != null) ? "u:" + portal : null;
    String campus = getRefID("campus");
    campus = (campus != null) ? "p:" + campus : null;
    return (portal != null ? portal : campus != null ? campus : null);
  }

  /**
   *  Set Ref from contact.
   *
   *@param  ref  Description of the Parameter
   */
  public void putRef(String ref) {
    if (ref != null && ref.length() > 2) {
      String op = ref.substring(0, 1);
      String refID = ref.substring(2);
      if( op.startsWith("u"))
        putRefID("portal", refID);
      else if( op.startsWith("p"))
        putRefID("campus", refID);
    }
  }
  /**
   *  Convert data of contact to xml structure.
   *
   *@param  xml  contains data of contact.
   */
  public void printXML(StringBuffer xml) {
    xml.append("<contact id= '" + ((String) getID() != null ? XML.esc((String) getID()) : "") + "'>");
    xml.append("<name>" + ((String) getName() != null ? XML.esc((String) getName()) : "") + "</name>");
    xml.append("<email>" + ((String) getEmail() != null ? XML.esc((String) getEmail()) : "") + "</email>");
    xml.append("<cell-phone>" + ((String) getCellPhone() != null ? XML.esc((String) getCellPhone()) : "") + "</cell-phone>");
    xml.append("<title>" + ((String) getTitle() != null ? XML.esc((String) getTitle()) : "") + "</title>");
    xml.append("<company>" + ((String) getCompany() != null ? XML.esc((String) getCompany()) : "") + "</company>");
    xml.append("<department>" + ((String) getDepartment() != null ? XML.esc((String) getDepartment()) : "") + "</department>");
    xml.append("<business-phone>" + ((String) getBusinessPhone() != null ? XML.esc((String) getBusinessPhone()) : "") + "</business-phone>");
    xml.append("<fax>" + ((String) getFax() != null ? XML.esc((String) getFax()) : "") + "</fax>");
    xml.append("<office-address>" + ((String) getOfficeAddress() != null ? XML.esc((String) getOfficeAddress()) : "") + "</office-address>");
    xml.append("<home-phone>" + ((String) getHomePhone() != null ? XML.esc((String) getHomePhone()) : "") + "</home-phone>");
    xml.append("<home-address>" + ((String) getHomeAddress() != null ? XML.esc((String) getHomeAddress()) : "") + "</home-address>");
    xml.append("<notes>" + ((String) getNotes() != null ? XML.esc((String) getNotes()) : "") + "</notes>");
    xml.append("</contact>");
  }
}
