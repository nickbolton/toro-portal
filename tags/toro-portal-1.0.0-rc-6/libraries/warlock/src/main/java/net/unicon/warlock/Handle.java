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

package net.unicon.warlock;

public final class Handle {

    // Instance Members.
    private final String value;

    /*
     * Public API.
     */

    public static final int MAX_LENGTH = 64;

    public static Handle create(String value) {

        // Assertions.
        if (value == null) {
            String msg = "Argument 'value' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (value.length() > MAX_LENGTH) {
            String msg = "The specified value exceeds the maximum length.";
            throw new IllegalArgumentException(msg);
        }

        return new Handle(value);

    }

    public String getValue() {
        return value;
    }

    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }

        boolean rslt = false;

        if (o instanceof Handle) {
            Handle h = (Handle) o;
            rslt = h.value.equals(value);
        }

        return rslt;

    }

    public int hashCode() {
        return value.hashCode();
    }

    /*
     * Implementation.
     */

    private Handle(String value) {

        // Assertions.
        if (value == null) {
            String msg = "Argument 'value' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (value.length() > MAX_LENGTH) {
            String msg = "The specified value exceeds the maximum length.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.value = value;

    }

}