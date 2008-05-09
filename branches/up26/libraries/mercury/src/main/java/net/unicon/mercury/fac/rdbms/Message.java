
package net.unicon.mercury.fac.rdbms;

import java.util.Date;


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
public class Message {

    private int id;
    private String sender;
    private String subject;
    private Date dateSent;
    private String body;
    private int priority;
    private Date dateExpires;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public Date getDateSent() {
        return dateSent;
    }
    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }
    public Date getDateExpires() {
        return dateExpires;
    }
    public void setDateExpires(Date dateExpires) {
        this.dateExpires = dateExpires;
    }
    
    
}
