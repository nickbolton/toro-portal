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

package net.unicon.alchemist.rdbms;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Properties;

import javax.sql.DataSource;

/**
 * SimpleDataSource provides a, wait for it, Simple DataSource implementation.
 *
 * There are three methods for providing connection information to SimpleDataSource:
 *
 * <ul>
 *  <li>Through the full constructor. See {@link #SimpleDataSource(String,String,String,String)}.
 *  <li>By providing the System properties:
 *      <ul>
 *        <li>simpledatasource.driver - JDBC Driver class
 *        <li>simpledatasource.url    - Database URL
 *        <li>simpledatasource.user   - Database username
 *        <li>simpledatasource.pass   - Database password
 *      </ul>
 *  <li>By providing a simpledatasource.propsfile property that contains the above properties.
 * </ul>
 *
 * @author Eric Andresen
 */
public class SimpleDataSource implements DataSource {
    private final Class driverClass;
    private final String url;
    private final String user;
    private final String pass;

    private int loginTimeout = 0;
    private PrintWriter logWriter = new PrintWriter(System.out);

    public SimpleDataSource (String driver, String url, String user, String pass)
                            throws ClassNotFoundException {
        if (driver == null || driver.trim().equals(""))
            throw new IllegalArgumentException(
                    "Argument 'driver' cannot be null or empty.");
        if (url == null || url.trim().equals(""))
            throw new IllegalArgumentException(
                    "Argument 'url' cannot be null or empty.");
        if (user == null)
            throw new IllegalArgumentException(
                    "Argument 'user' cannot be null.");
        if (pass== null)
            throw new IllegalArgumentException(
                    "Argument 'pass' cannot be null.");

        this.driverClass = Class.forName(driver);
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    public SimpleDataSource() throws ClassNotFoundException, IllegalArgumentException {
        String propsfile = System.getProperty("simpledatasource.propsfile");

        Properties props = System.getProperties();
        if (propsfile != null) {
            props = new Properties(props);
            try {
                props.load(new FileInputStream(propsfile));
            } catch (IOException e) {
                throw new RuntimeException(
                    "Unalble to open file '"+propsfile
                  + "' as specified by property simpledatasource.propsfile",
                  e);
            }
        }

        String driver = props.getProperty("simpledatasource.driver");
        String url   = props.getProperty("simpledatasource.url");
        String user   = props.getProperty("simpledatasource.user");
        String pass   = props.getProperty("simpledatasource.pass");

        if (driver == null || driver.trim().equals(""))
            throw new IllegalArgumentException(
                    "Argument 'driver' cannot be null or empty.");
        if (url == null || url.trim().equals(""))
            throw new IllegalArgumentException(
                    "Argument 'url' cannot be null or empty.");
        if (user == null)
            user = "";
        if (pass == null)
            pass = "";

        this.driverClass = Class.forName(driver);
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    public Connection getConnection() throws SQLException {
        return getConnection(this.user, this.pass);
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public int getLoginTimeout() throws SQLException {
        return this.loginTimeout;
    }

    public void setLoginTimeout(int timeout) throws SQLException {
        this.loginTimeout = timeout;
    }

    public PrintWriter getLogWriter() throws SQLException {
        return this.logWriter;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        this.logWriter = out;
    }
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
      throw new SQLException("Not implemented.");
    }
    public <T> T unwrap(Class<T> iface) throws SQLException {
      throw new SQLException("Not implemented.");
    }
}
