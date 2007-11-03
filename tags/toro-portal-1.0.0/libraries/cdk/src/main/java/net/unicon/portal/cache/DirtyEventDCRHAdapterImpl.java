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

import net.unicon.academus.domain.lms.Context;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.permissions.DirtyEvent;

import org.jasig.portal.PortalException;
import org.jasig.portal.services.LogService;

/**
The one (and only one should be needed) DirtyEventDCRHAdapter implementation.
This impl delegates pass-through methdos to the system's specified DCRH
implementation, and handles the two added methods on the adapter interface
by mapping them to the appropriate DCRH calls.

@author KG
*/
public final class DirtyEventDCRHAdapterImpl implements DirtyEventDCRHAdapter {

    private static DirtyCacheRequestHandler  __cacheHandler = null;
    private static DirtyEventDCRHAdapterImpl __adapter = null;

    private DirtyEventDCRHAdapterImpl() throws PortalException {
        try {
            __cacheHandler = DirtyCacheRequestHandlerFactory.getHandler();
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, "AcademusMultithreadedChannel: Unable to get DirtyCacheRequestHandler Implementation; DirtyCacheRequestHandlerFactory is throwing errors");
            e.printStackTrace();
	    throw new PortalException(e);
        }
    }

    public static DirtyEventDCRHAdapter getInstance() throws PortalException {
	if (__adapter == null) {
	    __adapter = new DirtyEventDCRHAdapterImpl();
	}
	return __adapter;
    }

    public void broadcastDirtyEvent(DirtyEvent de,
				    String upId) 
	throws PortalException
    {
	if (!de.getFlag().booleanValue()) {
	    // dirty event ain't even "on"
	    return;
	}

	// logic in here to look at the de, decide the scope of
	// what needs dirtying, and assemble the info for the
	// right call to the cacheHandler

	User user = null;
	Offering offering = null;
	Context context = null;
	String eventScope = de.getEventScope();
	if (eventScope.equals(DirtyEvent.SCOPE_USER)) {
	    user = ChannelDataManager.getDomainUser(upId);
	    broadcastDirtyChannels(user);
	    LogService.instance().log(LogService.INFO, "DCRH Adapter dirtying SCOPE_USER");
	}
	else if (eventScope.equals(DirtyEvent.SCOPE_CHANNEL)) {
	    // loop through the set of channel targets and call the dirty handler
	    String[] targets = de.getTargetChannelHandles();
	    for (int i = 0; i < targets.length; i++) {
		broadcastDirtyChannels(targets[i]);
	    }
	    LogService.instance().log(LogService.INFO, "DCRH Adapter dirtying SCOPE_CHANNEL");
	}
	else if (eventScope.equals(DirtyEvent.SCOPE_USER_CHANNEL)) {
	    user = ChannelDataManager.getDomainUser(upId);
	    // loop through the set of channel targets and call the dirty handler
	    String[] targets = de.getTargetChannelHandles();
	    for (int i = 0; i < targets.length; i++) {
		broadcastDirtyChannels(user, targets[i]);
	    }
	    LogService.instance().log(LogService.INFO, "DCRH Adapter dirtying SCOPE_USER_CHANNEL");
	}
	else if (eventScope.equals(DirtyEvent.SCOPE_USERS_CHANNEL)) {
	    // XXX no idea how to get the set of Users
	    LogService.instance().log(LogService.INFO, "DCRH Adapter cannot dirty SCOPE_USERS_CHANNEL");
	}
	else if (eventScope.equals(DirtyEvent.SCOPE_OFFERING_CHANNEL)) {
	    user = ChannelDataManager.getDomainUser(upId);
	    context = user.getContext();
	    offering = context.getCurrentOffering(TopicType.ACADEMICS);
	    // loop through the set of channel targets and call the dirty handler
	    String[] targets = de.getTargetChannelHandles();
	    for (int i = 0; i < targets.length; i++) {
		broadcastDirtyChannels(offering, targets[i], false);
	    }
	    LogService.instance().log(LogService.INFO, "DCRH Adapter dirtying SCOPE_OFFERING_CHANNEL");
	}
	else if (eventScope.equals(DirtyEvent.SCOPE_USER_OFFERING_CHANNEL)) {
	    user = ChannelDataManager.getDomainUser(upId);
	    context = user.getContext();
	    offering = context.getCurrentOffering(TopicType.ACADEMICS);
	    // loop through the set of channel targets and call the dirty handler
	    String[] targets = de.getTargetChannelHandles();
	    for (int i = 0; i < targets.length; i++) {
		broadcastDirtyChannels(user, offering, targets[i], false);
	    }
	    LogService.instance().log(LogService.INFO, "DCRH Adapter dirtying SCOPE_USER_OFFERING_CHANNEL");
	}
    }

    public void broadcastDirtyEvents(DirtyEvent[] de, 
				     String upId) 
	throws PortalException
    {
	if (de != null) {
	    for (int i = 0; i < de.length;) {
		broadcastDirtyEvent(de[i++], upId);
	    }
	}
    }

    // METHODS ON DirtyCacheRequestHandler

    /**
     * Broadcast a dirty message to each of the user's channels.
     * @param <code>User</code> - a Academus user domain object
     * @see net.unicon.portal.domain.User
     */
    public void broadcastDirtyChannels(User user)
	throws PortalException
    {
	__cacheHandler.broadcastDirtyChannels(user);
    }

    /**
     * Broadcast a dirty message to each user who has this channel.
     * @param <code>String</code> - a channel handle
     */
    public void broadcastDirtyChannels(String channelHandle)
	throws PortalException {
	__cacheHandler.broadcastDirtyChannels(channelHandle);
    }

    /**
     * Broadcast a dirty message to a specific user's channel.
     * @param <code>User</code> - a Academus user domain object
     * @param <code>String</code> - a channel handle
     * @see net.unicon.portal.domain.User
     */
    public void broadcastDirtyChannels(User user, String channelHandle)
	throws PortalException {
	__cacheHandler.broadcastDirtyChannels(user, channelHandle);
    }

    /**
     * Broadcast a dirty message to <u>many</u> user's specific channel.
     * @param <code>User []</code> - an array of Academus user domain object
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @see net.unicon.portal.domain.User
     */
    public void broadcastDirtyChannels(User[] users, String channelHandle)
	throws PortalException {
	__cacheHandler.broadcastDirtyChannels(users, channelHandle);
    }

    /**
     * Broadcast a dirty message to an offering's specific channel.
     * @param <code>Offering</code> - an Academus offering domain object
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @param <code>boolean</code> - forces all users in the offering to get dirtied
     * @see net.unicon.portal.domain.Offering
     */
    public void broadcastDirtyChannels(Offering offering, String channelHandle,
        boolean allUsers)
	throws PortalException {
	__cacheHandler.broadcastDirtyChannels(offering, channelHandle, allUsers);
    }

    /**
     * Broadcast a dirty message to an offering's specific channel but not the passed in user.  The reason we do this, is
     * because the user who made the request does not need to be dirty.
     * @param <code>User</code> - the request dirty user that will not be dirty.
     * @param <code>Offering</code> - an Academus offering domain object.
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @param <code>boolean</code> - forces all users in the offering to get dirtied
     * @see net.unicon.portal.domain.User
     * @see net.unicon.portal.domain.Offering
     */
    public void broadcastDirtyChannels(User user, Offering offering,
        String channel, boolean allUsers)
	throws PortalException {
	__cacheHandler.broadcastDirtyChannels(user, offering, channel, allUsers);
    }
}
