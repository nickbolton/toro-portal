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
package net.unicon.portal.groups.framework.eve.caching;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import net.unicon.portal.groups.framework.eve.EveGroupService;
import net.unicon.portal.groups.framework.eve.ExternalGroup;
import net.unicon.portal.groups.framework.eve.IEveGroup;
import net.unicon.portal.groups.framework.eve.Utils;
import net.unicon.sdk.cache.jms.JMSCacheMessagePublisher;
import net.unicon.sdk.cache.jms.TextMessageHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.groups.ComponentGroupServiceDescriptor;
import org.jasig.portal.groups.CompositeEntityIdentifier;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.properties.PropertiesManager;

/**
 * 
 */
public class CachingGroupService extends EveGroupService {
    
    private static final Log log = LogFactory.getLog(CachingGroupService.class);
    
    private Map entityCache = new HashMap();
    private Map groupCache = new HashMap();
    
    private static Map singletons = new HashMap();

    public static synchronized CachingGroupService cachingInstance(
        ComponentGroupServiceDescriptor svcDescriptor) throws GroupsException {
        
        String serviceName = svcDescriptor.getName();
        CachingGroupService service = (CachingGroupService)singletons.get(serviceName);
        
        if (service == null) {
            service = new CachingGroupService(svcDescriptor);
            singletons.put(serviceName, service);
        }
        
        return service;
    }
    
    public static synchronized CachingGroupService cachingInstance(
        String serviceName) throws GroupsException {
        
        CachingGroupService service =
            (CachingGroupService)singletons.get(serviceName);
        
        // the service needs to have already been instantiated using
        // cachingInstance(ComponentGroupServiceDescriptor svcDescriptor)
        // by the Composite Group service.
        // This instance method is provided so the jms message handlers
        // can get access to the caching service to update cache entries.
        if (service == null) {
            throw new GroupsException(
                "CachingGroupService : no service exists with name: " +
                serviceName);
        }
        return service;
    }
    
    /**
     * @param svcDescriptor
     * @throws GroupsException
     */
    private CachingGroupService(ComponentGroupServiceDescriptor svcDescriptor)
        throws GroupsException {
        super(svcDescriptor);
    }

