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

package net.unicon.academus.api;

import java.util.Map;

import net.unicon.academus.api.AcademusFacadeException;

public interface IAcademusUser {


	/**
	 * Returns the user's username.
	 *
	 * @return The user's username.
	 * @throws AcademusFacadeException.
	 */
	public String getUsername() throws AcademusFacadeException;

	/**
	 * Returns the user's id.
	 *
	 * @return The user's id.
	 * @throws AcademusFacadeException.
	 */
	public long getId() throws AcademusFacadeException;

	/**
	 * Returns the user's first name.
	 *
	 * @return The user's first name.
	 * @throws AcademusFacadeException.
	 */
	public String getFirstName() throws AcademusFacadeException;

	/**
	 * Returns the user's last name.
	 *
	 * @return The user's last name.
	 * @throws AcademusFacadeException.
	 */
	public String getLastName() throws AcademusFacadeException;

	/**
	 * Sets the user's first name.
	 *
	 * @param firstName The user's first name.
	 * @throws AcademusFacadeException.
	 */
	public void setFirstName (String firstName) throws AcademusFacadeException;

	/**
	 * Sets the user's last name.
	 *
	 * @param lastName The user's last name.
	 * @throws AcademusFacadeException.
	 */
	public void setLastName (String lastName) throws AcademusFacadeException;
	
	/**
	 * Gets the user Attributes based on the key
	 *
	 * @param key The key for the users attribute.
	 * @throws AcademusFacadeException.
	 */
	public String getAttribute(String key) throws AcademusFacadeException;
	
	/**
	 * Gets a map of all user attributes.
	 *
         * @return a Map&lt;String, String&gt; of user attributes.
	 * @throws AcademusFacadeException
	 */
	public Map getAttributes() throws AcademusFacadeException;
}

