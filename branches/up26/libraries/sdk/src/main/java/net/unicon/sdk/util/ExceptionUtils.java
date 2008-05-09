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
package net.unicon.sdk.util;

import java.io.StringWriter;
import java.io.PrintWriter;

import net.unicon.sdk.log.ILogService;
import net.unicon.sdk.log.LogServiceFactory;
import net.unicon.sdk.log.LogLevel;

public final class ExceptionUtils {
    private static ILogService __logger;

    static {
        try {
            __logger = LogServiceFactory.instance();
        }
        catch (Throwable t) {
            System.err.println("Exception getting ILogService: " + t.getClass().getName());
            __logger = new ILogService() {
                public void log(LogLevel logLevel, String message) {}
                public void log(LogLevel logLevel, Throwable ex) {}
                public void log(LogLevel logLevel, String message, Throwable ex) {}
            };
        }
    }

    public static String toXML(Throwable t) {
        StringBuffer xmlBuffer = new StringBuffer();

        return __toXML(t, xmlBuffer);
    }

    private static String __toXML(Throwable t, StringBuffer sb) {
        if (sb == null) return null;
        if (t == null) return sb.toString();

        boolean isCheckedException = false;

        if (t instanceof java.lang.Exception && !(t instanceof java.lang.RuntimeException)) {
            isCheckedException = true;
        }
        Throwable cause = t.getCause();
        String message  = t.getMessage();
        StackTraceElement[] stackFrames = t.getStackTrace();
        StackTraceElement thisFrame = null;
        String tClassName = "";
        String tFileName  = "";
        int tLineNumber   = 1;
        String tMethodName= "";
        String tDesc      = "";
        if (stackFrames != null && stackFrames.length > 0) {
            thisFrame   = stackFrames[0];
            tClassName  = thisFrame.getClassName();
            tFileName   = thisFrame.getFileName();
            tLineNumber = thisFrame.getLineNumber();
            tMethodName = thisFrame.getMethodName();
            tDesc       = thisFrame.toString();
        }

        // create the document fragment
        sb.append("<throwable classname=\"");
        sb.append(t.getClass().getName());
        sb.append("\" isCheckedException=\"");
        sb.append( ((isCheckedException) ? "yes" : "no"));
        sb.append("\">\n\t<message>");
        sb.append( ((message == null) ? "" : message) ); // XXX
        sb.append("</message>\n\t<stackframe>\n\t\t<throwableclass>");
        sb.append(tClassName);
        sb.append("</throwableclass>\n\t\t<throwablefile>");
        sb.append(tFileName);
        sb.append("</throwablefile>\n\t\t<throwableline>" + tLineNumber);
        sb.append("</throwableline>\n\t\t<throwablemethod>");
        sb.append(tMethodName);
        sb.append("</throwablemethod>\n\t\t<description>\n\t\t");
        sb.append(tDesc);
        sb.append("\n\t\t</description>\n\t</stackframe>\n");

        // recursive call to process nested throwable
        __toXML(cause, sb);
        // when back, close this throwable
        sb.append("</throwable>\n");
        return sb.toString();
    }

    public static String getExceptionMessage(Exception e) {
        String retStr = null;
        if (e == null || e.getMessage() == null) {
            return "";
        }
        int pos = e.getMessage().indexOf(":");
        if (pos >= 0) {
            retStr = e.getMessage().substring(pos + 1);
        } else {
            retStr = e.getMessage();
        }
        return StringUtils.replaceSpecialCharsOfString(retStr);
    }
    public static void logCause(Throwable t) {
        if (t == null) return;
        //LogService.instance().log(LogService.ERROR, getCause(t));
        logCause(t.getCause());
    }
    public static String getCause(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        t.printStackTrace(writer);
        return sw.toString();
    }
    public static void logTrace(Throwable t) {
        if (t == null) return;
        //LogService.instance().log(LogService.ERROR, getTrace(t));
    }
    public static String getTrace(Throwable t) {
        StringBuffer sb = new StringBuffer();
        StackTraceElement[] elements = t.getStackTrace();
        for (int i = 0; i < elements.length; i++) {
            sb.append(elements[i].toString());
        }
        return sb.toString();
    }
    private ExceptionUtils() { }

    public static void main(String[] args) {
        // test toXML
        try {
            String xmlVal = args[11111];
        }
        catch (Throwable t) {
            System.out.println(toXML(new Exception("E1", new RuntimeException("RT1", t))));
        }
        System.exit(0);
    }
}
