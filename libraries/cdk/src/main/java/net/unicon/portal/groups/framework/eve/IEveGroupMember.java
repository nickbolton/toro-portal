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

import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IGroupMember;

/**
 * @author nbolton
 *
 * This extends <code>IGroupMember</code> to allow group members
 * to respond to membership querries by asking the group service.
 */
public interface IEveGroupMember extends IGroupMember {
    
    /**
     * Returns the <code>IEveGroupService</code> to which this group
     * member belongs.
     * 
     * @return the group service.
     */
	public IEveGroupService getEveGroupService();
    
    /**
     * Set the group service to which this member should belong.
     * 
     * @param gs a <code>IEveGroupService</code> object.
     * @throws GroupsException if the group service object is null.
     */
	public void setEveGroupService(IEveGroupService gs)
    throws GroupsException;
}
