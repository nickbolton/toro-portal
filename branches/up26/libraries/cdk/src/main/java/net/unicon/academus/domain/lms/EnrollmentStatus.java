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
package net.unicon.academus.domain.lms;

import net.unicon.academus.domain.ItemNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
/**
 * Represents the different enrollment status realms that can be adopted by an
 * enrollment model within an offering.  This class follows the type-safe
 * enumeration pattern (see Effective Java).  Consequently, all possible instances of
 * <code>EnrollmentStatus</code> are static members of the class itself. Examples include: <ul> <li>ENROLLED</li>
 * <li>PENDING</li> <li>INVITED</li> </ul>
 */
public final class EnrollmentStatus {
    // Static Members
    private static Map instances = new HashMap();
    private static List orderedInstances = new ArrayList();
    // Enrollment Status for open and facilitator based enrollment models,
    // and for any and all approved requests from the Request/Approve
    // enrollment model.
    public static final EnrollmentStatus ENROLLED =
    new EnrollmentStatus("ENROLLED", 1);
    // Enrollment Status for any and all pending requests for the
    // Request/Approve enrollment model.
    public static final EnrollmentStatus PENDING =
    new EnrollmentStatus("PENDING", 2);
    // Enrollment Status for any and all pending invites for the Invite-Only
    // enrollment model.
    public static final EnrollmentStatus INVITED =
    new EnrollmentStatus("INVITED", 3);
    // Instance Members
    private final int _intVal;
    private final String _strVal;
    // Constructor
    private EnrollmentStatus (String strVal, int intVal) {
        this._strVal = strVal;
        this._intVal = intVal;
        instances.put(strVal, this);
        if (orderedInstances.size() == 0) orderedInstances.add(null);
        orderedInstances.add(intVal, this);
    }
     /* Utility Methods */
    // toInt()
    public final int toInt() {
        return _intVal;
    }
    // toString()
    public final String toString() {
        return _strVal;
    }
    public final String toStringInitialCapital() {
        return _strVal.substring(0, 1).toUpperCase()
        + _strVal.substring(1).toLowerCase();
    }
    // getInstance()
    public static EnrollmentStatus getInstance (String strVal)
    throws ItemNotFoundException {
        // Establish return value
        EnrollmentStatus rtn = (EnrollmentStatus) instances.get(strVal);
        if (rtn == null) {
            String msg = "There is not an enrollment status with label=" +
            strVal;
            throw new ItemNotFoundException(msg);
        }
        return rtn;
    }
    public static EnrollmentStatus getInstance(int intVal)
    throws ItemNotFoundException {
        // Establish return value
        EnrollmentStatus rtn = null;
        try {
            rtn = (EnrollmentStatus) orderedInstances.get(intVal);
        } catch (IndexOutOfBoundsException obe) {
            String msg = "There is not an enrollment status with intVal=" +
            intVal;
            throw new ItemNotFoundException(msg);
        }
        return rtn;
    }
    // getEnrollmentStatus() - returns all possible enrollment status labels
    public static ArrayList getEnrollmentStatus() {
        return new ArrayList (instances.keySet());
    }
}
