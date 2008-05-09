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

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.unicon.academus.apps.AcademusBaseApp;
import net.unicon.academus.common.AcademusException;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.academus.producer.IProducer;
import net.unicon.portal.permissions.Activity;
import net.unicon.portal.permissions.ActivityFactory;
import net.unicon.portal.permissions.DirtyEvent;
import net.unicon.portal.util.db.AcademusDBUtil;

public final class AnnouncementApp extends AcademusBaseApp {

    // haha! underscores! ya gotta love 'em ;) --KG
    private static Activity[] __activities = null;
    private String   __viewName = null;
    private List     __announcements = null;

    private User     __user;
    private String   __annIDString = null;
    private String   __lastCommandStr = null;
    
    // these need to be cleaned up in the lifecycle of each request!
    // init them in setup, clean 'em up in finish
    private transient Connection __conn = null;
    private transient AnnouncementService __announcementService = null;

    public synchronized Activity[] getActivities()
    throws Exception {
        if (__activities == null) {
            // path to the permissions.xml file that should be deployed
            // to the same directory as this application
            String permissionsFilePath = 
                AnnouncementApp.class.getResource("permissions.xml").toString();
            __activities = ActivityFactory.getActivities(permissionsFilePath);
        }
        return __activities;
    }

    protected AnnouncementApp(String appID, String username, String contextID) {
        super.init(appID, username, contextID);
    }

    protected void destroy() throws AcademusException {
    try {
        // should be called by ADM.removeApplication
        // orphan your state objects
        __viewName              = null;
        __announcements         = null;
        __user                  = null;
        __annIDString           = null;
        __lastCommandStr        = null;
    }
    catch (Exception e) {
        e.printStackTrace();
        throw new AcademusException(e);
    }
    }

    // METHODS THE COMMAND CALLS TO HANDLE A COMMAND STRING

    public Map addEventHandler(String cmd,
			       Map params) throws AcademusException {
	Map rval = new HashMap();
	//------------------------
	// ADD ANNOUNCEMENT FORM
	//------------------------
    try {
	setup(false, cmd);
	__viewName = ADD_VIEW;
    } finally {
	finish(false, true, rval);
    }
	
	//System.out.println("End of AnnouncementApp.addEventHandler, view is " + __viewName + ", cmd is " + __lastCommandStr);

	return rval;
    }

    public Map editEventHandler(String cmd,
				 Map params) throws AcademusException {
	Map rval = new HashMap();
	//------------------------
	// EDIT ANNOUNCEMENT FORM
	//------------------------
    try {
	setup(true, cmd);
	
	__annIDString = ((String[])params.get(ANNOUNCEMENT_ID))[0];
	// Validating announcement string data
	if (__annIDString != null) {
	    int announcementID = Integer.parseInt(__annIDString);
	    // Getting announcement
	    __announcements = __announcementService.getAnnouncement(getContextID(),
								    announcementID,
								    __conn);
	}
	__viewName = EDIT_VIEW;

    } finally {
	finish(true, true, rval);
    }

	return rval;
    } 

    public Map deleteEventHandler(String cmd,
				   Map params) throws AcademusException {
	Map rval = new HashMap();
	boolean needsDirtying = false;

	//------------------------
	// DELETE AN ANNOUNCEMENT
	//------------------------
    try {
	setup(true, cmd);
	
	__annIDString = ((String[])params.get(ANNOUNCEMENT_ID))[0];
	if (__annIDString != null) {
	    int announcementID = Integer.parseInt(__annIDString);
	    if (__announcementService.deleteAnnouncement(announcementID, __conn) == 0) {
		// display message stating announcement not deleted
	    }
	    else {
		needsDirtying = true;
	    }
	}
	__announcements = __announcementService.getAnnouncements(getContextID(),
								 __conn);
	__setViewName();

    } finally {
	finish(true, needsDirtying, rval);
    }

	return rval;
    } 

    public Map deleteAnnouncementEventHandler(String cmd,
					       Map params) throws AcademusException {
	Map rval = new HashMap();
	//------------------------
	// DELETE CONFIRMATION FORM
	//------------------------
    try {
	setup(true, cmd);

	__annIDString = ((String[])params.get(ANNOUNCEMENT_ID))[0];
	if (__annIDString != null) {
	    int announcementID = Integer.parseInt(__annIDString);
	    // Getting announcement
	    __announcements = __announcementService.getAnnouncement(getContextID(),
								    announcementID,
								    __conn);
	}

	__viewName = DELETE_VIEW;

    } finally {
	finish(true, true, rval);
    }

	return rval;
    } 

    public Map submitEditEventHandler(String cmd,
				       Map params) throws AcademusException {
	Map rval = new HashMap();
	boolean needsDirtying = false;
	//------------------------
	// EDIT CONFIRMATION/SUBMIT
	//------------------------
    try {
	setup(true, cmd);

	__annIDString  = ((String[])params.get(ANNOUNCEMENT_ID))[0];
	String message = ((String[])params.get(MESSAGE_PARAM))[0];

	// Validating announcement string data
	if (__annIDString != null) {
	    int announcementID = Integer.parseInt(__annIDString);
	    __announcementService.updateAnnouncement(getContextID(),
						     announcementID,
						     message,
						     getUsername(),
						     __conn);
	    // Dirty cache for all users in the offering
	    needsDirtying = true;
	}

	__announcements = __announcementService.getAnnouncements(getContextID(),
								 __conn);

	__setViewName();

    } finally {
	finish(true, needsDirtying, rval);
    }

	return rval;    
    } 

