/*
 * @(#)CallableStatementWrapper.java 11-29-2000    
 * @author Unicon, Inc
 * @version 1.0
 *
 *  
 */

package net.unicon.db;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import net.unicon.db.*;
import net.unicon.util.Debug;
import net.unicon.util.*;
import java.text.*;
import oracle.jdbc.driver.*;

public class CallableStatementWrapper extends PreparedStatementWrapper implements CallableStatement 
{
    // Constants
    public static final int REG_OUT01 = 1;
    public static final int REG_OUT02 = 2;
    public static final int REG_OUT03 = 3;
    
    protected ArrayList outParameters = new ArrayList();

    /**
     * Constructor
     * @param pstmt callablestatement
     * @param pconn connection
     * @param psql sql string
     */
    public CallableStatementWrapper(CallableStatement pstmt, ConnectionWrapper pconn, String psql)
    {
        super(pstmt, pconn, psql);
    }

    /**
     * Rebuild the callable statement.  It is done only if an error has occured (oracle error
     * 4068).  Since the statement gets trashed with an exception we have to recreate it 
     * and resubmit it.
     *
     * @return callable statement
     */
    public void recreateCallable()
    {
        Debug.out.println(6, "db", "Recreating statement from connection:" + conn);
        DbParameter p;
        int pSize = 0;

        try {
            if (sql == null) {
                stmt = conn.recreateStatement();
            }
            else {
                stmt = conn.recreateCallable(sql);
            }
            // Rebuild the IN parameters.  The in parameters are only in the prepared
            // statement.
            rebuildInParameters();

            Debug.out.println(6, "db", "Rebuilding OUT parameters. " + outParameters.size());
            // Rebuild the OUT parameters.
            // NOTE: This process will add more parameters into the outParameters list.  
            //       The pSize variable will stop the process from getting into an infinite
            //       loop.  Since we are clearing out the list afterwards anyway, we leave
            //       the functions as is even though it will add the extra parameters.
            pSize = outParameters.size();
            for (int i = 0; i < pSize; i++) {
                p = (DbParameter)outParameters.get(i);
                Debug.out.println(6, "db", "OUT - rebuilding ref:" + p);
                switch(p.getFunction()) {
                    case REG_OUT01:
                        this.registerOutParameter(p.getIndex(), ((Integer)p.getObject()).intValue());
                        break;
                    case REG_OUT02:
                        this.registerOutParameter(p.getIndex(), ((Integer)p.getObject()).intValue(),
                            p.getLength());
                        break;
                    case REG_OUT03:
                        this.registerOutParameter(p.getIndex(), ((Integer)p.getObject()).intValue(),
                            p.getTypeName());
                        break;
                    default:
                        break;
                }
            }
        }
        catch (SQLException se) {
            Debug.out.println(6, "db", "Unable to rebuild the callable statement.");
        }
    }

