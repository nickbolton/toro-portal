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

package com.interactivebusiness.portal.channel.utils;

import java.util.Map;

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.IServant;
import org.jasig.portal.security.IPerson;

/**
 * <p>This is the interface for the Webmail Authentication.
 * Allows for one to supply their own authentication implementation. </p>
 *
 * @author Freddy Lopez, flopez@interactivebusiness.com
 * @version $LastChangedRevision$
 */

public interface IAuthentication extends IChannel, IServant {


  /**
   * Returns a Map object containing the credentials for the specified application.
   * @return A Map object containing the credentials for the specified application.
   */
  public Map getCredentials (IPerson p);
  public Map getCredentials (String sApplicationName);

  /**
   * Returns a boolean value after storing credentials.
   * @return A boolean value of 'true' when stored successfully or 'false' when storing was not successful.
   */
  public boolean storeCredentials (String sApplicationName, Map mCredentials);

  /**
   * Returns no value.  This method will remove old credentials from storage location.
   * Best used when a password has changed, simply remove old credentials and reprompt user for new credentials.
   */
  public void removeCredentials (String sApplication);

  /**
   *  This method has been added so that Central Authentication Service (CAS) can be supported
   */
  public void setChannelStaticData(ChannelStaticData csd);
}
