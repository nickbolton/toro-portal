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

package net.unicon.portal.channels.gradebook;

import java.util.*;
import java.io.*;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.w3c.dom.Document;

import java.sql.Connection;
import java.sql.Time;
import java.text.DecimalFormat;
import javax.servlet.http.*;

import org.jasig.portal.services.LogService;
import org.jasig.portal.*;

import org.jasig.portal.UploadStatus;

import net.unicon.academus.common.SearchCriteria;
import net.unicon.academus.common.XMLAbleEntity;
import net.unicon.academus.delivery.virtuoso.activation.ActivationAdjunctAgent;
import net.unicon.academus.delivery.virtuoso.theme.IStructuralTheme;
import net.unicon.academus.delivery.virtuoso.theme.ThemeBroker;
import net.unicon.academus.domain.lms.*;
import net.unicon.portal.channels.gradebook.base.*;
import net.unicon.portal.common.properties.*;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.common.service.file.FileService;
import net.unicon.portal.common.service.file.FileServiceFactory;
import net.unicon.portal.common.BaseOfferingSubChannel;
import net.unicon.portal.common.service.activation.Activation;
import net.unicon.portal.common.properties.*;


import net.unicon.penelope.Handle;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IOption;
import net.unicon.penelope.IComplement;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.IDecision;

import net.unicon.portal.permissions.PermissionsFactory;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.sdk.properties.*;
import net.unicon.sdk.FactoryCreateException;

import net.unicon.sdk.time.*;
import net.unicon.sdk.catalog.*;
import net.unicon.sdk.catalog.db.*;

public class GradebookChannel extends BaseOfferingSubChannel implements IMultithreadedMimeResponse  {

    public GradebookChannel() {
        super();
    }

    /** Channel Handle found in Channels.xml */
    protected static final String CHANNEL_HANDLE = "GradebookChannel";

    protected static final String fileInfoKey = "gradebookFileInfo";

    protected static final File activationDir =
            new File(UniconPropertiesFactory.getManager(
                    PortalPropertiesType.LMS).getProperty(
                            "net.unicon.portal.gradebook.uploadedActivationFileDir"));

    protected static final File submissionDir =
            new File(UniconPropertiesFactory.getManager(
                    PortalPropertiesType.LMS).getProperty(
                            "net.unicon.portal.gradebook.submissionFileDir"));

    protected static final File feedbackDir =
            new File(UniconPropertiesFactory.getManager(
                    PortalPropertiesType.LMS).getProperty(
                            "net.unicon.portal.gradebook.feedbackFileDir"));

    protected static final int maxFileSize =
            PropertiesManager.getPropertyAsInt(
                    "org.jasig.portal.RequestParamWrapper.file_upload_max_size");

    protected static final String SIZE_FORMAT = "##0.##";

    protected static DecimalFormat fileSizeFormatter =  new DecimalFormat(SIZE_FORMAT);

