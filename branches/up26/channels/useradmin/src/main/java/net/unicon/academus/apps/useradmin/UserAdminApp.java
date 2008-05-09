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
package net.unicon.academus.apps.useradmin;

import net.unicon.sdk.authentication.AuthenticationServiceFactory;

import net.unicon.academus.apps.AcademusBaseApp;
import net.unicon.academus.common.AcademusException;
import net.unicon.academus.common.PersonDirectoryService;
import net.unicon.academus.common.PersonDirectoryServiceFactory;
import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.academus.domain.lms.MemberSearchCriteria;
import net.unicon.academus.domain.lms.OperationFailedException;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserComparator;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.academus.producer.IProducer;
import net.unicon.portal.permissions.Activity;
import net.unicon.portal.permissions.ActivityFactory;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.IMember;
import net.unicon.portal.groups.UniconGroupsException;
import net.unicon.portal.util.db.AcademusDBUtil;
import net.unicon.portal.domain.FColUserEntryDataSource;
import net.unicon.sdk.util.ExceptionUtils;
import net.unicon.sdk.catalog.Catalog;
import net.unicon.sdk.catalog.CatalogException;
import net.unicon.sdk.catalog.IDataSource;
import net.unicon.sdk.catalog.IFilterMode;
import net.unicon.sdk.catalog.FLazyCatalog;
import net.unicon.sdk.catalog.IPageMode;
import net.unicon.sdk.catalog.ISortMode;

import net.unicon.sdk.catalog.FSimplePageMode;
import net.unicon.sdk.catalog.collection.IColEntryConvertor;
import net.unicon.sdk.catalog.collection.FColSortMode;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.File;

public final class UserAdminApp extends AcademusBaseApp {

    // these are keys into the Map returned to the client
    public static final String USERNAME = "username";

    // haha! underscores! ya gotta love 'em ;) --KG
    private static Activity[] __activities = null;
    private String   __viewName       = null;
    private String   __lastCommandStr = null;
    private Catalog  __ctlg           = null;
    private int      __ctlgPgSize     = 10; // Default.
    private int      __ctlgPgNum      = 1;  // Default.
    private IPageMode __pgMode        = null;
    private UserAdminMsgType __msg    = null;
    private User     __currentUser    = null;
    private boolean  __ok = false;

    // Search fields.
    private String __username  = null;
    private String __firstName = null;
    private String __lastName  = null;
    private String __email     = null;

    // these need to be cleaned up in the lifecycle of each request!
    // init them in setup, clean 'em up in finish
    private transient Connection __conn = null;
    public synchronized Activity[] getActivities()
    throws Exception {
        if (__activities == null) {
            // path to the permissions.xml file that should be deployed
            // to the same directory as this application
            String permissionsFilePath =
                UserAdminApp.class.getResource("permissions.xml").toString();
            __activities = ActivityFactory.getActivities(permissionsFilePath);
        }
        return __activities;
    }

    protected UserAdminApp(String appID, String username, String contextID) {
        super.init(appID, username, contextID);

        // set the current user
        try {
            __currentUser = UserFactory.getUser(username);
            __ok = true;
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        } catch (OperationFailedException ofe) {
            ofe.printStackTrace();
        } catch (ItemNotFoundException infe) {
            infe.printStackTrace();
        }
    }

    protected void destroy() throws AcademusException {
        try {
        // should be called by ADM.removeApplication
        // orphan your state objects
            __ctlg           = null;
            __viewName       = null;
            __lastCommandStr = null;
            __msg            = null;
            __currentUser    = null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AcademusException(e);
        }
    }

    // METHODS THE COMMAND CALLS TO HANDLE A COMMAND STRING

    public Map addEventHandler(String cmd, Map params)
    throws AcademusException {
    Map rval = new HashMap();

        if (!__ok) {
            throw new AcademusException(
                "Application was not instantiated successfully.");
        }
        //------------------------
        // ADD USER FORM
        //------------------------
        try {
        setup(false, cmd);
        __viewName = ADD_VIEW;
        } finally {
        finish(false, rval);
        }

    return rval;
    }

