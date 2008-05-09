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
package net.unicon.portal.channels.permissionadmin;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import net.unicon.academus.common.properties.AcademusPropertiesType;
import net.unicon.academus.domain.lms.DefaultRoleType;
import net.unicon.academus.domain.lms.Memberships;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.OfferingFactory;
import net.unicon.academus.domain.lms.Role;
import net.unicon.academus.domain.lms.RoleFactory;
import net.unicon.portal.channels.BaseSubChannel;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.domain.ChannelClassFactory;
import net.unicon.portal.domain.ChannelMode;
import net.unicon.portal.domain.ChannelType;
import net.unicon.portal.groups.GroupFactory;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.UniconGroupService;
import net.unicon.portal.groups.UniconGroupServiceFactory;
import net.unicon.portal.permissions.Activity;
import net.unicon.portal.permissions.ChannelClassPermissions;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.permissions.PermissionsService;
import net.unicon.sdk.properties.UniconPropertiesFactory;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PermissionsChannel extends BaseSubChannel {
    // String name found in the manifest
    private static final String channelName = "PermissionsChannel";
    private static IGroup systemRoleGroup = null;

    // Different Object Types
    private static final int PERMISSIONS  = 0;
    private static final int CHANNELCLASS = 1;

    private static UniconGroupService groupService = null;

    static {
        try {
            groupService = UniconGroupServiceFactory.getService();
            IGroup personRoot = groupService.getRootGroup();
            String groupPath = null;

            try {
                groupPath = UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getProperty("net.unicon.academus.domain.lms.Role.defaultSystemRoleGroupPath");
                StringTokenizer st = new StringTokenizer(groupPath, ",");
                String[] path = new String[st.countTokens()];
                int i = 0;

                while (st.hasMoreTokens()) {
                    path[i++] = st.nextToken();
                }
                systemRoleGroup = groupService.getGroupByPath(path);
            } catch (Exception e) {
                LogService.log(LogService.ERROR,
                    "PermissionsChannel : Error retrieving System Role Group " +
                    "for path: " + groupPath + ": " + e, e);
                systemRoleGroup = personRoot;
            }
        } catch (Exception e) {
            LogService.log(LogService.ERROR,
                "PermissionsChannel : Error retrieving System Role Group: " +
                e, e);
        }
    }

    public PermissionsChannel() {
        super();
    }

    public void buildXML(String upId) throws Exception {
    ChannelDataManager.setSSLLocation(upId,
          ChannelDataManager.getChannelClass(upId).getSSLLocation());

        super.setSheetName(upId, "general");

        Hashtable permParams = getXSLParameters(upId);
        ChannelRuntimeData runtimeData = super.getRuntimeData(upId);

/* -->  uncomment to debug this channel...
// Spill parameters.
System.out.println("*****> Spilling Parameters...");
Enumeration keys = runtimeData.getParameterNames();
while (keys.hasMoreElements()) {
    String key = null;
    try {
        key = (String) keys.nextElement();
        Object value = runtimeData.getParameter(key);
        String rslt = null;
        if (value instanceof String) {
            rslt = (String) value;
        } else {
            rslt = "[" + value.getClass().getName() + "]";
        }
        System.out.println("\t" + key + "=" + rslt);
    } catch (Throwable t) {
        System.out.println("\tEXCEPTION..." + key);
    }
}
*/

        Document doc = null;

        String command = runtimeData.getParameter("command");
        if (command != null) {
            // Initializing Roles and Permissions for Channel
            IGroup group = null;
            Role role = null;
            ChannelMode channelMode = ChannelMode.OFFERING;
            int roleType = Role.OFFERING;
            boolean globalModeChannels = true;
            ChannelType channelType = ChannelType.OFFERING;

            IPermissions[] permissions = null;

            if (!"confirm_delete_role".equals(command) &&
                !"confirm_delete_system_group".equals(command)) {
                // Getting the type of permissions to configure
                String type = runtimeData.getParameter("type");
                permParams.put("type", type);

                // Getting Channel Manfiest from the properties dir
                if (type.equals("system") ) {
                    channelMode = ChannelMode.ADMIN;
                    roleType = Role.SYSTEM;
                    channelType = ChannelType.ADMIN;
                    globalModeChannels = false;
                }
            }

            if (command.equals("add_role")) {
                super.setSheetName(upId, "add_role");
                ChannelClass[] channels = (ChannelClass[])ChannelClassFactory.
                    getChannelClasses(channelType, globalModeChannels).
                        toArray(new ChannelClass[0]);
                doc = buildDocument(channels);
            } else if (command.equals("add_system_group")) {
                super.setSheetName(upId, "add_system_group");
                ChannelClass[] channels = (ChannelClass[])ChannelClassFactory.
                    getChannelClasses(channelType, globalModeChannels).
                        toArray(new ChannelClass[0]);
                doc = buildDocument(channels);
            } else if (command.equals("edit_system_group")) {
                super.setSheetName(upId, "edit_system_group");
                String groupKey = runtimeData.getParameter("group_key");

                if (groupKey != null) {
                    group = GroupFactory.getGroup(groupKey);

                    // Putting role name in
                    permParams.put("group_name", group.getName());
                    permParams.put("group_key",   groupKey);


                    ChannelClass[] channelClasses =
                        (ChannelClass[])ChannelClassFactory.getChannelClasses(
                            channelType, globalModeChannels).toArray(
                                new ChannelClass[0]);
                    permissions = PermissionsService.instance().
                        getPermissions(channelClasses, group);

                    // Building document based on the permission list
                    doc = buildDocument(permissions);
                } else {
                    LogService.log(LogService.ERROR,
                        "PermissionsChannel::buildXML : no group " +
                        "specified on edit");
                }
            } else if (command.equals("edit_role")) {
                super.setSheetName(upId, "edit_role");

                // Getting role ID and role Name from formdata
                String roleName = null;
                String roleIDStr = null;
                String roleIdAndName =
                    runtimeData.getParameter("role_id_and_name");

                if (roleIdAndName != null) {
                    roleName = roleIdAndName.substring(
                        roleIdAndName.indexOf('.')+1, roleIdAndName.length());
                    roleIDStr = roleIdAndName.substring(
                        0, roleIdAndName.indexOf('.'));
                }

                if (roleName != null && roleName.length() > 0 &&
                    roleIDStr != null) {
                    int roleID = Integer.parseInt(roleIDStr);

                    // Putting role name in
                    permParams.put("role_name", roleName);
                    permParams.put("role_id",   roleIDStr.trim());

                    role = RoleFactory.getRole((long) roleID);

                    ChannelClass[] channelClasses =
                        (ChannelClass[])ChannelClassFactory.getChannelClasses(
                            channelType, globalModeChannels).toArray(
                                new ChannelClass[0]);
                    permissions = PermissionsService.instance().
                        getPermissions(channelClasses, role.getGroup());

                    // Building document based on the permission list
                    doc = buildDocument(permissions);
                } else {
                    LogService.log(LogService.ERROR,
                        "PermissionsChannel::buildXML : no role " +
                        "specified on edit: " + roleIdAndName);
                }
            } else if (command.equals("insert_system_group") ) {
                // Inserting a new Default Role and Permission
                String groupName = runtimeData.getParameter("group_name");
                if (groupName != null) {
                    // check if a group with this name already exists
                    if (systemRoleGroup.contains(groupName)) {
                        StringBuffer sb = new StringBuffer(64);
                        sb.append("The system role group ");
                        sb.append("name '").append(groupName).append("' ");
                        sb.append("you have entered is already in use.");
                        setErrorMsg(upId, sb.toString());
                        return;
                    }
                    try {
                        group = groupService.createGroup(getDomainUser(upId),
                            systemRoleGroup, groupName, groupName);
                    } catch (Exception ie) {
                        StringBuffer sb = new StringBuffer(64);
                        sb.append("There was an error creating the system ");
                        sb.append("role group you specified.");
                        setErrorMsg(upId, sb.toString());
                        return;
                    }

                    ChannelClass[] channelClasses =
                        (ChannelClass[])ChannelClassFactory.getChannelClasses(
                            channelType, globalModeChannels).toArray(
                                new ChannelClass[0]);
                    permissions = PermissionsService.instance().getPermissions(
                        channelClasses, role.getGroup());

                    updatePermissions(permissions, runtimeData);

                    // inform other users that this channel was modified
                    broadcastDirtyChannel(channelName);
                } else {
                    LogService.log(LogService.INFO,
                        "PermissionsChannel::buildXML : no group name" +
                        "specified on add command");
                }
            } else if (command.equals("insert_role") ) {
                // Inserting a new Default Role and Permission
                String roleName = runtimeData.getParameter("role_name");
                if (roleName != null) {
                    try {
                        if(RoleFactory.isExistingName(roleName, roleType,
                            Role.USER_UNIQUE)) {        // NB:  change from user-defined
                            throw new  Exception();     // to user_unique changes 80% of
                        } else {                        // the problem.  Still trouble if collision...
                            role = RoleFactory.createRole(roleName,
                                roleType | Role.USER_DEFINED,
                                getDomainUser(upId));
                        }
                    } catch (Exception ie) {
                        System.out.println("error creating a new role: " + ie);
                        StringBuffer sb = new StringBuffer(64);
                        sb.append("The ").append(runtimeData.getParameter("type")).append(" role ");
                        sb.append("name '").append(roleName).append("' ");
                        sb.append("you have entered is already in use.");
                        setErrorMsg(upId, sb.toString());
                        return;
                    }

                    ChannelClass[] channelClasses =
                        (ChannelClass[])ChannelClassFactory.getChannelClasses(
                            channelType, globalModeChannels).toArray(
                                new ChannelClass[0]);
                    permissions = PermissionsService.instance().getPermissions(
                        channelClasses, role.getGroup());

                    updatePermissions(permissions, runtimeData);

                    // inform other users that this channel was modified
                    broadcastDirtyChannel(channelName);

                    // inform the roster channel that a new offering role was added
                    broadcastDirtyChannel("RosterChannel");

                } else {
                    LogService.log(LogService.INFO,
                        "PermissionsChannel::buildXML : no role name" +
                        "specified on add command");
                }
            } else if (command.equals("update_system_group") ) {
                // Updating a Default Role and Permisssion
                String groupName  = runtimeData.getParameter("group_name");
                String groupKey = runtimeData.getParameter("group_key");

                if (groupKey != null && groupKey.length() > 0) {

                    // Getting role to modify
                    group = GroupFactory.getGroup(groupKey);

                    // Change the label if needed
                    if (!group.getName().equals(groupName) ) {
                        // Attempt to change it...
                        try {
                            role = RoleFactory.getRole(group);
                            role.setLabel(groupName);
                            RoleFactory.persist(role);  // NB:  Due to mysterious design choices,
                            group.setName(groupName);   // we have to change in two places...
                        } catch (Throwable t) {
                            StringBuffer sb = new StringBuffer();
                            sb.append("Unable to make the requested name ");
                            sb.append("change for the following reason:  ");
                            sb.append(t.getMessage());
                            setErrorMsg(upId, sb.toString());
                            return;
                        }
                    }
                    ChannelClass[] channelClasses =
                        (ChannelClass[])ChannelClassFactory.getChannelClasses(
                            channelType, globalModeChannels).toArray(
                                new ChannelClass[0]);
                    permissions = PermissionsService.instance().getPermissions(
                        channelClasses, group);

                    updatePermissions(permissions, runtimeData);

                    // inform other users that this channel was modified
                    broadcastDirtyChannel(channelName);
                } else {
                    LogService.log(LogService.ERROR,
                        "PermissionsChannel::buildXML : no group" +
                        "specified on update: (" + groupName + ", " +
                        groupKey + ")");
                }
            } else if (command.equals("update_role") ) {
                // Updating a Default Role and Permisssion
                String roleName  = runtimeData.getParameter("role_name");
                String roleIDStr = runtimeData.getParameter("role_id");

                if (roleIDStr != null && roleIDStr.length() > 0) {
                    int roleID = Integer.parseInt(roleIDStr);

                    // Getting role to modify
                    role = RoleFactory.getRole((long) roleID);

                    // Change the label if needed
                    if (!role.getLabel().equals(roleName) ) {
                        // Attempt to change it...
                        try {
                            role.setLabel(roleName);
                            RoleFactory.persist(role);
                        } catch (Throwable t) {
                            StringBuffer sb = new StringBuffer();
                            sb.append("Unable to make the requested name ");
                            sb.append("change for the following reason:  ");
                            sb.append(t.getMessage());
                            setErrorMsg(upId, sb.toString());
                            return;
                        }
                    }
                    ChannelClass[] channelClasses =
                        (ChannelClass[])ChannelClassFactory.getChannelClasses(
                            channelType, globalModeChannels).toArray(
                                new ChannelClass[0]);
                    permissions = PermissionsService.instance().getPermissions(
                        channelClasses, role.getGroup());

                    updatePermissions(permissions, runtimeData);

                    // inform other users that this channel was modified
                    broadcastDirtyChannel(channelName);

                    // inform the roster channel that a new offering role was modified
                    broadcastDirtyChannel("RosterChannel");

                } else {
                    LogService.log(LogService.ERROR,
                        "PermissionsChannel::buildXML : no role " +
                        "specified on update: (" + roleIDStr + ", " +
                        roleName + ")");
                }
            } else if ("delete_system_group".equals(command)) {
                super.setSheetName(upId, "delete_system_group");

                // Getting role ID and role Name from formdata
                String groupKey = runtimeData.getParameter("group_key");

                if (groupKey != null) {
                    group = GroupFactory.getGroup(groupKey);
                    String groupName = group.getName();

                    // Check if this is a default role type.
                    if (DefaultRoleType.SYSTEM.isDefault(groupName)
                            || DefaultRoleType.OFFERING.isDefault(groupName)) {
                        StringBuffer msg = new StringBuffer(128);
                        msg.append("You are not allowed to delete ");
                        msg.append("default roles. Only user-defined ");
                        msg.append("roles may be deleted.");
                        setErrorMsg(upId, msg.toString());
                        return;
                    }

                    // Check if any users have been assigned the role.
                    if (group.getDescendantMembers().size() > 0) {
                        StringBuffer msg = new StringBuffer(128);
                        msg.append("You are not allowed to delete ");
                        msg.append("a system role ");
                        msg.append("that has members assigned.");
                        setErrorMsg(upId, msg.toString());
                        return;
                    }

                    // Putting role name in
                    permParams.put("group_name", groupName);
                    permParams.put("group_key",   groupKey);
                } else {
                    LogService.log(LogService.ERROR,
                        "PermissionsChannel::buildXML : no groupKey " +
                        "specified on delete");
                }
            } else if ("delete_role".equals(command)) {
                super.setSheetName(upId, "delete_role");

                // Getting role ID and role Name from formdata
                String roleIdAndName =
                    runtimeData.getParameter("role_id_and_name");
                String roleName = roleIdAndName.substring(
                                                roleIdAndName.indexOf('.')+1,
                                                roleIdAndName.length());
                String roleIDStr =
                    roleIdAndName.substring(0, roleIdAndName.indexOf('.'));
                if (roleName != null && roleIDStr != null) {
                    int roleID = Integer.parseInt(roleIDStr);

                    // We don't let the user delete default roles
                    // only user-defined roles can be deleted.
                    role = RoleFactory.getRole(roleID);
                    if ((role.getRoleType() & Role.USER_DEFINED) == 0) {

                        String msg = "You are not allowed to delete " +
                            "default roles. Only user-defined roles may be " +
                            "deleted.";
                        setErrorMsg(upId, msg);
                        return;
                    }

                    // Check if any users have been assigned the role.
                    if ((roleType == Role.OFFERING &&
                        Memberships.userEnrolledWithRole(roleID))) {
                        String msg = "You cannot delete a role that " +
                            "is currently assigned.";
                        setErrorMsg(upId, msg);
                        return;
                    }

                    // Check if any offerings have specified this role as
                    // their default
                    if (roleType == Role.OFFERING) {
                        List offerings =
                            OfferingFactory.getOfferingsWithDefaultRole(roleID);
                        if (offerings != null && offerings.size() > 0) {
                            StringBuffer msg = new StringBuffer();
                            msg.append("You cannot delete a role that ");
                            msg.append("is currently assigned to an Offering. The following ");
                            msg.append("offerings have this role assigned as ");
                            msg.append("their default. Search for them in ");
                            msg.append("the Offering Admin channel and ");
                            msg.append("change their default role: ");

                            boolean first=true;
                            Iterator itr = offerings.iterator();
                            Offering off;
                            while (itr.hasNext()) {

                                off = (Offering)itr.next();

                                // don't add the offering separator before the
                                // first offering.
                                if (first) {
                                    first = false;
                                } else {
                                    msg.append(", ");
                                }

                                msg.append(off.getName());
                            }
                            setErrorMsg(upId, msg.toString());
                            return;
                        }
                    }


                    // Putting role name in
                    permParams.put("role_name", roleName);
                    permParams.put("role_id",   roleIDStr.trim());

                } else {
                    LogService.log(LogService.ERROR,
                        "PermissionsChannel::buildXML : no roleId " +
                        "specified on delete");
                }
            } else if ("confirm_delete_system_group".equals(command)) {
                String ans = runtimeData.getParameter(
                    "confirm_delete_system_group");
                if ("yes".equals(ans)) {
                    String groupKey = runtimeData.getParameter("group_key");
                    group = GroupFactory.getGroup(groupKey);
                    PermissionsService.instance().removePermissions(group);
                    role = RoleFactory.getRole(group);
                    RoleFactory.deleteRole(role);
//                    group.delete();
                    // inform other users that this channel was modified
                    broadcastDirtyChannel(channelName);
                }
            } else if ("confirm_delete_role".equals(command)) {
                String ans = runtimeData.getParameter("confirm_delete_role");
                if ("yes".equals(ans)) {
                    String roleIDStr = runtimeData.getParameter("role_id");
                    role = RoleFactory.getRole(Long.parseLong(roleIDStr));
                    PermissionsService.instance().removePermissions(
                        role.getGroup());
                    RoleFactory.deleteRole(role);

                    // inform other users that this channel was modified
                    broadcastDirtyChannel(channelName);

                    // inform the roster channel that a new offering role was deleted
                    broadcastDirtyChannel("RosterChannel");
                }
            }
        }
        /* Start XML */
        if (doc == null) {
            List systemPermList   = systemRoleGroup.getDescendantGroups();
            List offeringPermList = RoleFactory.getDefaultRoles(Role.OFFERING);

            StringBuffer xmlBuff = new StringBuffer();
            xmlBuff.append("<?xml version=\"1.0\"?>");
            xmlBuff.append("<administration>");

            if (systemPermList != null) {
                xmlBuff.append("<system-permissions>");
                for (int ix=0; ix < systemPermList.size(); ++ix) {
                    xmlBuff.append(((IGroup) systemPermList.get(ix)).toXML());
                }
                xmlBuff.append("</system-permissions>");
            }

            if (offeringPermList != null) {
                xmlBuff.append("<offering-permissions>");
                for (int ix=0; ix < offeringPermList.size(); ++ix) {
                    xmlBuff.append(((Role) offeringPermList.get(ix)).toXML());
                }
                xmlBuff.append("</offering-permissions>");
            }

            xmlBuff.append("</administration>");
            super.setXML(upId, xmlBuff.toString());
        } else {
            super.setDocument(upId, doc);
            super.setXML(upId, null);
        }
    }

    private Document buildDocument(IPermissions[] permissions)
    throws Exception {
        Document doc = DocumentFactory.getNewDocument();
        Element manifest = doc.createElement("manifest");
        Element node = null;
        ChannelClass cc = null;

        for(int ix = 0; permissions != null && ix < permissions.length; ++ix) {
            if (permissions[ix] instanceof ChannelClassPermissions) {
                node = ((ChannelClassPermissions)permissions[ix]).
                    getModifiedElement();
                manifest.appendChild(
                    manifest.getOwnerDocument().importNode(node, true));
            } else {
                LogService.log(LogService.ERROR,
                    "PermissionsChannel::buildDocument : wrong IPermissions " +
                    "implementation: " + permissions[ix].getClass().getName());
            }
        }
        doc.appendChild(manifest);
        return doc;
    }

    private Document buildDocument(ChannelClass[] channels)
    throws Exception {
        Document doc = DocumentFactory.getNewDocument();
        Element manifest = doc.createElement("manifest");
        Element node = null;

        for(int ix = 0; channels != null && ix < channels.length; ++ix) {
            // Checks to see if the channel has any activities before
            // appending it to the manifest.
            if (channels[ix].getActivities().size() > 0) {
                node = channels[ix].getElement();
                manifest.appendChild(manifest.getOwnerDocument().importNode(
                        node, true));
            }
        }
        doc.appendChild(manifest);
        return doc;
    }

    private void updatePermissions(IPermissions[] permissions,
        ChannelRuntimeData formData)
    throws Exception {
        ChannelClass cc      = null;
        Activity activityObj = null;
        String handle        = null;
        String activity      = null;
        String value         = null;
        List   activityList  = null;
        String producer      = null;
        boolean willDo = false;

        if (permissions == null) return;

        for (int ix = 0; ix < permissions.length; ++ix) {
            if (!(permissions[ix] instanceof ChannelClassPermissions)) {
                LogService.log(LogService.ERROR,
                    "PermissionsChannel::updatePermissions : wrong " +
                    "IPermissions implementation: " +
                    permissions[ix].getClass().getName());
                continue;
            }
            cc = ((ChannelClassPermissions)permissions[ix]).getChannelClass();

            // Retrieving Data from the Channel List
            producer = cc.getProducer();
            if (producer == null || "".equals(producer)) {
                handle = cc.getHandle();
            } else {
                // this is a producer type channel
                // get the handle from its permissions dom
                handle = cc.getElement().getAttribute("handle");
            }
            activityList = cc.getActivities();

            for (int iy=0; iy < activityList.size(); ++iy) {
                activityObj = (Activity) activityList.get(iy);
                activity = activityObj.getHandle();
                value = (String) formData.getParameter(handle + "-" + activity);
                // Setting values
                if (value != null) {
                    willDo = true;
                } else {
                    willDo = false;
                }

                permissions[ix].setActivity(activity, willDo);
            }
        }
    }
}

