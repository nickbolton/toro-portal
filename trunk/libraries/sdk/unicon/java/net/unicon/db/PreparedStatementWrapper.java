/*
 * @(#)PreparedStatementWrapper.java 11-29-2000
 * @author Unicon, Inc
 * @version 1.0.1
 *
 * 1/4/2001 Ver 1.0.1 - Fixed executeQuery function so that it uses the ResultSetWrapper
 *    instead of ResultSet.
 */

package net.unicon.db;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import net.unicon.db.*;
import net.unicon.util.Debug;
import net.unicon.util.*;
import oracle.jdbc.driver.*;

public class PreparedStatementWrapper extends StatementWrapper implements PreparedStatement 
{
    // NOTE: SQL constant types are in the class java.sql.Types.
    public static final int BLANK = 0;
    
    // Function constants.  This is used to determine which function
    // should be used to set this parameter.  This is used since we
    // know the original function that was used.
    public static final int SET_NULL01 = 1;
    public static final int SET_BOOLEAN = 2;
    public static final int SET_BYTE = 3;
    public static final int SET_SHORT = 4;
    public static final int SET_INT = 5;
    public static final int SET_LONG = 6;
    public static final int SET_FLOAT = 7;
    public static final int SET_DOUBLE = 8;
    public static final int SET_BIGDECIMAL = 9;
    public static final int SET_STRING = 10;
    public static final int SET_BYTES = 11;
    public static final int SET_DATE01 = 12;
    public static final int SET_TIME = 13;
    public static final int SET_TIMESTAMP01 = 14;
    public static final int SET_ASCIISTREAM = 15;
    public static final int SET_UNICODESTREAM = 16;
    public static final int SET_BINARYSTREAM = 17;
    public static final int SET_OBJECT01 = 18;
    public static final int SET_OBJECT02 = 19;
    public static final int SET_CHARACTERSTREAM = 20;
    public static final int SET_REF = 21;
    public static final int SET_BLOB = 22;
    public static final int SET_CLOB = 23;
    public static final int SET_ARRAY = 24;
    public static final int SET_DATE02 = 25;
    public static final int SET_TIMESTAMP02 = 26;
    public static final int SET_NULL02 = 27;
    public static final int SET_OBJECT03 = 28;
    public static final int SET_TIME02 = 29;

    protected ArrayList inParameters = new ArrayList();
    protected String sql;

    /**
     * Constructor
     * @param pstmt preparedstatement
     * @param pconn connection
     * @param psql string
     */
    public PreparedStatementWrapper(PreparedStatement pstmt, ConnectionWrapper pconn, String psql)
    {
        super(pstmt, pconn);
        sql = psql;    
    }

    /**
     * Rebuild the prepared statement.  It is necessary to do this because there was
     * a database error and the connection had to be resetted thus loosing the prepared
     * statement's information.
     *
     * @return a prepared statement
     */
    public void rebuildStatement() 
    {
        try {
            if (sql == null) {    
                stmt = conn.recreateStatement();
            }
            else {
                stmt = conn.recreatePrepared(sql);
            }
        }
        catch (SQLException se) { 
            Debug.out.error("db", "Unable to rebuild the statement. :(");
            se.printStackTrace();
        }
    }

