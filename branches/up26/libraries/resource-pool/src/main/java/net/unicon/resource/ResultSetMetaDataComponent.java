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

public class ResultSetMetaDataComponent extends ResourceComponent implements java.sql.ResultSetMetaData {
	public ResultSetMetaDataComponent(ResourceThing parent, java.sql.ResultSetMetaData rawResourceComponent) {
		super(parent, rawResourceComponent);
	}

	private java.sql.ResultSetMetaData myRawResourceComponent() {
		return (java.sql.ResultSetMetaData) rawResourceComponent;
	}

	@Override
    protected void closeRawResource() {
	}

	public boolean isReadOnly(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().isReadOnly(parameter0);
		return answer;
	}

	public int getColumnCount() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getColumnCount();
		return answer;
	}

	public boolean isAutoIncrement(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().isAutoIncrement(parameter0);
		return answer;
	}

	public boolean isCaseSensitive(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().isCaseSensitive(parameter0);
		return answer;
	}

	public boolean isSearchable(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().isSearchable(parameter0);
		return answer;
	}

	public boolean isCurrency(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().isCurrency(parameter0);
		return answer;
	}

	public int isNullable(int parameter0) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().isNullable(parameter0);
		return answer;
	}

	public boolean isSigned(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().isSigned(parameter0);
		return answer;
	}

	public int getColumnDisplaySize(int parameter0) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getColumnDisplaySize(parameter0);
		return answer;
	}

	public java.lang.String getColumnLabel(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getColumnLabel(parameter0);
		return answer;
	}

	public java.lang.String getColumnName(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getColumnName(parameter0);
		return answer;
	}

	public java.lang.String getSchemaName(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getSchemaName(parameter0);
		return answer;
	}

	public int getPrecision(int parameter0) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getPrecision(parameter0);
		return answer;
	}

	public int getScale(int parameter0) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getScale(parameter0);
		return answer;
	}

	public java.lang.String getTableName(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getTableName(parameter0);
		return answer;
	}

	public java.lang.String getCatalogName(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getCatalogName(parameter0);
		return answer;
	}

	public int getColumnType(int parameter0) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getColumnType(parameter0);
		return answer;
	}

	public java.lang.String getColumnTypeName(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getColumnTypeName(parameter0);
		return answer;
	}

	public boolean isWritable(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().isWritable(parameter0);
		return answer;
	}

	public boolean isDefinitelyWritable(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().isDefinitelyWritable(parameter0);
		return answer;
	}

	public java.lang.String getColumnClassName(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getColumnClassName(parameter0);
		return answer;
	}

}
