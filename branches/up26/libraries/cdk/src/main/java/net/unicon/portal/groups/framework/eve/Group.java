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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.naming.Name;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.CompositeEntityIdentifier;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;

/**
 * @author nbolton
 */
public class Group extends AbstractGroupMember implements IEveGroup {
    
    private String creatorID;

    private String name;

    private String description;

    // A group and its members share an entityType.
    private java.lang.Class leafEntityType;
    
	private Set addedMembers = new HashSet();
	private Set removedMembers = new HashSet();
	
	private int hc = 0;
	
	public Group(String groupKey, Class entityType) throws GroupsException {
	    this(groupKey, entityType, null, null, null);
	}

	public Group(String groupKey, Class entityType, String name, 
			String creatorId, String description) 
	throws GroupsException
	{
	    super(new CompositeEntityIdentifier(groupKey,
	        org.jasig.portal.EntityTypes.GROUP_ENTITY_TYPE));
	    
	    if (isKnownEntityType(entityType)) {
	        leafEntityType = entityType;
	    } else {
	        throw new GroupsException("Unknown entity type: " + entityType);
	    }
		
		setName(name);
		setCreatorID(creatorId);
		setDescription(description);
		
		hc = groupKey.hashCode();
	}
	
	
	public int hashCode() {
        return hc;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof Group))
            return false;

        Group g = (Group)obj;
        
        return getEntityIdentifier().getKey().equals(
            g.getEntityIdentifier().getKey());
    }
	
	public String toString() {
	    String clsName = getEntityType().getName();
	    return "Group (" + clsName + ") "  + getKey() + ":" + getName();
	}
	
	/**
     * A member must share the <code>entityType</code> of its containing
     * <code>IEntityGroup</code>. If it is a group, it must have a unique
     * name within each of its containing groups and the resulting group must
     * not contain a circular reference. Removed the requirement for unique
     * group names. (03-04-2004, de)
     * 
     * @param gm
     *            org.jasig.portal.groups.IGroupMember
     * @exception org.jasig.portal.groups.GroupsException
     */
    protected void checkProspectiveMember(IGroupMember gm)
        throws GroupsException {
        if (gm.equals(this)) {
            throw new GroupsException("Attempt to add " + gm + " to itself.");
        }

        // Type check:
        if (this.getLeafType() != gm.getLeafType()) {
            throw new GroupsException(this + " and " + gm
                + " have different entity types.");
        }

        // Circular reference check:
        if (gm.isGroup() && gm.deepContains(this)) {
            throw new GroupsException("Adding " + gm + " to " + this
                + " creates a circular reference.");
        }
    }

    
    /**
     * Checks recursively if <code>GroupMember</code> gm is a member of this.
     * 
     * @return boolean
     * @param gm
     *            org.jasig.portal.groups.IGroupMember
     */
    public boolean deepContains(IGroupMember gm) throws GroupsException {
        if (this.contains(gm))
            return true;

        boolean found = false;
        Iterator it = getMembers();
        while (it.hasNext() && !found) {
            IGroupMember myGm = (IGroupMember) it.next();
            found = myGm.deepContains(gm);
        }

        return found;
    }

    
    /**
     * Returns the <code>EntityIdentifier</code> cast to a
     * <code>CompositeEntityIdentifier</code> so that its service nodes can be
     * pushed and popped.
     * 
     * @return CompositeEntityIdentifier
     */
    public CompositeEntityIdentifier getCompositeEntityIdentifier() {
        return (CompositeEntityIdentifier) getEntityIdentifier();
    }

    /**
     * @return java.lang.String
     */
    public java.lang.String getCreatorID() {
        return creatorID;
    }
    
    /**
     * @return java.lang.String
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * @return java.lang.String
     */
    public java.lang.String getDescription() {
        return description;
    }

    
    /**
     * @return EntityIdentifier
     */
    public EntityIdentifier getEntityIdentifier() {
        return getUnderlyingEntityIdentifier();
    }

    /**
     * Returns the key of the underyling entity.
     * 
     * @return java.lang.String
     */
    public String getEntityKey() {
        return getKey();
    }

    /**
     * Returns the entity type of this groups's leaf members.
     * 
     * @return java.lang.Class
     * @see org.jasig.portal.EntityTypes
     */
    public java.lang.Class getEntityType() {
        return leafEntityType;
    }

    /**
     * @return String
     */
    public String getGroupID() {
        return getKey();
    }

    /**
     * Returns the entity type of this groups's members.
     * 
     * @return java.lang.Class
     * @see org.jasig.portal.EntityTypes
     */
    public java.lang.Class getLeafType() {
        return leafEntityType;
    }
    
    /**
     * Returns the key from the group service of origin.
     * 
     * @return String
     */
    public String getLocalKey() {
        return getCompositeEntityIdentifier().getLocalKey();
    }
    
    /**
     * Returns the Name of the group service of origin.
     * 
     * @return javax.naming.Nme
     */
    public Name getServiceName() {
        return getCompositeEntityIdentifier().getServiceName();
    }

    
    /**
     * Returns this object's type for purposes of caching and locking, as
     * opposed to the underlying entity type.
     * 
     * @return java.lang.Class
     */
    public Class getType() {
        return org.jasig.portal.EntityTypes.GROUP_ENTITY_TYPE;
    }
    
    
    /**
     * Answers if there are any added or deleted memberships not yet committed
     * to the database.
     * 
     * @return boolean
     */
    public boolean hasDirtyMembers() {
        return hasAdds() || hasDeletes();
    }
    
    /**
     * Answers if this <code>IEntityGroup</code> can be changed or deleted.
     * 
     * @return boolean
     * @exception GroupsException
     */
    public boolean isEditable() throws GroupsException {
        return getEveGroupService().isEditable(this);
    }

    /**
     * @return boolean
     */
    public boolean isGroup() {
        return true;
    }

    /**
     * @param newCreatorID
     *            java.lang.String
     */
    public void setCreatorID(java.lang.String newCreatorID) {
        creatorID = newCreatorID;
    }
    
    /**
     * @param newDescription
     *            java.lang.String
     */
    public void setDescription(java.lang.String newDescription) {
        description = newDescription;
    }

    public void setEveGroupService(IEveGroupService gs) throws GroupsException {
        super.setEveGroupService(gs);
        setServiceName(gs.getServiceName());
    }

    /**
     * We used to check duplicate sibling names but no longer do.  
     * @param newName java.lang.String
     */
    public void setName(java.lang.String newName) throws GroupsException {
        name = newName;
    }

    /**
     * Convenience method to allow setting the service name based on a String.
     * This method creates a Name and passes it to setServiceName(Name) to 
     * actually set the service Name of the group service of origin.
     */
    public void setServiceName(String newServiceName) throws GroupsException {
        try {
            Name serviceName = getCompositeEntityIdentifier().newName().add(newServiceName);
            setServiceName(serviceName);
        } catch (javax.naming.InvalidNameException ine) {
            throw new GroupsException("Problem setting service name: "
                + ine.getMessage(), ine);
        }

    }
    
    /**
     * Sets the service Name of the group service of origin.
     */
    public void setServiceName(Name newServiceName) throws GroupsException {
        try {
            getCompositeEntityIdentifier().setServiceName(newServiceName);
        } catch (javax.naming.InvalidNameException ine) {
            throw new GroupsException("Problem setting service name: "
                + ine.getMessage(), ine);
        }

    }
	
	public void addMember(IGroupMember gm) throws GroupsException {
	    try {
            checkProspectiveMember(gm);
        } catch (GroupsException ge) {
            Utils.logTrace(ge);
            throw new GroupsException("Could not add IGroupMember: "
                + ge.getMessage(), ge);
        }

        if (!this.contains(gm)) {
            getEveGroupService().addMember(this, gm);

            if (removedMembers.contains(gm)) {
                removedMembers.remove(gm);
            } else {
                addedMembers.add(gm);
            }
        }
	}
	
	public void delete() throws GroupsException {
	    getEveGroupService().deleteGroup(this);
	}

	public void removeMember(IGroupMember gm) throws GroupsException {
	    getEveGroupService().removeMember(this, gm);
	    
        if (addedMembers.contains(gm)) {
            addedMembers.remove(gm);
        } else {
            removedMembers.add(gm);
        }
	}

	public boolean contains(IGroupMember member) throws GroupsException {
		return getEveGroupService().contains(this, member);
	}

	public Iterator getAllEntities() throws GroupsException {
		return getEveGroupService().getAllEntities(this);
	}

	public Iterator getAllMembers() throws GroupsException {
		return getEveGroupService().getAllMembers(this);
	}

	public Iterator getEntities() throws GroupsException {
		return getEveGroupService().getEntities(this);
	}

	public IEntityGroup getMemberGroupNamed(String name) throws GroupsException {
		return getEveGroupService().getMemberGroupNamed(this, name);
	}

	public Iterator getMembers() throws GroupsException {
		return getEveGroupService().getMembers(this);
	}

	public boolean hasMembers() throws GroupsException {
		return getEveGroupService().hasMembers(this);
	}

	public boolean isDeepMemberOf(IGroupMember member) throws GroupsException {
		return getEveGroupService().isDeepMemberOf(this, member);
	}

	public boolean isMemberOf(IGroupMember member) throws GroupsException {
		return getEveGroupService().isMemberOf(this, member);
	}


	protected void clearPendingUpdates() {
	    // HashSet implements clear()
	    addedMembers.clear();
	    removedMembers.clear();
	}
	
	public IGroupMember[] getAddedMembers() {
	    return (IGroupMember[])addedMembers.toArray(new IGroupMember[addedMembers.size()]);
	}
	
	public IGroupMember[] getRemovedMembers() {
	    return (IGroupMember[])removedMembers.toArray(new IGroupMember[removedMembers.size()]);
	}
	
	public boolean hasAdds() {
	    return addedMembers.size() > 0;
	}
	
	public boolean hasDeletes() {
	    return removedMembers.size() > 0;
	}
	
	protected void addAddedMember(IGroupMember gm) {
	    addedMembers.add(gm);
	}
	
	protected void addRemovedMember(IGroupMember gm) {
	    removedMembers.add(gm);
	}
	
	/**
     * Delegate to the factory.
     */
    public void update() throws GroupsException {
        getEveGroupService().updateGroup(this);
        clearPendingUpdates();
    }

    /**
     * Delegate to the factory.
     */
    public void updateMembers() throws GroupsException {
        if (hasDirtyMembers()) {
            getEveGroupService().updateGroupMembers(this);
            clearPendingUpdates();
        }
    }
    
    public IEveGroup getCacheableGroup() {
        return this;
    }
}