    public Map insertEventHandler(String cmd,
				   Map params) throws AcademusException {
	Map rval = new HashMap();
	boolean needsDirtying = false;

	//------------------------
	// ADD SUBMIT
	//------------------------
    try {
	setup(true, cmd);
	
	String message = ((String[])params.get(MESSAGE_PARAM))[0];
	// do I need to set __annID explicitly to null? don't think so
	
	__announcementService.addAnnouncement(getContextID(),
					      message,
					      getUsername(),
					      __conn);

	// Dirty cache for all users in the offering
	needsDirtying = true;

	__announcements = __announcementService.getAnnouncements(getContextID(),
								 __conn);
	__setViewName();

    } finally {
	finish(true, needsDirtying, rval);
    }

	return rval;
    }

    /**
     * Called when command is empty, default, or something unknown
     **/
    public Map defaultEventHandler(String cmd,
				    Map params) throws AcademusException {
	Map rval = new HashMap();

    try {
	setup(true, cmd);
	__announcements = __announcementService.getAnnouncements(getContextID(),
								 __conn);
	__setViewName();

    } finally {
	finish(true, true, rval);
    }

	return rval;
    }

    // THESE ARE THE LOCAL HELPER METHODS FOR EVENT HANDLERS

    private void __dirtyIfNeeded(String cmd, boolean flag, Map rval) {
	// if flag is false, we shoulda never bothered
	if (!flag) return;

	// see if cmd matches one of our activities' handle
	try {
	    boolean done = false;
	    Activity[] activities = getActivities();
	    DirtyEvent[] events = null;
	    for (int i = 0; i < activities.length && !done; i++) {
		if (cmd.equalsIgnoreCase(activities[i].getHandle()) ) {
		    events = activities[i].getDirtyEvents();
		    done = true;
		}
	    }
	    // if there are dirty events, put them in the Map
	    addResultMapping(rval, IProducer.DIRTY_EVENT, events);
	}
	catch (Exception exc) {
	    exc.printStackTrace();
	    // this was just something weird, to heck with dirtying
	}
    }

    private void setup(boolean needConnection, String cmd) 
    throws AcademusException 
    {
    try {
        // get an AnnouncementService
        // you could hang onto these right now across requests, but if you do
        // then they can't become pooled or stateful later
        __announcementService = AnnouncementServiceFactory.getService();

        // Getting User object
        __user = UserFactory.getUser(_username);
    }
    catch (Exception e) {
        e.printStackTrace();
        throw new AcademusException(e);
    }

    // save the command
    __lastCommandStr = cmd;

    // setup the connection if needed
    try {
	if (needConnection) {
        __conn = AcademusDBUtil.getDBConnection();
	}
    }
    catch (Exception e) {
        e.printStackTrace();
        throw new AcademusException("Unable to acquire connection on AcademusApp event setup");
    }

    }

    private void finish(boolean releaseConnection, 
			boolean needsDirtying,
			Map rval) 
	throws AcademusException {
    
	// reset the state of any transient vars
	
	// release the connection
	if (releaseConnection && __conn != null) {
	    AcademusDBUtil.safeReleaseDBConnection(__conn);
	    __conn = null;
	}

	// set the rval stuff - this should be a djinn thing later
	addResultMapping(rval, IProducer.APP_ID, getApplicationID());
	addResultMapping(rval, IProducer.CONTEXT_ID, getContextID());
	addResultMapping(rval, IProducer.VIEW_NAME, getViewName());
	addResultMapping(rval, IProducer.COMMAND, getCommand());
	addResultMapping(rval, ANNOUNCEMENT_ID, getAnnouncementID());
	
	__dirtyIfNeeded(__lastCommandStr, needsDirtying, rval);
    }

    private void __setViewName() {
    if (getContextID() == null) {
        __viewName = EMPTY_VIEW;
    }
    // could put an inactive test in here...
    else {
        __viewName = MAIN_VIEW;
    }
    }

    // basic readers
    public String getViewName() { return __viewName; }
    public List getAnnouncements() { return __announcements; }
    public User getUser() { return __user; }
    public String getAnnouncementID() { return __annIDString; }
    public String getCommand() { return __lastCommandStr; }

    // ABSTRACT METHODS FROM ACADEMUSBASEAPP
    public boolean equals(AcademusBaseApp app) {
    return (app instanceof AnnouncementApp) && 
        (app.getApplicationID() == this.getApplicationID());
    }

    // Params
    protected static final String MESSAGE_PARAM  = "message";
    // Views
    protected static final String EMPTY_VIEW    = "empty";
    protected static final String INACTIVE_VIEW = "inactive";
    protected static final String ADD_VIEW      = "add";
    protected static final String EDIT_VIEW     = "edit";
    protected static final String MAIN_VIEW     = "main";
    protected static final String DELETE_VIEW   = "delete";

    // these are keys into the Map returned to the client - must match XSL
    public static final String ANNOUNCEMENT_ID = "ID";

}
