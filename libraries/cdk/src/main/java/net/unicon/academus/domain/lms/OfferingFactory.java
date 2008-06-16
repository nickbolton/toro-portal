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
package net.unicon.academus.domain.lms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.unicon.academus.cache.DomainCacheRequestHandler;
import net.unicon.academus.cache.DomainCacheRequestHandlerFactory;
import net.unicon.academus.common.AcademusException;
import net.unicon.academus.common.properties.AcademusPropertiesType;
import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.academus.domain.sor.AccessType;
import net.unicon.academus.domain.sor.IEntityRecordInfo;
import net.unicon.academus.domain.sor.ISystemOfRecord;
import net.unicon.academus.domain.sor.SorViolationException;
import net.unicon.academus.domain.sor.SystemOfRecordBroker;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.domain.ChannelClassFactory;
import net.unicon.portal.groups.GroupFactory;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.UniconGroupService;
import net.unicon.portal.groups.UniconGroupServiceFactory;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.permissions.PermissionsException;
import net.unicon.portal.permissions.PermissionsService;
import net.unicon.portal.util.db.AcademusDBUtil;
import net.unicon.sdk.properties.UniconPropertiesFactory;

/**
 * Provides static methods to create and access offerings.
 * <code>OfferingFactory</code> is not instantiable.
 */
public final class OfferingFactory {

    private static Map cache = new HashMap();
    private static final boolean isCaching = UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getPropertyAsBoolean("net.unicon.academus.domain.lms.isCaching");
    private final static String NAVIGATION_CHANNEL = "NavigationChannel";
    private static final boolean isMultiBox = UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getPropertyAsBoolean("net.unicon.portal.Academus.multipleBoxConfig");
    private static DomainCacheRequestHandler handler = null;

    public static final int MONDAY    = 2;
    public static final int TUESDAY   = 4;
    public static final int WEDNESDAY = 8;
    public static final int THURSDAY  = 16;
    public static final int FRIDAY    = 32;
    public static final int SATURDAY  = 64;
    public static final int SUNDAY    = 128;

