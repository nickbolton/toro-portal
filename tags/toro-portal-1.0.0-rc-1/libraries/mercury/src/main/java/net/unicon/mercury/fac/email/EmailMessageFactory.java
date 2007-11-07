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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.search.BodyTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import javax.mail.search.SubjectTerm;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;

import net.unicon.mercury.fac.AbstractMessageFactory;
import net.unicon.mercury.fac.AddressImpl;
import net.unicon.mercury.Features;
import net.unicon.mercury.FolderNotFoundException;
import net.unicon.mercury.IAttachment;
import net.unicon.mercury.IFolder;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.IRecipientType;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.MercurySendFailedException;
import net.unicon.mercury.Priority;
import net.unicon.mercury.SpecialFolder;
import net.unicon.penelope.complement.TypeDate;
import net.unicon.penelope.complement.TypeText64;
import net.unicon.penelope.EntityCreateException;
import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IOption;
import net.unicon.penelope.Label;
import net.unicon.penelope.store.jvm.JvmEntityStore;

import org.dom4j.Element;

/**
 * Email-based Message Factory.
 *
 * This class is essentially a bridge between Mercury and JavaMail.
 *
 * It is important to distinguish between the 'IFolder' and 'Folder' types; the
 * former is provided by Mercury, and the latter by JavaMail. They are in no
 * way compatible and have greatly differing semantics.
 *
 * This is also true of the 'IMessage' and 'Message' types, as well as
 * 'IAddress' and 'Address'.
 *
 * @author eandresen
 * @version 2005-02-15
 */
public class EmailMessageFactory extends AbstractMessageFactory
{
    /*
     * Member instances.
     */

    /** Features supported by this instance. */
    private Features features;
    /** JavaMail session information. */
    private Session session;
    /** JavaMail handler for message retrieval and organization. */
    private Store store;
    /** JavaMail handler for message sending. */
    private Transport transport;
    /** EmailAccount settings for the current instance. */
    private EmailAccount acct;
    /** Affirmation of Store connection status. */
    private boolean connected;
    /** Folder caching. Allows minimal connection management to occur. */
    private Map folderCache = Collections.synchronizedMap(new HashMap());

    /*
     * Static declarations.
     */

    /** X-Mailer header value for sent messages. */
    private static final String MERCURY_MAILER = "Unicon Mercury (JavaMail)";

    /** Allowable search criteria for this component. */
    private static final IChoiceCollection searchCriteria = initSearchCriteria();

//    /** The From: address to use for sendEmail() */
//    private static String systemFrom = "unknown@example.com";
//    /** The Session to use for sendEmail() */
//    private static Session defaultTransport = null;

    /** Mapping of Mercury IRecipientTypes to JavaMail Message.RecipientTypes.
     * @see #getRecipientTypeMap()
     */
    private static final Map recipientTypes;

    static {
        Map rMap = new HashMap();
        rMap.put(EmailRecipientType.TO, Message.RecipientType.TO);
        rMap.put(EmailRecipientType.CC, Message.RecipientType.CC);
        rMap.put(EmailRecipientType.BCC, Message.RecipientType.BCC);
        recipientTypes = Collections.unmodifiableMap(rMap);
    };

    /** Mapping of Choice handles to JavaMail search term classes. */
    private static final Map termMap;

    static {
        Map tMap = new HashMap();
        try {
            tMap.put("bodyContains", BodyTerm.class.getConstructor(new Class[] { String.class }));
            tMap.put("senderContains", FromStringTerm.class.getConstructor(new Class[] { String.class }));
            tMap.put("recipientsContains", AllRecipientStringTerm.class.getConstructor(new Class[] { String.class }));
            tMap.put("subjectContains", SubjectTerm.class.getConstructor(new Class[] { String.class }));
            tMap.put("afterDate", SentAfterDateTerm.class.getConstructor(new Class[] { Date.class }));
            tMap.put("beforeDate", SentBeforeDateTerm.class.getConstructor(new Class[] { Date.class }));
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to initialize the EmailMessageFactory SearchTerm mapping");
        }
        termMap = Collections.unmodifiableMap(tMap);
    }

    /*
     * Public API.
     */

