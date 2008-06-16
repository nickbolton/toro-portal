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

import java.util.Map;

import org._3pq.jgrapht.DirectedGraph;

import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.ILockableEntityGroup;

/**
 * This extends the uPortal <code>IEntityGroupStore</code> interface
 * and provides the ability to load and organize the group structure
 * into a graph implementation provided by jgrapht.
 * 
 * @author nbolton
 */
public interface IEveGroupStore extends IEntityGroupStore {
    
    /**
     * Returns the entire group structure.
     * 
     * @param groups A map containing all groups (as returned from getGroups())
     * @return a <code>DirectedGraph</code> object that
     * contains all the groups and entities as vertices and all the
     * membership knowledge as its edges.
     * @throws GroupsException
     */
	public DirectedGraph getMemberships(Map groups) throws GroupsException;

    /**
     * Return a map containing all groups.
     *
     * @return a <code>Map</code> of all groups, mapped by group key to
     * <code>IEntityGroup</code> object.
     * @throws Exception
     */
	public Map getGroups() throws Exception;
    
    /**
     * Returns the member relationships for a particular group.
     * 
     * @param group a <code>IEntityGroup</code> object
     * @return all the member keys wrapped in a
     * <code>MembershipKeys</code> object that belong to this group  
     * @throws GroupsException
     */
	public MembershipKeys getAllMemberships(IEntityGroup group)
    throws GroupsException;
    
    /**
     * Creates a lockable instance of a group.
     * 
     * @param newKey
     * @param newType
     * @param newCreatorID
     * @param newName
     * @param newDescription
     * @return
     * @throws GroupsException
     */
	public ILockableEntityGroup lockableInstance(String newKey,
        Class newType, String newCreatorID, String newName,
        String newDescription) throws GroupsException;
}
