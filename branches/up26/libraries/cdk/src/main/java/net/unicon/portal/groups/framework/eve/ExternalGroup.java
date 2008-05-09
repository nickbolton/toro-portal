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
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IIndividualGroupService;
import org.jasig.portal.services.GroupService;

/**
 * This is a wrapper for a group whose service is external that delegates
 * to the external service.
 */
public class ExternalGroup implements IEntityGroup {

    private IEntityGroup externalGroup = null;
    private String externalKey = null;
    private final int hc;
    private final String theString;
    
    /**
     * @param externalKey
     * @throws GroupsException
     */
    public ExternalGroup(String externalKey)
        throws GroupsException {
        
        if (externalKey == null) {
            throw new RuntimeException("ExternalGroup : null external key.");
        }
        this.externalKey = externalKey;
        this.hc = externalKey.hashCode();
        this.theString = "ExternalGroup: (" + externalKey + ")";
    }
    
    
    public synchronized IEntityGroup getExternalGroup() {
        try {
            if (externalGroup == null) {
                externalGroup = GroupService.findGroup(externalKey);
                if (externalGroup == null) {
                    throw new GroupsException(
                        "ExternalGroup::getGroup : failed to find external group: " + externalKey);
                }
            }
            return externalGroup;
        } catch (GroupsException ge) {
            Utils.logTrace(ge);
            throw new RuntimeException(ge.getMessage());
        }
    }
    
    public int hashCode() {
        return hc;
    }
    
    public String getExternalKey() {
        return externalKey;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof ExternalGroup))
            return false;

        ExternalGroup g = (ExternalGroup)obj;
        
        return externalKey.equals(g.getExternalKey());
    }
    

    public String toString() {
	    return theString;
	}

    public void addMember(IGroupMember gm) throws GroupsException {
        getExternalGroup().addMember(gm);
    }


    public void delete() throws GroupsException {
        getExternalGroup().delete();
    }


    public String getCreatorID() {
        return getExternalGroup().getCreatorID();
    }


    public String getDescription() {
        return getExternalGroup().getDescription();
    }


    public String getLocalKey() {
        return getExternalGroup().getLocalKey();
    }


    public String getName() {
        return getExternalGroup().getName();
    }



    public Name getServiceName() {
        return getExternalGroup().getServiceName();
    }



    public boolean isEditable() throws GroupsException {
        return getExternalGroup().isEditable();
    }



    public void removeMember(IGroupMember gm) throws GroupsException {
        getExternalGroup().removeMember(gm);
    }



    public void setCreatorID(String userID) {
        getExternalGroup().setCreatorID(userID);
    }

 
    public void setDescription(String name) {
        getExternalGroup().setDescription(name);
    }


    public void setName(String name) throws GroupsException {
        getExternalGroup().setName(name);
    }

 
    public void update() throws GroupsException {
        getExternalGroup().update();
    }


    public void updateMembers() throws GroupsException {
        getExternalGroup().updateMembers();
    }

 
    public void setLocalGroupService(IIndividualGroupService groupService) throws GroupsException {
        getExternalGroup().setLocalGroupService(groupService);
    }

 
    public boolean contains(IGroupMember gm) throws GroupsException {
        return getExternalGroup().contains(gm);
    }

 
    public boolean deepContains(IGroupMember gm) throws GroupsException {
        return getExternalGroup().deepContains(gm);
    }


    public Iterator getAllContainingGroups() throws GroupsException {
        return getExternalGroup().getAllContainingGroups();
    }

 
    public Iterator getAllEntities() throws GroupsException {
        return getExternalGroup().getAllEntities();
    }

 
    public Iterator getAllMembers() throws GroupsException {
        return getExternalGroup().getAllMembers();
    }

 
    public Iterator getContainingGroups() throws GroupsException {
        return getExternalGroup().getContainingGroups();
    }

 
    public Iterator getEntities() throws GroupsException {
        return getExternalGroup().getEntities();
    }

 
    public Class getEntityType() {
        return getExternalGroup().getEntityType();
    }

 
    public String getKey() {
        return externalKey;
    }


    public Class getLeafType() {
        return getExternalGroup().getLeafType();
    }


    public IEntityGroup getMemberGroupNamed(String name) throws GroupsException {
        return getExternalGroup().getMemberGroupNamed(name);
    }

 
    public Iterator getMembers() throws GroupsException {
        return getExternalGroup().getMembers();
    }

 
    public Class getType() {
        return getExternalGroup().getType();
    }

 
    public EntityIdentifier getUnderlyingEntityIdentifier() {
        return getExternalGroup().getUnderlyingEntityIdentifier();
    }

 
    public boolean hasMembers() throws GroupsException {
        return getExternalGroup().hasMembers();
    }


    public boolean isDeepMemberOf(IGroupMember gm) throws GroupsException {
        return getExternalGroup().isDeepMemberOf(gm);
    }
 
 
    public boolean isEntity() {
        return getExternalGroup().isEntity();
    }

 
    public boolean isGroup() {
        return getExternalGroup().isGroup();
    }

 
    public boolean isMemberOf(IGroupMember gm) throws GroupsException {
        return getExternalGroup().isMemberOf(gm);
    }

 
    public EntityIdentifier getEntityIdentifier() {
        return getExternalGroup().getEntityIdentifier();
    }
    
}
