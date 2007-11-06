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
package net.unicon.portal.groups.framework.eve.caching;


public class ActionType {
    
    public static final ActionType GROUP_ADD = new ActionType(0, "groupAdd");
    public static final ActionType GROUP_DELETE = new ActionType(1, "groupDelete");
    public static final ActionType GROUP_UPDATE = new ActionType(2, "groupUpdate");
    
    public int getId() {
        return id;
    }
    
    public String getLabel() {
        return label;
    }
    
    public static ActionType getAction(String label) {
        if (GROUP_ADD.getLabel().equals(label)) {
            return GROUP_ADD;
        } else if (GROUP_DELETE.getLabel().equals(label)) {
            return GROUP_DELETE;
        } else if (GROUP_UPDATE.getLabel().equals(label)) {
            return GROUP_UPDATE;
        }
        throw new RuntimeException("Failed to lookup ActionType: " + label);
    }
    
    public String toString() {
        return str;
    }
    
    private String label = null;
    private int id = 0;
    private String str = null;

    private ActionType(int id, String label) {
        this.id = id;
        this.label = label;
        this.str = new StringBuffer("ActionType( ").append(id).append(", ").
        	append(label).append(")").toString();
    }

}
