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

package net.unicon.academus.apps.briefcase;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletSession;
import javax.sql.DataSource;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.apps.TransletsConstants;
import net.unicon.alchemist.encrypt.EncryptionService;
import net.unicon.alchemist.log.Logger;
import net.unicon.civis.ICivisFactory;
import net.unicon.civis.grouprestrictor.IGroupRestrictor;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.StateMachine;
import net.unicon.warlock.WarlockException;
import net.unicon.warlock.fac.xml.XmlWarlockFactory;
import net.unicon.warlock.portlet.AbstractWarlockPortlet;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public final class BriefcasePortlet extends AbstractWarlockPortlet {
    
    // Instance Members.
    private String id;
    private BriefcaseApplicationContext context;
    private IScreen initialScreen;
    private IStateQuery initialQuery;

    /*
     * Public API.
     */

    public BriefcasePortlet() {
        this.id = null;
        this.initialScreen = null;
    }

    public void init(PortletConfig config) {

        // Instance Id.
        id = config.getInitParameter("Id");

        // Initialize.
        StateMachine m = null;
        try {

            PortletContext ctx = config.getPortletContext();

            // DataSource.
            DataSource ds = AcademusFacadeContainer.retrieveFacade().getAcademusDataSource();

            // Parse the config file.
            String configPath = (String) config.getInitParameter("configPath");
            SAXReader reader = new SAXReader();
            URL configUrl = ctx.getResource(configPath);
            Element configElement = (Element) reader.read(configUrl.toString()).selectSingleNode("briefcase");

            
            // Prep the drive(s).
            List dList = configElement.selectNodes("drive");
            Drive[] drives = new Drive[dList.size()];
            for (int i=0; i < drives.length; i++) {
                drives[i] = Drive.parse((Element) dList.get(i));
            }
            
            // read the selector civis factory information
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
            context = new BriefcaseApplicationContext(drives, id
                    , (ICivisFactory[])(civisFactories.toArray(new ICivisFactory[0]))
                    , groupRestrictors);

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
            List list = new ArrayList();
            IScreen peephole = null;
            Iterator it = ctx.getResourcePaths("/rendering/screens/briefcase/").iterator();
            while (it.hasNext()) {
                String path = (String) it.next();
                URL screenUrl = ctx.getResource(path);
                Element e = (Element) reader.read(screenUrl.toString()).selectSingleNode("screen");
                IScreen s = fac.parseScreen(e);
                if (s.getHandle().equals("welcome")) {
                    peephole = s;
                }
                list.add(s);
            }
            
            // construct the selector screens
            it = ctx.getResourcePaths("/rendering/screens/selector/").iterator();
            while (it.hasNext()) {
                String path = (String) it.next();
                URL screenUrl = ctx.getResource(path);
                Element e = (Element) reader.read(screenUrl.toString()).selectSingleNode("screen");
                IScreen s = fac.parseScreen(e);                
                list.add(s);
            }

            // Build the state machine.
            IScreen[] screens = (IScreen[]) list.toArray(new IScreen[0]);
            m = new StateMachine(context, screens, peephole);
            
            // initailize a logger for the application
            // read the logging information to activate the specified categories            
            Element loggerElement = (Element) configElement.selectSingleNode("logger");
            if(loggerElement != null){
	            Logger logger = Logger.parse(loggerElement);                
	            BriefcaseLogger.bootStrap(logger);   
            }

        } catch (Throwable t) {
            String msg = "BriefcasePortlet failed to initialize.  See stack "
                                                        + "trace below.";
            throw new RuntimeException(msg, t);
        }

        initialScreen = m.getScreen(Handle.create("welcome"));

        super.init(id, m);
    }

    public void destroy() {}

    public IScreen getInitialScreen(PortletSession session) {
        return initialScreen;
    }
    
    /**
     * @throws UnsupportedOperationException because this method should never be
     *         called. {@link #getInitialStateQuery(PortletSession, IUserContext)}
     *         should be called instead.         
     * @see AbstractWarlockPortlet#getInitialStateQuery(PortletSession)
     */
    @Override
    public IStateQuery getInitialStateQuery(PortletSession session) {
        final String msg = "This method should never be called. " + 
                "getInitialStateQuery(PortletSession, IUserContext) should " +
                "be called instead.";
        throw new UnsupportedOperationException(msg);
    }
    
    /**
     * @see AbstractWarlockPortlet#getInitialStateQuery(PortletSession, IUserContext)
     */
    @Override
    public IStateQuery getInitialStateQuery(PortletSession session, 
            IUserContext userContext) {
        
        // Assertions
        if (session == null) {
            final String msg = "Argument 'session' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        if (userContext == null) {
            final String msg = "Argument 'userContext' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        if (!(userContext instanceof BriefcaseUserContext)) {
            final String msg = "Argument 'userContext' must be of type " + 
                    "BriefcaseUserContext, instead it is " + 
                    userContext.getClass().getName();
            throw new IllegalArgumentException(msg);
        }
        
        BriefcaseUserContext buc = (BriefcaseUserContext)userContext;
        EncryptionService encryptionService = context.getEncryptionService();
        
        // Initial Query.
        StringBuffer state = new StringBuffer();
        state.append("<state><briefcase>");
        List<Drive> drives = buc.getDrives();
        String encryptedHandle = null; 
        
        for (Drive d : drives) {

            encryptedHandle = encryptionService.encrypt(d.getHandle());

            state.append("<drive handle=\"").append(encryptedHandle)
                    .append("\" class-large=\"").append(d.getLargeIcon())
                    .append("\" class-opened=\"").append(d.getOpenIcon())
                    .append("\" class-closed=\"").append(d.getClosedIcon())
                    .append("\">");
            state.append("<label>").append(d.getLabel()).append("</label>");
            state.append("<description>").append(d.getDescription())
                    .append("</description>");
            state.append("</drive>");
        }
        state.append("</briefcase></state>");
        initialQuery = new StateQueryImpl(state.toString());
        return initialQuery;
    }

    /*
     * Protected API.
     */

    protected IUserContext getUserContext(String username, PortletSession s) {

        // Assertions.
        if (username == null) {
            String msg = "Argument 'username' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (s == null) {
            String msg = "Argument 's [PortletSession]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // NB:  Use normal concatenation (not StringBuffer) b/c it's only
        // 1 and b/c if it already exists, the JVM won't create new.
        String key = id +":CONTEXT";

        // Look for existing, create if we don't find.
        IUserContext rslt = (IUserContext) s.getAttribute(key);
        if (rslt == null) {
            rslt = new BriefcaseUserContext(username, s, context);
            s.setAttribute(key, rslt);
        }

        return rslt;

    }

    /*
     * NestedTypes.
     */

    private static final class StateQueryImpl implements IStateQuery {

        // Instance Members.
        private final String state;

        /*
         * Public API.
         */

        public StateQueryImpl(String state) {

            // Assertions.
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.state = state;

        }

        public String query() throws WarlockException {
            return state;
        }

        public IDecisionCollection[] getDecisions() throws WarlockException {
            return new IDecisionCollection[0];
        }

    }

}
