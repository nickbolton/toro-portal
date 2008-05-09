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
package net.unicon.portal.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.unicon.academus.common.IdentityStore;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;

import org.jasig.portal.RDBMServices;
import org.jasig.portal.UserIdentityStoreFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;

public class PortalIdentityStoreImpl implements IdentityStore {

    static String uidKey   = UniconPropertiesFactory.getManager(
        PortalPropertiesType.LMS).getProperty(
            "org.jasig.portal.security.uidKey");
    static String templateNameKey = UniconPropertiesFactory.getManager(
        PortalPropertiesType.LMS).getProperty(
            "org.jasig.portal.security.templateNameKey");

    public long getUserId(String username, 
                          String templateName, 
                          boolean createTemplateData)
    throws Exception {
        IPerson person = new PersonImpl();
        person.setAttribute(uidKey, username);
        if (templateName != null && !"".equals(templateName)) {
            person.setAttribute(templateNameKey, templateName);
        }
        return UserIdentityStoreFactory.getUserIdentityStoreImpl().getPortalUID(person, createTemplateData);
    }

    public boolean userExists(String username) throws Exception {
        boolean exists = false;
        Connection con = null;
        try {
            con = RDBMServices.getConnection();
            PreparedStatement pstmt = null;
                
            try {
                String query = "SELECT USER_ID FROM UP_USER WHERE USER_NAME=?";
                    
                pstmt = con.prepareStatement(query);
                pstmt.setString(1, username);
                    
                ResultSet rs = null;
                try {
                    rs = pstmt.executeQuery();
                    exists = rs.next();
                } finally {
                    try { rs.close(); } catch (Exception e) { e.printStackTrace(); }
                }
            } finally {
                try { pstmt.close(); } catch (Exception e) { e.printStackTrace(); }
            }
        } finally {
            try { RDBMServices.releaseConnection(con); } catch (Exception e) { e.printStackTrace(); }
        }
             
        return exists;
    }
}

