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

package net.unicon.academus.apps.messaging;

import java.util.Map;

import net.unicon.civis.ICivisFactory;
import net.unicon.mercury.IMessageFactory;
import net.unicon.warlock.IApplicationContext;

public class MessagingApplicationContext implements IApplicationContext {

    // Instance Members.
    private final String id;
    private final long uploadLimit;
    private final IMessageFactory systemEmail;
    private final ICivisFactory addressBook;
    private final Map groupRestrictors;
    private final int msgLimit;
    private final String peepholeType;
    private final Map callbacks;
    private final boolean xhtml;

    /*
     * Public API.
     */

    public MessagingApplicationContext(String id, IMessageFactory systemEmail,
                                 ICivisFactory addressBook, long uploadLimit,
                                 Map gRestrictors, String peephole,
                                 int msgLimit, Map callbacks, boolean xhtml)
    {
        // Assertions.
        if (id == null || "".equals(id))
            throw new IllegalArgumentException(
                    "Argument 'id' cannot be null or empty.");
        if (systemEmail == null)
            throw new IllegalArgumentException(
                    "Argument 'systemEmail' cannot be null.");
        if (addressBook == null)
            throw new IllegalArgumentException(
                    "Argument 'addressBook' cannot be null.");
        if (uploadLimit < 0)
            throw new IllegalArgumentException(
                    "Argument 'uploadLimit' cannot be negative.");
        if (callbacks == null)
            throw new IllegalArgumentException(
                    "Argument 'callbacks' cannot be null.");

        // Instance Members.
        this.id = id;
        this.systemEmail = systemEmail;
        this.addressBook = addressBook;
        this.uploadLimit = uploadLimit;
        this.groupRestrictors = gRestrictors;
        this.msgLimit = msgLimit;
        this.peepholeType = peephole;
        this.callbacks = callbacks;
        this.xhtml = xhtml;
    }

    /** Retrieve the application context id. */
    public String getId() { return this.id; }

    public long getUploadLimit() { return this.uploadLimit; }

    public boolean allowXHTML() { return this.xhtml; }

    public IMessageFactory getSystemEmail() { return this.systemEmail; }

    public ICivisFactory getAddressBook() { return this.addressBook; }
    
    public Map getGroupRestrictors() { return this.groupRestrictors; }
    
    public int getMsgLimit(){ return this.msgLimit; }
    
    public String getPeepholeType() { return this.peepholeType; }

    public String getCallbackLocation(String name) { return (String)callbacks.get(name); }

}