    /**
     * Rebuild the in parameters.  This is done only if the statement was
     * recreated.
     *
     */
    public void rebuildInParameters() 
    {
        DbParameter p;
        int pSize = 0;

        Debug.out.println(6, "db", "Starting to rebuild the IN paramters. " + inParameters.size());
        pSize = inParameters.size();
        try {
            // Rebuild the parameters.  Loop through the array list and rebuild all 
            // parameters.
            for (int i = 0; i < pSize; i++) {
                Debug.out.println(7, "db", "Stuffing parameter:" + i);
                p = (DbParameter)inParameters.get(i);
                switch(p.getFunction()) {
                    case SET_NULL01:
                        setNull(p.getIndex(), ((Integer)p.getObject()).intValue());
                        break;
                    case SET_BOOLEAN:
                        setBoolean(p.getIndex(), ((Boolean)p.getObject()).booleanValue());
                        break;
                    case SET_BYTE:
                        setByte(p.getIndex(), ((Byte)p.getObject()).byteValue());
                        break;
                    case SET_SHORT:
                        setShort(p.getIndex(), ((Short)p.getObject()).shortValue());
                        break;
                    case SET_INT:
                        setInt(p.getIndex(), ((Integer)p.getObject()).intValue());
                        break;
                    case SET_LONG:
                        setLong(p.getIndex(), ((Long)p.getObject()).longValue());
                        break;
                    case SET_FLOAT:
                        setFloat(p.getIndex(), ((Float)p.getObject()).floatValue());
                        break;
                    case SET_DOUBLE:
                        setDouble(p.getIndex(), ((Double)p.getObject()).doubleValue());
                        break;
                    case SET_BIGDECIMAL:
                        setBigDecimal(p.getIndex(), (BigDecimal)p.getObject());
                        break;
                    case SET_STRING:
                        setString(p.getIndex(), (String)p.getObject());
                        break;
                    case SET_BYTES:
                        setBytes(p.getIndex(), (byte[])p.getObject());
                        break;
                    case SET_DATE01:
                        setDate(p.getIndex(), (java.sql.Date)p.getObject());
                        break;
                    case SET_TIME:
                        setTime(p.getIndex(), (java.sql.Time)p.getObject());
                        break;
                    case SET_TIMESTAMP01:
                        setTimestamp(p.getIndex(), (java.sql.Timestamp)p.getObject());
                        break;
                    case SET_ASCIISTREAM:
                        setAsciiStream(p.getIndex(), (java.io.InputStream)p.getObject(), 
                            p.getLength());
                        break;
                    case SET_UNICODESTREAM:
                        setUnicodeStream(p.getIndex(), (java.io.InputStream)p.getObject(),
                            p.getLength());
                        break;
                    case SET_BINARYSTREAM:
                        setBinaryStream(p.getIndex(), (java.io.InputStream)p.getObject(),
                            p.getLength());
                        break;
                    case SET_OBJECT01:
                        setObject(p.getIndex(), p.getObject(), p.getTargetSqlType(),
                            p.getLength());
                        break;
                    case SET_OBJECT02:
                        setObject(p.getIndex(), p.getObject(), p.getTargetSqlType());
                        break;
                    case SET_OBJECT03:
                        setObject(p.getIndex(), p.getObject());
                        break;
                    case SET_CHARACTERSTREAM:
                        setCharacterStream(p.getIndex(), (java.io.Reader)p.getObject(),
                            p.getLength());
                        break;
                    case SET_REF:
                        setRef(p.getIndex(), (Ref)p.getObject());
                        break;
                    case SET_BLOB:
                        setBlob(p.getIndex(), (Blob)p.getObject());
                        break;
                    case SET_CLOB:
                        setClob(p.getIndex(), (Clob)p.getObject());
                        break;
                    case SET_ARRAY:
                        setArray(p.getIndex(), (Array)p.getObject());
                        break;
                    case SET_DATE02:
                        setDate(p.getIndex(), (java.sql.Date)p.getObject(),
                            p.getCalendar());
                        break;
                    case SET_TIMESTAMP02:
                        setTime(p.getIndex(), (java.sql.Time)p.getObject(),
                            p.getCalendar());
                        break;
                    case SET_NULL02:
                        setNull(p.getIndex(), ((Integer)p.getObject()).intValue(),
                            p.getTypeName());
                        break;
                    case SET_TIME02:
                        setTime(p.getIndex(), (java.sql.Time)p.getObject(),
                            p.getCalendar());
                        break;
                }
            }
        }
        catch (Exception e) {
            Debug.out.error("db", "Problems encountered when recreating in params.");
            e.printStackTrace();
        }
    }

    /**
      * Build the SQL string for Oracle.  This function will allow the developer to check
     * if it is their Java code that is failing or the stored procedure.  A SQL string 
     * will be printed out where the developer can copy and paste into SQLPlus.  From 
     * there, the developer can run it and see the results.
     *
     * NOTE: Objects are not supported.
     */
    private String getOracleSqlString() 
    {
        int i = 1;
        int startIndex = 0;
        int endIndex = 0;
        int preEndIndex = 0;
        DbParameter p = null;
        StringBuffer procSql = new StringBuffer();

        procSql = procSql.append("Oracle SQL syntax:\n");

        // Loop through the parameters and find the one with the current index.  
        // Have to do this in order to guarantee the order of the parameters after
        // subsituting the parameters.  
        endIndex = sql.indexOf("?", startIndex);
        while (endIndex > 0 && validParameter(endIndex)) {
            p = atInParameter(i);
            if (p == null) {
                // Uh Oh
                Debug.out.error("db", "ERROR: No parameter with index " + i + " found!");
            }
            else {
                procSql.append(sql.substring(startIndex, endIndex ));
                procSql.append(getInParameterValue(p));
                
                i++;
            }
            // Look for the next ?
            preEndIndex = endIndex;
            startIndex = endIndex + 1;
            endIndex = sql.indexOf("?", startIndex);
        }
        // Get the rest of the procSql
        procSql.append(sql.substring(preEndIndex + 1, sql.length()));

        return (procSql.toString());
    }

