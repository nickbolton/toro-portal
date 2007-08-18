//**********************************************************************
//
//  File:            ListDBConnections.java
//
//  Copyright:       (c) 2001 UNICON, Inc. All Rights Reserved
//
//  This source code is the confidential and proprietary information
//  of UNICON, Inc.  No part of this work may be modified or used
//  without the prior written consent of UNICON, Inc.
//
//**********************************************************************

package net.unicon.db;

import net.unicon.util.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.http.*;
import javax.servlet.*;

/**
 *
 *
 * @author Unicon, Inc.
 * @version ?? */
public class ListDBConnections extends HttpServlet
{
    private String form = "<form method=\"post\"><b>Pool Name:</b> <input type=\"TEXT\" name=\"POOL\"><br><input type=\"SUBMIT\" name=\"Submit\" value=\"Submit\"></form>";

    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        res.setContentType("text/html; charset=UTF-8");

        PrintWriter out = new PrintWriter(
                          new OutputStreamWriter(res.getOutputStream(),"UTF8")
                                          ,true);

        out.println("<html><head><title>DB Connections</title></head>");
        out.println("<body>");

        String poolName = req.getParameter("POOL");

        if ((poolName == null) || (poolName.length() == 0))
        {
            out.println("Must Specify Pool Name");
            out.println(form);
            out.println("</body></html>\n");
            return;
        }

        out.println("<ul>");

        DBConnectionPoolManager mgr =
            DBConnectionPoolManager.getDBConnectionPoolManager();

        DBConnectionPool pool = mgr.getDBConnectionPool(poolName);

        List statusList = pool.getResourceStatus();

        for(int x=0; x < statusList.size(); x++)
        {
            out.print("  <li>");
            out.print(statusList.get(x).toString());
            out.println("</li>");
        }

        out.println("</ul>");
        out.println("</body></html>\n");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        doGet(req,res);
    }
}

