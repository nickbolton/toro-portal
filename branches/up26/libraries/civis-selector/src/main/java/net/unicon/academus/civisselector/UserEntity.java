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
import net.unicon.civis.IPerson;

public class UserEntity implements IEntity{

    private IPerson user = null;
    private String entityId = null;
    
    public UserEntity(IPerson user){
        
        // Assertions
        if(user == null){
            throw new IllegalArgumentException("Argument 'user' cannot be null.");
        }
        this.user = user;
        this.entityId = getEntityId(user);
    }   
    
    public String getId() {        
        return user.getName();
    }
    
    public String getEntityId() {        
        return this.entityId;
    }

    public EntityType getType() {
        return EntityType.MEMBER;
    }

    public Object getObject() {
        return user;
    }
    
    public static IEntity parse(String username, ICivisFactory fac){
        return new UserEntity(fac.getPerson(username));
    }
    
    public static String getEntityId(IPerson u) {        
        StringBuffer id = new StringBuffer(EntityType.MEMBER.getHandle()).append(":");
        // append the fac url
        id.append(u.getOwner().getUrl());
        // append the separator 
        id.append("@");
        // append the username
        id.append(u.getName());
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
