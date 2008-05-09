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

public class ClobComponent extends ResourceComponent implements java.sql.Clob {
	public ClobComponent(ResourceThing parent, java.sql.Clob rawResourceComponent) {
		super(parent, rawResourceComponent);
	}

	private java.sql.Clob myRawResourceComponent() {
		return (java.sql.Clob) rawResourceComponent;
	}

	@Override
    protected void closeRawResource() {
	}

	public long length() throws java.sql.SQLException {
		checkActive();
		long answer = myRawResourceComponent().length();
		return answer;
	}

	public long position(java.lang.String parameter0, long parameter1) throws java.sql.SQLException {
		checkActive();
		long answer = myRawResourceComponent().position(parameter0, parameter1);
		return answer;
	}

	public long position(java.sql.Clob parameter0, long parameter1) throws java.sql.SQLException {
		checkActive();
		long answer = myRawResourceComponent().position(parameter0, parameter1);
		return answer;
	}

	public java.lang.String getSubString(long parameter0, int parameter1) throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getSubString(parameter0, parameter1);
		return answer;
	}

	public java.io.Reader getCharacterStream() throws java.sql.SQLException {
		checkActive();
		java.io.Reader answer = myRawResourceComponent().getCharacterStream();
		return answer;
	}

	public java.io.InputStream getAsciiStream() throws java.sql.SQLException {
		checkActive();
		java.io.InputStream answer = myRawResourceComponent().getAsciiStream();
		return answer;
	}

	public int setString(long parameter0, java.lang.String parameter1) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().setString(parameter0, parameter1);
		return answer;
	}

	public int setString(long parameter0, java.lang.String parameter1, int parameter2, int parameter3) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().setString(parameter0, parameter1, parameter2, parameter3);
		return answer;
	}

	public java.io.OutputStream setAsciiStream(long parameter0) throws java.sql.SQLException {
		checkActive();
		java.io.OutputStream answer = myRawResourceComponent().setAsciiStream(parameter0);
		return answer;
	}

	public java.io.Writer setCharacterStream(long parameter0) throws java.sql.SQLException {
		checkActive();
		java.io.Writer answer = myRawResourceComponent().setCharacterStream(parameter0);
		return answer;
	}

	public void truncate(long parameter0) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().truncate(parameter0);
	}

}
