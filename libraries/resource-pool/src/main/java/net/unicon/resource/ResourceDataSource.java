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

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;

// uPortal Tree.

/**
 * <code>IDataSource</code> implementation for use with the
 * <code>AcademusConnection</code>.  Together, these classes hide connection
 * allocation and release (connection pooling) from clients that access the
 * database through the <code>IDataSource</code> contract.  This class overrides
 * the <code>getConnection()</code> method to allocate a connection from
 * RDBMServices.
 */
public final class ResourceDataSource
implements DataSource {

    private String name;
    private Log log = LogFactory.getLog(ResourceDataSource.class);

    /*
     * Public API.
     */

    public Connection getConnection() {
        return Resources.getConnection(name, name);
    }

    public Connection getConnection(String username, String password) {
        String msg = "Method getConnection(String username, String password) "
                                + "not supported by ResourceDataSource.";
        throw new UnsupportedOperationException(msg);
    }

    public int getLoginTimeout() {
        String msg = "Method getLoginTimeout() not supported by "
                                                + "ResourceDataSource.";
        throw new UnsupportedOperationException(msg);
    }

    public PrintWriter getLogWriter() {
        String msg = "Method getLogWriter() not supported by "
                                                + "ResourceDataSource.";
        throw new UnsupportedOperationException(msg);
    }

    public void setLoginTimeout(int seconds) {
        String msg = "Method setLoginTimeout(int seconds) not supported by "
                                                + "ResourceDataSource.";
        throw new UnsupportedOperationException(msg);
    }

    public void setLogWriter(PrintWriter out)  {
        String msg = "Method setLogWriter(PrintWriter out) not supported by "
                                                + "ResourceDataSource.";
        throw new UnsupportedOperationException(msg);
    }

    ResourceDataSource(String name) {
        this.name = name;
        if (log.isInfoEnabled()) {
            log.info("Using " + name + " Resource Pool");
        }
    }
}
