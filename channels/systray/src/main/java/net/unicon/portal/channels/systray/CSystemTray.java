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

package net.unicon.portal.channels.systray;

import java.io.Serializable;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NotContextException;

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.civis.fac.academus.AcademusCivisFactory;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.cache.IMercuryListener;
import net.unicon.mercury.cache.MercuryCacheValidatorContainer;
import net.unicon.mercury.fac.gt.GroupTriggerMessageFactory;
import net.unicon.mercury.fac.rdbms.RdbmsMessageFactory;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.portal.util.RenderingUtil;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.util.XmlUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ICacheable;
import org.jasig.portal.IChannel;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

public class CSystemTray implements IChannel, ICacheable, Serializable, IMercuryListener {
	private static final Log log =
        LogFactory.getLog(CSystemTray.class);
	
    // Will not be serialized, and as such will end up as 'false' after
    // restart, as we want.
    private static boolean bootstrapped = false;
    private static AcademusCivisFactory cFac;
    private static final String queryFileProperty = CSystemTray.class.getName()+".query_file";
    private static final String queryFile =
      UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).
            getProperty(queryFileProperty);
   
    private static final String sslLocation = "CSystemTray.ssl";

    private transient IMessageFactory messaging;
    private transient boolean dirty = false;
    private boolean needsinit = true;

    private ChannelRuntimeData crd;
    private ChannelStaticData csd;

    public CSystemTray() {
        super();
    }

    public ChannelCacheKey generateKey() {
        ChannelCacheKey cacheKey = new ChannelCacheKey();
        cacheKey.setKey("CSystemTray");
        if (csd.getPerson().isGuest()) {
            cacheKey.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
        } else {
            cacheKey.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);
        }
        cacheKey.setKeyValidity(new Integer(42));

        return cacheKey;
    }

    public boolean isCacheValid(Object validity) {
        boolean rslt = true;

        if (!csd.getPerson().isGuest()) {
        	rslt = !dirty;
        }

        return rslt;
    }

    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
        this.crd = rd;
    }

    public void setStaticData(ChannelStaticData sd) throws PortalException {
        this.csd = sd;
        if (!csd.getPerson().isGuest()) {
	        MercuryCacheValidatorContainer.getInstance()
	        	.registerListener(this, (String)csd.getPerson().getAttribute(IPerson.USERNAME));
        }
    }

    public ChannelRuntimeProperties getRuntimeProperties() {
        return new ChannelRuntimeProperties();
    }

    public void receiveEvent(PortalEvent ev) {
    	if (ev.getEventNumber() == PortalEvent.SESSION_DONE && !csd.getPerson().isGuest()) {
			MercuryCacheValidatorContainer.getInstance()
				.unregisterListener(this, (String)csd.getPerson().getAttribute(IPerson.USERNAME));
		}
    }

    public void renderXML(ContentHandler out) throws PortalException {
        if (!csd.getPerson().isGuest()) {
            try {
                Hashtable params = new Hashtable();
                params.put("baseActionURL", crd.getBaseActionURL());

                params.put("messagingPortlet_id", getChannelId("MessagingPortlet"));

                RenderingUtil.renderDocument(
                        this, out, toDocument(), sslLocation, "main",
                        crd.getBrowserInfo(), params);
            } catch (Exception e) {
            	log.error(e.getMessage(), e);
                throw new PortalException(e);
            }
        }
        dirty = false;
    }

    protected Document toDocument() {
        Document doc = null;

        try {
            doc = DocumentFactory.getNewDocument();
            Element root = doc.createElement("systray");

            XmlUtils.addNewNode(doc, root, "username", null,
                    (String)csd.getPerson().getAttribute(IPerson.USERNAME));

            XmlUtils.addNewNode(doc, root, "messages", null,
                    String.valueOf(getMessageCount()));

            doc.appendChild(root);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        return doc;
    }

    private String getChannelId(String fname) {
        String rslt = null;

        try {
            // Get the context that holds the global IDs for this user
            Context globalIDContext = (Context)csd.getJNDIContext().lookup("/channel-ids");
            rslt = (String)globalIDContext.lookup(fname);
        } catch (NotContextException nce) {
            log.warn("CSystemTray::getChannelId(): Could not find subcontext /channel-ids in JNDI", nce);
        } catch (NamingException e) {
        	log.warn("Failure in getChannelId("+fname+")", e);
        }

        if (rslt == null)
            rslt = "";

        return rslt;
    }

    private int getMessageCount() throws Exception {
        int rslt = 0;

        if (!csd.getPerson().isGuest()) {
            if (needsinit)
                init();
            rslt = this.messaging.getRoot().getUnreadCount();
        }

        return rslt;
    }

    private synchronized void init() throws Exception {
        if (!bootstrapped) {
           GroupTriggerMessageFactory.bootstrap(queryFile);
           RdbmsMessageFactory.bootstrap(queryFile);
           RdbmsMessageFactory.bootstrap(
                    AcademusFacadeContainer.retrieveFacade()
                    .getAcademusDataSource());
            cFac = new AcademusCivisFactory();
            bootstrapped = true;
        }

        if (this.messaging == null) {
            this.messaging =
            		new GroupTriggerMessageFactory(
                        AcademusFacadeContainer.retrieveFacade()
                        .getAcademusDataSource(),
                        "/tmp", 3, // Neither of these will be used for checking unread count
                        (String)csd.getPerson().getAttribute(IPerson.USERNAME),
                        0, cFac);
        }

        this.needsinit = false;
    }

	public void markAsDirty() {
      if (log.isDebugEnabled()) {
         log.debug("CSystemTray["
               +(String)csd.getPerson().getAttribute(IPerson.USERNAME)
               +"] Marking cache dirty.");
      }
		this.dirty = true;
	}
}

