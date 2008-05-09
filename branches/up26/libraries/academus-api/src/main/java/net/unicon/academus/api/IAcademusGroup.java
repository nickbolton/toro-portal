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

package net.unicon.academus.api;

import net.unicon.academus.api.AcademusFacadeException;
import net.unicon.academus.api.IAcademusUser;

public interface IAcademusGroup {

	public static final String GROUP_NAME_BASE_PATH_SEPARATOR = " - ";

	/**
     * Returns the name of the group.
     *
     * @return The name of this group.
     * @throws AcademusFacadeException
	 */
	public String getName() throws AcademusFacadeException;

	/**
	 * Returns the key of the group.
	 *
	 * @return The key of this group.
	 */
	public String getKey() throws AcademusFacadeException;

	/**
	 * Sets the name of this group.
	 *
	 * @param name The game of the group.
	 * @throws AcademusFacadeException
	 */
	public void setName(String name) throws AcademusFacadeException;

	/**
	 * Returns the descendant groups directly under this group.
	 *
	 * @return The groups directly under this group.
	 * @throws AcademusFacadeException
	 */
	public IAcademusGroup[] getDescendantGroups() throws AcademusFacadeException;

	/**
	 * Returns all the descendant groups of the specified group. It will recursively
	 * retrieve all groups under the specified group. No duplicate IAcademusGroup
	 * instances are returned even though the same group may appear in more than
	 * one places in the hierarchy.
     *
	 * @return The groups directly under this group.
	 * @throws AcademusFacadeException
	 */
	public IAcademusGroup[] getAllDescendantGroups() throws AcademusFacadeException;

	/**
	 * Returns the users contained directly under this group.
	 *
	 * @return The users directly under this group.
	 * @throws AcademusFacadeException
	 */
	public IAcademusUser[] getContainedUsers() throws AcademusFacadeException;

	/**
	 * Returns all the users contained under this group and all sub-groups. It
	 * will traverse all groups under the specified group and include those
	 * users in the results. No duplicate IAcademusUser instances are returned
	 * even though the same user may be contained in more than one locations
	 * under the specified group.
	 *
	 * @return The users under this group and all subgroups.
	 * @throws AcademusFacadeException
	 */
	public IAcademusUser[] getAllContainedUsers() throws AcademusFacadeException;

	/**
	 * Determines whether the user specified by the username is contained
	 * within this group. It will recursively traverse all groups under the
	 * this group until the entire group hierarchy is examined or the user
	 * is found.
	 *
	 * @param username The username of the user to search for.
   	 * @return Returns true if the user is contained in this group, false otherwise.
	 * @throws AcademusFacadeException
	 */
    public boolean containsUser (String username) throws AcademusFacadeException;

    /**
     * Retrieves this group's paths as an array of strings. The paths represent
     * the hierarchical base paths of this AcademusGroup instance. The paths
     * consist of group names that are separated by delimiter Strings.
     * If the delimiter parameter is null, it will default to the
     * AcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR separator.
     * @param delimiter A delimiter that separates the path nodes.
     * @param excludeThisNode If set, this will exclude this node from the
     * path results.
     * @return The group paths as a String array.
     */
    public String[] getGroupPaths (String delimiter, boolean excludeThisNode) throws AcademusFacadeException;
}

