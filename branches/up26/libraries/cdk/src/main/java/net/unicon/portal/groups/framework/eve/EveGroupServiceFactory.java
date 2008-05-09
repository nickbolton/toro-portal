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
import org.jasig.portal.groups.IComponentGroupService;
import org.jasig.portal.groups.IComponentGroupServiceFactory;

/**
 * Creates an instance of an <code>IEveGroupService</code> object.
 */

public class EveGroupServiceFactory implements IComponentGroupServiceFactory {

    /**
     * EveGroupServiceFactory constructor.
     */
    public EveGroupServiceFactory() {
        super();
    }

    /**
     * Return an instance of the service implementation.
     * 
     * @return IIndividualGroupService
     * @exception GroupsException
     */
    public IComponentGroupService newGroupService() throws GroupsException {
        return newGroupService(new ComponentGroupServiceDescriptor());
    }

    /**
     * Return an instance of the service implementation.
     * 
     * @return IIndividualGroupService
     * @exception GroupsException
     */
    public IComponentGroupService newGroupService(
        ComponentGroupServiceDescriptor svcDescriptor) throws GroupsException {
        try {
            return EveGroupService.instance(svcDescriptor);
        } catch (GroupsException ge) {
            Utils.logTrace(ge);
            throw new GroupsException(ge.getMessage());
        }
    }
}
