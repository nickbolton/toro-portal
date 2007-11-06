
package net.unicon.mercury.fac.rdbms;

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

/**
 * Dummy bean to make hibernate db loading happy.
 */
public class Dispatch {

    private int messageId;
    private String dispatchOwner;
    private int dispatchType;
    private int recipientType;
    private char unread;
    private int folders;
    public int getMessageId() {
        return messageId;
    }
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
    public String getDispatchOwner() {
        return dispatchOwner;
    }
    public void setDispatchOwner(String dispatchOwner) {
        this.dispatchOwner = dispatchOwner;
    }
    public int getDispatchType() {
        return dispatchType;
    }
    public void setDispatchType(int dispatchType) {
        this.dispatchType = dispatchType;
    }
    public int getRecipientType() {
        return recipientType;
    }
    public void setRecipientType(int recipientType) {
        this.recipientType = recipientType;
    }
    public char getUnread() {
        return unread;
    }
    public void setUnread(char unread) {
        this.unread = unread;
    }
    public int getFolders() {
        return folders;
    }
    public void setFolders(int folders) {
        this.folders = folders;
    }
    
    
}
