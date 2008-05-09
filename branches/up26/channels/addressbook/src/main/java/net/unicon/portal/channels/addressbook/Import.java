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



import java.util.*;

import java.util.Vector;

import java.util.Hashtable;

import java.util.Enumeration;

import java.util.Comparator;



import org.jasig.portal.MultipartDataSource;



import net.unicon.academus.apps.addressbook.ContactData;
import net.unicon.academus.apps.addressbook.ExportImport;
import net.unicon.academus.apps.rad.Sorted;
import net.unicon.academus.apps.rad.TDLine;
import net.unicon.academus.apps.rad.XML;
import net.unicon.portal.channels.rad.Screen;










/**

*  Description of the Class

*

*@author     nvvu

*@created    April 9, 2003

*/

public class Import extends AddressBookScreen {

  Screen m_back = null;

  /*

   *  Override from Screen in rad.

   */

  public final static String SID = "Import";



  /*

   *  Contain data from file.

   */

  private TDLine[] m_data = null;



  /*

   *  Content of cbo

   */

  private String m_firstName = null;

  private String m_lastName = null;

  private String m_middleName = null;

  private String m_email = null;

  private String m_mobile = null;

  private String m_title = null;

  private String m_company = null;

  private String m_department = null;

  private String m_businessPhone = null;

  private String m_fax = null;

  private String m_officeAddress = null;

  private String m_homePhone = null;

  private String m_homeAddress = null;

  private String m_note = null;



  private boolean m_isOpen = false;



  /**

   *  Override from <code>Screen</code> in rad.

   *

   *@return    Description of the Return Value

   *@see       net.unicon.portal.channels.rad.Screen.

   */

  public String sid() {

    return SID;

  }



  /**

   *  Override from <code>Screen</code> in rad.

   *

   *@param  params         Description of the Parameter

   *@exception  Exception  Description of the Exception

   *@see                   net.unicon.portal.channels.rad.Screen.

   */

  public void init(Hashtable params) throws Exception {

    String back = (String)params.get("back");

    if( back == null)

      back = Personal.SID;

    m_back = getScreen(back);

  }



  /**

   *  Override from <code>Screen</code> in rad.

   *

   *@param  params         Description of the Parameter

   *@exception  Exception  Description of the Exception

   *@see                   net.unicon.portal.channels.rad.Screen.

   */

  public void reinit(Hashtable params) throws Exception {

    m_isOpen = false;

    m_data = null;

  }



  /**

   *  Override from <code>Screen</code> in rad.

   *

   *@return    The xSLTParams value

   *@see       net.unicon.portal.channels.rad.Screen.

   */

  public Hashtable getXSLTParams() {

    Hashtable params = new Hashtable();

    params.put("isOpen", (new Boolean(m_isOpen)).toString());

    if( m_firstName != null)

      params.put("first-name", m_firstName);

    if( m_lastName != null)

      params.put("last-name", m_lastName);

    if( m_middleName != null)

      params.put("middle-name", m_middleName);

    if( m_email != null)

      params.put("email", m_email);

    if( m_mobile != null)

      params.put("mobile", m_mobile);

    if( m_title != null)

      params.put("title", m_title);

    if( m_company != null)

      params.put("company", m_company);

    if( m_department != null)

      params.put("department", m_department);

    if( m_businessPhone != null)

      params.put("business-phone", m_businessPhone);

    if( m_fax != null)

      params.put("fax", m_fax);

    if( m_officeAddress != null)

      params.put("office-address", m_officeAddress);

    if( m_homePhone != null)

      params.put("home-phone", m_homePhone);

    if( m_homeAddress != null)

      params.put("home-address", m_homeAddress);

    if( m_note != null)

      params.put("notes", m_note);

    return params;

  }



  /**

   *  Override from <code>AddressBookScreen</code>.

   *

   *@param  xml            Description of the Parameter

   *@exception  Exception  Description of the Exception

   *@see net.unicon.portal.channels.addressbook.AddressBookScreen.

   */

