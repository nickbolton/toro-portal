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

import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.permissions.PermissionsException;
import net.unicon.portal.permissions.PermissionsService;

import org.jasig.portal.AuthorizationException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.AuthorizationService;

import com.interactivebusiness.portal.VersionResolver;

public final class PermissionsUtil {

    public static IPermissions getPermissions(IGroup group, ChannelClass cc,
        IPerson person)
    throws PermissionsException {
        IPermissions p = null;

        if (group != null) {
            p = PermissionsService.instance().getPermissions(cc, group);
        } else {
            IAuthorizationPrincipal principal = VersionResolver.getInstance().
                getPrincipalByPortalVersions(person);
            p = PermissionsService.instance().getPermissions(
                cc, principal);
        }
        return p;
    }

    public static boolean isPortalAdministrator(IPerson person)
    throws PermissionsException {
        return isPortalAdministrator(VersionResolver.getInstance().
            getPrincipalByPortalVersions(person));
    }

    public static boolean isPortalAdministrator(
        IAuthorizationPrincipal principal)
    throws PermissionsException {
        try {
            IEntityGroup admin = GroupService.getDistinguishedGroup(
                IGroupConstants.PORTAL_ADMINISTRATORS);
            IGroupMember currUser = AuthorizationService.instance().
                getGroupMember(principal);
            return admin.deepContains(currUser);
        } catch (GroupsException ge) {
            throw new PermissionsException(ge);
        } catch (AuthorizationException ae) {
            throw new PermissionsException(ae);
        }
    }
}
