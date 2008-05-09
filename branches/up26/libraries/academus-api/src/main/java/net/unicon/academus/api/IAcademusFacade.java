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

package net.unicon.academus.api;

import java.util.Map;
import javax.sql.DataSource;
import java.sql.Timestamp;

import net.unicon.academus.api.AcademusFacadeException;
import net.unicon.academus.api.IAcademusGroup;
import net.unicon.academus.api.IAcademusUser;

/**
    The interface to the Academus Facade API. The methods in the API
    allow manipulating user and group information.
*/

public interface IAcademusFacade {

    public static final String SELECT_GROUP_ACTIVITY = "SELECT";

    /**
     * Returns the root group.
     *
     * @return The root group.
     * @throws AcademusFacadeException
     */
    public IAcademusGroup getRootGroup() throws AcademusFacadeException;

    /**
     * Returns the user with the specified username.
     *
     * @param username The username of the user to be retrieved.
     * @return The user with the specified username.
     * @throws AcademusFacadeException
     */
    public IAcademusUser getUser (String username) throws AcademusFacadeException;


    /**
     * Returns the groups the specified user is a direct member of.
     *
     * @param username The user's username.
     * @return Groups the specified user is a direct member of.
     * @throws AcademusFacadeException
     */
    public IAcademusGroup[] getContainingGroups(String username) throws AcademusFacadeException;

    /**
     * Returns all the groups the specified user is a member of.
     *
     * @param username The user's username.
     * @return All the groups the specified user is a member of.
     * @throws AcademusFacadeException
     */
    public IAcademusGroup[] getAllContainingGroups(String username) throws AcademusFacadeException;

    /**
     * Returns a data source that can be used to connect to the Academus database.
     *
     * @return A data source to the Academus database.
     * @throws AcademusFacadeException
     */
    public DataSource getAcademusDataSource() throws AcademusFacadeException;

    /**
         * Authenticates a user based on the credentials that are provided.
         *
     * @param username Username of user being authenticated.
     * @param password Password of user being authenticated.
     * @return Returns true if the user credentials are valid, false otherwise.
     */
    public boolean authenticate(String username, String password) throws AcademusFacadeException;

    /**
     * Returns a <code>java.sql.TimeStampt</code> instance that represents the current time
     * at the data source used by Academus.
     *
     * @return A <code>java.sql.TimeStampt</code> instance from the Academus data source.
     * false otherwise.
     */
    public Timestamp getTimeStamp () throws AcademusFacadeException;

    /**
     * Returns the group specified by the group id.
     *
     * @param groupId A group Id.
     * @return An <code>AcademusGroup</code> instance.
     */
    public IAcademusGroup getGroup(long groupId) throws AcademusFacadeException;
    
    /**
     * Returns the group specified by the group key.
     *
     * @param key A group key.
     * @return An <code>AcademusGroup</code> instance.
     */
    public IAcademusGroup getGroup(String key) throws AcademusFacadeException;

    /**
     * Return the group with the given path relative to the root group.
     * @param path a relative path
     * @return the target IAcademusGroup object.
     */
    public IAcademusGroup getGroupByPath(String[] path) throws AcademusFacadeException;

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
                    throws AcademusFacadeException;

    /**
     * Return the group with the given path relative to the root group.
     * @param path of the group including the root.
     * @return the target IAcademusGroup object.
     */
    public IAcademusGroup getGroupByPath(String path) throws AcademusFacadeException;
    
    /**
     * Return an array of the users in the system.
     * @return an array of IAcademusUser.
     */
    public IAcademusUser[] getAcademusUsers() throws AcademusFacadeException;

    /**
     * Return an array of the users in the system based on search criteria first name and last name.
     * @param String keyword
     * @return an array of IAcademusUser.
     */
    public IAcademusUser[] findAcademusUsers(String username, String firstName
            , String lastName, String email, boolean matchAll) 
    		throws AcademusFacadeException;
    
    
    /**
     * Return an array of the users in the system based on search criteria first name and last name.
     * @param String keyword
     * @return an array of IAcademusUser.
     */
    public IAcademusGroup[] findAcademusGroups(String keyword) 
    		throws AcademusFacadeException;

}

