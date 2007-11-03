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

import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.lms.Context;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.Topic;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.domain.ChannelClassFactory;

import org.jasig.portal.services.LogService;
import org.jasig.portal.PortalException;

/**
 * This actually does the dirty messages. To define your own, simply
 * set the property (factoryImpl.properties) to point to your own implementation of DirtyCacheExecutor.
 * @see net.unicon.portal.cache.DirtyCacheExecutor
 */
public class DirtyCacheExecutorImpl implements DirtyCacheExecutor {
    /**
     * Set the user's channels dirty. (No broadcasting)
     * @param <code>User</code> - a Academus user domain object
     * @see net.unicon.portal.domain.User
     */
    public void setUserDirtyChannels(User user)
    throws PortalException {
        setUserDirtyChannels(user.getUsername());
    }
    /**
     * Set the user's channels dirty. (No broadcasting)
     * @param <code>String</code> - a Academus username
     */
    public void setUserDirtyChannels(String username)
    throws PortalException {
        StringBuffer sb = new StringBuffer();
        sb.append("DirtyCacheExecutorImpl/setUserDirtyChannels: Dirty'n User cache for ");
        sb.append(username);
        LogService.instance().log(LogService.INFO, sb.toString());
        // Get userChannel map from the ChannelDataManager
        Map userChannelUIDs = ChannelDataManager.getChannelUIDs(username);
        // If we didn't get a map back. The user is
        // either not on the system or something else.
        // Regardless, we have no way to get the upId
        // to set the ChannelData dirty
        if (userChannelUIDs == null) return;
        // Iterate through all the channels and set the
        // channel dirty for the user.
        Iterator itr = userChannelUIDs.values().iterator();
        while (itr != null && itr.hasNext()) {
            // Getting the upID (unique channel id to the user session)
            String uid = (String)itr.next();
            // Setting the channel dirty
            this.setChannelDirty(uid, true);
        }
    }
    /**
     * Set each user who has this channel dirty. (No broadcasting)
     * @param <code>String</code> - a channel handle
     */
    public void setDirtyChannels(String channelHandle)
    throws PortalException {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("DirtyCacheExecutorImpl/setDirtyChannels: Dirty'n Channel cache for ");
            sb.append(channelHandle);
            LogService.log(LogService.INFO, sb.toString());
            // Get the whole map from ChannelDataManager (the Master List)
            Map uidTable = ChannelDataManager.getUIDTable();
            Iterator itr = uidTable.values().iterator();

            // do nothing if the channel class doesn't exist
            if (!ChannelClassFactory.exists(channelHandle)) {
                sb.delete(0, sb.length());
                sb.append("DirtyCacheExecutorImpl.setDirtyChannels(");
                sb.append(channelHandle);
                sb.append(") channelClass doesn't exist!");
                LogService.log(LogService.DEBUG, sb.toString());
                return;
            }

            ChannelClass cc = ChannelClassFactory.getChannelClass(channelHandle);
            // Iterate through the map
            while (itr.hasNext()) {
                // get the users Map of uids
                Map userChannelUIDs = (Map)itr.next();
                if (userChannelUIDs != null) {
                    // Get the uid of the passed in parameter value (Channel Handle)
                    String uid = (String)userChannelUIDs.get(cc);
                    // Setting the channel dirty
                    this.setChannelDirty(uid, true);
                }
            }
        } catch (DomainException de) {
            throw new PortalException(de);
        }
    }
    /**
     * Set specific user's channel dirty.
     * @param <code>User</code> - a Academus user domain object
     * @param <code>String</code> - a channel handle
     * @see net.unicon.portal.domain.User
     */
    public void setDirtyChannels(User user, String channel)
    throws PortalException {
        setDirtyChannels(user.getUsername(), channel);
    }
    /**
     * Set specific user's channel dirty.
     * @param <code>String</code> - a Academus user domain object
     * @param <code>String</code> - a channel handle
     */
    public void setDirtyChannels(String username, String channel)
    throws PortalException {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("DirtyCacheExecutorImpl/setDirtyChannels(user,channel): ");
            sb.append("Dirty'n Users Channel cache for ");
            sb.append(username);
            sb.append(" and channel ");
            sb.append(channel);
            LogService.instance().log(LogService.INFO, sb.toString());

            // do nothing if the channel class doesn't exist
            if (!ChannelClassFactory.exists(channel)) {
                sb.delete(0, sb.length());
                sb.append("DirtyCacheExecutorImpl.setDirtyChannels(");
                sb.append(username).append(", ").append(channel);
                sb.append(") channelClass doesn't exist!");
                LogService.log(LogService.DEBUG, sb.toString());
                return;
            }

            ChannelClass cc = ChannelClassFactory.getChannelClass(channel);
            // Get userChannel map from the ChannelDataManager
            Map userChannelUIDs = ChannelDataManager.getChannelUIDs(username);
            // If we didn't get a map back. The user is
            // either not on the system or something else.
            // Regardless, we have no way to get the upId
            // to set the ChannelData dirty
            if (userChannelUIDs != null) {
                // Get the uid of the passed in parameter value (Channel Handle)
                String uid = (String) userChannelUIDs.get(cc);
                // Setting the channel dirty
                this.setChannelDirty(uid, true);
            }
        } catch (DomainException de) {
            throw new PortalException(de);
        }
    }
    /**
     * Set <u>many</u> user's specific channel dirty.
     * @param <code>User []</code> - an array of Academus user domain object
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @see net.unicon.portal.domain.User
     */
    public void setDirtyChannels(User[] users, String channel)
    throws PortalException {
        try {
            // do nothing if the channel class doesn't exist
            if (!ChannelClassFactory.exists(channel)) {
                StringBuffer sb = new StringBuffer();
                sb.append("DirtyCacheExecutorImpl.setDirtyChannels(");
                sb.append("User[], ").append(channel);
                sb.append(") channelClass doesn't exist!");
                LogService.log(LogService.DEBUG, sb.toString());
                return;
            }

            Map userChannelUIDs = null;
            ChannelClass cc = ChannelClassFactory.getChannelClass(channel);
            String uid = null;
            for (int i = 0; i < users.length; i++) {
                userChannelUIDs = (Map) ChannelDataManager.getChannelUIDs(users[i]);
                if (userChannelUIDs != null) {
                    uid = (String) userChannelUIDs.get(cc);
                    this.setChannelDirty(uid, true);
                }
            }
        } catch (DomainException de) {
de.printStackTrace();
            throw new PortalException(de);
        }
    }
    /**
     * This method is designed for channels to set other channels dirty. Otherwise, use the setDirty() method
     * to set one's self dirty.
     * @param <code>String</code> - a channel's upId
     * @param <code>boolean</code> -
     */
    protected void setChannelDirty(String upId, boolean dirty)
    throws PortalException {
        if (ChannelDataManager.uidExists(upId)) {
        LogService.instance().log(LogService.INFO, "In dirty cache executor setChannelDirty for upId " + upId);
            ChannelDataManager.setDirty(upId, dirty);
        }
    }
    /**
     * Set a dirty message to an offering's specific channel.
     * @param <code>Offering</code> - an Academus offering domain object
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @param boolean - indicates whether all users in the offering are forced
     * to receive the dirty message.
     * @see net.unicon.portal.domain.Offering
     */
    public void setDirtyChannels(Offering offering, String channel,
        boolean allUsers)
    throws PortalException {
        this.setDirtyChannels((String) null, offering, channel, allUsers);
    }
    /**
     * Set a dirty message to an offering's specific channel.
     * @param <code>User</code> - the user not to be dirtied.
     * @param <code>Offering</code> - an Academus offering domain object
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @param boolean - indicates whether all users in the offering are forced
     * to receive the dirty message.
     * @see net.unicon.portal.domain.User
     * @see net.unicon.portal.domain.Offering
     */
    public void setDirtyChannels(User user, Offering offering,
        String channel, boolean allUsers)
    throws PortalException {
        if (user != null) {
            this.setDirtyChannels(user.getUsername(), offering, channel, allUsers);
        } else {
            this.setDirtyChannels(offering, channel, allUsers);
        }
    }
    /**
     * Set a dirty message to an offering's specific channel.
     * @param <code>String</code> - the username of the user not to be dirtied.
     * @param <code>Offering</code> - an Academus offering domain object
     * @param <code>String</code> - a channel handle (found in Channels.xml)
     * @param boolean - indicates whether all users in the offering are forced
     * to receive the dirty message.
     * @see net.unicon.portal.domain.Offering
     */
    public void setDirtyChannels(String username, Offering offering,
        String channel, boolean allUsers)
    throws PortalException {
        // If no offering was passed in, we throw an exception;
        if (offering == null) {
            throw new PortalException("Offering is not defined" );
        }
        List usersInOffering = null;
        ArrayList rval = new ArrayList();
        // Domain objects
        User     nextUser     = null;
        Context  nextContext  = null;
        Offering nextOffering = null;
        // If we do not get a topic we throw an exception
        Topic topic = offering.getTopic();
        if (topic == null) {
            throw new PortalException("Could not get topic for offering");
        }

        StringBuffer sb = new StringBuffer();
        sb.append("DirtyCacheExecutorImpl/setDirtyChannels(u,o,c): Dirty'n User cache for ");
        sb.append(username);
        sb.append(", ");
        sb.append(offering.getName());
        sb.append(", ");
        sb.append(channel);
        sb.append(", ");
        sb.append(allUsers);
        LogService.instance().log(LogService.INFO, sb.toString());

        try {
            usersInOffering = UserFactory.getUsers(offering);
            TopicType topicType = topic.getTopicType();
            /* Checking to see if any user is currently logged into the class */
            for (Iterator userIt = usersInOffering.iterator(); userIt.hasNext(); ) {
                nextUser = (User)userIt.next();

                // if we are forcing all users to be dirtied, then add it
                // and continue on...
                if (allUsers) {
                    rval.add(nextUser);
                    continue;
                }

                nextContext = nextUser.getContext();
                if (nextContext != null) {
                    nextOffering = nextContext.getCurrentOffering(topicType);
                    /* Checking to see if we are forcing all users or
                       the user is in the offering */
                    if (offering.equals(nextOffering) &&
                        !nextUser.getUsername().equals(username)) {
                        rval.add(nextUser);
                    }
                }
            }
            /* If there is are users in the list, make them dirty */
            if (rval.size() > 0) {
                this.setDirtyChannels(((User[]) rval.toArray(new User[0])), channel);
            }
        } catch (Exception ofe) {
ofe.printStackTrace();
            throw new PortalException("Could not set user offering channels dirty", ofe);
        }
    }
    public void setSubChannelDirty(String channelHandle, boolean dirtyValue)
    throws PortalException {
        this.setChannelDirty(channelHandle, dirtyValue);
    }
}
