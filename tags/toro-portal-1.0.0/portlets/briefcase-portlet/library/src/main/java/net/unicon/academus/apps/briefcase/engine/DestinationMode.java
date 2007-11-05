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

package net.unicon.academus.apps.briefcase.engine;

import java.util.HashMap;
import java.util.Map;

public class DestinationMode {

    // Static Members.
    private static final Map instances = new HashMap();

    // Instance Members.
    private String handle;
    private int value;

    /*
     * Public API.
     */

    public static final DestinationMode MOVE = new DestinationMode("move", 0);

    public static final DestinationMode COPY = new DestinationMode("copy", 1);

    public static DestinationMode[] getInstances() {
        return (DestinationMode[]) instances.values().toArray(
                                    new DestinationMode[0]);
    }

    public static DestinationMode getInstance(String handle) {

        // Assertions.
        if (!instances.containsKey(handle)) {
            String msg = "Unable to find destination mode with the specified "
                                                    + "handle:  " + handle;
            throw new IllegalArgumentException(msg);
        }

        return (DestinationMode) instances.get(handle);

    }

    public String getHandle() {
        return handle;
    }

    public int toInt() {
        return value;
    }

    /*
     * Implementation.
     */

    private DestinationMode(String handle, int value) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (instances.containsKey(handle)) {
            String msg = "Argument 'handle' must be unique.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.handle = handle;
        this.value = value;

        // Add to instances collection.
        instances.put(handle, this);

    }

}