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

package net.unicon.mercury.fac.email;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import net.unicon.mercury.fac.AddressImpl;
import net.unicon.mercury.fac.BaseAbstractMessage;
import net.unicon.mercury.IAddress;
import net.unicon.mercury.IAttachment;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.IRecipientType;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.Priority;

/**
 * Message class which bridges the JavaMail type 'Message' to the Mercury type
 * 'IMessage'.
 *
 * It is important to distinguish between the 'IMessage' and 'Message' types;
 * the former is provided by Mercury, and the latter by JavaMail. They are in
 * no way compatible and have greatly differing semantics.
 *
 * @author eandresen
 */
public class EmailMessage extends BaseAbstractMessage
{
    // Instance members.
    private final EmailFolder folder;
    private final Message message;

    // Caching
    private String id = null;
    private IAddress sender = null;
    private IRecipient[] recipients = null;
    private String subject = null;
    private Date date = null;
    private Priority priority = null;
    private boolean contentDone = false;
    private EmailAttachment body;
    private List attachments;

    /**
     * Constructor.
     *
     * @param owner Containing MessageFactory
     * @param message JavaMail Message object; cannot be Expunged.
     *
     * @throws JavaMailException if the message is already expunged.
     */
    public EmailMessage(IMessageFactory owner, EmailFolder folder,
                        Message message) throws MercuryException {
        super(owner);

        if (message == null)
            throw new IllegalArgumentException(
                    "Argument 'message' cannot be null.");
        if (message.isExpunged())
            throw new JavaMailException(
                    "Message was previously expunged. Illegal operation.");

        this.folder = folder;
        this.message = message;
    }

    /**
     * Construct a new EmailMessage from the contents of another IMessage.
     *
     * @param owner Containing EmailMessageFactory
     * @param msg Mercury IMessage object
     *
     * @throws MercuryException if the operation in unsuccessful
     */
    public EmailMessage(IMessageFactory owner, EmailFolder folder,
                        IMessage msg) throws MercuryException
    {
        super(owner);

        if (!(owner instanceof EmailMessageFactory))
            throw new IllegalArgumentException(
                    "Argument 'owner' must be of type EmailMessageFactory.");
        if (msg == null)
            throw new IllegalArgumentException(
                    "Argument 'msg' cannot be null.");

        Message jmMsg = new MimeMessage(((EmailMessageFactory)owner)
                                            .getAccount()
                                            .getSession());

        try {
            jmMsg.setFrom(new InternetAddress(msg.getSender().toNativeFormat()));
            jmMsg.setSubject(msg.getSubject());
            jmMsg.setSentDate(msg.getDate());

            Map recipientTypes = ((EmailMessageFactory)owner).getRecipientTypeMap();
            IRecipient[] recips = msg.getRecipients();
            for (int i = 0; i < recips.length; i++) {
                Message.RecipientType type =
                        (Message.RecipientType)recipientTypes
                                .get(recips[i].getType());
                if (type == null) // Default to TO
                    type = Message.RecipientType.TO;

                jmMsg.addRecipients(
                            type,
                            new Address[] {
                                new InternetAddress(recips[i].getAddress()
                                                         .toNativeFormat()) }
                        );
            }

            String body = msg.getBody();
            IAttachment[] attachments = msg.getAttachments();

            if (attachments.length == 0) {
                // Simple case: Text only
                jmMsg.setText(body);
            } else {
                // The not-so-easy way.
                Multipart mp = new MimeMultipart();
                BodyPart mbp = new MimeBodyPart();

                // Handle the message body.
                mbp.setText(body);
                mp.addBodyPart(mbp);

                for (int i = 0; i < attachments.length; i++) {
                    mbp = new MimeBodyPart();
                    mbp.setDataHandler(new DataHandler(new EmailAttachment(i+1, attachments[i])));
                    mbp.setFileName(attachments[i].getName());
                    mp.addBodyPart(mbp);
                }

                jmMsg.setContent(mp);
            }
        } catch (MessagingException me) {
            throw new MercuryException(
                    "Error cloning IMessage", me);
        }

        this.folder = folder;
        this.message = jmMsg;
    }

