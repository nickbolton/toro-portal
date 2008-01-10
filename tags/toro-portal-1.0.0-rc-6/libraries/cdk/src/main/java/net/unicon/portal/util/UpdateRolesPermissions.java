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

//import net.unicon.sdk.properties.*;
import net.unicon.sdk.FactoryCreateException;
import net.unicon.academus.common.properties.*;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.academus.domain.lms.Role;
import net.unicon.academus.domain.lms.RoleFactory;
import net.unicon.academus.producer.IProducer;
import net.unicon.portal.permissions.Activity;
import net.unicon.portal.permissions.ActivityFactory;

import net.unicon.portal.domain.ChannelMode;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.domain.ChannelClassFactory;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.permissions.PermissionsService;
import net.unicon.portal.groups.UniconGroupService;
import net.unicon.portal.groups.UniconGroupServiceFactory;
import net.unicon.portal.groups.GroupFactory;
import net.unicon.portal.groups.IGroup;
import net.unicon.sdk.properties.*;
import net.unicon.portal.util.ChannelDefinitionUtil;
import java.util.Iterator;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.lang.reflect.Method;

import org.jasig.portal.ChannelRegistryManager;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelDefinition;

import net.unicon.sdk.properties.*;
import net.unicon.portal.util.db.AcademusDBUtil;

public final class UpdateRolesPermissions {

    protected UpdateRolesPermissions() throws Exception {
        // echo JDBC info
        System.out.println("Academus JDBC_DRIVER: " + AcademusDBUtil.getJdbcDriver("Academus"));
        System.out.println("Academus JDBC_URL: " + AcademusDBUtil.getJdbcUrl("Academus"));
        System.out.println("Academus JDBC_LOGIN: " + AcademusDBUtil.getJdbcUser("Academus"));
       
        createRoles();
        updateLmsPermissions();
        updatePermissions();
    }

    private static void createRoles()
    throws Exception {
        System.out.print("Creating any missing default roles...");

        String roles = UniconPropertiesFactory.getManager(
            AcademusPropertiesType.LMS).getProperty(
                "net.unicon.academus.domain.lms.Role.defaultRoles");

        StringTokenizer st = new StringTokenizer(roles, ",");

        while (st.hasMoreTokens()) {
            createRole(st.nextToken());
        }
    }

    private static void createRole(String role)
    throws Exception {
        int pos = role.indexOf(".");

        if (pos < 0 || (pos == (role.length()-1))) {
            StringBuffer sb = new StringBuffer(64);
            sb.append("Role needs to be of the form \"type.label\". ");
            sb.append("This role is invalid: ").append(role);
            throw new Exception(sb.toString());
        }

        int roleType = -1;
        String label = role.substring(pos+1);
        String roleTypeStr = role.substring(0,pos);
        try {
            roleType = Integer.parseInt(roleTypeStr);
        } catch (NumberFormatException nfe) {
            StringBuffer sb = new StringBuffer(64);
            sb.append("Role needs to be of the form \"type.label\". ");
            sb.append("This type is invalid: ").append(roleTypeStr);
            throw new Exception(sb.toString());
        }

        long roleId = UniconPropertiesFactory.getManager(
            AcademusPropertiesType.LMS).getPropertyAsLong(
                "net.unicon.academus.domain.lms.Role."+roleType+"."+label);

        try {
            RoleFactory.getRole(roleId);
        } catch (ItemNotFoundException e) {
            System.out.print(".");
            RoleFactory.createRole(roleId, label, roleType, null);
        }
    }

    private static void updatePermissions()
    throws Exception {

        System.out.print("Updating non-lms activities...");
        Element rootEl = ChannelRegistryManager.getChannelRegistry().
            getDocumentElement();

        Method method;
        IPermissions p;
        IGroup[] groups;
        Activity[] activities;
        String chanId;
        String permissionsPath;
        ChannelDefinition cd;
        Element el;
        NodeList nl = rootEl.getElementsByTagName("channel");
        for (int i = 0; i < nl.getLength(); i++) {
            System.out.print(".");
            el = (Element)nl.item(i);
            chanId = el.getAttribute("chanID");
            cd = ChannelRegistryStoreFactory.
                    getChannelRegistryStoreImpl().
                        getChannelDefinition(Integer.parseInt(chanId));
            permissionsPath = ChannelDefinitionUtil.
                getParameter(cd, "permissions");

            // skip channels that dont have permissions manifests defined
            if (permissionsPath == null || "".equals(permissionsPath)) {
                continue;
            }

            activities = ActivityFactory.getActivities(cd);

            // for each group in the system, define default permissions
            groups =
                UniconGroupServiceFactory.getService().fetchAllGroupsArray();

            for (int j=0; groups != null && j<groups.length; j++) {
                p = PermissionsService.instance().getPermissions(cd, groups[j]);
                registerActivities(p, activities, groups[j].getName());
            }
        }

        addCampusAnnouncementsPermissions();
        System.out.println("Done.");
    }

