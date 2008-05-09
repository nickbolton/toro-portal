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

public abstract class AccessType {

    // Instance Members.
    private final String name;
    private final int value;
    private final String description;

    /*
     * Public API.
     */

    public final String getName() {
        return name;
    }
    
    public final String getDescription() {
        return this.description;
    }

    public final int toInt() {
        return value;
    }

    public final boolean equals(Object o) {

        boolean rslt = false;   // default...

        if (o != null && o instanceof AccessType) {
            AccessType y = (AccessType) o;
            rslt = y.toInt() == value;
        }

        return rslt;

    }
    
    public boolean compareInt(int value) {
        if (this.toInt() == value)
            return true;
        
        return false;
    }

    public final int hashCode() {
        return value;
    }

    public String toXml() {
        return (new StringBuffer("<accesstype>"))
                    .append(this.value)
                    .append("</accesstype>").toString();
    }

    /*
     * Protected API.
     */

    protected AccessType(String name, int value, String description) {

        // Assertions.
        if (name == null) {
            String msg = "Argument 'name' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (description == null) {
            String msg = "Argument 'description' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.name = name;
        this.value = value;
        this.description = description;

    }    

}
