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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public abstract class AEntityFactory implements EntityFactory {

    protected static HashMap entitiesSupported = null;

    // You probably want something like this in your class
    // so you can have your factory be a Singleton
    // private static EntityFactory myFactory = null;

    protected void init(String[] classes, String[] impls) 
	throws FactoryCreateException {

	if (classes.length != impls.length) {
	    throw new FactoryCreateException("Number of classes must equals number of impls");
	}

        // instantiate the set of classes I can create
        entitiesSupported = new HashMap(classes.length*2);

        try {
	    for (int i = 0; i < classes.length; i++) {
		entitiesSupported.put(Class.forName(classes[i]), Class.forName(impls[i]));
	    }
        }
        catch (Exception exc) {
            exc.printStackTrace();
	    throw new FactoryCreateException("Error initializing factory " + this.getClass(), exc);
        }
    }

    // all entity factories should have a static method that looks like
    // public static EntityFactory getEntityFactory() {
    //    if (myFactory == null) {
    //        myFactory = new AEntityFactoryImpl();
    //    }
    //
    //    return myFactory;
    // }

    /**
     * checks whether the factory implementation supports the entity type
     * @return <code>boolean</code> true if the factory can create it, false otherwise
     * @param <code>Class</code> the entity interface type
     */
    public boolean isEntitySupported(Class entityType) {

        return (entitiesSupported.get(entityType) != null);

    }

    /**
     * Lists the set of supported entities by Class type
     * @return <code>Class[]</code> entity types supported by this factory
     */
    public Class[] supportedEntities() {
        Set entitySet = entitiesSupported.keySet();
        Class[] rval = new Class[entitySet.size()];

        int i = 0;

        for (Iterator it = entitySet.iterator(); i < rval.length && it.hasNext(); ) {
            rval[i++] = (Class)it.next();
        }

        return rval;
    }

    public EntityObject createEntity(Class entityType)
	throws FactoryCreateException, EntityNotSupportedException {
	return createEntity(entityType, null);
    }

    public abstract EntityObject createEntity(Class entityType, Object[] params)
	throws FactoryCreateException, EntityNotSupportedException;

    /**
     * factory object creation method. Return value must be downcasted to the right interface type.
     * @return <code>EntityObject</code> the created object
     * @param <code>int</code> a constant parameter that refers to an entity type to be created.
     * @exception <code>FactoryCreateException</code> a proxy exception for any
     * exceptions created by the entity objects themselves or from other sources.
     * @exception <code>EntityNotSupportedException</code>
     * or if the given factory does not support creating the given entity type
     */
    protected Constructor createEntityMethod(Class entityType)
	throws FactoryCreateException, EntityNotSupportedException {

        // convenience method
        return createEntityMethod(entityType, null);
    }

    /**
     * factory object creation method. Return value must be downcasted to the right interface type.
     * @return <code>Constructor</code> method to the object to create
     * @param <code>int</code> a constant parameter that refers to an entity type to be created.
     * @param <code>Object[]</param> a set of parameters to pass to the entity constructor.
     * @exception <code>FactoryCreateException</code> a proxy exception for any
     * exceptions created by the entity objects themselves or from other sources.
     * @exception <code>EntityNotSupportedException</code>
     * or if the given factory does not support creating the given entity type
     */
    protected Constructor createEntityMethod(Class entityType, Object[] params)
	throws FactoryCreateException, EntityNotSupportedException {

        // get the Class type
        Class baseType = (Class)entitiesSupported.get(entityType);

        if (baseType == null) {
            throw new EntityNotSupportedException("Entity type " + entityType +
            " not supported by this factory");

        }

        try {
            // if params == null, call a default constructor
            Class[] paramClasses = null;
            if (params != null && params.length > 0) {
                paramClasses = new Class[params.length];
                for (int i = 0; i < paramClasses.length; i++) {
                    paramClasses[i] = params[i].getClass();
                }
            }

            return (baseType.getDeclaredConstructor(paramClasses));
        }

        catch (Exception exc) {
            exc.printStackTrace();

            throw new FactoryCreateException("Constructor parameter type mismatch");
        }
    }
}

