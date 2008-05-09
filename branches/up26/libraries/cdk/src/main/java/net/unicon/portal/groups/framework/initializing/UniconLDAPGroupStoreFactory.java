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

package net.unicon.portal.groups.framework.initializing;

import org.jasig.portal.groups.*;
import org.jasig.portal.services.LogService;

/**
 * Returns an instance of the ldap <code>IEntityGroupStore</code>.
 * @author Nick Bolton
 */

public class UniconLDAPGroupStoreFactory implements IEntityGroupStoreFactory {
	protected static UniconLDAPGroupStore groupStore;
/**
 * ReferenceGroupServiceFactory constructor.
 */
public UniconLDAPGroupStoreFactory() {
    super();
}
/**
 * @return org.jasig.portal.groups.ldap.LDAPGroupStore
 */
protected static synchronized UniconLDAPGroupStore getGroupStore() 
{
    if ( groupStore == null )
        { groupStore = new UniconLDAPGroupStore(); }
    return groupStore;
}
/**
 * Return an instance of the group store implementation.
 * @return IEntityGroupStore
 * @exception GroupsException
 */
public IEntityGroupStore newGroupStore() throws GroupsException
{
    return newInstance();
}
/**
 * Return an instance of the group store implementation.
 * @return IEntityGroupStore
 * @exception GroupsException
 */
public IEntityGroupStore newInstance() throws GroupsException
{
    try
        { return getGroupStore(); }
    catch ( Exception ex )
    {
        LogService.log (LogService.ERROR, "UniconLDAPGroupStoreFactory.newInstance(): " + ex);
        throw new GroupsException(ex.getMessage());
    }
}
public IEntityGroupStore newGroupStore(ComponentGroupServiceDescriptor svcDescriptor)
throws GroupsException
{
    return newInstance();
}


}
