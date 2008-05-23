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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import net.unicon.mercury.IAttachment;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.MercuryException;
import net.unicon.alchemist.EntityEncoder;

public abstract class BaseAbstractMessage extends AbstractMercuryEntity
                                      implements IMessage {

    private static final DateFormat formatter = new SimpleDateFormat();

    /*
     * Public API.
     */

    public BaseAbstractMessage(IMessageFactory owner) {
        super(owner);
    }

    /**
     * Serialize to an XML fragment.
     * @param rslt StringBuffer to append the fragment to.
     * @return A reference to the StringBuffer
     */
    public StringBuffer toXml(StringBuffer rslt) throws MercuryException {
        String buf;

        // Begin.
        rslt.append("<message ");

        // Message identifier.
        rslt.append("id=\"")
            .append(EntityEncoder.encodeEntities(getId()))
            .append("\"> ");

        // Received Date.
        rslt.append("<received>")
            .append(formatter.format(getDate()))
            .append(" </received>");

        // Read status.
        rslt.append("<status>")
            .append((isUnread() ? "unread" : "read"))
            .append("</status>");

        // Priority.
        rslt.append(getPriority().toXml());

        // Sender.
        rslt.append("<sender>");
        rslt.append(getSender().toXml());
        rslt.append("</sender>");

        // Recipients.
        IRecipient[] recipients = getRecipients();
        for (int i = 0; i < recipients.length; i++) {
            rslt.append(recipients[i].toXml());
        }
        
        // Subject
        rslt.append("<subject>")
            .append(EntityEncoder.encodeEntities(getSubject()))
            .append("</subject>");

        // Body.
        rslt.append("<body>")
            .append(EntityEncoder.encodeEntities(getBody()))
            .append("</body>");

        // Attachments.
        IAttachment[] attachments = getAttachments();
        rslt.append("<attachments total=\"")
        	.append(attachments.length)
        	.append("\"></attachments>");

        // Expiration
        rslt.append("<expires></expires>");
        
        // End.
        rslt.append("</message>");

        return rslt;

    }
}
