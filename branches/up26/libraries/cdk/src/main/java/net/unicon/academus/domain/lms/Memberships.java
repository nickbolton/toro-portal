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

import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.academus.cache.DomainCacheRequestHandler;
import net.unicon.academus.cache.DomainCacheRequestHandlerFactory;
import net.unicon.academus.common.AcademusException;
import net.unicon.academus.common.PersonDirectoryServiceFactory;
import net.unicon.academus.common.properties.*;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.portal.groups.UniconGroupsException;
import net.unicon.portal.groups.IMember;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.unicon.portal.util.db.AcademusDBUtil;
import org.jasig.portal.services.LogService;


public final class Memberships {
    private static Map userCache           = new HashMap();
    private static Map offeringCache       = new HashMap();
    private static Map membershipRoleCache = new HashMap();

    // turning off caching for multi-box model
    private static boolean isCaching = !UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getPropertyAsBoolean("net.unicon.portal.Academus.multipleBoxConfig") && UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getPropertyAsBoolean("net.unicon.academus.domain.lms.isCaching");
    private static final boolean isMultiBox = UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getPropertyAsBoolean("net.unicon.portal.Academus.multipleBoxConfig");

    public static boolean getCaching() {
        return isCaching;
    }
    public static void setCaching(boolean caching) {
        isCaching = caching;
    }

    private static DomainCacheRequestHandler handler = null;

    static {
        if (isMultiBox) {
            try {
                handler = DomainCacheRequestHandlerFactory.getHandler();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Memberships() { }

    private static Map _getMembersOfferingCacheMap (Long offeringId) {
        // Getting the Map from the Map based on the offering ID
        Map map = (Map) offeringCache.get(offeringId);
        // If the map is null, add a the offering to the
        // the offeringCache and have its key value pair
        // be offeringId as a Long and a HashMap;
        if (map == null) {
            map = new HashMap();
            if (isCaching) {
                offeringCache.put(offeringId, map);
            }
        }
        return map;
    }
    private static Map _getAllOfferingsUserCacheMap (String username) {
        // Getting the Map from the Map based on the username
        Map map = (Map)userCache.get(username);
        // If the map is null, add a the user to the
        // the userCache and have its key value pair
        // be username and a HashMap;
        if (map == null) {
            map = new HashMap();
            if (isCaching) {
                userCache.put(username, map);
            }
        }
        return map;
    }
    /**
     * Get Members based on a offering.
     * @param offering
     * @return List - a list of members in an offering
     * @see net.unicon.academus.domain.lms.EnrollmentStatus
     * @see net.unicon.academus.domain.lms.Offering;
     * @throws net.unicon.academus.domain.lms.IllegalArgumentException
     * @throws net.unicon.academus.domain.lms.ItemNotFoundException
     * @throws net.unicon.academus.domain.lms.OperationFailedException
     */
    public static List getMembers(Offering offering)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        if (isCaching) {
            synchronized (offeringCache) {
                return _getMembers(offering, EnrollmentStatus.ENROLLED);
            }
        }
        return _getMembers(offering, EnrollmentStatus.ENROLLED);
    }
    /**
     * Get Members based on a particular enrollement status and offering.
     * @param enrollmentStatus
     * @param offering
     * @return List - a list of members in an offering based on the a EnrollementStatus
     * @see net.unicon.academus.domain.lms.EnrollmentStatus
     * @see net.unicon.academus.domain.lms.Offering;
     * @throws net.unicon.academus.domain.lms.IllegalArgumentException
     * @throws net.unicon.academus.domain.lms.ItemNotFoundException
     * @throws net.unicon.academus.domain.lms.OperationFailedException
     */
    public static List getMembers(Offering offering, EnrollmentStatus enrollmentStatus)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        if (isCaching) {
            synchronized (offeringCache) {
                return _getMembers(offering, enrollmentStatus);
            }
        }
        return _getMembers(offering, enrollmentStatus);
    }

    /**
     * <p>
     * Search for a member with in the membership based on a specific set
     * of passed in search criteria.
     * </p><p>
     *
     * @param enrollmentStatus
     * @param offering
     * </p><p>
     *
     * @return List - a list of members in an offering based on the a EnrollementStatus
     * </p><p>
     *
     * @see net.unicon.academus.domain.lms.EnrollmentStatus
     * @see net.unicon.academus.domain.lms.Offering
     * @see net.unicon.academus.domain.lms.MemberSearchCriteria
     * </p><p>
     *
     * @throws net.unicon.academus.domain.lms.IllegalArgumentException
     * @throws net.unicon.academus.domain.lms.ItemNotFoundException
     * @throws net.unicon.academus.domain.lms.OperationFailedException
     * </p>
     */
    public static List findMembers(Offering offering, EnrollmentStatus enrollmentStatus,
    MemberSearchCriteria criteria) throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        return _findMembers(offering, enrollmentStatus, criteria);
    }

