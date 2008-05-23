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

package net.unicon.mercury;

import java.util.HashMap;

/**
 * Priority specification for a message.
 * @see IMessage#getPriority()
 */
public final class Priority implements Comparable
{
    // Static members.
    private static final HashMap instances = new HashMap();

    public static final Priority PRIORITY_HIGH   = new Priority("High"   , 1);
    public static final Priority PRIORITY_MEDIUM = new Priority("Medium" , 2);
    public static final Priority PRIORITY_LOW    = new Priority("Low"    , 3);

    private static final Priority[] priorities =
                    { PRIORITY_HIGH, PRIORITY_MEDIUM, PRIORITY_LOW };

    // Instance members.
    private final String label;
    private final int value;
    
    /*
     * Public API.
     */

    public static Priority getInstance(int value) {
        if (value > priorities.length || value <= 0)
            throw new IllegalArgumentException(
                    "Illegal Priority value: "+ value);
        return priorities[value-1];
    }

    public static Priority getInstance(String label) {
        label = label.trim().toLowerCase();
        if (!instances.containsKey(label))
            throw new IllegalArgumentException(
                    "Illegal Priority value: "+ label);
        return (Priority)instances.get(label);
    }

    public int toInt() { return this.value; }
    public String getLabel() { return this.label; }
    public String toString() { return getLabel(); }
    public String toXml() {
        return (new StringBuffer("<priority>"))
                .append(toInt())
                .append("</priority>")
                .toString();
    }

    public int compareTo(Object o) {
        if (!(o instanceof Priority))
            throw new IllegalArgumentException(
                    "Only a Priority can be compared with a Priority.");
        int p1 = this.toInt();
        int p2 = ((Priority)o).toInt();
        return (p1 == p2 ? 0 : ( p1 > p2 ? 1 : -1));
    }

    /*
     * Implementation.
     */

    private Priority(String label, int value) {
        this.label = label;
        this.value = value;

        synchronized(instances) {
            instances.put(label.toLowerCase(), this);
        }
    }

}
