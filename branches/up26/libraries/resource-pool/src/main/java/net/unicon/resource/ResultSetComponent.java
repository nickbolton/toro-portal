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
package net.unicon.resource;

public class ResultSetComponent extends ResourceComponent implements java.sql.ResultSet {
	public ResultSetComponent(ResourceThing parent, java.sql.ResultSet rawResourceComponent) {
		super(parent, rawResourceComponent);
	}

	private java.sql.ResultSet myRawResourceComponent() {
		return (java.sql.ResultSet) rawResourceComponent;
	}

	@Override
    protected void closeRawResource() {
        try {
            myRawResourceComponent().close();
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
        }
	}

	public java.lang.Object getObject(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.lang.Object answer = myRawResourceComponent().getObject(parameter0);
		return answer;
	}

	public java.lang.Object getObject(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.lang.Object answer = myRawResourceComponent().getObject(parameter0);
		return answer;
	}

	public java.lang.Object getObject(int parameter0, java.util.Map parameter1) throws java.sql.SQLException {
		checkActive();
		java.lang.Object answer = myRawResourceComponent().getObject(parameter0, parameter1);
		return answer;
	}

	public java.lang.Object getObject(java.lang.String parameter0, java.util.Map parameter1) throws java.sql.SQLException {
		checkActive();
		java.lang.Object answer = myRawResourceComponent().getObject(parameter0, parameter1);
		return answer;
	}

	public boolean getBoolean(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().getBoolean(parameter0);
		return answer;
	}

	public boolean getBoolean(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().getBoolean(parameter0);
		return answer;
	}

	public byte getByte(int parameter0) throws java.sql.SQLException {
		checkActive();
		byte answer = myRawResourceComponent().getByte(parameter0);
		return answer;
	}

	public byte getByte(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		byte answer = myRawResourceComponent().getByte(parameter0);
		return answer;
	}

	public short getShort(int parameter0) throws java.sql.SQLException {
		checkActive();
		short answer = myRawResourceComponent().getShort(parameter0);
		return answer;
	}

	public short getShort(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		short answer = myRawResourceComponent().getShort(parameter0);
		return answer;
	}

