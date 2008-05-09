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



import java.io.InputStream;

import java.io.ByteArrayInputStream;

import java.io.IOException;

import java.io.InputStreamReader;

import java.io.LineNumberReader;

import java.io.Reader;

import java.util.Hashtable;

import java.util.StringTokenizer;

import java.util.Enumeration;

import java.util.Vector;



import net.unicon.academus.apps.rad.TDLine;



//Temp for test.

import java.io.FileInputStream;

import java.io.File;



/**

 *  Description of the Class

 *

 *@author     nvvu

 *@created    April 9, 2003

 */

public final class ExportImport {

  private final static char TAB = '\t';

  private final static char NEWLINE = '\n';

  private final static char LINEFEED = '\r';



  // Tien 0521

  public static final TDLine HEADER = new TDLine( new String[] {

                                        "First Name", "Middle Name", "Last Name", "E-mail Address", "Mobile Phone",

                                        "Job Title", "Company", "Department", "Business Phone", "Business Fax",

                                        "Business Street", "Home Phone", "Home Street", "Notes"});

  //



  /*

   *  Have to order 0,1,2,3,4,...,n.

   *  FIRST_NAME, LAST_NAME, MIDDLE_NAME is fixed and not change value of it.

   */

  /**

   *  Description of the Field

   */

  public final static int FIRST_NAME = 0;

  /**

   *  Description of the Field

   */

  public final static int MIDDLE_NAME = 1;

  /**

   *  Description of the Field

   */

  public final static int LAST_NAME = 2;



  /**

   *  Description of the Field

   */

  public final static int EMAIL = 3;

  /**

   *  Description of the Field

   */

  public final static int CELL_PHONE = 4;

  /**

   *  Description of the Field

   */

  public final static int TITLE = 5;

  /**

   *  Description of the Field

   */

  public final static int COMPANY = 6;

  /**

   *  Description of the Field

   */

  public final static int DEPARTMENT = 7;

  /**

   *  Description of the Field

   */

  public final static int BUSINESS_PHONE = 8;

  /**

   *  Description of the Field

   */

  public final static int FAX = 9;

  /**

   *  Description of the Field

   */

  public final static int OFFICE_ADDRESS = 10;

  /**

   *  Description of the Field

   */

  public final static int HOME_PHONE = 11;

  /**

   *  Description of the Field

   */

  public final static int HOME_ADDRESS = 12;

  /**

   *  Description of the Field

   */

  public final static int NOTE = 13;



  /**

   *  Export address book to array of bytes.

   *

   *@param  contacts

   *@return           list of bytes.

   */

  public static byte[] exportContactDataByte(ContactData[] contacts) {

    StringBuffer sb = new StringBuffer();



    // Header: field names

    sb.append(HEADER.toCsv(TAB));

    sb.append(LINEFEED);

    sb.append(NEWLINE);



    // Data

    if (contacts != null)

      for (int i = 0; i < contacts.length; i++) {

        TDLine ct = export(contacts[i]);

        if( ct != null) {

          sb.append(ct.toCsv(TAB));

          sb.append(LINEFEED);

          sb.append(NEWLINE);

        }

      }



    return sb.toString().getBytes();

  }



  /**

   * extract full name to first, middle and last names

   * @param name

   * @return: 0:first, 1: middle and 2 : last name

   */

  static String[] extractNames( String name) {

    // First name

    int i = name.indexOf(" ");

    if( i == -1)

      return new String[] {name,null,null};

    String first = name.substring(0,i);

    name = name.substring(i+1);



    // Middle name

    i = name.indexOf(" ");

    if( i == -1)

      return new String[] {first,null,name};

    String middle = name.substring(0,i);

    String last = name.substring(i+1);

    return new String[] {first,middle,last};

  }

  static String esc(String s) {
    return s==null?"":s;
  }


//*** JK - fix for TT03728 
  static String escape(String s1) {

    String s = new String(esc(s1));

    // put quotes around strings containing embedded newlines
    if (s.indexOf("\r\n") >= 0)
    {
      s = "\"" + s + "\"";
    }
    return s;
  }
// *** end new code

