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
import net.unicon.portal.cache.DirtyCacheExecutor;
import net.unicon.portal.cache.DirtyCacheExecutorFactory;
import net.unicon.sdk.cache.jms.TextMessageHelper;

import org.jasig.portal.PortalException;
import org.jasig.portal.services.LogService;

/**
 * <p>This class <code>DirtyUserChannelMessageHelper</code> is a helper object that is used for JMS Text Messaging.  </p>
 * <p>Use this message object if you want to send a message that will dirty a
 * specific user logged in, currently looking at the a particular channel.  The channel parameter is the channel handle found
 * in the <code>properties/Channels.xml</code>. </p>
 * @author Unicon, Inc.
 */
public class DirtyUserChannelMessageHelper implements TextMessageHelper {
    private String channel  = null;
    private String username = null;
    private static DirtyCacheExecutor executor = null;
    static {
        loadExecutor();
    }
    private static void loadExecutor() {
        try {
            executor = DirtyCacheExecutorFactory.getExecutor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /** Constructor */
    public DirtyUserChannelMessageHelper () { }
    /**
     * Returns the channel handle associated with the message.
     * @return <code>String</code> channel - channel handle.
     */
    public String getChannelHandle() {
        return this.channel;
    }
    /**
     * Sets the channel handle associated with the message.
     * @param <code>String</code> channel - channel handle
     */
    public void setChannelHandle(String channel) {
        this.channel = channel;
    }
    /**
     * Returns the username associated with the message.
     * @return <code>String</code> - username.
     */
    public String getUsername() {
        return this.username;
    }
    /**
     * Returns the username associated with the message.
     * @param <code>String</code> - username.
     */
    public void setUsername(String username) {
        this.username = username;
    }
    /**
     * Returns a text Message string of the helper object.
     * @return text message - a text message representation of the helper object.
     */
    public String getTextMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append(JMSMessageTypeConstants.CHANNEL_DEL);
        sb.append(this.channel);
        sb.append(JMSMessageTypeConstants.SEPERATOR);
        sb.append(JMSMessageTypeConstants.USER_DEL);
        sb.append(this.username);
        return sb.toString();
    }
    /**
     * sets the values of the helper object based on the passed int text message.  The message is parsed for data.
     * @param textmessage - a text message that was built by the helper.
     */
    public void setTextMessage(String textMessage) {
        if (textMessage != null) {
            String [] splitString = textMessage.split(JMSMessageTypeConstants.SEPERATOR);
            if (splitString.length > 0) {
                try {
                    // The order is based on the above method getTextMessage
                    // Getting Channel Data
                    this.channel = splitString[0].replaceAll(
                    JMSMessageTypeConstants.CHANNEL_DEL, "");
                    // Getting Username Data
                    this.username = splitString[1].replaceAll(
                    JMSMessageTypeConstants.USER_DEL, "");
                } catch (ArrayIndexOutOfBoundsException e) {
                    LogService.instance().log(LogService.ERROR,
                    "Invalid format for Dirty Offering Channel Message: Missing channel or offering ID data");
                }
            }
        }
    }
    /**
     * Handles the text message that was passed in.  This call will actually
     * dirty the channel based on the data received from the text message or from the private memeber data.
     * @throws org.jasig.portal.PortalException;
     */
    public void handleTextMessage() throws PortalException {
        if (channel != null && username != null) {
            try {
                executor.setDirtyChannels(username, channel);
            } catch (Exception e) {
                throw new PortalException ("Unable to dirty user channel");
            }
        } else {
            LogService.instance().log(LogService.ERROR,
            "Username of Channel Handle not set: unable to dirty user channel cache");
        }
    }
    /** Test Driver for parsing data */
    public static void main (String [] args) {
        DirtyUserChannelMessageHelper helper = new DirtyUserChannelMessageHelper();
        String channel = "AnnoucnementChannel";
        String username = "admin";
        System.out.println("Username : " + username);
        helper.setUsername(username);
        System.out.println("Channel : " + channel);
        helper.setChannelHandle(channel);
        String textMessage = helper.getTextMessage();
        System.out.println("\tText Message : " + textMessage);
        helper.setTextMessage(textMessage);
        System.out.println("User Channel Data the same: " + (channel.equals(helper.getChannelHandle()) && username.equals(helper.getUsername())));
        System.exit(1);
    }
}
