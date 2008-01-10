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

import java.sql.*;
import java.util.*;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import net.unicon.sdk.db.DBUtil;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.log.ILogService;
import net.unicon.sdk.log.LogServiceFactory;


public class AcademusDBUtil {
	
    private static int failedConnectionCount = 0;

    private static Map metaTable = new Hashtable();

    private static ILogService logService = LogServiceFactory.instance();

    private static final String dbSource = UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).getProperty("net.unicon.portal.util.db.AcademusDBUtil.dbSource");
    private static final String baseJndiContext = UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).getProperty("net.unicon.portal.util.db.AcademusDBUtil.baseJndiContext");
    

    public static Connection getDBConnection() throws Exception {
        return getDBConnection(dbSource);
    }

    public static Connection getDBConnection(String name) throws Exception {
        Context initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup(baseJndiContext);
        return getDBConnection(envCtx, name);
    }

    public static Connection getDBConnection(Context jndiContext, String name) throws Exception {
        Connection conn = null;
        try {
            DataSource ds = (DataSource)jndiContext.lookup(name);
            if (ds != null) {
                conn = ds.getConnection();
                // Make sure autocommit is set to true
                if (conn != null && !conn.getAutoCommit()) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                }
            } else {
                logService.log(ILogService.ERROR, "The data source '"+jndiContext.getNameInNamespace()+"/"+name+"' could not be found.");
            }
        } catch (javax.naming.NamingException ne) {
            logService.log(ILogService.ERROR, ne);
        } catch (SQLException sqle) {
            logService.log(ILogService.ERROR, sqle);
        }
        return conn;
    }

    public static void releaseDBConnection(Connection conn) throws Exception {
        if (conn != null) {
            conn.close();
        }
    }

    public static Connection safeGetDBConnection() {

        try {

            return getDBConnection();

        } catch (Exception e) {

            return null;

        }

    }

    public static void safeReleaseDBConnection(Connection connection) {

        try {

            releaseDBConnection(connection);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void safeClosePreparedStatement(PreparedStatement ps) {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void safeCloseStatement(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void safeCloseResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getJdbcDriver(String jndiName) {
        if (metaTable.get(jndiName) == null) {
            initDbMetaData(jndiName);
        }
        return ((MetaData)metaTable.get(jndiName)).jdbcDriver;
    }

    public static String getJdbcUrl(String jndiName) {
        if (metaTable.get(jndiName) == null) {
            initDbMetaData(jndiName);
        }
        return ((MetaData)metaTable.get(jndiName)).jdbcUrl;
    }

    public static String getJdbcUser(String jndiName) {
        if (metaTable.get(jndiName) == null) {
            initDbMetaData(jndiName);
        }
        return ((MetaData)metaTable.get(jndiName)).jdbcUser;
    }

    private static synchronized void initDbMetaData(String jndiName) {
        Connection conn = null;

        try {
            conn = getDBConnection(jndiName);
            DatabaseMetaData metaData = conn.getMetaData();
            MetaData md = new MetaData();
            md.jdbcDriver = metaData.getDriverName();
            md.jdbcUrl = metaData.getURL();
            md.jdbcDriver = metaData.getUserName();
            metaTable.put(jndiName, md);
        } catch (Exception e) {
            if (conn == null) {
                if (failedConnectionCount == 0) {
                    logService.log(ILogService.ERROR, "AcademusDBUtil::initDbMetaData : failed to retrieve connection");
                }
                failedConnectionCount++;
            } else {
                logService.log(ILogService.ERROR, "AcademusDBUtil::initDbMetaData : failed to set meta data.", e);
            }
        } finally {
            safeReleaseDBConnection(conn);
        }
    }

    public static List query(Connection conn, String sql, List parameters) throws Exception {

        return DBUtil.executeQuery(conn, sql, parameters);

    }

    public static List query(String sql) throws Exception {

        Connection connection = null;

        try {

            connection = getDBConnection();

            return DBUtil.executeQuery(connection, sql);

        } catch (Error error) {

            throw error;

        } catch (Exception exception) {

            throw exception;

        } finally {

            releaseDBConnection(connection);

        }

    }

    public static List query(String sql, List parameters) throws Exception {

        Connection connection = null;

        try {

            connection = getDBConnection();

            return DBUtil.executeQuery(connection, sql, parameters);

        } catch (Error error) {

            throw error;

        } catch (Exception exception) {

            throw exception;

        } finally {

            releaseDBConnection(connection);

        }

    }

    public static void formatComparison(String columnName, List desiredValues, StringBuffer queryBuffer) {

        queryBuffer.append(columnName);

        if (desiredValues.size() == 1) {

            queryBuffer.append(" = ? ");

        } else {

            queryBuffer.append(" in ( ");

            for (int i = 0; i < desiredValues.size(); i++) {

                if (i > 0) {

                    queryBuffer.append(", ");

                }

                queryBuffer.append("?");

            }

            queryBuffer.append(" ) ");

        }

    }

    public static void safeRollback(Connection connection) {

        try {

            connection.rollback();

        } catch (Throwable t) {

            if (t instanceof ThreadDeath) {

                throw (ThreadDeath) t;

            }

        }

    }

    protected final static class MetaData {
        public String jdbcDriver;
        public String jdbcUrl;
        public String jdbcUser;
    }

}

