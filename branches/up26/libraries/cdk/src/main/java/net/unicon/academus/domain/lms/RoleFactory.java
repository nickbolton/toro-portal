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

import net.unicon.academus.cache.DomainCacheRequestHandler;
import net.unicon.academus.cache.DomainCacheRequestHandlerFactory;
import net.unicon.academus.common.AcademusException;
import net.unicon.academus.common.properties.*;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.portal.domain.*;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.permissions.PermissionsService;
import net.unicon.sdk.properties.*;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.GroupFactory;
import net.unicon.portal.groups.UniconGroupService;
import net.unicon.portal.groups.UniconGroupServiceFactory;
import net.unicon.portal.groups.UniconGroupsException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.unicon.portal.util.db.AcademusDBUtil;


/**
 * Provides static methods to create and access user roles.
 * <code>RoleFactory</code> is not instantiable.
 */
public final class RoleFactory {
    private static Map roleCacheById = new HashMap();
    private static Map roleCacheByGroup = new HashMap();
    private static final boolean isCaching = UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getPropertyAsBoolean("net.unicon.academus.domain.lms.isCaching");
    private static final long defaultSUOfferingRole = UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getPropertyAsLong("net.unicon.academus.domain.lms.Role.defaultSUOfferingRole");
    private static final long defaultOfferingRole = UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getPropertyAsLong("net.unicon.academus.domain.lms.Role.defaultOfferingRole");

    // This defines where a system role group will be created
    private static IGroup defaultSystemRoleGroup = null;

    // This defines where a system-wide offering role group will be created
    private static IGroup defaultOfferingRoleGroup = null;

    private static final boolean isMultiBox = UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getPropertyAsBoolean("net.unicon.portal.Academus.multipleBoxConfig");
    private static DomainCacheRequestHandler handler = null;

