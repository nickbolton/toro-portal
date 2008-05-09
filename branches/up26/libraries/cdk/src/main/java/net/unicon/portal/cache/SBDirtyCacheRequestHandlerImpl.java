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
package net.unicon.portal.cache;
import java.util.Map;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import net.unicon.academus.domain.lms.Context;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.Topic;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.portal.cache.DirtyCacheRequestHandler;
import net.unicon.portal.cache.DirtyCacheExecutor;
import net.unicon.portal.common.cdm.ChannelDataManager;

import org.jasig.portal.PortalException;
import org.jasig.portal.services.LogService;
/**
 * The broadcast methods call the set method.  The reason is that
 * there is none to broadcast to except it self.  In the case you want to broadcast to multiple boxes, you will need to create
 * your own DirtyCacheRequestHandler or use others in the Academus offering.  Simply
 * set the property (factoryImpl.properties) to point to your own implementation of DirtyCacheRequestHandler.
 * @see net.unicon.portal.cache.DirtyCacheRequestHandler
 */
public class SBDirtyCacheRequestHandlerImpl implements DirtyCacheRequestHandler {
    private static DirtyCacheExecutor executor = null;
    public SBDirtyCacheRequestHandlerImpl () {
        executor = (DirtyCacheExecutor) new DirtyCacheExecutorImpl();
    }
    /**
     * Broadcast a dirty message to each of the user's channels.
     * @param <code>User</code> - a Academus user domain object
     * @see net.unicon.academus.domain.lms.User
     */
    public void broadcastDirtyChannels(User user)
    throws PortalException {
        executor.setUserDirtyChannels(user);
    }
    /**
     * Broadcast a dirty message to each user who has this channel.
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @see net.unicon.academus.domain.lms.User
     */
    public void broadcastDirtyChannels(String channelHandle)
    throws PortalException {
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
        executor.setDirtyChannels(users, channel);
    }
    /**
     * Broadcast a dirty message to an offering's specific channel.
     * @param <code>Offering</code> - an Academus offering domain object
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @param <code>boolean</code> - forces all users in the offering to be dirtied
     * @see net.unicon.academus.domain.lms.Offering
     */
    public void broadcastDirtyChannels(Offering offering, String channel,
        boolean allUsers)
    throws PortalException {
        executor.setDirtyChannels(offering, channel, allUsers);
    }
    /**
     * Broadcast a dirty message to an offering's specific channel but not the passed in user.  The reason we do this, is
     * because the user who made the request does not need to be dirty.
     * @param <code>User</code> - the request dirty user that will not be dirty.
     * @param <code>Offering</code> - an Academus offering domain object.
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @param <code>boolean</code> - forces all users in the offering to be dirtied
     * @see net.unicon.academus.domain.lms.User
     * @see net.unicon.academus.domain.lms.Offering
     */
    public void broadcastDirtyChannels(User user, Offering offering,
        String channel, boolean allUsers)
    throws PortalException {
        executor.setDirtyChannels(user, offering, channel, allUsers);
    }
}
