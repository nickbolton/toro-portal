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
package net.unicon.sdk.cache.squid;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.unicon.sdk.log.ILogService;
import net.unicon.sdk.log.LogLevel;
import net.unicon.sdk.log.LogServiceFactory;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
 
public class SquidMapServlet extends HttpServlet {

    private static SquidMap squidMap = new SquidMap();
    private static ILogService logService = null;

    static {
        try {
            logService = LogServiceFactory.instance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {

        String key = request.getPathInfo().substring(1);

        log(ILogService.DEBUG, "Servlet key", key);

        Object value = squidMap.getTemp(key);

        /**
         * One of three things can occur here.
         * 1) The temp map has the map entry and will be serialized and
         *    pushed back through the response object.
         *
         * 2) The temp map does not have the map entry and this servlet
         *    is running on the origiating host. The servlet request will
         *    result in an EOF condition and SquidMap will treat this as
         *    the key/value pair not existing.
         *
         * 3) The temp map does not have the map entry and this servlet
         *    is not running on the origiating host. The request will be
         *    forwarded along to the originating host indicated by the
         *    http header field x-forwarded-for (specified by squid).
         **/
        
        if (value == null) {
            if (!isOriginator(request)) {
                forwardAlong(request, response);
            } else {
                log(ILogService.DEBUG, "Servlet tempMap value is null", key);
                return;
            }
        } else {
            ObjectOutputStream out =
                new ObjectOutputStream(response.getOutputStream());
            log(ILogService.DEBUG, "Servlet value: " + value, key);
            out.writeObject(value);
            out.flush();
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
        doGet(request, response);
    }

    private boolean isOriginator(HttpServletRequest request) {
        String originatingUrl = request.getHeader(
            SquidMap.originatingUrlHeaderName);

        log(ILogService.DEBUG, "isOriginator : (originatingUrl, " +
            "localUrl): (" + originatingUrl +
            ", " + SquidMap.localUrl + ")");
        return SquidMap.localUrl.equals(originatingUrl);
    }

    private void forwardAlong(HttpServletRequest request,
        HttpServletResponse response)
    throws IOException, ServletException {
        StringBuffer url = new StringBuffer();

        url.append(request.getHeader(SquidMap.originatingUrlHeaderName));
        url.append(request.getRequestURI());
       
        log(ILogService.DEBUG, "forwarding to: " + url.toString());

        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(url.toString());
        forwardHeaders(request, method);
        client.executeMethod(method);
        response.getOutputStream().write(method.getResponseBody());
    }

    private void forwardHeaders(HttpServletRequest request, HttpMethod method) {
        Enumeration en = request.getHeaderNames();
        String name;
        String value;
        while (en.hasMoreElements()) {
            name = (String)en.nextElement();
            value = request.getHeader(name);
            log(ILogService.DEBUG, "adding header: " + name + "/" + value);
            method.setRequestHeader(new Header(name, value));
        }
    }

    private static void log(LogLevel level, String msg) {
        log(level, msg, "");
    }

    private static void log(LogLevel level, String msg, String key) {
        if (logService == null) return;
        StringBuffer sb = new StringBuffer();
        sb.append("SquidMapServlet: ").append(msg).append(": ");
        sb.append("(key: ").append(key.toString()).append(")");
        logService.log(level, sb.toString());
    }
}
