/*
 * @(#)StatementWrapper.java 11-29-2000
 * @author Unicon, Inc
 * @version 1.0
 *
 */

package net.unicon.db;

import java.sql.*;
import java.util.*;
import net.unicon.db.*;
import net.unicon.util.Debug;
import net.unicon.util.*;

public class StatementWrapper implements Statement 
{
    protected Statement stmt;
    protected List resultSets = new ArrayList();
    protected ConnectionWrapper conn;

    /**
     * Constructor
     * @param pstmt statement
     * @param pconn connection
     */
    public StatementWrapper(Statement pstmt, ConnectionWrapper pconn)
    {
        stmt = pstmt;
        conn = pconn;
        Debug.out.println(6, "db", "Opening Statement: " + pconn.getStatementCount());
    }

    /**
     * Remove a recordset from this statement.
     * @param rst - resultset to be removed
     */
    public void removeResultSet(ResultSet rst) {
        int index = 0;
        
        if (resultSets.contains(rst)) {
            index = resultSets.indexOf(rst);
            resultSets.remove(index);
        }
    }
    
    /**
     * Run the query that is passed in and return the resultset.
     * @param sql string
     * @return resultset of the query
     */
    public ResultSet executeQuery(String sql) throws SQLException
    {
        ResultSet rs = new ResultSetWrapper(stmt.executeQuery(sql), this);

        resultSets.add(rs);
        return rs;
    }

    /** 
     * Run the query that is passed in.  It can be insert, update or delete.
     * @param sql string for update
     * @return number of rows affected
     */
    public int executeUpdate(String sql) throws SQLException
    {
        return stmt.executeUpdate(sql);
    }

    /**
     * Release the statement object.  This function will close all the resultsets 
     * that are assoicated with this statement.  The resultset will call the 
     * the statement remove the resultset from the arraylist.
     */
    public void close() throws SQLException
    {
        int size = resultSets.size();

        for(int i = size - 1; i >= 0; i--)
        {
            Debug.out.println(6, "db", "Closing resultset: "  + i);

            ((ResultSetWrapper)resultSets.get(i)).close();

        }

        Debug.out.println(6, "db", "Closing Statement: " + (conn.getStatementCount() - 1));
        stmt.close();
        conn.removeStatement(this);
    }

    /**
     * Returns the max number of bytes of the column value.
     * @return column size limit or 0 if unlimited
     */
    public int getMaxFieldSize() throws SQLException
    {
        return stmt.getMaxFieldSize();
    }
    
    /**
     * Set the max size for the field.
     * @param max size for the field
     */
    public void setMaxFieldSize(int max) throws SQLException
    {
        stmt.setMaxFieldSize(max);
    }

    /**
     * Gets the max number of rows that the resultset can have.
     * @return number of rows the resultset can have or 0 if unlimited
     */
    public int getMaxRows() throws SQLException
    {
        return stmt.getMaxRows();
    }

    /**
     * Sets the max number of rows the resultset will return.
     * @param max numbers of rows for this statement's resultset
     */
    public void setMaxRows(int max) throws SQLException
    {
        stmt.setMaxRows(max);
    }

    /**
     * Sets escape processing, if on the driver will do subsitution before 
     * sending the SQL to the database.
     * @param enable boolean value
     */
    public void setEscapeProcessing(boolean enable) throws SQLException
    {
        stmt.setEscapeProcessing(enable);
    }

    /**
     * Get the number of seconds the driver will wait for a statement to
     * execute.
     * @return query timeout limit in secs, or 0 for unlimited
     */
    public int getQueryTimeout() throws SQLException
    {
        return stmt.getQueryTimeout();
    }

    /**
     * Set the query timeout.
     * @param seconds
     */
    public void setQueryTimeout(int seconds) throws SQLException
    {
        stmt.setQueryTimeout(seconds);
    }

    /**
     * Cancel the statement object.
     */
    public void cancel() throws SQLException
    {
        stmt.cancel();
    }

    /**
     * Get the first warning reported by this statement.
     * @return warning or null
     */
    public SQLWarning getWarnings() throws SQLException
    {
        return stmt.getWarnings();
    }

    /**
     * Clear all warnings from this statement.
     */
    public void clearWarnings() throws SQLException
    {
        stmt.clearWarnings();
    }

    /**
     * Sets the cursor name for subsequent statement to execute methods.
     * @param name of the cursor
     */
    public void setCursorName(String name) throws SQLException
    {
        stmt.setCursorName(name);
    }

    /**
     * Execute a sql query.
     * @param sql string
     * @return true if resultset, false if it is count or no more results
     */
    public boolean execute(String sql) throws SQLException
    {
        try {
            return stmt.execute(sql);
        }
        catch (SQLException se) {
            Debug.out.println(6, "db", "Error code: " + se.getErrorCode());
            return false;
        }
    }

    /**
     * Returns the current resultset.
     * @return the resultset or the number of rows affected
     */
    public ResultSet getResultSet() throws SQLException 
    {
        ResultSet rs = new ResultSetWrapper(stmt.getResultSet(), this);

        if(!resultSets.contains(rs))
            resultSets.add(rs);

        return rs;
    }

    /**
     * Gets the result update count.
     * @return number of rows affected or -1 if no more results
     */
    public int getUpdateCount() throws SQLException
    {
        return stmt.getUpdateCount();
    }

    /**
     * Get the next resultset.
     * @return true if next result is a resultset, false if its an update 
     * count or there are no more results.
     */
    public boolean getMoreResults() throws SQLException
    {
        return stmt.getMoreResults();
    }

    /**
     * Sets the direction the result will be processed.
     * @param direction of data retrieval
     */
    public void setFetchDirection(int direction) throws SQLException
    {
        stmt.setFetchDirection(direction);
    }

    /**
     * Get the fetch direction.
     * @return the direction of the data retrieval
     */
    public int getFetchDirection() throws SQLException
    {
        return stmt.getFetchDirection();
    }

    /**
     * Sets the number of rows that should be fetched from the database.
     * @param rows to be retrieved
     */
    public void setFetchSize(int rows) throws SQLException
    {
        stmt.setFetchSize(rows);
    }

    /**
     * Get the number of result set rows that this statement will retrieve.
     * @return the number of rows to be returned by this statment.
     */
    public int getFetchSize() throws SQLException
    {
        return stmt.getFetchSize();
    }

    /**
     * Retrives the result set concurrency.
     * @return the number
     */
    public int getResultSetConcurrency() throws SQLException
    {
        return stmt.getResultSetConcurrency();
    }

    /**
     * Get the result set type.
     * @return the result set type
     */
    public int getResultSetType()  throws SQLException
    {
        return stmt.getResultSetType();
    }

    /**
     * Add a new batch to the statement.
     * @param sql string
     */
    public void addBatch( String sql ) throws SQLException
    {
        stmt.addBatch(sql);
    }

    /**
     * Clear the commands of the current batch.
     */
    public void clearBatch() throws SQLException
    {
        stmt.clearBatch();
    }

    /**
     * Submits a batch to the database for execution.
     * @return an array of update counts
     */
    public int[] executeBatch() throws SQLException
    {
        return stmt.executeBatch();
    }

    /**
     * Returns the connection for this statement.
     * @return the connection
     */
    public Connection getConnection()  throws SQLException
    {
        return stmt.getConnection();
    }

	public final static String notImplemented = "This method is not implemented in the CachedResultSet";

    public boolean getMoreResults(int current) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public ResultSet getGeneratedKeys() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public int getResultSetHoldability() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }
}

