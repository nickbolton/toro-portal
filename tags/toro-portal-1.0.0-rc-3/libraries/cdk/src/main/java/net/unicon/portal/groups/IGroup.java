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
package net.unicon.portal.groups;

import net.unicon.academus.domain.IDomainEntity;

import org.jasig.portal.groups.IEntityGroup;

import org.w3c.dom.Document;
import java.util.List;

/**
 * This interface defines the access to a the group object.
 */
public interface IGroup extends IDomainEntity {

    public static final String GROUP_NAME_BASE_PATH_SEPARATOR = " - ";

    /**
     * Retrieves an unique identifier for this group object.
     * @return The unique group identifier.
     */
    public long getGroupId();

    /**
     * Retrieves the group key. The group key is the group_id prefixed
     * with a Composite groups service name.
     * @return A unique group key
     */
    public String getKey();
    
    /**
     * Retrieves the group type. The group type represents some information
     * about the group.
     * @return A unique group type
     */
    public String getType();

    /**
     * Retrieves this group's parent groups.
     * @return List of parent groups.
     * @throws UniconGroupsException
     */
    public List getParents() throws UniconGroupsException;

    /**
     * Retrieves this group's parent keys. 
     * The group key is the group_id prefixed
     * with a Composite groups service name.
     * @return A list of unique keys
     */
    public List getParentKeys() throws UniconGroupsException;

    /**
     * Retrieves this group's paths as an array of strings. The paths represent
     * the hierarchical base paths of this IGroup instance. The paths
     * consist of group names that are separated by delim Strings.
     * If the delim parameter is null, it will default to the
     * IGroup.GROUP_NAME_BASE_PATH_SEPARATOR separator.
     * @param delim A delimiter that separates the path nodes.
     * @param excludeThisNode If set, this will exclude this node from the
     * results.
     * @return The group paths as a String array.
     */
    public String[] getPathsAsStrings(String delim, boolean excludeThisNode)
    throws UniconGroupsException;

    /**
     * Retrieves this group's direct descendent members.
     * @return <code>List</code>
     * @throws UniconGroupsException
     */
    public List getDescendantMembers() throws UniconGroupsException;

    /**
     * Retrieves this group's direct descendent groups.
     * @return <code>List</code>
     * @throws UniconGroupsException
     */
    public List getDescendantGroups() throws UniconGroupsException;

    /**
     * Retrieves all of this group's recursive descendent groups.
     * @return <code>List</code>
     * @throws UniconGroupsException
     */
    public List getAllDescendantGroups() throws UniconGroupsException;

    /**
     * Builds a <code>Document</code> the represents the group structure
     * from this group on down.
     * <code>Document</code>.
     * @param recurse A true value will build the document recursively. A false
     * value only returns this and the direct descendants.
     * @return <code>Document</code>
     * @throws UniconGroupsException
     */
    public Document buildDocument(boolean rescurse)
    throws UniconGroupsException;    

    /**
     * Retrieves the name for this group.
     * @return The name for this group.
     */
    public String getName();

    /**
     * Sets the name for this group.
     * @param name The new name.
     * @throws UniconGroupsException
     */
    public void setName(String name) throws UniconGroupsException;

    /**
     * Retrieves the creator of the group.
     * @return The creator
     */
    public String getCreatorId();

    /**
     * Sets the creator of the group
     * @param creatorId An identifier for the creator of the group
     * @throws UniconGroupsException
     */
    public void setCreatorId(String creatorId) throws UniconGroupsException;

    /**
     * Retrieves the description for this group.
     * @return The description for this group.
     */
    public String getDescription();
    
    /**
     * Sets the description for this group.
     * @param description The new description for this group.
     * @throws UniconGroupsException
     */
    public void setDescription(String description) throws UniconGroupsException;

    /**
     * Sets the preferred group path for this group.
     * @param preferredPath The preferred group path for this group.
     */
    public void setPreferredPath(String preferredPath);

    /**
     * Retrieves the preferred group path for this group.
     * @return The preferred group path for this group.
     */
    public String getPreferredPath();

    /**
     * Adds a member to this group.
     * @param member The member to add.
     * @throws UniconGroupsException
     */
    public void addMember(IMember member) throws UniconGroupsException;

    /**
     * Removes a member from this group.
     * @param member The member to remove.
     * @throws UniconGroupsException
     */
    public void removeMember(IMember member) throws UniconGroupsException;

    /**
     * Add a descendent group to this group
     * @param newDescendent
     * @throws UniconGroupsException
     */
    public void addGroup(IGroup newDescendent) throws UniconGroupsException;

    /**
     * Remove a descendent group from this group
     * @param descendent The group to remove.
     * @throws UniconGroupsException
     */
    public void removeGroup(IGroup descendent) throws UniconGroupsException;

    /**
     * Store the group object in persistent storage.
     * @throws UniconGroupsException
     */
    public void update() throws UniconGroupsException;

    /**
     * Delete this group object from persistent storage.
     * @throws UniconGroupsException
     */
    public void delete() throws UniconGroupsException;

    /**
     * Retrieves the associated <code>IEntityGroup</code> object.
     * @return The entity group object.
     */
    public IEntityGroup getEntityGroup() throws UniconGroupsException;

    /**
     * Checks whether the member object is a member of this group.
     * @return boolean
     */
    public boolean contains(IMember member) throws UniconGroupsException;

    public boolean contains(String memberName) throws UniconGroupsException;

    public IGroup getMemberGroup(String groupName) throws UniconGroupsException;

    /**
     * Checks whether this object has been created successfully.
     * @return boolean
     */
    public boolean ok();

    /**
     * Returns a <code>String</code> representation of the XML that 
     * describes this group object.
     * @return The XML representing this group object.
     */
    public String toXML();
}
