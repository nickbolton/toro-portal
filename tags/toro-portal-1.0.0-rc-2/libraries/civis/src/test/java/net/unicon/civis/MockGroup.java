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

package net.unicon.civis;

import net.unicon.penelope.IDecisionCollection;

public class MockGroup implements IGroup {
	private String name;
	
	public MockGroup(String name) {
		this.name = name;
	}

	public IPerson[] getMembers(boolean deep) {
		// TODO Auto-generated method stub
		return null;
	}

	public IGroup[] getSubgroups() {
		// TODO Auto-generated method stub
		return null;
	}

	public IGroup[] getDescendentGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPathAndId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	public ICivisFactory getOwner() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return name;
	}

	public IDecisionCollection getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	public String toXml() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}
}
