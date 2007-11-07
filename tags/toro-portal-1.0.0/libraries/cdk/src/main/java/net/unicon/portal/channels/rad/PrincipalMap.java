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


package net.unicon.portal.channels.rad;

import java.util.*;
import java.sql.*;

import org.jasig.portal.RDBMServices;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.provider.NotSoOpaqueCredentials;

/**
 * Help to implement the "SINGLE SIGN ON". Each application that requires the authentication will
 * get the user/password from this map ( mapping from uPortal user to application's principal).
 */
public class PrincipalMap {
  public String logon = null;
  public String password = null;

  /**
   * Authentication type: none ( do not require)
   */
  public static final String OPTION_NONE = "none";

  /**
   * Authentication type: portal
   */
  public static final String OPTION_PORTAL = "portal";

  /**
   * Authentication type: table ( the map is stored in the database table)
   */
  public static final String OPTION_TABLE = "table";

  /**
   * Authentication type: wallet ( use wallet servant)
   */
  public static final String OPTION_WALLET = "wallet";

  /**
   * Authentication type: Illinois university
   */
  public static final String OPTION_ILSTU = "ilstu";

  //
  // for the logged on user...
  //
  /**
   * Get the user/password as the data members of PrincipalMap for given application and
   * given authentication type.
   * @param person logon user as IPerson
   * @param application The application name
   * @option The authentication type.
   * @return The PrincipalMap contains user/password, used to log to given application.
   */
  public static PrincipalMap map(IPerson person, String application, String option) {
    if (option.equals(OPTION_NONE))
      return none(person);
    if (option.equals(OPTION_PORTAL))
      return portal(person);
    if (option.equals(OPTION_TABLE))
      return table(person, application);
    if (option.equals(OPTION_WALLET))
      return wallet(person, application);
    if (option.equals(OPTION_ILSTU))
      return ilstu(person, application);
    return null;
  }

  static PrincipalMap none(IPerson person) {
    //
    // application "logon" is the same as portal "username"
    // application "password" is "null"
    // used when application does not require additional authentication (e.g. DPCS)
    //
    PrincipalMap map = new PrincipalMap();
    map.logon = (String)person.getAttribute("username");
    return map;
  }

  static PrincipalMap portal(IPerson person) {
    //
    // application "logon" is the same as portal "username"
    // application "password" is the same as portal "password"
    //
    String password = null;
    ISecurityContext sc = person.getSecurityContext();
    IOpaqueCredentials oc = sc.getOpaqueCredentials();
    if (oc instanceof NotSoOpaqueCredentials)
      password = ((NotSoOpaqueCredentials)oc).getCredentials();
    else
      for (Enumeration e = sc.getSubContexts(); e.hasMoreElements();) {
        IOpaqueCredentials soc = ((ISecurityContext)e.nextElement()).getOpaqueCredentials();
        if (soc instanceof NotSoOpaqueCredentials) {
          password = ((NotSoOpaqueCredentials)soc).getCredentials();
          break;
        }
      }
    PrincipalMap map = new PrincipalMap();
    map.logon = (String)person.getAttribute("username");
    map.password = password;
    return map;
  }

  public static PrincipalMap table(IPerson person, String application) {
    //
    // application "logon" is specific to application
    // application "password" is specific to application
    // used with PORTAL_PRINC_MAP implementation
    //
    PrincipalMap map = null;
    RDBMServices rdbm = new RDBMServices();
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;

    try {
      String user = (String)person.getAttribute("username");
      String query = "SELECT LOGON, PASSWORD FROM PORTAL_PRINC_MAP WHERE USER_NAME='"+user+"' AND APPLICATION='"+application+"'";
      conn = rdbm.getConnection();
      stmt = conn.createStatement();
      stmt.executeQuery(query);
      rs = stmt.getResultSet();
      if (rs.next()) {
        map = new PrincipalMap();
        map.logon = rs.getString("LOGON");
        map.password = rs.getString("PASSWORD");
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        if (stmt != null) stmt.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      rdbm.releaseConnection(conn);
    }
    return map;
  }

  public static PrincipalMap wallet(IPerson person, String application) {
    //
    // application "logon" is specific to application
    // application "password" is specific to application
    // used with "security wallet" implementation
    //
    PrincipalMap map = new PrincipalMap();
    map.logon = (String)person.getAttribute("logon");
    map.password = (String)person.getAttribute("password");
    return map;
  }

  public static PrincipalMap ilstu(IPerson person, String application) {
    //
    // application "logon" is the same as portal "username"
    // application "password" is the same as portal "username"
    // customization for ILSTU
    //
    PrincipalMap map = new PrincipalMap();
    map.logon = map.password = (String)person.getAttribute("username");
    return map;
  }

  //
  // for a list of users...
  //

  public static String[] map(String[] usernames, String application, String option) {
    if (option.equals(OPTION_NONE))
      return usernames;
    if (option.equals(OPTION_PORTAL))
      return usernames;
    if (option.equals(OPTION_TABLE))
      return table(usernames, application);
    if (option.equals(OPTION_WALLET))
      return usernames;
    if (option.equals(OPTION_ILSTU))
      return usernames;
    return null;
  }

  public static String[] table(String[] usernames, String application) {
    RDBMServices rdbm = new RDBMServices();
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    String[] logons = new String[usernames.length];
    if (logons != null && logons.length > 0)
      for (int i = 0; i < usernames.length; i++) {
        try {
          String sql = "SELECT LOGON FROM PORTAL_PRINC_MAP WHERE USER_NAME = ? AND APPLICATION='" + application + "'";
          conn = rdbm.getConnection();
          ps = conn.prepareStatement(sql);
          ps.setString(1, usernames[i]);
          rs = ps.executeQuery();
          while (rs.next()) {
            logons[i] = rs.getString("LOGON");
          }
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          try {
            if (rs != null) rs.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
          try {
            if (ps != null) ps.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
          rdbm.releaseConnection(conn);
        }
      }
    return logons;
  }
}
