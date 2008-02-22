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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.CompositeName;
import javax.naming.Name;

import net.unicon.portal.groups.framework.eve.graph.GraphFactory;

import org._3pq.jgrapht.DirectedGraph;
import org._3pq.jgrapht.graph.DefaultDirectedGraph;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.groups.CompositeEntityIdentifier;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.services.GroupService;


/**
 * Store for <code>Group</code>.
 * 
 * @author nbolton
 */
public class EveGroupStore extends AbstractGroupStore implements IEveGroupStore {
    private static final Log log = LogFactory.getLog(EveGroupStore.class);

    private static final String DEFAULT_SERVICE_NAME = "local";
    private String serviceName = DEFAULT_SERVICE_NAME;
    
    private static Map singletons = new HashMap(); 
    
    /**
     * EveGroupStore constructor.
     */
    public EveGroupStore(String serviceName) {
        super();
        this.serviceName = serviceName;
    }
    
    public static synchronized EveGroupStore singleton(String serviceName) {
        EveGroupStore store = (EveGroupStore)singletons.get(serviceName);
        if (store == null) {
            store = new EveGroupStore(serviceName);
            singletons.put(serviceName, store);
        }
        return store;
    }

    /**
     * Retrieves a Map of Identifier to IEntityGroup objects
     */
    public Map getGroups() throws Exception {
        Map retMap = new HashMap();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = "select group_id, creator_id, entity_type_id, group_name, description from up_group";

        try {
            conn = RDBMServices.getConnection();
            ps = conn.prepareStatement(sql);
            
            log.info("EveGroupStore : retrieving groups...");
            rs = ps.executeQuery();

            IEntityGroup gr = null;

            while (rs.next()) {
                gr = instanceFromResultSet(rs);
                ((IEveGroup)gr).setServiceName(serviceName);
                retMap.put(gr.getKey(), gr);
            }
            log.info("EveGroupStore : done retrieving groups.");
        } catch (Throwable e) {
            Utils.logTrace(e);
            throw new GroupsException("Problem retrieving groups: " + e);
        } finally {
            RDBMServices.closeResultSet(rs);
            RDBMServices.closePreparedStatement(ps);
            RDBMServices.releaseConnection(conn);
        }

        return retMap;
    }
    
    /**
     * Return all member keys for this group.
     */
    public MembershipKeys getAllMemberships(IEntityGroup group) throws GroupsException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql =
            "select group_id, member_service, member_key, member_is_group from up_group_membership " +
            "where group_id = ?";

        Set groupKeys = new HashSet();
        Set entityKeys = new HashSet();
        MembershipKeys membershipKeys = new MembershipKeys();

        // retrieve all the memberships from the db
        try {
            conn = RDBMServices.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, group.getLocalKey());
            
            rs = ps.executeQuery();

            String memberService;
            String memberKey;
            String memberIsGroup;

