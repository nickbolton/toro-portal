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

import java.util.List;

import net.unicon.academus.domain.lms.MemberSearchCriteria;
import net.unicon.academus.domain.lms.User;

public interface PersonDirectoryService {

    /**
     * <p>
     * Get the person from either an Ldap or RDDM
     * </p><p>
     *
     * @param a string with the username to get
     * </p><p>
     *
     * @return a domain user object.
     * </p>
     */
    public User getPerson(String username) throws Exception;

    /**
     * Shadow version provides a parameter that requests user data be created
     * if it does not exist 
     * @param username
     * @param createData
     * @return User
     * @throws Exception
    */
    public User getPerson(String username, boolean createData) throws Exception;
    
    
    /**
     * <p>
     * Get the person from either an Ldap or RDBM
     * </p><p>
     *
     * @param a string with the username to get
     * @param a string with the firstname to get
     * @param a string with the lastname to get
     * @param a string with the email to get
     * </p><p>
     *
     * @return a list of user objects.
     * </p>
     */
    public List find(String username, String firstname, String lastname,
        String email, boolean matchAll) throws Exception;

    /**
     * <p>
     * Get the person from either an Ldap or RDBM and applies a username filter
     * and matches all or matches any of the search criteria
     * </p><p>
     *
     * @param a string array of usernames uses to filter
     * @param a string with the username to get
     * @param a string with the firstname to get
     * @param a string with the lastname to get
     * @param a string with the email to get
     * </p><p>
     *
     * @return a list of user objects.
     * </p>
     */
    public List find(String[] usernames, String username, String firstname,
        String lastname, String email, boolean matchAll) throws Exception;
}
