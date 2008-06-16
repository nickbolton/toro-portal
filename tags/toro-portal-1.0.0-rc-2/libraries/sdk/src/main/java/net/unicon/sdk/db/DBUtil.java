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

package net.unicon.sdk.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DBUtil {
    
    private static final Log log = LogFactory.getLog(DBUtil.class);
    
    public static String getDbProductName(Connection conn) {
        String connectionPackage = conn.getClass().getPackage().getName().toLowerCase();
        if (connectionPackage.indexOf("oracle") >= 0) {
            return "oracle";
        } else if (connectionPackage.indexOf("postgresql") >= 0) {
            return "postgresql";
        } else if (connectionPackage.indexOf("microsoft") >= 0 ||
                   connectionPackage.indexOf("sqlserver") >= 0 ||
                   connectionPackage.indexOf("datadirect") >= 0) {
            return "microsoft";
        } else {
            return "unknown";
        }
    }

    public static Object queryDatum(Connection conn, String sql) throws SQLException {
        List row = queryUnique(conn, sql);
        return (row == null || row.size() == 0) ? null : row.get(0);
    }

    public static Object queryDatum(Connection conn, String sql, Object parameter) throws SQLException {
        List row = queryUnique(conn, sql, parameter);
        return (row == null || row.size() == 0) ? null : row.get(0);
    }

    public static Object queryDatum(Connection conn, String sql, List parameters) throws SQLException {
        List row = queryUnique(conn, sql, parameters);
        return (row == null || row.size() == 0) ? null : row.get(0);
    }

    public static List queryUnique(Connection conn, String sql) throws SQLException {
        return singleSelect(conn, sql);
    }

    public static List queryUnique(Connection conn, String sql, Object parameter) throws SQLException {
        return singleSelect(conn, sql, parameter);
    }

    public static List queryUnique(Connection conn, String sql, List parameters) throws SQLException {
        return singleSelect(conn, sql, parameters);
    }

    public static List singleSelect(Connection conn, String sql) throws SQLException {
        List results = executeQuery(conn, sql);
        if (results.size() == 0) {
            return null;
        } else if (results.size() == 1) {
            return (List) results.get(0);
        } else {
            System.out.println("sql = " + sql);
        }

        throw new Error("Multiple results in singleSelect");
    }

    public static List singleSelect(Connection conn, String sql, Object parameter) throws SQLException {
        List parameters = new ArrayList(3);
        parameters.add(parameter);
        return singleSelect(conn, sql, parameters);
    }

    public static List singleSelect(Connection conn, String sql, List parameters) throws SQLException {
        List results = executeQuery(conn, sql, parameters);
        if (results.size() == 0) {
            return null;
        } else if (results.size() == 1) {
            return (List) results.get(0);
        } else {
            System.out.println("sql = " + sql);
        }

        System.out.println("parameters = " + parameters);
        throw new Error("Multiple results in singleSelect");
    }

    public static List executeQuery(Connection conn, String sql) throws SQLException {
        return executeQuery(conn, sql, new ArrayList());
    }

    public static List executeQuery(Connection conn, String sql, int input) throws SQLException {
        List inputs = new ArrayList(3);
        inputs.add(new Integer(input));
        return executeQuery(conn, sql, inputs);
    }

    public static List executeQuery(Connection conn, String sql, Object input) throws SQLException {
        List inputs = new ArrayList(3);
        inputs.add(input);
        return executeQuery(conn, sql, inputs);
    }

    public static List executeQuery(Connection conn, String sql, List inputs) throws SQLException {
        List answer = new ArrayList();
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            statement = conn.prepareStatement(sql);
            for (int i = 0; i < inputs.size(); i++) {
                statement.setObject(i + 1, inputs.get(i));
            }
            rs = statement.executeQuery();
            while (rs.next()) {
                List row = new ArrayList();
                ResultSetMetaData metaData = rs.getMetaData();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    row.add(rs.getObject(i));
                }
                answer.add(row);
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            safeClose(rs, statement);
        }

        return answer;
    }

    public static int executeUpdate(Connection conn, String sql) throws SQLException {
        return executeUpdate(conn, sql, new ArrayList());
    }

    public static int executeUpdate(Connection conn, String sql, int input) throws SQLException {
        List inputs = new ArrayList(3);
        inputs.add(new Integer(input));
        return executeUpdate(conn, sql, inputs);
    }

    public static int executeUpdate(Connection conn, String sql, Object input) throws SQLException {
        List inputs = new ArrayList(3);
        inputs.add(input);
        return executeUpdate(conn, sql, inputs);
    }

    public static int executeUpdate(Connection conn, String sql, List inputs) throws SQLException {
        PreparedStatement statement = null;
        int count = -1;
        int place = -1;

        try {
            statement = conn.prepareStatement(sql);
            for (int i = 0; i < inputs.size(); i++) {
                place = i;
                Object input = inputs.get(i);
                if (input instanceof java.util.Date) {
                    statement.setTimestamp(i + 1, new java.sql.Timestamp(((java.util.Date) input).getTime()));
                } else {
                    statement.setObject(i + 1, inputs.get(i));
                }
            }
            count = statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("inputs = " + inputs + ", place = " + place);
            throw e;
        } finally {
            safeClose(statement);
        }

        return count;
    }

    private static final int IN_CLAUSE_LIMIT = 50;

    public static List executeInQuery(Connection conn, String baseSQL, String key, List inputs) throws SQLException {
        if (inputs == null || inputs.isEmpty()) {
            return new ArrayList();
        }

        List allAnswers = new ArrayList();
        int start = 0;
        while (start < inputs.size()) {
            int inputCount = Math.min(IN_CLAUSE_LIMIT, inputs.size() - start);
            String sql = baseSQL + key + " IN " + buildInClause(inputCount);
            List theseAnswers = executeQuery(conn, sql, inputs.subList(start, start + inputCount));
            allAnswers.addAll(theseAnswers);
            start += inputCount;
        }

        return allAnswers;
    }

    public static List executeInQuery(Connection conn, String baseSQL, String key, List inputs, Object parameter) throws SQLException {
        if (inputs == null || inputs.isEmpty()) {
            return new ArrayList();
        }

        List allAnswers = new ArrayList();
        int start = 0;
        while (start < inputs.size()) {
            int inputCount = Math.min(IN_CLAUSE_LIMIT, inputs.size() - start);
            String sql = baseSQL + key + " IN " + buildInClause(inputCount);
            List parameters = new ArrayList();
            parameters.add(parameter);
            parameters.addAll(inputs.subList(start, start + inputCount));
            List theseAnswers = executeQuery(conn, sql, parameters);
            allAnswers.addAll(theseAnswers);
            start += inputCount;
        }

        return allAnswers;
    }

    public static int executeInUpdate(Connection conn, String baseSQL, String key, List inputs) throws SQLException {
        if (inputs == null || inputs.isEmpty()) {
            return 0;
        }

        int totalCount = 0;
        int start = 0;
        while (start < inputs.size()) {
            int inputCount = Math.min(IN_CLAUSE_LIMIT, inputs.size() - start);
            String sql = baseSQL + key + " IN " + buildInClause(inputCount);
            int count = executeUpdate(conn, sql, inputs.subList(start, start + inputCount));
            totalCount += count;
            start += inputCount;
        }

        return totalCount;
    }

    public static int executeInUpdate(Connection conn, String baseSQL, String key, List inputs, List otherParameters) throws SQLException {
        if (inputs == null || inputs.isEmpty()) {
            return 0;
        }

        int totalCount = 0;
        int start = 0;
        while (start < inputs.size()) {
            int inputCount = Math.min(IN_CLAUSE_LIMIT, inputs.size() - start);
            String sql = baseSQL + key + " IN " + buildInClause(inputCount);
            List allParameters = new ArrayList(otherParameters);
            allParameters.addAll(inputs.subList(start, start + inputCount));
            int count = executeUpdate(conn, sql, allParameters);
            totalCount += count;
            start += inputCount;
        }

        return totalCount;
    }

    protected static String buildInClause(int size) {
        StringBuffer buffer = new StringBuffer("(");
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                buffer.append(", ");
            }

            buffer.append("?");
        }
        buffer.append(" )");

        return buffer.toString();
    }

    public static CachedResultSet executeCachedQuery(PreparedStatement pstmt) throws SQLException {
        ResultSet rs = null;
        CachedResultSet crs = null;
        try {
            rs = pstmt.executeQuery();
            crs = new CachedResultSet(rs);
        } catch (SQLException e) {
            throw e;
        } finally {
            safeClose(rs, pstmt);
        }

        return crs;
    }

    public static boolean safeRollback(Connection conn) {
        if (conn == null) {
            return true;
        }

        try {
            conn.rollback();
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }

            return false;
        }

        return true;
    }

    public static void safeClose(ResultSet rs, Statement stmt) {
        safeClose(rs);
        safeClose(stmt);
    }

    public static void safeClose(ResultSet rs) {
        if (rs == null) {
            return;
        }

        try {
            rs.close();
        } catch (SQLException e) {
            log.error("Error closing ResultSet", e);
        }
    }

    public static void safeClose(Statement stmt) {
        if (stmt == null) {
            return;
        }

        try {
            stmt.close();
        } catch (SQLException e) {
            log.error("Error closing Statement", e);
        }
    }

    public static void begin(Connection conn, String productName) throws SQLException {
        if (isSQLServer(productName)) {
            executeUpdate(conn, "begin tran");
        } else if (isPostgres(productName)) {
            executeUpdate(conn, "begin");
        }
    }

    public static String forUpdateString(String productName) {
        if (isOracle(productName)) {
            return "for update";
        } 

        return "";
    }

    public static void commit(Connection conn, String productName) throws SQLException {
        if (isSQLServer(productName)) {
            executeUpdate(conn, "commit");
        }

        conn.commit();
    }

    public static boolean isOracle(String productName) {
        return productName.equalsIgnoreCase("oracle");
    }

    public static boolean isPostgres(String productName) {
        return productName.equalsIgnoreCase("postgresql");
    }

    public static boolean isSQLServer(String productName) {
        return productName.equalsIgnoreCase("sqlserver")
            || productName.equalsIgnoreCase("datadirect")
            || productName.equalsIgnoreCase("microsoft");
    }
}