  public void printXML(StringBuffer xml) throws Exception {

    xml.append("<fields>");

    if (m_data != null && m_data.length > 0) {

      TDLine header = m_data[0];

      for (int i = 0; i < header.size(); i++)

        xml.append("<field name='" + XML.esc( (String) header.elementAt(i)) + "'/>");

    }

    xml.append("</fields>");

  }



  /**

   *  Load data from file. init data in combo box.

   *

   *@param  params         Description of the Parameter

   *@return                Description of the Return Value

   *@exception  Exception  Description of the Exception

   */

  public Screen go(Hashtable params) throws Exception {

    MultipartDataSource[] fileParts = (MultipartDataSource[]) params.get("file");

    if (fileParts != null) {

      try {

        m_data = TDLine.loadDataFromDelimitedFile( fileParts[0].getInputStream(),'\t');

        // Default mapping

        if (fileParts[0].getContentType().equals("text/plain") && m_data != null && m_data.length > 0) {

          TDLine header = m_data[0];

          m_firstName = selectMap((String)ExportImport.HEADER.elementAt(0), header);

          m_middleName = selectMap((String)ExportImport.HEADER.elementAt(1), header);

          m_lastName = selectMap((String)ExportImport.HEADER.elementAt(2), header);

          m_email = selectMap((String)ExportImport.HEADER.elementAt(3), header);

          m_mobile = selectMap((String)ExportImport.HEADER.elementAt(4), header);

          m_title = selectMap((String)ExportImport.HEADER.elementAt(5), header);

          m_company = selectMap((String)ExportImport.HEADER.elementAt(6), header);

          m_department = selectMap((String)ExportImport.HEADER.elementAt(7), header);

          m_businessPhone = selectMap((String)ExportImport.HEADER.elementAt(8), header);

          m_fax = selectMap((String)ExportImport.HEADER.elementAt(9), header);

          m_officeAddress = selectMap((String)ExportImport.HEADER.elementAt(10), header);

          m_homePhone = selectMap((String)ExportImport.HEADER.elementAt(11), header);

          m_homeAddress = selectMap((String)ExportImport.HEADER.elementAt(12), header);

          m_note = selectMap((String)ExportImport.HEADER.elementAt(13), header);

        }
	else {
	  return error(CAddressBook.ERROR_INVALID_FILE);
	}

      } catch (Exception e) {

        return error(CAddressBook.ERROR_INVALID_FILE);

      }

      m_isOpen = true;

    }

    return this;

  }



  String selectMap( String org, TDLine header) {

    if( header.size() == 0)

      return null;



    int minIdx = 0;

    int minVal = 3;

    for (int i = 0; i < header.size(); i++) {

      String field = (String) header.elementAt(i);

      int val = compare(org, field);

      if( val < minVal) {

        minIdx = i;

        minVal = val;

      }

    }

    //System.out.println("^^^selectMap:org="+org+",map="+(String)header.elementAt(minIdx));

    return (String)header.elementAt(minIdx);

  }



  int compare(String org, String other) {

    if( org.equals(other))

      return 0;

    if( org.equalsIgnoreCase(other))

      return 1;

    if( org.indexOf(other) != -1 || other.indexOf(org) != -1)

      return 2;

    return 3;

  }



  /**

   *  Import contact. Save in database if database is good. Comfirm if

   *  database is wrong. Go to "Personal" screen.

   *

   *@param  params         Description of the Parameter

   *@return                Description of the Return Value

   *@exception  Exception  Description of the Exception

   */