    /**
     * Build the SQL string for Oracle.  This function will allow the developer to check
     * if it is their Java code that is failing or the stored procedure.  A SQL string 
     * will be printed out where the developer can copy and paste into SQLPlus.  From 
     * there, the developer can run it and see the results of the stored procedure.  
     * 
     * NOTE: Objects are not supported.
     * 
     * @return oracle sql string
     */
    private String getOracleSqlString() 
    {
        int i = 1;
        int startIndex = 0;
        int endIndex = 0;
        int preEndIndex = 0;
        DbParameter p = null;
        StringBuffer varSql = new StringBuffer();
        StringBuffer procSql = new StringBuffer();

        varSql = varSql.append("Oracle SQL syntax:\n");

        // Loop through the parameters and find the one with the current index.  
        // Have to do this in order to guarantee the order of the parameters after
        // subsituting the parameters.  
        endIndex = sql.indexOf("?", startIndex);
        while (endIndex > 0 && validParameter(endIndex)) {
            p = atInParameter(i);
            if (p == null) {
                p = atOutParameter(i);
                if (p == null) {
                    // Uh Oh
                    Debug.out.error("db", "ERROR: No parameter with index " + i + " found!");
                }
                else {
                    // Out parameter, we have to create this parameter variable and subsitute
                    // it in the sql string.
                    varSql.append("VAR out" + i + " " + getOracleVarType(p) + " \n");
                    procSql.append(sql.substring(startIndex, endIndex) + ":out" + i);
                    i++;
                }
            }
            else {
                // In parameter
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

        return (varSql.toString() + procSql.toString());
    }

    /**
     * Find the out paramter with the parameter index that is passed in.
     * 
     * @param index - parameter
     * @return parameter object
     */
    private DbParameter atOutParameter(int index)
    {
        DbParameter p;

        for (int i = 0; i < outParameters.size(); i++) {
            p = (DbParameter)outParameters.get(i);
            if (p.getIndex() == index) {
                return p;
            }
        }
        return null;
    }

    /**
     * Gets the SQL type for the out parameter so that we can declare the variable in
     * Oracle SQL.
     *
     * @param param - DbParameter 
     * @return type - Oracle variable type
     */
    private String getOracleVarType(DbParameter param) 
    {
        int sqlType = 0;

        if (param.getTypeName() != null) {
            return param.getTypeName();
        }
        else {
            sqlType = ((Integer)param.getObject()).intValue();
            return (convertJDBCtoOracleType(sqlType));
        }
    }

    /**
     * Converts the JDBC sql type to an Oracle data type.
     * 
     * @param type - JDBC sql type
     * @return oracleType - Oracle data type
     */
    private String convertJDBCtoOracleType(int jdbcType) 
    {
        if (jdbcType == java.sql.Types.NUMERIC ||
            jdbcType == java.sql.Types.DECIMAL ||
            jdbcType == java.sql.Types.TINYINT ||
            jdbcType == java.sql.Types.SMALLINT ||
            jdbcType == java.sql.Types.INTEGER ||
            jdbcType == java.sql.Types.BIGINT ||
            jdbcType == java.sql.Types.REAL ||
            jdbcType == java.sql.Types.FLOAT ||
            jdbcType == java.sql.Types.DOUBLE) {

            return "NUMBER";
        }
        else if (jdbcType == java.sql.Types.CHAR) {
            return "CHAR";
        }
        else if (jdbcType == java.sql.Types.VARCHAR) { 
            return "VARCHAR2(255)";
        }
        else if (jdbcType == java.sql.Types.DATE ||
            jdbcType == java.sql.Types.TIME ||
            jdbcType == java.sql.Types.TIMESTAMP) {

            return "DATE";
        }
        else if (jdbcType == java.sql.Types.REF ||
            jdbcType == OracleTypes.CURSOR) {

            return "REFCURSOR";
        }
        else if (jdbcType == java.sql.Types.LONGVARCHAR) {
            return "LONG";
        }
        else if (jdbcType == java.sql.Types.VARBINARY) {
            return "RAW";
        }
        else if (jdbcType == java.sql.Types.LONGVARBINARY) {
            return "LONG RAW";
        }
        else if (jdbcType == java.sql.Types.BLOB) {
            return "BLOB";
        }
        else if (jdbcType == java.sql.Types.CLOB) {
            return "CLOB";
        }
        else if (jdbcType == java.sql.Types.OTHER) {
            return "<OTHER = ?>";
        }
        else {
            return "<UNABLE TO RESOLVE JDBC TYPE >" + jdbcType + "< TO ORACLE DATA TYPE>";
        }
    }

    /**
     * Registers the output parameters.
     * @param parameterIndex
     * @param sqlType, JDBC type code
     */
    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException
    {
        outParameters.add(
            new DbParameter(parameterIndex, new Integer(sqlType), BLANK, BLANK,
            REG_OUT01, null, null));
        ((CallableStatement)stmt).registerOutParameter(parameterIndex, sqlType);    
    }

    /**
     * Register the output parameter.
     * @param parameterIndex
     * @param sqlType, JDBC type code
     * @param scale is the number of digits of the decimal
     */
    public void registerOutParameter(int parameterIndex, int sqlType, int scale) 
                                     throws SQLException
    {
        outParameters.add(
            new DbParameter(parameterIndex, new Integer(sqlType), scale, BLANK,
            REG_OUT02, null, null));
        ((CallableStatement)stmt).registerOutParameter(parameterIndex, sqlType, scale);
    }

    /**
     * Checks to see if the last out parameter had the value of SQL NULL.
     * @return true if the last parameter read was SQL NULL
     */
    public boolean wasNull() throws SQLException
    {
        return ((CallableStatement)stmt).wasNull();
    }

    /**
     * Gets the string value of the parameter.
     * @param parameterIndex
     * @return the string value of the parameter
     */
    public String getString(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)stmt).getString(parameterIndex);
    }

