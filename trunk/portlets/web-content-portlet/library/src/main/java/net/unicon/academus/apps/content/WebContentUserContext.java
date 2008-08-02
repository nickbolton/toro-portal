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

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.IAcademusFacade;
import net.unicon.academus.api.IAcademusGroup;
import net.unicon.academus.apps.SsoEntry;
import net.unicon.academus.apps.SsoEvaluator;
import net.unicon.alchemist.access.*;
import net.unicon.warlock.IUserContext;

import java.util.HashMap;
import java.util.Map;

public class WebContentUserContext implements IUserContext, SsoEvaluator {

    // Instance Members.
    private final WebContentApplicationContext app;
    private final String username;
    private final Map userAttributes;
    private Web web;
    private String path;
    private final String cacheKey;

    /*
     * Public API.
     */

    public WebContentUserContext(WebContentApplicationContext app,
                        String username, Map userAttributes, String cacheKey) {

        // Assertions.
        if (app == null) {
            String msg = "Argument 'app' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (username == null) {
            String msg = "Argument 'username' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (userAttributes == null) {
            String msg = "Argument 'userAttributes' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (cacheKey == null) {
            String msg = "Argument 'cacheKey' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.app = app;
        this.username = username;
        this.userAttributes = new HashMap(userAttributes);
        this.web = null;
        this.path = null;
        this.cacheKey = cacheKey;

    }

    public String evaluateAllEntries(boolean exposeParameters) {
        StringBuffer sb = new StringBuffer();

        SsoEntry[] entries = app.getSsoEntries(getPrincipal());
	    for (int i=0; i < entries.length; i++) {
            sb.append(app.getSsoHandler().evaluate(
                entries[i], userAttributes, entries[i].getSequences()[0].getType(), exposeParameters));
	    }

        return sb.toString();
    }

    public Web getCurrentWeb() {

        if (web == null) {
            // See if there's only one...
            Principal p = getPrincipal(username);
            IAccessEntry[] entries = (IAccessEntry[]) app.getBroker()
                                        .getEntries(p, new AccessRule[0]);
            if (entries.length == 1 && entries[0].getAccessRules()[0]
                 .equals(new AccessRule(WebContentAccessType.VIEW, true))) {
                web = (Web) entries[0].getTarget();
            }
        }

        return web;

    }

    public String getCurrentPath() {

        if (path == null) {
            Web w = getCurrentWeb();
            path = w != null?w.getDefaultDocument():null;
        }

        return path;

    }

    public void setCurrentPath(String path) {

        // Assertions.
        if (path == null) {
            String msg = "Argument 'path' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        this.path = path;

    }

    public Principal getPrincipal() {
        return WebContentUserContext.getPrincipal(username);
    }

    public void addAttributes(Map m) {
        userAttributes.putAll(m);
    }

    public Map getUserAttributes() {
        return new HashMap(userAttributes);
    }

    public String getCacheKey() {
        return cacheKey;
    }
    
    /*
     * Implementation.
     */

    private static Principal getPrincipal(String username) {

        Principal rslt = null;

        try {
            IAcademusFacade facade = AcademusFacadeContainer.retrieveFacade(true);
            IAcademusGroup[] groups = facade.getAllContainingGroups(username);
            
            // Create an Identity array that includes all containing groups,
            // all containing groups formatted to include groupId, and username.
            Identity[] ids = new Identity[(groups.length * 2) + 1];
            for (int i=0; i < groups.length; i++) {
                ids[i] = new Identity(
                		groups[i].getGroupPaths(
                				IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR, 
                				false)[0],
                                IdentityType.GROUP);
                ids[i + groups.length] = new Identity(
                		groups[i].getGroupPaths(
                				IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR, 
                				false)[0] + "[" + groups[i].getKey() + "]",
                                IdentityType.GROUP);
            }
            ids[ids.length - 1] = new Identity(username, IdentityType.USER);
            rslt = new Principal(ids);
        } catch (Throwable t) {
            String msg = "Unable to evaluate the user's identity within academus.";
            throw new RuntimeException(msg, t);
        }

        return rslt;

    }

}
