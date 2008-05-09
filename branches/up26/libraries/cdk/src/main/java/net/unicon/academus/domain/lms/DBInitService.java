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
package net.unicon.academus.domain.lms;

import javax.naming.Name;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import net.unicon.sdk.FactoryCreateException;
import net.unicon.academus.common.properties.*;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.portal.domain.*;
import net.unicon.portal.permissions.Activity;
import net.unicon.portal.permissions.PermissionsService;
import net.unicon.portal.groups.UniconGroupService;
import net.unicon.portal.groups.UniconGroupServiceFactory;
import net.unicon.portal.groups.GroupFactory;
import net.unicon.portal.groups.IGroup;
import net.unicon.sdk.properties.UniconPropertiesFactory;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import org.jasig.portal.RDBMServices;

import net.unicon.portal.util.db.AcademusDBUtil;

public final class DBInitService {
    private static final DBInitService instance = new DBInitService();
    protected DBInitService() {
        try {

            // allocate a connection just to load the db pool
            Connection conn = AcademusDBUtil.getDBConnection();
            AcademusDBUtil.releaseDBConnection(conn);
            
            // echo software fingerprint
            System.out.println("VERSION: " + UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getProperty("net.unicon.academus.version"));
            // echo JDBC info
            System.out.println("Academus JDBC_DRIVER: " + AcademusDBUtil.getJdbcDriver("Academus"));
            System.out.println("Academus JDBC_URL: " + AcademusDBUtil.getJdbcUrl("Academus"));
            System.out.println("Academus JDBC_LOGIN: " + AcademusDBUtil.getJdbcUser("Academus"));

            loadRoles();
            //loadOfferings();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadOfferings()
    throws ItemNotFoundException, OperationFailedException,
    IllegalArgumentException {
        boolean isCaching = UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getPropertyAsBoolean("net.unicon.academus.domain.lms.isCaching");
        if (!isCaching) return;
        System.out.print("Loading Offerings...");
        // SQL.
        StringBuffer sql = new StringBuffer();
        sql.append("select offering_id from offering");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        long offeringId = 0;
        try {
            // Persist the Permissions
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.print(".");
                offeringId = rs.getLong("offering_id");
                OfferingFactory.getOffering(offeringId);
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation loadOfferings() failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            try {
                if (rs != null) rs.close();
                rs = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                AcademusDBUtil.safeReleaseDBConnection(conn);
                conn = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Done.");
    }
    private static void loadRoles()
    throws ItemNotFoundException, OperationFailedException {
        System.out.print("Loading Roles...");
        // SQL.
        StringBuffer sql = new StringBuffer();
        sql.append("select role_id ");
        sql.append("from role where role_id < 100");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        long roleId;

        // now load all roles that exist
        try {
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.print(".");
                roleId = rs.getLong("role_id");
                Role role = RoleFactory.getRole(roleId);
            }
        } catch (Exception e) {
        e.printStackTrace();
            StringBuffer msg = new StringBuffer();
            msg.append("Operation loadRoles() failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            try {
                if (rs != null) rs.close();
                rs = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                AcademusDBUtil.safeReleaseDBConnection(conn);
                conn = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Done.");
    }
}
