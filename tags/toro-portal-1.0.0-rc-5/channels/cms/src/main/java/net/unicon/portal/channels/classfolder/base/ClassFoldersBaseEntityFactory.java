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
package net.unicon.portal.channels.classfolder.base;

import java.lang.reflect.Constructor;

import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.academus.common.AEntityFactory;
import net.unicon.academus.common.EntityFactory;
import net.unicon.academus.common.EntityNotSupportedException;
import net.unicon.academus.common.EntityObject;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.sdk.FactoryCreateException;

/**
This class is responsible for creating entity objects in the
Class Folders channel's base implementation package. It uses the
default implementation provided in net.unicon.academus.common.AEntityFactory,
so there is not much code here!
*/
public class ClassFoldersBaseEntityFactory extends AEntityFactory {

    // It is OK to use constants for the classes we support. Typically when
    // you add new objects to the system you expect a factory that creates
    // them to know about them explicitly as they manage the object's
    // lifecycle, and may invoke special biz rules upon creating that
    // object. If there is no special behavior you could property-ize.
    String[] myInterfaces = { "net.unicon.portal.channels.classfolder.ClassFolders" };
    String[] myImpls      = { "net.unicon.portal.channels.classfolder.base.ClassFoldersImpl" };

    // As recommended by my parent, I am a singleton
    private static EntityFactory myFactory = null;

    // The constructor is private as I am a Singleton
    private ClassFoldersBaseEntityFactory() throws FactoryCreateException {
	super.init(myInterfaces, myImpls);
    }

    // Singleton method
    public static EntityFactory getEntityFactory() {
        if (myFactory == null) {
	    try {
		myFactory = new ClassFoldersBaseEntityFactory();
	    }
	    catch (FactoryCreateException fce) {
		fce.printStackTrace();
	    }
        }
    
        return myFactory;
    }

    // This method has to be in the package scope of the entity since
    // it is actually invoking the constructor
    public EntityObject createEntity(Class entityType, Object[] params)
	throws FactoryCreateException, EntityNotSupportedException {

	Constructor constructor = null;
	
	try {
	    // now for some example business logic. ClassFoldersImpl is
	    // an object whose state is represented directly as XML
	    // therefore lets see if we need to validate
	    if (params.length < 3) {
		// flag is the 3rd param, so we check if somebody else has already included it
		Object[] fparams = new Object[params.length+1];
		for (int i = 0; i < params.length; i++) {
		    fparams[i] = params[i];
		}
		String prop = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("entityXMLValidation");
		if (prop.equalsIgnoreCase("true")) {
		    fparams[fparams.length-1] = Boolean.TRUE;
		}
		else {
		    fparams[fparams.length-1] = Boolean.FALSE;
		}
		params = fparams;
	    }
	    // This is the work. Get the right constructor then call it.
	    constructor = super.createEntityMethod(entityType, params);

	    return (EntityObject)constructor.newInstance(params);
	}
	catch (Exception exc) {
	    exc.printStackTrace();
	    throw new FactoryCreateException(exc);
	}
    }

    // unit test
    public static void main(String[] args) {
	// quick test stuff
	Object[] params = new Object[2];
	params[0] = "";
	params[1] = "1";
	Object[] params2 = new Object[3];
	params2[0] = "";
	params2[1] = "2";
	params2[2] = Boolean.FALSE;
	Object[] params3 = new Object[3];
	params3[0] = ">this is not a valid XML string<";
	params3[1] = "3";
	params3[2] = Boolean.TRUE;

	EntityFactory cfbef = getEntityFactory();
	EntityObject po  = null;
	EntityObject po2 = null;
	EntityObject po3 = null;
	Class[] supportedEntities = cfbef.supportedEntities();

	System.out.println("Factory: " + cfbef.getClass());
	for (int i = 0; i < supportedEntities.length; i++) {
	    try {
		System.out.println(supportedEntities[i] + " supported");
		if (cfbef.isEntitySupported(supportedEntities[i])) {
		    System.out.println("isEntitySupported returns true");
		}
		else {
		    System.out.println("isEntitySupported returns false");
		}
		// we know our objects here take no args for a constructor
		po = cfbef.createEntity(supportedEntities[i], params);
		System.out.println("Created object of type " + po.getClass());
		po2 = cfbef.createEntity(supportedEntities[i], params2);
		System.out.println("Created object of type " + po2.getClass());
		System.out.println("should fail here");
		po3 = cfbef.createEntity(supportedEntities[i], params3);
		System.out.println("Created object of type " + po3.getClass());
	    }
	    catch (Exception e) {
		e.printStackTrace();
	    }
	}
	System.exit(0);
    }
}
