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
package net.unicon.resource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ResourcesServlet extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html; charset=UTF-8");

        PrintWriter out = new PrintWriter(new OutputStreamWriter(res.getOutputStream(),"UTF8") ,true);

        out.println("<html><head><title>DB Connections</title></head>");
        out.println("<body>");

        Map<String, List> poolMap = Resources.getAllocationInfo();
        List<String> poolNames = new ArrayList<String>(poolMap.keySet());
        Collections.sort(poolNames);
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < poolNames.size(); i++) {
            String name = poolNames.get(i);
            List infoRows = poolMap.get(name);

            out.println("<h2>" + name + "</h2>");
            out.println("<table>\n<tr><td>Resource</td><td>Requester</td><td>Age (milliseconds)</td></tr>");
            for (int j = 0; j < infoRows.size(); j++) {
                List info = (List) infoRows.get(j);
                Object resource = info.get(0);
                Object requester = info.get(1);
                Long allocationTime = (Long) info.get(2);
                out.println("<tr><td>" + resource + "</td><td>" + requester + "</td><td>" + (currentTime - allocationTime.longValue()) + "</td></tr>");
            }
            out.println("</table><br><br>");
        }

        out.println("</body></html>\n");
    }
}

