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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.academus.producer.IProducer;
import net.unicon.portal.cache.DirtyEventDCRHAdapter;
import net.unicon.portal.cache.DirtyEventDCRHAdapterImpl;
import net.unicon.portal.channels.rad.GroupData;
import net.unicon.portal.common.PermissibleFactory;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.cscr.CscrChannelRuntimeData;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.domain.ChannelType;
import net.unicon.portal.groups.GroupFactory;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.permissions.Activity;
import net.unicon.portal.permissions.DirtyEvent;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.permissions.PermissionsService;
import net.unicon.portal.servants.IResultsAdapter;
import net.unicon.portal.servants.ServantManager;
import net.unicon.portal.servants.ServantResults;
import net.unicon.portal.servants.ServantType;
import net.unicon.portal.util.ChannelDefinitionUtil;
import net.unicon.portal.util.ProducerRenderer;
import net.unicon.portal.util.RenderingUtil;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IMultithreadedCacheable;
import org.jasig.portal.IMultithreadedCharacterChannel;
import org.jasig.portal.IMultithreadedMimeResponse;
import org.jasig.portal.IMultithreadedPrivileged;
import org.jasig.portal.IPermissible;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.services.LogService;
import org.xml.sax.ContentHandler;

import com.interactivebusiness.portal.VersionResolver;

/**
 * This class creates a portlet object within the uportal world. The portlet
 * is responsible for gathering runtime data and handing it to an
 * application provider. The provider is then responsible for returning
 * content, which could be XML, HTML, an URL, ..., depending on the type
 * of provider. Once the provider returns its content, the portlet will
 * pass it along to the uPortal framework
 */