	public int getInt(int parameter0) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getInt(parameter0);
		return answer;
	}

	public int getInt(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getInt(parameter0);
		return answer;
	}

	public long getLong(int parameter0) throws java.sql.SQLException {
		checkActive();
		long answer = myRawResourceComponent().getLong(parameter0);
		return answer;
	}

	public long getLong(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		long answer = myRawResourceComponent().getLong(parameter0);
		return answer;
	}

	public float getFloat(int parameter0) throws java.sql.SQLException {
		checkActive();
		float answer = myRawResourceComponent().getFloat(parameter0);
		return answer;
	}

	public float getFloat(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		float answer = myRawResourceComponent().getFloat(parameter0);
		return answer;
	}

	public double getDouble(int parameter0) throws java.sql.SQLException {
		checkActive();
		double answer = myRawResourceComponent().getDouble(parameter0);
		return answer;
	}

	public double getDouble(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		double answer = myRawResourceComponent().getDouble(parameter0);
		return answer;
	}

	public byte[] getBytes(int parameter0) throws java.sql.SQLException {
		checkActive();
		byte[] answer = myRawResourceComponent().getBytes(parameter0);
		return answer;
	}

	public byte[] getBytes(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		byte[] answer = myRawResourceComponent().getBytes(parameter0);
		return answer;
	}

	public boolean next() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().next();
		return answer;
	}

	public java.net.URL getURL(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.net.URL answer = myRawResourceComponent().getURL(parameter0);
		return answer;
	}

	public java.net.URL getURL(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.net.URL answer = myRawResourceComponent().getURL(parameter0);
		return answer;
	}

	public int getType() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getType();
		return answer;
	}

	public boolean previous() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().previous();
		return answer;
	}

	public void close() throws java.sql.SQLException {
		if(!isClosed()){
			checkActive();
	        parent.removeComponent(this);
	        clear(true);
		}
	}

	public java.lang.String getString(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getString(parameter0);
		return answer;
	}

	public java.lang.String getString(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getString(parameter0);
		return answer;
	}

	public java.sql.Ref getRef(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Ref answer = myRawResourceComponent().getRef(parameter0);
		return answer;
	}

	public java.sql.Ref getRef(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Ref answer = myRawResourceComponent().getRef(parameter0);
		return answer;
	}

	public java.sql.Time getTime(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Time answer = myRawResourceComponent().getTime(parameter0);
		return answer;
	}

	public java.sql.Time getTime(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Time answer = myRawResourceComponent().getTime(parameter0);
		return answer;
	}

	public java.sql.Time getTime(int parameter0, java.util.Calendar parameter1) throws java.sql.SQLException {
		checkActive();
		java.sql.Time answer = myRawResourceComponent().getTime(parameter0, parameter1);
		return answer;
	}

	public java.sql.Time getTime(java.lang.String parameter0, java.util.Calendar parameter1) throws java.sql.SQLException {
		checkActive();
		java.sql.Time answer = myRawResourceComponent().getTime(parameter0, parameter1);
		return answer;
	}

	public java.sql.Date getDate(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Date answer = myRawResourceComponent().getDate(parameter0);
		return answer;
	}

	public java.sql.Date getDate(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Date answer = myRawResourceComponent().getDate(parameter0);
		return answer;
	}

	public java.sql.Date getDate(int parameter0, java.util.Calendar parameter1) throws java.sql.SQLException {
		checkActive();
		java.sql.Date answer = myRawResourceComponent().getDate(parameter0, parameter1);
		return answer;
	}

	public java.sql.Date getDate(java.lang.String parameter0, java.util.Calendar parameter1) throws java.sql.SQLException {
		checkActive();
		java.sql.Date answer = myRawResourceComponent().getDate(parameter0, parameter1);
		return answer;
	}

	public boolean wasNull() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().wasNull();
		return answer;
	}

	public java.math.BigDecimal getBigDecimal(int parameter0, int parameter1) throws java.sql.SQLException {
		checkActive();
		java.math.BigDecimal answer = myRawResourceComponent().getBigDecimal(parameter0, parameter1);
		return answer;
	}

	public java.math.BigDecimal getBigDecimal(java.lang.String parameter0, int parameter1) throws java.sql.SQLException {
		checkActive();
		java.math.BigDecimal answer = myRawResourceComponent().getBigDecimal(parameter0, parameter1);
		return answer;
	}

	public java.math.BigDecimal getBigDecimal(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.math.BigDecimal answer = myRawResourceComponent().getBigDecimal(parameter0);
		return answer;
	}

	public java.math.BigDecimal getBigDecimal(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.math.BigDecimal answer = myRawResourceComponent().getBigDecimal(parameter0);
		return answer;
	}

	public java.sql.Timestamp getTimestamp(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Timestamp answer = myRawResourceComponent().getTimestamp(parameter0);
		return answer;
	}

	public java.sql.Timestamp getTimestamp(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Timestamp answer = myRawResourceComponent().getTimestamp(parameter0);
		return answer;
	}

	public java.sql.Timestamp getTimestamp(int parameter0, java.util.Calendar parameter1) throws java.sql.SQLException {
		checkActive();
		java.sql.Timestamp answer = myRawResourceComponent().getTimestamp(parameter0, parameter1);
		return answer;
	}

	public java.sql.Timestamp getTimestamp(java.lang.String parameter0, java.util.Calendar parameter1) throws java.sql.SQLException {
		checkActive();
		java.sql.Timestamp answer = myRawResourceComponent().getTimestamp(parameter0, parameter1);
		return answer;
	}

	public java.io.InputStream getAsciiStream(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.io.InputStream answer = myRawResourceComponent().getAsciiStream(parameter0);
		return answer;
	}

	public java.io.InputStream getAsciiStream(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.io.InputStream answer = myRawResourceComponent().getAsciiStream(parameter0);
		return answer;
	}

	public java.io.InputStream getUnicodeStream(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.io.InputStream answer = myRawResourceComponent().getUnicodeStream(parameter0);
		return answer;
	}

	public java.io.InputStream getUnicodeStream(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.io.InputStream answer = myRawResourceComponent().getUnicodeStream(parameter0);
		return answer;
	}

	public java.io.InputStream getBinaryStream(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.io.InputStream answer = myRawResourceComponent().getBinaryStream(parameter0);
		return answer;
	}

	public java.io.InputStream getBinaryStream(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.io.InputStream answer = myRawResourceComponent().getBinaryStream(parameter0);
		return answer;
	}

	public java.sql.SQLWarning getWarnings() throws java.sql.SQLException {
		checkActive();
		java.sql.SQLWarning answer = myRawResourceComponent().getWarnings();
		return answer;
	}

	public void clearWarnings() throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().clearWarnings();
	}

	public java.lang.String getCursorName() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getCursorName();
		return answer;
	}

	public java.sql.ResultSetMetaData getMetaData() throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSetMetaData answer = myRawResourceComponent().getMetaData();
        answer = new ResultSetMetaDataComponent(this, answer);
		return answer;
	}

	public int findColumn(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().findColumn(parameter0);
		return answer;
	}

	public java.io.Reader getCharacterStream(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.io.Reader answer = myRawResourceComponent().getCharacterStream(parameter0);
		return answer;
	}

	public java.io.Reader getCharacterStream(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.io.Reader answer = myRawResourceComponent().getCharacterStream(parameter0);
		return answer;
	}

	public boolean isBeforeFirst() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().isBeforeFirst();
		return answer;
	}

	public boolean isAfterLast() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().isAfterLast();
		return answer;
	}

	public boolean isFirst() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().isFirst();
		return answer;
	}

	public boolean isLast() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().isLast();
		return answer;
	}

	public void beforeFirst() throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().beforeFirst();
	}

	public void afterLast() throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().afterLast();
	}

	public boolean first() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().first();
		return answer;
	}

	public boolean last() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().last();
		return answer;
	}

	public int getRow() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getRow();
		return answer;
	}

	public boolean absolute(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().absolute(parameter0);
		return answer;
	}

	public boolean relative(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().relative(parameter0);
		return answer;
	}

	public void setFetchDirection(int parameter0) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setFetchDirection(parameter0);
	}

	public int getFetchDirection() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getFetchDirection();
		return answer;
	}

	public void setFetchSize(int parameter0) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setFetchSize(parameter0);
	}

	public int getFetchSize() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getFetchSize();
		return answer;
	}

	public int getConcurrency() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getConcurrency();
		return answer;
	}

	public boolean rowUpdated() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().rowUpdated();
		return answer;
	}

	public boolean rowInserted() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().rowInserted();
		return answer;
	}

	public boolean rowDeleted() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().rowDeleted();
		return answer;
	}

	public void updateNull(int parameter0) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateNull(parameter0);
	}

	public void updateNull(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateNull(parameter0);
	}

	public void updateBoolean(int parameter0, boolean parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateBoolean(parameter0, parameter1);
	}

	public void updateBoolean(java.lang.String parameter0, boolean parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateBoolean(parameter0, parameter1);
	}

	public void updateByte(int parameter0, byte parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateByte(parameter0, parameter1);
	}

	public void updateByte(java.lang.String parameter0, byte parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateByte(parameter0, parameter1);
	}

	public void updateShort(int parameter0, short parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateShort(parameter0, parameter1);
	}

	public void updateShort(java.lang.String parameter0, short parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateShort(parameter0, parameter1);
	}

	public void updateInt(int parameter0, int parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateInt(parameter0, parameter1);
	}

	public void updateInt(java.lang.String parameter0, int parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateInt(parameter0, parameter1);
	}

	public void updateLong(int parameter0, long parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateLong(parameter0, parameter1);
	}

	public void updateLong(java.lang.String parameter0, long parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateLong(parameter0, parameter1);
	}

	public void updateFloat(int parameter0, float parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateFloat(parameter0, parameter1);
	}

	public void updateFloat(java.lang.String parameter0, float parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateFloat(parameter0, parameter1);
	}

	public void updateDouble(int parameter0, double parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateDouble(parameter0, parameter1);
	}

	public void updateDouble(java.lang.String parameter0, double parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateDouble(parameter0, parameter1);
	}

	public void updateBigDecimal(int parameter0, java.math.BigDecimal parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateBigDecimal(parameter0, parameter1);
	}

	public void updateBigDecimal(java.lang.String parameter0, java.math.BigDecimal parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateBigDecimal(parameter0, parameter1);
	}

	public void updateString(int parameter0, java.lang.String parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateString(parameter0, parameter1);
	}

	public void updateString(java.lang.String parameter0, java.lang.String parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateString(parameter0, parameter1);
	}

	public void updateBytes(int parameter0, byte[] parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateBytes(parameter0, parameter1);
	}

	public void updateBytes(java.lang.String parameter0, byte[] parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateBytes(parameter0, parameter1);
	}

	public void updateDate(int parameter0, java.sql.Date parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateDate(parameter0, parameter1);
	}

	public void updateDate(java.lang.String parameter0, java.sql.Date parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateDate(parameter0, parameter1);
	}

	public void updateTime(int parameter0, java.sql.Time parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateTime(parameter0, parameter1);
	}

	public void updateTime(java.lang.String parameter0, java.sql.Time parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateTime(parameter0, parameter1);
	}

	public void updateTimestamp(int parameter0, java.sql.Timestamp parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateTimestamp(parameter0, parameter1);
	}

	public void updateTimestamp(java.lang.String parameter0, java.sql.Timestamp parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateTimestamp(parameter0, parameter1);
	}

	public void updateAsciiStream(int parameter0, java.io.InputStream parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateAsciiStream(parameter0, parameter1, parameter2);
	}

	public void updateAsciiStream(java.lang.String parameter0, java.io.InputStream parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateAsciiStream(parameter0, parameter1, parameter2);
	}

	public void updateBinaryStream(int parameter0, java.io.InputStream parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateBinaryStream(parameter0, parameter1, parameter2);
	}

	public void updateBinaryStream(java.lang.String parameter0, java.io.InputStream parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateBinaryStream(parameter0, parameter1, parameter2);
	}

	public void updateCharacterStream(int parameter0, java.io.Reader parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateCharacterStream(parameter0, parameter1, parameter2);
	}

	public void updateCharacterStream(java.lang.String parameter0, java.io.Reader parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateCharacterStream(parameter0, parameter1, parameter2);
	}

	public void updateObject(int parameter0, java.lang.Object parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateObject(parameter0, parameter1, parameter2);
	}

	public void updateObject(int parameter0, java.lang.Object parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateObject(parameter0, parameter1);
	}

	public void updateObject(java.lang.String parameter0, java.lang.Object parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateObject(parameter0, parameter1, parameter2);
	}

	public void updateObject(java.lang.String parameter0, java.lang.Object parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateObject(parameter0, parameter1);
	}

	public void insertRow() throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().insertRow();
	}

	public void updateRow() throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateRow();
	}

	public void deleteRow() throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().deleteRow();
	}

	public void refreshRow() throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().refreshRow();
	}

	public void cancelRowUpdates() throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().cancelRowUpdates();
	}

	public void moveToInsertRow() throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().moveToInsertRow();
	}

	public void moveToCurrentRow() throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().moveToCurrentRow();
	}

	public java.sql.Statement getStatement() throws java.sql.SQLException {
		checkActive();
		java.sql.Statement answer = myRawResourceComponent().getStatement();
        answer = new StatementComponent(this, answer);
		return answer;
	}

	public java.sql.Blob getBlob(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Blob answer = myRawResourceComponent().getBlob(parameter0);
        answer = new BlobComponent(this, answer);
		return answer;
	}

	public java.sql.Blob getBlob(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Blob answer = myRawResourceComponent().getBlob(parameter0);
        answer = new BlobComponent(this, answer);
		return answer;
	}

	public java.sql.Clob getClob(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Clob answer = myRawResourceComponent().getClob(parameter0);
        answer = new ClobComponent(this, answer);
		return answer;
	}

	public java.sql.Clob getClob(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Clob answer = myRawResourceComponent().getClob(parameter0);
        answer = new ClobComponent(this, answer);
		return answer;
	}

	public java.sql.Array getArray(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Array answer = myRawResourceComponent().getArray(parameter0);
		return answer;
	}

	public java.sql.Array getArray(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Array answer = myRawResourceComponent().getArray(parameter0);
		return answer;
	}

	public void updateRef(int parameter0, java.sql.Ref parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateRef(parameter0, parameter1);
	}

	public void updateRef(java.lang.String parameter0, java.sql.Ref parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateRef(parameter0, parameter1);
	}

	public void updateBlob(int parameter0, java.sql.Blob parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateBlob(parameter0, parameter1);
	}

	public void updateBlob(java.lang.String parameter0, java.sql.Blob parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateBlob(parameter0, parameter1);
	}

	public void updateClob(int parameter0, java.sql.Clob parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateClob(parameter0, parameter1);
	}

	public void updateClob(java.lang.String parameter0, java.sql.Clob parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateClob(parameter0, parameter1);
	}

	public void updateArray(int parameter0, java.sql.Array parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateArray(parameter0, parameter1);
	}

	public void updateArray(java.lang.String parameter0, java.sql.Array parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().updateArray(parameter0, parameter1);
	}

}
