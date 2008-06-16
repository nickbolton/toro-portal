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

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.jit.ICreator;
import net.unicon.civis.ICivisFactory;
import net.unicon.mercury.fac.ge.GroupExpandMessageFactory;
import net.unicon.mercury.fac.gt.GroupTriggerMessageFactory;
import net.unicon.mercury.fac.rdbms.RdbmsMessageFactory;
import net.unicon.mercury.DraftMessage;
import net.unicon.mercury.Features;
import net.unicon.mercury.FolderNotFoundException;
import net.unicon.mercury.IAttachment;
import net.unicon.mercury.IFolder;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.IRecipientType;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.Priority;
import net.unicon.mercury.SpecialFolder;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecisionCollection;

import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Creates new instances of the RdbmsMessageFactory from JitAccessBroker.  The
 * parse method is used to create an instance of the class.
 * @author eandresen
 */
public class RdbmsMessageFactoryCreator implements ICreator {

    // Instance Members.
    private String attachPath; 
    private int layers;
    private int messageExpiration;
    private String configName;
    private boolean active;
    private ICivisFactory cFac;

    // Static members.
    private static DataSource ds;
    private static Map instances = Collections.synchronizedMap(new HashMap());

    /**
     * Returns an instance of the class by parsing the <code>Element</code>.
     * @param e XML <code>Element</code>
     * @return an instance of the class
     */
    public static ICreator parse(Element e) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("creator")) {
            String msg = "Argument 'e [Element]' must be a <creator> "
                                                            + "element.";
            throw new IllegalArgumentException(msg);
        }

        Attribute t = e.attribute("handle");
        if (t == null)
            throw new IllegalArgumentException(
                        "Element <creator> must have an attribute 'handle'.");
        String configName = t.getValue();
        
        t = e.attribute("group-trigger");
        if (t == null)
            throw new IllegalArgumentException(
                        "Element <creator> must have an attribute 'group-trigger'.");
        boolean active = Boolean.valueOf(t.getValue()).booleanValue();

        // get the root attachment folder
        Element el = (Element)e.selectSingleNode("attachment-path");
        if (el == null)
            throw new IllegalArgumentException(
                        "Element <creator> needs to have a " +
                        "required element <attachment-path>.");

        String attachPath = el.getText();
        if (attachPath.equals(""))
            throw new IllegalArgumentException(
                    "Element <attachment-path> must have a valid file "
                  + "system path.");

        // parse the numbers of folder hashing layers from the XML config
        el = (Element)e.selectSingleNode("hashing-layers");
        int layers = 3;
        if(el != null) {
            layers = Integer.parseInt(el.getText());
        }
        
        int expiration = 0;
        // parse the numbers of folder hashing layers from the XML config
        el = (Element)e.selectSingleNode("message-expiration");
        if(el != null) {
            expiration = Integer.parseInt(el.getText());
        }
        
        // Civis factory for email lookups (Send copy via email) and group breakups
        ICivisFactory addressBook = null;
        el = (Element)e.selectSingleNode("civis[@id='addressBook']");
        if (el != null) {
            Attribute impl = el.attribute("impl");
            if (impl == null)
                throw new IllegalArgumentException(
                        "Element <civis> must have an 'impl'"
                      + " attribute.");
            try {
                addressBook =
                    (ICivisFactory)Class.forName(impl.getValue())
                     .getDeclaredMethod("parse",
                             new Class[] { Element.class })
                     .invoke(null, new Object[] { el });
            } catch (Exception ex) {
               // Allowed to fail in the case of BatchImporter.
            }
        }

        return new RdbmsMessageFactoryCreator(configName, attachPath, layers, expiration, active, addressBook);
    }

    /*
     * Implementation.
     */

    private RdbmsMessageFactoryCreator(String configName, String attachPath,
                                       int layers, int messageExpiration
                                       , boolean active, ICivisFactory cFac) {
        this.attachPath = attachPath;
        this.layers     = layers;
        this.messageExpiration = messageExpiration;
        this.configName = configName;
        this.active = active;
        this.cFac = cFac;

        instances.put(configName, this);
    }

    private static RdbmsMessageFactoryCreator getInstance(String configName) {
        return (RdbmsMessageFactoryCreator)instances.get(configName);
    }

    public String getAttachmentPath() { return this.attachPath; }
    public int getAttachmentLayers() { return this.layers; }
    public int getExpiration() { return this.messageExpiration; }
    public DataSource getDataSource() { return ds; }
    public String getHandle() { return this.configName; }
    public ICivisFactory getCivisFactory() { return this.cFac; }
    public boolean isGroupTriggerActive() { return this.active; }

    /* (non-Javadoc)
     * @see net.unicon.academus.access.jit.ICreator#create()
     */
    public Object create(Identity id) {
        RdbmsMessageFactory msgFac = null;

        if (isGroupTriggerActive()) {
            msgFac = new GroupTriggerMessageFactory(ds, attachPath, layers
                    , id.getId(), messageExpiration, cFac);
        } else {
            msgFac = new GroupExpandMessageFactory(ds, attachPath, layers
                    , id.getId(), messageExpiration, cFac);
        }
        return new CreatorRdbmsMessageFactory(id.getId(), configName
                , msgFac);
    }
 
    public static void bootstrap(String queryFile) {
       // Delegate bootstrap
       GroupTriggerMessageFactory.bootstrap(queryFile);
       RdbmsMessageFactory.bootstrap(queryFile);
    }    
    
    public static void bootstrap(DataSource dsrc) {
        // Hold onto a copy.
        ds = dsrc;

        // Delegate bootstrap
        RdbmsMessageFactory.bootstrap(ds);
    }
    
    /**
     * Wrapper class for RdbmsMessageFactory that overrides the to and from URL handling.
     */
    public static class CreatorRdbmsMessageFactory implements IMessageFactory {
        private final String url;
        private IMessageFactory msgFac;

        public CreatorRdbmsMessageFactory(
                                String domainOwner, 
                                String configName
                                , IMessageFactory fac) {
            
            this.msgFac = fac;

            StringBuffer buf = new StringBuffer();
            buf.append("MSG://")
               .append(this.getClass().getName())
               .append("/")
               .append(configName)
               .append("/")
               .append(domainOwner);
            this.url = buf.toString();
        }

        public String getUrl() {
            return this.url;
        }

        public static IMessageFactory fromUrl(String url) {
            // Assertions.
            if(url == null || url.trim().equals("")){
                String msg = "Argument 'url' cannot be null or empty";
                throw new IllegalArgumentException(msg);
            }

            // URL: MSG://net.unicon.academus.apps.messaging.RdbmsMessageFactoryCreator$CreatorRdbmsMessageFactory/myconfig/myuser
            String[] tmp = url.split("/",5);
            // 0 = 'MSG:'
            // 1 = ''
            // 2 = classname
            // 3 = configName
            // 4 = username

            String configName = tmp[3];
            String username = tmp[4];

            RdbmsMessageFactoryCreator c = RdbmsMessageFactoryCreator.getInstance(configName);
            String attachPath = c.getAttachmentPath();
            int layers = c.getAttachmentLayers();
            int expiration = c.getExpiration();
            
            IMessageFactory msgFac = null;

            if (c.isGroupTriggerActive()) {
                msgFac = new GroupTriggerMessageFactory(ds, attachPath, layers
                        , username, expiration, c.getCivisFactory());
            } else {
                msgFac = new GroupExpandMessageFactory(ds, attachPath, layers
                        , username, expiration, c.getCivisFactory());
            }
 
            return new CreatorRdbmsMessageFactory(username, configName, msgFac);
            
        }
        
        
        /**
         * 
         */
        public void cleanup() {
            msgFac.cleanup();
        }
        /**
         * @param filename
         * @param type
         * @param stream
         * @return
         * @throws MercuryException
         */
        public IAttachment createAttachment(String filename, String type,
                InputStream stream) throws MercuryException {
            return msgFac.createAttachment(filename, type, stream);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object arg0) {
            return msgFac.equals(arg0);
        }
        /**
         * @return
         */
        public Features getFeatures() {
            return msgFac.getFeatures();
        }
        /**
         * @param id
         * @return
         * @throws FolderNotFoundException
         * @throws MercuryException
         */
        public IFolder getFolder(String id) throws FolderNotFoundException,
                MercuryException {
            return msgFac.getFolder(id);
        }
        /**
         * @param id
         * @return
         * @throws MercuryException
         */
        public IMessage getMessage(String id) throws MercuryException {
            return msgFac.getMessage(id);
        }
        /**
         * @return
         */
        public IDecisionCollection[] getPreferences() {
            return msgFac.getPreferences();
        }
        /**
         * @return
         */
        public IRecipientType[] getRecipientTypes() {
            return msgFac.getRecipientTypes();
        }
        /**
         * @return
         * @throws MercuryException
         */
        public IFolder getRoot() throws MercuryException {
            return msgFac.getRoot();
        }
        /**
         * @return
         */
        public IChoiceCollection getSearchCriteria() {
            return msgFac.getSearchCriteria();
        }
        /**
         * @param sFolder
         * @return
         * @throws MercuryException
         */
        public IFolder getSpecialFolder(SpecialFolder sFolder)
                throws MercuryException {
            return msgFac.getSpecialFolder(sFolder);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return msgFac.hashCode();
        }
        /**
         * @param msg
         * @param fromFolder
         * @param toFolder
         * @throws MercuryException
         */
        public void move(IMessage msg, IFolder fromFolder, IFolder toFolder)
                throws MercuryException {
            msgFac.move(msg, fromFolder, toFolder);
        }
        /**
         * @param where
         * @param filters
         * @return
         * @throws MercuryException
         */
        public IMessage[] search(IFolder[] where, IDecisionCollection filters)
                throws MercuryException {
            return msgFac.search(where, filters);
        }
        /**
         * @param draft
         * @return
         * @throws MercuryException
         */
        public IMessage sendMessage(DraftMessage draft) throws MercuryException {
            return msgFac.sendMessage(draft);
        }
        /**
         * @param recipients
         * @param subject
         * @param body
         * @param attachments
         * @param priority
         * @return
         * @throws MercuryException
         */
        public IMessage sendMessage(IRecipient[] recipients, String subject,
                String body, IAttachment[] attachments, Priority priority)
                throws MercuryException {
            return msgFac.sendMessage(recipients, subject, body, attachments,
                    priority);
        }
        /**
         * @param recipients
         * @param subject
         * @param body
         * @param priority
         * @return
         * @throws MercuryException
         */
        public IMessage sendMessage(IRecipient[] recipients, String subject,
                String body, Priority priority) throws MercuryException {
            return msgFac.sendMessage(recipients, subject, body, priority);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return msgFac.toString();
        }
    }
}
