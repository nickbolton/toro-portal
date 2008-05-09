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

import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.ThemeStylesheetDescription;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.node.UserLayoutChannelDescription;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.LogService;

/**
 * This provides a utility for accessing various objects relating to
 * <code>PortalControlStructures</code>.
 */
public final class PortalControlStructuresUtil {

    /**
     * This will retrieve the <code>UserPreferences</code> that is
     * contained with a <code>PortalControlStructures</code> object.
     * @param pcs The <code>PortalControlStructures</code> from which to
     * retrieve the <code>UserPreferences</code> object.
     * @return The desired <code>UserPreferences</code> object.
     * @throws PortalException
     */
    public static UserPreferences getUserPreferences(
        PortalControlStructures pcs)
    throws PortalException {
        return pcs.getUserPreferencesManager().getUserPreferences();
    }

    /**
     * This will lookup a channel publish id from a channel subscribe id.
     * @param pcs The <code>PortalControlStructures</code> source.
     * @return The desired publish id.
     * @throws PortalException
     */
    public static String getChannelPublishId(PortalControlStructures pcs,
        String channelSubscribeId)
    throws PortalException {
        IUserLayoutManager ulm = pcs.getUserPreferencesManager().
                getUserLayoutManager();
        IUserLayoutNodeDescription node = null;

        // catching the situation where the user doesn't have the
        // given subscribe ID in their layout.  This is possible
        // when a session times out and the guest user is trying to
        // access stale links. In this case, return a null
        // and let the client handle it appropriately.
        try {
            node = ulm.getNode(channelSubscribeId);
        } catch (PortalException pe) {
            LogService.log(LogService.INFO, "PortalControlStructuresUtil.getChannelPublishId: " +
                "No node found in layout(" + ulm.getLayoutId() + ") with subId: " +
                channelSubscribeId);
            return null;
        }
        

        if (!(node instanceof UserLayoutChannelDescription)) {
            StringBuffer sb = new StringBuffer();
            sb.append("No UserLayoutChannelDescription exists for: ");
            sb.append(channelSubscribeId);
            throw new PortalException(sb.toString());
        }
        return ((UserLayoutChannelDescription)node).getChannelPublishId();
    }

    public static IPerson getPerson(PortalControlStructures pcs)
    throws PortalException {
        return pcs.getUserPreferencesManager().getPerson();
    }

    public static ThemeStylesheetDescription getThemeStylesheetDescription(
        PortalControlStructures pcs)
    throws PortalException {
        try {
            return pcs.getUserPreferencesManager().
                getThemeStylesheetDescription();
        } catch (Exception e) {
            throw new PortalException(e);
        }
    }

    public static ThemeStylesheetUserPreferences
    getThemeStylesheetUserPreferences(PortalControlStructures pcs)
    throws PortalException {
        return getUserPreferences(pcs).getThemeStylesheetUserPreferences();
    }
}
