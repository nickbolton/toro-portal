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

package net.unicon.academus.apps.briefcase.migrate;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.dom4j.Element;

import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;

public final class UserMatcher1_3 implements IUserMatcher {

    // Instance Members.
    private final String driver;
    private final String url;
    private final String user;
    private final String pswd;
    private Connection conn;
    private PreparedStatement pstmt;

    /*
     * Public API.
     */

    public static IUserMatcher parse(Element e) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("user-matcher")) {
            String msg = "Argument 'e [Element]' must be a <user-matcher> "
                                                            + "element.";
            throw new IllegalArgumentException(msg);
        }

        // Driver.
        Element d = (Element) e.selectSingleNode("jdbc-driver");
        if (d == null) {
            String msg = "Element <user-matcher> is missing required child "
                                            + "element <jdbc-driver>.";
            throw new IllegalArgumentException(msg);
        }
        String driver = d.getText();

        // Url.
        Element u = (Element) e.selectSingleNode("jdbc-url");
        if (u == null) {
            String msg = "Element <user-matcher> is missing required child "
                                            + "element <jdbc-driver>.";
            throw new IllegalArgumentException(msg);
        }
        String url = u.getText();

        // User.
        Element r = (Element) e.selectSingleNode("jdbc-user");
        if (r == null) {
            String msg = "Element <user-matcher> is missing required child "
                                            + "element <jdbc-user>.";
            throw new IllegalArgumentException(msg);
        }
        String user = r.getText();

        // Pswd.
        Element p = (Element) e.selectSingleNode("jdbc-pswd");
        if (p == null) {
            String msg = "Element <user-matcher> is missing required child "
                                            + "element <jdbc-pswd>.";
            throw new IllegalArgumentException(msg);
        }
        String pswd = p.getText();

        return new UserMatcher1_3(driver, url, user, pswd);

    }

    public void init() {

        try {

            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, pswd);

            String sql = "SELECT user_name FROM up_user WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);

        } catch (Throwable t) {
            String msg = "UserMatcher1_3 failed to establish database "
                                                    + "resources.";
            throw new RuntimeException(msg, t);
        }

    }

    public void destroy() {

        try {
            pstmt.close();
            conn.close();
        } catch (Throwable t) {
            String msg = "UserMatcher1_3 failed to release database resources.";
            throw new RuntimeException(msg, t);
        }

    }

    public Identity matchIdentity(File f) {

        // Assertions.
        if (f == null) {
            String msg = "Argument 'f [File]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Identity rslt = null;

        try {

            int id = Integer.parseInt(f.getName());

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                rslt = new Identity(rs.getString(1), IdentityType.USER);
            }

        } catch (Throwable t) {
            String msg = "UserMatcher1_3 encountered an error in seeking the "
                                    + "owner of the specified directory:  "
                                    + f.getPath();
            throw new RuntimeException(msg, t);
        }

        return rslt;

    }

    /*
     * Implementation.
     */

    private UserMatcher1_3(String driver, String url, String user,
                                                String pswd) {

        // Assertions.
        if (driver == null) {
            String msg = "Argument 'driver' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (url == null) {
            String msg = "Argument 'url' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (user == null) {
            String msg = "Argument 'user' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (pswd == null) {
            String msg = "Argument 'pswd' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.pswd = pswd;
        this.conn = null;
        this.pstmt = null;

    }

}