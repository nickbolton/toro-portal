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

import java.io.InputStream;
import java.io.IOException;

import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.blojsom.authorization.AuthorizationProvider;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.blog.BlojsomConfigurationException;
import org.blojsom.BlojsomException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomProperties;

/**
 * CompositeAuthorizationProvider
 *
 * @author Eric Andresen
 */
public class CompositeAuthorizationProvider implements AuthorizationProvider,
       BlojsomConstants
{
    private Log _logger = LogFactory.getLog(CompositeAuthorizationProvider.class);
    public static final String BLOG_COMPOSITEAUTH_IP = "blog-auth-composite";
    private static final String AUTHENTICATION_KEY = "authentication";
    private static final String AUTHORIZATION_KEY = "authorization";

    protected boolean bootstrapped = false;
    protected AuthorizationProvider authentication = null;
    protected AuthorizationProvider authorization = null;

    public CompositeAuthorizationProvider() {}

    public void init(ServletConfig servletConfig,
                     BlojsomConfiguration blojsomConfiguration)
                        throws BlojsomConfigurationException {
        if (!bootstrapped) {
            synchronized(this) {
                if (!bootstrapped) {
                    String authorizationConfiguration = servletConfig.getInitParameter(BLOG_COMPOSITEAUTH_IP);
                    if (BlojsomUtils.checkNullOrBlank(authorizationConfiguration)) {
                        _logger.error("No authorization configuration file specified: Attempted to load: "+authorizationConfiguration+" with key: "+BLOG_COMPOSITEAUTH_IP);
                        throw new BlojsomConfigurationException("No authorization configuration file specified");
                    }

                    Properties authorizationProperties;
                    InputStream is = servletConfig.getServletContext().getResourceAsStream(authorizationConfiguration);
                    authorizationProperties = new BlojsomProperties();
                    try {
                        authorizationProperties.load(is);
                        is.close();
                        Map authorizationMap = BlojsomUtils.propertiesToMap(authorizationProperties);

                        if (!authorizationMap.containsKey(AUTHENTICATION_KEY) || !authorizationMap.containsKey(AUTHORIZATION_KEY)) {
                            _logger.error("Both authentication and authorization keys must be specified.");
                            throw new BlojsomConfigurationException("Both authentication and authorization keys must be specified.");
                        }

                        authorization = loadClass((String)authorizationMap.get(AUTHORIZATION_KEY));
                        authentication = loadClass((String)authorizationMap.get(AUTHENTICATION_KEY));
                    } catch (IOException e) {
                        _logger.error(e);
                        throw new BlojsomConfigurationException(e);
                    }


                    this.bootstrapped = true;
                }
            }
        }

        authorization.init(servletConfig, blojsomConfiguration);
        authentication.init(servletConfig, blojsomConfiguration);
    }

    public void loadAuthenticationCredentials(BlogUser blogUser)
    throws BlojsomException
    {
        _logger.info("Loading authentication for blog instance: "+blogUser.getId());
        authentication.loadAuthenticationCredentials(blogUser);
        authorization.loadAuthenticationCredentials(blogUser);
    }

    // Really should be called authenticate... *sigh*
    public void authorize(BlogUser blogUser, Map authorizationContext,
        String username, String password) throws BlojsomException
    {
        _logger.info("Authenticating user: "+username+" for blog: "+blogUser.getId());
        authentication.authorize(blogUser, authorizationContext, username, password);
    }

    public void checkPermission(BlogUser blogUser, Map permissionContext,
        String username, String permission) throws BlojsomException
    {
        _logger.info("Checking permission for blog: "+blogUser.getId()+" user: "+username+" permission: "+permission);
        authorization.checkPermission(blogUser, permissionContext, username, permission);
    }

    protected AuthorizationProvider loadClass(String className) throws BlojsomConfigurationException {
        AuthorizationProvider rslt = null;
        _logger.info("Loading AuthorizationProvider: "+className);
        try {
            Class authorizationProviderClass = Class.forName(className);
            rslt = (AuthorizationProvider) authorizationProviderClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new BlojsomConfigurationException(e);
        } catch (InstantiationException e) {
            throw new BlojsomConfigurationException(e);
        } catch (IllegalAccessException e) {
            throw new BlojsomConfigurationException(e);
        }
        return rslt;
    }

} // end CompositeAuthorizationProvider class
