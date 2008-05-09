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
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.OfferingFactory;
import net.unicon.portal.cache.DirtyCacheExecutor;
import net.unicon.portal.cache.DirtyCacheExecutorFactory;
import net.unicon.sdk.cache.jms.TextMessageHelper;

import org.jasig.portal.PortalException;
import org.jasig.portal.services.LogService;

/**
 * <p>This class <code>DirtyOfferingChannelMessageHelper</code> is a helper object that is used for JMS Messaging. </p>
 * <p>Use this message object if you want to send a message that will dirty all
 * users logged in a specific offering, currently looking at the a particular channel.  The channel parameter is the channel
 * handle found in the <code>properties/Channels.xml</code>. </p>
 * @author Unicon, Inc.
 */
public class DirtyOfferingChannelMessageHelper implements TextMessageHelper {
    private String channel  = null;
    private String username = null;
    private int offeringID = -1;
    private boolean allUsers = false;
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
    public DirtyOfferingChannelMessageHelper () {
    }
    /**
     * Returns the offering ID of this message object.
     * @return <code>int</code> offeringID - offering id
     */
    public int getOfferingID() {
        return this.offeringID;
    }
    /**
     * Sets the offering ID of this message object.
     * @param <code>int</code> offeringID - offering id
     */
    public void setOfferingID(int offeringID) {
        this.offeringID = offeringID;
    }
    /**
     * Returns the allUsers state.
     * @return <code>boolean</code> the allUsers state
     */
    public boolean getAllUsers() {
        return this.allUsers;
    }
    /**
     * Sets the allUsers state
     * @param <code>boolean</code> allUsers
     */
    public void setAllUsers(boolean allUsers) {
        this.allUsers = allUsers;
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
     * Gets the username associated with the message. This user will be filtered out of the dirtying.  Usually
     * this is the person who orginally sent the message.
     * @return <code>String</code> channel - channel handle
     */
    public String getUsername() {
        return this.username;
    }
    /**
     * Sets the username associated with the message. This user will be filtered out of the dirtying.  Usually
     * this is the person who orginally sent the message.
     * @param <code>String</code> channel - channel handle
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
        sb.append(JMSMessageTypeConstants.OFFERING_DEL);
        sb.append(this.offeringID);
        sb.append(JMSMessageTypeConstants.SEPERATOR);
        sb.append(JMSMessageTypeConstants.USER_DEL);
        sb.append(this.username);
        sb.append(JMSMessageTypeConstants.SEPERATOR);
        sb.append(JMSMessageTypeConstants.ALLUSERS_DEL);
        sb.append(this.allUsers);
        return sb.toString();
    }
    /**
     * sets the values of the helper object based on the passed int text message.  The message is parsed for data.
     * @param textmessage - a text message that was built by the helper.
     */
    public void setTextMessage(String textMessage) {
        if (textMessage != null) {
            String [] splitString = textMessage.split(JMSMessageTypeConstants.SEPERATOR);
            String offeringIDStr = null;
            if (splitString.length > 0) {
                try {
                    // The order is based on the above method getTextMessage
                    // Getting Channel Data
                    this.channel = splitString[0].replaceAll(
                    JMSMessageTypeConstants.CHANNEL_DEL, "");
                    // Getting Offering Data
                    offeringIDStr = splitString[1].replaceAll(
                    JMSMessageTypeConstants.OFFERING_DEL, "");
                    if (offeringIDStr != null) {
                        this.offeringID = Integer.parseInt(offeringIDStr);
                    }
                    // Getting Username data
                    this.username = splitString[2].replaceAll(
                    JMSMessageTypeConstants.USER_DEL, "");

                    // Getting allUsers data
                    this.allUsers = new Boolean(splitString[3].replaceAll(
                    JMSMessageTypeConstants.ALLUSERS_DEL, "")).booleanValue();
                } catch (ArrayIndexOutOfBoundsException e) {
                    LogService.instance().log(LogService.ERROR,
                    "Invalid format for Dirty Offering Channel Message: Missing channel or offering ID data");
                } catch (NumberFormatException nfe) {
                    LogService.instance().log(LogService.ERROR,
                    "Invalid OfferingID String: Unable to Parse: ->" + offeringIDStr);
                }
            }
        }
    }
    /**
     * Handles the text message that was passed in.  This call will actually
     * dirty the channel based on the data received from the text message or from the private memeber data.
     * @see net.unicon.portal.cache.DirtyCacheExecutor
     */
    public void handleTextMessage() throws PortalException {
        if (channel != null && offeringID > -1) {
            try {
                executor.setDirtyChannels(username,
                    OfferingFactory.getOffering(offeringID), channel, allUsers);
            } catch (Exception e) {
                throw new PortalException ("Unable to dirty offering channel");
            }
        } else {
            LogService.instance().log(LogService.ERROR,
            "Channel Handle not set: unable to dirty channel cache");
        }
    }
    /** Test Driver for parsing data */
    public static void main (String [] args) {
        DirtyOfferingChannelMessageHelper helper    = new DirtyOfferingChannelMessageHelper();
        DirtyOfferingChannelMessageHelper newhelper = new DirtyOfferingChannelMessageHelper();
        String channel = "AnnoucnementChannel";
        String username = null; //"admin";
        int offeringID = 111;
        boolean allUsers = true;
        System.out.println("Channel    : " + channel);
        System.out.println("OfferingID : " + offeringID);
        System.out.println("Username   : " + username);
        System.out.println("AllUsers   : " + allUsers);
        helper.setChannelHandle(channel);
        helper.setOfferingID(offeringID);
        helper.setUsername(username);
        helper.setAllUsers(allUsers);
        String textMessage = helper.getTextMessage();
        System.out.println("\tText Message : " + textMessage);
        newhelper.setTextMessage(textMessage);
        System.out.println("Channel  Data the same: " + channel.equals(newhelper.getChannelHandle()));
        System.out.println("\t" + channel + " == " + newhelper.getChannelHandle());
        System.out.println("Offering Data the same: " + (offeringID == newhelper.getOfferingID()));
        System.out.println("\t" + offeringID + " == " + newhelper.getOfferingID());
        System.out.println("Username Data the same: "); // + username.equals(newhelper.getUsername()));
        System.out.println("\t" + username + " == " + newhelper.getUsername());
        System.out.println("AllUsers Data the same: ");
        System.out.println("\t" + allUsers + " == " + newhelper.getAllUsers());
        System.exit(1);
    }
}
