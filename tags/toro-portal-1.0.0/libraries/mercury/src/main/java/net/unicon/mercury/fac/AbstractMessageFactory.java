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

package net.unicon.mercury.fac;

import java.io.InputStream;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.unicon.mercury.DraftMessage;
import net.unicon.mercury.IAddress;
import net.unicon.mercury.IAttachment;
import net.unicon.mercury.IFolder;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.Priority;
import net.unicon.penelope.IDecisionCollection;

public abstract class AbstractMessageFactory implements IMessageFactory {

    /*
     * Public API.
     */
    //TODO verify with Andrew about the caching
    protected static Map factoryMap = new HashMap(); 

    /**
     * @see IMessageFactory#search(IFolder[], IDecisionCollection)
     */
    public IMessage[] search(IFolder[] folders, IDecisionCollection filters)
                                                 throws MercuryException {

        // Assertions
        assert folders != null : "Argument 'folders' cannot be null.";
        assert filters != null : "Argument 'filters' cannot be null.";

        List rslt = new LinkedList();

        // Search each folder
        for (int i = 0; i < folders.length; i++) {
            if (folders[i] == null)
                throw new IllegalArgumentException(
                        "Argument 'folders' cannot contain null values.");
            rslt.addAll(Arrays.asList(folders[i].search(filters, false)));

        }

        return (IMessage[])rslt.toArray(new IMessage[0]);
    }

   /* public IAddress createAddress(String label, String nativeFormat) {
        return new AddressImpl(this, label, nativeFormat);
    }*/

    /**
     * @see IMessageFactory#createAttachment(String,String,InputStream)
     * @see IAttachment
     */
    public IAttachment createAttachment(String filename,
                                        String type,
                                        InputStream stream)
                                    throws MercuryException {
        return new AttachmentImpl(-1, filename, type, stream);
    }

    /**
     * Send a message with no attachments.
     * <p>The default implementation calls
     * {@link IMessageFactory#sendMessage(IRecipient[],String,String,IAttachment[],Priority)}
     * with a null for the attachments parameter.</p>
     * @see IMessageFactory#sendMessage(IRecipient[],String,String,Priority)
     */
    public final IMessage sendMessage(IRecipient[] recipients, String subject,
                                String body, Priority priority)
                                throws MercuryException
    {
        return sendMessage(recipients, subject, body, null, priority);
    }

    public final IMessage sendMessage(DraftMessage draft) throws MercuryException {
        return sendMessage(draft.getRecipients(), draft.getSubject(),
                           draft.getBody(), draft.getAttachments(),
                           draft.getPriority());
    }

    public boolean equals(Object obj) {
        boolean rslt = false;

        if (obj instanceof IMessageFactory) {
            IMessageFactory m = (IMessageFactory)obj;
            rslt = getUrl().equals(m.getUrl());
        }

        return rslt;
    }

    public void cleanup() {}
}
