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
package net.unicon.portal.cache.jms;

import javax.jms.JMSException;

import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.portal.cache.DirtyCacheRequestHandler;
import net.unicon.portal.cache.DirtyCacheExecutor;
import net.unicon.portal.cache.DirtyCacheExecutorFactory;
import net.unicon.sdk.cache.jms.JMSCacheMessagePublisher;
import net.unicon.sdk.cache.jms.JMSPropertiesManager;
import net.unicon.sdk.cache.jms.TextMessageHelper;

import org.jasig.portal.PortalException;
import org.jasig.portal.services.LogService;

/**
 * The broadcast methods call the set method.  The reason is that
 * there is none to broadcast to except it self.  In the case you want to broadcast 
 * to multiple boxes, you will need to create your own DirtyCacheRequestHandler or 
 * use others in the Academus offering.  Simply set the property (factoryImpl.properties) 
 * to point to your own implementation of DirtyCacheRequestHandler.
 * NOTE: This does not handle a message to the where this message originated.
 * @see net.unicon.portal.cache.DirtyCacheRequestHandler
 */
public class JMSDirtyCacheRequestHandlerImpl implements DirtyCacheRequestHandler {
    private static DirtyCacheExecutor executor = null;
    public JMSDirtyCacheRequestHandlerImpl () {
        try {
            executor = DirtyCacheExecutorFactory.getExecutor();
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR,
            "Unable to get Dirty Cache Executor from Factory, unable to dirty this server!");
            e.printStackTrace();
        }
    }
    /**
     * Broadcast a dirty message to each of the user's channels.
     * @param <code>User</code> - a Academus user domain object
     * @see net.unicon.academus.domain.lms.User
     */
    public void broadcastDirtyChannels(User user)
    throws PortalException {
        DirtyUserMessageHelper messageHelper = new DirtyUserMessageHelper();
        messageHelper.setUsername(user.getUsername());
        this.publishTextMessage(messageHelper);
        executor.setUserDirtyChannels(user);
    }
    /**
     * Broadcast a dirty message to each user who has this channel.
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @see net.unicon.academus.domain.lms.User
     */
    public void broadcastDirtyChannels(String channelHandle)
    throws PortalException {
        DirtyChannelMessageHelper messageHelper = new DirtyChannelMessageHelper();
        messageHelper.setChannelHandle(channelHandle);
        this.publishTextMessage(messageHelper);
        executor.setDirtyChannels(channelHandle);
    }
    /**
     * Broadcast a dirty message to a specific user's channel.
     * @param <code>User</code> - a Academus user domain object
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @see net.unicon.academus.domain.lms.User
     */
    public void broadcastDirtyChannels(User user, String channel)
    throws PortalException {
        DirtyUserChannelMessageHelper messageHelper = new DirtyUserChannelMessageHelper();
        messageHelper.setChannelHandle(channel);
        messageHelper.setUsername(user.getUsername());
        this.publishTextMessage(messageHelper);
        executor.setDirtyChannels(user, channel);
    }
    /**
     * Broadcast a dirty message to <u>many</u> user's specific channel.
     * @param <code>User []</code> - an array of Academus user domain object
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @see net.unicon.academus.domain.lms.User
     */
    public void broadcastDirtyChannels(User[] users, String channel)
    throws PortalException {
        DirtyUserChannelMessageHelper messageHelper = null;
        
        for (int ix = 0; ix < users.length; ++ix) {
            messageHelper = new DirtyUserChannelMessageHelper();
            messageHelper.setUsername(users[ix].getUsername());
            messageHelper.setChannelHandle(channel);
            this.publishTextMessage(messageHelper);
        }
        executor.setDirtyChannels(users, channel);
    }
    /**
     * Broadcast a dirty message to an offering's specific channel.
     * @param <code>Offering</code> - an Academus offering domain object
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @param boolean - indicates whether all users in the offering are forced
     * to receive the dirty message.
     * @see net.unicon.academus.domain.lms.Offering
     */
    public void broadcastDirtyChannels(Offering offering, String channel,
        boolean allUsers)
    throws PortalException {
        DirtyOfferingChannelMessageHelper messageHelper = new DirtyOfferingChannelMessageHelper();
        messageHelper.setChannelHandle(channel);
        messageHelper.setOfferingID((int) offering.getId());
        messageHelper.setAllUsers(allUsers);
        this.publishTextMessage(messageHelper);
        executor.setDirtyChannels(offering, channel, allUsers);
    }
    /**
     * Broadcast a dirty message to an offering's specific channel but not the passed in user.  The reason we do this, is
     * because the user who made the request does not need to be dirty.
     * @param <code>User</code> - the request dirty user that will not be dirty.
     * @param <code>Offering</code> - an Academus offering domain object.
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @param boolean - indicates whether all users in the offering are forced
     * to receive the dirty message.
     * @see net.unicon.academus.domain.lms.User
     * @see net.unicon.academus.domain.lms.Offering
     */
    public void broadcastDirtyChannels(User user, Offering offering,
        String channel, boolean allUsers)
    throws PortalException {
        DirtyOfferingChannelMessageHelper messageHelper = new DirtyOfferingChannelMessageHelper();
        messageHelper.setChannelHandle(channel);
        messageHelper.setOfferingID((int) offering.getId());
        messageHelper.setAllUsers(allUsers);
        String username = null;
        if (user != null) {
            username = user.getUsername();
        }
        messageHelper.setUsername(username);
        this.publishTextMessage(messageHelper);
        executor.setDirtyChannels(user, offering, channel, allUsers);
    }
    private void publishTextMessage(TextMessageHelper messageHelper)
    throws PortalException {
        try {
            JMSCacheMessagePublisher.publishAsynchTextMessage(
            messageHelper.getTextMessage(),
            messageHelper.getClass().getName());
        } catch (JMSException e) {
            LogService.instance().log(LogService.ERROR,
            ": Unable to send JMS TextMessage.");
            LogService.instance().log(LogService.ERROR, e);
        }
    }
    /*========================================================*
      DEBUG/UNIT TEST METHOD
     *========================================================*/
    public static void main (String [] args) throws Exception {
        JMSDirtyCacheRequestHandlerImpl jmsUtil = new JMSDirtyCacheRequestHandlerImpl();
        /*
        net.unicon.portal.domain.User user = net.unicon.portal.domain.UserFactory.getUser("admin");
        System.out.println("Sending dirty User message");
        jmsUtil.broadcastDirtyChannels(user);
        System.out.println("Finished dirty User message");
        */
        for (int ix = 0; ix < 5; ++ix) {
            long start = System.currentTimeMillis();
            System.out.println("Sending dirty Channel message : " + start);
            jmsUtil.broadcastDirtyChannels("TheShizoChannel");
            long end   = System.currentTimeMillis();
            System.out.println("Finished dirty Channel message : " + end);
            long total = start - end;
            System.out.println("\tTotal approx time : " + total);
        }
        /*
        System.out.println("Sending dirty User's Channel message");
        jmsUtil.broadcastDirtyChannels(user, "TheShizoChannel");
        System.out.println("Finished dirty User's Channel message");
        */
        System.exit(1);
    }
}