    protected static boolean useDelivery = ((String) UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.academus.delivery.DeliveryAdapter")).equals("true");
    
    public void buildXML(String upId) throws Exception {
        net.unicon.portal.util.Debug.instance().markTime();
        net.unicon.portal.util.Debug.instance().timeElapsed(
                "BEGIN BUILDING GradebookChannel", true);

        /* check upload status before continuing */
        UploadStatus uploadStatus =
            (UploadStatus)getRuntimeData(upId).getObjectParameter(
                "upload_status");
        if (uploadStatus != null
                && uploadStatus.getStatus() == UploadStatus.FAILURE) {
            StringBuffer errMsg = new StringBuffer(
            "File exceeds max size limit of ");
            errMsg.append(uploadStatus.getFormattedMaxSize());
            setErrorMsg(upId, errMsg.toString());
            return;
        }

        /* set the location for the gradebook ssl file.  The ssl
           file contains a pointer to all the xsl files. */
        ChannelDataManager.setSSLLocation(upId, ChannelDataManager.getChannelClass(upId).getSSLLocation());

        /* making a hashtable for xsl parameters */
        Hashtable gradebookParams = getXSLParameters(upId);

        /* Building runtimeData object */
        ChannelRuntimeData runtimeData = super.getRuntimeData(upId);

/*
// spill parameters (uncomment to see)...
System.out.println("#### SPILLING PARAMETERS...");
Iterator it = runtimeData.keySet().iterator();
while (it.hasNext()) {
    String key = (String) it.next();
    String value = runtimeData.getParameter(key);
    System.out.println("\t"+key.toString()+"="+value);
}
*/

        // Getting User object
        User user = super.getDomainUser(upId);

        // Getting Context object for the current user
        Context context = user.getContext();

        // Getting offering Object from the Context Object
        Offering offering  = context.getCurrentOffering(TopicType.ACADEMICS);

        if (offering == null ) {
            super.setSheetName(upId, "empty");
            super.setXML(upId, "<?xml version=\"1.0\"?><gradebooks/>");
            return;
        }

        List members = Memberships.getMembers(offering, EnrollmentStatus.ENROLLED);

        int numberOfEnrolled = members.size();

        /* Gettting database Connection */
        Connection conn = null;
        try {
        conn = super.getDBConnection();

        List gradebooks = null;

        super.setSheetName(upId, "instructor");

        boolean viewAll = getPermissions(upId).canDo(user.getUsername(),
            "viewAll");

        /* Getting filterModes for roles */
        List filterModes = this.getRoleFilters(upId, offering);

        /* Getting commands */
        String command = getRuntimeData(upId).getParameter("command");

        gradebookParams.remove("current_command");

        boolean isActivation = false;

        if (command != null && command.length() > 0) {

            /* HOUSTON ... We have a command ... */
            String gbItemIDstr = runtimeData.getParameter("gradebook_itemID");
            String positionStr = runtimeData.getParameter("position");

            if (command.equals(PAGE_COMMAND)) {
                command = DEFAULT_COMMAND;
                gradebooks = getCatalogData(
                    upId, user, offering, filterModes, viewAll, conn);
            } else if (command.equals(ADD_COLUMN_COMMAND)) {
                gradebookParams.put("onlineAssessmentAvailable", ""+useDelivery);
                gradebooks = addEventHandler(
                                ADD_COLUMN_COMMAND,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                conn);
            } else if (command.equals(EDIT_COLUMN_COMMAND)
                        && gbItemIDstr != null) {
                gradebooks = editEventHandler(
                                EDIT_COLUMN_COMMAND,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            } else if (command.equals(EDIT_COLUMN_SCORES_COMMAND)
                        && gbItemIDstr != null) {
                gradebooks = editColumnScoreEventHandler(
                                EDIT_COLUMN_SCORES_COMMAND,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            } else if (command.equals(EDIT_ALL_SCORES_COMMAND)) {
                gradebooks = editAllScoresEventHandler(
                                EDIT_ALL_SCORES_COMMAND,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                conn);
            } else if (command.equals("update") || command.equals("insert")
                        || command.equals("insert_activate")
                        || command.equals("update_activate")) {
                /* Getting GradebookService(s) */
                GradebookService gbService = GradebookServiceFactory.getService();

                String typeStr = runtimeData.getParameter("item_type");
                String currPositionStr  = runtimeData.getParameter("current_position");
                String weightStr    = runtimeData.getParameter("weight");
                String name         = runtimeData.getParameter("item_name");
                String minStr       = runtimeData.getParameter("min_score");
                String maxStr       = runtimeData.getParameter("max_score");
                String feedback     = runtimeData.getParameter("feedback");
                String association  = null;

                boolean newItem = false;

                if (command.equals("insert") || command.equals("insert_activate")) {
                    newItem = true;
                }

                int position    = -1;
                int newPosition = -1;

                if (positionStr != null) {
                    /* Position is requested to change */
                    newPosition  = Integer.parseInt(positionStr);

                    if (currPositionStr != null) {
                            /* current position (position moving from */
                        position     = Integer.parseInt(currPositionStr);
                    }

                } else if (currPositionStr != null) {
                    /* No change in position */
                    position    = Integer.parseInt(currPositionStr);
                    newPosition = position;
                }

                if (typeStr != null && weightStr != null) {

                    int type = 1;
                    if (typeStr.equals("2")) {
                        type = Integer.parseInt(typeStr);
                    } else {
                        association = typeStr;
                    }

                    int weight    = Integer.parseInt(weightStr);
                    int max = -1;
                    int min = -1;

                    if (type == 2) {
                        /* Get more information for other assignment */
                        if (maxStr == null || "".equals(maxStr.trim())) {
                            maxStr = "100";
                        }
                        if (minStr == null || "".equals(minStr.trim())) {
                            minStr = "0";
                        }
                        max = Integer.parseInt(maxStr);
                        min = Integer.parseInt(minStr);

                        if (max < min) {
                            super.setErrorMsg(upId,
                            "Maximum Score cannot be less than the Minimum Score value.");
                            return;
                        }
                    }

                    if (newItem) {
                        int gbItemID = gbService.insertGradebookItem(user,
                                        offering, type, weight, newPosition,
                                        name, max, min, feedback, association,
                                        conn);

                        gradebookParams.put("gradebookItemID",  "" + gbItemID);
                        gbItemIDstr = "" + gbItemID;
                        runtimeData.setParameter("gradebook_itemID", gbItemIDstr);

                    } else if (gbItemIDstr != null) {
                        int gbItemID = Integer.parseInt(gbItemIDstr);
                        gbService.saveGradebookItem(
                                            gbItemID,
                                            offering,
                                            type,
                                            weight,
                                            newPosition,
                                            position,
                                            name,
                                            max,
                                            min,
                                            feedback,
                                            association,
                                            conn);

                    }

                }

                // Dirty cache for all users in the offering
                super.broadcastUserOfferingDirtyChannel(user, offering,
                    CHANNEL_HANDLE, false);

                /* Goto activation */
                if (command.equals("insert_activate") || command.equals("update_activate")) {
                    command = ACTIVATION_COMMAND;
                } else {
                    command = DEFAULT_COMMAND;
                    gradebooks = getCatalogData(upId, user, offering, filterModes, viewAll, conn);
                }
            } else if (command.equals(UPDATE_GB_COLUMN_SCORES_COMMAND)
                    || command.equals(UPDATE_GB_SCORES_COMMAND)) {
                gradebooks = updateGradebookScoresEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            } else if (command.equals(DELETE_COLUMN_COMMAND)) {
                gradebooks = deleteColumnEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            } else if (command.equals(DELETE_CONFIRM_COMMAND)) {
                gradebooks = deleteColumnConfirmEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            } else if (command.equals(USER_DETAILS_COMMAND)) {
                gradebooks = userDetailsEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            } else if (command.equals(UPDATE_QUESTION_DETAILS_COMMAND)) {
                gradebooks = updateQuestionDetailsEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            } else if (command.equals(VIEW_ALL_WEIGHTS_COMMAND)) {
                gradebooks = viewWeightingEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                conn);
            } else if (command.equals(UPDATE_WEIGHTS_COMMAND)) {
                gradebooks = updateWeightsEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            } else if (command.equals(EXPORT_COMMAND)) {
                gradebooks = exportEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                conn);
            } else if (command.equals(QUESTION_DETAILS_COMMAND)) {
                gradebooks = questionDetailsEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            } else if (command.equals(ALL_QUESTION_DETAILS_COMMAND)) {
                gradebookParams.put("username", user.getUsername());
                gradebooks = allQuestionDetailsEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            }
            if (command.equals(DEACTIVATE_COMMAND)) {
                gradebooks = deactivateActivationEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            }
            if (command.equals(VIEW_ACTIVATION_COMMMAND)) {
                gradebooks = viewActivationEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            }
            if (command.equals(INSERT_ACTIVATION_COMMAND)) {
                gradebooks = insertActivationEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            }
            if (command.equals(VIEW_ALL_ACTIVATIONS_COMMAND)) {
                gradebooks = viewAllActivationsEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            }
            if (command.equals(ACTIVATION_COMMAND)) {
                gradebooks = activationEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            }
            if (command.equals(ASSOCIATE_COMMAND)) {
                associateEventHandler(
                                command,
                                upId,
                                runtimeData,
                                gradebookParams);
            }
            if (command.equals(SEARCH_ASSOCIATION_COMMAND)) {
                gradebooks = searchAssociationsEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            }
            if (command.equals(SET_ASSOCIATION_COMMAND)) {
                gradebooks = setAssociationEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
            }
            if (command.equals("submissions")) {
                super.setSheetName(upId, "submissions");
                gradebooks = getCatalogData(upId, user, offering, filterModes, viewAll, conn);
            }

            // Some command had an error no need to continue.
            // The framework will populate the xml/sheet name

            if (! "".equals(getErrorMsg(upId))) return;
        } else {
            command = DEFAULT_COMMAND;
            gradebooks = getCatalogData(upId, user, offering, filterModes, viewAll, conn);
        }


        StringBuffer xmlBuffer = new StringBuffer();
        xmlBuffer.append("<?xml version=\"1.0\"?>");
        xmlBuffer.append("<gradebooks>");
        if (gradebooks != null) {
            for (int ix = 0; ix < gradebooks.size(); ++ix) {
                xmlBuffer.append(((XMLAbleEntity)gradebooks.get(ix)).toXML());
            }
        }
        xmlBuffer.append("</gradebooks>");

        /* Setting catCurrentCommand */
        String catCurrentCommand = "";
        String fName = evaluateSearchFirstName(upId);
        String lName = evaluateSearchLastName(upId);
        if (command.equals(DEFAULT_COMMAND)
                && (fName == null) && (lName == null)) {
            catCurrentCommand = PAGE_COMMAND;
        }
        gradebookParams.put("catCurrentCommand", catCurrentCommand);

        /* Setting current_command */
        gradebookParams.put("current_command", command);

// System.out.println(xmlBuffer.toString());

        super.setXML(upId, xmlBuffer.toString());

        } finally {
        /* Releasing DB Connection */
        super.releaseDBConnection(conn);
        }
        net.unicon.portal.util.Debug.instance().timeElapsed("DONE BUILDING GradebookChannel", true);
    } // end buildXML

    /**
     * <p>
     * Return the catalog data based on paramters
     * </p><p>
     *
     * @return a list with the catalog data
     * </p>
     */
    private List getCatalogData(
                        String upId,
                        User user,
                        Offering offering,
                        List filterModes,
                        boolean viewAll,
                        Connection conn) throws Exception {
        GradebookService gbService = GradebookServiceFactory.getService();

        List rtnList = null;
        if (viewAll) {
            rtnList = doDefaultPageAction(
                    upId,
                    offering,
                    gbService,
                    filterModes,
                    conn);
        } else if (!viewAll){
            rtnList = gbService.getGradebookEntries(
                    user,
                    offering,
                    viewAll,
                    (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),
                    conn);
        }
        return rtnList;
    }

    /**
     * @param <code>net.unicon.portal.domain.Offering</code> - an offering
     * @return <code>String</code>
     * @exception <{Exception}>
     * @see <{net.unicon.portal.domain.Offering}>
     */

    public String exportChannel(Offering offering) throws Exception {
        Connection conn = null;
        try {
        conn = getDBConnection();

        GradebookService gbService = GradebookServiceFactory.getService();
        List gradebooks =
        gbService.getGradebookEntries(null, offering, true, conn);

        if (gradebooks == null) return "";
        StringBuffer sb = new StringBuffer("<gradebooks>\n");
        for (int ix = 0; ix < gradebooks.size(); ++ix) {
           XMLAbleEntity  obj = (XMLAbleEntity) gradebooks.get(ix);
            sb.append(obj.toXML());
        }
        sb.append("</gradebooks>\n");
        return sb.toString();
        } finally {
        releaseDBConnection(conn);
        }
    }

    /**
     * @param <code>org.w3c.dom.Document</code> - a dom document
     * @param <code>net.unicon.portal.domain.Offering</code> - an offering
     * @return <code>Map</code>
     * @exception <{Exception}>
     * @see <{net.unicon.portal.domain.Offering}>
     * @see <{Document}>
     */
    public synchronized Map importChannel(Offering offering, Document dom)
    throws Exception {
        return new HashMap();
    }

    // IMultithreadedMimeResponse methods
    public IGradebookFileInfo getFileInfo(String upId) {
        IGradebookFileInfo fileInfo = null;

        String type = getRuntimeData(upId).getParameter("fileType");
        String idStr = getRuntimeData(upId).getParameter("gradebookScoreId");

        int id = 0;

        fileInfo = (IGradebookFileInfo)getChannelAttribute(upId, fileInfoKey);

        if (fileInfo != null) return fileInfo;

        GradebookService gbService = null;

        try {
            id = Integer.parseInt(idStr);
            gbService = GradebookServiceFactory.getService();
        } catch (NumberFormatException e) {
            LogService.instance().log(LogService.ERROR,
            "GradebookChannel::getContentType() : invalid gradebookScoreId: " +
            idStr);
            return null;
        } catch (FactoryCreateException fce) {
            LogService.instance().log(LogService.ERROR,
            "GradebookChannel::getContentType() : Unable to get GradebookService");
            return null;
        }

        Connection conn = null;
        if ("submission".equals(type)) {
            try {
            conn = getDBConnection();
            fileInfo = gbService.getGradebookSubmission(id, conn);
            } finally {
            releaseDBConnection(conn);
            }
        } else if ("feedback".equals(type)) {
            try {
            conn = getDBConnection();
            fileInfo = gbService.getGradebookFeedback(id, conn);
            } finally {
            releaseDBConnection(conn);
            }
        } else {
            LogService.instance().log(LogService.ERROR,
            "GradebookChannel::getContentType() : invalid fileType: " +
            type);
            return null;
        }

        if (fileInfo == null) {
            LogService.instance().log(LogService.ERROR,
            "GradebookChannel::getContentType() : null fileInfo");
            return null;
        }

        putChannelAttribute(upId, fileInfoKey, fileInfo);

        return fileInfo;

    } // end getFileInfo

    public String getContentType(String upId) {
        IGradebookFileInfo fileInfo = getFileInfo(upId);
        if (fileInfo == null) {
            return "";
        }

        if (fileInfo.getContentType() == null) {
            LogService.instance().log(LogService.ERROR,
            "GradebookChannel::getContentType() : null contentType");
            return "";
        }
        return fileInfo.getContentType();
    }

    public InputStream getInputStream(String upId) throws IOException {

        IGradebookFileInfo fileInfo = getFileInfo(upId);
        String type = getRuntimeData(upId).getParameter("fileType");

        if ("submission".equals(type) || "feedback".equals(type)) {
            if (fileInfo == null) {
                throw new IOException ("FileInfo not found!");
            }
        }

        String filename = getName(upId);

        if (filename == null) {
            throw new IOException ("No filename given!");
        }

        File baseDir = null;

        if ("submission".equals(type)) {
            baseDir = new File(submissionDir, "" + fileInfo.getGradebookScoreID());
        } else if ("feedback".equals(type)) {
            baseDir = new File(feedbackDir, "" + fileInfo.getGradebookScoreID());
        } else if ("activation".equals(type)) {
            String actId = getRuntimeData(upId).getParameter("activationId");
            baseDir = new File(activationDir, actId);
        } else {
            LogService.instance().log(LogService.ERROR,
            "GradebookChannel::getContentType() : invalid fileType: " +
            type);
            throw new IOException ("Invalid file type: " + type);
        }

        removeChannelAttribute(upId, fileInfoKey);

        File file = null;

        try {
            file = FileServiceFactory.getService().getFile(baseDir, filename);
            if (file == null) {
                throw new IOException ("File " + filename + " not found.");
            }
        } catch (FactoryCreateException fce) {
            fce.printStackTrace();
            throw new IOException ("Error gettiner FileService");
        }
        return new FileInputStream(file);
    } // end getInputStream

    public String getName(String upId) {

        String type = getRuntimeData(upId).getParameter("fileType");

        if ("submission".equals(type) || "feedback".equals(type)) {

            IGradebookFileInfo fileInfo = getFileInfo(upId);

            if (fileInfo == null) {
                return null;
            }

            if (fileInfo.getFilename() == null) {
                LogService.instance().log(LogService.ERROR,
                "GradebookChannel::getContentType() : null filename");
                return null;
            }

            return fileInfo.getFilename();

        } else if ("activation".equals(type)) {
            try {

                String actId = getRuntimeData(upId).getParameter("activationId");
                FileService us = FileServiceFactory.getService();
                File dir = us.getFile(activationDir, actId);
                File[] files = dir.listFiles();
                if (files == null) return null;
                return files[0].getName();
            } catch (FactoryCreateException fce) {
                fce.printStackTrace();
            }
        }
        return null;
    } // end getName

    public Map getHeaders(String upId) {
        HashMap headers = new HashMap();
        headers.put("Content-Disposition", "attachment; filename=\"" +
        getName(upId) + "\"");
        return headers;
    }

    public void downloadData(OutputStream out, String upId) throws IOException {
        removeChannelAttribute(upId, fileInfoKey);
    }

    /**
     * Let the channel know that there were problems with the download
     * @param e
     */
    public void reportDownloadError(Exception e) {
      LogService.log(LogService.ERROR, "GradebookChannel::reportDownloadError(): " + e.getMessage());
    }

    private static final String delim = "|";

    private void storeChangeLog(
    String changeLog,
    ChannelRuntimeData runtimeData,
    GradebookService gbService,
    Offering offering,
    Connection conn) {

        StringTokenizer tokenizer = new StringTokenizer(changeLog, delim);

        String token         = null;
        String userName      = null;
        String gbItemIDStr   = null;
        String scoreString   = null;

        boolean columnChange = false;

        int score = 0;
        int gbItemID = -1;

        // The set of gbItems that have been updated

        Set gbItemIDSet = new HashSet();

        while (tokenizer.hasMoreTokens()) {

            token = tokenizer.nextToken();

            // Get username

            userName = token.substring (

            token.indexOf('_') + 1,

            token.lastIndexOf('_')

            );

            // Getting column index ID

            gbItemIDStr = token.substring(

            token.lastIndexOf('_') + 1,

            token.length());

            //Get the score from the form data

            scoreString = "";

            if (runtimeData.getParameter(token) != null) {

                scoreString = runtimeData.getParameter(token);

                gbItemID  = Integer.parseInt(gbItemIDStr);

                if (!scoreString.equals("")) {

                    score = Integer.parseInt(scoreString);
                }
                else{
                    score = -1;
                }

                    columnChange = true;

                    gbItemIDSet.add(new Integer(gbItemID));

                    gbService.updateUserScore(gbItemID, userName, score, offering, conn);

                

            }

        }

        /* End while loop */

        // Storing mean and median

        if (columnChange && gbItemIDSet.size() > 0) {

            Object[] gbItemIntegers = gbItemIDSet.toArray();

            for (int ix = 0; ix < gbItemIntegers.length; ix++) {

                /* need to update mean and media */

                gbItemID = ((Integer) gbItemIntegers[ix]).intValue();

                String meanStr = runtimeData.getParameter("Mean_" + gbItemID);

                String medianStr   = runtimeData.getParameter("Median_" + gbItemID);

                int mean = -1;

                int median = -1;

                if (medianStr != null) {

                    if(medianStr.equals(""))
                    {
                        median = 0;
                    }
                    else
                    {
                        median = Math.round(Float.parseFloat(medianStr));
                    }

                }

                if (meanStr != null) {
                    
                    if(meanStr.equals(""))
                    {
                        mean = 0;    
                    }
                    else
                    {
                        mean = Math.round(Float.parseFloat(meanStr));    
                    }

                }

                gbService.updateGradebookMeanAndMedian(

                gbItemID,

                offering,

                mean,

                median,

                conn);

            } /* End for loop */
        }
    } // end storeChangeLog

    private void parseActivationData(
    String upId,
    GradebookService gbService,
    GradebookActivationService gbActivationService,
    Offering offering,
    ChannelRuntimeData runtimeData,
    User user,
    Connection conn,
    IDecisionCollection dc) throws Exception {

        TimeService ts = TimeServiceFactory.getService();

        String gbItemIDstr  = runtimeData.getParameter("gradebook_itemID");
        String nowBox       = runtimeData.getParameter("nowBox");
        String attemptsStr  = runtimeData.getParameter("attempts");
        String type         = runtimeData.getParameter("type");
        String entryLink    = runtimeData.getParameter("entryLink");
        Date startDate = null;

        Time startTime = null;

        int gbItem = -1;

        if (gbItemIDstr != null) {
            gbItem = Integer.parseInt(gbItemIDstr);
        } else {
            return;
        }

        if (nowBox == null ) {
            String hourStart    = runtimeData.getParameter("hourStart");
            String minuteStart  = runtimeData.getParameter("minuteStart");
            String ampmStart    = runtimeData.getParameter("ampmStart");
            String monthStart   = runtimeData.getParameter("monthStart");
            String dayStart     = runtimeData.getParameter("dayStart");
            String yearStart    = runtimeData.getParameter("yearStart");

            int shour  = Integer.parseInt(hourStart);
            int smin   = Integer.parseInt(minuteStart);

            if (ampmStart.equals("PM") && shour != 12) {
                shour += 12;
            } else if (ampmStart.equals("AM") && shour == 12) {
                shour = 0;
            }

            startTime = new Time(shour, smin, 0);

            int smonth = Integer.parseInt(monthStart);
            int sday   = Integer.parseInt(dayStart);
            int syear  = Integer.parseInt(yearStart);

            startDate = new Date(syear - 1900, smonth - 1, sday);
        } else {
            startDate = ts.getTimestamp();
        }

        String hourEnd    = runtimeData.getParameter("hourEnd");
        String minuteEnd  = runtimeData.getParameter("minuteEnd");
        String ampmEnd    = runtimeData.getParameter("ampmEnd");
        String monthEnd   = runtimeData.getParameter("monthEnd");
        String dayEnd     = runtimeData.getParameter("dayEnd");
        String yearEnd    = runtimeData.getParameter("yearEnd");

        int ehour  = Integer.parseInt(hourEnd);
        int emin   = Integer.parseInt(minuteEnd);

        if (ampmEnd.equals("PM") && ehour != 12) {
            ehour += 12;
        } else if (ampmEnd.equals("AM") && ehour == 12) {
            ehour = 0;
        }

        Time endTime = new Time(ehour, emin, 0);

        int emonth = Integer.parseInt(monthEnd);
        int eday   = Integer.parseInt(dayEnd);
        int eyear  = Integer.parseInt(yearEnd);

        Date endDate = new Date(eyear - 1900, emonth - 1, eday);

        /* Checking to see if the end date is greater than now */
        Date checkEndDate = new Date();
        checkEndDate.setTime(endTime.getTime());
        checkEndDate.setMonth(endDate.getMonth());
        checkEndDate.setDate (endDate.getDate());
        checkEndDate.setYear (endDate.getYear());

        if (ts.getTimestamp().getTime() > checkEndDate.getTime()) {
            setErrorMsg(upId,
                "Sorry, we were unable to activate the end date in the past.  " +
                "Please set the end date for a time in the future.");
            return;
        }

        /* Date and time validation */
        if (endDate.before(startDate)) {
            setErrorMsg(upId,
                "Sorry, we were unable to activate because the end date occurs before the start date.  " +
                "Please set the end date for a time in the future that is after the start date.");
            return;
        }
        if (endDate.equals(startDate) && endTime.before(startTime)) {
            setErrorMsg(upId,
                "Sorry, we were unable to activate because the end time occurs before the start time.  " +
                "Please set the end time for a time that is after the start date.");
            return;
        }


        /* activation for what users */
        String[] users = runtimeData.getParameterValues("activationFor");
        boolean allUsers = true;
        List userList = new ArrayList();

        if (users.length > 0) {
            allUsers = false;

            if (users[0].equals("all")) {

                allUsers = true;
                List list = Memberships.getMembers(offering);
                for (int ix = 0; ix < list.size(); ++ix) {

                    userList.add(((User)list.get(ix)).getUsername());
                }
            } else {

                for (int ix = 0; ix < users.length; ++ix) {

                    userList.add((String) users[ix]);
                }
            }
        }

        /* Activation Attributes */
        Map actAttr = new HashMap();

        actAttr.put("name", runtimeData.getParameter("name"));

        if (type.equals("1")) {

            /* Activation information */
            String assessmentForm = runtimeData.getParameter("assessment_form");
            String comment        = runtimeData.getParameter("comment");
            String assessment     = runtimeData.getParameter("association");

            if (assessmentForm == null || assessment == null) {
                super.setErrorMsg(upId, "Required Value for assocation and form not present");
                return;
            }

            actAttr.put("assessment_form", assessmentForm);
            actAttr.put("assessment",      assessment);

            if (comment != null && comment.length() > 0) {

                actAttr.put("comment", runtimeData.getParameter("comment"));

            }

            //setting min and max score

            String maxScoreStr = runtimeData.getParameter(assessmentForm + "_max");
            String minScoreStr = runtimeData.getParameter(assessmentForm + "_min");

            if (maxScoreStr != null && minScoreStr != null ) {
                int minScore = Integer.parseInt(minScoreStr);
                int maxScore = Integer.parseInt(maxScoreStr);
                gbService.updateMaxAndMinScore(gbItem, minScore, maxScore, conn);
            } else {
                super.setErrorMsg(upId, "Assessment does not contain a Max and Min Score");
            }
        } else if (type.equals("2")) {
            actAttr.put("link_type", entryLink);
        }

        int attempts = 1;

        if (attemptsStr != null) {
            attempts = Integer.parseInt(attemptsStr);
        }

        actAttr.put("attempts", "" + attempts);

        InputStream is     = null;

        String contentType = null;
        String filename    = null;

        /* store any uploaded files */

        if ("file".equals(entryLink)) {

            Object[] x = runtimeData.getObjectParameterValues("uploadedFile");

            if (x == null || x.length <= 0) {
                super.setErrorMsg(upId, "Error uploading file.");
                return;
            }

            MultipartDataSource content = (MultipartDataSource) x[0];

            is = content.getInputStream();

            filename = content.getName();

            contentType = content.getContentType();

            if (is == null) {
                super.setErrorMsg(upId, "Error uploading file.");
                return;
            }


            if (filename == null || "".equals(filename)) {
                super.setErrorMsg(upId, "No filename given for uploaded file.");
                return;
            }

            if (contentType == null || "".equals(contentType)) {
                LogService.instance().log(LogService.ERROR,
                "GradebookChannel::parseActivationData() : no contentType: "
                + contentType);
                super.setErrorMsg(upId, "Error uploading file.");
                return;
            }
            actAttr.put("link_uri", filename);
        } else if ("url".equals(entryLink)) {
            actAttr.put("link_uri", runtimeData.getParameter("entryURL"));
        }

        // Evaluate exam duration.
        if (dc != null) {
            IDecision dMin = dc.getDecision(ActivationAdjunctAgent.getInstance().getChoices().getChoice(Handle.create("minutes")));
            String strMin = dMin.getSelections()[0].getOption().getHandle().getValue();
            int intMin = Integer.parseInt(strMin);
            IDecision dHr = dc.getDecision(ActivationAdjunctAgent.getInstance().getChoices().getChoice(Handle.create("hours")));
            String strHr = dHr.getSelections()[0].getOption().getHandle().getValue();
            int intHr = Integer.parseInt(strHr);
            if (intMin == 0 && intHr == 0) {
                setErrorMsg(upId,
                    "Sorry, exams may not be activated for zero minutes.  " +
                    "Please set the duration of the exam for a length greater than zero.");
                return;
            }
        }

        Activation activation = gbActivationService.addActivation(
        gbItem,
        (int) offering.getId(),
        type,
        startDate,
        endDate,
        startTime.getTime(),
        endTime.getTime(),
        actAttr,
        userList,
        allUsers,
        user,
        conn,
        dc);

        if (activation == null) {
            LogService.instance().log(LogService.ERROR,
            "GradebookChannel::parseActivationData() : " +
            "addActivation failed!");
            return;
        }

        if ("file".equals(entryLink)) {
            /* Had to do final file uploading here, because we need the activation
             * ID to store the file, but we did validate the file before creating
             * the activation */
            FileService us = FileServiceFactory.getService();
            File dir = new File(activationDir,
            "" + activation.getActivationID());
            File uploadedFile = us.uploadFile(dir, filename, is);
            is.close();
        }
    } // end parseActivationData

    private List getRoleFilters(String upId, Offering offering) {
        List roleFilters = new ArrayList();
        try
        {
            ChannelClass channelClass =
                ChannelDataManager.getChannelClass(upId);
            List offeringRoles = RoleFactory.getOfferingRoles(offering);
            for (int i = 0; i < offeringRoles.size(); i++)
            {
                Role role = (Role)offeringRoles.get(i);
                IPermissions permissions =
                    PermissionsFactory.getPermissions(
                        role.getGroup(), channelClass, null);
                boolean setting =
                    permissions.canDo(null, "excludeFromGradebook");
                if (setting)
                {
                    Object[] oParams =
                        new Object[] { new Long(role.getId()) };
                    roleFilters.add(
                        new FDbFilterMode("M.ROLE_ID <> ?", oParams));
                }
            } // end offeringRoles loop
        }
        catch (Exception e)
        {
            LogService.instance().log(LogService.ERROR,
            "Exception creating role filters in GradebookChannel.getRoleFilters: " + e);
        }
        return roleFilters;
    } // end getRoleFilters

    private List doDefaultPageAction(
                                    String upId,
                                    Offering offering,
                                    GradebookService gbService,
                                    List filterModes,
                                    Connection conn)
    throws Exception {

        /*--------------- EVALUATE INPUT/SEARCH PARAMETERS ----------------*/

        // Determine search pattern.
        int searchPattern = evaluateSearchAndOr(upId);
        boolean matchAllCriteria = false;
        switch (searchPattern) {
            case 1:
                matchAllCriteria = false;
                break;

            case 2:
                matchAllCriteria = true;
                break;
        }

        // Get first and last name search values.
        String fName = evaluateSearchFirstName(upId);
        String lName = evaluateSearchLastName(upId);

        // Calculate which page to view.
        int pgSize = evaluatePageSize(upId);
        int pgNum =
            (fName != null || lName != null) ? 1 : evaluateCurrentPage(upId);



        /*------------------ CREATE FILTER MODES ------------------*/

        // Filter by offering.
        Object[] oParams = new Object[] { new Long(offering.getId()) };
        filterModes.add(new FDbFilterMode("M.OFFERING_ID = ?", oParams));

        // Filter by enrollment status.
        oParams = new Object[] { new Long(EnrollmentStatus.ENROLLED.toInt()) };
        filterModes.add(new FDbFilterMode("M.ENROLLMENT_STATUS = ?", oParams));

        // Filter by first and last name.
        if (fName != null || lName != null) {
            // Search for members by first and last name.
            MemberSearchCriteria criteria = new MemberSearchCriteria();
            criteria.matchAllCriteria(matchAllCriteria);
            criteria.setFirstName(fName);
            criteria.setLastName(lName);
            List memberSearchResults = Memberships.findMembers(
                offering, EnrollmentStatus.ENROLLED, criteria);
            int resultCount = memberSearchResults.size();

            // Create Filter.
            if (resultCount > 0)
            {
                oParams = new Object[resultCount];
                StringBuffer sql = new StringBuffer("");
                if (resultCount > 1) sql.append("(");
                for (int i = 0; i < resultCount; i++)
                {
                    User user = (User)memberSearchResults.get(i);
                    sql.append("M.USER_NAME = ?");
                    if (resultCount > 1 && i < (resultCount - 1))
                    {
                        sql.append(" OR ");
                    }
                    oParams[i] = user.getUsername();
                }
                if (resultCount > 1) sql.append(")");
                filterModes.add(new FDbFilterMode(sql.toString(), oParams));
            }
            else
            {
                filterModes.add(new FDbFilterMode(
                    "M.USER_NAME = ?", new Object[] { "" } ));
            }
        }


        /*------------------- GET GRADEBOOK PAGE ENTRIES -------------------*/

        // Get the Page Entries.
        IPageMode pg = new GbPageMode(pgSize, pgNum);
        List pageEntries = gbService.getGradebookPageEntries(
                        offering,
                        (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),
                        pg,
                        conn);


        /*-------------------- SET XSL OUPUT PARAMETERS --------------------*/

        // Set XSL output catalog parameters.
        Map xslParams = getXSLParameters(upId);
        xslParams.put("catChannel", "gradebook");
        xslParams.put("catPageSize", new Integer(pgSize));
        xslParams.put("catCurrentPage", new Integer(pgNum));
        xslParams.put("catLastPage", new Integer(
            ((pg.isUsed()) ? pg.getPageCount():1)));
        // Set searchAndOr XSL output parameter.
        if (searchPattern != 0) {
            xslParams.put("searchAndOr", new Integer(searchPattern));
        }
        // Set first and last name XSL output parameters.
        if (fName != null) {
            xslParams.put("firstName", fName);
        }
        if (lName != null) {
            xslParams.put("lastName", lName);
        }

        return pageEntries;
    } // end doDefaultPageAction

    private int evaluatePageSize(String upId) {
        int pgSize = 10;    // Default.
        String strPageSize = getRuntimeData(upId).getParameter("catPageSize");

        if (strPageSize != null) {
            if (strPageSize.equals("0")) {
                pgSize = 0;
            } else if (strPageSize.trim().equals("")) {
                // Fall through...go w/ default.
            } else {
                pgSize = Integer.parseInt(strPageSize);
            }
        }
        return pgSize;
    } // end evaluatePageSize

    private int evaluateCurrentPage(String upId) {
        int pgNum = 1;      // Default.

        //String strPageNum = getRuntimeData(upId).getParameter("catCurrentPage");
        String strPageNum = getRuntimeData(upId).getParameter("catSelectPage");
        if (strPageNum != null) {
            pgNum = Integer.parseInt(strPageNum);
        }
        return pgNum;
    } // end evaluateCurrentPage

    private String evaluateSearchFirstName(String upId) {
        String param = getRuntimeData(upId).getParameter("firstName");

        if (param != null) {
            if (param.equals("First Name") || param.trim().equals("")) {
                param = null;
            } // otherwise fall through...
        }
        return param;
    } // end evaluateSearchFirstName

    private String evaluateSearchLastName(String upId) {
        String param = getRuntimeData(upId).getParameter("lastName");

        if (param != null) {
            if (param.equals("Last Name") || param.trim().equals("")) {
                param = null;
            } // otherwise fall through...
        }
        return param;
    } // end evaluateSearchLastName

    private int evaluateSearchAndOr(String upId) {
        int rslt = 0;       // Default -- means no search.

        // Get out if there's no fName or lName.
        if (evaluateSearchFirstName(upId) != null || evaluateSearchLastName(upId) != null) {
            try {
                String s = getRuntimeData(upId).getParameter("searchAndOr");
                rslt = Integer.parseInt(s);
            } catch (Exception e) { }
        }
        return rslt;
    } // end evaluateSearchAndOr

    /**
     * <p>
     * get the Maxium allowed upload size for a gradebook file.
     * </p></p>
     *
     * @return a String representation of the max upload size.
     * </p>
     */
    private String getMaxUploadSize () {
        double availableSpace = ((double)maxFileSize) / (1024.0 * 1024.0);
        return (fileSizeFormatter.format(availableSpace) + "MB");
    }

    /**
     * <p>
     * Add Gradebook Colum to the Gradebook Event Handler.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List addEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Connection conn) throws Exception {
        GradebookService gbService = GradebookServiceFactory.getService();
        boolean viewAll = getPermissions(upId).canDo(user.getUsername(),
            "viewAll");

        List rtnList = gbService.getGradebookEntries(
                    user,
                    offering,
                    viewAll,
                    conn);
        super.setSheetName(upId, "add_gradebookitem");
        return rtnList;
    }

    /**
     * <p>
     * Edit a Gradebook Colum to the Gradebook Event Handler.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List editEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {
        GradebookService gbService
            = GradebookServiceFactory.getService();
        GradebookAssessmentService gbAssessmentService
            = GradebookAssessmentServiceFactory.getService();

        String gbItemIDstr = runtimeData.getParameter("gradebook_itemID");

        gradebookParams.put("gradebookItemID", gbItemIDstr);

        boolean viewAll = getPermissions(upId).canDo(user.getUsername(),
            "viewAll");

        List rtnList = gbService.getGradebookEntries(
                    user,
                    offering,
                    viewAll,
                    conn);

        String association = null;
        GradebookItem gbItem = null;
        int gbItemID = Integer.parseInt(gbItemIDstr);

        for (int ix = 0; ix < rtnList.size(); ++ix) {
            gbItem = (GradebookItem) rtnList.get(ix);
            if (gbItem.getId() == gbItemID
                && (association = gbItem.getAssociation()) != null) {
                ix = rtnList.size();
                XMLAbleEntity assessment = (XMLAbleEntity)
                        gbAssessmentService.getAssessment(
                                                user,
                                                offering,
                                                association,
                                                false,
                                                conn);
                if (assessment != null) {
                    rtnList.add(assessment);
                }
            }
        }
        super.setSheetName(upId, "edit_gradebookitem");
        return rtnList;
    }

    /**
     * <p>
     * Edit the scores of a gradebook Column.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List editColumnScoreEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {
        GradebookService gbService
            = GradebookServiceFactory.getService();

        String gbItemIDstr = runtimeData.getParameter("gradebook_itemID");
        gradebookParams.put("gradebookItemID", gbItemIDstr);
        boolean viewAll = getPermissions(upId).canDo(user.getUsername(),
            "viewAll");

        /* Getting filterModes for roles */
        List filterModes = this.getRoleFilters(upId, offering);

        int gbItemID = Integer.parseInt(gbItemIDstr);
        List rtnList = gbService.getGradebookEntry(
                        user,
                        offering,
                        gbItemID,
                        viewAll,
                        (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),
                        conn);
        super.setSheetName(upId, "show_only");
        return rtnList;
    }

