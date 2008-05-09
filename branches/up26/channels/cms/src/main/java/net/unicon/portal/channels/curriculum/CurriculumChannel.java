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

package net.unicon.portal.channels.curriculum;

import java.util.List;

import java.util.ArrayList;

import java.util.Map;

import java.util.HashMap;

import java.util.Hashtable;

import java.sql.Connection;

import java.io.*;

import java.net.URL;

import java.text.DecimalFormat;

import javax.servlet.http.*;

import javax.servlet.ServletResponseWrapper;

import net.unicon.sdk.properties.*;
import net.unicon.academus.common.XMLAbleEntity;
import net.unicon.academus.delivery.virtuoso.content.ContentAssociationManager;
import net.unicon.academus.domain.lms.Context;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.Topic;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.common.properties.*;
import net.unicon.portal.common.BaseOfferingSubChannel;
import net.unicon.portal.common.cdm.ChannelDataManager;
//import net.unicon.portal.common.PortalObject;

import net.unicon.sdk.FactoryCreateException;

import org.jasig.portal.UploadStatus;
import net.unicon.portal.common.service.file.FileService;

import net.unicon.portal.common.service.file.FileServiceFactory;





import org.jasig.portal.IMultithreadedMimeResponse;

import org.jasig.portal.ChannelRuntimeData;

import org.jasig.portal.MultipartDataSource;


import org.jasig.portal.services.LogService;

import org.jasig.portal.PropertiesManager;

public class CurriculumChannel extends BaseOfferingSubChannel

