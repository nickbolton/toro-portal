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
package net.unicon.academus.cache.jms;

import net.unicon.academus.cache.DomainCacheExecutor;
import net.unicon.academus.cache.DomainCacheExecutorFactory;
import net.unicon.academus.common.AcademusException;
import net.unicon.sdk.cache.jms.TextMessageHelper;

public class RoleDomainMessageHelper implements TextMessageHelper {
    private int roleID = -1;
    private String action;
    private static DomainCacheExecutor executor = null;

    static {
        loadExecutor();
    }

    /** Gets the appropiate thread safe executor */
    private static void loadExecutor() {
        try {
            executor = DomainCacheExecutorFactory.getExecutor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Constructor */
    public RoleDomainMessageHelper () { }

    /**
     * Returns the role ID of this message object.
     * @return <code>int</int> roleID - role id
     */
    public int getRoleID() {
        return this.roleID;
    }

    /**
     * Sets the role ID of this message object.
     * @return <code>int</int> roleID - role id
     */
    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }

    /**
     * Sets what action to perform on the domain. object
     * @param action - an action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Returns what action is needed to perform on the domain object.
     * @return action - an action
     */
    public String getAction() {
        return this.action;
    }

    /**
     * Returns a text Message string of the helper object.
     * @return text message - a text message representation of the helper object
     */
    public String getTextMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append(JMSMessageTypeConstants.ACTION_DEL);
        sb.append(action);
        sb.append(JMSMessageTypeConstants.SEPERATOR);
        sb.append(JMSMessageTypeConstants.ROLE_DEL);
        sb.append(this.roleID);
        return sb.toString();
    }

    /**
     * sets the values of the helper object based on the passed int text message.  The message is parsed for data.
     * @param textmessage - a text message that was built by the helper.
     */
    public void setTextMessage(String textMessage) {
        if (textMessage != null) {
            String [] splitString = textMessage.split(
                JMSMessageTypeConstants.SEPERATOR);
            String roleIDStr = null;

            if (splitString.length > 0) {
                try {
                    // The order is based on the above method getTextMessage
                    // Getting Action
                    this.action = splitString[0].replaceAll(
                        JMSMessageTypeConstants.ACTION_DEL, "");

                    // Getting Role Data
                    roleIDStr = splitString[1].replaceAll(
                        JMSMessageTypeConstants.ROLE_DEL, "");

                    if (roleIDStr != null) {
                        this.roleID = Integer.parseInt(roleIDStr);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
            }
        }
    }

    /**
     * Handles the text message that was passed in.  This call will actually
     * dirty the role domain based on the data received from the text message or from the private memeber data.
     * @see net.unicon.portal.cache.DomainCacheExecutor
     */
    public void handleTextMessage()
    throws AcademusException {
        if (JMSMessageTypeConstants.REFRESH.equals(action) && roleID != -1) {
            // Refreshing role domain
            executor.refreshRole(roleID);
        } else if (JMSMessageTypeConstants.REMOVE.equals(action) &&
                   roleID != -1) {
            // Removing role domain
            executor.removeRole(roleID);
        }
    }

    public String toString() {
        return "[" + this.getTextMessage() + "]";
    }

    /** Test Driver for parsing data */
    public static void main (String [] args) {
        RoleDomainMessageHelper helper = new RoleDomainMessageHelper();
        int roleID = 111;
        System.out.println("RoleID : " + roleID);
        helper.setRoleID(roleID);
        System.out.println("Action : " + JMSMessageTypeConstants.REFRESH);
        helper.setAction(JMSMessageTypeConstants.REFRESH);
        String textMessage = helper.getTextMessage();
        System.out.println("\tText Message : " + textMessage);
        helper.setRoleID(-100);
        helper.setAction(null);
        System.out.println("Clearing data");
        helper.setTextMessage(textMessage);
        System.out.println(helper);
        System.exit(1);
    }
}
