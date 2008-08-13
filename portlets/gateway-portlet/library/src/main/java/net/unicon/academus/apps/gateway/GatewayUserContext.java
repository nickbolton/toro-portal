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

package net.unicon.academus.apps.gateway;

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.IAcademusFacade;
import net.unicon.academus.api.IAcademusGroup;
import net.unicon.academus.apps.SsoEntry;
import net.unicon.academus.apps.SsoEvaluator;
import net.unicon.academus.apps.SsoHandler;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.alchemist.access.Principal;
import net.unicon.warlock.IUserContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GatewayUserContext implements
IUserContext, SsoEvaluator {
    
    // Instance Members.
    private final GatewayApplicationContext app;
    private final String username;
    private final String cacheKey;
    private String entryId;
    private Map userAttribs;
    private String entryLoginQueue;
    private Set loggedInSequences;
    private boolean changeCreds;

    /*
     * Public API.
     */

    public GatewayUserContext(GatewayApplicationContext app, String username,
        String cacheKey, Map userAttribs) {

        // Assertions.
        if (app == null) {
            String msg = "Argument 'app' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (username == null) {
        	// if username is null, assume we are 'guest'
        	username = "guest";
//            String msg = "Argument 'username' cannot be null.";
//            throw new IllegalArgumentException(msg);
        }
        if (cacheKey == null) {
            String msg = "Argument 'cacheKey' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.app = app;
        this.username = username;
        this.cacheKey = cacheKey;
        this.entryId = null;
        this.userAttribs = Collections.unmodifiableMap(userAttribs);
        this.loggedInSequences = new HashSet();
    }
    
    public String evaluateAllEntries(boolean exportParameters) {
        StringBuffer sb = new StringBuffer();

        SsoEntry[] entries = app.getSsoEntries();

        for (int i = 0; i < entries.length; i++) {
            SsoHandler ssoHandler = app.getSsoHandler();
            String result = ssoHandler.evaluate(
                entries[i],
                getUserAttributes(),
                entries[i].getSequences()[0].getType(),
                exportParameters);
            sb.append(result);
        }

        return sb.toString();
    }
    
    public String getCacheKey() {
        return cacheKey;
    }
    
    public Principal getPrincipal() {
        return GatewayUserContext.getPrincipal(username);
    }

    private static Principal getPrincipal(String username) {
        Principal result = null;

        try {
            IAcademusFacade facade = AcademusFacadeContainer.retrieveFacade();
            IAcademusGroup[] groups = facade.getAllContainingGroups(username);
            
            // Create an Identity array that includes all containing groups,
            // all containing groups formatted to include groupId, and username.
            Identity[] ids = new Identity[(groups.length * 2) + 1];
            for (int i=0; i < groups.length; i++) {
                ids[i] = new Identity(groups[i].getGroupPaths(
                    IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR, false)[0],
                    IdentityType.GROUP);
                ids[i + groups.length] = new Identity(groups[i].getGroupPaths(
                        IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR, false)[0] + "[" + groups[i].getKey() + "]",
                        IdentityType.GROUP);
            }

            ids[ids.length - 1] = new Identity(username, IdentityType.USER);
            result = new Principal(ids);

        } catch (Throwable t) {
            String msg = "Unable to evaluate the user's identity within academus.";
            throw new RuntimeException(msg, t);
        }

        return result;
    } // end getPrincipal

    /*
     * Implementation.
     */

    public GatewayApplicationContext getAppContext() {
        return this.app;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getEntryId() {
        return this.entryId;
    }

    public void clearEntryId() {
        this.entryId = null;
    }

    public Map getUserAttributes() {
        return this.userAttribs;
    }

    public void setChangeCreds(boolean b) {
        this.changeCreds = b;
    }

    public boolean getChangeCreds() { return this.changeCreds; }
    
    public String getCurrentSeq(SsoEntry entry) {
        boolean logged = this.loggedInSequences.contains(entry.getHandle());
        
        // check if a refresh sequence is defined
        if (logged && entry.getSequence("refresh") != null) {
           return "refresh";
        }
        
        // return login sequence if user has not logged 
        // in or when the login sequence is the only sequence defined.
        return "login";
    }
 
    public void setWillBeLoggedIn(String entryHandle) {
       this.entryLoginQueue = entryHandle;
    }

    public void setLoggedIn() {
       if (this.entryLoginQueue != null) {
          this.loggedInSequences.add(this.entryLoginQueue);
          this.entryLoginQueue = null;
       }
    }

} // end GatewayUserContext class