            while (rs.next()) {
                memberService = rs.getString("member_service");
                memberKey = rs.getString("member_key");
                memberIsGroup = rs.getString("member_is_group");
                
                if (MEMBER_IS_GROUP.equals(memberIsGroup)) {
                    groupKeys.add(buildMemberKey(memberService, memberKey));
                } else {
                    entityKeys.add(memberKey);
                }
            }
        } catch (Exception e) {
            Utils.logTrace(e);
            throw new GroupsException(
                "Problem loading group memberships: " + e.getMessage());
        } finally {
            RDBMServices.closeResultSet(rs);
            RDBMServices.closePreparedStatement(ps);
            RDBMServices.releaseConnection(conn);
        }
        
        membershipKeys.setEntityKeys(entityKeys);
        membershipKeys.setGroupKeys(groupKeys);
        return membershipKeys;
    }
    
    private String buildLocalKey(String localKey) {
        return new StringBuffer(serviceName).
    		append(GROUP_NODE_SEPARATOR).append(localKey).toString();
    }
    
    private String buildMemberKey(String memberService, String key) {
        return new StringBuffer(memberService).
    		append(GROUP_NODE_SEPARATOR).append(key).toString();
    }

    public DirectedGraph getMemberships(Map groups) throws GroupsException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = "select group_id, member_service, member_key, member_is_group from up_group_membership";

        Set vertexSet = new HashSet(groups.size());
        Set edgeSet = new HashSet();
        
        DefaultDirectedGraph groupStructure =
            GraphFactory.instance().newGraph();
        
        GroupProxyFactory proxyFactory = GroupProxyFactory.getInstance();

        // this will improve performance
        groupStructure.setAllowMultipleEdges(true);

        IEntityGroup gr;
        Iterator itr = groups.keySet().iterator();
        while (itr.hasNext()) {
            gr = (IEntityGroup) groups.get(itr.next());
            vertexSet.add(proxyFactory.getProxy(gr));
        }
        
        log.info("EveGroupStore : getting group memberships...");

        // retrieve all the memberships from the db
        try {
            conn = RDBMServices.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            String groupId = null;
            String memberService = null;
            String memberKey = null;
            String memberIsGroup = null;
            CompositeEntityIdentifier ei = null;

            while (rs.next()) {
                groupId = rs.getString("group_id");
                memberService = rs.getString("member_service");
                memberKey = rs.getString("member_key");
                memberIsGroup = rs.getString("member_is_group");
                
                String parentKey = buildLocalKey(groupId);

                IEntityGroup parentGroup = (IEntityGroup) groups.get(parentKey);
                
                if (parentGroup == null) {
                    log.error("EveGroupStore : inconsistent membership table. No group exists with group ID: " + groupId);
                }
                
                String fullMemberKey = buildMemberKey(memberService, memberKey);

                IGroupMember groupMember = null;
                if (MEMBER_IS_GROUP.equals(memberIsGroup)) {
                    groupMember = getMemberGroup(groups, memberService, fullMemberKey);
                } else if (parentGroup != null) {
                    groupMember = entityInstance(memberKey, parentGroup
                        .getLeafType(), memberService);
                }

                if (log.isDebugEnabled()) {
                    log.debug("EveGroupStore::loadMemberships : adding member to group: "
                        + fullMemberKey + ", " + parentKey);
                }

                if (parentGroup != null) {
                    if (groupMember != null) {
                        Object memberOrProxy = groupMember;
                        if (MEMBER_IS_GROUP.equals(memberIsGroup)) {
                            memberOrProxy = proxyFactory.getProxy(
                                groupMember.getKey());
                        }
                        vertexSet.add(memberOrProxy);
                        edgeSet.add(groupStructure.getEdgeFactory().createEdge(
                            proxyFactory.getProxy(parentGroup), memberOrProxy));
                    } else {
                        log.error(
                            "EveGroupStore : group has non-existent member - group: " +
                            groupId + ", member: " + fullMemberKey);
                    }
                }
                
            }
        } catch (Exception e) {
            Utils.logTrace(e);
            throw new GroupsException(
                "Problem loading memberships: " + e.getMessage());
        } finally {
            RDBMServices.closeResultSet(rs);
            RDBMServices.closePreparedStatement(ps);
            RDBMServices.releaseConnection(conn);
        }

        log.info("EveGroupStore : adding " + vertexSet.size() + " vertices.");

        groupStructure.addAllVertices(vertexSet);

        log.info("EveGroupStore : adding " + edgeSet.size() + " edges.");
        
        groupStructure.addAllEdges(edgeSet);

        groupStructure.setAllowMultipleEdges(false);
        return groupStructure;
    }
    
    private IEntityGroup getMemberGroup(Map groups, String memberService,
        String memberKey)
    throws GroupsException {
        IEntityGroup group = (IEntityGroup) groups.get(memberKey);
        
        if (group == null) {
            
            if (serviceName.equals(memberService)) {
                log.error("EveGroupStore : inconsistent membership table. No group exists with member key: " + memberKey);
            } else {
                // Wrap in an ExternalGroup object because the
                // uber GroupService is not yet initialized and
                // the external group service in question might not
                // yet be available. The group service will replace
                // these with the real group objects when accessed.
                group = new ExternalGroup(memberKey);
                
                if (log.isDebugEnabled()) {
                    log.debug("EveGroupStore::getMemberGroup : wrapping external group: " + group);
                }
            }
        }
        
        return group;
    }
    
    private IEntity entityInstance(String key, Class type, String service) throws GroupsException {
		if (serviceName.equals(service)) {
			return new Entity(key, type);
		}
		IEntity ent = GroupService.getEntity(key, type, service);
		return ent;
	}
    
    // RDBMEntityGroupStore methods
    
    public IEntityGroup newInstance(Class type) throws GroupsException {
        if (EntityTypes.getEntityTypeID(type) == null) {
            throw new GroupsException("Invalid group type: " + type);
        }
        try {
            return new Group(getNextKey(), type);
        } catch (Exception ex) {
            Utils.logTrace(ex);
            throw new GroupsException("Could not create new group: "
                + ex.getMessage());
        }
    }

    protected IEntityGroup newInstance(String newKey, Class newType,
        String newCreatorID, String newName, String newDescription)
        throws GroupsException {
        return new Group(newKey, newType, newName, newCreatorID, newDescription);
    }
    
    protected ILockableEntityGroup newLockableInstance(String newKey,
        Class newType, String newCreatorID, String newName,
        String newDescription) throws GroupsException {
        return new LockableGroup(newKey, newType, newName, newCreatorID, newDescription);
    }
    
    public ILockableEntityGroup lockableInstance(String newKey,
        Class newType, String newCreatorID, String newName,
        String newDescription) throws GroupsException {
        return newLockableInstance(newKey, newType, newCreatorID, newName,
            newDescription);
    }
}
