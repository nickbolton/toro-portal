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
package net.unicon.academus.apps.campus;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.jasig.portal.utils.ResourceLoader;

import net.unicon.portal.channels.rad.Channel;
/**
 * Provides LDAP access in a way similar to a relational DBMS.
 */
public class LdapServices {
  static boolean m_ldapServerWorking = true;

  public static final int NUM_ATTRIBUTE = 7;
  private static String sLdapHost = null;
  private static String sLdapPort = null;
  private static String sLdapBaseDN = null;
  private static String sLdapUidAttribute = null;
  private static String sLdapManagerDN = null;
  private static String sLdapManagerPW = null;
  private static String[] sOutputAttrs = null;

  /***************************************************************/
  /* Added by Mike Marquiz 10/07/2003 for Secure LDAP Processing */
  private static String sLdapSecure = null;
  /***************************************************************/

  public static String sId = null;
  public static String sName = null;
  public static String sEmail = null;
  public static String sTitle = null;
  public static String sTelephone = null;
  public static String sDepartment = null;
  public static String sPortalLogon = null;

  static{
    sLdapHost = Channel.getRADProperty("campus.ldap.host");
    sLdapPort = Channel.getRADProperty("campus.ldap.port");
    sLdapBaseDN = Channel.getRADProperty("campus.ldap.baseDN");
    sLdapUidAttribute = Channel.getRADProperty("campus.ldap.uidAttribute");
    sLdapManagerDN = Channel.getRADProperty("campus.ldap.managerDN");
    sLdapManagerPW = Channel.getRADProperty("campus.ldap.managerPW");

    /***************************************************************/
    /* Added by Mike Marquiz 10/07/2003 for Secure LDAP Processing */
    sLdapSecure = Channel.getRADProperty("campus.ldap.secure");
    /***************************************************************/

    Channel.log("campus.ldap.host = " + sLdapHost);
    Channel.log("campus.ldap.port = " + sLdapPort);
    Channel.log("campus.ldap.baseDN = " + sLdapBaseDN);
    Channel.log("campus.ldap.uidAttribute = " + sLdapUidAttribute);
    Channel.log("campus.ldap.managerDN = " + sLdapManagerDN);
    Channel.log("campus.ldap.managerPW = " + sLdapManagerPW);

    /***************************************************************/
    /* Added by Mike Marquiz 10/07/2003 for Secure LDAP Processing */
    Channel.log("campus.ldap.secure = " + sLdapSecure);
    /***************************************************************/

    sId = Channel.getRADProperty("campus.ldap.id");
    sName = Channel.getRADProperty("campus.ldap.name");
    sEmail = Channel.getRADProperty("campus.ldap.email");
    sTelephone = Channel.getRADProperty("campus.ldap.telephone");
    sDepartment = Channel.getRADProperty("campus.ldap.department");
    sTitle = Channel.getRADProperty("campus.ldap.title");
    sPortalLogon = Channel.getRADProperty("campus.ldap.logon");

    //Fix number of attribute
    int numAttrs = NUM_ATTRIBUTE;
    sOutputAttrs = new String[NUM_ATTRIBUTE];
    sOutputAttrs[0] = sId;
    sOutputAttrs[1] = sName;
    sOutputAttrs[2] = sEmail;
    sOutputAttrs[3] = sTelephone;
    sOutputAttrs[4] = sDepartment;
    sOutputAttrs[5] = sTitle;
    sOutputAttrs[6] = sPortalLogon;
  }

  public static String getPortalAttribute() {
    return sPortalLogon;
  }

  /**
   * Gets an LDAP directory context.
   * @return an LDAP directory context object
   */
  public static DirContext getConnection() throws Exception {
    DirContext conn = null;
    if( m_ldapServerWorking == false) {
      Channel.log("----- LDAP:getConnection():m_ldapServerWorking == false");
      return null;
    }

    /******************************************************************/
    /* Changed by Mike Marquiz 10/07/2003 for secure LDAP Processing  */
    /* HashTable initialCapacity changed from 5 to 6 to account for   */
    /*   the Context.SECURITY.PROTOCOL hash key.                      */
    /*                                                                */
    /*  OLD: Hashtable env = new Hashtable(5, 0.75f);                 */
    /*  NEW: Hashtable env = new Hashtable(6, 0.75f);                 */
    /******************************************************************/

    Hashtable env = new Hashtable(6, 0.75f);
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

    /******************************************************************/
    /* Changed by Mike Marquiz 10/08/2003 for secure LDAP Processing  */
    /* Regardless of whether non-secure or secure LDAP is being used, */
    /* the protocol is always ldap://.                                */
    /*                                                                */
    /* OLD: StringBuffer urlBuffer = null;                            */
    /* NEW: StringBuffer urlBuffer = new StringBuffer("ldap://");     */
    /******************************************************************/

    StringBuffer urlBuffer = new StringBuffer("ldap://");

    urlBuffer.append(sLdapHost).append(":").append(sLdapPort);
    env.put(Context.PROVIDER_URL, urlBuffer.toString());
    env.put(Context.SECURITY_AUTHENTICATION, "simple");

    /******************************************************************/
    /* Added by Mike Marquiz 10/07/2003 for secure LDAP Processing    */
    /*                                                                */
    /* if ((sLdapSecure != null) && (sLdapSecure.equals("yes"))) {    */
    /*         env.put(Context.SECURITY_PROTOCOL, "ssl");             */
    /* }                                                              */
    /******************************************************************/

    if ((sLdapSecure != null) && (sLdapSecure.equals("yes"))) {   
             env.put(Context.SECURITY_PROTOCOL, "ssl");             
    }                                                              

    if( sLdapManagerDN != null) {
      env.put(Context.SECURITY_PRINCIPAL,      sLdapManagerDN);
      if( sLdapManagerPW != null)
        env.put(Context.SECURITY_CREDENTIALS,    sLdapManagerPW);
    }

    try {
      conn = new InitialDirContext(env);
    } catch (Exception e) {
      Channel.log("LDAP:getConnection() fails");
      m_ldapServerWorking = false;
    }
    return conn;
  }

  /**
   * Gets the base DN used to search the LDAP directory context.
   * @return a DN to use as reference point or context for queries
   */
  public static String getBaseDN() {
    return sLdapBaseDN;
  }

  /**
   * Gets the uid attribute used to search the LDAP directory context.
   * @return a DN to use as reference point or context for queries
   */
  public static String getUidAttribute() {
    return sLdapUidAttribute;
  }
  /**
   * Gets the output attribute.
   * @return
   */
  public static String[] getOutputAttributes() {
    return sOutputAttrs;
  }

  /**
   * Releases an LDAP directory context.
   * @param an LDAP directory context object
   */
  public static void releaseConnection (DirContext conn) {
    if (conn == null)
      return;
    try {
      conn.close();
    } catch (Exception e) {
      Channel.log("getConnect()LDAP fail: "+e);
    }
  }
}

// eof: LdapServices.java
