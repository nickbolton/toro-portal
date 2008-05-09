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

package  net.unicon.portal.security;

import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.security.provider.ChainingSecurityContext;
import org.jasig.portal.security.provider.NotSoOpaqueCredentials;


/**
 * <p>This is an implementation of a SecurityContext that authenticates a 
 * user's credentials against the database, and if the user is valid,
 * caches the credentials for use by channels (i.e. for single-sign-on
 * portlets/channels).</p>
 *
 * @author Mike Marquiz (UNICON, Inc.)
 * @version $LastChangedRevision$
 */
public class CacheSimpleSecurityContext extends SimpleSecurityContext {
  private final int CACHEDSIMPLESECURITYAUTHTYPE = 0xFF02;
  private byte[] cachedCredentials;


  /**
    * Class constructor, merely invokes the constructor of the parent class.
   */

  CacheSimpleSecurityContext () {
    super();
  }

  /**
   * Returns the type of authentication provided by this class.
   * @return authorization type
   */
  public int getAuthType () {
    return  this.CACHEDSIMPLESECURITYAUTHTYPE;
  }

  /**
   * Authenticates the user.
   */
  public synchronized void authenticate () throws PortalSecurityException {

      // Save the credentials before they are blown away by the parent's
      // authenticate() method.

         this.cachedCredentials = 
                new byte[this.myOpaqueCredentials.credentialstring.length];
         System.arraycopy(this.myOpaqueCredentials.credentialstring, 0, 
                          this.cachedCredentials, 0, 
                          this.myOpaqueCredentials.credentialstring.length);
    super.authenticate();

    // If the user has not been successfully authenticated, then we
    // erase any reference to the user's cached credentials.

    if(!this.isAuthenticated()) {
        this.cachedCredentials = null;
    }

  }

  /**
   * We need to override this method in order to return a class that implements
   * the NotSoOpaqueCredentals interface.
   */
  public IOpaqueCredentials getOpaqueCredentials () {
    if (this.isauth) {
      NotSoOpaqueCredentials oc = new CacheOpaqueCredentials();
      oc.setCredentials(this.cachedCredentials);
      return  oc;
    }
    else
      return  null;
  }

  /**
   * This is a new implementation of an OpaqueCredentials class that
   * implements the less-opaque NotSoOpaqueCredentials.
   */
  private class CacheOpaqueCredentials 
          extends ChainingSecurityContext.ChainingOpaqueCredentials
          implements NotSoOpaqueCredentials {

    /**
     * Gets the credentials
     * @return the credentials
     */
    public String getCredentials () {
      if (this.credentialstring != null)
        return  new String(this.credentialstring);
      else
        return  null;
    }
  }
}



