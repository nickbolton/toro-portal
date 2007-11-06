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
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.User;

import org.jasig.portal.PortalException;
public interface DirtyCacheRequestHandler {
    /**
     * Broadcast a dirty message to each of the user's channels.
     * @param <code>User</code> - a Academus user domain object
     * @see net.unicon.portal.domain.User
     */
    public void broadcastDirtyChannels(User user)
    throws PortalException;
    /**
     * Broadcast a dirty message to each user who has this channel.
     * @param <code>String</code> - a channel handle
     */
    public void broadcastDirtyChannels(String channelHandle)
    throws PortalException;
    /**
     * Broadcast a dirty message to a specific user's channel.
     * @param <code>User</code> - a Academus user domain object
     * @param <code>String</code> - a channel handle
     * @see net.unicon.portal.domain.User
     */
    public void broadcastDirtyChannels(User user, String channelHandle)
    throws PortalException;
    /**
     * Broadcast a dirty message to <u>many</u> user's specific channel.
     * @param <code>User []</code> - an array of Academus user domain object
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @see net.unicon.portal.domain.User
     */
    public void broadcastDirtyChannels(User[] users, String channelHandle)
    throws PortalException;
    /**
     * Broadcast a dirty message to an offering's specific channel.
     * @param <code>Offering</code> - an Academus offering domain object
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @param <code>boolean</code> - forces all uses in the offering to be dirtied
     * @see net.unicon.portal.domain.Offering
     */
    public void broadcastDirtyChannels(Offering offering, String channelHandle,
        boolean allUsers)
    throws PortalException;
    /**
     * Broadcast a dirty message to an offering's specific channel but not the passed in user.  The reason we do this, is
     * because the user who made the request does not need to be dirty.
     * @param <code>User</code> - the request dirty user that will not be dirty.
     * @param <code>Offering</code> - an Academus offering domain object.
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @param <code>boolean</code> - forces all uses in the offering to be dirtied
     * @see net.unicon.portal.domain.User
     * @see net.unicon.portal.domain.Offering
     */
    public void broadcastDirtyChannels(User user, Offering offering,
        String channel, boolean allUsers)
    throws PortalException;
}