implements IMultithreadedMimeResponse {

    public CurriculumChannel () {

        super();

    }

    /** Channel Handle found in Channels.xml */

    protected static final String CHANNEL_HANDLE = "CurriculumChannel";

    // Commands

    protected static final String ADD_COMMAND     = "add";

    protected static final String INSERT_COMMAND  = "insert";

    protected static final String DELETE_COMMAND  = "delete";

    protected static final String CONFIRM_COMMAND = "confirm";

    protected static final String REMOVE_CONF_COMMAND  = "remove";

    // Stylesheets

    protected static final String ADD_SHEET      = "add";

    protected static final String REMOVE_SHEET   = "remove";

    protected static final String INACTIVE_SHEET = "inactive";

    protected static final String MAIN_SHEET     = "main";

    // Paramaters

    protected static final String CURR_ID_PARAM_KEY = "id";

    protected static final String TITLE_PARAM_KEY   = "title";

    protected static final String CURRICULUM_ID_KEY = "curriculumID";

    protected CurriculumService currService = null;

    protected static final int maxFileSize =

    PropertiesManager.getPropertyAsInt(

    "org.jasig.portal.RequestParamWrapper.file_upload_max_size");

    protected static final String SIZE_FORMAT = "##0.##";

    protected static DecimalFormat fileSizeFormatter =

    new DecimalFormat(SIZE_FORMAT);

    public void buildXML(String upId) throws Exception {

        // Debug

        net.unicon.portal.util.Debug.instance().markTime();

        net.unicon.portal.util.Debug.instance().timeElapsed(

        "BEGIN BUILDING CurriculumChannel", true);
        //End debug

        ChannelRuntimeData runtimeData = super.getRuntimeData(upId);

        /* check upload status before continuing */
        UploadStatus uploadStatus =
            (UploadStatus)runtimeData.getObjectParameter("upload_status");
        if (uploadStatus != null
                && uploadStatus.getStatus() == UploadStatus.FAILURE) {
            StringBuffer errMsg = new StringBuffer(
            "File exceeds max size limit of ");
            errMsg.append(uploadStatus.getFormattedMaxSize());
            setErrorMsg(upId, errMsg.toString());
            return;
        }

        //super.setSSLLocation(upId, "CurriculumChannel.ssl");
    ChannelDataManager.setSSLLocation(upId,
          ChannelDataManager.getChannelClass(upId).getSSLLocation());

        Hashtable curriculumParams = getXSLParameters(upId);

        /* Getting the user id from the super.IPerson object */

        int userID = getUPortalUser(upId).getID();

        // Getting User object

        User user = super.getDomainUser(upId);

        // Getting Context object for the current user

        Context context = user.getContext();

        // Getting offering Object from the Context Object

        Offering offering  = context.getCurrentOffering(TopicType.ACADEMICS);


        super.setSheetName(upId, MAIN_SHEET);

        String command = runtimeData.getParameter("command");

        /* Getting Database Connection */

        Connection conn = null;
        try {
        conn = super.getDBConnection();

        List courses = new ArrayList();

        currService = CurriculumServiceFactory.getService();

        if (command != null && offering != null) {

            curriculumParams.put("offeringName", offering.getName());

            if (command.equals(ADD_COMMAND)) {

                //------------------------

                // ADD CURRICULUM FORM

                //------------------------

                super.setSheetName(upId, ADD_SHEET);

                // Getting available curriculum if delivery system = true

                Topic t = offering.getTopic();
                courses = currService.getAvailableCurriculum(ContentAssociationManager.getAssociations(t), user);

            }

            else if (command.equals(REMOVE_CONF_COMMAND)) {

                //------------------------

                // DELETE CURRICULUM (Confirmation Page)

                //------------------------

                super.setSheetName(upId, REMOVE_SHEET);

                // Passing data into the XSL

                curriculumParams.put(CURR_ID_PARAM_KEY,

                runtimeData.getParameter(CURR_ID_PARAM_KEY));

                curriculumParams.put(TITLE_PARAM_KEY,

                runtimeData.getParameter(TITLE_PARAM_KEY));

            }

            else if (command.equals(INSERT_COMMAND)) {

                //------------------------

                // ADD SUBMIT

                //------------------------

                this.addCurriculum(runtimeData, offering, upId, conn);

                // Dirty cache for all users in the offering

                super.broadcastUserOfferingDirtyChannel(user, offering,
                    CHANNEL_HANDLE, false);
            }

            else if (command.equals(DELETE_COMMAND)) {

                //------------------------

                // DELETE CURRICULUM

                //------------------------

                String idStr   = runtimeData.getParameter(CURR_ID_PARAM_KEY);

                String confirm = runtimeData.getParameter("commandButton");

                // Looking for a Yes for deleting curriculum

                if (idStr != null && confirm.equals(CONFIRM_COMMAND)) {

                    int id = Integer.parseInt(idStr);

                    currService.deleteCurriculum(id, offering, conn);

                    super.removeChannelAttribute(upId, idStr);

                    // Dirty cache for all users in the offering

                    super.broadcastUserOfferingDirtyChannel(user, offering,
                        CHANNEL_HANDLE, false);

                }

            }

            // Getting upto date curriculum data and cache it in channel cache

            if (command.equals(INSERT_COMMAND) || command.equals(DELETE_COMMAND)) {

                courses = currService.getCurriculum(offering, user, true, conn);

                this.cacheCurriculumObjects(courses, upId);

            }

        } else if (offering == null) {

            //Getting empty sheet

            super.setSheetName(upId, "empty");

        } else {

            courses = currService.getCurriculum(offering, user, true, conn);

            this.cacheCurriculumObjects(courses, upId);

        }


        StringBuffer stuff = new StringBuffer("<?xml version=\"1.0\"?>");

        stuff.append("<course-list>");

        if (courses != null) {

            for (int ix = 0; ix < courses.size(); ix++ ) {

                stuff.append(((XMLAbleEntity) courses.get(ix)).toXML());

            }

        }

        stuff.append("</course-list>");

        setXML(upId, stuff.toString());

        } finally {
        super.releaseDBConnection(conn);
        }

        net.unicon.portal.util.Debug.instance().timeElapsed("DONE BUILDING CurriculumChannel", true);

    }

    protected static final File curriculumDir = new File(UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.portal.curriculum.curriculumFileDir"));

    /**
     * @param <code>net.unicon.academus.domain.lms.Offering</code> - an offering
     * @return <code>String</code>
     * @exception <{Exception}>
     * @see <{net.unicon.academus.domain.lms.Offering}>
     */

    public String exportChannel(Offering offering)

    throws Exception {

        return "";

    }

    /**
     * @param <code>org.w3c.dom.Document</code> - a dom document
     * @param <code>net.unicon.academus.domain.lms.Offering</code> - an offering
     * @return <code>Map</code>
     * @exception <{Exception}>
     * @see <{net.unicon.academus.domain.lms.Offering}>
     * @see <{org.w3c.dom.Document}>
     */

    public synchronized Map importChannel(Offering offering)

    throws Exception {

        return new HashMap();

    }

    private void cacheCurriculumObjects(List currList, String upId) {

        if (currList != null) {

            Curriculum currObj = null;

            for (int ix = 0; ix < currList.size(); ++ix) {

                currObj = (Curriculum) currList.get(ix);

                super.putChannelAttribute(upId, currObj.getId(), currObj);

            }

        }

    }

    public String getContentType(String upId) {

        String currIDstr = super.getRuntimeData(upId).getParameter(CURRICULUM_ID_KEY);

        String contentType = null;

        if (currIDstr != null) {

            Curriculum curriculum = (Curriculum) super.getChannelAttribute(upId, currIDstr);

            contentType = curriculum.getContentType();

        }

        return contentType;

    }

    public InputStream getInputStream(String upId) throws IOException {

        String currIDstr = super.getRuntimeData(upId).getParameter(CURRICULUM_ID_KEY);

        if (currIDstr == null) {

            throw new IOException("No id for file given");

        }

        Curriculum curriculum = (Curriculum) super.getChannelAttribute(upId, currIDstr);

        String type = curriculum.getType();

        InputStream is = null;

        if (type.equals(Curriculum.ONLINE)) {

            // Getting User object
            User user = super.getDomainUser(upId);
            Connection conn = null;
            try {
                currService = CurriculumServiceFactory.getService();
                conn = super.getDBConnection();
                String urlref = currService.getCurriculumLink(
                curriculum.getReference(),
                user, null,
                conn, false);
                URL url = new URL(urlref);
                is = url.openStream();

            } catch (Exception e) {

                throw new IOException("Unable to Initalize Service");

            } finally {
                super.releaseDBConnection(conn);
            }

        }

        else if (type.equals(Curriculum.FILE)) {

            File baseDir = new File(curriculumDir, "" + curriculum.getOfferingID());

            try {

                File file = FileServiceFactory.getService().getFile(baseDir, curriculum.getReference());

                if (file == null) {

                    throw new IOException ("File " + getName(upId) + " not found.");

                }

                is = new FileInputStream(file);

            } catch (FactoryCreateException fce) {

                fce.printStackTrace();

                throw new IOException ("Error getting FileService");

            }

        }

        return is;

    }

    public void downloadData(OutputStream out, String upId) throws IOException {

    }

    public String getName(String upId) {

        String currIDstr = super.getRuntimeData(upId).getParameter(CURRICULUM_ID_KEY);

        String fileName = null;

        if (currIDstr != null) {

            Curriculum curriculum = (Curriculum) super.getChannelAttribute(upId, currIDstr);

            fileName = curriculum.getReference();

        }

        return fileName;

    }

    public Map getHeaders(String upId) {

        String currIDstr = super.getRuntimeData(upId).getParameter(CURRICULUM_ID_KEY);

        if (currIDstr != null) {

            Curriculum curriculum = (Curriculum) super.getChannelAttribute(upId, currIDstr);

            String type = curriculum.getType();

            if ((Curriculum.FILE).equals(type)) {

                HashMap headers = new HashMap();

                headers.put("Content-Disposition", "attachment; filename=\"" +

                getName(upId) + "\"");

                return headers;

            }

        }

        return null;

    }

    private String getMaxUploadSize () {

        double availableSpace = ((double)maxFileSize) / (1024.0 * 1024.0);

        return (fileSizeFormatter.format(availableSpace) + "MB");

    }

    private void addCurriculum(

    ChannelRuntimeData runtimeData,

    Offering offering,

    String upId,

    Connection conn) throws Exception {


        String name        = runtimeData.getParameter("name");

        String description = runtimeData.getParameter("description");

        String type        = runtimeData.getParameter("type");

        String contentType = null;

        String reference   = null;

        // Theme & style
        String theme = null;
        String style = null;

        if (type.equals("online")) {

            // Adding a Delivery System of curriculum

            reference = runtimeData.getParameter("onlineCurriculum");

            contentType = "text/html";

            // Theme.
            theme = runtimeData.getParameter("theme");
            if (theme == null || theme.trim().length() == 0) {
                String msg = "Online curriculum 'theme' must be specified in the request.";
                throw new RuntimeException(msg);
            }

            // Style.
            style = runtimeData.getParameter("style");
            if (style == null || style.trim().length() == 0) {
                String msg = "Online curriculum 'style' must be specified in the request.";
                throw new RuntimeException(msg);
            }

        }

        else if (type.equals("url")) {

            // Adding a URL of curriculum

            reference = runtimeData.getParameter("curriculumURL");

        }

        else if (type.equals("file")) {


            // Adding a File suchas PPT, etc for curriculum

            Object[] x = runtimeData.getObjectParameterValues("uploadedFile");

            if (x == null || x.length <= 0) {

                setErrorMsg(upId, "Error uploading file.");

                return;

            }

            MultipartDataSource content = (MultipartDataSource) x[0];

            InputStream is = content.getInputStream();

            reference      = content.getName();

            contentType    = content.getContentType();

            // Checking if file is under the max size limit

            /*
            if (is.available() > maxFileSize) {

                StringBuffer errMsg = new StringBuffer(

                "File exceeds max size limit of ");

                errMsg.append(getMaxUploadSize());

                setErrorMsg(upId, errMsg.toString());

                return;

            }
            */

            // Checking the integirty of the file

            if (reference == null || "".equals(reference)) {

                setErrorMsg(upId, "No filename given for uploaded file.");

                return;

            }

            // Verify file type

            if (contentType == null || "".equals(contentType)) {

                LogService.instance().log(LogService.ERROR,

                "GradebookChannel::parseActivationData() : no contentType: "

                + contentType);

                setErrorMsg(upId, "Error uploading file.");

                return;

            }

            if (is == null) {

                setErrorMsg(upId, "Error uploading file.");

                return;

            }

            FileService us = FileServiceFactory.getService();

            File dir = new File(curriculumDir,

            "" + (int) offering.getId());

            File uploadedFile = us.uploadFile(dir, reference, is);

            is.close();

        }

        else {

            return;

        }

        if (name != null && type != null && reference != null) {

            currService.saveCurriculum(name, description, type, reference, contentType, offering, theme, style, conn);

        }

    }

    /**
     * Let the channel know that there were problems with the download
     * @param e
     */
    public void reportDownloadError(Exception e) {
      LogService.log(LogService.ERROR, "CurriculumChannel::reportDownloadError(): " + e.getMessage());
    }
}

