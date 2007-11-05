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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import java.sql.Timestamp;

import net.unicon.academus.common.AcademusException;
import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.academus.domain.lms.DefaultRoleType;
import net.unicon.academus.domain.lms.EnrollmentModel;
import net.unicon.academus.domain.lms.EnrollmentStatus;
import net.unicon.academus.domain.lms.Memberships;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.OfferingFactory;
import net.unicon.academus.domain.lms.Role;
import net.unicon.academus.domain.lms.RoleFactory;
import net.unicon.academus.domain.lms.Topic;
import net.unicon.academus.domain.lms.TopicFactory;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.academus.domain.sor.AccessType;
import net.unicon.academus.domain.sor.IEntityRecordInfo;
import net.unicon.academus.domain.sor.ISystemOfRecord;
import net.unicon.academus.domain.sor.SystemOfRecordBroker;
import net.unicon.academus.service.calendar.CalendarServiceFactory;
import net.unicon.portal.domain.ChannelClassFactory;
import net.unicon.portal.domain.ChannelMode;



import net.unicon.portal.groups.UniconGroupsException;
import net.unicon.portal.groups.UniconGroupServiceFactory;
import net.unicon.portal.groups.UniconGroupService;
import net.unicon.portal.groups.GroupFactory;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.IMember;

import net.unicon.sdk.properties.*;
import net.unicon.sdk.log.*;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.sdk.util.ExceptionUtils;


/**
 * @author Kevin Gary
 *
 */
public final class FAcademusDefaultImportService implements IImportService {

    private static IImportService __service = null;
    private static String localGroupService = null;
    private static IGroup externalRootGroup = null;

    private ILogService __logService = null;

