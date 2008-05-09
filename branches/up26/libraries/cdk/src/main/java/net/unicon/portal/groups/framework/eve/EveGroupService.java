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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Name;
import javax.naming.NamingException;

import net.unicon.portal.groups.framework.eve.graph.GraphFactory;

import org._3pq.jgrapht.DirectedGraph;
import org._3pq.jgrapht.Edge;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.concurrency.IEntityLock;
import org.jasig.portal.concurrency.LockingException;
import org.jasig.portal.groups.ComponentGroupServiceDescriptor;
import org.jasig.portal.groups.CompositeEntityIdentifier;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntityGroupStoreFactory;
import org.jasig.portal.groups.IEntitySearcher;
import org.jasig.portal.groups.IEntitySearcherFactory;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IEntityStoreFactory;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.services.EntityCachingService;
import org.jasig.portal.services.EntityLockService;
import org.jasig.portal.services.GroupService;


/**
 * Updating group/members procedure:
 * 
 * 1) Find group
 * 2) set attributes or add/remove members
 * 3) update group or updateMembers.
 * 
 * The Eve group service is designed for a mostly read operation environment.
 * It is based on a graph implementation provided by jgrapht. The basic
 * operation is as follows. 
 * 
 * When the service is initialized, it asks the group store for ALL the 
 * membership information and stores it in a graph object called the mirror.
 * The mirror is immutable and never synchronized, so the read operations
 * are fast.  Any write or updating operation is transactional and
 * performed within synchronized blocks.  A copy is made of the mirror,
 * the operations are performed on the copy, and then the copy is committed.
 * 
 * Bootstrapping is done a little odd due to the requirement to support
 * external (i.e. pags) group members. This is necessary to solve a
 * chicken and egg problem due to the composite group service is
 * in an initialization state when this group service is being initialized.
 * So if you ask the composite group service to fetch a group that
 * belongs to an external service, it will begin initializing again and
 * an infinite loop will ensue.
 * 
 * The initial group store query is stored in the bootstrapStructure attribute
 * with the vertices being GroupProxy wrappers for groups, and actual entities
 * for users. This structure becomes the group structure after
 * primeServiceObjects() is completed, and the bootstrap reference is released.
 * 
 * Memory considerations:
 * The graph object will consume and hold a substantial amount of memory
 * and care needs to be given so that deployments can support a particular
 * set of groups, entities and memberships. Below is an estimation for
 * how much memory will be needed for a given data set.
 * 
 * Gn - number of groups
 * En - number of entities
 * Mn - number of memberships
 * 
 * Average group sizes:
 *     Memory = 729*Gn + 176*En + 222*Mn
 * 
 * Worst case group sizes:
 *     Memory = 1024*Gn + 176*En + 222*Mn
 * 
 * Worst case becomes irrelavent as Mn becomes much larger than Gn.  
 * 
 */
