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

import net.unicon.civis.ICivisFactory;
import net.unicon.civis.IGroup;

public class GroupEntity implements IEntity{

    private IGroup group = null;
    private String entityId = null;
    
    public GroupEntity(IGroup group){
        
        // Assertions
        if(group == null){
            throw new IllegalArgumentException("Argument 'group' cannot be null.");
        }
        this.group = group;
        this.entityId = getEntityId(group);
    }   
    
    /* (non-Javadoc)
     * @see net.unicon.academus.apps.briefcase.ISharee#getId()
     */
    public String getId() {        
    	StringBuffer id = new StringBuffer();
    	id.append(this.group.getId());
        return id.toString();
    }
    
    /* (non-Javadoc)
     * @see net.unicon.academus.apps.briefcase.ISharee#getId()
     */
    public String getEntityId() {        
        return this.entityId;
    }

    /* (non-Javadoc)
     * @see net.unicon.academus.apps.briefcase.ISharee#getType()
     */
    public EntityType getType() {
        return EntityType.GROUP;
    }

    /* (non-Javadoc)
     * @see net.unicon.academus.apps.briefcase.ISharee#getObject()
     */
    public Object getObject() {
        return group;
    }
    
    public static IEntity parse(String groupId, ICivisFactory fac){
    	return new GroupEntity(fac.getGroupById(groupId));
    }
    
    public static String getEntityId(IGroup g){
        StringBuffer id = new StringBuffer(EntityType.GROUP.getHandle()).append(":");
        // append the fac url
        id.append(g.getOwner().getUrl());
        // append the separator 
        id.append("@");
        // append the group path
        id.append(g.getId());
        
        return id.toString();
    }

    public boolean equals(Object o){
        if(!(o instanceof IEntity))
            return false;
        if(this.entityId.equals(((IEntity)o).getEntityId())){
            return true;
        }
        return false;
    }
        
    public int hashcode(){
        return this.entityId.hashCode();
    }
   
}
