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
package net.unicon.sdk.guid;

import java.io.Serializable;

/**
 * Represents the context that a GUID is to be created for.
 *
 * A GUID may only need to be unique to a specific data source, such as
 * a database table.
 */
public class Context implements Serializable {
    private String name = null;
    
    /** constructor */
    public Context(String name) {
        this.name = name;
    }

    /**
     * Provides the name of this context.
     *
     * Name would be could be the name of a database table.
     *
     * @return Name for this context.
     */
    public String getName() {
        return name;
    }

} // end Context
