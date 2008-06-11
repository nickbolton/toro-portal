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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;

import net.unicon.mercury.Features;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.XmlFormatException;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;

import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Composite preferences and account information for EmailMessageFactory.
 *
 * Requires both a Store and Transport account.
 *
 * @author eandresen
 */
public class EmailAccount {

    /* Penelope decision collection handling. */
    /** Maps IDecisionCollection handles to their handled class. */
    private static final Map storeTypeMap;
    /** Maps IDecisionCollection handles to their handled class. */
    private static final Map transportTypeMap;

    static {
        Map map = new HashMap();
        map.put("imapAccount", IMAPStoreAccount.class);
        map.put("pop3Account", POP3StoreAccount.class);
        storeTypeMap = Collections.unmodifiableMap(map);

        map = new HashMap();
        map.put("smtpAccount", SMTPTransportAccount.class);
        transportTypeMap = Collections.unmodifiableMap(map);
    }

    /*
     * Member instances.
     */

    private EmailStoreAccount store;
    private EmailTransportAccount transport;
    private Session session = null;
    private String bodyType = "text/plain";
    private IDecisionCollection[] prefs = null;
    private boolean valid;

    /*
     * Public API.
     */

    /**
     * Construct an EmailAccount from the composite of a Store and Transport
     * account.
     * @param store Account information for the message store.
     * @param transport Account information for the message transport.
     * @param dc User preferences.
     */
    public EmailAccount(EmailStoreAccount store,
                        EmailTransportAccount transport,
                        IDecisionCollection[] dc)
    {
        this(store, transport);
        this.prefs = dc;
    }

    /**
     * Construct an EmailAccount from the composite of a Store and Transport
     * account.
     * @param store Account information for the message store.
     * @param transport Account information for the message transport.
     */
    public EmailAccount(EmailStoreAccount store,
                        EmailTransportAccount transport)
    {
        this.valid = true;

        // Assertions.
        if (store == null)
            this.valid = false;
        else if (!store.getType().equals("store"))
            throw new IllegalArgumentException("Invalid account type");

        if (transport == null)
            this.valid = false;
        else if (!transport.getType().equals("transport"))
            throw new IllegalArgumentException("Invalid account type");

        this.store = store;
        this.transport = transport;
    }

    public boolean isValid() {
        return this.valid;
    }

    /**
     * Retrieve the user preferences.
     * @return User preferences.
     */
    public IDecisionCollection[] getPreferences() {
        return this.prefs;
    }

    /**
     * Set the message body content (MIME) type.
     * @param type Content (MIME) type
     */
    public void setBodyContentType(String type) {
        bodyType = type;
    }

    /**
     * Get the message body content (MIME) type.
     * @return Content (MIME) type
     */
    public String getBodyContentType() {
        return bodyType;
    }

    /**
     * Get the JavaMail Session associated with this account information.
     * The first call will establish a new JavaMail Session object. Subsequent
     * calls will return the same Session.
     * @return Session associated with this account information.
     */
    public Session getSession() {
        if (this.session == null)
            establishSession();
        return this.session;
    }

    /**
     * Get the JavaMailAccount representing this account's Store.
     */
    public JavaMailAccount getStoreAccount() {
        return this.store;
    }

    /**
     * Get the JavaMail Store associated with this account information.
     * @see #getSession()
     */
    public Store getStore() {
        Store rslt = null;

        try {
            rslt = getSession().getStore();
        } catch (NoSuchProviderException e) {
            rslt = null;
        }

        return rslt;
    }

    /**
     * Get the JavaMailAccount representing this account's Transport.
     */
    public JavaMailAccount getTransportAccount() {
        return this.transport;
    }

    /**
     * Get the JavaMail Transport associated with this account information.
     * @see #getSession()
     */
    public Transport getTransport() {
        Transport rslt = null;

        try {
            rslt = getSession().getTransport();
        } catch (NoSuchProviderException e) {
            rslt = null;
        }

        return rslt;
    }

    /**
     * Return the feature flags supported by this account.
     * @return feature flags supported by this account.
     * @see Features
     */
    public int getFeatures() {
        int rslt = 0;
        if (store != null)
            rslt = store.getFeatures();
        return rslt;
    }

