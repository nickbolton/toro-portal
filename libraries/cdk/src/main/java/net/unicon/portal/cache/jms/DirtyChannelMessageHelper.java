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
 * <p>This class <code>DirtyChannelMessageHelper</code> is a helper class object that is used for JMS Messaging.</p>
 * <p>Use this message helper if you want to send a message that will dirty all
 * users logged in, currently looking at the a particular channel.  The channel
 * parameter is the channel handle found in the <code>properties/Channels.xml</code>. </p>
 * @see net.unicon.portal.cache.jms.TextMessageHelper
 * @author Unicon, Inc.
 */
public class DirtyChannelMessageHelper implements TextMessageHelper {
    private String channel = null;
    private static DirtyCacheExecutor executor = null;
    static {
        loadExecutor();
    }
    /** Constructor */
    public DirtyChannelMessageHelper () { }
    /** Gets the appropiate thread safe executor */
    private static void loadExecutor() {
        try {
            executor = DirtyCacheExecutorFactory.getExecutor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Returns the channel handle associated with the message.
     * @return <code>String</code> channel - channel handle
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
     * Returns a text Message string of the helper object.
     * @return text message - a text message representation of the helper object.
     */
    public String getTextMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append(JMSMessageTypeConstants.CHANNEL_DEL);
        sb.append(channel);
        return sb.toString();
    }
    /**
     * sets the values of the helper object based on the passed int text message.  The message is parsed for data.
     * @param textmessage - a text message that was built by the helper.
     */
    public void setTextMessage(String textMessage) {
        if (textMessage != null) {
            this.channel = textMessage.replaceAll(
            JMSMessageTypeConstants.CHANNEL_DEL, "");
        }
    }
    /**
     * Handles the text message that was passed in.  This call will actually
     * dirty the channel based on the data received from the text message or from the private member data.
     * @param executor - a Dirty Cache Executer given by the <code>DirtyCacheExecuterFactory.</code>
     * @see net.unicon.portal.cache.DirtyCacheExecutor
     */
    public void handleTextMessage() throws PortalException {
        if (channel != null) {
            executor.setDirtyChannels(channel);
        } else {
            LogService.instance().log(LogService.ERROR,
            "Channel Handle not set: unable to dirty channel cache");
        }
    }
    /** Test Driver for parsing data */
    public static void main (String [] args) {
        DirtyChannelMessageHelper helper = new DirtyChannelMessageHelper();
        String channel = "AnnoucnementChannel";
        System.out.println("Channel : " + channel);
        helper.setChannelHandle(channel);
        String textMessage = helper.getTextMessage();
        System.out.println("\tText Message : " + textMessage);
        helper.setTextMessage(textMessage);
        System.out.println("Channel Data the same: " + channel.equals(helper.getChannelHandle()));
        System.exit(1);
    }
}
