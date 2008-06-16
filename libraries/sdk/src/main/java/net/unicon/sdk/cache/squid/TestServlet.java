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
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
 
public class TestServlet extends HttpServlet {

    private static SquidMap squidMap = new SquidMap();

    public class TestClass implements java.io.Serializable {
        public String one;
        public String two;

        public String toString() {
            return one + " " + two;
        }
    }
 
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String key = "foo";

        String value = "01234567890123456789012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789012345678901234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678901234567890123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890123456789012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678901234567890123456789012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890012345678900";

        PrintWriter out = response.getWriter();
        out.print("<html><body><pre>");
        testGet(key, out);
        testPut(key, value, out);
        testGet(key, out);
        testGet(key, out);
        testRemove(key, out);
        testGet(key, out);
        out.print("</pre></body></html>");
    }

    private void testGet(Object key, PrintWriter out) {
        long time = System.currentTimeMillis();
        Object obj = squidMap.get(key);
        out.println("get time : " + (System.currentTimeMillis() - time));
        out.println("obj: " + obj);
    }

    private void testRemove(Object key, PrintWriter out) {
        long time = System.currentTimeMillis();
        Object obj = squidMap.remove(key);
        out.println("remove time : " + (System.currentTimeMillis() - time));
        out.println("obj: " + obj);
    }

    private void testPut(Object key, Object value, PrintWriter out) {
        long time = System.currentTimeMillis();
        Object obj = squidMap.put(key, value);
        out.println("put time : " + (System.currentTimeMillis() - time));
        out.println("obj: " + obj);
    }

    private void testGetContainsKey(Map map, Object key) {
        long i;
        boolean found;
        double timeSum=0.0;
        long time;
        long iterations=100000;

        for (i=0; i<iterations; i++) {
            time = System.currentTimeMillis();
            map.get(key);
            timeSum+=(System.currentTimeMillis() - time);
        }
        System.out.println("Mean get time: " + (timeSum/Double.parseDouble(""+iterations)));

        timeSum=0.0;
        for (i=0; i<iterations; i++) {
            time = System.currentTimeMillis();
            map.containsKey(key);
            timeSum+=(System.currentTimeMillis() - time);
        }
        System.out.println("Mean containsKey time: " + (timeSum/Double.parseDouble(""+iterations)));
    }
}
