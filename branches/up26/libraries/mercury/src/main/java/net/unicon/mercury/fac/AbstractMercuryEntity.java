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

package net.unicon.mercury.fac;

import net.unicon.mercury.IMercuryEntity;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.MercuryException;

public abstract class AbstractMercuryEntity implements IMercuryEntity {

    // Instance Members.
    private final IMessageFactory owner;

    /*
     * Public API.
     */

    /**
     * Retrieve the object's owner reference.
     */
    public final IMessageFactory getOwner() {
        return owner;
    }

    /**
     * Serialize to an XML fragment. Calls
     * toXml(StringBuffer) on implementation class.
     * @return XML Fragment as a String
     */
    public final String toXml() throws MercuryException {
        return toXml(new StringBuffer()).toString();
    }

    public abstract StringBuffer toXml(StringBuffer out) throws MercuryException;

    /*
     * Protected API.
     */

    protected AbstractMercuryEntity(IMessageFactory owner) {

        // Assertions.
        if (owner == null) {
            String msg = "Argument 'owner' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.owner = owner;

    }

}
