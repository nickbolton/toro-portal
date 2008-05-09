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
package net.unicon.sdk.log;

import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.properties.CommonPropertiesType;
import net.unicon.sdk.FactoryCreateRuntimeException;

/**
 * LogServiceFactory is responsible for creating the appropriate
 * implementation for ILogService.
 */
public class LogServiceFactory {
    private static ILogService logServiceImpl = null;

    /**
     * Gets the ILogService implementation
     *
     * the implementation is specified by the 
     * net.unicon.sdk.log.ILogService.implementation property
     *
     * @return <code>ILogService</code> implementation
     * @see <{GuidService}>
     */
    public static ILogService instance() throws FactoryCreateRuntimeException {
        if (logServiceImpl == null) {
            initialize();
        }
        return logServiceImpl;
    } // end instance 

    /* Loads the appropiate logService Implementation. */
    private static void initialize() throws FactoryCreateRuntimeException {
        String className = UniconPropertiesFactory.getManager(
            CommonPropertiesType.SERVICE).getProperty(
                "net.unicon.sdk.log.ILogService.implementation");
        if (className != null) {
            try {
                logServiceImpl = 
                    (ILogService) Class.forName(className).newInstance();
            } catch (Exception e) {
                throw new FactoryCreateRuntimeException(
                    "LogServiceFactory: Could not instantiate " + className, e);
            }
        } else {
            throw new FactoryCreateRuntimeException("LogServiceFactory: Could not find value for 'net.unicon.sdk.log.ILogService.implementation' in unicon-service.properties");
        }
    } // end initialize

} // end LogServiceFactory
