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

package net.unicon.academus.apps.briefcase;

import net.unicon.alchemist.access.AccessRule;

/**
 * 
 * @author ibiswas
 *
 * Helper class to represent the permissions that a Civis Entity has
 * on a specified target.
 */
public class CivisAccessEntry {

    private final String entityId; 
    private AccessRule[] rules;
    private boolean isNew;
    
    public CivisAccessEntry(String entityId, AccessRule[] rules, boolean isNew){
        
        // assertions 
        if(entityId == null){
            throw new IllegalArgumentException("Argument 'entityId' cannot be null.");
        }
        if(rules == null){
            throw new IllegalArgumentException("Argument 'rules' cannot be null.");
        }
            
        this.entityId = entityId;
        this.rules = new AccessRule[rules.length];
        System.arraycopy(rules, 0, this.rules, 0, rules.length);
        this.isNew = isNew;
    }
    
    public String getEntityId(){
        return this.entityId;
    }
    
    public AccessRule[] getAccessRules(){
        return this.rules;
    }
    
    public void setAccessRules(AccessRule[] rules){
        this.rules = new AccessRule[rules.length];
        System.arraycopy(rules, 0, this.rules, 0, rules.length);
    }
    
    public boolean isNew(){
        return isNew;
    }
}
