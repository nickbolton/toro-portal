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
package net.unicon.resource.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;

public class ErrorUtils {

    private static Log log = LogFactory.getLog(ErrorUtils.class);

    public static void swallow(Throwable t) {
        net.unicon.resource.util.ErrorUtils.swallow(t.getMessage(), t);
    }

    /**
     * Log an exception at error level.
     * @param s exception message
     * @param t throwable
     * @deprecated just use Commons Logging.
     */
    @Deprecated
    public static void swallow(String s, Throwable t) {
        log.error("Swallowed Exception: " + s + "\n" + net.unicon.resource.util.ErrorUtils.stackTrace(t));
    }

    public static String stackTrace(Throwable t) {
        Writer w = new StringWriter(2000);
        t.printStackTrace(new PrintWriter(w));
        return w.toString();
    }

    public static String errorString(Throwable t) {
        return t.getMessage() + ":\n" + net.unicon.resource.util.ErrorUtils.stackTrace(t);
    }

    public static String currentStack() {
        String answer = "";

        try {
            throw new Exception("Trace of Current Stack");
        } catch (Exception e) {
            answer = net.unicon.resource.util.ErrorUtils.errorString(e);
        }

        return answer;
    }
}
