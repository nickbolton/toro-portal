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
package net.unicon.portal.groups.framework.initializing;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

import org.jasig.portal.PropertiesManager;
import org.jasig.portal.groups.*;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.LogService;
import org.jasig.portal.services.SequenceGenerator;
import org.jasig.portal.utils.SqlTransaction;

/**
 * Store for <code>UniconEntityGroup</code>.
 * 
 * @author nbolton
 */
public class UniconRDBMEntityGroupStore implements IEntityGroupStore, IGroupConstants {
   private static UniconRDBMEntityGroupStore singleton;

   // Constant strings for GROUP table:
   private static String GROUP_TABLE = "UP_GROUP";
   private static String GROUP_TABLE_ALIAS = "T1";
   private static String GROUP_ID_COLUMN = "GROUP_ID";
   private static String GROUP_CREATOR_COLUMN = "CREATOR_ID";
   private static String GROUP_TYPE_COLUMN = "ENTITY_TYPE_ID";
   private static String GROUP_NAME_COLUMN = "GROUP_NAME";
   private static String GROUP_DESCRIPTION_COLUMN = "DESCRIPTION";

   // SQL strings for GROUP crud:
   private static String allGroupColumns;
   private static String allGroupColumnsWithTableAlias;
   private static String findContainingGroupsForEntitySql;
   private static String findContainingGroupsForGroupSql;
   private static String findGroupSql;
   private static String findGroupsByCreatorSql;
   private static String findMemberGroupKeysSql;
   private static String findMemberGroupSql;
   private static String findMemberGroupsSql;
   private static String insertGroupSql;
   private static String updateGroupSql;

   // Constant strings for MEMBERS table:
   private static String MEMBER_TABLE = "UP_GROUP_MEMBERSHIP";
   private static String MEMBER_TABLE_ALIAS = "T2";
   private static String MEMBER_GROUP_ID_COLUMN = "GROUP_ID";
   private static String MEMBER_MEMBER_SERVICE_COLUMN = "MEMBER_SERVICE";
   private static String MEMBER_MEMBER_KEY_COLUMN = "MEMBER_KEY";
   private static String MEMBER_IS_GROUP_COLUMN = "MEMBER_IS_GROUP";
   private static String MEMBER_IS_ENTITY = "F";
   private static String MEMBER_IS_GROUP = "T";
   private static String GROUP_NODE_SEPARATOR;

   // SQL strings for group MEMBERS crud:
   private static String allMemberColumns;
   private static String deleteMembersInGroupSql;
   private static String deleteMemberGroupSql;
   private static String deleteMemberEntitySql;
   private static String insertMemberSql;

   // SQL group search string
   private static String searchGroupsPartial = "SELECT "+GROUP_ID_COLUMN+" FROM "+GROUP_TABLE+" WHERE "+GROUP_TYPE_COLUMN+"=? AND UPPER("+GROUP_NAME_COLUMN+") LIKE ?";
   private static String searchGroups = "SELECT "+GROUP_ID_COLUMN+" FROM "+GROUP_TABLE+" WHERE "+GROUP_TYPE_COLUMN+"=? AND UPPER("+GROUP_NAME_COLUMN+") = ?";

