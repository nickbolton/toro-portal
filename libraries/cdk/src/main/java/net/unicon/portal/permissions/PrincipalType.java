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

/**
 * Represents the set principal types used by the IPermission objects.
 * This class follows the type-safe enumeration pattern (see Effective Java).
 * Consequently, all possible instances of <code>PrincipalType</code>
 * are static members of the class itself.
 */
public final class PrincipalType {
    /* Static Members */
    public static final PrincipalType GROUP =
        new PrincipalType("Group");
    public static final PrincipalType PERSON =
        new PrincipalType("Person");
    public static final PrincipalType AUTH_PRINCIPAL =
        new PrincipalType("AuthorizationPrincipal");

    /* Instance Members */
    private String label = null;
    private PrincipalType(String label) {
        this.label = label;
    }

    public static PrincipalType getPrincipalType(String PrincipalType)
    throws Exception {
        if (GROUP.toString().equalsIgnoreCase(PrincipalType)) {
            return GROUP;
        }
        if (PERSON.toString().equalsIgnoreCase(PrincipalType)) {
            return PERSON;
        }
        if (AUTH_PRINCIPAL.toString().equalsIgnoreCase(PrincipalType)) {
            return AUTH_PRINCIPAL;
        }
        throw new Exception(
        "Could not find Principal Type: " + PrincipalType);
    }
    /**
     * Provides the label for this servant type.
     * This information is useful for display purposes.
     * @return the servant type in string form.
     */
    public String toString() {
        return label;
    }

    public boolean equals(PrincipalType type) {
        return this.label.equals(type.toString());
    }
}
