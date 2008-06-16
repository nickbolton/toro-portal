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
package net.unicon.sdk.tpm;

import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.properties.CommonPropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;

/**
 * TransactionLogFactory is responsible for creating the appropriate
 * implementation for ITransactionLog.
 */
public class TransactionLogFactory {
    private static ITransactionLog transLog = null;

    /**
     * Gets the ITransactionLog implementation
     * 
     * the implementation is specified by the 
     * net.unicon.sdk.tpm.ITransactionLog.implementation property
     *
     * @return <code>ITransactionLog</code> implementation
     * @see <{ITransactionLog}>
     */
    public static ITransactionLog getService() 
    throws FactoryCreateException {
        if (transLog == null) {
            initialize();
        }
        return transLog;
    } // end getService

    /* Loads the appropiate transLog Implementation. */
    private static void initialize() throws FactoryCreateException {
        String className = UniconPropertiesFactory.getManager(
            CommonPropertiesType.SERVICE).getProperty(
                "net.unicon.sdk.tpm.ITransactionLog.implementation");
        if (className != null) {
            try {
                transLog = 
                    (ITransactionLog) Class.forName(className).
                        newInstance();
            } catch (Exception e) {
                throw new FactoryCreateException(
                    "TransactionLogFactory: Could not instantiate " + 
                        className, e);
            }
        } else {
            throw new FactoryCreateException("TransactionLogFactory: Could not find value for 'net.unicon.sdk.tpm.ITransactionLog.implementation' in unicon-service.properties");
        }
    } // end initialize

} // end TransactionLogFactory
