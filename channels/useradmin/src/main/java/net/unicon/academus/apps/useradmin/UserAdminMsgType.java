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
package net.unicon.academus.apps.useradmin;

/**
 * Represents the set of UserAdmin Application message types in the system. This
 * class follows the type-safe enumeration pattern (see Effective Java).
 * Consequently, all possible instances of <code>UserAdminMsgType</code> are
 * static members of the class itself. Examples include:
 * <ul> <li>XML</li> <li>HTML</li> <li>Velocity</li></ul>
 */

public final class UserAdminMsgType {
    /* Static Members */
    public static final UserAdminMsgType OK = new UserAdminMsgType("OK");
    public static final UserAdminMsgType USER_EXISTS =
        new UserAdminMsgType("USER_EXISTS");

    /* Instance Members */
    private String label = null;

    private UserAdminMsgType(String label) {
        this.label = label;
    }

    public static UserAdminMsgType getUserAdminMsgType(String UserAdminMsgType)
    throws Exception {
        if (OK.toString().equalsIgnoreCase(UserAdminMsgType)) {
            return OK;
        }
        if (USER_EXISTS.toString().equalsIgnoreCase(UserAdminMsgType)) {
            return USER_EXISTS;
        }
        throw new Exception("Could not find UserAdmin Message Type: " +
            UserAdminMsgType);
    }

    /**
     * Provides the label for this producer type. 
     * This information is useful for display purposes.
     * @return the producer type in string form.
     */
    public String toString() {
        return label;
    }

    /**
     * Overrides the equals() method.
     */
    public boolean equals(Object obj) {
        if (obj instanceof UserAdminMsgType) {
            UserAdminMsgType t = (UserAdminMsgType)obj;
            return this.toString().equals(t.toString());
        }
        return false;
    }
}
