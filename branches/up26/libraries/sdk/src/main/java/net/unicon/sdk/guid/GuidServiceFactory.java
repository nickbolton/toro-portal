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
package net.unicon.sdk.guid;

import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.properties.CommonPropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;

/**
 * GuidServiceFactory is responsible for creating the appropriate
 * implementation for IGuidService.
 */
public class GuidServiceFactory {
    private static IGuidService guidServiceImpl = null;

    /**
     * Gets the IGuidService implementation
     *
     * the implementation is specified by the 
     * net.unicon.sdk.guid.IGuidService.implementation property
     *
     * @return <code>IGuidService</code> implementation
     * @see <{GuidService}>
     */
    public static IGuidService getService() throws FactoryCreateException {
        if (guidServiceImpl == null) {
            initialize();
        }
        return guidServiceImpl;
    } // end getService

    /* Loads the appropiate guidService Implementation. */
    private static void initialize() throws FactoryCreateException {
        String className = UniconPropertiesFactory.getManager(
            CommonPropertiesType.SERVICE).getProperty(
                "net.unicon.sdk.guid.IGuidService.implementation");
        if (className != null) {
            try {
                guidServiceImpl = 
                    (IGuidService) Class.forName(className).newInstance();
            } catch (Exception e) {
                throw new FactoryCreateException(
                "GuidServiceFactory: Could not instantiate " + className, e);
            }
        } else {
            throw new FactoryCreateException("GuidServiceFactory: Could not find value for 'net.unicon.sdk.guid.IGuidService.implementation' in unicon-service.properties");
        }
    } // end initialize

} // end GuidServiceFactory
