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

import net.unicon.resource.util.ErrorUtils;

import java.sql.*;
import java.util.Properties;

public class BobsConnectionPoolDriver implements Driver {
    // while this singleton is never referenced once instantiated,
    // the call to initializeDriver() has important side effects
    private static final BobsConnectionPoolDriver singleton = initializeDriver();
    private static final String URL_PREFIX = "jdbc:bcp:";

    private BobsConnectionPoolDriver() {
    }

    @Override
    public String toString() {
        return "BobsConnectionPoolDriver (singleton)";
    }

    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }


        String requestInfo = url.substring(URL_PREFIX.length());
        String[] requestTokens = requestInfo.split(":");
        String poolName = (requestTokens.length >= 1) ? requestTokens[0] : null;
        String requester = (requestTokens.length >= 2) ? (String) requestTokens[1] : "anonymous";
        if (info.containsKey("requester")) {
            requester = (String) info.get("requester");
        }

        if (poolName == null) {
            throw new SQLException("The URL used with BobsConnectionPoolDriver must include a token with the pool name");
        }

        try {
            return Resources.getConnection(poolName, requester);
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }

            throw new SQLException("Error while attempting to allocate a Connection using the url " + url + "\n\nUnderlying exception:\n" + ErrorUtils.stackTrace(t));
        }
    }

    public boolean acceptsURL(String url) throws SQLException {
        return url != null && url.toLowerCase().startsWith(URL_PREFIX);
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return null;
    }

    public int getMajorVersion() {
        return 1;
    }

    public int getMinorVersion() {
        return 0;
    }

    public boolean jdbcCompliant() {
        return false;
    }

    private void register() throws SQLException {
        DriverManager.registerDriver(this);
    }

    private static BobsConnectionPoolDriver initializeDriver() {
        try {
            BobsConnectionPoolDriver driver = new BobsConnectionPoolDriver();
            driver.register();
            return driver;
        } catch (Exception exc) {
            throw new Error("Unable to initialize BobsConnectionPool", exc);
        }
    }
}
