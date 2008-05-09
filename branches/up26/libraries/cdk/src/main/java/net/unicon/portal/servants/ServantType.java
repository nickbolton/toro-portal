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
package net.unicon.portal.servants;

/**
 * Represents the set servant types used by the <code>ServantManager</code.
 * This class follows the type-safe enumeration pattern (see Effective Java).
 * Consequently, all possible instances of <code>ServantType</code>
 * are static members of the class itself.
 */
public final class ServantType {
    /* Static Members */

    // when you create a new static ServantType instance, you also need
    // to add it to the servantTypes array.
    public static final ServantType PERMISSION_ADMIN =
        new ServantType("PermissionAdmin");
    public static final ServantType PERMISSIONS_MANAGER =
        new ServantType("PermissionsManager");
    public static final ServantType GROUPS_MANAGER =
        new ServantType("GroupsManager");
    public static final ServantType IDENTITY_SELECTOR =
        new ServantType("IdentitySelector");

    private static final ServantType[] servantTypes = {PERMISSION_ADMIN,
        PERMISSIONS_MANAGER, GROUPS_MANAGER, IDENTITY_SELECTOR};

    /* Instance Members */
    private String label = null;
    private ServantType(String label) {
        this.label = label;
    }

    public static ServantType[] getServantTypes() {
        return servantTypes;
    }

    public static ServantType getServantType(String ServantType)
    throws Exception {
        if (PERMISSION_ADMIN.toString().equalsIgnoreCase(ServantType)) {
            return PERMISSION_ADMIN;
        }
        if (PERMISSIONS_MANAGER.toString().equalsIgnoreCase(ServantType)) {
            return PERMISSIONS_MANAGER;
        }
        if (GROUPS_MANAGER.toString().equalsIgnoreCase(ServantType)) {
            return GROUPS_MANAGER;
        }
        if (IDENTITY_SELECTOR.toString().equalsIgnoreCase(ServantType)) {
            return IDENTITY_SELECTOR;
        }
        throw new Exception(
        "Could not find Servant Type: " + ServantType);
    }
    /**
     * Provides the label for this servant type.
     * This information is useful for display purposes.
     * @return the servant type in string form.
     */
    public String toString() {
        return label;
    }
}
