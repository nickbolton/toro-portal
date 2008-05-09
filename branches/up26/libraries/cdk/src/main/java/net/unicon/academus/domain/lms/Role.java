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
package net.unicon.academus.domain.lms;

import net.unicon.academus.common.properties.AcademusPropertiesType;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.UniconGroupsException;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.permissions.PermissionsException;
import net.unicon.portal.permissions.PermissionsService;
import net.unicon.portal.domain.ChannelMode;
import net.unicon.sdk.properties.UniconPropertiesFactory;

import org.jasig.portal.services.LogService;

/**  */
public final class Role {

    static String groupPrefix = null;

    static {
        try {
            groupPrefix = UniconPropertiesFactory.getManager(
                AcademusPropertiesType.LMS).getProperty(
                    "net.unicon.academus.domain.lms.Role.groupPrefix");
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
        }
    }

    // Role Types
    public static final int SYSTEM         = 1;
    public static final int OFFERING       = 2;
    // Role Modifiers
    public static final int USER_DEFINED   = 4;
    public static final int USER_UNIQUE    = 8;
    /* Instance Members */
    private long Id         = 0;
    private IGroup group    = null;
    private long offeringId = 0;
    private String label = null;
    private int roleType = 0;
    private boolean isSystemWide = true;
    Role(long Id, IGroup group, Offering offering, String label, int roleType) {
        this.Id = Id;
        if (offering != null) {
            this.offeringId = offering.getId();
            isSystemWide = false;
        }
        this.group = group;
        this.label = label;
        this.roleType = roleType;
    }
    public void dumpPermissions()
    throws OperationFailedException {
        try {
            System.out.println("Permissions for role: " + Id);
            IPermissions[] permissions =
                PermissionsService.instance().getPermissions(group);

            if (permissions != null) {
                for (int i=0; i<permissions.length; i++) {
                    permissions[i].dump();
                }
            }
        } catch (PermissionsException pe) {
            throw new OperationFailedException(pe);
        }
    }
    public ChannelMode getChannelMode() throws ItemNotFoundException {
        if ( (roleType & SYSTEM) != 0 ) {
            return ChannelMode.ADMIN;
        } else if ( (roleType & OFFERING) != 0 ) {
            return ChannelMode.OFFERING;
        }
        String msg = "This role (" + Id + ") has an invalid roleType: " +
        roleType;
        throw new ItemNotFoundException(msg);
    }
    public IGroup getGroup() {
        return group;
    }
    public long getId() {
        return Id;
    }

    public Offering getOffering() {
        Offering offering = null;
        try {
            if (offeringId > 0) {
                offering = OfferingFactory.getOffering(offeringId);
            }
        } catch (OperationFailedException ofe) {
            LogService.log(LogService.ERROR,
            "getOffering Operation Failed : Role.getOffering()");
        } catch (ItemNotFoundException infe) {
            LogService.log(LogService.ERROR,
            "Cannot Find Offering Object : Role.getOffering() " + offeringId);
        }
        return offering;
    }
    /**
     * Provides the name (label) associated with this class of users (role). Examples include the following: <ul>
     * <li>Administrator</li> <li>Facilitator</li> <li>User</li> <li>Student</li> <li>Assistant</li> </ul>
     * @return the name of this role.
     */
    public String getLabel() {
        return label;
    }
    /**
     * Modifies the name (label) associated with this class of users (role).
     * @param label the new name for this role.
     * @throws IllegalArgumentException if the label is zero length or contains
     * only whitespace characters or already exists (for another role) within the offering.
     */
    public void setLabel(String label) throws IllegalArgumentException,
    OperationFailedException,
    ItemNotFoundException {
        if (label.trim().length() == 0) {
            StringBuffer msg = new StringBuffer();
            msg.append("Role label can't be zero-length ");
            msg.append("or contain only whitespace.");
            throw new IllegalArgumentException(msg.toString());
        }
        if (RoleFactory.roleExistsInContext(label, this)) {
            StringBuffer msg = new StringBuffer();
            msg.append("The specified role label already exists ");
            msg.append("within the portal system or offering.");
            throw new IllegalArgumentException(msg.toString());
        }
        this.label = label;

        // Update the group.
        try {
            group.setName(groupPrefix+label);
            group.setDescription(groupPrefix+label);
        } catch (UniconGroupsException ge) {
            StringBuffer msg = new StringBuffer();
            msg.append("Failed to update the associated group: ");
            msg.append(ge.getMessage());
            throw new OperationFailedException(msg.toString());
        }
    }

    public int getRoleType() {
        return roleType;
    }
    public boolean isSystemWide() {
        return isSystemWide;
    }
    public boolean isSystemRole() {
        return isType(SYSTEM);
    }
    public boolean isOfferingRole() {
        return isType(OFFERING);
    }
    public boolean isUserDefinedRole() {
        return isType(USER_DEFINED);
    }
    public boolean isUserUniqueRole() {
        return isType(USER_UNIQUE);
    }
    public boolean isType(int type) {
        return (roleType & type) != 0;
    }
    public String toXML() {
        StringBuffer xml = new StringBuffer();
        xml.append("<role");
        xml.append(" id=\"");
        xml.append("" + Id);
        xml.append("\"");
        // Need to change to String representation in the future
        xml.append(" type=\"");
        xml.append("" + roleType);
        xml.append("\"");
        xml.append(">");
        xml.append("<label>");
        xml.append(label);
        xml.append("</label>");
        xml.append("</role>");
        return xml.toString();
    }
}
