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

package net.unicon.academus.api.uportal;

import java.io.IOException;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.jasig.portal.PortalException;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.security.InitialSecurityContextFactory;
import org.jasig.portal.services.Authentication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.ResourceLoader;

/**
 * Receives the username and password and tries to authenticate the user.
 * This class is used for external systems to authenticate against uPortal.
 */
public class PortalAuthentication {
    private static final Log log = LogFactory.getLog(PortalAuthentication.class);
    private static HashMap credentialTokens = null;
    private static HashMap principalTokens = null;
    private static Authentication authService = null;

    static {
        authService = new Authentication();
        HashMap cHash = new HashMap(1);
        HashMap pHash = new HashMap(1);

        try {
            String key = null;
            // We retrieve the tokens representing the credential and principal
            // parameters from the portal security properties file.
            Properties props = ResourceLoader.getResourceAsProperties(
                PortalAuthentication.class,
                "/properties/security.properties");

            Enumeration propNames = props.propertyNames();
            while (propNames.hasMoreElements()) {
                String propName = (String)propNames.nextElement();
                String propValue = props.getProperty(propName);
                if (propName.startsWith("credentialToken.")) {
                    key = propName.substring(16);
                    cHash.put(key, propValue);
                }
                if (propName.startsWith("principalToken.")) {
                    key = propName.substring(15);
                    pHash.put(key, propValue);
                }
            }

        } catch(PortalException pe) {
            log.error("::static ", pe);
        } catch(IOException ioe) {
            log.error("::static ", ioe);
        }

        credentialTokens = cHash;
        principalTokens = pHash;

    } // end static block

    /**
     * Authenticate the user based on the credentials provided. 
     *
     * @param person IPerson associated with the user
     * @param username Username of user being authenticated.
     * @param password Password of the user being authenticated.
     * @return Returns true if the user credentials are valid, false otherwise.
     */
    public static boolean authenticate(IPerson person, String username, String password) {
        try {
            if (person == null) {
                person = PersonFactory.createPerson();
                person.setSecurityContext(
                    InitialSecurityContextFactory.getInitialContext("root"));
            }

            HashMap principals = populateTokens(principalTokens, username);
            HashMap credentials = populateTokens(credentialTokens, password);

            // Attempt to authenticate the user.
            authService.authenticate(principals, credentials, person);
        } catch (Exception e) {
	    // Log the exception
            log.error("PortalAuthenitaction unable to authenticate " +
                username, e);
            person = null;
        }

        return (person != null && person.getSecurityContext().isAuthenticated());
    }

    /**
     * Authenticate the user based on the credentials provided. 
     *
     * @param username Username of user being authenticated.
     * @param password Password of the user being authenticated.
     * @return Returns true if the user credentials are valid, false otherwise.
     */
    public static boolean authenticate(String username, String password) {
        return authenticate(null, username, password);
    } // end authenticate

    private static HashMap populateTokens(Map tokens, String value) throws Exception
    {
        // null value causes exception in context.authentication
        value = (value == null ? "" : value).trim();

log.debug("Enter PortalAuthenitaction::populateTokens");

        HashMap result = new HashMap(1);
        Iterator itr = tokens.keySet().iterator();
        while (itr.hasNext()) {
            /*
                The relationship between the way the properties are stored and
                the way the subcontexts are named has to be closely looked at
                to make this work.  The keys are either "root" or the subcontext
                name that follows "root.". As as example, the contexts
                ["root", "root.simple", "root.cas"] are represented
                as ["root", "simple", "cas"].
            */
            String ctxName = (String)itr.next();
            String key = (ctxName.startsWith("root.") ?
                ctxName.substring(5) : ctxName);

log.debug("    * key --> "+key+", value --> "+value);

            result.put(key, value);
        }

        return result;
    } // end populateTokens

} // end PortalAuthentication
