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

package net.unicon.portal.channels.classfolder;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.Iterator;
import java.sql.Connection;
import java.lang.reflect.Method;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import net.unicon.academus.common.DOMAbleEntity;
import net.unicon.academus.common.EntityFactory;
import net.unicon.academus.domain.lms.Context;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.common.BaseOfferingSubChannel;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.sdk.properties.*;
import net.unicon.portal.common.properties.*;
import net.unicon.portal.channels.ChannelException;

import net.unicon.sdk.FactoryCreateException;
import net.unicon.portal.common.service.channel.ChannelService;
import net.unicon.portal.common.service.channel.ChannelServiceFactory;
import net.unicon.portal.common.service.file.FileServiceFactory;

import org.jasig.portal.MultipartDataSource;
import org.jasig.portal.IMultithreadedMimeResponse;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.PortalException;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.services.LogService;

import org.jasig.portal.UploadStatus;
import net.unicon.sdk.util.NodeToString;
import net.unicon.sdk.util.PrintFormat;

import org.w3c.dom.Document;

/**
 * @author Kevin Gary
 * @version 1.0
 */

public class ClassFolderChannel extends BaseOfferingSubChannel implements IMultithreadedMimeResponse {

    // must match what is in the Channels.xml. Couldn't find a

    // method to read it from there...

    protected static String CHANNEL_HANDLE = "ClassFolderChannel";

    protected static final File curriculumFileDir =

