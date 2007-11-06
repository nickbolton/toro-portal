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

package net.unicon.academus.apps.content;

import java.util.HashMap;
import java.util.Map;

import net.unicon.alchemist.access.AccessType;

public class WebContentAccessType extends AccessType {

    private static Map instances = new HashMap();

//    private static final int ADMIN_VALUE = 0;
    private static final int VIEW_VALUE = 1;

//    public static final WebContentAccessType ADMIN = new WebContentAccessType("ADMIN", ADMIN_VALUE, "");
    public static final WebContentAccessType VIEW = new WebContentAccessType("VIEW", VIEW_VALUE, "");

    public static WebContentAccessType getAccessType(int type) {

        WebContentAccessType rslt = null;

        switch (type) {

//            case ADMIN_VALUE:
//                rslt = ADMIN;
//                break;
            case VIEW_VALUE:
                rslt = VIEW;
                break;
            default:
                String msg = "Unrecognized Access Type Value:  " + type;
                throw new IllegalArgumentException(msg);

        }

        return rslt;

    }

    public static WebContentAccessType[] getInstances(){
        return (WebContentAccessType[]) instances.values()
                .toArray(new WebContentAccessType[0]);
    }

    public static WebContentAccessType getAccessType(String name) {
        WebContentAccessType rslt = (WebContentAccessType) instances.get(name);
        if (rslt == null) {
            String msg = "Unrecognized Access Type:  " + name;
            throw new IllegalArgumentException(msg);
        }
        return rslt;
    }

    private WebContentAccessType(String name, int type, String description) {
        super(name, type, description);
        instances.put(name, this);
    }

}
