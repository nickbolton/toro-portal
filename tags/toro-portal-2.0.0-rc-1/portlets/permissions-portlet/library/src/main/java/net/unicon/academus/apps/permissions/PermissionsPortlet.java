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

package net.unicon.academus.apps.permissions;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import net.unicon.academus.apps.permissions.engine.InitialQuery;
import net.unicon.civis.grouprestrictor.IGroupRestrictor;
import net.unicon.civis.ICivisFactory;
import net.unicon.warlock.fac.xml.XmlWarlockFactory;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.portlet.AbstractWarlockPortlet;
import net.unicon.warlock.StateMachine;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public final class PermissionsPortlet extends AbstractWarlockPortlet {

    // Instance Members.
    private String id;
    private String session_key;
    private PermissionsApplicationContext context;
    private IScreen initialScreen;
    private IStateQuery initialQuery;

    /*
     * Public API.
     */

    public PermissionsPortlet() {

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
                                .selectSingleNode("permissions");

            boolean useXsltc = Boolean.valueOf(configElement.attributeValue("useXsltc"));

            boolean cacheTemplates = true;

            if (configElement.attributeValue("cacheTemplates") != null) {
                cacheTemplates = Boolean.valueOf(configElement.attributeValue("cacheTemplates"));
            }

            configElement = ConfigHelper.handle(configElement);

            // read the portlet specified
            Map portletMap = new HashMap();			
            
            List pList = configElement.selectNodes("portlet");
            if (!pList.isEmpty()) {
                try {              
                    Element e = null;
                    Element eLabel = null;
                    Element eDesc = null;
                    Element eAccess = null;
                    
                    for (Iterator it = pList.iterator(); it.hasNext();) {
                        e = (Element) it.next();

                        Attribute portletHandle = e.attribute("handle");
                        if (portletHandle == null)
                            throw new IllegalArgumentException(
                                    "The <portlet> Element"
                                    + " must have a 'handle' attribute.");
                        
                        portletMap.put(portletHandle.getValue(), new PortletHelper(e));                        
                    }
                } catch (Throwable e) {
                    throw new RuntimeException("Failed the Portlet reading.", e);
                }
            }
            
            List civisFactories = new ArrayList();
            ICivisFactory cFac = null;
            Map groupRestrictors = new HashMap();
            Element el = (Element)configElement.selectSingleNode("civis[@id='addressBook']");
            if (el != null) {
                Attribute impl = el.attribute("impl");
                if (impl == null)
                    throw new IllegalArgumentException(
                            "Element <civis> must have an 'impl'"
                          + " attribute.");
                try {
                    cFac = (ICivisFactory)Class.forName(impl.getValue())
                    .getDeclaredMethod("parse",
                            new Class[] { Element.class })
                    .invoke(null, new Object[] { el });
                    
                    civisFactories.add(cFac);
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
                    
                    groupRestrictors.put(cFac.getUrl()
                            , restrictor);
                    
                }else{
                    throw new IllegalArgumentException(
                            "Element <civis> must have an 'restrictor'"
                          + " element.");
                }
                
            }

            // Construct the application context.
            context = new PermissionsApplicationContext(id, portletMap
                    , (ICivisFactory[])(civisFactories.toArray(new ICivisFactory[0]))
                    , groupRestrictors);

            // Construct the rendering engine;
            URL xslUrl = ctx.getResource("/rendering/templates/layout.xsl");
            Source trans = new StreamSource(xslUrl.toString());
            IWarlockFactory fac = null;
            
            if (useXsltc) {
                fac = new XmlWarlockFactory(trans,
                    TransletsConstants.xsltcTransformerFactoryImplementation,
                    TransletsConstants.xsltcDebug,
                    TransletsConstants.xsltcPackage,
                    TransletsConstants.xsltcGenerateTranslet,
                    TransletsConstants.xsltcAutoTranslet,
                    TransletsConstants.xsltcUseClasspath,
                    cacheTemplates);
            } else {
                fac = new XmlWarlockFactory(trans, cacheTemplates);
            }

            // Construct the screens;
            List screenList = new ArrayList();
            List libList = new ArrayList();
            Iterator it = ctx.getResourcePaths("/rendering/screens/permissions/").iterator();
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
                if (s.getHandle().getValue().endsWith("welcome")) {
                    peephole = s;
                }
                screens[i] = s;
            }

            // Build the state machine.
            m = new StateMachine(context, screens, peephole);

        } catch (Throwable t) {
            String msg = "PermissionsPortlet failed to initialize.  See stack "
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
        PermissionsUserContext uctx = (PermissionsUserContext)session.getAttribute(session_key);
        return new InitialQuery(uctx);
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
        PermissionsUserContext rslt = (PermissionsUserContext) s.getAttribute(session_key);
        
        if (rslt == null) {
            rslt = new PermissionsUserContext(context, s, username);
            s.setAttribute(session_key, rslt);
        }

        return rslt;

    }
}
