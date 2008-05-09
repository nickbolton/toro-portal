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

package net.unicon.civis;

import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecisionCollection;

public interface ICivisFactory {

    /**
     * Obtains the root group. 
     */
    IGroup getRoot();
    
    /**
     * Obtains an array of all the users in the system. 
     */
    IPerson[] getUsers();

    /**
     * Obtains the set of supported person attributes and their allowable
     * values.
     */
    IChoiceCollection getPersonAttributes();

    /**
     * Obtains the set of supported group attributes and their allowable values.
     */
    IChoiceCollection getGroupAttributes();

    void startListening(Event e, IEventListener listener);

    void stopListening(Event e, IEventListener listener);

    ITransaction beginTransaction();

    /**
     * Creates a new person with the specified name and attributes.  The
     * <code>name</code> parameter must not match any existing person within the
     * factory.
     */
    IPerson createPerson(String name, IDecisionCollection attributes);

    IPerson createPerson(ITransaction trans, String label,
                        IDecisionCollection attributes);

    IPerson getPerson(String name);
    
    IGroup getGroupByPath(String groupPath);
    
    IGroup getGroupById(String groupId);

    void rename(IPerson person, String name);

    void rename(ITransaction trans, IPerson person, String name);

    void updateAttributes(IPerson person, IDecisionCollection attributes);

    void updateAttributes(ITransaction trans, IPerson person,
                        IDecisionCollection attributes);

    void delete(IPerson person);

    void delete(ITransaction trans, IPerson person);

    /**
     * Creates a new group with the specified name and attributes.  The
     * <code>name</code> parameter must not match any existing group within the
     * factory.
     */
    IGroup createGroup(IGroup parent, String label,
                IDecisionCollection attributes);

    IGroup createGroup(ITransaction trans, IGroup parent, String label,
                                    IDecisionCollection attributes);

    void rename(IGroup group, String name);

    void rename(ITransaction trans, IGroup group, String name);

    void updateAttributes(IGroup group, IDecisionCollection attributes);

    void updateAttributes(ITransaction trans, IGroup group,
                        IDecisionCollection attributes);

    void delete(IGroup group);

    void delete(ITransaction trans, IGroup group);
    
    /**
     * Obtains an URL representation of this Civis factory.
     * The URL contains information of the factory, namely, the domain, 
     * class and a unique identifier (if applicable).
     */
    String getUrl();    

    
    /**
     * Obtaibs a list of groups that match the search strign in their name.
     * @param searchString
     * @return an array of IGroup 
     */
    IGroup[] searchGroupsByName(String searchString);
    
    /**
     * Obtains a colleciton of users based on the search parameters
     * @param username
     * @param firstName
     * @param lastName
     * @param email
     * @param matchAll
     * @return
     */
    IPerson[] searchUsers(String username, String firstName
            , String lastName, String email, boolean matchAll);
}
