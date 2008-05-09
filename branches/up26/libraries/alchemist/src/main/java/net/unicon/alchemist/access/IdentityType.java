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

public final class IdentityType {

    // Instance Members.
    private String name;
    private int value;

    /*
     * Public API.
     */

    public static final IdentityType USER = new IdentityType("User", 0);

    public static final IdentityType GROUP = new IdentityType("Group", 1);

    public static IdentityType getInstance(String name) {

        IdentityType rslt = null;

        if (name.equalsIgnoreCase(USER.getName())) {
            rslt = IdentityType.USER;
        } else if (name.equalsIgnoreCase(GROUP.getName())) {
            rslt = IdentityType.GROUP;
        } else {
            String msg = "Unrecognized identity type:  " + name;
            throw new IllegalArgumentException(msg);
        }

        return rslt;

    }

    public static IdentityType getInstance(int value) {

        IdentityType rslt = null;

        switch(value){
            case 0:
                rslt = IdentityType.USER;
                break;

            case 1:
                rslt = IdentityType.GROUP;
                break;
            default:
                String msg = "Unrecognized identity type:  " + value;
                throw new IllegalArgumentException(msg);
        }

        return rslt;

    }

    public String getName() {
        return name;
    }

    public int toInt() {
        return value;
    }

    /*
     * Protected API.
     */

    private IdentityType(String name, int value) {

        // Assertions.
        if (name == null) {
            String msg = "Argument 'name' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.name = name;
        this.value = value;

    }

}