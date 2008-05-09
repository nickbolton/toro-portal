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

import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.RemoteException;

import java.lang.reflect.Method;

import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.OfferingFactory;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.academus.domain.sor.ISystemOfRecord;
import net.unicon.academus.domain.sor.SystemOfRecordBroker;
import net.unicon.sdk.properties.UniconProperties;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.properties.RMIPropertiesType;
import net.unicon.sdk.properties.CommonPropertiesType;


import net.unicon.sdk.log.LogServiceFactory;
import net.unicon.sdk.log.ILogService;
import net.unicon.sdk.log.LogLevel;
import net.unicon.sdk.util.ExceptionUtils;

/**
 * @author Kevin Gary
 *
 */
public class RFAcademusImportService extends UnicastRemoteObject
    implements RIAcademusImportService {

    // private IImportService __internalImportService;
    private IImportService __internalImportService = null;
    private static RFAcademusImportService self = null;

    private static ILogService __logService = null;

    static {
        try {
            __logService = LogServiceFactory.instance();
        }
        catch (Throwable t) {
            // couldn't create an ILogService, try this:
            __logService = new ILogService() {
                                      public void log(LogLevel logLevel, String message) {}
                                      public void log(LogLevel logLevel, Throwable ex) {}
                                      public void log(LogLevel logLevel, String message, Throwable ex) {}
                                   };
        }
    }

    private RFAcademusImportService() throws RemoteException
    {
        UniconProperties props = UniconPropertiesFactory.getManager(CommonPropertiesType.FACTORY);
        String importServiceClass = props.getProperty("net.unicon.academus.service.IImportService.implementation");

        __logService.log(ILogService.INFO,
                                         "Instantiating IImportService implementation to class: " +
                                         importServiceClass);
        // reflect and get the service
        try {
            Class serviceClass = Class.forName(importServiceClass);
            Method m = serviceClass.getMethod("getService", null);
            // static method with no parameters
            __internalImportService = (IImportService)m.invoke(null, null);
        }
        catch (Exception exc) {
            __logService.log(ILogService.ERROR, "Exception initializing internal import service in RFAcademusImportService: " +
                                             exc.getMessage());
            exc.printStackTrace();
            throw new RemoteException(exc.getMessage());
        }
    }

    public static void initRMIImportService() throws RemoteException {
        initRMIImportService(false);
    }

    // initialization method
    public static void initRMIImportService(boolean rebindMe)
        throws RemoteException {

        try {
            if (self == null || rebindMe) {
                // Get my props
                UniconProperties props = UniconPropertiesFactory.getManager(RMIPropertiesType.RMI);
                String rmiName = props.getProperty("rmi.AcademusImportService.bindname");

                __logService.log(ILogService.INFO, "Starting RFAcademusImportService");
                self = new RFAcademusImportService();
                Naming.rebind(rmiName, self);
                __logService.log(ILogService.INFO, "Service started...");
            }
            else {
                __logService.log(ILogService.WARN, "RFAcademusImportService already initialized");
            }
        }
        catch (Exception exc) {
            __logService.log(ILogService.ERROR,
                                             "Exception initializing RFAcademusImportService: " +
                                             exc.getMessage());
            exc.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see net.unicon.academus.service.RIAcademusImportService#importUser(net.unicon.academus.service.UserData)
     */
    public FImportStatusResult importUser(UserData userData)
        throws RemoteException {

        FImportStatusResult result = null;
        try {
            // Get the System of Record.
            ISystemOfRecord sor =
               SystemOfRecordBroker.getSystemOfRecord(userData.getSource());

             // Step 1. Get an offering ID if it exists (shouldn't be there)
             String externalID = userData.getExternalID();

             try {
                 long userID = sor.getUserId(externalID);
                 // houston, we have a problem, not supposed to be one there
                 result = new FImportStatusResult(FImportStatusResult.DEFAULT_FAILURE,
                                                  new Long(userID));
             } catch (DomainException de) {
                 // Fall through...this is what we expect...
             }

            result = __internalImportService.importUser(userData);

            // post-import step. Provided the result was successful, get the new userID
            if (result.wasImportSuccessful()) {
                long userID = ((Long)result.getObject()).longValue();
                // get the User object we just created
                User user = UserFactory.getUser(userID);
                sor.addRecordInfo(user, externalID, userData.getForeignId());
                // Set successful result.
                result = FImportStatusResult.DEFAULT_SUCCESS;
            }
        }
        catch (Exception exc) {
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "importUser operation failed",
                                             RServiceError.USER_IMPORT_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                           "Exception occurred on import: " + exc.getMessage());
        }
        return result;
    }

    /* (non-Javadoc)
     * @see net.unicon.academus.service.RIAcademusImportService#importUsers(net.unicon.academus.service.UserData[])
     */
    public FImportStatusResult[] importUsers(UserData[] userData)
        throws RemoteException {

        FImportStatusResult[] result = null;
        try {
            if (userData != null && userData.length > 0) {
                result = new FImportStatusResult[userData.length];
                for (int i = 0; i < userData.length; ) {
                    result[i] = this.importUser(userData[i]);
                }
            }
        }
        catch (Exception exc) {
            result = new FImportStatusResult[1];
            result[0] = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "importUsers operation failed",
                                             RServiceError.USER_BATCH_IMPORT_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                           "Exception occurred on batch import: " + exc.getMessage());
        }
        return result;
    }

    /**
     * Deletes a User from Academus
     * @param username username of the user to remove
     * @param sourceName name of the external system of record
     * @return a status indicating whether the user was deleted or not, or any exceptions
     */
    public FImportStatusResult deleteUser(UserData userData) throws RemoteException
    {
        FImportStatusResult result = null;
        try {
            // Get the System of Record.
            ISystemOfRecord sor =
                 SystemOfRecordBroker.getSystemOfRecord(userData.getSource());

            // Step 1. Get an offering ID if it exists (shouldn't be there)
            String externalID = userData.getExternalID();
            long userID = sor.getUserId(externalID);

            if (userID == -1L) {
                // houston, we have a problem, there is supposed to be one there
                result = new FImportStatusResult(FImportStatusResult.DEFAULT_FAILURE,
                                                 new Long(userID));
            }
            result = __internalImportService.deleteUser(userData);
        }
        catch (Exception exc) {
            __logService.log(ILogService.ERROR,
                           "Exception occurred on import service deleteUser: " + exc.getMessage());
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "deleteUser operation failed",
                                             RServiceError.USER_DELETE_FAILURE,
                                             ExceptionUtils.toXML(exc));
        }
        return result;
    }

    /**
     * attempts to delete all the given Users. Non-transactional
     * @param username
     * @param sourceName name of the external system of record
     * @return an array of status object corresponding to the usernames in the param
     * @throws RemoteException
     */
    public FImportStatusResult[] deleteUsers(UserData[] userData) throws RemoteException
    {
        FImportStatusResult[] result = null;
        try {
            if (userData != null && userData.length > 0) {
                result = new FImportStatusResult[userData.length];
                for (int i = 0; i < userData.length; ) {
                    result[i] = this.deleteUser(userData[i]);
                }
            }
        }
        catch (Exception exc) {
            result = new FImportStatusResult[1];
            result[0] = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "deleteUsers operation failed",
                                             RServiceError.USER_BATCH_DELETE_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                           "Exception occurred on batch import delete: " + exc.getMessage());
        }
        return result;
    }

    /**
     * updates the specified User's information.
     * @param userData
     * @return
     * @throws RemoteException
     */
    public FImportStatusResult updateUser(UserData userData) throws RemoteException
    {
        FImportStatusResult result = null;
        try {
            result = __internalImportService.updateUser(userData);
        }
        catch (Exception exc) {
            __logService.log(ILogService.ERROR,
                           "Exception occurred on import service updateUser: " + exc.getMessage());
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "updateUser operation failed",
                                             RServiceError.USER_UPDATE_FAILURE,
                                             ExceptionUtils.toXML(exc));
        }
        return result;
    }

    /**
     *
     * @param userData
     * @return an array of status objects
     * @throws RemoteException
     */
    public FImportStatusResult[] updateUsers(UserData[] userData) throws RemoteException
    {
        FImportStatusResult[] result = null;
        try {
            if (userData != null && userData.length > 0) {
                result = new FImportStatusResult[userData.length];
                for (int i = 0; i < userData.length; ) {
                    result[i] = this.updateUser(userData[i]);
                }
            }
        }
        catch (Exception exc) {
            result = new FImportStatusResult[1];
            result[0] = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "updateUsers operation failed",
                                             RServiceError.USER_BATCH_UPDATE_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                           "Exception occurred on batch import delete: " + exc.getMessage());
        }
        return result;
    }

    public FImportStatusResult syncUser(UserData u) throws RemoteException {

        FImportStatusResult result = null;
        try {

            // Get the System of Record.
            ISystemOfRecord sor = SystemOfRecordBroker.getSystemOfRecord(u.getSource());

            // Choose a path -- feed through this class again b/c it handles SOR for this entity.
            if (sor.hasUser(u.getExternalID())) {
                result = this.updateUser(u);
            } else {
                result = this.importUser(u);
            }

        } catch (Exception exc) {
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "syncUser operation failed",
                                             RServiceError.USER_SYNC_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                "Exception occurred on user sync: " + exc.getMessage());
        }
        return result;
    }

    /**
     * imports Membership information.
     *
     * @param m The details of membership.
     * @return FImportStatusResult indicating whether the membership has been successfully added.
     * @throws RemoteException
     */
    public FImportStatusResult importMembership(MemberData m) throws RemoteException {

        FImportStatusResult result = null;
        try {
            result = __internalImportService.importMembership(m);
        } catch (Exception exc) {
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "importMembership operation failed",
                                             RServiceError.MEMBERSHIP_IMPORT_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                "Exception occurred on membership import: " + exc.getMessage());
        }
        return result;
    }

    /**
     * updates Membership information.
     *
     * @param m The details of membership.
     * @return FImportStatusResult indicating whether the membership has been successfully updated.
     * @throws RemoteException
     */
    public FImportStatusResult updateMembership(MemberData m) throws RemoteException {

        FImportStatusResult result = null;
        try {
            result = __internalImportService.updateMembership(m);
        } catch (Exception exc) {
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "updateMembership operation failed",
                                             RServiceError.MEMBERSHIP_UPDATE_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                "Exception occurred on membership update: " + exc.getMessage());
        }
        return result;
    }

    /**
     * deletes a Membership relationship.
     *
     * @param m The details of membership.
     * @return FImportStatusResult indicating whether the membership has been successfully removed.
     * @throws RemoteException
     */
    public FImportStatusResult deleteMembership(MemberData m) throws RemoteException {

        FImportStatusResult result = null;
        try {
            result = __internalImportService.deleteMembership(m);
        } catch (Exception exc) {
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "deleteMembership operation failed",
                                             RServiceError.MEMBERSHIP_DELETE_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                "Exception occurred on membership delete: " + exc.getMessage());
        }
        return result;
    }

    public FImportStatusResult syncMembership(MemberData m) throws RemoteException {

        FImportStatusResult result = null;
        try {

            // (Attempt to) Remove and then add...it's the best we can do.
            __internalImportService.deleteMembership(m);
            result = __internalImportService.importMembership(m);

/* The normal pattern doesn't work for memberships...
            // Choose a path.
            if (sor.hasMembership(m.getExternalID())) {
                // NB:  Do nothing...there's no such thing
                // as a membership update in academus.
            } else {
                result = __internalImportService.importMembership(m);
            }
*/

        } catch (Exception exc) {
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "syncMembership operation failed",
                                             RServiceError.MEMBERSHIP_SYNC_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                "Exception occurred on membership sync: " + exc.getMessage());
        }
        return result;
    }

    /**
     * imports Group information.
     * @param groupData
     * @return FImportStatusResult indicating whether the group has been successfully added.
     * A false indicates the Group already exists in the system or is not persistable.
     * @throws RemoteException
     */
    public FImportStatusResult importGroup(GroupData groupData)
    throws RemoteException {

        FImportStatusResult result = null;
        try {
            result = __internalImportService.importGroup(groupData);
        } catch (Exception exc) {
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "importGroup operation failed",
                                             RServiceError.GROUP_IMPORT_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                "Exception occurred on group import: " + exc.getMessage());
        }
        return result;
    }

    /**
     * batch import of groups
     * @param groupData
     * @return FImportStatusResult[] one per each transaction.
     * @throws RemoteException
     */
    public FImportStatusResult[] importGroups(GroupData[] groupData)
    throws RemoteException {

        FImportStatusResult[] result = null;
        try {
            result = __internalImportService.importGroups(groupData);
        } catch (Exception exc) {
            result = new FImportStatusResult[1];
            result[0] = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                                "updateGroups operation failed",
                                                RServiceError.GROUP_BATCH_IMPORT_FAILURE,
                                                ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
            "Exception occurred on batch group import: " + exc.getMessage());
        }
        return result;
    }

    /**
     * deletes a Group from Academus
     * @param groupData
     * @return a status indicating whether the group was deleted or not, or any exceptions
     * @throws RemoteException
     */
    public FImportStatusResult deleteGroup(GroupData groupData)
    throws RemoteException {

        FImportStatusResult result = null;
        try {
            result = __internalImportService.deleteGroup(groupData);
        } catch (Exception exc) {
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "deleteGroup operation failed",
                                             RServiceError.GROUP_DELETE_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                "Exception occurred on group delete: " + exc.getMessage());
        }
        return result;
    }

    /**
     * attempts to delete all the given Groups. Non-transactional
     * @param groupData
     * @return FImportStatusResult[] one per each transaction.
     * @throws RemoteException
     */
    public FImportStatusResult[] deleteGroups(GroupData[] groupData)
    throws RemoteException {

        FImportStatusResult[] result = null;
        try {
            result = __internalImportService.deleteGroups(groupData);
        } catch (Exception exc) {
            result    = new FImportStatusResult[1];
            result[0] = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                                "deleteGroups operation failed",
                                                RServiceError.GROUP_BATCH_DELETE_FAILURE,
                                                ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
            "Exception occurred on batch group delete: " + exc.getMessage());
        }
        return result;
    }

    /**
     * updates the specified Group's information.
     * @param groupData
     * @return FImportStatusResult indicating whether the group has been successfully updated.
     * @throws RemoteException
     */
    public FImportStatusResult updateGroup(GroupData groupData)
    throws RemoteException {

        FImportStatusResult result = null;
        try {
            result = __internalImportService.updateGroup(groupData);
        } catch (Exception exc) {
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "updateGroup operation failed",
                                             RServiceError.GROUP_UPDATE_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                "Exception occurred on group update: " + exc.getMessage());
        }
        return result;
    }

    /**
     * attempts to update all the given Groups. Non-transactional
     * @param groupData
     * @return an array of status objects
     * @throws RemoteException
     */
    public FImportStatusResult[] updateGroups(GroupData[] groupData)
    throws RemoteException {

        FImportStatusResult[] result = null;
        try {
            result = __internalImportService.updateGroups(groupData);
        } catch (Exception exc) {
            result    = new FImportStatusResult[1];
            result[0] = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                                "updateGroups operation failed",
                                                RServiceError.GROUP_BATCH_UPDATE_FAILURE,
                                                ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
            "Exception occurred on batch group update: " + exc.getMessage());
        }
        return result;
    }

    public FImportStatusResult syncGroup(GroupData g) throws RemoteException {

        FImportStatusResult result = null;
        try {

            // Get the System of Record.
            ISystemOfRecord sor = SystemOfRecordBroker.getSystemOfRecord(g.getSource());

            // Choose a path.
            if (sor.hasGroup(g.getExternalID())) {
                result = __internalImportService.updateGroup(g);
            } else {
                result = __internalImportService.importGroup(g);
            }

        } catch (Exception exc) {
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "syncGroup operation failed",
                                             RServiceError.GROUP_SYNC_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                "Exception occurred on group sync: " + exc.getMessage());
        }
        return result;
    }

    /**
     * imports Topic information.
     *
     * @param t The details of the topic.
     * @return FImportStatusResult indicating whether the topic has been successfully added.
     * @throws RemoteException
     */
    public FImportStatusResult importTopic(TopicData t)
    throws RemoteException {

        FImportStatusResult result = null;
        try {
            result = __internalImportService.importTopic(t);
        } catch (Exception exc) {
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "importTopic operation failed",
                                             RServiceError.TOPIC_IMPORT_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                "Exception occurred on topic import: " + exc.getMessage());
        }
        return result;
    }

    /**
     * updates Topic information.
     *
     * @param t The details of the topic.
     * @return FImportStatusResult indicating whether the topic has been successfully updated.
     * @throws RemoteException
     */
    public FImportStatusResult updateTopic(TopicData t)
    throws RemoteException {

        FImportStatusResult result = null;
        try {
            result = __internalImportService.updateTopic(t);
        } catch (Exception exc) {
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "updateTopic operation failed",
                                             RServiceError.TOPIC_UPDATE_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                "Exception occurred on topic update: " + exc.getMessage());
        }
        return result;
    }

    /**
     * deletes a Topic relationship.
     *
     * @param t The details of the topic.
     * @return FImportStatusResult indicating whether the topic has been successfully removed.
     * @throws RemoteException
     */
    public FImportStatusResult deleteTopic(TopicData t)
    throws RemoteException {

        FImportStatusResult result = null;
        try {
            result = __internalImportService.deleteTopic(t);
        } catch (Exception exc) {
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "deleteTopic operation failed",
                                             RServiceError.TOPIC_DELETE_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                "Exception occurred on topic delete: " + exc.getMessage());
        }
        return result;
    }

    public FImportStatusResult syncTopic(TopicData t) throws RemoteException {

        FImportStatusResult result = null;
        try {

            // Get the System of Record.
            ISystemOfRecord sor = SystemOfRecordBroker.getSystemOfRecord(t.getSource());

            // Choose a path.
            if (sor.hasTopic(t.getExternalID())) {
                result = __internalImportService.updateTopic(t);
            } else {
                result = __internalImportService.importTopic(t);
            }

        } catch (Exception exc) {
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "syncTopic operation failed",
                                             RServiceError.TOPIC_SYNC_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                "Exception occurred on topic sync: " + exc.getMessage());
        }
        return result;
    }

    /*
     *  The steps for importing an Offering:
     * 1. Step 1. Get an offering ID if it exists (shouldn't be there)
     * 2. Step2: it is a new offering. now let's check Topics
     * 3. Step 3: swap in the internal topic ids on the OfferingData
     * 4. Step 4: Do the import
     * 5. Step 5: post-import step. Provide the result was successful, get the new offeringID
     * --> steps 1-3 and 5 happen here. step 4 happens in the local import service impl
     */
    /**
     * imports an offering into Academus
     * @param offeringData array of OfferingData objects representing Offering to import
     * @return FImportStatusResult[] indicating whether imports were successful or not
     * @throws RemoteException only if the call itself fails, not if any specific import fails
     */
    public FImportStatusResult importOffering(OfferingData offeringData)
        throws RemoteException {
        FImportStatusResult result = null;
        try {
            // Get the System of Record.
             ISystemOfRecord sor =
                 SystemOfRecordBroker.getSystemOfRecord(offeringData.getSource());

            // Step 1. Get an offering ID if it exists (shouldn't be there)
            String externalID = offeringData.getExternalID();

            try {
                long offeringID = sor.getOfferingId(externalID);
                // houston, we have a problem, not supposed to be one there
                result = new FImportStatusResult(FImportStatusResult.DEFAULT_FAILURE,
                                                 new Long(offeringID));
            } catch (DomainException de) {
                // Fall through...this is what we expect...
            }

            // Translate Topics.
            translateTopicIds(offeringData, sor);

            // Step 4: Do the import
            result = __internalImportService.importOffering(offeringData);

            // Step 5: post-import step. Provide the result was successful, get the new offeringID
            if (result.wasImportSuccessful()) {
                long offeringID = ((Long)result.getObject()).longValue();
                // get the Offering object we just created
                Offering offering = OfferingFactory.getOffering(offeringID);
                sor.addRecordInfo(offering, externalID);
                // Set successful result.
                result = FImportStatusResult.DEFAULT_SUCCESS;
            }
            // else if result was not successful, we'll just return it

        }
        catch (Exception exc) {
            __logService.log(
                ILogService.ERROR,
                "Exception occurred on import service importOffering: "
                    + exc.getMessage());
            if (result == null) {
                result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                                 "importOffering operation failed",
                                                 RServiceError.OFFERING_IMPORT_FAILURE,
                                                 ExceptionUtils.toXML(exc));
            }
        }
        return result;
    }

    /**
     * imports offerings into Academus
     * @param offeringData array of OfferingData objects representing Offering to import
     * @return FImportStatusResult[] indicating whether imports were successful or not
     * @throws RemoteException
     */
    public FImportStatusResult[] importOfferings(OfferingData[] offeringData)
        throws RemoteException {
        FImportStatusResult[] result = null;
        try {
            if (offeringData != null && offeringData.length > 0) {
                result = new FImportStatusResult[offeringData.length];
                for (int i = 0; i < offeringData.length; ) {
                    result[i] = this.importOffering(offeringData[i]);
                }
            }
        }
        catch (Exception exc) {
            result = new FImportStatusResult[1];
            result[0] = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                                "importOfferings operation failed",
                                                RServiceError.OFFERING_BATCH_IMPORT_FAILURE,
                                                ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(
                ILogService.ERROR,
                "Exception occurred on batch import offering: "
                    + exc.getMessage());
        }
        return result;
    }

    /**
     * updates an existing offering according to the marshalled parameter object
     * @param offeringData
     * @return FImportStatusResult
     * @throws RemoteException
     */
    public FImportStatusResult updateOffering(OfferingData offeringData)
        throws RemoteException {
        FImportStatusResult result = null;
        try {
            // Get the System of Record.
            ISystemOfRecord sor =
               SystemOfRecordBroker.getSystemOfRecord(offeringData.getSource());

            // Get an offering ID if it exists (should be there)
            String externalID = offeringData.getExternalID();
            long offeringID = sor.getOfferingId(externalID);
            if (offeringID == -1L) {
               // houston, we have a problem, supposed to be one there
               result = new FImportStatusResult(FImportStatusResult.DEFAULT_FAILURE,
                                                externalID);
            }
            else {
                offeringData.setOfferingID(String.valueOf(offeringID));
                result = __internalImportService.updateOffering(offeringData);
            }
        }
        catch (Exception exc) {
            __logService.log(
                ILogService.ERROR,
                "Exception occurred on import service updateOffering: "
                    + exc.getMessage());
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "updateOffering operation failed",
                                             RServiceError.OFFERING_UPDATE_FAILURE,
                                             ExceptionUtils.toXML(exc));
        }
        return result;
    }

    /**
     * batch version of updateOffering
     * @param offeringData
     * @return FImportStatusResult array one per each array element in the input param
     * @throws RemoteException
     */
    public FImportStatusResult[] updateOfferings(OfferingData[] offeringData)
        throws RemoteException {
        FImportStatusResult[] result = null;
        try {
            if (offeringData != null && offeringData.length > 0) {
                result = new FImportStatusResult[offeringData.length];
                for (int i = 0; i < offeringData.length; ) {
                    result[i] = this.updateOffering(offeringData[i]);
                }
            }
        }
        catch (Throwable t) {
            result = new FImportStatusResult[1];
            result[0] = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                                "updateOfferings operation failed",
                                                RServiceError.OFFERING_BATCH_UPDATE_FAILURE,
                                                ExceptionUtils.toXML(t));
            // log something here
            __logService.log(ILogService.ERROR,
                                             "Exception occurred on batch updateOfferings: "
                                             + t.getMessage());
        }
        return result;
    }

    /**
     * Inactivates an offering (marks as deleted but leaves data in the database)
     * @param offeringData all we care about in here is the external OfferingID
     * @return FImportStatusResult
     * @throws RemoteException
     */
    public FImportStatusResult inactivateOffering(OfferingData offeringData)
        throws RemoteException {
        FImportStatusResult result = null;

        try {
            // Get the System of Record.
            ISystemOfRecord sor =
                 SystemOfRecordBroker.getSystemOfRecord(offeringData.getSource());

            // Step 1. Get an offering ID if it exists (shouldn't be there)
            String externalID = offeringData.getExternalID();
            long offeringID = sor.getOfferingId(externalID);

            if (offeringID == -1L) {
                // houston, we have a problem, there is supposed to be one there
                result = new FImportStatusResult(FImportStatusResult.DEFAULT_FAILURE,
                                                 new Long(offeringID));
            }
            else {
                offeringData.setOfferingID(String.valueOf(offeringID));
                result = __internalImportService.inactivateOffering(offeringData);
            }
        }
        catch (Exception exc) {
            __logService.log(
                ILogService.ERROR,
                "Exception occurred on import service inactivateOffering: "
                    + exc.getMessage());
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "inactivateOffering operation failed",
                                             RServiceError.OFFERING_INACTIVATE_FAILURE,
                                             ExceptionUtils.toXML(exc));
        }
        return result;
    }

    /**
     * batch version of inactivateOffering
     * @param offeringID
     * @return FImportStatusResult array one per each array element in the input param
     * @throws RemoteException
     */
    public FImportStatusResult[] inactivateOfferings(OfferingData[] offeringData)
        throws RemoteException {
        FImportStatusResult[] result = null;
        try {
            if (offeringData != null && offeringData.length > 0) {
                result = new FImportStatusResult[offeringData.length];
                for (int i = 0; i < offeringData.length; ) {
                    result[i] = this.inactivateOffering(offeringData[i]);
                }
            }
        }
        catch (Exception exc) {
            result = new FImportStatusResult[1];
            result[0] = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                                "inactivateOfferings operation failed",
                                                RServiceError.OFFERING_BATCH_INACTIVATE_FAILURE,
                                                ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(
                ILogService.ERROR,
                "Exception occurred on batch inactivateOfferings: "
                    + exc.getMessage());
        }
        return result;
    }

    public FImportStatusResult syncOffering(OfferingData o) throws RemoteException {

        FImportStatusResult result = null;
        try {

            // Get the System of Record.
            ISystemOfRecord sor = SystemOfRecordBroker.getSystemOfRecord(o.getSource());

            // Choose a path -- feed through this class again b/c it handles SOR for this entity.
            if (sor.hasOffering(o.getExternalID())) {
                result = this.updateOffering(o);
            } else {
                result = this.importOffering(o);
            }

        } catch (Exception exc) {
            result = new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                             "syncOffering operation failed",
                                             RServiceError.OFFERING_SYNC_FAILURE,
                                             ExceptionUtils.toXML(exc));
            // log something here
            __logService.log(ILogService.ERROR,
                "Exception occurred on offering sync: " + exc.getMessage());
        }
        return result;
    }

    private static void translateTopicIds(OfferingData o, ISystemOfRecord sor) throws DomainException {

        String[] externalTopicIDs = o.getTopicIDs();
        String[] internalTopicIDs = null;

        // Note: It is not this service's place to enforce whether Offering's
        // must have at least one topic. That should come from the internal
        // import service via the internal domain object factory
        if (externalTopicIDs != null && externalTopicIDs.length > 0) {
            internalTopicIDs = new String[externalTopicIDs.length];
            for (int i = 0; i < internalTopicIDs.length; i++) {
                // again, we don't care if the topic id was really there or
                // not. That will be handled downstream.
                internalTopicIDs[i] = "" + sor.getTopicId(externalTopicIDs[i]);
            }
        }
        else {
            internalTopicIDs = new String[0];
        }

        // Step 3: swap in the internal topic ids on the OfferingData
        o.setTopicIDs(internalTopicIDs);

    }

}