    public synchronized IEntityGroup newGroup(Class type) throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("CachingGroupService::newGroup() type: " + type);
        }
        IEntityGroup group = super.newGroup(type);
        cacheAdd(group);
        return group;
    }
    
    public synchronized void deleteGroup(IEntityGroup group)
	throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("CachingGroupService::deleteGroup() group: " + group);
        }
        super.deleteGroup(group);
        cacheRemove(group);
        broadcastDirty(group, ActionType.GROUP_DELETE);
    }
    
    public synchronized void updateGroup(IEntityGroup group)
	throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("CachingGroupService::updateGroup() group: " + group);
        }
        super.updateGroup(group);
        cacheUpdate(group);
        broadcastDirty(group, ActionType.GROUP_UPDATE);
    }
    
    public synchronized void updateGroupMembers(IEntityGroup group)
	throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("CachingGroupService::updateGroupMembers() group: " + group);
        }
        super.updateGroupMembers(group);
        cacheUpdate(group);
        broadcastDirty(group, ActionType.GROUP_UPDATE);
    }
    
    public void updateGroup(ILockableEntityGroup group, boolean renewLock)
    throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("CachingGroupService::updateGroup() group, renewLock: " +
                group + ", " + renewLock);
        }
        super.updateGroup(group, renewLock);
        cacheUpdate(group);
        broadcastDirty(group, ActionType.GROUP_UPDATE);
    }
    
    public void updateGroupMembers(ILockableEntityGroup group,
        boolean renewLock) throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("CachingGroupService::updateGroupMembers() group, renewLock: " +
                group + ", " + renewLock);
        }
        super.updateGroupMembers(group, renewLock);
        cacheUpdate(group);
        broadcastDirty(group, ActionType.GROUP_UPDATE);
    }
    
    public IEntityGroup findGroup(CompositeEntityIdentifier ent) throws GroupsException {
        IEntityGroup group = getGroup(ent.getKey());
        
        if (log.isDebugEnabled()) {
            log.debug("CachingGroupService::findGroup() ent: " +
                ent + ": " + group);
        }
        
        if (group == null) {
            group = super.findGroup(ent);
            if (group != null) {
                cacheAdd(group);
            }
        }
        
        return group;
    }
    
    public IEntityGroup findGroup(String key)
    throws GroupsException {
        if (log.isDebugEnabled()) {
            log.debug("CachingGroupService::findGroup() key: " +
                key);
        }
        return this.findGroup(newGroupCompositeEntityIdentifier(key));
    }
    
    /**
     * Returns an <code>IEntity</code> representing a portal entity. This does
     * not guarantee that the underlying entity actually exists.
     */
    public IEntity getEntity(String key, Class type) throws GroupsException {
        IEntity entity = getEntity(type, key);
        
        if (log.isDebugEnabled()) {
            log.debug("CachingGroupService::getEntity() key, type: " +
                key + ", " + type + ": " + entity);
        }
        
        if (entity == null) {
            entity = super.getEntity(key, type);
            if (entity != null) {
                cacheAdd(entity);
            }
        }
        
        return entity;
    }
    
	protected void primeServiceObject(IGroupMember gm) throws GroupsException {
	    super.primeServiceObject(gm);
	    if (!(gm instanceof ExternalGroup)) {
	        cacheAdd(gm);
	    }
	}
	
	private String getCacheKey(IGroupMember gm) {
	    IGroupMember correctMember = getCorrectMember(gm);
	    String cacheKey = null;
	    
	    if (correctMember.isGroup()) {
            cacheKey = correctMember.getKey();
        } else {
            cacheKey = correctMember.getUnderlyingEntityIdentifier().getKey();
        }
	    
	    return cacheKey;
	}
	
	private Map getCache(IGroupMember gm) {
	    
	    IGroupMember correctMember = getCorrectMember(gm);
	    Map cache = null;
	    if (correctMember.isGroup()) {
            cache = groupCache;
        } else {
            cache = getEntityCache(correctMember.getEntityType());
        }
	    return cache;
	}
	
	private IGroupMember getCorrectMember(IGroupMember gm) {
	    IGroupMember correctMember = null;
	    
	    if (gm instanceof IEveGroup) {
	        // Only cache the Group object, not ModifiableGroup objects.
	        correctMember = ((IEveGroup)gm).getCacheableGroup();
	    } else {
	        correctMember = gm;
	    }
	    
	    return correctMember;
	}
	
	synchronized void cacheAdd(IGroupMember gm) {
	    
	    Map cache = getCache(gm);
	    String cacheKey = getCacheKey(gm);
	    
	    IGroupMember correctMember = getCorrectMember(gm);
	    
	    if (log.isDebugEnabled()) {
            log.debug("CachingGroupService::cacheAdd(): cacheKey, member: " +
                cacheKey + ", " + correctMember);
        }
	    
	    cache.put(cacheKey, correctMember);
	}
	
	synchronized void cacheRemove(IGroupMember gm) {
	    
	    Map cache = getCache(gm);;
	    String cacheKey = getCacheKey(gm);;
	    
	    if (log.isDebugEnabled()) {
            log.debug("CachingGroupService::cacheRemove(): cacheKey, member: " +
                cacheKey + ", " + getCorrectMember(gm));
        }
	    
	    cache.remove(cacheKey);
	}
	
	synchronized void cacheUpdate(IGroupMember gm) {
	    if (log.isDebugEnabled()) {
            log.debug("CachingGroupService::cacheUpdate() gm: " + gm);
        }
	    cacheAdd(gm);
	}
	
	private IEntityGroup getGroup(String key) {
	    
	    IEntityGroup group = (IEntityGroup)groupCache.get(key);
	    
	    if (log.isDebugEnabled()) {
            log.debug("CachingGroupService::getGroup() key: " + key + " : " +
                (group != null ? ("CACHE HIT: " + group) : "CACHE MISS"));
        }
	    
	    return group;
	}
	
	private IEntity getEntity(Class type, String key) {
	    IEntity entity = (IEntity)getEntityCache(type).get(key);
	    
	    if (log.isDebugEnabled()) {
            log.debug("CachingGroupService::getEntity() type, key: " + 
                type + ", " + key + " : " +
                (entity != null ? ("CACHE HIT: " + entity) : "CACHE MISS"));
        }
	    return entity;
	}
	
	private synchronized Map getEntityCache(Class type) {
	    Map cache = (Map)entityCache.get(type);
	    if (cache == null) {
	        cache = new HashMap();
	        entityCache.put(type, cache);
	    }
	    return cache;
	}
	
	private void broadcastDirty(IEntityGroup group, ActionType action) {
	    if (PropertiesManager.getPropertyAsBoolean(
	        "org.jasig.portal.concurrency.multiServer")) {
	        
	        if (log.isDebugEnabled()) {
	            log.debug("CachingGroupService::broadcastDirty() group, action: " +
	                group + ", " + action);
	        }
	        
	        GroupUpdateMessageHelper messageHelper =
	            new GroupUpdateMessageHelper();
	        messageHelper.setGroupKey(group.getKey());
	        messageHelper.setAction(action);
	        messageHelper.setServiceName(getServiceName().toString());
	        publishTextMessage(messageHelper);
	    }
	}
	
	private void publishTextMessage(TextMessageHelper messageHelper) {
        try {
            JMSCacheMessagePublisher.publishAsynchTextMessage(
                messageHelper.getTextMessage(),
                messageHelper.getClass().getName());
        } catch (JMSException e) {
            Utils.logTrace(e);
        }
    }

}