    public Map editEventHandler(String cmd, Map params)
    throws AcademusException {
    Map rval = new HashMap();

        if (!__ok) {
            throw new AcademusException(
                "Application was not instantiated successfully.");
        }
        //------------------------
        // EDIT USER FORM
        //------------------------
        try {
            setup(false, cmd);

            String username = getParameter(params, "user_name");
            User user = UserFactory.getUser(username);

            __viewName = EDIT_VIEW;
        } catch (ItemNotFoundException e) {
            String msg = "Failed to find user for editing: " +
                ExceptionUtils.getExceptionMessage(e);
            throw new AcademusException(msg, e);
        } catch (OperationFailedException e) {
            String msg = "Failed to find user for editing: " +
                ExceptionUtils.getExceptionMessage(e);
            throw new AcademusException(msg, e);

        } catch (IllegalArgumentException e) {
            String msg = "Failed to find user for editing: " +
                ExceptionUtils.getExceptionMessage(e);
            throw new AcademusException(msg, e);
        } finally {
                finish(false, rval);
        }
        return rval;
    }

    public Map deleteEventHandler(String cmd, Map params)
    throws AcademusException {
    Map rval = new HashMap();

        if (!__ok) {
            throw new AcademusException(
                "Application was not instantiated successfully.");
        }
        //------------------------
        // DELETE AN USER
        //------------------------
        try {
        setup(false, cmd);
        __viewName = DELETE_VIEW;
        } finally {
        finish(false, rval);
        }
    return rval;
    }

    public Map deleteConfirmationEventHandler(String cmd, Map params)
    throws AcademusException {
    Map rval = new HashMap();

        if (!__ok) {
            throw new AcademusException(
                "Application was not instantiated successfully.");
        }
        //------------------------
        // DELETE CONFIRMATION FORM
        //------------------------
        try {
            setup(true, cmd);
            __viewName = DELETE_CONFIRM_VIEW;

            String username = getParameter(params, "user_name");
            String confirm  = getParameter(params, "deleteConfirmation");
            if ((confirm.toLowerCase()).equals("yes")) {
                UserFactory.delete(username);
            }
        } catch (ItemNotFoundException e) {
            String msg = "Failed to delete user: " +
                ExceptionUtils.getExceptionMessage(e);
            throw new AcademusException(msg, e);
        } catch (OperationFailedException e) {
            String msg = "Failed to delete user: " +
                ExceptionUtils.getExceptionMessage(e);
            throw new AcademusException(msg, e);
        } catch (IllegalArgumentException e) {
            String msg = "Failed to delete user: " +
                ExceptionUtils.getExceptionMessage(e);
            throw new AcademusException(msg, e);
        } catch (DomainException e) {
            String msg = "Failed to delete user: " +
                ExceptionUtils.getExceptionMessage(e);
            throw new AcademusException(msg, e);
        } finally {
            finish(true, rval);
        }
        return rval;
    }

    public Map searchEventHandler(String cmd, Map params)
    throws AcademusException {
        Map rval = new HashMap();

        if (!__ok) {
            throw new AcademusException(
                "Application was not instantiated successfully.");
        }
        //------------------------
        // SEARCH CONFIRMATION FORM
        //------------------------
        try {
        setup(true, cmd);
        __viewName = SEARCH_VIEW;
        } finally {
        finish(true, rval);
        }
        return rval;
    }

