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
package net.unicon.academus.apps.blogger.access;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.sql.DataSource;

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.IAcademusFacade;
import net.unicon.academus.api.IAcademusGroup;
import net.unicon.alchemist.access.AccessBroker;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.AccessType;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.alchemist.access.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.BlojsomException;
import org.blojsom.authorization.AuthorizationProvider;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.blog.BlojsomConfigurationException;
import org.blojsom.util.BlojsomConstants;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * AcademusAuthorizationProvider
 *
 * @author Mike Freestone
 */
public class AcademusAuthorizationProvider implements AuthorizationProvider,
                                                      BlojsomConstants
{
    private Log _logger = LogFactory.getLog(AcademusAuthorizationProvider.class);

    protected static final String ACADEMUS_CONFIGURATION = "academus_config.xml";

    protected ServletConfig _servletConfig = null;
    protected IAcademusFacade _facade = null;
    protected String _jndiName = "";
    protected boolean _inited = false;
    protected Map _brokers = null;

    /**
     * Default constructor
     */
    public AcademusAuthorizationProvider() {
        _brokers = new HashMap();
    }

    /**
     * Initialization method for the authorization provider
     *
     * @param servletConfig ServletConfig for obtaining any initialization parameters
     * @param blojsomConfiguration BlojsomConfiguration for blojsom-specific
     * configuration information
     * @throws org.blojsom.blog.BlojsomConfigurationException
     *   If there is an error initializing the provider
     */
    public void init(ServletConfig servletConfig,
                     BlojsomConfiguration blojsomConfiguration)
                        throws BlojsomConfigurationException
    {
        if (_inited) return;

        _servletConfig = servletConfig;

        try {
            // Parse the gateway config file.
            String configPath =
                blojsomConfiguration.getBaseConfigurationDirectory() +
                ACADEMUS_CONFIGURATION;

            URL configUrl =
                _servletConfig.getServletContext().getResource(configPath);

            SAXReader reader = new SAXReader();
            Element configElement =
                (Element) reader.read(configUrl.toString())
                                .getRootElement();

            Element el = (Element)configElement.selectSingleNode("jndi-ref");
            if (el != null) {
                _jndiName = el.getText();
            }

            // Bootstrap all RDBMS Access Brokers.
            List bsList = configElement.selectNodes("//*[@needsDataSource='true']");
            if (!bsList.isEmpty()) {
                DataSource ds = AcademusFacadeContainer.retrieveFacade(true).getAcademusDataSource();

                for (Iterator it = bsList.iterator(); it.hasNext();) {
                    Element e = (Element) it.next();

                    Attribute impl = e.attribute("impl");
                    if (impl == null)
                        throw new IllegalArgumentException(
                                "Elements with the 'needsDataSource' attribute "
                                + " must have an 'impl' attribute.");

                    Class.forName(impl.getValue())
                        .getDeclaredMethod("bootstrap",
                                new Class[] { DataSource.class })
                        .invoke(null, new Object[] { ds });
                }
            }

            // Get Permissions Access Broker.
            List list = configElement.selectNodes("access-broker");
            if (list == null)
                throw new Exception(
                        "AcademusAuthorizationProvider requires one <access-broker> element per blog instance.");
            else {
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    el = (Element)it.next();
                    AccessBroker ab = AccessBroker.parse(el);
                    String handle = el.attribute("handle").getValue().substring(5);
                    _logger.debug("Adding AccessBroker for blog instance '"+handle+"'");
                    _brokers.put(handle, ab);
                }
            }

            // Get Academus Facade.
            _facade = AcademusFacadeContainer.retrieveFacade(true);

        } catch (Throwable e) {
            _logger.debug("Error AcademusAuthorizationProvider::init");
            e.printStackTrace(System.err);
            throw new BlojsomConfigurationException(e);
        }

        _logger.debug("Initialized Academus authorization provider");
        _inited = true;

    } // end init

    /**
     * Loads the authentication credentials for a given user
     *
     * @param blogUser {@link BlogUser}
     * @throws BlojsomException If there is an error loading the user's
     * authentication credentials
     */
    public void loadAuthenticationCredentials(BlogUser blogUser)
    throws BlojsomException
    {
    }

    /**
     * Authorize a username and password
     *
     * @param blogUser {@link BlogUser}
     * @param authorizationContext {@link Map} to be used to provide other
     * information for authorization. This will
     * change depending on the authorization provider.
     * This parameter is not used in this implementation.
     * @param username Username
     * @param password Password
     * @throws BlojsomException If there is an error authorizing the
     * username and password
     */
    public void authorize(BlogUser blogUser, Map authorizationContext,
        String username, String password) throws BlojsomException
    {
        _logger.debug("Checking authentication for user '"+username+"' on blog '"+blogUser.getId()+"'");

        // First check to see if they provided a valid user login.
        boolean result = false;
        try {
            result = _facade.authenticate(username, password);
        } catch (Exception e) {
            _logger.error(e);
            throw new BlojsomException(e);
        }
        if (!result) {
            String msg = "Authentication failed for user '"
                +username+"' in blog '"+blogUser.getId()
                +"': Incorrect username or password.";
            _logger.info(msg);
            throw new BlojsomException(msg);
        }

        // Now check to see if they have any permissions. They should not be
        // authenticated if they do not have permissions; it leads to
        // confusion.
        AccessBroker broker = (AccessBroker)_brokers.get(blogUser.getId());
        if (broker == null)
            throw new BlojsomException(
                    "Unable to locate an AccessBroker for blog instance: "+blogUser.getId());
        try {
            Principal pricipal = getPrincipal(username);
            IAccessEntry[] entries = broker.getEntries(pricipal);

            if (entries != null && entries.length > 0) {
                AccessRule[] rules = entries[0].getAccessRules();

                if (rules == null || rules.length == 0)
                    result = false;
            } else {
                result = false;
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new BlojsomException(e);
        }

        if (!result) {
            String msg = "Authentication failed for user '"
                +username+"' in blog '"+blogUser.getId()
                +"': No permissions exist.";
            _logger.info(msg);
            throw new BlojsomException(msg);
        }

    } // end authorize

    /**
     * Check a permission for the given user
     *
     * @param blogUser  {@link org.blojsom.blog.BlogUser}
     * @param permissionContext {@link java.util.Map} to be used to provide
     * other information for permission check. This will
     * change depending on the authorization provider.
     * @param username Username
     * @param permission Permission
     * @throws org.blojsom.BlojsomException If there is an error checking
     * the permission for the username and permission
     */
    public void checkPermission(BlogUser blogUser, Map permissionContext,
        String username, String permission) throws BlojsomException
    {
        if (username == null) {
            throw new BlojsomException(
                "No username provided to check permission");
        }
        if (permission == null) {
            throw new BlojsomException("Cannot check null permission");
        }

        _logger.debug("Checking authorization for user '"
                        +username+"' on blog '"+blogUser.getId()
                        +"' for permission '"+permission+"'");

        boolean result = false; 

        AccessBroker broker = (AccessBroker)_brokers.get(blogUser.getId());
        if (broker == null)
            throw new BlojsomException(
                    "Unable to locate an AccessBroker for blog instance: "+blogUser.getId());
        try {
            Principal pricipal = getPrincipal(username);
            IAccessEntry[] entries = broker.getEntries(pricipal);

            if (entries != null && entries.length > 0) {
                AccessRule[] rules = entries[0].getAccessRules();

                for (int i = 0; !result && i < rules.length; i++) {
		            AccessRule rule = rules[i];
                    AccessType type = rule.getAccessType();

                    if (type.getName().equals(permission) && rule.getStatus()) {
                        result = true;
                    }
                }
            } 
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new BlojsomException(e);
        }

        if (!result) {
            String msg = "Authorization for user '"
                        +username+"' on blog '"+blogUser.getId()
                        +"' for permission '"+permission+"' failed.";
            _logger.info(msg);
            throw new BlojsomException(msg);
        }

    } // end checkPermission

    protected Principal getPrincipal(String username) throws Exception {
        Principal result = null;

        try {
            IAcademusGroup[] groups = _facade.getAllContainingGroups(username);
            
            // Need to create two identities for each group, one with the group
            // key appended to the path, one without. This enables backwards
            // compatibility with existing access entrys after group keys are 
            // added to Access Entry Identity Names as a result of AC-393 
            Identity[] ids = new Identity[(groups.length * 2) + 1];

            StringBuffer path = new StringBuffer();
            for (int i = 0, j = 0; i < groups.length; i++) {
                path.setLength(0); // Clear buffer
                path.append(groups[i].getGroupPaths(
                        IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR, 
                        false)[0]);
                
                // Create Identity without the group key
                ids[j++] = new Identity(path.toString(), IdentityType.GROUP);
                
                // Create second Identity for group with the group key appended
                path.append("[");
                path.append(groups[i].getKey());
                path.append("]");
                ids[j++] = new Identity(path.toString(), IdentityType.GROUP);
            }

            ids[ids.length - 1] = new Identity(username, IdentityType.USER);
            result = new Principal(ids);

        } catch (Throwable t) {
            String msg = "Unable to evaluate the user's identity within academus.";
            throw new Exception(msg, t);
        }

        return result;
    } // end getPrincipal

} // end AcademusAuthorizationProvider class
