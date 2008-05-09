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
package net.unicon.portal.util;

import javax.servlet.http.HttpServletRequest;

import net.unicon.sdk.util.StringUtils;

import java.util.Map;
import java.util.Enumeration;
import java.util.Iterator;
import org.jasig.portal.*;
import org.jasig.portal.services.LogService;

public class RuntimeDataUtil {
    public static void escapeParameters(ChannelRuntimeData crd) {
        Enumeration e = crd.getParameterNames();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            Object[] values = crd.getObjectParameterValues(key);
            if ((values != null) && (values.length > 0)) {
                if (values instanceof String[]) {
                    /*for (int i = 0; i < values.length; i++) {
                        values[i] = StringUtils.replaceSpecialCharsOfString(
                            (String)values[i]);
                    }*/
                    escapeParameters(values);
                    crd.setParameterValues(key, (String[]) values);
                } else if (values instanceof
                com.oreilly.servlet.multipart.Part[]) {
                    crd.setParameterValues(key,
                    (com.oreilly.servlet.multipart.Part[]) values);
                } else {
                    crd.put(key, values);
                }
            }
        }
    } // end escapeParameters
    public static void escapeParameters(Object[] values) {
        if ((values != null) && (values.length > 0)) {
            if (values instanceof String[]) {
                for (int i = 0; i < values.length; i++) {
                    values[i] = StringUtils.replaceSpecialCharsOfString(
                    (String)values[i]);
                }
            }
        }
    } // end escapeParameters

    public static void logServletRequestParameters(HttpServletRequest req) {
        StringBuffer sb = new StringBuffer();
        String name = null;
        String[] values = null;
        String value = null;

        sb.append("HttpServletRequest parameters...");
        Enumeration e = req.getParameterNames();
        while (e.hasMoreElements()) {
            name = (String)e.nextElement();
            value = req.getParameter(name);
            values = req.getParameterValues(name);

            if (values != null) {
                sb.append("\n").append(name).append(" values: ");
                for (int i=0; i<values.length; i++) {
                    if (i>0) {
                        sb.append(", ");
                    }
                    sb.append(values[i]);
                }
            } else if (value != null) {
                sb.append("\n").append(name).append(": ").append(value);
            }
        }
        LogService.instance().log(LogService.DEBUG, sb.toString());
    }

    protected RuntimeDataUtil() { } // protected default constructor
} // end RuntimeDataUtil class