    /**
     * <p>
     * Search for a member with in the membership based on a specific set
     * of passed in search criteria.
     * </p><p>
     *
     * @param enrollmentStatus
     * @param offering
     * </p><p>
     *
     * @return List - a list of members in an offering based on the a EnrollementStatus
     * </p><p>
     *
     * @see net.unicon.academus.domain.lms.EnrollmentStatus
     * @see net.unicon.academus.domain.lms.Offering
     * @see net.unicon.academus.domain.lms.MemberSearchCriteria
     * </p><p>
     *
     * @throws net.unicon.academus.domain.lms.IllegalArgumentException
     * @throws net.unicon.academus.domain.lms.ItemNotFoundException
     * @throws net.unicon.academus.domain.lms.OperationFailedException
     * </p>
     */
    protected static List _findMembers(Offering offering, EnrollmentStatus enrollmentStatus,
    MemberSearchCriteria criteria) throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        if (offering == null) {
            String msg = "The offering object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (enrollmentStatus == null) {
            String msg = "The enrollment status object can't be null.";
            throw new IllegalArgumentException(msg);
        }
         if (criteria == null) {
            String msg = "The search criteria object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        List rtnList = new ArrayList();

        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Try to find the users.
            conn = AcademusDBUtil.getDBConnection();

            List usernames = new ArrayList();
            StringBuffer sql = new StringBuffer();
            sql.append("select user_name from membership ");
            sql.append("where offering_id = ? and enrollment_status = ?");
            pstmt = conn.prepareStatement(sql.toString());
            int i=1;
            pstmt.setLong(i++, offering.getId());
            pstmt.setInt(i++, enrollmentStatus.toInt());

            rs = pstmt.executeQuery();

            while (rs.next()) {
                usernames.add(rs.getString("user_name"));
            }

            rtnList = PersonDirectoryServiceFactory.getService().
                find((String[])usernames.toArray(new String[0]),
                    criteria.getUsername(), criteria.getFirstName(),
                    criteria.getLastName(), criteria.getEmail(),
                    criteria.matchAllCriteria());
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs    != null) rs.close();
                rs    = null;
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
            conn  = null;
        }
        return rtnList;
    }

    /**
     * @param enrollmentStatus
     * @param offering
     * @return List - a list of members in an offering based on the a EnrollementStatus
     * @see net.unicon.academus.domain.lms.EnrollmentStatus
     * @see net.unicon.academus.domain.lms.Offering;
     * @throws net.unicon.academus.domain.lms.IllegalArgumentException
     * @throws net.unicon.academus.domain.lms.ItemNotFoundException
     * @throws net.unicon.academus.domain.lms.OperationFailedException
     */
    protected static List _getMembers(Offering offering, EnrollmentStatus enrollmentStatus)
    throws IllegalArgumentException, ItemNotFoundException, OperationFailedException {
        if (offering == null) {
            String msg = "The offering object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (enrollmentStatus == null) {
            String msg = "The enrollment status object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        List rtn = null;
        if (isCaching) {
            Map map = _getMembersOfferingCacheMap(new Long(offering.getId()));
            rtn = (List)map.get(enrollmentStatus);
            if (rtn != null) {
                // convert the list of usernames to User's
                Iterator itr = rtn.iterator();
                ArrayList rtnArrayList = new ArrayList();
                String username = null;
                while (itr.hasNext()) {
                    username = (String)itr.next();
                    try {
                        rtnArrayList.add(UserFactory.getUser(username));
                    } catch (Exception e) {
                        LogService.instance().log(LogService.WARN,
                        "User with the username " + username +
                        " is not currently found in the system.");
                    }
                }
                return rtnArrayList;
            }
        }
        List cacheList = new ArrayList();
        rtn = new ArrayList();
        // SQL.
        StringBuffer sql = new StringBuffer();
        sql.append("select user_name from membership where offering_id = ?");
        sql.append(" and enrollment_status = ?" );
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Try to find the user.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setLong(i++, offering.getId());
            pstmt.setInt(i++, enrollmentStatus.toInt());
            rs = pstmt.executeQuery();
            String username = null;
            while (rs.next()) {
                username = rs.getString("user_name");
                try {
                    rtn.add(UserFactory.getUser(username));
                    cacheList.add(username);
                } catch (Exception e) {
                    LogService.instance().log(LogService.ERROR,
                        "User with the username " + username +
                        " is not currently found in the system.");
                }
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getMembers(offering[,enrollmentStatus]) failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString(), e);
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
        if (isCaching) {
            Map map = _getMembersOfferingCacheMap(new Long(offering.getId()));

            // Since this is a select statement, and not an insert
            // I'm not going to call the handler
            map.put(enrollmentStatus, cacheList);
        }
        ArrayList rtnArrayList = new ArrayList(rtn);
        return rtnArrayList;
    }
    /**
     * @param user
     * @param status - ??
     * @see net.unicon.academus.domain.lms.User;
     * @throws net.unicon.academus.domain.lms.IllegalArgumentException
     * @throws net.unicon.academus.domain.lms.ItemNotFoundException
     * @throws net.unicon.academus.domain.lms.OperationFailedException
     */
    public static List getOfferings(User user, int status) throws IllegalArgumentException,
                                    ItemNotFoundException, OperationFailedException {

        if (user == null) {
            String msg = "The user object can't be null.";
            throw new IllegalArgumentException(msg);
        }

        // Check to be sure the LMS is even in use -- needs to be done for any
        // operation that doesn't have either offerings or topics in its method
        // signiture.
        if (!LmsSettings.isEnabled()) {
            // The LMS is not in use, so get out w/o using resources.
            return new ArrayList();
        }

        List retList = new ArrayList();
        Iterator itr = getAllOfferings(user).iterator();
        while (itr != null && itr.hasNext()) {
            Offering offering = (Offering)itr.next();
            if (offering.getStatus() == status) {
                retList.add(offering);
            }
        }

        return retList;

    }

    /**
     * New method for request/approve enrollment/model (used by roster channel)
     * @param user
     * @param status - ??
     * @param enrollmentStatus
     * @return List - a list of members in an offering based on the a EnrollementStatus
     * @see net.unicon.academus.domain.lms.EnrollmentStatus
     * @see net.unicon.academus.domain.lms.User;
     * @throws net.unicon.academus.domain.lms.IllegalArgumentException
     * @throws net.unicon.academus.domain.lms.ItemNotFoundException
     * @throws net.unicon.academus.domain.lms.OperationFailedException
     */
    public static List getOfferings(User user, int status, EnrollmentStatus enrollmentStatus)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {

        if (user == null) {
            String msg = "The user object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (enrollmentStatus == null) {
            String msg = "The enrollment status object can't be null.";
            throw new IllegalArgumentException(msg);
        }

        // Check to be sure the LMS is even in use -- needs to be done for any
        // operation that doesn't have either offerings or topics in its method
        // signiture.
        if (!LmsSettings.isEnabled()) {
            // The LMS is not in use, so get out w/o using resources.
            return new ArrayList();
        }

        List retList = new ArrayList();
        Iterator itr = getAllOfferings(user, enrollmentStatus).iterator();
        while (itr != null && itr.hasNext()) {
            Offering offering = (Offering)itr.next();
            if (offering.getStatus() == status) {
                retList.add(offering);
            }
        }

        return retList;

    }
    /**
     * Enrolled users are only those with an enrollment status of ENROLLED.
     * Users with enrollment status values of PENDING or INVITED are not
     * considered as being enrolled in an offering.  Refer to EnrollmentStatus.java for more details on the enrollment status
     * values, which are ultimately stored in the enrollment_status field of the membership table.
     * @throws net.unicon.academus.domain.lms.IllegalArgumentException
     * @throws net.unicon.academus.domain.lms.ItemNotFoundException
     * @throws net.unicon.academus.domain.lms.OperationFailedException
     */
    public static boolean isEnrolled(User user, Offering offering)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        boolean rslt = false;
        if (isCaching) {
            rslt = getMembers(offering).contains(user);
        } else {
            rslt = isEnrolled(user, offering, EnrollmentStatus.ENROLLED);
        }
        return rslt;
    }
    // new overloaded method for request/approve enrollment model
    public static boolean isEnrolled(User user, Offering offering,
    EnrollmentStatus enrollmentStatus)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        if (user == null) {
            String msg = "The user object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (offering == null) {
            String msg = "The offering object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (enrollmentStatus == null) {
            String msg = "The enrollment status object can't be null.";
            throw new IllegalArgumentException(msg);
        }

        if (isCaching) {
            return getMembers(offering, enrollmentStatus).contains(user);
        }

        final String sql = "select membership_id from membership where user_name = ? and enrollment_status = ? and offering_id = ?";

        boolean rslt = false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Try to find the user.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql);
            int i = 1;
            pstmt.setString(i++, user.getUsername());
            pstmt.setInt(i++, enrollmentStatus.toInt());
            pstmt.setLong(i++, offering.getId());
            rs = pstmt.executeQuery();
            if (rs.next())
                rslt = true;
        } catch (Exception se) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getOfferings(user[,enrollmentStatus]) failed ");
            msg.append("with the following message:\n");
            msg.append(se.getMessage());
            throw new OperationFailedException(msg.toString(), se);
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

        return rslt;
    }

    public static int enrolledMembers(Offering offering)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        if (offering == null) {
            String msg = "The offering object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        
        if (isCaching) {
            List enrolledMembers = getMembers(offering, EnrollmentStatus.ENROLLED);
            List pendingMembers  = getMembers(offering, EnrollmentStatus.PENDING);
            List invitedMembers  = getMembers(offering, EnrollmentStatus.INVITED);
            return (enrolledMembers.size() + pendingMembers.size() + invitedMembers.size());
        }

        int rslt = 0;
        final String sql = "select count(*) from membership where offering_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql);
            int i = 1;
            pstmt.setLong(i++, offering.getId());
            rs = pstmt.executeQuery();
            if (rs.next())
                rslt += rs.getInt(1);
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation enrolledMembers(offering) failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString(), e);
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

        return rslt;
    }
    public static boolean hasMembership(Offering offering)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        if (offering == null) {
            String msg = "The offering object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        return enrolledMembers(offering) > 0;
    }
    // new overloaded method created to qualify membership based on the desired
    // enrollment status (see EnrollmentStatus.java for more details).
    public static boolean hasMembership(Offering offering, EnrollmentStatus enrollmentStatus)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        if (offering == null) {
            String msg = "The offering object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        List qualifiedMembers = getMembers(offering, enrollmentStatus);
        return qualifiedMembers.size() > 0;
    }
    public static boolean userEnrolledWithRole(long roleId)
    throws IllegalArgumentException, OperationFailedException, ItemNotFoundException {
        Iterator itr = OfferingFactory.getOfferings().iterator();
        while (itr != null && itr.hasNext()) {
            Offering offering = (Offering)itr.next();
            Iterator itr2 = getMembers(offering).iterator();
            while (itr2 != null && itr2.hasNext()) {
                Role role = getRole((User)itr2.next(), offering);
                if (role.getId() == roleId) return true;
            }
        }
        return false;
    }
    public static List getAllOfferings(User user)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {

        // Check to be sure the LMS is even in use -- needs to be done for any
        // operation that doesn't have either offerings or topics in its method
        // signiture.
        if (!LmsSettings.isEnabled()) {
            // The LMS is not in use, so get out w/o using resources.
            return new ArrayList();
        }

        if (isCaching) {
            synchronized (userCache) {
                return _getAllOfferings(user, EnrollmentStatus.ENROLLED);
            }
        }
        return _getAllOfferings(user, EnrollmentStatus.ENROLLED);
    }

    // new method for request/approve and other enrollment models
    public static List getAllOfferings(User user, EnrollmentStatus enrollmentStatus)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {

        // Check to be sure the LMS is even in use -- needs to be done for any
        // operation that doesn't have either offerings or topics in its method
        // signiture.
        if (!LmsSettings.isEnabled()) {
            // The LMS is not in use, so get out w/o using resources.
            return new ArrayList();
        }

        if (isCaching) {
            synchronized (userCache) {
                return _getAllOfferings(user, enrollmentStatus);
            }
        }

        return _getAllOfferings(user, enrollmentStatus);

    }

    public static List _getAllOfferings(User user, EnrollmentStatus enrollmentStatus)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {

        if (user == null) {
            String msg = "The user object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (enrollmentStatus == null) {
            String msg = "The enrollment status object can't be null.";
            throw new IllegalArgumentException(msg);
        }

        // Check to be sure the LMS is even in use -- needs to be done for any
        // operation that doesn't have either offerings or topics in its method
        // signiture.
        if (!LmsSettings.isEnabled()) {
            // The LMS is not in use, so get out w/o using resources.
            return new ArrayList();
        }

        List rtn = null;
        if (isCaching) {
            Map map = _getAllOfferingsUserCacheMap(user.getUsername());
            rtn = (List)map.get(enrollmentStatus);
            if (rtn != null) {
                // Have to convert the offeringIDs to Offering Objects
                return buildOfferingObjectList(rtn);
            }
        }
        rtn = new ArrayList();
        List cacheList = new ArrayList();
        // SQL.
        StringBuffer sql = new StringBuffer();
        sql.append("select offering_id from membership where user_name = ?");
        sql.append(" and enrollment_status = ?");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Try to find the user.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setString(i++, user.getUsername());
            pstmt.setInt(i++, enrollmentStatus.toInt());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Long offeringId = new Long(rs.getLong("offering_id"));
                rtn.add(OfferingFactory.getOffering(offeringId.longValue()));
                cacheList.add(offeringId);
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getOfferings(user[,enrollmentStatus]) failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString(), e);
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
        if (isCaching) {
            Map map = _getAllOfferingsUserCacheMap(user.getUsername());
            // Since this is a select statement and not and insert
            // nor update, i am not calling the handler.
            map.put(enrollmentStatus, cacheList);
        }
        ArrayList rtnArrayList = new ArrayList(rtn);
        return rtnArrayList;
    }

    public static void changeRoles(Offering offering,
    Role oldRole, Role newRole)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        if (isCaching) {
            synchronized (membershipRoleCache) {
                _changeRoles(offering, oldRole, newRole);
            }
        } else {
            _changeRoles(offering, oldRole, newRole);
        }
    }

    protected static void _changeRoles(Offering offering,
    Role oldRole, Role newRole)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        if (offering == null) {
            String msg = "The offering object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (oldRole == null) {
            String msg = "The oldRole object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (newRole == null) {
            String msg = "The newRole object can't be null.";
            throw new IllegalArgumentException(msg);
        }

