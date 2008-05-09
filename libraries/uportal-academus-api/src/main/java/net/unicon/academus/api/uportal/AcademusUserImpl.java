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

package net.unicon.academus.api.uportal;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.security.IPerson;

import net.unicon.academus.api.AcademusFacadeException;
import net.unicon.academus.api.IAcademusFacade;
import net.unicon.academus.api.IAcademusUser;

public class AcademusUserImpl implements IAcademusUser {

	// Instance Members.
	private final IPerson person;
	private final IAcademusFacade owner;

	/*
	 * Public API.
	 */

	public AcademusUserImpl(IPerson person, IAcademusFacade owner) {
		
		// Assertions.
		if (person == null) {
			String msg = "Argument 'person' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (owner == null) {
			String msg = "Argument 'owner' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		// Instance Members.
		this.person = person; 
		this.owner = owner;
		
	}

	public String getUsername() throws AcademusFacadeException {
		return (String) person.getAttribute(IPerson.USERNAME);
	}

	public long getId() throws AcademusFacadeException {
		return person.getID();
	}

	public String getFirstName() throws AcademusFacadeException {
		String rslt = (String) person.getAttribute("user.name.given");
		if (rslt == null) {
			rslt = "[first name unknown]";
		}
		return rslt;
	}

	public String getLastName() throws AcademusFacadeException {
		String rslt = (String) person.getAttribute("user.name.family");
		if (rslt == null) {
			rslt = "[first name unknown]";
		}
		return rslt;
	}

	public void setFirstName (String firstName) throws AcademusFacadeException {
		throw new UnsupportedOperationException();
	}

	public void setLastName (String lastName) throws AcademusFacadeException {
		throw new UnsupportedOperationException();
	}
	
	public String getAttribute(String key) throws AcademusFacadeException {
		return (String) person.getAttribute(key);
	}
	
	public Map getAttributes() throws AcademusFacadeException {
		Map attrs = new HashMap();
		for (Enumeration en = person.getAttributeNames(); en.hasMoreElements();) {
			String key = (String) en.nextElement();
			attrs.put(key, person.getAttribute(key));
		}
		return attrs;
	}

}