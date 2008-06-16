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

/**
 * @author ibiswas
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EntityType {
    
    private static final int USER_VALUE = 0;
    private static final int GROUP_VALUE = 0;
    
    public static final EntityType USER = new EntityType(USER_VALUE, "User");
    public static final EntityType GROUP = new EntityType(GROUP_VALUE, "Group");
    
    private final int value;
    private final String label;
    
    private EntityType(int value, String label){
        
        this.value = value;
        this.label = label;
    } 
    
    public static EntityType getInstance(String label){
        
        // Assertions
        if(label == null || label.equals("")){
            throw new IllegalArgumentException("Argument 'label' cannot be null or empty.");
        }
        
        if(label.equalsIgnoreCase("user")){
            return USER;
        }else if(label.equalsIgnoreCase("group")){
            return GROUP;
        }else{
            throw new IllegalArgumentException("EntityType with given label was not found.");
        }
    }
    
    public String toString() {
       return this.label;
    }
    
    public int toInt() {
       return this.value;
    }

}
