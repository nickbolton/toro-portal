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

package net.unicon.academus.api.impl;

// Java API
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.AcademusFacadeException;
import net.unicon.academus.api.IAcademusFacade;
import net.unicon.academus.api.IAcademusGroup;
import net.unicon.academus.api.IAcademusUser;
import net.unicon.academus.common.PersonDirectoryServiceFactory;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.portal.groups.GroupFactory;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.IGroupPermissionsManager;
import net.unicon.portal.groups.IMember;
import net.unicon.portal.groups.MemberFactory;
import net.unicon.portal.groups.UniconGroupService;
import net.unicon.portal.groups.UniconGroupServiceFactory;
import net.unicon.portal.groups.permissions.UniconGroupPermissionsManager;
import net.unicon.portal.security.external.GatewayPortalAuthentication;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.time.TimeService;
import net.unicon.sdk.time.TimeServiceFactory;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.services.GroupService;

public class AcademusFacadeImpl implements IAcademusFacade{
    
    private static final String DATASOURCE_PROPERTY_NAME =
        "net.unicon.portal.util.db.AcademusDBUtil.dbSource";

    private static final String DATASOURCE_PROPERTY_BASE_JNDI_NAME =
        "net.unicon.portal.util.db.AcademusDBUtil.baseJndiContext";

