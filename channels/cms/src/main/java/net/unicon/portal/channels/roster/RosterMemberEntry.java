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

package net.unicon.portal.channels.roster;

import net.unicon.academus.common.XMLAbleEntity;
import net.unicon.academus.domain.lms.EnrollmentStatus;
import net.unicon.academus.domain.lms.User;

public class RosterMemberEntry implements XMLAbleEntity {

    private User user = null;
    private EnrollmentStatus status = null;
    private String roleLabel = "";

    public RosterMemberEntry(
                            User user,
                            EnrollmentStatus status,
                            String roleLabel) {

        this.user = user;
        this.status = status;
        this.roleLabel = roleLabel;
    } // end RosterMemberEntry constructor

    protected RosterMemberEntry() {} // protected default constructor

    public String getFirstName() {
        return this.user.getFirstName();
    }

    public String getLastName() {
        return this.user.getLastName();
    }

    public String toXML() {
        StringBuffer rslt = new StringBuffer();

        // open.
        rslt.append("<user id=\"");
        rslt.append(this.user.getUsername());
        rslt.append("\">");

        // first name.
        rslt.append("<firstname>");
        rslt.append(this.user.getFirstName());
        rslt.append("</firstname>");

        // last name.
        rslt.append("<lastname>");
        rslt.append(this.user.getLastName());
        rslt.append("</lastname>");

        // role.
        rslt.append("<role>");
        rslt.append(this.roleLabel);
        rslt.append("</role>");

        // status.
        rslt.append("<status>");
        rslt.append(status.toStringInitialCapital());
        rslt.append("</status>");

        // close.
        rslt.append("</user>");

        return rslt.toString();
    } // end toXML

} // end RosterMemberEntry class
