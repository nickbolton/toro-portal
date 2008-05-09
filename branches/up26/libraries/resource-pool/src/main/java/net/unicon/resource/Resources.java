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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Resources {
    protected static Map<String, ResourcePool> connectionPools = new HashMap<String, ResourcePool>();
    private static Log log = LogFactory.getLog(Resources.class);

    public static Object dangerousGetUnderlyingResource(Object resource) {
        return ((Resource) resource).getRawResource();
    }

    public static Connection getConnection(String name, String requester) {
        Connection rslt = (Connection) getConnectionPool(name).allocate(requester);
        try {
            rslt.setAutoCommit(true);   // important to guarantee consistancy...
        } catch (Throwable t) {
            String msg = "SDK Resources was unable to set the autoCommit property of a connection.";
            throw new RuntimeException(msg, t);
        }
        return rslt;
    }

    public static void releaseConnection(String name, Connection connection) {
        if (connection == null) {
            return;
        }

        getConnectionPool(name).release((ConnectionResource) connection);
    }

    public static ResourcePool getConnectionPool(String name) {
        return connectionPools.get(name);
    }

    public static Map<String, List> getAllocationInfo() {
        Map<String, List> answer = new HashMap<String, List>();
        Iterator itr = connectionPools.values().iterator();
        while (itr.hasNext()) {
            ResourcePool pool = (ResourcePool) itr.next();
            answer.put(pool.getName(), pool.getAllocationInfo());
        }
        return answer;
    }

    protected static ResourcePool addConnectionPool(ResourcePool pool) {
        ResourcePoolConfiguration resourcePoolConfiguration = pool.getConfig();

        if (! (resourcePoolConfiguration instanceof ConnectionPoolConfiguration)) {
            throw new IllegalArgumentException("addConnectionPool only supports adding ResourcePools that are in fact configured as ConnectionPools.  " + resourcePoolConfiguration + " was not an instance of ConnectionPoolConfiguration but it must be for this method to work.");
        }

        ConnectionPoolConfiguration config = (ConnectionPoolConfiguration) resourcePoolConfiguration;

        try {
            Class.forName(config.getDriver());
        } catch (Exception e) {
            log.error("Unable to load JDBC driver: " + config.getDriver());
            throw new Error("ConnectionPool driver invalid for " + config.getName());
        }

        if (log.isInfoEnabled()) {
            log.info("Adding ResourcePool: " + pool);
        }

        connectionPools.put(pool.getName(), pool);

        return pool;
    }

    protected static ResourcePool findPoolByName(List pools, String name) {
        for (int i = 0; i < pools.size(); i++) {
            ResourcePool pool = (ResourcePool) pools.get(i);
            if (pool.getName().equals(name)) {
                return pool;
            }
        }

        return null;
    }

    protected static ResourcePool findPoolByDescriptor(List pools, String descriptor) {
        for (int i = 0; i < pools.size(); i++) {
            ResourcePool pool = (ResourcePool) pools.get(i);
            if (pool.getDescriptor().equals(descriptor)) {
                return pool;
            }
        }

        return null;
    }
}