    public Map findEventHandler(String cmd, Map params)
    throws AcademusException {
        Map rval = new HashMap();

        if (!__ok) {
            throw new AcademusException(
                "Application was not instantiated successfully.");
        }
        try {
            setup(false, cmd);
            //------------------------
            // FIND CONFIRMATION FORM
            //------------------------
            __viewName = FIND_VIEW;

            // Reset the Catalog?
            String rstStr = getParameter(params, "resetCatalog").trim();
            if (rstStr != null && rstStr.length() != 0) {
                boolean rst = Boolean.valueOf(rstStr).booleanValue();
                if (rst) __ctlgPgNum = 1;
            }

            // Evaluate first name, last name, username and email.
            __username  = getSearchParameter(params, "user_name").trim();
            __firstName = getSearchParameter(params, "first_name").trim();
            __lastName  = getSearchParameter(params, "last_name").trim();
            __email     = getSearchParameter(params, "email").trim();

            // Create user search criteria.
            MemberSearchCriteria criteria = new MemberSearchCriteria();
            criteria.matchAllCriteria(true);
            criteria.setUsername(__username);
            criteria.setFirstName(__firstName);
            criteria.setLastName(__lastName);
            criteria.setEmail(__email);
            List users = UserFactory.findUsers(criteria);

            // Choose a page.
            String pgNumStr = getParameter(params, "catSelectPage").trim();
            int pgNum = -1;  // Signals no change.
            try {
                pgNum = Integer.valueOf(pgNumStr).intValue();
            } catch (NumberFormatException nfe) {
                // Suppress...
            }
            if (pgNum != -1) __ctlgPgNum = pgNum;

            //  Choose page size.
            String pgSizeStr = getParameter(params, "catPageSize").trim();
            int pgSize = -1;  // Signals no change.
            try {
                pgSize = Integer.valueOf(pgSizeStr).intValue();
            } catch (NumberFormatException nfe) {
                // Suppress...
            }
            if (pgSize != -1) __ctlgPgSize = pgSize;
            IPageMode pg = new FSimplePageMode(__ctlgPgSize, __ctlgPgNum);

            // Sort by first and last name.
            ISortMode[] sorts = new ISortMode[1];
            sorts[0] = new FColSortMode(new UserComparator());

            // Create catalog.
            __ctlg = createBaseCatalog(users).subCatalog(
                sorts, new IFilterMode[0], pg);

            // Set some XSL parameters.
            addResultMapping(rval, "catCurrentCommand", cmd);
            addResultMapping(rval, "catPageSize", String.valueOf(__ctlgPgSize));
            addResultMapping(rval, "catCurrentPage", String.valueOf(__ctlgPgNum));
            addResultMapping(rval, "user_name", __username);
            addResultMapping(rval, "first_name", __firstName);
            addResultMapping(rval, "last_name", __lastName);
            addResultMapping(rval, "email", __email);
            __pgMode = pg;  // Must call getPageCount() later.

        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Find user failed: ");
            msg.append(ExceptionUtils.getExceptionMessage(e));
            throw new AcademusException(msg.toString(), e);
        } finally {
            finish(false, rval);
        }
        return rval;
    }

    public Map insertEventHandler(String cmd, Map params)
    throws AcademusException {
    Map rval = new HashMap();

        if (!__ok) {
            throw new AcademusException(
                "Application was not instantiated successfully.");
        }
        //------------------------
        // INSERT CONFIRMATION FORM
        //------------------------

        try {
            setup(true, cmd);
            __viewName = INSERT_VIEW;

            // Getting formdata
            String username = getParameter(params, "user_name");
            String password = null;
            String firstName = getParameter(params, "first_name");
            String lastName  = getParameter(params, "last_name");
            String email     = getParameter(params, "email");
            if (username == null || firstName == null ||
                lastName == null || email == null) {
                throw new AcademusException(
                    "Username, First Name, Last Name, and Email are required");
            }

            // Getting Password
            String entryType = getParameter(params, "entry_type");
            if (entryType.equals("1")) {
                password = username;
            } else {
                password = getParameter(params, "password");
                // Trimming
                if (password != null && password.length() > 0 ) {
                    password.trim();
                }
            }

            boolean usernameExists = UserFactory.usernameExists(username);

            if (usernameExists) {
                __msg = UserAdminMsgType.USER_EXISTS;

                // not ready to run the group selector, abort!
                addResultMapping(rval, IProducer.ABORT_SERVANT, "abort");

                // Error Handling
                // Calling the add with the prepopulated data but
                // telling the user that username has already in use
                __viewName = ADD_VIEW;
            } else {
                UserFactory.createUser(username, password, firstName,
                                       lastName, email, null);
                addResultMapping(rval, "addedUserName", username);
            }
        } catch (OperationFailedException ofe) {
            StringBuffer msg = new StringBuffer();
            msg.append("User creation failed: ");
            msg.append(ExceptionUtils.getExceptionMessage(ofe));
            throw new AcademusException(msg.toString(), ofe);
        } catch (IllegalArgumentException iae) {
            StringBuffer msg = new StringBuffer();
            msg.append("User creation failed: ");
            msg.append(ExceptionUtils.getExceptionMessage(iae));
            throw new AcademusException(msg.toString(), iae);
        }
        finally {
            finish(true, rval);
        }
        return rval;
    }

