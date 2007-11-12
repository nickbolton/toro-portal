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
package net.unicon.portal.groups;

import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.GroupsException;

import org.jasig.portal.channels.groupsmanager.GroupsManagerConstants;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IUpdatingPermissionManager;

import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.DocumentFactory;

import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.portal.common.properties.*;
import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.util.XmlUtils;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import java.lang.reflect.Method;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This Implementation of IGroup is a wrapper around the uPortal
 * <code>IEntityGroup</code> object.
 */
public class LockableGroupImpl extends GroupImpl {

    LockableGroupImpl(Long id) {
        this(id.longValue());
    }

    // this constructor assumes a local group service
    LockableGroupImpl(long id) {

        super(id); 

        LogService.instance().log(LogService.DEBUG,
            "LockableGroupImpl::LockableGroupImpl() instantiating IGroup object with id: "
            + id);
    }

    LockableGroupImpl(String key) {

        super(key); 

        LogService.instance().log(LogService.DEBUG,
            "LockableGroupImpl::LockableGroupImpl() instantiating IGroup object with key:"
            + key);              
    }

    LockableGroupImpl(IEntityGroup entityGroup) {

        super(entityGroup);       
    }

    /**
     * Sets the name for this group.
     * @param name The new name.
     * @throws UniconGroupsException if the underlying group cannot be updated.
     */
    public void setName(String name)
    throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "LockableGroupImpl::setName() : group: (" + getKey() + ", " +
                getName() + ") setting name: " + name);

            ILockableEntityGroup lockedGroup = getLockableEntityGroup();
            lockedGroup.setName(name);
            update(lockedGroup);
            lockedGroup.getLock().release();
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Sets the creator of the group
     * @param creatorId An identifier for the creator of the group
     * @throws UniconGroupsException if the underlying group cannot be updated.
     */
    public void setCreatorId(String creatorId)
    throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "LockableGroupImpl::setCreatorId() : group: (" + getKey() + ", " +
                getName() + ") setting creatorId: " + creatorId);

            ILockableEntityGroup lockedGroup = getLockableEntityGroup();
            lockedGroup.setCreatorID(creatorId);
            update(lockedGroup);
            lockedGroup.getLock().release();
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Sets the description for this group.
     * @param description The new description for this group.
     * @throws UniconGroupsException if the underlying group cannot be updated.
     */
    public void setDescription(String desc)
    throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "LockableGroupImpl::setDescription() : group: (" + getKey() + ", " +
                getName() + ") setting desc: " + desc);

            ILockableEntityGroup lockedGroup = getLockableEntityGroup();
            lockedGroup.setDescription(desc);
            update(lockedGroup);
            lockedGroup.getLock().release();
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Adds a user member to this group.
     * @param member The member to add.
     * @throws UniconGroupsException if the underlying group cannot be updated.
     */
    public void addMember(IMember member)
    throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "LockableGroupImpl::addMember() : group: (" + getKey() + ", " +
                getName() + ") adding member: " + member.getKey());

            IEntity entity = retrieveUserEntity(member.getKey());
            ILockableEntityGroup lockedGroup = getLockableEntityGroup();
            lockedGroup.addMember(entity);
            update(lockedGroup);
            lockedGroup.getLock().release();
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Remove a user from this group.
     * @param member The member to remove.
     * @throws UniconGroupsException if the underlying group cannot be updated.
     */
    public void removeMember(IMember member)
    throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "LockableGroupImpl::removeMember() : group: (" + getKey() + ", " +
                getName() + ") removing member: " + member.getKey());

            IEntity entity = retrieveUserEntity(member.getKey());
            ILockableEntityGroup lockedGroup = getLockableEntityGroup();
            lockedGroup.removeMember(entity);
            update(lockedGroup);
            lockedGroup.getLock().release();
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Add a descendent group to this group
     * @param newDescendent
     * @throws UniconGroupsException if the underlying group cannot be updated.
     */
    public void addGroup(IGroup group)
    throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "LockableGroupImpl::addGroup() : group: (" + getKey() + ", " +
                getName() + ") adding group: " +
                group.getKey() + " - " + group.getName());
            ILockableEntityGroup lockedGroup = getLockableEntityGroup();
            lockedGroup.addMember(group.getEntityGroup());
            update(lockedGroup);
            lockedGroup.getLock().release();
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Remove a descendent group from this group
     * @param descendent The group to remove.
     * @throws UniconGroupsException if the underlying group cannot be updated.
     */
    public void removeGroup(IGroup group)
    throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "LockableGroupImpl::removeGroup() : group: (" + getKey() + ", " +
                getName() + ") removing group: " +
                group.getKey() + " - " + group.getName());
            ILockableEntityGroup lockedGroup = getLockableEntityGroup();
            lockedGroup.removeMember(group.getEntityGroup());
            update(lockedGroup);
            lockedGroup.getLock().release();
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Store the group object in persistent storage.
     * @throws UniconGroupsException if the underlying group cannot be updated.
     */
    public void update() throws UniconGroupsException {
        update(getLockableEntityGroup());
    }

    private void update(ILockableEntityGroup lockedGroup) 
    throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "LockableGroupImpl::update() : updating group: " +
                getKey() + " - " + getName());

            // update the group
            lockedGroup.update();

            // get the entity caching service
            Method method = Class.forName(
                "org.jasig.portal.services.EntityCachingService").
                getDeclaredMethod("instance", null);
            Object entityCachingService = method.invoke(null, null);

            // now update the entity cache
            Object[] params = {lockedGroup};
            Class[] paramClasses =
                {Class.forName("org.jasig.portal.IBasicEntity")};
            method = entityCachingService.getClass().
                getDeclaredMethod("update", paramClasses);
            method.invoke(entityCachingService, params);
        } catch (Exception e) {
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Delete this group object from persistent storage.
     * @throws UniconGroupsException if the underlying group cannot be deleted.
     */
    public void delete() throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "LockableGroupImpl::delete() : deleting group: " +
                getKey() + " - " + getName());

            // remove this group from any of it's current parents
            IEntityGroup parent = null;
            ILockableEntityGroup lockedGroup = getLockableEntityGroup();
            Iterator itr = lockedGroup.getContainingGroups();
            while (itr.hasNext()) {
                parent = (IEntityGroup)itr.next();

                LogService.instance().log(LogService.DEBUG,
                    "LockableGroupImpl::delete() : removing parent association: " + parent.getName());

                ILockableEntityGroup lockedParent =
                    GroupService.findLockableGroup(parent.getKey(), key);
                lockedParent.removeMember(lockedGroup);
                lockedParent.update();
                lockedParent.getLock().release();
            }
            deletePermissions((IGroupMember)lockedGroup);
            lockedGroup.delete();
        } catch (Exception e) {
            throw new UniconGroupsException(e);
        }
    }

    private ILockableEntityGroup getLockableEntityGroup() 
    throws UniconGroupsException {
        try {
            ILockableEntityGroup lockedGroup = 
                GroupService.findLockableGroup(key, key);
            if (lockedGroup == null) {
                StringBuffer sb = new StringBuffer();
                sb.append("LockableGroupImpl::getEntityGroup() : ");
                sb.append("GroupService lookup ");
                sb.append("failed for group: " + id);
                throw new Exception(sb.toString());
            }
            return lockedGroup;
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }
    }
    
} // end LockableGroupImpl class
