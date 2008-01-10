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

package net.unicon.demetrius;

/**
 * Enumeration class to indentify the types of resources 
 * supported by the factory.
 * @author gtrujillo
 * 
 */
public class ResourceType {

    // Instance members.
    private final int type;
    private final String name;

    /*
     * Public API.
     */

    public static final int FOLDER_SWITCHABLE = 1;
    public static final int FILE_SWITCHABLE = 2;

    public static final ResourceType FOLDER 
    				= new ResourceType(FOLDER_SWITCHABLE, "Folder");
    public static final ResourceType FILE   
    				= new ResourceType(FILE_SWITCHABLE, "File");

    public int toInt() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static ResourceType[] getAllInstances() {
        return new ResourceType[]{FOLDER, FILE};
    }

    /*
     * Implementation.
     */

    private ResourceType(int type, String name) {

        // Assertions.
        if (name == null) {
            String msg = "Arugument 'name' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.name = name;
        this.type = type;

    }

}