    private static void addCampusAnnouncementsPermissions()
    throws Exception {
        // SQL.
        StringBuffer sql = new StringBuffer();
        String owner = "net.unicon.portal.channels.campusannouncement.CampusAnnouncementChannel";
        String target = "net.unicon.portal.channels.campusannouncement.CampusAnnouncementChannel";
        String activity = "assignAnnouncer";
        String principal = "3.14";

        sql.append("select count(*) AS count_up_permission from up_permission where ");
        sql.append("target = '").append(target).append("' ");
        sql.append("and owner = '").append(owner).append("' ");
        sql.append("and activity = '").append(activity).append("' ");
        sql.append("and principal = '").append(principal).append("'");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        long roleId;

        // update permissions for lms channels
        try {
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            rs = pstmt.executeQuery();            if (rs.next()) {                int count = rs.getInt("count_up_permission");                if(count <= 0) {                 	
                	pstmt.close();                	
                	sql = new StringBuffer();                	sql.append("insert into up_permission values ");                	sql.append("('").append(owner).append("', ");                	sql.append("'").append(principal).append("', ");                	sql.append("'").append(activity).append("', ");                	sql.append("'").append(target).append("'");                	sql.append(",'GRANT', null, null)");                	
                	pstmt = conn.prepareStatement(sql.toString());                	pstmt.execute();                	
                }            }
        } catch (Exception e) {
        	e.printStackTrace();
            throw e;
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
               e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception e) {
               e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            rs = null;
            pstmt = null;
            conn = null;
        }
        System.out.println("Done.");
    }

    private static void updateLmsPermissions()
    throws Exception {
        System.out.print("Updating lms activities...");
        // SQL.
        StringBuffer sql = new StringBuffer();
        sql.append("select role_id ");
        sql.append("from role where role_id < 100");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        long roleId;

        // update permissions for lms channels
        try {
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.print(".");
                roleId = rs.getLong("role_id");
                Role role = RoleFactory.getRole(roleId);
                List channels = new ArrayList(ChannelClassFactory.
                getChannelClasses(role.getChannelMode()));
                channels.addAll(ChannelClassFactory.
                getChannelClasses(ChannelMode.GLOBAL));
                if (role.getChannelMode().toString().equals(
                ChannelMode.ADMIN.toString())) {
                    Iterator itr = ChannelMode.getMiscModes().iterator();
                    while (itr.hasNext()) {
                        channels.addAll(ChannelClassFactory.
                        getChannelClasses((ChannelMode)itr.next()));
                    }
                }
                Iterator itr = channels.iterator();
                while (itr != null && itr.hasNext()) {
                    ChannelClass cc = (ChannelClass)itr.next();
                    IPermissions p = PermissionsService.instance().
                        getPermissions(cc, role.getGroup());
                    registerActivities(p,
                        (Activity[])cc.getActivities().toArray(new Activity[0]),
                        role.getLabel());
                }
            }
        } catch (Exception e) {
        e.printStackTrace();
            throw e;
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
               e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception e) {
               e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            rs = null;
            pstmt = null;
            conn = null;
        }
        System.out.println("Done.");
    }

    private static void registerActivities(IPermissions permissions,
        Activity[] activities, String label)
    throws Exception {
        Activity activity;
        String handle;
        boolean value;

        for (int i=0; activities!=null && i<activities.length; i++) {
            handle = activities[i].getHandle();
            value = activities[i].getDefaultPermissionsSetting(label);
            if (permissions != null) {
                permissions.removeActivity(handle);
                if (value) {
                    permissions.setActivity(handle, value);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            new UpdateRolesPermissions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
