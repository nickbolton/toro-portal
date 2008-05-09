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

import net.unicon.academus.domain.lms.Context;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.Role;
import net.unicon.academus.domain.lms.RoleFactory;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.producer.ContentProducerException;
import net.unicon.academus.producer.IProducer;
import net.unicon.academus.producer.ProducerType;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.common.SubChannelFactory;
import net.unicon.portal.util.ChannelDefinitionUtil;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.permissions.PermissionsService;
import net.unicon.sdk.authentication.PasswordUtil;

import com.interactivebusiness.portal.VersionResolver;

import org.jasig.portal.BrowserInfo;
import org.jasig.portal.PortalException;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.services.LogService;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.utils.XSLT;

import org.xml.sax.SAXException;

import java.util.Map;
import java.util.HashMap;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;

public final class ProducerRenderer {

    private static ProducerRenderer _instance = null;

    public static synchronized ProducerRenderer instance() {
        if (_instance == null) {
            _instance = new ProducerRenderer();
        }
        return _instance;
    }

    public ProducerResults produceContent(String upId,
                      ChannelRuntimeData rd,
                      User user,
                      IProducer producer, 
                      Object caller)
    throws PortalException {

        ProducerResults producerResults = null;
    Map results = null;
    
    LogService.instance().log(LogService.DEBUG, "PRODUCING CONTENT IN PRODUCERRENDERER");
        try {
            Map producerData = new HashMap(rd);
            String[] command = (String[])producerData.get(IProducer.COMMAND);

            if (command == null) {
                command = new String[1];
                command[0] = IProducer.DEFAULT_COMMAND;
                producerData.put(IProducer.COMMAND, command);
            }

            if ("editPermissions".equals(command[0])) {
                // bypass the produce and produce an blank tag
                results = new HashMap();
                results.put(IProducer.VIEW_NAME, "blank");
                results.put(IProducer.XML, "<blank/>");
                return renderXmlType(upId, rd, caller, results);
            }

            // 2 ways to get the appId - only on the first way do we
        // need to initialize the connection
            if (producerData.get(IProducer.APP_ID) == null) {
                String[] appID = new String[1];
                appID[0] = initAppConnection(upId, producer, user); 

                producerData.put(IProducer.APP_ID, appID);
            }

            // Ask the producer for content
            results = producer.getContent(producerData);
            Integer resultsCode = (Integer)results.get(IProducer.STATUS);
            String content = null;

            if (resultsCode.equals(IProducer.OK)) {
                if (producer.getType().equals(ProducerType.HTML)) {
                    content = (String)results.get(IProducer.HTML);
                    if (content == null) {
                        StringBuffer errormsg = new StringBuffer();
                        errormsg.append("Producer: ").append(producer.getClass());
                        errormsg.append(" is an HTML type but did not return ");
                        errormsg.append("any content.");
                        throw new PortalException(errormsg.toString());
                    }
                } else if (producer.getType().equals(ProducerType.XML)) {
                    return renderXmlType(upId, rd, caller, results);
                }
            } else {
                String errormsg = (String)results.get(IProducer.STATUS_MSG);

                StringBuffer xmlSB = new StringBuffer();
                xmlSB.append("<errorPage><message>" + errormsg +
                    "</message></errorPage>");

                ChannelRuntimeData clonerd =
                    (ChannelRuntimeData)rd.clone();

                // bypass cscr
                ChannelClass cc = ChannelDataManager.getChannelClass(upId);
                String parentUID = SubChannelFactory.getParentUID(upId);
                UPFileSpec upfs = null;
                if (cc != null && parentUID != null) {
                    upfs = RenderingUtil.setupUPURL(parentUID, cc,
                        clonerd.getBrowserInfo(), false);
                } else {
                    upfs = RenderingUtil.setupUPURL(upId,
                        clonerd.getBrowserInfo(), false);
                }
                clonerd.setUPFile(upfs);	 

                Map xslParams = setupXSLParameters(clonerd, upId);
                StringWriter sw = new StringWriter();
                PrintWriter out = new PrintWriter(sw);
                transform(upId, clonerd.getBrowserInfo(), out,
                    "/net/unicon/portal/channels/error/error.ssl",
                    "error", xmlSB.toString(), xslParams, caller);
                  content = sw.toString();
            }
            producerResults = new ProducerResults(results, content);
        } catch (Exception e) {
        e.printStackTrace();
            throw new PortalException(e);
        } 
	/* DEBUG ONLY
	   //net.unicon.common.util.debug.DebugUtil.spillParameters(producerResults.getReturnData(),
	   "PR results returned: ");
	   //net.unicon.common.util.debug.DebugUtil.spillParameters(results, 
	   "results returned from Producer: ");
	*/

        return producerResults;
    }

    private ProducerResults renderXmlType(String upId,
        ChannelRuntimeData rd, Object caller, Map results)
    throws Exception {
        // the xsl params that the producer hands
        // back are in the results object
        Map xslParams = setupXSLParameters(rd, upId);
        xslParams.putAll(results);
        addPermissionParams(upId, xslParams);

        String sheet = (String)results.get(IProducer.VIEW_NAME);
        String xml = (String)results.get(IProducer.XML);
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        transform(upId, rd.getBrowserInfo(), out, sheet,
            xml, xslParams, caller);
        return new ProducerResults(results, sw.toString());
    }