   private IGroupService groupService;
   
/**
 * UniconRDBMEntityGroupStore constructor.
 */
public UniconRDBMEntityGroupStore()
{
   super();
   initialize();
}
/**
 * @param conn
 *            java.sql.Connection
 * @exception java.sql.SQLException
 */
protected static void commit(Connection conn) throws java.sql.SQLException
{
   SqlTransaction.commit(conn);
}
/**
 * If this entity exists, delete it.
 * 
 * @param group
 *            org.jasig.portal.groups.IEntityGroup
 */
public void delete(IEntityGroup group) throws GroupsException
{
   if ( existsInDatabase(group) )
   {
       try
           { primDelete(group); }
       catch (SQLException sqle)
           { throw new GroupsException("Problem deleting " + group + ": " + sqle.getMessage()); }
   }
}
/**
 * Answer if the IEntityGroup entity exists in the database.
 * 
 * @return boolean
 * @param group
 *            IEntityGroup
 */
private boolean existsInDatabase(IEntityGroup group) throws GroupsException
{
   IEntityGroup ug = this.find(group.getLocalKey());
   return ug != null;
}
/**
 * Find and return an instance of the group.
 * 
 * @return org.jasig.portal.groups.IEntityGroup
 * @param key
 *            java.lang.Object
 */
public IEntityGroup find(String groupID) throws GroupsException
{
   return primFind(groupID, false);
}
/**
 * Find the groups that this entity belongs to.
 * 
 * @return java.util.Iterator
 * @param group
 *            org.jasig.portal.groups.IEntity
 */
public java.util.Iterator findContainingGroups(IEntity ent) throws GroupsException
{
   String memberKey = ent.getKey();
   Integer type = EntityTypes.getEntityTypeID(ent.getLeafType());
   return findContainingGroupsForEntity(memberKey, type.intValue(), false);
}
/**
 * Find the groups that this group belongs to.
 * 
 * @return java.util.Iterator
 * @param group
 *            org.jasig.portal.groups.IEntityGroup
 */
public java.util.Iterator findContainingGroups(IEntityGroup group) throws GroupsException
{
   String memberKey = group.getLocalKey();
   String serviceName = group.getServiceName().toString();
   Integer type = EntityTypes.getEntityTypeID(group.getLeafType());
   return findContainingGroupsForGroup(serviceName, memberKey, type.intValue(), true);
}
/**
 * Find the groups that this group member belongs to.
 * 
 * @return java.util.Iterator
 * @param group
 *            org.jasig.portal.groups.IGroupMember
 */
public Iterator findContainingGroups(IGroupMember gm) throws GroupsException
{
   if ( gm.isGroup() )
   {
       IEntityGroup group = (IEntityGroup) gm;
       return findContainingGroups(group);
   }
   else
   {
       IEntity ent = (IEntity) gm;
       return findContainingGroups(ent);
   }
}
/**
 * Find the groups associated with this member key.
 * 
 * @return java.util.Iterator
 * @param String
 *            memberKey
 */
private java.util.Iterator findContainingGroupsForEntity(String memberKey, int type, boolean isGroup)
throws GroupsException
{
   java.sql.Connection conn = null;
   Collection groups = new ArrayList();
   IEntityGroup eg = null;
   String groupOrEntity = isGroup ? MEMBER_IS_GROUP : MEMBER_IS_ENTITY;

   try
   {
           conn = RDBMServices.getConnection();
           String sql = getFindContainingGroupsForEntitySql();
           RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn, sql);
           try
           {
                   ps.setString(1, memberKey);
                   ps.setInt(2, type);
                   ps.setString(3, groupOrEntity);
                   LogService.log (LogService.DEBUG,
                     "UniconRDBMEntityGroupStore.findContainingGroupsForEntity(): " + ps +
                     " (" + memberKey + ", " + type + ", " + groupOrEntity + ")");
                   java.sql.ResultSet rs = ps.executeQuery();
                   try
                   {
                           while (rs.next())
                           {
                                   eg = instanceFromResultSet(rs);
                                   groups.add(eg);
                           }
                   }
                   finally
                       { rs.close(); }
       }
           finally
               { ps.close(); }
   }
   catch (Exception e)
   {
       LogService.log (LogService.ERROR, "UniconRDBMEntityGroupStore.findContainingGroupsForEntity(): " + e);
       throw new GroupsException("Problem retrieving containing groups: " + e);
   }

   finally
       { RDBMServices.releaseConnection(conn); }

   return groups.iterator();
}
/**
 * Find the groups associated with this member key.
 * 
 * @return java.util.Iterator
 * @param String
 *            memberKey
 */
private java.util.Iterator findContainingGroupsForGroup(String serviceName, String memberKey, int type, boolean isGroup)
throws GroupsException
{
   java.sql.Connection conn = null;
   Collection groups = new ArrayList();
   IEntityGroup eg = null;
   String groupOrEntity = isGroup ? MEMBER_IS_GROUP : MEMBER_IS_ENTITY;

   try
   {
           conn = RDBMServices.getConnection();
           String sql = getFindContainingGroupsForGroupSql();
           RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn, sql);
           try
           {
                   ps.setString(1, serviceName);
                   ps.setString(2, memberKey);
                   ps.setInt(3, type);
                   ps.setString(4, groupOrEntity);
                   LogService.log (LogService.DEBUG,
                     "UniconRDBMEntityGroupStore.findContainingGroupsForGroup(): " + ps +
                     " (" + memberKey + ", " + type + ", " + groupOrEntity + ")");
                   java.sql.ResultSet rs = ps.executeQuery();
                   try
                   {
                           while (rs.next())
                           {
                                   eg = instanceFromResultSet(rs);
                                   groups.add(eg);
                           }
                   }
                   finally
                       { rs.close(); }
       }
           finally
               { ps.close(); }
   }
   catch (Exception e)
   {
       LogService.log (LogService.ERROR, "UniconRDBMEntityGroupStore.findContainingGroupsForGroup(): " + e);
       throw new GroupsException("Problem retrieving containing groups: " + e);
   }

   finally
       { RDBMServices.releaseConnection(conn); }

   return groups.iterator();
}
/**
 * Find the <code>IEntities</code> that are members of the
 * <code>IEntityGroup</code>.
 * 
 * @return java.util.Iterator
 * @param group
 *            org.jasig.portal.groups.IEntityGroup
 */
