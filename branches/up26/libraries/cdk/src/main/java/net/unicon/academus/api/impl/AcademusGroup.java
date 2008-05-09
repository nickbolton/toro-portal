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

package net.unicon.academus.api.impl;

import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;

import net.unicon.academus.api.AcademusFacadeException;
import net.unicon.academus.api.IAcademusGroup;
import net.unicon.academus.api.IAcademusUser;
import net.unicon.academus.common.PersonDirectoryService;
import net.unicon.academus.common.PersonDirectoryServiceFactory;
import net.unicon.academus.domain.lms.User;


import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.IMember;
import net.unicon.portal.groups.MemberFactory;
import net.unicon.portal.groups.UniconGroupsException;


public class AcademusGroup implements IAcademusGroup {

    private IGroup group = null;

    /**
     * AcademusGroup constructor.
     */
    public AcademusGroup (IGroup group) {

        this.group = group;
    }

    /**
     * Returns the name of the group.
     *
     * @return The name of this group.
     */
    public String getName() {

         return this.group.getName();
    }

    /**
     * Returns the id  of the group.
     *
     * @return The id of this group.
     */
    public long getId() {

        return this.group.getGroupId();
    }

    /**
     * Returns the key of the group.
     *
     * @return The key of this group.
     */
    public String getKey() {

        return this.group.getKey();
    }

