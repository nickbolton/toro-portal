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
package net.unicon.portal.groups.framework.eve;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IGroupMember;

/**
 * @author nbolton
 */
public class Entity extends AbstractGroupMember implements IEntity {
    
    protected EntityIdentifier entityIdentifier;
    
    private int hc;

	public Entity(String newEntityKey, Class newEntityType) throws GroupsException {
	    this(new EntityIdentifier(newEntityKey, newEntityType));
	}
	
	public Entity(EntityIdentifier ei) throws GroupsException {
	    super(ei);
	    Integer id = org.jasig.portal.EntityTypes.getEntityTypeID(ei.getType());
	    String key = id + "." + ei.getKey();
	    entityIdentifier = new EntityIdentifier(key, org.jasig.portal.EntityTypes.LEAF_ENTITY_TYPE);
	    
	    hc = key.hashCode();
	}
	
	public boolean isEntity() {
	    return true;
	}
	
    public int hashCode() {
        return hc;
    }
    
    public EntityIdentifier getEntityIdentifier() {
        return entityIdentifier;
    }
    
    public Class getEntityType()
    {
        return getUnderlyingEntityIdentifier().getType();
    }
    
    public Class getLeafType()
    {
        return getEntityType();
    }
    
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof Entity))
            return false;

        Entity g = (Entity)obj;
            
        return getEntityIdentifier().getType().equals(g.getEntityIdentifier().getType()) &&
        	getEntityIdentifier().getKey().equals(g.getEntityIdentifier().getKey());
    }
    
	public String toString() {
	    String clsName = getEntityType().getName();
	    return "Entity (" + clsName + ") "  + getKey();
	}
	
	public boolean isDeepMemberOf(IGroupMember member) throws GroupsException {
		return getEveGroupService().isDeepMemberOf(this, member);
	}

	public boolean isMemberOf(IGroupMember member) throws GroupsException {
		return getEveGroupService().isMemberOf(this, member);
	}

}