    /*
     * Static method that allows a message to be sent via the default
     * transport.
     * @see #parse(Element)
    public static void sendEmail(
                                IRecipient[] recipients, String subject,
                                String body, IAttachment[] attachments,
                                Priority priority) throws MercuryException {
        // _sendMessage handles all assertions.
        Session session = defaultTransport;
        if (session == null)
            throw new IllegalStateException(
                    "No default transport exists.");

        try {
            Transport transport = session.getTransport();
            if (transport == null)
                throw new IllegalStateException(
                        "No default transport exists.");

            _sendMessage(session, transport, systemFrom, recipients,
                    subject, body, "text/plain", attachments, priority);
        } catch (MessagingException me) {
            throw new MercuryException(
                    "Unable to send message via default transport", me);
        }
    }
     */

    /**
     * Instansiate an EmailMessageFactory instance from an XML fragment.
     * @param e Root element of the XML fragment.
     * @return a new EmailMessageFactory instance
     * @throws MercuryException if any part of the parsing fails
     */
    public static EmailMessageFactory parse(Element e) throws MercuryException {
        return new EmailMessageFactory(e);
    }

    /*
     * Initialize global (static) variables from an XML fragment.  The call to
     * parse() is optional, as sane defaults should be provided.
     * <p>In order to use the static 'sendEmail' method, parse() must be called.</p>
     *
     * @param e Element containing the base &lt;message-factory&gt; element for
     *          this implementation.
     * @throws RuntimeException if the parsing fails.
    public static void parse(Element e) throws MercuryException {
        assert e != null : "Argument 'e' cannot be null";

        if (!e.getName().equals("message-factory"))
            throw new IllegalArgumentException(
                    "Argument 'e [Element]' must be a <message-factory> element.");

        Element e2 = (Element)e.selectSingleNode("default-smtp/smtp-account");
        if (e2 != null) {
            SMTPTransportAccount trans = new SMTPTransportAccount();
            trans.parse(e2);
            EmailAccount ea = new EmailAccount(null, trans);
            defaultTransport = ea.getSession();
        }

        e2 = (Element)e.selectSingleNode("system-from");
        if (e2 != null) {
            systemFrom = e2.getText();
        }
    }
     */
    
    /**
     * Construct an EmailMessageFactory using the given decision collection.
     * @param dc Decision collection with initial state/preference information.
     */
    public EmailMessageFactory(IDecisionCollection[] dc) throws MercuryException
    {
        this(EmailAccount.parse(dc));
    }

    /**
     * Construct an EmailMessageFactory from an XML fragment.
     * @param e Root element of the XML fragment.
     */
    public EmailMessageFactory(Element e) throws MercuryException
    {
        this(EmailAccount.parse(e));
    }

    /**
     * Construct an EmailMessageFactory using a given EmailAccount.
     * @param acct Email account information
     */
    public EmailMessageFactory(EmailAccount acct)
    {
        if (acct == null)
            throw new IllegalArgumentException("Argument 'acct' cannot be null");

        this.acct = acct;
        this.session = acct.getSession();
        this.store = acct.getStore();
        this.transport = acct.getTransport();
        this.connected = false;
        this.features = new Features(this, acct.getFeatures());

        if (session == null) {
            throw new IllegalStateException("The provided EmailAccount has "
                                          + "not been fully initialized.");
        }
    }

    /**
     * @see IMessageFactory#getFeatures()
     */
    public Features getFeatures() {
        return this.features;
    }

    /**
     * @see IMessageFactory#getUrl()
     */
    public String getUrl() {
        // ToDo: XXX: This isn't helpful.
        return (new StringBuffer("MSG://"))
                    .append(this.getClass().getName())
                    .append("/")
                    .toString();
    }

    /**
     * @see IMessageFactory#getPreferences()
     */
    public IDecisionCollection[] getPreferences() {
        return acct.getPreferences();
    }

    /**
     * Return a mapping of IRecipientTypes to JavaMail recipient types.
     *
     * <p>This method is not part of the IMessageFactory interface.</p>
     * 
     * @return a <code>Map</code> containing <code>IRecipientType</code> keys
     *         that correspond to their JavaMail
     *         <code>Message.RecipientType</code> complement.
     */
    public Map getRecipientTypeMap() {
        return recipientTypes;
    }