/* AW 7/21/05:  Changed for a more direct impl to improve performance...

        Iterator itr;
        User user;
        EnrollmentStatus[] statusArr = {
            EnrollmentStatus.ENROLLED,
            EnrollmentStatus.PENDING,
            EnrollmentStatus.INVITED
        };


        // gather the list of users whos role needs to change
        for (int i=0; i<statusArr.length; i++) {
            itr = getMembers(offering, statusArr[i]).iterator();
            while (itr.hasNext()) {
                user = (User)itr.next();
                if (oldRole.getId() == getRole(user, offering).getId()) {
                    // Change their membership role
                    changeRole(user, offering, newRole);
                }
            }
        }
*/

        // Evaluate the users that will be affected by the change...
        List affectedUsers = new ArrayList();
        affectedUsers.addAll(_getMembers(offering, EnrollmentStatus.ENROLLED));
        affectedUsers.addAll(_getMembers(offering, EnrollmentStatus.PENDING));
        affectedUsers.addAll(_getMembers(offering, EnrollmentStatus.INVITED));
        for (Iterator it=affectedUsers.iterator(); it.hasNext();) {
            User u = (User) it.next();
            if (oldRole.getId() != getRole(u, offering).getId()) {
                it.remove();
            }
        }

        // Make the change...
        StringBuffer sql = new StringBuffer();
        sql.append("update membership set role_id = ? ");
        sql.append("where role_id = ? and offering_id = ?");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            // Access the data.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setLong(i++, newRole.getId());
            pstmt.setLong(i++, oldRole.getId());
            pstmt.setLong(i++, offering.getId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation Memberships._changeRoles() failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString(), e);
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

        // Update the cache, etc...
        if (isCaching) {
            Map userMap = (Map)membershipRoleCache.get(new Long(offering.getId()));
            if (userMap == null) {
                userMap = new HashMap();
                membershipRoleCache.put(new Long(offering.getId()), userMap);

                if (handler != null) {
                    try {
                    handler.broadcastRemoveRoleMembership((int) offering.getId());
                    } catch (AcademusException ae) {
                        ae.printStackTrace();
                    }
                }
            }

            for (Iterator it=affectedUsers.iterator(); it.hasNext();) {
                User u = (User) it.next();
                userMap.put(u.getUsername(), new Long(newRole.getId()));
                if (handler != null) {
                    try {
                        handler.broadcastRefreshUserMembership(u.getUsername());
                    } catch (AcademusException ae) {
                        ae.printStackTrace();
                    }
                }
            }
        }
        for (Iterator it=affectedUsers.iterator(); it.hasNext();) {
            User u = (User) it.next();
            removeGroupAssociation(u, offering, oldRole);
            createGroupAssociation(u, offering, newRole);
        }

    }

    public static void changeRole(User user, Offering offering,
    Role role)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        if (isCaching) {
            synchronized (membershipRoleCache) {
                _changeRole(user, offering, role);
            }
        } else {
            _changeRole(user, offering, role);
        }
    }

    protected static void _changeRole(
    User user,
    Offering offering,
    Role role)
    throws IllegalArgumentException, ItemNotFoundException, OperationFailedException {
        if (user == null) {
            String msg = "The user object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (offering == null) {
            String msg = "The offering object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (role == null) {
            String msg = "The role object can't be null.";
            throw new IllegalArgumentException(msg);
        }

        Role oldRole = getRole(user, offering);
        StringBuffer sql = new StringBuffer();
        sql.append("update membership set role_id = ? ");
        sql.append("where offering_id = ? and user_name = ?");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            // Access the data.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setLong(i++, role.getId());
            pstmt.setLong(i++, offering.getId());
            pstmt.setString(i++, user.getUsername());
            pstmt.executeUpdate();
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation Memberships.changeRole() failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString(), e);
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
        if (isCaching) {
            Map userMap = (Map)membershipRoleCache.get(new Long(offering.getId()));
            if (userMap == null) {
                userMap = new HashMap();
                membershipRoleCache.put(new Long(offering.getId()), userMap);

                if (handler != null) {
                    try {
                    handler.broadcastRemoveRoleMembership((int) offering.getId());
                    } catch (AcademusException ae) {
                        ae.printStackTrace();
                    }
                }
            }

            userMap.put(user.getUsername(), new Long(role.getId()));
            if (handler != null) {
                try {
                    handler.broadcastRefreshUserMembership(user.getUsername());
                } catch (AcademusException ae) {
                    ae.printStackTrace();
                }
            }
        }

        // handle group memberships
        removeGroupAssociation(user, offering, oldRole);
        createGroupAssociation(user, offering, role);
    }
    public static Role getRole(User user, Offering offering)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        if (isCaching) {
            synchronized (membershipRoleCache) {
                return _getRole(user, offering);
            }
        }
        return _getRole(user, offering);
    }
    public static Role _getRole(User user, Offering offering)
    throws IllegalArgumentException, ItemNotFoundException, OperationFailedException {
        if (user == null) {
            String msg = "The user object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (offering == null) {
            String msg = "The offering object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        Role rtn = null;
        Map userMap = null;
        if (isCaching) {
            userMap = (Map)membershipRoleCache.get(new Long(offering.getId()));
            if (userMap == null) {
                userMap = new HashMap();
                membershipRoleCache.put(new Long(offering.getId()), userMap);
            }
            try {
                Long l = (Long) userMap.get(user.getUsername());
                if (l != null) {
                    rtn = RoleFactory.getRole(l.longValue());
                    if (rtn != null) return rtn;
                }
            } catch (ItemNotFoundException infe) {
                LogService.instance().log(LogService.ERROR, "Could not find role : " +
                ((Long) userMap.get(user.getUsername())).longValue());
            } catch (OperationFailedException ofe) {
                ofe.printStackTrace();
            }
        }
        StringBuffer sql = new StringBuffer();
        sql.append("Select role_id from membership ");
        sql.append("where offering_id = ? and user_name = ?");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Access the data.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setLong(i++, offering.getId());
            pstmt.setString(i++, user.getUsername());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                rtn = RoleFactory.getRole(rs.getLong("role_id"));
            } else {
                StringBuffer msg = new StringBuffer();
                msg.append("Method Memberships.getRole() ");
                msg.append("could not find membership with offering_id=");
                msg.append(offering.getId() + " and ");
                msg.append("username=" + user.getUsername());
                throw new ItemNotFoundException(msg.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            StringBuffer msg = new StringBuffer();
            msg.append("Operation Memberships.getRole() failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString(), e);
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
        if (isCaching) {
            // Since this is a select statement and not and insert
            // nor update, i am not calling the handler.
            userMap.put(user.getUsername(), new Long(rtn.getId()));
        }
        return rtn;
    }
    public static void add(User user, Offering offering,
    Role role)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        if (isCaching) {
            synchronized (membershipRoleCache) {
                _add(user, offering, role, EnrollmentStatus.ENROLLED);
            }
        } else {
            _add(user, offering, role, EnrollmentStatus.ENROLLED);
        }
    }
    // new method for the request/approve enrollment model
    public static void add(User user, Offering offering,
    Role role, EnrollmentStatus enrollmentStatus)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        if (isCaching) {
            synchronized (membershipRoleCache) {
                _add(user, offering, role, enrollmentStatus);
            }
        } else {
            _add(user, offering, role, enrollmentStatus);
        }
    }
    public static void _add(User user,
    Offering offering,
    Role role,
    EnrollmentStatus enrollmentStatus)
    throws IllegalArgumentException, ItemNotFoundException, OperationFailedException {
        if (user == null) {
            String msg = "The user object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (offering == null) {
            String msg = "The offering object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (role == null) {
            String msg = "The role object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (enrollmentStatus == null) {
            String msg = "The enrollment status object can't be null.";
            throw new IllegalArgumentException(msg);
        }

        if (isCaching) {
            Map omap = _getMembersOfferingCacheMap(new Long(offering.getId()));
            List members = (List)omap.get(enrollmentStatus);
            if (members == null) {
                omap.put(enrollmentStatus, new ArrayList());
                members = (List)omap.get(enrollmentStatus);
            }
            if ((members.size() == 0) || (!members.contains(user.getUsername()))) {
                members.add(user.getUsername());
            }
            Map umap = _getAllOfferingsUserCacheMap(user.getUsername());
            List offerings = (List)umap.get(enrollmentStatus);
            if (offerings == null) {
                umap.put(enrollmentStatus, new ArrayList());
                offerings = (List)umap.get(enrollmentStatus);
            }
            if ((offerings.size() == 0) || (!offerings.contains(new Long(offering.getId())))) {
                offerings.add(new Long(offering.getId()));
            }
            if (enrollmentStatus.equals(EnrollmentStatus.ENROLLED) &&
            offering.getEnrollmentModel().toString().equals(
            EnrollmentModel.REQUESTAPPROVE.toString())) {
                List mpending = (List)omap.get(EnrollmentStatus.PENDING);
                if (mpending == null) {
                    omap.put(EnrollmentStatus.PENDING, new ArrayList());
                    mpending = (List)omap.get(EnrollmentStatus.PENDING);
                }
                if (mpending.contains(user.getUsername())) {
                    mpending.remove(user.getUsername());
                }
                List opending = (List)umap.get(EnrollmentStatus.PENDING);
                if (opending == null) {
                    umap.put(EnrollmentStatus.PENDING, new ArrayList());
                    opending = (List)umap.get(EnrollmentStatus.PENDING);
                }
                if (opending.contains(new Long(offering.getId()))) {
                    opending.remove(new Long(offering.getId()));
                }
            }
            Map userMap = (Map)membershipRoleCache.get(new Long(offering.getId()));
            if (userMap == null) {
                userMap = new HashMap();
                membershipRoleCache.put(new Long(offering.getId()), userMap);
            }
            userMap.put(user.getUsername(), new Long(role.getId()));

            if (handler != null) {
                try {
                    handler.broadcastRefreshMembership((int)offering.getId());
                    handler.broadcastRefreshUserMembership(user.getUsername());
                    handler.broadcastRemoveRoleMembership((int) offering.getId());
                } catch (AcademusException ae) {
                    ae.printStackTrace();
                }
            }
        }
        // This is wrong and should be changed !!! These are comparing to
        // to hard coded values.  Scary - Scary
        boolean hideGradebook = (role.getId() == 5L || role.getId() == 6L);

        addEnrollment(offering.getId(), user.getUsername(), role.getId(),
        hideGradebook, enrollmentStatus);

        // Now create the group associations to the offering and role groups.
        createGroupAssociation(user, offering, role);
    }

    private static void createGroupAssociation(User user, Offering offering, Role role)
    throws OperationFailedException {
        try {
            IMember member = user.getGroupMember();
            offering.getGroup().addMember(member);

            // First check if the user has been previously added to this
            // group. This can happen if the user has been enrolled in
            // another offering with the same role.
            if (!role.getGroup().contains(member)) {
                role.getGroup().addMember(member);
            }
        } catch (UniconGroupsException ge) {
            throw new OperationFailedException(ge);
        }
    }

    public static void remove(User user, Offering offering)
    throws IllegalArgumentException, ItemNotFoundException, OperationFailedException {
        if (isCaching) {
            synchronized (membershipRoleCache) {
                _remove(user, offering, EnrollmentStatus.ENROLLED);
            }
        } else {
            _remove(user, offering, EnrollmentStatus.ENROLLED);
        }
    }
    public static void remove(
    User user,
    Offering offering,
    EnrollmentStatus enrollmentStatus)
    throws IllegalArgumentException, ItemNotFoundException, OperationFailedException {
        if (isCaching) {
            synchronized (membershipRoleCache) {
                _remove(user, offering, enrollmentStatus);
            }
        } else {
            _remove(user, offering, enrollmentStatus);
        }
    }

    protected static void _remove(
    User user,
    Offering offering,
    EnrollmentStatus enrollmentStatus)
    throws IllegalArgumentException, ItemNotFoundException, OperationFailedException {
        if (user == null) {
            String msg = "The user object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (offering == null) {
            String msg = "The offering object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (enrollmentStatus == null) {
            String msg = "The enrollment status object can't be null.";
            throw new IllegalArgumentException(msg);
        }

        if (isCaching) {
            Map omap = _getMembersOfferingCacheMap(new Long(offering.getId()));
            List members = (List)omap.get(enrollmentStatus);
            if (members == null) {
                omap.put(enrollmentStatus, new ArrayList());
            }
            else if ((members.size() > 0) && (members.contains(user.getUsername()))) {
                members.remove(user.getUsername());
            }
            Map umap = _getAllOfferingsUserCacheMap(user.getUsername());
            List offerings = (List)umap.get(enrollmentStatus);
            if (offerings == null) {
                umap.put(enrollmentStatus, new ArrayList());
            }
            else if ((offerings.size() > 0) &&
            (offerings.contains(new Long(offering.getId())))) {
                offerings.remove(new Long(offering.getId()));
            }
            Map userMap = (Map)membershipRoleCache.get(new Long(offering.getId()));
            if (userMap != null) {
                userMap.remove(user.getUsername());
            }

            if (handler != null) {
                try {
                    handler.broadcastRemoveMembership((int)offering.getId());
                    handler.broadcastRemoveUserMembership(user.getUsername());
                    handler.broadcastRemoveRoleMembership((int) offering.getId());
                } catch (AcademusException ae) {
                    ae.printStackTrace();
                }
            }
        }

        // save the user's enrolled role
        Role role = getRole(user, offering);

        // unenroll the user
        __removeEnrollment(user.getUsername(), offering.getId());

        // now remove the user from the offering group and role group
        // if the user isn't enrolled in any other offerings with the same role
        try {
            offering.getGroup().removeMember(user.getGroupMember());
            removeGroupAssociation(user, offering, role);
        } catch (UniconGroupsException uge) {
            throw new OperationFailedException(uge);
        } catch (IllegalArgumentException iae) {
            throw new OperationFailedException(iae);
        }
    }

    private static void removeGroupAssociation(User user, Offering offering, Role role)
    throws OperationFailedException {
        try {
            IMember member = user.getGroupMember();
            offering.getGroup().removeMember(member);

            if (okToRemoveRoleGroup(user, role, offering)) {
                role.getGroup().removeMember(member);
                if (role.isUserUniqueRole()) {
                    RoleFactory.deleteRole(role);
                }
            }
        } catch (UniconGroupsException ge) {
            throw new OperationFailedException(ge);
        } catch (ItemNotFoundException infe) {
            throw new OperationFailedException(infe);
        }
    }

    private static void __removeEnrollment(String username, long offeringId)
    throws OperationFailedException {
        // SQL goo
        Connection conn = null;
        PreparedStatement pstmt = null;
        StringBuffer sql = new StringBuffer();
        sql.append("delete from membership where user_name = ? ");
        sql.append("and offering_id = ?");
        try {
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setString(i++, username);
            pstmt.setLong(i++, offeringId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation removeEnrollment failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString(), e);
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
    }
    private static boolean okToRemoveRoleGroup(User user, Role targetRole,
        Offering targetOffering)
    throws ItemNotFoundException, OperationFailedException {

        if (targetRole == null || user == null || targetOffering == null) {
            return false;
        }

        // look for an offering this user is enrolled in
        Role enrolledRole = null;
        Offering off = null;
        Iterator itr = getAllOfferings(user).iterator();
        while (itr != null && itr.hasNext()) {
            off = (Offering)itr.next();
            enrolledRole = getRole(user, off);
            if (targetRole.getId() == enrolledRole.getId()) {
                // they're enrolled with the target role, it's not ok
                return false;
            }
        }

        // they're not enrolled with the target role, it's ok to remove
        return true;
    }

    private static void addEnrollment(
    long offeringId,
    String username,
    long roleId,
    boolean hideGradebook,
    EnrollmentStatus enrollmentStatus)
    throws OperationFailedException {
        // SQL goo
        Connection conn = null;
        PreparedStatement pstmt = null;
        StringBuffer sql = new StringBuffer();
        sql.append("insert into membership ");
        sql.append("(offering_id, user_name, role_id, hide_gradebook, enrollment_status) ");
        sql.append("values (?,?,?,?,?)");
        try {
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setLong(i++, offeringId);
            pstmt.setString(i++, username);
            pstmt.setLong(i++, roleId);
            pstmt.setBoolean(i++, hideGradebook);
            pstmt.setInt(i++, enrollmentStatus.toInt());
            pstmt.executeUpdate();
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation addEnrollment failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString(), e);
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
    }

    public static void removeEnrollment(long offeringId)
    throws OperationFailedException {
        // SQL goo
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Offering offering = null;
        User user;
        EnrollmentStatus status;

        try {
            offering = OfferingFactory.getOffering(offeringId);
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(
                "select user_name from membership where offering_id = ?");
            int i = 1;
            pstmt.setLong(i++, offeringId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                user = UserFactory.getUser(rs.getString(1));
                status = getEnrollmentStatus(user, offering);
                remove(user, offering, status);
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation removeEnrollment failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString(), e);
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
    }

    public static EnrollmentStatus getEnrollmentStatus(User u, Offering o)
    throws OperationFailedException {
        // Assertions.
        if (u == null) {
            String msg = "Argument u (User) cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (o == null) {
            String msg = "Argument o (Offering) cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // Db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        // Lookup the status.
        EnrollmentStatus rslt = null;
        try {
            conn = AcademusDBUtil.getDBConnection();
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT ENROLLMENT_STATUS FROM MEMBERSHIP ");
            sql.append("WHERE USER_NAME = ? AND OFFERING_ID = ?");
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, u.getUsername());
            pstmt.setLong(2, o.getId());
            // See what we get.
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int es = rs.getInt("ENROLLMENT_STATUS");
                rslt = EnrollmentStatus.getInstance(es);
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getEnrollmentStatus failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString(), e);
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
        return rslt;
    }
    private static List buildOfferingObjectList(List offeringIdList) {
        List rtnList = new ArrayList();
        for (int ix = 0; ix < offeringIdList.size(); ++ix) {
            try {
                long offeringId = ((Long) offeringIdList.get(ix)).longValue();
                rtnList.add(OfferingFactory.getOffering(offeringId));
            } catch (ItemNotFoundException infe) {
                LogService.instance().log(LogService.ERROR, "Could not find offering : " +
                ((Long) offeringIdList.get(ix)).longValue());
            } catch (OperationFailedException ofe) {
                ofe.printStackTrace();
            }
        }
        return rtnList;
    }
    private static List buildRoleObjectList(List roleIdList) {
        List rtnList = new ArrayList();
        for (int ix = 0; ix < roleIdList.size(); ++ix) {
            try {
                rtnList.add(
                RoleFactory.getRole(((Long) roleIdList.get(ix)).longValue()));
            } catch (ItemNotFoundException infe) {
                LogService.instance().log(LogService.ERROR, "Could not find role : " +
                ((Long) roleIdList.get(ix)).longValue());
            } catch (OperationFailedException ofe) {
                ofe.printStackTrace();
            }
        }
        return rtnList;
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
            offeringCache.remove(new Long(offeringID));
        }
    }

    /**
     * Refresh offering membership memory/cache.
     * @param offeringID - the offering id to remove from cache
     * @see net.unicon.portal.domain.IllegalArgumentException
     * @see net.unicon.portal.domain.OperationFailedException
     */
    public static void refreshOfferingCache(long offeringID)
    throws IllegalArgumentException, OperationFailedException {
        if (isCaching) {
            offeringCache.remove(new Long(offeringID));
            try {
                Offering offering = OfferingFactory.getOffering(offeringID);
                getMembers(offering);
            } catch (Exception e) {
                LogService.instance().log(LogService.ERROR,
                    "Offering with the offering ID " + offeringID +
                    " is not currently found in the system." +
                    "Unable to refresh cache. " );
            }
        }
    }

    /**
     * Removes the user object from memory/cache.
     * @param username - the username to remove from cache
     * @see net.unicon.portal.domain.IllegalArgumentException
     * @see net.unicon.portal.domain.OperationFailedException
     */
    public static void removeUserFromCache(String username)
    throws IllegalArgumentException, OperationFailedException {
        if (isCaching) {
            userCache.remove(username);
        }
    }

    /**
     * Refresh the user object from memory/cache.
     * @param username - the username to remove from cache
     * @see net.unicon.portal.domain.IllegalArgumentException
     * @see net.unicon.portal.domain.OperationFailedException
     */
    public static void refreshUserCache(String username)
    throws IllegalArgumentException, OperationFailedException {
        if (isCaching) {
            userCache.remove(username);
            try {
                User user = UserFactory.getUser(username);
                getAllOfferings(user);
            } catch (Exception e) {
                LogService.instance().log(LogService.ERROR,
                    "User with the username " + username +
                    " is not currently found in the system." +
                    "Unable to refresh cache. " );
            }
        }
    }

    /**
     * Removes the role object from memory/cache.
     * @param username - the username to remove from cache
     * @see net.unicon.portal.domain.IllegalArgumentException
     * @see net.unicon.portal.domain.OperationFailedException
     */
    public static void removeRoleFromCache(long offeringID)
    throws IllegalArgumentException, OperationFailedException {
        if (isCaching) {
            membershipRoleCache.remove(new Long(offeringID));
        }
    }

    /**
     * Refresh the role object from memory/cache.
     * @param username - the username to remove from cache
     * @see net.unicon.portal.domain.IllegalArgumentException
     * @see net.unicon.portal.domain.OperationFailedException
     */
    /*
    public static void refreshRoleCache(long offeringID)
    throws IllegalArgumentException, OperationFailedException {
        if (isCaching) {
            membershipRoleCache.remove(new Long(offeringID));
            try {
                OfferingFactory offering = OfferingFactory.getOffering();
                getAllOfferings(user);
            } catch (Exception e) {
                LogService.instance().log(LogService.ERROR,
                    "Roles for the offering " + offeringID +
                    " is not currently found in the system." +
                    "Unable to refresh cache. " );
            }
        }
    }
    */
}
