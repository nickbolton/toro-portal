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

import net.unicon.sdk.FactoryCreateException;

import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.LogService;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * This implementation of <code>IMember</code> is a wrapper for the uPortal
 * <code>IGroupMember</code> class.
 */
public final class MemberImpl implements IMember {

    private String key;

    /**
     * Retrieves an unique identifier for this member object.
     * @return The unique member identifier.
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns an array of <code>IGroup</code> objects representing this
     * member's recursively-retrieved parent groups.
     * @return IGroup[]
     * @throws UniconGroupsException
     */
    public IGroup[] getAllContainingGroups() throws UniconGroupsException {
        List groups = new ArrayList();
        try {
            Iterator itr = getGroupMember().getAllContainingGroups();
            while (itr.hasNext()) {
                groups.add(GroupFactory.getGroup((IEntityGroup)itr.next()));
            }
        } catch (FactoryCreateException fce) {
            throw new UniconGroupsException(fce);
        } catch (GroupsException ge) {
            throw new UniconGroupsException(ge);
        }
        return (IGroup[])groups.toArray(new IGroup[0]);
    }

    /**
     * Returns an array of <code>IGroup</code> objects representing
     * this member's direct parent groups.
     * @return java.util.Iterator
     * @throws UniconGroupsException
     */
    public IGroup[] getContainingGroups() throws UniconGroupsException {
        List groups = new ArrayList();
        try {
            Iterator itr = getGroupMember().getContainingGroups();
            while (itr.hasNext()) {
                groups.add(GroupFactory.getGroup((IEntityGroup)itr.next()));
            }
        } catch (FactoryCreateException fce) {
            throw new UniconGroupsException(fce);
        } catch (GroupsException ge) {
            throw new UniconGroupsException(ge);
        }
        return (IGroup[])groups.toArray(new IGroup[0]);
    }

    private IGroupMember getGroupMember() throws UniconGroupsException {
        IGroupMember gm = null;
        try {
            gm = AuthorizationService.instance().getGroupMember(
                AuthorizationService.instance().newPrincipal(key,
                    org.jasig.portal.security.IPerson.class));
        } catch (Exception e) {
            throw new UniconGroupsException(e.getMessage(), e);
        }
        return gm;
    }

    MemberImpl(IGroupMember groupMember) {
        this.key = groupMember.getKey();
    }

    MemberImpl(String key) throws UniconGroupsException {
        this.key = key;
    }
}