    new File(UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.portal.relatedresources.uploadDir"));

    protected static final int maxFileSize =

    PropertiesManager.getPropertyAsInt(

    "org.jasig.portal.RequestParamWrapper.file_upload_max_size");

    protected static final String SIZE_FORMAT = "##0.##";

    protected static DecimalFormat fileSizeFormatter =

    new DecimalFormat(SIZE_FORMAT);

    public ClassFolderChannel() {

        super();

    }

    // IMultithreadedMimeResponse methods

    public String getContentType(String upId) {

        return getRuntimeData(upId).getParameter("filetype");

    }

    /**
     * Let the channel know that there were problems with the download
     * @param e
     */
    public void reportDownloadError(Exception e) {
      LogService.log(LogService.ERROR, "ClassFolderChannel::reportDownloadError(): " + e.getMessage());
    }

    public InputStream getInputStream(String upId) throws IOException {

        Offering offering = getDomainUser(upId).getContext().

        getCurrentOffering(TopicType.ACADEMICS);

        File baseDir = new File(curriculumFileDir, "" + offering.getId());

        String oid =  getRuntimeData(upId).getParameter("oid");

        File file = null;

        try {

            file = FileServiceFactory.getService().getFile(baseDir, oid + "_" + getName(upId));

            if (file == null) {

                throw new IOException ("File " + getName(upId) + " not found.");

            }

        } catch (FactoryCreateException fce) {

            fce.printStackTrace();

            throw new IOException ("Error gettiner FileService");

        }

        return new FileInputStream(file);

    }

    public String getName(String upId) {

        String fileName = null;
        Connection conn = null;

        try {

            User user = super.getDomainUser(upId);

            // Getting Context object for the current user

            Context context = user.getContext();

            // Getting offering Object from the Context Object

            Offering offering  = context.getCurrentOffering(TopicType.ACADEMICS);

            ChannelService channelService = ChannelServiceFactory.getService();

            /* Getting Database Connection */

            conn = super.getDBConnection();

            ClassFolders offeringClassFolders = null;

            String oIDstr = super.getRuntimeData(upId).getParameter("oid");

            fileName = channelService.getFileNameByOID(offering, oIDstr, conn);

        } catch (Exception e) {

            e.printStackTrace();

        } finally {
            releaseDBConnection(conn);
        }

        return fileName;

    }

    public Map getHeaders(String upId) {

        HashMap headers = new HashMap();

        headers.put("Content-Disposition", "attachment; filename=\"" +

        getName(upId) + "\"");

        return headers;

    }

    public void downloadData(OutputStream out, String upId) throws IOException {

    }

    public String exportChannel(Offering offering) throws Exception {

        String upId = ""; // where can I get this from?

        // YYY method not completed, needs upId, H2, Deuce said not used

        //    Connection conn = getDBConnection();

        //    ChannelService channelService = ChannelServiceFactory.getService();

        // ClassFolders classFolders = channelService.getClassFolders(offering, upId, this, conn);

        //    releaseDBConnection(conn);

        // return classFolders.toXML();

        return null;

    }

    public Map importChannel(Offering offering, Document dom)

    throws Exception {

        String xmlString = (new NodeToString(dom)).toString();

        //ChannelService channelService = ChannelServiceFactory.getService();

        Connection conn = null;
        try {
        conn = getDBConnection();

        String factoryName = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("ClassFoldersEntityFactory");

        Class factoryClass = Class.forName(factoryName);

        Method factoryMethod = factoryClass.getMethod("getEntityFactory", null);

        EntityFactory factory = (EntityFactory)(factoryMethod.invoke(null, null));

        Class entityClass = Class.forName("net.unicon.portal.channels.classfolder.ClassFolders");

        Object[] objs = new Object[2];

        objs[0] = xmlString;

        objs[1] = "" + offering.getId();

        ClassFolders rval = (ClassFolders)factory.createEntity(entityClass, objs);

        // OK, now we have to persist this thing

        // YYY H2 and Deuce said this is not called now, so insert not completed

        } finally {
        releaseDBConnection(conn);
        }

        return new HashMap(); // AnnouncementChannel does this too...

    }

    private String getMaxUploadSize () {

        double availableSpace = ((double)maxFileSize) / (1024.0 * 1024.0);

        return (fileSizeFormatter.format(availableSpace) + "MB");

    }

    public void buildXML(String upId) throws Exception {

        ChannelRuntimeData runtimeData = super.getRuntimeData(upId);

        /* check upload status before continuing */
        UploadStatus uploadStatus = 
            (UploadStatus)runtimeData.getObjectParameter(
                "upload_status");
        if (uploadStatus != null 
                && uploadStatus.getStatus() == UploadStatus.FAILURE) {
            StringBuffer errMsg = new StringBuffer(
            "File exceeds max size limit of ");
            errMsg.append(uploadStatus.getFormattedMaxSize());
            setErrorMsg(upId, errMsg.toString());
            return;
        }

        Map args = null;

        // of course, we have to go create this XSLfirst
        // super.setSSLLocation(upId, "ResourceChannel.ssl");
	ChannelDataManager.setSSLLocation(upId, 
	      ChannelDataManager.getChannelClass(upId).getSSLLocation());

        Hashtable classFolderParams = getXSLParameters(upId);


        // Getting User object

        User user = super.getDomainUser(upId);

        // Getting Context object for the current user

        Context context = user.getContext();

        // Getting offering Object from the Context Object

        Offering offering  = context.getCurrentOffering(TopicType.ACADEMICS);

        ChannelService channelService = ChannelServiceFactory.getService();

        /* Getting Database Connection */

        Connection conn = null;

        try {
        conn = super.getDBConnection();

        super.setSheetName(upId, "main");

        String command = runtimeData.getParameter("command");

        String xmlString = "";

        if (command == null

        || command.startsWith("s-")

        || command.equals("fold")) {

            ClassFolders offeringClassFolders = null;

            args = (Map)getChannelAttribute(upId, "" + offering.getId());

            if (args == null) {

                args = new HashMap();

            }

            if (command == null) {

                offeringClassFolders = channelService.getClassFolder(

                offering,

                conn);

                xmlString = offeringClassFolders.toXML();

            }

            else if (command.equals("s-add")) {
                // return from the add dialog, do the add, keep sheet main
                String type = runtimeData.getParameter("type");
                String oid  = runtimeData.getParameter("nodeid");
                String name = runtimeData.getParameter("name");
                String desc = prettyUpDescription(
                    runtimeData.getParameter("description"));
                String href = runtimeData.getParameter("hyperlink");

                xmlString = channelService.addClassFolder(offering,
                    oid,   // oid of the folder to add under or null
                    type,  // folder | URL | file
                    name,  // descriptive name (from Title:)
                    desc,  // Long description
                    href, // if type == URL or type == file
                    args,  // so we can add to {}'s
                    runtimeData.getObjectParameter("uploadedFile"),
                    conn
                    );

                // put the args back in the channel data for next time
                putChannelAttribute(upId, "" + offering.getId(), args);

                if (xmlString != null) {
                    // set the channel dirty for all users in this offering
                    try {
                        super.broadcastUserOfferingDirtyChannel(
                        user, offering, CHANNEL_HANDLE, false);
                    }
                    catch (PortalException pe) {
                        pe.printStackTrace();
                    }
                }
                else {
                    /*StringBuffer errorMsg = new StringBuffer("Error: ");
                    errorMsg.append("File exceeded the size limit of ");
                    errorMsg.append(getMaxUploadSize()).append(".<br/>");
                    setErrorMsg(upId, errorMsg.toString());
                    return;*/

                    runtimeData.remove("uploadedFile");
                    classFolderParams.put("upload_status", "FAILURE");
                    classFolderParams.put("max_file_size", getMaxUploadSize());
                    super.setSheetName(upId, "add");
                }
                //System.out.println("ADDING XMLSTRING: " + xmlString);
            } // end s-add

            else if (command.equals("s-edit")) {

                String oid  = runtimeData.getParameter("oid");
                String name = runtimeData.getParameter("name");
                String url = runtimeData.getParameter("entryURL");
                String desc = prettyUpDescription(
                    runtimeData.getParameter("desc"));

                xmlString = channelService.updateClassFolder(offering, oid, name, desc, url, conn);

                // set the channel dirty for all users in this offering

                try {
                    super.broadcastUserOfferingDirtyChannel(user, offering,
                        CHANNEL_HANDLE, false);
                }

                catch (PortalException pe) {

                    pe.printStackTrace();

                }

            }

            else if (command.equals("s-delete")) {

                String oid      = runtimeData.getParameter("oid");

                String confirm  = runtimeData.getParameter("commandButton");

                // if the user selected "no" on confirm, do not delete

                if (confirm.equalsIgnoreCase("no")) {

                    offeringClassFolders = channelService.getClassFolder(offering,

                    conn);

                    xmlString = offeringClassFolders.toXML();

                }

                else {

                    xmlString = channelService.deleteClassFolder(offering, oid, conn);

                    // set the channel dirty for all users in this offering

                    try {
                        super.broadcastUserOfferingDirtyChannel(user, offering,
                            CHANNEL_HANDLE, false);
                    }

                    catch (PortalException pe) {

                        pe.printStackTrace();

                    }

                }

            }

            else if (command.equals("fold")) {

                String oid  = runtimeData.getParameter("nodeid");

                xmlString = channelService.toggleClassFolder(offering, args, oid, conn);

                // put the args back in the channel data for next time

                putChannelAttribute(upId, "" + offering.getId(), args);

            }

            else {

                offeringClassFolders = channelService.getClassFolder(offering,

                conn);

                xmlString = offeringClassFolders.toXML();

            }

        }

        else { // ADD/EDIT/DELETE from main to these dialogs

            String noid  = runtimeData.getParameter("nodeid");

            String ntype = runtimeData.getParameter("nodetype");

            String nname = runtimeData.getParameter("nodename");

            if (noid != null)  { classFolderParams.put("nodeid", noid); }

            if (ntype != null) { classFolderParams.put("nodetype", ntype); }

            if (nname != null) { classFolderParams.put("nodename", nname); }

            if (command.equals("add")) {

                // bring up the add dialog
                classFolderParams.put("max_file_size", getMaxUploadSize());

                super.setSheetName(upId, "add");

            }

            else if (command.equals("edit")) {

                String nurl  = runtimeData.getParameter("nodeurl");

                String ndesc = runtimeData.getParameter("nodedesc");

                if (nurl != null)  { classFolderParams.put("nodeurl", nurl); }

                if (ndesc != null) { classFolderParams.put("nodedesc", ndesc); }

                // edit code

                super.setSheetName(upId, "edit");

            }

            else if (command.equals("delete")) {

                // delete code

                super.setSheetName(upId, "delete");

            }

            setXSLParameters(upId, classFolderParams);

        }

        // XSL needs something to grab onto

        if (xmlString == null || xmlString.trim().length() == 0) {

            StringBuffer xmlSB = new StringBuffer("<related-resources offering_id=\"");

            xmlSB.append("" + offering.getId());

            xmlSB.append("\"><class-folder /></related-resources>");

            xmlString = xmlSB.toString();

            // for the XSL to know where it is

            classFolderParams.put("current_command", "add");

            setXSLParameters(upId, classFolderParams);

        }

        else {

            PrintFormat xmlPF = new PrintFormat(xmlString);

            xmlPF.setDefaultValue("N"); // just in case we miss any, especially new ones.

            xmlString = xmlPF.format(args);

        }

        //System.out.println("Before setXML:::\n\t" + xmlString);

        super.setXML(upId, xmlString);

        // Releasing DB Connection

        } finally {
        super.releaseDBConnection(conn);
        }

    }

    private String prettyUpDescription(String desc) {
        String retStr = desc;

        if (retStr != null){
            retStr = retStr.trim();
        }
        if (retStr == null || retStr.trim().length()==0){
            return "&#x20;";
        } else {

            StringBuffer strBuff = new StringBuffer();

            //String.replace didnt replace all the old char to new
            //therefore we have to loop thr this string
            for(int x=0; x<retStr.length(); x++){
                char ch = retStr.charAt(x);
                if (ch == '\r'){
                    strBuff.append(" ");
                } else if (ch== '\n'){
                    strBuff.append(" ");
                } else {
                    strBuff.append(ch);
                }
            }
            retStr = strBuff.toString();
        }
        return retStr;
    }
}

