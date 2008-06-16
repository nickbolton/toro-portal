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

import java.util.Iterator;

import javax.naming.Name;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.concurrency.IEntityLock;
import org.jasig.portal.groups.CompositeEntityIdentifier;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IIndividualGroupService;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.groups.ILockableGroupService;

/**
 * 
 */
public class LockableGroup implements IEveGroup, ILockableEntityGroup {

    private IEntityLock lock = null;
    private Group group = null;
    
    /**
     * @param groupKey
     * @param entityType
     * @throws GroupsException
     */
    public LockableGroup(String groupKey, Class entityType)
        throws GroupsException {
        group = new Group(groupKey, entityType);
    }
    
    /**
     * @param groupKey
     * @param entityType
     * @param name
     * @param creatorId
     * @param description
     * @throws GroupsException
     */
    public LockableGroup(String groupKey, Class entityType, String name,
        String creatorId, String description) throws GroupsException {
        group = new Group(groupKey, entityType, name, creatorId, description);
    }
    
    /**
     * Delegates to the factory.
     */
    public void delete() throws GroupsException
    {
        getLockableGroupService().deleteGroup(this);
    }

    /**
     * @return org.jasig.portal.concurrency.IEntityLock
     */
    public IEntityLock getLock() {
        return lock;
    }

    /**
     * @return org.jasig.portal.groups.ILockableGroupService
     */
    protected ILockableGroupService getLockableGroupService() throws GroupsException
    {
        return (ILockableGroupService) group.getEveGroupService();
    }

    /**
     * Ask the service to update this group (in the store), update the 
     * back-pointers of the updated members, and force the retrieval of 
     * containing groups in case the memberships of THIS group have 
     * changed during the time the group has been locked.  
     */
    private void primUpdate(boolean renewLock) throws GroupsException
    {
        getLockableGroupService().updateGroup(this, renewLock);
        group.clearPendingUpdates();
    }

    /**
     * Ask the service to update this group (in the store), update the 
     * back-pointers of the updated members, and force the retrieval of 
     * containing groups in case the memberships of THIS group have 
     * changed during the time the group has been locked.  
     */
    private void primUpdateMembers(boolean renewLock) throws GroupsException
    {
        getLockableGroupService().updateGroupMembers(this, renewLock);
        group.clearPendingUpdates();
    }

    /**
     * @param newLock org.jasig.portal.concurrency.IEntityLock
     */
    public void setLock(IEntityLock newLock)
    {
        lock = newLock;
    }

    /**
     */
    public String toString()
    {
        String clsName = getEntityType().getName();
	    return "LockableGroup (" + clsName + ") "  + getKey() + ":" + getName();
    }


    // we fall back on the delgated groups equals and hashCode,
    // because LockableGroup objects are never stored in caches.
    
    public boolean equals(Object obj) {
        return group.equals(obj);
    }

    public int hashCode() {
        return group.hashCode();
    }
    
    
    /**
     *
     */
    public void update() throws GroupsException
    {
        primUpdate(false);
    }

    /**
     *
     */
    public void updateAndRenewLock() throws GroupsException
    {
        primUpdate(true);
    }

    /**
     *
     */
    public void updateMembers() throws GroupsException
    {
        primUpdateMembers(false);
    }

    /**
     *
     */
    public void updateMembersAndRenewLock() throws GroupsException
    {
        primUpdateMembers(true);
    }

    public void addMember(IGroupMember gm) throws GroupsException {
        group.addMember(gm);
    }

    public String getCreatorID() {
        return group.getCreatorID();
    }

    public String getDescription() {
        return group.getDescription();
    }

    public String getLocalKey() {
        return group.getLocalKey();
    }

    public String getName() {
        return group.getName();
    }

    public Name getServiceName() {
        return group.getServiceName();
    }

    public boolean isEditable() throws GroupsException {
        return group.isEditable();
    }

    public void removeMember(IGroupMember gm) throws GroupsException {
        group.removeMember(gm);
    }

    public void setCreatorID(String userID) {
        group.setCreatorID(userID);
    }

    public void setDescription(String name) {
        group.setDescription(name);
    }

    public void setName(String name) throws GroupsException {
        group.setName(name);
    }

    public void setLocalGroupService(IIndividualGroupService groupService)
    throws GroupsException {
        group.setLocalGroupService(groupService);
    }

    public boolean contains(IGroupMember gm) throws GroupsException {
        return group.contains(gm);
    }

    public boolean deepContains(IGroupMember gm) throws GroupsException {
        return group.deepContains(gm);
    }

    public Iterator getAllContainingGroups() throws GroupsException {
        return group.getAllContainingGroups();
    }

    public Iterator getAllEntities() throws GroupsException {
        return group.getAllEntities();
    }

    public Iterator getAllMembers() throws GroupsException {
        return group.getAllMembers();
    }

    public Iterator getContainingGroups() throws GroupsException {
        return group.getContainingGroups();
    }

    public Iterator getEntities() throws GroupsException {
        return group.getEntities();
    }

    public Class getEntityType() {
        return group.getEntityType();
    }

    public String getKey() {
        return group.getKey();
    }

    public Class getLeafType() {
        return group.getLeafType();
    }

    public IEntityGroup getMemberGroupNamed(String name) throws GroupsException {
        return group.getMemberGroupNamed(name);
    }

    public Iterator getMembers() throws GroupsException {
        return group.getMembers();
    }

    public Class getType() {
        return group.getType();
    }

    public EntityIdentifier getUnderlyingEntityIdentifier() {
        return group.getUnderlyingEntityIdentifier();
    }

    public boolean hasMembers() throws GroupsException {
        return group.hasMembers();
    }

    public boolean isDeepMemberOf(IGroupMember gm) throws GroupsException {
        return group.isDeepMemberOf(gm);
    }

    public boolean isEntity() {
        return false;
    }

    public boolean isGroup() {
        return true;
    }

    public boolean isMemberOf(IGroupMember gm) throws GroupsException {
        return group.isMemberOf(gm);
    }

    public EntityIdentifier getEntityIdentifier() {
        return group.getEntityIdentifier();
    }
    
    public IEveGroup getCacheableGroup() {
        return group;
    }

    public boolean hasDirtyMembers() {
        return group.hasDirtyMembers();
    }

    public boolean hasDeletes() {
        return group.hasDeletes();
    }

    public boolean hasAdds() {
        return group.hasAdds();
    }

    public IGroupMember[] getRemovedMembers() {
        return group.getRemovedMembers();
    }

    public IGroupMember[] getAddedMembers() {
        return group.getAddedMembers();
    }

    public void setServiceName(String newServiceName) throws GroupsException {
        group.setServiceName(newServiceName);
    }
    
    public void setServiceName(Name newServiceName) throws GroupsException {
        group.setServiceName(newServiceName);
    }

    public CompositeEntityIdentifier getCompositeEntityIdentifier() {
        return group.getCompositeEntityIdentifier();
    }
}