public Iterator findEntitiesForGroup(IEntityGroup group) throws GroupsException
{
   Collection entities = new ArrayList();
   Connection conn = null;
   String groupID = group.getLocalKey();
   Class cls = group.getLeafType();

   try
   {
       conn = RDBMServices.getConnection();
       Statement stmnt = conn.createStatement();
       try {

           String query = "SELECT " + MEMBER_MEMBER_KEY_COLUMN + " FROM " + MEMBER_TABLE +
                          " WHERE " + MEMBER_GROUP_ID_COLUMN + " = '" +  groupID +
                          "' AND "  + MEMBER_IS_GROUP_COLUMN + " = '" + MEMBER_IS_ENTITY + "'";

           ResultSet rs = stmnt.executeQuery(query);
           try {
               while (rs.next())
               {
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
       }
   catch (SQLException sqle)
   {
       LogService.log(LogService.ERROR, sqle);
       throw new GroupsException("Problem retrieving Entities for Group: " + sqle.getMessage());
   }
   finally
       { RDBMServices.releaseConnection(conn); }

   return entities.iterator();
}
/**
 * Find the groups with this creatorID.
 * 
 * @return java.util.Iterator
 * @param String
 *            creatorID
 */
public java.util.Iterator findGroupsByCreator(String creatorID) throws GroupsException
{
   java.sql.Connection conn = null;
   Collection groups = new ArrayList();
   IEntityGroup eg = null;

   try
   {
           conn = RDBMServices.getConnection();
           String sql = getFindGroupsByCreatorSql();
           RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn, sql);
       try
       {
               ps.setString(1, creatorID);
               LogService.log (LogService.DEBUG, "UniconRDBMEntityGroupStore.findGroupsByCreator(): " + ps);
               ResultSet rs = ps.executeQuery();
               try
               {
                       while (rs.next())
                       {
                               eg = instanceFromResultSet(rs);
                               groups.add(eg);
                       }
               }
               finally
                   { rs.close(); }
               }
       finally
           { ps.close(); }
   }
   catch (Exception e)
   {
       LogService.log (LogService.ERROR, "UniconRDBMEntityGroupStore.findGroupsByCreator(): " + e);
       throw new GroupsException("Problem retrieving groups: " + e);
   }

   finally
       { RDBMServices.releaseConnection(conn); }

   return groups.iterator();
}
/**
 * Find and return an instance of the group.
 * 
 * @return org.jasig.portal.groups.ILockableEntityGroup
 * @param key
 *            java.lang.Object
 */
public ILockableEntityGroup findLockable(String groupID) throws GroupsException
{
   return (ILockableEntityGroup) primFind(groupID, true);
}
/**
 * Find the keys of groups that are members of group.
 * 
 * @return String[]
 * @param group
 *            org.jasig.portal.groups.IEntityGroup
 */
public String[] findMemberGroupKeys(IEntityGroup group) throws GroupsException
{
   java.sql.Connection conn = null;
   Collection groupKeys = new ArrayList();
   String groupKey = null;

   try
   {
       conn = RDBMServices.getConnection();
       String sql = getFindMemberGroupKeysSql();
       RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn, sql);
       try
       {
           ps.setString(1, group.getLocalKey());
           LogService.log (LogService.DEBUG,
             "UniconRDBMEntityGroupStore.findMemberGroupKeys(): " + ps + " (" + group.getLocalKey() + ")");
           java.sql.ResultSet rs = ps.executeQuery();
           try
           {
               while (rs.next())
               {
                   groupKey = rs.getString(1) + GROUP_NODE_SEPARATOR + rs.getString(2);
                   groupKeys.add(groupKey);
               }
           }
           finally
               { rs.close(); }
       }
       finally
           { ps.close(); }
   }
   catch (Exception sqle)
       {
           LogService.log (LogService.ERROR, "UniconRDBMEntityGroupStore.findMemberGroupKeys(): " + sqle);
           throw new GroupsException("Problem retrieving member group keys: " + sqle);
       }
   finally
       { RDBMServices.releaseConnection(conn); }

   return (String[]) groupKeys.toArray(new String[groupKeys.size()]);
}
/**
 * Find the IUserGroups that are members of the group.
 * 
 * @return java.util.Iterator
 * @param group
 *            org.jasig.portal.groups.IEntityGroup
 */
public Iterator findMemberGroups(IEntityGroup group) throws GroupsException
{
   java.sql.Connection conn = null;
   Collection groups = new ArrayList();
   IEntityGroup eg = null;
   String serviceName = group.getServiceName().toString();
   String localKey = group.getLocalKey();

   try
   {
       conn = RDBMServices.getConnection();
       String sql = getFindMemberGroupsSql();
       RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn, sql);
       try
       {
           ps.setString(1, localKey);
           ps.setString(2, serviceName);
           LogService.log (LogService.DEBUG,
             "UniconRDBMEntityGroupStore.findMemberGroups(): " + ps + " (" + localKey + ", " + serviceName + ")");
           java.sql.ResultSet rs = ps.executeQuery();
           try
           {
               while (rs.next())
               {
                   eg = instanceFromResultSet(rs);
                   groups.add(eg);
               }
           }
           finally
               { rs.close(); }
       }
       finally
           { ps.close(); }
   }
   catch (Exception sqle)
       {
           LogService.log (LogService.ERROR, "UniconRDBMEntityGroupStore.findMemberGroups(): " + sqle);
           throw new GroupsException("Problem retrieving member groups: " + sqle);
       }
   finally
       { RDBMServices.releaseConnection(conn); }

   return groups.iterator();
}
/**
 * @return java.lang.String
 */
