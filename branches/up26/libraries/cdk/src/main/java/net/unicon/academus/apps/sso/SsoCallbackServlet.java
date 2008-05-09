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

package net.unicon.academus.apps.sso;

import net.unicon.academus.apps.util.TimedCache;
import net.unicon.academus.apps.SsoEvaluator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.UserInstance;
import org.jasig.portal.UserInstanceManager;
import org.jasig.portal.security.IPerson;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

public class SsoCallbackServlet extends HttpServlet {
    
    /**
     * The serialization version of this class.  This field must be updated
     * whenever the serialized state of this class is modified (e.g. by adding
     * an instance variable).
     */
    private static final long serialVersionUID = 1L;

    protected final Log log = LogFactory.getLog(getClass());
    
    private String unauthenticatedRedirectUrl = null;
    
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        unauthenticatedRedirectUrl = config.getInitParameter("unauthenticatedRedirectUrl");
    }
    
    private String buildRedirectAttribute(HttpServletRequest req) {
        StringBuffer sb = new StringBuffer("redirect=\"");
        sb.append(req.getContextPath()).append(unauthenticatedRedirectUrl);
        sb.append("\"");
        return sb.toString();
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) {

        doGet(req, res);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) {

        try {
            res.setHeader("pragma", "no-cache");
            res.setHeader("Cache-Control", "no-store, no-cache, max-age=0, must-revalidate, post-check=0, pre-check=0");
            res.setDateHeader("Expires", 0);
            
            Map cache = TimedCache.getCache("ssoAuthentication");
            
            if (log.isDebugEnabled()) {
	            log.debug("SsoCallbackServlet uri: " + req.getRequestURI() + "?" + req.getQueryString());
            }
            
            HttpSession session = req.getSession(false);
            
            if (session == null) {
                returnErrorResponse(res, buildRedirectAttribute(req));
                return;
            }
            
            UserInstance userInstance = UserInstanceManager.getUserInstance(req);
            String gatewayUserContextKey = req.getParameter("gatewayUserContextKey");
            if (gatewayUserContextKey == null) {
                returnErrorResponse(res);
                return;
            }
            
            String[] split = gatewayUserContextKey.split("/");
            String username = split[0];
            
            String sessionUsername = (String)userInstance.getPerson().getAttribute(IPerson.USERNAME);
            if (!username.equals(sessionUsername)) {
                log.warn("SsoCallbackServlet user not authoriated: " +
                    sessionUsername + ", " + username);
                returnErrorResponse(res);
                return;
            }
            
            if (log.isDebugEnabled()) {
                log.debug("SsoCallbackServlet lookup name: " + gatewayUserContextKey);
            }
            
            SsoEvaluator evaluator = (SsoEvaluator)session.getAttribute(gatewayUserContextKey);
            if (evaluator == null) {
                evaluator = (SsoEvaluator)cache.get(gatewayUserContextKey);
                if (evaluator != null) {
                    session.setAttribute(gatewayUserContextKey, evaluator);
                    cache.remove(gatewayUserContextKey);
                }
            }
            
            if (evaluator == null) {
                returnErrorResponse(res);
                return;
            }
            
            String entries = evaluator.evaluateAllEntries(true);
            
            if (log.isDebugEnabled()) {
                log.debug("SsoCallbackServlet retrieving id/userContext: " +
                    gatewayUserContextKey + "/" +entries);
            }

            if (entries == null) {
                returnErrorResponse(res);
                return;
            }
            
            res.getWriter().print(entries);
        } catch (Exception e) {
            log.error("Exception producing callback response.", e);
            try {
	            returnErrorResponse(res);
            } catch (IOException ioe) {
                log.error("Exception returning error response.", e);
            }
        }
    }
    
    private void returnErrorResponse(HttpServletResponse res) throws IOException {
        returnErrorResponse(res, null);
    }

    private void returnErrorResponse(HttpServletResponse res, String attributes)
        throws IOException {
        StringBuffer errorMsg = new StringBuffer("<error ");
        if (attributes != null) {
            errorMsg.append(attributes);
        }
        errorMsg.append("/>");
        if (log.isDebugEnabled()) {
            log.debug("SsoCallbackServlet errorMsg: " + errorMsg);
        }
        res.getWriter().print(errorMsg.toString());
    }

}
