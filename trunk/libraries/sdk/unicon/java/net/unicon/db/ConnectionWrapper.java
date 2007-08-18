/*
 * @(#)ConnectionWrapper.java $Revision: 1.6 $ $Date: 2002/05/02 21:53:06 $
 *
 */

package net.unicon.db;

import java.sql.*;
import java.util.*;
import net.unicon.db.*;
import net.unicon.util.*;

/**
 * Wrapper class for connection.  This class will keep track all the statements
 * that are being open using this connection.  Whenever a statement is closed
 * it will be removed from this connection. 
 *
 * @version $Revision: 1.6 $ $Date: 2002/05/02 21:53:06 $
 * @author Unicon, Inc
 */
public class ConnectionWrapper implements Connection {
    private Connection conn;
    private List stmts = new ArrayList();
    private String DbUrl;
    private String DbUser;
    private String DbPwd;

    /**
     * Constructor
     * @param pConn connection
     * @param url database connection url
     * @param user for connecting to the database
     * @param pwd password for connecting to the database
     */
    public ConnectionWrapper(Connection pConn, String url, String user, 
            String pwd) {
        conn = pConn;
        DbUrl = url;
        DbUser = user;
        DbPwd = pwd;
    }

    /**
     * Resets the connection by closing it and re-establishing the connection 
     * to the database.
     */
    public void resetConnection() {
        Debug.out.println(6,"db","Resetting connection");
        try {
            if (conn == null) {
                conn = DriverManager.getConnection(DbUrl, DbUser, DbPwd);
                Debug.out.println(6, "db", "Got a new connection");
            }
            else {
                Debug.out.println(6, "db", "Connection is okay");
            }
        }
        catch (Exception e) {
            Debug.out.println(6,"db","Error trying to close the connection " +
                    "for reset");
        }
    }

    /**
     * Recreate the prepared statement only.  This function will NOT recreate a 
     * prepared statement wrapper.
     * 
     * @param sql - string to recreate the statement
     * @return PreparedStatement 
     */
    public PreparedStatement recreatePrepared(String sql) 
            throws SQLException {
        Statement temp = conn.prepareStatement(sql);
        return (PreparedStatement)temp;
    }

    /** 
     * Recreate a statement only.  Same thing here, this will not create a 
     * wrapper.  This function will only recreate the statement.
     *
     * @return statement
     */
    public Statement recreateStatement() throws SQLException {
        Statement temp = conn.createStatement();
        return temp; 
    }

    /** 
     * Recreate a callable statement.
     * 
     * @param sql - string to recreate a callable statement
     * @return callable statement
     */
    public CallableStatement recreateCallable(String sql) 
            throws SQLException {
        return conn.prepareCall(sql);
    }

    /**
     * Return the number of statements that are currently open using this
     * connectin.
     * @return number of statements open
     */
    public int getStatementCount() {
        return stmts.size();
    }

    /**
     * Remove a statement from the list.
     * @param pstmt the statement to be removed from the list
     */
    public void removeStatement(Statement pstmt) {
        Debug.out.println(6,"db","Removing statement: " + pstmt);
        stmts.remove(pstmt);
    }
    
    /**
     * Create a regular statement using the StatementWrapper class.
     * @return statement 
     */
    public Statement createStatement() throws SQLException {
        Statement stmt = new StatementWrapper(conn.createStatement(), this);

        stmts.add(stmt);
        return stmt;
    }

    /**
     * Preparestatement using the PrepareStatementWrapper class.
     * @param sql string
     * @return statement using the sql that was passed in
     */
    public PreparedStatement prepareStatement(String sql) 
            throws SQLException {
        Debug.out.println(6,"db","PrepareStatement" + sql);
        PreparedStatement pstmt = new PreparedStatementWrapper(
                conn.prepareStatement(sql), this, sql);

        stmts.add(pstmt);
        return pstmt;
    }

    /**
     * Create a CallableStatement using the CallableStatementWrapper.
     * @param sql string
     * @return CallableStatement
     */
    public CallableStatement prepareCall(String sql) throws SQLException {
        Debug.out.println(6,"db","Creating CallableStatement: " + sql);
        CallableStatement cstmt = new CallableStatementWrapper(
                conn.prepareCall(sql), this, sql);
    
        stmts.add(cstmt);
        return cstmt;
    }
    
    /**
     * Converts the sql to the system's native SQL grammar.
     * @param sql string
     * @return SQL string that is being sent to the system.
     */
    public String nativeSQL(String sql) throws SQLException {
        return conn.nativeSQL(sql);
    }

    /**
     * Sets the connection to auto-commit mode.
     * @param autoCommit boolean value
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        conn.setAutoCommit(autoCommit);
    }

    /**
     * Gets the status of the auto-commit state.
     * @return boolean of the auto-commit state.
     */
    public boolean getAutoCommit() throws SQLException {
        return conn.getAutoCommit();
    }

    /**
     * Commits all changes since the previous commmit or rollback.
     */
    public void commit() throws SQLException {
        conn.commit();
    }

