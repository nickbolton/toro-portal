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
 * <p>This class <code>DirtyUserMessageHelper</code> is a helper object that is used for JMS Text Messaging.  </p>
 * <p>Use this message object if you want to send a message that will dirty a
 * specific user logged in, regardless of what channels the user iscurrently looking at the a particular channel.  This will
 * dirty all of the channels in the Academus LMS. </p>
 * @author Unicon, Inc.
 */
public class DirtyUserMessageHelper implements TextMessageHelper  {
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
    public DirtyUserMessageHelper () {
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
        sb.append(JMSMessageTypeConstants.USER_DEL);
        sb.append(username);
        return sb.toString();
    }
    /**
     * sets the values of the helper object based on the passed int text message.  The message is parsed for data.
     * @param textmessage - a text message that was built by the helper.
     */
    public void setTextMessage(String textMessage) {
        if (textMessage != null) {
            this.username = textMessage.replaceAll(JMSMessageTypeConstants.USER_DEL, "");
        }
    }
    /**
     * Handles the text message that was passed in.  This call will actually
     * dirty the channel based on the data received from the text message or from the private member data.
     * @see net.unicon.portal.cache.DirtyCacheExecutor
     * @throws org.jasig.portal.PortalException
     */
    public void handleTextMessage()
    throws PortalException {
        if (username != null) {
            executor.setUserDirtyChannels(username);
        } else {
            LogService.instance().log(LogService.ERROR,
            "Username not set: unable to dirty user cache");
        }
    }
    /** Test Driver for parsing data */
    public static void main (String [] args) {
        DirtyUserMessageHelper helper = new DirtyUserMessageHelper();
        System.out.println("Classname : " + ((TextMessageHelper) helper).getClass().getName());
        String username = "admin";
        System.out.println("Username : " + username);
        helper.setUsername(username);
        String textMessage = helper.getTextMessage();
        System.out.println("\tText Message : " + textMessage);
        helper.setTextMessage(textMessage);
        System.out.println("Username Data the same: " + username.equals(helper.getUsername()));
        System.exit(1);
    }
}
