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
package net.unicon.academus.cache.jms;

import javax.jms.JMSException;

import net.unicon.academus.cache.DomainCacheRequestHandler;
import net.unicon.academus.common.AcademusException;
import net.unicon.sdk.cache.jms.JMSCacheMessagePublisher;
import net.unicon.sdk.cache.jms.TextMessageHelper;

public class JMSDomainCacheRequestHandlerImpl
implements DomainCacheRequestHandler {

    /**
     * Broadcast a message to notify others that the passed in offering has been added or changed.
     * @param offeringID - offering ID
     * @see net.unicon.portal.lms.domain.Offering
     * @exception AcademusException;
     */

    public void broadcastRefreshOffering(int offeringID)
    throws AcademusException {
        this.buildOfferingTextMessage(offeringID,
            JMSMessageTypeConstants.REFRESH);
    }

    /**
     * Broadcast a message to notify others  that the passed in offering needs to be removed.
     * @param offeringID - offering ID
     * @see net.unicon.portal.lms.domain.Offering
     * @exception AcademusException
     */
    public void broadcastRemoveOffering(int offeringID)
    throws AcademusException {
        this.buildOfferingTextMessage(offeringID,
            JMSMessageTypeConstants.REMOVE);
    }

    /**
     * Builds the Text Message for offering domain objects.
     * @param offeringID - the offering ID
     * @param action - the action to perform.
     * @see net.unicon.portal.cache.jms.JMSMessageTypeConstants
     * @see net.unicon.portal.lms.domain.Offering
     * @exception AcademusException
     */
    private void buildOfferingTextMessage(int offeringID, String action)
    throws AcademusException {
        OfferingDomainMessageHelper messageHelper =
            new OfferingDomainMessageHelper();
        messageHelper.setOfferingID(offeringID);
        messageHelper.setAction(action);

        // Publish Message
        this.publishTextMessage(messageHelper);
    }

    /**
     * Broadcast a message to notify others that the passed in user reference has been added or changed.
     * @param username - a user name
     * @see net.unicon.portal.lms.domain.User
     * @exception AcademusException
     */

    public void broadcastRefreshUser(String username)
    throws AcademusException {
        this.buildUserTextMessage(username, JMSMessageTypeConstants.REFRESH);
    }

    /**
     * Broadcast a message to notify others that the passed in user reference needs to be removed.
     * @param username - a user name
     * @see net.unicon.portal.lms.domain.User
     * @exception AcademusException
     */
    public void broadcastRemoveUser(String username)
    throws AcademusException {
        this.buildUserTextMessage(username, JMSMessageTypeConstants.REMOVE);
    }

    /**
     * Builds the Text Message for user domain objects.
     * @param username - the username
     * @param action - the action to perform.
     * @see net.unicon.portal.cache.jms.JMSMessageTypeConstants
     * @see net.unicon.portal.lms.domain.User
     * @exception AcademusException
     */
    private void buildUserTextMessage(String username, String action)
    throws AcademusException {
        UserDomainMessageHelper messageHelper = new UserDomainMessageHelper();
        messageHelper.setUsername(username);
        messageHelper.setAction(action);

        // Publish Message
        this.publishTextMessage(messageHelper);
    }

    /**
     * Broadcast a message to notify others that the passed in topic reference has been added or changed.
     * @param topicID - a topic id
     * @see net.unicon.portal.lms.domain.Topic
     * @exception AcademusException
     */
    public void broadcastRefreshTopic(int topicID)
    throws AcademusException {
        this.buildTopicTextMessage(topicID, JMSMessageTypeConstants.REFRESH);
    }

    /**
     * Broadcast a message to notify others that the passed in topic reference needs to be removed.
     * @param topicID - a topic id
     * @see net.unicon.portal.lms.domain.Topic
     * @exception AcademusException
     */
    public void broadcastRemoveTopic(int topicID)
    throws AcademusException {
        this.buildTopicTextMessage(topicID, JMSMessageTypeConstants.REMOVE);
    }

    /**
     * Builds the Text Message for topic domain objects.
     * @param topicID - the topic ID
     * @param action - the action to perform.
     * @see net.unicon.portal.cache.jms.JMSMessageTypeConstants
     * @see net.unicon.portal.lms.domain.Topic
     * @exception AcademusException
     */
    private void buildTopicTextMessage(int topicID, String action)
    throws AcademusException {
        TopicDomainMessageHelper messageHelper = new TopicDomainMessageHelper();
        messageHelper.setTopicID(topicID);
        messageHelper.setAction(action);

        // Publish Message
        this.publishTextMessage(messageHelper);
    }

    /**
     * Broadcast a message to notify others that the passed in role reference has been added or changed.
     * @param roleID - a role id
     * @see net.unicon.portal.lms.domain.Role
     * @exception AcademusException
     */
    public void broadcastRefreshRole(int roleID)
    throws AcademusException {
        this.buildRoleTextMessage(roleID, JMSMessageTypeConstants.REFRESH);
    }

    /**
     * Broadcast a message to notify others that the passed in role reference needs to be removed.
     * @param roleID - a rold id
     * @see net.unicon.portal.lms.domain.Role
     * @exception AcademusException
     */
    public void broadcastRemoveRole(int roleID)
    throws AcademusException {
        this.buildRoleTextMessage(roleID, JMSMessageTypeConstants.REMOVE);
    }
    
    /**
     * Builds the Text Message for role domain objects.
     * @param roleID - the role ID
     * @param action - the action to perform.
     * @see net.unicon.portal.cache.jms.JMSMessageTypeConstants
     * @see net.unicon.portal.lms.domain.Role
     * @exception AcademusException
     */
    private void buildRoleTextMessage(int roleID, String action)
    throws AcademusException {
        RoleDomainMessageHelper messageHelper = new RoleDomainMessageHelper();
        messageHelper.setRoleID(roleID);
        messageHelper.setAction(action);

        // Publish Message
        this.publishTextMessage(messageHelper);
    }
    
    /**
     * Broadcast a message to notify others that the membership for 
     * an offering needs to be removed. 
     * @param roleID - a offering id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRemoveMembership(int offeringID)
    throws AcademusException {
        this.buildMembershipTextMessage(offeringID,
            JMSMessageTypeConstants.REMOVE);
    }
    
    /**
     * Broadcast a message to notify others that the membership for 
     * an offering needs to be removed. 
     * @param roleID - a offering id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRefreshMembership(int offeringID)
    throws AcademusException {
        this.buildMembershipTextMessage(offeringID,
            JMSMessageTypeConstants.REFRESH);
    }
    
    /**
     * Builds the Text Message for memberships domain objects.
     * @param offeringID - the offering ID
     * @param action - the action to perform.
     * @see net.unicon.portal.cache.jms.JMSMessageTypeConstants
     * @see net.unicon.portal.lms.domain.Offering
     * @exception AcademusException
     */
    private void buildMembershipTextMessage(int offeringID, String action)
    throws AcademusException {
        MembershipDomainMessageHelper messageHelper =
            new MembershipDomainMessageHelper();
        messageHelper.setOfferingID(offeringID);
        messageHelper.setAction(action);

        // Publish Message
        this.publishTextMessage(messageHelper);
    }

    /**
     * Broadcast a message to notify others that the membership for 
     * an offering needs to be removed. 
     * @param roleID - a offering id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRemoveUserMembership(String username)
    throws AcademusException {
        this.buildUserMembershipTextMessage(username,
            JMSMessageTypeConstants.REMOVE);
    }
    
    /**
     * Broadcast a message to notify others that the membership for 
     * an offering needs to be removed. 
     * @param roleID - a offering id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRefreshUserMembership(String username)
    throws AcademusException {
        this.buildUserMembershipTextMessage(username,
            JMSMessageTypeConstants.REFRESH);
    }
    
    /**
     * Builds the Text Message for memberships domain objects.
     * @param username - the user
     * @param action - the action to perform.
     * @see net.unicon.portal.cache.jms.JMSMessageTypeConstants
     * @see net.unicon.portal.lms.domain.Offering
     * @exception AcademusException
     */
    private void buildUserMembershipTextMessage(String username, String action)
    throws AcademusException {
        UserMembershipDMH messageHelper =
            new UserMembershipDMH();
        messageHelper.setUsername(username);
        messageHelper.setAction(action);

        // Publish Message
        this.publishTextMessage(messageHelper);
    }
    
    /**
     * Broadcast a message to notify others that the membership for 
     * an role needs to be removed. 
     * @param roleID - a offering id
     * @see net.unicon.academus.domain.lms.Role
     * @exception net.unicon.academus.common.AcademusException
     */
    public void broadcastRemoveRoleMembership(int offeringID)
    throws AcademusException{
        this.buildRoleMembershipTextMessage(offeringID,
            JMSMessageTypeConstants.REMOVE);
    }

    /**
     * Builds the Text Message for role domain objects.
     * @param roleID - the role ID
     * @param action - the action to perform.
     * @see net.unicon.portal.cache.jms.JMSMessageTypeConstants
     * @see net.unicon.portal.lms.domain.Role
     * @exception AcademusException
     */
    private void buildRoleMembershipTextMessage(int offering, String action)
    throws AcademusException {
        RoleMembershipDMH messageHelper = new RoleMembershipDMH();
        messageHelper.setOfferingID(offering);
        messageHelper.setAction(action);

        // Publish Message
        this.publishTextMessage(messageHelper);
    }

    private void publishTextMessage(TextMessageHelper messageHelper)
    throws AcademusException {
        try {
            JMSCacheMessagePublisher.publishAsynchTextMessage(
                messageHelper.getTextMessage(), 
                messageHelper.getClass().getName());
        } catch (JMSException e) {
            e.printStackTrace();
            throw new AcademusException(e);
        }
    }

    /*========================================================*

      DEBUG/UNIT TEST METHOD

     *========================================================*/

    public static void main (String [] args) throws Exception {
        JMSDomainCacheRequestHandlerImpl jmsUtil =
            new JMSDomainCacheRequestHandlerImpl();

        for (int ix = 0; ix < 5; ++ix) {
            long start = System.currentTimeMillis();

            System.out.println("Sending dirty Offering domain message : " +
                start);

            jmsUtil.broadcastRefreshOffering(11);
            long end   = System.currentTimeMillis();
            System.out.println("Finished dirty Channel message : " + end);
            long total = start - end;
            System.out.println("\tTotal approx time : " + total);
        }
        System.exit(1);
    }
}
