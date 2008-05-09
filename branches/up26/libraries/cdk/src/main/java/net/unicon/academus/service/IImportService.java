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

import net.unicon.academus.common.AcademusException;

/**
 * IImportService is a generic interface for importing various data formats into
 * Academus. The responsibility of service classes implementing this interface
 * is to accept the required set of objects and persist them to both Academus
 * and uPortal. The responsibility for marshalling input data formats such as
 * XML is <em>not</em> within the boundary of implementors.
 *
 * @author Kevin Gary
 *
 */
public interface IImportService {
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
     * @exception AcademusException if the operation fails
     */
    public FImportStatusResult importUser(UserData userData) throws AcademusException;

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
     * @throws AcademusException
     */
    public FImportStatusResult[] importUsers(UserData[] userData) throws AcademusException;

    /**
     * Deletes a User from Academus
     * @return a status indicating whether the user was deleted or not, or any exceptions
     * @param userData must have username of the user to remove
     * @throws AcademusException
     */
    public FImportStatusResult deleteUser(UserData userData) throws AcademusException;

    /**
     * attempts to delete all the given Users. Non-transactional
     * @param userData
     * @return an array of status object corresponding to the usernames in the param
     * @throws AcademusException
     */
    public FImportStatusResult[] deleteUsers(UserData[] userData) throws AcademusException;

    /**
     * updates the specified User's information.
     * @param userData
     * @return
     * @throws AcademusException
     */
    public FImportStatusResult updateUser(UserData userData) throws AcademusException;

    /**
     *
     * @param userData
     * @return an array of status objects
     * @throws AcademusException
     */
    public FImportStatusResult[] updateUsers(UserData[] userData) throws AcademusException;
    /**
     *
     * @param t The details of the topic.
     * @return A status object detailing the results of the import.
     * @throws RemoteException If there was a problem of any ilk.
     */
    public FImportStatusResult importTopic(TopicData t)
    throws AcademusException;

    /**
     *
     * @param t The details of the topic.
     * @return A status object detailing the results of the update.
     * @throws RemoteException If there was a problem of any ilk.
     */
    public FImportStatusResult updateTopic(TopicData t)
    throws AcademusException;

    /**
     *
     * @param t The details of the topic.
     * @return A status object detailing the results of the delete.
     * @throws RemoteException If there was a problem of any ilk.
     */
    public FImportStatusResult deleteTopic(TopicData t)
    throws AcademusException;

    // METHODS RELATING TO OFFERINGS

    /**
     * imports an offering into Academus
     * @param offeringData array of OfferingData objects representing Offering to import
     * @return FImportStatusResult[] indicating whether imports were successful or not
     * @throws AcademusException only if the call itself fails, not if any specific import fails
     */
    public FImportStatusResult importOffering(OfferingData offeringData) throws AcademusException;

    /**
     * imports offerings into Academus
     * @param offeringData array of OfferingData objects representing Offering to import
     * @return FImportStatusResult[] indicating whether imports were successful or not
     * @throws AcademusException
     */
    public FImportStatusResult[] importOfferings(OfferingData[] offeringData) throws AcademusException;

    /**
     * updates an existing offering according to the marshalled parameter object
     * @param offeringData
     * @return FImportStatusResult
     * @throws AcademusException
     */
    public FImportStatusResult updateOffering(OfferingData offeringData) throws AcademusException;

    /**
     * batch version of updateOffering
     * @param offeringData
     * @return FImportStatusResult array one per each array element in the input param
     * @throws AcademusException
     */
    public FImportStatusResult[] updateOfferings(OfferingData[] offeringData) throws AcademusException;

    /**
      * Inactivates an offering (marks as deleted but leaves data in the database)
      * @param offeringData only need the offering ID filled in
      * @return FImportStatusResult
      * @throws AcademusException
      */
     public FImportStatusResult inactivateOffering(OfferingData offeringData)
         throws AcademusException;

     /**
      * batch version of inactivateOffering
      * @param offeringData array of OfferingData objects, only the ID matters
      * @return FImportStatusResult array one per each array element in the input param
      * @throws AcademusException
      */
     public FImportStatusResult[] inactivateOfferings(OfferingData[] offeringData)
         throws AcademusException;

    /**
     *
     * @param m The details of the membership.
     * @return A status object detailing the results of the import.
     * @throws RemoteException If there was a problem of any ilk.
     */
    public FImportStatusResult importMembership(MemberData m) throws AcademusException;

    /**
     *
     * @param m The details of the membership.
     * @return A status object detailing the results of the update.
     * @throws RemoteException If there was a problem of any ilk.
     */
    public FImportStatusResult updateMembership(MemberData m) throws AcademusException;

    /**
     *
     * @param m The details of the membership.
     * @return A status object detailing the results of the delete.
     * @throws RemoteException If there was a problem of any ilk.
     */
    public FImportStatusResult deleteMembership(MemberData m) throws AcademusException;

    /**
     * imports Group information.
     * @param groupData
     * @return FImportStatusResult indicating whether the group has been successfully added.
     * A false indicates the Group already exists in the system or is not persistable.
     * @throws AcademusException
     */
    public FImportStatusResult importGroup(GroupData groupData)
    throws AcademusException;

    /**
     * batch import of groups
     *
     * @param groupData
     * @return FImportStatusResult[] one per each transaction.
     * @throws AcademusException
     */
    public FImportStatusResult[] importGroups(GroupData[] groupData)
    throws AcademusException;

    /**
     * deletes a Group from Academus
     * @param groupData
     * @return FImportStatusResult indicating whether the group has been successfully deleted.
     * @throws AcademusException
     */
    public FImportStatusResult deleteGroup(GroupData groupData)
    throws AcademusException;

    /**
     * attempts to delete all the given Groups. Non-transactional
     * @param groupData
     * @return FImportStatusResult[] one per each transaction.
     * @throws AcademusException
     */
    public FImportStatusResult[] deleteGroups(GroupData[] groupData)
    throws AcademusException;

    /**
     * updates the specified Group's information.
     * @param groupData
     * @return FImportStatusResult indicating whether the group has been successfully updated.
     * @throws AcademusException
     */
    public FImportStatusResult updateGroup(GroupData groupData)
    throws AcademusException;

    /**
     * attempts to update all the given Groups. Non-transactional
     * @param groupData
     * @return an array of status objects
     * @throws AcademusException
     */
    public FImportStatusResult[] updateGroups(GroupData[] groupData)
    throws AcademusException;
}