    private void addPermissionParams(String upId, Map params)
    throws Exception {

        IPermissions permissions = null;
        IAuthorizationPrincipal principal = null;
        ChannelClass cc = ChannelDataManager.getChannelClass(upId);
        IPerson person = ChannelDataManager.getUPortalUser(upId);

        if (cc != null) {
            IGroup group = null;
            Role role = RoleFactory.getUserRoleForOfferingChannel(
                ChannelDataManager.getDomainUser(upId), cc);
            if (role != null) {
                group = role.getGroup();
            }
        
            permissions = PermissionsUtil.getPermissions(group, cc, person);
        } else {
            principal = VersionResolver.getInstance().
                getPrincipalByPortalVersions(person);
            permissions = PermissionsService.instance().getPermissions(
                principal, ChannelDataManager.getChannelDefinition(upId));
        }

        params.putAll(permissions.getEnumeratedActivities(
            ChannelDataManager.getDomainUser(upId).getUsername()));
    }

    private String initAppConnection(String upId, IProducer producer, User user) throws PortalException
    {
        // Initialize the producer and store the appId
        try {
	    LogService.instance().log(LogService.DEBUG, "INITIALIZING PRODUCER IN CDM.REGISTERCHANNELUSER");

	    // Check for a user's context and offering. We really shouldn't be using Offering
	    // but rather a Group that represents the community of interest of the SuperChannel
	    // the User is currently register this channel subscription in! YYY --KG
	    Context context = user.getContext();
	    Offering offering  = null;
	    if (context != null) {
		offering = context.getCurrentOffering(TopicType.ACADEMICS);
	    }
	    
	    String[] initInfo = null;
	    if (offering != null) {
		initInfo = new String[3];
	    }
	    else {
		initInfo = new String[2];
	    }   
	    initInfo[0] = upId;
	    initInfo[1] = user.getUsername();
	    if (offering != null) {
		  LogService.log(LogService.INFO, "Got an OFFERING in CDM RCU. group id is " + offering.getGroup().getGroupId());
		  initInfo[2] = "" + offering.getGroup().getGroupId();
	    }

	    // need to preserve the upId to appId association in the CDM
	    String appId = producer.allocateAppConnection(initInfo);
	    ChannelDataManager.setApplicationId(upId, appId);

	    return appId;
        } 
    catch (ContentProducerException cpe) {
            throw new PortalException(cpe);
        }
    catch (Exception e) {
        throw new PortalException(e); 
    }
    }

    private Map setupXSLParameters(ChannelRuntimeData rd, String upId)
    throws PortalException {
        Map ht = new HashMap(4);
        // Check if the parameters are specified as a generic (overridden)
        // parameter first
        String instanceId = null;

        try {
            instanceId = PasswordUtil.createHash(upId);
        } catch (java.security.NoSuchAlgorithmException nsae) {
            throw new PortalException(nsae);
        }

        ht.put("instanceId", instanceId);
        if (rd.getParameter("baseActionURL") != null) {
            ht.put("baseActionURL", rd.getParameter("baseActionURL"));
        } else {
            ht.put("baseActionURL", rd.getBaseActionURL());
        }
        if (rd.getParameter("workerActionURL") != null) {
            ht.put("workerActionURL", rd.getParameter("workerActionURL"));
        } else {
            ht.put("workerActionURL",
                rd.getBaseWorkerURL(UPFileSpec.FILE_DOWNLOAD_WORKER, true));
        }
        if (rd.getParameter("skin") != null) {
            ht.put("skin", rd.getParameter("skin"));
        }
        return ht;
    }

    private void transform(String upId, BrowserInfo bi, PrintWriter out,
        String sheet, String xml, Map params, Object caller)
    throws Exception {

        String ssl = null;

        if (ChannelDataManager.getChannelClass(upId) != null) {
            ssl = ChannelDataManager.getChannelClass(upId).getSSLLocation();
        } else {
            ssl = ChannelDefinitionUtil.getParameter(
                    ChannelDataManager.getChannelDefinition(upId),
                        "sslLocation");
        }
        transform(upId, bi, out, ssl, sheet, xml, params, caller);
    }

    private void transform(String upId, BrowserInfo bi, PrintWriter out,
        String ssl, String sheet, String xml, Map params, Object caller)
    throws ResourceMissingException, PortalException, SAXException {
    	XSLT xsl = new XSLT(caller);
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        xsl.setTarget(os);
        xsl.setXML(xml);
        xsl.setXSL(ssl, sheet, bi);
        xsl.setStylesheetParameters(new HashMap(params));
        xsl.transform();
        out.print(os.toString());
    }

    private ProducerRenderer() {}

    public final class ProducerResults {
        Map returnData;
        String content;

        ProducerResults(Map returnData, String content) {
            this.returnData = returnData;
            this.content = content;
        }
    
        public Object put(Object key, Object value) {
            return returnData.put(key, value);
        }

        public void putAll(Map m) {
            returnData.putAll(m);
        }

        public Map getReturnData() {
            return returnData;
        }

        public String getContent() {
            return content;
        }
}
}
