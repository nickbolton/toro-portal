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
package net.unicon.portal.permissions;

import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.IMember;

import org.jasig.portal.security.IAuthorizationPrincipal;

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class decorates a <code>PermissionsImpl</code> class that stores
 * a <code>ChannelClass</code> object as the target.
 */

public class ChannelClassPermissions implements IPermissions {

    private ChannelClass channelClass;
    private IPermissions permissions;

    ChannelClassPermissions(IPermissions permissions,
        ChannelClass channelClass) {
        this.channelClass = channelClass;
        this.permissions = permissions;
    }

    public ChannelClass getChannelClass() {
        return channelClass;
    }

    public void setChannelClass(ChannelClass channelClass) {
        this.channelClass = channelClass;
    }

    /**
     * Gets an element that represents the state of the permissions object
     * modified from the <code>ChannelClass</code> defaults.
     * @param channelClass the channel class with default permissions
     * @return a modified <code>Element</code> object.
     * @throws PermissionsException
     */
    public Element getModifiedElement()
    throws PermissionsException {
        Element ccNode = channelClass.getElement();
        NodeList nl = ccNode.getElementsByTagName("activity");
        Element privElement = null;
        String value;
        Map completeMap = permissions.getEnumeratedActivities(null);

        for (int ix = 0; ix < nl.getLength(); ix++) {
            privElement = (Element) nl.item(ix);
            value = (String)completeMap.get(privElement.getAttribute("handle"));
            if (value != null) {
                privElement.setAttribute("allowed", value);
            } else {
                privElement.setAttribute("allowed", IPermissions.NO);
            }
        }
        return ccNode;
    }

    // decorator methods

    public String getTarget() {
        return permissions.getTarget();
    }

    public void setTarget (String target) {
        permissions.setTarget(target);
    }

    public IGroup getGroupPrincipal() {
        return permissions.getGroupPrincipal();
    }

    public IMember getMemberPrincipal() {
        return permissions.getMemberPrincipal();
    }

    public IAuthorizationPrincipal getPrincipal() {
        return permissions.getPrincipal();
    }

    public void setPrincipal (IGroup newPrincipal) throws PermissionsException {
        permissions.setPrincipal(newPrincipal);
    }

    public void setPrincipal (IMember newPrincipal)
    throws PermissionsException {
        permissions.setPrincipal(newPrincipal);
    }

    public void setPrincipal (IAuthorizationPrincipal newPrincipal)
    throws PermissionsException {
        permissions.setPrincipal(newPrincipal);
    }

    public PrincipalType getPrincipalType() {
        return permissions.getPrincipalType();
    }

    public Map getActivities() throws PermissionsException {
        return permissions.getActivities();
    }

    public Map getCompleteActivities() {
        return permissions.getCompleteActivities();
    }

    public Map getEnumeratedActivities(String username)
    throws PermissionsException {
        return permissions.getEnumeratedActivities(username);
    }

    public void addActivity(String name, boolean value)
	throws PermissionsException {
        permissions.addActivity(name, value);
    }

    public void setActivity(String name, boolean value)
	throws PermissionsException {
        permissions.setActivity(name, value);
    }

    public void removeActivity(String name) throws PermissionsException {
        permissions.removeActivity(name);
    }

    public boolean hasActivity(String name) throws PermissionsException {
        return permissions.hasActivity(name);
    }

    public boolean canDo(String username, String activity) {
        return permissions.canDo(username, activity);
    }

    public String getTargetToken() {
        return permissions.getTargetToken();
    }

    public void dump() {
        permissions.dump();
        System.out.println("\tChannelClass: " + channelClass.getClassName());
    }
}
