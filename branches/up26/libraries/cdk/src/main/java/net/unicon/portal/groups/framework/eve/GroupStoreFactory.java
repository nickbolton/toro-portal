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

package net.unicon.portal.groups.framework.eve;

import org.jasig.portal.groups.ComponentGroupServiceDescriptor;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroupStore;

/**
 * Creates an instance of the reference <code>IEntityGroupStore</code>.
 */

public class GroupStoreFactory implements IEveGroupStoreFactory {

	/**
	 * GroupStoreFactory constructor.
	 */
	public GroupStoreFactory() {
		super();
	}

	/**
	 * Return an instance of the group store implementation.
	 * 
	 * @return IEntityGroupStore
	 * @exception GroupsException
	 */
	public IEntityGroupStore newGroupStore() throws GroupsException {
		return newInstance("local");
	}

	public IEntityGroupStore newGroupStore(
			ComponentGroupServiceDescriptor svcDescriptor)
			throws GroupsException {
		return newInstance(svcDescriptor.getName());
	}

	/**
	 * Return an instance of the group store implementation.
	 * 
	 * @return IEntityGroupStore
	 * @exception GroupsException
	 */
	public IEntityGroupStore newInstance(String serviceName)
			throws GroupsException {
		try {
			return EveGroupStore.singleton(serviceName);
		} catch (Exception ex) {
		    Utils.logTrace(ex);
			throw new GroupsException(ex.getMessage());
		}
	}

}