    /**
     * @see IMessageFactory#getRecipientTypes()
     */
    public IRecipientType[] getRecipientTypes() {
        return (IRecipientType[])recipientTypes
                                    .keySet()
                                    .toArray(new IRecipientType[0]);
    }

    /**
     * @see IMessageFactory#getRoot()
     */
    public IFolder getRoot() throws MercuryException {
        if (this.store == null)
            throw new IllegalStateException(
                    "No Store has been configured for this MessageFactory.");
        return getFolder(this.getPreference("default.folder", "INBOX"));
    }

    /**
     * @see IMessageFactory#getFolder(String)
     */
    public IFolder getFolder(String id) throws FolderNotFoundException, MercuryException
    {
        if (this.store == null)
            throw new IllegalStateException(
                    "No Store has been configured for this MessageFactory.");

        EmailFolder rslt = (EmailFolder)folderCache.get(id);

        if (rslt == null) {
            try {
                synchronized(folderCache) {
                    Folder f = store.getFolder(id);
                    if (f != null && !f.getFullName().equals("")) {
                        // EmailFolder ctor checks folder existance.
                        rslt = new EmailFolder(this, f);
                        folderCache.put(rslt.getFullName(), rslt);
                    }
                }
            } catch (MessagingException me) {
                throw new JavaMailException("Failed to acquire folder from store: "+id, me);
            }
        }

        return rslt;
    }

    /**
     * Connect to the message store.
     *
     * <p>This method MUST be called prior to using any message or folder
     * retrieval methods on this class. If preference "sent.store" is true,
     * this includes sending of messages.</p>
     *
     * <p>Calling this more than once is an no-op.</p>
     *
     * @see #disconnect()
     */
    public synchronized void connect() throws MercuryException {
        if (this.store == null) return;
        try {
            // EA: Don't use store.isConnected() as that will open a NEW
            // connection to the server if all current connections are busy.
            if (!connected) {
                store.connect();
                connected = true;
            }
        } catch (MessagingException me) {
            throw new MercuryException("Connection error", me);
        }
    }

    /**
     * Disconnect from the message store.
     *
     * <p>This method MUST be called before losing scope of the factory. Call
     * this method from a finally block.</p>
     *
     * <p>Calling this more than once is an no-op.</p>
     */
    public synchronized void disconnect() throws MercuryException {
        try {
            // EA: Don't use store.isConnected() as that will open a NEW
            // connection to the server if all current connections are busy.
            if (store != null && connected)
                store.close();
        } catch (MessagingException me) {
            throw new MercuryException("Connection error", me);
        } finally {
            connected = false;
        }
    }

    /**
     * Create an attachment to be used within the Email Mercury component.
     * <p>This method is not present in IMessageFactory.</p>
     *
     * @param filename Filename (without path information)
     * @param type MIME Type of the attachment
     * @param contents Byte array containing the file's contents.
     * @return IAttachment representing the given data.
     * @see #createAttachment(String,String,InputStream)
     * @see IAttachment
     */
    public IAttachment createAttachment(String filename,
                                        String type,
                                        byte[] contents)
                                    throws MercuryException {
        return new EmailAttachment(-1, filename, type, contents);
    }

    /**
     * @see IMessageFactory#createAttachment(String,String,InputStream)
     */
    public IAttachment createAttachment(String filename,
                                        String type,
                                        InputStream stream)
                                    throws MercuryException {
        return new EmailAttachment(-1, filename, type, stream);
    }

    /**
     * Send a message.
     *
     * <p>Send a message to the recipients secified by the recipients
     * parameter. The "From:" address on the message will correspond to the
     * Session's "mail.from" property.</p>
     *
     * <p>The content type of the message body is determined by the preference
     * "body.contenttype".</p>
     *
     * <p>If the preference "sent.store" is set to 'true', the sent message
     * will be stored into the folder named by the preference "sent.folder". If
     * the named folder does not exist, it will be created. If the current
     * protocol or server does not support folders, it will not be stored.</p>
     *
     * @throws MercuryException if any part of the process fails
     * @see IMessageFactory#sendMessage(IRecipient[],String,String,IAttachment[],Priority)
     */
    public synchronized IMessage sendMessage(
                                IRecipient[] recipients, String subject,
                                String body, IAttachment[] attachments,
                                Priority priority) throws MercuryException
    {
        if (this.transport == null)
            throw new IllegalStateException(
                    "No Transport has been configured for this MessageFactory.");

        EmailMessage rslt = null;
        String fromAddr = this.getPreference("mail.from");
        String bodyType = this.getPreference("body.contenttype", "text/plain");

        // Try to be smart about choosing the MIME type.
        if (body.startsWith("<html>"))
            bodyType = "text/html";

        // initialize the MessageQueue
        EmailMessageQueue queue = new EmailMessageQueue(this, session, transport, fromAddr, recipients,
                subject, body, bodyType, attachments, priority);
        Thread t = new Thread(queue, "EmailMessageQueue");
        t.start();
        
        return rslt;
    }