private static java.lang.String getAllGroupColumns()
{

   if ( allGroupColumns == null )
   {
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
private static java.lang.String getAllGroupColumnsWithTableAlias()
{

   if ( allGroupColumnsWithTableAlias == null )
   {
           StringBuffer buff = new StringBuffer(100);
           buff.append(prependGroupTableAlias(GROUP_ID_COLUMN));
           buff.append(", ");
           buff.append(prependGroupTableAlias(GROUP_CREATOR_COLUMN));
           buff.append(", ");
           buff.append(prependGroupTableAlias(GROUP_TYPE_COLUMN));
           buff.append(", ");
           buff.append(prependGroupTableAlias(GROUP_NAME_COLUMN));
           buff.append(", ");
           buff.append(prependGroupTableAlias(GROUP_DESCRIPTION_COLUMN));

           allGroupColumnsWithTableAlias = buff.toString();
       }
   return allGroupColumnsWithTableAlias;
}
/**
 * @return java.lang.String
 */
private static java.lang.String getAllMemberColumns()
{
   if ( allMemberColumns == null )
   {
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
private static java.lang.String getDeleteGroupSql(IEntityGroup group)
{
   StringBuffer buff = new StringBuffer(100);
   buff.append("DELETE FROM ");
   buff.append(GROUP_TABLE);
   buff.append(" WHERE ");
   buff.append(GROUP_ID_COLUMN);
   buff.append(" = '");
   buff.append(group.getLocalKey());
   buff.append("'");
   return buff.toString();
}
/**
 * @return java.lang.String
 */
private static java.lang.String getDeleteMemberEntitySql()
{
   if ( deleteMemberEntitySql == null )
   {
       StringBuffer buff = new StringBuffer(100);
       buff.append("DELETE FROM ");
       buff.append(MEMBER_TABLE);
       buff.append(" WHERE ");
       buff.append(MEMBER_GROUP_ID_COLUMN);
       buff.append(" = ? AND ");
       buff.append(MEMBER_MEMBER_KEY_COLUMN);
       buff.append(" = ? AND ");
       buff.append(MEMBER_IS_GROUP_COLUMN);
       buff.append(" = ? ");

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
			buff.append(MEMBER_GROUP_ID_COLUMN);
			buff.append(" = ? AND ");
			buff.append(MEMBER_MEMBER_SERVICE_COLUMN);
			buff.append(" = ? AND ");
			buff.append(MEMBER_MEMBER_KEY_COLUMN);
			buff.append(" = ? AND ");
			buff.append(MEMBER_IS_GROUP_COLUMN);
			buff.append(" = ? ");
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
			buff.append(GROUP_ID_COLUMN);
			buff.append(" = ");
			deleteMembersInGroupSql = buff.toString();
		}
		return deleteMembersInGroupSql;
	}
	/**
	 * @return java.lang.String
	 */
	private static java.lang.String getDeleteMembersInGroupSql(
			IEntityGroup group) {
		StringBuffer buff = new StringBuffer(getDeleteMembersInGroupSql());
		buff.append("'");
		buff.append(group.getLocalKey());
		buff.append("'");
		return buff.toString();
	}
	/**
	 * @return java.lang.String
	 */
	private static java.lang.String getFindContainingGroupsForEntitySql() {
		if (findContainingGroupsForEntitySql == null) {
			StringBuffer buff = new StringBuffer(500);
			buff.append("SELECT ");
			buff.append(getAllGroupColumnsWithTableAlias());
			buff.append(" FROM ");
			buff.append(GROUP_TABLE + " " + GROUP_TABLE_ALIAS);
			buff.append(", ");
			buff.append(MEMBER_TABLE + " " + MEMBER_TABLE_ALIAS);
			buff.append(" WHERE ");
			buff.append(prependGroupTableAlias(GROUP_ID_COLUMN));
			buff.append(" = ");
			buff.append(prependMemberTableAlias(MEMBER_GROUP_ID_COLUMN));
			buff.append(" AND ");
			buff.append(prependMemberTableAlias(MEMBER_MEMBER_KEY_COLUMN));
			buff.append(" = ? AND ");
			buff.append(prependGroupTableAlias(GROUP_TYPE_COLUMN));
			buff.append(" = ? AND ");
			buff.append(prependMemberTableAlias(MEMBER_IS_GROUP_COLUMN));
			buff.append(" = ? ");
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
			buff.append(GROUP_TABLE + " " + GROUP_TABLE_ALIAS);
			buff.append(", ");
			buff.append(MEMBER_TABLE + " " + MEMBER_TABLE_ALIAS);
			buff.append(" WHERE ");
			buff.append(prependGroupTableAlias(GROUP_ID_COLUMN));
			buff.append(" = ");
			buff.append(prependMemberTableAlias(MEMBER_GROUP_ID_COLUMN));
			buff.append(" AND ");
			buff.append(prependMemberTableAlias(MEMBER_MEMBER_SERVICE_COLUMN));
			buff.append(" = ? AND ");
			buff.append(prependMemberTableAlias(MEMBER_MEMBER_KEY_COLUMN));
			buff.append(" = ? AND ");
			buff.append(prependGroupTableAlias(GROUP_TYPE_COLUMN));
			buff.append(" = ? AND ");
			buff.append(prependMemberTableAlias(MEMBER_IS_GROUP_COLUMN));
			buff.append(" = ? ");
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
			buff.append(GROUP_CREATOR_COLUMN);
			buff.append(" = ? ");
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
			buff.append(GROUP_ID_COLUMN);
			buff.append(" = ? ");
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
			buff.append(MEMBER_GROUP_ID_COLUMN);
			buff.append(" = ? AND ");
			buff.append(MEMBER_IS_GROUP_COLUMN);
			buff.append(" = '");
			buff.append(MEMBER_IS_GROUP);
			buff.append("'");
			;
			findMemberGroupKeysSql = buff.toString();
		}
		return findMemberGroupKeysSql;
	}
	/**
	 * @return java.lang.String
	 */
	private static java.lang.String getFindMemberGroupSql() {
		if (findMemberGroupSql == null) {
			StringBuffer buff = new StringBuffer(getFindMemberGroupsSql());
			buff.append("AND ");
			buff.append(GROUP_TABLE_ALIAS);
			buff.append(".");
			buff.append(GROUP_NAME_COLUMN);
			buff.append(" = ?");
			findMemberGroupSql = buff.toString();
		}
		return findMemberGroupSql;
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
			buff.append(prependGroupTableAlias(GROUP_ID_COLUMN));
			buff.append(" = ");
			buff.append(prependMemberTableAlias(MEMBER_MEMBER_KEY_COLUMN));
			buff.append(" AND ");
			buff.append(prependMemberTableAlias(MEMBER_IS_GROUP_COLUMN));
			buff.append(" = '");
			buff.append(MEMBER_IS_GROUP);
			buff.append("' AND ");
			buff.append(prependMemberTableAlias(MEMBER_GROUP_ID_COLUMN));
			buff.append(" = ? ");
			buff.append(" AND ");
			buff.append(prependMemberTableAlias(MEMBER_MEMBER_SERVICE_COLUMN));
			buff.append(" = ? ");
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
	private String getNextKey() throws java.lang.Exception {
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
			buff.append(GROUP_CREATOR_COLUMN);
			buff.append(" = ?, ");
			buff.append(GROUP_TYPE_COLUMN);
			buff.append(" = ?, ");
			buff.append(GROUP_NAME_COLUMN);
			buff.append(" = ?, ");
			buff.append(GROUP_DESCRIPTION_COLUMN);
			buff.append(" = ? WHERE ");
			buff.append(GROUP_ID_COLUMN);
			buff.append(" = ? ");
			updateGroupSql = buff.toString();
		}
		return updateGroupSql;
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
		String msg = "UniconRDBMEntityGroupStore.initialize(): Node separator set to "
				+ sep;
		LogService.log(LogService.INFO, msg);
	}
	/**
	 * Find and return an instance of the group.
	 * 
	 * @return org.jasig.portal.groups.IEntityGroup
	 * @param key
	 *            java.lang.Object
	 */
	private IEntityGroup instanceFromResultSet(java.sql.ResultSet rs)
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
	 * @return org.jasig.portal.groups.ILockableEntityGroup
	 * @param key
	 *            java.lang.Object
	 */
	private ILockableEntityGroup lockableInstanceFromResultSet(
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
		LogService.log(LogService.WARN, msg);
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
	public IEntityGroup newInstance(Class type) throws GroupsException {
		if (EntityTypes.getEntityTypeID(type) == null) {
			throw new GroupsException("Invalid group type: " + type);
		}
		try {
			return new UniconEntityGroup(getNextKey(), type);
		} catch (Exception ex) {
			throw new GroupsException("Could not create new group: "
					+ ex.getMessage());
		}
	}
	/**
	 * @return org.jasig.portal.groups.IEntityGroup
	 */
	private IEntityGroup newInstance(String newKey, Class newType,
			String newCreatorID, String newName, String newDescription)
			throws GroupsException {
		UniconEntityGroup gr = new UniconEntityGroup(newKey, newType);
		gr.setCreatorID(newCreatorID);
		gr.primSetName(newName);
		gr.setDescription(newDescription);
		return gr;
	}
	/**
	 * @return org.jasig.portal.groups.ILockableEntityGroup
	 */
	private ILockableEntityGroup newLockableInstance(String newKey,
			Class newType, String newCreatorID, String newName,
			String newDescription) throws GroupsException {
		UniconLockableEntityGroup group = new UniconLockableEntityGroup(newKey,
				newType);
		group.setCreatorID(newCreatorID);
		group.primSetName(newName);
		group.setDescription(newDescription);
		return group;
	}
	/**
	 * @return java.lang.String
	 */
	private static java.lang.String prependGroupTableAlias(String column) {
		return GROUP_TABLE_ALIAS + "." + column;
	}
	/**
	 * @return java.lang.String
	 */
	private static java.lang.String prependMemberTableAlias(String column) {
		return MEMBER_TABLE_ALIAS + "." + column;
	}
	/**
	 * Insert the entity into the database.
	 * 
	 * @param group
	 *            org.jasig.portal.groups.IEntityGroup
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
				LogService.log(LogService.DEBUG,
						"UniconRDBMEntityGroupStore.primAdd(): " + ps + "("
								+ group.getLocalKey() + ", "
								+ group.getCreatorID() + ", " + typeID + ", "
								+ group.getName() + ", "
								+ group.getDescription() + ")");
				int rc = ps.executeUpdate();
				if (rc != 1) {
					String errString = "Problem adding " + group;
					LogService.log(LogService.ERROR, errString);
					throw new GroupsException(errString);
				}
			} finally {
				ps.close();
			}
		} catch (java.sql.SQLException sqle) {
			LogService.log(LogService.ERROR, sqle);
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
				LogService.log(LogService.DEBUG,
						"UniconRDBMEntityGroupStore.primDelete(): "
								+ deleteMembershipSql);
				stmnt.executeUpdate(deleteMembershipSql);
				LogService.log(LogService.DEBUG,
						"UniconRDBMEntityGroupStore.primDelete(): " + deleteGroupSql);
				stmnt.executeUpdate(deleteGroupSql);
			} finally {
				stmnt.close();
			}
			commit(conn);
		} catch (SQLException sqle) {
			rollback(conn);
			throw sqle;
		} finally {
			setAutoCommit(conn, true);
			RDBMServices.releaseConnection(conn);
		}
	}
	/**
	 * Find and return an instance of the group.
	 * 
	 * @return org.jasig.portal.groups.IEntityGroup
	 * @param key
	 *            java.lang.Object
	 * @param lockable
	 *            boolean
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
				LogService.log(LogService.DEBUG,
						"UniconRDBMEntityGroupStore.find(): " + ps + " (" + groupID
								+ ")");
				java.sql.ResultSet rs = ps.executeQuery();
				try {
					while (rs.next()) {
						eg = (lockable)
								? lockableInstanceFromResultSet(rs)
								: instanceFromResultSet(rs);
					}
				} finally {
					rs.close();
				}
			} finally {
				ps.close();
			}
		} catch (Exception e) {
			LogService.log(LogService.ERROR, "UniconRDBMEntityGroupStore.find(): "
					+ e);
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
				LogService.log(LogService.DEBUG,
						"UniconRDBMEntityGroupStore.primUpdate(): " + ps + "("
								+ group.getCreatorID() + ", " + typeID + ", "
								+ group.getName() + ", "
								+ group.getDescription() + ", "
								+ group.getLocalKey() + ")");
				int rc = ps.executeUpdate();
				if (rc != 1) {
					String errString = "Problem updating " + group;
					LogService.log(LogService.ERROR, errString);
					throw new GroupsException(errString);
				}
			} finally {
				ps.close();
			}
		} catch (java.sql.SQLException sqle) {
			LogService.log(LogService.ERROR, sqle);
			throw sqle;
		}
	}
	/**
	 * Insert and delete group membership rows. The transaction is maintained by
	 * the caller.
	 * 
	 * @param group
	 *            net.unicon.portal.groups.framework.UniconEntityGroup
	 */
	private void primUpdateMembers(UniconEntityGroup egi, Connection conn)
			throws java.sql.SQLException {
		String groupKey = egi.getLocalKey();
		String memberKey, isGroup, serviceName = null;
		try {
			if (egi.hasDeletes()) {
				List deletedGroups = new ArrayList();
				List deletedEntities = new ArrayList();
				Iterator deletes = egi.getRemovedMembers().values().iterator();
				while (deletes.hasNext()) {
					IGroupMember gm = (IGroupMember) deletes.next();
					if (gm.isGroup()) {
						deletedGroups.add(gm);
					} else {
						deletedEntities.add(gm);
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
							psDeleteMemberGroup.setString(4, isGroup);
							LogService.log(LogService.DEBUG,
									"UniconRDBMEntityGroupStore.primUpdateMembers(): "
											+ psDeleteMemberGroup + "("
											+ groupKey + ", " + serviceName
											+ ", " + memberKey + ", " + isGroup
											+ ")");
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
							psDeleteMemberEntity.setString(3, isGroup);
							LogService.log(LogService.DEBUG,
									"UniconRDBMEntityGroupStore.primUpdateMembers(): "
											+ psDeleteMemberEntity + "("
											+ groupKey + ", " + memberKey
											+ ", " + isGroup + ")");
							psDeleteMemberEntity.executeUpdate();
						} // for
					} // try
					finally {
						psDeleteMemberEntity.close();
					}
				} //  if ( ! deletedEntities.isEmpty() )
			}
			if (egi.hasAdds()) {
				RDBMServices.PreparedStatement psAdd = new RDBMServices.PreparedStatement(
						conn, getInsertMemberSql());
				try {
					Iterator adds = egi.getAddedMembers().values().iterator();
					while (adds.hasNext()) {
						IGroupMember addedGM = (IGroupMember) adds.next();
						memberKey = addedGM.getKey();
						if (addedGM.isGroup()) {
							IEntityGroup addedGroup = (IEntityGroup) addedGM;
							isGroup = MEMBER_IS_GROUP;
							serviceName = addedGroup.getServiceName()
									.toString();
							memberKey = addedGroup.getLocalKey();
						} else {
							isGroup = MEMBER_IS_ENTITY;
							serviceName = egi.getServiceName().toString();
							memberKey = addedGM.getUnderlyingEntityIdentifier()
									.getKey();
						}
						psAdd.setString(1, groupKey);
						psAdd.setString(2, serviceName);
						psAdd.setString(3, memberKey);
						psAdd.setString(4, isGroup);
						LogService.log(LogService.DEBUG,
								"UniconRDBMEntityGroupStore.primUpdateMembers(): "
										+ psAdd + "(" + groupKey + ", "
										+ memberKey + ", " + isGroup + ")");
						psAdd.executeUpdate();
					}
				} finally {
					psAdd.close();
				}
			}
		} catch (SQLException sqle) {
			LogService.log(LogService.ERROR, sqle);
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
				case IS :
					ps = new RDBMServices.PreparedStatement(conn,
							this.searchGroups);
					break;
				case STARTS_WITH :
					query = query + "%";
					ps = new RDBMServices.PreparedStatement(conn,
							this.searchGroupsPartial);
					break;
				case ENDS_WITH :
					query = "%" + query;
					ps = new RDBMServices.PreparedStatement(conn,
							this.searchGroupsPartial);
					break;
				case CONTAINS :
					query = "%" + query + "%";
					ps = new RDBMServices.PreparedStatement(conn,
							this.searchGroupsPartial);
					break;
				default :
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
			LogService.log(LogService.ERROR,
					"RDBMChannelDefSearcher.searchForEntities(): " + ps);
			LogService.log(LogService.ERROR, e);
		} finally {
			RDBMServices.releaseConnection(conn);
		}
		return (EntityIdentifier[]) ar.toArray(r);
	}
	/**
	 * @param conn
	 *            java.sql.Connection
	 * @param newValue
	 *            boolean
	 * @exception java.sql.SQLException
	 *                The exception description.
	 */
	protected static void setAutoCommit(Connection conn, boolean newValue)
			throws java.sql.SQLException {
		SqlTransaction.setAutoCommit(conn, newValue);
	}
	/**
	 * @param newGroupService
	 *            org.jasig.portal.groups.IGroupService
	 */
	public void setGroupService(IGroupService newGroupService) {
		groupService = newGroupService;
	}
	/**
	 * @return net.unicon.portal.groups.framework.UniconRDBMEntityGroupStore
	 */
	public static synchronized UniconRDBMEntityGroupStore singleton()
			throws GroupsException {
		if (singleton == null) {
			singleton = new UniconRDBMEntityGroupStore();
		}
		return singleton;
	}
	/**
	 * Commit this entity AND ITS MEMBERSHIPS to the underlying store.
	 * 
	 * @param group
	 *            org.jasig.portal.groups.IEntityGroup
	 */
	public void update(IEntityGroup group) throws GroupsException {
		Connection conn = null;
		boolean exists = existsInDatabase(group);
		try {
			conn = RDBMServices.getConnection();
			setAutoCommit(conn, false);
			try {
				if (exists) {
					primUpdate(group, conn);
				} else {
					primAdd(group, conn);
				}
				primUpdateMembers((UniconEntityGroup) group, conn);
				commit(conn);
			} catch (Exception ex) {
				rollback(conn);
				throw new GroupsException("Problem updating " + this + ex);
			}
		} catch (SQLException sqlex) {
			throw new GroupsException(sqlex.getMessage());
		} finally {
			try {
				setAutoCommit(conn, true);
			} catch (SQLException sqle) {
				throw new GroupsException(sqle.getMessage());
			}
			RDBMServices.releaseConnection(conn);
		}
	}
	/**
	 * Insert and delete group membership rows inside a transaction.
	 * 
	 * @param group
	 *            org.jasig.portal.groups.IEntityGroup
	 */
	public void updateMembers(IEntityGroup eg) throws GroupsException {
		Connection conn = null;
		UniconEntityGroup egi = (UniconEntityGroup) eg;
		if (egi.isDirty())
			try {
				conn = RDBMServices.getConnection();
				setAutoCommit(conn, false);
				try {
					primUpdateMembers(egi, conn);
					commit(conn);
				} catch (SQLException sqle) {
					rollback(conn);
					throw new GroupsException(
							"Problem updating memberships for " + egi + " "
									+ sqle.getMessage());
				}
			} catch (SQLException sqlex) {
				throw new GroupsException(sqlex.getMessage());
			} finally {
				try {
					setAutoCommit(conn, true);
				} catch (SQLException sqle) {
					throw new GroupsException(sqle.getMessage());
				}
				RDBMServices.releaseConnection(conn);
			}
	}

    // ******** STUBBED *******
    public boolean contains(IEntityGroup group, IGroupMember member) 
    throws GroupsException {
    return false;
    }
}
