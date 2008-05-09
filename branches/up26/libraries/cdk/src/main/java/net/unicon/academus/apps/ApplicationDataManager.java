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
package net.unicon.academus.apps;

import net.unicon.sdk.properties.UniconProperties;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.academus.common.AcademusException;
import net.unicon.academus.common.properties.*;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.academus.domain.lms.OperationFailedException;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.portal.permissions.IPermissions;

import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;


/**
 * This class manages the storage and retrieval of <code>ApplicationData</code>.
 * The present implementation assumes AcademusBaseApp objects are stateful,
 * which is an assumption that needs to be reconsidered going forward.
 */
public class ApplicationDataManager {
    // This maps users to UIDs
    protected static final Hashtable uidTable = new Hashtable();
    protected static final Hashtable applicationData = new Hashtable();
    protected static final List appIdList = new ArrayList();

    /**
     * Checks for the existence of UID.
     * @param appID the application's unique ID
     * @return boolean
     */
    public static boolean appIdExists(String appID) {
        return appIdList.contains(appID);
    }

    /**
     * Factory method as a utility for generating app ids. It is the
     * responsibility of the application itself to determine how it's
     * appId is formed, but this is a convenience method for the most
     * common way to do it
     */
    public static String generateAppId(String clientID,
				       String username,
				       String contextID,
				       String appName)   {
	// XXX - This needs to be a JNDI name in the future
	String appID = clientID + "_" + username + "_" + appName;
	
	if (contextID != null) {
	    appID = appID + "_" + contextID;
	}

	return appID;
    }

    public static void saveApplication(String appID,
				       AcademusBaseApp app) throws AcademusException
    {
	ApplicationData ad = getApplicationData(appID);
	ad.setApplication(app);

	/* DEBUG ONLY
	// net.unicon.common.util.debug.DebugUtil.spillParameters(applicationData, 
							       "State of the ADM0:\n ");
	*/

	registerApplicationData(appID, app.getUsername());
    }

    public static AcademusBaseApp getApplication(String appID) {
	ApplicationData appData = (ApplicationData)applicationData.get(appID);
	if (appData != null) {
	    return appData.getApplication();
	}
	return null;
    }

    public static AcademusBaseApp initializeApplication(String clientID,
							String username,
							String contextID,
							String appName,
							Object[] params)
	throws AcademusException 
    {
	String appID = generateAppId(clientID, username, contextID, appName);
	AcademusBaseApp rval = getApplication(appID);
	if (rval == null) {  // create the app
	    // XXX we need to pool the factories, this reflection will be too expensive
	    // each time
	    // first get the command factory object
	    Class factoryClass = null;
	    UniconProperties props = UniconPropertiesFactory.getManager(AcademusPropertiesType.FACTORY);
	    if (props != null) {
		try {
		    // get the factory method and onvoke to create the app
		    factoryClass = Class.forName(props.getProperty(appName + ".Factory"));
		    String factoryMethod = props.getProperty(appName + ".FactoryMethod");
		    // the argument types to the factory method better be appID, username, contextID
		    // followed by whatever params are being passed along
		    String[] paramTypes = props.getPropertyAsStrings(appName + ".FactoryParamTypes");
		    Class[] argTypes = new Class[3 + paramTypes.length];
		    argTypes[0] = Class.forName("java.lang.String");
		    argTypes[1] = Class.forName("java.lang.String");
		    argTypes[2] = Class.forName("java.lang.String");
		    for (int i = 3; i < argTypes.length; i++) {
			argTypes[i] = Class.forName(paramTypes[i-3]);
		    }
		    Method m = factoryClass.getMethod(factoryMethod, argTypes);
		    Object[] args = new Object[argTypes.length];
		    args[0] = appID;
		    args[1] = username;
		    args[2] = contextID;
		    for (int i = 3; i < args.length; i++) {
			args[i] = params[i-3];
		    }
		    rval = (AcademusBaseApp)m.invoke(null, args);
		    saveApplication(appID, rval);
		}
		catch (Exception exc) {
		    exc.printStackTrace();
		    throw new AcademusException("Error initializing factory for " +
						appName + "Factory");
		}
	    }
	    else {
		throw new AcademusException("Cannot get command factory for " + 
					    appName + ".CommandFactory");
	    }
	}
	return rval;
    }

