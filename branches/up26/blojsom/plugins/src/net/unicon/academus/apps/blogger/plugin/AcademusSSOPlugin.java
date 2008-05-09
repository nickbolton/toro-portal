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
package net.unicon.academus.apps.blogger.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.BlojsomException;
import org.blojsom.authorization.AuthorizationProvider;
import org.blojsom.blog.*;
import org.blojsom.plugin.admin.PermissionedPlugin;
import org.blojsom.plugin.BlojsomPlugin;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomMetaDataConstants;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.resources.ResourceManager;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * AcademusSSOPlugin
 *
 * @author Mike Freestone
 */
public class AcademusSSOPlugin implements BlojsomPlugin, BlojsomConstants,
BlojsomMetaDataConstants, PermissionedPlugin
{
    protected static final Log _logger = LogFactory.getLog(AcademusSSOPlugin.class);

    // Constants
    protected static final String BLOJSOM_ADMIN_PLUGIN_AUTHENTICATED_KEY =
        "org.blojsom.plugin.admin.Authenticated";
    protected static final String BLOJSOM_ADMIN_PLUGIN_USERNAME_KEY =
        "org.blojsom.plugin.admin.Username";
    protected static final String BLOJSOM_ADMIN_PLUGIN_USERNAME = "BLOJSOM_ADMIN_PLUGIN_USERNAME";
    protected static final String BLOJSOM_ADMIN_PLUGIN_USERNAME_PARAM = "username";
    protected static final String BLOJSOM_ADMIN_PLUGIN_PASSWORD_PARAM = "password";
    protected static final String BLOJSOM_ADMIN_PLUGIN_OPERATION_RESULT =
        "BLOJSOM_ADMIN_PLUGIN_OPERATION_RESULT";
    protected static final String BLOJSOM_USER_AUTHENTICATED =
        "BLOJSOM_USER_AUTHENTICATED";

    protected ServletConfig _servletConfig = null;
    protected BlojsomConfiguration _blojsomConfiguration = null;
    protected ResourceManager _resourceManager = null;
    protected AuthorizationProvider _authorizationProvider = null;

    /**
     * Default constructor.
     */
    public AcademusSSOPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin
     * is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve
     * any initialization parameters
     * @param blojsomConfiguration {@link BlojsomConfiguration} information
     * @throws org.blojsom.plugin.BlojsomPluginException If there is an error
     * initializing the plugin
     */
    public void init(ServletConfig servletConfig,
        BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException
    {
        _blojsomConfiguration = blojsomConfiguration;
        _servletConfig = servletConfig;

        try {
            Class authorizationProviderClass = Class.forName(
                _blojsomConfiguration.getAuthorizationProvider());
            _authorizationProvider =
                (AuthorizationProvider)authorizationProviderClass.newInstance();
            _authorizationProvider.init(servletConfig, blojsomConfiguration);
        } catch (ClassNotFoundException e) {
            throw new BlojsomPluginException(e);
        } catch (InstantiationException e) {
            throw new BlojsomPluginException(e);
        } catch (IllegalAccessException e) {
            throw new BlojsomPluginException(e);
        } catch (BlojsomConfigurationException e) {
            throw new BlojsomPluginException(e);
        }
         
        String resourceManagerClass = _blojsomConfiguration.getResourceManager();
        try {
            Class resourceManagerClazz = Class.forName(resourceManagerClass);
            _resourceManager = (ResourceManager) resourceManagerClazz.newInstance();
            _resourceManager.init(_blojsomConfiguration);
        } catch (InstantiationException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        } catch (IllegalAccessException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        } catch (ClassNotFoundException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        } catch (BlojsomException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        }

    } // end init

    /**
     * Authenticate the user if their authentication session variable is not present
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @param context Context
     * @param blogUser User information
     * @return <code>true</code> if the user is authenticated,
     * <code>false</code> otherwise
     */
    protected boolean authenticateUser(HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse, Map context, BlogUser blogUser)
    {
        Blog blog = blogUser.getBlog();
        BlojsomUtils.setNoCacheControlHeaders(httpServletResponse);
        HttpSession httpSession = httpServletRequest.getSession();

        String username = httpServletRequest.getParameter(
            BLOJSOM_ADMIN_PLUGIN_USERNAME_PARAM);
        String password = httpServletRequest.getParameter(
            BLOJSOM_ADMIN_PLUGIN_PASSWORD_PARAM);

        // Check for the authenticated key and if not authenticated,
        // look for a "username" and "password" parameter.
        if (httpSession.getAttribute(blog.getBlogAdminURL() + "_" +
            BLOJSOM_ADMIN_PLUGIN_AUTHENTICATED_KEY) == null)
        {
            if (username == null || password == null || "".equals(username) ||
                "".equals(password))
            {
                _logger.debug("No username/password provided or username/password was empty");
                return false;
            }

        } else if (username != null && !("".equals(username) && httpSession.getAttribute(blog.getBlogAdminURL() + "_" + BLOJSOM_ADMIN_PLUGIN_USERNAME_KEY).equals(username)))
        {
            httpSession.removeAttribute(blog.getBlogAdminURL() + "_" + BLOJSOM_ADMIN_PLUGIN_AUTHENTICATED_KEY);
            httpSession.removeAttribute(BLOJSOM_ADMIN_PLUGIN_USERNAME);
            httpSession.removeAttribute(BLOJSOM_USER_AUTHENTICATED);

            if (username == null || password == null || "".equals(username) ||
                "".equals(password))
            {
                _logger.debug("No username/password provided or username/password was empty");
                return false;
            }
        } else {
            return ((Boolean)httpSession.getAttribute(blog.getBlogAdminURL() + "_" + BLOJSOM_ADMIN_PLUGIN_AUTHENTICATED_KEY)).booleanValue();
        }

        // Let's attempt to authenticate the user.
        try {
            _authorizationProvider.loadAuthenticationCredentials(blogUser);
            _authorizationProvider.authorize(blogUser, null, username, password);
            httpSession.setAttribute(blog.getBlogAdminURL() + "_" +
                BLOJSOM_ADMIN_PLUGIN_AUTHENTICATED_KEY, Boolean.TRUE);
            httpSession.setAttribute(blog.getBlogAdminURL() + "_" +
                BLOJSOM_ADMIN_PLUGIN_USERNAME_KEY, username);
            httpSession.setAttribute(BLOJSOM_ADMIN_PLUGIN_USERNAME, username);
            httpSession.setAttribute(BLOJSOM_USER_AUTHENTICATED, Boolean.TRUE);

            _logger.debug("Passed authentication for username: " + username);

            return true;
        } catch (BlojsomException e) {
            _logger.debug("Failed authentication for username: " + username);
            addOperationResultMessage(context,
                 "Failed authentication for username: " + username);

            return false;
        }

    } // end authenticateUser

    /**
     * Retrieve the current authorized username for this session
     *
     * @param httpServletRequest Request
     * @param blog {@link Blog}
     * @return Authorized username for this session or
     * <code>null</code> if no user is currently authorized 
     */
    protected String getUsernameFromSession(HttpServletRequest httpServletRequest,
        Blog blog)
    {
        String username = (String)httpServletRequest.getSession().getAttribute(
            blog.getBlogAdminURL() + "_" + BLOJSOM_ADMIN_PLUGIN_USERNAME_KEY);

        return username;
    }

    /**
     * Check the permission for a given username and permission
     *
     * @param blogUser          {@link org.blojsom.blog.BlogUser} information
     * @param permissionContext {@link java.util.Map} containing context
     * information for checking permission
     * @param username          Username
     * @param permission        Permission
     * @return <code>true</code> if the username has the required permission,
     * <code>false</code> otherwise
     */
    public boolean checkPermission(BlogUser blogUser, Map permissionContext,
        String username, String permission)
    {
        try {
            _authorizationProvider.checkPermission(
                blogUser, permissionContext, username, permission);
        } catch (BlojsomException e) {
            _logger.error(e);
            return false;
        }

        return true;
    }

    /**
     * Adds a message to the context under the
     * <code>BLOJSOM_ADMIN_PLUGIN_OPERATION_RESULT</code> key
     *
     * @param context Context
     * @param message Message to add
     */
    protected void addOperationResultMessage(Map context, String message) {
        context.put(BLOJSOM_ADMIN_PLUGIN_OPERATION_RESULT, message);
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @param user {@link org.blojsom.blog.BlogUser} instance
     * @param context Context
     * @param entries Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing
     * the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse, BlogUser user, Map context,
        BlogEntry[] entries) throws BlojsomPluginException
    {
        authenticateUser(httpServletRequest, httpServletResponse, context, user);
        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws BlojsomPluginException If there is an error performing cleanup
     * for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws BlojsomPluginException If there is an error in finalizing
     * this plugin
     */
    public void destroy() throws BlojsomPluginException {
    }

} // end AcademusSSOPlugin class
