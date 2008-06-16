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

/**
 * PreparedStatementComponent currently logs at the DEBUG level the SQL of the prepared
 * statements and their time to execute.
 */
public class PreparedStatementComponent extends StatementComponent implements java.sql.PreparedStatement {

    private String sql;


	public PreparedStatementComponent(ResourceThing parent, java.sql.PreparedStatement rawResourceComponent, String sql) {
		super(parent, rawResourceComponent);
        this.sql = sql;
	}

	private java.sql.PreparedStatement myRawResourceComponent() {
		return (java.sql.PreparedStatement) rawResourceComponent;
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

	public void setBoolean(int parameter0, boolean parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setBoolean(parameter0, parameter1);
	}

	public void setByte(int parameter0, byte parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setByte(parameter0, parameter1);
	}

	public void setShort(int parameter0, short parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setShort(parameter0, parameter1);
	}

	public void setInt(int parameter0, int parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setInt(parameter0, parameter1);
	}

	public void setLong(int parameter0, long parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setLong(parameter0, parameter1);
	}

	public void setFloat(int parameter0, float parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setFloat(parameter0, parameter1);
	}

	public void setDouble(int parameter0, double parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setDouble(parameter0, parameter1);
	}

	public void setURL(int parameter0, java.net.URL parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setURL(parameter0, parameter1);
	}

	public void setTime(int parameter0, java.sql.Time parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setTime(parameter0, parameter1);
	}

	public void setTime(int parameter0, java.sql.Time parameter1, java.util.Calendar parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setTime(parameter0, parameter1, parameter2);
	}

	public boolean execute() throws java.sql.SQLException {
		checkActive();
        long t = 0;
        if (log.isDebugEnabled()) {
            log.debug("executing: " + sql);
            t = System.currentTimeMillis();
        }
		boolean answer = myRawResourceComponent().execute();
        if (log.isDebugEnabled()) {
            log.debug("finished (" + (System.currentTimeMillis() - t) + " ms): " + sql);
        }
		return answer;
	}

	public java.sql.ResultSet executeQuery() throws java.sql.SQLException {
		checkActive();
        long t = 0;
        if (log.isDebugEnabled()) {
            log.debug("executing: " + sql);
            t = System.currentTimeMillis();
        }
		java.sql.ResultSet answer = myRawResourceComponent().executeQuery();
        if (log.isDebugEnabled()) {
            log.debug("finished (" + (System.currentTimeMillis() - t) + " ms): " + sql);
        }
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public int executeUpdate() throws java.sql.SQLException {
		checkActive();
        long t = 0;
        if (log.isDebugEnabled()) {
            log.debug("executing: " + sql);
            t = System.currentTimeMillis();
        }
		int answer = myRawResourceComponent().executeUpdate();
        if (log.isDebugEnabled()) {
            log.debug("finished (" + (System.currentTimeMillis() - t) + " ms): " + sql);
        }
		return answer;
	}

	public void setNull(int parameter0, int parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setNull(parameter0, parameter1);
	}

	public void setNull(int parameter0, int parameter1, java.lang.String parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setNull(parameter0, parameter1, parameter2);
	}

	public void setBigDecimal(int parameter0, java.math.BigDecimal parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setBigDecimal(parameter0, parameter1);
	}

	public void setString(int parameter0, java.lang.String parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setString(parameter0, parameter1);
	}

	public void setBytes(int parameter0, byte[] parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setBytes(parameter0, parameter1);
	}

	public void setDate(int parameter0, java.sql.Date parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setDate(parameter0, parameter1);
	}

	public void setDate(int parameter0, java.sql.Date parameter1, java.util.Calendar parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setDate(parameter0, parameter1, parameter2);
	}

	public void setTimestamp(int parameter0, java.sql.Timestamp parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setTimestamp(parameter0, parameter1);
	}

	public void setTimestamp(int parameter0, java.sql.Timestamp parameter1, java.util.Calendar parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setTimestamp(parameter0, parameter1, parameter2);
	}

	public void setAsciiStream(int parameter0, java.io.InputStream parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setAsciiStream(parameter0, parameter1, parameter2);
	}

	public void setUnicodeStream(int parameter0, java.io.InputStream parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setUnicodeStream(parameter0, parameter1, parameter2);
	}

	public void setBinaryStream(int parameter0, java.io.InputStream parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setBinaryStream(parameter0, parameter1, parameter2);
	}

	public void clearParameters() throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().clearParameters();
	}

	public void setObject(int parameter0, java.lang.Object parameter1, int parameter2, int parameter3) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setObject(parameter0, parameter1, parameter2, parameter3);
	}

	public void setObject(int parameter0, java.lang.Object parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setObject(parameter0, parameter1, parameter2);
	}

	public void setObject(int parameter0, java.lang.Object parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setObject(parameter0, parameter1);
	}

	public void addBatch() throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().addBatch();
	}

	public void setCharacterStream(int parameter0, java.io.Reader parameter1, int parameter2) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setCharacterStream(parameter0, parameter1, parameter2);
	}

	public void setRef(int parameter0, java.sql.Ref parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setRef(parameter0, parameter1);
	}

	public void setBlob(int parameter0, java.sql.Blob parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setBlob(parameter0, parameter1);
	}

	public void setClob(int parameter0, java.sql.Clob parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setClob(parameter0, parameter1);
	}

	public void setArray(int parameter0, java.sql.Array parameter1) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setArray(parameter0, parameter1);
	}

	public java.sql.ResultSetMetaData getMetaData() throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSetMetaData answer = myRawResourceComponent().getMetaData();
        answer = new ResultSetMetaDataComponent(this, answer);
		return answer;
	}

	public java.sql.ParameterMetaData getParameterMetaData() throws java.sql.SQLException {
		checkActive();
		java.sql.ParameterMetaData answer = myRawResourceComponent().getParameterMetaData();
        answer = new ParameterMetaDataComponent(this, answer);
		return answer;
	}

}