    public Map updateGroupsEventHandler(String cmd, Map params)
    throws AcademusException {
    Map rval = new HashMap();

        if (!__ok) {
            throw new AcademusException(
                "Application was not instantiated successfully.");
        }
        //------------------------
        // INSERT CONFIRMATION FORM
        //------------------------

        IGroup[] curGroups = new IGroup[0];
        int i = 0;
        StringBuffer msg = new StringBuffer();
        
        try {
            setup(true, cmd);
            //int i;
            __viewName = MAIN_VIEW;

            // Getting formdata
            String username = getParameter(params, "username");
            Object[] groups = (Object[])params.get(IProducer.SERVANT_RESULTS);
            if (username == null) {
                throw new AcademusException(
                    "Username is required");
            }

            User user = UserFactory.getUser(username);
            IMember member = user.getGroupMember();
            IGroup gr;

            if (member == null) {
                throw new AcademusException(
                    "Group selection failed, no membership for user: " +
                    username);
            }

            curGroups = member.getContainingGroups();
            List processedGroups = new ArrayList();
            boolean needEditableGroupWarn = false;

            // add all the ones that are selected, if they aren't
            // already a member
            for (i=0; groups != null && i<groups.length; i++) {
                gr = (IGroup)groups[i];
                processedGroups.add(gr.getKey());
                if (gr.getEntityGroup().isEditable()) {
	                if (!gr.contains(member)) {
                        gr.addMember(member);
                    }
                } else {
                    needEditableGroupWarn = true;
                    msg.append("WARN: Group '"+gr.getName()+"' is not modifiable.\n");
                }
            }

            // now remove group membership for ones that were removed
            for (i=0; curGroups != null && i<curGroups.length; i++) {
                if (!processedGroups.contains(curGroups[i].getKey())) {
                    if (curGroups[i].getEntityGroup().isEditable())
                        curGroups[i].removeMember(member);
                    else {
                        needEditableGroupWarn = true;
                        msg.append("ERROR: Group '"+curGroups[i].getName()+"' is not modifiable.\n");
                    }
                }
            }

            if (needEditableGroupWarn)
                msg.append("All other group modifications were sucessfully processed.");

            addResultMapping(rval, "updatedGroupUser", username);
        } catch (org.jasig.portal.groups.GroupsException jage) {
            //StringBuffer msg = new StringBuffer();
            msg.append("User group assignment failed for group : " + curGroups[i].getName() + "\n");
            msg.append(ExceptionUtils.getExceptionMessage(jage));
            //throw new AcademusException(msg.toString(), uge);
        } catch (UniconGroupsException uge) {
            //StringBuffer msg = new StringBuffer();
            msg.append("User group assignment failed for group : " + curGroups[i].getName() + "\n");
            msg.append(ExceptionUtils.getExceptionMessage(uge));
            //throw new AcademusException(msg.toString(), uge);
        } catch (ItemNotFoundException infe) {
            //StringBuffer msg = new StringBuffer();
        	msg.append("User group assignment failed for group : " + curGroups[i].getName() + "\n");
            msg.append(ExceptionUtils.getExceptionMessage(infe));
            //throw new AcademusException(msg.toString(), infe);
        } catch (OperationFailedException ofe) {
            //StringBuffer msg = new StringBuffer();
        	msg.append("User group assignment failed for group : " + curGroups[i].getName() + "\n");
            msg.append(ExceptionUtils.getExceptionMessage(ofe));
            //throw new AcademusException(msg.toString(), ofe);
        } catch (IllegalArgumentException iae) {
            //StringBuffer msg = new StringBuffer();
        	msg.append("User group assignment failed for group : " + curGroups[i].getName() + "\n");
            msg.append(ExceptionUtils.getExceptionMessage(iae));
            //throw new AcademusException(msg.toString(), iae);
        } finally {
            finish(true, rval);
        }
        if(msg.length() > 0){
        	throw new AcademusException(msg.toString());
        }
        return rval;
    }