    /**
     * Check to see if the question "?" is between two "'", if so return false.  This
     * function will help the function getOracelSqlString to determine if it needs
     * to subsitute the "?" with the IN/OUT parameter.  If the number of "'" is odd then
     * we assume that the ? is inside a string value.
     *
     * @param endat - index to stop at
     * @return false if the number of ' is odd before the endat index
     */
    public boolean validParameter(int endat) 
    {
        int start = 0;
        int count = 0;

        String str = sql.substring(0, endat);

        start = str.indexOf("'", start);
        while (start > 0) {
            count++;
            start = str.indexOf("'", start);
        }
        if (count % 2 == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Find the in paramter with the parameter index that is passed in.
     * 
     * @param index - parameter
     * @return parameter object
     */
    public DbParameter atInParameter(int index)
    {
        DbParameter p;

        for (int i = 0; i < inParameters.size(); i++) {
            p = (DbParameter)inParameters.get(i);
            if (p.getIndex() == index) {
                return p;
            }
        }
        return null;
    }

    /** 
     * Get the parameter value so that we can build the Oracle SQL string.
     * This string will allow the developer to check if the code is the
     * problem or the stored procedure.
     * 
     * NOTE: Objects are not supported.
     *
     * @param p - DbParameter object
     * @return value - string value of data
     */
    public String getInParameterValue(DbParameter p) 
    {
        int fn = p.getFunction();

        if (p.getObject() == null) {
            return "<NULL VALUE>";
        }

        if (fn == SET_INT ||
            fn == SET_LONG ||
            fn == SET_FLOAT ||
            fn == SET_DOUBLE ||
            fn == SET_BIGDECIMAL) {

            return p.getObject().toString();
        }
        else if (fn == SET_NULL01) {
            return "null";
        }
        else if (fn == SET_BOOLEAN) {
            return p.getObject().toString();
        }
        else if (fn == SET_BYTE) {
            return p.getObject().toString();
        }
        else if (fn == SET_STRING) {
            return ("'" + p.getObject().toString() + "'");
        }
        else if (fn == SET_BYTES) {
            return p.getObject().toString();
        }
        else if (fn == SET_DATE01) {
            return ("TO_DATE('" + 
                ((java.sql.Date)p.getObject()).toString() + "', 'yyyy-mm-dd')");
        }
        else if (fn == SET_DATE02) {
            // Not supporting locale and timezone.
            return ("TO_DATE('" + 
                ((java.sql.Date)p.getObject()).toString() + "', 'yyyy-mm-dd')");
        }
        else if (fn == SET_TIME) {
            return ("'" + ((java.sql.Time)p.getObject()).toString() + "'");
        }
        else if (fn == SET_TIME02) {
            // Not supporting locale and timezone.
            return ("'" + ((java.sql.Time)p.getObject()).toString() + "'");
        }
        else if (fn == SET_TIMESTAMP01) {
            return ("TO_DATE('" + 
                ((java.sql.Timestamp)p.getObject()).toString() + "', 'yyyy-mm-dd hh:mm:ss')");
        }
        else if (fn == SET_TIMESTAMP02) {
            // Needs locale and timezone 
            return ("TO_DATE('" + 
                ((java.sql.Timestamp)p.getObject()).toString() + "', 'yyyy-mm-dd hh:mm:ss')");
        }
        else if (fn == SET_ASCIISTREAM) {
            return "<ASCII stream>"; 
        }
        else if (fn == SET_UNICODESTREAM) {
            return "<Unicode stream>";
        }
        else if (fn == SET_BINARYSTREAM) {
            return "<Binary stream>";
        }
        else if (fn == SET_OBJECT01) {
            return "<Object>";
        }
        else if (fn == SET_OBJECT02) {
            return "<Object>";
        }
        else if (fn == SET_OBJECT03) {
            return "<Object>";
        }
        else if (fn == SET_CHARACTERSTREAM) {
            return "<Character Stream>";
        }
        else if (fn == SET_REF) {
            return "<Ref>";
        }
        else if (fn == SET_BLOB) {
            return "<BLOB>";
        }
        else if (fn == SET_CLOB) {
            return "<CLOB>";
        }
        else if (fn == SET_ARRAY) {
            return "<ARRAY>";
        }
        else if (fn == SET_NULL02) {
            return "null";
        }
        else { 
            return "<ERROR>";
        }
    }

    /**
     * Execute the prepared statement.
     * @return resultset of the query
     */
    public ResultSet executeQuery() throws SQLException
    {
        ResultSet rs = new ResultSetWrapper(((PreparedStatement)stmt).executeQuery(), this);

        inParameters.clear();
        resultSets.add(rs);
        return rs;
    }

    /**
     * Execute the prepared statement, inserts, updates or deletes
     * @return the number of rows affected
     */
    public int executeUpdate() throws SQLException
    {
        inParameters.clear();
        return ((PreparedStatement)stmt).executeUpdate();
    }

    /**
     * Sets the parameter to SQL NULL
     * @param parameterIndex
     * @param sqlType
     */
    public void setNull(int parameterIndex, int sqlType) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, new Integer(sqlType), BLANK, BLANK, 
            SET_NULL01, null, null));
        ((PreparedStatement)stmt).setNull(parameterIndex, sqlType);
    }

    /**
     * Sets the parameter to a Java boolean value.
     * @param parameterIndex
     * @param x boolean value
     */
    public void setBoolean(int parameterIndex, boolean x) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, new Boolean(x), BLANK, BLANK, SET_BOOLEAN, null, null));
        ((PreparedStatement)stmt).setBoolean(parameterIndex, x);
    }

    /**
     * Sets the parameter to a Java byte value.
     * @param parameterIndex
     * @param x byte value
     */
    public void setByte(int parameterIndex, byte x) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, new Byte(x), BLANK, BLANK, SET_BYTE, null, null));
        ((PreparedStatement)stmt).setByte(parameterIndex, x);
    }

    /**
     * Sets the parameter to a Java short value.
     * @param parameterIndex 
     * @param x short value
     */
    public void setShort(int parameterIndex, short x) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, new Short(x), BLANK, BLANK, SET_SHORT, null, null));
        ((PreparedStatement)stmt).setShort(parameterIndex, x);
    }

    /**
     * Sets the parameter to a Java int value.
     * @param parameterIndex
     * @param x int value
     */
    public void setInt(int parameterIndex, int x) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, new Integer(x), BLANK, BLANK, SET_INT, null, null));
        ((PreparedStatement)stmt).setInt(parameterIndex, x);
    }

    /**
     * Sets the parameter to a Java long value.
     * @param parameterIndex
     * @param x long value
     */
    public void setLong(int parameterIndex, long x) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, new Long(x), BLANK, BLANK, SET_LONG, null, null));
        ((PreparedStatement)stmt).setLong(parameterIndex, x);
    }

    /**
     * Sets the parameter to a Java float value.
     * @param parameterIndex
     * @param x float value
     */
    public void setFloat(int parameterIndex, float x) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, new Float(x), BLANK, BLANK, SET_FLOAT, null, null));
        ((PreparedStatement)stmt).setFloat(parameterIndex, x);
    }

    /**
     * Sets the parameter to a Java double value.
     * @param parameterIndex
     * @param x double value
     */
    public void setDouble(int parameterIndex, double x) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, new Double(x), BLANK, BLANK, SET_DOUBLE, null, null));
        ((PreparedStatement)stmt).setDouble(parameterIndex, x);
    }

    /**
     * Sets the parameter to Java big decimal value.
     * @param parameterIndex
     * @param x BigDecimal value
     */
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, x, BLANK, BLANK, SET_BIGDECIMAL, null, null));
        ((PreparedStatement)stmt).setBigDecimal(parameterIndex, x);
    }

    /**
     * Sets the parameter to a Java string.
     * @param parameterIndex
     * @param x string value
     */
    public void setString(int parameterIndex, String x) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, x, BLANK, BLANK, SET_STRING, null, null));
        ((PreparedStatement)stmt).setString(parameterIndex, x);
    }

    /**
     * Sets the parameter value to Java bytes.
     * @param parameterIndex
     * @param x array of bytes
     */
    public void setBytes(int parameterIndex, byte x[]) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, x, BLANK, BLANK, SET_BYTES, null, null));
        ((PreparedStatement)stmt).setBytes(parameterIndex, x);
    }

    /**
     * Sets the parameter to a Java date value.
     * @param parameterIndex
     * @param x date value
     */
    public void setDate(int parameterIndex, java.sql.Date x) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, x, BLANK, BLANK, SET_DATE01, null, null));
        ((PreparedStatement)stmt).setDate(parameterIndex, x);
    }

    /**
     * Sets the parameter to a Java time value.
     * @param parameterIndex
     * @param x time value
     */
    public void setTime(int parameterIndex, java.sql.Time x) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, x, BLANK, BLANK, SET_TIME, null, null));
        ((PreparedStatement)stmt).setTime(parameterIndex, x);
    }

    /**
     * Sets the parameter to a Java timestamp value.
     * @param parameterIndex
     * @param x timestamp value
     */
    public void setTimestamp(int parameterIndex, java.sql.Timestamp x) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, x, BLANK, BLANK, SET_TIMESTAMP01, null, null));
        ((PreparedStatement)stmt).setTimestamp(parameterIndex, x);
    }

    /**
     * Sets the parameter to a Java asciistream value.
     * @param parameterIndex
     * @param x the input stream
     * @param length of the stream
     */
    public void setAsciiStream(int parameterIndex, java.io.InputStream x, int length) 
                        throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, x, length, BLANK, SET_ASCIISTREAM, null, null));
        ((PreparedStatement)stmt).setAsciiStream(parameterIndex, x, length);
    }

    /**
     * Sets the parameter to a Java unicodestream.
     * @param parameterIndex
     * @param x input stream
     * @param length of the stream
     */
    public void setUnicodeStream(int parameterIndex, java.io.InputStream x, 
                            int length) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, x, length, BLANK, SET_UNICODESTREAM, null, null));
        ((PreparedStatement)stmt).setUnicodeStream(parameterIndex, x, length);
    }

    /**
     * Sets the parameter to a binary stream.
     * @param parameterIndex
     * @param x the input stream
     * @param length of the stream
     */
    public void setBinaryStream(int parameterIndex, java.io.InputStream x, 
                          int length) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, x, length, BLANK, SET_BINARYSTREAM, null, null));
        ((PreparedStatement)stmt).setBinaryStream(parameterIndex, x, length);
    }

    /**
     * Clears the parameter values.
     */
    public void clearParameters() throws SQLException
    {
        inParameters.clear();
        ((PreparedStatement)stmt).clearParameters();
    }

    /**
     * Sets the parameter using an object.
     * @param parameterIndex
     * @param x object to be set
     * @param targetSqlType is the SQL type
     * @param scale 
     */
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale)
                   throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, x, scale, targetSqlType, SET_OBJECT01, null, null));
        ((PreparedStatement)stmt).setObject(parameterIndex, x, targetSqlType, scale);
    }

    /**
     * Sets the parameter using an object.
     * @param parameterIndex
     * @param x object to be set
     * @param targetSqlType is the SQL type
     */
    public void setObject(int parameterIndex, Object x, int targetSqlType) 
                     throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, x, BLANK, targetSqlType, SET_OBJECT02, null, null));
        ((PreparedStatement)stmt).setObject(parameterIndex, x, targetSqlType);
    }

    /**
     * Sets the parameter using an object.
     * @param parameterIndex
     * @param x is the oject to be set
     */
    public void setObject(int parameterIndex, Object x) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, x, BLANK, BLANK, SET_OBJECT03, null, null));
        ((PreparedStatement)stmt).setObject(parameterIndex, x);
    }

    /**
     * Execute the statement.
     * @return boolean as a result of the query execution
     */
    public boolean execute() throws SQLException
    {
        int errorCode = 0;

        try
        {
            Debug.out.println(6, "db", getOracleSqlString());
            return ((PreparedStatement)stmt).execute();
        }
        catch(SQLException se)
        {
            errorCode = se.getErrorCode();
            Debug.out.error("db", "SQL Error code: " + errorCode);
            // Error code 4068 occurs when there is an error when attempting to execute a
            // stored procedure.  In our case, this happens when a stored procedure has been
            // recompiled due to changes.  Since we do not want to restart the web server
            // when this happens, we will try to establish a new connection and rebuild the
            // statement.  The statement is then re-excuted.
            if (errorCode == 4068) {
                conn.resetConnection();
                // Re-create the statement
                this.rebuildStatement();
                this.rebuildInParameters();
                Debug.out.println(6, "db", "Re-executing call.");
                return ((PreparedStatement)stmt).execute();
            }
            return false;
        }
        finally {
            inParameters.clear();
        }
    }

    /**
     * Adds a set of parameters to the batch.
     */
    public void addBatch() throws SQLException
    {
        ((PreparedStatement)stmt).addBatch();
    }

    /**
     * Sets the parameter to the reader.
     * @param parameterIndex
     * @param reader 
     * @param length of the reader
     */
    public void setCharacterStream(int parameterIndex,
                                 java.io.Reader reader,
                              int length) throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, reader, length, BLANK, SET_CHARACTERSTREAM, null, null));
        ((PreparedStatement)stmt).setCharacterStream(parameterIndex, reader, length);
    }

    /**
     * Set a reference to a parameter.
     * @param i, parameter index
     * @param x reference
     */
    public void setRef (int i, Ref x) throws SQLException
    {
        inParameters.add(
            new DbParameter(i, x, BLANK, BLANK, SET_REF, null, null));
        ((PreparedStatement)stmt).setRef(i, x);
    }

    /**
     * Sets a BLOB to an index.
     * @param i, parameter index
     * @param x, blob object
     */
    public void setBlob (int i, Blob x) throws SQLException
    {
        inParameters.add(
            new DbParameter(i, x, BLANK, BLANK, SET_BLOB, null, null));
        ((PreparedStatement)stmt).setBlob(i, x);
    }

    /**
     * Sets a CLOB to an index.
     * @param i, parameter index
     * @param x, clob object
     */
    public void setClob (int i, Clob x) throws SQLException
    {
        inParameters.add(
            new DbParameter(i, x, BLANK, BLANK, SET_CLOB, null, null));
        ((PreparedStatement)stmt).setClob(i, x);
    }

    /**
     * Sets an array parameter.
     * @param i, parameter index
     * @param x, SQL array
     */
    public void setArray (int i, Array x) throws SQLException
    {
        inParameters.add(
            new DbParameter(i, x, BLANK, BLANK, SET_ARRAY, null, null));
        ((PreparedStatement)stmt).setArray(i, x);
    }

    /**
     * Gets the column information of the resultset.
     * @return metadata of the resultset
     */
    public ResultSetMetaData getMetaData() throws SQLException
    {
        return ((PreparedStatement)stmt).getMetaData();
    }

    /**
     * Sets the parameter to a Java date value.
     * @param parameterIndex
     * @param x sql date
     * @param cal, Java calendar
     */
    public void setDate(int parameterIndex, java.sql.Date x, Calendar cal)
                     throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, x, BLANK, BLANK, SET_DATE02, cal, null));
        ((PreparedStatement)stmt).setDate(parameterIndex, x, cal);
    }

    /**
     * Sets the parameter to Java time value.
     * @param parameterIndex
     * @param x, sql time
     * @param cal, Java calendar
     */
    public void setTime(int parameterIndex, java.sql.Time x, Calendar cal) 
                 throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, x, BLANK, BLANK, SET_TIME02, cal, null));
        ((PreparedStatement)stmt).setTime(parameterIndex, x, cal);
    }

    /**
     * Sets the parameter to a timestamp value.
     * @param parameterIndex
     * @param x, timestamp
     * @param cal, Java calendar
     */
    public void setTimestamp(int parameterIndex, java.sql.Timestamp x, Calendar cal)
                      throws SQLException
    {
        inParameters.add(
            new DbParameter(parameterIndex, x, BLANK, BLANK, SET_TIMESTAMP02, cal, null));
        ((PreparedStatement)stmt).setTimestamp(parameterIndex, x, cal);
    }

    /**
     * Sets the parameter to SQL NULL.
     * @param paramIndex
     * @param sqlType, sql type
     * @param typeName
     */
    public void setNull (int paramIndex, int sqlType, String typeName) 
                  throws SQLException
    {
        inParameters.add(
            new DbParameter(paramIndex, new Integer(sqlType), BLANK, BLANK, 
            SET_NULL02, null, typeName));
        ((PreparedStatement)stmt).setNull(paramIndex, sqlType, typeName);
    }

    public final static String notImplemented = "This method is not implemented in the CachedResultSet";

    public void setURL(int parameterIndex, java.net.URL x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public ParameterMetaData getParameterMetaData() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }
}

