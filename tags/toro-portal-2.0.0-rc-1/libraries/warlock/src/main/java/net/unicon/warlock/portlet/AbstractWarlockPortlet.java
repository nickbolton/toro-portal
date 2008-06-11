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

package net.unicon.warlock.portlet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IAction;
import net.unicon.warlock.IActionResponse;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.StateMachine;
import net.unicon.warlock.WarlockException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractWarlockPortlet implements Portlet {
    private static final Log log =
        LogFactory.getLog(AbstractWarlockPortlet.class);
	
    // Instance Members.
    private String id;
    private StateMachine machine;

    /*
     * Public API.
     */

    public void init(String id, StateMachine m) {

        // Assertions.
        if (id == null) {
            String msg = "Argument 'id' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.id = id;
        this.machine = m;
        
        if (log.isInfoEnabled())
        	log.info("Initializing portlet [id: "+id+"]");

    }

    public abstract IScreen getInitialScreen(PortletSession session);
    
    /**
     * Returns an initial <code>IStateQuery</code> for the provided session.
     * 
     * <p>Subclasses which require initial state customized for the current user
     * should override 
     * {@link #getInitialStateQuery(PortletSession, IUserContext)} and implement
     * this method to throw {@link UnsupportedOperationException}.</p>
     * 
     * @param session a <code>Portlet</code> user session.
     * @return initial <code>IStateQuery</code> for the provided 
     *         <code>PortletSession</code>
     */
    public abstract IStateQuery getInitialStateQuery(PortletSession session);
    
    /**
     * Returns an initial <code>IStateQuery</code> for the provided session that
     * is potentially customized for the current user. This default 
     * implementation defers to {@link #getInitialStateQuery(PortletSession)} 
     * for backwards compatibility. 
     * 
     * <p>Subclasses may override this method to provide user specific 
     * information to the initial state query.</p>
     * 
     * @param session a <code>Portlet</code> user session.
     * @param userContext custom user data
     * @return initial <code>IStateQuery</code> for the provided 
     *         <code>PortletSession</code>
     */
    public IStateQuery getInitialStateQuery(PortletSession session, 
            IUserContext userContext) {
        return getInitialStateQuery(session);
    }

    public final void processAction(ActionRequest req, ActionResponse res)
                                            throws PortletException {

        // Assertions.
        if (id == null) {
            String msg = "Portlet not initialized.";
            throw new IllegalStateException(msg);
        }
        if (req == null) {
            String msg = "Argument 'req' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (res == null) {
            String msg = "Argument 'res' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        if (log.isInfoEnabled())
        	log.info("Processing action for portlet [id: "+id+"]");

        // Access the previous token.
        PortletSession session = req.getPortletSession(true); // TT 5496
        SessionToken token = (SessionToken) session.getAttribute(id);

        if (log.isDebugEnabled()) {
            Map params = req.getParameterMap();
            Iterator it = params.entrySet().iterator();
            StringBuffer buf = new StringBuffer();
            while (it.hasNext()) {
                Map.Entry me = (Map.Entry)it.next();
                buf.append(me.getKey()).append("=").append(((String[])me.getValue())[0]).append(", ");
            }
            log.debug("Request received; parameters = ["+buf+"]; Expected session token timestamp: "+token.getTimestamp());
        }

        if (token != null) {
	        // We can only continue if the request timestamp matches that of our token.
	        String ts = req.getParameter("ts");
	        if (ts == null || Long.parseLong(ts) != token.getTimestamp()) {
	            return;
            }
        } else {
            // workaround for missing session token which gets
            // lost on the first click through...
            try {
	            Map userInfo = (Map) req.getAttribute(PortletRequest.USER_INFO);
	            IUserContext uCtx = getUserContext(userInfo, session);
	            IScreen screen = getInitialScreen(session);
	            IStateQuery query = getInitialStateQuery(session, uCtx);
	            IChoiceCollection[] choices =
                    screen.getOwner().getRenderingEngine().render(screen,
                        query, new HashMap(),
                        new PrintWriter(new StringWriter()));
	            token = new SessionToken(
                    System.currentTimeMillis(), screen, query, choices);
            } catch (WarlockException we) {
                String msg = "AbstractWarlockPortlet failed to evaluate the "
                    + "specified screen.";
				throw new PortletException(msg, we);
            }
        }

        // Prep the input map.
        Map inpt = null;
        try {
            inpt = RequestReader.readActionRequest(req);
        } catch (Throwable t) {
            String msg = "AbstractWarlockPortlet was unable to analyze the "
                                                            + "request.";
            throw new PortletException(msg, t);
        }

        // Try to find an action.
        String actHandle = null;
        Iterator names = inpt.keySet().iterator();
        while (names.hasNext()) {
            String n = (String) names.next();
            if (n.startsWith("act_")) {

                // Make sure there's only one action invocation.
                if (actHandle != null) {
                    String msg = "Unable to process action.  Only one action "
                                        + "may be invoked in each request.  "
                                        + "Actions:  " + actHandle + ", "
                                        + n.substring(4);
                    throw new PortletException(msg);
                }

                // Strip the prefix.
                actHandle = n.substring(4);

            }
        }

        // We can only continue if there's an action invocation.
        if (actHandle == null) {
            return;
        }
        
        // Obtain Portlet User Attributes
        Map userInfo = (Map) req.getAttribute(PortletRequest.USER_INFO);
        
        // Propagate User Attributes via ThreadLocal in UserAttributesManager
        UserAttributesManager attributesManager = new UserAttributesManager();
        attributesManager.setAttributes(userInfo);

        // Invoke the action.
        IAction act = token.getScreen().getAction(Handle.create(actHandle));
        IActionResponse p = null;
        try {

            List list = Arrays.asList(act.getRequiredChoices());
            IDecisionCollection[] decisions = new IDecisionCollection[list.size()];
            for (int i=0; i < list.size(); i++) {
                String handle = (String) list.get(i);
                IChoiceCollection c = token.getChoices(handle);
                decisions[i] = DecisionReader.readDecisionCollection(c, inpt);
            }

            // Add user information to user context
            IUserContext uCtx = getUserContext(userInfo, session);
            p = act.invoke(decisions, uCtx);

        } catch (Throwable t) {
            String msg = "An error occured while invoking the specified "
                            + "action:  " + act.getHandle().getValue();
            PortletException pe = new PortletException(msg, t);
            pe.printStackTrace(System.out);
            throw pe;
        }

        // Set the session token.
        session.setAttribute(id, new SessionToken(p.getScreen(),
                                        p.getStateQuery()));

        // Manage peephole behavior where applicable.
        if (machine.getPeephole() != null) {
            // Peephole behavior enabled...we need to enforce window state.
            if (!p.getScreen().equals(machine.getPeephole())) {
                // set the window state...
                res.setWindowState(WindowState.MAXIMIZED);
            }
        }

    }

    public final void render(RenderRequest req, RenderResponse res)
                                        throws PortletException {

        // Assertions.
        if (id == null) {
            String msg = "Portlet not initialized.";
            throw new IllegalStateException(msg);
        }
        if (req == null) {
            String msg = "Argument 'req' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (res == null) {
            String msg = "Argument 'res' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        if (log.isInfoEnabled())
        	log.info("Rendering portlet [id: "+id+"]");

        // Access the previous token.
        PortletSession session = req.getPortletSession(true); // TT 5496
        
        SessionToken token = (SessionToken) session.getAttribute(id);

        // Access screen & query.
        IScreen screen = null;
        IStateQuery query = null;
        
        // Obtain Portlet User Attributes
        Map userInfo = (Map) req.getAttribute(PortletRequest.USER_INFO);
        
        // Propagate User Attributes via ThreadLocal in UserAttributesManager
        UserAttributesManager attributesManager = new UserAttributesManager();
        attributesManager.setAttributes(userInfo);
        
        if (token != null) {
            // Resume from current spot.
            screen = token.getScreen();
            query = token.getStateQuery();
        } else {
            // Start fresh...

            // Create the UserContext
            IUserContext uCtx = getUserContext(userInfo, session);

            screen = getInitialScreen(session);
            query = getInitialStateQuery(session, uCtx);
        }

        // Create a timestamp to tag this rendering cycle.
        long timestamp = System.currentTimeMillis();

        // Render.
        IChoiceCollection[] choices = null;
        try {
            Map params = new HashMap();
            PortletURL url = res.createActionURL();
            url.setParameter("ts", String.valueOf(timestamp));
            params.put("actionUrl", url.toString());
            params.put("appsRoot", req.getContextPath());
            params.put("namespace", res.getNamespace());
            res.setContentType(req.getResponseContentType());
            choices = screen.getOwner().getRenderingEngine().render(screen,
                                                    query, params, res.getWriter());
        } catch (Throwable t) {
            String msg = "AbstractWarlockPortlet failed to evaluate the "
                                                + "specified screen.";
            throw new PortletException(msg, t);
        }

        // (Re)set the session token.
        session.setAttribute(id, new SessionToken(timestamp, screen, query,
                                                    choices));

    }

    /*
     * Protected API.
     */

    protected final StateMachine getStateMachine() {
        return machine;
    }

    /**
     * Provides a warlock user context for the specified user session.
     * Subclasses may override this implementation if they require different
     * behavior. The subclass is responsible for storing and retreiving its
     * UserContext from the provided PortletSession.
     *
     * @param username A name that uniquely identifies the user in this system.
     * @param s A valid portlet user session.
     * @return A warlock user context.
     */
    protected abstract IUserContext getUserContext(String username, PortletSession s);

    /**
     * Provides a warlock user context for the specified user session.  This
     * overload has the potential to provide access to the entire body of user
     * information to the implementing portlet.
     *
     * @param userInfo A map of user information.
     * @param s A valid portlet user session.
     * @return A warlock user context.
     */
    protected IUserContext getUserContext(Map userInfo, PortletSession s) {
        String username = (String) userInfo.get("user.login.id");
        return getUserContext(username, s);
    }

    /*
     * Nested Types.
     */

    private final class SessionToken {

        // Instance Members.
        private final long timestamp;
        private final IScreen screen;
        private final IStateQuery query;
        private final Map choices;

        /*
         * Public API.
         */

        /**
         * Creates a session token from a screen and a query for the action cycle.
         */
        public SessionToken(IScreen screen, IStateQuery query) {

            // Assertions.
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (query == null) {
                String msg = "Argument 'query' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.timestamp = -1L;
            this.screen = screen;
            this.query = query;
            this.choices = null;

        }

        /**
         * Creates a session token from a timestamp, screen, query, and set of
         * choices for the rendering cycle.
         */
        public SessionToken(long timestamp, IScreen screen, IStateQuery query,
                                            IChoiceCollection[] choices) {

            // Assertions.
            if (timestamp <= 0) {
                String msg = "Argument 'timestamp' cannot be zero or less.";
                throw new IllegalArgumentException(msg);
            }
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (query == null) {
                String msg = "Argument 'query' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (choices == null) {
                String msg = "Argument 'choices' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.timestamp = timestamp;
            this.screen = screen;
            this.query = query;
            this.choices = new HashMap();
            Iterator it = Arrays.asList(choices).iterator();
            while (it.hasNext()) {
                IChoiceCollection c = (IChoiceCollection) it.next();
                this.choices.put(c.getHandle(), c);
            }

        }

        public long getTimestamp() {
            return timestamp;
        }

        public IScreen getScreen() {
            return screen;
        }

        public IStateQuery getStateQuery() {
            return query;
        }

        public IChoiceCollection getChoices(String handle) {

            // Assertions.
            if (handle == null) {
                String msg = "Argument 'handle' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Create penelope handle.
            net.unicon.penelope.Handle h =
                    net.unicon.penelope.Handle.create(handle);

            // Find the match.
            if (!choices.containsKey(h)) {
                String msg = "Unrecognized choice collection.  User state does "
                                + "not contain a choice collection with the "
                                + "specified handle:  " + handle;
                throw new IllegalArgumentException(msg);
            }
            return (IChoiceCollection) choices.get(h);

        }

    }

}