    /**
     * Sets the name of this group.
     *
     * @param name The game of the group.
     * @throws AcademusFacadeException
     */
    public void setName(String name) throws AcademusFacadeException {

        try {

            this.group.setName(name);

        } catch (UniconGroupsException uge) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("AcademusGroup:setName():");
            errorMsg.append("An error occured while setting a group's name");

            throw new AcademusFacadeException(errorMsg.toString(), uge);
        }
    }

    /**
     * Returns the groups contained directly under this group.
     *
     * @return The groups directly under this group.
     * @throws AcademusFacadeException
     */
    public IAcademusGroup[] getDescendantGroups () throws AcademusFacadeException {

        IGroup currentGroup = null;

        IAcademusGroup[] academusGroups = null;

        List groups = null;

        try {

            groups = this.group.getDescendantGroups();

        } catch (UniconGroupsException uge) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("AcademusGroup:getDescendantGroups():");
            errorMsg.append("An error occured while retrieving contained groups of ");
            errorMsg.append("the group with id:" + this.group.getGroupId());

            throw new AcademusFacadeException(errorMsg.toString(), uge);
        }

        academusGroups = new IAcademusGroup[groups.size()];

        for (int index = 0; index < groups.size(); index++) {

            currentGroup = (IGroup) groups.get(index);

            academusGroups[index] = new AcademusGroup(currentGroup);
        }

        return academusGroups;
    }


    /**
     * Returns all the descendant groups of the specified group. It will recursively
     * retrieve all groups under the specified group. No duplicate IAcademusGroup
     * instances are returned even though the same group may appear in more than
     * one places in the hierarchy.
     *
     * @return The groups directly under this group.
     * @throws AcademusFacadeException
     */
    public IAcademusGroup[] getAllDescendantGroups() throws AcademusFacadeException {

        IGroup currentGroup = null;
        String currentId = null;
        IAcademusGroup[] academusGroups = null;
        List academusGroupList = null;
        List groups = null;
        Set groupIdSet = null;

        try {

            groups = this.group.getAllDescendantGroups();

        } catch (UniconGroupsException uge) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("AcademusGroup:getAllDescendantGroups():");
            errorMsg.append("An error occured while retrieving all contained groups of ");
            errorMsg.append("the group with id:" + this.group.getGroupId());

            throw new AcademusFacadeException(errorMsg.toString(), uge);
        }
        groupIdSet = new HashSet(groups.size()*4/3);

        academusGroupList = new LinkedList();

        for (int index = 0; index < groups.size(); index++) {

            currentGroup = (IGroup) groups.get(index);
            currentId = Long.toString(currentGroup.getGroupId());

            if (!groupIdSet.contains(currentId)) {
                academusGroupList.add(new AcademusGroup(currentGroup));
                groupIdSet.add(currentId);
            }
        }

        academusGroups = (IAcademusGroup[]) academusGroupList.toArray(new AcademusGroup[0]);

        return academusGroups;
    }

    /**
     * Returns the users contained directly under this group.
     *
     * @return The users directly under this group.
     * @throws AcademusFacadeException
     */
    public IAcademusUser[] getContainedUsers() throws AcademusFacadeException {

        IAcademusUser[] academusUsers = null;
        List users = null;

        try {

            users = group.getDescendantMembers();
            if (users != null && !users.isEmpty()) {
                academusUsers = getAcademusUsers(users);
            } else {
                academusUsers = new IAcademusUser[0];
            }

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("AcademusGroup:getContainedUsers():");
            errorMsg.append("An error occured while retrieving contained users of ");
            errorMsg.append("the group with id:" + this.group.getGroupId());

            throw new AcademusFacadeException(errorMsg.toString(), e);
        }

        return academusUsers;
    }


    // Converts a list of IMember instances to an array of
    // IAcademusUser instances
    private IAcademusUser[] getAcademusUsers (List members)
    throws Exception {

        int memberCount = members.size();

        String[] usernames = new String[memberCount];

        for (int index = 0; index < memberCount; index++) {

            usernames[index] = ((IMember) members.get(index)).getKey();
        }

        return getAcademusUsers(usernames);
    }

    // Converts an array of usernames to an array with their corresponding
    // IAcademusUser instances
    private IAcademusUser[] getAcademusUsers (String[] usernames) throws Exception {

        // Assertions.
        if (usernames == null) {
            String msg = "Argument 'usernames' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Get the list of users from the portal.
        List users = PersonDirectoryServiceFactory.getService()
            .find(usernames, null, null, null, null, true);

        // Convert to AcademusUser instances.
        IAcademusUser[] rslt = new IAcademusUser[users.size()];
        for (int i=0; i < users.size(); i++) {
            User u = (User) users.get(i);
            rslt[i] = new AcademusUser(u);
        }

        return rslt;

    }

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
    public IAcademusUser[] getAllContainedUsers() throws AcademusFacadeException {

        List groups =null;
        List users = null;
        IGroup currentGroup = null;
        String username = null;
        Set usernameSet = new HashSet();
        IAcademusUser[] academusUsers = null;
        String[] usernames = null;

        try {

            groups = this.group.getAllDescendantGroups();

            for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {

                currentGroup = (IGroup) groups.get(groupIndex);

                users = currentGroup.getDescendantMembers();

                for (int userIndex = 0; userIndex < users.size(); userIndex++) {

                    username = ((IMember) users.get(userIndex)).getKey();
                    usernameSet.add(username);
                }
            }
            
            // add all the users under the given group
            users = this.group.getDescendantMembers();
            for (int userIndex = 0; userIndex < users.size(); userIndex++) {

                username = ((IMember) users.get(userIndex)).getKey();
                usernameSet.add(username);
            }

            usernames = (String[]) usernameSet.toArray(new String[0]);

            academusUsers = getAcademusUsers(usernames);

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("AcademusGroup:getAllContainedUsers():");
            errorMsg.append("An error occured while retrieving contained users of ");
            errorMsg.append("the group with id:" + this.group.getGroupId());

            throw new AcademusFacadeException(errorMsg.toString(), e);
        }

        return academusUsers;
    }

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
    public boolean containsUser (String username) throws AcademusFacadeException {

        boolean contains = false;

        try {

            IMember member = MemberFactory.getMember(username);

            contains = this.group.contains(member);

            return contains;

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("AcademusGroup:containsUser():");
            errorMsg.append("An error occured while determining if user ");
            errorMsg.append(username);
            errorMsg.append(" is a member of the group with id:" + this.group.getGroupId());

            throw new AcademusFacadeException(errorMsg.toString(), e);
        }
    }

    /**
     * Retrieves this group's paths as an array of strings. The paths represent
     * the hierarchical base paths of this AcademusGroup instance. The paths
     * consist of group names that are separated by delimiter Strings.
     * If the delimiter parameter is null, it will default to the
     * AcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR separator.
     * @param delim A delimiter that separates the path nodes.
     * @param excludeThisNode If set, this will exclude this node from the
     * path results.
     * @return The group paths as a String array.
     */
    public String[] getGroupPaths (String delimiter, boolean excludeNode)
    throws AcademusFacadeException {

        try {

            if (delimiter == null) {

                delimiter = IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR;
            }

            String[] groupPaths = this.group.getPathsAsStrings(delimiter, excludeNode);

            return groupPaths;

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("AcademusGroup:getGroupPaths():");
            errorMsg.append("An error occured while retrieving paths ");
            errorMsg.append(" of the group with id:" + this.group.getGroupId());

            throw new AcademusFacadeException(errorMsg.toString(), e);
        }
    }
}

