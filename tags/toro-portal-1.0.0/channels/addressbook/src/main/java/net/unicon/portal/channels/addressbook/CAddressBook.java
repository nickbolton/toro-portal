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

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.portal.channels.rad.MimeResponseChannel;
import net.unicon.portal.channels.rad.Screen;
/**
 *  It is channel of addressbook project.
 *
 *@author     nvvu
 *@created    April 10, 2003
 */
public class CAddressBook extends MimeResponseChannel {
  /**
   *  Description of the Field
   */
  public final static String ERROR_EMPTY_FOLDER_NAME = "ERROR_EMPTY_FOLDER_NAME";
  /**
   *  Description of the Field
   */
  public final static String ERROR_CONTACT_NAME_TOO_LONG = "ERROR_CONTACT_NAME_TOO_LONG";
  /**
   *  Description of the Field
   */
  public final static String ERROR_EMPTY_CONTACT_NAME = "ERROR_EMPTY_CONTACT_NAME";
  /**
   *  Description of the Field
   */
  public final static String ERROR_INVALID_EMAIL = "ERROR_INVALID_EMAIL";
  /**
   *  Description of the Field
   */
  public final static String ERROR_DUPLICATE_FOLDER_NAME = "ERROR_DUPLICATE_FOLDER_NAME";
  /**
   *  Description of the Field
   */
  public final static String CONFIRM_DELETE_FOLDER = "CONFIRM_DELETE_FOLDER";
  /**
   *  Description of the Field
   */
  public final static String CONFIRM_DELETE_CONTACT = "CONFIRM_DELETE_CONTACT";
  /**
   *  Description of the Field
   */
  public final static String CONFIRM_IMPORT_CONTACTS = "CONFIRM_IMPORT_CONTACTS";
  /**
   *  Description of the Field
   */
  public final static String ERROR_DELETE_FOLDER = "ERROR_DELETE_FOLDER";
  /**
   *  Description of the Field
   */
  public final static String ERROR_FOLDER_NAME_TOO_LONG = "ERROR_FOLDER_NAME_TOO_LONG";
  /**
   *  Description of the Field
   */
  public final static String ERROR_UPDATE_FOLDER = "ERROR_UPDATE_FOLDER";
  /**
   *  Description of the Field
   */
  public final static String ERROR_CREATE_FOLDER = "ERROR_CREATE_FOLDER";
  /**
   *  Description of the Field
   */
  public final static String ERROR_UPDATE_CONTACT = "ERROR_UPDATE_CONTACT";
  /**
   *  Description of the Field
   */
  public final static String ERROR_CREATE_CONTACT = "ERROR_CREATE_CONTACT";
  /**
   *  Description of the Field
   */
  public final static String INFO_CONTACT_NOT_EXISTED = "INFO_CONTACT_NOT_EXISTED";
  /**
   *  Description of the Field
   */
  public final static String ERROR_OTHER_TOO_LONG = "ERROR_OTHER_TOO_LONG";
  /**
   *  Description of the Field
   */
  public final static String ERROR_INVALID_FILE = "ERROR_INVALID_FILE";
  
  /**
   *  Description of the Field
   */
  public final static String DUPLICATE_CREATE_CONTACT = "DUPLICATE_CREATE_CONTACT";
  
  // version
  final static String VERSION = "1.0.2";
  /**
   *  Description of the Field
   */
  public final static String ALL_FOLDER = "-1";

  /**
   *  Description of the Field
   */
  public final static int ROW_PER_PAGE = 10;

  /**
   *  Override from rad
   *
   *@return    The main value
   *@see       net.unicon.portal.channels.rad.Channel
   */
  public String getMain() {
    // Unicon specific modification because of UI changes they made
  	try {
      if (getScreen("Personal") == null) {
        ;
      }
      Screen personal = makeScreen("Personal");
      personal.init(getParameters());
    } catch (Exception e) {}
    return "net.unicon.portal.channels.addressbook.Peephole";
  }

  /**
   *  To check valid or invalid mail.
   *
   *@param  sEmail  mail.
   *@return         true mail is valid. false mail is invalid.
   */
  public static boolean isValidEmail(String sEmail) {
    return (sEmail == null) || (sEmail != null && sEmail.length() == 0) || (sEmail != null && sEmail.indexOf("@") > 0 && sEmail.indexOf(".") > 0 && sEmail.length() > 5);
  }
  
  public boolean isCacheValid(Object validity) {
  	boolean cacheValid = true;


/*
System.out.println("***** SPILLING CRD: ");
java.util.Enumeration e = m_crd.getParameterNames();
while (e.hasMoreElements()) {
    String s = (String) e.nextElement();
    System.out.println("\tkey=" + s + " , value=" + m_crd.getParameter(s));
}*/
/*
System.out.println("***** SPILLING True CRD: ");
e = m_trueCrd.getParameterNames();
while (e.hasMoreElements()) {
    String s = (String) e.nextElement();
    System.out.println("\tkey=" + s + " , value=" + m_trueCrd.getParameter(s));
}*/
    if(m_crd.getParameters().entrySet().isEmpty())
    	cacheValid = false;

    if (m_crd.isTargeted()) {
        cacheValid = false;
    }

    return cacheValid;

  }

}

