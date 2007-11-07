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

public class CallableStatementComponent extends PreparedStatementComponent implements java.sql.CallableStatement {
	public CallableStatementComponent(ResourceThing parent, java.sql.CallableStatement rawResourceComponent) {
		super(parent, rawResourceComponent, "");
	}

	private java.sql.CallableStatement myRawResourceComponent() {
		return (java.sql.CallableStatement) rawResourceComponent;
	}

	@Override
    protected void closeRawResource() {
        try {
            myRawResourceComponent().close();
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }

            if (log.isInfoEnabled()) {
                log.info("Throwable generated closing raw underlying resource component.", t);
            }
        }
	}

	public java.lang.Object getObject(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.lang.Object answer = myRawResourceComponent().getObject(parameter0);
		return answer;
	}

	public java.lang.Object getObject(int parameter0, java.util.Map parameter1) throws java.sql.SQLException {
		checkActive();
		java.lang.Object answer = myRawResourceComponent().getObject(parameter0, parameter1);
		return answer;
	}

	public java.lang.Object getObject(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.lang.Object answer = myRawResourceComponent().getObject(parameter0);
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

	public void setBoolean(java.lang.String parameter0, boolean parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setBoolean(parameter0, parameter1);
	}

	public void setByte(java.lang.String parameter0, byte parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setByte(parameter0, parameter1);
	}

	public void setShort(java.lang.String parameter0, short parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setShort(parameter0, parameter1);
	}

	public void setInt(java.lang.String parameter0, int parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setInt(parameter0, parameter1);
	}

	public void setLong(java.lang.String parameter0, long parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setLong(parameter0, parameter1);
	}

	public void setFloat(java.lang.String parameter0, float parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setFloat(parameter0, parameter1);
	}

	public void setDouble(java.lang.String parameter0, double parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setDouble(parameter0, parameter1);
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

	public void setURL(java.lang.String parameter0, java.net.URL parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setURL(parameter0, parameter1);
	}

	public void setTime(java.lang.String parameter0, java.sql.Time parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setTime(parameter0, parameter1);
	}

	public void setTime(java.lang.String parameter0, java.sql.Time parameter1, java.util.Calendar parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setTime(parameter0, parameter1, parameter2);
	}

	public java.sql.Time getTime(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Time answer = myRawResourceComponent().getTime(parameter0);
		return answer;
	}

	public java.sql.Time getTime(int parameter0, java.util.Calendar parameter1) throws java.sql.SQLException {
		checkActive();
		java.sql.Time answer = myRawResourceComponent().getTime(parameter0, parameter1);
		return answer;
	}

	public java.sql.Time getTime(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Time answer = myRawResourceComponent().getTime(parameter0);
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

	public java.sql.Date getDate(int parameter0, java.util.Calendar parameter1) throws java.sql.SQLException {
		checkActive();
		java.sql.Date answer = myRawResourceComponent().getDate(parameter0, parameter1);
		return answer;
	}

	public java.sql.Date getDate(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Date answer = myRawResourceComponent().getDate(parameter0);
		return answer;
	}

	public java.sql.Date getDate(java.lang.String parameter0, java.util.Calendar parameter1) throws java.sql.SQLException {
		checkActive();
		java.sql.Date answer = myRawResourceComponent().getDate(parameter0, parameter1);
		return answer;
	}

	public void registerOutParameter(int parameter0, int parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().registerOutParameter(parameter0, parameter1);
	}

	public void registerOutParameter(int parameter0, int parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().registerOutParameter(parameter0, parameter1, parameter2);
	}

	public void registerOutParameter(int parameter0, int parameter1, java.lang.String parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().registerOutParameter(parameter0, parameter1, parameter2);
	}

	public void registerOutParameter(java.lang.String parameter0, int parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().registerOutParameter(parameter0, parameter1);
	}

	public void registerOutParameter(java.lang.String parameter0, int parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().registerOutParameter(parameter0, parameter1, parameter2);
	}

	public void registerOutParameter(java.lang.String parameter0, int parameter1, java.lang.String parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().registerOutParameter(parameter0, parameter1, parameter2);
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

	public java.sql.Timestamp getTimestamp(int parameter0, java.util.Calendar parameter1) throws java.sql.SQLException {
		checkActive();
		java.sql.Timestamp answer = myRawResourceComponent().getTimestamp(parameter0, parameter1);
		return answer;
	}

	public java.sql.Timestamp getTimestamp(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.Timestamp answer = myRawResourceComponent().getTimestamp(parameter0);
		return answer;
	}

	public java.sql.Timestamp getTimestamp(java.lang.String parameter0, java.util.Calendar parameter1) throws java.sql.SQLException {
		checkActive();
		java.sql.Timestamp answer = myRawResourceComponent().getTimestamp(parameter0, parameter1);
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

	public void setNull(java.lang.String parameter0, int parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setNull(parameter0, parameter1);
	}

	public void setNull(java.lang.String parameter0, int parameter1, java.lang.String parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setNull(parameter0, parameter1, parameter2);
	}

	public void setBigDecimal(java.lang.String parameter0, java.math.BigDecimal parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setBigDecimal(parameter0, parameter1);
	}

	public void setString(java.lang.String parameter0, java.lang.String parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setString(parameter0, parameter1);
	}

	public void setBytes(java.lang.String parameter0, byte[] parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setBytes(parameter0, parameter1);
	}

	public void setDate(java.lang.String parameter0, java.sql.Date parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setDate(parameter0, parameter1);
	}

	public void setDate(java.lang.String parameter0, java.sql.Date parameter1, java.util.Calendar parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setDate(parameter0, parameter1, parameter2);
	}

	public void setTimestamp(java.lang.String parameter0, java.sql.Timestamp parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setTimestamp(parameter0, parameter1);
	}

	public void setTimestamp(java.lang.String parameter0, java.sql.Timestamp parameter1, java.util.Calendar parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setTimestamp(parameter0, parameter1, parameter2);
	}

	public void setAsciiStream(java.lang.String parameter0, java.io.InputStream parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setAsciiStream(parameter0, parameter1, parameter2);
	}

	public void setBinaryStream(java.lang.String parameter0, java.io.InputStream parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setBinaryStream(parameter0, parameter1, parameter2);
	}

	public void setObject(java.lang.String parameter0, java.lang.Object parameter1, int parameter2, int parameter3) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setObject(parameter0, parameter1, parameter2, parameter3);
	}

	public void setObject(java.lang.String parameter0, java.lang.Object parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setObject(parameter0, parameter1, parameter2);
	}

	public void setObject(java.lang.String parameter0, java.lang.Object parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setObject(parameter0, parameter1);
	}

	public void setCharacterStream(java.lang.String parameter0, java.io.Reader parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setCharacterStream(parameter0, parameter1, parameter2);
	}

}