    private static final String DATASOURCE_NAME =
        UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).
            getProperty(DATASOURCE_PROPERTY_NAME);

    private static final String DATASOURCE_BASE_JNDI_NAME =
        UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).
            getProperty(DATASOURCE_PROPERTY_BASE_JNDI_NAME);

    private static DataSource academusDataSource = null;

    private static AcademusFacadeImpl facade = null;

    private AcademusFacadeImpl () throws Exception {

        // Lookup Academus data source
        Context initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup(DATASOURCE_BASE_JNDI_NAME);
        academusDataSource = (DataSource) envCtx.lookup(DATASOURCE_NAME);
    }

    public static void register() throws Exception {

        if (facade == null) {

            // Instantiating the facade as a singleton
            // and registering it in the Academus Facade
            // container
            facade = new AcademusFacadeImpl();
            AcademusFacadeContainer.registerFacade(facade);
        }
    }

    /**
     * Returns the root group.
     *
     * @return The root group.
     * @throws AcademusFacadeException
     */
    public IAcademusGroup getRootGroup() throws AcademusFacadeException {

        try {

            UniconGroupService groupService = UniconGroupServiceFactory.getService();

            IGroup group = groupService.getRootGroup();

            IAcademusGroup academusGroup = new AcademusGroup(group);

            return academusGroup;

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("AcademusFacadeImpl:getRootGroup(): ");
            errorMsg.append("An error occured while retrieving the root group");

            throw new AcademusFacadeException(errorMsg.toString(), e);
        }
    }

    /**
     * Returns the user with the specified username.
     *
     * @param username The username of the user to be retrieved.
     * @return The user with the specified username.
     * @throws AcademusFacadeException
     */
    public IAcademusUser getUser (String username) throws AcademusFacadeException {

        IAcademusUser academusUser = null;

        try {

            // Lookup user based on specified username
            User user = UserFactory.getUser(username);
            academusUser = new AcademusUser(user);

        } catch (Exception e) {

            e.printStackTrace();

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("AcademusFacadeImpl:getUser(): ");
            errorMsg.append("An error occured while retrieving a user's information");

            throw new AcademusFacadeException(errorMsg.toString(), e);
        }

        return academusUser;
    }

    public IAcademusGroup[] getContainingGroups(String username) throws AcademusFacadeException {

        try {

            IMember member = MemberFactory.getMember(username);

            IGroup[] groups = member.getContainingGroups();

            IAcademusGroup[] academusGroups = new AcademusGroup[groups.length];

            for (int index = 0; index < groups.length; index++ ) {

                academusGroups[index] = new AcademusGroup(groups[index]);
            }

            return academusGroups;

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("AcademusFacadeImpl:getContainingGroups(): ");
            errorMsg.append("An error occured while retrieving containing groups ");
            errorMsg.append("of the user with username:");
            errorMsg.append(username);

            throw new AcademusFacadeException(errorMsg.toString(), e);
        }
    }

    /**
     * Returns all the groups the specified user is a member of.
     *
     * @param username The user's username.
     * @return All the groups the specified user is a member of.
     * @throws AcademusFacadeException
     */
    public IAcademusGroup[] getAllContainingGroups(String username) throws AcademusFacadeException {

        try {

            IMember member = MemberFactory.getMember(username);

            IGroup[] groups = member.getAllContainingGroups();

            IAcademusGroup[] academusGroups = new AcademusGroup[groups.length];

            for (int index = 0; index < groups.length; index++ ) {

                academusGroups[index] = new AcademusGroup(groups[index]);
            }

            return academusGroups;

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("AcademusFacadeImpl:getAllContainingGroups(): ");
            errorMsg.append("An error occured while retrieving containing groups ");
            errorMsg.append("of the user with username:");
            errorMsg.append(username);

            throw new AcademusFacadeException(errorMsg.toString(), e);
        }
    }

    /**
     * Returns a data source that can be used to connect to the Academus database.
     *
     * @return A data source to the Academus database.
     * @throws AcademusFacadeException
     */
    public DataSource getAcademusDataSource() throws AcademusFacadeException {

        return academusDataSource;
    }

    /**
     * Authenticates a user based on the credentials that are provided.
     *
     * @param username Username of user being authenticated.
     * @param password Password of user being authenticated.
     * @return Returns true if the user credentials are valid, false otherwise.
     */
    public boolean authenticate(String username, String password)
        throws AcademusFacadeException
    {
        boolean result = false;
        try {
            result = GatewayPortalAuthentication.authenticate(username, password);
        } catch (Exception e) {
            StringBuffer errorMsg = new StringBuffer(128);
            errorMsg.append("AcademusFacadeImpl:authenticate(): ");
            errorMsg.append("An error while authenticating user: " + username);

            throw new AcademusFacadeException(errorMsg.toString(), e);
        }

        return result;
    }

    /**
     * Returns a <code>java.sql.TimeStampt</code> instance that represents the current time
     * at the database used by Academus.
     *
     * @return A <code>java.sql.TimeStampt</code> instance from the Academus database.
     * false otherwise.
     */
    public Timestamp getTimeStamp () throws AcademusFacadeException {

        Timestamp tStamp = null;
        TimeService timeService = null;

        try {

          timeService = TimeServiceFactory.getService();

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("AcademusFacadeImpl:getTimeStamp(): ");
            errorMsg.append("An error occured while retrieving a time stamp ");
            errorMsg.append("from the academus database.");

            throw new AcademusFacadeException(errorMsg.toString(), e);
        }

        tStamp = timeService.getTimestamp();

        return tStamp;
    }

    /**
     * Returns the group specified by the group id.
     *
     * @param groupId A group Id.
     * @return An <code>AcademusGroup</code> instance.
     */
    public IAcademusGroup getGroup(long groupId) throws AcademusFacadeException {

        IGroup group = null;
        IAcademusGroup academusGroup = null;

        try {

            group = GroupFactory.getGroup(groupId);
            academusGroup = new AcademusGroup(group);

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("AcademusFacadeImpl:getGroup(): ");
            errorMsg.append("An error occured while retrieving a group ");
            errorMsg.append("with group id:");
            errorMsg.append(groupId);

            throw new AcademusFacadeException(errorMsg.toString(), e);
        }

        return academusGroup;
    }
    
    /**
     * Returns the group specified by the group key.
     *
     * @param key A group key.
     * @return An <code>AcademusGroup</code> instance.
     */
    public IAcademusGroup getGroup(String key) throws AcademusFacadeException {

        IGroup group = null;
        IAcademusGroup academusGroup = null;

        try {

            group = GroupFactory.getGroup(key);
            academusGroup = new AcademusGroup(group);

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("AcademusFacadeImpl:getGroup(): ");
            errorMsg.append("An error occured while retrieving a group ");
            errorMsg.append("with group key:");
            errorMsg.append(key);

            throw new AcademusFacadeException(errorMsg.toString(), e);
        }

        return academusGroup;
    }

    /**
     * Return the group with the given path relative to the root group.
     * @param path a relative path
     * @return the target IAcademusGroup object.
     */
    public IAcademusGroup getGroupByPath(String[] path) throws AcademusFacadeException {

        IAcademusGroup academusGroup = null;

        try {

            UniconGroupService groupService = UniconGroupServiceFactory.getService();

            IGroup group = groupService.getGroupByPath(path);

            if (group != null) {

                academusGroup = new AcademusGroup(group);
            }

        } catch (Throwable t) {

            StringBuffer errorMsg = new StringBuffer();
            errorMsg.append("An error occured while retrieving a group ");
            errorMsg.append("with group path:  ");

            for (int i=0; i < path.length; i++) {
                errorMsg.append(path[i]);
                errorMsg.append("->");
            }

            throw new AcademusFacadeException(errorMsg.toString(), t);
        }

        return academusGroup;

    }

    /**
     * Return the group with the given absolute path.
     * @param path of the group including the root.
     * @return the target IAcademusGroup object.
     */
    public IAcademusGroup getGroupByPath(String path) throws AcademusFacadeException {

        // generate an array of groups relative to the root
        String[] pathArray = path.split(IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR);
        String[] relativePathArray = new String[pathArray.length - 1];
        System.arraycopy(pathArray, 1, relativePathArray, 0, pathArray.length - 1);

        return getGroupByPath(relativePathArray);
    }

    /**
     * Determines if the given user can perform the specified activity on the given group.
     * @param user An <code>IAcademusUser</code> instance that uniquely identifies the user.
     * @param group An <code>AcademusGroup</code> that uniquely identifies the target group.
     * @param activity The name of the activity being tested.
     * @param inherited Defines whether inherited permissions should be taked into consideration.
     *
     * @return True if the activity is allowed, false otherwise.
     */
    public boolean checkUsersGroupPermission(IAcademusUser user, IAcademusGroup group,
            String activity, boolean inherited)
                    throws AcademusFacadeException {

        boolean allowed = false;
        IGroupPermissionsManager gpm = UniconGroupPermissionsManager.getInstance();

        // The owner of all permissions for the group restrictor is
        // currently the groups manager channel
        String owner = UniconGroupPermissionsManager.GROUPSMANAGERCHAN;

        // The principal for a user corresponds to the user's username
        String principal = user.getUsername();

        // The target for a group corresponds to the group key
        String target = group.getKey();

        // The acting principal is always a user
        String principalType = UniconGroupPermissionsManager.IS_USER;

        try {

            if (inherited) {

                allowed =
                    gpm.inheritsPermission(owner, principal, target,
                        principalType, activity);
            } else {

                allowed =
                    gpm.hasPermission(owner, principal, target,
                        principalType, activity);
            }

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("AcademusFacadeImpl:hasPermission(): ");
            errorMsg.append("An error occured while checking a permission. ");
            errorMsg.append("\nOwner:");
            errorMsg.append(owner);
            errorMsg.append("\nPrincipal:");
            errorMsg.append(principal);
            errorMsg.append("\nTarget:");
            errorMsg.append(target);
            errorMsg.append("\nActivity:");
            errorMsg.append(activity);

            throw new AcademusFacadeException(errorMsg.toString(), e);
        }

        return allowed;
    }
    
    public IAcademusUser[] getAcademusUsers() throws AcademusFacadeException {
        try {
            List users = PersonDirectoryServiceFactory.getService().find("", "", "", "", true);
            List aUsers = new ArrayList();
            Iterator it = users.iterator();
            while(it.hasNext()){
                aUsers.add(new AcademusUser((User)it.next()));
            }
            return (IAcademusUser[])aUsers.toArray(new IAcademusUser[0]);
        } catch (Exception e) {
            throw new AcademusFacadeException("Error in retrieving Academus users.", e);
        }
    }
    
    public IAcademusUser[] findAcademusUsers(String username, String firstName
            , String lastName, String email, boolean matchAll) 
    		throws AcademusFacadeException{
        try {
            List users = PersonDirectoryServiceFactory.getService().find(username, firstName
                    , lastName, email, matchAll);
            List aUsers = new ArrayList();
            Iterator it = users.iterator();
            while(it.hasNext()){
                aUsers.add(new AcademusUser((User)it.next()));
            }
            return (IAcademusUser[])aUsers.toArray(new IAcademusUser[0]);
        } catch (Exception e) {
            throw new AcademusFacadeException("Error in finding Academus users.", e);
        }
    }

    public IAcademusGroup[] findAcademusGroups(String keyword) throws AcademusFacadeException {
        try {
            EntityIdentifier[] groups = GroupService.searchForGroups(keyword, GroupService.CONTAINS,
                    Class.forName(GroupService.EVERYONE));
            IAcademusGroup[] rslt = new IAcademusGroup[groups.length];
            IGroup temp = null;
            for(int i = 0; i < groups.length; i++){
                temp = GroupFactory.getGroup(groups[i].getKey());                
                rslt[i] = new AcademusGroup(temp);
            }
            
            return rslt;
        } catch (Exception e) {
            throw new AcademusFacadeException("Error in finding Academus groups.", e);
        }
    }
    
    
}

