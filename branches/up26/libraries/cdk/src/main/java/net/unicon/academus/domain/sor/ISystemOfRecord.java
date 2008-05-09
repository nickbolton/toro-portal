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

package net.unicon.academus.domain.sor;

import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.IDomainEntity;

/**
 * Represents a source of domain entities (users, offerings, &amp;c) within
 * Academus.
 */
public interface ISystemOfRecord {

    /*
     * Public API.
     */

    /**
     * Obtains the name for this system of record (source) of domain entities.
     *
     * @return The name of this source.
     */
    String getSourceName();

    /**
     * Obtains the maximum level of access that users within Academus may
     * exercise on entities that originate from within this system of record.
     *
     * @return An access level.
     */
    AccessType getEntityAccessLevel();

    /**
     * Indicates whether the system of record contains a group entry for the
     * specified external id.
     *
     * @param xId An external identifier.
     * @return <code>true</code> if there is a group with the specified id;
     * otherwise, <code>false</code>.
     * @throws DomainException If the operation could not be performed
     * successfully for any reason.
     */
    public boolean hasGroup(String xId) throws DomainException;

    /**
     * Indicates whether the system of record contains a role entry for the
     * specified external id.
     *
     * @param xId An external identifier.
     * @return <code>true</code> if there is a role with the specified id;
     * otherwise, <code>false</code>.
     * @throws DomainException If the operation could not be performed
     * successfully for any reason.
     */
    public boolean hasRole(String xId) throws DomainException;

    /**
     * Indicates whether the system of record contains a topic entry for the
     * specified external id.
     *
     * @param xId An external identifier.
     * @return <code>true</code> if there is a topic with the specified id;
     * otherwise, <code>false</code>.
     * @throws DomainException If the operation could not be performed
     * successfully for any reason.
     */
    public boolean hasTopic(String xId) throws DomainException;

    /**
     * Indicates whether the system of record contains a user entry for the
     * specified external id.
     *
     * @param xId An external identifier.
     * @return <code>true</code> if there is a user with the specified id;
     * otherwise, <code>false</code>.
     * @throws DomainException If the operation could not be performed
     * successfully for any reason.
     */
    public boolean hasUser(String xId) throws DomainException;

    /**
     * Indicates whether the system of record contains an offering entry for the
     * specified external id.
     *
     * @param xId An external identifier.
     * @return <code>true</code> if there is an offering with the specified id;
     * otherwise, <code>false</code>.
     * @throws DomainException If the operation could not be performed
     * successfully for any reason.
     */
    public boolean hasOffering(String xId) throws DomainException;

    /**
     * Indicates whether the system of record contains a membership entry for
     * the specified external id.
     *
     * @param xId An external identifier.
     * @return <code>true</code> if there is a membership with the specified id;
     * otherwise, <code>false</code>.
     * @throws DomainException If the operation could not be performed
     * successfully for any reason.
     */
    public boolean hasMembership(String xId) throws DomainException;

    /**
     * Provides an internal system id for the given external id.
     *
     * @param xId An external id for a group.
     * @return The internal id for a group.
     * @throws DomainException If the internal id lookup failed.
     */
    public long getGroupId(String xId) throws DomainException;

    /**
     * Provides an internal system id for the given external id.
     *
     * @param xId An external id for a role.
     * @return The internal id for a role.
     * @throws DomainException If the internal id lookup failed.
     */
    public long getRoleId(String xId) throws DomainException;

    /**
     * Provides an internal system id for the given external id.
     *
     * @param xId An external id for a topic.
     * @return The internal id for a topic.
     * @throws DomainException If the internal id lookup failed.
     */
    public long getTopicId(String xId) throws DomainException;

    /**
     * Provides an internal system id for the given external id.
     *
     * @param xId An external id for a user.
     * @return The internal id for a user.
     * @throws DomainException If the internal id lookup failed.
     */
    public long getUserId(String xId) throws DomainException;

    /**
     * Provides an internal system id for the given external (foreign) name.
     *
     * @param foreignId An external (foreign) name for a user.
     * @return The internal id for a user.
     * @throws DomainException If the internal id lookup failed.
     */
    public long getUserIdByForeignName(String foreignId) throws DomainException;

    /**
     * Provides an internal system id for the given external id.
     *
     * @param xId An external id for an offering.
     * @return The internal id for an offering.
     * @throws DomainException If the internal id lookup failed.
     */
    public long getOfferingId(String xId) throws DomainException;

    /**
     * Provides an internal system id for the given external id.
     *
     * @param xId An external id for a membership.
     * @return The internal id for a membership.
     * @throws DomainException If the internal id lookup failed.
     */
    public long getMembershipId(String xId) throws DomainException;


    /**
     * Persists record information for the specified domain entity.
     *
     * @param e A domain entity.
     * @param xId An external id for the given domain entity.
     * @throws DomainException If the request to add the domain entity failed.
     */
    public void addRecordInfo(IDomainEntity e, String xId)
    throws DomainException;

    /**
     * Persists record information for the specified domain entity.
     *
     * @param e A domain entity.
     * @param xId An external id for the given domain entity.
     * @param foreignId A (relevant) foreign name for the entity.
     * @throws DomainException If the request to add the domain entity failed.
     */
    public void addRecordInfo(IDomainEntity e, String xId, String foreignId)
    throws DomainException;

    /**
     * Deletes record information for the specified domain entity.
     *
     * @param e A domain entity.
     * @throws DomainException If the requested delete failed.
     */
    public void deleteRecordInfo(IDomainEntity e)
    throws DomainException;

}
