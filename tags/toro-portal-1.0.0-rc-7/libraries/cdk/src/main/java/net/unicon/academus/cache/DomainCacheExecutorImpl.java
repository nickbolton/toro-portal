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
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.academus.domain.lms.Memberships;
import net.unicon.academus.domain.lms.OfferingFactory;
import net.unicon.academus.domain.lms.OperationFailedException;
import net.unicon.academus.domain.lms.RoleFactory;
import net.unicon.academus.domain.lms.TopicFactory;
import net.unicon.academus.domain.lms.UserFactory;

public class DomainCacheExecutorImpl implements DomainCacheExecutor {
    /**
     * offering has been added or changed.
     * @param offeringID - offering ID
     * @see net.unicon.academus.domain.lms.Offering
     * @exception net.unicon.academus.common.AcademusException
     */
    public void refreshOffering(int offeringID)
    throws AcademusException {
        try {
            OfferingFactory.refreshOfferingCache((long) offeringID);
        } catch (OperationFailedException ofe) {
            //LogService.instance().log(LogService.ERROR,
            //"Refreshing Offering : WARNING Operation failed");
            ofe.printStackTrace();
        } catch (ItemNotFoundException infe) {
            //LogService.instance().log(LogService.ERROR,
            //"Unable to find offering with the id = " + offeringID);
        }
    }
    /**
     * offering needs to be removed.
     * @param offeringID - offering ID
     * @see net.unicon.academus.domain.lms.Offering
     * @exception net.unicon.academus.common.AcademusException
     */
    public void removeOffering(int offeringID)
    throws AcademusException {
        try {
            OfferingFactory.removeOfferingFromCache((long) offeringID);
        } catch (OperationFailedException ofe) {
            //LogService.instance().log(LogService.ERROR,
            //"Removing Offering :  WARNING operation failed");
            ofe.printStackTrace();
        }
    }
    /**
     * user reference has been added or changed.
     * @param username - a user name
     * @see net.unicon.academus.domain.lms.User
     * @exception net.unicon.academus.common.AcademusException
     */
    public void refreshUser(String username)
    throws AcademusException {
        try {
            UserFactory.refreshUserCache(username);
        } catch (OperationFailedException ofe) {
//            LogService.instance().log(LogService.ERROR,
//            "Refreshing User : WARNING Operation failed");
            ofe.printStackTrace();
        } catch (ItemNotFoundException infe) {
            //LogService.instance().log(LogService.ERROR,
            //"Unable to find user with the username = " + username);
        infe.printStackTrace();
        }
    }
    /**
     * user reference needs to be removed.
     * @param username - a user name
     * @see net.unicon.academus.domain.lms.User
     * @exception net.unicon.academus.common.AcademusException
     */
    public void removeUser(String username)
    throws AcademusException {
        try {
            UserFactory.removeUserFromCache(username);
        } catch (OperationFailedException ofe) {
            //LogService.instance().log(LogService.ERROR,
            //"Removing User : WARNING Operation failed");
            ofe.printStackTrace();
        } catch (ItemNotFoundException infe) {
            //LogService.instance().log(LogService.ERROR,
            //"Unable to find user with the username = " + username);
        infe.printStackTrace();
        }
    }
    /**
     * topic reference has been added or changed.
     * @param topicID - a topic id
     * @see net.unicon.academus.domain.lms.Topic
     * @exception net.unicon.academus.common.AcademusException
     */
    public void refreshTopic(int topicID)
    throws AcademusException {
        try {
            TopicFactory.refreshTopicCache((long) topicID);
        } catch (OperationFailedException ofe) {
            //LogService.instance().log(LogService.ERROR,
            //"Refreshing Topic : WARNING Operation failed");
            ofe.printStackTrace();
        } catch (ItemNotFoundException infe) {
            //LogService.instance().log(LogService.ERROR,
            //"Unable to find Topic with the id = " + topicID);
        infe.printStackTrace();
        }
    }
    /**
     * topic reference needs to be removed.
     * @param topicID - a topic id
     * @see net.unicon.academus.domain.lms.Topic
     * @exception net.unicon.academus.common.AcademusException
     */
    public void removeTopic(int topicID)
    throws AcademusException {
        try {
            TopicFactory.removeTopicFromCache((long) topicID);
        } catch (OperationFailedException ofe) {
            //LogService.instance().log(LogService.ERROR,
            //"Removing Topic : WARNING Operation failed");
            ofe.printStackTrace();
        }
    }
    /**
     * role reference has been added or changed.
     * @param roleID - a role id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void refreshRole(int roleID)
    throws AcademusException {
        try {
            RoleFactory.refreshRoleCache((long) roleID);
        } catch (OperationFailedException ofe) {
            //LogService.instance().log(LogService.ERROR,
            //"Refreshing Role : WARNING Operation failed");
            ofe.printStackTrace();
        } catch (ItemNotFoundException infe) {
            //LogService.instance().log(LogService.ERROR,
            //"Unable to find role with the id = " + roleID);
        infe.printStackTrace();
        }
    }
    /**
     * role reference needs to be removed.
     * @param roleID - a rold id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void removeRole(int roleID)
    throws AcademusException {
        try {
            RoleFactory.removeRoleFromCache((long) roleID);
        } catch (OperationFailedException ofe) {
            //LogService.instance().log(LogService.ERROR,
            //"Removing Role : WARNING Operation failed");
            ofe.printStackTrace();
        }
    }
    /**
     * Execute the behavior for a message to notify others that the membership for 
     * an offering needs to be removed. 
     * @param roleID - a offering id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void removeMembership(int offeringID)
    throws AcademusException {
        try {
            Memberships.removeOfferingFromCache((long) offeringID);
        } catch (OperationFailedException ofe) {
            //LogService.instance().log(LogService.ERROR,
            ofe.printStackTrace();
        } 
    }
    
    /**
     * Execute the behavior for a message to notify others that the membership for 
     * an offering needs to be removed. 
     * @param roleID - a offering id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void refreshMembership(int offeringID)
    throws AcademusException {
        try {
            Memberships.refreshOfferingCache((long) offeringID);
        } catch (OperationFailedException ofe) {
            //LogService.instance().log(LogService.ERROR,
            ofe.printStackTrace();
        } 
    }

    /**
     * Execute the behavior for a message to notify others that the membership for 
     * an offering needs to be removed. 
     * @param roleID - a offering id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void removeUserMembership(String username)
    throws AcademusException {
        try {
            Memberships.removeUserFromCache(username);
        } catch (OperationFailedException ofe) {
            //LogService.instance().log(LogService.ERROR,
            ofe.printStackTrace();
        } 
    }
    
    /**
     * Execute the behavior for a message to notify others that the membership for 
     * an offering needs to be removed. 
     * @param roleID - a offering id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void refreshUserMembership(String username)
    throws AcademusException {
        try {
            Memberships.refreshUserCache(username);
        } catch (OperationFailedException ofe) {
            //LogService.instance().log(LogService.ERROR,
            ofe.printStackTrace();
        } 
    }
    
    /**
     * Execute the behavior for a message to notify others that the membership for 
     * an role needs to be removed. 
     * @param roleID - a offering id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void removeRoleMembership(int offeringID)
    throws AcademusException{
        try {
            Memberships.removeRoleFromCache((long) offeringID);
        } catch (OperationFailedException ofe) {
            //LogService.instance().log(LogService.ERROR,
            ofe.printStackTrace();
        } 
    }
}