    static {
        try {
            localGroupService = UniconPropertiesFactory.getManager(
                PortalPropertiesType.PORTAL).getProperty(
                    "net.unicon.portal.groups.GroupImpl.localGroupService");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static IImportService getService()  {
        if (__service == null) {
            __service = new FAcademusDefaultImportService();
        }
        return __service;
    }

    private FAcademusDefaultImportService() {
        try {
            setLogService(LogServiceFactory.instance());
        }
        catch (Throwable t) {
            // couldn't create an ILogService, try this:
            setLogService(new ILogService() {
                            public void log(LogLevel logLevel, String message) {}
                            public void log(LogLevel logLevel, Throwable ex) {}
                            public void log(LogLevel logLevel, String message, Throwable ex) {}
                          });
        }
    }

    public void setLogService(ILogService logger) {
        if (logger != null) {
            __logService = logger;
        }
    }

    /* (non-Javadoc)
     * @see net.unicon.academus.service.IImportService#importUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public FImportStatusResult importUser(UserData userData)
    throws AcademusException {
	    String templateUser = null;
            FImportStatusResult rval = null;
            try {

                if (userData == null) {  // should never happen
                    rval = FImportStatusResult.FAILED_BAD_DATA;
                }
                else {
                    String createAcademusUser = UniconPropertiesFactory.getManager(
                                                ImportServicePropertiesType.IMPORT).getProperty(
                                                "net.unicon.academus.service.ImportService.createAcademusUser");
                    // if the user already exists and we are using Academus to authenticate then return false
                    if (UserFactory.usernameExists(userData.getUserName()) && !createAcademusUser.equals("false")) {
                        // failed precondition that user not in DB already
                        rval = FImportStatusResult.FAILED_PRECONDITION;
                    }
                    else {
                        // Obtain the sor.
                        ISystemOfRecord sor = null;
                        try {
                            sor = SystemOfRecordBroker.getSystemOfRecord(
                                userData.getSource());
                        } catch (DomainException de) {
                            return new FImportStatusResult(
                                FImportStatusResult.FAILED_EXCEPTION,
                                "The specified source name was:  " + userData.getSource(),
                                ServiceError.SOR_NOT_FOUND,
                                ExceptionUtils.toXML(de));
                        }

                        /*
                            If valid System Role exists, add corresponding
                            template user attribute.
                        */
                        String systemRole =
                            userData.getAttribute(UserData.SYSTEM_ROLE);

                        if (systemRole != null
                                && systemRole.trim().length() > 0) {

                            // Check if valid default system role.
                            if (!DefaultRoleType.SYSTEM.isDefault(systemRole)) {
                                return new FImportStatusResult(
                                    FImportStatusResult.FAILED,
                                    "The specified role was:  " + systemRole,
                                    ServiceError.USER_INVALID_SYSTEM_ROLE,
                                    null);
                            }

                            // Template user name for the system role.
                            templateUser = TemplateUserRegistry.getTemplateUserName(systemRole);

                            /*
                                Adding a template user name to the attributes
                                will add the user to the appropriate groups
                                and layouts for the given system role.
                            */

                            if (UserFactory.usernameExists(templateUser)) {
                                //If the template user exists,
                                //add to user attributes.
                                userData.getMetadataAttributes().put(
                                    UserFactory.TEMPLATE_KEY, templateUser);
                            } else {
                                return new FImportStatusResult(
                                    FImportStatusResult.FAILED_EXCEPTION,
                                    "The specified role was:  " + systemRole,
                                    ServiceError.USER_NO_TEMPLATE_USER,
                                    null);
                            }

                        } else {
                            return new FImportStatusResult(
                                    FImportStatusResult.FAILED,
                                    "The user was:  " + userData.getUserName(),
                                    ServiceError.USER_NO_SYSTEM_ROLE,
                                    null);
                        } // end if for systemRole

                        // Call createUser
			if (!createAcademusUser.equals("false")) {
	                        User user = UserFactory.createUser(
	                            userData.getUserName(),
	                            userData.getPassword(),
	                            userData.getFirstName(),
	                            userData.getLastName(),
	                            userData.getEmail(),
	                            (String)userData.getAttribute(UserData.PREFIX),
	                            (String)userData.getAttribute(UserData.SUFFIX),
	                            (String)userData.getAttribute(UserData.ADDRESS1),
	                            (String)userData.getAttribute(UserData.ADDRESS2),
	                            (String)userData.getAttribute(UserData.CITY),
	                            (String)userData.getAttribute(UserData.STATE),
	                            (String)userData.getAttribute(UserData.ZIP),
	                            (String)userData.getAttribute(UserData.PHONE),
	                            (String)userData.getAttribute(UserData.SYSTEM_ROLE),
        	                    userData.getMetadataAttributes());
                        // NB:  SOR entry is handled by the IImportService implementation.
                        	rval = new FImportStatusResult(FImportStatusResult.DEFAULT_SUCCESS,
	                                                       new Long(user.getId()));
			} else {
			// User information exists in an external auth source (LDAP, etc).
				long userId = UserFactory.getUserId(userData.getUserName(), templateUser, false);
	                        rval = new FImportStatusResult(FImportStatusResult.DEFAULT_SUCCESS,
		                                               new Long(userId));
			}



                    } // end if for username
                } // end if for userData
            }
            catch (Throwable t) {
                // unable to import, return failed with exception
                rval = new FImportStatusResult(
                        FImportStatusResult.FAILED_EXCEPTION,
                        "EXCEPTION:  " + t.getMessage(),
                        ServiceError.UNRECOGNIZED_ERROR,
                        ExceptionUtils.toXML(t));
            }

            return rval;
    }

    /* (non-Javadoc)
     * @see net.unicon.academus.service.IImportService#importUsers(java.lang.String[], java.lang.String[], java.lang.String[], java.lang.String[], java.lang.String[], FImportStatusResult)
     */
    public FImportStatusResult[] importUsers(UserData[] userData)
        throws AcademusException {

        // You would think we could batch a transaction here - but no!
        if (userData == null || userData.length == 0) {
            return new FImportStatusResult[0];
        }
        FImportStatusResult[] rval = new FImportStatusResult[userData.length];
        for (int i = 0; i < rval.length; i++) {
            rval[i] = importUser(userData[i]);
        }
        return rval;
    }

    /**
     * Deletes a User from Academus
     * @param username username of the user to remove
     * @param sourceName name of the external system of record
     * @return a status indicating whether the user was deleted or not, or any exceptions
     */
    public FImportStatusResult deleteUser(UserData userData) throws AcademusException
    {
        FImportStatusResult rval = null;
        User user = null;
        String username = userData.getUserName();
        String sourceName = userData.getSource();

        try {
            if (username == null || username.trim().length() == 0) {  // should never happen
                rval = FImportStatusResult.FAILED_BAD_DATA;
            }
            else {
                try {
                    user = UserFactory.getUser(username);
                }
                catch (Throwable t) {
                    return new FImportStatusResult(
                                FImportStatusResult.FAILED_EXCEPTION,
                                "The username was:  " + username,
                                ServiceError.USER_NOT_FOUND,
                                ExceptionUtils.toXML(t));
                }
                if (rval == null) {

                    // Obtain the sor.
                    ISystemOfRecord sor = null;
                    try {
                        sor = SystemOfRecordBroker.getSystemOfRecord(sourceName);
                    } catch (DomainException de) {
                        return new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                            "The specified source name was:  " + sourceName,
                                            ServiceError.SOR_NOT_FOUND,
                                            ExceptionUtils.toXML(de));
                    }

                    // otherwise call deleteUser

                    // Remove the sor record first.
                    try {
                        sor.deleteRecordInfo(user);
                    } catch (DomainException de) {
                        return new FImportStatusResult(
                            FImportStatusResult.FAILED_EXCEPTION,
                            "Unable to remove System of Record information "
                                            + "for the specified user:  "
                                            + username,
                            ServiceError.UNRECOGNIZED_ERROR,
                            ExceptionUtils.toXML(de));
                    }
                    // Remove the User.
                    try {
                        UserFactory.delete(username, sor);
                        rval = FImportStatusResult.DEFAULT_SUCCESS;
                    }
                    catch (ItemNotFoundException ifne) {
                        // this shouldn't happen since we called usernameExists above, but
                        // is possible since we're not synchronized. Fail on precondition.
                        rval = FImportStatusResult.FAILED_BAD_DATA;
                    }
                    catch (Throwable t) {
                        // screw it
                        rval = new FImportStatusResult(
                            FImportStatusResult.FAILED_EXCEPTION,
                                "EXCEPTION:  " + t.getMessage(),
                                ServiceError.UNRECOGNIZED_ERROR,
                                ExceptionUtils.toXML(t));
                    }
                }
            }
        }
        catch (Throwable t) {
            rval = new FImportStatusResult(
                            FImportStatusResult.FAILED_EXCEPTION,
                                "EXCEPTION:  " + t.getMessage(),
                                ServiceError.UNRECOGNIZED_ERROR,
                                ExceptionUtils.toXML(t));
        }
        return rval;
    }

    /**
     * attempts to delete all the given Users. Non-transactional
     * @param userData
     * @return an array of status object corresponding to the usernames in the param
     * @throws RemoteException
     */
    public FImportStatusResult[] deleteUsers(UserData[] userData) throws AcademusException
    {
        // You would think we could batch a transaction here - but no!
        if (userData == null || userData.length == 0) {
            return new FImportStatusResult[0];
        }
        FImportStatusResult[] rval = new FImportStatusResult[userData.length];
        for (int i = 0; i < rval.length; i++) {
            rval[i] = deleteUser(userData[i]);
        }
        return rval;
    }

    /**
     * updates the specified User's information.
     * @param userData
     * @return
     * @throws AcademusException
     */
    public FImportStatusResult updateUser(UserData userData) throws AcademusException
    {
        FImportStatusResult rval = null;
        User user = null;

        try {
            if (userData == null) {  // should never happen
                rval = FImportStatusResult.FAILED_BAD_DATA;
            }
            else {
                // if the user already exists then return false
                String createAcademusUser = UniconPropertiesFactory.getManager(
                                            ImportServicePropertiesType.IMPORT).getProperty(
                                            "net.unicon.academus.service.ImportService.createAcademusUser");
                user = UserFactory.getUser(userData.getUserName());

                if (user == null && !createAcademusUser.equals("false")) {
                    // failed precondition that user not in DB already
                    rval = FImportStatusResult.FAILED_PRECONDITION;
                }
                else {

                    // Update user's system role if it is valid.
                    String systemRole =
                        userData.getAttribute(UserData.SYSTEM_ROLE);

                    if (systemRole != null) {
                        // Check if valid default system role.
                        if (!DefaultRoleType.SYSTEM.isDefault(systemRole)) {
                            return new FImportStatusResult(
                                FImportStatusResult.FAILED,
                                "The specified role was:  " + systemRole,
                                ServiceError.USER_INVALID_SYSTEM_ROLE,
                                null);
                        }

                        /*
                            Now check which default system role user
                            is a member of and change if necessary.
                        */
                        try {
                            IGroup systemRoleGroup = null;
                            IMember groupMember = user.getGroupMember();
                            List defaultRoles =
                                RoleFactory.getDefaultRoles(Role.SYSTEM);
                            defaultRoles.addAll(
                                RoleFactory.getDefaultRoles(
                                    Role.USER_DEFINED|Role.SYSTEM));

                            for (int i = 0; i < defaultRoles.size(); i++) {
                                systemRoleGroup =
                                    ((Role)defaultRoles.get(i)).getGroup();
                                boolean isMember =
                                    systemRoleGroup.contains(groupMember);
                                boolean roleMatch =
                                    systemRole.equals(
                                        systemRoleGroup.getName());

                                if (roleMatch && !isMember) {
                                    // Add user as member of this group.
                                    systemRoleGroup.addMember(groupMember);
                                } else if (!roleMatch && isMember) {
                                    // Remove user from this group membership.
                                    systemRoleGroup.removeMember(groupMember);
                                }

                            } // end for
                        } catch (Throwable t) {
                            rval = new FImportStatusResult(
                                    FImportStatusResult.FAILED_EXCEPTION,
                                    "Unable to update the specified System Role.  "
                                                + "EXCEPTION:  " + t.getMessage(),
                                    ServiceError.UNRECOGNIZED_ERROR,
                                    ExceptionUtils.toXML(t));
                        } // end try

                    } // end if

                    // update the user's state based on attribute values filled in
                    // on UserData. Set all the attributes then call persist
                    user.setFirstName(userData.getFirstName());
                    user.setLastName(userData.getLastName());

                    /* Currently do not allow to update that password in the
                     * system through SIS.  User would have to go through
                     * the academus UserAdmin tool. Even though the code is
                     * allowing it here, the UserFactory is not update in
                     * the persist method.  You have to call another method
                     * because not all object populate the password field,
                     * so we don't want to accidently change someones password
                     * on accident.  -H2 */
                    user.setPassword(userData.getPassword());
                    user.setEmail(userData.getEmail());

                    // deal with the optional attributes, then move persist down to the end
                    user.setPrefix(userData.getAttribute(UserData.PREFIX));
                    user.setSuffix(userData.getAttribute(UserData.SUFFIX));
                    user.setAddress1(userData.getAttribute(UserData.ADDRESS1));
                    user.setAddress2(userData.getAttribute(UserData.ADDRESS2));
                    user.setCity(userData.getAttribute(UserData.CITY));
                    user.setState(userData.getAttribute(UserData.STATE));
                    user.setZip(userData.getAttribute(UserData.ZIP));
                    user.setRole(userData.getAttribute(UserData.SYSTEM_ROLE));
                    user.setPhone(userData.getAttribute(UserData.PHONE));
                    user.setAttribute(UserData.COUNTRY, userData.getAttribute(UserData.COUNTRY));

                    // now deal with metadata attributes
                    user.clearAttributes();

                    Map metadataAttrs = userData.getMetadataAttributes();

                    if (metadataAttrs != null) {
                        Set metadataEntries = metadataAttrs.entrySet();
                        Map.Entry mdEntry = null;
                        for (Iterator mdIter = metadataEntries.iterator(); mdIter.hasNext(); ) {
                            mdEntry = (Map.Entry)mdIter.next();
                            user.setAttribute((String)mdEntry.getKey(),
                                              (String)mdEntry.getValue());
                        }
                    }

                    // Obtain the sor.
                    ISystemOfRecord sor = null;
                    try {
                        sor = SystemOfRecordBroker.getSystemOfRecord(userData.getSource());
                    } catch (DomainException de) {
                        return new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                            "The specified source name was:  " + userData.getSource(),
                                            ServiceError.SOR_NOT_FOUND,
                                            ExceptionUtils.toXML(de));
                    }

                    // now persist the User and return success
                    UserFactory.persist(user, sor);

                    // post-import step. Provided the result was successful, update record info
                    sor.addRecordInfo(user, userData.getExternalID(), userData.getForeignId());


                    rval = FImportStatusResult.DEFAULT_SUCCESS;
                } // end else user == null
            }  // end else userData == null
        }
        catch (Throwable t) {
            rval = new FImportStatusResult(
                            FImportStatusResult.FAILED_EXCEPTION,
                                "EXCEPTION:  " + t.getMessage(),
                                ServiceError.UNRECOGNIZED_ERROR,
                                ExceptionUtils.toXML(t));
        }
        return rval;
    }

    /**
     *
     * @param userData
     * @return an array of status objects
     * @throws RemoteException
     */
    public FImportStatusResult[] updateUsers(UserData[] userData) throws AcademusException
    {
        // You would think we could batch a transaction here - but no!
        if (userData == null || userData.length == 0) {
            return new FImportStatusResult[0];
        }
        FImportStatusResult[] rval = new FImportStatusResult[userData.length];
        for (int i = 0; i < rval.length; i++) {
            rval[i] = updateUser(userData[i]);
        }
        return rval;
    }

    public FImportStatusResult importTopic(TopicData td) {

        // Assertions
        if (td == null) {
            return new FImportStatusResult(
                            FImportStatusResult.FAILED,
                            "Argument 'td [TopicData]' cannot be null.",
                            ServiceError.MISSING_INPUT,
                            null);
        }

        // Obtain the sor.
        ISystemOfRecord sor = null;
        try {
            sor = SystemOfRecordBroker.getSystemOfRecord(td.getSource());
        } catch (DomainException de) {
            return new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                "The specified source name was:  " + td.getSource(),
                                ServiceError.SOR_NOT_FOUND,
                                ExceptionUtils.toXML(de));
        }

        // Parent.
        IGroup parent = null;
        String[] ids = td.getParentIDs();
        if (ids.length > 0) {
            String sourceId = ids[0];
            long academusId = 0L;

            // Parent might be a topic, and might be a group.
            if (academusId == 0L) {
                try {
                    academusId = sor.getTopicId(sourceId);
                } catch (DomainException de) {
                    // Not here -- fall through...
                }
            }
            if (academusId == 0L) {
                try {
                    academusId = sor.getGroupId(sourceId);
                } catch (DomainException de) {
                    // Not here -- fall through...
                }
            }
            if (academusId == 0L) {
                return new FImportStatusResult(FImportStatusResult.FAILED,
                                    "The specified parent id was:  " + sourceId,
                                    ServiceError.TOPIC_PARENT_NOT_FOUND,
                                    null);
            }
            try {
                parent = GroupFactory.getGroup(academusId);
            } catch (Throwable t) {
                return new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                    "Academus failed in locating the specified "
                                                    + "parent group. The "
                                                    + "specified parent id was:  "
                                                    + sourceId,
                                    ServiceError.UNRECOGNIZED_ERROR,
                                    ExceptionUtils.toXML(t));
            }
        } else {
            try {
                parent = getExternalRootGroup();
            } catch (Throwable t) {
                return new FImportStatusResult(
                                FImportStatusResult.FAILED_EXCEPTION,
                                "See attached error.",
                                ServiceError.TOPIC_EXTERNAL_ROOT_NOT_FOUND,
                                ExceptionUtils.toXML(t));
            }
        }

        // Create the new topic.
        Topic newTopic = null;
        try {
            String name = generateGroupName(td.getShortName(), td.getExternalID());
            newTopic = TopicFactory.createTopic(name, td.getDescription(),
                                    TopicType.ACADEMICS, parent, null);
        } catch (Throwable t) {
            return new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                "Academus failed in creating the requested topic.",
                                ServiceError.UNRECOGNIZED_ERROR,
                                ExceptionUtils.toXML(t));
        }

        // Create an sor entry for the new topic.
        try {
            sor.addRecordInfo(newTopic, td.getGroupId());
        } catch (DomainException de) {
            return new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                "Unable to write system of record entry for "
                                                + "the specified topic:  "
                                                + td.getGroupId(),
                                ServiceError.UNRECOGNIZED_ERROR,
                                ExceptionUtils.toXML(de));
        }

        // Success!
        return FImportStatusResult.DEFAULT_SUCCESS;

    }

    public FImportStatusResult updateTopic(TopicData td) {

        // Assertions
        if (td == null) {
            return new FImportStatusResult(
                            FImportStatusResult.FAILED,
                            "Argument 'td [TopicData]' cannot be null.",
                            ServiceError.MISSING_INPUT,
                            null);
        }

        // Obtain the sor.
        ISystemOfRecord sor = null;
        try {
            sor = SystemOfRecordBroker.getSystemOfRecord(td.getSource());
        } catch (DomainException de) {
            return new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                "The specified source name was:  " + td.getSource(),
                                ServiceError.SOR_NOT_FOUND,
                                ExceptionUtils.toXML(de));
        }

        // Obtain the specified topic.
        Topic target = null;
        try {
            target = TopicFactory.getTopic(sor.getTopicId(td.getGroupId()));
        } catch (Throwable t) {
            return new FImportStatusResult(
                                FImportStatusResult.FAILED_EXCEPTION,
                                "Unable to locate the specified topic.  "
                                                    + "See attached error.",
                                ServiceError.TOPIC_NOT_FOUND,
                                ExceptionUtils.toXML(t));
        }

        // Make changes.
        try {
            // Name.
            String grpName = generateGroupName(td.getShortName(), td.getExternalID());
            if (!grpName.equals(target.getName())) {
                target.setName(grpName);
            }
            // Description.
            if (!td.getDescription().equals(target.getDescription())) {
                target.setDescription(td.getDescription());
            }
            // Parent.
            IGroup parent = null;
            String[] ids = td.getParentIDs();
            if (ids.length > 0) {
                String sourceId = ids[0];
                long academusId = 0L;

                // Parent might be a topic, and might be a group.
                if (academusId == 0L) {
                    try {
                        academusId = sor.getTopicId(sourceId);
                    } catch (DomainException de) {
                        // Not here -- fall through...
                    }
                }
                if (academusId == 0L) {
                    try {
                        academusId = sor.getGroupId(sourceId);
                    } catch (DomainException de) {
                        // Not here -- fall through...
                    }
                }
                if (academusId == 0L) {
                    // We've run out of options, we can't handle this...
                    return new FImportStatusResult(FImportStatusResult.FAILED,
                                        "The specified parent id was:  " + sourceId,
                                        ServiceError.TOPIC_PARENT_NOT_FOUND,
                                        null);
                }
                try {
                    parent = GroupFactory.getGroup(academusId);
                } catch (Throwable t) {
                    return new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                        "Academus failed in locating the specified "
                                                        + "parent group. The "
                                                        + "specified parent id was:  "
                                                        + sourceId,
                                        ServiceError.UNRECOGNIZED_ERROR,
                                        ExceptionUtils.toXML(t));
                }
            }
            if (parent == null) {
                parent = getExternalRootGroup();
            }
            if (parent != target.getParentGroup()) {
                target.setParentGroup(parent);
            }
            TopicFactory.persist(target, sor);
        } catch (Throwable t) {
            return new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                "Academus failed to make the requested changes.",
                                ServiceError.UNRECOGNIZED_ERROR,
                                ExceptionUtils.toXML(t));
        }

        // Success!
        return FImportStatusResult.DEFAULT_SUCCESS;

    }

    public FImportStatusResult deleteTopic(TopicData td) {

        // Assertions
        if (td == null) {
            return new FImportStatusResult(
                            FImportStatusResult.FAILED,
                            "Argument 'td [TopicData]' cannot be null.",
                            ServiceError.MISSING_INPUT,
                            null);
        }

        // Obtain the sor.
        ISystemOfRecord sor = null;
        try {
            sor = SystemOfRecordBroker.getSystemOfRecord(td.getSource());
        } catch (DomainException de) {
            return new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                "The specified source name was:  " + td.getSource(),
                                ServiceError.SOR_NOT_FOUND,
                                ExceptionUtils.toXML(de));
        }

        // Obtain the specified topic.
        Topic target = null;
        try {
            target = TopicFactory.getTopic(sor.getTopicId(td.getGroupId()));
        } catch (Throwable t) {
            return new FImportStatusResult(
                                FImportStatusResult.FAILED_EXCEPTION,
                                "Unable to locate the specified topic.  "
                                                    + "See attached error.",
                                ServiceError.TOPIC_NOT_FOUND,
                                ExceptionUtils.toXML(t));
        }

        // Remove the sor record.
        try {
            sor.deleteRecordInfo(target);
        } catch (DomainException de) {
            return new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                "Unable to remove system of record entry for "
                                                + "the specified topic:  "
                                                + td.getGroupId(),
                                ServiceError.UNRECOGNIZED_ERROR,
                                ExceptionUtils.toXML(de));
        }

        // Delete it.
        try {
            TopicFactory.deleteTopic(target.getId(), sor);
        } catch (Throwable t) {
            return new FImportStatusResult(FImportStatusResult.FAILED_EXCEPTION,
                                "Academus failed to delete the specified "
                                            + "topic.  See attached error.",
                                ServiceError.UNRECOGNIZED_ERROR,
                                ExceptionUtils.toXML(t));
        }

        // Success!
        return FImportStatusResult.DEFAULT_SUCCESS;

    }

    // Offering section

    /**
     * imports an offering into Academus
     *
     * @param offeringData array of OfferingData objects representing Offering to import
     * @return FImportStatusResult[] indicating whether imports were successful or not
     * @throws AcademusException only if the call itself fails, not if any specific import fails
     */
    public FImportStatusResult importOffering(OfferingData offeringData) throws AcademusException
    {
        FImportStatusResult result = null;

        // create the Offering
        // first we need a set of channels for the Offering

        // Enrollment Model is going to be a default via a property. See static block.
        String[] topicIDs = offeringData.getTopicIDs();
        Topic[] topics = new Topic[topicIDs.length];

        try {
            for (int i = 0; i < topics.length; i++ ) {
                topics[i] = TopicFactory.getTopic(Long.parseLong(topicIDs[i]));
            }
        }
        catch (Exception t) {
            __logService.log(ILogService.WARN, "Could not get parent topics in importOffering");
            // should return topic-specific problem here
            result = new FImportStatusResult(
                            FImportStatusResult.FAILED_EXCEPTION,
                            "See attached error.",
                            ServiceError.OFFERING_PARENT_NOT_FOUND,
                            ExceptionUtils.toXML(t));
        }
        EnrollmentModel eModel = null;

        try {
            eModel = EnrollmentModel.getInstance(offeringData.getEnrollmentModel());
        }
        catch (Exception t2) {
            __logService.log(ILogService.WARN, "Could not get enrollment model in importOffering");
            // should return enrollment-specific problem here
            result = new FImportStatusResult(
                            FImportStatusResult.FAILED_EXCEPTION,
                            "Enrollment model was:  " + offeringData.getEnrollmentModel(),
                            ServiceError.OFFERING_ENROLLMENT_MODEL_NOT_FOUND,
                            ExceptionUtils.toXML(t2));
        }

        Role defaultRole = null;
        try {
            defaultRole = RoleFactory.getDefaultRole(offeringData.getDefaultRole(), Role.OFFERING);
        }
        catch (Exception t3) {
            __logService.log(ILogService.WARN, "Could not get default role in importOffering");
            // should return role-specific problem here
            result = new FImportStatusResult(
                            FImportStatusResult.FAILED_EXCEPTION,
                            "The default role was:  " + offeringData.getDefaultRole(),
                            ServiceError.OFFERING_DEFAULT_ROLE_NOT_FOUND,
                            ExceptionUtils.toXML(t3));
        }
        User creator = null;

        try {
            String creatorUser = UniconPropertiesFactory.getManager(ImportServicePropertiesType.IMPORT).getProperty(
                                     "net.unicon.academus.service.ImportService.defaultUser");

            if (creatorUser == null || creatorUser.length() == 0) {
                creatorUser = "admin"; // what else can you do?
            }
            creator = UserFactory.getUser(creatorUser);
        }
        catch (Throwable t4) {
            __logService.log(ILogService.WARN, "Could not get creator User in importOffering");
            // should return user-specific problem here
            result = new FImportStatusResult(
                            FImportStatusResult.FAILED_EXCEPTION,
                            "n/t",
                            ServiceError.OFFERING_CREATOR_NOT_FOUND,
                            ExceptionUtils.toXML(t4));
        }

        // now do the offering if there have been no other problems
        Offering offering = null;
        String name = generateGroupName(offeringData.getShortName(), offeringData.getExternalID());

        if (result == null) {
            try {
              OptionalOfferingData oData = new OptionalOfferingData();
              boolean haveOptionalData = __dealWithOptionalOfferingData(oData, offeringData);
              if (haveOptionalData) {
                  offering = OfferingFactory.createOffering(name,
                                                            offeringData.getDescription(),
                                                            topics,
                                                            eModel,
                                                            defaultRole,
                                                            ChannelClassFactory.getChannelClasses(ChannelMode.OFFERING),
                                                            creator, //creator
                                                            null,    // optionalIdString
                                                            oData.term,
                                                            oData.startMonth, oData.startDay, oData.startYear,
                                                            oData.endMonth, oData.endDay, oData.endYear,
                                                            oData.daysOfWeek,
                                                            oData.meetingStartHour, oData.meetingStartMins,
                                                            oData.meetingStartAmPm, oData.meetingEndHour,
                                                            oData.meetingEndMins, oData.meetingEndAmPm,
                                                            oData.location);
              }
              else {
                 offering = OfferingFactory.createOffering(name,
                                                        offeringData.getDescription(),
                                                        topics,
                                                        eModel,
                                                        defaultRole,
                                                        ChannelClassFactory.getChannelClasses(ChannelMode.OFFERING),
                                                        creator); //creator
              }

              // create the offering calendar
              CalendarServiceFactory.getService().createCalendar(offering);
            }
            catch (Throwable t5) {
             __logService.log(ILogService.WARN, "Could not create Offering in importOffering");
             // should return user-specific problem here
             result = new FImportStatusResult(
                            FImportStatusResult.FAILED_EXCEPTION,
                            "EXCEPTION:  " + t5.getMessage(),
                            ServiceError.UNRECOGNIZED_ERROR,
                            ExceptionUtils.toXML(t5));
            }
        }

        // if the result has been set, there has been a problem so leave it. Otherwise,
        // set it to all good
        if (result == null) {
            result = new FImportStatusResult(FImportStatusResult.DEFAULT_SUCCESS,
                                             new Long(offering.getId()));
        }
        // return value
        return result;
    }

    /**
     * imports offerings into Academus
     * @param offeringData array of OfferingData objects representing Offering to import
     * @return FImportStatusResult[] indicating whether imports were successful or not
     * @throws AcademusException
     */
    public FImportStatusResult[] importOfferings(OfferingData[] offeringData) throws AcademusException
    {
        // You would think we could batch a transaction here - but no!
        if (offeringData == null || offeringData.length == 0) {
            return new FImportStatusResult[0];
        }
        FImportStatusResult[] rval = new FImportStatusResult[offeringData.length];
        for (int i = 0; i < rval.length; i++) {
            rval[i] = importOffering(offeringData[i]);
        }
        return rval;
    }

    /**
     * updates an existing offering according to the marshalled parameter object
     * @param offeringData
     * @return FImportStatusResult
     * @throws AcademusException
     */
    public FImportStatusResult updateOffering(OfferingData offeringData)
        throws AcademusException {
        FImportStatusResult result = null;
        try {
            // 1. Need to remove all topics and add all new ones - H2's method?
            // 2. Need to remap topic ids - in the remote service impl?
            // 3. Do we need to do the EnrollmentModel biz rule check?
            // 4. setEnrollmentModel and setDefaultRole require domain objs
            Offering offering = OfferingFactory.getOffering(Long.parseLong(offeringData.getOfferingID()));

            // Obtain the sor
            IEntityRecordInfo rec = SystemOfRecordBroker.getRecordInfo(offering);
            ISystemOfRecord sor = rec.getSystemOfRecord();

            String name = generateGroupName(offeringData.getShortName(), offeringData.getExternalID());
            offering.setName(name);
            offering.setDescription(offeringData.getDescription());

            // TOPICS
            // get all the Offering's topics and remove them
            Topic[] currentTopics = offering.getTopics();
            for (int j = 0; currentTopics != null && j < currentTopics.length; j++) {
                offering.removeTopic(currentTopics[j]);
            }

            // NB:  topic IDs are external IDs [attn:  KG]
            String[] topicIDs = offeringData.getTopicIDs();
            Topic topic = null;
            for (int i = 0; topicIDs != null && i < topicIDs.length; i++) {
                long tId = sor.getTopicId(topicIDs[i]);
                try {
                    topic = TopicFactory.getTopic(tId);
                }
                catch (Throwable t) {
                    result = new FImportStatusResult(
                                    FImportStatusResult.FAILED_EXCEPTION,
                                    "See attached error.",
                                    ServiceError.OFFERING_PARENT_NOT_FOUND,
                                    ExceptionUtils.toXML(t));
                }
                offering.addTopic(topic);
            }
            // end TOPICS

            if (result == null) {
                // Enrollment Model
                EnrollmentModel eModel = null;
                try {
                    eModel = EnrollmentModel.getInstance(offeringData.getEnrollmentModel());

                    // biz rule taken from OfferingAdminChannel - if the enrollment model was
                    // request/approve, and a Student was pending, don't allow change
                    EnrollmentModel previousEnrollmentModel = offering.getEnrollmentModel();
                    String prevEnrollmentModelString = previousEnrollmentModel.toString();
                    String requestApproveString = EnrollmentModel.REQUESTAPPROVE.toString();
                    if ((prevEnrollmentModelString.equals(requestApproveString)) &&
                        (! eModel.toString().equals(requestApproveString)) &&
                        (Memberships.hasMembership(offering,EnrollmentStatus.PENDING))) {
                            // need a specific error type here
                            result = FImportStatusResult.DEFAULT_FAILURE;
                    }
                    else {
                        offering.setEnrollmentModel(eModel);
                    }
                }
                catch (Throwable t2) {
                    __logService.log(ILogService.WARN, "Could not get enrollment model in updateOffering");
                    // should return enrollment-specific problem here
                    result = new FImportStatusResult(
                                    FImportStatusResult.FAILED_EXCEPTION,
                                    "See attached error.",
                                    ServiceError.OFFERING_ENROLLMENT_MODEL_NOT_FOUND,
                                    ExceptionUtils.toXML(t2));
                }
            }

            if (result == null) {
                // Default ROLE
                Role defaultRole = null;
                try {
                    defaultRole = RoleFactory.getDefaultRole(offeringData.getDefaultRole(), Role.OFFERING);
                    offering.setDefaultRole(defaultRole.getId());
                }
                catch (Throwable t2) {
                    __logService.log(ILogService.WARN, "Could not get default role in updateOffering");
                    // should return role-specific problem here
                    result = new FImportStatusResult(
                                    FImportStatusResult.FAILED_EXCEPTION,
                                    "See attached error.",
                                    ServiceError.DEFAULT_ROLE_NOT_FOUND,
                                    ExceptionUtils.toXML(t2));
                }
            }

            // optional data
            if (result == null) {
                try {
                    OptionalOfferingData oData = new OptionalOfferingData();
                    boolean haveOptionalData = __dealWithOptionalOfferingData(oData, offeringData);
                    if (haveOptionalData) {
                        if (oData.term != null) { offering.setOptionalTerm(oData.term); }
                        if (oData.startMonth != -1) { offering.setOptionalMonthStart(oData.startMonth); }
                        if (oData.startDay != -1) { offering.setOptionalDayStart(oData.startDay); }
                        if (oData.startYear != -1) { offering.setOptionalYearStart(oData.startYear); }
                        if (oData.endMonth != -1) { offering.setOptionalMonthEnd(oData.endMonth); }
                        if (oData.endDay != -1) { offering.setOptionalDayEnd(oData.endDay); }
                        if (oData.endYear != -1) { offering.setOptionalYearEnd(oData.endYear); }
                        if (oData.daysOfWeek != 0) { offering.setDaysOfWeek(oData.daysOfWeek); }
                        if (oData.meetingStartHour != -1) { offering.setOptionalHourStart(oData.meetingStartHour); }
                        if (oData.meetingStartMins != -1) { offering.setOptionalMinuteStart(oData.meetingStartMins); }
                        if (oData.meetingStartAmPm != 0) { offering.setOptionalAmPmStart(oData.meetingStartAmPm); }
                        if (oData.meetingEndHour != -1) { offering.setOptionalHourEnd(oData.meetingEndHour); }
                        if (oData.meetingEndMins != -1) { offering.setOptionalMinuteEnd(oData.meetingEndMins); }
                        if (oData.meetingEndAmPm != 0) { offering.setOptionalAmPmEnd(oData.meetingEndAmPm); }
                        if (oData.location != null) { offering.setOptionalLocation(oData.location); }
                    }
                }
                catch (Throwable t6) {
                    __logService.log(ILogService.WARN, "Could not set optional attributes in updateOffering");
                    result = new FImportStatusResult(
                                    FImportStatusResult.FAILED_EXCEPTION,
                                    "See attached error.",
                                    ServiceError.OFFERING_OPTIONAL_ATTRIBUTES_NOT_FOUND,
                                    ExceptionUtils.toXML(t6));

                }
            }

            // channels? MeetingData?
            if (result == null) {
                // if result is null we encountered no problems
                OfferingFactory.persist(offering, sor);

                // update the offering calendar
                CalendarServiceFactory.getService().updateCalendar(offering);
                result = FImportStatusResult.DEFAULT_SUCCESS;
            }

        }
        catch (Throwable t) {
            result = new FImportStatusResult(
                FImportStatusResult.FAILED_EXCEPTION,
                "See attached error.",
                ServiceError.UNRECOGNIZED_ERROR,
                ExceptionUtils.toXML(t));
        }
        return result;
    }

    /**
     * batch version of updateOffering
     * @param offeringData
     * @return FImportStatusResult array one per each array element in the input param
     * @throws AcademusException
     */
    public FImportStatusResult[] updateOfferings(OfferingData[] offeringData) throws AcademusException
    {
        // You would think we could batch a transaction here - but no!
        if (offeringData == null || offeringData.length == 0) {
            return new FImportStatusResult[0];
        }
        FImportStatusResult[] rval = new FImportStatusResult[offeringData.length];
        for (int i = 0; i < rval.length; i++) {
            rval[i] = updateOffering(offeringData[i]);
        }
        return rval;
    }

    /**
     * Inactivates an offering (marks as deleted but leaves data in the database)
     * @param offeringID
     * @return FImportStatusResult
     * @throws AcademusException
     */
    public FImportStatusResult inactivateOffering(OfferingData offeringData)
        throws AcademusException

    {
        String offeringID = offeringData.getOfferingID();
        FImportStatusResult result = null;
        try {
            Offering o = OfferingFactory.getOffering(Long.parseLong(offeringID));

            // Obtain the sor
            IEntityRecordInfo rec = SystemOfRecordBroker.getRecordInfo(o);
            ISystemOfRecord sor = rec.getSystemOfRecord();

            // We must be sure this isn't an SOR violation.
            /*
            IEntityRecordInfo rec = SystemOfRecordBroker.getRecordInfo(o);
            if (rec.getSystemOfRecord().getEntityAccessLevel().compareTo(AccessType.DELETE) < 0) {
                // this import process is not allowed to inactivate this offering
                result = FImportStatusResult.DEFAULT_FAILURE;
            }
            else {
            */
                o.setStatus(Offering.INACTIVE);
                OfferingFactory.persist(o, sor);
                result = new FImportStatusResult(FImportStatusResult.DEFAULT_SUCCESS,
                                                 offeringID);
            //}
        }
        catch (Throwable t) {
            __logService.log(ILogService.ERROR,
                             "Cannot inactivate Offering " + offeringID);
            result = new FImportStatusResult(
                FImportStatusResult.FAILED_EXCEPTION,
                "EXCEPTION:  " + t.getMessage(),
                ServiceError.UNRECOGNIZED_ERROR,
                ExceptionUtils.toXML(t));
        }

        return result;
    }

    /**
     * batch version of inactivateOffering
     * @param offeringID
     * @return FImportStatusResult array one per each array element in the input param
     * @throws AcademusException
     */
    public FImportStatusResult[] inactivateOfferings(OfferingData[] offeringData) throws AcademusException
    {
        // You would think we could batch a transaction here - but no!
        if (offeringData == null || offeringData.length == 0) {
            return new FImportStatusResult[0];
        }
        FImportStatusResult[] rval = new FImportStatusResult[offeringData.length];
        for (int i = 0; i < rval.length; i++) {
            rval[i] = inactivateOffering(offeringData[i]);
        }
        return rval;
    }

    public FImportStatusResult importMembership(MemberData m) {

        // Assertions
        if (m == null) {
            return new FImportStatusResult(
                            FImportStatusResult.FAILED,
                            "Argument 'm [MemberData]' cannot be null.",
                            ServiceError.MISSING_INPUT,
                            null);
        }

        // Obtain the sor.
        ISystemOfRecord sor = null;
        try {
            sor = SystemOfRecordBroker.getSystemOfRecord(m.getSource());
        } catch (DomainException de) {
            return new FImportStatusResult(
                FImportStatusResult.FAILED_EXCEPTION,
                "The specified source name was:  " + m.getSource(),
                ServiceError.SOR_NOT_FOUND,
                ExceptionUtils.toXML(de));
        }

        // Obtain the user.
        User u = null;
        try {
            long uId = sor.getUserId(m.getPersonIdRef());
            u = UserFactory.getUser(uId);
        } catch (Throwable t) {
            return new FImportStatusResult(
                FImportStatusResult.FAILED_EXCEPTION,
                "The user id was:  " + m.getPersonIdRef(),
                ServiceError.MEMBERSHIP_USER_NOT_FOUND,
                ExceptionUtils.toXML(t));
        }

        // Determine if membership is with an offering.
        boolean isOffering = false;
        try {
            isOffering = sor.hasOffering(m.getGroupIdRef());
        } catch (DomainException de) {
            return new FImportStatusResult(
                FImportStatusResult.FAILED_EXCEPTION,
                "Unable to determine membership type.",
                ServiceError.UNRECOGNIZED_ERROR,
                ExceptionUtils.toXML(de));
        }

        if (!isOffering) { // Add group membership.

            // Obtain the parent group.
            IGroup parentGroup = null;
            try {
                long parentId = sor.getGroupId(m.getGroupIdRef());
                parentGroup   = GroupFactory.getGroup(parentId);
            } catch (Throwable t) {
                return new FImportStatusResult(
                    FImportStatusResult.FAILED_EXCEPTION,
                    "The group id was:  " + m.getGroupIdRef(),
                    ServiceError.MEMBERSHIP_GROUP_NOT_FOUND,
                    ExceptionUtils.toXML(t));
            }

            // Add the membership.
            try {
                IMember groupMember = u.getGroupMember();
                parentGroup.addMember(groupMember);
            } catch (Throwable t) {
                return new FImportStatusResult(
                    FImportStatusResult.FAILED_EXCEPTION,
                    "Unable to register the requested membership.",
                    ServiceError.UNRECOGNIZED_ERROR,
                    ExceptionUtils.toXML(t));
            }

        } else { // Add offering membership.

            // Obtain the offering.
            Offering o = null;
            try {
                long oId = sor.getOfferingId(m.getGroupIdRef());
                o = OfferingFactory.getOffering(oId);
            } catch (Throwable t) {
                return new FImportStatusResult(
                    FImportStatusResult.FAILED_EXCEPTION,
                    "The offering id was:  " + m.getGroupIdRef(),
                    ServiceError.MEMBERSHIP_OFFERING_NOT_FOUND,
                    ExceptionUtils.toXML(t));
            }

            // Obtain the role.
            Role r = null;
            try {
                Iterator it = RoleFactory.getOfferingRoles(o).iterator();
                 while (it.hasNext()) {
                     Role l = (Role) it.next();
                     if (l.getLabel().equals(m.getGroupRole())) {
                         r = l;
                         break;
                     }
                 }
            } catch (Throwable t) {
                return new FImportStatusResult(
                    FImportStatusResult.FAILED_EXCEPTION,
                    "The role was:  " + m.getGroupRole(),
                    ServiceError.MEMBERSHIP_ROLE_NOT_FOUND,
                    ExceptionUtils.toXML(t));
            }

            // Obtain the enrollment status.
            EnrollmentStatus s = null;
            try {
                s = EnrollmentStatus.getInstance(m.getStatus());
            } catch (ItemNotFoundException infe) {
                return new FImportStatusResult(
                    FImportStatusResult.FAILED_EXCEPTION,
                    "The enrollment status was:  " + m.getStatus(),
                    ServiceError.MEMBERSHIP_ENROLLMENT_STATUS_NOT_FOUND,
                    ExceptionUtils.toXML(infe));
            }

            // Add the membership.
            try {
                Memberships.add(u, o, r, s);

                // Share the user with the offering's calendar
                CalendarServiceFactory.getService().addUser(o, u);
            } catch (Throwable t) {
                return new FImportStatusResult(
                    FImportStatusResult.FAILED_EXCEPTION,
                    "Unable to register the requested membership.",
                    ServiceError.UNRECOGNIZED_ERROR,
                    ExceptionUtils.toXML(t));
            }

        } // end if hasOffering

        // Success!
        return FImportStatusResult.DEFAULT_SUCCESS;

    }

    public FImportStatusResult updateMembership(MemberData m) {

        // Assertions
        if (m == null) {
            return new FImportStatusResult(
                            FImportStatusResult.FAILED,
                            "Argument 'm [MemberData]' cannot be null.",
                            ServiceError.MISSING_INPUT,
                            null);
        }

        // Obtain the sor.
        ISystemOfRecord sor = null;
        try {
            sor = SystemOfRecordBroker.getSystemOfRecord(m.getSource());
        } catch (DomainException de) {
            return new FImportStatusResult(
                FImportStatusResult.FAILED_EXCEPTION,
                "The specified source name was:  " + m.getSource(),
                ServiceError.SOR_NOT_FOUND,
                ExceptionUtils.toXML(de));
        }

        // Obtain the user.
        User u = null;
        try {
            long uId = sor.getUserId(m.getPersonIdRef());
            u = UserFactory.getUser(uId);
        } catch (Throwable t) {
            return new FImportStatusResult(
                FImportStatusResult.FAILED_EXCEPTION,
                "The user id was:  " + m.getPersonIdRef(),
                ServiceError.MEMBERSHIP_USER_NOT_FOUND,
                ExceptionUtils.toXML(t));
        }

        // Obtain the offering.
        Offering o = null;
        try {
            long oId = sor.getOfferingId(m.getGroupIdRef());
            o = OfferingFactory.getOffering(oId);
        } catch (Throwable t) {
            return new FImportStatusResult(
                FImportStatusResult.FAILED_EXCEPTION,
                "The offering id was:  " + m.getGroupIdRef(),
                ServiceError.MEMBERSHIP_OFFERING_NOT_FOUND,
                ExceptionUtils.toXML(t));
        }

        // Obtain the role.
        Role r = null;
        try {
            Iterator it = RoleFactory.getOfferingRoles(o).iterator();
             while (it.hasNext()) {
                 Role l = (Role) it.next();
                 if (l.getLabel().equals(m.getGroupRole())) {
                     r = l;
                     break;
                 }
             }
        } catch (Throwable t) {
            return new FImportStatusResult(
                FImportStatusResult.FAILED_EXCEPTION,
                "The role was:  " + m.getGroupRole(),
                ServiceError.MEMBERSHIP_ROLE_NOT_FOUND,
                ExceptionUtils.toXML(t));
        }
        if (r == null) {
            return new FImportStatusResult(
                FImportStatusResult.FAILED,
                "The role was:  " + m.getGroupRole(),
                ServiceError.MEMBERSHIP_ROLE_NOT_FOUND,
                null);
        }

        // Obtain the enrollment status.
        EnrollmentStatus s = null;
        try {
            s = EnrollmentStatus.getInstance(m.getStatus());
        } catch (ItemNotFoundException infe) {
            return new FImportStatusResult(
                FImportStatusResult.FAILED_EXCEPTION,
                "The enrollment status was:  " + m.getStatus(),
                ServiceError.MEMBERSHIP_ENROLLMENT_STATUS_NOT_FOUND,
                ExceptionUtils.toXML(infe));
        }

        // Set the role.
        try {
            Memberships.changeRole(u, o, r);
        } catch (Throwable t) {
            return new FImportStatusResult(
                FImportStatusResult.FAILED_EXCEPTION,
                "Unable to change the user's role.",
                ServiceError.UNRECOGNIZED_ERROR,
                ExceptionUtils.toXML(t));
        }

        // Set the enrollment status.
        try {
            if (s != Memberships.getEnrollmentStatus(u, o)) {
                /*
                    NB:  It's obnoxious that we have to remove/add
                    to change status.
                */
                Memberships.remove(u, o);
                Memberships.add(u, o, r, s);
            }
        } catch (Throwable t) {
            return new FImportStatusResult(
                FImportStatusResult.FAILED_EXCEPTION,
                "Unable to change the user's enrollment status.",
                ServiceError.UNRECOGNIZED_ERROR,
                ExceptionUtils.toXML(t));
        }

        // Success!
        return FImportStatusResult.DEFAULT_SUCCESS;

    }

    public FImportStatusResult deleteMembership(MemberData m) {

        // Assertions
        if (m == null) {
            return new FImportStatusResult(
                            FImportStatusResult.FAILED_EXCEPTION,
                            "Argument 'm [MemberData]' cannot be null.",
                            ServiceError.MISSING_INPUT,
                            null);
        }

        // Obtain the sor.
        ISystemOfRecord sor = null;
        try {
            sor = SystemOfRecordBroker.getSystemOfRecord(m.getSource());
        } catch (DomainException de) {
            return new FImportStatusResult(
                FImportStatusResult.FAILED_EXCEPTION,
                "The specified source name was:  " + m.getSource(),
                ServiceError.SOR_NOT_FOUND,
                ExceptionUtils.toXML(de));
        }

        // Obtain the user.
        User u = null;
        try {
            long uId = sor.getUserId(m.getPersonIdRef());
            u = UserFactory.getUser(uId);
        } catch (Throwable t) {
            return new FImportStatusResult(
                FImportStatusResult.FAILED_EXCEPTION,
                "The user id was:  " + m.getPersonIdRef(),
                ServiceError.MEMBERSHIP_USER_NOT_FOUND,
                ExceptionUtils.toXML(t));
        }

        // Determine if membership is with an offering.
        boolean isOffering = false;
        try {
            isOffering = sor.hasOffering(m.getGroupIdRef());
        } catch (DomainException de) {
            return new FImportStatusResult(
                FImportStatusResult.FAILED_EXCEPTION,
                "Unable to determine membership type.",
                ServiceError.UNRECOGNIZED_ERROR,
                ExceptionUtils.toXML(de));
        }

        if (!isOffering) { // Remove group membership.

            // Obtain the parent group.
            IGroup parentGroup = null;
            try {
                long parentId = sor.getGroupId(m.getGroupIdRef());
                parentGroup   = GroupFactory.getGroup(parentId);
            } catch (Throwable t) {
                return new FImportStatusResult(
                    FImportStatusResult.FAILED_EXCEPTION,
                    "The group id was:  " + m.getGroupIdRef(),
                    ServiceError.MEMBERSHIP_GROUP_NOT_FOUND,
                    ExceptionUtils.toXML(t));
            }

            // Remove the membership.
            try {
                IMember groupMember = u.getGroupMember();
                parentGroup.removeMember(groupMember);
            } catch (Throwable t) {
                return new FImportStatusResult(
                    FImportStatusResult.FAILED_EXCEPTION,
                    "Unable to delete the specified membership.",
                    ServiceError.UNRECOGNIZED_ERROR,
                    ExceptionUtils.toXML(t));
            }

        } else { // Remove offering membership.

            // Obtain the offering.
            Offering o = null;
            try {
                long oId = sor.getOfferingId(m.getGroupIdRef());
                o = OfferingFactory.getOffering(oId);
            } catch (Throwable t) {
                return new FImportStatusResult(
                    FImportStatusResult.FAILED_EXCEPTION,
                    "The offering id was:  " + m.getGroupIdRef(),
                    ServiceError.MEMBERSHIP_OFFERING_NOT_FOUND,
                    ExceptionUtils.toXML(t));
            }

            // Remove the membership, if necessary.
            try {
                if (Memberships.isEnrolled(u, o)) {
                    Memberships.remove(u, o);

                    // remove the user from the offering's calendar
                    CalendarServiceFactory.getService().removeUser(o, u);
                }
            } catch (Throwable t) {
                return new FImportStatusResult(
                    FImportStatusResult.FAILED_EXCEPTION,
                    "Unable to delete the specified membership.",
                    ServiceError.UNRECOGNIZED_ERROR,
                    ExceptionUtils.toXML(t));
            }

        } // end if hasOffering

        // Success!
        return FImportStatusResult.DEFAULT_SUCCESS;

    }

    private static IGroup getExternalRootGroup()
    throws Exception
    {
        if (externalRootGroup != null)
        {
            return externalRootGroup;
        }
        else
        {
            UniconGroupService gs =
                UniconGroupServiceFactory.getService();

            // Set the parent group.
            IGroup rootGroup = gs.getRootGroup();

            try {
                String groupPath =
                    UniconPropertiesFactory.getManager(
                        PortalPropertiesType.PORTAL).getProperty(
                        "net.unicon.portal.groups.defaultExternalGroupPath");

                if (groupPath.trim().length() > 0
                        && groupPath.indexOf(",") == -1) {

                    String[] path = new String[1];
                    path[0] = groupPath.trim();

                    externalRootGroup = gs.getGroupByPath(path);

                } else {

                    StringTokenizer st =
                        new StringTokenizer(groupPath, ",");
                    String[] path = new String[st.countTokens()+1];

                    int i = 0;
                    while (st.hasMoreTokens()) {
                        path[i++] = st.nextToken();
                    }

                    externalRootGroup = gs.getGroupByPath(path);

                } // end if

            } catch (Exception e) {
                // Default to the root (Everyone) group.
                externalRootGroup = rootGroup;
            }
        } // end if

        return externalRootGroup;
    } // end getExternalRootGroup

    /**
     * imports Group information.
     * @param groupData
     * @return FImportStatusResult indicating whether the group has been successfully added.
     * A false indicates the Group already exists in the system or is not persistable.
     * @throws AcademusException
     */
    public FImportStatusResult importGroup(GroupData groupData)
    throws AcademusException {

        FImportStatusResult rval = null;
        try {
            if (groupData == null) {  // should never happen
                rval = FImportStatusResult.FAILED_BAD_DATA;
            } else {
                // Get the System of Record.
                String groupSource  = groupData.getSource();
                ISystemOfRecord sor =
                    SystemOfRecordBroker.getSystemOfRecord(groupSource);

                try {
                    sor.getGroupId(groupData.getExternalID());
                    // We shouldn't get here...
                    String msg = "The group id was:  " + groupData.getExternalID();
                    return new FImportStatusResult(
                            FImportStatusResult.FAILED_EXCEPTION,
                            msg,
                            ServiceError.GROUP_ALREADY_EXISTS,
                            null);
                } catch (DomainException de) {
                    // Fall through...this is what we want (Hi KG).
                }

                /* Create and persist the group. */
                IGroup defaultParentGroup = null;

                // If an external key is invalid, an exception will be thrown.
                List parentKeys = getExternalParentKeys(groupData);

                // If there is no parents, then set as member of default.
                if (!(parentKeys != null && parentKeys.size() > 0)) {
                    defaultParentGroup = getExternalRootGroup();
                }

                UniconGroupService gs =
                    UniconGroupServiceFactory.getService();

                String name = generateGroupName(groupData.getShortName(), groupData.getExternalID());
                IGroup group = gs.createGroup(
                                    null,
                                    defaultParentGroup,
                                    name,
                                    groupData.getDescription());

                if (group != null) {
                    // Add group as member of existing parent groups.
                    this.addGroupRelationships(group, parentKeys);
                    // Add information to the client key table.
                    sor.addRecordInfo(group, groupData.getExternalID());

                    // Set successful result.
                    rval = FImportStatusResult.DEFAULT_SUCCESS;
                } else {
                    StringBuffer msg = new StringBuffer("");
                    msg.append("Error importing group with ");
                    msg.append("external id:  ");
                    msg.append(groupData.getExternalID());
                    throw new Exception(msg.toString());
                }

            }
        } catch (Throwable t) {
            // Unable to import, return failed with exception.
            rval = new FImportStatusResult(
                        FImportStatusResult.FAILED_EXCEPTION,
                        "EXCEPTION:  " + t.getMessage(),
                        ServiceError.UNRECOGNIZED_ERROR,
                        ExceptionUtils.toXML(t));
        }

        return rval;
    } // end importGroup

    /**
     * batch import of groups
     * @param groupData
     * @return FImportStatusResult[] one per each transaction.
     * @throws AcademusException
     */
    public FImportStatusResult[] importGroups(GroupData[] groupData)
    throws AcademusException {

        if (groupData == null || groupData.length == 0) {
            return new FImportStatusResult[0];
        }

        FImportStatusResult[] rval = new FImportStatusResult[groupData.length];
        for (int i = 0; i < rval.length; i++) {
            rval[i] = importGroup(groupData[i]);
        }

        return rval;
    } // end importGroups

    /**
     * Updates Group information.
     * @param groupData
     * @return FImportStatusResult indicating whether the group has been successfully updated.
     * A false indicates the Group doesn't already exists in the system or is not persistable.
     * @throws AcademusException
     */
    public FImportStatusResult updateGroup(GroupData groupData)
    throws AcademusException {

        FImportStatusResult rval = null;
        try {
            if (groupData == null) {  // should never happen
                rval = FImportStatusResult.FAILED_BAD_DATA;
            } else {
                // Get the System of Record.
                String groupSource = groupData.getSource();
                ISystemOfRecord sor =
                    SystemOfRecordBroker.getSystemOfRecord(groupSource);

                // Get group for update.
                IGroup group = null;

                try {
                    long groupId = sor.getGroupId(groupData.getExternalID());
                    String groupKey = localGroupService + groupId;
                    group = GroupFactory.getGroup(groupKey);
                } catch (DomainException de) {}

                if (group == null)  {

                    String msg = "The group id was:  " + groupData.getExternalID();
                    return new FImportStatusResult(
                                FImportStatusResult.FAILED_EXCEPTION,
                                msg,
                                ServiceError.GROUP_NOT_FOUND,
                                null);

                } else {

                    // If an external key is invalid,
                    // an exception will be thrown.
                    List parentKeys = getExternalParentKeys(groupData);

                    // Now update the group.
                    String name = generateGroupName(groupData.getShortName(), groupData.getExternalID());
                    group.setName(name);
                    group.setDescription(groupData.getDescription());
                    group.update();

                    // Now update the group's relationships.
                    this.updateGroupRelationships(group, parentKeys);

                    // Set successful result.
                    rval = FImportStatusResult.DEFAULT_SUCCESS;

                }
            }
        } catch (Throwable t) {
            // Unable to update, return failed with exception.
            rval = new FImportStatusResult(
                        FImportStatusResult.FAILED_EXCEPTION,
                        "EXCEPTION:  " + t.getMessage(),
                        ServiceError.UNRECOGNIZED_ERROR,
                        ExceptionUtils.toXML(t));
        }

        return rval;
    } // end updateGroup

    /**
     * batch update of groups
     * @param groupData
     * @return FImportStatusResult[] one per each transaction.
     * @throws AcademusException
     */
    public FImportStatusResult[] updateGroups(GroupData[] groupData)
    throws AcademusException {

        if (groupData == null || groupData.length == 0) {
            return new FImportStatusResult[0];
        }

        FImportStatusResult[] rval = new FImportStatusResult[groupData.length];
        for (int i = 0; i < rval.length; i++) {
            rval[i] = updateGroup(groupData[i]);
        }

        return rval;
    } // end updateGroups

    /**
     * Deletes Group information.
     * @param groupData
     * @return FImportStatusResult indicating whether the group has been successfully deleted.
     * A false indicates the Group doesn't already exists in the system or cannot be deleted.
     * @throws AcademusException
     */
    public FImportStatusResult deleteGroup(GroupData groupData)
    throws AcademusException {

        FImportStatusResult rval = null;
        try {
            if (groupData == null) {  // should never happen
                rval = FImportStatusResult.FAILED_BAD_DATA;
            } else {
                // Get the System of Record.
                String groupSource = groupData.getSource();
                ISystemOfRecord sor =
                    SystemOfRecordBroker.getSystemOfRecord(groupSource);

                // Get group for delete.
                IGroup group = null;

                try {
                    long groupId = sor.getGroupId(groupData.getExternalID());
                    String groupKey = localGroupService + groupId;
                    group = GroupFactory.getGroup(groupKey);
                } catch (DomainException de) {}

                if (group == null)  {

                    String msg = "The group id was:  " + groupData.getExternalID();
                    return new FImportStatusResult(
                                FImportStatusResult.FAILED_EXCEPTION,
                                msg,
                                ServiceError.GROUP_NOT_FOUND,
                                null);

                } else {

                    // Now delete record from the client key table.
                    sor.deleteRecordInfo(group);

                    // Now delete the group.
                    group.delete();

                    // Set successful result.
                    rval = FImportStatusResult.DEFAULT_SUCCESS;

                }
            }
        } catch (Throwable t) {
            // Unable to delete, return failed with exception.
            rval = new FImportStatusResult(
                        FImportStatusResult.FAILED_EXCEPTION,
                        "EXCEPTION:  " + t.getMessage(),
                        ServiceError.UNRECOGNIZED_ERROR,
                        ExceptionUtils.toXML(t));
        }

        return rval;
    } // end deleteGroup

    /**
     * batch delete of groups
     * @param groupData
     * @return FImportStatusResult[] one per each transaction.
     * @throws AcademusException
     */
    public FImportStatusResult[] deleteGroups(GroupData[] groupData)
    throws AcademusException {

        if (groupData == null || groupData.length == 0) {
            return new FImportStatusResult[0];
        }

        FImportStatusResult[] rval = new FImportStatusResult[groupData.length];
        for (int i = 0; i < rval.length; i++) {
            rval[i] = deleteGroup(groupData[i]);
        }

        return rval;
    } // end deleteGroups

    private List getExternalParentKeys(GroupData groupData) throws Exception {
        // If groupData does not have parent IDs, then return.
        String[] xParentIDs = groupData.getParentIDs();
        if (xParentIDs == null) return null;

        List xParentKeys  = new ArrayList();
        String xParentKey = null;
        String xID        = null;
        long xParentID;

        // Get the System of Record.
        String source = groupData.getSource();
        ISystemOfRecord sor =
            SystemOfRecordBroker.getSystemOfRecord(source);

        for (int i = 0; i < xParentIDs.length; i++)
        {
            try {
                xID = xParentIDs[i];
                // If parent does not exist, exeption will be thrown.
                xParentID   = sor.getGroupId(xID);
                xParentKey  = localGroupService + xParentID;
                xParentKeys.add(xParentKey);
            }
            catch (DomainException de)
            {
                StringBuffer msg = new StringBuffer("");
                msg.append("Parent group with external id of ");
                msg.append(xID).append(" does not exist!");
                throw new Exception(msg.toString(), de);
            }
        } // end for loop of parentIDs

        return xParentKeys;
    } // end getExternalParentKeys

    private void addGroupRelationships(IGroup group, List xParentKeys)
    throws Exception {

        // If xParentKeys is null, then return.
        if (xParentKeys == null) return;

        String xParentKey  = null;
        IGroup parentGroup = null;

        for (int i = 0; i < xParentKeys.size(); i++)
        {
            try {
                xParentKey   = (String)xParentKeys.get(i);
                parentGroup = GroupFactory.getGroup(xParentKey);

                // Attach group to its parent.
                parentGroup.addGroup(group);
                parentGroup.update();
            }
            catch (Exception e)
            {
                StringBuffer msg = new StringBuffer("");
                msg.append("Unable to add group with key of ");
                msg.append(group.getKey()).append(". ");
                msg.append("Parent group with key of ").append(xParentKey);
                msg.append(" does not exist!");
                throw new Exception(msg.toString(), e);
            }
        } // end for loop of xParentKeys

    } // end addGroupRelationships

    private void updateGroupRelationships(IGroup group, List xParentKeys)
    throws Exception {

        // If xParentKeys is null, then return.
        if (xParentKeys == null) return;
        String xParentKey = null;

        // Current parents.
        List parentKeys = group.getParentKeys();

        // Invalid parents.
        IGroup invalidParentGroup = null;
        String invalidParentKey   = null;
        List keysForRemove        = new ArrayList();
        keysForRemove.addAll(parentKeys);

        // New parents.
        IGroup newParentGroup = null;

        // Add new relationships.
        for (int i = 0; i < xParentKeys.size(); i++)
        {
            xParentKey = (String)xParentKeys.get(i);

            if (!parentKeys.contains(xParentKey))
            { // if the parent relationship doesn't exist, then add it.
                try {
                    newParentGroup = GroupFactory.getGroup(xParentKey);
                    newParentGroup.addGroup(group);
                    newParentGroup.update();
                }
                catch (Exception e)
                {
                    StringBuffer msg = new StringBuffer("");
                    msg.append("Unable to add group with key of ");
                    msg.append(group.getKey()).append(". ");
                    msg.append("Parent group with key of ").append(xParentKey);
                    msg.append(" does not exist!");
                    throw new Exception(msg.toString(), e);
                }
            }
            else
            { // otherwise assemble the list of relationships to remove.
                keysForRemove.remove(xParentKey);
            }

        } // end for loop of xParentIDs

        // Now remove invalid relationships.
        for (int i = 0; i < keysForRemove.size(); i++)
        {
            invalidParentKey = (String)keysForRemove.get(i);
            try {
                invalidParentGroup = GroupFactory.getGroup(invalidParentKey);

                // Remove relationship if parent not external root group.
                if (!getExternalRootGroup().getKey().equals(
                        invalidParentGroup.getKey()) )
                {
                    invalidParentGroup.removeGroup(group);
                    invalidParentGroup.update();
                }
            }
            catch (Exception e)
            {
                StringBuffer msg = new StringBuffer("");
                msg.append("Unable to remove group member with key of ");
                msg.append(group.getKey()).append(" from parent group ");
                msg.append("with key of ").append(invalidParentKey);
                throw new Exception(msg.toString(), e);
            }
        } // end for loop of keysForRemove

    } // end updateGroupRelationships

    private boolean __dealWithOptionalOfferingData(OptionalOfferingData oData,
                                                   OfferingData offeringData)
        throws Throwable
    {
        boolean haveOptionalData = false;
        try {
            MeetingData[] mData = offeringData.getMeetingData();
            TimeframeData tData = offeringData.getTimeData();

            // even though MeetingData is an array, do not see how we can handle > 1,
            // so only deal with the first, if it exists
            if (mData != null && mData.length > 0) {
                haveOptionalData = true;
                // from MeetingData we get location, days of week, meeting start/end times
                // should move this off to some helper class
                String meetingDays = mData[0].getDays();
                String nextDay     = null;
                StringTokenizer st = new StringTokenizer(meetingDays, ",");
                while (st.hasMoreTokens()) {
                    // NOTE: There is no specification of what can appear in the XML,
                    // nor do examples exist, so these represent my best guess --KG
                    nextDay = st.nextToken().trim();
                    if (nextDay.equalsIgnoreCase("M")) {
                        oData.daysOfWeek |= 2;
                    }
                    else if (nextDay.equalsIgnoreCase("T")) {
                        oData.daysOfWeek |= 3;
                    }
                    else if (nextDay.equalsIgnoreCase("W")) {
                        oData.daysOfWeek |= 4;
                    }
                    else if (nextDay.equalsIgnoreCase("Th")) {
                        oData.daysOfWeek |= 5;
                    }
                    else if (nextDay.equalsIgnoreCase("F")) {
                        oData.daysOfWeek |= 6;
                    }
                    else if (nextDay.equalsIgnoreCase("S")) {
                        oData.daysOfWeek |= 7;
                    }
                    else if (nextDay.equalsIgnoreCase("Su")) {
                        oData.daysOfWeek |= 1;
                    }
                }
                oData.location = mData[0].getLocation();
                // meeting start/end times
                Timestamp meetingStartTS = mData[0].getStartDate();
                Timestamp meetingEndTS   = mData[0].getEndDate();

                if (meetingStartTS != null) {
                    oData.meetingStartHour = meetingStartTS.getHours();
                    oData.meetingStartAmPm = oData.meetingStartHour / 12 + 1;  // 1 == AM, 2 == PM
                    if (oData.meetingStartHour == 0) {
                        oData.meetingStartHour = 12;
                    }
                    else {
                        oData.meetingStartHour %= 12;
                    }
                    oData.meetingStartMins = meetingStartTS.getMinutes();
                }
                if (meetingEndTS != null) {
                    oData.meetingEndHour   = meetingEndTS.getHours();
                    oData.meetingEndAmPm = oData.meetingEndHour / 12 + 1;  // 1 == AM, 2 == PM
                    if (oData.meetingEndHour == 0) {
                        oData.meetingEndHour = 12;
                    }
                    else {
                        oData.meetingEndHour %= 12;
                    }
                    oData.meetingEndMins   = meetingEndTS.getMinutes();
                }

                // we can get recurrence too, but nowhere to put on an Offering
            }
            if (tData != null) {
                haveOptionalData = true;
                // TimeframeData includes term, start/end dates
                oData.term = tData.getTerm();
                Timestamp startTS = tData.getStartDate();
                Timestamp endTS   = tData.getEndDate();

                if (startTS != null) {
                    // these methods are deprecated on a Timestamp, but there
                    // really isn't another convenient way...
                    oData.startMonth = startTS.getMonth() + 1;      // See below...
                    oData.startYear  = startTS.getYear() + 1900;
                    oData.startDay   = startTS.getDate();
                }
                if (endTS != null) {
                    // these methods are deprecated on a Timestamp, but there
                    // really isn't another convenient way...
                    oData.endMonth = endTS.getMonth() + 1;          // See below...
                    oData.endYear  = endTS.getYear() + 1900;
                    oData.endDay   = endTS.getDate();
                }

                // NB:  java.util.Date numbers months from 0-11,
                // whereas the academus domain expects 1-12.

            }
        } // end try
        catch (Throwable t5) {
            __logService.log(ILogService.WARN, "Could not get handle optional offering data in private method");
            // rethrow, we're a private method
            throw t5;
        }
        return haveOptionalData;
    }

    private static String generateGroupName(String xName, String xId) {

        // Assertions.
        if (xName == null) {
            String msg = "Argument 'xName' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (xId == null) {
            String msg = "Argument 'xId' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (xId.length() > 60) {
            String msg = "Argument 'xId' may not be longer than 60 characters.";
            throw new IllegalArgumentException(msg);
        }

        // NB:  This method creates a unique group name from the external name
        // and the external Id of a group-type entity (Offering, Topic, &c.).
        // The resultant name will be 60 characters in length or shorter.

        StringBuffer rslt = new StringBuffer();

        // Handle the xId first.
        rslt.append("[").append(xId).append("]");

        // Now handle the xName, maintaining the maximum length.
        if (rslt.length() + xName.length() < 80) {  // simple case...
            rslt.insert(0, " ");
            rslt.insert(0, xName);
        } else {                                    // do some work...
            // Name max = total - (xId part + elipses part)...
            int maxNameLen = 80 - (rslt.length() + 4);
            rslt.insert(0, "... ").insert(0, xName.substring(0, maxNameLen));
        }

        return rslt.toString();

    }

    private class OptionalOfferingData {
        // now deal with Meeting data
        boolean haveOptionalData = false;
        String location      = null;
        String term          = null;
        int daysOfWeek       = 0;
        int startMonth       = 0;
        int startYear        = 0;
        int startDay         = 0;
        int endMonth         = 0;
        int endYear          = 0;
        int endDay           = 0;
        int meetingStartHour = 0;   // NB:  Academus can only
        int meetingStartMins = 0;   // handle optional offering
        int meetingStartAmPm = 0;   // data if it's all there.
        int meetingEndAmPm   = 0;   // I've therefore changed
        int meetingEndHour   = 0;   // these fields to the same
        int meetingEndMins   = 0;   // defaults as OfferingAdmin .
                                    // - drew
    }

} // end FAcademusDefaultImportService class
