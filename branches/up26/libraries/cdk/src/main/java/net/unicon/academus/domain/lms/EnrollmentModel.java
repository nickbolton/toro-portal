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
import java.util.ArrayList;
public final class EnrollmentModel {
    /* Static Members */
    private static Map instances = new HashMap();

    /**
     * A facilitator must enroll all members of the offering.
     */
    public static final EnrollmentModel FACILITATOR =
                new EnrollmentModel("Facilitator");

    /**
     * Anyone can enroll/un-enroll themselves in the offering.
     */
    public static final EnrollmentModel OPEN = new EnrollmentModel("Open");

    /**
     * Anyone may request enrollment, but facilitator must approve.
     */
    public static final EnrollmentModel REQUESTAPPROVE =
                new EnrollmentModel("Request/Approve");

    /**
     * Enrollment is handled be an external system (Student Information System).
     */
    public static final EnrollmentModel SIS = new EnrollmentModel("Student Information System");

    /**
     * Provides the enrollment model that was created with the specified label.
     * @param label the label of the desired enrollment model.
     * @return the enrollment model with the specified label.
     * @throws ItemNotFoundException if the instance isn't found.
     */
    public static EnrollmentModel getInstance(String label)
    throws ItemNotFoundException {
        // The rtn.
        EnrollmentModel rtn = (EnrollmentModel) instances.get(label);
        if (rtn == null) {
            String msg = "There is no enrollment model with label=" + label;
            throw new ItemNotFoundException(msg);
        }
        return rtn;
    }
    /* Instance Members */
    private String label = null;
    private EnrollmentModel(String label) {
        this.label = label;
        instances.put(label, this);
    }
    /**
     * Provides the label of this enrollment model.  This information is useful for display purposes.
     * @return the enrollment model in string form.
     */
    public String toString() {
        return label;
    }
    /** Returns an ArrayList of all enrollment model names */
    public static ArrayList getEnrollmentModels() // open enrollment
    {
        return new ArrayList(instances.keySet());
    }
}
