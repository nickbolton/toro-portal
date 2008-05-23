/*
 *******************************************************************************
 *
 * File:       GatewayPortlet.java
 *
 * Copyright:  (c)2004 UNICON, Inc.  All Rights Reserved.
 *
 * This source code is the confidential and proprietary information of UNICON.
 * No part of this work may be modified or used without the prior written
 * consent of UNICON.
 *
 *******************************************************************************
 */

package net.unicon.academus.apps.gateway;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletSession;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.unicon.academus.apps.ConfigHelper;
import net.unicon.academus.apps.SsoEntry;
import net.unicon.academus.apps.SsoHandler;
import net.unicon.academus.apps.SsoHandlerXML;
import net.unicon.academus.apps.TransletsConstants;
import net.unicon.academus.apps.gateway.engine.InitialQuery;
import net.unicon.academus.apps.util.TimedCache;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.StateMachine;
import net.unicon.warlock.fac.xml.XmlWarlockFactory;
import net.unicon.warlock.portlet.AbstractWarlockPortlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletSession;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public final class GatewayPortlet extends AbstractWarlockPortlet {
    
    private Map userContextMap;
    
    private final Log log = LogFactory.getLog(GatewayPortlet.class);

    // Instance Members.
    private String id;
    private GatewayApplicationContext context;
    private IScreen initialScreen;

    /*
     * Public API.
     */

    public GatewayPortlet() {
        this.id = null;
        this.context = null;
        this.initialScreen = null;
    }
    
    private String constructUserContextCacheKey(PortletSession session,
        String username) {
        // put the username first, so we can be assured of parsing it out later
        StringBuffer sb = new StringBuffer(username).append('/');
        sb.append(id).append('/');
        sb.append(session.getId());
        return sb.toString();
    }
    
    public void init(PortletConfig config) {
        
        // Instance Id.
        this.id = config.getInitParameter("Id");
        
        // Initialize.
        IScreen peephole = null;
        StateMachine m = null;
        try {
            
        	String uct = config.getInitParameter("userContextTimer");
        	if (uct == null) {
        		String msg = "Required Portlet Init Parameter 'userContextTimer' was not set.";
        		throw new RuntimeException(msg);
        	}
        	int timer = Integer.parseInt(uct);
            userContextMap = TimedCache.getCache("ssoAuthentication", timer);

            PortletContext ctx = config.getPortletContext();

            // Parse the config file.
            // ToDo: This should be taken from PortletPreferences, or some
            // other Publish-time parameter.
            String configPath = (String) config.getInitParameter("configPath");

            SAXReader reader = new SAXReader();
            URL configUrl = ctx.getResource(configPath);
            String configUrlStr = configUrl.toString();
            Document doc = reader.read(configUrlStr);
            Element configElement = (Element)doc.selectSingleNode("gateway");
            
            boolean useXsltc = Boolean.valueOf(configElement.attributeValue("useXsltc"));

            //Element configElement = (Element) reader.read(
            //    configUrl.toString()).selectSingleNode("gateway");            // Resolve any copy-of and imports
            configElement = ConfigHelper.handle(configElement);

            // Title.
            String title = "Gateway Portlet";
            Element el = (Element)configElement.selectSingleNode("title");
            if (el != null)
                title = el.getText();
            
            // AJAX form population
            String ajaxCallbackUrl = null;
            
            el = (Element) configElement.selectSingleNode("ajax-callback-url");
            if (el != null) {
                ajaxCallbackUrl = el.getText();
            }

            SsoHandler sh = new SsoHandlerXML();
            SsoEntry[] entries = sh.parse(ctx.getResourceAsStream(configPath));

            // Construct the application context.
            context = new GatewayApplicationContext(sh, entries, title, ajaxCallbackUrl);
            
            // Construct the rendering engine.
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
                    TransletsConstants.xsltcUseClasspath);
            } else {
                fac = new XmlWarlockFactory(trans);
            }

            // Construct the screens, and choose a peephole.
            List list = new ArrayList();
            Attribute p = configElement.attribute("peephole");
            String peepName = "gateway_main";
            if (p != null)
                peepName = p.getValue();

            Iterator it =
                ctx.getResourcePaths("/rendering/screens/gateway/").iterator();
            while (it.hasNext()) {
                String path = (String) it.next();
                URL screenUrl = ctx.getResource(path);
                Element e = (Element) reader.read(
                    screenUrl.toString()).selectSingleNode("screen");
                IScreen s = fac.parseScreen(e);

                if (s.getHandle().getValue().equals(peepName))
                    peephole = s;

                list.add(s);
            }

            // Build the state machine.
            IScreen[] screens = (IScreen[]) list.toArray(new IScreen[0]);
            m = new StateMachine(context, screens, peephole);

        } catch (Throwable t) {
            String msg = "GatewayPortlet failed to initialize.  See stack "
                                                        + "trace below.";
            throw new RuntimeException(msg, t);
        }

        initialScreen = peephole;

        super.init(id, m);

    } // end init

    public void destroy() {
        userContextMap = null;
    }

    public IScreen getInitialScreen(PortletSession session) {
        return initialScreen;
    }

    public IStateQuery getInitialStateQuery(PortletSession session) {
        return new InitialQuery((GatewayUserContext)getUserContext(session));
    }

    /*
     * Protected API.
     */

    protected IUserContext getUserContext(String username, PortletSession s) {
        throw new UnsupportedOperationException();
    }

    protected IUserContext getUserContext(Map userInfo, PortletSession s) {
        if (s == null) {
            String msg = "Argument 's [PortletSession]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        final String key = id + ":CONTEXT";

        // Look for existing, create if we don't find.
        GatewayUserContext rslt = (GatewayUserContext) s.getAttribute(key);
        if (rslt == null) {
            String username = (String)userInfo.get("user.login.id");
            String cacheKey = constructUserContextCacheKey(s, username);
            rslt = new GatewayUserContext(context, username, cacheKey, userInfo);
            s.setAttribute(key, rslt);
            
	        if (log.isDebugEnabled()) {
	            log.debug("GatewayPortlet::getUserContext adding user context: " +
	                rslt.getCacheKey());
	        }
	        userContextMap.put(rslt.getCacheKey(), rslt);
        }
        
        return rslt;

    } // end getUserContext

    /*
     * NestedTypes.
     */

    private IUserContext getUserContext(PortletSession s) {
        final String key = id + ":CONTEXT";
        return (IUserContext)s.getAttribute(key);
    }

} // end GatewayPortlet class