public class EveGroupService  
implements IEveGroupService
{
	private static final Log log = LogFactory.getLog(EveGroupService.class);
    private final boolean disableClone = "true".equals(System.getProperty("net.unicon.portal.groups.framework.eve.disableClone", "false"));
	
    // This is used to bootstrap the group structure because
    // the mirror is unmodifiable.
    // Once the service name gets set and all the group service objects
    // are primed (made to point to this service), this graph
    // will no longer be used.
	private DirectedGraph bootstrapStructure = null;
	private Map bootstrapGroups = null;
	
	// Made volatile to ensure the most recent version.
	// The read operations will read from the mirror
	private volatile DirectedGraph mirror = null;
    
	private static Map singletons = new HashMap();
	
	
	 // Describes the attributes of this service. See compositeGroupServices.xml.
    private ComponentGroupServiceDescriptor serviceDescriptor;

    private IEntityGroupStore groupStore;
    private IEntityStore entityStore;

    // Entity searcher
    private IEntitySearcher entitySearcher;
    
    
    private Name serviceName;
    
    // keep a map of proxy objects to minimize the number
    // of objects
    private Map proxies = new HashMap();
    
    private Object lock = new Object();
    
    public static synchronized EveGroupService instance(
        ComponentGroupServiceDescriptor svcDescriptor) throws GroupsException {
        
        String serviceName = svcDescriptor.getName();
        if (serviceName == null || "".equals(serviceName)) {
            serviceName = "DefaultEveGroupService";
        }
        
        EveGroupService service = (EveGroupService)singletons.get(serviceName);
        
        if (service == null) {
            service = new EveGroupService(svcDescriptor);
            singletons.put(serviceName, service);
        }
        
        return service;
    }
    
    private IEntityGroup findGroupFromProxy(GroupProxy proxy) {
        if (proxy != null) {
            return proxy.getUnderlyingGroup(); 
        }
        return null;
    }
    
    private Object getAppropriateVertex(IGroupMember gm) throws GroupsException {
        if (gm.isGroup()) {
            return GroupProxyFactory.getInstance().getProxy((IEntityGroup)gm);
        }
        return gm;
    }
    
    private void removeProxy(String key) {
        proxies.remove(key);
    }
    

	/**
	 * ReferenceGroupsService constructor.
	 */
	protected EveGroupService(ComponentGroupServiceDescriptor svcDescriptor)
	throws GroupsException
	{
	    super();
        serviceDescriptor = svcDescriptor;
        initialize();
	}
	
	
	protected IEveGroupStore getEveGroupStore() throws GroupsException {
	    return (IEveGroupStore)getGroupStore();
	}
	
	
    public void setServiceName(Name newServiceName) {
        
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::setServiceName() name: " + newServiceName);
        }
        
        this.serviceName = newServiceName;
        
        try {
            primeServiceObjects();
        } catch (GroupsException ge) {
            Utils.logTrace(ge);
        }
    }
    
    /**
     * Returns the FULLY-QUALIFIED <code>Name</code> of the service, which 
     * may not be known until the composite service is assembled.  
     */
    public javax.naming.Name getServiceName() {
    	return serviceName;
    }
    
    
    // This method sets all groups and entities to point to this service and
    // sets up the shadow.
	protected void primeServiceObjects() throws GroupsException {
		
        log.info("EveGroupService : priming service objects...");
		Iterator itr = bootstrapGroups.values().iterator();
		while (itr.hasNext()) {
		    primeServiceObject((IGroupMember)itr.next());
		}
        setMirror(bootstrapStructure);
        bootstrapStructure = null;
        bootstrapGroups = null;
        log.info("EveGroupService : priming service objects complete.");

	}
	
	protected void primeServiceObject(IGroupMember gm) throws GroupsException {
	    if (gm instanceof IEveGroupMember) {
	        ((IEveGroupMember)gm).setEveGroupService(this);
	    }
	}
	
    public synchronized IEntityGroup newGroup(Class type) throws GroupsException {
        throwExceptionIfNotInternallyManaged();
        
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::newGroup() type: " + type);
        }
        
        IEntityGroup group = groupStore.newInstance(type);
        group.setLocalGroupService(this);
        
        ensureMemberExists(group);
        
        return group;
    }
    
    public void addMember(IEntityGroup group, IGroupMember gm)
    throws GroupsException {
        
    }
    
    public void removeMember(IEntityGroup group, IGroupMember gm) throws GroupsException {
        
    }
    
    /**
     * Answers if <code>group</code> contains <code>member</code>.  
     * If the group belongs to another service and the present service is 
     * not editable, simply return false.
     * @return boolean
     * @param group org.jasig.portal.groups.IEntityGroup
     * @param member org.jasig.portal.groups.IGroupMember
     */
	
    public boolean contains(IEntityGroup group, IGroupMember member) throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::contains() parent, gm: " + group + ", " + member);
        }
        
        boolean b = ( isForeign(member) && ! isEditable() )
          ? false
          : getMirror().containsEdge(getAppropriateVertex(group),
              getAppropriateVertex(member));

        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::contains() parent, gm, result: " + group + ", " + member + ", " + b);
        }
        return b;
    }

	public Iterator getAllContainingGroups(IGroupMember member) throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::getAllContainingGroups() gm: " + member);
        }
        
	    Set s = __getAllContainingGroups(new HashSet(), member);
	    
	    if (log.isDebugEnabled()) {
            log.debug("EveGroupService::getAllContainingGroups() gm: results size: " + member + ": " + s.size());
            Iterator itr = s.iterator();
            while (itr.hasNext()) {
                log.debug("EveGroupService::getAllContainingGroups() group: " + itr.next());
            }
        }
        
	    return s.iterator();
	}
    
    private Set __getAllContainingGroups(Set s, IGroupMember gm) throws GroupsException {
        Iterator i = getContainingGroups(gm);
        while ( i.hasNext() )
        {
            IGroupMember gmi = (IGroupMember) i.next();
            s.add(gmi);
            __getAllContainingGroups(s, gmi);
        }
        return s;
    }
    
    
	private Set __getContainingGroups(Set s,
	    IGroupMember member, boolean recurse) throws GroupsException {
	    
	    List edges = getMirror().incomingEdgesOf(
            getAppropriateVertex(member));
	    Edge e = null;
	    
        GroupProxy proxy = null;
	    Iterator itr = edges.iterator();
	    while (itr.hasNext()) {
	        e = (Edge)itr.next();
            proxy = (GroupProxy)e.getSource();
            
	        s.add(findGroupFromProxy(proxy));
	        if (recurse) {
	            __getContainingGroups(s, proxy, true);
	        }
	    }
	    
	    return s;
	}
	
	public Iterator getAllEntities(IEntityGroup group) throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::getAllEntities() group: " + group);
        }
        
        if (isForeign(group)) {
            return group.getAllEntities();
        } else {
            Set s = __getMembers(new HashSet(), group, MemberType.ENTITY, true);
            
            if (log.isDebugEnabled()) {
                log.debug("EveGroupService::getAllEntities() group: results size: " + group + ": " + s.size());
                Iterator itr = s.iterator();
                while (itr.hasNext()) {
                    log.debug("EveGroupService::getAllEntities() entity: " + itr.next());
                }
            }
            
            return s.iterator();
        }
	}
	
	
	public Iterator getAllMembers(IEntityGroup group) throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::getAllMembers() group: " + group);
        }
        
        if (isForeign(group)) {
            return group.getAllMembers();
        } else {
            Set s = __getAllMembersSet(new HashSet(), group);
            
            if (log.isDebugEnabled()) {
                log.debug("EveGroupService::getAllMembers() group: results size: " + group + ": " + s.size());
                Iterator itr = s.iterator();
                while (itr.hasNext()) {
                    log.debug("EveGroupService::getAllMembers() member: " + itr.next());
                }
            }
            
            return s.iterator();
        }
	}
	
	public Iterator getContainingGroups(IGroupMember member) throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::getContainingGroups() member: " + member);
        }
        return GroupService.getCompositeGroupService().
            findContainingGroups(member);
	}
	
	private Set __getMembers(Set s, IEntityGroup group,
	    MemberType type, boolean recurse) throws GroupsException {
        
	    Edge e = null;
	    IGroupMember gm = null;
        GroupProxy proxy = null;
	    Iterator itr = getMirror().outgoingEdgesOf(
            getAppropriateVertex(group)).iterator();
	    while (itr.hasNext()) {
	        e = (Edge)itr.next();
	        gm = (IGroupMember)e.getTarget();
	        
	        if (MemberType.ANY.equals(type) ||
	            (MemberType.ENTITY.equals(type) && gm.isEntity()) ||
	            (MemberType.GROUP.equals(type) && gm.isGroup())) {
	            
                if (gm.isGroup()) {
                    proxy = (GroupProxy)gm;
                    s.add(findGroupFromProxy(proxy));
                } else {
                    s.add(gm);
                }
	        }
	        if (recurse && gm.isGroup()) {
	            __getMembers(s, (IEntityGroup)gm, type, true);
	        }
	    }
	    return s;
	}
	
	public Set getMembersSet(IEntityGroup group) throws GroupsException {
		return __getMembers(new HashSet(), group, MemberType.ANY, false);
	}
    
    private Set __getAllMembersSet(Set s, IEntityGroup eg)
    throws GroupsException {
        Iterator i = eg.getMembers();
        while (i.hasNext()) {
            IGroupMember gmi = (IGroupMember) i.next();
            if (gmi != null) {
	            s.add(gmi);
	            if (gmi.isGroup()) {
	                __getAllMembersSet(s, (IEntityGroup)gmi);
	            }
            }
        }
        return s;
    }
	
	public Iterator getEntities(IEntityGroup group) throws GroupsException {
        if (isForeign(group)) {
            return group.getEntities();
        } else {
            return __getMembers(new HashSet(), group, MemberType.ENTITY, false).iterator();
        }
	}
	
	public Iterator getMembers(IEntityGroup group) throws GroupsException {
        if (isForeign(group)) {
            return group.getMembers();
        } else {
            return getMembersSet(group).iterator();
        }
	}
	
	public IEntityGroup getMemberGroupNamed(IEntityGroup group, String name) throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::getMemberGroupNamed() group, name: " + group + ", " + name);
        }
        
        if (isForeign(group)) {
            return group.getMemberGroupNamed(name);
        } else {
            Iterator itr = getMembers(group);
            IEntityGroup retValue = null;
            while (retValue == null && itr.hasNext()) {
                IGroupMember gm = (IGroupMember)itr.next();
                if (gm.isGroup() && name.equals(((IEntityGroup)gm).getName())) {
                    retValue = (IEntityGroup)gm;
                }
            }
            
            if (log.isDebugEnabled()) {
                log.debug("EveGroupService::getMemberGroupNamed() group, name, result: " + group + ", " + name + ", " + retValue);
            }
            
            return retValue;
        }
	}
	
	public boolean hasMembers(IEntityGroup group) throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::hasMembers() group: " + group);
        }
        
        if (isForeign(group)) {
            return group.hasMembers();
        } else {
            boolean b = false;
            
            b = getMirror().outgoingEdgesOf(
                getAppropriateVertex(group)).size() > 0;
            
            if (log.isDebugEnabled()) {
                log.debug("EveGroupService::hasMembers() group, result: " + group + ", " + b);
            }
            
            return b;
        }
	}
	
	public boolean isDeepMemberOf(IGroupMember member, IGroupMember potentialContainingGroup) throws GroupsException {
        boolean b = false;
        
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::isDeepMemberOf() member, potentialContainingGroup: " +
                member + ", " + potentialContainingGroup);
        }
        
        if (!potentialContainingGroup.isEntity()) {
            if (member.isMemberOf(potentialContainingGroup)) {
                b = true;
            }
            
            Iterator itr = potentialContainingGroup.getMembers();
            while (!b && itr.hasNext()) {
                b = isDeepMemberOf(member, (IGroupMember)itr.next());
            }
        }
		
		if (log.isDebugEnabled()) {
            log.debug("EveGroupService::isDeepMemberOf() member, potentialContainingGroup, result: " +
                member + ", " + potentialContainingGroup + ", " + b);
        }
		
		return b;
	}
    
	public boolean isMemberOf(IGroupMember member, IGroupMember potentialContainingGroup) throws GroupsException {
	    boolean b = false;
        
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::isMemberOf() member, potentialContainingGroup: " +
                member + ", " + potentialContainingGroup);
        }
	    
		if (potentialContainingGroup.isGroup()) {
		    b = ((IEntityGroup)potentialContainingGroup).contains(member);
		}
		
		if (log.isDebugEnabled()) {
            log.debug("EveGroupService::isMemberOf() member, potentialContainingGroup, result: " +
                member + ", " + potentialContainingGroup + ", " + b);
        }
		
		return b;
	}
	
	public synchronized void deleteGroup(IEntityGroup group)
	throws GroupsException {
        if (isForeign(group)) {
            throw new GroupsException("Cannot delete foreign group: " + group);
        }
        
	    if (log.isDebugEnabled()) {
            log.debug("EveGroupService::deleteGroup() group: " + group);
        }
	    
	    throwExceptionIfNotInternallyManaged();
	    
	    getGroupStore().delete(group);
	    
        // begin graph transaction
        DirectedGraph copy = cloneMirror();
        
	    Iterator itr = group.getMembers();
	    while (itr.hasNext()) {
            removeMember(copy, group, (IGroupMember)itr.next());
	    }
	    
	    copy.removeVertex(getAppropriateVertex(group));
        
        removeProxy(group.getKey());
	    
	    setMirror(copy);
        // end graph transaction

	}
	
	
	public synchronized void updateGroup(IEntityGroup group)
	throws GroupsException {
        if (isForeign(group)) {
            throw new GroupsException("Cannot update foreign group: " + group);
        }
        
	    if (log.isDebugEnabled()) {
            log.debug("EveGroupService::updateGroup() group: " + group);
        }
	    
	    throwExceptionIfNotInternallyManaged();
	    
	    getGroupStore().update(group);
        
        IEveGroup eveGroup = (IEveGroup)group;
        if (!getMirror().containsVertex(getAppropriateVertex(group)) ||
            eveGroup.hasDirtyMembers()) {
            // begin graph transaction
            DirectedGraph copy = cloneMirror();
            ensureMemberExists(copy, group);
            
            if (eveGroup.hasDirtyMembers()) {
                syncMemberships(copy, group);
            }
            
            setMirror(copy);
            // end graph transaction
            
        }
	}
	
	public synchronized void updateGroupMembers(IEntityGroup group)
	throws GroupsException {
        if (isForeign(group)) {
            throw new GroupsException("Cannot update foreign group members: " + group);
        }
        
	    if (log.isDebugEnabled()) {
            log.debug("EveGroupService::updateGroupMembers() group: " + group);
        }
	    
	    throwExceptionIfNotInternallyManaged();
	    
	    if (((IEveGroup)group).hasDirtyMembers()) {
	        getGroupStore().updateMembers(group);
            
            // begin graph transaction
            DirectedGraph copy = cloneMirror();
            syncMemberships(copy, group);
            setMirror(copy);
            // end graph transaction
	    }
	}
    
    private void syncMemberships(DirectedGraph copy, IEntityGroup group)
    throws GroupsException {
        IEveGroup eveGroup = (IEveGroup)group;
        
        IGroupMember[] members = eveGroup.getAddedMembers();
        for (int i=0; i<members.length; i++) {
            addMember(copy, group, members[i]);
        }
        
        members = eveGroup.getRemovedMembers();
        for (int i=0; i<members.length; i++) {
            removeMember(copy, group, members[i]);
        }
    }
    
	private void addMember(DirectedGraph copy, IEntityGroup group,
	    IGroupMember gm)
	throws GroupsException {
	    if (log.isDebugEnabled()) {
            log.debug("EveGroupService::addMember() parent, gm: " + group + ", " + gm);
        }
	    
        ensureMemberExists(copy, gm);

	    // check if the added member is an external group and
	    // add it to the vertex set if necessary
	    
	    if (gm.isGroup()) {
	        IEntityGroup ieg = (IEntityGroup)gm;
	        if (!serviceName.equals(ieg.getServiceName())) {
	            copy.addVertex(getAppropriateVertex(gm));
	        }
	    }
	    
	    copy.addEdge(getAppropriateVertex(group),
            getAppropriateVertex(gm));
    }
	
	
    private void removeMember(DirectedGraph copy, IEntityGroup group,
        IGroupMember gm) throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::removeMember() parent, gm: " + group + ", " + gm);
        }
        
        copy.removeEdge(getAppropriateVertex(group),
            getAppropriateVertex(gm));
        
        // check if the removed member is an external group and
	    // remove it from the vertex set if necessary
	    
	    if (gm.isGroup()) {
	        IEntityGroup ieg = (IEntityGroup)gm;
            Object vertex = getAppropriateVertex(ieg);
	        if (isForeign(gm) && copy.edgesOf(vertex).size() == 0) {
	            copy.removeVertex(vertex);
	        }
	    }
    }
	
	public synchronized void updateGroup(ILockableEntityGroup group, boolean renewLock) throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::updateGroup() group, renewLock: " + group + ", " + renewLock);
        }
        
        if (isForeign(group)) {
            throw new GroupsException("Cannot update foreign group: " + group);
        }
        
	    throwExceptionIfNotInternallyManaged();

	    try {
            if (!group.getLock().isValid()) {
                throw new GroupsException("Could not update group "
                    + group.getKey() + " has invalid lock.");
            }
            
            getGroupStore().update(group);
            
            IEveGroup eveGroup = (IEveGroup)group;
            if (!getMirror().containsVertex(getAppropriateVertex(group)) ||
                eveGroup.hasDirtyMembers()) {
                // begin graph transaction
                DirectedGraph copy = cloneMirror();
                ensureMemberExists(copy, group);
                
                if (eveGroup.hasDirtyMembers()) {
                    syncMemberships(copy, group);
                }
                
                setMirror(copy);
                // end graph transaction
            }

            if (renewLock) {
                group.getLock().renew();
            } else {
                group.getLock().release();
            }

        } catch (LockingException le) {
            Utils.logTrace(le);
            throw new GroupsException("Problem updating group "
                + group.getKey() + " : " + le.getMessage(), le);
        }		
	}
	
	public synchronized void updateGroupMembers(ILockableEntityGroup group,
        boolean renewLock) throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::updateGroupMembers() group, renewLock: " + group + ", " + renewLock);
        }
        
        if (isForeign(group)) {
            throw new GroupsException("Cannot update foreign group members: " + group);
        }
        
	    throwExceptionIfNotInternallyManaged();

	    try {
            if (!group.getLock().isValid()) {
                throw new GroupsException("Could not update group "
                    + group.getKey() + " has invalid lock.");
            }

            if (((IEveGroup)group).hasDirtyMembers()) {
                getGroupStore().updateMembers(group);
                
                // begin graph transaction
                DirectedGraph copy = cloneMirror();
                syncMemberships(copy, group);
                setMirror(copy);
                // end graph transaction
    	    }
            
            if (renewLock) {
                group.getLock().renew();
            } else {
                group.getLock().release();
            }

        } catch (LockingException le) {
            Utils.logTrace(le);
            throw new GroupsException("Problem updating group "
                + group.getKey() + " : " + le.getMessage(), le);
        }
	}
	
	/**
     * Returns a pre-existing <code>IEntityGroup</code> or null if it does not
     * exist.
     */
    public IEntityGroup findGroup(CompositeEntityIdentifier ent) throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::findGroup() ent: " + ent);
        }
        
        return loadGroup(ent.getLocalKey());
    }
    
    
    public IEntityGroup findGroup(String key) throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::findGroup() key: " + key);
        }
        
        return loadGroup(newGroupCompositeEntityIdentifier(key).getLocalKey());
    }
    
    private IEntityGroup loadGroup(String localKey)
        throws GroupsException {
        IEntityGroup group = groupStore.find(localKey);
        if (group != null) {
            group.setLocalGroupService(this);
            
            establishMemberships(group);
        }
        
	    if (log.isDebugEnabled()) {
            log.debug("EveGroupService::findGroup() localKey: " +
                localKey + ": " + group);
        }
	    
        return group;
    }
    
    protected ILockableEntityGroup getLockableGroup(String key)
    throws GroupsException {
        
        ILockableEntityGroup lockableGroup = null;
        
        IEntityGroup gr = findGroup(key);
        
        if (gr != null) {
            String groupId =
                ((IEveGroup)gr).getCompositeEntityIdentifier().getLocalKey();
            lockableGroup =
                getEveGroupStore().lockableInstance(groupId,
                    gr.getEntityType(), gr.getCreatorID(), gr.getName(),
                    gr.getDescription());
        }
        
        return lockableGroup;
    }
    
    
    /**
     * Returns a pre-existing <code>ILockableEntityGroup</code> or null if the
     * group is not found.
     */
    public ILockableEntityGroup findGroupWithLock(String key, String owner,
        int secs) throws GroupsException {

        throwExceptionIfNotInternallyManaged();

        Class groupType = org.jasig.portal.EntityTypes.GROUP_ENTITY_TYPE;
        try {
            IEntityLock lock = (secs == 0) ? EntityLockService.instance()
                .newWriteLock(groupType, key, owner) : EntityLockService
                .instance().newWriteLock(groupType, key, owner, secs);
                
            ILockableEntityGroup lockableGroup = getLockableGroup(key);
            
            if (lockableGroup == null) {
                lock.release();
            } else {
                lockableGroup.setLock(lock);
                lockableGroup.setLocalGroupService(this);
            }

            return lockableGroup;
        } catch (LockingException le) {
            Utils.logTrace(le);
            throw new GroupsException("Problem getting lock for group " + key
                + " : " + le.getMessage(), le);
        }

    }

    
	private synchronized void establishMemberships(IEntityGroup group)
	throws GroupsException {
        // begin graph transaction
	    DirectedGraph copy = cloneMirror();
	    
	    ensureMemberExists(copy, group);
		    
	    MembershipKeys memberKeys = getEveGroupStore().getAllMemberships(group);
	    
        Set removedEdges = new HashSet(copy.outDegreeOf(getAppropriateVertex(group)));
	    Iterator itr = copy.outgoingEdgesOf(
            getAppropriateVertex(group)).iterator();
        while (itr.hasNext()) {
            removedEdges.add(itr.next());
	    }
        
        itr = removedEdges.iterator();
        while (itr.hasNext()) {
	        copy.removeEdge(((Edge)itr.next()));
        }
	    
	    IGroupMember gm = null;
	    
	    // set group memberships
	    for (int i=0; i<memberKeys.getGroupKeys().length; i++) {
	        gm = GroupService.findGroup(memberKeys.getGroupKeys()[i]);
	        ensureMemberExists(copy, gm);
	        copy.addEdge(getAppropriateVertex(group),
                getAppropriateVertex(gm));
	    }
	    
	    // set entity memberships
	    for (int i=0; i<memberKeys.getEntityKeys().length; i++) {
	        gm = GroupService.getEntity(memberKeys.getEntityKeys()[i],
	            group.getLeafType());
	        ensureMemberExists(copy, gm);
	        copy.addEdge(getAppropriateVertex(group),
                getAppropriateVertex(gm));
	    }
	    
	    setMirror(copy);
        // end graph transaction
	}
    
    
	private void ensureMemberExists(DirectedGraph copy,
        IGroupMember gm)
	throws GroupsException {
	    if (copy != null) {
	        if (gm != null && !copy.containsVertex(getAppropriateVertex(gm))) {
	            copy.addVertex(getAppropriateVertex(gm));
	        }
        }
	}
	
	// auto-transactional version
	private void ensureMemberExists(IGroupMember gm)
    throws GroupsException {
        if (gm != null && !getMirror().containsVertex(getAppropriateVertex(gm))) {
            // begin graph transaction
            synchronized (lock) {
                DirectedGraph copy = cloneMirror();
                ensureMemberExists(copy, gm);
                setMirror(copy);
            }
            // end graph transaction
        }
	}
	
	private synchronized DirectedGraph cloneMirror()
	throws GroupsException {
	    return (disableClone ? mirror : GraphFactory.instance().cloneGraph(mirror));
	}
	
	private synchronized void setMirror(DirectedGraph graph)
	throws GroupsException {
//        mirror = GraphFactory.instance().unmodifiableClone(graph);
        mirror = graph;
	}
    
	protected DirectedGraph getMirror() throws GroupsException {
	    return mirror;
	}

	public Iterator findContainingGroups(IGroupMember gm) throws GroupsException {
	    
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::findContainingGroups() member: " + gm);
        }
        
        // if no vertex exists in the graph, the group member
        // cannot have any containing groups in this service
        if (!getMirror().containsVertex(getAppropriateVertex(gm))) {
            if (log.isDebugEnabled()) {
                log.debug("EveGroupService::findContainingGroups() member, result: " + gm + ", none");
            }
            
            return Collections.EMPTY_SET.iterator();
        }
        
	    Set s = __getContainingGroups(new HashSet(), gm, false);
        
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::findContainingGroups() member: size: " + gm + ": " + s.size());
            Iterator itr = s.iterator();
            while (itr.hasNext()) {
                log.debug("EveGroupService::findContainingGroups() group: " + itr.next());
            }
        }
        
        return s.iterator();
	}
	
	
	/**
     * Removes the <code>ILockableEntityGroup</code> from its containing
     * groups. The <code>finally</code> block tries to release any groups that
     * are still locked, which can occur if an attempt to remove the group from
     * one of its containing groups fails and throws a GroupsException. In this
     * event, we do not try to roll back any successful removes, since that
     * would probably fail anyway.
     * 
     * @param group
     *            ILockableEntityGroup
     */
    private void removeDeletedGroupFromContainingGroups(
        ILockableEntityGroup group) throws GroupsException {
        Iterator itr;
        IEntityGroup containingGroup = null;
        ILockableEntityGroup lockableGroup = null;
        IEntityLock lock = null;
        List lockableGroups = new ArrayList();
        
        try {
            String lockOwner = group.getLock().getLockOwner();
            for (itr = group.getContainingGroups(); itr.hasNext();) {
                containingGroup = (IEntityGroup) itr.next();
                lockableGroup = GroupService.findLockableGroup(containingGroup
                    .getKey(), lockOwner);
                if (lockableGroup != null) {
                    lockableGroups.add(lockableGroup);
                }
            }
            for (itr = lockableGroups.iterator(); itr.hasNext();) {
                lockableGroup = (ILockableEntityGroup) itr.next();
                lockableGroup.removeMember(group);
                lockableGroup.updateMembers();
            }
        } catch (GroupsException ge) {
            ge.printStackTrace();
            Utils.logTrace(ge);
            throw new GroupsException("Could not remove deleted group "
                + group.getKey() + " from parent : " + ge.getMessage(), ge);
        } finally {
            for (itr = lockableGroups.iterator(); itr.hasNext();) {
                lock = ((ILockableEntityGroup) itr.next()).getLock();
                try {
                    if (lock.isValid()) {
                        lock.release();
                    }
                } catch (LockingException le) {
                    Utils.logTrace(le);
                    log
                        .error("AbstractGroupService.removeDeletedGroupFromContainingGroups(): "
                            + "Problem unlocking parent group: "
                            + le.getMessage());
                }
            }
        }
    }

    /**
     * Removes the <code>ILockableEntityGroup</code> from the cache and the
     * store, including both parent and child memberships.
     * 
     * @param group
     *            ILockableEntityGroup
     */
    public void deleteGroup(ILockableEntityGroup group) throws GroupsException {
        if (isForeign(group)) {
            throw new GroupsException("Cannot delete foreign group: " + group);
        }
        
        throwExceptionIfNotInternallyManaged();
        try {
            if (group.getLock().isValid()) {
                removeDeletedGroupFromContainingGroups(group);
                deleteGroup((IEntityGroup) group);
            } else {
                throw new GroupsException("Could not delete group "
                    + group.getKey() + " has invalid lock.");
            }
        } catch (LockingException le) {
            Utils.logTrace(le);
            throw new GroupsException("Could not delete group "
                + group.getKey() + " : " + le.getMessage(), le);
        } finally {
            try {
                group.getLock().release();
            } catch (LockingException le) {
                Utils.logTrace(le);
            }
        }
    }
    
    protected IEntityStore getEntityStore() {
        return entityStore;
    }

    private EntityIdentifier[] filterEntities(EntityIdentifier[] entities,
        IEntityGroup ancestor) throws GroupsException {
        ArrayList ar = new ArrayList(entities.length);
        for (int i = 0; i < entities.length; i++) {
            IGroupMember gm = this.getGroupMember(entities[i]);
            if (ancestor.deepContains(gm)) {
                ar.add(entities[i]);
            }
        }
        return (EntityIdentifier[]) ar.toArray(new EntityIdentifier[0]);
    }

    
    /**
     * Returns a pre-existing <code>ILockableEntityGroup</code> or null if the
     * group is not found.
     */
    public ILockableEntityGroup findGroupWithLock(String key, String owner)
        throws GroupsException {
        return findGroupWithLock(key, owner, 0);
    }

    /**
     * Finds the <code>IEntities</code> that are members of <code>group</code>.
     */
    public Iterator findMemberEntities(IEntityGroup group)
        throws GroupsException {
        if (isForeign(group)) {
            throw new GroupsException(
                "Cannot find member entities for foreign group: " + group);
        }
        
        return getGroupStore().findEntitiesForGroup(group);
    }

    /**
     * Returns and members for the <code>IEntityGroup</code>.
     * 
     * @param eg
     *            IEntityGroup
     */
    public Iterator findMembers(IEntityGroup eg) throws GroupsException {
        return eg.getMembers();
    }
    
    public Iterator findMemberGroups(IEntityGroup group)
    throws GroupsException {
        if (isForeign(group)) {
            throw new GroupsException(
                "Cannot find member groups for foreign group: " + group);
        }

        Set s = __getMembers(new HashSet(), group, MemberType.GROUP, false);
        
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService::findMemberGroups() group: " + group +
                " : " + s);
        }
        return s.iterator();
    }
    
    
    /**
     * Returns an <code>IEntity</code> representing a portal entity. This does
     * not guarantee that the underlying entity actually exists.
     */
    public IEntity getEntity(String key, Class type) throws GroupsException {
        IEntity entity = entityStore.newInstance(key, type);
        ((Entity)entity).setEveGroupService(this);
        return entity;
    }

    /**
     * Returns an <code>IGroupMember</code> representing either a group or a
     * portal entity. If the parm <code>type</code> is the group type, the
     * <code>IGroupMember</code> is an <code>IEntityGroup</code> else it is
     * an <code>IEntity</code>.
     */
    public IGroupMember getGroupMember(String key, Class type)
        throws GroupsException {
        IGroupMember gm = null;
        if (type == org.jasig.portal.EntityTypes.GROUP_ENTITY_TYPE)
            gm = findGroup(key);
        else
            gm = getEntity(key, type);
        return gm;
    }

    /**
     * Returns an <code>IGroupMember</code> representing either a group or a
     * portal entity, based on the <code>EntityIdentifier</code>, which
     * refers to the UNDERLYING entity for the <code>IGroupMember</code>.
     */
    public IGroupMember getGroupMember(
        EntityIdentifier underlyingEntityIdentifier) throws GroupsException {
        return getGroupMember(underlyingEntityIdentifier.getKey(),
            underlyingEntityIdentifier.getType());
    }

    /**
     * Returns the implementation of <code>IEntityGroupStore</code> whose
     * class name was retrieved by the PropertiesManager (see initialize()).
     */
    public IEntityGroupStore getGroupStore() throws GroupsException {
        return groupStore;
    }

    /**
     *  
     */
    protected ComponentGroupServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    /**
     * @exception org.jasig.portal.groups.GroupsException
     */
    protected void initialize() throws GroupsException {
        String eMsg = null;
        String svcName = getServiceDescriptor().getName();
        log.info("EveGroupService initializing.");
        
        if (log.isDebugEnabled()) {
            log.debug("EveGroupService descriptor attributes: " + svcName);
        }

        // print service descriptor attributes:
        for (Iterator i = getServiceDescriptor().keySet().iterator(); i
            .hasNext();) {
            String descriptorKey = (String) i.next();
            Object descriptorValue = getServiceDescriptor().get(descriptorKey);
            if (descriptorValue != null) {
                log.debug("  " + descriptorKey + " : " + descriptorValue);
            }
        }

        String groupStoreFactoryName = getServiceDescriptor()
            .getGroupStoreFactoryName();
        String entityStoreFactoryName = getServiceDescriptor()
            .getEntityStoreFactoryName();
        String entitySearcherFactoryName = getServiceDescriptor()
            .getEntitySearcherFactoryName();

        if (groupStoreFactoryName == null) {
            eMsg = "ReferenceGroupService.initialize(): (" + svcName
                + ") No Group Store factory specified in service descriptor.";
            log.info(eMsg);
        }

        else {
            try {
                IEntityGroupStoreFactory groupStoreFactory = (IEntityGroupStoreFactory) Class
                    .forName(groupStoreFactoryName).newInstance();
                groupStore = groupStoreFactory
                    .newGroupStore(getServiceDescriptor());
                
                if (! (groupStore instanceof IEveGroupStore) ) {
        		    throw new RuntimeException (
        		        "EveGroupService requires group store that implements IEveGroupStore");
        		}

                bootstrapGroups = getEveGroupStore().getGroups();
                bootstrapStructure = getEveGroupStore().getMemberships(bootstrapGroups);
            } catch (Exception e) {
                Utils.logTrace(e);
                eMsg = "AbstractGroupService.initialize(): Failed to instantiate group store ("
                    + svcName + "): " + e;
                log.error(eMsg);
                throw new GroupsException(eMsg, e);
            }
        }

        if (entityStoreFactoryName == null) {
            eMsg = "AbstractGroupService.initialize(): No Entity Store Factory specified in service descriptor ("
                + svcName + ")";
            log.info(eMsg);
        }

        else {
            try {
                IEntityStoreFactory entityStoreFactory = (IEntityStoreFactory) Class
                    .forName(entityStoreFactoryName).newInstance();
                entityStore = entityStoreFactory.newEntityStore();

                if (!(entityStore instanceof IEveEntityStore)) {
                    eMsg = "EntityStore must implement IEveEntityStore!";
                    log.error(eMsg);
                    throw new GroupsException(eMsg);
                }
                
            } catch (GroupsException ge) {
                Utils.logTrace(ge);
                throw ge;
            } catch (Exception e) {
                Utils.logTrace(e);
                eMsg = "AbstractGroupService.initialize(): Failed to instantiate entity store "
                    + e;
                log.error(eMsg);
                throw new GroupsException(eMsg, e);
            }
        }

        if (entitySearcherFactoryName == null) {
            eMsg = "AbstractGroupService.initialize(): No Entity Searcher Factory specified in service descriptor.";
            log.info(eMsg);
        }

        else {
            try {
                IEntitySearcherFactory entitySearcherFactory = (IEntitySearcherFactory) Class
                    .forName(entitySearcherFactoryName).newInstance();
                entitySearcher = entitySearcherFactory.newEntitySearcher();
            } catch (Exception e) {
                Utils.logTrace(e);
                eMsg = "AbstractGroupService.initialize(): Failed to instantiate entity searcher "
                    + e;
                log.error(eMsg);
                throw new GroupsException(eMsg, e);
            }
        }

        log.info("EveGroupService initialization complete.");
    }

    /**
     * Answers if the group can be updated or deleted in the store.
     */
    public boolean isEditable(IEntityGroup group) throws GroupsException {
        return isInternallyManaged();
    }

    /**
     * Answers if this service is managed by the portal and is therefore
     * updatable.
     */
    protected boolean isInternallyManaged() {
        return getServiceDescriptor().isInternallyManaged();
    }

    /**
     * Answers if this service is a leaf in the composite; a service that
     * actually operates on groups.
     */
    public boolean isLeafService() {
        return true;
    }

    /**
     * Answers if this service is updateable by the portal.
     */
    public boolean isEditable() {
        return isInternallyManaged();
    }


    private EntityIdentifier[] removeDuplicates(EntityIdentifier[] entities) {
        ArrayList ar = new ArrayList(entities.length);
        for (int i = 0; i < entities.length; i++) {
            if (!ar.contains(entities[i])) {
                ar.add(entities[i]);
            }
        }
        return (EntityIdentifier[]) ar.toArray(new EntityIdentifier[0]);
    }

    public EntityIdentifier[] searchForEntities(String query, int method,
        Class type) throws GroupsException {
        return removeDuplicates(entitySearcher.searchForEntities(query, method,
            type));
    }

    public EntityIdentifier[] searchForEntities(String query, int method,
        Class type, IEntityGroup ancestor) throws GroupsException {
        return filterEntities(searchForEntities(query, method, type), ancestor);
    }

    public EntityIdentifier[] searchForGroups(String query, int method,
        Class leaftype) throws GroupsException {
        return removeDuplicates(groupStore.searchForGroups(query, method,
            leaftype));
    }

    public EntityIdentifier[] searchForGroups(String query, int method,
        Class leaftype, IEntityGroup ancestor) throws GroupsException {
        return filterEntities(searchForGroups(query, method, leaftype),
            ancestor);
    }

    /**
     *  
     */
    protected void throwExceptionIfNotInternallyManaged()
        throws GroupsException {
        if (!isInternallyManaged()) {
            throw new GroupsException("Group Service " + getServiceName()
                + " is not updatable.");
        }

    }
    
    /**
     * Updates the <code>ILockableEntityGroup</code> in the cache and the
     * store.
     * 
     * @param group
     *            ILockableEntityGroup
     */
    public void updateGroup(ILockableEntityGroup group) throws GroupsException {
        updateGroup(group, false);
    }
    
    /**
     * Updates the <code>ILockableEntityGroup</code> in the cache and the
     * store.
     * 
     * @param group
     *            ILockableEntityGroup
     */
    public void updateGroupMembers(ILockableEntityGroup group)
        throws GroupsException {
        updateGroupMembers(group, false);
    }

    
    /**
     * A foreign member is a group from a different service.
     * @param member IGroupMember
     * @return boolean
     */
    protected boolean isForeign(IGroupMember member) {
        if (member.isEntity()) {
            return false;
        } else {
            Name memberSvcName = ((IEntityGroup) member).getServiceName();
            return (!getServiceName().equals(memberSvcName));
        }
    }
    
    /**
     * Returns a cached <code>IEntity</code> or null if it has not been cached.
     */
    protected IEntity getEntityFromCache(String key) throws CachingException {
        return (IEntity) EntityCachingService.instance().get(
            org.jasig.portal.EntityTypes.LEAF_ENTITY_TYPE, key);
    }

    /**
     * Returns a <code>CompositeEntityIdentifier</code> for the group identified
     * by <code>key</code>.
     */
    protected CompositeEntityIdentifier newGroupCompositeEntityIdentifier(String key)
        throws GroupsException {
        
        try {
            
            CompositeEntityIdentifier compositeEntityId = 
                    new CompositeEntityIdentifier(key,
                            org.jasig.portal.EntityTypes.GROUP_ENTITY_TYPE);            
            
            // check if the service name is missing, set if it is
            Name serviceName = compositeEntityId.getServiceName();
            if (serviceName == null) {
                compositeEntityId.setServiceName(getServiceName());
            }
            
            return compositeEntityId;
            
        } catch (NamingException ne) {
            Utils.logTrace(ne);
            throw new GroupsException(
                "EveGroupService::newGroupCompositeEntityIdentifier : " +
                "failed parsing group key: " + key, ne);
        }
    }
    
    
	public static class MemberType {
	    public static MemberType ANY = new MemberType(0);
	    public static MemberType GROUP = new MemberType(1);
	    public static MemberType ENTITY = new MemberType(2);
	    
	    private int id;
	    
	    public int getId() {
	        return id;
	    }
	    
	    public boolean equals(Object obj) {
	        if (obj == null)
	            return false;
	        if (obj == this)
	            return true;
	        if (!(obj instanceof MemberType))
	            return false;

	        return id == ((MemberType)obj).getId();
	    }
	    
	    private MemberType(int id) {
	        this.id = id;
	    }
	}
    
    
	

	// un-used ICompositeGroupService methods
	// For some reason IIndividualEntityGroupService extends from
	// ICompositeGroupService.  But IIndividualEntityGroupService implementations
	// Do not need these...
	
    public IEntity getEntity(String key, Class type, String service) throws GroupsException {
        throw new UnsupportedOperationException();
    }
    
    public IEntityGroup newGroup(Class type, Name serviceName) throws GroupsException {
        throw new UnsupportedOperationException();
    }
    
    public Map getComponentServices() {
        throw new UnsupportedOperationException();
    }
    
}