    static {
        if (isMultiBox) {
            try {
                handler = DomainCacheRequestHandlerFactory.getHandler();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private OfferingFactory() {}

    /**
     * Creates a new offering with the specified name, within the specified
     * topic, and which uses the specified enrollment model. <code>createOffering</code> will throw an
     * <code>IllegalArgumentException</code> if any of the following is true: <ul>
     * <li>the name is zero-length or contains only whitespace</li>
     * <li>the description is zero-length or contains only whitespace</li>
     * <li>an offering with the specified name already exists within the specified topic</li>
     * <li>the specified topic is null</li> <li>the specified enrollment model is null</li> </ul>
     * @param name the label to be displayed for the new offering.
     * @param description the description to be displayed for the new offering.
     * @param parentTopic the topic under which the new offering should exist.
     * @param enrollModel the method of enrollment to be used for this offering.
     * @return a newly created offering.
     * @throws IllegalArgumentException if a new offering cannot be created with the specified parameters.
     */
    public static Offering createOffering(String name, String description,
    Topic parentTopic, EnrollmentModel enrollModel, Role defaultRole,
    List channels, User creator)
    throws IllegalArgumentException,
    OperationFailedException,
    ItemNotFoundException {
        return createOffering(name, description, new Topic[] {parentTopic},
                              enrollModel, defaultRole, channels, creator);
    }

    /**
     * Overloaded version provides for an array of parent Topics
     * @param name
     * @param description
     * @param parentTopic
     * @param enrollModel
     * @param defaultRole
     * @param channels
     * @param creator
     * @return
     * @throws IllegalArgumentException
     * @throws OperationFailedException
     * @throws ItemNotFoundException
     */
    public static Offering createOffering(String name, String description,
    Topic[] parentTopic, EnrollmentModel enrollModel, Role defaultRole,
    List channels, User creator)
    throws IllegalArgumentException,
    OperationFailedException,
    ItemNotFoundException {
        // Assertions.
        if (name == null) {
            String msg = "The offering name can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (description == null) {
            String msg = "The offering description can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (parentTopic == null) {
            String msg = "The parent topic can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (enrollModel == null) {
            String msg = "The enrollment model can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (defaultRole == null) {
            String msg = "The default role can't be null.";
            throw new IllegalArgumentException(msg);
        }
        // The rtn & stuff to build it.
        Offering rtn = null;
        long Id = 0;
        IGroup group = null;

        // SQL -- should return zero rows.
        StringBuffer sql = new StringBuffer();
        sql.append("insert into offering (group_id, name, description, ");
        sql.append("enrollment_model, default_role_id, status) ");
        sql.append("values (?,?,?,?,?,?)");

        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // create the associated group object
            UniconGroupService gs = UniconGroupServiceFactory.getService();

            group = gs.createGroup(creator, parentTopic[0].getGroup(), // XXX
                Offering.groupPrefix+name, description);

            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setLong(i++, group.getGroupId());
            pstmt.setString(i++, name);
            pstmt.setString(i++, description);
            // we no longer have a topic_id in the offering table, it is now a join table
            pstmt.setString(i++, enrollModel.toString());
            pstmt.setLong(i++, defaultRole.getId());
            pstmt.setInt(i++, Offering.ACTIVE);
            pstmt.executeUpdate();
        } catch (Throwable t) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation createOffering failed ");
            msg.append("with the following message:\n");
            msg.append(t.getMessage());
            throw new OperationFailedException(msg.toString(), t);
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            conn = null;
        }
        sql = new StringBuffer();
        //sql.append("select O.offering_id from offering O, topic_offering OT where O.name = ? and OT.topic_id = ? and OT.offering_id = O.offering_id");
        sql.append("select O.offering_id from offering O where O.name = ?");
        try {
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setString(i++, name);
            rs = pstmt.executeQuery();
            if (!rs.next()) {
                StringBuffer msg = new StringBuffer();
                msg.append("Could not find offering: (" + name + ")");
                throw new ItemNotFoundException(msg.toString());
            }
            Id = rs.getLong(1);

            // Clean up...
            rs.close();
            rs = null;
            pstmt.close();
            pstmt = null;

            __persistTopics(parentTopic, Id, conn);
        } catch (Throwable t) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation createOffering failed ");
            msg.append("with the following message:\n");
            msg.append(t.getMessage());
            throw new OperationFailedException(msg.toString(), t);
        } finally {
            try {
                if (rs != null) rs.close();
                rs = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            conn = null;
        }
        if (Id <= 0) {
            StringBuffer msg = new StringBuffer();
            msg.append("Could not find offering: (" + name + ")");
            throw new ItemNotFoundException(msg.toString());
        }

        // Create and cache the offering & return.
        rtn = new Offering(Id, group, name, description, Offering.ACTIVE,
                           enrollModel, parentTopic, defaultRole.getId(), channels);
        if (isCaching) {
            cache.put(new Long(Id), rtn);

            if (handler != null) {
                try {
                    handler.broadcastRefreshOffering((int) Id);
                } catch (AcademusException ae) {
                    ae.printStackTrace();
                }
            }
        }
        return rtn;
    }
    //*********************************************************************
    // Modified for Requirements OA 4.1-4.12
    /**
     * Creates a new offering with the specified name, within the specified
     * topic, and which uses the specified enrollment model. <code>createOffering</code> will throw an
     * <code>IllegalArgumentException</code> if any of the following is true: <ul>
     * <li>the name is zero-length or contains only whitespace</li>
     * <li>the description is zero-length or contains only whitespace</li>
     * <li>an offering with the specified name already exists within the specified topic</li>
     * <li>the specified topic is null</li> <li>the specified enrollment model is null</li> </ul>
     * @param name the label to be displayed for the new offering.
     * @param description the description to be displayed for the new offering.
     * @param parentTopic the topic under which the new offering should exist.
     * @param enrollModel the method of enrollment to be used for this offering.
     * @param optionalIdString the optional offering identification id
     * @param optionalTermString the optional offering term
     * @param optionalMonthStartInt the optional offering starting month
     * @param optionalDayStartInt the optional offering starting day
     * @param optionalYearStartInt the optional offering starting year
     * @param optionalMonthEndInt the optional offering ending month
     * @param optionalDayEndInt the optional offering ending day
     * @param optionalYearEndInt the optional offering ending year
     * @param daysOfWeek the integer (bit mask) of what days the class meets.
     * @param optionalHourStartInt the optional offering starting hour
     * @param optionalMinuteStartInt the optional offering starting minute
     * @param optionalAmPmStartInt the optional offering starting time flag 1 means AM, 2 means PM
     * @param optionalHourEndInt the optional offering ending hour
     * @param optionalMinuteEndInt the optional offering ending minute
     * @param optionalAmPmEndInt the optional offering ending time flag 1 means AM, 2 means PM
     * @param optionalLocationString the optional offering location
     * @return a newly created offering.
     * @throws IllegalArgumentException if a new offering cannot be created with the specified parameters.
     */
    public static Offering createOffering(String name,
                      String description,
                      Topic parentTopic,
                      EnrollmentModel enrollModel,
                      Role defaultRole,
                      List channels,
                      User creator,
                      String optionalIdString,
                      String optionalTermString,
                      int optionalMonthStartInt,
                      int optionalDayStartInt,
                      int optionalYearStartInt,
                      int optionalMonthEndInt,
                      int optionalDayEndInt,
                      int optionalYearEndInt,
                      int daysOfWeek,
                      int optionalHourStartInt,
                      int optionalMinuteStartInt,
                      int optionalAmPmStartInt,
                      int optionalHourEndInt,
                      int optionalMinuteEndInt,
                      int optionalAmPmEndInt,
                      String optionalLocationString)
    throws IllegalArgumentException,
           OperationFailedException,
           ItemNotFoundException {
               return createOffering(name, description, new Topic[] {parentTopic}, enrollModel,
                                     defaultRole, channels, creator, optionalIdString,
                                     optionalTermString, optionalMonthStartInt, optionalDayStartInt,
                                     optionalYearStartInt, optionalMonthEndInt, optionalDayEndInt,
                                     optionalYearEndInt, daysOfWeek, optionalHourStartInt,
                                     optionalMinuteStartInt, optionalAmPmStartInt, optionalHourEndInt,
                                     optionalMinuteEndInt, optionalAmPmEndInt, optionalLocationString);
     }

    /**
     * Overloaded version takes an array of parent Topics for cross-listing
     * @param name
     * @param description
     * @param parentTopic
     * @param enrollModel
     * @param defaultRole
     * @param channels
     * @param creator
     * @param optionalIdString
     * @param optionalTermString
     * @param optionalMonthStartInt
     * @param optionalDayStartInt
     * @param optionalYearStartInt
     * @param optionalMonthEndInt
     * @param optionalDayEndInt
     * @param optionalYearEndInt
     * @param daysOfWeek
     * @param optionalHourStartInt
     * @param optionalMinuteStartInt
     * @param optionalAmPmStartInt
     * @param optionalHourEndInt
     * @param optionalMinuteEndInt
     * @param optionalAmPmEndInt
     * @param optionalLocationString
     * @return
     * @throws IllegalArgumentException
     * @throws OperationFailedException
     * @throws ItemNotFoundException
     */
    public static Offering createOffering(String name,
                      String description,
                      Topic[] parentTopic,
                      EnrollmentModel enrollModel,
                      Role defaultRole,
                      List channels,
                      User creator,
                      String optionalIdString,
                      String optionalTermString,
                      int optionalMonthStartInt,
                      int optionalDayStartInt,
                      int optionalYearStartInt,
                      int optionalMonthEndInt,
                      int optionalDayEndInt,
                      int optionalYearEndInt,
                      int daysOfWeek,
                      int optionalHourStartInt,
                      int optionalMinuteStartInt,
                      int optionalAmPmStartInt,
                      int optionalHourEndInt,
                      int optionalMinuteEndInt,
                      int optionalAmPmEndInt,
                      String optionalLocationString)
    throws IllegalArgumentException,
           OperationFailedException,
           ItemNotFoundException {

        // Assertions.
        if (name == null) {
            String msg = "The offering name can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (description == null) {
            String msg = "The offering description can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (parentTopic == null) {
            String msg = "The parent topic can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (enrollModel == null) {
            String msg = "The enrollment model can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (defaultRole == null) {
            String msg = "The default role can't be null.";
            throw new IllegalArgumentException(msg);
        }
        // The rtn & stuff to build it.
        Offering rtn = null;
        long Id = 0;
        IGroup group = null;

        // SQL -- should return zero rows.
        StringBuffer sql = new StringBuffer();
        sql.append(
//            "insert into offering (group_id, name, description, topic_id, ");
            "insert into offering (group_id, name, description, ");
        sql.append("enrollment_model, default_role_id, status, ");
        sql.append("opt_offeringId, opt_offeringTerm, ");
        sql.append("opt_offeringMonthStart, opt_offeringDayStart, ");
        sql.append("opt_offeringYearStart, opt_offeringMonthEnd, ");
        sql.append("opt_offeringDayEnd, opt_offeringYearEnd, ");
        sql.append("opt_daysOfWeek, ");
        sql.append("opt_offeringHourStart, ");
        sql.append("opt_offeringMinuteStart, ");
        sql.append("opt_offeringAmPmStart, ");
        sql.append("opt_offeringHourEnd, ");
        sql.append("opt_offeringMinuteEnd, ");
        sql.append("opt_offeringAmPmEnd, opt_offeringLocation) ");
        //sql.append("values (?,?,?,?,?,?,?,");
        sql.append("values (?,?,?,?,?,?,");
        sql.append("?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // create the associated group object
            UniconGroupService gs = UniconGroupServiceFactory.getService();
            /* XXX KG don't know what to do here - do we insert a new offering row
             * per group? Leaving commented out for now
             */
            //for (int i = 0; i < parentTopic.length; i++) {
            //    group = gs.createGroup(creator, parentTopic[i].getGroup(),
            //            Offering.groupPrefix+name, description);
            //}
            group = gs.createGroup(creator, parentTopic[0].getGroup(),
                                   Offering.groupPrefix+name, description);

            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setLong(i++, group.getGroupId());
            pstmt.setString(i++, name);
            pstmt.setString(i++, description);
            //pstmt.setLong(i++, parentTopic.getId());
            pstmt.setString(i++, enrollModel.toString());
            pstmt.setLong(i++, defaultRole.getId());
            pstmt.setInt(i++, Offering.ACTIVE);
            pstmt.setString(i++, optionalIdString);
            pstmt.setString(i++, optionalTermString);
            pstmt.setInt(i++, optionalMonthStartInt);
            pstmt.setInt(i++, optionalDayStartInt);
            pstmt.setInt(i++, optionalYearStartInt);
            pstmt.setInt(i++, optionalMonthEndInt);
            pstmt.setInt(i++, optionalDayEndInt);
            pstmt.setInt(i++, optionalYearEndInt);
            pstmt.setInt(i++, daysOfWeek);
            pstmt.setInt(i++, optionalHourStartInt);
            pstmt.setInt(i++, optionalMinuteStartInt);
            pstmt.setInt(i++, optionalAmPmStartInt);
            pstmt.setInt(i++, optionalHourEndInt);
            pstmt.setInt(i++, optionalMinuteEndInt);
            pstmt.setInt(i++, optionalAmPmEndInt);
            pstmt.setString(i++, optionalLocationString);
            pstmt.executeUpdate();

            // Clean up...
            pstmt.close();
            pstmt = null;

            // Fetch the id...
            sql = new StringBuffer();
            sql.append("select max(O.offering_id) from offering O ");
            sql.append("where O.name = ?");

            pstmt = conn.prepareStatement(sql.toString());
            i = 1;
            pstmt.setString(i++, name);
            rs = pstmt.executeQuery();
            if (!rs.next()) {
                StringBuffer msg = new StringBuffer();
                msg.append("Could not find offering: (" + name + ")");
                throw new ItemNotFoundException(msg.toString());
            }
            Id = rs.getLong(1);

            // Clean up...
            rs.close();
            rs = null;
            pstmt.close();
            pstmt = null;

            __persistTopics(parentTopic, Id, conn);
        } catch (Throwable t) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation createOffering failed ");
            msg.append("with the following message:\n");
            msg.append(t.getMessage());
            throw new OperationFailedException(msg.toString(), t);
        } finally {
            try {
                if (rs != null) rs.close();
                rs = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            conn = null;
        }
        if (Id <= 0) {
            StringBuffer msg = new StringBuffer();
            msg.append("Could not find offering: (" + name + ")");
            throw new ItemNotFoundException(msg.toString());
        }

        // Create and cache the offering & return.
        rtn = new Offering(Id, group, name, description, Offering.ACTIVE,
        enrollModel, parentTopic, defaultRole.getId(), channels);
        rtn.setOptionalId(optionalIdString);
        rtn.setOptionalTerm(optionalTermString);
        rtn.setOptionalMonthStart(optionalMonthStartInt);
        rtn.setOptionalDayStart(optionalDayStartInt);
        rtn.setOptionalYearStart(optionalYearStartInt);
        rtn.setOptionalMonthEnd(optionalMonthEndInt);
        rtn.setOptionalDayEnd(optionalDayEndInt);
        rtn.setOptionalYearEnd(optionalYearEndInt);
        rtn.setOptionalHourStart(optionalHourStartInt);
        rtn.setOptionalMinuteStart(optionalMinuteStartInt);
        rtn.setOptionalAmPmStart(optionalAmPmStartInt);
        rtn.setOptionalHourEnd(optionalHourEndInt);
        rtn.setOptionalMinuteEnd(optionalMinuteEndInt);
        rtn.setOptionalAmPmEnd(optionalAmPmEndInt);
        rtn.setOptionalLocation(optionalLocationString);
        rtn.setDaysOfWeek(daysOfWeek);
        if (isCaching) {
            cache.put(new Long(Id), rtn);

            if (handler != null) {
                try {
                    handler.broadcastRefreshOffering((int) Id);
                } catch (AcademusException ae) {
                    ae.printStackTrace();
                }
            }
        }
        return rtn;
    }
    //*********************************************************************
    public static List getNavigationOfferings(User user)
    throws IllegalArgumentException, OperationFailedException,
    ItemNotFoundException {
        List offerings = null;

        try {
            if (user.isSuperUser()) {
                offerings = getOfferings();
            } else {
                ChannelClass navCC = ChannelClassFactory.getChannelClass(
                NAVIGATION_CHANNEL);
                offerings = Memberships.getOfferings(user, Offering.ACTIVE);
                // Now add the inactive offerings, if they have the permissions
                Iterator itr = Memberships.getOfferings(
                user, Offering.INACTIVE).iterator();
                while (itr.hasNext()) {
                    Offering offering = (Offering)itr.next();
                    Role offRole = Memberships.getRole(user, offering);
                    IPermissions p =
                        PermissionsService.instance().getPermissions(
                            navCC, offRole.getGroup());
                    boolean canViewInactiveOfferings =
                        p.canDo(user.getUsername(), "viewInactiveOfferings");
                    if (p != null && canViewInactiveOfferings) {
                        offerings.add(offering);
                    }
                }
            }
        } catch (PermissionsException pe) {
            throw new OperationFailedException(pe);
        } catch (DomainException de) {
            throw new OperationFailedException(de);
        }
        return offerings;
    }
    /**
     * Provides the offering associated with the specified Id.
     * @param offeringId the unique identifier for the desired offering.
     * @return an offering in the portal system.
     * @throws ItemNotFoundException if there is no corresponding offering in the portal system.
     */
    public static Offering getOffering(long offeringId)
    throws ItemNotFoundException, OperationFailedException {
        if (isCaching) {
            synchronized (cache) {
                return _getOffering(offeringId);
            }
        }
        return _getOffering(offeringId);
    }
    /**
     * Method which obtains an offering object for the passed-in offering Id.
     * @param offeringId which corresponds to an existing offering ID value.
     * @throws ItemNotFoundException if there is not an offering with the specified offering ID.
     * @throws OperationFailedException if the corresponding offering object could not be found in the database
     */
    private static Offering _getOffering(long offeringId)
    throws ItemNotFoundException, OperationFailedException {
        Offering rtn = null;
        if (isCaching) {
            rtn = (Offering) cache.get(new Long(offeringId));
            if (rtn != null) return rtn;
        }
        rtn = getOfferingFromDB(offeringId);
        if (isCaching) {
            cache.put(new Long(offeringId), rtn);
        }
        return rtn;
    }

    /**
     * Method which obtains an offering object for the passed-in offering Id
     * @param IGroup which correspons to an existing offering ID value.
     * @throws ItemNotFoundException if there is not an offering with the specified offering ID.
     * @throws OperationFailedException if the corresponding offering object could not be found in the database
     */
     public static Offering getOffering(IGroup group)
     throws ItemNotFoundException, OperationFailedException {
         Offering rtn = null;
         if (group != null) {
             StringBuffer sql = new StringBuffer();
             sql.append("select offering_id from offering where group_id = ? ");

             // db objects.
             Connection conn = null;
             PreparedStatement pstmt = null;
             ResultSet rs = null;

             try {
                 conn = AcademusDBUtil.getDBConnection();
                 pstmt = conn.prepareStatement(sql.toString());

                 pstmt.setLong(1, group.getGroupId());
                 rs = pstmt.executeQuery();

                 if (rs.next() ) {
                     rtn = getOffering(rs.getLong("offering_id"));
                 }
             } catch (Exception e) {
                 StringBuffer msg = new StringBuffer();
                 msg.append("Operation getOffering by group failed ");
                 msg.append("with the following message:\n");
                 msg.append(e.getMessage());
                throw new OperationFailedException(msg.toString());
             } finally {
                 try {
                     if (rs != null) rs.close();
                     rs = null;
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 try {
                     if (pstmt != null) pstmt.close();
                     pstmt = null;
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 AcademusDBUtil.safeReleaseDBConnection(conn);
                 conn = null;
             }
         } else {
             throw new OperationFailedException (
                 "Passed in parameter for group is null or not available");
         }

         return rtn;
     }

    /**
     * Method which obtains an offering object for the passed-in offering Id.
     * @param offeringId which corresponds to an existing offering ID value.
     * @return Offering - an offering
     * @throws ItemNotFoundException if there is not an offering with the specified offering ID.
     * @throws OperationFailedException if the corresponding offering object could not be found in the database
     */
    private static Offering getOfferingFromDB(long offeringId)
    throws ItemNotFoundException, OperationFailedException {
        Offering rtn = null;
        // STEP 1:  get the channel classes.
        List channels = new ArrayList();
        ChannelClass cnl = null;
        // SQL -- the set of channel instances.
        StringBuffer sql = new StringBuffer();
        sql.append("Select Channel_Instance_Id, Channel_Handle ");
        sql.append("from Channel_Instance where Offering_Id = ?");
        // db objects.
        Connection conn         = null;
        PreparedStatement pstmt = null;
        ResultSet rs            = null;

        // STEP 2:  Get the name, description, enrollmentModel, Topic,
        //          defaultRole, offering status, and all of the optional
        //          offering attributes for the associated offering.
        IGroup group           = null;
        String name            = null;
        String description     = null;
        EnrollmentModel eModel = null;
        Topic topic            = null;
        long defaultRoleId     = -1;
        int status = 0;
        String optionalOfferingIdString       = null;
        String optionalOfferingTermString     = null;
        int optionalOfferingMonthStartInt     = 0;
        int optionalOfferingDayStartInt       = 0;
        int optionalOfferingYearStartInt      = 0;
        int optionalOfferingMonthEndInt       = 0;
        int optionalOfferingDayEndInt         = 0;
        int optionalOfferingYearEndInt        = 0;
        int daysOfWeek                        = 0;
        int optionalOfferingHourStartInt      = 0;
        int optionalOfferingMinuteStartInt    = 0;
        int optionalOfferingAmPmStartInt      = 0;
        int optionalOfferingHourEndInt        = 0;
        int optionalOfferingMinuteEndInt      = 0;
        int optionalOfferingAmPmEndInt        = 0;
        String optionalOfferingLocationString = null;
        //************************************************************
        // SQL
        sql = new StringBuffer();
        sql.append("Select O.group_id, O.Name, O.description, O.Enrollment_model, OT.topic_id, ");
        //*************************************************************
        // Changed for Requirements OA 4.1-4.12
        // from:
        // sql.append("default_role_id, status ");
        // to
        sql.append("O.default_role_id, O.status, O.opt_offeringId, ");
        sql.append("O.opt_offeringTerm, O.opt_offeringMonthStart, ");
        sql.append("O.opt_offeringDayStart, O.opt_offeringYearStart, ");
        sql.append("O.opt_offeringMonthEnd, O.opt_offeringDayEnd, ");
        sql.append("O.opt_offeringYearEnd, ");
        sql.append("O.opt_daysOfWeek, ");
        sql.append("O.opt_offeringHourStart, ");
        sql.append("O.opt_offeringMinuteStart, ");
        sql.append("O.opt_offeringAmPmStart, ");
        sql.append("O.opt_offeringHourEnd, ");
        sql.append("O.opt_offeringMinuteEnd, ");
        sql.append("O.opt_offeringAmPmEnd, ");
        sql.append("O.opt_offeringLocation ");
        //*************************************************************
        sql.append("from OFFERING O, TOPIC_OFFERING OT where O.offering_Id = ? ");
        sql.append("and O.offering_Id = OT.offering_id");
        try {
            // Look for the offering with the passed-in offering ID.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, offeringId);
            rs = pstmt.executeQuery();
            if (!rs.next()) {
                StringBuffer msg = new StringBuffer();
                msg.append("Could not find offering with offeringId=");
                msg.append(offeringId + "");
                throw new ItemNotFoundException(msg.toString());
            }
            group = GroupFactory.getGroup(rs.getLong("group_id"));
            name = rs.getString("Name");
            description = rs.getString("description");
            eModel = EnrollmentModel.getInstance(
            rs.getString("Enrollment_Model"));
            topic = TopicFactory.getTopic(rs.getLong("topic_id"));
            defaultRoleId = rs.getLong("default_role_id");
            status = rs.getInt("status");
            //**********************************************************
            // Added for Requirements OA 4.1-4.12
            optionalOfferingIdString = rs.getString("opt_offeringId");
            optionalOfferingTermString = rs.getString("opt_offeringTerm");

            optionalOfferingMonthStartInt = rs.getInt("opt_offeringMonthStart");
            optionalOfferingDayStartInt   = rs.getInt("opt_offeringDayStart");
            optionalOfferingYearStartInt  = rs.getInt("opt_offeringYearStart");

            optionalOfferingMonthEndInt =  rs.getInt("opt_offeringMonthEnd");
            optionalOfferingDayEndInt   =  rs.getInt("opt_offeringDayEnd");
            optionalOfferingYearEndInt  =  rs.getInt("opt_offeringYearEnd");

            daysOfWeek =  rs.getInt("opt_daysOfWeek");
            optionalOfferingHourStartInt =
            rs.getInt("opt_offeringHourStart");
            optionalOfferingMinuteStartInt =
            rs.getInt("opt_offeringMinuteStart");
            optionalOfferingAmPmStartInt =
            rs.getInt("opt_offeringAmPmStart");
            optionalOfferingHourEndInt =
            rs.getInt("opt_offeringHourEnd");
            optionalOfferingMinuteEndInt =
            rs.getInt("opt_offeringMinuteEnd");
            optionalOfferingAmPmEndInt =
            rs.getInt("opt_offeringAmPmEnd");
            optionalOfferingLocationString =
            rs.getString("opt_offeringLocation");
            //*************************************************************
        } catch (ItemNotFoundException infe) {
            throw infe;
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getOffering(offeringId) failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            try {
                if (rs != null) rs.close();
                rs = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            AcademusDBUtil.safeReleaseDBConnection(conn);
            conn = null;
        }
        //*******************************************************
        // Added all optionalOffering... parameters to this call
        // for implementation of Requirements OA 4.1-4.12
        // Create and cache the offering & return.
        rtn = new Offering(offeringId, group, name, description, status,
        eModel, topic, defaultRoleId, channels);

        rtn.setOptionalId(optionalOfferingIdString);
        rtn.setOptionalTerm(optionalOfferingTermString);
        rtn.setDaysOfWeek(daysOfWeek);
        rtn.setOptionalLocation(optionalOfferingLocationString);

        try {
            rtn.setOptionalMonthStart(optionalOfferingMonthStartInt);
            rtn.setOptionalDayStart(optionalOfferingDayStartInt);
            rtn.setOptionalYearStart(optionalOfferingYearStartInt);
        } catch (IllegalArgumentException iae) {
            StringBuffer msg = new StringBuffer();
            msg.append("IllegalArguement from the DB, ignoring");
            msg.append("setting Optional start date\n");
            msg.append(iae.getMessage());
        }

        try {
            rtn.setOptionalMonthEnd(optionalOfferingMonthEndInt);
            rtn.setOptionalDayEnd(optionalOfferingDayEndInt);
            rtn.setOptionalYearEnd(optionalOfferingYearEndInt);
        } catch (IllegalArgumentException iae) {
            StringBuffer msg = new StringBuffer();
            msg.append("IllegalArguement from the DB, ignoring");
            msg.append("setting Optional end date\n");
            msg.append(iae.getMessage());
        }

        try {
            rtn.setOptionalHourStart(optionalOfferingHourStartInt);
            rtn.setOptionalMinuteStart(optionalOfferingMinuteStartInt);
            rtn.setOptionalAmPmStart(optionalOfferingAmPmStartInt);
        } catch (IllegalArgumentException iae) {
            StringBuffer msg = new StringBuffer();
            msg.append("IllegalArguement from the DB, ignoring");
            msg.append("setting Optional start time\n");
            msg.append(iae.getMessage());
        }

        try {
            rtn.setOptionalHourEnd(optionalOfferingHourEndInt);
            rtn.setOptionalMinuteEnd(optionalOfferingMinuteEndInt);
            rtn.setOptionalAmPmEnd(optionalOfferingAmPmEndInt);
        } catch (IllegalArgumentException iae) {
            StringBuffer msg = new StringBuffer();
            msg.append("IllegalArguement from the DB, ignoring");
            msg.append("setting Optional End time\n");
            msg.append(iae.getMessage());
        }


        return rtn;
    }
    /**
     * Provides the set of offerings contained within the specified topic.
     * @param parentTopic a topic within the portal system.
     * @return all the offerings for the specified topic.
     * @throws IllegalArgumentException if the specified topic is null.
     */
    public static List getOfferings(Topic parentTopic)
    throws IllegalArgumentException,
    OperationFailedException {
        if (parentTopic == null) {
            String msg = "The parent topic can't be null.";
            throw new IllegalArgumentException(msg);
        }
        // The return.
        List rtn = new ArrayList();
        // SQL -- the set of offerings.
        StringBuffer sql = new StringBuffer();
        sql.append("Select offering_id from TOPIC_OFFERING where topic_Id = ?");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List offIds = new ArrayList();
        try {
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, parentTopic.getId());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                offIds.add(new Long(rs.getLong("offering_id")));
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getOfferings(parentTopic) failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            try {
                if (rs != null) rs.close();
                rs = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            conn = null;
        }
        // Loop & get the offerings.
        Iterator it = offIds.iterator();
        long offId = 0;
        try {
            while (it.hasNext()) {
                offId = ((Long)it.next()).longValue();
                rtn.add(getOffering(offId));
            }
        } catch (ItemNotFoundException infe) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getOfferings(parentTopic) failed ");
            msg.append("while looking up offeringId=");
            msg.append(offId + "");
            throw new OperationFailedException(msg.toString());
        }
        return rtn;
    }
    /**
     * Provides the set of all offerings for a given Offering status.
     * @return all the offerings for the specified topic.
     * @throws IllegalArgumentException if the specified topic is null.
     */
    public static List getOfferings(Topic parentTopic, int status)
    throws IllegalArgumentException, OperationFailedException {
        List offerings = new ArrayList();
        Iterator itr = getOfferings(parentTopic).iterator();
        while (itr != null && itr.hasNext()) {
            Offering offering = (Offering)itr.next();
            if (offering.getStatus() == status) {
                offerings.add(offering);
            }
        }
        return offerings;
    }
    /**
     * Provides the set of all offerings for a given Offering status.
     * @return all the offerings for the specified topic.
     * @throws IllegalArgumentException if the specified topic is null.
     */
    public static List getOfferings(int status)
    throws IllegalArgumentException, OperationFailedException {
        List offerings = new ArrayList();
        Iterator itr = getOfferings().iterator();
        while (itr != null && itr.hasNext()) {
            Offering offering = (Offering)itr.next();
            if (offering.getStatus() == status) {
                offerings.add(offering);
            }
        }
        return offerings;
    }
    public static List getOfferings(Topic topic,
    EnrollmentModel enrollmentModel)
    throws IllegalArgumentException, OperationFailedException {
        List offerings = new ArrayList();
        Iterator itr = getOfferings().iterator();
        while (itr != null && itr.hasNext()) {
            Offering offering = (Offering)itr.next();
            if (offering.getTopic().getId() == topic.getId() &&
            offering.getEnrollmentModel() == enrollmentModel) {
                offerings.add(offering);
            }
        }
        return offerings;
    }
    public static List getOfferings(Topic topic,
    String offeringName,
    String offeringDesc,
    EnrollmentModel enrollmentModel)
    throws IllegalArgumentException, OperationFailedException {
        List offerings = new ArrayList();
        Iterator itr = getOfferings().iterator();
        while (itr != null && itr.hasNext()) {
            Offering offering = (Offering)itr.next();
            if ( ((topic == null) || (offering.getTopic().getId() == topic.getId())) &&
            (offering.getName().toLowerCase().indexOf(offeringName.toLowerCase()) != -1)  &&
            (offering.getDescription().toLowerCase().indexOf(offeringDesc.toLowerCase()) != -1)  &&
            ((enrollmentModel == null) || offering.getEnrollmentModel() == enrollmentModel)) {
                offerings.add(offering);
            }
        }
        return offerings;
    }
    /**
     * Provides the set of all offerings.
     * @return all the offerings for the specified topic.
     * @throws IllegalArgumentException if the specified topic is null.
     */
    public static List getOfferings()
    throws IllegalArgumentException, OperationFailedException {
        // The return.
        List rtn = new ArrayList();
        // SQL -- the set of offerings.
        StringBuffer sql = new StringBuffer();
        sql.append("Select Offering_Id from OFFERING");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List offIds = new ArrayList();
        try {
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                offIds.add(new Long(rs.getLong("Offering_Id")));
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getOfferings() failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            try {
                if (rs != null) rs.close();
                rs = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            conn = null;
        }
        // Loop & get the offerings.
        Iterator it = offIds.iterator();
        long offId = 0;
        try {
            while (it.hasNext()) {
                offId = ((Long)it.next()).longValue();
                rtn.add(getOffering(offId));
            }
        } catch (ItemNotFoundException infe) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getOfferings() failed ");
            msg.append("while looking up offeringId=");
            msg.append(offId + "");
            throw new OperationFailedException(msg.toString());
        }
        return rtn;
    }

    /**
     * Provides the set of all offerings with a certain default role.
     * @param defaultRoleId the target default role ID
     * @return all the offerings with the specified default role.
     * @throws IllegalArgumentException if the specified topic is null.
     */
    public static List getOfferingsWithDefaultRole(long defaultRoleId)
    throws OperationFailedException {

        // The return.
        List rtn = new ArrayList();

        // SQL -- the set of offerings.
        StringBuffer sql = new StringBuffer();
        sql.append("Select Offering_Id from OFFERING where ");
        sql.append("default_role_id = ?");

        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List offIds = new ArrayList();
        try {
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i=1;
            pstmt.setLong(i++, defaultRoleId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                offIds.add(new Long(rs.getLong("Offering_Id")));
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getOfferingsWithDefaultRole(");
            msg.append(defaultRoleId).append(") failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            try {
                if (rs != null) rs.close();
                rs = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            conn = null;
        }
        // Loop & get the offerings.
        Iterator it = offIds.iterator();
        long offId = 0;
        try {
            while (it.hasNext()) {
                offId = ((Long)it.next()).longValue();
                rtn.add(getOffering(offId));
            }
        } catch (ItemNotFoundException infe) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getOfferingsWithDefaultRole(");
            msg.append(defaultRoleId).append(") failed ");
            msg.append("while looking up offeringId=");
            msg.append(offId + "");
            throw new OperationFailedException(msg.toString());
        }
        return rtn;
    }
    

    /**
     * Deletes the specified offering.
     *
     * @param obj The offering tol delete.
     * @throws OperationFailedException If the delete fails.
     */
    public static void deleteOffering(Offering obj)
    throws IllegalArgumentException, OperationFailedException, DomainException {
        deleteOffering(obj, SystemOfRecordBroker.ACADEMUS);
    }

    public static void deleteOffering(Offering obj, ISystemOfRecord principal)
    throws IllegalArgumentException, OperationFailedException, DomainException {

        // Assertions.
        if (obj == null) {
            String msg = "The offering object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (principal == null) {
            String msg = "Argument 'principal' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Make sure we don't violate an SOR.
        ensureSorAccess(obj, principal, AccessType.DELETE);

        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = AcademusDBUtil.getDBConnection();
            // delete the set of offerings
            String sql = "delete from channel_instance where offering_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, obj.getId());
            pstmt.executeUpdate();

            // delete associated group
            obj.getGroup().delete();
            sql = "delete from topic_offering where offering_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, obj.getId());
            pstmt.executeUpdate();

            // now delete offering
            sql = "delete from offering where offering_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, obj.getId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation deleteOffering(offering) failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            pstmt = null;
            conn = null;
        }
        if (isCaching) {
            cache.remove(new Long(obj.getId()));
            if (handler != null) {
               try {
                    handler.broadcastRemoveOffering((int) obj.getId());
                } catch (AcademusException ae) {
                    ae.printStackTrace();
                }
            }
        }
    }
    /**
     * Removes the offering object from memory/cache.
     * @param offeringID - the offering id to remove from cache
     * @see net.unicon.portal.domain.IllegalArgumentException
     * @see net.unicon.portal.domain.OperationFailedException
     */
    public static void removeOfferingFromCache(long offeringID)
    throws IllegalArgumentException, OperationFailedException {
        if (isCaching) {
            cache.remove(new Long(offeringID));
        }
    }
    /**
     * Refreshed the offering object from memory/cache.
     * @param offeringID - the offering id to remove from cache
     * @see net.unicon.portal.domain.IllegalArgumentException
     * @see net.unicon.portal.domain.ItemNotFoundException
     * @see net.unicon.portal.domain.OperationFailedException
     */
    public static void refreshOfferingCache(long offeringID)
    throws IllegalArgumentException, OperationFailedException, ItemNotFoundException  {
        if (isCaching) {
            cache.remove(new Long(offeringID));
            getOffering(offeringID);
        }
    }
    /**
     * Saves the offering object in the database and in cache if the variable is set.
     * @param obj - the offering object
     * @see net.unicon.portal.domain.Offering
     * @see net.unicon.portal.domain.IllegalArgumentException
     * @see net.unicon.portal.domain.ItemNotFoundException
     * @see net.unicon.portal.domain.OperationFailedException
     */
    public static void persist(Offering obj) throws IllegalArgumentException,
    OperationFailedException, ItemNotFoundException, DomainException {
        persist(obj, SystemOfRecordBroker.ACADEMUS);
    }

    public static void persist(Offering obj, ISystemOfRecord principal) throws IllegalArgumentException,
    OperationFailedException, ItemNotFoundException, DomainException {
        if (obj == null) {
            String msg = "The offering object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (principal == null) {
            String msg = "The principal object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        persistOffering(obj, principal);
        persistChannels(obj);
    }

    /**
     * Method which updates the database with the contents of an offering object.
     * @param offeringObject An offering object which is to be committed to the
     * database.
     * @throws ItemNotFoundException if an existing offering record is not found
     * in the database with a matching offering Id.
     * @throws OperationFailedException if the database update fails
     */
    private static void persistOffering(Offering obj)
    throws OperationFailedException, ItemNotFoundException, DomainException {
        persistOffering(obj, SystemOfRecordBroker.ACADEMUS);
    }

    private static void persistOffering(Offering obj, ISystemOfRecord principal)
    throws OperationFailedException, ItemNotFoundException, DomainException {

        // Assertions.
        if (obj == null) {
            String msg = "Argument 'obj' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (principal == null) {
            String msg = "Argument 'principal' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Make sure we don't violate an SOR.
        ensureSorAccess(obj, principal, AccessType.MODIFY);

        // SQL.
        StringBuffer sql = new StringBuffer();
        sql.append("Update OFFERING ");
        sql.append("set Name  = ?, description = ?, status = ?, ");
        sql.append("Enrollment_Model = ?, default_role_id = ?, ");
        //sql.append("topic_id = ?, ");
        sql.append("opt_offeringId = ?, ");
        sql.append("opt_offeringTerm = ?, ");
        sql.append("opt_offeringMonthStart = ?, ");
        sql.append("opt_offeringDayStart = ?, ");
        sql.append("opt_offeringYearStart = ?, ");
        sql.append("opt_offeringMonthEnd = ?, ");
        sql.append("opt_offeringDayEnd = ?, ");
        sql.append("opt_offeringYearEnd = ?, ");
        sql.append("opt_daysOfWeek = ?, ");
        sql.append("opt_offeringHourStart = ?, ");
        sql.append("opt_offeringMinuteStart = ?, ");
        sql.append("opt_offeringAmPmStart = ?, ");
        sql.append("opt_offeringHourEnd = ?, ");
        sql.append("opt_offeringMinuteEnd = ?, ");
        sql.append("opt_offeringAmPmEnd = ?, ");
        sql.append("opt_offeringLocation = ? ");
        //**********************************************************
        sql.append("where Offering_Id = ?");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setString(i++, obj.getName());
            pstmt.setString(i++, obj.getDescription());
            pstmt.setInt(i++, obj.getStatus());
            pstmt.setString(i++, obj.getEnrollmentModel().toString());
            pstmt.setLong(i++, obj.getDefaultRole().getId());
            pstmt.setString(i++, obj.getOptionalId());
            pstmt.setString(i++, obj.getOptionalTerm());
            pstmt.setInt(i++, obj.getOptionalMonthStart());
            pstmt.setInt(i++, obj.getOptionalDayStart());
            pstmt.setInt(i++, obj.getOptionalYearStart());
            pstmt.setInt(i++, obj.getOptionalMonthEnd());
            pstmt.setInt(i++, obj.getOptionalDayEnd());
            pstmt.setInt(i++, obj.getOptionalYearEnd());
            pstmt.setInt(i++, obj.getDaysOfWeek());
            pstmt.setInt(i++, obj.getOptionalHourStart());
            pstmt.setInt(i++, obj.getOptionalMinuteStart());
            pstmt.setInt(i++, obj.getOptionalAmPmStart());
            pstmt.setInt(i++, obj.getOptionalHourEnd());
            pstmt.setInt(i++, obj.getOptionalMinuteEnd());
            pstmt.setInt(i++, obj.getOptionalAmPmEnd());
            pstmt.setString(i++, obj.getOptionalLocation());
            //**********************************************************
            pstmt.setLong(i++, obj.getId());
            pstmt.execute();

            // Clean up...
            pstmt.close();
            pstmt = null;

            __persistTopics(obj.getTopics(), obj.getId(), conn);
            if (isCaching && handler != null) {
                try {
                    handler.broadcastRefreshOffering((int) obj.getId());
                } catch (AcademusException ae) {
                    ae.printStackTrace();
                }
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation persistOffering failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            pstmt = null;
            conn = null;
        }
    }

    /**
     *
     */
    private static void persistChannels(Offering obj)
    throws OperationFailedException {
        // for the time being, all channels will offer all channels
        // so there is no need to persist
    }

    /**
     *
     */
    private static Map prepCnlMap(List lst) {
        Iterator it = lst.iterator();
        ChannelClass cnl = null;
        Map rtn = new HashMap();
        while (it.hasNext()) {
            cnl = (ChannelClass) it.next();
            rtn.put(cnl.getHandle(), cnl);
        }
        return rtn;
    }

    /**
     *
     */
    static boolean offeringExistsInContext(String name, Offering sibling)
    throws IllegalArgumentException,
    OperationFailedException {
        // Assertions.
        if (name == null) {
            String msg = "The name can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (sibling == null) {
            String msg = "The sibling offering can't be null.";
            throw new IllegalArgumentException(msg);
        }
        // The rtn.
        boolean rtn = false;
        // SQL.
        StringBuffer sql = new StringBuffer();
        sql.append("Select Name from Offering O, Topic_Offering OT ");
        sql.append("where O.Offering_id = OT.Offering_Id and OT.Topic_Id ");
        sql.append("in(Select topic_id from topic_offering where offering_id = ?) ");
        sql.append("and O.offering_id != ?");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, sibling.getId());
            pstmt.setLong(2, sibling.getId());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                if (rs.getString("Name").equalsIgnoreCase(name)) rtn = true;
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation offeringExistsInContext(name, sibling) ");
            msg.append("failed with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            rs = null;
            pstmt = null;
            conn = null;
        }
        return rtn;
    }

    private static boolean __persistTopics(Topic [] topics, long offeringId, Connection conn)
    throws OperationFailedException {
        PreparedStatement pstmt = null;
        ResultSet rs    = null;

        boolean rtnSuccess= false;

        String sql = "select topic_id from topic_offering where offering_id = ?";
        try {
            List newTopicList = new ArrayList();
            for (int ix=0; ix < topics.length; ++ix) {
                newTopicList.add(new Long(topics[ix].getId()));
            }

            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, offeringId);
            rs = pstmt.executeQuery();

            List oldTopicList = new ArrayList();
            Long tempLong = null;
            boolean found = false;
            // Getting the list of current topic ids currently associated with
            // the offering.
            while (rs.next() ) {
                tempLong = new Long(rs.getLong("topic_id"));
                found = newTopicList.remove(tempLong);
                /*
                for (int ix=0; ix < newTopicList.size(); ++ix) {
                    if(newTopicList.getLong() == tempLong.getLong()) {
                        newTopicList.remove(ix);
                        found = true;
                        ix = newTopicList.size();
                    }
                }*/
                if (!found)
                    oldTopicList.add(tempLong);
            }
	    rs.close();
	    rs = null;
	    pstmt.close();
	    pstmt = null;

            // We build two list.
            // newTopicList contains the topic_id(s) that need to be added.
            // oldTopicList contains the topic_ids to remove.
            // If we found a same long in both list, we remove them from both
            // because we didn't have to make a change.
            sql = "insert into topic_offering (topic_id, offering_id) values (?,?)";
            pstmt = conn.prepareStatement(sql.toString());
            for(int ix=0;ix < newTopicList.size(); ++ix) {
                pstmt.setLong(1, ((Long) newTopicList.get(ix)).longValue());
                pstmt.setLong(2, offeringId);
                pstmt.executeUpdate();
            }
	    pstmt.close();
	    pstmt = null;

            sql = "delete from topic_offering where topic_id = ? and offering_id = ?";
            pstmt = conn.prepareStatement(sql.toString());
            for(int ix=0; ix < oldTopicList.size(); ++ix) {
                pstmt.setLong(1, ((Long) oldTopicList.get(ix)).longValue());
                pstmt.setLong(2, offeringId);
                pstmt.executeUpdate();
            }
	    pstmt.close();
	    pstmt = null;

            rtnSuccess =  true;
        } catch (Exception se) {
            String msg = "Unable to persist topics.";
            throw new OperationFailedException(msg, se);
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }            
            rs    = null;
            pstmt = null;
        }
        return rtnSuccess;
    }

    private static void ensureSorAccess(Offering o, ISystemOfRecord principal,
                                AccessType t) throws DomainException {

        // Assertions.
        if (o == null) {
            String msg = "Argument 'o [Offering]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (principal == null) {
            String msg = "Argument 'principal' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (t == null) {
            String msg = "Argument 't [AccessType]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        IEntityRecordInfo rec = SystemOfRecordBroker.getRecordInfo(o);

        // Get out if the sor is the principal.
        if (rec.getSystemOfRecord() == principal) {
            return;
        }

        // Otherwise, see if the requested access is allowable.
        if (rec.getSystemOfRecord().getEntityAccessLevel().compareTo(t) < 0) {
            String msg = "Access of type " + t.toString() + " is not allowed "
                                    + "on entities from the "
                                    + rec.getSystemOfRecord().getSourceName()
                                    + " system.";
            throw new SorViolationException(msg);
        }

    }

}