    /**
     * <p>
     * Delete a gradebook column.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List deleteColumnEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {
        GradebookService gbService
            = GradebookServiceFactory.getService();

        String gbItemIDstr = runtimeData.getParameter("gradebook_itemID");
        boolean viewAll = getPermissions(upId).canDo(user.getUsername(),
            "viewAll");

        if (gbItemIDstr != null) {
            super.setSheetName(upId, "delete_gradebookitem");

            /* Adding  stylesheet parameters */
            gradebookParams.put("gradebookItemID", gbItemIDstr);
        }
        else {
            super.setErrorMsg(upId, "Gradebook Item ID not provided");
            return null;
        }
        return gbService.getGradebookEntries(
                    user,
                    offering,
                    viewAll,
                    conn);
    }
    /**
     * <p>
     * Confirm deletion of the gradebook column.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List deleteColumnConfirmEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {
        GradebookService gbService = GradebookServiceFactory.getService();

        String gbItemIDstr = runtimeData.getParameter("gradebook_itemID");
        String positionStr = runtimeData.getParameter("position");
        boolean viewAll = getPermissions(upId).canDo(user.getUsername(),
            "viewAll");

        command = DEFAULT_COMMAND;

        /* Getting filterModes for roles */
        List filterModes = this.getRoleFilters(upId, offering);

        if (gbItemIDstr != null && positionStr != null) {
            int gbItemID  = Integer.parseInt(gbItemIDstr);
            int position  = Integer.parseInt(positionStr);

            gbService.deleteGradebookItem(
                        user,
                        offering,
                        gbItemID,
                        position,
                        conn);

            // Dirty cache for all users in the offering
            super.broadcastUserOfferingDirtyChannel(user, offering,
                      CHANNEL_HANDLE, false);
        } else {
            super.setErrorMsg(upId,
                "Gradebook Item ID or Position ID not provided");
            return null;
        }
        return getCatalogData(upId,user,offering,filterModes,viewAll,conn);
    }

    /**
     * <p>
     * Edit all the columns' gradebook scores.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List editAllScoresEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Connection conn) throws Exception {
        GradebookService gbService
            = GradebookServiceFactory.getService();
        boolean viewAll = getPermissions(upId).canDo(user.getUsername(),
            "viewAll");

        /* Getting filterModes for roles */
        List filterModes = this.getRoleFilters(upId, offering);

        List rtnList = gbService.getGradebookEntries(
                        user,
                        offering,
                        viewAll,
                        (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),
                        conn);
        super.setSheetName(upId, "editAll");
        return rtnList;
    }

    /**
     * <p>
     * Show user details gradebook scores.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content. List is empty if the user 
     *         does not have permission to modify the requested details.
     * </p>
     */
    private List userDetailsEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {
        GradebookService gbService  = GradebookServiceFactory.getService();
        GradebookActivationService gbActivationService
            = GradebookActivationServiceFactory.getService();

        boolean viewAll = getPermissions(upId).canDo(user.getUsername(),
            "viewAll");

        /* Getting filterModes for roles */
        List filterModes = this.getRoleFilters(upId, offering);

        String gbItemIDstr = runtimeData.getParameter("gradebook_itemID");
        String username = runtimeData.getParameter("username");
        int gbItemID    = Integer.parseInt(gbItemIDstr);
        
        // If not authorized by 'externalUserDetailEditing' property and not same user, send error message.
        if (!getPermissions(upId).canDo(user.getUsername(), "externalUserDetailEditing") && !user.getUsername().equals(username)) {
            super.setErrorMsg(upId, "Not authorized to modify this user's details.");
            return new ArrayList(); 
        }
        
        gradebookParams.put("gradebookItemID", gbItemIDstr);
        gradebookParams.put("username", username);

        // Getting all of the gradebook entries based on who
        // the current user is
        List rtnList = gbService.getGradebookEntries(
                        user,
                        offering,
                        viewAll,
                        (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),
                        conn);

        // Getting all of the activations based on on who
        // the current user is
        gbActivationService.addGradebookActivations(
                        rtnList,
                        user,
                        offering,
                        gbItemID,
                        conn);
        try {
            // Getting all of the score details based on who the
            // select person is.  This may not necessarly be who
            // current person is.  For example, an admin could be
            // looking at someone else submission details.
            gbService.addGradebookScoreDetails(rtnList,
                                            UserFactory.getUser(username),
                                            gbItemID,
                                            false,
                                            offering,
                                            conn);
        } catch (Exception e) {
            super.setErrorMsg(upId, "User with the username "
                + username +
                " is not currently found in the system.");
        }

        super.setSheetName(upId, "details");
        return rtnList;
    }

    /**
     * <p>
     * Export the gradebook.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List exportEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Connection conn) throws Exception {
        GradebookService gbService  = GradebookServiceFactory.getService();
        boolean viewAll = getPermissions(upId).canDo(user.getUsername(),
            "viewAll");
        /* Getting filterModes for roles */
        List filterModes = this.getRoleFilters(upId, offering);
        List rtnList = gbService.getGradebookEntries(
                        user,
                        offering,
                        viewAll,
                        (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),
                        conn);
        super.setSheetName(upId, "export");
        super.setParentChannelDirty(upId, false);
        return rtnList;
    }

    /**
     * <p>
     * View a single question details.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List questionDetailsEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {
        GradebookAssessmentService gbAssessmentService
            = GradebookAssessmentServiceFactory.getService();

        String gbItemIDstr = runtimeData.getParameter("gradebook_itemID");
        String username = runtimeData.getParameter("username");
        int gbItemID = Integer.parseInt(gbItemIDstr);
        gradebookParams.put("gradebookItemID", gbItemIDstr);
        gradebookParams.put("username", username);

        List rtnList = gbAssessmentService.getGBItemQuestionDetails(
                        gbItemID,
                        username, /* what username info is being requested */
                        user,     /* who is requesting */
                        offering,
                        conn);
        
        if (ThemeBroker.isInitialized()) {

            XMLAbleEntity e = new XMLAbleEntity() {
                public String toXML() {

                    // Begin.
                    StringBuffer rslt = new StringBuffer();
                    rslt.append("<theme-set>");

                    Iterator it = Arrays.asList(ThemeBroker.getThemes()).iterator();
                    while (it.hasNext()) {
                        IStructuralTheme t = (IStructuralTheme) it.next();
                        rslt.append(t.toXml());
                    }

                    // End.
                    rslt.append("</theme-set>");
                    return rslt.toString();

                }
            };
            rtnList.add(e);
        }

        super.setSheetName(upId, "question_details");
        return rtnList;
    }

    /**
     * <p>
     * Update a users question details, such as score, file upload, feedback
     * etc.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List updateQuestionDetailsEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {

        GradebookService gbService  = GradebookServiceFactory.getService();
        boolean viewAll = getPermissions(upId).canDo(user.getUsername(),
            "viewAll");

        /* Getting filterModes for roles */
        List filterModes = this.getRoleFilters(upId, offering);

        String gbItemIDstr = runtimeData.getParameter("gradebook_itemID");
        String username     = runtimeData.getParameter("username");

        int gbItemID  = -1;

        if (gbItemIDstr != null) {
            gbItemID  = Integer.parseInt(gbItemIDstr);
        } else {
            super.setErrorMsg(upId, "Gradebook Item ID not provided");
            return null;
        }

        /* Update Score if they have permission */
        if (getPermissions(upId).canDo(user.getUsername(), "editItemScores")) {
            String scoreStr = runtimeData.getParameter("score");

            int score     = -1;
            if (scoreStr != null) {
                scoreStr = scoreStr.trim();
            }

            /* Score is not a required attribute */

            if (scoreStr != null && ! "".equals(scoreStr)) {
                try {
                    score  = Integer.parseInt(scoreStr);
                } catch (NumberFormatException e) {
                    super.setErrorMsg(upId,
                    "Score is not an integer");
                }
            }

            gbService.updateUserScore(gbItemID,username,score,offering,conn);
            gbService.recalculateStatistics(offering, conn);
        }


        Object[] x = runtimeData.getObjectParameterValues("submission-file");

        if (getPermissions(upId).canDo(user.getUsername(),
            "editSubmissionDetails")) {

            String submissionComment = runtimeData.getParameter("submission-comment");

            String submissionFileName    = null;
            InputStream submissionStream = null;
            String submissionContentType = null;

            /*
            StringBuffer errMsg
                = new StringBuffer("File exceeds max size limit of ");

            errMsg.append(getMaxUploadSize());
            */

            if (x != null && x.length > 0) {
                MultipartDataSource content = (MultipartDataSource) x[0];
                submissionFileName = content.getName();
                submissionStream = content.getInputStream();
                submissionContentType = content.getContentType();

                /*
                if (submissionStream.available() > maxFileSize) {
                    super.setErrorMsg(upId, errMsg.toString());
                    return null;
                }
                */
            }

            gbService.updateSubmissionDetails(
            username,
            gbItemID,
            submissionFileName,
            submissionComment,
            submissionContentType,
            submissionStream,
            conn);

            if (submissionStream != null) submissionStream.close();
        }

        if (getPermissions(upId).canDo(user.getUsername(),
            "editFeedbackDetails")) {
            String feedbackComment        = runtimeData.getParameter("feedback-comment");
            String feedbackFileName      = null;
            InputStream feedbackStream   = null;
            String feedbackContentType   = null;

            x = runtimeData.getObjectParameterValues("feedback-file");

            /*
            StringBuffer errMsg = new StringBuffer("File exceeds max size limit of ");
            errMsg.append(getMaxUploadSize());
            */

            if (x != null && x.length > 0) {
                MultipartDataSource content = (MultipartDataSource) x[0];
                feedbackFileName = content.getName();
                feedbackStream = content.getInputStream();
                feedbackContentType = content.getContentType();

                /*
                if (feedbackStream.available() > maxFileSize) {
                    super.setErrorMsg(upId, errMsg.toString());
                    return null;
                }
                */
            }

            gbService.updateFeedbackDetails(
            username,
            gbItemID,
            feedbackFileName,
            feedbackComment,
            feedbackContentType,
            feedbackStream,
            conn);

            if (feedbackStream   != null) feedbackStream.close();
        }

        // Dirty cache for all users in the offering
        super.broadcastUserOfferingDirtyChannel(user, offering,
            CHANNEL_HANDLE, false);

        return getCatalogData(upId, user, offering, filterModes, viewAll, conn);
    }

    /**
     * <p>
     * View all of the question details.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List allQuestionDetailsEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {
        GradebookAssessmentService gbAssessmentService
            = GradebookAssessmentServiceFactory.getService();

        boolean viewAll = getPermissions(upId).canDo(user.getUsername(),
            "viewAll");

        /* Getting filterModes for roles */
        List filterModes = this.getRoleFilters(upId, offering);

        String gbItemIDstr = runtimeData.getParameter("gradebook_itemID");
        int gbItemID = Integer.parseInt(gbItemIDstr);
        gradebookParams.put("gradebookItemID", gbItemIDstr);

        List rtnList = gbAssessmentService.getGBItemQuestionDetails(
                        gbItemID,
                        user,
                        offering,
                        viewAll,
                        (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),
                        conn);

        if (ThemeBroker.isInitialized()) {

            XMLAbleEntity e = new XMLAbleEntity() {
                public String toXML() {

                    // Begin.
                    StringBuffer rslt = new StringBuffer();
                    rslt.append("<theme-set>");

                    Iterator it = Arrays.asList(ThemeBroker.getThemes()).iterator();
                    while (it.hasNext()) {
                        IStructuralTheme t = (IStructuralTheme) it.next();
                        rslt.append(t.toXml());
                    }

                    // End.
                    rslt.append("</theme-set>");
                    return rslt.toString();

                }
            };
            rtnList.add(e);

        }

        super.setSheetName(upId, "question_details");
        return rtnList;
    }

    /**
     * <p>
     * Associate a gradebook column with a delivery system.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private void associateEventHandler(
            String command,
            String upId,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams) throws Exception {
        String associate = runtimeData.getParameter(ASSOCIATION_PARAM_KEY);

        if (associate != null && associate.length() > 0) {
            gradebookParams.put(ASSOCIATIONID_PARAM_KEY, associate);
        }
        super.setSheetName(upId, ASSOCIATION_VIEW);
    }

    /**
     * <p>
     * Search through available association options for
     * a gradebook column.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List searchAssociationsEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {
        List rtnList = new ArrayList();

        GradebookAssessmentService gbAssessmentService
            = GradebookAssessmentServiceFactory.getService();

        String title     = runtimeData.getParameter(TITLE_PARAM_KEY);
        String desc      = runtimeData.getParameter(DESCRIPTION_PARAM_KEY);
        String keyword   = runtimeData.getParameter(KEYWORD_PARAM_KEY);
        String andor     = runtimeData.getParameter(ANDOR_PARAM_KEY);
        String associate = runtimeData.getParameter(ASSOCIATION_PARAM_KEY);

        if (associate != null && associate.length() > 0) {
            gradebookParams.put(ASSOCIATIONID_PARAM_KEY, associate);
        }

        SearchCriteria criteria = new SearchCriteria();

        if (title != null && title.length() > 0) {
            criteria.setName(title);
            gradebookParams.put("searchTitle", title);
        }

        if (desc != null && desc.length() > 0) {
            criteria.setDescription(desc);
            gradebookParams.put("searchDescription", desc);
        }

        // We're doing an OR, if we want to have
        // the user select, then you'd need to
        // pass the selection and set this value
        // accordingly.  The other option is to
        // make a search criteria factory, and
        // it does all this work. - H2
        criteria.matchAllCriteria(false);

        /* adding available assessments */
        rtnList.add(gbAssessmentService.findAssessment(criteria, user, offering));
        super.setSheetName(upId, ASSOCIATION_VIEW);
        return rtnList;
    }

    /**
     * <p>
     * Search through available association options for
     * a gradebook column.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List setAssociationEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {
        GradebookService gbService  = GradebookServiceFactory.getService();
        GradebookAssessmentService gbAssessmentService
            = GradebookAssessmentServiceFactory.getService();
        boolean viewAll = getPermissions(upId).canDo(user.getUsername(),
            "viewAll");

        List rtnList = gbService.getGradebookEntries(
                    user,
                    offering,
                    viewAll,
                    conn);

        String associate = runtimeData.getParameter(ASSOCIATION_PARAM_KEY);

        if (associate != null && associate.length() > 0) {
            gradebookParams.put(ASSOCIATIONID_PARAM_KEY, associate);

            XMLAbleEntity assessment
                = (XMLAbleEntity) gbAssessmentService.getAssessment(
                                                user,
                                                offering,
                                                associate,
                                                true,
                                                conn);
             if (assessment != null) {
                 rtnList.add(assessment);
             }
        }
        super.setSheetName(upId, "add_gradebookitem");
        return rtnList;
    }

    /**
     * <p>
     * Update the gradebook or a gradebook column.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List updateGradebookScoresEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {
        GradebookService gbService  = GradebookServiceFactory.getService();

        String changeLog = runtimeData.getParameter("changeLog");

        if (changeLog != null) {
            storeChangeLog(changeLog, runtimeData, gbService, offering, conn);

            // Dirty cache for all users in the offering
            super.broadcastUserOfferingDirtyChannel(offering,
                        CHANNEL_HANDLE, true);
        }

        boolean viewAll = getPermissions(upId).canDo(user.getUsername(),
            "viewAll");

        /* Getting filterModes for roles */
        List filterModes = this.getRoleFilters(upId, offering);

        return getCatalogData(upId, user, offering, filterModes, viewAll, conn);
    }

    /**
     * <p>
     * View all the gradebook column's weighting values.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List viewWeightingEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            Connection conn) throws Exception {
        boolean viewAll = getPermissions(upId).canDo(user.getUsername(),
            "viewAll");

        /* Getting filterModes for roles */
        List filterModes = this.getRoleFilters(upId, offering);

        super.setSheetName(upId, "weighting");

        return getCatalogData(upId, user, offering, filterModes, viewAll, conn);
    }

    /**
     * <p>
     * Update the gradebook column weights.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List updateWeightsEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {
        GradebookService gbService  = GradebookServiceFactory.getService();
        command = DEFAULT_COMMAND;

        String changeLog = runtimeData.getParameter("changeLog");

        StringTokenizer tokenizer = new StringTokenizer(changeLog, delim);
        String token        = null;
        String gbItemIDStr  = null;
        String weightString = null;

        int gbItemID = -1;
        int weight   = -1;

        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();

            // Getting column index ID
            gbItemIDStr = token.substring(
                                token.lastIndexOf('_') + 1,
                                token.length());

            //Get the weight from the form data
            weightString = "";

            if (runtimeData.getParameter(token) != null) {
                gbItemID  = Integer.parseInt(gbItemIDStr);
                weightString = runtimeData.getParameter(token);

                if (!weightString.equals("")) {
                    weight = Integer.parseInt(weightString);
                    gbService.updateWeight(weight, gbItemID, conn);
                }
            }
        } /* End while loop */

        // Dirty cache for all users in the offering
        super.broadcastUserOfferingDirtyChannel(user, offering,
            CHANNEL_HANDLE, false);

        boolean viewAll = getPermissions(upId).canDo(user.getUsername(),
            "viewAll");

        /* Getting filterModes for roles */
        List filterModes = this.getRoleFilters(upId, offering);

        return getCatalogData(upId, user, offering, filterModes, viewAll, conn);
    }

    /**
     * <p>
     * Activate an gradebook column.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List activationEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {
        GradebookService gbService
                = GradebookServiceFactory.getService();
        GradebookAssessmentService gbAssessmentService
                = GradebookAssessmentServiceFactory.getService();

        List members = Memberships.getMembers(offering, EnrollmentStatus.ENROLLED);

        int numberOfEnrolled = members.size();

        if (numberOfEnrolled <= 0) {
            super.setErrorMsg(upId, "Users must be enrolled in order to activate");
            return null;
        }
        String gbItemIDstr = (String) runtimeData.getParameter("gradebook_itemID");
        int gbItemID = Integer.parseInt(gbItemIDstr);

        boolean viewAll = getPermissions(upId).canDo(user.getUsername(),
            "viewAll");

        String association = null;

        int type = 0;

        List rtnList = gbService.getGradebookEntries(
                user,
                offering,
                viewAll,
                conn);

            /* Finding Association */

        GradebookItem gbItem =  null;
        for (int ix = 0; ix < rtnList.size(); ++ix) {
            gbItem = (GradebookItem) rtnList.get(ix);

            if (gbItem.getId() == gbItemID) {
                association = gbItem.getAssociation();
                type = gbItem.getType();
                if (association != null) {
                    XMLAbleEntity pObj = (XMLAbleEntity) gbAssessmentService.getAssessment(
                    user,
                    offering,
                    association,
                    true,
                    conn);

                    if (pObj != null) {
                        gbItem.addXMLAbleEntity(pObj);
                    }
                }
            }
        }

        //Getting the current time from the db.
        TimeService ts = TimeServiceFactory.getService();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ts.getTimestamp());
        gradebookParams.put("currentMonth",  "" + (calendar.get(calendar.MONTH) + 1));
        gradebookParams.put("currentDay",    "" + calendar.get(calendar.DAY_OF_MONTH));
        gradebookParams.put("currentYear",   "" + calendar.get(calendar.YEAR));
        gradebookParams.put("gradebookItemID", gbItemIDstr);

        if (GradebookServiceImpl.getDeliverySystemUse()){
            if (type == GradebookItem.TYPE_ASSESSMENT){
                AdjunctXMLAbleEntity choices = new AdjunctXMLAbleEntity();
                rtnList.add(choices);
            }
        }

        /* Getting filterModes for roles */
        List filterModes = this.getRoleFilters(upId, offering);

        super.setSheetName(upId, "activation");

        return rtnList;
    }

    /**
     * <p>
     * View a single activation in the gradebook.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List viewActivationEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {
        GradebookActivationService gbActivationService
            = GradebookActivationServiceFactory.getService();

        String activationIDstr = runtimeData.getParameter("activation_id");

        if (activationIDstr == null) {
            super.setErrorMsg(upId, "Users must be enrolled in order to activate");
            return null;
        }

        int activationID = Integer.parseInt(activationIDstr);
        List rtnList = gbActivationService.getGradebookActivation(
                            activationID,
                            user,
                            offering,
                            conn);

        super.setSheetName(upId, "view_activation");

        return rtnList;
    }

    /**
     * <p>
     * View all activations in the gradebook.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List viewAllActivationsEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {
        GradebookActivationService gbActivationService
            = GradebookActivationServiceFactory.getService();
        boolean viewAllActivations = getPermissions(upId).canDo(
            user.getUsername(), "viewAllUserActivations");

        gradebookParams.put("username", user.getUsername());

        List rtnList = gbActivationService.getGradebookItemWithActivations(
                                        offering,
                                        user,
                                        viewAllActivations,
                                        conn);
        
        // Searches for any delivery assessments...
        boolean addThemeStyle = false;
        Iterator it = rtnList.iterator();
        GradebookItem gbItem = null;
        Activation activation = null;
        while (it.hasNext()) {
            gbItem = (GradebookItem) it.next();
            List activations = gbItem.getActivations();
            Iterator ita = activations.iterator();
            while (ita.hasNext()) {
                activation = (Activation) ita.next();
                if (activation.getType().equalsIgnoreCase("ASSESSMENT")) {
                    addThemeStyle = true;
                }
            }
        }
        
        // Append theme and style
        if (ThemeBroker.isInitialized() && addThemeStyle) {

            XMLAbleEntity e = new XMLAbleEntity() {
                public String toXML() {

                    // Begin.
                    StringBuffer rslt = new StringBuffer();
                    rslt.append("<theme-set>");

                    Iterator it = Arrays.asList(ThemeBroker.getThemes()).iterator();
                    while (it.hasNext()) {
                        IStructuralTheme t = (IStructuralTheme) it.next();
                        rslt.append(t.toXml());
                    }

                    // End.
                    rslt.append("</theme-set>");
                    return rslt.toString();

                }
            };
            rtnList.add(e);

        }

        super.setSheetName(upId, "all_activations");

        return rtnList;
    }

    /**
     * <p>
     * Activate an activation in the gradebook.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List insertActivationEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {
        GradebookActivationService gbActivationService
            = GradebookActivationServiceFactory.getService();
        GradebookService gbService
                = GradebookServiceFactory.getService();

        IDecisionCollection dc = null;

        String type = runtimeData.getParameter("type");
        if (type.equals("1")) {
            dc = evaluateAdjunctActivationData(runtimeData);
        }

        parseActivationData(upId, gbService, gbActivationService, offering,
        runtimeData, user, conn, dc);


        // Dirty cache for all users in the offering
        super.broadcastUserOfferingDirtyChannel(user, offering,
            CHANNEL_HANDLE, false);
        return viewAllActivationsEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
    }

    /**
     * <p>
     * Deactivate an activation in the gradebook.
     * </p><p>
     *
     * @param a String of the command
     * @param a String with the multithreaded id
     * @param a User with the user who is making the request
     * @param a Offering with the current context
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return a List with the gradebook content
     * </p>
     */
    private List deactivateActivationEventHandler(
            String command,
            String upId,
            User user,
            Offering offering,
            ChannelRuntimeData runtimeData,
            Hashtable gradebookParams,
            Connection conn) throws Exception {
        GradebookActivationService gbActivationService
            = GradebookActivationServiceFactory.getService();
        String activationIDstr = runtimeData.getParameter("activation_id");

        if (activationIDstr != null) {
            int activationID = Integer.parseInt(activationIDstr);
            gbActivationService.removeActivation(activationID,
                                    offering,
                                    user,
                                    conn);

            // Dirty cache for all users in the offering
            super.broadcastUserOfferingDirtyChannel(user, offering,
                CHANNEL_HANDLE, false);
        }
        return viewAllActivationsEventHandler(
                                command,
                                upId,
                                user,
                                offering,
                                runtimeData,
                                gradebookParams,
                                conn);
    }


    /**
     * <p>
     * Evaluate decisions for a virtuoso activation in the gradebook.
     * </p><p>
     *
     * @param a Map with the runtime parameters
     * </p><p>
     *
     * @return an IDecisionCollection with the decisions
     * </p>
     */
    private IDecisionCollection evaluateAdjunctActivationData(ChannelRuntimeData runtimeData) throws Exception {

        IDecisionCollection dc = null;

        IAdjunctAgent a = ActivationAdjunctAgent.getInstance();

        IChoiceCollection l = a.getChoices();
        String s1 = l.getHandle().getValue();

        IEntityStore store = l.getOwner();

        List dec = new ArrayList();

        Iterator choices = Arrays.asList(l.getChoices()).iterator();
        while (choices.hasNext()) {

            IChoice c = (IChoice) choices.next();
            String s2 = c.getHandle().getValue();

            List sel = new ArrayList();

            if (c.getMaxSelections() == 1) {

                // Uses only the choice-collection and choice as the parameter key
                String key = s1.concat(s2);

                String handle = runtimeData.getParameter(key);
                if (handle != null && !handle.trim().equals("")) {
                    IOption o = c.getOption(Handle.create(handle));
                    IComplement p = o.getComplementType().parse(null);  // must be type-none for now...
                    sel.add(store.createSelection(o, p));
                }

            } else {

                Iterator options = Arrays.asList(c.getOptions()).iterator();
                while (options.hasNext()) {
                    IOption o = (IOption) options.next();
                    String s3 = o.getHandle().getValue();

                    // Uses the full path as the parameter key
                    String key = s1.concat(s2).concat(s3);

                    String value = runtimeData.getParameter(key);
                    if (value != null && !value.trim().equals("")) {
                        IComplement p = o.getComplementType().parse(value);
                        sel.add(store.createSelection(o, p));
                    }
                }
            }

            ISelection[] selections = (ISelection[]) sel.toArray(new ISelection[0]);
            dec.add(store.createDecision(null, c, selections));
        }

        IDecision[] decisions = (IDecision[]) dec.toArray(new IDecision[0]);
        dc = store.createDecisionCollection(l, decisions);

        return dc;
    }

    // View
    protected static final String ASSOCIATION_VIEW = "association";

    // Commands (some of the many commands)
    protected static final String DEFAULT_COMMAND            = "main";
    protected static final String PAGE_COMMAND               = "page";
    protected static final String ADD_COLUMN_COMMAND         = "add";
    protected static final String EDIT_COLUMN_COMMAND        = "edit";
    protected static final String DELETE_COLUMN_COMMAND      = "delete";
    protected static final String DELETE_CONFIRM_COMMAND     = "confirm";
    protected static final String EDIT_COLUMN_SCORES_COMMAND = "show only";
    protected static final String EDIT_ALL_SCORES_COMMAND    = "editscores";
    protected static final String SEARCH_ASSOCIATION_COMMAND = "searchAssociation";
    protected static final String ASSOCIATE_COMMAND          = "associate";
    protected static final String SET_ASSOCIATION_COMMAND    = "setAssociation";
    protected static final String USER_DETAILS_COMMAND       = "details";
    protected static final String QUESTION_DETAILS_COMMAND   = "question_details";
    protected static final String UPDATE_QUESTION_DETAILS_COMMAND = "submit_details";
    protected static final String EXPORT_COMMAND             = "export";
    protected static final String VIEW_ALL_WEIGHTS_COMMAND   = "weighting";
    protected static final String UPDATE_WEIGHTS_COMMAND     = "update_weight";
    protected static final String UPDATE_GB_SCORES_COMMAND   = "update_gb";
    protected static final String ACTIVATION_COMMAND         = "activation";
    protected static final String DEACTIVATE_COMMAND         = "deactivate";
    protected static final String INSERT_ACTIVATION_COMMAND  = "insert_activation";
    protected static final String VIEW_ACTIVATION_COMMMAND   = "view_activation";
    protected static final String VIEW_ALL_ACTIVATIONS_COMMAND    = "all_activations";
    protected static final String UPDATE_GB_COLUMN_SCORES_COMMAND = "update_column";
    protected static final String ALL_QUESTION_DETAILS_COMMAND    = "all_question_details";

    // Runtime Parameters
    protected static final String TITLE_PARAM_KEY          = "title";
    protected static final String DESCRIPTION_PARAM_KEY    = "description";
    protected static final String KEYWORD_PARAM_KEY        = "keyword";
    protected static final String ANDOR_PARAM_KEY          = "andor";
    protected static final String ASSOCIATION_PARAM_KEY    = "onlineAsmt";
    protected static final String ASSOCIATIONID_PARAM_KEY  = "associationID";

    /*
     * Protected inner class to wrap IChoiceCollection
     */
    protected class AdjunctXMLAbleEntity implements XMLAbleEntity {
        public String toXML(){

            StringBuffer xmlBuffer = new StringBuffer();

            IAdjunctAgent agent = ActivationAdjunctAgent.getInstance();
            IChoiceCollection c  = agent.getChoices();

            xmlBuffer.append("<adjunct>");
            xmlBuffer.append(c.toXml());
            xmlBuffer.append("</adjunct>");

            return xmlBuffer.toString();
        }
    } // end AdjunctXMLAbleEntity

} // end GradebookChannel class

