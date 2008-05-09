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

import java.security.MessageDigest;

import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.security.provider.*;
import org.jasig.portal.services.LogService;

/**
 * <p>This is an implementation of a SecurityContext that checks a user's
 * credentials against an MD5 hashed password entry.
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $LastChangedRevision$
 */
public class SimpleSecurityContext extends ChainingSecurityContext
    implements ISecurityContext {
  private final int SIMPLESECURITYAUTHTYPE = 0xFF02;


  SimpleSecurityContext () {
    super();
  }

  public int getAuthType () {
    return  this.SIMPLESECURITYAUTHTYPE;
  }

  /**
   * Authenticate user.
   * @exception PortalSecurityException
   */
  public synchronized void authenticate() throws PortalSecurityException {
    this.isauth = false;
    if (this.myPrincipal.getUID() != null && this.myOpaqueCredentials.credentialstring != null) {
      String first_name = null, last_name = null, md5_passwd = null;

      try {
        String acct[] = AccountStoreFactory.getAccountStoreImpl().getUserAccountInformation(this.myPrincipal.getUID());
        if (acct[0] != null) {

          first_name = acct[1];
          last_name = acct[2];
          md5_passwd = acct[0];
          if (!md5_passwd.substring(0, 5).equals("(MD5)")) {
            LogService.log(LogService.ERROR, "Password not an MD5 hash: " + md5_passwd.substring(0, 5));
            return;
          }
          String txthash = md5_passwd.substring(5);
          byte[] whole, salt = new byte[8], compare = new byte[16], dgx;
          whole = decode(txthash);
          if (whole.length != 24) {
            LogService.log(LogService.INFO, "Invalid MD5 hash value");
            return;
          }
          System.arraycopy(whole, 0, salt, 0, 8);
          System.arraycopy(whole, 8, compare, 0, 16);
          MessageDigest md = MessageDigest.getInstance("MD5");
          md.update(salt);
          dgx = md.digest(this.myOpaqueCredentials.credentialstring);
          boolean same = true;
          int i;
          for (i = 0; i < dgx.length; i++)
            if (dgx[i] != compare[i])
              same = false;
          if (same) {
            this.myPrincipal.setFullName(first_name + " " + last_name);
            LogService.log(LogService.INFO, "User " + this.myPrincipal.getUID() + " is authenticated");
            this.isauth = true;
          }
          else
            LogService.log(LogService.INFO, "MD5 Password Invalid");
        }
        else {
            LogService.log(LogService.INFO, "No such user: " + this.myPrincipal.getUID());
        }
      } catch (Exception e) {
        PortalSecurityException ep = new PortalSecurityException("SQL Database Error");
        LogService.log(LogService.ERROR, e);
        throw  (ep);
      }
    }
    // If the principal and/or credential are missing, the context authentication
    // simply fails. It should not be construed that this is an error.
    else {
        LogService.log(LogService.INFO, "Principal or OpaqueCredentials not initialized prior to authenticate");
    }
    // Ok...we are now ready to authenticate all of our subcontexts.
    super.authenticate();
    return;
  }

  //
  // This was originally Jonathan B. Knudsen's Example from his book
  // Java Cryptography published by O'Reilly Associates (1st Edition 1998)
  //
  public static byte[] decode(String base64) {
    int pad = 0;
    for (int i = base64.length() - 1; base64.charAt(i) == '='; i--)
      pad++;
    int length = base64.length()*6/8 - pad;
    byte[] raw = new byte[length];
    int rawIndex = 0;
    for (int i = 0; i < base64.length(); i += 4) {
      int block = (getValue(base64.charAt(i)) << 18) + (getValue(base64.charAt(i + 1)) << 12) + (getValue(base64.charAt(
          i + 2)) << 6) + (getValue(base64.charAt(i + 3)));
      for (int j = 0; j < 3 && rawIndex + j < raw.length; j++)
        raw[rawIndex + j] = (byte)((block >> (8*(2 - j))) & 0xff);
      rawIndex += 3;
    }
    return  raw;
  }


  protected static int getValue(char c) {
    if (c >= 'A' && c <= 'Z')
      return  c - 'A';
    if (c >= 'a' && c <= 'z')
      return  c - 'a' + 26;
    if (c >= '0' && c <= '9')
      return  c - '0' + 52;
    if (c == '+')
      return  62;
    if (c == '/')
      return  63;
    if (c == '=')
      return  0;
    return  -1;
  }
}
