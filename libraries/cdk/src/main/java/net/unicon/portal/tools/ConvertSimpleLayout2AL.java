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
package net.unicon.portal.tools;

import org.jasig.portal.tools.*;
import org.jasig.portal.RDBMServices;
import net.unicon.portal.util.db.AcademusDBUtil;

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.sql.DataSource;
import javax.naming.Name;
import javax.naming.CompositeName;

public final class ConvertSimpleLayout2AL {

    public static void main(String[] args) throws Exception {

        if (args.length <= 0) {
            System.out.println("Usage ConvertSimpleLayout2AL <all | list of userIDs>");
            System.exit(0);
        }

        String[] users;
        if ("all".equalsIgnoreCase(args[0])) {
            users = getAllUsers();
        } else {
            users = args;
        }

        convertLayouts(users);
        System.out.println("Done.");
    }

    private static String[] getAllUsers() throws Exception {
        StringBuffer sql = new StringBuffer();
        sql.append("Select user_id from up_user");
        List users = new ArrayList();
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = RDBMServices.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(rs.getString("user_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            AcademusDBUtil.safeClosePreparedStatement(pstmt);
            AcademusDBUtil.safeReleaseDBConnection(conn);
            rs = null;
            pstmt = null;
            conn = null;
        }
        return (String[])users.toArray(new String[users.size()]);
    }

    private static void convertLayouts(String[] users) {
        String[] args = new String[2];
        args[1] = "1";  // profileId 1

        for (int i=0; users!=null && i<users.length; i++) {
            args[0] = users[i];
            try {
                SimpleLayout2ALIM.main(args);
            } catch (Exception e) {
                System.err.println("Failed to convert layout for user: " + users[i] + " - reason: " + e.getMessage());
            }
        }
    }
}
