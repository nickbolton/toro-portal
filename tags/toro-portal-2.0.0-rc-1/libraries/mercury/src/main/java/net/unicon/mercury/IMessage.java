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

package net.unicon.mercury;

import java.util.Date;

public interface IMessage extends IMercuryEntity {

    /* EA: Utility of this identifier is implementation specific.  It only has
     * usable semantics in implementations that can globally address messages.
     */
    String getId() throws MercuryException;

    IAddress getSender() throws MercuryException;

    /**
     * Obtains the set of recipients targeted by this message.  This list always
     * contains the entries that are visible from the present context.  In the
     * case of a message sent from the owner message factory (i.e. a 'Sent
     * Item'), this list will always include all recipients.  In the case of a
     * message received by the owner message factory (i.e. an 'Inbox' item),
     * there may be actual recipients who do not appear on the list.  These
     * issues are the responsibility of the message factory implementations.
     */
    IRecipient[] getRecipients() throws MercuryException;

    /**
     * Obtains the recipients of the specified type(s).  Specify a zero-length
     * array to obtain all recipients.
     */
    IRecipient[] getRecipients(IRecipientType[] types) throws MercuryException;

    String getSubject() throws MercuryException;

    /**
     * Obtains the date and time when this message came into existence in this
     * context.
     */
    Date getDate() throws MercuryException;

    String getAbstract() throws MercuryException;

    String getBody() throws MercuryException;

    IAttachment[] getAttachments() throws MercuryException;

    /**
     * Obtain the priority of the message.
     * @see Priority
     */
    Priority getPriority() throws MercuryException;

    boolean isUnread() throws MercuryException;

    boolean isDeleted() throws MercuryException;
    
    /**
     * Set the read (seen) flag on a message.
     * @param seen Seen/read status
     * @throws MercuryException if the operation fails.
     */
    void setRead(boolean seen) throws MercuryException;

}
