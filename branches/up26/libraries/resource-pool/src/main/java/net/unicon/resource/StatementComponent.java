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

public class StatementComponent extends ResourceComponent implements java.sql.Statement {
	public StatementComponent(ResourceThing parent, java.sql.Statement rawResourceComponent) {
		super(parent, rawResourceComponent);
	}

	private java.sql.Statement myRawResourceComponent() {
		return (java.sql.Statement) rawResourceComponent;
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

	public void close() throws java.sql.SQLException {
		if(!isClosed()){
			checkActive();
	        parent.removeComponent(this);
			clear(true);
		}
	}

	public boolean execute(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().execute(parameter0);
		return answer;
	}

	public boolean execute(java.lang.String parameter0, int parameter1) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().execute(parameter0, parameter1);
		return answer;
	}

	public boolean execute(java.lang.String parameter0, int[] parameter1) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().execute(parameter0, parameter1);
		return answer;
	}

	public boolean execute(java.lang.String parameter0, java.lang.String[] parameter1) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().execute(parameter0, parameter1);
		return answer;
	}

	public java.sql.ResultSet executeQuery(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().executeQuery(parameter0);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public int executeUpdate(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().executeUpdate(parameter0);
		return answer;
	}

	public int executeUpdate(java.lang.String parameter0, int parameter1) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().executeUpdate(parameter0, parameter1);
		return answer;
	}

	public int executeUpdate(java.lang.String parameter0, int[] parameter1) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().executeUpdate(parameter0, parameter1);
		return answer;
	}

	public int executeUpdate(java.lang.String parameter0, java.lang.String[] parameter1) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().executeUpdate(parameter0, parameter1);
		return answer;
	}

	public int getMaxFieldSize() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxFieldSize();
		return answer;
	}

	public void setMaxFieldSize(int parameter0) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setMaxFieldSize(parameter0);
	}

	public int getMaxRows() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxRows();
		return answer;
	}

	public void setMaxRows(int parameter0) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setMaxRows(parameter0);
	}

	public void setEscapeProcessing(boolean parameter0) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setEscapeProcessing(parameter0);
	}

	public int getQueryTimeout() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getQueryTimeout();
		return answer;
	}

	public void setQueryTimeout(int parameter0) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setQueryTimeout(parameter0);
	}

	public void cancel() throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().cancel();
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

	public void setCursorName(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().setCursorName(parameter0);
	}

	public java.sql.ResultSet getResultSet() throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getResultSet();
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public int getUpdateCount() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getUpdateCount();
		return answer;
	}

	public boolean getMoreResults() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().getMoreResults();
		return answer;
	}

	public boolean getMoreResults(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().getMoreResults(parameter0);
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

	public int getResultSetConcurrency() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getResultSetConcurrency();
		return answer;
	}

	public int getResultSetType() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getResultSetType();
		return answer;
	}

	public void addBatch(java.lang.String parameter0) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().addBatch(parameter0);
	}

	public void clearBatch() throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().clearBatch();
	}

	public int[] executeBatch() throws java.sql.SQLException {
		checkActive();
		int[] answer = myRawResourceComponent().executeBatch();
		return answer;
	}

	public java.sql.Connection getConnection() throws java.sql.SQLException {
		checkActive();
        return (java.sql.Connection) getTopResource();
	}

	public java.sql.ResultSet getGeneratedKeys() throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getGeneratedKeys();
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public int getResultSetHoldability() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getResultSetHoldability();
		return answer;
	}

}