    /**
     * Resolve a preference name into its setting.
     * @param optname Name of the preference to resolve
     * @return preference associated with the given name, or null if unset.
     */
    public String getPreference(String optname)
    {
        // TODO: Review.
        return getPreference(optname, null);
    }

    /**
     * Resolve a preference name into its setting.
     * @param optname Name of the preference to resolve
     * @param def Default value to used if the preference is unset
     * @return preference associated with the given name, or <code>def</code>
     *         if unset
     */
    public String getPreference(String optname, String def)
    {
        String rslt = session.getProperty(optname);
        if (rslt == null)
            rslt = def;
        return rslt;
    }

    /**
     * @see IMessageFactory#getSearchCriteria()
     **/
    public IChoiceCollection getSearchCriteria() {
        return searchCriteria;
    }

    /**
     * @see IMessageFactory#getMessage(String)
     */
    public IMessage getMessage(String id) throws MercuryException {
        if (this.store == null)
            throw new IllegalStateException(
                    "No Store has been configured for this MessageFactory.");
        assert id != null;

        if (id.equals("")) {
            throw new IllegalArgumentException("Argument 'id' cannot be empty.");
        }

        String tmp[] = id.split(":::", 3);

        if (tmp.length < 2) {
            throw new IllegalArgumentException(
                    "Argument 'id' is not a valid EmailMessageFactory "
                  + "message identifier: "+id);
        }

        return getFolder(tmp[0]).getMessage(id);
    }

    /**
     * @see IMessageFactory#search(IFolder[], IDecisionCollection)
     */
    public IMessage[] search(IFolder[] folders, IDecisionCollection filters)
                             throws MercuryException
    {
        if (this.store == null)
            throw new IllegalStateException(
                    "No Store has been configured for this MessageFactory.");
        assert folders != null : "Argument 'folders' cannot be null.";
        assert filters != null : "Argument 'filters' cannot be null.";

        Set rslt = new HashSet();
        SearchTerm filter = convertFilters(filters);

        for (int i = 0; i < folders.length; i++) {
            if (folders[i] == null)
                throw new IllegalArgumentException(
                        "Argument 'folders' cannot contain null values.");
            rslt.addAll(
                ((EmailFolder)folders[i]).search(filter, false));
        }

        return (IMessage[])rslt.toArray(new IMessage[0]);
    }

    /**
     * Retrieve the EmailAccount being used.
     * @return EmailAccount in used by this object
     */
    public EmailAccount getAccount() {
        return this.acct;
    }

    /**
     * Compare the semantic equivilency of two objects.
     * @param obj Object to compare against
     * @return Boolean affirmation that the parameter is semantically
     * equivilent to this object.
     */
    public boolean equals(Object obj) {
        boolean rslt = false;

        if (obj instanceof EmailMessageFactory) {
            EmailMessageFactory e = (EmailMessageFactory)obj;

            // Two EmailMessageFactorys are equal if:
            //  Their accounts are the same.
            rslt = this.getAccount().equals(e.getAccount());
        }

        return rslt;
    }

    /* (non-Javadoc)
     * @see net.unicon.mercury.IMessageFactory#getSpecialFolder(java.lang.String)
     */
    public IFolder getSpecialFolder(SpecialFolder sFolder) throws MercuryException {
        IFolder rslt = null;

        if (sFolder == null)
            throw new IllegalArgumentException(
                    "Argument 'sFolder' cannot be null.");
        
        if (SpecialFolder.INBOX_VALUE == sFolder.toLong()) {
            rslt = getRoot();
        } else if (SpecialFolder.OUTBOX_VALUE == sFolder.toLong()) {
            rslt = getSentFolder();
        }

        return rslt;
    }
    
