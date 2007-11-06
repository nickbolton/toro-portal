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
import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.IDomainEventHandler;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.academus.domain.sor.AccessType;
import net.unicon.academus.domain.sor.IEntityRecordInfo;
import net.unicon.academus.domain.sor.ISystemOfRecord;
import net.unicon.academus.domain.sor.SorViolationException;
import net.unicon.academus.domain.sor.SystemOfRecordBroker;
import net.unicon.portal.domain.*;
import net.unicon.sdk.properties.*;
import net.unicon.portal.groups.UniconGroupService;
import net.unicon.portal.groups.UniconGroupServiceFactory;
import net.unicon.portal.groups.GroupFactory;
import net.unicon.portal.groups.IGroup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.unicon.portal.util.db.AcademusDBUtil;


/** Provides static methods to create and access topics (groups of offerings). <code>TopicFactory</code> is not instantiable. */
public final class TopicFactory {

    private static Map cache = new HashMap();
    private static final boolean isCaching = UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getPropertyAsBoolean("net.unicon.academus.domain.lms.isCaching");

    private static final boolean isMultiBox = UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getPropertyAsBoolean("net.unicon.portal.Academus.multipleBoxConfig");
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

    private static final List deleteHandlers = new ArrayList();

    private TopicFactory() { }

    /**
     * Creates a new top-level topic with the specified name and type.  The
     * parent topic member for top-level topics is <code>null</code>.
     * @param name the name (label) for the new topic.
     * @param description the description for the new topic.
     * @param type a topic type within the portal system.
     * @param creator the User that's creating this topic
     * @return the newly created topic.
     * @throws IllegalArgumentException if there is already a top-level topic
     * with the specified name for the specified topic type or if
     * <code>name</code> is zero-length or contains only whitespace or if <code>type</code> or <code>type</code> is null.
     * @throws OperationFailedException if the database interation fails.
     */
    public static Topic createTopic(String name, String description,
                    TopicType type, IGroup parentGroup, User creator)
    throws IllegalArgumentException,
    OperationFailedException {
        // Assertions.
        if (name == null) {
            throw new IllegalArgumentException("Topic name can't be null.");
        }
        if (description == null) {
            throw new IllegalArgumentException(
            "Topic description can't be null.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Topic type can't be null.");
        }
        if (name.trim().length() == 0) {
            StringBuffer msg = new StringBuffer();
            msg.append("Topic name can't be zero-length ");
            msg.append("or contain only whitespace.");
            throw new IllegalArgumentException(msg.toString());
        }
        if (description.trim().length() == 0) {
            StringBuffer msg = new StringBuffer();
            msg.append("Topic description can't be zero-length ");
            msg.append("or contain only whitespace.");
            throw new IllegalArgumentException(msg.toString());
        }
        if (topicExistsInContext(name, parentGroup)) {
            StringBuffer msg = new StringBuffer();
            msg.append("A top-level topic with the specified name already ");
            msg.append("exists for this topic type.");
            throw new IllegalArgumentException(msg.toString());
        }

        // The topic object.
        Topic top = null;
        // SQL -- should return zero rows.
        StringBuffer sql = new StringBuffer();
        sql.append("Insert into TOPIC ");
        sql.append("(group_id, Name, description, Parent_Group_Id, Type) ");
        sql.append("values(?, ?, ?, ?, ?)");
        // db objects.
        Connection conn = null;
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = null;
            // create the associated group object
            UniconGroupService gs = UniconGroupServiceFactory.getService();
            IGroup group = gs.createGroup(creator, parentGroup,
                Topic.groupPrefix + name, description);

            // Create a topic in the db.
            conn = AcademusDBUtil.getDBConnection();

            try {
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setLong(i++, group.getGroupId());
            pstmt.setString(i++, name);
            pstmt.setString(i++, description);
            pstmt.setLong(i++, parentGroup.getGroupId());
            pstmt.setString(i++, type.toString());
            pstmt.execute();
            } finally {
                try {
                    if (pstmt != null) pstmt.close();
                    pstmt = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
            // Retrieve the Id.
            sql = new StringBuffer();
            sql.append("Select Topic_Id from TOPIC ");
            sql.append("where Name = ? and Parent_Group_Id = ?");
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, name);
            pstmt.setLong(2, parentGroup.getGroupId());
            rs = pstmt.executeQuery();
            rs.next();
            // Create a topic object.
            top = new Topic(rs.getLong("Topic_Id"), group, name,
            description, type, parentGroup);
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

        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation createTopic(name, type) failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            AcademusDBUtil.safeReleaseDBConnection(conn);
            conn = null;
        }
        if (isCaching) {
            cache.put(new Long(top.getId()), top);
            if (handler != null) {
                try {
                    handler.broadcastRefreshTopic((int) top.getId());
                } catch (AcademusException ae) {
                    ae.printStackTrace();
                }
            }
        }
        return top;
    }

