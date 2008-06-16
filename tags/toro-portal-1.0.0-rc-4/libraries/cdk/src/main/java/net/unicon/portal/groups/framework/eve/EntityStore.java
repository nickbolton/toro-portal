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

import org.jasig.portal.EntityTypes;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityStore;

/**
 * Reference implementation for IEntityStore.
 * 
 */
public class EntityStore implements IEveEntityStore {
    private static IEntityStore singleton;

    /**
     * EntityStore constructor.
     */
    public EntityStore() {
        super();
    }
    
    /**
     * @return org.jasig.portal.groups.IEntity
     * @param key
     *            java.lang.String
     */
    public IEntity newInstance(String key) throws GroupsException {
        return newInstance(key, null);
    }

    /**
     * @return org.jasig.portal.groups.IEntity
     * @param key
     *            java.lang.String
     * @param type
     *            java.lang.Class
     */
    public IEntity newInstance(String key, Class type) throws GroupsException {
        if (EntityTypes.getEntityTypeID(type) == null) {
            throw new GroupsException("Invalid group type: " + type);
        }
        return new Entity(key, type);
    }

    /**
     * @return org.jasig.portal.groups.IEntityStore
     */
    public static synchronized IEntityStore singleton() {
        if (singleton == null) {
            singleton = new EntityStore();
        }
        return singleton;
    }
}
