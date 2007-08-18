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
package net.unicon.academus.cache;

import net.unicon.academus.common.AcademusException;

public interface DomainCacheRequestHandler {
    /**
     * Broadcast a message to notify others that the passed in offering has been added or changed.
     * @param offeringID - offering ID
     * @see net.unicon.academus.domain.lms.Offering
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRefreshOffering(int offeringID)
    throws AcademusException;
    /**
     * Broadcast a message to notify others  that the passed in offering needs to be removed.
     * @param offeringID - offering ID
     * @see net.unicon.academus.domain.lms.Offering
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRemoveOffering(int offeringID)
    throws AcademusException;
    /**
     * Broadcast a message to notify others that the passed in user reference has been added or changed.
     * @param username - a user name
     * @see net.unicon.academus.domain.lms.User
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRefreshUser(String username)
    throws AcademusException;
    /**
     * Broadcast a message to notify others that the passed in user reference needs to be removed.
     * @param username - a user name
     * @see net.unicon.academus.domain.lms.User
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRemoveUser(String username)
    throws AcademusException;
    /**
     * Broadcast a message to notify others that the passed in topic reference has been added or changed.
     * @param topicID - a topic id
     * @see net.unicon.academus.domain.lms.Topic
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRefreshTopic(int topicID)
    throws AcademusException;
    /**
     * Broadcast a message to notify others that the passed in topic reference needs to be removed.
     * @param topicID - a topic id
     * @see net.unicon.academus.domain.lms.Topic
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRemoveTopic(int topicID)
    throws AcademusException;
    /**
     * Broadcast a message to notify others that the passed in role reference has been added or changed.
     * @param roleID - a role id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRefreshRole(int roleID)
    throws AcademusException;
    /**
     * Broadcast a message to notify others that the passed in role reference needs to be removed.
     * @param roleID - a rold id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRemoveRole(int roleID)
    throws AcademusException;
    
    /**
     * Broadcast a message to notify others that the membership for 
     * an offering needs to be removed. 
     * @param roleID - a offering id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRemoveMembership(int offeringID)
    throws AcademusException;
    
    /**
     * Broadcast a message to notify others that the membership for 
     * an offering needs to be removed. 
     * @param roleID - a offering id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRefreshMembership(int offeringID)
    throws AcademusException;

    /**
     * Broadcast a message to notify others that the membership for 
     * an offering needs to be removed. 
     * @param roleID - a offering id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRemoveUserMembership(String username)
    throws AcademusException;
    
    /**
     * Broadcast a message to notify others that the membership for 
     * an offering needs to be removed. 
     * @param roleID - a offering id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRefreshUserMembership(String username)
    throws AcademusException;
    
    /**
     * Broadcast a message to notify others that the membership for 
     * an role needs to be removed. 
     * @param roleID - a offering id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRemoveRoleMembership(int offeringID)
    throws AcademusException;
    
}
