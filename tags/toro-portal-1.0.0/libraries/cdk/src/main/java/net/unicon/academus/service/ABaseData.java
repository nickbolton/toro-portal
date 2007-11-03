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
package net.unicon.academus.service;

import java.io.Serializable;

/**
 * base class for all *Data marshalling objects
 * @author Kevin Gary
 *
 */
abstract class ABaseData implements Serializable {

    // Instance Members.
    private String __src;
    private String __externalID;
    private String __foreignId;

    protected ABaseData(String externalID, String src) {

        /* ASSERTIONS */
        // check externalID
        if (!(externalID != null && externalID.trim().length() > 0)) {
            throw new IllegalArgumentException(
                "Argument 'externalID [String]' must be provided.");
        }
        // check src
        if (!(src != null && src.trim().length() > 0)) {
            throw new IllegalArgumentException(
                "Argument 'src [String]' must be provided.");
        }

        __externalID = externalID;
        __src = src;

    }

    protected ABaseData(String externalID, String src, String foreignId) {

        /* ASSERTIONS */
        // check externalID
        if (!(externalID != null && externalID.trim().length() > 0)) {
            throw new IllegalArgumentException(
                "Argument 'externalID [String]' must be provided.");
        }
        // check src
        if (!(src != null && src.trim().length() > 0)) {
            throw new IllegalArgumentException(
                "Argument 'src [String]' must be provided.");
        }
        // NB:  foreignId is nullable.

        // Instance Members.
        this.__src = src;
        this.__externalID = externalID;
        this.__foreignId = foreignId;

    }

    /** Accessor for a Group objects groupSource field */
    public String getSource() { return this.__src; }
    public String getExternalID() { return this.__externalID; }

    /**
     * Gets the foriegn id for this entity if it has one.  A foreign id is
     * always native to the foreign system, whereas the external id may be
     * chosen by academus or some piece of middleware.
     *
     * @return The foreign id for this entity (where applicaple) or
     * <code>null</code>.
     */
    public String getForeignId() {
        return this.__foreignId;
    }

}
