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

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CachedResultSet implements ResultSet, Serializable
{
    public static final long serialVersionUID = 0L;

    List rows = new ArrayList();
    private int currentRow = -1;
    private final static String notImplemented = "This method is not implemented in the CachedResultSet";
    String[] columnNames;

    public int getRowCount() { return rows.size(); }

    public CachedResultSet(ResultSet resultSet)
    {
        try
        {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            Object[] row;

            columnNames = new String[columnCount];

            for(int i = 0; i < columnCount; i++)
                columnNames[i] = metaData.getColumnName(i + 1);

            while(resultSet.next())
            {
                row = new Object[columnCount];

                for(int i = 0; i < columnCount; i++)
                    row[i] = resultSet.getObject(i + 1);

                rows.add(row);
            }
        }
        catch(SQLException e)
        {
            System.setErr(System.out);
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public CachedResultSet(CachedResultSet resultSet,boolean dummy) {
        columnNames = new String[resultSet.columnNames.length];
        
        for(int i = 0; i < resultSet.columnNames.length; i++) {
            columnNames[i] = resultSet.columnNames[i];
        }
        rows.addAll(resultSet.rows);
    }

    public boolean next() throws SQLException
    {
        return ++currentRow < rows.size() ? true : false;
    }

    public void close() throws SQLException
    {
    }

    public boolean wasNull() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public String getString(int columnIndex) throws SQLException
    {
        Object object = ((Object[])rows.get(currentRow))[columnIndex - 1];

        if(object == null)
            return "";
        else
            return object.toString();
    }

    public boolean getBoolean(int columnIndex) throws SQLException
    {
        Object object = ((Object[])rows.get(currentRow))[columnIndex - 1];

        if(object == null)
            return false;
        else
            return ((Number)object).intValue() == 0 ? false : true;
    }

    public byte getByte(int columnIndex) throws SQLException
    {
        Object object = ((Object[])rows.get(currentRow))[columnIndex - 1];

        if(object == null)
            return (byte)0;
        else
            return ((Number)object).byteValue();
    }

    public short getShort(int columnIndex) throws SQLException
    {
        Object object = ((Object[])rows.get(currentRow))[columnIndex - 1];

        if(object == null)
            return 0;
        else
            return ((Number)object).shortValue();
    }

    public int getInt(int columnIndex) throws SQLException
    {
        Object object = ((Object[])rows.get(currentRow))[columnIndex - 1];

        if(object == null)
            return 0;
        else
            return ((Number)object).intValue();
    }

    public long getLong(int columnIndex) throws SQLException
    {
        Object object = ((Object[])rows.get(currentRow))[columnIndex - 1];

        if(object == null)
            return 0;
        else
            return ((Number)object).longValue();
    }

    public float getFloat(int columnIndex) throws SQLException
    {
        Object object = ((Object[])rows.get(currentRow))[columnIndex - 1];

        if(object == null)
            return 0;
        else
            return ((Number)object).floatValue();
    }

    public double getDouble(int columnIndex) throws SQLException
    {
        Object object = ((Object[])rows.get(currentRow))[columnIndex - 1];

        if(object == null)
            return 0;
        else
            return ((Number)object).doubleValue();
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
    {
        BigDecimal returnValue = (BigDecimal)((Object[])rows.get(currentRow))[columnIndex - 1];

        returnValue.setScale(scale);
        return returnValue;
    }

    public byte[] getBytes(int columnIndex) throws SQLException
    {
        return (byte[])((Object[])rows.get(currentRow))[columnIndex - 1];
    }

    public java.sql.Date getDate(int columnIndex) throws SQLException
    {
        Object object = ((Object[])rows.get(currentRow))[columnIndex - 1];

        if(object == null)
            return new java.sql.Date(0);
        else
            return
                new java.sql.Date(((java.util.Date)object).getTime());
    }

    public java.sql.Time getTime(int columnIndex) throws SQLException
    {
        Object object = ((Object[])rows.get(currentRow))[columnIndex - 1];

        if(object == null)
            return new java.sql.Time(0);
        else
            return (java.sql.Time)object;
    }

    public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException
    {
        Object object = ((Object[])rows.get(currentRow))[columnIndex - 1];

        if(object == null)
            return new java.sql.Timestamp(0);
        else
            return (java.sql.Timestamp)object;
    }

    public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public java.io.InputStream getBinaryStream(int columnIndex) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public String getString(String columnName) throws SQLException
    {
        return getString(findColumn(columnName));
    }

    public boolean getBoolean(String columnName) throws SQLException
    {
        return getBoolean(findColumn(columnName));
    }

    public byte getByte(String columnName) throws SQLException
    {
        return getByte(findColumn(columnName));
    }

    public short getShort(String columnName) throws SQLException
    {
        return getShort(findColumn(columnName));
    }

    public int getInt(String columnName) throws SQLException
    {
        return getInt(findColumn(columnName));
    }

    public long getLong(String columnName) throws SQLException
    {
        return getLong(findColumn(columnName));
    }

    public float getFloat(String columnName) throws SQLException
    {
        return getFloat(findColumn(columnName));
    }

    public double getDouble(String columnName) throws SQLException
    {
        return getDouble(findColumn(columnName));
    }

    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException
    {
        return getBigDecimal(findColumn(columnName), scale);
    }

    public byte[] getBytes(String columnName) throws SQLException
    {
        return getBytes(findColumn(columnName));
    }

    public java.sql.Date getDate(String columnName) throws SQLException
    {
        return getDate(findColumn(columnName));
    }

    public java.sql.Time getTime(String columnName) throws SQLException
    {
        return getTime(findColumn(columnName));
    }

    public java.sql.Timestamp getTimestamp(String columnName) throws SQLException
    {
        return getTimestamp(findColumn(columnName));
    }

    public java.io.InputStream getAsciiStream(String columnName) throws SQLException
    {
        return getAsciiStream(findColumn(columnName));
    }

    public java.io.InputStream getUnicodeStream(String columnName) throws SQLException
    {
        return getUnicodeStream(findColumn(columnName));
    }

    public java.io.InputStream getBinaryStream(String columnName) throws SQLException
    {
        return getBinaryStream(findColumn(columnName));
    }

    public SQLWarning getWarnings() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void clearWarnings() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public String getCursorName() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public ResultSetMetaData getMetaData() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Object getObject(int columnIndex) throws SQLException
    {
        return ((Object[])rows.get(currentRow))[columnIndex - 1];
    }

    public Object getObject(String columnName) throws SQLException
    {
        return getObject(findColumn(columnName));
    }

    public int findColumn(String columnName) throws SQLException
    {
        for(int i = 0; i < columnNames.length; i++)
            if(columnName.equalsIgnoreCase(columnNames[i]))
                return i+1;

        throw new IndexOutOfBoundsException(columnName + " is not a valid column name for the resultset.");
    }

    public java.io.Reader getCharacterStream(int columnIndex) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public java.io.Reader getCharacterStream(String columnName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException
    {
        return (BigDecimal)((Object[])rows.get(currentRow))[columnIndex - 1];
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException
    {
        return getBigDecimal(findColumn(columnName));
    }

    public boolean isBeforeFirst() throws SQLException
    {
        return currentRow == -1;
    }

    public boolean isAfterLast() throws SQLException
    {
        return currentRow >= rows.size();
    }

    public boolean isFirst() throws SQLException
     {
        return currentRow == 0;
    }

    public boolean isLast() throws SQLException
    {
        return currentRow == rows.size() - 1;
    }

    public void beforeFirst() throws SQLException
    {
        currentRow = -1;
    }

    public void afterLast() throws SQLException
    {
        currentRow = rows.size();
    }

    public boolean first() throws SQLException
    {
        currentRow = 0;
        return currentRow >= 0 && currentRow < rows.size() ? true : false;
    }

    public boolean last() throws SQLException
    {
        currentRow = rows.size() - 1;
        return currentRow >= 0 && currentRow < rows.size() ? true : false;
    }

    public int getRow() throws SQLException
    {
        return currentRow + 1;
    }

    public boolean absolute(int row) throws SQLException
    {
        currentRow = row - 1;
        return currentRow >= 0 && currentRow < rows.size() ? true : false;
    }

    public boolean relative(int rowChange) throws SQLException
    {
        currentRow += rowChange;
        return currentRow >= 0 && currentRow < rows.size() ? true : false;
    }

    public boolean previous() throws SQLException
    {
        currentRow--;
        return currentRow >= 0 && currentRow < rows.size() ? true : false;
    }

    public void setFetchDirection(int direction) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public int getFetchDirection() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void setFetchSize(int rows) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public int getFetchSize() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public int getType() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public int getConcurrency() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public boolean rowUpdated() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public boolean rowInserted() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public boolean rowDeleted() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateNull(int columnIndex) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateByte(int columnIndex, byte x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateShort(int columnIndex, short x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateInt(int columnIndex, int x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateLong(int columnIndex, long x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateFloat(int columnIndex, float x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateDouble(int columnIndex, double x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateString(int columnIndex, String x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateBytes(int columnIndex, byte x[]) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateDate(int columnIndex, java.sql.Date x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateTime(int columnIndex, java.sql.Time x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateTimestamp(int columnIndex, java.sql.Timestamp x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateAsciiStream(int columnIndex, java.io.InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateBinaryStream(int columnIndex, java.io.InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateCharacterStream(int columnIndex, java.io.Reader x, int length) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateObject(int columnIndex, Object x, int scale) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateObject(int columnIndex, Object x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateNull(String columnName) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateBoolean(String columnName, boolean x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateByte(String columnName, byte x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateShort(String columnName, short x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateInt(String columnName, int x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateLong(String columnName, long x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateFloat(String columnName, float x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateDouble(String columnName, double x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateString(String columnName, String x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateBytes(String columnName, byte x[]) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateDate(String columnName, java.sql.Date x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateTime(String columnName, java.sql.Time x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateTimestamp(String columnName, java.sql.Timestamp x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateAsciiStream(String columnName, java.io.InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateBinaryStream(String columnName, java.io.InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateCharacterStream(String columnName, java.io.Reader reader, int length) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateObject(String columnName, Object x, int scale) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateObject(String columnName, Object x) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void insertRow() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void updateRow() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void deleteRow() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void refreshRow() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void cancelRowUpdates() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void moveToInsertRow() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public void moveToCurrentRow() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Statement getStatement() throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Object getObject(int columnIndex, java.util.Map map) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Ref getRef(int columnIndex) throws SQLException
    {
        return (Ref)((Object[])rows.get(currentRow))[columnIndex - 1];
    }

    public Blob getBlob(int columnIndex) throws SQLException
    {
        return (Blob)((Object[])rows.get(currentRow))[columnIndex - 1];
    }

    public Clob getClob(int columnIndex) throws SQLException
    {
        return (Clob)((Object[])rows.get(currentRow))[columnIndex - 1];
    }

    public Array getArray(int columnIndex) throws SQLException
    {
        return (Array)((Object[])rows.get(currentRow))[columnIndex - 1];
    }

    public Object getObject(String colName, java.util.Map map) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public Ref getRef(String colName) throws SQLException
    {
        return getRef(findColumn(colName));
    }

    public Blob getBlob(String colName) throws SQLException
    {
        return getBlob(findColumn(colName));
    }

    public Clob getClob(String colName) throws SQLException
    {
        return getClob(findColumn(colName));
    }

    public Array getArray(String colName) throws SQLException
    {
        return getArray(findColumn(colName));
    }

    public java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public java.sql.Date getDate(String columnName, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public java.sql.Time getTime(int columnIndex, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public java.sql.Time getTime(String columnName, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

    public java.sql.Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException(notImplemented);
    }

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