    public Map updateEventHandler(String cmd, Map params)
    throws AcademusException {
    Map rval = new HashMap();

        if (!__ok) {
            throw new AcademusException(
                "Application was not instantiated successfully.");
        }
        //------------------------
        // UPDATE CONFIRMATION FORM
        //------------------------

        try {
            setup(true, cmd);
            __viewName = UPDATE_VIEW;

            boolean updatePassword = false;

            String username = getParameter(params, "user_name");
            String firstName = getParameter(params, "first_name");
            String lastName  = getParameter(params, "last_name");
            String email     = getParameter(params, "email");
            if (username == null || firstName == null ||
                lastName == null || email == null) {
                throw new AcademusException(
                    "Username, First Name, Last Name, and Email are required");
            }

            User user = UserFactory.getUser(username);

            String password  = null;
            String entryType = getParameter(params, "entry_type");

            if (entryType.equals("2")) {
                updatePassword = true;
                password = getParameter(params, "password");

                // Trimming
                if (password != null && password.length() > 0) {
                    password.trim();
                }

                UserFactory.changePassword(user, password);
            }

            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            UserFactory.persist(user);
   //         broadcastDirtyChannels(user);
        } catch (IllegalArgumentException iae) {
            StringBuffer msg = new StringBuffer();
            msg.append("Edit user failed: ");
            msg.append(ExceptionUtils.getExceptionMessage(iae));
            throw new AcademusException(msg.toString(), iae);
        } catch (ItemNotFoundException infe) {
            StringBuffer msg = new StringBuffer();
            msg.append("Edit user failed: ");
            msg.append(ExceptionUtils.getExceptionMessage(infe));
            throw new AcademusException(msg.toString(), infe);
        } catch (OperationFailedException ofe) {
            StringBuffer msg = new StringBuffer();
            msg.append("Edit user failed: ");
            msg.append(ExceptionUtils.getExceptionMessage(ofe));
            throw new AcademusException(msg.toString(), ofe);
        } catch (DomainException de) {
            StringBuffer msg = new StringBuffer();
            msg.append("Edit user failed: ");
            msg.append(ExceptionUtils.getExceptionMessage(de));
            throw new AcademusException(msg.toString(), de);
        } finally {
            finish(true, rval);
        }
        return rval;
    }

    public Map changePasswordEventHandler(String cmd, Map params)
    throws AcademusException {
    Map rval = new HashMap();

        if (!__ok) {
            throw new AcademusException(
                "Application was not instantiated successfully.");
        }
        //------------------------
        // CHANGE PASSWORD CONFIRMATION FORM
        //------------------------

        try {
        setup(false, cmd);
        __viewName = CHANGE_PASSWORD_VIEW;
        } finally {
        finish(false, rval);
        }
    return rval;
    }

    public Map updatePasswordEventHandler(String cmd, Map params)
    throws AcademusException {
    Map rval = new HashMap();

        if (!__ok) {
            throw new AcademusException(
                "Application was not instantiated successfully.");
        }
        //------------------------
        // UPDATE PASSWORD CONFIRMATION FORM
        //------------------------

        try {
            setup(true, cmd);
            __viewName = UPDATE_PASSWORD_VIEW;

            String currentPassword = getParameter(params, "currentPassword");
            String newPassword  = getParameter(params, "newPassword");
            String verifiedPassword  = getParameter(params, "verifiedPassword");

            // Authenticate the user based on the entry that they specified
            // for the current password field (oldPassword).  A user can
            // only change their password if they know their current
            // password.
            if (!AuthenticationServiceFactory.getService().authenticate(
                __currentUser.getUsername(), currentPassword)) {
                throw new AcademusException(
                    "Current password incorrectly specified.");
            } else {
                // Make sure that the user has specified the content for the
                // new password and the verification password before setting
                // the user's password permanently.
                if (newPassword == null && verifiedPassword != null) {
                    throw new AcademusException(
                        "New password verification failed.");
                } else if (newPassword != null && verifiedPassword == null) {
                    throw new AcademusException(
                        "New password verification failed.");
                } else if (newPassword != null && verifiedPassword != null &&
                    (!newPassword.equals(verifiedPassword))) {
                    throw new AcademusException(
                        "New password verification failed.");
                } else {
                    // set the user's password to the new value
                    UserFactory.changePassword(__currentUser, verifiedPassword);
                    UserFactory.persist(__currentUser);
                }
            }
        } catch (AcademusException ae) {
            throw ae;
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Change user password failed: ");
            msg.append(ExceptionUtils.getExceptionMessage(e));
            throw new AcademusException(msg.toString(), e);
        } finally {
            finish(true, rval);
        }
        return rval;
    }

