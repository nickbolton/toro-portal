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

package net.unicon.portal.security;

import org.jasig.portal.security.*;
import org.jasig.portal.security.provider.*;
import org.jasig.portal.services.LogService;
import org.jasig.portal.RDBMServices;
import java.util.*;
import java.sql.*;

import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import org.jasig.portal.security.provider.JAASInlineCallbackHandler;

/**
 * <p>This is an implementation of a SecurityContext that checks a user's
 * credentials using JAAS.
 *
 * @author Nathan Jacobs
 * @version $LastChangedRevision$
 *
 */

class JAASSecurityContext extends ChainingSecurityContext implements ISecurityContext {

  private final int JAASSECURITYAUTHTYPE = 0xFF05;

  JAASSecurityContext() {
    super();
  }

  public int getAuthType() {
    return this.JAASSECURITYAUTHTYPE;
  }

  public synchronized void authenticate() throws PortalSecurityException {
    this.isauth = false;
    RDBMServices rdbmservices = new RDBMServices();

    if (this.myPrincipal.getUID() != null && this.myOpaqueCredentials.credentialstring != null) {

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rset = null;
      String first_name = null, last_name = null;
//      int globalUID;

      try {
        // JAAS Stuff

        LoginContext lc = null;

        // Establish a LoginContext for the JAAS Authentication process.
        // Note that the user's specified username and password are passed 
        // to the constructor of the JAASInlineCallbackHandler.

        lc = new LoginContext("uPortal",
                 new JAASInlineCallbackHandler(
                             this.myPrincipal.getUID(),
                             (new String(this.myOpaqueCredentials.credentialstring)).toCharArray())); // could not come up w/ a better way to do this

        // Try to authenticate the user via the LoginModule(s) and associated 
        // options for the "uPortal" application (defined within the 
        // "uPortal-jaas.conf" JAAS configuration file).  If authentication
        // is unsuccessful an exception will be thrown.
 
        lc.login();

        this.isauth = true;

        String query = "SELECT FIRST_NAME, LAST_NAME FROM up_person_dir " +
                       "WHERE USER_NAME = ?";

        conn = rdbmservices.getConnection();
        stmt = conn.prepareStatement(query);
        stmt.setString(1, this.myPrincipal.getUID());
        rset = stmt.executeQuery();

        if (rset.next()) {

//          globalUID  = rset.getInt("ID");
          first_name = rset.getString("FIRST_NAME");
          last_name  = rset.getString("LAST_NAME");

          boolean isAuthenticated = false;

//          this.myPrincipal.globalUID = globalUID;
          this.myPrincipal.setFullName(first_name + " " + last_name);
          LogService.log(LogService.INFO, "User " + this.myPrincipal.getUID() + 
                         " is authenticated");

        } else {
          LogService.log(LogService.INFO, "No such user: " + 
                         this.myPrincipal.getUID());
        }
      } catch (SQLException e) {
        LogService.log(LogService.ERROR, new PortalSecurityException ("error"));
      } catch (LoginException e) {
        LogService.log(LogService.INFO, "User " + this.myPrincipal.getUID() + 
                       ": invalid password");
        LogService.log(LogService.DEBUG,"LoginException: " + e.getMessage());
      } finally {
        try { rset.close(); } catch (Exception e) { }
        try { stmt.close(); } catch (Exception e) { }
        rdbmservices.releaseConnection(conn);
      }
    } else {
      LogService.log (LogService.ERROR, 
       "Principal or OpaqueCredentials not initialized prior to authenticate");
    }

    // authenticate all subcontexts.
    super.authenticate();

    return;
  }
  // static initialization block to obtain - and then set - the various
  // JAAS properties for Kerberos5 authentication to an Active Directory
  // server.
  static
  {
    try {
      java.io.InputStream inStream = 
            JAASSecurityContext.class.getResourceAsStream(
                                         "/properties/security.properties");
      java.util.Properties jaasProps = new Properties();
      jaasProps.load(inStream);
      String sProp = jaasProps.getProperty("jaasKrb5ServerAddress");
      System.setProperty ("java.security.krb5.kdc", sProp);
      sProp = jaasProps.getProperty("jaasKrb5Realm");
      System.setProperty ("java.security.krb5.realm", sProp);
      sProp = jaasProps.getProperty("jaasConfigFile");
      System.setProperty ("java.security.auth.login.config", sProp);
      inStream.close ();
    } catch (java.io.IOException ex) {}
  }
}
