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

package net.unicon.academus.common;

import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.sdk.FactoryCreateException;

public class PersonDirectoryServiceFactory {

    private static PersonDirectoryService serviceImpl = null;
    private static String propName =
    "net.unicon.academus.common.PersonDirectoryService.implementation";

    /**
     * Gets the PersonDirectoryService implementation specified by the
     * Factory properties.
     * @return <code>PersonDirectoryService</code> implementation
     * @see <{PersonDirectoryService}>
     */
    public static PersonDirectoryService getService()
    throws FactoryCreateException {
        if (serviceImpl == null) {
            initialize();
        }
        return serviceImpl;
    }

    /** Loads the appropiate PersonDirectoryService Implementation. */

    private static void initialize() throws FactoryCreateException {

        // Get class name from FactoryImplPropertiesManager
        String className = UniconPropertiesFactory.getManager(PortalPropertiesType.FACTORY).getProperty(propName);
        System.out.println("PersonDirectoryService implementation is: " + className);
        if (className != null) {
            try {
                serviceImpl = (PersonDirectoryService)Class.forName(className).newInstance();
            } catch (Exception e) {
                throw new FactoryCreateException(
                    "PersonDirectoryServiceFactory: Could not instantiate " +
                    className, e);
            }
        } else {
            throw new FactoryCreateException("PersonDirectoryServiceFactory: Could not find value for '" + propName + "' in " + PortalPropertiesType.FACTORY.getFileName());
        }
    }
}