    /**
     * Retrieve the message identifier. This is only guaranteed to be unique
     * within the containing folder.
     *
     * @return Message identifier; it is possible for this to be null for
     *         messages that do not belong to a folder (new or imported
     *         message).
     * @throws JavaMailException if JavaMail has an internal error.
     */
    public String getId() throws MercuryException {
        if (folder == null)
            throw new MercuryException("Non-identifiable message");

        if (this.id == null) {
            StringBuffer rslt = new StringBuffer();
            String[] tmp = null;

            // Include Folder identification
            rslt.append(folder.getFullName())
                .append(":::")
                .append(message.getMessageNumber());

            try {
                // Use Message-Id header if possible
                tmp = message.getHeader("Message-Id");
            } catch (MessagingException me) {
                // Occurs only on error, not on "not found"
                throw new JavaMailException(
                        "Failed to retrieve Message-Id header",
                        me);
            }

            if (tmp != null && tmp.length > 0) {
                rslt.append(":::")
                    .append(tmp[0]);
            }
            this.id = rslt.toString();
        }

        return this.id;
    }

    /**
     * Get the Message's sender.
     * @return Address representing the Message's sender, or null if none could be determined.
     * @throws JavaMailException if getFrom() fails on the underlying Message.
     */
    public IAddress getSender() throws MercuryException {
        if (this.sender == null) {
            try {
                Address[] addrs = message.getFrom();
                if (addrs != null && addrs.length >= 1) {
                    this.sender = new AddressImpl(addrs[0].toString(), addrs[0].toString());;
                }
            } catch (MessagingException me) {
                throw new JavaMailException(
                        "Failed to determine From address",
                        me);
            }
        }

        return this.sender;
    }

    /**
     * Obtain all recipients of all types.
     * @return Recipients of the all types.
     * @throws JavaMailException if the underlying Message throws an error
     * @see #getRecipients(IRecipientType[])
     */
    public IRecipient[] getRecipients() throws MercuryException {
        return getRecipients(new IRecipientType[0]);
    }

    /**
     * Obtains the recipients of the specified type(s).  Specify a zero-length
     * array to obtain all recipients.
     * @return Recipients of the requested types.
     * @throws IllegalArgumentException if types specifies invalid IRecipientTypes
     * @throws JavaMailException if the underlying Message throws an error
     */
    public IRecipient[] getRecipients(IRecipientType[] types)
            throws MercuryException
    {
        if (types == null)
            throw new IllegalArgumentException(
                    "Argument 'types' cannot be null.");

        IRecipient[] ret = null;

        if (this.recipients == null) {
            // JavaMail Message allows us to either get all recipients, or
            // get recipients of a single type. We need to know the type
            // information, so we must do this the long way and enumerate
            // the allowable types, and request the list of each type.
            List rslt = new ArrayList();
            IRecipientType[] mercRecipTypes = getOwner().getRecipientTypes();
            Map recipTypeMap = ((EmailMessageFactory)getOwner())
                .getRecipientTypeMap();
            Message.RecipientType[] recipientTypes =
                (Message.RecipientType[])
                recipTypeMap.values()
                .toArray(new Message.RecipientType[0]);

            try {
                for (int i = 0; i < recipientTypes.length; i++) {
                    Address[] recips =
                        message.getRecipients(recipientTypes[i]);
                    if (recips != null) {
                        for (int j = 0; j < recips.length; j++) {
                            rslt.add(
                                    new EmailRecipient(
                                        new AddressImpl(recips[j].toString(),
                                            recips[j].toString()),
                                        mercRecipTypes[i])
                                    );
                        }
                    }
                }
            } catch (MessagingException me) {
                throw new JavaMailException(
                        "Failed to determine message recipients",
                        me);
            }

            this.recipients = (IRecipient[])rslt.toArray(new IRecipient[0]);
        }

        if (types.length == 0) {
            ret = this.recipients;
        } else {
            List rslt = new ArrayList();
            // Find the matching id of the IRecipientType and match it to
            // our recipientTypes array.
            IRecipient[] recips = this.recipients;
            String tmp = null;
            for (int i = 0; i < recips.length; i++) {
                tmp = recips[i].getType().getLabel();
                for (int j = 0; j < types.length; j++) {
                    if (tmp.equalsIgnoreCase(types[j].getLabel()))
                        rslt.add(recips[i]);
                }
            }
            ret = (IRecipient[])rslt.toArray(new IRecipient[0]);
        }

        return ret;
    }

    /**
     * Retrieve the Subject of the Message.
     *
     * @return Message's subject.
     * @throws JavaMailException if JavaMail has an internal error
     */
    public String getSubject()
            throws MercuryException
    {
        if (this.subject == null) {
            try {
                this.subject = message.getSubject();
            } catch (MessagingException me) {
                throw new JavaMailException(
                        "Failed to determine message subject",
                        me);
            }
        }

        return this.subject;
    }

