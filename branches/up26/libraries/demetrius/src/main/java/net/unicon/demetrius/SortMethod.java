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

package net.unicon.demetrius;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.unicon.demetrius.fac.DateAscComparator;
import net.unicon.demetrius.fac.DateDescComparator;
import net.unicon.demetrius.fac.NameAscComparator;
import net.unicon.demetrius.fac.NameDescComparator;
import net.unicon.demetrius.fac.SizeAscComparator;
import net.unicon.demetrius.fac.SizeDescComparator;
import net.unicon.demetrius.fac.TypeAscComparator;
import net.unicon.demetrius.fac.TypeDescComparator;

public class SortMethod {

    // Static Members.
    private static final Map instances = new HashMap();

    // Instance Members.
    private final String mode;
    private final String direction;
    private final int switchable;

    /*
     * Public API.
     */

    public static final SortMethod NAME_ASCENDING  
    								= new SortMethod("name", "asc", 0);
    public static final SortMethod NAME_DESCENDING 
    								= new SortMethod("name", "des", 1);
    public static final SortMethod DATE_ASCENDING  
    								= new SortMethod("date", "asc", 2);
    public static final SortMethod DATE_DESCENDING 
    								= new SortMethod("date", "des", 3);
    public static final SortMethod TYPE_ASCENDING  
    								= new SortMethod("type", "asc", 4);
    public static final SortMethod TYPE_DESCENDING 
    								= new SortMethod("type", "des", 5);
    public static final SortMethod SIZE_ASCENDING  
    								= new SortMethod("size", "asc", 6);
    public static final SortMethod SIZE_DESCENDING 
    								= new SortMethod("size", "des", 7);

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
                rslt = NAME_DESCENDING;
                break;
            case 1:
                rslt = NAME_ASCENDING;
                break;
            case 2:
                rslt = DATE_DESCENDING;
                break;
            case 3:
                rslt = DATE_ASCENDING;
                break;
            case 4:
                rslt = TYPE_DESCENDING;
                break;
            case 5:
                rslt = TYPE_ASCENDING;
                break;
            case 6:
                rslt = SIZE_DESCENDING;
                break;
            case 7:
                rslt = SIZE_ASCENDING;
                break;
            default:
                String msg = "Unrecognized SortMethod:  " + method.getHandle();
                throw new RuntimeException(msg);
        }

        return rslt;

    }

    public String getHandle() {
        return mode + "-" + direction;
    }

    public String getMode() {
        return mode;
    }

    public String getDirection() {
        return direction;
    }

    public int toInt() {
        return switchable;
    }
    
    public Comparator getComparator(){
        Comparator c = null;
        switch (this.toInt()){
            case 0 : c = new NameAscComparator();
                                        break;
            case 1 : c = new NameDescComparator();
                                        break;
            case 2 : c = new DateAscComparator();
            break;
            case 3 : c = new DateDescComparator();
            break;
            case 4 : c = new TypeAscComparator();
            break;
            case 5 : c = new TypeDescComparator();
            break;
            case 6 : c = new SizeAscComparator();
            break;
            case 7 : c = new SizeDescComparator();
            break;

        }        
        return c;
    }

    /*
     * Implementation.
     */

    private SortMethod(String mode, String direction, int switchable) {

        // Assertions.
        if (mode == null) {
            String msg = "Argument 'mode' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (direction == null) {
            String msg = "Argument 'direction' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.mode = mode;
        this.direction = direction;
        this.switchable = switchable;

        instances.put(mode + "-" + direction, this);

    }

}