  static TDLine export(ContactData contact) {

    TDLine tdl = new TDLine();

    // First, middle and last name

    String[] names = extractNames(contact.getName());

    tdl.addElement(esc(names[0]));

    tdl.addElement(esc(names[1]));

    tdl.addElement(esc(names[2]));



    // email

    tdl.addElement(esc(contact.getEmail()));



    // Mobile phone

    tdl.addElement(esc(contact.getCellPhone()));



    // Job title

    tdl.addElement(esc(contact.getTitle()));



    //"Company"

    tdl.addElement(esc(contact.getCompany()));



    //"Department"

    tdl.addElement(esc(contact.getDepartment()));



    //"Business Phone"

    tdl.addElement(esc(contact.getBusinessPhone()));



    //"Business Fax",

    tdl.addElement(esc(contact.getFax()));



    //"Business Street"


//*** JK fix for TT03728 - call new method "escape" 
//    tdl.addElement(esc(contact.getOfficeAddress()));
    String s = contact.getOfficeAddress();
    s = escape(s);
    tdl.addElement(s);
// *** JK end new code

    //"Home Phone"

    tdl.addElement(esc(contact.getHomePhone()));



    //"Home Street"

//*** JK fix for TT03728 - call new method "escape" 
//    tdl.addElement(esc(contact.getHomeAddress()));
    s = contact.getHomeAddress();
    s = escape(s);
    tdl.addElement(s);
// *** JK end new code



    //"Notes"

//*** JK fix for TT03728 - call new method "escape" 
//    tdl.addElement(esc(contact.getNotes()));
    s = contact.getNotes();
    s = escape(s);
    tdl.addElement(s);
// *** JK end new code



    return tdl;

  }



  /**

   *  Export address book to stream.

   *

   *@param  contacts

   *@return                  Export address book to stream InputStream

   *@exception  IOException  Description of the Exception

   */

  public static InputStream exportContactDataStream(ContactData[] contacts) throws IOException {

    return new ByteArrayInputStream(exportContactDataByte(contacts));

  }



  /**

   *  Checking file is valid or non valid.

   *

   *@param  in  contains of file.

   *@return     true is valid. false is not valid.

   */

  private static boolean validateTabDelimited(InputStream in) {

    //Not yet

    return true;

  }



  /**

   *  Import data from list of input and map. Examples : inputs is only one

   *  element. { "First Name" "Phuoc", "Last Name" "Nguyen", "Middle Name"

   *  "Thanh", "Job Title" "Engineer" } map { "0" "First Name", "1" "Last Name",

   *  "2" "Middle Name", "4" "Job Title" } return one contact Name : Phuoc Thanh

   *  Nguyen. Title : Engineer.

   *

   *@param  inputs  list of input.

   *@param  map     map.

   *@return         List of contacts.

   */

  public static ContactData[] importContactData(TDLine[] inputs, String[] map, String logonId) {

    if (inputs == null || inputs.length < 2)

      return null;



    Vector v = new Vector();

    TDLine header = inputs[0];

    for (int i = 1; i < inputs.length; i++) {
        ContactData ct = createContactData( header, inputs[i], map, logonId);
        if( ct != null && ct.getName() != null)

          v.addElement(ct);
    }

    return (ContactData[])v.toArray(new ContactData[0]);

  }


  static String convertEmpty( Vector v, int idx) {
    String s = (idx >= 0 && idx < v.size())? (String)v.elementAt(idx):null;
    return s != null && s.length() > 0? s:null;
  }