    /**
     * Retrieve the Sent Date of the Message.
     *
     * @return Original sent date of the message.
     * @throws JavaMailException if JavaMail has an internal error
     */
    public Date getDate() throws MercuryException {
        if (this.date == null) {
            try {
                this.date = message.getSentDate();
            } catch (MessagingException me) {
                throw new JavaMailException(
                        "Failed to determine message sent date",
                        me);
            }
        }

        return this.date;
    }

    /**
     * Retrieve an Abstract of the message body.
     * This is no more efficient than simply retrieving the entire body.
     *
     * @return first 50 characters of the message body.
     * @throws JavaMailException if JavaMail has an internal error
     * @see #getBody()
     */
    public String getAbstract() throws MercuryException {
        // XXX: Provide the first sentence/line from the content?
        // -- Not any cheaper to do, but meets the requirements.
        String rslt = getBody();
        if (rslt != null && rslt.length() > 50)
            rslt = rslt.substring(0, 50);

        return rslt;
    }

    /**
      * Retrieve the Message body.
      * This will return only the primary text (or HTML) portion of the
      * message. The {@link #getAttachments()} method can be used to retrieve
      * other message parts.
      *
      * @return message body
      * @throws JavaMailException if JavaMail has an internal error
      * @see #getAttachments()
      */
    public String getBody() throws MercuryException {
        /*
         * TODO: This should probably return an IAttachment instead, to associate
         * it with a Mimetype so we know how to format it (html versus plain text)
         */
        if (!contentDone)
            handleContent();
        // There may not be a body (attachments only, if that).
        return (body == null ? "" : body.getText());
    }

    /**
     * Retrieve the attachments associated with the Message.
     *
     * @return Array of attachments to this message; an empty array if none
     * @throws JavaMailException if JavaMail has an internal error
     */
    public IAttachment[] getAttachments()
                                    throws MercuryException {
        if (!contentDone)
            handleContent();
        return (IAttachment[])attachments.toArray(new IAttachment[0]);
    }

    /**
     * @see IMessage#getPriority()
     */
    public Priority getPriority() throws MercuryException {
        if (this.priority == null) {
            Priority rslt = Priority.PRIORITY_MEDIUM;
            String tmp[] = null;

            /*
             * X-Priority header
             * 1 = Highest
             * 3 = Normal
             * 5 = Lowest
             */
            try {
                tmp = message.getHeader("X-Priority");
            } catch (MessagingException me) {
                throw new MercuryException(
                        "Error while retriving X-Priority header", me);
            }

            if (tmp != null && tmp.length > 0) {
                int pri = -1;
                Pattern p = Pattern.compile("(\\d+).*");
                Matcher m = p.matcher(tmp[0]);
                if (m.matches()) {
                    pri = Integer.parseInt(m.group(1));
                }
                if (pri > 3)
                    rslt = Priority.PRIORITY_LOW;
                else if (pri > 0 && pri < 3)
                    rslt = Priority.PRIORITY_HIGH;
                else if (pri == 3)
                    rslt = Priority.PRIORITY_MEDIUM;
            }

            // ... implement other priority headers? (X-MSMail-Priority, Importance)
            
            this.priority = rslt;
        }

        return this.priority;
    }

    /**
     * Affirmation of the Message's unread status.
     * @return affirmation of unread status.
     * @throws JavaMailException if JavaMail has an internal error
     */
    public boolean isUnread() throws MercuryException {
        boolean rslt = false;

        try {
            rslt = !message.isSet(Flags.Flag.SEEN);
        } catch (MessagingException me) {
            throw new JavaMailException(
                        "Unable to check message flags",
                        me);
        } 

        return rslt;
    }

    /**
     * Affirmation of the Message's deleted status.
     * @return affirmation of deleted status.
     * @throws JavaMailException if JavaMail has an internal error
     */
    public boolean isDeleted() throws MercuryException {
        boolean rslt = false;

        try {
            rslt = message.isSet(Flags.Flag.DELETED);
        } catch (MessagingException me) {
            throw new JavaMailException(
                        "Unable to check message flags",
                        me);
        } 

        return rslt;
    }

