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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.unicon.mercury.fac.RecipientAscComparator;
import net.unicon.mercury.fac.RecipientDescComparator;
import net.unicon.mercury.fac.StatusAscComparator;
import net.unicon.mercury.fac.StatusDescComparator;

public class RecipientSortMethod {

    // Static Members.
    private static final Map instances = new HashMap();

    // Instance Members.
    private final String mode;
    private final String handle;
    private final String direction;
    private final int switchable;
    private final Comparator comparator;

    /*
     * Public API.
     */

    public static final RecipientSortMethod RECIPIENT_ASCENDING  = new RecipientSortMethod("name", "asc", 0, new RecipientAscComparator());
    public static final RecipientSortMethod RECIPIENT_DESCENDING = new RecipientSortMethod("name", "des", 1,  new RecipientDescComparator());
    public static final RecipientSortMethod STATUS_ASCENDING  = new RecipientSortMethod("status", "asc", 2, new StatusAscComparator());
    public static final RecipientSortMethod STATUS_DESCENDING = new RecipientSortMethod("status", "des", 3,  new StatusDescComparator());

    public static RecipientSortMethod getInstance(String handle) {
        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!instances.containsKey(handle)) {
            String msg = "The specified SortMethod is not defined:  " + handle;
            throw new IllegalArgumentException(msg);
        }

        return (RecipientSortMethod) instances.get(handle);

    }

    public static RecipientSortMethod reverse(RecipientSortMethod method) {
        // Assertions.
        if (method == null) {
            String msg = "Argument 'method' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        RecipientSortMethod rslt = null;

        switch (method.toInt()) {
            case 0:
                rslt = RECIPIENT_DESCENDING;
                break;
            case 1:
                rslt = RECIPIENT_ASCENDING;
                break;
            case 2:
                rslt = STATUS_DESCENDING;
                break;
            case 3:
                rslt = STATUS_ASCENDING;
                break;
            default:
                String msg = "Unrecognized RecipientSortMethod:  " + method.getHandle();
                throw new RuntimeException(msg);
        }

        return rslt;

    }

    public String getHandle() {
        return handle;
    }

    public String getMode() {
        return mode;
    }

    public String getDirection() {
        return direction;
    }

    public Comparator getComparator() {
        return comparator;
    }

    public int toInt() {
        return switchable;
    }

    /*
     * Implementation.
     */

    private RecipientSortMethod(String mode, String direction, int switchable, Comparator comparator) {

        // Assertions.
        if (mode == null) {
            String msg = "Argument 'mode' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (direction == null) {
            String msg = "Argument 'direction' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (comparator == null) {
            String msg = "Argument 'comparator' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.mode = mode;
        this.direction = direction;
        this.handle = mode + "-" + direction;
        this.switchable = switchable;
        this.comparator = comparator;

        synchronized(instances) {
            instances.put(this.handle, this);
        }
    }
}
