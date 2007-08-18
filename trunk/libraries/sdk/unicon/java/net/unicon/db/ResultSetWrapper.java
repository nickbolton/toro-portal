/*
 * @(#)ResultSetWrapper.java 11-22-2000
 * @author Unicon, Inc
 * @version 1.0
 *
 * Wrapper class for the resultsets.  This wrapper class will help reduce 
 * problems with developers not closing the resultsets when they are done
 * by closing the resultsets for them. 
 *
 * This class is exactly like the ResultSet class from Sun.  For more 
 * information about the functions, please see the Java api.  
 */

package net.unicon.db;

import java.math.BigDecimal;
import java.util.Calendar;
import java.sql.*;
import net.unicon.db.*;
import net.unicon.util.Debug;
import net.unicon.util.*;

public class ResultSetWrapper implements ResultSet {

    private ResultSet rs;
    private StatementWrapper stmt;


private Object lock = "Lock";
private static Object staticLock = "Lock";
private static int count = 0;
private boolean closed;
private static int maxHit = 0;

    /**
     * Constructor
     * @param resultset
     * @param stm - pointer to the parent statement that this resultst 
     *  belongs to
     */
    protected ResultSetWrapper(ResultSet rst, StatementWrapper stm) {
        rs = rst;
        stmt = stm;
        synchronized(lock) {
            closed=false;
        }
        synchronized(staticLock)
        {
            count++;
            if (count > maxHit)
            {
                maxHit = count;
                Debug.out.println(6,"db","Creating a ResultSetWrapper: (NEW MAXIMUM) " + count);
            }
            else
            {
                Debug.out.println(6,"db","Creating a ResultSetWrapper: " + count);
            }
        }
    }

    /**
     * This function will return true if there is another record in 
     * the resultset, otherwise false if it is the end of the resultset.
     * @return boolean, true if there are more records in the resultset.
     */
    public boolean next() throws SQLException {
        try {
            return rs.next();
        }
        catch(SQLException se) {
            throw (se);
        }
    }

