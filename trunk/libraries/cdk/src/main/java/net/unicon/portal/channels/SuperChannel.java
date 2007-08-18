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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.academus.domain.lms.OperationFailedException;
import net.unicon.academus.domain.lms.Role;
import net.unicon.academus.domain.lms.RoleFactory;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.producer.IProducer;
import net.unicon.portal.common.AcademusMultithreadedChannel;
import net.unicon.portal.common.SubChannelFactory;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.domain.ChannelClassFactory;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.util.ChannelCacheTable;
import net.unicon.portal.util.ChannelRendererWrapper;
import net.unicon.portal.util.PermissionsUtil;
import net.unicon.portal.util.PortalControlStructuresUtil;
import net.unicon.portal.util.PortalSAXUtils;
import net.unicon.portal.util.ProducerRenderer;
import net.unicon.portal.util.RenderingUtil;
import net.unicon.portal.util.StatsRecorderWrapper;
import net.unicon.portal.util.TemplateUtils;

import org.jasig.portal.BrowserInfo;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.IMimeResponse;
import org.jasig.portal.IMultithreadedCacheable;
import org.jasig.portal.IMultithreadedMimeResponse;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.services.LogService;
import org.jasig.portal.services.StatsRecorder;
import org.jasig.portal.services.stats.StatsRecorderSettings;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.utils.SoftHashMap;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.interactivebusiness.portal.VersionResolver;

