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
package net.unicon.academus.hibernate;

/**
 * Hibernate stub for schema creation.
 */
public class UpcNotificationStub {
    private int notificationId;
    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }
    private String recipientId;
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    private String appType;
    public String getAppType() { return appType; }
    public void setAppType(String appType) { this.appType = appType; }
    private String sender;
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    private java.util.Date sentDate;
    public java.util.Date getSentDate() { return sentDate; }
    public void setSentDate(java.util.Date sentDate) { this.sentDate = sentDate; }
    private String notiText;
    public String getNotiText() { return notiText; }
    public void setNotiText(String notiText) { this.notiText = notiText; }
    private String notified;
    public String getNotified() { return notified; }
    public void setNotified(String notified) { this.notified = notified; }
    private String params;
    public String getParams() { return params; }
    public void setParams(String params) { this.params = params; }
    private String status;
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

