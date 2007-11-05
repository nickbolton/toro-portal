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

// Unicon classes
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.academus.domain.lms.OperationFailedException;
import net.unicon.academus.domain.lms.Role;
import net.unicon.academus.domain.lms.RoleFactory;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.portal.channels.SuperChannel;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.common.properties.PortalPropertiesType;
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
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.UserInstance;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonManagerFactory;
import org.jasig.portal.serialize.BaseMarkupSerializer;
import org.jasig.portal.serialize.CachingSerializer;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.utils.URLUtil;
import org.xml.sax.SAXException;

import com.interactivebusiness.portal.VersionResolver;

public class SingleChannelWorker implements IWorkerRequestProcessor
{
    public static final String BASIC_WORKER_METHOD = "basic";

    private static final String CHARACTER_SET = "UTF-8";

    private static String frameworkPrefix = "uP_";

    private static final boolean showChannelResults =
                    UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).
                        getPropertyAsBoolean(
                            "net.unicon.portal.common.ChannelRendererWorker.showChannelResults");

    private static final String uidKey =
                    UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).
                        getProperty("org.jasig.portal.security.uidKey");

    private Map inputParams;
    private Map extras;

    public SingleChannelWorker()
    {
        inputParams = new HashMap();
        extras      = new HashMap();
    }

   /**
    * Process the worker request. This will delegate rendering for either regular IChannel's
    * or sub channels.
    * @param pcs a <code>PortalControlStructures</code> object
    * @exception PortalException if an error occurs
    */
    public void processWorkerDispatch(PortalControlStructures pcs)
    throws PortalException
    {
        HttpServletRequest req = pcs.getHttpServletRequest();
        HttpServletResponse res = pcs.getHttpServletResponse();
        res.setContentType("text/html; charset=" + CHARACTER_SET);

        if (Debug.instance().isDebugEnabled())
        {
            RuntimeDataUtil.logServletRequestParameters(req);
        }

        // Initialize request parameters
        initParams(pcs);

        if (hasFrameworkParam(req))
        {
            // Redirect back to the framework to handle the framework parameters
            frameworkRedirect(pcs);
        }
        // If the SC_TARGET_HANDLE param exists it's a sub channel
        else if (getParameter(SuperChannel.SC_TARGET_HANDLE) != null)
        {
            // Render a sub channel
            renderSubChannel(pcs);
        }
        else
        {
            // Render a regular channel - not a sub channel
            renderChannel(pcs);
        }
    }

    // Checks if a request includes any framework request parameters
    private boolean hasFrameworkParam(HttpServletRequest req)
    {
            Enumeration e = req.getParameterNames();
            boolean frameworkParams = false;

            while (e.hasMoreElements())
            {
                // Check if a parameter has a framework parameter prefix
                if (((String)e.nextElement()).indexOf(frameworkPrefix) == 0)
                {
                    frameworkParams = true;
                }
            }

            return frameworkParams;
    }

    // Parse the UPFileSpec and return the
    // results in a Map of name/value pairs
    private void initParams(PortalControlStructures pcs)
    throws PortalException
    {
        HttpServletRequest req = pcs.getHttpServletRequest();
        UPFileSpec up = new UPFileSpec(req);

        // Extract target node Id
        String targetNodeId = up.getTargetNodeId();

        // Construct parent user Id
        String parentUID = req.getSession(false).getId() + "/" + targetNodeId;

        // The cscrParent is equal to the target node id if used
        inputParams.put("cscrParentId", targetNodeId);
        inputParams.put("parentUID", parentUID);
        inputParams.put("targetChannel", targetNodeId);

        // Extract extra fields from the UPFileSpec
        extras = RenderingUtil.parseExtrasField(up);
        inputParams.putAll(extras);
    }

   /**
    * Render a regular channel.
    *
    * @param pcs a <code>PortalControlStructures</code> object
    * @exception PortalException if an error occurs
    */
    private void renderChannel(PortalControlStructures pcs)
    throws PortalException
    {
        LogService.instance().log(LogService.DEBUG,
                    "SingleChannelWorker: renderChannel()");

        HttpServletRequest req = pcs.getHttpServletRequest();
        HttpServletResponse res = pcs.getHttpServletResponse();

        String subscribeId = getParameter("targetChannel");
        Hashtable params = new Hashtable();

        try
        {
            // Get channel publish id
            String publishIdStr = PortalControlStructuresUtil.
                getChannelPublishId(pcs, subscribeId);

            // Check for a dead session
            if (publishIdStr == null)
            {
                frameworkRedirect(pcs);
            }
            else
            {
                // Get channel definition
                ChannelDefinition cd = ChannelRegistryStoreFactory.
                            getChannelRegistryStoreImpl().
                                        getChannelDefinition(Integer.parseInt(publishIdStr));

                // Get all channel publishing parameters
                params.putAll(ChannelDefinitionUtil.getParameters(cd));

                ChannelManager cm = pcs.getChannelManager();

                // Build a UPFileSpec from the request using the request
                // and set the target node id
                UPFileSpec up = new UPFileSpec(req);
                up.setTargetNodeId(subscribeId);

                // Start the rendering cycle
                cm.startRenderingCycle(req, res, up);

                // Build the page
                StringWriter sw = new StringWriter();
                BaseMarkupSerializer serializer =
                    SerializerFactory.instance().getSerializer(cm.getSerializerName(), sw);
                serializer.asContentHandler();

                // Start rendering this channel
                cm.startChannelRendering(subscribeId);

                // Get channel output
                cm.outputChannel(subscribeId, serializer);

                // Write output to the response
                res.getWriter().print(sw.toString());

                // Finish rendering cycle
                cm.finishedRenderingCycle();
            }
        }
        catch (IOException e)
        {
            LogService.instance().log(LogService.ERROR,
                        "SingleChannelWorker::renderChannel() " +
                        "Rendering a regular channel:" + e);

            throw new PortalException(e);
        }
        catch (Exception e)
        {
            LogService.instance().log(LogService.ERROR,
                        "SingleChannelWorker::renderChannel() " +
                        "Rendering a regular channel:" + e);

            throw new PortalException(e);
        }
    }

    // Returns a request parameter value for the specified key
    private String getParameter(String key)
    {
        return (String) inputParams.get(key);
    }

    // Redirects a request back to the framework
    private void frameworkRedirect(PortalControlStructures pcs)
    throws PortalException
    {
        HttpServletRequest req = pcs.getHttpServletRequest();
        HttpServletResponse res = pcs.getHttpServletResponse();

        // Redirect back to the framework
        URLUtil.redirect(req, res, getParameter("targetChannel"), false,
            new String[0], Charset.forName(CHARACTER_SET).toString());
    }

   /**
    * Renders a sub-channel. This will render the specified sub channel.
    * It does not consider channel dirtying and only renders the content for
    * a single channel.
    * @param pcs a <code>PortalControlStructures</code> object
    * @exception PortalException if an error occurs
    */
    private void renderSubChannel(PortalControlStructures pcs)
    throws PortalException
    {
        LogService.instance().log(LogService.DEBUG,
                        "SingleChannelWorker: renderSubChannel()");

        HttpServletRequest  req = pcs.getHttpServletRequest();
        HttpServletResponse res = pcs.getHttpServletResponse();

        String parentUID = getParameter("parentUID");

        // Check for a dead session
        if (!ChannelDataManager.hasChannelData(parentUID))
        {
            frameworkRedirect(pcs);
        }
        else
        {
            String chMode   = (String) ChannelDataManager.
                            getAttribute(parentUID, SuperChannel.CHANNEL_MODE_KEY);

            String ccHandle = getParameter(SuperChannel.SC_TARGET_HANDLE);

            ChannelMode mode = null;
            ChannelClass targetcc = null;
            ChannelClass cc = null;
            boolean targetRerendered = false;
            HashMap args = new HashMap(1);
            String name;
            Object[] objectValue;
            Iterator itr = null;

            try
            {
                mode = ChannelMode.getChannelMode(chMode);
                targetcc = ChannelClassFactory.getChannelClass(ccHandle);

                // Extract request parameters
                Enumeration en = req.getParameterNames();
                Hashtable targetParams = new Hashtable();

                // Check if there were any parameters at all
                if (en != null)
                {
                    while (en.hasMoreElements())
                    {
                        name = (String) en.nextElement();
                        objectValue = (Object[]) req.getParameterValues(name);
                        if (objectValue == null)
                        {
                            objectValue =
                                ((IRequestParamWrapper) req).
                                            getObjectParameterValues(name);
                        }

                        if (objectValue != null)
                        {
                            // Escape parameter and add it to the target parameter map
                            RuntimeDataUtil.escapeParameters(objectValue);
                            targetParams.put(name, objectValue);
                        }
                    }
                }

                // Add any extra params to runtime data
                itr = extras.keySet().iterator();
                objectValue = new String[1];

                while (itr.hasNext())
                {
                    name = (String) itr.next();
                    objectValue[0] = extras.get(name);
                    targetParams.put(name, objectValue);
                }
                
                // record channel targeted
                StatsRecorderWrapper.getInstance().recordChannelTargeted(
                	SubChannelFactory.getSubChannelSubscribeId(getParameter("cscrParentId"), ccHandle),
                	targetcc, PersonManagerFactory.getPersonManagerInstance().getPerson(req),
                	pcs.getUserPreferencesManager().getCurrentProfile());

                // Start rendering the target channel
                ChannelRendererWrapper crw = startChannel(pcs, targetcc, mode, targetParams);

                // Get the results from the channel
                Object buffer = null;
                buffer = crw.getResults();

                StringWriter sw = new StringWriter();

                BaseMarkupSerializer serializer =
                    SerializerFactory.instance().getSerializer(
                        pcs.getChannelManager().getSerializerName(), sw);
                
                if (!(serializer instanceof CachingSerializer)) {
                    throw new PortalException("SingleChannelWorker::renderSubChannel : Unsupposed serializer: " +
                        serializer != null ? serializer.getClass().getName() : "NULL");
                }
                
                CachingSerializer cachingSerializer = (CachingSerializer)serializer;

                serializer.asContentHandler();

                // Start serializer caching
                cachingSerializer.startCaching();

                // Serialize buffer contents
                serializeBuffer(serializer, buffer, pcs.getChannelManager(),
                                SubChannelFactory.getSubChannelSubscribeId(
                                    parentUID, targetcc), pcs);

                // Stop serializer caching
                cachingSerializer.stopCaching();

                // Write content to the response
                res.getWriter().print(sw.toString());
            }
            catch (IOException e)
            {
                LogService.instance().log(LogService.ERROR,
                        "SingleChannelWorker::renderSubChannel() " +
                        "Rendering a sub channel:" + e);

                throw new PortalException(e);
            }
            catch (SAXException e)
            {
                LogService.instance().log(LogService.ERROR,
                        "SingleChannelWorker::renderSubChannel() " +
                        "Rendering a sub channel:" + e);

                throw new PortalException(e);
            }
            catch (Exception e)
            {
                LogService.instance().log(LogService.ERROR,
                        "SingleChannelWorker::renderSubChannel() " +
                        "Rendering a sub channel:" + e);

                throw new PortalException(e);
            }
        }
    }

    // Starts channel rendering and returns a wrapped IChannel instance
    private ChannelRendererWrapper startChannel(PortalControlStructures pcs,
        ChannelClass cc, ChannelMode mode, Map targetParams)
    throws PortalException
    {
        try
        {
            HttpServletRequest req = pcs.getHttpServletRequest();
            HttpServletResponse res = pcs.getHttpServletResponse();

            IPerson person =
                    PersonManagerFactory.
                        getPersonManagerInstance().getPerson(req);

            String username = (String) person.getAttribute(uidKey);

            User user = UserFactory.getUser(username);

            String parentSubscribeId = getParameter("cscrParentId");
            String parentUID = req.getSession(false).getId() + "/" + parentSubscribeId;

            IAuthorizationPrincipal principal = VersionResolver.getInstance().
                getPrincipalByPortalVersions(person);

            IChannel ch =
                SubChannelFactory.getChannelInstance(
                    parentUID, cc, new ChannelStaticData(), person, principal,
                    pcs.getUserPreferencesManager());

            String subChannelUID =
                SubChannelFactory.getSubChannelUID(parentUID, cc);

            String subId =
                SubChannelFactory.getSubChannelSubscribeId(parentUID, cc);


            // Set up runtime data
            ChannelManager cm = pcs.getChannelManager();
            ChannelRuntimeData rd = new ChannelRuntimeData();
            rd.setParameters(targetParams);
            rd.setBrowserInfo(new BrowserInfo(req));
            rd.setParameter(SuperChannel.parentIdKey, parentSubscribeId);
            rd.setParameter(SuperChannel.channelUidKey, subId);
            rd.setParameter("targetChannel",
                getParameter(SuperChannel.SC_TARGET_HANDLE));
            rd.setParameter("skin", PortalControlStructuresUtil.
                getThemeStylesheetUserPreferences(pcs).
                getParameterValue("skin"));

            // Build UPFileSpec
            UPFileSpec up = new UPFileSpec(req);
            up.setTargetNodeId(parentSubscribeId);

            // Set the scTargetHandle
            StringBuffer extras = new StringBuffer(512);
            extras.append(SuperChannel.SC_TARGET_HANDLE).
                        append(UPFileSpec.PORTAL_URL_SEPARATOR);
            extras.append(cc.getHandle());
            up.setUPFileExtras(extras.toString());
            rd.setUPFile(up);
            rd.setParameter("workerActionURL",
                rd.getBaseWorkerURL(UPFileSpec.FILE_DOWNLOAD_WORKER, true) + "?download=true");


            // Get user permissions
            Role role = RoleFactory.getUserRoleForOfferingChannel(user, cc);
            IGroup group = null;
            if (role != null) {
                group = role.getGroup();
            }
            IPermissions p = PermissionsUtil.getPermissions(group, cc, person);
            ChannelDataManager.setPermissions(subChannelUID, p);

            // Wrap IChannel instance
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
        }
        catch (ItemNotFoundException e)
        {
            LogService.instance().log(LogService.ERROR,
                        "SingleChannelWorker::startChannel() " +
                        "Start sub channel rendering:" + e);

            throw new PortalException(e);
        }
        catch (OperationFailedException e)
        {
            LogService.instance().log(LogService.ERROR,
                        "SingleChannelWorker::startChannel() " +
                        "Start sub channel rendering:" + e);

            throw new PortalException(e);
        }
        catch (Exception e)
        {
            LogService.instance().log(LogService.ERROR,
                        "SingleChannelWorker::startChannel() " +
                        "Start sub channel rendering:" + e);

            throw new PortalException(e);
        }
    }

   /**
    * Serialize a content buffer. This will take the buffer and feed it into the serializer.
    *
    * @param serializer a <code>BaseMarkupSerializer</code> object
    * @param buffer a <code>SAX2BufferImpl</code> or <code>String</code> object
    * @param cm a <code>ChannelManager</code> object
    * @param subscribeId a <code>String</code> object
    * @exception PortalException if an error occurs
    */
    private void serializeBuffer(BaseMarkupSerializer serializer,
                    Object buffer, ChannelManager cm, String subscribeId,
                    PortalControlStructures pcs)
    throws PortalException
    {
        if (!(serializer instanceof CachingSerializer)) {
            throw new PortalException("SingleChannelWorker::serializeBuffer : Unsupposed serializer: " +
                serializer != null ? serializer.getClass().getName() : "NULL");
        }
        CachingSerializer cachingSerializer = (CachingSerializer)serializer;
        
        try
        {
            // Determine the type of buffer passed
            if (buffer instanceof String)
            {
                // Serialize characters from String buffer
                cachingSerializer.printRawCharacters((String) buffer);

                LogService.instance().log(LogService.DEBUG,
                        "SingleChannelWorker::serializeBuffer():" +
                            "channel character buffer contents: " + subscribeId);

                if (showChannelResults)
                {
                    LogService.instance().log(LogService.DEBUG, (String) buffer);
                }

            }
            else if (buffer instanceof SAX2BufferImpl)
            {
                if (UserInstance.CHARACTER_CACHE_ENABLED && !cachingSerializer.startCaching())
                {
                    LogService.instance().log(LogService.ERROR,
                        "SingleChannelWorker::serializeBuffer() : " +
                            "unable to restart channel cache on rendering!");
                }

                // Serialize SAX buffer content
                ( (SAX2BufferImpl) buffer).outputBuffer(serializer);

                LogService.instance().log(LogService.DEBUG,
                    "SingleChannelWorker::serializeBuffer() :" +
                        "channel sax buffer contents: " + subscribeId);

                if (showChannelResults)
                {
                    LogService.instance().log(LogService.DEBUG,
                        PortalSAXUtils.serializeSAX(cm.getSerializerName(), (SAX2BufferImpl) buffer));
                }
            }
            // Unknown buffer type
            else
            {
                StringBuffer msg = new StringBuffer();
                msg.append("Invalid buffer class: ");
                msg.append(buffer.getClass().toString());
                LogService.instance().log(LogService.ERROR, msg.toString());

                throw new PortalException(msg.toString());
            }

        }
        catch (SAXException e)
        {
            LogService.instance().log(LogService.ERROR,
                        "SingleChannelWorker::serializeBuffer() " +
                        "Serializing result buffer:" + e);

            throw new PortalException(e);
        }
        catch (IOException e)
        {
            LogService.instance().log(LogService.ERROR,
                        "SingleChannelWorker::serializeBuffer() " +
                        "Serializing result buffer:" + e);

            throw new PortalException(e);
        }
    }
}
