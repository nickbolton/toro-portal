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

public class ParameterMetaDataComponent extends ResourceComponent implements java.sql.ParameterMetaData {
	public ParameterMetaDataComponent(ResourceThing parent, java.sql.ParameterMetaData rawResourceComponent) {
		super(parent, rawResourceComponent);
	}

	private java.sql.ParameterMetaData myRawResourceComponent() {
		return (java.sql.ParameterMetaData) rawResourceComponent;
	}

	@Override
    protected void closeRawResource() {
	}

	public int getParameterCount() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getParameterCount();
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

	public int getParameterType(int parameter0) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getParameterType(parameter0);
		return answer;
	}

	public java.lang.String getParameterTypeName(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getParameterTypeName(parameter0);
		return answer;
	}

	public java.lang.String getParameterClassName(int parameter0) throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getParameterClassName(parameter0);
		return answer;
	}

	public int getParameterMode(int parameter0) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getParameterMode(parameter0);
		return answer;
	}

}
