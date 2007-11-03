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

package net.unicon.academus.apps.messaging;

import java.util.HashMap;

public final class ViewMode {

    // Static Members.
    private static final HashMap states = new HashMap();

    // Instance Members.
    private String handle;
    private int value;

    /*
     * Static API
     */

    public static final ViewMode ALL    = new ViewMode("all"    , 0);
    public static final ViewMode UNREAD = new ViewMode("unread" , 1);
    public static final ViewMode READ   = new ViewMode("read"   , 2);

    public static ViewMode getInstance(String handle) {
        handle = handle.toLowerCase();
        if (!states.containsKey(handle))
            throw new IllegalArgumentException(
                    "Illegal ViewMode: "+handle);
        return (ViewMode)states.get(handle);
    }

    /*
     * Public API.
     */

    public String getHandle() { return this.handle; }
    public String toString() { return this.handle; }
    public int toInt() { return this.value; }

    public String toXml() {
        StringBuffer rslt = new StringBuffer();

        rslt.append("<view>")
            .append(this.toString())
            .append("</view>");

        return rslt.toString();
    }

    /*
     * Implementation.
     */
    private ViewMode(String handle, int value) {
        this.handle = handle;
        this.value = value;

        synchronized(states) {
            states.put(handle, this);
        }
    }
}
