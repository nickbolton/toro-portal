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
package net.unicon.portal.permissions;

public final class DirtyEvent {
    // scope identifiers. Only these should appear in a permissions.xml
    public final static String SCOPE_USER = "User";                  // all channels of specific User
    public final static String SCOPE_CHANNEL = "Channel";            // specific channel, all Users
    public final static String SCOPE_USER_CHANNEL = "UserChannel";   // specific User's specific Channel
    public final static String SCOPE_USERS_CHANNEL= "UsersChannel";  // User set by specific channel
    public final static String SCOPE_OFFERING_CHANNEL = "OfferingChannel"; // offering specific channel
    public final static String SCOPE_USER_OFFERING_CHANNEL = "UserOfferingChannel"; 
    // Offering's specific Channel *except* for this User

    DirtyEvent(String eventName, Boolean onOff, String eventScope, String[] targets) {
	this.__eventName  = eventName;
	this.__flag       = onOff;
	this.__eventScope = eventScope;
	this.__targets    = targets;
    }

    public String getEventName() {
	return __eventName;
    }
    public Boolean getFlag() {
	return __flag;
    }
    public String getEventScope() {
	return __eventScope;
    }
    public String[] getTargetChannelHandles() {
	return __targets;
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("DirtyEvent: ");
	sb.append(getEventName());
	sb.append(", ");
	sb.append(getEventScope());
	sb.append(", ");
	sb.append(getFlag());
	sb.append("\n\tTargets: ");
	for (int i = 0; i < __targets.length; i++) {
	    sb.append(__targets[i]);
	    sb.append(":");
	}
	return sb.toString();
    }

    private String   __eventName;
    private Boolean  __flag;
    private String   __eventScope;
    private String[] __targets;
}
