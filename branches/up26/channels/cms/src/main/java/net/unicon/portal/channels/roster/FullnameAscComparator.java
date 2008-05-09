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

import java.util.Comparator;
import java.util.List;

class FullnameAscComparator implements Comparator {

    public int compare(Object o1, Object o2) {

        RosterMemberEntry entry1 = (RosterMemberEntry)o1;
        RosterMemberEntry entry2 = (RosterMemberEntry)o2;

        int result = (entry1.getLastName().compareToIgnoreCase(
            entry2.getLastName()));

        if (result == 0) {
            result = (entry1.getFirstName().compareToIgnoreCase(
                entry2.getFirstName()));
        }

        return result;
    }

    public boolean equals(Object obj) {

        return obj.equals(this);

    }

} // end FullnameAscComparator class
