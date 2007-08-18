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

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.OperationFailedException;
import net.unicon.academus.domain.lms.Role;
import net.unicon.academus.domain.lms.RoleFactory;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.portal.channels.SuperChannel;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.portal.cscr.CscrBrowserRegistry;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.domain.ChannelClassFactory;
import net.unicon.portal.domain.ChannelMode;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.util.ChannelCacheTable;
import net.unicon.portal.util.ChannelDefinitionUtil;
import net.unicon.portal.util.ChannelRendererWrapper;
import net.unicon.portal.util.Debug;
import net.unicon.portal.util.PermissionsUtil;
import net.unicon.portal.util.PortalControlStructuresUtil;
import net.unicon.portal.util.PortalSAXUtils;
import net.unicon.portal.util.RenderingUtil;
import net.unicon.portal.util.RuntimeDataUtil;
import net.unicon.portal.util.SerializerFactory;
import net.unicon.portal.util.StatsRecorderWrapper;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.util.ExceptionUtils;
import net.unicon.util.PrintFormat;

import org.jasig.portal.BrowserInfo;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelManager;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.IRequestParamWrapper;
import org.jasig.portal.IWorkerRequestProcessor;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.ThemeStylesheetDescription;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.UserInstance;
import org.jasig.portal.layout.alm.IALFolderDescription;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonManagerFactory;
import org.jasig.portal.serialize.BaseMarkupSerializer;
import org.jasig.portal.serialize.CachingSerializer;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.utils.URLUtil;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.interactivebusiness.portal.VersionResolver;
/**
 * A uPortal worker class that produces page fragments consisting of individual channels and its dependent channels.
 * @author  Nick Bolton
 * @version %I%, %G%
 */
public class ChannelRendererWorker implements IWorkerRequestProcessor {
    private static String[] __DIRTY_REMOVE_PARAMS =   { "command" };
    private static final String divTagIdPrefix = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.portal.common.ChannelRendererWorker.divTagIdPrefix");
    private static final String divTagIdSuffix = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.portal.common.ChannelRendererWorker.divTagIdSuffix");
    private static final boolean showChannelResults = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getPropertyAsBoolean("net.unicon.portal.common.ChannelRendererWorker.showChannelResults");
    protected static final String uidKey = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("org.jasig.portal.security.uidKey");
    private static PrintFormat fragment = null;

    // if any of the runtime parameters have this prefix, the request
    // get redirected to the framework.
    private static String frameworkPrefix = "uP_";

    private final static String NAVIGATION_CHANNEL = "NavigationChannel";

    // channels that dirty the super channel
    private static Set channelsThatDirtySC = null;

    private static final String characterSet = "UTF-8";

    Map inputParams = new HashMap();
    Map extras = new HashMap();

    static {
        try {
            String classUrl = ChannelRendererWorker.class.getResource("ChannelRendererWorker/page_fragment.html").getFile();
            fragment = new PrintFormat(
            ResourceLoader.getResourceAsFile(ChannelRendererWorker.class,
            "ChannelRendererWorker/page_fragment.html"));

            channelsThatDirtySC = new HashSet(1);
            channelsThatDirtySC.add(NAVIGATION_CHANNEL);
        } catch (IOException e) {
            LogService.instance().log(LogService.ERROR,
            "ChannelRendererWorker : Failed to load fragment template: " +
            ExceptionUtils.getExceptionMessage(e));
        } catch (ResourceMissingException e) {
            LogService.instance().log(LogService.ERROR,
            "ChannelRendererWorker : Failed to load fragment template - missing resource: " +
            ExceptionUtils.getExceptionMessage(e));
        }
    }
    /**
     * Process the worker request. This will delegate rendering regular IChannel's or sub channels
     * @param pcs a <code>PortalControlStructures</code> object
     * @exception PortalException if an error occurs
     */
    public void processWorkerDispatch(PortalControlStructures pcs)
    throws PortalException {
        HttpServletRequest req = pcs.getHttpServletRequest();
        HttpServletResponse res = pcs.getHttpServletResponse();
        ThemeStylesheetDescription tsd = null;

        try {
            tsd = pcs.getUserPreferencesManager().
                getThemeStylesheetDescription();
        } catch (Exception e) {
            throw new PortalException(e);
        }
            
        res.setContentType(tsd.getMimeType() + "; charset=" +
            characterSet);

        if (Debug.instance().isDebugEnabled()) {
            RuntimeDataUtil.logServletRequestParameters(req);
        }

        initParams(pcs);

        if (hasFrameworkParam(req)) {
            URLUtil.redirect(req, res, getParameter("targetChannel"), false,
                new String[0], Charset.forName(characterSet).toString());
            return;
        }

        // If the SC_TARGET_HANDLE param exists it's a sub channel
        if (getParameter(SuperChannel.SC_TARGET_HANDLE) != null) {
            renderSubChannel(pcs);
        } else {
            renderChannel(pcs);
        }
    }

