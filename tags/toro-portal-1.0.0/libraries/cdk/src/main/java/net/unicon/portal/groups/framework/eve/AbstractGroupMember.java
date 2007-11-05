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

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IIndividualGroupService;

/**
 * 
 */
public abstract class AbstractGroupMember implements IEveGroupMember
{
/*
 * The <code>EntityIdentifier</code> that uniquely identifies the entity,
 * e.g., the <code>IPerson</code>, <code>ChannelDefinition</code>, etc.,
 * that underlies the <code>IGroupMember</code>.
 */
    private EntityIdentifier underlyingEntityIdentifier;
    
    private IEveGroupService eveGroupService;

/**
 * AbstractGroupMember constructor
 */
public AbstractGroupMember(String key, Class type) throws GroupsException
{
    this(new EntityIdentifier(key, type));
}
/**
 * AbstractGroupMember constructor
 */
public AbstractGroupMember(EntityIdentifier newEntityIdentifier) throws GroupsException
{
    super();
    if ( isKnownEntityType(newEntityIdentifier.getType()) )
        { underlyingEntityIdentifier = newEntityIdentifier; }
    else
        { throw new GroupsException("Unknown entity type: " + newEntityIdentifier.getType()); }
}


/**
 * Default implementation, overridden on EntityGroupImpl.
 * @param gm org.jasig.portal.groups.IGroupMember
 * @return boolean
 */
public boolean contains(IGroupMember gm) throws GroupsException {
    return false;
}


/**
 * Default implementation, overridden on EntityGroupImpl.
 * @param gm org.jasig.portal.groups.IGroupMember
 * @return boolean
 */
public boolean deepContains(IGroupMember gm) throws GroupsException {
    return false;
}


/**
 * Returns an <code>Iterator</code> over the <code>Set</code> of this
 * <code>IGroupMember's</code> recursively-retrieved parent groups.
 *
 * @return java.util.Iterator
 */
public Iterator getAllContainingGroups() throws GroupsException {
	return getEveGroupService().getAllContainingGroups(this);
}


/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return java.util.Iterator
 */
public java.util.Iterator getAllEntities() throws GroupsException {
    return getEmptyIterator();
}


/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return java.util.Iterator
 */
public java.util.Iterator getAllMembers() throws GroupsException {
    return getEmptyIterator();
}


/**
 * @return java.lang.String
 */
protected String getCacheKey() {
    return getEntityIdentifier().getKey();
}

/**
 * Returns an <code>Iterator</code> over this <code>IGroupMember's</code> parent groups.
 * Synchronize the collection of keys with adds and removes.
 * @return java.util.Iterator
 */
public java.util.Iterator getContainingGroups() throws GroupsException {
    return getEveGroupService().getContainingGroups(this);
}


/**
 * @return java.util.Iterator
 */
protected java.util.Iterator getEmptyIterator()
{
    return java.util.Collections.EMPTY_LIST.iterator();
}


/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return java.util.Iterator
 */
public java.util.Iterator getEntities() throws GroupsException {
    return getEmptyIterator();
}


/**
 * @return java.lang.String
 */
public java.lang.String getKey() {
    return getUnderlyingEntityIdentifier().getKey();
}


/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return org.jasig.portal.groups.IEntityGroup
 * @param name java.lang.String
 */
public IEntityGroup getMemberGroupNamed(String name) throws GroupsException {
    return null;
}


/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return java.util.Iterator
 */
public java.util.Iterator getMembers() throws GroupsException {
    return getEmptyIterator();
}


/**
 * @return java.lang.Class
 */
public java.lang.Class getType() {
    return getUnderlyingEntityIdentifier().getType();
}


/**
 * @return EntityIdentifier
 */
public EntityIdentifier getUnderlyingEntityIdentifier() {
    return underlyingEntityIdentifier;
}


/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return boolean
 */
public boolean hasMembers() throws GroupsException {
    return false;
}


/**
 * Answers if this <code>IGroupMember</code> is, recursively, a member of <code>IGroupMember</code> gm.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
abstract public boolean isDeepMemberOf(IGroupMember gm) throws GroupsException;


/**
 * @return boolean
 */
public boolean isEntity() {
    return false;
}


/**
 * @return boolean
 */
public boolean isGroup() {
    return false;
}


/**
 * @return boolean.
 */
protected boolean isKnownEntityType(Class anEntityType) throws GroupsException
{
    return ( org.jasig.portal.EntityTypes.getEntityTypeID(anEntityType) != null );
}


/**
 * Answers if this <code>IGroupMember</code> is a member of <code>IGroupMember</code> gm.
 * @param gm org.jasig.portal.groups.IGroupMember
 * @return boolean
 */
abstract public boolean isMemberOf(IGroupMember gm) throws GroupsException;


public void setEveGroupService(IEveGroupService groupService)
throws GroupsException {
    
    if (groupService == null) {
        throw new GroupsException("Null groupService!");
    }
    eveGroupService = groupService;
}

public void setLocalGroupService(IIndividualGroupService groupService) throws GroupsException {
	if ( ! (groupService instanceof IEveGroupService)) {
		throw new GroupsException("Group implementation requires groups service of type IEveGroupService");
	}
	setEveGroupService((IEveGroupService)groupService);
}

public IEveGroupService getEveGroupService() {
    return eveGroupService;
}
}
