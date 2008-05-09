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

package net.unicon.portal.groups.framework.eve;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.groups.GroupServiceConfiguration;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IGroupService;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.SequenceGenerator;
import org.jasig.portal.utils.SqlTransaction;

/**
 * Store for <code>AbstractGroup</code>.
 * 
 * @author Dan Ellentuck, nbolton
 */
public abstract class AbstractGroupStore implements IEntityGroupStore,
    IGroupConstants {
    private static final Log log = LogFactory.getLog(AbstractGroupStore.class);

    // Constant SQL strings:
    private static String EQ = " = ";

    private static String QUOTE = "'";

    private static String EQUALS_PARAM = EQ + "?";

    // Constant strings for GROUP table:
    private static String GROUP_TABLE = "UP_GROUP";

    private static String GROUP_TABLE_ALIAS = "T1";

    private static String GROUP_TABLE_WITH_ALIAS = GROUP_TABLE + " "
        + GROUP_TABLE_ALIAS;

    private static String GROUP_ID_COLUMN = "GROUP_ID";

    private static String GROUP_CREATOR_COLUMN = "CREATOR_ID";

    private static String GROUP_TYPE_COLUMN = "ENTITY_TYPE_ID";

    private static String GROUP_NAME_COLUMN = "GROUP_NAME";

    private static String GROUP_DESCRIPTION_COLUMN = "DESCRIPTION";

    // SQL strings for GROUP crud:
    private static String allGroupColumns;

    private static String allGroupColumnsWithTableAlias;

    private static String countAMemberGroupSql;

    private static String countMemberGroupsNamedSql;

    private static String countAMemberEntitySql;

    private static String findContainingGroupsForEntitySql;

    private static String findContainingGroupsForGroupSql;

    private static String findGroupSql;

    private static String findGroupsByCreatorSql;

    private static String findMemberGroupKeysSql;

    private static String findMemberGroupsSql;

    private static String insertGroupSql;

    private static String updateGroupSql;

    // Constant strings for MEMBERS table:
    private static String MEMBER_TABLE = "UP_GROUP_MEMBERSHIP";

    private static String MEMBER_TABLE_ALIAS = "T2";

    private static String MEMBER_TABLE_WITH_ALIAS = MEMBER_TABLE + " "
        + MEMBER_TABLE_ALIAS;

    private static String MEMBER_GROUP_ID_COLUMN = "GROUP_ID";

    private static String MEMBER_MEMBER_SERVICE_COLUMN = "MEMBER_SERVICE";

    private static String MEMBER_MEMBER_KEY_COLUMN = "MEMBER_KEY";

    private static String MEMBER_IS_GROUP_COLUMN = "MEMBER_IS_GROUP";

    private static String MEMBER_IS_ENTITY = "F";

    protected static String MEMBER_IS_GROUP = "T";

    protected static String GROUP_NODE_SEPARATOR;

    // SQL strings for group MEMBERS crud:
    private static String allMemberColumns;

    private static String deleteMembersInGroupSql;

    private static String deleteMemberGroupSql;

    private static String deleteMemberEntitySql;

    private static String insertMemberSql;

    // SQL group search string
    private static String searchGroupsPartial = "SELECT " + GROUP_ID_COLUMN
        + " FROM " + GROUP_TABLE + " WHERE " + GROUP_TYPE_COLUMN + "=? AND UPPER("
        + GROUP_NAME_COLUMN + ") LIKE ?";

    private static String searchGroups = "SELECT " + GROUP_ID_COLUMN + " FROM "
        + GROUP_TABLE + " WHERE " + GROUP_TYPE_COLUMN + "=? AND UPPER("
        + GROUP_NAME_COLUMN + ") = ?";

    private IGroupService groupService;

    /**
     * AbstractGroupStore constructor.
     */
    public AbstractGroupStore() {
        super();
        initialize();
    }

    /**
     * Get the node separator character from the GroupServiceConfiguration.
     * Default it to IGroupConstants.NODE_SEPARATOR.
     */
    private void initialize() {
        String sep;
        try {
            sep = GroupServiceConfiguration.getConfiguration()
                .getNodeSeparator();
        } catch (Exception ex) {
            sep = NODE_SEPARATOR;
        }
        GROUP_NODE_SEPARATOR = sep;
        String msg = "AbstractGroupStore.initialize(): Node separator set to "
            + sep;
        log.info(msg);
    }

    /**
     * @param conn
     *            java.sql.Connection
     * @exception java.sql.SQLException
     */
    protected static void commit(Connection conn) throws java.sql.SQLException {
        SqlTransaction.commit(conn);
    }

    /**
     * Answers if <code>IGroupMember</code> member is a member of
     * <code>group</code>.
     * 
     * @return boolean
     * @param group
     *            org.jasig.portal.groups.IEntityGroup
     * @param member
     *            org.jasig.portal.groups.IGroupMember
     */
    public boolean contains(IEntityGroup group, IGroupMember member)
        throws GroupsException {
        return (member.isGroup()) ? containsGroup(group, (IEntityGroup) member)
            : containsEntity(group, member);
    }

    private boolean containsEntity(IEntityGroup group, IGroupMember member)
        throws GroupsException {
        String groupKey = group.getLocalKey();
        String memberKey = member.getKey();
        Connection conn = RDBMServices.getConnection();
        try {
            String sql = getCountAMemberEntitySql();
            RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(
                conn, sql);
            try {
                ps.clearParameters();
                ps.setString(1, groupKey);
                ps.setString(2, memberKey);
                log.debug("AbstractGroupStore.containsEntity(): " + ps + " ("
                    + groupKey + ", " + memberKey + ")");
                ResultSet rs = ps.executeQuery();
                try {
                    return (rs.next()) && (rs.getInt(1) > 0);
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception e) {
            log.error("AbstractGroupStore.containsEntity(): " + e);
            throw new GroupsException("Problem retrieving data from store: "
                + e);
        } finally {
            RDBMServices.releaseConnection(conn);
        }
    }

    private boolean containsGroup(IEntityGroup group, IEntityGroup member)
        throws GroupsException {
        String memberService = member.getServiceName().toString();
        String groupKey = group.getLocalKey();
        String memberKey = member.getLocalKey();
        Connection conn = RDBMServices.getConnection();
        try {
            String sql = getCountAMemberGroupSql();
            RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(
                conn, sql);
            try {
                ps.clearParameters();
                ps.setString(1, groupKey);
                ps.setString(2, memberKey);
                ps.setString(3, memberService);
                log.debug("AbstractGroupStore.containsGroup(): " + ps + " ("
                    + groupKey + ", " + memberKey + ", " + memberService + ")");
                ResultSet rs = ps.executeQuery();
                try {
                    return (rs.next()) && (rs.getInt(1) > 0);
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception e) {
            log.error("AbstractGroupStore.containsGroup(): " + e);
            throw new GroupsException("Problem retrieving data from store: "
                + e);
        } finally {
            RDBMServices.releaseConnection(conn);
        }
    }

    public boolean containsGroupNamed(IEntityGroup containingGroup,
        String memberName) throws GroupsException {
        String groupKey = containingGroup.getLocalKey();
        String service = containingGroup.getServiceName().toString();

        Connection conn = RDBMServices.getConnection();
        try {
            String sql = getCountMemberGroupsNamedSql();
            RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(
                conn, sql);
            try {
                ps.clearParameters();
                ps.setString(1, groupKey);
                ps.setString(2, memberName);
                ps.setString(3, service);
                log.debug("AbstractGroupStore.containsGroupNamed(): " + ps
                    + " (" + groupKey + ", " + memberName + ", " + service
                    + ")");
                ResultSet rs = ps.executeQuery();
                try {
                    return (rs.next()) && (rs.getInt(1) > 0);
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception e) {
            log.error("AbstractGroupStore.containsGroup(): " + e);
            throw new GroupsException("Problem retrieving data from store: "
                + e);
        } finally {
            RDBMServices.releaseConnection(conn);
        }
    }

    /**
     * If this entity exists, delete it.
     * 
     * @param group
     *            org.jasig.portal.groups.IEntityGroup
     */
    public void delete(IEntityGroup group) throws GroupsException {
        if (existsInDatabase(group)) {
            try {
                primDelete(group);
            } catch (SQLException sqle) {
                throw new GroupsException("Problem deleting " + group + ": "
                    + sqle.getMessage());
            }
        }
    }

    /**
     * Answer if the IEntityGroup entity exists in the database.
     * 
     * @return boolean
     * @param group
     *            IEntityGroup
     */
    private boolean existsInDatabase(IEntityGroup group) throws GroupsException {
        IEntityGroup ug = this.find(group.getLocalKey());
        return ug != null;
    }

    /**
     * Find and return an instance of the group.
     * 
     * @param groupID
     *            the group ID
     * @return org.jasig.portal.groups.IEntityGroup
     */
    public IEntityGroup find(String groupID) throws GroupsException {
        return primFind(groupID, false);
    }

    /**
     * Find the groups that this entity belongs to.
     * 
     * @param ent
     *            the entity in question
     * @return java.util.Iterator
     */
    public java.util.Iterator findContainingGroups(IEntity ent)
        throws GroupsException {
        String memberKey = ent.getKey();
        Integer type = EntityTypes.getEntityTypeID(ent.getLeafType());
        return findContainingGroupsForEntity(memberKey, type.intValue());
    }

    /**
     * Find the groups that this group belongs to.
     * 
     * @param group
     *            org.jasig.portal.groups.IEntityGroup
     * @return java.util.Iterator
     */
    public java.util.Iterator findContainingGroups(IEntityGroup group)
        throws GroupsException {
        String memberKey = group.getLocalKey();
        String serviceName = group.getServiceName().toString();
        Integer type = EntityTypes.getEntityTypeID(group.getLeafType());
        return findContainingGroupsForGroup(serviceName, memberKey, type
            .intValue());
    }

    /**
     * Find the groups that this group member belongs to.
     * 
     * @param gm
     *            the group member in question
     * @return java.util.Iterator
     */
    public Iterator findContainingGroups(IGroupMember gm)
        throws GroupsException {
        if (gm.isGroup()) {
            IEntityGroup group = (IEntityGroup) gm;
            return findContainingGroups(group);
        } else {
            IEntity ent = (IEntity) gm;
            return findContainingGroups(ent);
        }
    }

    /**
     * Find the groups associated with this member key.
     * 
     * @param memberKey
     * @param type
     * @return java.util.Iterator
     */
    private java.util.Iterator findContainingGroupsForEntity(String memberKey,
        int type) throws GroupsException {
        java.sql.Connection conn = null;
        Collection groups = new ArrayList();
        IEntityGroup eg = null;

        try {
            conn = RDBMServices.getConnection();
            String sql = getFindContainingGroupsForEntitySql();
            RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(
                conn, sql);
            try {
                ps.setString(1, memberKey);
                ps.setInt(2, type);
                log
                    .debug("AbstractGroupStore.findContainingGroupsForEntity(): "
                        + ps
                        + " ("
                        + memberKey
                        + ", "
                        + type
                        + ", memberIsGroup = F)");
                java.sql.ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        eg = instanceFromResultSet(rs);
                        groups.add(eg);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception e) {
            log.error("AbstractGroupStore.findContainingGroupsForEntity(): "
                + e);
            throw new GroupsException("Problem retrieving containing groups: "
                + e);
        }

        finally {
            RDBMServices.releaseConnection(conn);
        }

        return groups.iterator();
    }

    /**
     * Find the groups associated with this member key.
     * 
     * @param serviceName
     * @param memberKey
     * @param type
     * @return java.util.Iterator
     */
    private java.util.Iterator findContainingGroupsForGroup(String serviceName,
        String memberKey, int type) throws GroupsException {
        java.sql.Connection conn = null;
        Collection groups = new ArrayList();
        IEntityGroup eg = null;

        try {
            conn = RDBMServices.getConnection();
            String sql = getFindContainingGroupsForGroupSql();
            RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(
                conn, sql);
            try {
                ps.setString(1, serviceName);
                ps.setString(2, memberKey);
                ps.setInt(3, type);
                log.debug("AbstractGroupStore.findContainingGroupsForGroup(): "
                    + ps + " (" + serviceName + ", " + memberKey + ", " + type
                    + ", memberIsGroup = T)");
                java.sql.ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        eg = instanceFromResultSet(rs);
                        groups.add(eg);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception e) {
            log
                .error("AbstractGroupStore.findContainingGroupsForGroup(): "
                    + e);
            throw new GroupsException("Problem retrieving containing groups: "
                + e);
        }

        finally {
            RDBMServices.releaseConnection(conn);
        }

        return groups.iterator();
    }

    /**
     * Find the <code>IEntities</code> that are members of the
     * <code>IEntityGroup</code>.
     * 
     * @param group
     *            the entity group in question
     * @return java.util.Iterator
     */
    public Iterator findEntitiesForGroup(IEntityGroup group)
        throws GroupsException {
        Collection entities = new ArrayList();
        Connection conn = null;
        String groupID = group.getLocalKey();
        Class cls = group.getLeafType();

        try {
            conn = RDBMServices.getConnection();
            Statement stmnt = conn.createStatement();
            try {

                String query = "SELECT " + MEMBER_MEMBER_KEY_COLUMN + " FROM "
                    + MEMBER_TABLE + " WHERE " + MEMBER_GROUP_ID_COLUMN
                    + " = '" + groupID + "' AND " + MEMBER_IS_GROUP_COLUMN
                    + " = '" + MEMBER_IS_ENTITY + "'";

                ResultSet rs = stmnt.executeQuery(query);
                try {
                    while (rs.next()) {
                        String key = rs.getString(1);
                        IEntity e = newEntity(cls, key);
                        entities.add(e);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                stmnt.close();
            }
        } catch (SQLException sqle) {
            log.error("Problem retrieving Entities for Group: " + group, sqle);
            throw new GroupsException("Problem retrieving Entities for Group: "
                + sqle.getMessage());
        } finally {
            RDBMServices.releaseConnection(conn);
        }

        return entities.iterator();
    }

    /**
     * Find the groups with this creatorID.
     * 
     * @param creatorID
     * @return java.util.Iterator
     */
    public java.util.Iterator findGroupsByCreator(String creatorID)
        throws GroupsException {
        java.sql.Connection conn = null;
        Collection groups = new ArrayList();
        IEntityGroup eg = null;

        try {
            conn = RDBMServices.getConnection();
            String sql = getFindGroupsByCreatorSql();
            RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(
                conn, sql);
            try {
                ps.setString(1, creatorID);
                log.debug("AbstractGroupStore.findGroupsByCreator(): " + ps);
                ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        eg = instanceFromResultSet(rs);
                        groups.add(eg);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception e) {
            log.error("AbstractGroupStore.findGroupsByCreator(): " + e);
            throw new GroupsException("Problem retrieving groups: " + e);
        }

        finally {
            RDBMServices.releaseConnection(conn);
        }

        return groups.iterator();
    }

    /**
     * Find and return an instance of the group.
     * 
     * @param groupID
     *            the group ID
     * @return org.jasig.portal.groups.ILockableEntityGroup
     */
    public ILockableEntityGroup findLockable(String groupID)
        throws GroupsException {
        return (ILockableEntityGroup) primFind(groupID, true);
    }

    /**
     * Find the keys of groups that are members of group.
     * 
     * @param group
     *            the org.jasig.portal.groups.IEntityGroup
     * @return String[]
     */
    public String[] findMemberGroupKeys(IEntityGroup group)
        throws GroupsException {
        java.sql.Connection conn = null;
        Collection groupKeys = new ArrayList();
        String groupKey = null;

        try {
            conn = RDBMServices.getConnection();
            String sql = getFindMemberGroupKeysSql();
            RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(
                conn, sql);
            try {
                ps.setString(1, group.getLocalKey());
                log.debug("AbstractGroupStore.findMemberGroupKeys(): " + ps
                    + " (" + group.getLocalKey() + ")");
                java.sql.ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        groupKey = rs.getString(1) + GROUP_NODE_SEPARATOR
                            + rs.getString(2);
                        groupKeys.add(groupKey);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception sqle) {
            log.error("AbstractGroupStore.findMemberGroupKeys(): " + sqle);
            throw new GroupsException("Problem retrieving member group keys: "
                + sqle);
        } finally {
            RDBMServices.releaseConnection(conn);
        }

        return (String[]) groupKeys.toArray(new String[groupKeys.size()]);
    }

    /**
     * Find the IUserGroups that are members of the group.
     * 
     * @param group
     *            org.jasig.portal.groups.IEntityGroup
     * @return java.util.Iterator
     */
    public Iterator findMemberGroups(IEntityGroup group) throws GroupsException {
        java.sql.Connection conn = null;
        Collection groups = new ArrayList();
        IEntityGroup eg = null;
        String serviceName = group.getServiceName().toString();
        String localKey = group.getLocalKey();

        try {
            conn = RDBMServices.getConnection();
            String sql = getFindMemberGroupsSql();
            RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(
                conn, sql);
            try {
                ps.setString(1, localKey);
                ps.setString(2, serviceName);
                log.debug("AbstractGroupStore.findMemberGroups(): " + ps + " ("
                    + localKey + ", " + serviceName + ")");
                java.sql.ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        eg = instanceFromResultSet(rs);
                        groups.add(eg);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception sqle) {
            log.error("AbstractGroupStore.findMemberGroups(): " + sqle);
            throw new GroupsException("Problem retrieving member groups: "
                + sqle);
        } finally {
            RDBMServices.releaseConnection(conn);
        }

        return groups.iterator();
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getAllGroupColumns() {

        if (allGroupColumns == null) {
            StringBuffer buff = new StringBuffer(100);
            buff.append(GROUP_ID_COLUMN);
            buff.append(", ");
            buff.append(GROUP_CREATOR_COLUMN);
            buff.append(", ");
            buff.append(GROUP_TYPE_COLUMN);
            buff.append(", ");
            buff.append(GROUP_NAME_COLUMN);
            buff.append(", ");
            buff.append(GROUP_DESCRIPTION_COLUMN);

            allGroupColumns = buff.toString();
        }
        return allGroupColumns;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getAllGroupColumnsWithTableAlias() {

        if (allGroupColumnsWithTableAlias == null) {
            StringBuffer buff = new StringBuffer(100);
            buff.append(groupAlias(GROUP_ID_COLUMN));
            buff.append(", ");
            buff.append(groupAlias(GROUP_CREATOR_COLUMN));
            buff.append(", ");
            buff.append(groupAlias(GROUP_TYPE_COLUMN));
            buff.append(", ");
            buff.append(groupAlias(GROUP_NAME_COLUMN));
            buff.append(", ");
            buff.append(groupAlias(GROUP_DESCRIPTION_COLUMN));

            allGroupColumnsWithTableAlias = buff.toString();
        }
        return allGroupColumnsWithTableAlias;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getAllMemberColumns() {
        if (allMemberColumns == null) {
            StringBuffer buff = new StringBuffer(100);

            buff.append(MEMBER_GROUP_ID_COLUMN);
            buff.append(", ");
            buff.append(MEMBER_MEMBER_SERVICE_COLUMN);
            buff.append(", ");
            buff.append(MEMBER_MEMBER_KEY_COLUMN);
            buff.append(", ");
            buff.append(MEMBER_IS_GROUP_COLUMN);

            allMemberColumns = buff.toString();
        }
        return allMemberColumns;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getCountAMemberEntitySql() {
        if (countAMemberEntitySql == null) {
            StringBuffer buff = new StringBuffer(100);
            buff.append("SELECT COUNT(*) FROM " + MEMBER_TABLE);
            buff.append(" WHERE " + MEMBER_GROUP_ID_COLUMN + EQUALS_PARAM);
            buff.append(" AND " + MEMBER_MEMBER_KEY_COLUMN + EQUALS_PARAM);
            buff.append(" AND " + MEMBER_IS_GROUP_COLUMN + EQ
                + sqlQuote(MEMBER_IS_ENTITY));
            countAMemberEntitySql = buff.toString();
        }
        return countAMemberEntitySql;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getCountAMemberGroupSql() {
        if (countAMemberGroupSql == null) {
            StringBuffer buff = new StringBuffer(100);
            buff.append("SELECT COUNT(*) FROM " + MEMBER_TABLE);
            buff.append(" WHERE " + MEMBER_GROUP_ID_COLUMN + EQUALS_PARAM);
            buff.append(" AND " + MEMBER_MEMBER_KEY_COLUMN + EQUALS_PARAM);
            buff.append(" AND " + MEMBER_MEMBER_SERVICE_COLUMN + EQUALS_PARAM);
            buff.append(" AND " + MEMBER_IS_GROUP_COLUMN + EQ
                + sqlQuote(MEMBER_IS_GROUP));
            countAMemberGroupSql = buff.toString();
        }
        return countAMemberGroupSql;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getCountMemberGroupsNamedSql() {
        if (countMemberGroupsNamedSql == null) {
            StringBuffer buff = new StringBuffer(100);
            buff.append("SELECT COUNT (*) FROM ");
            buff
                .append(GROUP_TABLE_WITH_ALIAS + ", " + MEMBER_TABLE_WITH_ALIAS);
            buff.append(" WHERE " + groupAlias(GROUP_ID_COLUMN) + EQ);
            buff.append(memberAlias(MEMBER_MEMBER_KEY_COLUMN));
            buff.append(" AND " + memberAlias(MEMBER_GROUP_ID_COLUMN)
                + EQUALS_PARAM);
            buff.append(" AND " + groupAlias(GROUP_NAME_COLUMN) + EQUALS_PARAM);
            buff.append(" AND " + memberAlias(MEMBER_MEMBER_SERVICE_COLUMN)
                + EQUALS_PARAM);
            countMemberGroupsNamedSql = buff.toString();
        }
        return countMemberGroupsNamedSql;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getDeleteGroupSql(IEntityGroup group) {
        StringBuffer buff = new StringBuffer(100);
        buff.append("DELETE FROM ");
        buff.append(GROUP_TABLE);
        buff.append(" WHERE ");
        buff.append(GROUP_ID_COLUMN + EQ + sqlQuote(group.getLocalKey()));
        return buff.toString();
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getDeleteMemberEntitySql() {
        if (deleteMemberEntitySql == null) {
            StringBuffer buff = new StringBuffer(100);
            buff.append("DELETE FROM ");
            buff.append(MEMBER_TABLE);
            buff.append(" WHERE ");
            buff.append(MEMBER_GROUP_ID_COLUMN + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(MEMBER_MEMBER_KEY_COLUMN + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(MEMBER_IS_GROUP_COLUMN + EQ
                + sqlQuote(MEMBER_IS_ENTITY));

            deleteMemberEntitySql = buff.toString();
        }
        return deleteMemberEntitySql;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getDeleteMemberGroupSql() {
        if (deleteMemberGroupSql == null) {
            StringBuffer buff = new StringBuffer(100);
            buff.append("DELETE FROM ");
            buff.append(MEMBER_TABLE);
            buff.append(" WHERE ");
            buff.append(MEMBER_GROUP_ID_COLUMN + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(MEMBER_MEMBER_SERVICE_COLUMN + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(MEMBER_MEMBER_KEY_COLUMN + EQUALS_PARAM);
            buff.append(" AND ");
            buff
                .append(MEMBER_IS_GROUP_COLUMN + EQ + sqlQuote(MEMBER_IS_GROUP));
            deleteMemberGroupSql = buff.toString();
        }
        return deleteMemberGroupSql;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getDeleteMembersInGroupSql() {
        if (deleteMembersInGroupSql == null) {
            StringBuffer buff = new StringBuffer(100);
            buff.append("DELETE FROM ");
            buff.append(MEMBER_TABLE);
            buff.append(" WHERE ");
            buff.append(GROUP_ID_COLUMN + EQ);

            deleteMembersInGroupSql = buff.toString();
        }
        return deleteMembersInGroupSql;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getDeleteMembersInGroupSql(
        IEntityGroup group) {
        return getDeleteMembersInGroupSql() + sqlQuote(group.getLocalKey());
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getFindContainingGroupsForEntitySql() {
        if (findContainingGroupsForEntitySql == null) {
            StringBuffer buff = new StringBuffer(500);
            buff.append("SELECT ");
            buff.append(getAllGroupColumnsWithTableAlias());
            buff.append(" FROM " + GROUP_TABLE_WITH_ALIAS + ", "
                + MEMBER_TABLE_WITH_ALIAS);
            buff.append(" WHERE ");
            buff.append(groupAlias(GROUP_ID_COLUMN) + EQ);
            buff.append(memberAlias(MEMBER_GROUP_ID_COLUMN));
            buff.append(" AND ");
            buff.append(memberAlias(MEMBER_MEMBER_KEY_COLUMN) + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(groupAlias(GROUP_TYPE_COLUMN) + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(memberAlias(MEMBER_IS_GROUP_COLUMN) + EQ
                + sqlQuote(MEMBER_IS_ENTITY));

            findContainingGroupsForEntitySql = buff.toString();
        }
        return findContainingGroupsForEntitySql;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getFindContainingGroupsForGroupSql() {
        if (findContainingGroupsForGroupSql == null) {
            StringBuffer buff = new StringBuffer(500);
            buff.append("SELECT ");
            buff.append(getAllGroupColumnsWithTableAlias());
            buff.append(" FROM ");
            buff.append(GROUP_TABLE_WITH_ALIAS);
            buff.append(", ");
            buff.append(MEMBER_TABLE_WITH_ALIAS);
            buff.append(" WHERE ");
            buff.append(groupAlias(GROUP_ID_COLUMN) + EQ);
            buff.append(memberAlias(MEMBER_GROUP_ID_COLUMN));
            buff.append(" AND ");
            buff.append(memberAlias(MEMBER_MEMBER_SERVICE_COLUMN)
                + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(memberAlias(MEMBER_MEMBER_KEY_COLUMN) + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(groupAlias(GROUP_TYPE_COLUMN) + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(memberAlias(MEMBER_IS_GROUP_COLUMN) + EQ
                + sqlQuote(MEMBER_IS_GROUP));

            findContainingGroupsForGroupSql = buff.toString();
        }
        return findContainingGroupsForGroupSql;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getFindGroupsByCreatorSql() {
        if (findGroupsByCreatorSql == null) {
            StringBuffer buff = new StringBuffer(200);
            buff.append("SELECT ");
            buff.append(getAllGroupColumns());
            buff.append(" FROM ");
            buff.append(GROUP_TABLE);
            buff.append(" WHERE ");
            buff.append(GROUP_CREATOR_COLUMN + EQUALS_PARAM);

            findGroupsByCreatorSql = buff.toString();
        }
        return findGroupsByCreatorSql;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getFindGroupSql() {

        if (findGroupSql == null) {
            StringBuffer buff = new StringBuffer(200);
            buff.append("SELECT ");
            buff.append(getAllGroupColumns());
            buff.append(" FROM ");
            buff.append(GROUP_TABLE);
            buff.append(" WHERE ");
            buff.append(GROUP_ID_COLUMN + EQUALS_PARAM);

            findGroupSql = buff.toString();
        }
        return findGroupSql;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getFindMemberGroupKeysSql() {
        if (findMemberGroupKeysSql == null) {
            StringBuffer buff = new StringBuffer(200);
            buff.append("SELECT ");
            buff.append(MEMBER_MEMBER_SERVICE_COLUMN + ", "
                + MEMBER_MEMBER_KEY_COLUMN);
            buff.append(" FROM ");
            buff.append(MEMBER_TABLE);
            buff.append(" WHERE ");
            buff.append(MEMBER_GROUP_ID_COLUMN + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(MEMBER_IS_GROUP_COLUMN + EQ);
            buff.append(sqlQuote(MEMBER_IS_GROUP));

            findMemberGroupKeysSql = buff.toString();
        }

        return findMemberGroupKeysSql;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getFindMemberGroupsSql() {
        if (findMemberGroupsSql == null) {
            StringBuffer buff = new StringBuffer(500);
            buff.append("SELECT ");
            buff.append(getAllGroupColumnsWithTableAlias());
            buff.append(" FROM ");
            buff.append(GROUP_TABLE + " " + GROUP_TABLE_ALIAS);
            buff.append(", ");
            buff.append(MEMBER_TABLE + " " + MEMBER_TABLE_ALIAS);
            buff.append(" WHERE ");
            buff.append(groupAlias(GROUP_ID_COLUMN) + EQ);
            buff.append(memberAlias(MEMBER_MEMBER_KEY_COLUMN));
            buff.append(" AND ");
            buff.append(memberAlias(MEMBER_IS_GROUP_COLUMN) + EQ);
            buff.append(sqlQuote(MEMBER_IS_GROUP));
            buff.append(" AND ");
            buff.append(memberAlias(MEMBER_GROUP_ID_COLUMN) + EQUALS_PARAM);
            buff.append(" AND ");
            buff.append(memberAlias(MEMBER_MEMBER_SERVICE_COLUMN)
                + EQUALS_PARAM);

            findMemberGroupsSql = buff.toString();
        }

        return findMemberGroupsSql;
    }

    /**
     * @return org.jasig.portal.groups.IGroupService
     */
    public IGroupService getGroupService() {
        return groupService;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getInsertGroupSql() {
        if (insertGroupSql == null) {
            StringBuffer buff = new StringBuffer(200);
            buff.append("INSERT INTO ");
            buff.append(GROUP_TABLE);
            buff.append(" (");
            buff.append(getAllGroupColumns());
            buff.append(") VALUES (?, ?, ?, ?, ?)");

            insertGroupSql = buff.toString();
        }
        return insertGroupSql;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getInsertMemberSql() {
        if (insertMemberSql == null) {
            StringBuffer buff = new StringBuffer(200);
            buff.append("INSERT INTO ");
            buff.append(MEMBER_TABLE);
            buff.append(" (");
            buff.append(getAllMemberColumns());
            buff.append(") VALUES (?, ?, ?, ? )");

            insertMemberSql = buff.toString();
        }
        return insertMemberSql;
    }

    /**
     * @return java.lang.String
     * @exception java.lang.Exception
     */
    protected String getNextKey() throws java.lang.Exception {
        return SequenceGenerator.instance().getNext(GROUP_TABLE);
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String getUpdateGroupSql() {
        if (updateGroupSql == null) {
            StringBuffer buff = new StringBuffer(200);
            buff.append("UPDATE ");
            buff.append(GROUP_TABLE);
            buff.append(" SET ");
            buff.append(GROUP_CREATOR_COLUMN + EQUALS_PARAM);
            buff.append(", ");
            buff.append(GROUP_TYPE_COLUMN + EQUALS_PARAM);
            buff.append(", ");
            buff.append(GROUP_NAME_COLUMN + EQUALS_PARAM);
            buff.append(", ");
            buff.append(GROUP_DESCRIPTION_COLUMN + EQUALS_PARAM);
            buff.append(" WHERE ");
            buff.append(GROUP_ID_COLUMN + EQUALS_PARAM);

            updateGroupSql = buff.toString();
        }
        return updateGroupSql;
    }

    /**
     * Find and return an instance of the group.
     * 
     * @param rs
     *            the SQL result set
     * @return org.jasig.portal.groups.IEntityGroup
     */
    protected IEntityGroup instanceFromResultSet(java.sql.ResultSet rs)
        throws SQLException, GroupsException {
        IEntityGroup eg = null;

        String key = rs.getString(1);
        String creatorID = rs.getString(2);
        Integer entityTypeID = new Integer(rs.getInt(3));
        Class entityType = EntityTypes.getEntityType(entityTypeID);
        String groupName = rs.getString(4);
        String description = rs.getString(5);

        if (key != null) {
            eg = newInstance(key, entityType, creatorID, groupName, description);
        }

        return eg;
    }

    /**
     * Find and return an instance of the group.
     * 
     * @param rs
     *            the SQL result set
     * @return org.jasig.portal.groups.ILockableEntityGroup
     */
    protected ILockableEntityGroup lockableInstanceFromResultSet(
        java.sql.ResultSet rs) throws SQLException, GroupsException {
        ILockableEntityGroup eg = null;

        String key = rs.getString(1);
        String creatorID = rs.getString(2);
        Integer entityTypeID = new Integer(rs.getInt(3));
        Class entityType = EntityTypes.getEntityType(entityTypeID);
        String groupName = rs.getString(4);
        String description = rs.getString(5);

        if (key != null) {
            eg = newLockableInstance(key, entityType, creatorID, groupName,
                description);
        }

        return eg;
    }

    /**
     *  
     */
    protected static void logNoTransactionWarning() {
        String msg = "You are running the portal on a database that does not support transactions.  "
            + "This is not a supported production environment for uPortal.  "
            + "Sooner or later, your database will become corrupt.";
        log.warn(msg);
    }

    /**
     * @return org.jasig.portal.groups.IEntity
     */
    public IEntity newEntity(Class type, String key) throws GroupsException {
        if (EntityTypes.getEntityTypeID(type) == null) {
            throw new GroupsException("Invalid group type: " + type);
        }
        return GroupService.getEntity(key, type);
    }

    /**
     * @return org.jasig.portal.groups.IEntityGroup
     */
    abstract public IEntityGroup newInstance(Class type) throws GroupsException;

    /**
     * @return org.jasig.portal.groups.IEntityGroup
     */
    abstract protected IEntityGroup newInstance(String newKey, Class newType,
        String newCreatorID, String newName, String newDescription)
        throws GroupsException;

    /**
     * @return org.jasig.portal.groups.ILockableEntityGroup
     */
    abstract protected ILockableEntityGroup newLockableInstance(String newKey,
        Class newType, String newCreatorID, String newName,
        String newDescription) throws GroupsException;

    /**
     * @return java.lang.String
     */
    private static java.lang.String groupAlias(String column) {
        return GROUP_TABLE_ALIAS + "." + column;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String memberAlias(String column) {
        return MEMBER_TABLE_ALIAS + "." + column;
    }

    /**
     * Insert the entity into the database.
     * 
     * @param group
     *            org.jasig.portal.groups.IEntityGroup
     * @param conn
     *            the database connection
     */
    private void primAdd(IEntityGroup group, Connection conn)
        throws SQLException, GroupsException {
        try {
            RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(
                conn, getInsertGroupSql());
            try {
                Integer typeID = EntityTypes.getEntityTypeID(group
                    .getLeafType());
                ps.setString(1, group.getLocalKey());
                ps.setString(2, group.getCreatorID());
                ps.setInt(3, typeID.intValue());
                ps.setString(4, group.getName());
                ps.setString(5, group.getDescription());

                log.debug("AbstractGroupStore.primAdd(): " + ps + "("
                    + group.getLocalKey() + ", " + group.getCreatorID() + ", "
                    + typeID + ", " + group.getName() + ", "
                    + group.getDescription() + ")");

                int rc = ps.executeUpdate();

                if (rc != 1) {
                    String errString = "Problem adding " + group;
                    throw new GroupsException(errString);
                }
            } finally {
                ps.close();
            }
        } catch (java.sql.SQLException sqle) {
            log.error("Error inserting an entity into the database. Group:"
                + group, sqle);
            throw sqle;
        }
    }

    /**
     * Delete this entity from the database after first deleting its
     * memberships. Exception java.sql.SQLException - if we catch a
     * SQLException, we rollback and re-throw it.
     * 
     * @param group
     *            org.jasig.portal.groups.IEntityGroup
     */
    private void primDelete(IEntityGroup group) throws SQLException {
        java.sql.Connection conn = null;
        String deleteGroupSql = getDeleteGroupSql(group);
        String deleteMembershipSql = getDeleteMembersInGroupSql(group);

        try {
            conn = RDBMServices.getConnection();
            Statement stmnt = conn.createStatement();
            setAutoCommit(conn, false);

            try {
                log.debug("AbstractGroupStore.primDelete(): "
                    + deleteMembershipSql);
                stmnt.executeUpdate(deleteMembershipSql);

                log.debug("AbstractGroupStore.primDelete(): " + deleteGroupSql);
                stmnt.executeUpdate(deleteGroupSql);
            } finally {
                stmnt.close();
            }
            commit(conn);

        } catch (SQLException sqle) {
            rollback(conn);
            throw sqle;
        } finally {
            try {
                setAutoCommit(conn, true);
            } finally {
                RDBMServices.releaseConnection(conn);
            }
        }
    }

    /**
     * Find and return an instance of the group.
     * 
     * @param groupID
     *            the group ID
     * @param lockable
     *            boolean
     * @return org.jasig.portal.groups.IEntityGroup
     */
    private IEntityGroup primFind(String groupID, boolean lockable)
        throws GroupsException {
        IEntityGroup eg = null;
        java.sql.Connection conn = null;
        try {
            conn = RDBMServices.getConnection();
            String sql = getFindGroupSql();
            RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(
                conn, sql);
            try {
                ps.setString(1, groupID);
                log.debug("AbstractGroupStore.find(): " + ps + " (" + groupID
                    + ")");
                java.sql.ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        eg = (lockable) ? lockableInstanceFromResultSet(rs)
                            : instanceFromResultSet(rs);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception e) {
            log.error("AbstractGroupStore.find(): " + e);
            throw new GroupsException("Error retrieving " + groupID + ": " + e);
        } finally {
            RDBMServices.releaseConnection(conn);
        }

        return eg;
    }

    /**
     * Update the entity in the database.
     * 
     * @param group
     *            org.jasig.portal.groups.IEntityGroup
     * @param conn
     *            the database connection
     */
    private void primUpdate(IEntityGroup group, Connection conn)
        throws SQLException, GroupsException {
        try {
            RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(
                conn, getUpdateGroupSql());

            try {
                Integer typeID = EntityTypes.getEntityTypeID(group
                    .getLeafType());

                ps.setString(1, group.getCreatorID());
                ps.setInt(2, typeID.intValue());
                ps.setString(3, group.getName());
                ps.setString(4, group.getDescription());
                ps.setString(5, group.getLocalKey());

                log.debug("AbstractGroupStore.primUpdate(): " + ps + "("
                    + group.getCreatorID() + ", " + typeID + ", "
                    + group.getName() + ", " + group.getDescription() + ", "
                    + group.getLocalKey() + ")");

                int rc = ps.executeUpdate();

                if (rc != 1) {
                    String errString = "Problem updating " + group;
                    throw new GroupsException(errString);
                }
            } finally {
                ps.close();
            }
        } catch (java.sql.SQLException sqle) {
            log.error("Error updating entity in database. Group: " + group,
                sqle);
            throw sqle;
        }
    }

    /**
     * Insert and delete group membership rows. The transaction is maintained by
     * the caller.
     * 
     * @param egi
     *            org.jasig.portal.groups.EntityGroupImpl
     * @param conn
     *            the database connection
     */
    protected void primUpdateMembers(IEveGroup g, Connection conn)
        throws java.sql.SQLException {
        String groupKey = g.getLocalKey();
        String memberKey, isGroup, serviceName = null;
        try {
            if (g.hasDeletes()) {
                List deletedGroups = new ArrayList();
                List deletedEntities = new ArrayList();
                IGroupMember[] deletes = g.getRemovedMembers();
                for (int i = 0; i < deletes.length; i++) {
                    if (deletes[i].isGroup()) {
                        deletedGroups.add(deletes[i]);
                    } else {
                        deletedEntities.add(deletes[i]);
                    }
                }

                if (!deletedGroups.isEmpty()) {
                    RDBMServices.PreparedStatement psDeleteMemberGroup = new RDBMServices.PreparedStatement(
                        conn, getDeleteMemberGroupSql());

                    try {
                        for (Iterator groups = deletedGroups.iterator(); groups
                            .hasNext();) {
                            IEntityGroup removedGroup = (IEntityGroup) groups
                                .next();
                            memberKey = removedGroup.getLocalKey();
                            isGroup = MEMBER_IS_GROUP;
                            serviceName = removedGroup.getServiceName()
                                .toString();

                            psDeleteMemberGroup.setString(1, groupKey);
                            psDeleteMemberGroup.setString(2, serviceName);
                            psDeleteMemberGroup.setString(3, memberKey);

                            log
                                .debug("AbstractGroupStore.primUpdateMembers(): "
                                    + psDeleteMemberGroup
                                    + "("
                                    + groupKey
                                    + ", "
                                    + serviceName
                                    + ", "
                                    + memberKey
                                    + ", isGroup = T)");

                            psDeleteMemberGroup.executeUpdate();
                        } // for
                    } // try
                    finally {
                        psDeleteMemberGroup.close();
                    }
                } // if ( ! deletedGroups.isEmpty() )

                if (!deletedEntities.isEmpty()) {
                    RDBMServices.PreparedStatement psDeleteMemberEntity = new RDBMServices.PreparedStatement(
                        conn, getDeleteMemberEntitySql());

                    try {
                        for (Iterator entities = deletedEntities.iterator(); entities
                            .hasNext();) {
                            IGroupMember removedEntity = (IGroupMember) entities
                                .next();
                            memberKey = removedEntity
                                .getUnderlyingEntityIdentifier().getKey();
                            isGroup = MEMBER_IS_ENTITY;

                            psDeleteMemberEntity.setString(1, groupKey);
                            psDeleteMemberEntity.setString(2, memberKey);

                            log
                                .debug("AbstractGroupStore.primUpdateMembers(): "
                                    + psDeleteMemberEntity
                                    + "("
                                    + groupKey
                                    + ", " + memberKey + ", " + "isGroup = F)");

                            psDeleteMemberEntity.executeUpdate();
                        } // for
                    } // try
                    finally {
                        psDeleteMemberEntity.close();
                    }
                } //  if ( ! deletedEntities.isEmpty() )

            }

            if (g.hasAdds()) {
                RDBMServices.PreparedStatement psAdd = new RDBMServices.PreparedStatement(
                    conn, getInsertMemberSql());

                try {
                    IGroupMember[] adds = g.getAddedMembers();
                    for (int i = 0; i < adds.length; i++) {
                        IGroupMember addedGM = adds[i];
                        memberKey = addedGM.getKey();
                        if (addedGM.isGroup()) {
                            IEntityGroup addedGroup = (IEntityGroup) addedGM;
                            isGroup = MEMBER_IS_GROUP;
                            serviceName = addedGroup.getServiceName()
                                .toString();
                            memberKey = addedGroup.getLocalKey();
                        } else {
                            isGroup = MEMBER_IS_ENTITY;
                            serviceName = g.getServiceName().toString();
                            memberKey = addedGM.getUnderlyingEntityIdentifier()
                                .getKey();
                        }

                        psAdd.setString(1, groupKey);
                        psAdd.setString(2, serviceName);
                        psAdd.setString(3, memberKey);
                        psAdd.setString(4, isGroup);

                        log.debug("AbstractGroupStore.primUpdateMembers(): "
                            + psAdd + "(" + groupKey + ", " + memberKey + ", "
                            + isGroup + ")");

                        psAdd.executeUpdate();
                    }
                } finally {
                    psAdd.close();
                }
            }

        } catch (SQLException sqle) {
            log.error("Error inserting/deleting membership rows.", sqle);
            throw sqle;
        }
    }

    /**
     * @param conn
     *            java.sql.Connection
     * @exception java.sql.SQLException
     */
    protected static void rollback(Connection conn)
        throws java.sql.SQLException {
        SqlTransaction.rollback(conn);
    }

    public EntityIdentifier[] searchForGroups(String query, int method,
        Class leaftype) throws GroupsException {
        EntityIdentifier[] r = new EntityIdentifier[0];
        ArrayList ar = new ArrayList();
        Connection conn = null;
        RDBMServices.PreparedStatement ps = null;
        int type = EntityTypes.getEntityTypeID(leaftype).intValue();
        //System.out.println("Checking out groups of leaftype
        // "+leaftype.getName()+" or "+type);

        query = query.toUpperCase();
        try {
            conn = RDBMServices.getConnection();
            switch (method) {
            case IS:
                ps = new RDBMServices.PreparedStatement(conn,
                    AbstractGroupStore.searchGroups);
                break;
            case STARTS_WITH:
                query = query + "%";
                ps = new RDBMServices.PreparedStatement(conn,
                    AbstractGroupStore.searchGroupsPartial);
                break;
            case ENDS_WITH:
                query = "%" + query;
                ps = new RDBMServices.PreparedStatement(conn,
                    AbstractGroupStore.searchGroupsPartial);
                break;
            case CONTAINS:
                query = "%" + query + "%";
                ps = new RDBMServices.PreparedStatement(conn,
                    AbstractGroupStore.searchGroupsPartial);
                break;
            default:
                throw new GroupsException("Unknown search type");
            }
            ps.clearParameters();
            ps.setInt(1, type);
            ps.setString(2, query);
            ResultSet rs = ps.executeQuery();
            //System.out.println(ps.toString());
            while (rs.next()) {
                //System.out.println("result");
                ar.add(new EntityIdentifier(rs.getString(1),
                    org.jasig.portal.EntityTypes.GROUP_ENTITY_TYPE));
            }
            ps.close();
        } catch (Exception e) {
            log.error("RDBMChannelDefSearcher.searchForEntities(): " + ps, e);
        } finally {
            RDBMServices.releaseConnection(conn);
        }
        return (EntityIdentifier[]) ar.toArray(r);
    }

    /**
     * @param conn java.sql.Connection
     * @param newValue boolean
     * @exception java.sql.SQLException The exception description.
     */
    protected static void setAutoCommit(Connection conn, boolean newValue)
        throws java.sql.SQLException {
        SqlTransaction.setAutoCommit(conn, newValue);
    }

    /**
     * @param newGroupService org.jasig.portal.groups.IGroupService
     */
    public void setGroupService(IGroupService newGroupService) {
        groupService = newGroupService;
    }

    /**
     * @return java.lang.String
     */
    private static java.lang.String sqlQuote(Object o) {
        return QUOTE + o + QUOTE;
    }

    /**
     * Commit this entity AND ITS MEMBERSHIPS to the underlying store.
     * @param group org.jasig.portal.groups.IEntityGroup
     */
    public void update(IEntityGroup group) throws GroupsException {
        Connection conn = null;
        try {
            conn = RDBMServices.getConnection();
            setAutoCommit(conn, false);

            try {
                try {
                    primUpdate(group, conn);
                } catch (GroupsException ge) {
                    primAdd(group, conn);
                }
                primUpdateMembers((IEveGroup) group, conn);
                commit(conn);
            } catch (Exception ex) {
                rollback(conn);
                log.error(ex.getMessage(), ex);
                throw new GroupsException("Problem updating " + this + ex);
            }
        }

        catch (SQLException sqlex) {
            throw new GroupsException(sqlex.getMessage());
        }

        finally {
            try {
                setAutoCommit(conn, true);
            } catch (SQLException sqle) {
                throw new GroupsException(sqle.getMessage());
            } finally {
                RDBMServices.releaseConnection(conn);
            }
        }
    }

    /**
     * Insert and delete group membership rows inside a transaction.
     * @param eg org.jasig.portal.groups.IEntityGroup
     */
    public void updateMembers(IEntityGroup eg) throws GroupsException {
        Connection conn = null;
        IEveGroup g = (IEveGroup) eg;
        if (g.hasDirtyMembers())
            try {
                conn = RDBMServices.getConnection();
                setAutoCommit(conn, false);

                try {
                    primUpdateMembers(g, conn);
                    commit(conn);
                } catch (SQLException sqle) {
                    rollback(conn);
                    throw new GroupsException(
                        "Problem updating memberships for " + g + " "
                            + sqle.getMessage());
                }
            }

            catch (SQLException sqlex) {
                throw new GroupsException(sqlex.getMessage());
            }

            finally {
                try {
                    setAutoCommit(conn, true);
                } catch (SQLException sqle) {
                    throw new GroupsException(sqle.getMessage());
                } finally {
                    RDBMServices.releaseConnection(conn);
                }
            }
    }
}
