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

import java.util.*;
import org.jasig.portal.services.LogService;

import net.unicon.academus.common.properties.*;
import net.unicon.academus.domain.*;
import net.unicon.sdk.properties.UniconPropertiesFactory;

public final class DefaultRoleType {
    /* Static Members */
    public static final DefaultRoleType SYSTEM = 
        new DefaultRoleType("System", 1);
    public static final DefaultRoleType OFFERING = 
        new DefaultRoleType("Offering", 2);

    /* Instance Members */
    private String typeStr = null;
    private int typeInt = 0;
    private List labels = null;

    private DefaultRoleType(String typeStr, int typeInt) { 
        this.typeStr = typeStr;
        this.typeInt = typeInt;
        try {
            this.labels = getRoleLabels();
        } catch (Exception e) {
            LogService.log(LogService.ERROR, "DefaultRoleType: " + 
                e.getMessage());
        }
    }

    public static DefaultRoleType getDefaultRoleType(String defaultRoleType)
    throws Exception {
        try {
            if (SYSTEM.toString().equalsIgnoreCase(defaultRoleType)) {
                return SYSTEM;
            }
            if (OFFERING.toString().equalsIgnoreCase(defaultRoleType)) {
                return OFFERING;
            }
            throw new ItemNotFoundException(
                "Could not find Default Role Type: " + defaultRoleType);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private List getRoleLabels() throws Exception {
        List roleLabels = new ArrayList();
        
        String roles = UniconPropertiesFactory.getManager(
            AcademusPropertiesType.LMS).getProperty(
                "net.unicon.academus.domain.lms.Role.defaultRoles");

        StringTokenizer st = new StringTokenizer(roles, ",");

        while (st.hasMoreTokens()) {
            String role = (String)st.nextToken();
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
                if (roleType == this.typeInt) {
                    roleLabels.add(label);
                }
            } catch (NumberFormatException nfe) {
                StringBuffer sb = new StringBuffer(64);
                sb.append("Role needs to be of the form \"type.label\". ");
                sb.append("This type is invalid: ").append(roleTypeStr);
                throw new Exception(sb.toString());
            }
        } // end while loop

    return roleLabels;
}

/**
 * Returns true if role type is a default role type.
 * @param label - label of role.
 * @return <code>boolean</code> 
 */
public boolean isDefault(String label) {
    return labels.contains(label);
}

/**
 * Provides the type for this default role type.
 * @return the default role type as a string.
 */
public String toString() {
    return typeStr;
}

/**
 * Provides the type for this default role type.
 * @return the default role type as an int.
 */
public int toInt() {
    return typeInt;
}

} // end DefaultRoleType class