    /**
     * Gets the value of a JDBC bit parameter as a boolean.
     * @param parameterIndex
     * @return false if the value is SQL NULL
     */
    public boolean getBoolean(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)stmt).getBoolean(parameterIndex);
    }

    /**
     * Gets the value of a JDBC tinyint as a byte.
     * @param parameterIndex 
     * @return parameter value as a byte, 0 if SQL NULL
     */
    public byte getByte(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)stmt).getByte(parameterIndex);
    }

    /**
     * Gets the value of a JDBC smallint parameter as a short.
     * @param parameterIndex
     * @return parameter value as a short, 0 if SQL NULL
     */
    public short getShort(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)stmt).getShort(parameterIndex);
    }

    /**
     * Gets the value of a JDBC integer as an int.
     * @param parameterIndex
     * @return the parameter value as an int, 0 if SQL NULL
     */
    public int getInt(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)stmt).getInt(parameterIndex);
    }

    /**
     * Get the value of a JDBC bigint as a long.
     * @param parameterIndex
     * @return parameter value as a long, 0 if SQL NULL
     */
    public long getLong(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)stmt).getLong(parameterIndex);
    }

    /**
     * Gets the value of a JDBC float as a float.
     * @param parameterIndex
     * @return parameter value as a float, 0 if SQL NULL
     */
    public float getFloat(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)stmt).getFloat(parameterIndex);
    }

    /**
     * Gets the value of a JDBC double as a double.
     * @param parameterIndex
     * @return parameter value as a double, 0 if SQL NULL
     */
    public double getDouble(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)stmt).getDouble(parameterIndex);
    }

    /**
     * Gets the value of a JDBC numeric as a BigDecimal.
     * @param parameterIndex
     * @param scale - number of digits 
     * @return parameter value, null if SQL NULL
     */
    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException
    {
        return ((CallableStatement)stmt).getBigDecimal(parameterIndex, scale);
    }

    /**
     * Gets the value of JDBC binary or varbinary to an array of bytes.
     * @param parameterIndex
     * @return the parameter value, null if SQL NULL
     */
    public byte[] getBytes(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)stmt).getBytes(parameterIndex);
    }

    /**
     * Gets the value of a JDBC date as a date.
     * @param paremeterIndex
     * @return parameter value, null if SQL NULL
     */
    public java.sql.Date getDate(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)stmt).getDate(parameterIndex);
    }

    /**
     * Gets the value of a JDBC time parameter as time.
     * @param parameterIndex
     * @return parameter value, null if SQL NULL
     */
    public java.sql.Time getTime(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)stmt).getTime(parameterIndex);
    }

    /**
     * Gets the value of a JDBC timestamp as a timestamp.
     * @param parameterIndex
     * @return parameter value, null if SQL NULL
     */
    public java.sql.Timestamp getTimestamp(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)stmt).getTimestamp(parameterIndex);
    }

    /**
     * Gets the value of a parameter as an object.
     * @param parameterIndex
     * @return parameter value, null if SQL NULL
     */
    public Object getObject(int parameterIndex) throws SQLException
    {
        Object o = ((CallableStatement)stmt).getObject(parameterIndex);

        if(o instanceof ResultSet)
        {
            ResultSet rs = new ResultSetWrapper((ResultSet)o, this);
            
            Debug.out.println(6, "db", "Opening resultset: " + resultSets.size());
            resultSets.add(rs);
            return rs;
        }
            
        return o;
    }

    /**
     * Gets the JDBC numeric as a BigDecimal.
     * @param parameterIndex
     * @return parameter value, null if SQL NULL
     */
    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)stmt).getBigDecimal(parameterIndex);
    }

    /**
     * Gets an object representing the value of OUT parameter.
     * @param i - parameter index
     * @param map - mapping form SQL types to Java
     * @return out parameter object
     */
    public Object getObject (int i, java.util.Map map) throws SQLException
    {
        Object o = ((CallableStatement)stmt).getObject(i, map);

        if(o instanceof ResultSet)
        {
            ResultSet rs = new ResultSetWrapper((ResultSet)o, this);
            
            Debug.out.println(6, "db", "Opening resultset: " + resultSets.size());
            resultSets.add(rs);
            return rs;
        }
            
        return o;
    }

    /**
     * Gets the value of a JDBC ref.
     * @param i - parameter index
     * @return the parameter value as a ref, null if SQL NULL
     */
    public Ref getRef (int i) throws SQLException
    {
        return ((CallableStatement)stmt).getRef(i);
    }

    /**
     * Gets the JDBC blob as a blob in Java.
     * @param i - parameter index
     * @return parameter value as a bolb
     */
    public Blob getBlob (int i) throws SQLException
    {
        return ((CallableStatement)stmt).getBlob(i);
    }

    /**
     * Gets the JDBC clob as a clob in Java.
     * @param i - parameter index
     * @return parameter value as a clob
     */
    public Clob getClob (int i) throws SQLException
    {
        return ((CallableStatement)stmt).getClob(i);
    }

    /**
     * Get the value of a JDBC array as an array object in Java.
     * @param i - parameter index
     * @return parameter value as an array, null if SQL NULL
     */
    public Array getArray (int i) throws SQLException
    {
        return ((CallableStatement)stmt).getArray(i);
    }

    /**
     * Get the value of a JDBC date as a Java date.
     * @param parameterIndex
     * @param cal - calendar object to use to construct the date
     * @return parameter value, null if SQL NULL
     */
    public java.sql.Date getDate(int parameterIndex, Calendar cal) throws SQLException
    {
        return ((CallableStatement)stmt).getDate(parameterIndex, cal);
    }

    /**
     * Get the value of a JDBC time as a Java time.
     * @param parameterIndex
     * @param cal - calendar object use to construct the date
     * @return parameter value, null if SQL NULL
     */
    public java.sql.Time getTime(int parameterIndex, Calendar cal) throws SQLException
    {
        return ((CallableStatement)stmt).getTime(parameterIndex, cal);
    }

    /**
     * Gets the value of a JDBC timestamp as a Java timestamp.
     * @param parameterIndex
     * @param cal - calendar object use to construct the date
     * @return parameter value, null if SQL NULL
     */
    public java.sql.Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException
    {
        return ((CallableStatement)stmt).getTimestamp(parameterIndex, cal);
    }

    /**
     * Registers the output parameter.
     * @param paramIndex
     * @param sqlType
     * @param typeName - SQL structured type
     */
    public void registerOutParameter (int paramIndex, int sqlType, String typeName) 
                                      throws SQLException
    {
        outParameters.add(
            new DbParameter(paramIndex, new Integer(sqlType), BLANK, BLANK, REG_OUT03,
            null, typeName));    
        ((CallableStatement)stmt).registerOutParameter(paramIndex, sqlType, typeName);
    }

    /**
     * Excute the statement.  This function will try to execute the statement and if
     * there is an error 4068, it will rebuild the statement and re-submit it.
     *
     * @return true if the execution was successful.
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
                // Recreate the callable statemtnt.
                this.recreateCallable();
                Debug.out.println(6, "db", "Re-executing call. ->" + stmt);
                return ((PreparedStatement)stmt).execute();
            }
            return false;
        }
        finally {
            outParameters.clear();
            Debug.out.println(6,"db","OUT parameters cleared.");
        }
    }

    public final static String notImplemented = "This method is not implemented in the CachedResultSet";

    public void registerOutParameter(String parameterName, int sqlType) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public java.net.URL getURL(int parameterIndex) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setURL(String parameterName, java.net.URL val) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setNull(String parameterName, int sqlType) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setBoolean(String parameterName, boolean x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setByte(String parameterName, byte x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setShort(String parameterName, short x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setInt(String parameterName, int x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setLong(String parameterName, long x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setFloat(String parameterName, float x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setDouble(String parameterName, double x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setString(String parameterName, String x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setBytes(String parameterName, byte[] x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setDate(String parameterName, Date x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setTime(String parameterName, Time x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setTimestamp(String parameterName, Timestamp x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setObject(String parameterName, Object x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public String getString(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public boolean getBoolean(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public byte getByte(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public short getShort(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public int getInt(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public long getLong(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public float getFloat(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public double getDouble(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public byte[] getBytes(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Date getDate(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Time getTime(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Timestamp getTimestamp(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Object getObject(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public BigDecimal getBigDecimal(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Object getObject(String parameterName, Map map) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Ref getRef(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Blob getBlob(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Clob getClob(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Array getArray(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Date getDate(String parameterName, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Time getTime(String parameterName, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public URL getURL(String parameterName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }
}