    /*
     * Parse an XML fragment containing an embeded RFC822 Message.
     *
     * @param fact EmailMessageFactory containing the Session and Store
     *             information.
     * @param e Element object representing the <code>&lt;message&gt;</code>
     *          element.
     * @return EmailMessage representing the embedded RFC822 message
    public static IMessage parse(IMessageFactory fact, Element e)
                                 throws MercuryException {
        // Assertions.
        assert fact != null : "Argument 'fact' cannot be null.";
        assert e != null : "Argument 'e [Element]' cannot be null.";
        if (!(fact instanceof EmailMessageFactory))
            throw new IllegalArgumentException(
                    "Argument 'fact' must be of type EmailMessageFactory");
        if (!e.getName().equals("message"))
            throw new XmlFormatException(
                    "Argument 'e [Element]' must be a <message> element.");

        Message msg = null;
        String buf = null;
        ByteArrayInputStream bufInputStream = null;
        EmailMessage rslt = null;
        EmailMessageFactory efact = (EmailMessageFactory) fact;
        Attribute t = null;
        List list = null;

        try {
            t = e.attribute("impl");
            if (t == null || !t.getValue().equals(EmailMessage.class.getName()))
                throw new XmlFormatException(
                        "Argument 'e [Element]' has an invalid 'class' attribute.");

            // RFC822 Message.
            list = e.elements("rfc822");
            if (list.size() != 1)
                throw new XmlFormatException(
                        "Exactly one <rfc822> element required per message.");
            buf = ((Element)list.get(0)).getText();
            bufInputStream = new ByteArrayInputStream(buf.getBytes());

            msg = new MimeMessage(efact.getAccount().getSession(), bufInputStream);
            rslt = new EmailMessage(efact, null, msg);
        } catch (MessagingException me) {
            throw new JavaMailException(
                    "Failed to parse embedded RFC822 Message", me);
        }

        return rslt;
    }
    */

    /*
     * Serialize to an XML fragment.
     * @param rslt StringBuffer to append the fragment to.
     * @return A reference to the StringBuffer
    public StringBuffer toXml(StringBuffer rslt) throws MercuryException {
        String buf;

        // Begin.
        rslt.append("<message ");

//        // Implementing class.
//        rslt.append("impl=\"")
//            .append(this.getClass().getName())
//            .append("\" ");

        // Message identifier.
        rslt.append("id=\"")
            .append(EntityEncoder.encodeEntities(getId()))
            .append("\" ");

        // Date.
        rslt.append("date=\"")
            .append(String.valueOf(getDate().getTime()))
            .append("\" ");

        // Read status.
        rslt.append("read=\"")
            .append((isUnread() ? "false" : "true"))
            .append("\">");

        // Sender.
        rslt.append("<sender>");
        ((AbstractMercuryEntity) getSender()).toXml(rslt);
        rslt.append("</sender>");

        // Recipients.
        IRecipient[] recipients = getRecipients();
        for (int i = 0; i < recipients.length; i++) {
            ((AbstractMercuryEntity) recipients[i]).toXml(rslt);
        }
        
        // Subject
        rslt.append("<subject>")
            .append(EntityEncoder.encodeEntities(getSubject()))
            .append("</subject>");

        // Body.
        rslt.append("<body>")
            .append(EntityEncoder.encodeEntities(getBody()))
            .append("</body>");

        // TODO: Attachments.

//        // RFC822 message.
//        rslt.append("<rfc822>");
//        rslt.append("<![CDATA[");
//
//        ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
//        try {
//            message.writeTo(bufStream);
//        } catch (MessagingException me) {
//            throw new JavaMailException(
//                    "Failed to write message output stream.", me);
//        } catch (IOException ioe) {
//            throw new MercuryException(
//                    "Failed to write message output stream.", ioe);
//        }
//        rslt.append(bufStream.toString());
//
//        rslt.append("]]>")
//            .append("</rfc822>");

        // End.
        rslt.append("</message>");

        return rslt;
    }
     */
 
    /**
     * Compare the semantic equivilency of two objects.
     * @param obj Object to compare against
     * @return Boolean affirmation that the parameter is semantically
     * equivilent to this object.
     */
    public boolean equals(Object obj) {
        boolean rslt = false;

        if (obj instanceof EmailMessage) {
            EmailMessage e = (EmailMessage)obj;

            // Two EmailMessages are equal to each other if:
            //  They belong to the same folder.
            //  They have the same Message number
            try {
                String myId = this.getId();
                String theirId = e.getId();
                rslt = (e.folder != null && this.folder != null
                        && this.folder.equals(e.folder)
                        && myId != null && myId.equals(theirId));
            } catch (MercuryException ex) {
                rslt = false;
            }
        }

        return rslt;
    }
    
