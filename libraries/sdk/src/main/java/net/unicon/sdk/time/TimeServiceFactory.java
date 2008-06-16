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
package net.unicon.sdk.time;

import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.properties.CommonPropertiesType;

public class TimeServiceFactory {
    private static TimeService timeServiceImpl = null;
    /**
     * Gets the TimeService implementation specified by the Factory properties.
     * @return <code>TimeService</code> implementation
     * @see <{TimeService}>
     */
    public static TimeService getService() throws FactoryCreateException {
        if (timeServiceImpl == null) {
            initialize();
        }
        return timeServiceImpl;
    }
    /** Loads the appropiate timeService Implementation. */
    private static void initialize() throws FactoryCreateException {
        // Get class name from FactoryImplPropertiesManager
        String className = UniconPropertiesFactory.getManager(CommonPropertiesType.FACTORY).getProperty("net.unicon.sdk.time.implementation");
        if (className != null) {
            try {
                timeServiceImpl = (TimeService) Class.forName(className).newInstance();
            } catch (Exception e) {
                throw new FactoryCreateException(
                "TimeServiceFactory: Could not instantiate " + className, e);
            }
        } else {
            throw new FactoryCreateException("TimeServiceFactory: Could not find value for 'net.unicon.portal.channels.service.TimeService.implementation' in factoryImpl.properties");
        }
    }
}
