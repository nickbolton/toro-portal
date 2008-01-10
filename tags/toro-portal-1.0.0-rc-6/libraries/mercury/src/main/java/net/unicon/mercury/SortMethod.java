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

import net.unicon.mercury.fac.AttachNumAscComparator;
import net.unicon.mercury.fac.AttachNumDescComparator;
import net.unicon.mercury.fac.DateAscComparator;
import net.unicon.mercury.fac.DateDescComparator;
import net.unicon.mercury.fac.MsgRecipientAscComparator;
import net.unicon.mercury.fac.MsgRecipientDescComparator;
import net.unicon.mercury.fac.PriorityAscComparator;
import net.unicon.mercury.fac.PriorityDescComparator;
import net.unicon.mercury.fac.SenderAscComparator;
import net.unicon.mercury.fac.SenderDescComparator;
import net.unicon.mercury.fac.SubjectAscComparator;
import net.unicon.mercury.fac.SubjectDescComparator;

public class SortMethod {

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

    public static final SortMethod SUBJECT_ASCENDING   = new SortMethod("subject" , "asc", 0, new SubjectAscComparator());
    public static final SortMethod SUBJECT_DESCENDING  = new SortMethod("subject" , "des", 1, new SubjectDescComparator());
    public static final SortMethod DATE_ASCENDING      = new SortMethod("date"    , "asc", 2, new DateAscComparator());
    public static final SortMethod DATE_DESCENDING     = new SortMethod("date"    , "des", 3, new DateDescComparator());
    public static final SortMethod SENDER_ASCENDING    = new SortMethod("sender"  , "asc", 4, new SenderAscComparator());
    public static final SortMethod SENDER_DESCENDING   = new SortMethod("sender"  , "des", 5, new SenderDescComparator());
    public static final SortMethod PRIORITY_ASCENDING  = new SortMethod("priority", "asc", 6, new PriorityAscComparator());
    public static final SortMethod PRIORITY_DESCENDING = new SortMethod("priority", "des", 7, new PriorityDescComparator());
    public static final SortMethod ATTACH_ASCENDING  = new SortMethod("attach", "asc", 8, new AttachNumAscComparator());
    public static final SortMethod ATTACH_DESCENDING = new SortMethod("attach", "des", 9, new AttachNumDescComparator());
    public static final SortMethod RECIPIENT_ASCENDING    = new SortMethod("recipient"  , "asc", 10, new MsgRecipientAscComparator());
    public static final SortMethod RECIPIENT_DESCENDING   = new SortMethod("recipient"  , "des", 11, new MsgRecipientDescComparator());


    public static SortMethod getInstance(String handle) {
        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!instances.containsKey(handle)) {
            String msg = "The specified SortMethod is not defined:  " + handle;
            throw new IllegalArgumentException(msg);
        }

        return (SortMethod) instances.get(handle);

    }

    public static SortMethod reverse(SortMethod method) {
        // Assertions.
        if (method == null) {
            String msg = "Argument 'method' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        SortMethod rslt = null;

        switch (method.toInt()) {
            case 0:
                rslt = SUBJECT_DESCENDING;
                break;
            case 1:
                rslt = SUBJECT_ASCENDING;
                break;
            case 2:
                rslt = DATE_DESCENDING;
                break;
            case 3:
                rslt = DATE_ASCENDING;
                break;
            case 4:
                rslt = SENDER_DESCENDING;
                break;
            case 5:
                rslt = SENDER_ASCENDING;
                break;
            case 6:
                rslt = PRIORITY_DESCENDING;
                break;
            case 7:
                rslt = PRIORITY_ASCENDING;
                break;
            case 8:
                rslt = ATTACH_DESCENDING;
                break;
            case 9:
                rslt = ATTACH_ASCENDING;
                break;
            case 10:
                rslt = RECIPIENT_DESCENDING;
                break;
            case 11:
                rslt = RECIPIENT_ASCENDING;
                break;
            default:
                String msg = "Unrecognized SortMethod:  " + method.getHandle();
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

    private SortMethod(String mode, String direction, int switchable, Comparator comparator) {

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
