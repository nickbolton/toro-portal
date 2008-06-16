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

package net.unicon.alchemist.access;

public final class Identity {

    // Instance Members.
    private String id;
    private IdentityType type;

    /*
     * Public API.
     */

    public Identity(String id, IdentityType type) {

        // Assertions.
        if (id == null) {
            String msg = "Argument 'id' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (type == null) {
            String msg = "Argument 'type' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.id = id;
        this.type = type;

    }

    public String getId() {
        return id;
    }

    public IdentityType getType() {
        return type;
    }

    public final boolean equals(Object o) {

        boolean rslt = false;   // default...

        if (o != null && o instanceof Identity) {
            Identity y = (Identity) o;
            rslt = y.getId().equals(id) && y.getType().equals(type);
        }

        return rslt;

    }

    public final int hashCode() {
        return id.hashCode();
    }

}