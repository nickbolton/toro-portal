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
package net.unicon.portal.groups;

import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.properties.*;

public class UniconGroupServiceFactory {
    private static UniconGroupService groupServiceImpl = null;
    private static final String propName =
        "net.unicon.portal.groups.UniconGroupService.implementation";

    /**
     * Retrieves a <code>UniconGroupService</code> object
     * with an implementation that is specified by the
     * <code>CommonPropertiesType</code>.FACTORY property file.
     * @return The desired <code>UniconGroupService</code> object.
     * @throws FactoryCreateException if the group object cannot be
     * instantiated.
     */
    public static UniconGroupService getService()
    throws FactoryCreateException {
        if (groupServiceImpl == null) {
            initialize();
        }
        return groupServiceImpl;
    }

    /** Loads the appropiate UniconGroupService Implementation. */
    private static void initialize()
    throws FactoryCreateException {
        // Get class name from FactoryImplPropertiesManager
        String className = UniconPropertiesFactory.getManager(
            CommonPropertiesType.FACTORY).getProperty(propName);
        if (className != null) {
            try {
                groupServiceImpl =
                    (UniconGroupService) Class.forName(className).newInstance();
            } catch (Exception e) {
                StringBuffer sb = new StringBuffer();
                sb.append("UniconGroupServiceFactory: Could not instantiate ");
                sb.append(className);
                throw new FactoryCreateException(sb.toString(), e);
            }
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("UniconGroupServiceFactory: Could not find value for ");
            sb.append("'").append(propName).append("' ");
            sb.append("in ").append(CommonPropertiesType.FACTORY.getFileName());
            throw new FactoryCreateException(sb.toString());
        }
    }
}
