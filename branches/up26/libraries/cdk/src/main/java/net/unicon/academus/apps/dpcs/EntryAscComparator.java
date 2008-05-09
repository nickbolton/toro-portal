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
package net.unicon.academus.apps.dpcs;

import java.util.Comparator;
import java.util.Date;

import net.unicon.academus.apps.calendar.EntryData;

public class EntryAscComparator implements Comparator {

    public int compare(Object obj1, Object obj2) {
        int rslt = 0;
        EntryData d1 = (EntryData)obj1;
        EntryData d2 = (EntryData)obj2;

        if (d1 != null && d2 != null) {
            try {
                Date d1d = d1.getDuration().getStart();
                Date d2d = d2.getDuration().getStart();

                if (d1d.before(d2d)) {
                    rslt = -1;
                } else if (d1d.equals(d2d)) {
                    long d1l = d1.getDuration().getLength().getLength();
                    long d2l = d2.getDuration().getLength().getLength();
                    if (d1l < d2l) {
                        rslt = -1;
                    } else if (d1l == d2l) {
                        rslt = 0;
                    } else {
                        rslt = 1;
                    }
                } else {
                    rslt = 1;
                }
            } catch (Exception ex) {
                rslt = 0;
            }
        }

        return rslt;
    }
}
