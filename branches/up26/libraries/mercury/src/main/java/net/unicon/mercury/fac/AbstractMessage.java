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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.unicon.mercury.IAddress;
import net.unicon.mercury.IAttachment;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.IRecipientType;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.Priority;

// TODO: Once things such as attachments come into play, loading the entire
// message at once may become less desirable. Headers such as dates,
// recipients and subject should be loaded on lookup, but others such as
// body and attachments should only be loaded on demand.
// TODO Need an expiration date on the messages
public abstract class AbstractMessage extends BaseAbstractMessage
                                      implements IMessage {

    /*
     * Class members
     */
    // Assuming 80 character window for abstract,
    // 5 lines is 400 characters.
    protected static final int ABSTRACT_LENGTH = 400;
    
    /*
     * Instance members
     */
    protected final String id;
    protected final IAddress sender;
    protected final IRecipient[] recipients;
    protected final String subject;
    protected Date date;
    protected final Priority priority;
    protected final String body;
    protected final IAttachment[] attachments;
    protected boolean read;
    protected boolean deleted;

    /*
     * Public API.
     */

    public AbstractMessage(IMessageFactory owner,
                       String id,
                       IAddress sender,
                       IRecipient[] recipients,
                       String subject,
                       Date date,
                       String body,
                       IAttachment[] attachments,
                       Priority priority,
                       boolean read) {
        super(owner);

        // Assertions.
        if (id == null) {
            String msg = "Argument 'id' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (sender == null) {
            String msg = "Argument 'sender' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (recipients == null) {
            String msg = "Argument 'recipients' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        for (int i = 0; i < recipients.length; i++) {
            if (recipients[i] == null) {
                String msg = "Argument 'recipients' contains an "
                                + "illegal null entry.";
                throw new IllegalArgumentException(msg);
            }
        }
        if (subject == null) {
            String msg = "Argument 'subject' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (date == null) {
            String msg = "Argument 'date' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (body == null) {
            String msg = "Argument 'body' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // Attachments can be null.
        if (priority == null) {
            String msg = "Argument 'priority' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.id = id;
        this.sender = sender;
        this.recipients = recipients;
        this.subject = subject;
        this.date= date;
        this.body = body;
        this.attachments = attachments;
        this.priority = priority;
        this.read = read;
    }

    /**
     * Obtain all recipients.
     */
    public IRecipient[] getRecipients() throws MercuryException {
        return getRecipients(new IRecipientType[0]);
    }

    /**
     * Obtains the recipients of the specified type(s).  Specify a
     * zero-length array to obtain all recipients.
     */
    public IRecipient[] getRecipients(IRecipientType[] types) 
                                         throws MercuryException {
        IRecipient[] rslt = null;

        if (types != null && types.length > 0) {
            List resultlist = new ArrayList();

            // For each recipient, check its type against the requested
            // types; if requested, add it to the result set.
            for (int i = 0; i < recipients.length; i++) {
                for (int j = 0; j < types.length; j++) {
                    if (recipients[i].getType().equals(types[j]))
                        resultlist.add(recipients[i]);
                }
            }

            rslt = (IRecipient[])resultlist.toArray();
        } else {
            // All recipients requested
            rslt = recipients;
        }

        return rslt;
    }

    public String getId() throws MercuryException {
        return id;
    }

    public IAddress getSender() throws MercuryException {
        return this.sender;
    }

    public String getSubject() throws MercuryException {
        return subject;
    }

    public Date getDate() throws MercuryException {
        return date;
    }

    public String getBody() throws MercuryException {
        return body;
    }

    public Priority getPriority() throws MercuryException {
        return priority;
    }
    
    /**
     * Returns all attachments to this message.
     * 
     * @return an array of <code>IAttachment</code> objects.
     */
    public IAttachment[] getAttachments() throws MercuryException {
        return this.attachments;
    }

    public boolean isUnread() throws MercuryException {
        return !read;
    }

    public boolean isDeleted() throws MercuryException {
        return deleted;
    }
    
    /**
     * Returns an abstract of the complete message body. 
     * 
     * @return 
     *     a <code>String</code> containing the first xx number of characters 
     *     from the message body. If the message body length is greater than xx, 
     *     then the first xx - 3 characters are returned with ellipses appended.
     *     Otherwise, the entire message body is returned with no ellipses 
     *     appended. 
     */
    public String getAbstract() throws MercuryException {
        String rslt = null;
        
        if (this.body.length() < ABSTRACT_LENGTH) {
            rslt = this.body;
        } else { // Message is too large to be shown fully
            StringBuffer abs = new StringBuffer();
            // Make room for ellipses as last three characters
            abs.append(this.body.substring(0, ABSTRACT_LENGTH - 3));
            abs.append("...");
            rslt = abs.toString();
        }

        return rslt;
    }

}