    public static void removeApplication (String appID) {
	synchronized (applicationData) {
	    ApplicationData appData = (ApplicationData)applicationData.get(appID);

	    removeApplicationUser(appID);
	    if (appData != null) {
		AcademusBaseApp app = appData.getApplication();
		if (app != null) { // should always be true
		    try {
			app.destroy();
		    }
		    catch (Exception e) {
			e.printStackTrace();
			// not worth crashing over
		    }
		}
		appData.setApplication(null);
		applicationData.remove(appID);
	    }
	}
    }

    /**
     * Retrieves a channels generic attribute. This can be any Object type.
     * @param appID the channel's unique ID
     * @param key the key used to retrieve the attribute.
     * @return <{Object}>
     */
    public static Object getAttribute(String appID, Object key) {
        return getApplicationData(appID).getAttribute(key);
    }

    /**
     * Stores a channels generic attribute. This can be any Object type.
     * @param appID the channel's unique ID
     * @param key the key used to retrieve the attribute.
     * @param value the generic value to store.
     */
    public static void putAttribute(String appID, Object key, Object value) {
        getApplicationData(appID).putAttribute(key, value);
    }

    /**
     * Removes a channels generic attribute. This can be any Object type.
     * @param appID the channel's unique ID
     * @param key the key used to retrieve the attribute.
     */
    public static void removeAttribute(String appID, Object key) {
        getApplicationData(appID).removeAttribute(key);
    }

    /**
     * Gets the domain <code>User</code> for the channel identified by the appID
     * @param appID the channel's unique ID
     * @return <{User}>
     */
    public static User getDomainUser(String appID) {
	/*
	ApplicationData ad = getApplicationData(appID);
	try {
	    return ad.getDomainUser();
	}
	catch (Exception e) {
	    // probably gets thrown with any stateful app nowadays
	}
	*/

	/* DEBUG ONLY
	// net.unicon.common.util.debug.DebugUtil.spillParameters(applicationData, 
							       "State of the ADM:\n ");
	*/

	User domainUser = null;
	AcademusBaseApp app = getApplication(appID);
	if (app != null) {
	    String username = app.getUsername();

	    try {
		domainUser = UserFactory.getUser(username);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return domainUser;
    }

    /**
     * Sets the domain <code>User</code> for the channel identified by the appID
     * @param appID the channel's unique ID
     * @param domainUser the channel's domain <code>User</code>
     */
    public static void setDomainUser(String appID, User domainUser) {
        getApplicationData(appID).setDomainUser(domainUser);
    }


    /**
     * Gets an <code>IPermissions</code> for the channel identified by the appID
     * @param appID the channel's unique ID
     * @return <{IPermissions}>
     */
    public static IPermissions getPermissions(String appID) {
        return getApplicationData(appID).getPermissions();
    }
    /**
     * Sets an <code>IPermissions</code> for the channel identified by the appID
     * @param appID the channel's unique ID
     * @param p the channel's permissions object
     */
    public static void setPermissions(String appID, IPermissions p) {
        getApplicationData(appID).setPermissions(p);
    }

    /**
     * Gets the <code>ApplicationData</code> object for the channel identified
     * by the appID
     * @param appID the channel's unique ID
     * @return <{net.unicon.portal.common.cdm.ApplicationData}>
     */
    public static ApplicationData getApplicationData(String appID) {
        synchronized (applicationData) {
            ApplicationData cd = (ApplicationData)applicationData.get(appID);
            if (cd == null) {
                cd = new ApplicationData();
                applicationData.put(appID, cd);
            }
            return cd;
        }
    }

    private static void registerApplicationData(String appID,
					       String username)
	throws AcademusException {
	
        User user = null;
        try {
            user = UserFactory.getUser(username);
        } catch (IllegalArgumentException e) {
            throw new AcademusException(e);
        } catch (ItemNotFoundException e) {
            throw new AcademusException(e);
        } catch (OperationFailedException e) {
            throw new AcademusException(e);
        }

        setDomainUser(appID, user);
	appIdList.add(appID);

        Map userApplicationUIDs = (Map)uidTable.get(user.getUsername());
        if (userApplicationUIDs == null) {
            userApplicationUIDs = new HashMap();
            uidTable.put(user.getUsername(), userApplicationUIDs);
        }
    }

    static void removeApplicationUser(String appID) {
        User user = getDomainUser(appID);
	if (user != null) {
	    Map userApplicationUIDs = (Map)uidTable.get(user.getUsername());
	    if (userApplicationUIDs != null) {
		if (userApplicationUIDs.size() == 0) {
		    uidTable.remove(user.getUsername());
		}
	    }
	    appIdList.remove(appID);
	}
    }

    protected ApplicationDataManager() { }
}
