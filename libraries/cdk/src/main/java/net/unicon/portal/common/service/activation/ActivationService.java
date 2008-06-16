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

package net.unicon.portal.common.service.activation;

import java.sql.Connection;

import java.sql.SQLException;

import java.util.Date;

import java.util.List;

import java.util.Map;


import net.unicon.academus.domain.lms.Offering;
import net.unicon.portal.common.service.activation.Activation;

public interface ActivationService {

    public final static String ONLINE_ASSESSMENT = "ASSESSMENT";

    public final static String ASSIGNMENT        = "ASSIGNMENT";

    public final static String CURRICULUM        = "CURRICULUM";

    public final static String CHAT              = "CHAT";

    /**
     * Get an Activation object based on the activation ID
     * @param <code>int</code> activationID
     * @param <code>Connection</code> - a database connection
     * @return <code>Activation</code> - activation object based on activation ID
     */

    public Activation getActivation(int activationID, Connection conn) throws SQLException;

    /**
     * Get an Activation object based on the activation ID and if its activate for a particuluar user.
     * @param <code>int</code> activationID
     * @param <code>Connection</code> - a database connection
     * @param <code>String</code> - a user name
     * @return <code>Activation</code> - activation object based on activation ID
     */

    public Activation getActivation(int activationID, String username, Connection conn) throws SQLException;

    /**
     * Returns all the Activations for an Offering in a List.
     * @param <code>int</code> offeringID
     * @param <code>Connection</code> - a database connection
     * @return <code>List</code> - list of activation object based on the offering ID
     */

    public List getActivations( int offeringID, String type, Connection conn) throws SQLException;

    public List getActivations( int offeringID, Connection conn) throws SQLException;

    /**
     * Returns all the Activations for an Offering in a List.
     * @param <code>net.unicon.portal.domain.Offering</code> offering
     * @param <code>Connection</code> - a database connection
     * @return <code>List</code> - list of activation object based on the offering
     */
    public List getActivations( Offering offering, String type, Connection conn) throws SQLException;

    public List getActivations( Offering offering, Connection conn) throws SQLException;

    /**
     * Returns all the Activations for an user in an offering in a List.
     * @param <code>int</code> offeringID
     * @param <code>String</code> - username
     * @param <code>Connection</code> - a database connection
     * @return <code>List</code> - list of activation object based on the username and offering ID
     */

    public List getUserActivations(int offeringID, String username, Connection conn) throws SQLException;

    /**
     * Returns all the Activations for an user in an offering in a List.
     * @param <code>net.unicon.portal.domain.Offering</code> offering
     * @param <code>String</code> - username
     * @param <code>Connection</code> - a database connection
     * @return <code>List</code> - list of activation object based on the username and offering
     */

    public List getUserActivations(Offering offering, String username, Connection conn) throws SQLException;
    
    /**
     * Returns all the Activations for an user in an offering in a List based on a type.
     * @param <code>int</code> offeringID
     * @param <code>String</code> - username
     * @param <code>String</code> - Activation type
     * @param <code>Connection</code> - a database connection
     * @return <code>List</code> - list of activation object based on the username and offering ID
     */

    public List getUserActivations(int offeringID, String username, String type, Connection conn) throws SQLException;

    /**
     * Returns all the Activations for an user in an offering in a Listbased on a type.
     * @param <code>net.unicon.portal.domain.Offering</code> offering
     * @param <code>String</code> - username
     * @param <code>String</code> - Activation type
     * @param <code>Connection</code> - a database connection
     * @return <code>List</code> - list of activation object based on the username and offering
     */

    public List getUserActivations(Offering offering, String username, String type, Connection conn) throws SQLException;

    /**
     * Deactivate an Activation
     * @param <code>int</code> activationID
     * @param <code>Connection</code> - a database connection
     */

    public void deactivateActivation(int activationID, Connection conn) throws SQLException;

    /**
     * Delete/Remove persistance of an Activation
     * @param <code>int</code> activationID
     * @param <code>Connection</code> - a database connection
     */

    public void deleteActivation(int activationID, Connection conn) throws SQLException;

    public Activation createActivation(

    int offeringID,

    String type,

    Date startDate,

    Date endDate,

    long startTime,

    long endTime,

    Map attributes,

    List userList,

    boolean allUsers,

    Connection conn) throws SQLException ;


    /**
     * Link an activation up with calendar events
     * @param <code>Activation</code> an activation object
     * @param <code>Connection</code> - a database connection
     * @return <code>boolean</code> - Indicates success or failure
     */

    public boolean addEventsToActivation(Activation newact, Connection conn);
    

     /**
     * Removes calendar events from an activation
     * @param <code>int</code> an activation identifier
     * @param <code>Connection</code> - a database connection
     */
                                                                        
    public void removeEventsFromActivation(int activationID, Connection conn);

}

