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
package net.unicon.academus.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Kevin Gary
 * This is the primary RMI server interface for importing data into Academus
 *
 */
public interface RIAcademusImportService extends Remote {
    /**
     *
     * @param username  unique username (key field)
     * @param firstName first name of the user. Required field
     * @param lastName  last name of the user. Required field
     * @param email     User's email address.
     * @param password  User's encrypted password. This could be left null, in
     *                  which case a default business rule is implemented.
     * @return FImportStatusResult indicating whether the user has been successfully added.
     * A false indicates the User already exists in the system or is not persistable.
     * @exception RemoteException if the operation fails
     */
    public FImportStatusResult importUser(UserData userData)
    throws RemoteException;

    /**
     * batch import of users
     *
     * @param usernames
     * @param firstnames
     * @param lastnames
     * @param emails
     * @param passwords
     * @param allCommit If true, then all operations must succeed for any of them
     * to succeed, else operations are treated individually.
     * @return FImportStatusResult[] one per each transaction.
     * @throws RemoteException
     */
    public FImportStatusResult[] importUsers(UserData[] userData)
    throws RemoteException;

    /**
     * Deletes a User from Academus
     * @param userData The UserData package, needs the username and source name
     * @return a status indicating whether the user was deleted or not, or any exceptions
     */
    public FImportStatusResult deleteUser(UserData userData)
    throws RemoteException;

    /**
     * attempts to delete all the given Users. Non-transactional
     * @param userData
     * @return an array of status object corresponding to the usernames in the param
     * @throws RemoteException
     */
    public FImportStatusResult[] deleteUsers(UserData[] userData)
    throws RemoteException;

    /**
     * updates the specified User's information.
     * @param userData
     * @return
     * @throws RemoteException
     */
    public FImportStatusResult updateUser(UserData userData)
    throws RemoteException;

    /**
     *
     * @param userData
     * @return an array of status objects
     * @throws RemoteException
     */
    public FImportStatusResult[] updateUsers(UserData[] userData) throws RemoteException;
    public FImportStatusResult syncUser(UserData u) throws RemoteException;

    /**     *
     * @param t The details of the topic.
     * @return A status object detailing the results of the import.
     * @throws RemoteException If there was a problem of any ilk.
     */
    public FImportStatusResult importTopic(TopicData t)
    throws RemoteException;

    /**
     *
     * @param t The details of the topic.
     * @return A status object detailing the results of the update.
     * @throws RemoteException If there was a problem of any ilk.
     */
    public FImportStatusResult updateTopic(TopicData t)
    throws RemoteException;

    /**
     *
     * @param t The details of the topic.
     * @return A status object detailing the results of the delete.
     * @throws RemoteException If there was a problem of any ilk.
     */
    public FImportStatusResult deleteTopic(TopicData t)
    throws RemoteException;

    public FImportStatusResult syncTopic(TopicData t) throws RemoteException;

    // OFFERINGS SECTION

    /**
     * imports an offering into Academus
     * @param offeringData array of OfferingData objects representing Offering to import
     * @return FImportStatusResult[] indicating whether imports were successful or not
     * @throws RemoteException only if the call itself fails, not if any specific import fails
     */
    public FImportStatusResult importOffering(OfferingData offeringData) throws RemoteException;

    /**
     * imports offerings into Academus
     * @param offeringData array of OfferingData objects representing Offering to import
     * @return FImportStatusResult[] indicating whether imports were successful or not
     * @throws RemoteException
     */
    public FImportStatusResult[] importOfferings(OfferingData[] offeringData) throws RemoteException;

    /**
     * updates an existing offering according to the marshalled parameter object
     * @param offeringData
     * @return FImportStatusResult
     * @throws RemoteException
     */
    public FImportStatusResult updateOffering(OfferingData offeringData) throws RemoteException;


    /**
     * batch version of updateOffering
     * @param offeringData
     * @return FImportStatusResult array one per each array element in the input param
     * @throws RemoteException
     */
    public FImportStatusResult[] updateOfferings(OfferingData[] offeringData) throws RemoteException;

    /**
     * Inactivates an offering (marks as deleted but leaves data in the database)
     * @param offeringData for the OfferingID
     * @return FImportStatusResult
     * @throws RemoteException
     */
    public FImportStatusResult inactivateOffering(OfferingData offeringData)
        throws RemoteException;

    /**
     * batch version of inactivateOffering
     * @param offeringData for the OfferingID
     * @return FImportStatusResult array one per each array element in the input param
     * @throws RemoteException
     */
    public FImportStatusResult[] inactivateOfferings(OfferingData[] offeringData)
        throws RemoteException;
    public FImportStatusResult syncOffering(OfferingData o) throws RemoteException;

    /**
     *
     * @param m The details of the membership.
     * @return A status object detailing the results of the import.
     * @throws RemoteException If there was a problem of any ilk.
     */
    public FImportStatusResult importMembership(MemberData m)
    throws RemoteException;

    /**
     *
     * @param m The details of the membership.
     * @return A status object detailing the results of the update.
     * @throws RemoteException If there was a problem of any ilk.
     */
    public FImportStatusResult updateMembership(MemberData m)
    throws RemoteException;

    /**
     *
     * @param m The details of the membership.
     * @return A status object detailing the results of the delete.
     * @throws RemoteException If there was a problem of any ilk.
     */
    public FImportStatusResult deleteMembership(MemberData m)
    throws RemoteException;
    public FImportStatusResult syncMembership(MemberData m) throws RemoteException;

    /**
     * imports Group information.
     * @param groupData
     * @return FImportStatusResult indicating whether the group has been successfully added.
     * A false indicates the Group already exists in the system or is not persistable.
     * @throws RemoteException
     */
    public FImportStatusResult importGroup(GroupData groupData)
    throws RemoteException;

    /**
     * batch import of groups
     * @param groupData
     * @return FImportStatusResult[] one per each transaction.
     * @throws RemoteException
     */
    public FImportStatusResult[] importGroups(GroupData[] groupData)
    throws RemoteException;

    /**
     * deletes a Group from Academus
     * @param groupData
     * @return FImportStatusResult indicating whether the group has been successfully deleted.
     * @throws RemoteException
     */
    public FImportStatusResult deleteGroup(GroupData groupData)
    throws RemoteException;

    /**
     * attempts to delete all the given Groups. Non-transactional
     * @param groupData
     * @return FImportStatusResult[] one per each transaction.
     * @throws RemoteException
     */
    public FImportStatusResult[] deleteGroups(GroupData[] groupData)
    throws RemoteException;

    /**
     * updates the specified Group's information.
     * @param groupData
     * @return FImportStatusResult indicating whether the group has been successfully updated.
     * @throws RemoteException
     */
    public FImportStatusResult updateGroup(GroupData groupData)
    throws RemoteException;

    /**
     * attempts to update all the given Groups. Non-transactional
     * @param groupData
     * @return an array of status objects
     * @throws RemoteException
     */
    public FImportStatusResult[] updateGroups(GroupData[] groupData)
    throws RemoteException;
    public FImportStatusResult syncGroup(GroupData g) throws RemoteException;

}
