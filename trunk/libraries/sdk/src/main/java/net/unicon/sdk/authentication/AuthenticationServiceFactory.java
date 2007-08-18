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
package net.unicon.sdk.authentication;

import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.properties.CommonPropertiesType;
import net.unicon.sdk.FactoryCreateException;

public class AuthenticationServiceFactory {
    private static AuthenticationService authenticationServiceImpl = null;
    /**
     * Gets the AuthenticationService implementation specified by the Factory properties.
     * @return <code>AuthenticationService</code> implementation
     * @see <{AuthenticationService}>
     */
    public static AuthenticationService getService() throws FactoryCreateException {
        if (authenticationServiceImpl == null) {
            initialize();
        }
        return authenticationServiceImpl;
    }
    /** Loads the appropiate AuthenticationService Implementation. */
    private static void initialize() throws FactoryCreateException {
        // Get class name from FactoryImplPropertiesManager
        String className = UniconPropertiesFactory.getManager(CommonPropertiesType.FACTORY).getProperty("net.unicon.sdk.authentication.AuthenticationService.implementation");
        if (className != null) {
            try {
                authenticationServiceImpl = (AuthenticationService) Class.forName(className).newInstance();
            } catch (Exception e) {
                throw new FactoryCreateException(
                "AuthenticationServiceFactory: Could not instantiate " + className, e);
            }
        } else {
            throw new FactoryCreateException("AuthenticationServiceFactory: Could not find value for 'net.unicon.sdk.authentication.AuthenticationService.implementation' in common-factory.properties");
        }
    }
}