  public Screen ok(Hashtable params) throws Exception {


    getContactOnForm(params);

    String[] map = new String[14];

    map[0] = m_firstName;

    map[1] = m_middleName;

    map[2] = m_lastName;

    map[3] = m_email;

    map[4] = m_mobile;

    map[5] = m_title;

    map[6] = m_company;

    map[7] = m_department;

    map[8] = m_businessPhone;

    map[9] = m_fax;

    map[10] = m_officeAddress;

    map[11] = m_homePhone;

    map[12] = m_homeAddress;

    map[13] = m_note;

    // Import to buffer... (It also checks validity of data)

    ContactData[] contacts = ExportImport.importContactData(m_data, map, getUserLogon());

    if( contacts == null) // no data to import

      return this;



    // Check duplicate...

    boolean dup = false;

    Vector existings = getBo().listAllContacts(getUserLogon());

    for( int i = 0; i < existings.size() && dup == false; i++) {

      ContactData ct = (ContactData)existings.elementAt(i);

      dup = contains( contacts, ct);

    }


    // Ask if duplicate found

    Hashtable parameters = new Hashtable();

    parameters.put("contacts", contacts);

    parameters.put("option", "replace");

    if( dup) {

      Screen confirm = getScreen("Confirm");

      if (confirm == null)

        confirm = makeScreen("Confirm");

      parameters.put("back", sid());

      parameters.put("methodName", "importDatabase");

      confirm.init(parameters);

      return confirm;

    } else

      return importDatabase(parameters);

  }



  static boolean contains( ContactData[] contacts, ContactData ct) {

    String n = ct.getName();

    for( int i = 0; i < contacts.length; i++) {

      String name = contacts[i].getName();

      if( name.equals(n))

        return true;

    }

    return false;

  }



  /**

   *  Save contacts into database.

   *

   *@param  params         Description of the Parameter

   *@return                Description of the Return Value

   *@exception  Exception  Description of the Exception

   */

  public Screen importDatabase(Hashtable params) throws Exception {

System.out.println("Import.importDatabase:"+params);

    String logonUser = getUserLogon();

    ContactData[] contacts = (ContactData[]) params.get("contacts");

    String option = (String)params.get("option");

    boolean replace = ( option != null && option.equals("replace"));

    boolean add

      = ( option != null && option.equals("add"));

    boolean skip = ( option != null && option.equals("skip"));

    if (contacts != null && contacts.length > 0) {

      for (int i = 0; i < contacts.length; i++) {

        if( replace) {

          ContactData old = getBo().getContactByName(contacts[i].getName(),logonUser);

          if( old != null) {

            old.copyFrom(contacts[i]);

            getBo().updateContact(old,null);
System.out.println("Import.importDatabase:replace "+contacts[i].getName());

          } else {

            getBo().addContact(contacts[i], null);
System.out.println("Import.importDatabase:add "+contacts[i].getName());
          }

        } else if( add) {

            getBo().addContact(contacts[i],null);
System.out.println("Import.importDatabase:add "+contacts[i].getName());
        }
        else if( skip) {

          ContactData old = getBo().getContactByName(contacts[i].getName(), logonUser);

          if( old == null) {

System.out.println("Import.importDatabase:add-skip "+contacts[i].getName());
            getBo().addContact(contacts[i],null);
          }
          else
System.out.println("Import.importDatabase:skip "+contacts[i].getName());

        }

      }

    }

    return ( (Personal) getScreen(Personal.SID)).refreshContactOnScreen(params);

  }



  /**

   *  Cancel, go to "Personal" Screen.

   *

   *@param  params         Description of the Parameter

   *@return                Description of the Return Value

   *@exception  Exception  Description of the Exception

   */

  public Screen cancel(Hashtable params) throws Exception {

    return getScreen(Personal.SID);

  }



  /**

   *  Get data from form.

   *

   *@param  params  Description of the Parameter

   */

  private void getContactOnForm(Hashtable params) {

    m_firstName = (String)params.get("first-name");

    m_lastName = (String)params.get("last-name");

    m_middleName = (String)params.get("middle-name");

    m_email = (String)params.get("email");

    m_mobile = (String)params.get("mobile");

    m_title = (String)params.get("title");

    m_company = (String)params.get("company");

    m_department = (String)params.get("department");

    m_businessPhone = (String)params.get("business-phone");

    m_fax = (String)params.get("fax");

    m_officeAddress = (String)params.get("office-address");

    m_homePhone = (String)params.get("home-phone");

    m_homeAddress = (String)params.get("home-address");

    m_note = (String)params.get("notes");

  }

}