    public void move(IMessage msg, IFolder srcFolder, IFolder destFolder)
                                                throws MercuryException {
        if (this.store == null)
            throw new IllegalStateException(
                    "No Store has been configured for this MessageFactory.");

        if (!srcFolder.equals(destFolder)) {
            destFolder.addMessage(msg);
            srcFolder.removeMessage(msg);
            srcFolder.expunge();
        }
    }

    /**
     * @see IMessageFactory#cleanup()
     */
    public void cleanup() {
        try {
            disconnect();
        } catch (Exception e) {}
    }

    /*
     * Protected API.
     */

    /**
     * Convert the penelope decision collection into JavaMail SearchTerm
     * objects.
     * @param filters Decision collection to convert.
     * @return SearchTerm object corresponding to the decision collection.
     */
    protected SearchTerm convertFilters(IDecisionCollection filters)
                                throws MercuryException
    {
        assert filters != null : "Argument 'filters' cannot be null.";

        List terms = new LinkedList();
        IDecision[] decisions = filters.getDecisions();
        for (int i = 0; i < decisions.length; i++) {
            String handle = decisions[i].getChoice().getHandle().getValue();
            Object term = decisions[i].getFirstSelectionValue();
            Constructor tcons = (Constructor)termMap.get(handle);

            if (tcons == null)
                throw new IllegalArgumentException(
                        "Invalid choice used in decision collection: "+handle);
            if (term != null && (
                        !(term instanceof String)
                     || !"".equals(((String)term).trim())
                     )) {
                try {
                    terms.add(tcons.newInstance(new Object[] { term }));
                } catch (IllegalArgumentException e) {
                    throw new MercuryException(
                            "Unexpected complement type for choice: " + handle,
                            e);
                } catch (Exception e) {
                    // Shouldn't happen unless the mapping is incorrect.
                    throw new MercuryException(
                            "Internal error in convertFilters()", e);
                }
            } // else continue
        }

        return new OrTerm((SearchTerm[])terms.toArray(new SearchTerm[0]));
    }

    /**
     * Provide a last ditch attempt to free any used resources.
     *
     * <p>Do <b>NOT</b> depend on this method to be called. Always explicitly
     * call {@link #disconnect()} in a finally block!</p>
     *
     * @see #disconnect()
     */
    protected void finalize() {
        try {
            disconnect();
        } catch (Exception e) {}
    }

    /*
     * Implementation.
     */

    /**
     * Create a folder using the provided JavaMail Folder.
     *
     * @param fold JavaMail Folder that does not exist.
     * @return Created folder, or null if unsuccessful.
     * @throws MercuryException if EmailFolder throws an exception
     * @throws UnsupportedOperationException if subfolders are not supported
     */
    EmailFolder createFolder(Folder fold) throws MercuryException
    {
        if (this.store == null)
            throw new IllegalStateException(
                    "No Store has been configured for this MessageFactory.");

        if (!this.getFeatures().allowSubfolders())
            throw new UnsupportedOperationException(
                        "Subfolders are not supported");

        EmailFolder rslt = null;

        try {
            if (fold.create(Folder.HOLDS_FOLDERS|Folder.HOLDS_MESSAGES)) {
                rslt = new EmailFolder(this, fold);
            }
        } catch (MessagingException ex) {
            // EA: null return has to suffice, as there's no way to filter
            // between actual errors and per-protocol exceptions to indicate
            // unsupported folder types.
        }

        return rslt;
    }

    /**
     * SearchTerm that will match any type of Recipient.
     */
    private static class AllRecipientStringTerm extends SearchTerm {
        private SearchTerm term;
        public AllRecipientStringTerm(String pattern) {
            Message.RecipientType[] types = 
                (Message.RecipientType[])recipientTypes.values()
                    .toArray(new Message.RecipientType[0]);

            SearchTerm[] terms = new SearchTerm[types.length];
            for (int i = 0; i < types.length; i++) {
                terms[i] = new RecipientStringTerm(types[i], pattern);
            }

            this.term = new OrTerm(terms);
        }

        public boolean match(Message msg) {
            return this.term.match(msg);
        }
    }