    /**
     * Close the resultset and tell the parent statement to remove this
     * resultset from the statement.
     */
    public void close() throws SQLException {
        try {
            Debug.out.println(6,"db","Closing Resultset:" + this);
            stmt.removeResultSet(this);
            rs.close();

            if (closed == false)
            {
                count--;
                synchronized(lock) {
                    closed=true;
                }
            }
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Checks to see if the last column read has a value of SQL NULL or not.
     * @return boolean value of the last column read
     */
    public boolean wasNull() throws SQLException {
        try {
            return rs.wasNull();
        }
        catch (SQLException se) {
            throw se;
        }
    }
   
    /**
     * Gets the value of the column values. 
     * @param column index
     * @return string value of the column
     */ 
    public String getString(int columnIndex) throws SQLException {
        try {
            return rs.getString(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Gets the value of the current column of the current row and return
     * it as a boolean.
     * @param column index
     * @return boolean value of the current column
     */
    public boolean getBoolean(int columnIndex) throws SQLException {
        try {
            return rs.getBoolean(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Gets the value of a column in the current row as a Java byte.
     *
     * @param column index the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is 0
     */
    public byte getByte(int columnIndex) throws SQLException {
        try {
            return rs.getByte(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    }


    /**
     * Gets the value of a column in the current row as a Java short.
     *
     * @param column index the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is 0
     */
    public short getShort(int columnIndex) throws SQLException {
        try {
            return rs.getShort(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Gets the value of a column in the current row as a Java int.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is 0
     */
    public int getInt(int columnIndex) throws SQLException {
        try {
            return rs.getInt(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Gets the value of a column in the current row as a Java long.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is 0
     */
    public long getLong(int columnIndex) throws SQLException {
        try {
            return rs.getLong(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Gets the value of a column in the current row as a Java float.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is 0
     */
    public float getFloat(int columnIndex) throws SQLException {
        try {
            return rs.getFloat(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Gets the value of a column in the current row as a Java double.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is 0
     */
    public double getDouble(int columnIndex) throws SQLException {
        try {
            return rs.getDouble(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Gets the value of a column in the current row as a java.math.BigDecimal object.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param scale the number of digits to the right of the decimal
     * @return the column value; if the value is SQL NULL, the result is null
     */
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        try {
            return rs.getBigDecimal(columnIndex, scale);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Gets the value of a column in the current row as a Java byte array.
     * The bytes represent the raw values returned by the driver.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database access error occurs
     */
    public byte[] getBytes(int columnIndex) throws SQLException {
        try {
            return rs.getBytes(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Gets the value of a column in the current row as a java.sql.Date object.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is null
     */
    public java.sql.Date getDate(int columnIndex) throws SQLException {
        try {
            return rs.getDate(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Gets the value of a column in the current row as a java.sql.Time object.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is null
     */
    public java.sql.Time getTime(int columnIndex) throws  SQLException {
        try {
            return rs.getTime(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
     } 

    /**
     * Gets the value of a column in the current row as a java.sql.Timestamp object.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is null
     */
    public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException {
        try {
            return rs.getTimestamp(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
     } 

    /**
     * Gets the value of a column in the current row as a stream of
     * ASCII characters. The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large LONGVARCHAR values.  The JDBC driver will
     * do any necessary conversion from the database format into ASCII.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a get method implicitly closes the stream.  Also, a
     * stream may return 0 when the method <code>available</code>
     * is called whether there is data
     * available or not.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     * as a stream of one byte ASCII characters.  If the value is SQL NULL
     * then the result is null.  
     */
    public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException {
        try {
            return rs.getAsciiStream(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a stream of
     * Unicode characters. The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large LONGVARCHAR values.  The JDBC driver will
     * do any necessary conversion from the database format into Unicode.
     * The byte format of the Unicode stream must Java UTF-8,
     * as specified in the Java Virtual Machine Specification.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     * as a stream of two-byte Unicode characters.  If the value is SQL NULL
     * then the result is null.  
     */
    public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException {
        try {
            return rs.getUnicodeStream(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a stream of
     * uninterpreted bytes. The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large LONGVARBINARY values.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     * as a stream of uninterpreted bytes.  If the value is SQL NULL
     * then the result is null.  
     */
    public java.io.InputStream getBinaryStream(int columnIndex) throws SQLException {
        try {
            return rs.getBinaryStream(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    }


    //======================================================================
    // Methods for accessing results by column name
    //======================================================================

    /**
     * Gets the value of a column in the current row as a Java String.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is null
     */
    public String getString(String columnName) throws SQLException {
        try {
            return rs.getString(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Gets the value of a column in the current row as a Java boolean.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is false
     */
    public boolean getBoolean(String columnName) throws SQLException {
        try {
            return rs.getBoolean(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a Java byte.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is 0
     */
    public byte getByte(String columnName) throws SQLException {
        try {
            return rs.getByte(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a Java short.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is 0
     */
    public short getShort(String columnName) throws SQLException {
        try {
            return rs.getShort(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a Java int.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is 0
     */
    public int getInt(String columnName) throws SQLException {
        try {
            return rs.getInt(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a Java long.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is 0
     */
    public long getLong(String columnName) throws SQLException {
        try {
            return rs.getLong(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Gets the value of a column in the current row as a Java float.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is 0
     */
    public float getFloat(String columnName) throws SQLException {
        try {
            return rs.getFloat(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a Java double.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is 0
     */
    public double getDouble(String columnName) throws SQLException {
        try {
            return rs.getDouble(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a java.math.BigDecimal 
     * object.
     *
     * @param columnName the SQL name of the column
     * @param scale the number of digits to the right of the decimal
     * @return the column value; if the value is SQL NULL, the result is null
     */
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        try {
            return rs.getBigDecimal(columnName, scale);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a Java byte array.
     * The bytes represent the raw values returned by the driver.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is null
     */
    public byte[] getBytes(String columnName) throws SQLException {
        try {
            return rs.getBytes(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a java.sql.Date object.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is null
     */
    public java.sql.Date getDate(String columnName) throws SQLException {
        try {
            return rs.getDate(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a java.sql.Time object.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is null
     */
    public java.sql.Time getTime(String columnName) throws SQLException {
        try {
            return rs.getTime(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a java.sql.Timestamp object.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is null
     */
    public java.sql.Timestamp getTimestamp(String columnName) throws SQLException {
        try {
            return rs.getTimestamp(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a stream of
     * ASCII characters. The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large LONGVARCHAR values.  The JDBC driver will
     * do any necessary conversion from the database format into ASCII.
     *
     * @param columnName the SQL name of the column
     * @return a Java input stream that delivers the database column value
     * as a stream of one byte ASCII characters.  If the value is SQL NULL
     * then the result is null.
     */
    public java.io.InputStream getAsciiStream(String columnName) throws SQLException {
        try {
            return rs.getAsciiStream(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a stream of
     * Unicode characters. The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large LONGVARCHAR values.  The JDBC driver will
     * do any necessary conversion from the database format into Unicode.
     * The byte format of the Unicode stream must be Java UTF-8,
     * as defined in the Java Virtual Machine Specification.
     *
     * @param columnName the SQL name of the column
     * @return a Java input stream that delivers the database column value
     * as a stream of two-byte Unicode characters.  If the value is SQL NULL
     * then the result is null.
     */
    public java.io.InputStream getUnicodeStream(String columnName) throws SQLException {
        try {
            return rs.getUnicodeStream(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a stream of
     * uninterpreted bytes. The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large LONGVARBINARY values.  The JDBC driver will
     * do any necessary conversion from the database format into uninterpreted
     * bytes.
     *
     * @param columnName the SQL name of the column
     * @return a Java input stream that delivers the database column value
     * as a stream of uninterpreted bytes.  If the value is SQL NULL
     * then the result is null.
     */
    public java.io.InputStream getBinaryStream(String columnName) throws SQLException{
        try {
            return rs.getBinaryStream(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    //=====================================================================
    // Advanced features:
    //=====================================================================

    /**
     * <p>The first warning reported by calls on this ResultSet is
     * returned. Subsequent ResultSet warnings will be chained to this
     * SQLWarning.
     *
     * <P>The warning chain is automatically cleared each time a new
     * row is read.
     *
     * <P><B>Note:</B> This warning chain only covers warnings caused
     * by ResultSet methods.  Any warning caused by statement methods
     * (such as reading OUT parameters) will be chained on the
     * Statement object. 
     *
     * @return the first SQLWarning or null 
     */
    public SQLWarning getWarnings() throws SQLException {
        try {
            return rs.getWarnings();
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * After this call getWarnings returns null until a new warning is
     * reported for this ResultSet.  
     *
     */
    public void clearWarnings() throws SQLException {
        try {
            rs.clearWarnings();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the name of the SQL cursor used by this ResultSet.
     *
     * <P>In SQL, a result table is retrieved through a cursor that is
     * named. The current row of a result can be updated or deleted
     * using a positioned update/delete statement that references the
     * cursor name. To insure that the cursor has the proper isolation
     * level to support update, the cursor's select statement should be 
     * of the form 'select for update'. If the 'for update' clause is 
     * omitted the positioned updates may fail.
     * 
     * <P>JDBC supports this SQL feature by providing the name of the
     * SQL cursor used by a ResultSet. The current row of a ResultSet
     * is also the current row of this SQL cursor.
     *
     * <P><B>Note:</B> If positioned update is not supported a
     * SQLException is thrown
     *
     * @return the ResultSet's SQL cursor name
     */
    public String getCursorName() throws SQLException {
        try {
            return rs.getCursorName();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Retrieves the  number, types and properties of a ResultSet's columns.
     *
     * @return the description of a ResultSet's columns
     */
    public ResultSetMetaData getMetaData() throws SQLException {
        try {
            return rs.getMetaData();
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * <p>Gets the value of a column in the current row as a Java object.
     *
     * <p>This method will return the value of the given column as a
     * Java object.  The type of the Java object will be the default
     * Java object type corresponding to the column's SQL type,
     * following the mapping for built-in types specified in the JDBC 
     * spec.
     *
     * <p>This method may also be used to read datatabase-specific
     * abstract data types.
     *
     * In the JDBC 2.0 API, the behavior of method
     * <code>getObject</code> is extended to materialize  
     * data of SQL user-defined types.  When the a column contains
     * a structured or distinct value, the behavior of this method is as 
     * if it were a call to: getObject(columnIndex, 
     * this.getStatement().getConnection().getTypeMap()).
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a java.lang.Object holding the column value  
     */
    public Object getObject(int columnIndex) throws SQLException {
        try {
            return rs.getObject(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * <p>Gets the value of a column in the current row as a Java object.
     *
     * <p>This method will return the value of the given column as a
     * Java object.  The type of the Java object will be the default
     * Java object type corresponding to the column's SQL type,
     * following the mapping for built-in types specified in the JDBC 
     * spec.
     *
     * <p>This method may also be used to read datatabase-specific
     * abstract data types.
     *
     * JDBC 2.0
     *
     * In the JDBC 2.0 API, the behavior of method
     * <code>getObject</code> is extended to materialize  
     * data of SQL user-defined types.  When the a column contains
     * a structured or distinct value, the behavior of this method is as 
     * if it were a call to: getObject(columnIndex, 
     * this.getStatement().getConnection().getTypeMap()).
     *
     * @param columnName the SQL name of the column
     * @return a java.lang.Object holding the column value.  
     */
    public Object getObject(String columnName) throws SQLException {
        try {
            return rs.getObject(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    //----------------------------------------------------------------

    /**
     * Maps the given Resultset column name to its ResultSet column index.
     *
     * @param columnName the name of the column
     * @return the column index
     */
    public int findColumn(String columnName) throws SQLException {
        try {
            return rs.findColumn(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 


    //---------------------------------------------------------------------
    // Getter's and Setter's
    //---------------------------------------------------------------------

    /**
     * <p>Gets the value of a column in the current row as a java.io.Reader.
     * @param columnIndex the first column is 1, the second is 2, ...
     */
    public java.io.Reader getCharacterStream(int columnIndex) throws SQLException {
        try {
            return rs.getCharacterStream(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * <p>Gets the value of a column in the current row as a java.io.Reader.
     * @param columnName the name of the column
     * @return the value in the specified column as a <code>java.io.Reader</code>
     */
    public java.io.Reader getCharacterStream(String columnName) throws SQLException {
        try {
            return getCharacterStream(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a java.math.BigDecimal 
     * object with full precision.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value (full precision); if the value is SQL NULL, 
     * the result is null
     */
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        try {
            return rs.getBigDecimal(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a java.math.BigDecimal 
     * object with full precision.
     * @param columnName the column name
     * @return the column value (full precision); if the value is SQL NULL, 
     * the result is null
     */
    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        try {
            return rs.getBigDecimal(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    //---------------------------------------------------------------------
    // Traversal/Positioning
    //---------------------------------------------------------------------

    /**
     * <p>Indicates whether the cursor is before the first row in the result 
     * set.   
     *
     * @return true if the cursor is before the first row, false otherwise. Returns
     * false when the result set contains no rows.
     */
    public boolean isBeforeFirst() throws SQLException {
        try {
            return rs.isBeforeFirst();
        }
        catch (SQLException se) {
            throw se;
        }
    } 
      
    /**
     * <p>Indicates whether the cursor is after the last row in the result 
     * set.   
     *
     * @return true if the cursor is  after the last row, false otherwise.  Returns
     * false when the result set contains no rows.
     */
    public boolean isAfterLast() throws SQLException {
        try {
            return rs.isAfterLast(); 
        }
        catch (SQLException se) {
            throw se;
        }
    }
 
    /**
     * <p>Indicates whether the cursor is on the first row of the result set.   
     *
     * @return true if the cursor is on the first row, false otherwise.   
     */
    public boolean isFirst() throws SQLException {
        try {
            return rs.isFirst();
        }
        catch (SQLException se) {
            throw se;
        }
    } 
 
    /**
     * <p>Indicates whether the cursor is on the last row of the result set.   
     * Note: Calling the method <code>isLast</code> may be expensive
     * because the JDBC driver
     * might need to fetch ahead one row in order to determine 
     * whether the current row is the last row in the result set.
     *
     * @return true if the cursor is on the last row, false otherwise. 
     */
    public boolean isLast() throws SQLException {
        try {
            return rs.isLast();
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * <p>Moves the cursor to the front of the result set, just before the
     * first row. Has no effect if the result set contains no rows.
     */
    public void beforeFirst() throws SQLException {
        try {
            rs.beforeFirst();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * <p>Moves the cursor to the end of the result set, just after the last
     * row.  Has no effect if the result set contains no rows.
     */
    public void afterLast() throws SQLException {
        try {
            rs.afterLast();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * <p>Moves the cursor to the first row in the result set.  
     *
     * @return true if the cursor is on a valid row; false if
     * there are no rows in the result set
     */
    public boolean first() throws SQLException {
        try {
            return rs.first(); 
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * <p>Moves the cursor to the last row in the result set.  
     *
     * @return true if the cursor is on a valid row;
     * false if there are no rows in the result set
     */
    public boolean last() throws SQLException {
        try {
            return rs.last();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * <p>Retrieves the current row number.  The first row is number 1, the
     * second number 2, and so on.  
     *
     * @return the current row number; 0 if there is no current row
     */
    public int getRow() throws SQLException {
        try {
            return rs.getRow();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * <p>Moves the cursor to the given row number in the result set.
     *
     * <p>If the row number is positive, the cursor moves to 
     * the given row number with respect to the
     * beginning of the result set.  The first row is row 1, the second
     * is row 2, and so on. 
     *
     * <p>If the given row number is negative, the cursor moves to
     * an absolute row position with respect to
     * the end of the result set.  For example, calling
     * <code>absolute(-1)</code> positions the 
     * cursor on the last row, <code>absolute(-2)</code> indicates the next-to-last
     * row, and so on.
     *
     * <p>An attempt to position the cursor beyond the first/last row in
     * the result set leaves the cursor before/after the first/last
     * row, respectively.
     *
     * <p>Note: Calling <code>absolute(1)</code> is the same
     * as calling <code>first()</code>.
     * Calling <code>absolute(-1)</code> is the same as calling <code>last()</code>.
     *
     * @return true if the cursor is on the result set; false otherwise
     */
    public boolean absolute(int row ) throws SQLException {
        try {
            return rs.absolute(row);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * <p>Moves the cursor a relative number of rows, either positive or negative.
     * Attempting to move beyond the first/last row in the
     * result set positions the cursor before/after the
     * the first/last row. Calling <code>relative(0)</code> is valid, but does
     * not change the cursor position.
     *
     * <p>Note: Calling <code>relative(1)</code>
     * is different from calling <code>next()</code>
     * because is makes sense to call <code>next()</code> when there is no current row,
     * for example, when the cursor is positioned before the first row
     * or after the last row of the result set.
     *
     * @return true if the cursor is on a row; false otherwise
     */
    public boolean relative(int rows ) throws SQLException {
        try {
            return rs.relative(rows);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * <p>Moves the cursor to the previous row in the result set.  
     *
     * <p>Note: <code>previous()</code> is not the same as
     * <code>relative(-1)</code> because it
     * makes sense to call</code>previous()</code> when there is no current row.
     *
     * @return true if the cursor is on a valid row; false if it is off the result set
     */
    public boolean previous() throws SQLException {
        try {
            return rs.previous();
        }
        catch (SQLException se) {
            throw se;
        }
    }

    //---------------------------------------------------------------------
    // Properties
    //---------------------------------------------------------------------

    /**
     * Gives a hint as to the direction in which the rows in this result set
     * will be processed.  The initial value is determined by the statement
     * that produced the result set.  The fetch direction may be changed
     * at any time.
     */
    public void setFetchDirection(int direction) throws SQLException {
        try {
            rs.setFetchDirection(direction);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Returns the fetch direction for this result set.
     *
     * @return the current fetch direction for this result set
     */
    public int getFetchDirection() throws SQLException {
        try {
            return rs.getFetchDirection();
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Gives the JDBC driver a hint as to the number of rows that should 
     * be fetched from the database when more rows are needed for this result
     * set.  If the fetch size specified is zero, the JDBC driver 
     * ignores the value and is free to make its own best guess as to what
     * the fetch size should be.  The default value is set by the statement 
     * that created the result set.  The fetch size may be changed at any 
     * time.
     *
     * @param rows the number of rows to fetch
     */
    public void setFetchSize(int rows) throws SQLException {
        try {
            rs.setFetchSize(rows);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Returns the fetch size for this result set.
     *
     * @return the current fetch size for this result set
     */
    public int getFetchSize() throws SQLException {
        try {
            return rs.getFetchSize();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Returns the type of this result set.  The type is determined by
     * the statement that created the result set.
     *
     * @return TYPE_FORWARD_ONLY, TYPE_SCROLL_INSENSITIVE, or
     * TYPE_SCROLL_SENSITIVE
     */
    public int getType() throws SQLException {
        try {
            return rs.getType();
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Returns the concurrency mode of this result set.  The concurrency
     * used is determined by the statement that created the result set.
     *
     * @return the concurrency type, CONCUR_READ_ONLY or CONCUR_UPDATABLE
     */
    public int getConcurrency() throws SQLException {
        try {
            return rs.getConcurrency();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    //---------------------------------------------------------------------
    // Updates
    //---------------------------------------------------------------------

    /**
     * Indicates whether the current row has been updated.  The value returned 
     * depends on whether or not the result set can detect updates.
     *
     * @return true if the row has been visibly updated by the owner or
     * another, and updates are detected
     */
    public boolean rowUpdated() throws SQLException {
        try {
            return rs.rowUpdated();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Indicates whether the current row has had an insertion.  The value returned 
     * depends on whether or not the result set can detect visible inserts.
     *
     * @return true if a row has had an insertion and insertions are detected
     */
    public boolean rowInserted() throws SQLException {
        try {
            return rs.rowInserted();
        }
        catch (SQLException se) {
            throw se;
        }
    } 
   
    /**
     * Indicates whether a row has been deleted.  A deleted row may leave
     * a visible "hole" in a result set.  This method can be used to
     * detect holes in a result set.  The value returned depends on whether 
     * or not the result set can detect deletions.
     *
     * @return true if a row was deleted and deletions are detected
     */
    public boolean rowDeleted() throws SQLException {
        try {
            return rs.rowDeleted();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Give a nullable column a null value.
     * 
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     */
    public void updateNull(int columnIndex) throws SQLException {
        try {
            rs.updateNull(columnIndex);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a boolean value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     */
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        try {
            rs.updateBoolean(columnIndex, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a byte value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     */
    public void updateByte(int columnIndex, byte x) throws SQLException {
        try {
            rs.updateByte(columnIndex, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a short value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     */
    public void updateShort(int columnIndex, short x) throws SQLException {
        try {
            rs.updateShort(columnIndex, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with an integer value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     */
    public void updateInt(int columnIndex, int x) throws SQLException {
        try {
            rs.updateInt(columnIndex, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a long value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     */
    public void updateLong(int columnIndex, long x) throws SQLException {
        try {
            rs.updateLong(columnIndex, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a float value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     */
    public void updateFloat(int columnIndex, float x) throws SQLException {
        try {
            rs.updateFloat(columnIndex, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a Double value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     */
    public void updateDouble(int columnIndex, double x) throws SQLException {
        try {
            rs.updateDouble(columnIndex, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a BigDecimal value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     */
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        try {
            rs.updateBigDecimal(columnIndex, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a String value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     */
    public void updateString(int columnIndex, String x) throws SQLException {
        try {
            rs.updateString(columnIndex, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a byte array value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     */
    public void updateBytes(int columnIndex, byte x[]) throws SQLException {
        try {
            rs.updateBytes(columnIndex, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a Date value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     */
    public void updateDate(int columnIndex, java.sql.Date x) throws SQLException {
        try {
            rs.updateDate(columnIndex, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a Time value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     */
    public void updateTime(int columnIndex, java.sql.Time x) throws SQLException {
        try {
            rs.updateTime(columnIndex, x);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Updates a column with a Timestamp value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     */
    public void updateTimestamp(int columnIndex, java.sql.Timestamp x) throws SQLException {
        try {
            rs.updateTimestamp(columnIndex, x);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /** 
     * Updates a column with an ascii stream value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param length the length of the stream
     */
    public void updateAsciiStream(int columnIndex, 
               java.io.InputStream x, 
               int length) throws SQLException {

        try {
            rs.updateAsciiStream(columnIndex, x, length);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /** 
     * Updates a column with a binary stream value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value     
     * @param length the length of the stream
     */
    public void updateBinaryStream(int columnIndex, 
                java.io.InputStream x,
                int length) throws SQLException {

        try {
            rs.updateBinaryStream(columnIndex, x, length);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a character stream value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param length the length of the stream
     */
    public void updateCharacterStream(int columnIndex,
                 java.io.Reader x,
                 int length) throws SQLException {

        try {
            rs.updateCharacterStream(columnIndex, x, length);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with an Object value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param scale For java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types
     *  this is the number of digits after the decimal.  For all other
     *  types this value will be ignored.
     */
    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        try {
            rs.updateObject(columnIndex, x, scale);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Updates a column with an Object value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     */
    public void updateObject(int columnIndex, Object x) throws SQLException {
        try {
            rs.updateObject(columnIndex, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a null value.
     *
     * @param columnName the name of the column
     */
    public void updateNull(String columnName) throws SQLException {
        try {
            rs.updateNull(columnName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a boolean value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     */
    public void updateBoolean(String columnName, boolean x) throws SQLException {
        try {
            rs.updateBoolean(columnName, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a byte value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     */
    public void updateByte(String columnName, byte x) throws SQLException {
        try {
            rs.updateByte(columnName, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a short value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     */
    public void updateShort(String columnName, short x) throws SQLException {
        try {
            rs.updateShort(columnName, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with an integer value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     */
    public void updateInt(String columnName, int x) throws SQLException {
        try {
            rs.updateInt(columnName, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a long value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     */
    public void updateLong(String columnName, long x) throws SQLException {
        try {
            rs.updateLong(columnName, x);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Updates a column with a float value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     */
    public void updateFloat(String columnName, float x) throws SQLException {
        try {
            rs.updateFloat(columnName, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a double value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     */
    public void updateDouble(String columnName, double x) throws SQLException {
        try {
            rs.updateDouble(columnName, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a BigDecimal value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     */
    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        try {
            rs.updateBigDecimal(columnName, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a String value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     */
    public void updateString(String columnName, String x) throws SQLException {
        try {
            rs.updateString(columnName, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a byte array value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     */
    public void updateBytes(String columnName, byte x[]) throws SQLException {
        try {
            rs.updateBytes(columnName, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a Date value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     */
    public void updateDate(String columnName, java.sql.Date x) throws SQLException {
        try {
            rs.updateDate(columnName, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a Time value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     */
    public void updateTime(String columnName, java.sql.Time x) throws SQLException {
        try {
            rs.updateTime(columnName, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a Timestamp value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     */
    public void updateTimestamp(String columnName, java.sql.Timestamp x) throws SQLException {
        try {
            rs.updateTimestamp(columnName, x);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /** 
     * Updates a column with an ascii stream value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param length of the stream
     */
    public void updateAsciiStream(String columnName, 
               java.io.InputStream x, 
               int length) throws SQLException {

        try {
            rs.updateAsciiStream(columnName, x, length);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /** 
     * Updates a column with a binary stream value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param length of the stream
     */
    public void updateBinaryStream(String columnName, 
                java.io.InputStream x,
                int length) throws SQLException {

        try {
            rs.updateBinaryStream(columnName, x, length);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with a character stream value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param length of the stream
     */
    public void updateCharacterStream(String columnName,
                 java.io.Reader reader,
                 int length) throws SQLException {

        try {
            rs.updateCharacterStream(columnName, reader, length);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates a column with an Object value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param scale For java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types
     *  this is the number of digits after the decimal.  For all other
     *  types this value will be ignored.
     */
    public void updateObject(String columnName, Object x, int scale) throws SQLException {
        try {
            rs.updateObject(columnName, x, scale);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Updates a column with an Object value.
     *
     * @param columnName the name of the column
     * @param x the new column value
     */
    public void updateObject(String columnName, Object x) throws SQLException {
        try {
            rs.updateObject(columnName, x);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Inserts the contents of the insert row into the result set and
     * the database.  Must be on the insert row when this method is called.
     */
    public void insertRow() throws SQLException {
        try {
            rs.insertRow();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Updates the underlying database with the new contents of the
     * current row.  Cannot be called when on the insert row.
     */
    public void updateRow() throws SQLException {
        try {
            rs.updateRow();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Deletes the current row from the result set and the underlying
     * database.  Cannot be called when on the insert row.
     */
    public void deleteRow() throws SQLException {
        try {
            rs.deleteRow();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Refreshes the current row with its most recent value in 
     * the database.  Cannot be called when on the insert row.
     *
     * The <code>refreshRow</code> method provides a way for an application to 
     * explicitly tell the JDBC driver to refetch a row(s) from the
     * database.  An application may want to call <code>refreshRow</code> when 
     * caching or prefetching is being done by the JDBC driver to
     * fetch the latest value of a row from the database.  The JDBC driver 
     * may actually refresh multiple rows at once if the fetch size is 
     * greater than one.
     * 
     * All values are refetched subject to the transaction isolation 
     * level and cursor sensitivity.  If <code>refreshRow</code> is called after
     * calling <code>updateXXX</code>, but before calling <code>updateRow</code>, then the
     * updates made to the row are lost.  Calling the method <code>refreshRow</code> frequently
     * will likely slow performance.
     */
    public void refreshRow() throws SQLException {
        try {
            rs.refreshRow();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Cancels the updates made to a row.
     * This method may be called after calling an
     * <code>updateXXX</code> method(s) and before calling <code>updateRow</code> to rollback 
     * the updates made to a row.  If no updates have been made or 
     * <code>updateRow</code> has already been called, then this method has no 
     * effect.
     */
    public void cancelRowUpdates() throws SQLException {
        try {
            rs.cancelRowUpdates();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Moves the cursor to the insert row.  The current cursor position is 
     * remembered while the cursor is positioned on the insert row.
     *
     * The insert row is a special row associated with an updatable
     * result set.  It is essentially a buffer where a new row may
     * be constructed by calling the <code>updateXXX</code> methods prior to 
     * inserting the row into the result set.  
     *
     * Only the <code>updateXXX</code>, <code>getXXX</code>,
     * and <code>insertRow</code> methods may be 
     * called when the cursor is on the insert row.  All of the columns in 
     * a result set must be given a value each time this method is
     * called before calling <code>insertRow</code>.  
     * The method <code>updateXXX</code> must be called before a
     * <code>getXXX</code> method can be called on a column value.
     *
     */
    public void moveToInsertRow() throws SQLException {
        try {
            rs.moveToInsertRow();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Moves the cursor to the remembered cursor position, usually the
     * current row.  This method has no effect if the cursor is not on the insert 
     * row. 
     */
    public void moveToCurrentRow() throws SQLException {
        try {
            rs.moveToCurrentRow();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Returns the Statement that produced this <code>ResultSet</code> object.
     * If the result set was generated some other way, such as by a
     * <code>DatabaseMetaData</code> method, this method returns <code>null</code>.
     *
     * @return the Statment that produced the result set or
     * null if the result set was produced some other way
     */
    public Statement getStatement() throws SQLException {
        try {
            return rs.getStatement();
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Returns the value of a column in the current row as a Java object.  
     * This method uses the given <code>Map</code> object
     * for the custom mapping of the
     * SQL structured or distinct type that is being retrieved.
     *
     * @param i the first column is 1, the second is 2, ...
     * @param map the mapping from SQL type names to Java classes
     * @return an object representing the SQL value
     */
    public Object getObject(int i, java.util.Map map) throws SQLException {
        try {
            return rs.getObject(i, map);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets a REF(&lt;structured-type&gt;) column value from the current row.
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Ref</code> object representing an SQL REF value
     */
    public Ref getRef(int i) throws SQLException {
        try {
            return rs.getRef(i);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets a BLOB value in the current row of this <code>ResultSet</code> object.
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Blob</code> object representing the SQL BLOB value in
     *         the specified column
     */
    public Blob getBlob(int i) throws SQLException {
        try {
            return rs.getBlob(i);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets a CLOB value in the current row of this <code>ResultSet</code> object.
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Clob</code> object representing the SQL CLOB value in
     *         the specified column
     */
    public Clob getClob(int i) throws SQLException {
        try {
            return rs.getClob(i);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets an SQL ARRAY value from the current row of this <code>ResultSet</code> object.
     *
     * @param i the first column is 1, the second is 2, ...
     * @return an <code>Array</code> object representing the SQL ARRAY value in
     *         the specified column
     */
    public Array getArray(int i) throws SQLException {
        try {
            return rs.getArray(i);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Returns the value in the specified column as a Java object.  
     * This method uses the specified <code>Map</code> object for
     * custom mapping if appropriate.
     *
     * @param colName the name of the column from which to retrieve the value
     * @param map the mapping from SQL type names to Java classes
     * @return an object representing the SQL value in the specified column
     */
    public Object getObject(String colName, java.util.Map map) throws SQLException {
        try {
            return rs.getObject(colName, map);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets a REF(&lt;structured-type&gt;) column value from the current row.
     *
     * @param colName the column name
     * @return a <code>Ref</code> object representing the SQL REF value in
     *         the specified column
     */
    public Ref getRef(String colName) throws SQLException {
        try {
            return rs.getRef(colName);
        }
        catch (SQLException se) {
            throw se;
        }
     }

    /**
     * Gets a BLOB value in the current row of this <code>ResultSet</code> object.
     *
     * @param colName the name of the column from which to retrieve the value
     * @return a <code>Blob</code> object representing the SQL BLOB value in
     *         the specified column
     */
    public Blob getBlob(String colName) throws SQLException {
        try {
            return rs.getBlob(colName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets a CLOB value in the current row of this <code>ResultSet</code> object.
     *
     * @param colName the name of the column from which to retrieve the value
     * @return a <code>Clob</code> object representing the SQL CLOB value in
     *         the specified column
     */
    public Clob getClob(String colName) throws SQLException {
        try {
            return rs.getClob(colName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets an SQL ARRAY value in the current row of this <code>ResultSet</code> object.
     *
     * @param colName the name of the column from which to retrieve the value
     * @return an <code>Array</code> object representing the SQL ARRAY value in
     *         the specified column
     */
    public Array getArray(String colName) throws SQLException {
        try {
            return rs.getArray(colName);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a java.sql.Date 
     * object. This method uses the given calendar to construct an appropriate millisecond
     * value for the Date if the underlying database does not store
     * timezone information.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the calendar to use in constructing the date
     * @return the column value; if the value is SQL NULL, the result is null
     */
    public java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException {
        try {
            return rs.getDate(columnIndex, cal);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a java.sql.Date 
     * object. This method uses the given calendar to construct an appropriate millisecond
     * value for the Date, if the underlying database does not store
     * timezone information.
     *
     * @param columnName the SQL name of the column from which to retrieve the value
     * @param cal the calendar to use in constructing the date
     * @return the column value; if the value is SQL NULL, the result is null
     */
    public java.sql.Date getDate(String columnName, Calendar cal) throws SQLException {
        try {
            return rs.getDate(columnName, cal);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a java.sql.Time 
     * object. This method uses the given calendar to construct an appropriate millisecond
     * value for the Time if the underlying database does not store
     * timezone information.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the calendar to use in constructing the time
     * @return the column value; if the value is SQL NULL, the result is null
     */
    public java.sql.Time getTime(int columnIndex, Calendar cal) throws SQLException {
        try {
            return rs.getTime(columnIndex, cal);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a java.sql.Time 
     * object. This method uses the given calendar to construct an appropriate millisecond
     * value for the Time if the underlying database does not store
     * timezone information.
     *
     * @param columnName the SQL name of the column
     * @param cal the calendar to use in constructing the time
     * @return the column value; if the value is SQL NULL, the result is null
     */
    public java.sql.Time getTime(String columnName, Calendar cal) throws SQLException {
        try {
            return rs.getTime(columnName, cal);
        }
        catch (SQLException se) {
            throw se;
        }
    } 

    /**
     * Gets the value of a column in the current row as a java.sql.Timestamp 
     * object. This method uses the given calendar to construct an appropriate millisecond
     * value for the Timestamp if the underlying database does not store
     * timezone information.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the calendar to use in constructing the timestamp
     * @return the column value; if the value is SQL NULL, the result is null
     */
    public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        try {
            return rs.getTimestamp(columnIndex, cal);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Gets the value of a column in the current row as a java.sql.Timestamp 
     * object. This method uses the given calendar to construct an appropriate millisecond
     * value for the Timestamp if the underlying database does not store
     * timezone information.
     *
     * @param columnName the SQL name of the column
     * @param cal the calendar to use in constructing the timestamp
     * @return the column value; if the value is SQL NULL, the result is null
     */
    public java.sql.Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        try {
            return rs.getTimestamp(columnName, cal);
        }
        catch (SQLException se) {
            throw se;
        }
    }

    public final static String notImplemented = "This method is not implemented in the CachedResultSet";

    public java.net.URL getURL(int columnIndex) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public java.net.URL getURL(String columnName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateRef(int columnIndex, java.sql.Ref x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateRef(String columnName, java.sql.Ref x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateBlob(int columnIndex, java.sql.Blob x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateBlob(String columnName, java.sql.Blob x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateClob(int columnIndex, java.sql.Clob x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateClob(String columnName, java.sql.Clob x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateArray(int columnIndex, java.sql.Array x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateArray(String columnName, java.sql.Array x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }
}

