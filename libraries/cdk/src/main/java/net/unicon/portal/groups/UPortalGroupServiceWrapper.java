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
import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.util.XmlUtils;

import org.jasig.portal.services.LogService;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.DocumentFactory;

import java.util.Vector;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class UPortalGroupServiceWrapper implements UniconGroupService {

    /**
     * Return an array of all user entity IGroup objects
     * (wrappers to uPortal IEntityGroup objects).
     * @return The list of IEntityGroup objects
     * @throws UniconGroupsException if the uPortal <code>IGroupMember</code>
     * object fails retrieving the groups containing the given member.
     */
    public IGroup[] fetchAllGroupsArray()
    throws UniconGroupsException {
        List groups = new ArrayList();
        IGroup root = getRootGroup();

        groups.add(root);
        groups.addAll(root.getAllDescendantGroups());
        return (IGroup[])groups.toArray(new IGroup[0]);
    }

    /**
     * Return a Document of IGroup objects 
     * for all the user groups.
     * @return Document representing the parent group membership hierarchy.
     */
    public Document fetchAllGroupsDocument()
    throws UniconGroupsException {
        return getRootGroup().buildDocument(true);
    }

    /**
     * Return an array of IGroup objects
     * (wrappers to uPortal IEntityGroup objects)
     * that the target member belongs to.
     * @param member the group member target.
     * @return The list of IEntityGroup objects
     * @throws UniconGroupsException if the uPortal <code>IGroupMember</code>
     * object fails retrieving the groups containing the given member.
     */
    public IGroup[] fetchUserGroupsArray(IMember member)
    throws UniconGroupsException {
        return member.getAllContainingGroups();
    }

    /**
     * Retieves the root group for users.
     * @return A <code>IGroup</code> the represents the root user group
     * in the uPortal system.
     * @throws UniconGroupsException if the uPortal <code>GroupService</code>
     * fails to retrieve the root user group or the root group cannot
     * be instantiated.
     */
    public IGroup getRootGroup()
    throws UniconGroupsException {
        IGroup group = null;

        try {
            group = GroupFactory.getGroup(
                GroupService.getRootGroup(
                org.jasig.portal.security.IPerson.class));
        } catch (Exception e) {
            throw new UniconGroupsException(e);
        }

        if (group == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("UPortalGroupServiceWrapper::getRootGroup() : Failed ");
            sb.append("finding root group for IPerson!");
            throw new UniconGroupsException(sb.toString());
        }

        return group;
    }


    /**
     * Return the user group that contains administrators.
     * @return the admin IGroup object.
     */
    public IGroup getAdministratorGroup()
    throws UniconGroupsException {
        IGroup group = null;

        try {
            group = GroupFactory.getGroup(
                GroupService.getDistinguishedGroup(
                    GroupService.PORTAL_ADMINISTRATORS));
        } catch (Exception e) {
            throw new UniconGroupsException(e);
        }

        if (group == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("UPortalGroupServiceWrapper::getAdministratorGroup() : ");
            sb.append("Failed finding administrator group!");
            throw new UniconGroupsException(sb.toString());
        }

        return group;
    }


    /**
     * Creates a group in the uPortal system and adds it to the members of
     * the given parent group.
     * @param creator The user that creates the group.
     * @param parentGroup The parent group to which the new group
     * should be assigned.
     * @param name The name of the new group.
     * @param description The description of the new group.
     * @return The desired <code>IGroup</code> object.
     * @throws UniconGroupsException if the uPortal <code>GroupService</code>
     * fails creating a new group.
     */
    public IGroup createGroup(User creator, IGroup parentGroup,
        String name, String description)
    throws UniconGroupsException {
        IGroup newGroup = null;

        try {
            IEntityGroup group = GroupService.instance().newGroup(
                org.jasig.portal.security.IPerson.class);

            if (creator != null) {
                group.setCreatorID(creator.getGroupMember().getKey());
            } else {
                group.setCreatorID("0");
            }
            group.setName(name);
            group.setDescription(description);
            group.update();

            newGroup = GroupFactory.getGroup(group);

            // attach it to its parent
            if (parentGroup != null) {
                // addGroup calls update
                parentGroup.addGroup(newGroup);
            }
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }

        return newGroup;
    }

    /**
     * Return the group with the given path relative to the root group.
     * @param path a relative path
     * @return the target IGroup object.
     */
    public IGroup getGroupByPath(String[] path)
    throws UniconGroupsException {
        if (path == null) return null;

        boolean error = false;
        StringBuffer pathSB = new StringBuffer();
        IGroup rootGroup = getRootGroup();
        IGroup group = rootGroup;
        for (int i=0; i<path.length; i++) {
            pathSB.append("->");
            pathSB.append(path[i]);
            if (!error) {
                group = group.getMemberGroup(path[i]);
                if (group == null) {
                    error = true;
                }
            }
        }

        if (error) {             
            throw new UniconGroupsException("UPortalGroupServiceWrapper::" +
                "getGroupByPath() : invalid group path: " +
                rootGroup.getName() + pathSB.toString());
        }
        return group;
    }

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
    public IGroup[] getPreferredPathGroups (IGroup[] groups, String delim, boolean excludeThisNode)
    throws UniconGroupsException {

        List groupList = new ArrayList();
        IGroup currentGroup = null;
        IGroup clonedGroup = null;
        String[] groupPaths = null;

        // Iterate through all groups
        for (int groupIndex = 0; groupIndex < groups.length; groupIndex++) {
         
            currentGroup = groups[groupIndex];

            // Get all group paths of the current group
            groupPaths = currentGroup.getPathsAsStrings(delim, excludeThisNode);

            // Iterate through all paths of each group
            for (int pathIndex = 0; pathIndex < groupPaths.length; pathIndex++) {

                try {
                    // Clone the group for each different group path
                    clonedGroup = GroupFactory.getGroup(currentGroup.getKey());
                } catch (FactoryCreateException fce) {

                    StringBuffer sb = new StringBuffer();
                    sb.append("UPortalGroupServiceWrapper::getPreferredPathGroups() : Failed ");
                    sb.append("getting group named:" + currentGroup.getName() + ":");
                    sb.append(fce.getMessage());

                    LogService.instance().log(LogService.ERROR,sb.toString(), fce);                        
                }
                //Set preferred path for cloned group
                clonedGroup.setPreferredPath(groupPaths[pathIndex]);
                groupList.add(clonedGroup);
            }
        }

        return (IGroup[]) groupList.toArray(new IGroup[0]);
    }

    UPortalGroupServiceWrapper() {}
}