    /**
     * Wraps a SentDateTerm to allow a simplified constructor.
     */
    private static class SentBeforeDateTerm extends SearchTerm {
        private SentDateTerm term;
        public SentBeforeDateTerm(Date date) {
            this.term = new SentDateTerm(ComparisonTerm.LE, date);
        }

        public boolean match(Message msg) {
            return this.term.match(msg);
        }
    }

    /**
     * Wraps a SentDateTerm to allow a simplified constructor.
     */
    private static class SentAfterDateTerm extends SearchTerm {
        private SentDateTerm term;
        public SentAfterDateTerm(Date date) {
            this.term = new SentDateTerm(ComparisonTerm.GE, date);
        }

        public boolean match(Message msg) {
            return this.term.match(msg);
        }
    }

    /**
     * Initialize the Search Criteria penelope choice collection for this
     * component.
     */
    private static IChoiceCollection initSearchCriteria() {
        IEntityStore store = new JvmEntityStore();
        IChoiceCollection rslt = null;

        // Create the choice collection for the search criteria
        try {

            // Sender
            IOption osender =
                store.createOption(Handle.create("OsenderContains"), null,
                                                 TypeText64.INSTANCE);

            IChoice csender =
                store.createChoice(Handle.create("senderContains"), 
                                                 Label.create("Sender:"),
                                                 new IOption[] { osender },
                                                 0, 1);

            // Recipients
            IOption orecipients =
                store.createOption(Handle.create("OrecipientsContains"), null,
                                               TypeText64.INSTANCE);

            IChoice crecipients =
                store.createChoice(Handle.create("recipientsContains"), 
                                               Label.create("Recipients:"),
                                               new IOption[] { orecipients },
                                               0, 1);

            // Subject
            IOption osubject =
                store.createOption(Handle.create("OsubjectContains"), null,
                                               TypeText64.INSTANCE);

            IChoice csubject =
                store.createChoice(Handle.create("subjectContains"), 
                                               Label.create("Subject:"),
                                               new IOption[] { osubject },
                                               0, 1);

            // Body
            IOption obody =
                store.createOption(Handle.create("ObodyContains"), null,
                                               TypeText64.INSTANCE);

            IChoice cbody =
                store.createChoice(Handle.create("bodyContains"), 
                                               Label.create("body:"),
                                               new IOption[] { obody },
                                               0, 1);

            // After Date
            IOption oafterDate =
                store.createOption(Handle.create("OafterDateContains"), null,
                                               TypeDate.INSTANCE);

            IChoice cafterDate =
                store.createChoice(Handle.create("afterDateContains"), 
                                               Label.create("After Date:"),
                                               new IOption[] { oafterDate },
                                               0, 1);

            // Before Date
            IOption obeforeDate =
                store.createOption(Handle.create("ObeforeDateContains"), null,
                                               TypeDate.INSTANCE);

            IChoice cbeforeDate =
                store.createChoice(Handle.create("beforeDateContains"), 
                                               Label.create("Before Date:"),
                                               new IOption[] { obeforeDate },
                                               0, 1);

            // Search Criteria Choice Collection...
            rslt = store.createChoiceCollection(
                            Handle.create("searchCriteria"),
                            Label.create("Search Criteria"),
                            new IChoice[] {
                                csender, crecipients, csubject,
                                cbody, cbeforeDate, cafterDate
                            });
        } catch (EntityCreateException ex) {
            throw new RuntimeException("Failed to initialize search criteria", ex);
        }

        return rslt;
    }

    private EmailFolder getSentFolder() throws MercuryException {
        if (this.store == null)
            throw new IllegalStateException(
                    "No Store has been configured for this MessageFactory.");

        EmailFolder rslt = null;
        if (this.getFeatures().allowSubfolders() &&
            "true".equalsIgnoreCase(this.getPreference("sent.store", "true"))) {

            String foldName = this.getPreference("sent.folder", "INBOX.Sent");

            try {
                try {
                    rslt = (EmailFolder)this.getFolder(foldName);
                } catch (FolderNotFoundException ex) {
                    // Folder didn't exist. Create it.
                    rslt = createFolder(store.getFolder(foldName));
                }
            } catch (MessagingException ex) {
                throw new MercuryException("Failed to acquire Sent folder", ex);
            }
        }

        return rslt;
    }