public abstract class SuperChannel extends AcademusMultithreadedChannel
implements IMultithreadedMimeResponse, IMultithreadedCacheable {
    public static final String channelUidKey = "channelUid";
    public static final String CHANNEL_MODE_KEY = "ChannelMode";
    public static final String CONTROLLER_CHANNEL_CONTENT = "controllerChannelContent";
    public static final String parentIdKey = "ParentId";
    public static final String reloadParamKey = "reloadParams";
    public static final String EXTRA_FIELD_SEP =
        UPFileSpec.PORTAL_URL_SEPARATOR;
    public static final String SC_TARGET_HANDLE = "scTargetHandle";
    protected static final String SC_FOCUSED_CHANNEL="focusedChannel";
    protected static final String cacheKey = "CACHE_BUFFERS";
    protected static final int NUM_COLS = 2;


    public SuperChannel() {
        super();
    }
    public void setStaticData (ChannelStaticData sd, String upId)
    throws PortalException {
        super.setStaticData(sd, upId);

        try {
            int publishId = Integer.parseInt(sd.getChannelPublishId());
            ChannelDefinition cd = ChannelRegistryStoreFactory.
                getChannelRegistryStoreImpl().getChannelDefinition(publishId);
            ChannelDataManager.registerChannelUser(sd.getPerson(),
                null, cd, upId);
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
            throw new PortalException(e);
        }

        IAuthorizationPrincipal principal = VersionResolver.getInstance().
            getPrincipalByPortalVersions(sd.getPerson());
        ChannelDataManager.setAuthorizationPrincipal(upId, principal);
    }
    public ChannelCacheKey generateKey(String upId) {
        ChannelCacheKey key = new ChannelCacheKey();
        key.setKey(upId);
        key.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
        key.setKeyValidity(null);
        return key;
    }
    public boolean isCacheValid(Object validity, String upId) {
        return false;
/*
        // invalidate if they are working in the channel
        // (working in a sub-channel of this super channel implies
        // they are working in this super channel)
        String targetSubChannel = getTargetChannelId(upId);
        if (targetSubChannel == null) return true;
        return targetSubChannel.indexOf(getChannelSubscribeId(upId)) < 0;
*/
    }
    public void receiveEvent (PortalEvent event, String upId) {
        // delegate events to sub-channels
        Hashtable userChannelTables = SubChannelFactory.getUserChannelTables();
        Hashtable channelTable = null;
        if (userChannelTables != null) {
            channelTable = (Hashtable)userChannelTables.get(upId);
            if (channelTable != null) {
                Iterator itr = channelTable.values().iterator();
                while (itr != null && itr.hasNext()) {
                    IChannel ch = (IChannel)itr.next();
                    ch.receiveEvent(event);
                }
                userChannelTables.remove(upId);
            }
        }
        if (event.getEventNumber() == PortalEvent.SESSION_DONE) {
            // The user logged out or session timed out, clean up cache
            removeChannelAttribute(upId, cacheKey);
        }
        super.receiveEvent(event, upId);
    }
    // We need to pass runtime data along any sub channel that's requesting
    // a download
    public void setRuntimeData (ChannelRuntimeData rd, String upId)
    throws PortalException {
	/* DEBUG ONLY
	   //net.unicon.common.util.debug.DebugUtil.spillParameters(rd,
	   "SETTING RUNTIME DATA IN SuperChannel");
	*/

        // RuntimeDataUtil.escapeParameters(rd) will escape
        // any characters in form data fields of type string
        // that have an entry in CharacterMappings.xml
        // (located in the properties directory).
        //RuntimeDataUtil.escapeParameters(rd);
        super.setRuntimeData(rd, upId);
        parseExtrasField(upId);
        if (hasDownloadRequest(upId)) {
            try {
                getTargetChannel(upId).setRuntimeData(rd);
            } catch (Exception e) {
                LogService.log(LogService.ERROR, e);
                throw new PortalException(e.toString());
            }
        }
    }

    private void parseExtrasField(String upId)
    throws PortalException {

        PortalControlStructures pcs = getPortalControlStructures(upId);
        Map extras = RenderingUtil.parseExtrasField(
            new UPFileSpec(pcs.getHttpServletRequest()));
      
        String name;
        String value;
        Iterator itr = extras.keySet().iterator();
        while (itr.hasNext()) {
            name = (String)itr.next();
            value = (String)extras.get(name);
            getRuntimeData(upId).setParameter(name, value);
        }
    }

    public void buildXML(String upId) throws Exception {

        // check if a sub channel needs to be reloaded
        String[] channels = getRuntimeData(upId).getParameterValues("reloadChannel");
        for (int i = 0; channels != null && i < channels.length; i++) {
            super.setChannelDirty(
            SubChannelFactory.getSubChannelUID(upId,
            SubChannelFactory.getSubChannelSubscribeId(
            upId, channels[i])), true);
        }

        determineFocusedChannelState(upId);
        // We use the SuperChannel's dirty flag as follows
        // First we clear it. Then if any sub channel sets it,
        // that means they want the super channel to re-render.
        // We only do it once in case a sub-channel keeps setting it
        // and thus creating an infinite loop.
        super.setDirty(upId, false);
        putChannelAttribute(upId, reloadParamKey, new Hashtable());
        setCharacterBuffer(upId, renderState(upId));
        if (super.isDirty(upId)) {
            // we remove the 'command' form parameter so the sub
            // channel that caused us to re-render doesn't perform the
            // same command
            getRuntimeData(upId).remove(SC_TARGET_HANDLE);
            getRuntimeData(upId).remove("command");
            initialize(upId);
            setCharacterBuffer(upId, renderState(upId));
        }
    }
    protected abstract ChannelObjects getControllingChannelObjects(String upId);
    protected abstract ChannelClass getControllingChannelClass();
    protected abstract void initialize(String upId) throws Exception;
    protected abstract List getRenderableChannels(String upId)
    throws Exception;
    public void setupXSLParameters(String upId) throws PortalException {
        super.setupXSLParameters(upId);
        Hashtable ht = getXSLParameters(upId);
        ht.put("skin", getSkin(upId));
    }

    private String getSkin(String upId) throws PortalException {
        try {
            PortalControlStructures pcs = getPortalControlStructures(upId);
            return PortalControlStructuresUtil.
                getThemeStylesheetUserPreferences(pcs).
                getParameterValue("skin");
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
            throw new PortalException(e);
        }
    }

    protected String renderState(String upId) throws Exception {
        
        return renderBuffers(upId, startRendering(upId));
    }
    // IMimeResponseAdapter interface methods
    public String getContentType(String upId) {
        try {
            IChannel ch = getTargetChannel(upId);
            if (ch instanceof IMimeResponse) {
                IMimeResponse mr = (IMimeResponse)ch;
                return mr.getContentType();
            }
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
        }
        return "";
    }
    public InputStream getInputStream(String upId) throws IOException {
        IChannel ch = null;
        try {
            ch = getTargetChannel(upId);
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
            ch = null;
        }
        if (ch != null && ch instanceof IMimeResponse) {
            IMimeResponse mr = (IMimeResponse)ch;
            return mr.getInputStream();
        }
        return null;
    }
    public void downloadData(OutputStream out, String upId) throws IOException {
        IChannel ch = null;
        try {
            ch = getTargetChannel(upId);
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
            ch = null;
        }
        if (ch != null && ch instanceof IMimeResponse) {
            IMimeResponse mr = (IMimeResponse)ch;
            mr.downloadData(out);
        }
    }
    public String getName(String upId) {
        try {
            IChannel ch = getTargetChannel(upId);
            if (ch instanceof IMimeResponse) {
                IMimeResponse mr = (IMimeResponse)ch;
                return mr.getName();
            }
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
        }
        return null;
    }
    public Map getHeaders(String upId) {
        try {
            IChannel ch = getTargetChannel(upId);
            if (ch instanceof IMimeResponse) {
                IMimeResponse mr = (IMimeResponse)ch;
                return mr.getHeaders();
            }
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
        }
        return null;
    }
    protected boolean hasDownloadRequest(String upId) {
        String download = getRuntimeData(upId).getParameter("download");
        if (download == null) return false;
        return "true".equalsIgnoreCase(download);
    }
    protected String resolveChannelClass(String subChannelId) {
        int pos = subChannelId.lastIndexOf(
        SubChannelFactory.subChannelIdSeparator);
        if (pos > 0 && pos < (subChannelId.length() - 1)) {
            return subChannelId.substring(pos + 1);
        }
        return "";
    }

    protected List startRendering(String upId)
    throws Exception {
        ChannelObjects channelObjects = null;
        List channels = new ArrayList();

    ChannelObjects controllingChannelObjects = getControllingChannelObjects(upId);
    if (controllingChannelObjects != null) {
        channels.add(controllingChannelObjects);
    }
        channels.addAll(getRenderableChannels(upId));

        // first fully render the controlling channel
        ChannelClass controllingClass = getControllingChannelClass();
        // if there is no controlling class, we have no controller
        if (controllingClass != null) {
            String controllerUID = SubChannelFactory.getSubChannelUID(upId,
                getControllingChannelClass());
            ChannelRuntimeData rd =
                (ChannelRuntimeData)getRuntimeData(upId).clone();
            setSubChannelRuntimeData(rd, upId, getControllingChannelClass());
            IProducer producer = ChannelDataManager.getProducer(controllerUID);
            User user = ChannelDataManager.getDomainUser(upId);
            // if we have a producer, it is the controlling app, else the old
            // pattern is used and the controlling app is the container.
            if (producer != null) {
                ProducerRenderer.ProducerResults results =
                    ProducerRenderer.instance().
                produceContent(controllerUID, rd, user, producer, this);
                putChannelAttribute(upId, CONTROLLER_CHANNEL_CONTENT,
                    results.getContent());
            }
        }

        // record channel targeted event
        recordChannelTargeted(upId);
        
        // For each renderable channel, start the ChannelRenderer threads.
        // But skip the ones that are ICacheable and have valid caches.
        Iterator itr = channels.iterator();
        while (itr != null && itr.hasNext()) {
            channelObjects = (ChannelObjects)itr.next();

            // skip the controlling channel
            if (getControllingChannelClass() ==
                channelObjects.getChannelClass()) continue;

            startRenderingSubChannel(upId, channelObjects, false);
        }
        return channels;
    }

    private void recordChannelTargeted(String upId) throws DomainException {
        String targetChannelId = getTargetChannelId(upId);
        if (targetChannelId == null) return;
        String handle = resolveChannelClass(targetChannelId);
        ChannelClass cc = ChannelClassFactory.getChannelClass(handle);

        StatsRecorderWrapper.getInstance().recordChannelTargeted(targetChannelId,
            cc, getStaticData(upId).getPerson(),
            getPortalControlStructures(upId).getUserPreferencesManager().getCurrentProfile());
    }

    protected void startRenderingSubChannel(String upId,
        ChannelObjects channelObjects,
        boolean controllingChannel)
    throws PortalException {
        ChannelClass cc = channelObjects.getChannelClass();
        String subChannelId =
            SubChannelFactory.getSubChannelSubscribeId(upId, cc);
        String focusedChannel = getFocusedChannel(upId);
        boolean focusedChannelExists = (focusedChannel != null);

        IChannel ch = channelObjects.getIChannel();
        if (ch == null) {
            LogService.log(LogService.ERROR,
                "Null channel found.");
            return;
        }

        boolean isMinimized = false;
        boolean isFocused = subChannelId.equals(focusedChannel);

        // If the channel is focused, ignore the minimized state
        if (isFocused) {
            isMinimized = false;
        } else {
            isMinimized = getSubChannelMinimizedState(upId, cc);
        }

        // We only want to spawn a ChannelRenderer Thread if
        // 1) the channel is not minimized
        // 2) No focused channel exists or if a focused channel
        //    exists, it's this channel
        // 3) or it's the Controlling channel
        if (controllingChannel ||
            (!isMinimized && (isFocused == focusedChannelExists))) {
            ChannelRuntimeData rd =
                (ChannelRuntimeData)getRuntimeData(upId).clone();
            rd.setRenderingAsRoot(rd.isRenderingAsRoot() && isFocused);
            String workingChannel = getWorkingChannelId(upId);

            // only pass the runtime parameters for the working channel or
            // controlling channel
            if (!controllingChannel && (workingChannel == null ||
                !workingChannel.equals(subChannelId))) {
                rd.clear();
            }

            try {
                PortalControlStructures pcs = getPortalControlStructures(upId);
                // set the portal control structures if privileged
                if (ch instanceof IPrivilegedChannel) {
                    ((IPrivilegedChannel)ch).setPortalControlStructures(pcs);
                }
                setSubChannelRuntimeData(rd, upId, cc);
                ChannelRendererWrapper crw =
                    new ChannelRendererWrapper(upId, pcs, cc, ch,
                        pcs.getChannelManager(), cc.getTimeout(), rd,
                        getUPortalUser(upId), true);
                channelObjects.setChannelRendererWrapper(crw);

                
                ChannelCacheTable table =
                    (ChannelCacheTable)pcs.getHttpServletRequest().
                    getSession(false).getAttribute(
                    "net.unicon.portal.channels.cacheTables");
                if (table == null) {
                    table = new ChannelCacheTable();
                    pcs.getHttpServletRequest().getSession(false).setAttribute(
                        "net.unicon.portal.channels.cacheTables", table);
                }
                crw.startRendering(table);
            } catch (PortalException pe) {
                LogService.log(LogService.ERROR, pe);
                throw pe;
            } catch (Exception e) {
                LogService.log(LogService.ERROR, e);
                throw new PortalException(e);
            }
        }
    }

    protected String renderBuffers(String upId, List channels)
    throws Exception {
        //net.unicon.portal.util.Debug.instance().markTime();
        ChannelObjects channelObjects = null;
        ChannelObjects nextChannelObjects = null;
        ChannelClass cc = null;
        ChannelClass ncc = null;
        IChannel ch = null;
        boolean isMinimized = false;
        String focusedChannel = getFocusedChannel(upId);
        boolean focusedChannelExists = (focusedChannel != null);
        boolean isFocused = false;
        AttributesImpl channelAttributes = new AttributesImpl();
        AttributesImpl columnAttributes = new AttributesImpl();
        StringBuffer channelBuf = null;
        HashMap params = new HashMap();
        String subId = null;
        String nextSubId = "";
        String nextChannelTitle = null;
        int numCols = Math.min(NUM_COLS, channels.size());
        StringBuffer[] columns = new StringBuffer[numCols];
        int i = 0;
        for ( ; i < numCols; i++) {
            columns[i] = new StringBuffer();
        }
        //Iterator itr = channels.iterator();
        i = 0;
        int layoutColumn = 0;
        int channelCount = channels.size();
        //while (itr != null && itr.hasNext()) {
        for ( ; i < channelCount; i++) {
            layoutColumn = i % numCols;
            //i++;
            //channelObjects = (ChannelObjects)itr.next();
            channelObjects = (ChannelObjects)channels.get(i);
            cc = channelObjects.getChannelClass();
            ch = channelObjects.getIChannel();
            subId = SubChannelFactory.getSubChannelSubscribeId(upId, cc);
            // nextSubId is used for bookmarking the next channel in line
            // for ADA compliance.
            if (i < (channelCount - 1)) {
                nextChannelObjects = (ChannelObjects)channels.get(i + 1);
                ncc = nextChannelObjects.getChannelClass();
                nextSubId = SubChannelFactory
                .getSubChannelSubscribeId(upId, ncc);
                nextChannelTitle = getChannelTitle(upId, ncc);
            } else {
                nextSubId = subId;
                nextChannelTitle = getChannelTitle(upId, cc);
            }

            // If the channel is focused, ignore the minimized state
            isFocused = subId.equals(focusedChannel);
            if (isFocused) {
                isMinimized = false;
            } else {
                isMinimized = getSubChannelMinimizedState(upId, cc);
            }
            params.put("isBlank", "false");
            params.put("subId", subId);
            params.put("nextSubId", nextSubId);
            params.put("ID", subId);
            params.put("title", getChannelTitle(upId, cc));
            params.put("nextChannelTitle", nextChannelTitle);
            params.put("focused", "" + isFocused);
            params.put("minimized", "" + isMinimized);
            params.put("baseActionURL",
            getRuntimeData(upId).getBaseActionURL());
            /*params.put("mediaPath",
                "media/org/jasig/portal/layout/tab-column/nested-tables");*/
            params.put("skin", getSkin(upId));
            params.put("helpHandle", getChannelHelpHandle(cc));
            channelBuf = new StringBuffer();
            // We only want to fetch the results if
            // 1) the channel is not minimized
            // 2) Not focused channel exists or if a focused channel
            //    exists, it's this channel
            if (!isMinimized && (isFocused == focusedChannelExists)) {

                // the controlling channel's content was saved,
                // pull it from there
                if (getControllingChannelClass() ==
                    channelObjects.getChannelClass()) {
                    channelBuf.append(
                        getChannelAttribute(upId, CONTROLLER_CHANNEL_CONTENT));
                } else {
                    ChannelRendererWrapper crw =
                        channelObjects.getChannelRendererWrapper();
                    Object buffer = crw.getResults();
                    if (buffer instanceof String) {
                        channelBuf.append((String)buffer);
                    } else if (buffer instanceof SAX2BufferImpl) {
                        channelBuf.append(
                            PortalSAXUtils.serializeSAX(getStaticData(upId).getSerializerName(),
                                (SAX2BufferImpl)buffer));
                    } else {
                        StringBuffer msg = new StringBuffer();
                        msg.append("Invalid buffer class: ");
                        msg.append(buffer.getClass().toString());
                        LogService.log(LogService.ERROR,
                            msg.toString());
                        throw new PortalException(msg.toString());
                    }
                }
            }
            params.put("CHANNEL", channelBuf);
            columns[layoutColumn].append(
            net.unicon.portal.util.TemplateUtils.mergeToString(
            params, "SuperChannel/ChannelPlaceholder.vm"));
        } // end sub channel looping
        // merge subchannel buffers into SuperChannel template
        // and then return results.
        params.clear();
        params.put("width", "" + (100 / numCols));
        /*params.put("mediaPath",
            "media/org/jasig/portal/layout/tab-column/nested-tables");*/
        params.put("skin", getSkin(upId));
        i = 0;
        // set each of the column parameters
        for ( ; i < numCols; i++) {
            StringBuffer currentColumn = new StringBuffer("COL");
            currentColumn.append((i + 1)).append("_CHANNELS");
            params.put(currentColumn.toString(), columns[i]);
        }
        // set the appropriate layout for the template depending on how
        // many columns are available (ex. single is for focused and
        // detached types of content).
        if (numCols > 1) {
            params.put("layout", "default");
        } else {
            params.put("layout", "single");
        }
        //net.unicon.portal.util.Debug.instance().timeElapsed(
        //"Done renderBuffers with template", true);

        return TemplateUtils.mergeToString(
        params, "SuperChannel/SuperChannel.vm");
    } // end renderBuffers
    protected void wrapContent(ContentHandler contentHandler, String buffer)
    throws SAXException {
        contentHandler.startElement("", "deuce", "deuce", new AttributesImpl());
        char[] chars = buffer.toCharArray();
        contentHandler.characters(chars, 0, chars.length);
        contentHandler.endElement("", "deuce", "deuce");
    }
    protected String getChannelTitle(String upId, ChannelClass cc)
    throws Exception {
        return cc.getLabel();
    }
    
    protected String getChannelHelpHandle (ChannelClass cc)throws Exception 
	{
    	return cc.getHandle();
	}
    protected void setSubChannelRuntimeData(ChannelRuntimeData rd,
    String upId, ChannelClass cc)
    throws PortalException {
        try {
            String subChannelUID =
                SubChannelFactory.getSubChannelUID(upId, cc);
            String subChannelSubId =
                getChannelSubscribeId(subChannelUID);
            rd.setParameters(
                (Hashtable)getChannelAttribute(upId, reloadParamKey));

            // Set permissions for the channel
            IPermissions p = null;
            Role role = RoleFactory.getUserRoleForOfferingChannel(
                getDomainUser(upId), cc);
            IGroup group = null;
            if (role != null) {
                group = role.getGroup();
            }
            p = PermissionsUtil.getPermissions(group, cc, getUPortalUser(upId));
            rd.setParameter(parentIdKey, getChannelSubscribeId(upId));
            rd.setParameter(channelUidKey, subChannelUID);
            ChannelDataManager.setPermissions(subChannelUID, p);

            // pass the skin along
            rd.setParameter("skin", getSkin(upId));

            // tell the sub channel if the context changed
            ChannelDataManager.setContextChanged(subChannelUID,
                isContextChanged(upId));

            // Set the targetChannel and workerActionURL
            rd.setParameter("targetChannel",
                getRuntimeData(upId).getParameter(SC_TARGET_HANDLE));
            rd.setParameter("workerActionURL",
                getDownloadWorkerUrl(upId, cc));

            // set the extras field in the UPFileSpec
            // this allows params to be passed to sub channels
            // without disturbing the query string
            PortalControlStructures pcs = getPortalControlStructures(upId);
            BrowserInfo bi = getRuntimeData(upId).getBrowserInfo();
            rd.setUPFile(RenderingUtil.setupUPURL(
                pcs.getHttpServletRequest(), upId, cc, bi));
            
        } catch (OperationFailedException ofe) {
            LogService.log(LogService.ERROR,
                "Failed setting sub channel runtime data!", ofe);
            throw new PortalException(ofe);
        } catch (ItemNotFoundException infe) {
            LogService.log(LogService.ERROR,
                "Failed setting sub channel runtime data!", infe);
            throw new PortalException(infe);
        } catch (Exception e) {
            LogService.log(LogService.ERROR,
                "Failed setting sub channel runtime data!", e);
            throw new PortalException(e);
        }
    }

    private String getDownloadWorkerUrl(String upId, ChannelClass cc)
    throws PortalException {
    
        PortalControlStructures pcs = getPortalControlStructures(upId);
        BrowserInfo bi = getRuntimeData(upId).getBrowserInfo();
        UPFileSpec upfs = RenderingUtil.setupUPURL(
                pcs.getHttpServletRequest(), upId, cc, bi);
        upfs.setMethod(UPFileSpec.WORKER_METHOD);
        upfs.setMethodNodeId(UPFileSpec.FILE_DOWNLOAD_WORKER);
        upfs.setTagId(PortalSessionManager.IDEMPOTENT_URL_TAG);
        return upfs.getUPFile() + "?download=true";
    }

    protected SubChannelCache getSubChannelCache(String upId) {
        SubChannelCache cache =
        (SubChannelCache)getChannelAttribute(upId, cacheKey);
        if (cache == null) {
            cache = new SubChannelCache();
            putChannelAttribute(upId, cacheKey, cache);
        }
        return cache;
    }

    protected void determineFocusedChannelState(String upId) {
        // the super channel is no longer rendering as root, clear any
        // saved focused channel
        String targetId = getTargetChannelId(upId);
        String focusId = getFocusedChannel(upId);
        if (!getRuntimeData(upId).isRenderingAsRoot() || (targetId != null && !targetId.equals(focusId))) {
            putChannelAttribute(upId, SC_FOCUSED_CHANNEL, null);
        } else {
            // if the focusedChannel param exists, start a focused channel state
            String focusedChannel = getFocusedChannelFromRuntime(upId);
            if (focusedChannel != null) {
                putChannelAttribute(upId, SC_FOCUSED_CHANNEL, focusedChannel);
            }
        }
    }

    protected String getFocusedChannel(String upId) {
        String focusedChannel =
            (String)getChannelAttribute(upId, SC_FOCUSED_CHANNEL);
        return focusedChannel;
    }

    protected String getFocusedChannelFromRuntime(String upId) {
        return getRuntimeData(upId).getParameter(SC_FOCUSED_CHANNEL);
    }

    // this method pulls the target channel from the runtime data, then
    // resolves an IChannel creating it if necessary
    public IChannel getTargetChannel(String upId) throws Exception {
        String targetChannel = getTargetChannelId(upId);
        if (targetChannel == null) return null;

        String handle = resolveChannelClass(targetChannel);
        ChannelClass cc = ChannelClassFactory.getChannelClass(handle);
        return SubChannelFactory.getChannelInstance(upId, cc,
            getStaticData(upId), getUPortalUser(upId),
            getAuthorizationPrincipal(upId),
            getPortalControlStructures(upId).getUserPreferencesManager());
    }
    // This method will determine which channel is currently working.
    // A working channel is one that is the target channel or currently
    // focused.
    public String getWorkingChannelId(String upId) {
        String workingChannel = getTargetChannelId(upId);
        if (workingChannel == null) {
            workingChannel = getFocusedChannel(upId);
        }
        return workingChannel;
    }
    public String getTargetChannelId(String upId) {
        String channelHandle =
            getRuntimeData(upId).getParameter(SC_TARGET_HANDLE);

        // no target channel handle, therefore no target channel id,
        // bail out!
        if (channelHandle == null) return null;

        return SubChannelFactory.convertUIDToSubscribeId(
           SubChannelFactory.convertChannelHandleToSubUIDFromParent(
               upId, channelHandle));
    }
    protected boolean getSubChannelMinimizedState(String upId,
    ChannelClass cc) {
        // First check if the minimized state exists in the runtime data
        String paramValue = getRuntimeData(upId).getParameter("minimized_" +
        SubChannelFactory.getSubChannelSubscribeId(upId, cc) + "_value");
        Boolean minimizedState = null;
        String subUID = SubChannelFactory.getSubChannelUID(upId, cc);
        if (paramValue != null) {
            minimizedState = new Boolean("true".equalsIgnoreCase(paramValue));
            getSubChannelCache(upId).getMinimizedStates().
            put(subUID, minimizedState);
        } else {
            // no minimzed state in runtime data, fallback to
            // channel attributes. If no channel attribute exists,
            // assume not minimized.
            minimizedState = (Boolean)getSubChannelCache(upId).
            getMinimizedStates().get(subUID);
            if (minimizedState == null) return false;
        }
        return minimizedState.booleanValue();
    }
    public void setContextChanged(String upId, boolean b) {
        ChannelDataManager.setContextChanged(upId, b);
    }
    public boolean isContextChanged(String upId) {
        return ChannelDataManager.isContextChanged(upId);
    }
    protected class SubChannelCache {
        Map minimizedStates = new SoftHashMap();
        public SubChannelCache() { }
        public Map getMinimizedStates() {
            return minimizedStates;
        }
    }
    protected class ChannelObjects {
        ChannelClass channelClass;
        IChannel ichannel;
        ChannelRendererWrapper channelRendererWrapper;
        public ChannelObjects(ChannelClass cc, IChannel ch) {
            channelClass = cc;
            ichannel = ch;
            channelRendererWrapper = null;
        }
        public void setChannelRendererWrapper(ChannelRendererWrapper crw) {
            channelRendererWrapper = crw;
        }
        public ChannelClass getChannelClass() {
            return channelClass;
        }
        public IChannel getIChannel() {
            return ichannel;
        }
        public ChannelRendererWrapper getChannelRendererWrapper() {
            return channelRendererWrapper;
        }
    }

    /**
     * Let the channel know that there were problems with the download
     * @param e
     */
    public void reportDownloadError(Exception e) {
      LogService.log(LogService.ERROR, getClass().getName()+"::reportDownloadError(): " + e.getMessage());
    }
}
