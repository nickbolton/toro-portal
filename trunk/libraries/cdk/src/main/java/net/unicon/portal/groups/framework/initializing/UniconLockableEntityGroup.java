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

import java.util.Set;

import org.jasig.portal.concurrency.IEntityLock;
import org.jasig.portal.groups.*;

    /**
 * Extends <code>EntityGroupImpl</code> to make it lockable for writing.
 * <p>
 * @author nbolton
 */

public class UniconLockableEntityGroup extends UniconEntityGroup implements ILockableEntityGroup
{
    protected IEntityLock lock;
/**
 * UniconLockableEntityGroup constructor.
 * @param groupKey java.lang.String
 * @param groupType java.lang.Class
 * @exception org.jasig.portal.groups.GroupsException.
 */
public UniconLockableEntityGroup(String groupKey, Class groupType) throws GroupsException {
    super(groupKey, groupType);
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
    return (ILockableGroupService) super.getLocalGroupService();
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
    clearPendingUpdates();
    setGroupKeysInitialized(false);
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
    clearPendingUpdates();
    setGroupKeysInitialized(false);
}
/**
 * @param lock org.jasig.portal.concurrency.IEntityLock
 */
public void setLock(IEntityLock newLock)
{
    lock = newLock;
}
/**
 */
public String toString()
{
    return "UniconLockableEntityGroup (" + getKey() + ") "  + getName();
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

}
