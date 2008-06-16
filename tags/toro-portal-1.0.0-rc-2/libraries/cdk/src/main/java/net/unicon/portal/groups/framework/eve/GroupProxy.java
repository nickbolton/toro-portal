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

import javax.naming.InvalidNameException;
import javax.naming.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.CompositeEntityIdentifier;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IComponentGroupService;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IIndividualGroupService;
import org.jasig.portal.services.GroupService;

public class GroupProxy implements IEntityGroup {
    
    private String key = null;
    private Log log = LogFactory.getLog(GroupProxy.class);
    private int hc = -1;
    private String strValue = null;
    private Name serviceName = null;
    private IIndividualGroupService individualGroupService = null;
    private CompositeEntityIdentifier compositeId = null;
    private boolean invalidGroupService = false;
    
    public GroupProxy(String key) throws GroupsException {
        this.key = key;
        this.hc = key.hashCode();
        this.strValue = new StringBuffer(
            "GroupProxy (").append(key).append(')').toString();
        
        compositeId = new CompositeEntityIdentifier(
            key, org.jasig.portal.EntityTypes.GROUP_ENTITY_TYPE);

        int pos = key.lastIndexOf(IGroupConstants.NODE_SEPARATOR);
        if (pos < 0) {
            throw new RuntimeException("GroupProxy::GroupProxy : Invalid group key: " + key);
        }

        try {
            this.serviceName = GroupService.parseServiceName(key.substring(0,pos));
        } catch (InvalidNameException ine) {
            StringBuffer sb = new StringBuffer();
            sb.append("GroupProxy::GroupProxy : failed to parse group service name: ");
            sb.append(key).append(", ").append(pos).append(", ").append(key.substring(0,pos));
            log.error(sb.toString(), ine);
            throw new GroupsException(sb.toString());
        }
    }
    
    public IEntityGroup getUnderlyingGroup() {
        IEntityGroup gr = null;
        
        if (invalidGroupService) {
            return null;
        }
        
        try {
            // this will ensure we get the most recent cached version
            IIndividualGroupService service = getIndividualGroupService(); 
            if (service != null) {
                gr = service.findGroup(compositeId);
            }
        } catch (GroupsException ge) {
            Utils.logTrace(ge);
            StringBuffer sb = new StringBuffer();
            sb.append("GroupProxy::getUnderlyingGroup : ");
            sb.append("error retrieving underlying group object: ");
            sb.append(key);
            log.error(sb, ge);
        }
        return gr;
    }
    
    private IIndividualGroupService getIndividualGroupService()
    throws GroupsException {
        if (individualGroupService == null) {
            individualGroupService = 
                (IIndividualGroupService)GroupService.getCompositeGroupService().
            getComponentServices().get(serviceName);
            
            if (individualGroupService == null) {
                invalidGroupService = true;
                throw new GroupsException(
                    "Failed to find component service for: " + serviceName);
            }
        }
        return individualGroupService;
    }
    
    public String toString() {
        return strValue;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof GroupProxy))
            return false;

        GroupProxy g = (GroupProxy)obj;
        
        return getKey().equals(g.getKey());
    }

    public int hashCode() {
        return hc;
    }

    public void addMember(IGroupMember gm) throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            gr.addMember(gm);
        }
    }

    public void delete() throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            gr.delete();
        }
    }

    public String getCreatorID() {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.getCreatorID();
        }
        return null;
    }

    public String getDescription() {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.getDescription();
        }
        return null;
    }

    public String getLocalKey() {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.getLocalKey();
        }
        return null;
    }

    public String getName() {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.getName();
        }
        return null;
    }

    public Name getServiceName() {
        return serviceName;
    }

    public boolean isEditable() throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.isEditable();
        }
        return false;
    }

    public void removeMember(IGroupMember gm) throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            gr.removeMember(gm);
        }
    }

    public void setCreatorID(String val) {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            gr.setCreatorID(val);
        }
    }

    public void setDescription(String val) {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            gr.setDescription(val);
        }
    }

    public void setName(String val) throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            gr.setName(val);
        }
    }

    public void update() throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            gr.update();
        }
    }

    public void updateMembers() throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            gr.updateMembers();
        }
    }

    public void setLocalGroupService(IIndividualGroupService val) throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            gr.setLocalGroupService(val);
        }
    }

    public boolean contains(IGroupMember val) throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.contains(val);
        }
        return false;
    }

    public boolean deepContains(IGroupMember val) throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.deepContains(val);
        }
        return false;
    }

    public Iterator getAllContainingGroups() throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.getAllContainingGroups();
        }
        return null;
    }

    public Iterator getAllEntities() throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.getAllEntities();
        }
        return null;
    }

    public Iterator getAllMembers() throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.getAllMembers();
        }
        return null;
    }

    public Iterator getContainingGroups() throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.getContainingGroups();
        }
        return null;
    }

    public Iterator getEntities() throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.getEntities();
        }
        return null;
    }

    public Class getEntityType() {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.getEntityType();
        }
        return null;
    }

    public String getKey() {
        return key;
    }

    public Class getLeafType() {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.getLeafType();
        }
        return null;
    }

    public IEntityGroup getMemberGroupNamed(String val) throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.getMemberGroupNamed(val);
        }
        return null;
    }

    public Iterator getMembers() throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.getMembers();
        }
        return null;
    }

    public Class getType() {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.getType();
        }
        return null;
    }

    public EntityIdentifier getUnderlyingEntityIdentifier() {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.getUnderlyingEntityIdentifier();
        }
        return null;
    }

    public boolean hasMembers() throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.hasMembers();
        }
        return false;
    }

    public boolean isDeepMemberOf(IGroupMember val) throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.isDeepMemberOf(val);
        }
        return false;
    }
    
    // we know these two answers  :)
    public boolean isEntity() {
        return false;
    }

    public boolean isGroup() {
        return true;
    }

    public boolean isMemberOf(IGroupMember val) throws GroupsException {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.isMemberOf(val);
        }
        return false;
    }

    public EntityIdentifier getEntityIdentifier() {
        IEntityGroup gr = getUnderlyingGroup();
        if (gr != null) {
            return gr.getEntityIdentifier();
        }
        return null;
    }

}
