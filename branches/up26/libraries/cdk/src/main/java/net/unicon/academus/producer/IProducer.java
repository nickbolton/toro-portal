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
package net.unicon.academus.producer;

import net.unicon.portal.permissions.Activity;

import java.util.Map;
import java.util.Set;

public interface IProducer {
    /**
     * Passes back content through the ChannelData object.
     */
    public Map getContent(Map channelData) throws ContentProducerException;

    /**
     * Gets the type of producer
     */
    public ProducerType getType();

    /**
     * allocateAppConnection sets aside application resources (if needed) because a client
     * expects to use the app through this producer. It isn't actually required that the
     * application be instantiated, but a reference to it must be allocated.
     * @param <code>string[]</code> 0 is upID, 1 is username, 2 is context (if exists yet)
     **/
    public String allocateAppConnection(String[] initInfo) throws ContentProducerException;

    /**
     * Deallocate means to disconnect this client from this application via this
     * Producer, presumably because the client is being shutdown.
     * @param appId of the application to disconnect from
     **/
    public void deallocateAppConnection(String appId) throws ContentProducerException;

    /**
     * Return the set of connection applictions to this producer
     * @return Iterator application IDs
     **/
    public Set getConnectedApplicationIDs();

    /**
     * Gets an array of permissible <code>Activity</code> objects.
     */
    public Activity[] getActivities() throws ContentProducerException;

    // Constants used for common messages for ProducerData
    public static final String SERVANT_DONE   = "servantDone"; // value: String
    public static final String SERVANT_RESULTS = "servantResults"; // value: String
    public static final String ABORT_SERVANT = "abortServant"; // value: String

    // Constants used for common return values in the returned Map of getContent
    public static final String VIEW_NAME   = "view";         // value: String
    public static final String APP_ID      = "app_id";       // value: String  (see below)
    public static final String DIRTY_EVENT = "dirty_event";  // value: String
    public static final String STATUS      = "status";       // value: Integer (see below)
    public static final String STATUS_MSG  = "status_msg";   // value: String
    public static final String COMMAND     = "command";      // value: String
    public static final String DEFAULT_COMMAND = "default";  // value: String
    public static final String CONTEXT_ID  = "context_id";   // value: String
    public static final String MODE        = "mode";         // value: String

    // Status return object codes
    public static final Integer OK         = new Integer(0);
    public static final Integer EXCEPTION  = new Integer(1);
    public static final Integer NEW_APP    = new Integer(2); // lifecycle notification, new app_id
    public static final Integer APP_DESTROYED = new Integer(3);  // lifecycle, app_id gone
    // Only one of the following should come back in the payload
    public static final String XML         = "xml";          // value: String (XML)
    public static final String XSL_PARAMS  = "xslParams";    // value: String (XSL_PARAMS)
    public static final String DOCUMENT    = "dom";          // value: org.w3c.dom.Document
    public static final String HTML        = "html";         // value: String (HTML)
    public static final String WML         = "wml";          // value: String (WML)
    public static final String UNKNOWN     = "unknown";      // value: Object
}