  static ContactData createContactData( TDLine header, TDLine data, String[] map, String logonId) {

    int idx;

    ContactData ct = new ContactData();

    ct.putOwnerId(logonId);

    // First name

    String firstName = null;

    if( map[FIRST_NAME] != null && (idx = header.indexOf( map[FIRST_NAME])) != -1)
      firstName = convertEmpty(data,idx);



    // Middle name

    String middleName = null;

    if( map[MIDDLE_NAME] != null && (idx = header.indexOf( map[MIDDLE_NAME])) != -1)

      middleName = convertEmpty(data,idx);



    // Last name

    String lastName = null;

    if( map[LAST_NAME] != null && (idx = header.indexOf( map[LAST_NAME])) != -1)

      lastName = convertEmpty(data,idx);



    // Name of contact

    String ml = (middleName != null)? middleName + (lastName != null? " " + lastName: ""): (lastName != null? lastName: null);

    String name = (firstName != null)? firstName + (ml != null? " " + ml: ""):(ml != null? ml: null);

    if( name != null)

      ct.putName(limit(name,ContactData.MAX_NAME_LENGTH));
    else {
      System.out.println("^^^^^import: name is null");
      return null;
    }



    // email

    if( map[EMAIL] != null && (idx = header.indexOf( map[EMAIL])) != -1) {

      String s = convertEmpty(data,idx);

      if( s != null)

        ct.putEmail(limit(s,ContactData.MAX_EMAIL_LENGTH));

    }



    //public final static int CELL_PHONE = 4;

    if( map[CELL_PHONE] != null && (idx = header.indexOf( map[CELL_PHONE])) != -1) {

      String s = convertEmpty(data,idx);

      if( s != null)

        ct.putCellPhone(limit(s,ContactData.MAX_MOBILE_LENGTH));

    }



    //public final static int TITLE = 5;

    if( map[TITLE] != null && (idx = header.indexOf( map[TITLE])) != -1) {

      String s = convertEmpty(data,idx);

      if( s != null)

        ct.putTitle(s);

    }



    //public final static int COMPANY = 6;

    if( map[COMPANY] != null && (idx = header.indexOf( map[COMPANY])) != -1) {

      String s = convertEmpty(data,idx);

      if( s != null)

        ct.putCompany(limit(s,ContactData.MAX_COMPANY_LENGTH));

    }



    //public final static int DEPARTMENT = 7;

    if( map[DEPARTMENT] != null && (idx = header.indexOf( map[DEPARTMENT])) != -1) {

      String s = convertEmpty(data,idx);

      if( s != null)

        ct.putDepartment(limit(s,ContactData.MAX_DEPARTMENT_LENGTH));

    }



    //public final static int BUSINESS_PHONE = 8;

    if( map[BUSINESS_PHONE] != null && (idx = header.indexOf( map[BUSINESS_PHONE])) != -1) {

      String s = convertEmpty(data,idx);

      if( s != null)

        ct.putBusinessPhone(limit(s,ContactData.MAX_BUSINESS_PHONE_LENGTH));

    }



    //public final static int FAX = 9;

    if( map[FAX] != null && (idx = header.indexOf( map[FAX])) != -1) {

      String s = convertEmpty(data,idx);

      if( s != null)

        ct.putFax(limit(s,ContactData.MAX_FAX_LENGTH));

    }



    //public final static int OFFICE_ADDRESS = 10;

    if( map[OFFICE_ADDRESS] != null && (idx = header.indexOf( map[OFFICE_ADDRESS])) != -1) {

      String s = convertEmpty(data,idx);

      if( s != null)

        ct.putOfficeAddress(limit(s,ContactData.MAX_OFFICE_ADDRESS_LENGTH));

    }



    //public final static int HOME_PHONE = 11;

    if( map[HOME_PHONE] != null && (idx = header.indexOf( map[HOME_PHONE])) != -1) {

      String s = convertEmpty(data,idx);

      if( s != null)

        ct.putHomePhone(limit(s,ContactData.MAX_HOME_PHONE_LENGTH));

    }



    //public final static int HOME_ADDRESS = 12;

    if( map[HOME_ADDRESS] != null && (idx = header.indexOf( map[HOME_ADDRESS])) != -1) {

      String s = convertEmpty(data,idx);

      if( s != null)

        ct.putHomeAddress(limit(s,ContactData.MAX_HOME_ADDRESS_LENGTH));

    }



    //public final static int NOTE = 13;

    if( map[NOTE] != null && (idx = header.indexOf( map[NOTE])) != -1) {

      String s = convertEmpty(data,idx);

      if( s != null)

        ct.putNotes(limit(s,ContactData.MAX_NOTES_LENGTH));

    }



    return ct;

  }



  static String limit( String s, int len) {

    return ( s.length() > len? s.substring(0, len):s);

  }

  //----------------------------------------------//
  static public void main(String args[]) {
    String firstName = "Tien";
    String middleName = null;
    String lastName = "Last";

    // Name of contact
    String ml = (middleName != null)? middleName + (lastName != null? " " + lastName: ""): (lastName != null? lastName: null);
    String name = (firstName != null)? firstName + (ml != null? " " + ml: ""):(ml != null? ml: null);
//    System.out.println("name="+name);
  }

}

