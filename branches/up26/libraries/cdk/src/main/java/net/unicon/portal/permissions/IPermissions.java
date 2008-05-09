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

import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.IMember;

import org.jasig.portal.security.IAuthorizationPrincipal;

import java.util.Map;

/**
 * This is the permissions interface. Typical implementations should take
 * advantage of the underlying permissions mechanisms of the target portal
 * application if they exist.
 */

public interface IPermissions {

    public final static String YES = "Y";
    public final static String NO = "N";

    // Target - Channel

    /**
     * Retrieve the target (channel) for this permissions object.
     * @return String The target
     */
    public String getTarget();

    /**
     * Sets the target (channel) for this permissions object.
     * @param target the new target
     */
    public void setTarget (String target);

    // Principal - Group

    /**
     * Retrieve the principal (<code>IGroup</code>) object for this permission.
     * @return The <code>IGroup</code> object for this permission.
     */
    public IGroup getGroupPrincipal();
    public IMember getMemberPrincipal();
    public IAuthorizationPrincipal getPrincipal();

    /**
     * Sets the principal (<code>IGroup</code>) object for this permission.
     * @param newPrincipal the new <code>IGroup</code> object.
     * @throws PermissionsException
     */
    public void setPrincipal (IGroup newPrincipal) throws PermissionsException;

    public void setPrincipal (IMember newPrincipal) throws PermissionsException;
    public void setPrincipal (IAuthorizationPrincipal newPrincipal)
    throws PermissionsException;
    public PrincipalType getPrincipalType();


    // Activities

    /**
     * Retrieve the activities for this permission. A permission object
     * consists of a collection of activities. The mapping
     * should contain names mapped to <code>Boolean</code> values.
     * @return A <code>Map</code> that contains the <code>Boolean</code> objects
     * for a permissions object.
     * @throws PermissionsException
     */
    public Map getActivities() throws PermissionsException;

    public Map getCompleteActivities();

    /**
     * Retrieve the activities for this permission. A permission object
     * consists of a collection of activities. The mapping
     * should contain names mapped to an enumeration of the permission value
     * (i.e. "Y"/"N", "true"/"false", etc).
     * @return A <code>Map</code> that contains <code>String</code> objects
     * that enumerate the value for a permissions object.
     * @throws PermissionsException
     */
    public Map getEnumeratedActivities(String username)
    throws PermissionsException;

    /**
     * This adds a name/value activity with a permissions object.
     * @param name the name of the new activty.
     * @param value the initial value of the new activity.
     * @throws PermissionsException
     */
    public void addActivity(String name, boolean value)
	throws PermissionsException;

    /**
     * This associates a value with an activity of a permissions object. If
     * the activity doesn't exist, it should add it.
     * @param name the name of the activty.
     * @param value the value of the activity.
     * @throws PermissionsException
     */
    public void setActivity(String name, boolean value)
	throws PermissionsException;

    /**
     * This removes the association with the permissions object and 
     * the activity with the given name.
     * @param name the name of the activty to be removed.
     * @throws PermissionsException
     */
    public void removeActivity(String name) throws PermissionsException;

    /**
     * Tells if a permissions object has the given activity.
     * @return The existence value of the given activity.
     * @throws PermissionsException
     */
    public boolean hasActivity(String name) throws PermissionsException;

    /**
     * Tells if the principal can perform the given activity.
     * @return The ability to perform the given activity.
     */
    public boolean canDo(String username, String activity);

    /**
     * Gets the token for the target channel. The token is used to storing
     * purposes and should be unique.
     */
    public String getTargetToken();

    /**
     * Prints out a listing of the contents of a permissions object. This 
     * is used for debugging.
     */
    public void dump();
}
