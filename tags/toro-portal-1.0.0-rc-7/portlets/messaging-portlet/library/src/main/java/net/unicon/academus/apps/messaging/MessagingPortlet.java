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

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletSession;

import javax.sql.DataSource;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.unicon.academus.apps.ConfigHelper;
import net.unicon.academus.apps.TransletsConstants;
import net.unicon.academus.apps.messaging.RdbmsMessageFactoryCreator.CreatorRdbmsMessageFactory;
import net.unicon.academus.apps.messaging.engine.MessageListQuery;
import net.unicon.academus.apps.messaging.engine.MessagePeepholeNarrowQuery;
import net.unicon.academus.apps.messaging.engine.MessagePeepholeWideQuery;
import net.unicon.civis.grouprestrictor.IGroupRestrictor;
import net.unicon.civis.ICivisFactory;
import net.unicon.mercury.IMessageFactory;
import net.unicon.warlock.fac.xml.XmlWarlockFactory;
import net.unicon.warlock.fac.xml.XmlWarlockFactory;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.portlet.AbstractWarlockPortlet;
import net.unicon.warlock.StateMachine;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public final class MessagingPortlet extends AbstractWarlockPortlet {
    
    // Instance Members.
    private String id;
    private String session_key;
    private MessagingApplicationContext context;
    private IScreen initialScreen;
    private IStateQuery initialQuery;
    private Map globalInstances;

    private static final int PEEPHOLE_MESSAGE_LIMIT = 3;
    
    /*
     * Public API.
     */

    public MessagingPortlet() {

        // Instance Members.
        this.id = null;
        this.session_key = null;
        this.initialScreen = null;
        this.initialQuery = null;
    }

    public void init(PortletConfig config) {

        // Instance Id.
        id = config.getInitParameter("Id");
        session_key = id + ":CONTEXT";

        // Initialize.
        StateMachine m = null;
        try {

            PortletContext ctx = config.getPortletContext();

            // Parse the config file.
            String configPath = (String) config.getInitParameter("configPath");
            SAXReader reader = new SAXReader();
            URL configUrl = ctx.getResource(configPath);
            Element configElement =
                (Element) reader.read(configUrl.toString())
                                .selectSingleNode("messaging");

            configElement = ConfigHelper.handle(configElement);
            
            Element el = (Element)configElement.selectSingleNode("query-file");
            if (el == null) {
               throw new IllegalArgumentException("Unable to locate required <query-file> element");
            }
            RdbmsMessageFactoryCreator.bootstrap(el.getText());

            IMessageFactory systemEmail = null;

            // Look for the system email account
            el = (Element)configElement.selectSingleNode("message-factory[@id='system']");
            if (el != null) {
                Attribute impl = el.attribute("impl");
                if (impl == null)
                    throw new IllegalArgumentException(
                            "Element <message-factory> must have an 'impl'"
                          + " attribute.");
                try {
                    systemEmail =
                        (IMessageFactory)Class.forName(impl.getValue())
                         .getDeclaredMethod("parse",
                                 new Class[] { Element.class })
                         .invoke(null, new Object[] { el });
                } catch (Throwable t) {
                    throw new RuntimeException(
                        "Failed to parse the <message-factory> element.", t);
                }
            }

            // Parse the deployment default factories; these are provider
            // per-user by an access broker as defined in the configuration
            // file.
            globalInstances = new HashMap();
            List mList = configElement.selectNodes("message-center");
            for (Iterator mIt = mList.iterator(); mIt.hasNext();) {
                Element e = (Element)mIt.next();
                Attribute id = e.attribute("id");
                if (id == null)
                    throw new IllegalArgumentException(
                        "Element <message-center> must have an 'id' attribute");
                globalInstances.put(id.getValue(), new MessageCenter(e));
            }
            
            // Upload Limit setting
            mList = configElement.selectNodes("upload-limit");
            long ulimit = 0;
            if (!mList.isEmpty()) {
                String buf = ((Element)mList.get(0)).getText();
                Pattern p = Pattern.compile("([0-9.]+)([MmKkBb])?");
                Matcher mt = p.matcher(buf);
                if (mt.matches()) {
                    double td = Double.parseDouble(mt.group(1));
                    buf = mt.group(2);
                    if (buf == null || buf.equals("") || buf.equalsIgnoreCase("b"))
                        ulimit = (long)td;
                    else if (buf.equalsIgnoreCase("k"))
                        ulimit = (long)(td * 1024);
                    else if (buf.equalsIgnoreCase("m"))
                        ulimit = (long)(td * 1024 * 1024);
                }
            }

            boolean xhtml = false;
            Attribute attr = configElement.attribute("allow-xhtml");
            if (attr != null)
                xhtml = Boolean.valueOf(attr.getValue()).booleanValue();

            //  peephole setting
            Element tElement = (Element)configElement.selectSingleNode("peephole/type");
            Element mElement = (Element)configElement.selectSingleNode("peephole/message-limit");
            int msgLimit = PEEPHOLE_MESSAGE_LIMIT;			// user default value if element not specified
            String peepholeType = null;
            if (tElement != null) {
                peepholeType = tElement.getText();
            }else{
                throw new RuntimeException(
                        "Element <peephole><type> is missing inside" +
                        "the 'messaging' element.");
            }
            if (mElement != null) {
                msgLimit = Integer.parseInt(mElement.getText());
            }            

            // Civis factory for email lookups (Send copy via email)
            ICivisFactory addressBook = null;
            Map groupRestrictors = new HashMap();
            el = (Element)configElement.selectSingleNode("civis[@id='addressBook']");
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
                } catch (Throwable t) {
                    throw new RuntimeException(
                        "Failed to parse the <civis> element.", t);
                }
                
                // parse for the group restrictor
                
                Element gAttr = (Element)el.selectSingleNode("restrictor");
                if(gAttr != null){
                    Attribute a = gAttr.attribute("impl");
                    IGroupRestrictor restrictor = 
                        (IGroupRestrictor)Class.forName(a.getValue()).newInstance();
                    
                    groupRestrictors.put(addressBook.getUrl(), restrictor);
                    
                }else{
                    throw new IllegalArgumentException(
                            "Element <civis> must have an 'restrictor'"
                          + " element.");
                }
                
            }

            // Parsing name mappings for notifications callback
            Map callbacks = new HashMap();
            el = (Element)configElement.selectSingleNode("callbacks");
            if (el != null) {
                List l = el.selectNodes("entry");
                Iterator it = l.iterator();
                while (it.hasNext()) {
                    Element en = (Element)it.next();

                    Element name = (Element)en.selectSingleNode("name");
                    Element loc = (Element)en.selectSingleNode("location");

                    if (name == null || loc == null)
                        throw new IllegalArgumentException(
                                "callbacks/entry elements must contain both a "
                              + "<name> and <location> declaration.");

                    callbacks.put(name.getText(), loc.getText());
                }
            } // else, no mappings.

            // Construct the application context.
            context = new MessagingApplicationContext(id, systemEmail, addressBook
                    , ulimit, groupRestrictors, peepholeType, msgLimit, callbacks,
                    xhtml);

            // Construct the rendering engine;
            URL xslUrl = ctx.getResource("/rendering/templates/layout.xsl");
            Source trans = new StreamSource(xslUrl.toString());
            IWarlockFactory fac = new XmlWarlockFactory(trans,
                TransletsConstants.xsltcTransformerFactoryImplementation,
                TransletsConstants.xsltcDebug,
                TransletsConstants.xsltcPackage,
                TransletsConstants.xsltcGenerateTranslet,
                TransletsConstants.xsltcAutoTranslet,
                TransletsConstants.xsltcUseClasspath);

            // Construct the screens;
            List screenList = new ArrayList();
            List libList = new ArrayList();
            Iterator it = ctx.getResourcePaths("/rendering/screens/messaging/").iterator();
            while (it.hasNext()) {
                String path = (String) it.next();
                URL screenUrl = ctx.getResource(path);
                Element e = (Element) reader.read(screenUrl.toString()).getRootElement();
                if (e.getName().equals("screen")) {
                    screenList.add(e);
                } else if (e.getName().equals("library")) {
                    libList.add(e);
                }
            }
            
            // construct the selector screens
            it = ctx.getResourcePaths("/rendering/screens/selector/").iterator();
            while (it.hasNext()) {
                String path = (String) it.next();
                URL screenUrl = ctx.getResource(path);
                Element e = (Element) reader.read(screenUrl.toString()).getRootElement();
                if (e.getName().equals("screen")) {
                    screenList.add(e);
                } else if (e.getName().equals("library")) {
                    libList.add(e);
                }
            }

            IScreen peephole = null;
            Element[] libs = (Element[]) libList.toArray(new Element[0]);
            IScreen[] screens = new IScreen[screenList.size()];
            for (int i=0; i < screens.length; i++) {
                Element e = (Element) screenList.get(i);
                IScreen s = fac.parseScreen(e, libs);
                if (s.getHandle().getValue().endsWith(peepholeType)) {
                    peephole = s;
                }
                screens[i] = s;
            }

            // Build the state machine.
            m = new StateMachine(context, screens, peephole);

        } catch (Throwable t) {
            String msg = "MessagingPortlet failed to initialize.  See stack "
                                                        + "trace below.";
            throw new RuntimeException(msg, t);
        }

        initialScreen = m.getPeephole();
        
        super.init(id, m);

    }

    public void destroy() {}

    public IScreen getInitialScreen(PortletSession session) {
        return initialScreen;
    }

    public IStateQuery getInitialStateQuery(PortletSession session) {
        MessagingUserContext uctx = (MessagingUserContext)session.getAttribute(session_key);
        uctx.setFactorySelection(uctx.getFactory("notifications"));

        String peephole = uctx.getAppContext().getPeepholeType();
        if(peephole.equalsIgnoreCase("wide")){
            return new MessagePeepholeWideQuery(uctx);
        }
        
        return new MessagePeepholeNarrowQuery(uctx);
    }

    /*
     * Protected API.
     */

    protected IUserContext getUserContext(String username, PortletSession s) {

        // Assertions.
        if (username == null || "".equals(username))
            throw new IllegalArgumentException(
                    "Argument 'username' cannot be null or empty.");
        if (s == null)
            throw new IllegalArgumentException(
                    "Argument 's [PortletSession]' cannot be null.");

        // Look for an existing IUserContext; create one if necessary.
        MessagingUserContext rslt = (MessagingUserContext) s.getAttribute(session_key);
        if (rslt == null) {
            rslt = new MessagingUserContext(context, s, username);

            // Add the deployment defaults
            for (Iterator it = globalInstances.values().iterator(); it.hasNext();) {
                MessageCenter mc = (MessageCenter)it.next();
                FactoryInfo info = mc.createFactory(rslt.getPrincipal());
                if (info != null)
                    rslt.addFactory(info);
            }

            // TODO: Add user-added factories (personal email accounts, etc)
            
            s.setAttribute(session_key, rslt);
        }

        return rslt;

    }
}
