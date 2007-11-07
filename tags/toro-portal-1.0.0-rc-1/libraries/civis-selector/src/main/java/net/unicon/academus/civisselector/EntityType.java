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

package net.unicon.academus.civisselector;

import java.util.HashMap;
import java.util.Map;

import net.unicon.civis.ICivisFactory;
import net.unicon.civis.fac.AbstractCivisFactory;

public class EntityType {

    // Static Members.
    private static final Map instances = new HashMap();
    
    private static final int MEMBER_VALUE = 0;
    private static final int GROUP_VALUE = 1;

    // Instance Members.
    private String handle;
    private int identityType;

    /*
     * Public API.
     */

    public static final EntityType MEMBER = new EntityType("MEMBER", MEMBER_VALUE);
    public static final EntityType GROUP = new EntityType("GROUP", GROUP_VALUE);

    public static EntityType[] getInstances() {
        return (EntityType[]) instances.values().toArray(
                                    new EntityType[instances.size()]);
    }

    public static EntityType getInstance(String handle) {

        // Assertions.
        if (!instances.containsKey(handle)) {
            String msg = "Unable to find entity type with the specified "
                                                + "handle:  " + handle;
            throw new IllegalArgumentException(msg);
        }

        return (EntityType) instances.get(handle);
    }

    public String getHandle() {
        return handle;
    }

    public int toInt() {
        return identityType;
    }
    
    public static IEntity getEntity(String entityId){
        
        EntityType type = getInstance(entityId.substring(0, entityId.indexOf(":")));
        String facUrl = entityId.substring(entityId.indexOf(":") + 1, entityId.indexOf("@"));
        ICivisFactory fac = AbstractCivisFactory.fromUrl(facUrl);
        String id = entityId.substring(entityId.indexOf("@") + 1);
        IEntity rslt = null;
        
        switch(type.toInt()){
        	case MEMBER_VALUE : rslt = UserEntity.parse(id, fac);
        	    		break;
        	    		
        	case GROUP_VALUE : rslt = GroupEntity.parse(id, fac);
        	    		break;        	    
        }
        return rslt;
    } 
    

    /*
     * Implementation.
     */

    private EntityType(String handle, int value) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (instances.containsKey(handle)) {
            String msg = "Argument 'handle' must be unique.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.handle = handle;
        this.identityType = value;

        // Add to instances collection.
        instances.put(handle, this);

    }

}