    private static Message _sendMessage(
                                Session session, Transport transport,
                                String fromAddr, IRecipient[] recipients,
                                String subject, String body, String bodyType,
                                IAttachment[] attachments, Priority priority)
                                throws MercuryException, MercurySendFailedException
    {
        if (session == null)
            throw new IllegalArgumentException(
                    "Argument 'session' cannot be null.");
        if (transport == null)
            throw new IllegalArgumentException(
                    "Argument 'transport' cannot be null.");
        if (recipients == null)
            throw new IllegalArgumentException(
                    "Argument 'recipients' cannot be null.");
        if (recipients.length <= 0)
            throw new IllegalArgumentException("No recipients specified.");
        for (int i = 0; i < recipients.length; i++)
            if (recipients[i] == null)
                throw new IllegalArgumentException(
                        "Argument 'recipients' cannot contain null values.");
        if (subject == null)
            throw new IllegalArgumentException(
                    "Argument 'subject' cannot be null.");
        if (body == null)
            throw new IllegalArgumentException(
                    "Argument 'body' cannot be null.");
        if (fromAddr == null || "".equals(fromAddr))
            throw new IllegalStateException("Invalid From Address.");
        // attachments can be null.
        if (priority == null)
            throw new IllegalArgumentException(
                    "Argument 'priority' cannot be null.");

        Message msg = null;
        boolean success = false;
        List invalidAddrList = new ArrayList();

        try {
            msg = new MimeMessage(session);

            msg.setSentDate(new Date());
            msg.setFrom(new InternetAddress(fromAddr));
            msg.addHeader("X-Mailer", MERCURY_MAILER);
            msg.setSubject(subject);

            if (priority == Priority.PRIORITY_HIGH) {
                msg.addHeader("X-Priority", "1"); // Highest
            } else if (priority == Priority.PRIORITY_LOW) {
                msg.addHeader("X-Priority", "5"); // Lowest
            } // No header for Medium/Normal

            if (attachments == null || attachments.length == 0) {
                // The easy way.
                msg.setContent(body, bodyType);
            } else {
                // The not-so-easy way.
                Multipart mp = new MimeMultipart();
                BodyPart mbp = new MimeBodyPart();

                // Handle the message body.
                mbp.setContent(body, bodyType);
                mp.addBodyPart(mbp);

                for (int i = 0; i < attachments.length; i++) {
                    mbp = new MimeBodyPart();
                    if (attachments[i] instanceof EmailAttachment)
                        mbp.setDataHandler(new DataHandler(
                                    (EmailAttachment)attachments[i]));
                    else
                        mbp.setDataHandler(new DataHandler(
                                    new EmailAttachment(-1, attachments[i])));
                    mbp.setFileName(attachments[i].getName());
                    mp.addBodyPart(mbp);
                }

                msg.setContent(mp);
            }
            
            // Group recipients by type
            HashMap recipMap = new HashMap();
            for (int i = 0; i < recipients.length; i++) {
                IRecipient r = recipients[i];
                IRecipientType rt = r.getType();
                if (!(rt instanceof EmailRecipientType)) {
                    rt = EmailRecipientType.getType(rt.getLabel());
                }

                ArrayList rtRecips = (ArrayList)recipMap.get(rt);
                if (rtRecips == null) {
                    rtRecips = new ArrayList();
                    recipMap.put(rt, rtRecips);
                }

                InternetAddress addr = null;
                if (r.getAddress().toNativeFormat().equals(r.getAddress().getLabel()))
                    addr = new InternetAddress(
                                r.getAddress().toNativeFormat());
                else
                    addr = new InternetAddress(
                                r.getAddress().toNativeFormat(),
                                r.getAddress().getLabel());

                rtRecips.add(addr);
            }

            Address[] invalids = new Address[0];
            Address[] validSent = new Address[0];

            while(!success){
                try{
		            
		            // Add recipients.
		            Iterator it = recipMap.entrySet().iterator();
		            while (it.hasNext()) {
		                Map.Entry et = (Map.Entry)it.next();
		                IRecipientType rt = (IRecipientType)et.getKey();
		                ArrayList rtRecips = (ArrayList)et.getValue();
		
		                // remove the invalid addresses
		                for(int i = 0; invalids != null && i < invalids.length; i++){
		                    rtRecips.remove(invalids[i]);
		                }
		                
		                // remove the valid sent addresses
		                for(int i = 0; validSent != null && i < validSent.length; i++){
		                    rtRecips.remove(validSent[i]);
		                }
		                Address[] recips = (Address[])rtRecips.toArray(new Address[0]);
		
		                msg.setRecipients(
		                        (Message.RecipientType) recipientTypes.get(rt),
		                        recips);
		            }

		            msg.saveChanges();
		            
		            invalids = new Address[0];
		            validSent = new Address[0];
		            success = true;
		            
		            transport.send(msg, msg.getAllRecipients());

		            // Sent messages have been "seen".
		            msg.setFlag(Flags.Flag.SEEN, true);
		            
		        } catch (SendFailedException e) {
		            // add the invalid addresses to the list
		            invalids = e.getInvalidAddresses();
		            if(invalids != null){
		                invalidAddrList.addAll(Arrays.asList(invalids));
		            }
		            
		            // check if any valid addresses were not sent the email
		            if(e.getValidUnsentAddresses() != null && e.getValidUnsentAddresses().length > 0){
		                success = false;
		                validSent = e.getValidSentAddresses();
		            }
		        }
            }
            if(!invalidAddrList.isEmpty()){
                StringBuffer buf = new StringBuffer();
	            buf.append("Unable to send message; send failed");
	            buf.append("; Invalid addresses: ");
	            for (int i = 0; i < invalidAddrList.size(); i++) {
	                buf.append((Address)invalidAddrList.get(i)).append("; ");
	            }	                        
	            throw new MercurySendFailedException(buf.toString(), new SendFailedException(), msg);
            }
        } catch (AddressException e) {
            throw new MercuryException("Invalid address specified.", e);
        } catch (UnsupportedEncodingException e) {
            throw new MercuryException("Invalid address label encoding specified.", e);
        } catch (MessagingException e) {
            throw new MercuryException("Failed to establish message.", e);
        }

        return msg;
    }
    