    private void noSessionRedirect(PortalControlStructures pcs)
    throws PortalException {
        try {
            HttpServletResponse res = pcs.getHttpServletResponse();
            res.sendRedirect("html/net/unicon/cscr/wrong_frame.html");
        } catch (IOException ioe) {
            throw new PortalException(ioe);
        }
    }

    private void renderSuperChannel(PortalControlStructures pcs)
    throws PortalException {
        try {
            // This redirect will send the request back through this worker,
            // but since there isn't a scTargetChannel param in the extras
            // field, it will get routed to the renderChannel method
            // where the super channel will get rendered with no target
            // sub channel. We cannot just call the renderChannel method
            // directly because the ChannelManager will not look at the
            // UPFileSpec we create and pass to it to determine the runtime
            // parameters to pass to the channels. This is to prevent
            // double commands...
            HttpServletRequest req = pcs.getHttpServletRequest();
            HttpServletResponse res = pcs.getHttpServletResponse();
            UPFileSpec up = createUPFileSpec(req);
            up.setTargetNodeId(getParameter("targetChannel"));
            up.setUPFileExtras(null);

            // No need to go through the URLUtil, we aren't going to pass
            // any runtime data.
            res.sendRedirect(up.getUPFile());
        } catch (IOException ioe) {
            throw new PortalException(ioe);
        }
    }

    private boolean hasFrameworkParam(HttpServletRequest req) {
        Enumeration e = req.getParameterNames();
        while (e.hasMoreElements()) {
            if (((String)e.nextElement()).indexOf(frameworkPrefix) == 0) {
                return true;
            }
        }

        return false;
    }

    private String getParameter(String key) {
        return (String)inputParams.get(key);
    }

    // parse the UPFileSpec and return the 
    // results in a Map that will contain name/value pairs
    private void initParams(PortalControlStructures pcs)
    throws PortalException {
        
        HttpServletRequest req = pcs.getHttpServletRequest();
        UPFileSpec up = createUPFileSpec(req);

        String parentUID = req.getSession(false).getId() + "/" +
            up.getTargetNodeId();
        inputParams.put("cscrParentId", up.getTargetNodeId());
        inputParams.put("parentUID", parentUID);
        inputParams.put("targetChannel", up.getTargetNodeId());

        extras = RenderingUtil.parseExtrasField(
            createUPFileSpec(pcs.getHttpServletRequest()));
        inputParams.putAll(extras);
    }

    // create a UPFileSpec based on the servlet request object
    // which is potentially altered based on the browser
    private UPFileSpec createUPFileSpec(HttpServletRequest req)
    throws PortalException {
        UPFileSpec up = new UPFileSpec(req);
        if (!CscrBrowserRegistry.instance().isEnabled(new BrowserInfo(req))) {
            up.setMethod(UPFileSpec.RENDER_METHOD);
            up.setMethodNodeId(IALFolderDescription.ROOT_FOLDER_ID);
        }
        return up;
    }

