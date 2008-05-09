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

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Hashtable;

import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.cache.DirtyCacheRequestHandler;
import net.unicon.portal.cache.DirtyCacheRequestHandlerFactory;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.cscr.CscrChannelRuntimeData;
import net.unicon.portal.servants.ServantManager;
import net.unicon.portal.servants.ServantUtil;
import net.unicon.portal.util.RenderingUtil;
import net.unicon.portal.util.db.AcademusDBUtil;

import org.jasig.portal.BrowserInfo;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IMultithreadedCharacterChannel;
import org.jasig.portal.IMultithreadedPrivileged;
import org.jasig.portal.IServant;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;

public class AcademusMultithreadedChannel
implements IMultithreadedCharacterChannel, IMultithreadedPrivileged {
    public AcademusMultithreadedChannel() {
        super();
        try {
            cacheHandler = DirtyCacheRequestHandlerFactory.getHandler();
        } catch (Exception e) {
            LogService.log(LogService.ERROR, "AcademusMultithreadedChannel: Unable to get DirtyCacheRequestHandler Implementation; DirtyCacheRequestHandlerFactory is throwing errors");
            e.printStackTrace();
        }
    }
    protected static DirtyCacheRequestHandler cacheHandler = null;
    private static final String contentHandlerKey = "ContentHandler";
    private static final String servantContentKey = "servantContent";
    private static final String printWriterKey = "PrintWriter";

    public void setPortalControlStructures(PortalControlStructures pcs,
        String upId)
    throws PortalException {
        ChannelDataManager.setPortalControlStructures(upId, pcs);
    }

    public PortalControlStructures getPortalControlStructures(String upId) {
        return ChannelDataManager.getPortalControlStructures(upId);
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
    public void setStaticData (ChannelStaticData sd, String upId)
    throws PortalException {
        ChannelDataManager.setStaticData(upId, sd);
    }
    public ChannelStaticData getStaticData(String upId) {
        return ChannelDataManager.getStaticData(upId);
    }
    public void setRuntimeData(ChannelRuntimeData rd, String upId)
    throws PortalException {
	/* DEBUG ONLY
	   net.unicon.common.util.debug.DebugUtil.spillParameters(rd,
	   "SETTING RUNTIME DATA IN AMTC: " + upId);
	*/

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
    public ContentHandler getContentHandler(String upId) {
        return (ContentHandler)getChannelAttribute(upId, contentHandlerKey);
    }
    public void setServantContent(String upId, String content) {
        putChannelAttribute(upId, servantContentKey, content);
    }
    public String getServantContent(String upId) {
        return (String)getChannelAttribute(upId, servantContentKey);
    }
    // This method will display the default error page for channels.
    // It handles setting the SSL and populates the xml. So this should
    // be the last action on errors. (i.e. you shouldn't set the xml or
    // stylesheet location after calling this method.
    private void showErrorPage(String upId, String errorMsg)
    throws PortalException {
        setSSLLocation(upId, "/net/unicon/portal/channels/error/error.ssl");
        setSheetName(upId, "error");
        StringBuffer xmlSB = new StringBuffer();
        xmlSB.append("<errorPage><message>" + errorMsg + "</message></errorPage>");
        // clear out any buffers the channel produced
        clearBuffers(upId);
        setXML(upId, xmlSB.toString());

        // bypass cscr
        String parentUID = SubChannelFactory.getParentUID(upId);
        if (parentUID != null) {
            BrowserInfo bi = getRuntimeData(upId).getBrowserInfo();
            UPFileSpec upfs = RenderingUtil.setupUPURL(parentUID,
                ChannelDataManager.getChannelClass(upId), bi, false);
            Hashtable ht = getXSLParameters(upId);
            ht.put("baseActionURL", upfs.getUPFile());
        }
    }
    private void clearBuffers(String upId) {
        setCharacterBuffer(upId, null);
        setSAXBuffer(upId, null);
        setDocument(upId, null);
        setXML(upId, null);
        setServantContent(upId, null);
    }
    public void renderXML (ContentHandler out, String upId)
    throws PortalException {
    LogService.log(LogService.WARN, "In AMTC renderXML!");
    }
    public void renderCharacters (PrintWriter out, String upId)
    throws PortalException {
        try {
        LogService.log(LogService.DEBUG, "In AMTC renderCharacters!");

            // Clear the dirty flag since we're rendering now
            setDirty(upId, false);
            // Clear out the xsl parameters
            Hashtable ht = getXSLParameters(upId);
            ht.clear();
            // clear out previous buffers
            clearBuffers(upId);
            // set the content handler for use by sub classes
            putChannelAttribute(upId, printWriterKey, out);
            // -----------------------------------------------------------------
            // You can use a different sheetname to specify that you want to use
            // a different stylesheet.  This name must match up with the 'title'
            // attribute of one of the processing instructions in your
            // stylesheet list.
            // -----------------------------------------------------------------
            // First reset the errorMsg
            setErrorMsg(upId, "");
            setupXSLParameters(upId);
            buildXML(upId);

            // The channel just finished rendering, clear it's context changed flag
            ChannelDataManager.setContextChanged(upId, false);

            // If channel set an error msg, redirect to the global error page.
            String msg = getErrorMsg(upId);
            if (msg != null && ! "".equals(msg)) {
                showErrorPage(upId, msg);
            }
            if (getCharacterBuffer(upId) != null) {
                out.print(getCharacterBuffer(upId));
                // append any servant content to the end of the channel
                if (getServantContent(upId) != null) {
                    out.print(getServantContent(upId));
                }
                return;
            }

            if (getSAXBuffer(upId) != null) {
                RenderingUtil.renderSAX(this, getStaticData(upId).getSerializerName(),
                    out, getSAXBuffer(upId),
                    getSSLLocation(upId), getSheetName(upId),
                    getRuntimeData(upId).getBrowserInfo(),
                    getXSLParameters(upId));
            } else if (getDocument(upId) != null) {
                RenderingUtil.renderDocument(this, getStaticData(upId).getSerializerName(),
                    out, getDocument(upId),
                    getSSLLocation(upId), getSheetName(upId),
                    getRuntimeData(upId).getBrowserInfo(),
                    getXSLParameters(upId));
            } else if (getXML(upId) != null) {
                RenderingUtil.renderXML(this, getStaticData(upId).getSerializerName(),
                    out, getXML(upId),
                    getSSLLocation(upId), getSheetName(upId),
                    getRuntimeData(upId).getBrowserInfo(),
                    getXSLParameters(upId));
            } else {
                // if no xml was set, we rely on the channel to render content
                // since they have access to the content handler
                 LogService.log(LogService.DEBUG,
                     "AcademusMultithreadedChannel::renderCharacters() " +
                     "relying on channel to render itself");
            }
        } catch (PortalException pe) {
            pe.printStackTrace();
            LogService.log(LogService.ERROR, pe);
            throw pe;
        } catch (Exception e) {
            e.printStackTrace();
            LogService.log(LogService.ERROR, e);
            throw new PortalException(e);
        }

        // append any servant content to the end of the channel
        if (getServantContent(upId) != null) {
            out.print(getServantContent(upId));
        }
    }
    // This method broadcasts a dirty message to each of the user's channels
    public void broadcastDirtyChannels(User user) throws PortalException {
        cacheHandler.broadcastDirtyChannels(user);
    }
    // This method broadcasts a dirty message to every user's channel
    // specified by the channelHandle
    public void broadcastDirtyChannel(String channelHandle)
    throws PortalException {
        cacheHandler.broadcastDirtyChannels(channelHandle);
    }
    // This method broadcasts a dirty channel message to a specific
    // channel for a specific user.
    public void broadcastUserDirtyChannel(User user, String channel)
    throws PortalException {
        cacheHandler.broadcastDirtyChannels(user, channel);
    }
    // This method broadcasts a dirty channel message to a specific
    // channel for each user specified in users[]
    public void broadcastUserDirtyChannel(User[] users, String channel)
    throws PortalException {
        cacheHandler.broadcastDirtyChannels(users, channel);
    }
    /**
     *       Sets the channels dirty for all currently logged in users looking at this Offering
     *       @author KG
     */
    public void broadcastUserOfferingDirtyChannel(Offering offering,
    String channelHandle, boolean allUsers) throws PortalException {
        cacheHandler.broadcastDirtyChannels(offering, channelHandle, allUsers);
    }
    /**
     *       Sets the channels dirty for all currently logged in users looking at this Offering except for the passed in user
     *       @author KG
     */
    public void broadcastUserOfferingDirtyChannel(User user, Offering offering,
    String channelHandle, boolean allUsers) throws PortalException {
        cacheHandler.broadcastDirtyChannels(user, offering,
            channelHandle, allUsers);
    }
    public void setupXSLParameters(String upId) throws PortalException {
        Hashtable ht = getXSLParameters(upId);
        ht.put("baseActionURL", getRuntimeData(upId).getBaseActionURL());
        ht.put("workerActionURL", getRuntimeData(upId).
        getBaseWorkerURL(UPFileSpec.FILE_DOWNLOAD_WORKER, true));
    }

    public void buildXML(String upId) throws Exception { }
    protected static Connection getDBConnection() {
        return AcademusDBUtil.safeGetDBConnection();
    }
    protected static void releaseDBConnection(Connection connection) {
        AcademusDBUtil.safeReleaseDBConnection(connection);
    }

    public void renderServant(String upId, IServant servant)
    throws PortalException {
        PrintWriter pw = (PrintWriter)getChannelAttribute(upId,
            printWriterKey);
        ContentHandler ch = (ContentHandler)getChannelAttribute(upId,
            contentHandlerKey);
        if (pw != null) {
            ServantUtil.renderServant(pw, servant, getStaticData(upId), getRuntimeData(upId));
        } else if (ch != null) {
            ServantUtil.renderServant(ch, servant, getRuntimeData(upId));
        } else {
            LogService.log(LogService.ERROR,
                "AcademusMultithreadedChannel::renderServant() " +
                "No PrintWriter found!");
        }
    }

    public Offering getCurrentOffering(String upId, TopicType topicType) {
        return getDomainUser(upId).getContext().getCurrentOffering(topicType);
    }
    public String getXML(String upId) {
        return ChannelDataManager.getXML(upId);
    }
    public void setXML(String upId, String xml) {
        ChannelDataManager.setXML(upId, xml);
    }
    public String getErrorMsg(String upId) {
        return ChannelDataManager.getErrorMsg(upId);
    }
    public void setErrorMsg(String upId, String errorMsg) {
        ChannelDataManager.setErrorMsg(upId, errorMsg);
    }
    public Document getDocument(String upId) {
        return ChannelDataManager.getDocument(upId);
    }
    public void setDocument(String upId, Document document) {
        ChannelDataManager.setDocument(upId, document);
    }
    public void setCharacterBuffer(String upId, String charBuffer) {
        ChannelDataManager.setCharacterBuffer(upId, charBuffer);
    }
    public String getCharacterBuffer(String upId) {
        return ChannelDataManager.getCharacterBuffer(upId);
    }
    public void setSAXBuffer(String upId, SAX2BufferImpl saxBuffer) {
        ChannelDataManager.setSAXBuffer(upId, saxBuffer);
    }
    public SAX2BufferImpl getSAXBuffer(String upId) {
        return ChannelDataManager.getSAXBuffer(upId);
    }
    public String getSSLLocation(String upId) {
        return ChannelDataManager.getSSLLocation(upId);
    }
    public void setSSLLocation(String upId, String sslLocation) {
        ChannelDataManager.setSSLLocation(upId, sslLocation);
    }
    public String getSheetName(String upId) {
        return ChannelDataManager.getSheetName(upId);
    }
    public void setSheetName(String upId, String sheetName) {
        ChannelDataManager.setSheetName(upId, sheetName);
        if (sheetName != null) {
            getXSLParameters(upId).put("current_command", sheetName);
        }
    }
    public Hashtable getXSLParameters(String upId) {
        return ChannelDataManager.getXSLParameters(upId);
    }
    public void setXSLParameters(String upId, Hashtable params) {
        ChannelDataManager.setXSLParameters(upId, params);
    }
    public IPerson getUPortalUser(String upId) {
        return ChannelDataManager.getUPortalUser(upId);
    }
    public void setUPortalUser(String upId, IPerson uportalUser) {
        ChannelDataManager.setUPortalUser(upId, uportalUser);
    }
    public IAuthorizationPrincipal getAuthorizationPrincipal(String upId) {
        return ChannelDataManager.getAuthorizationPrincipal(upId);
    }
    public void setAuthorizationPrincipal(String upId,
        IAuthorizationPrincipal principal) {
        ChannelDataManager.setAuthorizationPrincipal(upId, principal);
    }
    public User getDomainUser(String upId) {
        return ChannelDataManager.getDomainUser(upId);
    }
    public void setDomainUser(String upId, User domainUser) {
        ChannelDataManager.setDomainUser(upId, domainUser);
    }
    // This method is designed for channels to set
    // other channels dirty. Otherwise, use the setDirty()
    // method to set one's self dirty.
    public void setChannelDirty(String chanUID, boolean dirty)
    throws PortalException {
        if (ChannelDataManager.uidExists(chanUID)) {
            setDirty(chanUID, dirty);
        }
    }
    public boolean isDirty(String upId) {
        return ChannelDataManager.isDirty(upId);
    }
    public void setDirty(String upId, boolean dirty) {
        ChannelDataManager.setDirty(upId, dirty);
    }
    public Object getChannelAttribute(String upId, Object key) {
        return ChannelDataManager.getAttribute(upId, key);
    }
    public void putChannelAttribute(String upId, Object key, Object value) {
        ChannelDataManager.putAttribute(upId, key, value);
    }
    public void removeChannelAttribute(String upId, Object key) {
        ChannelDataManager.removeAttribute(upId, key);
    }
}