    protected class EmailMessageQueue implements Runnable{
        
        private IMessageFactory fac;
        private Session session;
        private Transport transport;
        private String fromAddr;
        private IRecipient[] recipients;
        private String subject;
        private String body; 
        private String bodyType;
        private IAttachment[] attachments;
        private Priority priority;
        private Message message;
        
        public EmailMessageQueue(IMessageFactory fac, Session session, Transport transport,
                String fromAddr, IRecipient[] recipients,
                String subject, String body, String bodyType,
                IAttachment[] attachments, Priority priority){
            
            this.fac = fac;
            this.session = session;
            this.transport = transport;
            this.fromAddr = fromAddr;
            this.recipients = recipients;
            this.subject = subject;
            this.body = body;
            this.bodyType = bodyType;
            this.attachments = attachments;
            this.priority = priority;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            try{
	            message = _sendMessage(this.session, this.transport, this.fromAddr
	                    , this.recipients, this.subject, this.body, this.bodyType
	                    , this.attachments, this.priority);
	            	            
            }catch (MercurySendFailedException e) {
                // send a message on invalid email addresses to the sender
                try{
                    message = e.getEmailMessage();
                    
	                _sendMessage(this.session, this.transport, this.fromAddr
		                    , new IRecipient[] {new EmailRecipient(
		                            new AddressImpl(this.fromAddr, this.fromAddr)
		                            , EmailRecipientType.TO)
		                            }
	                		, this.subject + " - Invalid Email Addresses."
		                    , e.getMessage(), this.bodyType
		                    , new IAttachment[0], Priority.PRIORITY_LOW);
                }catch(Exception me){
                    me.printStackTrace();
                }
            }catch (MercuryException e){
                e.printStackTrace();
            }
            
            // add the message to the sent folder of the sender
            if (message == null)
                throw new IllegalStateException("msg is null on return from _sendMessage()");
            IMessage rslt;
            try {
                rslt = new EmailMessage(fac, null, message);
            
            if (store != null) {
                /* Store sent items.
                 * XXX: Won't work for for POP3 or flat IMAP servers. This is too
                 * low-level to ask users about, so we have to go about it the long
                 * way.
                 */
                EmailFolder fold = getSentFolder();

                // Store the message, if we found the folder.
                if (fold != null) {
                    fold.addMessage(rslt);
                }
            }
            
            } catch (MercuryException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }            
        }     
        
    }

}