    /**
     * Render a sub-channel. This will render the specified sub channel and any subsequent dirty channels.
     * @param pcs a <code>PortalControlStructures</code> object
     * @exception PortalException if an error occurs
     */
    public void renderSubChannel(PortalControlStructures pcs)
    throws PortalException {
        LogService.instance().log(LogService.DEBUG,
        "ChannelRendererWorker : renderSubChannel()");

        HttpServletRequest  req = pcs.getHttpServletRequest();
        HttpServletResponse res = pcs.getHttpServletResponse();

        // verify the requestor has a session data object
        String parentUID = getParameter("parentUID");
        if (!ChannelDataManager.hasChannelData(parentUID)) {
            noSessionRedirect(pcs);
            return;
        }

        String chMode   = (String)ChannelDataManager.getAttribute(parentUID,
            SuperChannel.CHANNEL_MODE_KEY);
        String ccHandle = getParameter(SuperChannel.SC_TARGET_HANDLE);
        ChannelMode mode = null;
        ChannelClass targetcc = null;
        ChannelClass cc = null;
        boolean targetRerendered = false;
        HashMap args = new HashMap(1);
        String name;
        Object[] objectValue;
        Iterator itr = null;

        try {
            mode = ChannelMode.getChannelMode(chMode);
            targetcc = ChannelClassFactory.getChannelClass(ccHandle);
            
            // added creation of parameter map - moved from startChannel and
            // passed into that method.
            Enumeration en = req.getParameterNames();
            Hashtable targetParams = new Hashtable();
            if (en != null) {
                while (en.hasMoreElements()) {
                    name = (String) en.nextElement();
                    objectValue = (Object[]) req.getParameterValues(name);
                    if (objectValue == null) {
                        objectValue = ((IRequestParamWrapper) req
                            ).getObjectParameterValues(name);
                    }
                    if (objectValue != null) {
                        //RuntimeDataUtil.escapeParameters(objectValue);
                        targetParams.put(name, objectValue);
                    }
                }
            }

            // add any extra params to runtime data
            itr = extras.keySet().iterator();
            objectValue = new String[1];
            while (itr.hasNext()) {
                name = (String)itr.next();
                objectValue[0] = extras.get(name);
                targetParams.put(name, objectValue);
            }

            // record channel targeted
            StatsRecorderWrapper.getInstance().recordChannelTargeted(
            	SubChannelFactory.getSubChannelSubscribeId(getParameter("cscrParentId"), ccHandle),
            	targetcc, PersonManagerFactory.getPersonManagerInstance().getPerson(req),
            	pcs.getUserPreferencesManager().getCurrentProfile());
            
            // start rendering the target channel
            ChannelRendererWrapper crw =startChannel(pcs,
                targetcc, mode, targetParams);
            // get the results from the channel
            Object buffer = null;
            buffer = crw.getResults();

            AttributesImpl attrs = new AttributesImpl();
            StringWriter sw = new StringWriter();
            BaseMarkupSerializer serializer =
                SerializerFactory.instance().getSerializer(pcs.getChannelManager().getSerializerName(), sw);
            serializer.asContentHandler();

            if (true) {
                // now spawn threads for subsequent dirty channels
                List crList = new ArrayList();
                List dirtyChannels = new ArrayList();
                boolean renderSuperChannel =
                    getDirtyChannels(dirtyChannels, pcs, mode, parentUID);

                // if the navigation channel is dirty, just direct the
                // whole rendering to the super channel
                if (renderSuperChannel) {
                    renderSuperChannel(pcs);
                    return;
                }

                itr = dirtyChannels.iterator();
                while (itr.hasNext()) {
                    cc = (ChannelClass)itr.next();
                    crList.add(startChannel(pcs, cc, mode, new HashMap()));
                    if (cc.getClassName().equals(targetcc.getClassName())) {
                        targetRerendered = true;
                    }
                    LogService.instance().log(LogService.DEBUG,
                    "ChannelRendererWorker::renderSubChannel() : rerendering dirty channel: " + cc.getClassName());
                }
                // retrieve content from threads and serialize
                itr = crList.iterator();
                while (itr.hasNext()) {
                    crw = (ChannelRendererWrapper)itr.next();
                    cc = crw.getChannelClass();
                    String subId =
                    SubChannelFactory.getSubChannelSubscribeId(parentUID, cc);
                    attrs.clear();
                    attrs.addAttribute("", "id", "id", "CDATA",
                    divTagIdPrefix + subId + divTagIdSuffix);
                    serializer.startElement("", "div", "div", attrs);
                    serializeBuffer(serializer, crw.getResults(),
                        pcs.getChannelManager(),
                        SubChannelFactory.getSubChannelSubscribeId(
                            parentUID, targetcc), pcs);
                    serializer.endElement("", "div", "div");
                }

            }

            // serialize original channel, if it wasn't re-rendered
            if (!targetRerendered) {
                attrs.clear();
                attrs.addAttribute("", "id", "id", "CDATA",
                    divTagIdPrefix + 
                    SubChannelFactory.getSubChannelSubscribeId(parentUID,
                    targetcc) + divTagIdSuffix);
                serializer.startElement("", "div", "div", attrs);
                serializeBuffer(serializer, buffer, pcs.getChannelManager(),
                    SubChannelFactory.getSubChannelSubscribeId(
                        parentUID, targetcc), pcs);
                serializer.endElement("", "div", "div");
            }

            args.put("DATA", sw.toString());
            res.getWriter().print(fragment.format(args));

        } catch (IOException e) {
            throw new PortalException(e);
        } catch (SAXException e) {
            throw new PortalException(e);
        } catch (Exception e) {
            throw new PortalException(e);
        }
    }
    /**
     * Serialize a content buffer. This will take the buffer and feed it into the serializer. It will
     * also store the character cache if necessary.
     * @param serializer a <code>BaseMarkupSerializer</code> object
     * @param buffer a <code>SAX2BufferImpl</code> or <code>String</code> object
     * @param cm a <code>ChannelManager</code> object
     * @param subscribeId a <code>String</code> object
     * @exception PortalException if an error occurs
     */
    protected void serializeBuffer(BaseMarkupSerializer serializer,
    Object buffer, ChannelManager cm, String subscribeId, PortalControlStructures pcs)
    throws PortalException {
        if (!(serializer instanceof CachingSerializer)) {
            throw new PortalException("ChannelRendererWorker::serializeBuffer : Unsupposed serializer: " +
                serializer != null ? serializer.getClass().getName() : "NULL");
        }
        CachingSerializer cachingSerializer = (CachingSerializer)serializer;
        try {
            if (buffer instanceof String) {
                cachingSerializer.printRawCharacters((String)buffer);
                LogService.instance().log(LogService.DEBUG,
                "ChannelRendererWorker::serializeBuffer() : channel character buffer contents: " + subscribeId);
                if (showChannelResults) {
                    LogService.instance().log(LogService.INFO, (String)buffer);
                }
            } else if (buffer instanceof SAX2BufferImpl) {
                if (UserInstance.CHARACTER_CACHE_ENABLED &&
                !cachingSerializer.startCaching()) {
                    LogService.instance().log(LogService.ERROR,
                    "ChannelRendererWorker::serializeBuffer() : " +
                    "unable to restart channel cache on rendering!");
                }
                ((SAX2BufferImpl)buffer).outputBuffer(serializer);
                if (UserInstance.CHARACTER_CACHE_ENABLED &&
                    cachingSerializer.stopCaching()) {
/* ZZZ must rework character caching due to differences between 2.0.x and 2.1
                    try {
                        cm.setChannelCharacterCache(
                        subscribeId, serializer.getCache());
                    } catch (UnsupportedEncodingException e) {
                        LogService.instance().log(LogService.ERROR,
                        "ChannelRendererWorker::serializeBuffer() : " +
                        "unable to obtain character cache, " +
                        "invalid encoding specified ! " + e);
                    } catch (IOException ioe) {
                        LogService.instance().log(LogService.ERROR,
                        "ChannelRendererWorker::serializeBuffer() : " +
                        "IO exception occurred while retreiving " +
                        "character cache ! " + ioe);
                    }
*/
                } else {
                    LogService.instance().log(LogService.ERROR,
                    "ChannelRendererWorker::serializeBuffer() : " +
                    "unable to reset cache state! " +
                    "Serializer was not caching when it should've been !");
                }
                LogService.instance().log(LogService.DEBUG,
                "ChannelRendererWorker::serializeBuffer() : channel sax buffer contents: " + subscribeId);
                if (showChannelResults) {
                    LogService.instance().log(LogService.INFO,
                    PortalSAXUtils.serializeSAX(cm.getSerializerName(), (SAX2BufferImpl)buffer));
                }
            } else {
                StringBuffer msg = new StringBuffer();
                msg.append("Invalid buffer class: ");
                msg.append(buffer.getClass().toString());
                LogService.instance().log(LogService.ERROR,
                msg.toString());
                throw new PortalException(msg.toString());
            }
        } catch (SAXException e) {
            throw new PortalException(e);
        } catch (IOException e) {
            throw new PortalException(e);
        }
    }
    