    static {
        try {
            UniconGroupService gs = UniconGroupServiceFactory.getService();
            IGroup personRoot = gs.getRootGroup();
            try {
                String defaultSystemRoleGroupPath = UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getProperty("net.unicon.academus.domain.lms.Role.defaultSystemRoleGroupPath");
                defaultSystemRoleGroup =
                    getDefaultGroup(defaultSystemRoleGroupPath);
            } catch (Exception e) {
                e.printStackTrace();

                // default to the root (Everyone) group
                defaultSystemRoleGroup = personRoot;
            }

            try {
                String defaultOfferingRoleGroupPath = UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getProperty("net.unicon.academus.domain.lms.Role.defaultOfferingRoleGroupPath");
                defaultOfferingRoleGroup =
                    getDefaultGroup(defaultOfferingRoleGroupPath);
            } catch (Exception e) {
                e.printStackTrace();

                // default to the root (Everyone) group
                defaultOfferingRoleGroup = personRoot;
            }

            System.out.println("defaultSystemRoleGroup: " + defaultSystemRoleGroup.getKey());
            System.out.println("defaultOfferingRoleGroup: " + defaultOfferingRoleGroup.getKey());

            if (isMultiBox) {
                handler = DomainCacheRequestHandlerFactory.getHandler();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // created this method that wraps the recursive getDefaultGroup() so
    // the original groupPath can be logged on an exception
    private static IGroup getDefaultGroup(String groupPath)
    throws Exception {
        UniconGroupService gs = UniconGroupServiceFactory.getService();

        StringTokenizer st = new StringTokenizer(groupPath, ",");
        String[] path = new String[st.countTokens()];
        int i = 0;

        while (st.hasMoreTokens()) {
            path[i++] = st.nextToken();
        }

        return gs.getGroupByPath(path);
    }

    private RoleFactory() {}

    public static Role createRole(String name, int roleType, User creator)
    throws IllegalArgumentException, OperationFailedException,
    ItemNotFoundException {
        if (name == null) {
            String msg = "The name can't be null.";
            throw new IllegalArgumentException(msg);
        }
        return doCreateRole(-1, name, null, roleType, creator);
    }

    public static Role createRole(String name,
        Offering offering, int roleType, User creator)
    throws IllegalArgumentException, OperationFailedException,
    ItemNotFoundException {
        if (name == null) {
            String msg = "The name can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (offering == null) {
            String msg = "The offering can't be null.";
            throw new IllegalArgumentException(msg);
        }
        return doCreateRole(-1, name, offering, roleType, creator);
    }
    public static Role createRole(long roleId, String name,
        int roleType, User creator)
    throws IllegalArgumentException, OperationFailedException,
    ItemNotFoundException {
        if (name == null) {
            String msg = "The name can't be null.";
            throw new IllegalArgumentException(msg);
        }
        return doCreateRole(roleId, name, null, roleType, creator);
    }

    static public boolean isExistingName(String name, int roleType,
        int excludingType)
        throws IllegalArgumentException, OperationFailedException,
        ItemNotFoundException {

        if (name == null) {
            String msg = "The label can't be null.";
            throw new IllegalArgumentException(msg);
        }
        Role role = null;
        boolean rtn = false;
        StringBuffer sql = new StringBuffer();
        sql.append("Select role_id from Role where upper(label) = upper(?)");
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Try to find the role.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setString(i++, name);
            rs = pstmt.executeQuery();
            while (!rtn && rs.next()) {
                role = getRole(rs.getLong("role_id"));
                rtn = role.isType(roleType) && !role.isType(excludingType);
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation isExistingName(name) ");
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

    static private Role doCreateRole(long roleId, String name,
        Offering offering, int roleType, User creator)
    throws IllegalArgumentException, OperationFailedException,
    ItemNotFoundException {
        // The rtn.
        Role rtn = null;
        // SQL -- should return zero rows.
        StringBuffer sql = new StringBuffer();
        if (roleId < 1) {
            sql.append("Insert into ROLE(Offering_Id, Label, type, group_id) ");
            sql.append("values(?, ?, ?, ?)");
        } else {
            sql.append("Insert into ROLE(role_id, Offering_Id, ");
            sql.append("Label, type, group_id) values(?, ?, ?, ?, ?)");
        }
        // db objects.
        Connection conn = null;
        ResultSet rs = null;

        IGroup group = null;
        long offeringID = -1;
        if (offering != null) {
            offeringID = offering.getId();
        }
        try {
            PreparedStatement pstmt = null;
            // create the associated group object
            UniconGroupService gs = UniconGroupServiceFactory.getService();
            IGroup parentGroup = null;
            if (offering != null) {
                parentGroup = offering.getGroup();
            } else {
                if ((roleType & Role.OFFERING) != 0) {
                    parentGroup = defaultOfferingRoleGroup;
                } else {
                    parentGroup = defaultSystemRoleGroup;
                }
            }
            group = gs.createGroup(creator, parentGroup,
                Role.groupPrefix+name, Role.groupPrefix+name);

            // Try to find the role.
            conn = AcademusDBUtil.getDBConnection();

            try {
            pstmt = conn.prepareStatement(sql.toString());
            int i=1;
            if (roleId >= 1) {
                pstmt.setLong(i++, roleId);
            }
            pstmt.setLong(i++, offeringID);
            pstmt.setString(i++, name);
            pstmt.setInt(i++, roleType);
            pstmt.setLong(i++, group.getGroupId());
            pstmt.executeUpdate();
            } finally {
                try {
                    if (pstmt != null) pstmt.close();
                    pstmt = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (roleId < 1) {
                sql = new StringBuffer();
                sql.append("select Role_Id from ROLE ");
                sql.append("where Offering_Id = ? and Label = ? and type = ?");

                try {
                pstmt = conn.prepareStatement(sql.toString());
                int i=1;
                pstmt.setLong(i++, offeringID);
                pstmt.setString(i++, name);
                pstmt.setInt(i++, roleType);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    // Create the new role.
                    long Id = rs.getLong("Role_Id");
                    rtn = new Role(Id, group, offering, name, roleType);
                } else {
                    StringBuffer msg = new StringBuffer();
                    msg.append("Operation createOfferingRole failed ");
                    msg.append("with the following message:\n");
                    msg.append("Failed to lookup role with ");
                    msg.append("offering_id: " + offeringID);
                    msg.append(" label: " + name);
                    throw new OperationFailedException(msg.toString());
                }

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
                }
            } else {
                rtn = new Role(roleId, group, offering, name, roleType);
            }
            PermissionsService.instance().createPermissions(rtn.getGroup());
        } catch (Exception e) {
            e.printStackTrace();
            StringBuffer msg = new StringBuffer();
            msg.append("Operation createOfferingRole failed ");
            msg.append("for (").append("role:").append(roleId).append(", ");
            msg.append("name:").append(name).append(", ");
            msg.append("off:").append(offering.getId()).append(", ");
            msg.append("type:").append(roleType).append(", ");
            msg.append("creator:").append(creator.getUsername());
            msg.append(" with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            AcademusDBUtil.safeReleaseDBConnection(conn);
            conn = null;
        }
        if (isCaching) {
            updateCache(rtn);

            if (handler != null) {
                try {
                    handler.broadcastRefreshRole((int) rtn.getId());
                } catch (AcademusException ae) {
                    ae.printStackTrace();
                }
            }
        }
        return rtn;
    }

    public static Role getUserRoleForOfferingChannel(User user, ChannelClass cc)
    throws ItemNotFoundException, OperationFailedException{
        if (user == null) {
            String msg = "The user can't be null.";
            throw new IllegalArgumentException(msg);
        }

        if (cc == null) {
            String msg = "The channel class can't be null.";
            throw new IllegalArgumentException(msg);
        }

        Role role = null;

        if (ChannelType.OFFERING.equals(cc.getChannelType())) {
            Offering offering =
                user.getContext().getCurrentOffering(TopicType.ACADEMICS);
            List allOfferings = Memberships.getAllOfferings(user);

            // If the user is not enrolled in this offering anymore
            // default him to observer permissions.
            if (allOfferings == null || !allOfferings.contains(offering)) {
                offering = null;
            }

            // Super-users could be given any offering role.
            // It really doesn't matter since they have ALL permissions.
            if (user.isSuperUser()) {
                role = RoleFactory.getRole(defaultSUOfferingRole);
            } else {
                if (offering == null) {
                    // If the user has no memberships,
                    // return Observer permissions
                    role = RoleFactory.getRole(defaultOfferingRole);
                } else {
                    role = Memberships.getRole(user, offering);
                }
            }
        }
        return role;
    }

    public static List getOfferingRoles(Offering offering)
    throws ItemNotFoundException, OperationFailedException{

        if (offering == null) {
            String msg = "The offering can't be null.";
            throw new IllegalArgumentException(msg);
        }
        List roleList = new ArrayList();

        StringBuffer sql = new StringBuffer();
        sql.append("Select Role_Id, type from role ");
        sql.append("where (offering_id = ? or offering_id = ?) ");
        sql.append("order by label");

        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Access the data.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setInt(i++, (int) offering.getId());
            pstmt.setInt(i++, -1);
            long roleID = 0;
            String label      = null;
            Role role         = null;

            rs = pstmt.executeQuery();
            while (rs.next() ) {
                role = getRole(rs.getLong("role_id"));
                if ((rs.getInt("type") & Role.OFFERING) == 0) continue;
                // If is an offering-wide role. Remove any system-wide
                // roles that exist
                if (!role.isSystemWide()) {
                    Iterator itr = roleList.iterator();
                    List removeList = new ArrayList();
                    while (itr != null && itr.hasNext()) {
                        Role checkRole = (Role)itr.next();
                        // We need to compare the role types which are only
                        // bits one and two. So ORing each with the USER_DEFINED
                        // bit will allow us to compare the role type.
                        if (checkRole.isSystemWide()                        &&
                            ((checkRole.getRoleType()|Role.USER_DEFINED) ==
                             (role.getRoleType()|Role.USER_DEFINED)) &&
                            (checkRole.getLabel().equals(role.getLabel()))) {
                            removeList.add(checkRole);
                        }
                    }
                    itr = removeList.iterator();
                    while (itr != null && itr.hasNext()) {
                        roleList.remove(itr.next());
                    }
                }
                roleList.add(role);
            }

        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getOfferingRoles(offering, roleType) failed ");
            msg.append("with the following message:\n");
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

        return roleList;
    }
    public static List getDefaultRoles(int roleType)
    throws ItemNotFoundException, OperationFailedException{
        List roleList = new ArrayList();

        StringBuffer sql = new StringBuffer();
        sql.append("Select Role_Id, type from Role ");
        sql.append("where offering_id = ?");

        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Access the data.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setInt(i++, -1);
            rs = pstmt.executeQuery();

            long roleID = 0;
            Role role         = null;

            while (rs.next() ) {
                if ( (rs.getInt("type") & roleType) != 0 ) {
                    role = getRole(rs.getLong("role_id"));
                    roleList.add(role);
                }
            }

        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getDefaultRoles(roleType) failed ");
            msg.append("with the following message:\n");
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

        return roleList;
    }
    public static Role getDefaultRole(String label, int roleType)
    throws ItemNotFoundException, OperationFailedException {
        return getRole(label, roleType, null);
    }
    public static Role getRole(String label, int roleType, Offering offering)
    throws ItemNotFoundException, OperationFailedException{
        int roleId;
        StringBuffer sql = new StringBuffer();
        sql.append("Select Role_Id, type from Role ");
        sql.append("where Label = ? and offering_id = ?");

        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Access the data.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;

            pstmt.setString(i++, label);
            if (offering == null) {
                pstmt.setLong(i++, -1L);
            } else {
                pstmt.setLong(i++, offering.getId());
            }
            rs = pstmt.executeQuery();
            if (!rs.next()) {
                StringBuffer msg = new StringBuffer();
                msg.append("Method RoleFactory.getDefaultRole ");
                msg.append("could not find role with role label");
                msg.append(label + "").append(" and RoleType ");
                msg.append(roleType + "");
                throw new ItemNotFoundException(msg.toString());
            }
            if ( (rs.getInt("type") & roleType) == 0 ) {
                StringBuffer msg = new StringBuffer();
                msg.append("Method RoleFactory.getDefaultRole ");
                msg.append("could not find role with role label");
                msg.append(label + "").append(" and RoleType ");
                msg.append(roleType + "");
                throw new ItemNotFoundException(msg.toString());
            }
            roleId = rs.getInt("Role_id");
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getDefaultRole(label, roleType) failed ");
            msg.append("with the following message:\n");
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

        return doGetRole(roleId);
    }

    /**
     * Removes the role object from memory/cache.
     * @param roleID - the role id to remove from cache
     *
     * @see net.unicon.portal.domain.IllegalArgumentException
     * @see net.unicon.portal.domain.OperationFailedException
     */
    public static void removeRoleFromCache(long roleID)
    throws IllegalArgumentException, OperationFailedException {
        if (isCaching) {
            Role role = (Role)roleCacheById.get(new Long(roleID));
            if (role != null) {
                roleCacheById.remove(new Long(roleID));
                roleCacheByGroup.remove(role.getGroup());
            }
        }
    }

    private static void updateCache(Role role) {
        roleCacheById.put(new Long(role.getId()), role);
        roleCacheByGroup.put(role.getGroup(), role);
    }

    /**
     * Refreshed the role object from memory/cache.
     * @param roleID - the role id to remove from cache
     *
     * @see net.unicon.portal.domain.IllegalArgumentException
     * @see net.unicon.portal.domain.ItemNotFoundException
     * @see net.unicon.portal.domain.OperationFailedException
     */
    public static void refreshRoleCache(long roleID)
    throws IllegalArgumentException, OperationFailedException, ItemNotFoundException  {
        if (isCaching) {
            removeRoleFromCache(roleID);
            getRole(roleID);
        }
    }
    public static Role getRole(long roleId)
    throws ItemNotFoundException, OperationFailedException {
        return doGetRole(roleId);
    }
    public static void deleteRole(Role role)
    throws IllegalArgumentException, OperationFailedException {
        if (role == null) {
            String msg = "The role object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!role.isUserDefinedRole()) {
            String msg = "Cannot delete default roles. Only user-defined " +
                "roles may be deleted.";
            throw new IllegalArgumentException(msg);
        }
        StringBuffer sql = new StringBuffer();
        sql.append("delete from role ");
        sql.append("where role_id = ?");

        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            // Access the data.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;

            pstmt.setLong(i++, role.getId());
            pstmt.executeUpdate();

            // now delete the associated group object
            role.getGroup().delete();
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getDefaultRole(label, roleType) failed ");
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
            removeRoleFromCache(role.getId());
            if (handler != null) {
                try {
                    handler.broadcastRemoveRole((int) role.getId());
                } catch (AcademusException ae) {
                    ae.printStackTrace();
                }
            }
        }
    }

    public static void removeRoles(Offering offering)
    throws IllegalArgumentException, OperationFailedException,
    ItemNotFoundException {
        if (offering == null) {
            String msg = "The offering object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        StringBuffer sql = new StringBuffer();
        sql.append("delete from role ");
        sql.append("where offering_id = ?");

        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            // Access the data.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;

            pstmt.setLong(i++, offering.getId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getDefaultRole(label, roleType) failed ");
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
            Iterator itr = roleCacheById.keySet().iterator();
            while (itr != null && itr.hasNext()) {
                Long roleId = (Long)itr.next();
                Role role = getRole(roleId.longValue());
                Offering off = role.getOffering();
                if (off != null && off.getId() == offering.getId()) {
                    removeRoleFromCache(roleId.longValue());

                    if (handler != null) {
                        try {
                            handler.broadcastRefreshRole(roleId.intValue());
                        } catch (AcademusException ae) {
                            ae.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    private static Role doGetRole(long roleId)
    throws ItemNotFoundException, OperationFailedException {
        if (isCaching) {
            synchronized (roleCacheById) {
                return _doGetRole(roleId);
            }
        }
        return _doGetRole(roleId);
    }
    private static Role _doGetRole(long roleId)
    throws ItemNotFoundException, OperationFailedException {
        // The rtn.
        Long key = new Long(roleId);
        Role rtn = null;
        if (isCaching) {
            rtn = (Role) roleCacheById.get(key);
            if (rtn != null) return rtn;
        }
        rtn = getRoleFromDB(roleId);
        if (isCaching) {
            updateCache(rtn);
        }
        return rtn;
    }

    private static Role getRoleFromDB (long roleId)
    throws ItemNotFoundException, OperationFailedException {
        // The rtn.
        Long key = new Long(roleId);
        Role rtn = null;
        // SQL -- should return exactly one row.
        StringBuffer sql = new StringBuffer();
        sql.append("Select group_id, Label, offering_id, type from role ");
        sql.append("where Role_Id = ? ");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Access the data.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, roleId);
            rs = pstmt.executeQuery();
            if (!rs.next()) {
                StringBuffer msg = new StringBuffer();
                msg.append("Method RoleFactory.getRole ");
                msg.append("could not find role with roleId=");
                msg.append(roleId + "");

                throw new ItemNotFoundException(msg.toString());
            }
            IGroup group = GroupFactory.getGroup(rs.getLong("group_id"));
            String label = rs.getString("Label");
            long offeringId = rs.getLong("offering_id");
            int type = rs.getInt("type");
            Offering offering = null;

            if (offeringId >= 0) {
                offering = OfferingFactory.getOffering(offeringId);
            }
            rtn = new Role(roleId, group, offering, label, type);
        } catch (ItemNotFoundException infe) {
            throw infe;
        } catch (Exception e) {
        e.printStackTrace();
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getRole(roleId) failed ");
            msg.append("with the following message:\n");
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

    public static Role getRole(IGroup group)
    throws ItemNotFoundException, OperationFailedException {
        return doGetRole(group);
    }

    private static Role doGetRole(IGroup group)
    throws ItemNotFoundException, OperationFailedException {
        if (isCaching) {
            synchronized (roleCacheByGroup) {
                return _doGetRole(group);
            }
        }
        return _doGetRole(group);
    }

    private static Role _doGetRole(IGroup group)
    throws ItemNotFoundException, OperationFailedException {
        // The rtn.
        Role rtn = null;
        if (isCaching) {
            rtn = (Role) roleCacheByGroup.get(group);
            if (rtn != null) return rtn;
        }
        rtn = getRoleFromDB(group);
        // no need to cache it as it's cached from the getRole(long) method
        return rtn;
    }

    static Role getRoleFromDB(IGroup group)
    throws ItemNotFoundException, OperationFailedException {
        // The rtn.
        Role rtn = null;
        // SQL -- should return exactly one row.
        StringBuffer sql = new StringBuffer();
        sql.append("Select role_id from role ");
        sql.append("where group_id = ? ");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Access the data.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, group.getGroupId());
            rs = pstmt.executeQuery();
            if (!rs.next()) {
                StringBuffer msg = new StringBuffer();
                msg.append("Method RoleFactory.getRole ");
                msg.append("could not find role with groupId=");
                msg.append(group.getGroupId() + "");

                throw new ItemNotFoundException(msg.toString());
            }
            long roleId = rs.getLong("role_id");
            rtn = getRole(roleId);
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getRole(IGroup) failed ");
            msg.append("with the following message:\n");
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

    static boolean roleExistsInContext(String label, Role sibling)
                                            throws IllegalArgumentException,
                                            OperationFailedException {
        if (label == null) {
            String msg = "The label can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (sibling == null) {
            String msg = "The sibling role can't be null.";
            throw new IllegalArgumentException(msg);
        }
        // The rtn.
        boolean rtn = false;
        // SQL
        StringBuffer sql = new StringBuffer();
        sql.append("Select Label, Type from Role ");
        sql.append("where Offering_Id in ");
        sql.append("(Select distinct Offering_Id from Role ");
        sql.append("where Role_Id = ? or Offering_Id = -1)");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Try to find a match.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, sibling.getId());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                if (rs.getString("Label").equalsIgnoreCase(label)
                            && ((rs.getInt("Type") & sibling.getRoleType()) == 0)) {
                    rtn = true;
                }
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation roleExistsInContext(label, sibling) ");
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
    public static void persist(Role obj)
    throws OperationFailedException, ItemNotFoundException {
        // SQL -- should return exactly one row.
        StringBuffer sql = new StringBuffer();
        sql.append("Update ROLE set Label = ? ");
        sql.append("where Role_Id = ?");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            // Persist the role.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i=1;
            pstmt.setString(i++, obj.getLabel());
            pstmt.setLong(i++, obj.getId());
            pstmt.execute();

            if (isCaching && handler != null) {
                try {
                    handler.broadcastRefreshRole((int) obj.getId());
                } catch (AcademusException ae) {
                    ae.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            StringBuffer msg = new StringBuffer();
            msg.append("Operation persistRole failed ");
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
}
