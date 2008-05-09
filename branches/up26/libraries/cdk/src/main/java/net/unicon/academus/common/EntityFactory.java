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
package net.unicon.academus.common;

import net.unicon.sdk.FactoryCreateException;

public interface EntityFactory {
    // all entity factories should have a static method that looks like
    // public static EntityFactory getEntityFactory()

    /**
     * checks whether the factory implementation supports the entity type
     * @return <code>boolean</code> true if the factory can create it, false otherwise
     * @param <code>Class</code> the entity interface type
     */
    public boolean isEntitySupported(Class entityType);

    /**
     * Lists the set of supported entities by Class type
     * @return <code>Class[]</code> entity types supported by this factory
     */
    public Class[] supportedEntities();

    /**
     * factory object creation method. Return value must be downcasted to the right interface type.
     * @return <code>EntityObject</code> the created object
     * @param <code>int</code> a constant parameter that refers to an entity type to be created.
     * @exception <code>FactoryCreateException</code> a proxy exception for any
     * exceptions created by the entity objects themselves or from other sources.
     * @exception <code>EntityNotSupportedException</code>
     * or if the given factory does not support creating the given entity type
     */
    public EntityObject createEntity(Class entityType)
	throws FactoryCreateException, EntityNotSupportedException;

    /**
     * factory object creation method. Return value must be downcasted to the right interface type.
     * @return <code>EntityObject</code> the created object
     * @param <code>int</code> a constant parameter that refers to an entity type to be created.
     * @param <code>Object[]</param> a set of parameters to pass to the entity constructor.
     * @exception <code>FactoryCreateException</code> a proxy exception for any
     * exceptions created by the entity objects themselves or from other sources.
     * @exception <code>EntityNotSupportedException</code>
     * or if the given factory does not support creating the given entity type
     */
    public EntityObject createEntity(Class entityType, Object[] params)
	throws FactoryCreateException, EntityNotSupportedException;
    // later we might create cacheable factories and other such nonsense, but this is
    // rev 1.0
}
