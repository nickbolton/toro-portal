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

public class BlobComponent extends ResourceComponent implements java.sql.Blob {
	public BlobComponent(ResourceThing parent, java.sql.Blob rawResourceComponent) {
		super(parent, rawResourceComponent);
	}

	private java.sql.Blob myRawResourceComponent() {
		return (java.sql.Blob) rawResourceComponent;
	}

	@Override
    protected void closeRawResource() {
	}

	public long length() throws java.sql.SQLException {
		checkActive();
		long answer = myRawResourceComponent().length();
		return answer;
	}

	public byte[] getBytes(long parameter0, int parameter1) throws java.sql.SQLException {
		checkActive();
		byte[] answer = myRawResourceComponent().getBytes(parameter0, parameter1);
		return answer;
	}

	public long position(byte[] parameter0, long parameter1) throws java.sql.SQLException {
		checkActive();
		long answer = myRawResourceComponent().position(parameter0, parameter1);
		return answer;
	}

	public long position(java.sql.Blob parameter0, long parameter1) throws java.sql.SQLException {
		checkActive();
		long answer = myRawResourceComponent().position(parameter0, parameter1);
		return answer;
	}

	public java.io.InputStream getBinaryStream() throws java.sql.SQLException {
		checkActive();
		java.io.InputStream answer = myRawResourceComponent().getBinaryStream();
		return answer;
	}

	public int setBytes(long parameter0, byte[] parameter1) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().setBytes(parameter0, parameter1);
		return answer;
	}

	public int setBytes(long parameter0, byte[] parameter1, int parameter2, int parameter3) throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().setBytes(parameter0, parameter1, parameter2, parameter3);
		return answer;
	}

	public java.io.OutputStream setBinaryStream(long parameter0) throws java.sql.SQLException {
		checkActive();
		java.io.OutputStream answer = myRawResourceComponent().setBinaryStream(parameter0);
		return answer;
	}

	public void truncate(long parameter0) throws java.sql.SQLException {
		checkActive();
		myRawResourceComponent().truncate(parameter0);
	}

}
