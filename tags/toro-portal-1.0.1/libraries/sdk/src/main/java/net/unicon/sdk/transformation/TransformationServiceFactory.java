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
package net.unicon.sdk.transformation;

import java.lang.reflect.Constructor;

import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.properties.CommonPropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;

/**
 * TransformationServiceFactory is responsible for creating the appropriate
 * implementation for ITransformationService.
 */
public class TransformationServiceFactory {
    private static ITransformationService transformService = null;

    /**
     * Gets the ITransformationService implementation passing a valid
     * TransformMapping.xml file path.
     *
     * The implementation is specified by the 
     * net.unicon.sdk.transformation.ITransformationService.implementation property
     *
     * @param transformFilePath is a path to a valid TransformMapping.xml file.
     * @return <code>ITransformationService</code> implementation
     * @throws <code>net.unicon.sdk.FactoryCreateException</code>
     * @see <{ITransformationService}>
     */
    public static ITransformationService getService(String transformFilePath) 
    throws FactoryCreateException {

        if (transformService == null) {
            /* ASSERTIONS */
            // check transformFilePath 
            if (!(transformFilePath != null 
                    && transformFilePath.trim().length() > 0)) {
                throw new FactoryCreateException(
                    "TransformationServiceFactory: Configuration file path must be provided");
            } // end if

            initialize(transformFilePath);
        }

        return transformService;
    } // end getService(transformFilePath)

    /* Loads the appropiate transformationService Implementation. */
    private static void initialize(String transformFilePath) 
    throws FactoryCreateException {

        String className = UniconPropertiesFactory.getManager(
            CommonPropertiesType.SERVICE).getProperty(
                "net.unicon.sdk.transformation.ITransformationService.implementation");
        if (className != null) {
            try {
                Class impl = Class.forName(className);
                Class[] args = new Class[] { java.lang.String.class };
                Constructor c = impl.getConstructor(args);

                transformService = 
                    (ITransformationService) c.newInstance(
                        new Object[] { transformFilePath });

            } catch (Exception e) {
                throw new FactoryCreateException(
                    "TransformationServiceFactory: Could not instantiate " + 
                        className, e);
            }
        } else {
            throw new FactoryCreateException("TransformationServiceFactory: Could not find value for 'net.unicon.sdk.transformation.ITransformationService.implementation' in unicon-service.properties");
        }

    } // end initialize(transformFilePath)

} // end TransformationServiceFactory