    /**
     * Cancels all changes since the last commit or rollback and releases
     * any locks that are being held.
     */
    public void rollback() throws SQLException {
        int size = stmts.size();

        Debug.out.println(6, "db", "Beginning rolling back connection");

        for(int i = size - 1; i >= 0; i--) {
            if (stmts.get(i) != null) {
                ((Statement)stmts.get(i)).close();
            }
        }

        conn.rollback();
        Debug.out.println(6, "db", "Ending rolling back connection");
    }

    /** 
     * Releases the connection.  This also closes any statements that might be 
     * assoicated with this connection.
     */
    public void close() throws SQLException
    {
        int size = stmts.size();
    
        Debug.out.println(6, "db", "Closing Connection:" + size + 
                " statements");

        for(int i = size - 1; i >= 0; i--) {
            if (stmts.get(i) != null) {
                ((Statement)stmts.get(i)).close();
            }
            else {
                Debug.out.println(6, "db", "Got a null statement at index " + 
                        i);
            }
        }
    
        conn.close();
    }

    /**
     * Checks to see if the connection is closed.
     * @return the boolean of the state of the connection.
     */
    public boolean isClosed() throws SQLException {
        return conn.isClosed();
    }

    /** 
     * Gets the metadata information from this connection.
     * @return the metadata
     */
    public DatabaseMetaData getMetaData() throws SQLException {
        return conn.getMetaData();
    }

    /**
     * Set the connection to read-only mode.
     * @param readOnly boolean value to be set
     */
    public void setReadOnly(boolean readOnly) throws SQLException {
        conn.setReadOnly(readOnly);
    }

    /**
     * Checks to see if the connectin is read-only.
     * @return read-only mode of the connection.
     */
    public boolean isReadOnly() throws SQLException {
        return conn.isReadOnly();
    }

    /**
     * Sets the catalog name.
     * @param catalog name
     */
    public void setCatalog(String catalog) throws SQLException {
        conn.setCatalog(catalog);
    }

    /**
     * Get the connection's catalog name.
     * @return name of catalog
     */
    public String getCatalog() throws SQLException {
        return conn.getCatalog();
    }

    /**
     * Set the transaction isloation level.
     * @param level of isolation
     */
    public void setTransactionIsolation(int level) throws SQLException {
        conn.setTransactionIsolation(level);
    }

    /**
     * Get the isolation level.
     * @return isolation level.
     */
    public int getTransactionIsolation() throws SQLException {
        return conn.getTransactionIsolation();
    }

    /**
     * Returns the first warning that were reported by calls on this 
     * connection.
     * @return SQL warning or Null
     */
    public SQLWarning getWarnings() throws SQLException {
        return conn.getWarnings();
    }

    /**
     * Clears all warnings on this connection.
     */
    public void clearWarnings() throws SQLException {
        conn.clearWarnings();
    }

    /**
     * Create a statement with the given type and concurency.
     * @param resultSettype
     * @param resultSetconcurrency
     * @return statement
     */
    public Statement createStatement(int resultSetType, int 
            resultSetConcurrency) throws SQLException {
        Statement stmt = new StatementWrapper(conn.createStatement(
                resultSetType, resultSetConcurrency), this);

        stmts.add(stmt);
        return stmt;
    }
     
    /** 
     * Prepare a statement based on the result set type and concurrency.
     * @param sql string for this statement
     * @param resultSettype
     * @param resultSetConcurrency 
     * @return a prepared statement
     */
    public PreparedStatement prepareStatement(String sql, int resultSetType, 
            int resultSetConcurrency) throws SQLException {
        PreparedStatement pstmt = new PreparedStatementWrapper(
                conn.prepareStatement(sql, resultSetType, 
                resultSetConcurrency), this, sql);
    
        stmts.add(pstmt);
        return pstmt;
    }

    /**
     * Create a callablestatement using the settings that are passed in.
     * @param sql string
     * @param resultsettype
     * @param resultsetconcurrency type
     * @return callable statement
     */
    public CallableStatement prepareCall(String sql, int resultSetType, 
            int resultSetConcurrency) throws SQLException {
        CallableStatement cstmt = new CallableStatementWrapper(
                conn.prepareCall(sql, resultSetType, resultSetConcurrency),
                this, sql);
    
        Debug.out.println(6, "db", "Opening CallableStatement");
        stmts.add(cstmt);
        return cstmt;
    }

    /**
     * Gets the type map object associated with this connection.
     * @return map object
     */
    public java.util.Map getTypeMap() throws SQLException {
        return conn.getTypeMap();
    }

    /**
     * Sets the map type for this connection.
     */
    public void setTypeMap(java.util.Map map) throws SQLException {
        conn.setTypeMap(map);
    }

    public final static String notImplemented = "This method is not implemented in the CachedResultSet";

    public void setHoldability(int holdability) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public int getHoldability() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Savepoint setSavepoint() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Savepoint setSavepoint(String savepoint) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void rollback(Savepoint savepoint) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public java.sql.PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public java.sql.PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public java.sql.PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }
}

