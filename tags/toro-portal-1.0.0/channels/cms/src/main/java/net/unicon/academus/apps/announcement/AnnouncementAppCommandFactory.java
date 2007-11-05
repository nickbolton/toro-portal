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
package net.unicon.academus.apps.announcement;

import java.util.Map;
import java.util.HashMap;

import net.unicon.academus.apps.ApplicationDataManager;
import net.unicon.academus.commands.AcademusAppCommand;
import net.unicon.academus.commands.AcademusAppCommandFactory;
import net.unicon.academus.common.AcademusException;
import net.unicon.academus.producer.IProducer;
import net.unicon.sdk.FactoryCreateException;

public class AnnouncementAppCommandFactory implements AcademusAppCommandFactory {
    private static AnnouncementAppCommandFactory __factory = null;

    public static AcademusAppCommandFactory getInstance() {
	if (__factory == null) {
	    __factory = new AnnouncementAppCommandFactory();
	}
	return __factory;
    }

    /**
     * This app serves as its own command factory
     */
    public AcademusAppCommand createCommand(final String command)
	throws FactoryCreateException
    {
        if (command == null || command.trim().equals("") || 
	    command.equals(IProducer.DEFAULT_COMMAND) || command.equals(NO_COMMAND)) {
	    return new AcademusAppCommand () 
		{
		    public Map invokeOnApp(String appID,
					    Map params) throws AcademusException {
			try {
			    Map rval = new HashMap();
			    AnnouncementApp app = (AnnouncementApp)(ApplicationDataManager.getApplication(appID));
			    if (app != null) {
				rval = app.defaultEventHandler(command, params);
			    }
			    return rval;
			}
			catch (Exception exc) {
			    exc.printStackTrace();
			    throw new AcademusException(exc);
			}
		    }
		};
	}
	else if (command.equals(ADD_COMMAND)) {
	    System.out.println("Creating ADD_COMMAND");
	    return new AcademusAppCommand () 
		{
		    public Map invokeOnApp(String appID,
					    Map params) throws AcademusException {
			try {
			    Map rval = new HashMap();
			    AnnouncementApp app = (AnnouncementApp)(ApplicationDataManager.getApplication(appID));
			    if (app != null) {
				rval = app.addEventHandler(command, params);
			    }
			    return rval;
			}
			catch (Exception exc) {
			    exc.printStackTrace();
			    throw new AcademusException(exc);
			}
		    }
		};
	}        
	else if (command.equals(EDIT_COMMAND)) {
	    return new AcademusAppCommand () 
		{
		    public Map invokeOnApp(String appID,
					    Map params) throws AcademusException {
			try {
			    Map rval = new HashMap();
			    AnnouncementApp app = (AnnouncementApp)(ApplicationDataManager.getApplication(appID));
			    if (app != null) {
				rval = app.editEventHandler(command, params);
			    }
			    return rval;
			}
			catch (Exception exc) {
			    exc.printStackTrace();
			    throw new AcademusException(exc);
			}
		    }
		};
	}        
	else if (command.equals(DELETE_COMMAND)) {
	    return new AcademusAppCommand () 
		{
		    public Map invokeOnApp(String appID,
					    Map params) throws AcademusException {
			try {
			    Map rval = new HashMap();
			    AnnouncementApp app = (AnnouncementApp)(ApplicationDataManager.getApplication(appID));
			    if (app != null) {
				rval = app.deleteEventHandler(command, params);
			    }
			    return rval;
			}
			catch (Exception exc) {
			    exc.printStackTrace();
			    throw new AcademusException(exc);
			}
		    }
		};
	}        
	else if (command.equals(DELETE_ANNOUNCEMENT_COMMAND)) {
	    return new AcademusAppCommand () 
		{
		    public Map invokeOnApp(String appID,
					    Map params) throws AcademusException {
			try {
			    Map rval = new HashMap();
	    
			    AnnouncementApp app = (AnnouncementApp)(ApplicationDataManager.getApplication(appID));
			    if (app != null) {
				rval = app.deleteAnnouncementEventHandler(command, params);
			    }
			    return rval;
			}
			catch (Exception exc) {
			    exc.printStackTrace();
			    throw new AcademusException(exc);
			}
		    }
		};
	}
	else if (command.equals(SUBMIT_COMMAND)) {
	    return new AcademusAppCommand () 
		{
		    public Map invokeOnApp(String appID,
					    Map params) throws AcademusException {
			try {
			    Map rval = new HashMap();
			    AnnouncementApp app = (AnnouncementApp)(ApplicationDataManager.getApplication(appID));
			    if (app != null) {
				rval = app.submitEditEventHandler(command, params);
			    }
			    return rval;
			}
			catch (Exception exc) {
			    exc.printStackTrace();
			    throw new AcademusException(exc);
			}
		    }
		};
	}        
	else if (command.equals(INSERT_COMMAND)) {
	    return new AcademusAppCommand () 
		{
		    public Map invokeOnApp(String appID,
					    Map params) throws AcademusException {
			try {
			    Map rval = new HashMap();
			    AnnouncementApp app = (AnnouncementApp)(ApplicationDataManager.getApplication(appID));
			    if (app != null) {
				rval = app.insertEventHandler(command, params);
			    }
			    return rval;
			}
			catch (Exception exc) {
			    exc.printStackTrace();
			    throw new AcademusException(exc);
			}
		    }
		};
	}
	else { // we have no idea what this command is...
	    throw new FactoryCreateException(command + " not handled by this app");
	}
    }

    // Commands
    protected static final String DEFAULT_COMMAND = "main";
    protected static final String ADD_COMMAND     = "add";
    protected static final String NO_COMMAND      = "no";
    protected static final String EDIT_COMMAND    = "edit";
    protected static final String DELETE_COMMAND  = "delete";
    protected static final String DELETE_ANNOUNCEMENT_COMMAND = "delete_announcement";
    protected static final String SUBMIT_COMMAND  = "submit";
    protected static final String INSERT_COMMAND  = "insert";

}