    protected ChannelRendererWrapper startChannel(PortalControlStructures pcs,
    ChannelClass cc, ChannelMode mode, Map targetParams)
    throws PortalException {
        try {
            HttpServletRequest req = pcs.getHttpServletRequest();
            HttpServletResponse res = pcs.getHttpServletResponse();
            IPerson person =
            PersonManagerFactory.getPersonManagerInstance().getPerson(req);
            String username = (String) person.getAttribute(uidKey);
            User user = UserFactory.getUser(username);
            String parentSubscribeId = getParameter("cscrParentId");
            String parentUID = req.getSession(false).getId() + "/" +
            parentSubscribeId;
            IAuthorizationPrincipal principal = VersionResolver.getInstance().
                getPrincipalByPortalVersions(person);
            IChannel ch = SubChannelFactory.getChannelInstance(parentUID,
                cc, new ChannelStaticData(), person, principal,
                pcs.getUserPreferencesManager());
            String subChannelUID = SubChannelFactory.getSubChannelUID(
            parentUID, cc);
            String subId =
            SubChannelFactory.getSubChannelSubscribeId(parentUID, cc);
            // Set the channel dirty so it will always render
//            ChannelDataManager.setDirty(
//            SubChannelFactory.getSubChannelUID(parentUID, cc),
//            true);
            ChannelManager cm = pcs.getChannelManager();
            ChannelRuntimeData rd = new ChannelRuntimeData();
            rd.setParameters(targetParams);
            rd.setBrowserInfo(new BrowserInfo(req));
            rd.setParameter(SuperChannel.channelUidKey, subId);
            rd.setParameter(SuperChannel.parentIdKey, subChannelUID);
            rd.setParameter("targetChannel",
                getParameter(SuperChannel.SC_TARGET_HANDLE));
            rd.setParameter("skin", PortalControlStructuresUtil.
                getThemeStylesheetUserPreferences(pcs).
                getParameterValue("skin"));
            UPFileSpec up = createUPFileSpec(req);
            up.setTargetNodeId(parentSubscribeId);

            // set the scTargetHandle
            StringBuffer extras = new StringBuffer(512);
            extras.append(SuperChannel.SC_TARGET_HANDLE).append(
                UPFileSpec.PORTAL_URL_SEPARATOR);
            extras.append(cc.getHandle());
            up.setUPFileExtras(extras.toString());
            rd.setUPFile(up);
            rd.setParameter("workerActionURL",
                rd.getBaseWorkerURL(UPFileSpec.FILE_DOWNLOAD_WORKER, true) +
                "?download=true");

            Role role = RoleFactory.getUserRoleForOfferingChannel(user, cc);
            IGroup group = null;
            if (role != null) {
                group = role.getGroup();
            }
            IPermissions p = PermissionsUtil.getPermissions(group, cc, person);
            ChannelDataManager.setPermissions(subChannelUID, p);

            ChannelRendererWrapper crw = new ChannelRendererWrapper(
                parentUID, pcs, cc, ch, cm, cc.getTimeout(), rd, person, true);

            ChannelCacheTable table =
                (ChannelCacheTable)req.getSession(false).getAttribute(
                "net.unicon.portal.channels.cacheTables");
            if (table == null) {
                table = new ChannelCacheTable();
                req.getSession(false).setAttribute(
                    "net.unicon.portal.channels.cacheTables", table);
            }
            crw.startRendering(table);
            return crw;
        } catch (ItemNotFoundException e) {
            throw new PortalException(e);
        } catch (OperationFailedException e) {
            throw new PortalException(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new PortalException(e);
        }
    }

    // this will gather all the dirty channels as a result
    // of the target sub channel's rendering.
    // If any of the dirty channels are in the channelsThatDirtySC
    // array, the method will return true indicating to render the whole
    // super channel.
    protected boolean getDirtyChannels(List dirtyChannels,
        PortalControlStructures pcs, ChannelMode mode, String parentUID)
    throws PortalException {
        try {
            HttpServletRequest req = pcs.getHttpServletRequest();
            Iterator channelItr = null;
            if (ChannelMode.OFFERING.equals(mode)) {
                Offering offering = ChannelDataManager.getDomainUser(parentUID).
                    getContext().getCurrentOffering(TopicType.ACADEMICS);
                List channels = ChannelClassFactory.getChannelClasses(
                    ChannelMode.GLOBAL);
                channels.addAll(offering.getChannels(mode));
                channelItr = channels.iterator();
            } else if (ChannelMode.ADMIN.equals(mode) ||
                       ChannelMode.SUBSCRIPTION.equals(mode)) {
                List channels = ChannelClassFactory.getChannelClasses(
                    ChannelMode.GLOBAL);
                channels.addAll(ChannelClassFactory.getChannelClasses(mode));
                channelItr = channels.iterator();
            } else {
                StringBuffer sb = new StringBuffer();
                sb.append("ChannelRendererWorker::getDirtyChannels() ");
                sb.append("Invalid Channel Mode: " + mode);
                throw new PortalException(sb.toString());
            }
            ChannelClass cc = null;
            String uid = null;
            IChannel ch = null;
            while (channelItr.hasNext()) {
                cc = (ChannelClass)channelItr.next();
                uid = SubChannelFactory.getSubChannelUID(parentUID, cc);
                LogService.instance().log(LogService.DEBUG,
                    "ChannelRendererWorker::renderSubChannel() : " +
                    "Checking channel: " + cc.getHandle());
                if (ChannelDataManager.isDirty(uid)) {
                    if (channelsThatDirtySC.contains(cc.getHandle())) {
                        LogService.instance().log(LogService.DEBUG,
                            "ChannelRendererWorker::renderSubChannel() : " +
                            "Super Channel dirty");
                        return true;
                    } else {
                        LogService.instance().log(LogService.DEBUG,
                            "ChannelRendererWorker::renderSubChannel() : " +
                            "channel is dirty: " + cc.getHandle());
                        dirtyChannels.add(cc);
                    }
                }
            }
            return false;
        } catch (DomainException e) {
            throw new PortalException(e);
        } catch (NumberFormatException e) {
            throw new PortalException(e);
        }
    }

    protected void renderChannel(PortalControlStructures pcs)
    throws PortalException {
        LogService.instance().log(LogService.DEBUG,
        "ChannelRendererWorker : renderChannel()");
        HttpServletRequest req = pcs.getHttpServletRequest();
        HttpServletResponse res = pcs.getHttpServletResponse();

        String subscribeId = getParameter("targetChannel");
        Map args = new HashMap(1);
        Hashtable params = new Hashtable();
        try {
            String publishIdStr = PortalControlStructuresUtil.
                getChannelPublishId(pcs, subscribeId);

            // if the server was restarted and someone clicks a stale cscr
            // link, no channel registry will exist and therefore no
            // channel publish ID can be retrieved. Force the client js
            // to go through the uportal front door.
            if (publishIdStr == null) {
                noSessionRedirect(pcs);
                return;
            }

            ChannelDefinition cd = ChannelRegistryStoreFactory.
                getChannelRegistryStoreImpl().
                    getChannelDefinition(Integer.parseInt(publishIdStr));
            params.putAll(ChannelDefinitionUtil.getParameters(cd));
            ChannelManager cm = pcs.getChannelManager();
            
            UPFileSpec up = createUPFileSpec(req);
            up.setTargetNodeId(subscribeId);

            cm.startRenderingCycle(req, res, up);
            
            // Build the page
            AttributesImpl attrs = new AttributesImpl();
            StringWriter sw = new StringWriter();
            BaseMarkupSerializer serializer =
                SerializerFactory.instance().getSerializer(cm.getSerializerName(), sw);
            serializer.asContentHandler();
            attrs.clear();
            attrs.addAttribute("", "id", "id", "CDATA",
            divTagIdPrefix + subscribeId + divTagIdSuffix);
            serializer.startElement("", "div", "div", attrs);
            cm.startChannelRendering(subscribeId);
            cm.outputChannel(subscribeId, serializer);
            serializer.endElement("", "div", "div");

            args.put("DATA", sw.toString());

            res.getWriter().print(fragment.format(args));

            cm.finishedRenderingCycle();

        } catch (IOException e) {
            throw new PortalException(e);
        } catch (Exception e) {
            throw new PortalException(e);
        }
    }
}