public class PortletMultithreadedChannel
implements IMultithreadedCharacterChannel, IMultithreadedCacheable, IMultithreadedMimeResponse, IMultithreadedPrivileged {

    private DirtyEventDCRHAdapter __deAdapter = null;

    public PortletMultithreadedChannel() {
        super();

    // should use a factory later YYY --KG
    try {
        __deAdapter = DirtyEventDCRHAdapterImpl.getInstance();
    }
    catch (Exception exc) {
        exc.printStackTrace();
    }
    }

    public ChannelRuntimeProperties getRuntimeProperties (String uid) {
        return new ChannelRuntimeProperties();
    }

    public void receiveEvent (PortalEvent event, String upId) {
        if (event.getEventNumber() == PortalEvent.SESSION_DONE) {
            ChannelDataManager.removeData(upId);
        }
        ServantManager.sendPortalEvent(upId, event);
    }

    /**
     * Set the portal control structures.
     */
    public void setPortalControlStructures(PortalControlStructures pcs,
        String upId)
    throws PortalException {
        ChannelDataManager.setPortalControlStructures(upId, pcs);
    }

    public PortalControlStructures getPortalControlStructures(String upId) {
        return ChannelDataManager.getPortalControlStructures(upId);
    }

    public void setStaticData (ChannelStaticData sd, String upId)
    throws PortalException {
        try {
            // If this channel has not already been registered,
            // it must register itself with the ChannelDataManager.
            // Sub channels will be regsitered on behalf of the SuperChannel
            if (!ChannelDataManager.uidExists(upId)) {
                int publishId = Integer.parseInt(sd.getChannelPublishId());
                ChannelDefinition cd = ChannelRegistryStoreFactory.
                    getChannelRegistryStoreImpl().
                        getChannelDefinition(publishId);
                ChannelDataManager.registerChannelUser(
                    sd.getPerson(), null, cd, upId);
            }
        } catch (PortalException pe) {
            throw pe;
        } catch (Exception e) {
        e.printStackTrace();
            throw new PortalException(e);
        }

        ChannelDataManager.setStaticData(upId, sd);
        IAuthorizationPrincipal principal = VersionResolver.getInstance().
                getPrincipalByPortalVersions(sd.getPerson());
        ChannelDataManager.setAuthorizationPrincipal(upId, principal);
    }

    public ChannelStaticData getStaticData(String upId) {
        return ChannelDataManager.getStaticData(upId);
    }

    public void setRuntimeData(ChannelRuntimeData rd, String upId)
    throws PortalException {
        // Only non-cms channels are passed the CscrChannelRuntimeData object.
        // Cms channels (sub-channels) are handled by the super channel.
        // Sub-channels will not have an assigned publishId.
        String publishId = getStaticData(upId).getChannelPublishId();
        if (publishId == null || "".equals(publishId.trim())) {
            ChannelDataManager.setRuntimeData(upId, rd);
        } else {
            ChannelDataManager.setRuntimeData(upId,
                    new CscrChannelRuntimeData(publishId, rd));
        }
    }

    public ChannelRuntimeData getRuntimeData(String upId) {
        return ChannelDataManager.getRuntimeData(upId);
    }

    public String getChannelSubscribeId(String upId) {
        return getStaticData(upId).getChannelSubscribeId();
    }

    public void renderXML (ContentHandler out, String upId)
    throws PortalException {
       LogService.log(LogService.WARN, "renderXML called in PortletMTChannel");
    }

    public void renderCharacters (PrintWriter out, String upId)
    throws PortalException {
        try {
        LogService.log(LogService.DEBUG, "PMTC rendering characters");


            // Clear the dirty flag since we're rendering now
            ChannelDataManager.setDirty(upId, false);

            ChannelStaticData staticData =
                ChannelDataManager.getStaticData(upId);
            ChannelRuntimeData runtimeData =
                ChannelDataManager.getRuntimeData(upId);
            boolean servantDone = false;
            StringWriter sw = new StringWriter();
            PrintWriter servantOut = new PrintWriter(sw);

            String value;
            String command = runtimeData.getParameter("command");
            String servantCommand = runtimeData.getParameter("servant_command");
            String nextCommand = runtimeData.getParameter("next_command");
            String[] nextArgs =
                runtimeData.getParameterValues("next_command_arg");
            Hashtable nextValues = new Hashtable();
            for (int i=0; nextArgs != null && i<nextArgs.length; i++) {
                value = runtimeData.getParameter("next_command_" + nextArgs[i]);
                nextValues.put(nextArgs[i], value != null ? value : "");
            }

            ServantResults servantResults = null;

            if ("editPermissions".equals(servantCommand) ||
                "selectGroups".equals(servantCommand) ||
                "cancel".equals(servantCommand)) {
                // clear out a previous running permissions servant
                ServantManager.removeServant(upId,
                    ServantType.PERMISSION_ADMIN);
                ServantManager.removeServant(upId,
                    ServantType.IDENTITY_SELECTOR);
            }

            // first look for a running servant
            if (ServantManager.hasServant(upId,ServantType.PERMISSION_ADMIN)) {
                ServantManager.renderServant(upId, servantOut, staticData,
                    runtimeData, ServantType.PERMISSION_ADMIN);

                if (!ServantManager.hasServant(upId,
                    ServantType.PERMISSION_ADMIN)) {
                    servantDone = true;
                }
            }

            if (ServantManager.hasServant(upId,ServantType.IDENTITY_SELECTOR)) {
                servantResults = ServantManager.renderServant(upId,
                    servantOut, staticData, runtimeData, ServantType.IDENTITY_SELECTOR);

                if (!ServantManager.hasServant(upId,
                    ServantType.IDENTITY_SELECTOR)) {
                    servantDone = true;
                    if (servantResults != null) {
                        command = servantResults.getNextCommand();
                        runtimeData.setParameter("command", command);

                        // pass along the arguments to the next command
                        Map theNextArgs = servantResults.getNextCommandArgs();
                        String param;
                        Iterator itr = theNextArgs.keySet().iterator();
                        while (itr.hasNext()) {
                            param = (String)itr.next();
                            runtimeData.setParameter(param,
                                (String)theNextArgs.get(param));
                        }
                    }
                }
            }

            runtimeData.setParameter(IProducer.SERVANT_DONE, ""+servantDone);
            if (servantResults != null) {
                runtimeData.put(IProducer.SERVANT_RESULTS,
                    servantResults.getResults());
            }

            // Ask the producer for content
            IProducer producer = ChannelDataManager.getProducer(upId);

            ProducerRenderer.ProducerResults results =
                ProducerRenderer.instance().produceContent(upId,
                    runtimeData, ChannelDataManager.getDomainUser(upId),
                    producer, this);

            if (results.getReturnData() != null &&
                results.getReturnData().get(IProducer.ABORT_SERVANT) != null) {
                servantCommand = null;
            }

            // producer is finished rendering, clear its context changed flag
            ChannelDataManager.setContextChanged(upId, false);

            // if we're in the SC, check for dirty events
            // Membership in SC determined by having a ChannelClass
            ChannelClass cc = ChannelDataManager.getChannelClass(upId);
            if (cc != null && __deAdapter != null) {
                Map returnData = (Map)results.getReturnData();
                DirtyEvent[] dirtyEvents =
                    (DirtyEvent[])returnData.get(IProducer.DIRTY_EVENT);

                if (dirtyEvents != null) {
                    LogService.log(LogService.INFO, "PMTC dirty event processing:");
                    for (int i = 0; i < dirtyEvents.length; i++) {
                        LogService.log(LogService.INFO, dirtyEvents[i].toString());
                    }
                }

                __deAdapter.broadcastDirtyEvents(dirtyEvents, upId);
            }

            if ("editPermissions".equals(servantCommand)) {
                renderPermissionsServant(upId, producer, nextCommand,
                    nextValues, servantOut);
            } else if ("selectGroups".equals(servantCommand)) {
                renderSelectGroupsServant(upId, producer, nextCommand,
                    nextValues, servantOut);
            }

            // send out the channel's content
            out.print(results.getContent());

            // send out any servant content
            out.print(sw.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new PortalException(e);
        }
    }

    private void renderSelectGroupsServant(String upId, IProducer producer,
        String nextCommand, Map nextArgs, PrintWriter servantOut)
    throws Exception {

        IResultsAdapter resultsAdapter = new IResultsAdapter() {
            public Object[] adapt(Object[] results) throws PortalException {
                if (results == null) {
                    return new IGroup[0];
                }
                try {
                    String key;
                    IdentityData identityData;
                    Object[] adaptedResults = new Object[results.length];
                    int j=0;
                    for (int i=0; i<adaptedResults.length; i++) {
                        identityData = (IdentityData)results[i];
                        if (IdentityData.GROUP.equals(identityData.getType())) {
                            adaptedResults[j++] = GroupFactory.getGroup(
                                identityData.getID());
                        }
                    }
                    return adaptedResults;
                } catch (Exception e) {
                    throw new PortalException(e);
                }
            }
        };

        ChannelDataManager.getRuntimeData(upId).put("sources", "groupsOnly");
        ServantManager.createIdentitySelectorServant(upId,
            ChannelDataManager.getStaticData(upId), nextCommand, nextArgs,
            resultsAdapter);

        // setup initial selected groups if username was given
        ChannelDataManager.getRuntimeData(upId).put("selected",
            getInitialGroups(nextArgs));
        ServantManager.renderServant(upId, servantOut,
            ChannelDataManager.getStaticData(upId),
            ChannelDataManager.getRuntimeData(upId),
            ServantType.IDENTITY_SELECTOR);
    }

    private IdentityData[] getInitialGroups(Map nextArgs)
    throws Exception {
        if (nextArgs == null) return new IdentityData[0];

        String username = (String)nextArgs.get("username");

        IGroup[] groups = null;
        if (username != null) {
            User user = UserFactory.getUser(username);
            if (user != null && user.getGroupMember() != null) {
                groups = user.getGroupMember().getContainingGroups();
            } else {
                LogService.log(LogService.ERROR,
                    "PortletMultithreadedChannel::renderSelectGroupsServant() "
                    + ": User could not create group member object: " +
                    username);
            }
        }

        // now convert IGroup[] into IdentityData[]
        IdentityData[] retVal = new IdentityData[0];
        if (groups != null) {
            retVal = new IdentityData[groups.length];
            IEntityGroup gr;
            for (int i=0; i<groups.length; i++) {
                gr = groups[i].getEntityGroup();
                retVal[i] = new IdentityData(IdentityData.GROUP,
                    GroupData.S_GROUP, gr.getKey(), gr.getName());
            }
        }
        return retVal;
    }

    private void renderPermissionsServant(String upId, IProducer producer,
        String nextCommand, Map nextArgs, PrintWriter servantOut)
    throws Exception {
        // have been told to delegate to to permission manager servant
        Activity[] activities = producer.getActivities();

        // We need to generate an IPermissions object so we
        // can pass the channel target to the servant.
        // Subchannels will have ChannelClass objects.
        // Regular uportal channels will have ChannelDefinition objects
        ChannelClass cc =
            ChannelDataManager.getChannelClass(upId);
        ChannelDefinition cd =
            ChannelDataManager.getChannelDefinition(upId);

        String ownerName = null;
        String ownerToken = null;
        IPermissions p = null;
        IAuthorizationPrincipal principal = VersionResolver.getInstance().
            getPrincipalByPortalVersions(
                ChannelDataManager.getUPortalUser(upId));

        if (cc != null) {
            ownerToken = cc.getPermissionsOwner();
            ownerName = cc.getLabel();
            p = PermissionsService.instance().getPermissions(cc, principal);
        } else if (cd != null) {
            ownerToken = ChannelDefinitionUtil.getParameter(cd,"producer");
            ownerName = cd.getTitle();
            p = PermissionsService.instance().getPermissions(principal, cd);
        } else {
            throw new PortalException("No Channel Object exists for " + upId);
        }
        String[] targets = {p.getTargetToken()};

        IPermissible owner =
            PermissibleFactory.getPermissible(
            ownerToken,
            ownerName,
            activities,
            targets);

        // create the servant .. the ServantManager will
        // manage it for us.
        ServantManager.createPermissionAdminServant(upId, getStaticData(upId),
            owner, activities, null, nextCommand, nextArgs, true, true);
                
        // now render it
        ServantManager.renderServant(upId, servantOut,
            ChannelDataManager.getStaticData(upId),
            ChannelDataManager.getRuntimeData(upId),
            ServantType.PERMISSION_ADMIN);
    }

    // IMultithreadedCacheable interface methods

    public ChannelCacheKey generateKey(String upId) {
        ChannelCacheKey key = new ChannelCacheKey();
        key.setKey(getKey(upId));
        key.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
        key.setKeyValidity(null);
        return key;
    }

    public boolean isCacheValid(Object validity, String upId) {
        ChannelClass cc = ChannelDataManager.getChannelClass(upId);
  
        if (cc != null && cc.getChannelType().equals(ChannelType.OFFERING)) {
            boolean contextChanged = ChannelDataManager.isContextChanged(upId);
            if (contextChanged) {
                ChannelDataManager.setDirty(upId, false);
                return false;
            }
        }

        // if channel is dirty
        if (ChannelDataManager.isDirty(upId)) {
            return false;
        }

        // Are they working in this Channel?
        // If so, invalidate the cache
        String target = getChannelTargetId(upId);
        if (target != null && getChannelSubscribeId(upId).indexOf(target)>=0) {
            return false;
        }
        return true;
    }

    // In the case where we're running in cscr mode inside the lms,
    // the instatiation of UPFileSpec will fail. So both
    // ChannelRendererWorker and SuperChannel pass along the
    // targetChannel parameter. Otherwise, it will look for the 
    // target channel in the UPFileSpec.
    private String getChannelTargetId(String upId) {
        String targetId = null;
        try {
            targetId = getRuntimeData(upId).getParameter("targetChannel");
            if (targetId != null && !"".equals(targetId)) {
                return targetId;
            }
           
            PortalControlStructures pcs = getPortalControlStructures(upId);
            UPFileSpec upfs = new UPFileSpec(pcs.getHttpServletRequest());
            Map extras = RenderingUtil.parseExtrasField(upfs);
            targetId = (String)extras.get(
                SuperChannel.SC_TARGET_HANDLE);
            if (targetId == null || "".equals(targetId)) {
                targetId = upfs.getTargetNodeId();
            }
        } catch (PortalException pe) {
            LogService.log(LogService.ERROR, pe); 
        }
        return targetId;
    }

    

    private String getKey(String upId) {
        return upId;
    }

    // IMultithreadedMimeResponse methods

    public String getContentType(String upId) {
        return null;
    }

    public InputStream getInputStream(String upId) throws IOException {
        return null;
    }

    public String getName(String upId) {
        return null;
    }

    public Map getHeaders(String upId) {
        return null;
    }

    public void downloadData(OutputStream out, String upId) throws IOException {
    }

    /**
     * Let the channel know that there were problems with the download
     * @param e
     */
    public void reportDownloadError(Exception e) {
      LogService.log(LogService.ERROR, "PortletMultithreadedChannel::reportDownloadError(): " + e.getMessage());
    }
}
