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

package net.unicon.academus.apps.content;

import net.unicon.academus.apps.TransletsConstants;
import net.unicon.academus.apps.XHTMLFilter;
import net.unicon.academus.apps.SsoHandlerXML;
import net.unicon.academus.apps.XHTMLFilter.XHTMLFilterConfig;
import net.unicon.academus.apps.util.TimedCache;
import net.unicon.alchemist.access.AccessBroker;
import net.unicon.penelope.*;
import net.unicon.penelope.complement.TypeText1024;
import net.unicon.penelope.store.jvm.JvmEntityStore;
import net.unicon.warlock.Handle;
import net.unicon.warlock.*;
import net.unicon.warlock.fac.xml.XmlWarlockFactory;
import net.unicon.warlock.portlet.AbstractWarlockPortlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletSession;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public final class WebContentPortlet extends AbstractWarlockPortlet {

    private Map userContextMap;
    private final Log log = LogFactory.getLog(WebContentPortlet.class);

    // Instance Members.
    private String id;
    private WebContentApplicationContext context;
    private IScreen initialScreen;
    private IStateQuery initialQuery;
    private IChoiceCollection userAttributes;

    /*
     * Public API.
     */

    public WebContentPortlet() {
        this.id = null;
        this.context = null;
        this.initialScreen = null;
        this.initialQuery = null;
    }

    public void init(PortletConfig config) {

        // Instance Id.
        this.id = config.getInitParameter("Id");

        // Initialize.
        IScreen peephole = null;
        StateMachine m = null;
        try {

            int timer = Integer.parseInt(config.getInitParameter("userContextTimer"));
            userContextMap = TimedCache.getCache("ssoAuthentication", timer);

            PortletContext ctx = config.getPortletContext();

            // Parse the config file.
            String configPath = (String) config.getInitParameter("configPath");
            SAXReader reader = new SAXReader();
            URL configUrl = ctx.getResource(configPath);
            Element configElement = (Element) reader.read(configUrl.toString()).selectSingleNode("web-content");

            // AJAX form population
            String ajaxCallbackUrl = null;

            Element ajaxElement = (Element) configElement.selectSingleNode("wcms-sso/ajax-callback-url");
            if (ajaxElement != null) {
                ajaxCallbackUrl = ajaxElement.getText();
            }

            // Obtain the sso broker.
            Element brkrElement = (Element) configElement.selectSingleNode("wcms-sso/access-broker");
            AccessBroker ssoBroker = AccessBroker.parse(brkrElement);

            // Obtain the content broker.
            brkrElement = (Element) configElement.selectSingleNode("access-broker");
            AccessBroker contentBroker = AccessBroker.parse(brkrElement);

            // Obtain the BodyXpath.
            Attribute x = configElement.attribute("body-xpath");
            if (x == null) {
                String msg = "Element <web-content> is missing required "
                                    + "attribute 'body-xpath'.";
                throw new RuntimeException(msg);
            }
            String bodyXpath = x.getValue();

            // Obtain the input tags for PageGuid and ProjectGuid.
            Attribute t = configElement.attribute("input-tags");
            if (t == null) {
                String msg = "Element <web-content> is missing required "
                                    + "attribute 'input-tags'.";
                throw new RuntimeException(msg);
            }
            String inputTags = t.getValue();

            // Parse the rewriting rules.
            List list = configElement.selectNodes("url-rewriting/pattern");
            UrlRewritingRule[] rules = new UrlRewritingRule[list.size()];
            for (int i=0; i < rules.length; i++) {
                Element e = (Element) list.get(i);
                rules[i] = UrlRewritingRule.parse(e);
            }

            x = configElement.attribute("filter-config");
            XHTMLFilterConfig filterConfig = null;
            if (x != null && x.getValue().length() > 0) {
               filterConfig = XHTMLFilter.getConfiguration(x.getValue());
            }

            // Construct the application context.
            context = new WebContentApplicationContext(ssoBroker, contentBroker, bodyXpath
                    , inputTags, filterConfig, rules, new SsoHandlerXML(), ajaxCallbackUrl);

            // Construct the rendering engine.
            URL xslUrl = ctx.getResource("/rendering/templates/layout.xsl");
            Source trans = new StreamSource(xslUrl.toString());
            IWarlockFactory fac = new XmlWarlockFactory(trans,
                TransletsConstants.xsltcTransformerFactoryImplementation,
                TransletsConstants.xsltcDebug,
                TransletsConstants.xsltcPackage,
                TransletsConstants.xsltcGenerateTranslet,
                TransletsConstants.xsltcAutoTranslet,
                TransletsConstants.xsltcUseClasspath);

            // Choose a peephole.
            Attribute p = configElement.attribute("peephole");
            if (p != null) {
                String path = p.getValue();
                if (path.trim().length() != 0) {
                    URL screenUrl = ctx.getResource(path);
                    Element e = (Element) reader.read(screenUrl.toString()).selectSingleNode("screen");
                    peephole = fac.parseScreen(e);
                }
            }

            // Construct the screens.
            list = new ArrayList();
            Iterator it = ctx.getResourcePaths("/rendering/screens/content/").iterator();
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

        } catch (Throwable t) {
            String msg = "WebContentPortlet failed to initialize.  See stack "
                                                        + "trace below.";
            throw new RuntimeException(msg, t);
        }

        // Construct the user attributes collection.
        try {
            IEntityStore store = new JvmEntityStore();
            IOption oName = store.createOption(net.unicon.penelope.Handle.create("id"),
                                                    null, TypeText1024.INSTANCE);
            IOption oPswd = store.createOption(net.unicon.penelope.Handle.create("password"),
                                                    null, TypeText1024.INSTANCE);
            IChoice choice = store.createChoice(net.unicon.penelope.Handle.create("user.login"),
                                                    null, new IOption[] { oName, oPswd }, 2, 2);
            userAttributes = store.createChoiceCollection(net.unicon.penelope.Handle.create("userAttributes"),
                                                    null, new IChoice[] { choice });
        } catch (Throwable t) {
            String msg = "WebContentPortlet failed to initialize user "
                            + "attributes.  See stack trace below.";
            throw new RuntimeException(msg, t);
        }

        if (peephole != null) {
            initialQuery = new StateQueryImpl("<state />");
            initialScreen = peephole;
        } else {
            initialQuery = null;
            initialScreen = m.getScreen(Handle.create("index"));
        }

        super.init(id, m);

    }

    public void destroy() {
        userContextMap = null;
    }

    public IScreen getInitialScreen(PortletSession session) {
        return initialScreen;
    }

    public IStateQuery getInitialStateQuery(PortletSession session) {

        IStateQuery rslt = initialQuery;

        final String key = id + ":CONTEXT";
        if (rslt == null) {
            WebContentUserContext user = (WebContentUserContext) session.getAttribute(key);
            rslt = new IndexQuery(context, user);
        }

        return rslt;

    }

    /*
     * Protected API.
     */

    protected IUserContext getUserContext(String username, PortletSession s) {
        String msg = "WebContentPortlet uses the other overload of getUserContext().";
        throw new UnsupportedOperationException(msg);
    }

    protected IUserContext getUserContext(Map userInfo, PortletSession s) {

        // Assertions.
        if (userInfo == null) {
            String msg = "Argument 'userInfo' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (s == null) {
            String msg = "Argument 's [PortletSession]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        final String key = id + ":CONTEXT";

        // Look for existing, create if we don't find.
        WebContentUserContext rslt = (WebContentUserContext) s.getAttribute(key);
        if (rslt == null) {
            String username = (String) userInfo.get("user.login.id");
            
            // username set to "guest" to handle rendering portlet on unauthenticated guest view.
            if (username == null) {
            	username = "guest";
            }
            rslt = new WebContentUserContext(context, username, userInfo, constructUserContextCacheKey(s, username));
            s.setAttribute(key, rslt);

            if (log.isDebugEnabled()) {
	            log.debug("WebContentPortlet::getUserContext adding user context: " +
	                rslt.getCacheKey());
	        }
            userContextMap.put(rslt.getCacheKey(), rslt);
        }

        return rslt;

    }

    private String constructUserContextCacheKey(PortletSession session,
                                                String username) {
        // put the username first, so we can be assured of parsing it out later
        StringBuffer sb = new StringBuffer(username).append('/');
        sb.append(id).append('/');
        sb.append(session.getId());
        return sb.toString();
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
