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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class UniconErrorUtils {
    public static void swallow(Throwable t) {
        swallow(t.getMessage(), t);
    }

    public static void swallow(String s, Throwable t) {
        System.out.println("<<<<<<<<<<<<<<\nSwallowed Exception: " + s + "\n" + stackTrace(t) + ">>>>>>>>>>>>\n");
    }

    public static String stackTrace(Throwable t) {
        Writer w = new StringWriter(2000);
        t.printStackTrace(new PrintWriter(w));
        return w.toString();
    }

    public static String errorString(Throwable t) {
        return t.getMessage() + ":\n" + stackTrace(t);
    }

    public static String currentStack() {
        String answer = "";

        try {
            throw new Exception("Trace of Current Stack");
        } catch (Exception e) {
            answer = errorString(e);
        }

        return answer;
    }
}
