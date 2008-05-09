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

public final class Principal {

    // Instance Members.
    private Identity[] identities;

    /*
     * Public API.
     */

    public Principal(Identity[] identities) {

        // Assertions.
        if (identities == null) {
            String msg = "Argument 'identities' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.identities = new Identity[identities.length];
        System.arraycopy(identities, 0, this.identities, 0, identities.length);

    }

    public Identity[] getIdentities() {
        Identity[] rslt = new Identity[identities.length];
        System.arraycopy(identities, 0, rslt, 0, identities.length);
        return rslt;
    }

}