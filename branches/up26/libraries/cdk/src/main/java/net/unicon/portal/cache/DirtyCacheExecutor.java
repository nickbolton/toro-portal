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

public interface DirtyCacheExecutor {
    /**
     * Set the user's channels dirty. (No broadcasting)
     * @param <code>User</code> - a Academus user domain object
     * @see net.unicon.academus.domain.lms.User
     */
    public void setUserDirtyChannels(User user)
    throws PortalException;
    /**
     * Set the user's channels dirty. (No broadcasting)
     * @param <code>String</code> - a Academus username
     */
    public void setUserDirtyChannels(String username)
    throws PortalException;
    /**
     * Set each user who has this channel dirty. (No broadcasting)
     * @param <code>String</code> - a channel handle
     */
    public void setDirtyChannels(String channelHandle)
    throws PortalException;
    /**
     * Set specific user's channel dirty.
     * @param <code>User</code> - a Academus user domain object
     * @param <code>String</code> - a channel handle
     * @see net.unicon.academus.domain.lms.User
     */
    public void setDirtyChannels(User user, String channel)
    throws PortalException;
    /**
     * Set specific user's channel dirty.
     * @param <code>String</code> - a Academus user name
     * @param <code>String</code> - a channel handle
     */
    public void setDirtyChannels(String username, String channel)
    throws PortalException;
    /**
     * Set <u>many</u> user's specific channel dirty.
     * @param <code>User []</code> - an array of Academus user domain object
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @see net.unicon.academus.domain.lms.User
     */
    public void setDirtyChannels(User[] users, String channel)
    throws PortalException;
    /**
     * Set a dirty message to an offering's specific channel.
     * @param <code>Offering</code> - an Academus offering domain object
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @param boolean - indicates whether all users in the offering are forced
     * to receive the dirty message.
     * @see net.unicon.academus.domain.lms.Offering
     */
    public void setDirtyChannels(Offering offering, String channel,
        boolean allUsers)
    throws PortalException;
    /**
     * Set a dirty message to an offering's specific channel.
     * @param <code>String</code> - the username of the user not to be dirtied.
     * @param <code>Offering</code> - an Academus offering domain object
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @param boolean - indicates whether all users in the offering are forced
     * to receive the dirty message.
     * @see net.unicon.academus.domain.lms.Offering
     */
    public void setDirtyChannels(String username, Offering offering,
        String channel, boolean allUsers)
    throws PortalException;
    /**
     * Set a dirty message to an offering's specific channel.
     * @param <code>User</code> - the user not to be dirtied.
     * @param <code>Offering</code> - an Academus offering domain object
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @param boolean - indicates whether all users in the offering are forced
     * to receive the dirty message.
     * @see net.unicon.academus.domain.lms.User
     * @see net.unicon.academus.domain.lms.Offering
     */
    public void setDirtyChannels(User user, Offering offering,
        String channel, boolean allUsers)
    throws PortalException;
}
