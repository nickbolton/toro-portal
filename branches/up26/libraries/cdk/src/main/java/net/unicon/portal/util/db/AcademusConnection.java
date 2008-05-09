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

package net.unicon.portal.util.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.Savepoint;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Map;

// uPortal Tree.
import org.jasig.portal.RDBMServices;

/**
 * <code>Connection</code> implementation for use with the
 * <code>AcademusDataSource</code>.  Together, these classes hide connection
 * allocation and release (connection pooling) from clients that access the
 * database through the <code>IDataSource</code> contract.  This class overrides
 * the <code>close()</code> method to release the connection with RDBMServices.
 */
public final class AcademusConnection implements Connection {

    private Connection conn;

    /*
     * Public API.
     */

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public void clearWarnings() throws SQLException {
        conn.clearWarnings();
    }

    /**
     * Overridden to release the underlying <code>Connection</code> with
     * RDBMServices.
     */
    public void close() throws SQLException {
        RDBMServices.releaseConnection(conn);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public void commit() throws SQLException {
        conn.commit();
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public Statement createStatement() throws SQLException {
        return conn.createStatement();
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public Statement createStatement(int resultSetType,
                                            int resultSetConcurrency)
                                        throws SQLException {
        return conn.createStatement(resultSetType, resultSetConcurrency);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public Statement createStatement(int resultSetType,
                                            int resultSetConcurrency,
                                            int resultSetHoldability)
                                        throws SQLException {
        return conn.createStatement(resultSetType, resultSetConcurrency,
                                        resultSetHoldability);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public boolean getAutoCommit() throws SQLException {
        return conn.getAutoCommit();
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public String getCatalog() throws SQLException {
        return conn.getCatalog();
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public int getHoldability() throws SQLException {
        return conn.getHoldability();
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public DatabaseMetaData getMetaData() throws SQLException {
        return conn.getMetaData();
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public int getTransactionIsolation() throws SQLException {
        return conn.getTransactionIsolation();
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public Map getTypeMap() throws SQLException {
        return conn.getTypeMap();
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public SQLWarning getWarnings() throws SQLException {
        return conn.getWarnings();
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public boolean isClosed() throws SQLException {
        return conn.isClosed();
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public boolean isReadOnly() throws SQLException {
        return conn.isReadOnly();
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public String nativeSQL(String sql) throws SQLException {
        return conn.nativeSQL(sql);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public CallableStatement prepareCall(String sql) throws SQLException {
        return conn.prepareCall(sql);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public CallableStatement prepareCall(String sql, int resultSetType,
                                            int resultSetConcurrency)
                                        throws SQLException {
        return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public CallableStatement prepareCall(String sql,
                                            int resultSetType,
                                            int resultSetConcurrency,
                                            int resultSetHoldability)
                                        throws SQLException {
        return conn.prepareCall(sql, resultSetType, resultSetConcurrency,
                                            resultSetHoldability);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public PreparedStatement prepareStatement(String sql,
                                            int autoGeneratedKeys)
                                        throws SQLException {
        return conn.prepareStatement(sql, autoGeneratedKeys);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public PreparedStatement prepareStatement(String sql,
                                            int[] columnIndexes)
                                        throws SQLException {
        return conn.prepareStatement(sql, columnIndexes);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public PreparedStatement prepareStatement(String sql,
                                            int resultSetType,
                                            int resultSetConcurrency)
                                        throws SQLException {
        return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public PreparedStatement prepareStatement(String sql,
                                            int resultSetType,
                                            int resultSetConcurrency,
                                            int resultSetHoldability)
                                        throws SQLException {
        return conn.prepareStatement(sql, resultSetType, resultSetConcurrency,
                                                resultSetHoldability);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public PreparedStatement prepareStatement(String sql,
                                            String[] columnNames)
                                        throws SQLException {
        return conn.prepareStatement(sql, columnNames);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        conn.releaseSavepoint(savepoint);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public void rollback() throws SQLException {
        conn.rollback();
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public void rollback(Savepoint savepoint) throws SQLException {
        conn.rollback(savepoint);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        conn.setAutoCommit(autoCommit);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public void setCatalog(String catalog) throws SQLException {
        conn.setCatalog(catalog);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public void setHoldability(int holdability) throws SQLException {
        conn.setHoldability(holdability);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public void setReadOnly(boolean readOnly) throws SQLException {
        conn.setReadOnly(readOnly);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public Savepoint setSavepoint() throws SQLException {
        return conn.setSavepoint();
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public Savepoint setSavepoint(String name) throws SQLException {
        return conn.setSavepoint(name);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public void setTransactionIsolation(int level) throws SQLException {
        conn.setTransactionIsolation(level);
    }

    /**
     * Calls the corresponding method on the underlying <code>Connection</code>.
     */
    public void setTypeMap(Map map) throws SQLException {
        conn.setTypeMap(map);
    }

    /*
     * Package API.
     */

    AcademusConnection(Connection conn) {

        // Assertions.
        if (conn == null) {
            String msg = "Argument 'conn' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        this.conn = conn;

    }

}