    /**
     * Set the JavaMail message's seen flag.
     * @param read true if read, false if unread
     * @throws JavaMailException if unable to set Message flags
     */
    public synchronized void setRead(boolean read) throws MercuryException {
        if (this.folder == null)
            throw new IllegalStateException(
                    "Unparented message cannot be set as read.");

        // Must be open in Read-Write mode to update SEEN status.
        this.folder.ensureOpen(Folder.READ_WRITE);

        try {
            message.setFlag(Flags.Flag.SEEN, read);
        } catch (MessagingException me) {
            throw new JavaMailException(
                        "Unable to set message seen flag", me);
        } 
    }

    /*
     * Protected API.
     */

    /**
     * Set the JavaMail message's delete flag.
     * This does not immediately delete the message, but flags it for deletion
     * on the next expunge operation.
     *
     * @throws JavaMailException if unable to set Message flags
     */
    protected synchronized void setDeleted() throws MercuryException {
        try {
            message.setFlag(Flags.Flag.DELETED, true);
        } catch (MessagingException me) {
            throw new JavaMailException(
                        "Unable to set message flags",
                        me);
        } 
    }

        /**
     * Retrieve the underlying JavaMail message.
     * @return Associated JavaMail message.
     */
    protected Message getMessage() {
        return message;
    }

    /*
     * Implementation.
     */

    /**
     * Compare the Message-Id header of this Message with the given argument.
     * @param msgId Message-Id value to check against.
     * @return true if the Message-Id header of this message matches the
     *         parameter.
     * @throws JavaMailException if the Message-Id header could not be
     *         retrieved.
     */
    boolean checkMessageId(String msgId) throws MercuryException {
        assert msgId != null;
        assert !"".equals(msgId);

        boolean rslt = false;
        String[] tmp = null;

        try {
            // Use Message-Id header if possible
            tmp = message.getHeader("Message-Id");
        } catch (MessagingException me) {
            // Occurs only on error, not on "not found"
            throw new JavaMailException(
                        "Failed to retrieve Message-Id header",
                        me);
        }

        if (tmp != null && tmp.length > 0) {
            rslt = msgId.equals(tmp[0]);
        }

        return rslt;
    }

    /**
     * Parse the content from the JavaMail message.
     *
     * Entry point into the recursive call.
     * @throws JavaMailException on any internal JavaMail error
     */
    private synchronized void handleContent() throws MercuryException {
        // Handle the race condition.
        if (contentDone)
            return;

        this.body = null;
        this.attachments = new LinkedList();

        handleContent(message);

        contentDone = true;
    }

    /**
     * Parse the content from the JavaMail message.
     * Recursively calls itself to handle multipart content.
     *
     * @param part current message Part context
     * @throws JavaMailException on any internal JavaMail error
     */
    private synchronized void handleContent(Part part)
                                    throws MercuryException {
        try {
            String type = part.getContentType();
            // Strip off all but the Mime-Type
            int o = type.indexOf(';');
            if (o > 0)
                type = type.substring(0, o);
            type = type.toLowerCase();

            if (type.startsWith("multipart/")) {
                // Begin the recursive call.
                Multipart mp = (Multipart)part.getContent();

                for (int i=0, n=mp.getCount(); i < n; i++) {
                    Part mpp = mp.getBodyPart(i);

                    handleContent(mpp);
                }
            } else {
                if (part.getFileName() == null) {
                    // Assume body content when no filename.
                    if (body == null &&
                            (type.equals("text/html")
                          || type.equals("text/plain"))) {
                        // Initial body content.
                        EmailAttachment ea =
                            new EmailAttachment(0, part.getFileName(),
                                    type, part.getInputStream());
                        body = ea;
                    } else if (body != null && type.equals("text/html")
                            && body.getContentType().equals("text/plain")) {
                        // Replace the text/plain body for the text/html version.

                        // Don't bother to keep the text/plain.
                        //attachments.add(body);

                        EmailAttachment ea =
                            new EmailAttachment(0, part.getFileName(),
                                    type, part.getInputStream());

                        body = ea;
                    }
                    // Ignore all others... XXX: Probably should be logged.
                } else {
                    // We have an attachment. Add it to the list.
                    EmailAttachment ea =
                        new EmailAttachment(attachments.size()+1, part.getFileName(),
                                type, part.getInputStream());
                    attachments.add(ea);
                }
            }
        } catch (IOException ioe) {
            throw new JavaMailException(
                        "Failed to retrieve message body",
                        ioe);
        } catch (MessagingException me) {
            throw new JavaMailException(
                        "Failed to retrieve message body",
                        me);
        }
    }
}
