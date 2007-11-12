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

import net.unicon.academus.domain.lms.User;

import java.util.List;

import org.w3c.dom.Document;

public interface UniconGroupService {

    /**
     * Return an array of all user entity IGroup objects
     * (wrappers to uPortal IEntityGroup objects).
     * @return The list of IEntityGroup objects
     * @throws UniconGroupsException
     */
    public IGroup[] fetchAllGroupsArray()
    throws UniconGroupsException;

    /**
     * Return a Document of IGroup objects 
     * for all the user groups.
     * @return Document representing the parent group membership hierarchy.
     * @throws UniconGroupsException
     */
    public Document fetchAllGroupsDocument()
    throws UniconGroupsException;

    /**
     * Return the array of IGroup objects 
     * that the target member belongs to.
     * @param member the group member target.
     * @return The list of IGroup objects
     */
    public IGroup[] fetchUserGroupsArray(IMember member)
    throws UniconGroupsException;

    /**
     * Return the root user group.
     * @return the root IGroup object.
     */
    public IGroup getRootGroup()
    throws UniconGroupsException;

    /**
     * Return the user group that contains administrators.
     * @return the root IGroup object.
     */
    public IGroup getAdministratorGroup()
    throws UniconGroupsException;

    /**
     * Return the group with the given path relative to the root group.
     * @param path a relative path
     * @return the target IGroup object.
     */
    public IGroup getGroupByPath(String[] path)
    throws UniconGroupsException;

    /**
     * Creates a new group.
     * @param creator the user that is creating the new group.
     * @param parentGroup the IGroup object that will be the new group's parent.
     * @param name the name of the new group.
     * @param description a description of the new group.
     * @return The new IGroup object
     */
    public IGroup createGroup(User creator, IGroup parentGroup,
        String name, String description)
    throws UniconGroupsException;

    /**
     * Clones all specified groups that have multiple group paths so that each instance has
     * a unique preferred group path. For groups that do not have multiple
     * group paths the group is returned as is with its preferred path set as the only
     * group path it has.
     *
     * @param groups An array of IGroup[] instances.
     * @param delim A delimiter that separates the path nodes in the preferred group paths of each IGroup instance.
     * @param excludeThisNode If set, this will exclude the lowest child groups from the
     * preferred group paths of each IGroup instance.
     *
     * @return An IGroup array where each instance has a different preferred path.
     */
    public IGroup[] getPreferredPathGroups(IGroup[] groups, String delim, boolean excludeThisNode)
    throws UniconGroupsException;

}
