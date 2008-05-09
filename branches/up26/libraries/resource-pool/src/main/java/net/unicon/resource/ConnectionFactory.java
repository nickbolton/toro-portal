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
package net.unicon.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;

class ConnectionFactory implements ResourceFactory {
    protected String driverClass;
    protected String url;
    protected String user;
    protected String password;
    protected String simpleSQL;
    protected Log log = LogFactory.getLog(ConnectionFactory.class);

    ConnectionFactory(ConnectionPoolConfiguration config) {
        driverClass = config.getDriver();
        url = config.getURL();
        user = config.getUser();
        password = config.getPassword();
        simpleSQL = config.getSimpleSQL();
    }

    public Resource createEmptyResource(ResourcePool pool, String requester) {
        return new ConnectionResource(pool, requester, null, null, simpleSQL);
    }

    public Resource createResource(ResourcePool pool, String requester) {
        Object rawResource = createRawResource();
        long number = -1;

        if (rawResource != null) {
            number = ConnectionFactory.nextNumber();
        }

        return new ConnectionResource(pool, requester, new Long(number), rawResource, simpleSQL);
    }

    public Resource createResource(ResourcePool pool, String requester, Resource previousResource) {
        return new ConnectionResource(pool, requester, previousResource.getRawResourceID(), previousResource.getRawResource(), simpleSQL);
    }

    public Resource recreateResource(ResourcePool pool, Resource previousResource) {
        Resource newResource = createResource(pool, previousResource.getRequester(), previousResource);
        newResource.copyFrom(previousResource);
        return newResource;
    }

    Object createRawResource() {
        try {
            final String userName = "user";
            final String passwordName = "password";

            if (log.isDebugEnabled()) {
                log.debug("About to attempt to get a Connection\n\tdriver = " + driverClass + "\n\turl = " + url + "\n\tuser = " + user + "\n\tpassword = " + password);
            }
            Driver driver = (Driver)Class.forName(driverClass).newInstance();
            Properties props = new Properties();
            props.put(userName, user);
            props.put(passwordName, password);
            Connection connection = driver.connect(url, props);
            connection.setAutoCommit(false);

            return connection;
        } catch (Exception e) {
            // ERROR
            log.error("Received error = " + e.getMessage(), e);
            return null;
        }
    }

    protected static long connectionCount = 1;

    public static long getConnectionCount() {
        return connectionCount - 1;
    }

    protected synchronized static long nextNumber() {
        return connectionCount++;
    }
}