    public static void deleteTopic(long id)
                    throws OperationFailedException, ItemNotFoundException,
                    DomainException {
        deleteTopic(id, SystemOfRecordBroker.ACADEMUS);
    }

    public static void deleteTopic(long id, ISystemOfRecord principal)
                    throws OperationFailedException, ItemNotFoundException,
                    DomainException {

        // Assertions.
        if (principal == null) {
            String msg = "Argument 'principal' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Topic topic = getTopic(id);

        // Make sure we don't violate an SOR.
        ensureSorAccess(topic, principal, AccessType.DELETE);

        // Now that we know we're going to delete, notify the handlers.
        Topic t = getTopic(id);
        Iterator it = deleteHandlers.iterator();
        while (it.hasNext()) {
            IDomainEventHandler e = (IDomainEventHandler) it.next();
            e.handleEvent(t);
        }

        IGroup parentGroup = topic.getParentGroup();
        // SQL
        StringBuffer sql = new StringBuffer();
        sql.append("delete from TOPIC where topic_id = ?");
        // db objects.
        Connection conn = null;
        try {
            PreparedStatement pstmt = null;
            // Then delete the target from the db
            conn = AcademusDBUtil.getDBConnection();

            try {
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            } finally {
                try {
                    if (pstmt != null) pstmt.close();
                    pstmt = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
            // Now set all its children to point to its parent
            sql = new StringBuffer();
            sql.append("update topic set parent_group_id = ? ");
            sql.append("where parent_group_id = ?");
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setLong(i++, parentGroup.getGroupId());
            pstmt.setLong(i++, topic.getGroup().getGroupId());
            pstmt.executeUpdate();
            } finally {
                try {
                    if (pstmt != null) pstmt.close();
                    pstmt = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // now delete associated group
            topic.getGroup().delete();
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getTopics(type) failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            AcademusDBUtil.safeReleaseDBConnection(conn);
            conn = null;
        }
        if (isCaching) {
            cache.remove(new Long(id));
            if (handler != null) {
                try {
                    handler.broadcastRemoveTopic((int) id);
                } catch (AcademusException ae) {
                    ae.printStackTrace();
                }
            }
        }
    }
    /**
     * Removes the topic object from memory/cache.
     * @param topicID - the topic id to remove from cache
     * @see net.unicon.portal.domain.IllegalArgumentException
     * @see net.unicon.portal.domain.OperationFailedException
     */
    public static void removeTopicFromCache(long topicID)
    throws IllegalArgumentException, OperationFailedException {
        if (isCaching) {
            cache.remove(new Long(topicID));
        }
    }
    /**
     * Refreshed the topic object from memory/cache.
     * @param topicID - the topic id to remove from cache
     * @see net.unicon.portal.domain.IllegalArgumentException
     * @see net.unicon.portal.domain.ItemNotFoundException
     * @see net.unicon.portal.domain.OperationFailedException
     */
    public static void refreshTopicCache(long topicID)
    throws IllegalArgumentException, OperationFailedException, ItemNotFoundException  {
        if (isCaching) {
            cache.remove(new Long(topicID));
            getTopic(topicID);
        }
    }
    /**
     * Provides the set of top-level (<strong>not</strong> all topics) topics for the specified topic type.
     * @param type a topic type in the protal system.
     * @return all the top-level topics for the specified type.
     * @throws IllegalArgumentException if the <code>type</code> is <code>null</code>.
     * @throws OperationFailedException if the database interation fails.
     */
    public static List getTopics(TopicType type)
    throws IllegalArgumentException, OperationFailedException {
        if (isCaching) {
            synchronized (cache) {
                return _getTopics(type);
            }
        }
        return _getTopics(type);
    }
    public static List _getTopics(TopicType type)
    throws IllegalArgumentException,
    OperationFailedException {
        if (type == null) {
            String msg = "Topic type can't be null.";
            throw new IllegalArgumentException(msg);
        }
        // The rtn.
        List rtn = new ArrayList();
        // SQL
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT topic_id, name, parent_group_id, description, group_id FROM topic WHERE type = ?");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Obtain the rows.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, type.toString());
            rs = pstmt.executeQuery();
            // Topic & members.
            Topic top = null;
            long topicId = 0;
            IGroup group = null;
            IGroup parentGroup = null;
            String name = null;
            String description = null;
            // Loop and build the rtn.
            while (rs.next()) {
                top = null;   // necessary if not caching!
                topicId = rs.getLong("Topic_Id");
                if (isCaching) {
                    top = (Topic) cache.get(new Long(topicId));
                }
                if (top != null) {
                    rtn.add(top);
                } else {
                    name = rs.getString("Name");
                    description = rs.getString("description");
                    group = GroupFactory.getGroup(rs.getLong("group_id"));
                    long parentGroupId = rs.getLong("parent_group_id");
                    parentGroup =
                        GroupFactory.getGroup(parentGroupId);
                    top = new Topic(topicId, group, name, description,
                        type, parentGroup);
                    if (isCaching) {
                        cache.put(new Long(topicId), top);
                    }
                    rtn.add(top);
                }
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getTopics(type) failed ");
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
        // Return.
        return rtn;
    }
    /**
     * Gets a Topic object from the database or from cache, if cache is enabled.  The retrieving of
     * topic object is syncronized.
     * @param topicId - the topic id to retrieve from the db
     * @return Topic - topic object
     * @see net.unicon.portal.domain.OperationFailedException
     * @see net.unicon.portal.domain.ItemNotFoundException
     */
    public static Topic getTopic(long topicId)
    throws OperationFailedException, ItemNotFoundException {
        if (isCaching) {
            synchronized (cache) {
                return _getTopic(topicId);
            }
        }
        return _getTopic(topicId);
    }
    public static Topic _getTopic(long topicId)
    throws OperationFailedException, ItemNotFoundException {
        // The rtn.
        Topic rtn = null;
        // Getting the topic object from cache
        // if cached is enabled.
        if (isCaching) {
            rtn = (Topic) cache.get(new Long(topicId));
            // Returning the retrieved topic from cache
            if (rtn != null) return rtn;
        }
        rtn = getTopicFromDB(topicId);
        if (isCaching) {
            cache.put(new Long(topicId), rtn);
        }
        return rtn;
    }
    /**
     * Gets a Topic object from the database (no caching)
     * @param topicId - the topic id to retrieve from the db
     * @return Topic - topic object
     * @see net.unicon.portal.domain.OperationFailedException
     * @see net.unicon.portal.domain.ItemNotFoundException
     */
    public static Topic getTopicFromDB (long topicId)
    throws OperationFailedException, ItemNotFoundException {
        // The rtn.
        Topic rtn = null;
        // SQL
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT name, parent_group_id, type, description, group_id FROM topic WHERE topic_id = ?");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Obtain the rows.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, topicId);
            rs = pstmt.executeQuery();
            // Topic & members.
            String name = null;
            String description = null;
            IGroup group = null;

            // build the rtn.
            if (rs.next()) {
                name = rs.getString("Name");
                description = rs.getString("description");
                String type = rs.getString("type");
                long parentGroupId = rs.getLong("parent_group_id");
                group = GroupFactory.getGroup(rs.getLong("group_id"));
                IGroup parentGroup = GroupFactory.getGroup(parentGroupId);
                rtn = new Topic(topicId, group, name, description,
                TopicType.getTopicType(type), parentGroup);
            } else {
                StringBuffer msg = new StringBuffer();
                msg.append("Could not find topic with id: " + topicId);
                throw new ItemNotFoundException(msg.toString());
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getTopics(topicId) failed ");
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

    /**
     * Provides the list of topics, recursively, that have the
     * specified topic as their parent.
     * @param parentGroup a topic in the portal system.
     * @return all topics that have the specified topic as their parent.
     * @throws IllegalArgumentException if the parentGroup is <code>null</code>.
     * @throws OperationFailedException if the database interation fails.
     */
    public static List getAllTopics(IGroup parentGroup)
    throws IllegalArgumentException, OperationFailedException {
        List retList = new ArrayList();

        List children = getTopics(parentGroup);
        retList.addAll(children);
        Iterator itr = children.iterator();
        while (itr.hasNext()) {
            retList.addAll(getAllTopics(((Topic)itr.next()).getGroup()));
        }

        return retList;
    }

    /**
     * Provides the list of topics that have the specified
     * topic as their parent.
     * @param parentGroup a topic in the portal system.
     * @return all topics that have the specified topic as their parent.
     * @throws IllegalArgumentException if the parentGroup is <code>null</code>.
     * @throws OperationFailedException if the database interation fails.
     */
    public static List getTopics(IGroup parentGroup)
    throws IllegalArgumentException, OperationFailedException {
        if (isCaching) {
            synchronized (cache) {
                return _getTopics(parentGroup);
            }
        }
        return _getTopics(parentGroup);
    }
    public static List _getTopics(IGroup parentGroup)
    throws IllegalArgumentException,
    OperationFailedException {
        if (parentGroup == null) {
            String msg = "The parent group can't be null.";
            throw new IllegalArgumentException(msg);
        }
        // The rtn.
        List rtn = new ArrayList();
        // SQL
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT topic_id, name, type, description, group_id FROM topic WHERE parent_group_id = ?");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Obtain the rows.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, parentGroup.getGroupId());
            rs = pstmt.executeQuery();
            // Topic & members.
            Topic top = null;
            long topicId = 0;
            String name = null;
            TopicType type = null;
            String description = null;
            IGroup group = null;

            // Loop and build the rtn.
            while (rs.next()) {
                top = null;   // necessary if not caching!
                topicId = rs.getLong("Topic_Id");
                if (isCaching) {
                    top = (Topic) cache.get(new Long(topicId));
                }
                if (top != null) {
                    rtn.add(top);
                } else {
                    name = rs.getString("Name");
                    description = rs.getString("description");
                    group = GroupFactory.getGroup(rs.getLong("group_id"));
                    type = TopicType.getTopicType(rs.getString("type"));
                    top = new Topic(topicId, group, name, description,
                        type, parentGroup);
                    if (isCaching) {
                        cache.put(new Long(topicId), top);
                    }
                    rtn.add(top);
                }
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getTopics(parentGroup) failed ");
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
        // Return.
        return rtn;
    }

    public static void persist(Topic obj)
                        throws IllegalArgumentException, ItemNotFoundException,
                        OperationFailedException, DomainException {
        persist(obj, SystemOfRecordBroker.ACADEMUS);
    }

    public static void persist(Topic obj, ISystemOfRecord principal)
                        throws IllegalArgumentException, ItemNotFoundException,
                        OperationFailedException, DomainException {

        // Assertions.
        if (obj == null) {
            String msg = "The topic can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (principal == null) {
            String msg = "Argument 'principal' can't be null.";
            throw new IllegalArgumentException(msg);
        }

        // Make sure we don't violate an SOR.
        ensureSorAccess(obj, principal, AccessType.MODIFY);

        // SQL -- should return exactly one row.
        StringBuffer sql = new StringBuffer();
        sql.append("Update TOPIC set name = ?, description = ? ");
        sql.append("where Topic_Id = ?");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            // Persist the topic.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setString(i++, obj.getName());
            pstmt.setString(i++, obj.getDescription());
            pstmt.setLong(i++, obj.getId());
            pstmt.execute();
            if (isCaching && handler != null) {
                try {
                    handler.broadcastRefreshTopic((int) obj.getId());
                } catch (AcademusException ae) {
                    ae.printStackTrace();
                }
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation persist(Topic) failed ");
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

    public static void registerDeleteEventHandler(IDomainEventHandler h) {

        // Assertions.
        if (h == null) {
            String msg = "Argument 'h [IDomainEventHandler]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        if (!deleteHandlers.contains(h)) {
            deleteHandlers.add(h);
        }

    }

    static boolean topicExistsInContext(String name, IGroup parentGroup)
                                    throws OperationFailedException {
        boolean rslt = false;

        if (isCaching) {
            Iterator topics = getTopics(parentGroup).iterator();
            while (topics.hasNext()) {
                if (((Topic)topics.next()).getName().equals(name)) return true;
            }
            return false;
        }

        final String sql = "select topic_id from topic where parent_group_id = ? and name = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Persist the topic.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql);
            int i = 1;
            pstmt.setLong(i++, parentGroup.getGroupId());
            pstmt.setString(i++, name);
            rs = pstmt.executeQuery();
            if (rs.next())
                rslt = true;
        } catch (Exception e) {
            throw new OperationFailedException(e.getMessage(), e);
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
            pstmt = null;
            conn = null;
        }

        return rslt;
    }

    private static void ensureSorAccess(Topic c, ISystemOfRecord principal,
                                AccessType t) throws DomainException {

        // Assertions.
        if (c == null) {
            String msg = "Argument 'c [Topic]' cannot be null.";
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

        IEntityRecordInfo rec = SystemOfRecordBroker.getRecordInfo(c);

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
