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

package net.unicon.academus.util.djinn;

/**
 * Uniquely identifies an individual state value within a given context
 * (screen).
 */
public final class StateKey {

    // Instance Members.
    private String handle;

    /*
     * Public API.
     */

    /**
     * Creates a new <code>StateKey</code> with the specified handle.  This handle must be
     */
    public StateKey(String handle) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.handle = handle;

    }

    /**
     * Returns <code>true</code> if the input is a <code>StateKey</code>
     * instance with the same handle, otherwise <code>false</code>.
     *
     * @param obj the object to which this <code>StateKey</code> should be
     * compared.
     * @return <code>true</code> if the input is a <code>StateKey</code>
     * instance with the same handle, otherwise <code>false</code>.
     */
    public boolean equals(Object obj) {
        try {
            return ((StateKey) obj).handle.equals(this.handle);
        } catch (ClassCastException cce) {
            return false;
        }
    }

    /**
     * Returns the hash code for the handle to this <code>StateKey</code>.
     *
     * @return the hash code for the handle to this <code>StateKey</code>.
     */
    public int hashCode() {
        return handle.hashCode();
    }

    /*
     * Package API.
     */

    String getHandle() {
        return handle;
    }

}