    public Map changeEmailEventHandler(String cmd, Map params)
    throws AcademusException {
    Map rval = new HashMap();

        if (!__ok) {
            throw new AcademusException(
                "Application was not instantiated successfully.");
        }
        //------------------------
        // CHANGE EMAIL CONFIRMATION FORM
        //------------------------

        try {
        setup(false, cmd);
        __viewName = CHANGE_EMAIL_VIEW;
        } finally {
        finish(false, rval);
        }
    return rval;
    }

    public Map updateEmailEventHandler(String cmd, Map params)
    throws AcademusException {
    Map rval = new HashMap();

        if (!__ok) {
            throw new AcademusException(
                "Application was not instantiated successfully.");
        }
        //------------------------
        // UPDATE EMAIL CONFIRMATION FORM
        //------------------------

        try {
            setup(true, cmd);
            __viewName = UPDATE_EMAIL_VIEW;

            String currentPassword = getParameter(params, "currentPassword");
            String newEmail  = getParameter(params, "email");

            // Authenticate the user based on the entry that they specified
            // for the current password field (oldPassword).  A user can
            // only change their Email if they know their current
            // password.
            if (!AuthenticationServiceFactory.getService().authenticate(
                __currentUser.getUsername(), currentPassword)) {
                throw new AcademusException(
                    "Current password incorrectly specified.");
            } else {
                // Make sure that the user has specified the content for the
                // new Email address before setting user's Email permanently.
                if (newEmail == null) {
                    throw new AcademusException(
                        "New Email address cannot be null!");
                } else {
                    // set the user's Email address to the new value
                    __currentUser.setEmail(newEmail);
                    UserFactory.persist(__currentUser);
                }
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Change user email failed: ");
            msg.append(ExceptionUtils.getExceptionMessage(e));
            throw new AcademusException(msg.toString(), e);
        } finally {
            finish(true, rval);
        }
        return rval;
    }

    /**
     * Called when command is empty, default, or something unknown
     **/
    public Map defaultEventHandler(String cmd, Map params)
    throws AcademusException {
    Map rval = new HashMap();

        if (!__ok) {
            throw new AcademusException(
                "Application was not instantiated successfully.");
        }
        try {
        setup(false, cmd);
        __viewName = MAIN_VIEW;
        } finally {
        finish(false, rval);
        }
    return rval;
    }

    // THESE ARE THE LOCAL HELPER METHODS FOR EVENT HANDLERS

    private static Catalog createBaseCatalog(List users) {

        // EntryConvertor.
        IColEntryConvertor conv = new IColEntryConvertor() {

            public Object convertEntry(Object entry)
            throws CatalogException {
                User user = (User)entry;
                return user.toXML();
            }
        };

        // DataSource.
        IDataSource ds = new FColUserEntryDataSource(users, conv);

        // Catalog.
        Catalog cat = new FLazyCatalog(ds);

        return cat;
    }

    private void setup(boolean needConnection, String cmd)
    throws AcademusException
    {
        // save the command
        __lastCommandStr = cmd;

        // the application message will be OK by default
        __msg = UserAdminMsgType.OK;

        try {
            // setup the connection if needed
            if (needConnection) {
                __conn = AcademusDBUtil.getDBConnection();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AcademusException(
                "Unable to acquire connection on UserAdminApp event setup");
        }
    }

    private void finish(boolean releaseConnection,
                    Map rval) throws AcademusException {

        // reset the state of any transient vars
        // release the connection
        if (releaseConnection && __conn != null) {
            AcademusDBUtil.safeReleaseDBConnection(__conn);
            __conn = null;
        }

        addResultMapping(rval, IProducer.APP_ID, getApplicationID());
        addResultMapping(rval, IProducer.CONTEXT_ID, getContextID());
        addResultMapping(rval, IProducer.VIEW_NAME, getViewName());
        addResultMapping(rval, IProducer.COMMAND, getCommand());
        addResultMapping(rval, USERNAME, getUsername());

    }

    private String getParameter(Map params, String key) {
        if (params.get(key) instanceof String[]) {
            return ((String[])params.get(key))[0];
        } else if (params.get(key) instanceof String) {
            return (String)params.get(key);
        }
        return "";
    }

    // Special method to evaluate search parameters.
    private String getSearchParameter(Map params, String key) {
        String rslt = null;
        if (params.containsKey(key)) {
            if (params.get(key) instanceof String[]) {
                rslt = ((String[])params.get(key))[0];
            } else if (params.get(key) instanceof String) {
                rslt = (String)params.get(key);
            } else {
                String msg = "Search parameter '" + key
                                    + "' was mapped to an unexpected type.";
                throw new IllegalArgumentException(msg);
            }
        } else {
            // Choose which.
            if (key.equals("user_name")) {
                rslt = __username;
            } else if (key.equals("first_name")) {
                rslt = __firstName;
            } else if (key.equals("last_name")) {
                rslt = __lastName;
            } else if (key.equals("email")) {
                rslt = __email;
            } else {
                String msg = "Unrecognized search parameter:  " + key;
                throw new IllegalArgumentException(msg);
            }
        }
        return rslt;
    }

    // basic readers
    public List getUsers() throws AcademusException {
        List rslt = null;
        if (__ctlg != null) {
            try {
                rslt = __ctlg.elements();
                // Make sure we haven't gone beyond the last page.
                if (__ctlgPgNum > __pgMode.getPageCount()) {
                    __ctlgPgNum = __pgMode.getPageCount();
                    __pgMode = new FSimplePageMode(__ctlgPgSize, __ctlgPgNum);
                    __ctlg = __ctlg.subCatalog(new ISortMode[0],
                                new IFilterMode[0], __pgMode);
                    rslt = __ctlg.elements();
                }
            } catch (CatalogException ce) {
                String msg = "User Admin Channel failed to retrieve the "
                                                    + "list of users.";
                throw new AcademusException(msg, ce);
            }
        } else {
            rslt = new ArrayList();
        }
        return rslt;
    }
    // NB:  Producer must call getUsers() before getPageCount().
    public int getPageCount() throws AcademusException {
        int rslt = 0;
        if (__pgMode != null) {
            try {
                rslt = __pgMode.getPageCount();
            } catch (CatalogException ce) {
                String msg = "User Admin Channel failed to retrieve the "
                                                    + "page count.";
                throw new AcademusException(msg, ce);
            }
        }
        return rslt;
    }
    public String getViewName() { return __viewName; }
    public UserAdminMsgType getMsg() { return __msg; }
    public User getCurrentUser() { return __currentUser; }
    public String getCommand() { return __lastCommandStr; }

    // ABSTRACT METHODS FROM ACADEMUSBASEAPP
    public boolean equals(AcademusBaseApp app) {
        return (app instanceof UserAdminApp) &&
            (app.getApplicationID() == this.getApplicationID());
    }

    // Params
    // Views
    protected static final String MAIN_VIEW            = "main";
    protected static final String ADD_VIEW             = "add";
    protected static final String EDIT_VIEW            = "edit";
    protected static final String DELETE_VIEW          = "delete";
    protected static final String SEARCH_VIEW          = "search";
    protected static final String FIND_VIEW            = "find";
    protected static final String INSERT_VIEW          = MAIN_VIEW;
    protected static final String UPDATE_VIEW          = MAIN_VIEW;
    protected static final String DELETE_CONFIRM_VIEW  = MAIN_VIEW;
    protected static final String CHANGE_PASSWORD_VIEW = "change_password";
    protected static final String UPDATE_PASSWORD_VIEW = MAIN_VIEW;
    protected static final String CHANGE_EMAIL_VIEW    = "change_email";
    protected static final String UPDATE_EMAIL_VIEW    = MAIN_VIEW;

}