    /**
     * Establish an EmailAccount based on an XML fragment.
     * @param e Root element of the XML fragment.
     * @return EmailAccount object representing the given XML fragment.
     * @throws MercuryException if any part of the parsing process fails.
     */
    public static EmailAccount parse(Element e) throws MercuryException
    {
        assert e != null;
        String tmp = null;
        EmailStoreAccount storeacct = null;
        EmailTransportAccount transportacct = null;

        Element el;
        Attribute t;

        try {
            // Store
            el = (Element)e.selectSingleNode("account[@type='store']");
            if (el != null) {
                t = el.attribute("impl");
                if (t == null)
                    throw new XmlFormatException(
                            "Element <account> must have an attribute 'impl'");
                storeacct = (EmailStoreAccount)Class.forName(t.getValue())
                                 .getConstructor(new Class[0])
                                 .newInstance(new Object[0]);

                storeacct.parse(el);
            }

            // Transport
            el = (Element)e.selectSingleNode("account[@type='transport']");
            if (el != null) {
                t = el.attribute("impl");
                if (t == null)
                    throw new XmlFormatException(
                            "Element <account> must have an attribute 'impl'");
                transportacct = (EmailTransportAccount)Class.forName(t.getValue())
                                 .getConstructor(new Class[0])
                                 .newInstance(new Object[0]);

                transportacct.parse(el);
            }
        } catch (Exception ex) {
            throw new MercuryException(
                        "Error instansiating EmailMessageFactory.", ex);
        }

        return new EmailAccount(storeacct, transportacct);
    }

    /**
     * Establish an EmailAccount based on a series of decisions made by the
     * user.
     * @param dc Collections of decisions as made by the user.
     * @return EmailAccount object representing the given decisions.
     * @throws MercuryException if any part of the parsing process fails.
     */
    public static EmailAccount parse(IDecisionCollection[] dc)
                                    throws MercuryException
    {
        assert dc != null;
        String tmp = null;
        Map dcMap = null;
        EmailStoreAccount storeacct = null;
        EmailTransportAccount transportacct = null;

        try {
            dcMap = mapDecisionCollections(dc);

            IDecisionCollection d =
                    (IDecisionCollection)dcMap.get("accountTypes");
            assert d != null;

            // Store

            IDecision decision = d.getDecision("storeType");
            assert decision != null;

            tmp = decision.getFirstSelectionHandle();
            storeacct = (EmailStoreAccount)
                                ((Class)storeTypeMap.get(tmp)).newInstance();
            assert storeacct != null;

            // Pass the decisions off to the account class.
            storeacct.parse((IDecisionCollection)dcMap.get(tmp));

            // Transport

            decision = d.getDecision("transportType");
            assert decision != null;

            tmp = decision.getFirstSelectionHandle();
            transportacct = (EmailTransportAccount)
                                ((Class)storeTypeMap.get(tmp)).newInstance();
            assert storeacct != null;

            // Pass the decisions off to the account class.
            transportacct.parse((IDecisionCollection)dcMap.get(tmp));

        } catch (Exception e) {
            throw new MercuryException(
                        "Error instansiating EmailMessageFactory.", e);
        }

        return new EmailAccount(storeacct, transportacct, dc);
    }

    /**
     * Compare the semantic equivilency of two objects.
     * @param obj Object to compare against
     * @return Boolean affirmation that the parameter is semantically
     * equivilent to this object.
     */
    public boolean equals(Object obj) {
        boolean rslt = false;

        if (obj instanceof EmailAccount) {
            EmailAccount a = (EmailAccount)obj;

            // Two EmailAccounts are equal if:
            //  Their Store and Transport account URLNames are equal
            rslt = this.getStoreAccount().getURLName()
                       .equals(a.getStoreAccount().getURLName())
                && this.getTransportAccount().getURLName()
                       .equals(a.getTransportAccount().getURLName());
        }

        return rslt;
    }

    /*
     * Implementation.
     */

    /**
     * Establish a JavaMail Session based on the current account information.
     */
    private void establishSession() {
        Properties p = new Properties();
        final HashMap protoMap = new HashMap();

        if (store != null) {
            p.putAll(store.getProperties());
            protoMap.put(store.getProtocol(), store.getAuthentication());
        }

        if (transport != null) {
            p.putAll(transport.getProperties());
            protoMap.put(transport.getProtocol(), transport.getAuthentication());
        }

        Authenticator a = new Authenticator() {
           protected PasswordAuthentication getPasswordAuthentication() {
               return (PasswordAuthentication)protoMap.get(getRequestingProtocol());
           }
        };

        p.put("body.contenttype", getBodyContentType());
        p.put("mail.debug", System.getProperty("mail.debug", "false"));

        this.session = Session.getInstance(p, a);
    }

    /**
     * Convienence method to map an IDecisionCollection array.
     * @return a mapping of <code>IChoiceCollection</code>
     *         <code>Handle</code>'s to <code>IDecisionCollection</code>s.
     */
    protected static Map mapDecisionCollections(IDecisionCollection[] dc) {
        Map dcMap = new HashMap();
        for (int i = 0; i < dc.length; i++) {
            dcMap.put(
                    dc[i].getChoiceCollection()
                         .getHandle()
                         .getValue(),
                    dc[i]);
        }
        return dcMap;
    }
}

