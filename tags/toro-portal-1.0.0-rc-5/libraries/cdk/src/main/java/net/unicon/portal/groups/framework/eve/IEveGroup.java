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

import javax.naming.Name;

import org.jasig.portal.groups.CompositeEntityIdentifier;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;

/**
 * Extends <code>IEntityGroup</code> to facilitate group membership
 * management within the service.
 */
public interface IEveGroup extends IEntityGroup {
    
    /**
     * Returns the actual object to be cached. <code>LockableGroup</code>
     * objects delegate to <code>Group</code> objects and only
     * the Group objects should be cached.
     * 
     * @return a cachable group object. 
     */
    public IEveGroup getCacheableGroup();
    
    /**
     * Returns whether the group has dirty memberships.
     * 
     * @return true if the group has deleted members or added members
     * and false otherwise.
     */
    public boolean hasDirtyMembers();
    
    /**
     * Returns whether the group has deleted members.
     * 
     * @return true if the group has deleted members and false otherwise.
     */
    public boolean hasDeletes();
    
    /**
     * Returns whether the group has added members.
     * 
     * @return  true if the group has added members and false otherwise.
     */
    public boolean hasAdds();
    
    /**
     * Returns an array of <code>IGroupMember</code> objects that 
     * represent the group members that have been removed since the
     * last update.
     * 
     * @return an array of <code>IGroupMember</code> objects
     */
    public IGroupMember[] getRemovedMembers();
    
    /**
     * Returns an array of <code>IGroupMember</code> objects that 
     * represent the group members that have been added since the
     * last update.
     * 
     * @return an array of <code>IGroupMember</code> objects
     */
    public IGroupMember[] getAddedMembers();
    
    /**
     * Convenience method to allow setting the service name based on a String.
     * This method creates a Name and passes it to setServiceName(Name) to 
     * actually set the service Name to which this group belongs.
     * @param newServiceName
     * @throws GroupsException if the service name not able to be parsed.
     * @see #setServiceName(Name)
     */
    public void setServiceName(String newServiceName)
    throws GroupsException;
    
    /**
     * Sets the service name to which this group belongs.
     * @param newServiceName
     * @throws GroupsException if the service name not able to be parsed.
     */
    public void setServiceName(Name newServiceName)
    throws GroupsException;
    
    /**
     * Returns the composite entity identifier that identifies this group.
     * @return the <code>CompositeEntityIdentifier</code>
     */
    public CompositeEntityIdentifier getCompositeEntityIdentifier();
}
