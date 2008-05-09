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
package net.unicon.portal.channels;

import org.xml.sax.ContentHandler;
import org.w3c.dom.*;
import java.util.*;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import org.jasig.portal.*;
import org.jasig.portal.utils.*;
import org.jasig.portal.services.LogService;
import org.jasig.portal.security.IPerson;

import net.unicon.academus.domain.lms.*;
import net.unicon.portal.common.*;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.permissions.PermissionsException;
import net.unicon.portal.util.RenderingUtil;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.portal.common.properties.*;
import net.unicon.portal.common.cdm.*;
import net.unicon.portal.domain.ChannelClass;

public class BaseSubChannel extends AcademusMultithreadedChannel
implements IMultithreadedCacheable {

    public void setStaticData (ChannelStaticData sd, String upId)
    throws PortalException {
        super.setStaticData(sd, upId);
    }
    public void receiveEvent (PortalEvent event, String upId) {
        super.receiveEvent(event, upId);
    }
    private String getKey(String upId) {
        return upId;
    }
    protected String getClassHandle() {
        return getClass().toString().substring(6);
    }
    public IPermissions getPermissions(String upId) throws PortalException {
        return ChannelDataManager.getPermissions(upId);
    }
    // This method will add reload parameters to the parent channel
    // (SuperChannel) to forward along its sub channels
    public void addReloadParameter(String upId, String param, String value) {
        String parentId = SubChannelFactory.getParentUID(upId);
        Hashtable params = (Hashtable)getChannelAttribute(parentId,
        SuperChannel.reloadParamKey);
        String[] values = new String[1];
        values[0] = value;
        params.put(param, values);
    }
    public void setParentChannelDirty(String upId, boolean dirtyValue)
    throws PortalException {
        String parentId = SubChannelFactory.getParentUID(upId);
        if (parentId == null) return;
        super.setChannelDirty(parentId, dirtyValue);
    }
    // This method gives a channel the ability to set a channel dirty
    // within the user scope.
    // It takes the parentUID and target channelHandle and converts them
    // into the target UID
    public void setChannelDirty(String parentUID, String channelHandle, boolean dirtyValue)
    throws PortalException {
        this.setChannelDirty(SubChannelFactory.convertChannelHandleToSubUID(
        parentUID, channelHandle), dirtyValue);
    }
    public void setChannelDirty(String channelUID, boolean dirtyValue)
    throws PortalException {
        if (ChannelDataManager.uidExists(channelUID)) {
            super.setChannelDirty(channelUID, dirtyValue);
            this.setParentChannelDirty(channelUID, dirtyValue);
        }
    }
    // used to set a channel dirty without setting its parent dirty
    public void setSubChannelDirty(String upId, String channelHandle,
    boolean dirtyValue)
    throws PortalException {
        super.setChannelDirty(SubChannelFactory.convertChannelHandleToSubUID(
        upId, channelHandle), dirtyValue);
    }
    // used to set a channel dirty without setting its parent dirty
    public void setSubChannelDirty(String channelHandle, boolean dirtyValue)
    throws PortalException {
        super.setChannelDirty(channelHandle, dirtyValue);
    }
    public Map getActivities(String upId) throws PortalException {
        try {
            IPermissions p = getPermissions(upId);
            if (p == null) return null;
            return p.getEnumeratedActivities(getDomainUser(upId).getUsername());
        } catch (PermissionsException pe) {
            throw new PortalException(pe);
        } catch (IllegalArgumentException e) {
            throw new PortalException(e);
        }
    }
    public ChannelCacheKey generateKey(String upId) {
        ChannelCacheKey key = new ChannelCacheKey();
        key.setKey(getKey(upId));
        key.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
        key.setKeyValidity(null);
        return key;
    }
    public boolean isCacheValid(Object validity, String upId) {
        if (isDirty(upId)) {
            return false;
        }
        // Are they working in this Channel?
        // If so, invalidate the cache
        String targetChannel = getChannelTargetId(upId);
        if (targetChannel != null && !"".equals(targetChannel) &&
            getChannelSubscribeId(upId).indexOf(getChannelTargetId(upId)) >= 0) {
            return false;
        }
        return true;
    }
    public void setupXSLParameters(String upId) throws PortalException {
        super.setupXSLParameters(upId);
        Hashtable ht = getXSLParameters(upId);
        ht.put("workerActionURL",
            getRuntimeData(upId).getParameter("workerActionURL"));
        ht.put("baseActionURL",
            getRuntimeData(upId).getBaseActionURL());
        ht.put("skin", getRuntimeData(upId).
        getParameter("skin"));
        Map privMap = getActivities(upId);
        if (privMap != null) {
            ht.putAll(privMap);
        }
    }
    public String getChannelTargetId(String upId) {
        String target = getRuntimeData(upId).getParameter(
            SuperChannel.SC_TARGET_HANDLE);
        if (target == null) return "";
        return target;
    }

    public String getChannelURL(String upId, ChannelClass cc)
    throws PortalException {
        PortalControlStructures pcs = getPortalControlStructures(upId);
        BrowserInfo bi = getRuntimeData(upId).getBrowserInfo();
        return RenderingUtil.setupUPURL(pcs.getHttpServletRequest(),
            SubChannelFactory.getParentUID(upId), cc, bi).getUPFile();
    }

    public void buildXML(String upId) throws Exception { }
}
