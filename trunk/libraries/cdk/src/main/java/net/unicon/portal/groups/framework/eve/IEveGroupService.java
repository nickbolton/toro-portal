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

import java.util.Iterator;

import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IIndividualGroupService;
import org.jasig.portal.groups.ILockableGroupService;

/**
 * @author nbolton
 * 
 * This extends uPortal's <code>IIndividualGroupService</code> and
 * <code>ILockableGroupService</code> interfaces and is the main
 * interface for the Eve group service. Eve group members will, 
 * when querried, delegate membership querries back to the group service.
 * 
 */
public interface IEveGroupService
extends IIndividualGroupService, ILockableGroupService {
    
    /**
     * Add a group member to a group.
     * 
     * @param group a <code>IEntityGroup</code> object
     * @param gm a <code>IGroupMember</code> object
     * @throws GroupsException
     */
    public void addMember(IEntityGroup group, IGroupMember gm)
    throws GroupsException;
    
    /**
     * Remove a group member to a group.
     * 
     * @param group a <code>IEntityGroup</code> object
     * @param gm a <code>IGroupMember</code> object
     * @throws GroupsException
     */
    public void removeMember(IEntityGroup group, IGroupMember gm)
    throws GroupsException;
    
    /**
     * Returns all the containing group to which the group member belongs.
     * This is a recursive query that returns the group member's direct parents,
     * their parents, and so on...
     * 
     * @param member a <code>IGroupMember</code> object
     * @return an iterator for all the containing groups.
     * @throws GroupsException
     */
	public Iterator getAllContainingGroups(IGroupMember member)
    throws GroupsException;
    
    /**
     * Returns all the entities (leaf nodes) that are children of the group.
     * This is a recursive query that returns the group's direct children,
     * their children, and so on...
     * 
     * @param group a <code>IEntityGroup</code> object
     * @return an iterator for all the leaf node entities.
     * @throws GroupsException
     */
	public Iterator getAllEntities(IEntityGroup group) throws GroupsException;
    
    /**
     * Returns all the members that are children of the group. This includes
     * leaf nodes and other groups. This is a recursive query that returns
     * the group's direct children, their children, and so on...
     * 
     * @param group a <code>IEntityGroup</code> object
     * @return an iterator for all the members.
     * @throws GroupsException
     */
	public Iterator getAllMembers(IEntityGroup group) throws GroupsException;
    
    /**
     * Returns the containing group to which the group member belongs
     * directly.
     * 
     * @param member a <code>IGroupMember</code> object
     * @return an iterator for the containing groups.
     * @throws GroupsException
     */
	public Iterator getContainingGroups(IGroupMember member) throws GroupsException;
    
    /**
     * Returns the entities (leaf nodes) that are direct children of the group.
     * 
     * @param group a <code>IEntityGroup</code> object
     * @return an iterator for the leaf node entities.
     * @throws GroupsException
     */
	public Iterator getEntities(IEntityGroup group) throws GroupsException;
    
    /**
     * Returns the members that are direct children of the group. This includes
     * leaf nodes and other groups.
     * 
     * @param group a <code>IEntityGroup</code> object
     * @return an iterator for the members.
     * @throws GroupsException
     */
	public Iterator getMembers(IEntityGroup group) throws GroupsException;
    
    /**
     * Returns a member that is a group with the given name.
     * 
     * @param group a <code>IEntityGroup</code> object
     * @param name the sub-group name
     * @return an <IEntityGroup> object if there exists a group that is a
     * direct member of the given group whose name matches the given
     * name.
     * @throws GroupsException
     */
	public IEntityGroup getMemberGroupNamed(IEntityGroup group,
        String name) throws GroupsException;
    
    /**
     * Returns whether a group contains any members.
     * 
     * @param group a <code>IEntityGroup</code> object
     * @return true if the group has any groups or leaf nodes as direct
     * members and false otherwise.
     * @throws GroupsException
     */
	public boolean hasMembers(IEntityGroup group)
    throws GroupsException;
    
    /**
     * Returns whether a group member is directly or indirectly (recursively)
     * a member of another group.
     * 
     * @param member a <code>IGroupMember</code> object
     * @param group a <code>IEntityGroup</code> object
     * @return true if <param>group</param> contains
     * <param>member</param> or if any group that is recursively a
     * member of <param>group</param> contains
     * <param>member</param> and false otherwise.
     * @throws GroupsException
     */
	public boolean isDeepMemberOf(IGroupMember member,
        IGroupMember group) throws GroupsException;
    
    /**
     * Returns whether a group member is directly a member of another group.
     * @param member a <code>IGroupMember</code> object
     * @param group a <code>IEntityGroup</code> object
     * @return true if <param>group</param> directly contains
     * <param>member</param> and false otherwise.
     * @throws GroupsException
     */
	public boolean isMemberOf(IGroupMember member,
        IGroupMember group) throws GroupsException;
}
