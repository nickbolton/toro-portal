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

package net.unicon.portal.channels.offeringadmin;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import net.unicon.academus.apps.announcement.AnnouncementService;
import net.unicon.academus.apps.announcement.AnnouncementServiceFactory;
import net.unicon.academus.common.properties.AcademusPropertiesType;
import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.academus.domain.lms.EnrollmentModel;
import net.unicon.academus.domain.lms.EnrollmentStatus;
import net.unicon.academus.domain.lms.Memberships;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.OfferingFactory;
import net.unicon.academus.domain.lms.OperationFailedException;
import net.unicon.academus.domain.lms.Role;
import net.unicon.academus.domain.lms.RoleFactory;
import net.unicon.academus.domain.lms.Topic;
import net.unicon.academus.domain.lms.TopicFactory;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.ensemble.AdjunctOfferingData;
import net.unicon.academus.domain.lms.ensemble.EnsembleService;
import net.unicon.academus.domain.sor.AccessType;
import net.unicon.academus.domain.sor.IEntityRecordInfo;
import net.unicon.academus.domain.sor.SystemOfRecordBroker;
import net.unicon.academus.service.calendar.CalendarServiceFactory;
import net.unicon.portal.channels.BaseSubChannel;
import net.unicon.portal.channels.IOfferingSubChannel;
import net.unicon.portal.channels.classfolder.ClassFolderUtil;
import net.unicon.portal.channels.gradebook.GradebookFeedback;
import net.unicon.portal.channels.gradebook.GradebookService;
import net.unicon.portal.channels.gradebook.GradebookServiceFactory;
import net.unicon.portal.channels.gradebook.GradebookSubmission;
import net.unicon.portal.channels.gradebook.IGradebookFileInfo;
import net.unicon.portal.channels.notepad.NotepadServiceFactory;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.portal.common.service.file.FileService;
import net.unicon.portal.common.service.file.FileServiceFactory;
import net.unicon.portal.common.service.notification.NotificationService;
import net.unicon.portal.common.service.notification.NotificationServiceFactory;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.domain.ChannelClassFactory;
import net.unicon.portal.domain.ChannelMode;
import net.unicon.portal.permissions.PermissionsService;
import net.unicon.portal.util.PermissionsUtil;
import net.unicon.portal.util.db.FDbDataSource;
import net.unicon.sdk.catalog.Catalog;
import net.unicon.sdk.catalog.CatalogException;
import net.unicon.sdk.catalog.FLazyCatalog;
import net.unicon.sdk.catalog.IDataSource;
import net.unicon.sdk.catalog.IFilterMode;
import net.unicon.sdk.catalog.IPageMode;
import net.unicon.sdk.catalog.ISortMode;
import net.unicon.sdk.catalog.db.FDbFilterMode;
import net.unicon.sdk.catalog.db.FDbPageMode;
import net.unicon.sdk.catalog.db.FDbSortMode;
import net.unicon.sdk.catalog.db.IDbEntryConvertor;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.util.ExceptionUtils;

import org.jasig.portal.IMultithreadedMimeResponse;
import org.jasig.portal.PortalException;
import org.jasig.portal.services.LogService;



// ************************************************



public class OfferingAdminChannel extends BaseSubChannel

implements IMultithreadedMimeResponse {



    protected static final String workDir = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.portal.channels.admin.OfferingAdminChannel.workDir");

    protected static final String selfEnrolledChecked = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.portal.channels.admin.OfferingAdminChannel.enrollSelfChecked");

    private static final boolean ENSEMBLE_IS_ENABLED =
            UniconPropertiesFactory.getManager(
                    PortalPropertiesType.LMS).getPropertyAsBoolean(
                        "net.unicon.portal.channels.admin.OfferingAdminChannel.ensemble.enabled");

    protected static final String exportFileNameKey =

        "OfferingAdminChannel.exportFilename";



    protected static final String mainCommand         = "main";
    protected static final String viewAllCommand      = "viewAll";
    protected static final String viewInactiveCommand = "viewInactive";
    protected static final String exportCommand       = "export";
    protected static final String viewCommand         = "view";
    protected static final String addCommand          = "add";
    protected static final String deleteCommand       = "delete";
    protected static final String editCommand         = "edit";
    protected static final String inactivateCommand   = "inactivate";
    protected static final String activateCommand     = "activate";
    protected static final String confirmInactivateCommand =
        "confirmInactivate";

    protected static final String addSubmitCommand    = "addSubmit";
    protected static final String deleteSubmitCommand = "deleteSubmit";
    protected static final String editSubmitCommand   = "editSubmit";
    protected static final String searchCommand       = "search";
    protected static final String mainSearchCommand   = "mainSearch";

    protected static final String topicNameParam      = "topicName";
    protected static final String offeringNameParam   = "offeringName";
    protected static final String offeringNameSearchParam =
        "offeringNameSearch";

    protected static final String offeringDescParam   = "offeringDescription";
    protected static final String userEnrollmentModelParam =
        "userEnrollmentModel";

    protected static final String defaultRoleParam    = "defaultRole";
    protected static final String creatorRoleParam    = "selfDefaultType";
    protected static final String enrollCreatorParam  = "enrollSelf";
    protected static final String enrollCheckedParam  = "enrollSelfCheckedParam";
    protected static final String channelParam        = "channel";
    protected static final String confirmParam        = "confirm";



    // Added for Requirements OA 4.1 - 4.12
    protected static final String offeringIdParam     = "offeringIdParam";
    protected static final String offeringTermParam   = "offeringTermParam";
    protected static final String offeringMonthStartParam =
                                                "offeringMonthStartParam";
    protected static final String offeringDayStartParam =
                                                "offeringDayStartParam";
    protected static final String offeringYearStartParam =
                                                "offeringYearStartParam";
    protected static final String offeringMonthEndParam  =
                                                "offeringMonthEndParam";
    protected static final String offeringDayEndParam  = "offeringDayEndParam";
    protected static final String offeringYearEndParam = "offeringYearEndParam";
    protected static final String offeringMtgMonParam  = "offeringMtgMonParam";
    protected static final String offeringMtgTueParam  = "offeringMtgTueParam";
    protected static final String offeringMtgWedParam  = "offeringMtgWedParam";
    protected static final String offeringMtgThuParam  = "offeringMtgThuParam";
    protected static final String offeringMtgFriParam  = "offeringMtgFriParam";
    protected static final String offeringMtgSatParam  = "offeringMtgSatParam";
    protected static final String offeringMtgSunParam  = "offeringMtgSunParam";
    protected static final String offeringHourStartParam =
                                                "offeringHourStartParam";

    protected static final String offeringMinuteStartParam =

                                                "offeringMinuteStartParam";

    protected static final String offeringAmPmStartParam =

                                                "offeringAmPmStartParam";

    protected static final String offeringHourEndParam =

                                                "offeringHourEndParam";

    protected static final String offeringMinuteEndParam =

                                                "offeringMinuteEndParam";

    protected static final String offeringAmPmEndParam =

                                                "offeringAmPmEndParam";

    protected static final String offeringLocationParam =

                                                "offeringLocationParam";


    /**
     * Ensemble offering attributes
     */
    private static final String PUBLISHED_PARAM = "published";
    private static final String BUY_NOW_PARAM = "buy_now";

    // ***********************************************************************



    // Added for Requirement OA 2.1

    protected static final String copyCommand = "copy";

    protected static final String copySubmitCommand = "copySubmit";

    // ***********************************************************************



    //generic select/option entries

    protected static final String allTopics = "All Topics";

    protected static final String allEnrolTypes = "All Types";



    private static Catalog superUserCatalog = null;

    private static Catalog userCatalog = null;

    private static Catalog searchCatalog = null;



    private static final long ADMIN_USER = UniconPropertiesFactory.getManager(

        AcademusPropertiesType.LMS).getPropertyAsLong(

                "net.unicon.academus.domain.lms.Role.1.Administrator");

    public OfferingAdminChannel() {

        super();

    }



    public void buildXML(String upId) throws Exception {

        setupParameters(upId);

        Hashtable offerindAdminParams = getXSLParameters(upId);

        LogService.log(LogService.DEBUG, "OfferingAdminChannel:buildXML:(): workerActionURL:" + offerindAdminParams.get("workerActionURL"));

        String xmlBody = performCommand(upId,

        getRuntimeData(upId).getParameter("command"));

        StringBuffer xmlSB = new StringBuffer();

        xmlSB.append("<offeringAdmin ");

        xmlSB.append(xmlBody);

        xmlSB.append("</offeringAdmin>");

        setXML(upId, xmlSB.toString());

    }



    protected String performCommand(String upId, String command)

    throws Exception {

        String xmlBody = "";

        if (command == null || "".equals(command)) {

            setSheetName(upId, mainCommand);

            getXSLParameters(upId).put("catCurrentCommand", searchCommand);

            getXSLParameters(upId).put("catFormCommand", searchCommand);
            getXSLParameters(upId).put("catPageSize", "" + evaluatePageSize(upId));




            return ">";

        }



        if (mainSearchCommand.equals(command)) {

            setSheetName(upId, searchCommand);

            getXSLParameters(upId).put("catCurrentCommand", searchCommand);
            getXSLParameters(upId).put("catPageSize", "" + evaluatePageSize(upId));


            return ">";

        }



        setSheetName(upId, command);

        if (addCommand.equals(command)) {
            xmlBody = addCommand(upId);
        } else if (deleteCommand.equals(command)) {
            xmlBody = deleteCommand(upId);
        } else if (exportCommand.equals(command)) {

            xmlBody = exportCommand(upId);

        } else if (viewInactiveCommand.equals(command)) {

            return viewInactiveCommand(upId);

        } else if (searchCommand.equals(command)) {

            xmlBody = searchCommand(upId);

        }



        else if (addSubmitCommand.equals(command)) {

            xmlBody = addSubmitCommand(upId);

        } else if (deleteSubmitCommand.equals(command)) {

            try {

                xmlBody = deleteSubmitCommand(upId);

            } catch (Exception e) {

                String msg = "Offering delete failed: " +

                    ExceptionUtils.getExceptionMessage(e);

                setErrorMsg(upId, msg);

                return ">";



            }

        } else if (editSubmitCommand.equals(command)) {

            xmlBody = editSubmitCommand(upId);

        } else if (activateCommand.equals(command)) {

            xmlBody = activateCommand(upId);

        } else if (confirmInactivateCommand.equals(command)) {

            xmlBody = confirmInactivateCommand(upId);

            //************************************************************

            // added copyCommand to the qualification below for Req OA 2.1

            //************************************************************

        } else if (editCommand.equals(command) ||

                   viewCommand.equals(command) ||

                   inactivateCommand.equals(command) ||

                   copyCommand.equals(command)) {

            xmlBody = singularActionCommand(upId, editCommand.equals(command));

          //************************************************************

          // new condition for requirement OA 2.1

          //************************************************************

        } else if (copySubmitCommand.equals(command)) {

            xmlBody = copySubmitCommand(upId);

        }







 /*       if ((xmlBody == null || "".equals(xmlBody)) &&

            "".equals(getErrorMsg(upId))) {

            if ("".equals(command)) {

               // searchCommand:

               //xmlBody = searchCommand(upId);

               setSheetName(upId, searchCommand);

            }

        }*/

        getXSLParameters(upId).put("catPageSize", "" + evaluatePageSize(upId));

        return xmlBody;

    }



    protected String toXML(List offerings, String subElements, boolean editing, boolean single)

    throws Exception {

        StringBuffer xmlSB = new StringBuffer();

        List topics = TopicFactory.getTopics(TopicType.ACADEMICS);



        for (int ix=0; offerings != null && ix < offerings.size(); ++ix) {

            Offering offering = (Offering)offerings.get(ix);

            List channels = offering.getChannels();

            xmlSB.append("<offering id=\"" + offering.getId() + "\">\n");

            xmlSB.append("    <name>")
                .append("<![CDATA[")
                .append(offering.getName())
                .append("]]>")
                .append("</name>\n");

            xmlSB.append("    <description>")
                .append("<![CDATA[")
                .append(offering.getDescription())
                .append("]]>")
                .append("</description>\n");

            if (!editing) {

                xmlSB.append("<enrollmentModel default=\"true\">")
                    .append("<![CDATA[")
                    .append(offering.getEnrollmentModel().toString())
                    .append("]]>")
                    .append("</enrollmentModel>\n");

            }



            Iterator itr = channels.iterator();

            while (itr.hasNext()) {

                ChannelClass channel = (ChannelClass)itr.next();
                xmlSB.append("    <channel id=\"")
                    .append(channel.getHandle())
                    .append("\">")
                    .append("<![CDATA[")
                    .append(channel.getLabel())
                    .append("]]>")
                    .append("</channel>\n");

            }



            if (editing) {

                ArrayList enrollmentModels = EnrollmentModel.getEnrollmentModels();

                for (int i = 0; i < enrollmentModels.size(); i++)
                  {
                      String enrollmentModelName = (String) enrollmentModels.get(i);

                      xmlSB.append("    <enrollmentModel default=\"")
                          .append(offering.getEnrollmentModel().toString().equals(enrollmentModelName))
                        .append("\">")
                        .append("<![CDATA[")
                        .append(enrollmentModelName)
                        .append("]]>")
                        .append("</enrollmentModel>");
                  }

                List roles = RoleFactory.getOfferingRoles(offering);

                itr = roles.iterator();

                while (itr.hasNext()) {

                    Role role = (Role)itr.next();

                    boolean isDefault = offering.getDefaultRole().getId() ==
                        role.getId();

                    xmlSB.append("    <role id=\"")
                        .append(role.getId())
                        .append("\" default=\"")
                        .append(new Boolean(isDefault).toString())
                        .append("\">")
                        .append("<![CDATA[")
                        .append(role.getLabel())
                        .append("]]>")
                        .append("</role>\n");
                }

                itr = topics.iterator();

                while (itr.hasNext()) {

                    Topic topic = (Topic)itr.next();

                    String[] paths = topic.getParentGroup().getPathsAsStrings(" -&gt; ", false);

                    for (int index = 0; index < paths.length; index++) {

                        xmlSB.append("    <topic id=\"" )
                            .append(topic.getId())
                            .append("\"");

                        if (offering.getTopic().getId() == topic.getId()) {
                            xmlSB.append(" selected=\"true\"");
                        }

                        xmlSB.append(">")
                            .append("<topicType>")
                            .append(topic.getTopicType())
                            .append("</topicType>");

                        xmlSB.append("<name>")
                            .append(paths[index])
                            .append(" -&gt; ").append(topic.getName())
                            .append("</name>");

                        xmlSB.append("<description>")
                            .append("<![CDATA[")
                            .append(topic.getDescription())
                            .append("]]>")
                            .append("</description>")
                            .append("</topic>");

                    }

                }

            } else {

                Role role = offering.getDefaultRole();

                xmlSB.append("    <role id=\"")
                    .append(role.getId())
                    .append("\">")
                    .append(role.getLabel())
                    .append("</role>\n");

                Topic topic = offering.getTopic();

                String[] paths = topic.getParentGroup().getPathsAsStrings(" -&gt; ", false);

                for (int index = 0; index < paths.length; index++) {
                    xmlSB.append("    <topic id=\"" )
                        .append(topic.getId())
                        .append("\">");

                    xmlSB.append("<topicType>" )
                        .append(topic.getTopicType())
                        .append("</topicType>\n");

                    xmlSB.append("<name>")
                        .append(paths[index])
                        .append(" -&gt; ")
                        .append(topic.getName())
                        .append("</name>");

                    xmlSB.append("<description>")
                        .append("<![CDATA[")
                        .append(topic.getDescription())
                        .append("]]>")
                        .append("</description>")
                        .append("</topic>");

                }

            }

            if (subElements != null && !"".equals(subElements)) {

                xmlSB.append(subElements);

            }

            //***********************************************************/

            // New XML offering elements for requirements OA 4.1-4.12

            //

            // Optional Offering ID

            xmlSB.append("\n     <optionalOfferingId>");

            String optionalOfferingId =  offering.getOptionalId();

            if (optionalOfferingId != null) {
                xmlSB.append("<![CDATA[")
                    .append(optionalOfferingId)
                    .append("]]>");
            }

            xmlSB.append("</optionalOfferingId>\n");

            // Optional Offering Term

            xmlSB.append("     <optionalOfferingTerm>");

            String optionalOfferingTerm =  offering.getOptionalTerm();

            if (optionalOfferingTerm != null) {
                xmlSB.append("<![CDATA[")
                    .append(optionalOfferingTerm)
                    .append("]]>");
            }

            xmlSB.append("</optionalOfferingTerm>\n");

            // Optional Offering Start Date

            xmlSB.append("     <optionalOfferingStartDate>\n");

            int optionalOfferingMonthStart =  offering.getOptionalMonthStart();
            int optionalOfferingDayStart =  offering.getOptionalDayStart();
            int optionalOfferingYearStart =  offering.getOptionalYearStart();

            xmlSB.append("        <MonthStart>")
                .append(optionalOfferingMonthStart)
                .append("</MonthStart>\n");

            xmlSB.append("        <DayStart>");

            xmlSB.append(optionalOfferingDayStart);

            xmlSB.append("</DayStart>\n");

            xmlSB.append("        <YearStart>");

            xmlSB.append(optionalOfferingYearStart);

            xmlSB.append("</YearStart>\n");

            xmlSB.append("     </optionalOfferingStartDate>\n");



            // Optional Offering End Date

            xmlSB.append("     <optionalOfferingEndDate>\n");

            int optionalOfferingMonthEnd =  offering.getOptionalMonthEnd();

            int optionalOfferingDayEnd =  offering.getOptionalDayEnd();

            int optionalOfferingYearEnd =  offering.getOptionalYearEnd();

            xmlSB.append("        <MonthEnd>");

            xmlSB.append(optionalOfferingMonthEnd);

            xmlSB.append("</MonthEnd>\n");

            xmlSB.append("        <DayEnd>");

            xmlSB.append(optionalOfferingDayEnd);

            xmlSB.append("</DayEnd>\n");

            xmlSB.append("        <YearEnd>");

            xmlSB.append(optionalOfferingYearEnd);

            xmlSB.append("</YearEnd>\n");

            xmlSB.append("     </optionalOfferingEndDate>\n");



            // Optional Offering Meeting Days

            xmlSB.append("     <optionalOfferingMeetingDays>\n");

            xmlSB.append("          <MeetsMonday>");

            xmlSB.append(offering.doesMeet(OfferingFactory.MONDAY) ? 1 : 0);

            xmlSB.append("    </MeetsMonday>\n");

            xmlSB.append("          <MeetsTuesday>");

            xmlSB.append(offering.doesMeet(OfferingFactory.TUESDAY) ? 1 : 0);

            xmlSB.append("</MeetsTuesday>\n");

            xmlSB.append("          <MeetsWednesday>");

            xmlSB.append(offering.doesMeet(OfferingFactory.WEDNESDAY) ? 1 : 0);

            xmlSB.append("</MeetsWednesday>\n");

            xmlSB.append("          <MeetsThursday>");

            xmlSB.append(offering.doesMeet(OfferingFactory.THURSDAY) ? 1 : 0);

            xmlSB.append("</MeetsThursday>\n");

            xmlSB.append("          <MeetsFriday>");

            xmlSB.append(offering.doesMeet(OfferingFactory.FRIDAY) ? 1 : 0);

            xmlSB.append("</MeetsFriday>\n");

            xmlSB.append("          <MeetsSaturday>");

            xmlSB.append(offering.doesMeet(OfferingFactory.SATURDAY) ? 1 : 0);

            xmlSB.append("</MeetsSaturday>\n");

            xmlSB.append("          <MeetsSunday>");

            xmlSB.append(offering.doesMeet(OfferingFactory.SUNDAY) ? 1 : 0);

            xmlSB.append("</MeetsSunday>\n");

            xmlSB.append("     </optionalOfferingMeetingDays>\n");



            // Optional Offering Start Time

            xmlSB.append("     <optionalOfferingStartTime>\n");

            int optionalOfferingHourStart =  offering.getOptionalHourStart();

            int optionalOfferingMinuteStart =

                                           offering.getOptionalMinuteStart();

            int optionalOfferingAmPmStart =  offering.getOptionalAmPmStart();

            xmlSB.append("        <HourStart>");

            xmlSB.append(optionalOfferingHourStart);

            xmlSB.append("</HourStart>\n");

            xmlSB.append("        <MinuteStart>");

            xmlSB.append(optionalOfferingMinuteStart);

            xmlSB.append("</MinuteStart>\n");

            xmlSB.append("        <AmPmStart>");

            xmlSB.append(optionalOfferingAmPmStart);

            xmlSB.append("</AmPmStart>\n");

            xmlSB.append("     </optionalOfferingStartTime>\n");





            // Optional Offering End Time

            xmlSB.append("     <optionalOfferingEndTime>\n");

            int optionalOfferingHourEnd =  offering.getOptionalHourEnd();



            int optionalOfferingMinuteEnd =  offering.getOptionalMinuteEnd();

            int optionalOfferingAmPmEnd =  offering.getOptionalAmPmEnd();

            xmlSB.append("        <HourEnd>");

            xmlSB.append(optionalOfferingHourEnd);

            xmlSB.append("</HourEnd>\n");

            xmlSB.append("        <MinuteEnd>");

            xmlSB.append(optionalOfferingMinuteEnd);

            xmlSB.append("</MinuteEnd>\n");

            xmlSB.append("        <AmPmEnd>");

            xmlSB.append(optionalOfferingAmPmEnd);

            xmlSB.append("</AmPmEnd>\n");

            xmlSB.append("     </optionalOfferingEndTime>\n");



            // Optional Offering Location

            xmlSB.append("     <optionalOfferingLocation>");

            String optionalOfferingLocation =  offering.getOptionalLocation();

            if (optionalOfferingLocation != null) {
                xmlSB.append("<![CDATA[")
                    .append(optionalOfferingLocation)
                    .append("]]>");

            }

            xmlSB.append("</optionalOfferingLocation>\n");


            /***********************************************************/


            // Detect presence of Ensemble and add adjunct offering data
            // only if viewing a single offering
            if (ENSEMBLE_IS_ENABLED && single) {

                AdjunctOfferingData offeringData =
                    EnsembleService.getAdjunctOfferingData(offering.getId());

                boolean published = offeringData.isPublished();
                boolean hasBuyNowEnabled = offeringData.hasBuyNowEnabled();

                xmlSB.append("<ensemble published=\"");
                xmlSB.append(Boolean.toString(published));
                xmlSB.append("\" buyNowEnabled=\"");
                xmlSB.append(Boolean.toString(hasBuyNowEnabled));
                xmlSB.append("\" />");
            }

            xmlSB.append("</offering>");

        }

        return xmlSB.toString();

    }



    protected void setupParameters(String upId) {

        //setSSLLocation(upId, "OfferingAdminChannel.ssl");

    ChannelDataManager.setSSLLocation(upId,

          ChannelDataManager.getChannelClass(upId).getSSLLocation());



        Hashtable offeringAdminParams = getXSLParameters(upId);



        // Add the stylesheet parameters

//        offeringAdminParams.put("viewAllCommand", viewAllCommand);

        offeringAdminParams.put("viewInactiveCommand", viewInactiveCommand);

        offeringAdminParams.put("viewCommand", viewCommand);

        offeringAdminParams.put("exportCommand", exportCommand);

        offeringAdminParams.put("addCommand", addCommand);

        offeringAdminParams.put("deleteCommand", deleteCommand);

        offeringAdminParams.put("editCommand", editCommand);

        offeringAdminParams.put("inactivateCommand", inactivateCommand);

        offeringAdminParams.put("activateCommand", activateCommand);

        offeringAdminParams.put("searchCommand", searchCommand);



        offeringAdminParams.put("confirmInactivateCommand",

            confirmInactivateCommand);

        offeringAdminParams.put("addSubmitCommand", addSubmitCommand);

        offeringAdminParams.put("deleteSubmitCommand", deleteSubmitCommand);

        offeringAdminParams.put("editSubmitCommand", editSubmitCommand);

        offeringAdminParams.put("userEnrollmentModelParam",

            userEnrollmentModelParam);

        offeringAdminParams.put("defaultRoleParam", defaultRoleParam);

        offeringAdminParams.put("confirmParam", confirmParam);

        offeringAdminParams.put("channelParam", channelParam);

        offeringAdminParams.put("offeringNameParam", offeringNameParam);

        offeringAdminParams.put("topicNameParam", topicNameParam);

        offeringAdminParams.put("offeringDescParam",

            offeringDescParam);

        offeringAdminParams.put("offeringNameSearchParam",

            offeringNameSearchParam);

        offeringAdminParams.put("navigateAddMessage", "false");

        offeringAdminParams.put("navigateRemoveMessage", "false");



        // **************************************************************

        // Added for Requirements OA 4.1-4.12

        offeringAdminParams.put("offeringIdParam", offeringIdParam);

        offeringAdminParams.put("offeringTermParam", offeringTermParam);

        offeringAdminParams.put("offeringMonthStartParam",

                                offeringMonthStartParam);

        offeringAdminParams.put("offeringDayStartParam",

                                offeringDayStartParam);

        offeringAdminParams.put("offeringYearStartParam",

                                offeringYearStartParam);

        offeringAdminParams.put("offeringMonthEndParam",

                                offeringMonthEndParam);

        offeringAdminParams.put("offeringDayEndParam",

                                offeringDayEndParam);

        offeringAdminParams.put("offeringYearEndParam",

                                offeringYearEndParam);

        offeringAdminParams.put("offeringMtgMonParam", offeringMtgMonParam);

        offeringAdminParams.put("offeringMtgTueParam", offeringMtgTueParam);

        offeringAdminParams.put("offeringMtgWedParam", offeringMtgWedParam);

        offeringAdminParams.put("offeringMtgThuParam", offeringMtgThuParam);

        offeringAdminParams.put("offeringMtgFriParam", offeringMtgFriParam);

        offeringAdminParams.put("offeringMtgSatParam", offeringMtgSatParam);

        offeringAdminParams.put("offeringMtgSunParam", offeringMtgSunParam);

        offeringAdminParams.put("offeringHourStartParam",

                                offeringHourStartParam);

        offeringAdminParams.put("offeringMinuteStartParam",

                                offeringMinuteStartParam);

        offeringAdminParams.put("offeringAmPmStartParam",

                                offeringAmPmStartParam);

        offeringAdminParams.put("offeringHourEndParam",

                                offeringHourEndParam);

        offeringAdminParams.put("offeringMinuteEndParam",

                                offeringMinuteEndParam);

        offeringAdminParams.put("offeringAmPmEndParam",

                                offeringAmPmEndParam);

        offeringAdminParams.put("offeringLocationParam",

                                offeringLocationParam);



        // set the current day, month and year parameters for use during the

        // addition of an offering

        Calendar defaultCalendar = Calendar.getInstance();

        // DAY_OF_MONTH ranges from 1 to 31

        offeringAdminParams.put("currentDay",

                            defaultCalendar.get(Calendar.DAY_OF_MONTH) + "");

        // MONTH ranges from 0 (for Jan.) to 11 (for Dec.)

        offeringAdminParams.put("currentMonth",

                            (defaultCalendar.get(Calendar.MONTH) + 1) + "");

        // YEAR corresponds to the four digit year

        offeringAdminParams.put("currentYear",

                            defaultCalendar.get(Calendar.YEAR) + "");

        offeringAdminParams.put("copyCommand", copyCommand);

        offeringAdminParams.put("copySubmitCommand", copySubmitCommand);

        offeringAdminParams.put("publishedParam", PUBLISHED_PARAM);

        offeringAdminParams.put("buyNowParam", BUY_NOW_PARAM);

        // **************************************************************

    }



    protected String singularActionCommand(String upId, boolean editing)

    throws Exception {

        List offerings = new ArrayList();

        offerings.add(getOfferingFromParam(upId));



        String topicName = getRuntimeData(upId).getParameter("topicName");

        String offName = getRuntimeData(upId).getParameter("offName");

        String optIdString = getRuntimeData(upId).getParameter("optId");



        if ( topicName == null) {

            topicName ="";

        }

        if(offName == null) {

            offName ="";

        }



        if (optIdString == null || "".equals(optIdString)) {

            getXSLParameters(upId).put("optId", "");

            optIdString="0";

        }else {

            try {

                Long  optId=Long.valueOf(optIdString);

                 getXSLParameters(upId).put("optId", optIdString);



            } catch ( NumberFormatException nfe) {

                System.out.println("Offering id can not be a string." + nfe);

                getXSLParameters(upId).put("optId", "");



                optIdString = "0";

            }



        }



        getXSLParameters(upId).put("topicName",topicName);
        getXSLParameters(upId).put("offName", offName);
        getXSLParameters(upId).put("catCurrentCommand", searchCommand);
        getXSLParameters(upId).put("catPageSize", String.valueOf(evaluatePageSize(upId)));



        return ">"+toXML(offerings, null, editing, true);

    }



    protected Offering getOfferingFromParam(String upId) throws Exception {

        String offeringIDString = getRuntimeData(upId).getParameter("ID");

        Offering offering = null;

        if (offeringIDString != null) {

            long offeringID = Long.parseLong(offeringIDString);

            offering = OfferingFactory.getOffering(offeringID);

        }

        return offering;

    }



    protected String getDefaultRolesXML() throws Exception {

        StringBuffer sb = new StringBuffer();

        List roles = RoleFactory.getDefaultRoles(Role.OFFERING);

        if (roles != null) {

            Iterator itr = roles.iterator();

            while (itr != null && itr.hasNext()) {

                Role role = (Role)itr.next();

                String roleName = role.getLabel();

                Boolean isDefault = new Boolean("Member".equals(roleName));

                sb.append("<role id=\"")
                    .append(role.getId())
                    .append("\" default=\"")
                    .append(isDefault.toString())
                    .append("\">")
                    .append("<![CDATA[")
                    .append(roleName)
                    .append("]]>")
                    .append("</role>\n");

            }

        }

        return sb.toString();

    }



    protected String addCommand(String upId) throws Exception {

        StringBuffer xmlSB = new StringBuffer();

        List channelList =

            ChannelClassFactory.getChannelClasses(ChannelMode.OFFERING);

        List topics = TopicFactory.getTopics(TopicType.ACADEMICS);



        if (topics == null || topics.size() <= 0) {

            setErrorMsg(upId, "No topics available.");

            return ">";

        }



        if (channelList == null || channelList.size() <= 0) {

            setErrorMsg(upId, "No channels available.");

            return ">";

        }



        Offering offering = getOfferingFromParam(upId);

        ArrayList enrollmentModels = EnrollmentModel.getEnrollmentModels();

        xmlSB.append(">");

        for (int i = 0; i < enrollmentModels.size(); i++)

         {

          String enrollmentModelName = (String) enrollmentModels.get(i);

          xmlSB.append("    <enrollmentModel default=");

          if (i == 0) {

             xmlSB.append("\"true\"");

          } else {

             xmlSB.append("\"false\"");

          }

          xmlSB.append(">")
              .append("<![CDATA[")
              .append(enrollmentModelName)
            .append("]]>")
            .append("</enrollmentModel>");

         }

        xmlSB.append(getDefaultRolesXML());

        Iterator itr = channelList.iterator();

        while (itr.hasNext()) {
            ChannelClass channel = (ChannelClass)itr.next();
            xmlSB.append("    <channel id=\"")
                .append(channel.getHandle())
                .append("\">")
                .append(channel.getLabel())
                .append("</channel>\n");

        }

        itr = topics.iterator();

        while (itr.hasNext()) {

            Topic topic = (Topic)itr.next();

            String[] paths = topic.getParentGroup().getPathsAsStrings(" -&gt; ", false);

            for (int index = 0; index < paths.length; index++) {
                xmlSB.append("<topic id=\"").append(""+topic.getId()).append("\">");
                xmlSB.append("<name>");
                xmlSB.append(paths[index]);
                xmlSB.append(" -&gt; ").append(topic.getName());
                xmlSB.append("</name>");
                xmlSB.append("</topic>");

            }

        }

        // Detect presence of Ensemble
        if (ENSEMBLE_IS_ENABLED) {

            xmlSB.append("<ensemble published=\"false\" buyNowEnabled=\"false\" />");
        }

        getXSLParameters(upId).put("catPageSize", "" + evaluatePageSize(upId));
        getXSLParameters(upId).put(enrollCheckedParam, selfEnrolledChecked);
        return xmlSB.toString();

    }



    protected String viewInactiveCommand(String upId) throws Exception {

        ///setSheetName(upId, viewInactiveCommand);



        // Gather modes to apply.

        List sortModes = new ArrayList();

        List filterModes = new ArrayList();



        // Calculate which page to view.

        IPageMode pg = null;

        int pgSize = evaluatePageSize(upId);

        int pgNum = evaluateCurrentPage(upId);

        pg = new FDbPageMode(pgSize, pgNum);



        Object[] sParam = new Object[] { Integer.toString(Offering.INACTIVE) };

        Object[] eParam = new Object[]

            { Integer.toString(EnrollmentStatus.ENROLLED.toInt()) };



        Catalog cat = null;



        // Is this user the super-user

        boolean superUser = getDomainUser(upId).isSuperUser();



        // Is this user an Administrator

        boolean adminUser = PermissionsUtil.isPortalAdministrator(

            getStaticData(upId).getAuthorizationPrincipal());



        /* The superuser gets to see all of the inactive offerings.

           An administrator (non-super-user) gets to see all of the

           inactive offerings. A regular user gets to see all of



           the inactive offerings that they are enrolled in.

        */



        if (superUser || adminUser) {

            filterModes.add(new FDbFilterMode("status = ?", sParam));

            cat = getSuperUserCatalog().subCatalog(

                        (ISortMode[]) sortModes.toArray(new ISortMode[0]),

                        (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),

                        pg);



        } else {

            Object[] nParam = new Object[] {getDomainUser(upId).getUsername()};

            filterModes.add(new FDbFilterMode("status = ?", sParam));

            filterModes.add(new FDbFilterMode("m.enrollment_status=?", eParam));

            filterModes.add(new FDbFilterMode("m.user_name=?", nParam));

            cat = getUserCatalog().subCatalog(

                        (ISortMode[]) sortModes.toArray(new ISortMode[0]),

                        (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),

                        pg);



        }

        List elements = cat.elements();

        getXSLParameters(upId).put("catCurrentCommand", viewInactiveCommand);
        getXSLParameters(upId).put("catPageSize", "" + pgSize);



        return toXML(elements, pg, upId);



    }



    // IMimeResponseAdapter interface methods

    public String getContentType(String upId) {

        return "application/zip";

    }



    public InputStream getInputStream(String upId) throws IOException {

        String filename = null;

        try {

            filename = getTargetExportFileName(upId);



        } catch (Exception e) {

            throw new IOException(e.toString());

        }

        // Clear out the target export filename

        removeChannelAttribute(upId, exportFileNameKey);

        File file = new File(workDir + filename);

        if (file.exists()) {

            return new FileInputStream(file);

        }

        throw new IOException ("File " + filename + " not found.");

    }



    public void downloadData(OutputStream out, String upId) throws IOException {

        // Clear out the target export filename

        removeChannelAttribute(upId, exportFileNameKey);

    }



    public String getName(String upId) {

        String filename = null;

        try {

            filename = getTargetExportFileName(upId);

        } catch (Exception e) {

            e.printStackTrace();

            return null;

        }

        return filename;

    }



    public Map getHeaders(String upId) {

        HashMap headers = new HashMap();



        headers.put("Content-Disposition", "attachment; filename=\"" +

            getName(upId) + "\"");



        return headers;

    }



    /**

     * Let the channel know that there were problems with the download

     * @param e

     */

    public void reportDownloadError(Exception e) {

      LogService.log(LogService.ERROR, "OfferingAdminChannel::reportDownloadError(): " + e.getMessage());

    }





    // This method returns the path to the target export file generating

    // it if needed.

    protected String getTargetExportFileName(String upId) throws Exception {

        String filename =

            (String)getChannelAttribute(upId, exportFileNameKey);

        if (filename == null) {

            filename = generateExportFile(upId);

            putChannelAttribute(upId, exportFileNameKey, filename);



        }

        return filename;

    }



    protected String exportCommand(String upId) throws Exception {

        return "<export file=\"exports/" + generateExportFile(upId) + "\"/>";

    }



    protected String generateExportFile(String upId) throws Exception {

        Offering offering = getOfferingFromParam(upId);

        Method method = null;

        List offeringList = new ArrayList();

        offeringList.add(offering);



        Iterator itr = ChannelClassFactory.getChannelClasses(

            ChannelMode.OFFERING).iterator();

        StringBuffer sb = new StringBuffer();

        StringBuffer offeringElements = new StringBuffer();



        while (itr.hasNext()) {

            ChannelClass cc = (ChannelClass)itr.next();

            Object cobj = Class.forName(cc.getClassName()).newInstance();

            if (cobj instanceof IOfferingSubChannel) {

                offeringElements.append(((IOfferingSubChannel)cobj).

                    exportChannel(offering));

            }

        }



        File dir = new File(workDir);

        dir.mkdirs();



        String exportXML =

            toXML(offeringList, offeringElements.toString(), false, false);



        String filename = "offeringExport" + "-" + offering.getId() + "-" +

            getDomainUser(upId).getUsername() + ".zip";



        String fullfilename = workDir + filename;

        File zipfile = new File(fullfilename);

        ZipOutputStream zos =

            new ZipOutputStream(new FileOutputStream(zipfile));

        ZipEntry ze = new ZipEntry("offeringExport.xml");

        writeToZip(ze, new StringBufferInputStream(exportXML), zos);



        // Add in all the submissions/feedbacks for this offering

        String submissionDir = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.portal.gradebook.submissionFileDir");

        String feedbackDir = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.portal.gradebook.feedbackFileDir");

        String baseDir = null;



        GradebookService gbService = GradebookServiceFactory.getService();

        FileService fileService    = FileServiceFactory.getService();

        Connection conn = null;

        List files = null;



        try {

        conn = getDBConnection();

        files = gbService.getAllGradebookSubmissions(

            offering, conn);

        files.addAll(gbService.getAllGradebookFeedbacks(

            offering, conn));

        } finally {

        releaseDBConnection(conn);

        }



        itr = files.iterator();

        while (itr.hasNext()) {

            IGradebookFileInfo fileInfo = (IGradebookFileInfo)itr.next();

            if (fileInfo instanceof GradebookSubmission) {

                baseDir = submissionDir;

            } else if (fileInfo instanceof GradebookFeedback) {

                baseDir = feedbackDir;

            } else {

                LogService.instance().log(LogService.ERROR,

                    "OfferingAdminChannel::generateExportFile() : " +

                        "invalid fileInfo class: " + fileInfo.getClass());

                continue;



            }

            dir = new File(baseDir, ""+fileInfo.getGradebookScoreID());

            File file = fileService.getFile(dir, fileInfo.getFilename());

            File path = new File(dir, fileInfo.getFilename());

            if (file == null) {

                LogService.instance().log(LogService.ERROR,

                    "OfferingAdminChannel::generateExportFile() : " +

                        "file not found: " + path.getPath());

                continue;

            }

            ze = new ZipEntry(path.getPath());

            writeToZip(ze, new FileInputStream(file), zos);

        }

        zos.close();

        return filename;

    }



    protected void writeToZip(ZipEntry ze, InputStream is,

        ZipOutputStream zos)

    throws DataFormatException, ZipException, FileNotFoundException,

    IOException {



        // This function write s the data in to Zipoutput Stream.

        byte b[] = new byte[512];

        zos.putNextEntry(ze);

        int len = 0;

        while ((len =is.read(b))!= -1) {

            zos.write(b,0,len);

        }

        zos.closeEntry();

        is.close();

    }



    protected String confirmInactivateCommand(String upId) throws Exception {

        String topicName = getRuntimeData(upId).getParameter("topicName");

        String offName = getRuntimeData(upId).getParameter("offName");

        String optIdString = getRuntimeData(upId).getParameter("optId");



        if ( topicName == null) {

            topicName ="";

        }

        if(offName == null) {

            offName ="";

        }



        if (optIdString == null || "".equals(optIdString)) {

            getXSLParameters(upId).put("optId", "");

            optIdString="0";

        }else {

            try {

                Long  optId=Long.valueOf(optIdString);

                 getXSLParameters(upId).put("optId", optIdString);



            } catch ( NumberFormatException nfe) {

                System.out.println("Offering id can not be a string." + nfe);

                getXSLParameters(upId).put("optId", "");



                optIdString = "0";

            }



        }

        getXSLParameters(upId).put("topicName",topicName);
        getXSLParameters(upId).put("offName", offName);
        getXSLParameters(upId).put("catCurrentCommand", searchCommand);


        // Check for confirmation.

        String param = getRuntimeData(upId).getParameter(confirmParam);

        if ("yes".equals(param)) {



            long id = Long.parseLong(getRuntimeData(upId).getParameter("ID"));

            try {

                Offering target = OfferingFactory.getOffering(id);

                target.setStatus(Offering.INACTIVE);

                OfferingFactory.persist(target);

            } catch (DomainException e) {

                String msg = "Failed to inactivate offering: " +

                    ExceptionUtils.getExceptionMessage(e);

                setErrorMsg(upId, msg);

                e.printStackTrace();

                return ">";

            }

    //        Memberships.removeEnrollment(id);



            // Notification service to alert those who have pending requests

            Offering offering = getOfferingFromParam(upId);



            NotificationService notifier = NotificationServiceFactory.getService();



            try {

                if (offering.getEnrollmentModel().toString().equals(EnrollmentModel.REQUESTAPPROVE.toString())) {

                    String msg = "Your pending request to enroll in " +

                                  offering.getName() + " has been deferred " +

                                  "because this offering has been inactivated!";

                    ArrayList pendingUsersToNotify = (ArrayList) Memberships.getMembers(offering, EnrollmentStatus.PENDING);

                    if (pendingUsersToNotify.size() > 0) {

                        User fromUser = getDomainUser(upId);

                        notifier.sendNotifications(pendingUsersToNotify, fromUser, msg);

                    }

                }

            } catch (ItemNotFoundException e) {

                    throw new PortalException(e);

            }

            // Notification service to alert those who are enrolled in the offering

            ArrayList enrolledUsersToNotify = (ArrayList) Memberships.getMembers(offering, EnrollmentStatus.ENROLLED);

            String msg = "Offering: " + offering.getName() + " has been inactivated!";

            if (enrolledUsersToNotify.size() > 0) {

                User fromUser = getDomainUser(upId);

                notifier.sendNotifications(enrolledUsersToNotify, fromUser, msg);

            }



            broadcastUserOfferingDirtyChannel(offering, "NavigationChannel", true);

            // need to also dirty this user's navigation manually because

            // they might not be enrolled in the offering (super user)

            broadcastUserDirtyChannel(getDomainUser(upId), "NavigationChannel");



        }



        // New code added for request/approve enrollment model

        return searchCommand(upId);

    }



    protected String activateCommand(String upId) throws Exception {

        long id = Long.parseLong(getRuntimeData(upId).getParameter("ID"));

        try {

            Offering target = OfferingFactory.getOffering(id);

            target.setStatus(Offering.ACTIVE);

            OfferingFactory.persist(target);

        } catch (DomainException e) {

            String msg = "Failed to activate offering: " +

                ExceptionUtils.getExceptionMessage(e);

            setErrorMsg(upId, msg);

            e.printStackTrace();

            return ">";

        }



        // New code added for request/approve enrollment model

        // Notification service to alert those who have pending requests

        Offering offering = getOfferingFromParam(upId);



        NotificationService notifier = NotificationServiceFactory.getService();



        try {

            if (offering.getEnrollmentModel().toString().equals(EnrollmentModel.REQUESTAPPROVE.toString())) {

                String msg = "Your pending request to enroll in " +

                              offering.getName() + " can now be processed " +

                              "because this offering has been reactivated!";

                ArrayList pendingUsersToNotify = (ArrayList) Memberships.getMembers(offering, EnrollmentStatus.PENDING);

                if (pendingUsersToNotify.size() > 0) {

                    User fromUser = getDomainUser(upId);

                    notifier.sendNotifications(pendingUsersToNotify, fromUser, msg);

                }

            }

        } catch (ItemNotFoundException e) {

                throw new PortalException(e);

        }

        // Notification service to alert those who are enrolled in the offering

        ArrayList enrolledUsersToNotify = (ArrayList) Memberships.getMembers(offering, EnrollmentStatus.ENROLLED);

        String msg = "Offering: " + offering.getName() + " has been activated!";

        if (enrolledUsersToNotify.size() > 0) {

            User fromUser = getDomainUser(upId);

            notifier.sendNotifications(enrolledUsersToNotify, fromUser, msg);

        }



        broadcastUserOfferingDirtyChannel(offering, "NavigationChannel", true);

        // need to also dirty this user's navigation manually because

        // they might not be enrolled in the offering (super user)

        broadcastUserDirtyChannel(getDomainUser(upId), "NavigationChannel");



        return searchCommand(upId);

    }



    protected String deleteCommand(String upId) throws Exception {

        Offering offering = getOfferingFromParam(upId);

        if (offering == null) return ">";



        getXSLParameters(upId).put("enrolledUsers", Integer.toString(Memberships.enrolledMembers(offering)));



        /*if (Memberships.hasMembership(offering)) {

            String msg = "This offering currently has memberships. " +

                "Please unenroll all users before deleting.";

            setErrorMsg(upId, msg);

            return ">";

        }*/

        String topicName = getRuntimeData(upId).getParameter("topicName");

        String offName = getRuntimeData(upId).getParameter("offName");

        String optIdString = getRuntimeData(upId).getParameter("optId");



        if ( topicName == null) {

            topicName ="";

        }

        if(offName == null) {

            offName ="";

        }



        if (optIdString == null || "".equals(optIdString)) {

            getXSLParameters(upId).put("optId", "");

            optIdString="0";

        }else {

            try {

                Long  optId=Long.valueOf(optIdString);

                 getXSLParameters(upId).put("optId", optIdString);



            } catch ( NumberFormatException nfe) {

                System.out.println("Offering id can not be a string." + nfe);

                getXSLParameters(upId).put("optId", "");



                optIdString = "0";

            }



        }



        getXSLParameters(upId).put("topicName",topicName);

        getXSLParameters(upId).put("offName", offName);





        if (ChannelDataManager.getChannelClass(upId).isCscrEnabled()) {

            getXSLParameters(upId).put("navigateRemoveMessage", "true");

        }



        return singularActionCommand(upId, false);

    }



    protected String deleteSubmitCommand(String upId) throws Exception {

        String topicName = getRuntimeData(upId).getParameter("topicName");

        String offName = getRuntimeData(upId).getParameter("offName");

        String optIdString = getRuntimeData(upId).getParameter("optId");



        if ( topicName == null) {

            topicName ="";

        }

        if(offName == null) {

            offName ="";

        }



        if (optIdString == null || "".equals(optIdString)) {

            getXSLParameters(upId).put("optId", "");

            optIdString="0";

        }else {

            try {

                Long  optId=Long.valueOf(optIdString);

                 getXSLParameters(upId).put("optId", optIdString);



            } catch ( NumberFormatException nfe) {

                System.out.println("Offering id can not be a string." + nfe);

                getXSLParameters(upId).put("optId", "");



                optIdString = "0";

            }



        }



        getXSLParameters(upId).put("topicName",topicName);

        getXSLParameters(upId).put("offName", offName);

        getXSLParameters(upId).put("catCurrentCommand", viewInactiveCommand);

        getXSLParameters(upId).put("current_command", viewInactiveCommand);



        // Check for confirmation.

        String param = getRuntimeData(upId).getParameter(confirmParam);



        if ("yes".equals(param)) {



            Offering offering = getOfferingFromParam(upId);

            if (offering == null) return ">";



            // We must be sure this isn't an SOR violation.

            IEntityRecordInfo rec = SystemOfRecordBroker.getRecordInfo(offering);

            if (rec.getSystemOfRecord().getEntityAccessLevel().compareTo(AccessType.DELETE) < 0) {

                String msg = "Access of type " + AccessType.DELETE.toString()

                                        + " is not allowed "

                                        + "on entities from the "

                                        + rec.getSystemOfRecord().getSourceName()

                                        + " system.";

                setErrorMsg(upId, msg.toString());

                return ">";

            }



            // remove offering specific objects

            Connection conn = null;



            try {

            conn = getDBConnection();



            if (!ClassFolderUtil.deleteClassFolders(offering, conn)) {

                throw new PortalException("Failed removing class folders.");

            }



            if (!GradebookServiceFactory.getService().deleteGradebookItems(offering, conn)) {

                throw new PortalException("Failed removing gradebook items.");



            }



            AnnouncementService as = AnnouncementServiceFactory.getService();

            List annoucements = as.getAnnouncements("" + offering.getId(), conn);

            if (annoucements != null && annoucements.size() > 0) {



              if (as.deleteAnnouncements("" + offering.getId(), conn) == 0) {

                  LogService.instance().log(LogService.ERROR, "No announcements removed");

              }

            }



            if (!NotepadServiceFactory.getService().deleteAllOfferingNotes(offering, conn)) {

                throw new PortalException("Failed removing notes.");

            }



            Memberships.removeEnrollment(offering.getId());

            } finally {

            releaseDBConnection(conn);

            }



            Iterator itr = RoleFactory.getOfferingRoles(offering).iterator();

            while (itr != null && itr.hasNext()) {

                Role role = (Role)itr.next();

                if (role.getOffering() != null) {

                    PermissionsService.instance().

                        removePermissions(role.getGroup());

                }

            }

            RoleFactory.removeRoles(offering);

            // delete the offering calendar

            CalendarServiceFactory.getService().deleteCalendar(offering);

            // Detect presence of Ensemble and delete adjuct offering data
            if (ENSEMBLE_IS_ENABLED) {

                // Delete additional meta data specific to Ensemble
                EnsembleService.deleteAdjunctOfferingData(offering.getId());
            }

            OfferingFactory.deleteOffering(offering);


            broadcastUserOfferingDirtyChannel(offering, "NavigationChannel", true);

            // need to also dirty this user's navigation manually because

            // they might not be enrolled in the offering (super user)

            broadcastUserDirtyChannel(getDomainUser(upId), "NavigationChannel");



        } // end if for confirmParam



        return viewInactiveCommand(upId);

    }



    protected String editSubmitCommand(String upId) throws Exception {

        long id = -1;

        String idStr = getRuntimeData(upId).getParameter("ID");

        long topicId =

            Long.parseLong(getRuntimeData(upId).getParameter("topicNameParam"));

        String name = getRuntimeData(upId).getParameter(offeringNameParam);

        String desc = getRuntimeData(upId).getParameter(offeringDescParam);

        String modelStr =

            getRuntimeData(upId).getParameter(userEnrollmentModelParam);

        long roleId =

            Long.parseLong(getRuntimeData(upId).getParameter(defaultRoleParam));

        String[] channels =

            getRuntimeData(upId).getParameterValues(channelParam);

        EnrollmentModel model = EnrollmentModel.getInstance(modelStr);



        // *******************************************************************

        // Added for Requirements OA 4.1-4.12

        //

        // OA 4.1-4.2eOffering ID

        // retrieve the optional string from the runtime data

        String optionalIdString =

                         getRuntimeData(upId).getParameter(offeringIdParam);



        // OA 4.3-4.4 Offering Term

        // retrieve the optional string from the runtime data

        String optionalTermString =

                         getRuntimeData(upId).getParameter(offeringTermParam);



        // OA 4.5-4.6 Offering Start Date and Offering End Date

        String optionalMonthStartString =

                   getRuntimeData(upId).getParameter(offeringMonthStartParam);

        int optionalMonthStartInt = 0;

        if (optionalMonthStartString != null) {

           optionalMonthStartInt = Integer.parseInt(optionalMonthStartString);

        }



        String optionalDayStartString =

                   getRuntimeData(upId).getParameter(offeringDayStartParam);

        int optionalDayStartInt = 0;

        if (optionalDayStartString != null) {

           optionalDayStartInt = Integer.parseInt(optionalDayStartString);

        }



        String optionalYearStartString =

                   getRuntimeData(upId).getParameter(offeringYearStartParam);





        int optionalYearStartInt = 0;

        if (optionalYearStartString != null) {

           optionalYearStartInt = Integer.parseInt(optionalYearStartString);

        }



        String optionalMonthEndString =

                   getRuntimeData(upId).getParameter(offeringMonthEndParam);

        int optionalMonthEndInt = 0;

        if (optionalMonthEndString != null) {

           optionalMonthEndInt = Integer.parseInt(optionalMonthEndString);

        }



        String optionalDayEndString =



                   getRuntimeData(upId).getParameter(offeringDayEndParam);

        int optionalDayEndInt = 0;

        if (optionalDayEndString != null) {

           optionalDayEndInt = Integer.parseInt(optionalDayEndString);

        }



        String optionalYearEndString =

                   getRuntimeData(upId).getParameter(offeringYearEndParam);

        int optionalYearEndInt = 0;

        if (optionalYearEndString != null) {

           optionalYearEndInt = Integer.parseInt(optionalYearEndString);

        }





        // OA 4.7-4.8 Offering Meeting Days of the Week

        int daysOfWeek = 0;

        String optionalMeetsMondayString =

                   getRuntimeData(upId).getParameter(offeringMtgMonParam);

        if (optionalMeetsMondayString != null) {

            daysOfWeek |= OfferingFactory.MONDAY;

        }



        String optionalMeetsTuesdayString =

                   getRuntimeData(upId).getParameter(offeringMtgTueParam);

        if (optionalMeetsTuesdayString != null) {

            daysOfWeek |= OfferingFactory.TUESDAY;

        }



        String optionalMeetsWednesdayString =

                   getRuntimeData(upId).getParameter(offeringMtgWedParam);

        if (optionalMeetsWednesdayString != null) {

            daysOfWeek |= OfferingFactory.WEDNESDAY;

        }



        String optionalMeetsThursdayString =

                   getRuntimeData(upId).getParameter(offeringMtgThuParam);

        if (optionalMeetsThursdayString != null) {

            daysOfWeek |= OfferingFactory.THURSDAY;

        }



        String optionalMeetsFridayString =

                   getRuntimeData(upId).getParameter(offeringMtgFriParam);

        if (optionalMeetsFridayString != null) {

            daysOfWeek |= OfferingFactory.FRIDAY;

        }



        String optionalMeetsSaturdayString =

                   getRuntimeData(upId).getParameter(offeringMtgSatParam);

        if (optionalMeetsSaturdayString != null) {

            daysOfWeek |= OfferingFactory.SATURDAY;

        }



        String optionalMeetsSundayString =

                   getRuntimeData(upId).getParameter(offeringMtgSunParam);

        if (optionalMeetsSundayString != null) {

            daysOfWeek |= OfferingFactory.SUNDAY;

        }



        // OA 4.9-4.10 Offering Start Time and Offering End Time

        String optionalHourStartString =

                   getRuntimeData(upId).getParameter(offeringHourStartParam);

        int optionalHourStartInt = 0;

        if (optionalHourStartString != null) {

            optionalHourStartInt = Integer.parseInt(optionalHourStartString);

        }



        String optionalMinuteStartString =

                   getRuntimeData(upId).getParameter(offeringMinuteStartParam);

        int optionalMinuteStartInt = 0;

        if (optionalMinuteStartString != null) {

            optionalMinuteStartInt =

                                  Integer.parseInt(optionalMinuteStartString);

        }



        String optionalAmPmStartString =

                   getRuntimeData(upId).getParameter(offeringAmPmStartParam);

        int optionalAmPmStartInt = 0;

        if (optionalAmPmStartString != null) {

            optionalAmPmStartInt =

                             Integer.parseInt(optionalAmPmStartString);

        }



        String optionalHourEndString =

                   getRuntimeData(upId).getParameter(offeringHourEndParam);

        int optionalHourEndInt = 0;

        if (optionalHourEndString != null) {

            optionalHourEndInt = Integer.parseInt(optionalHourEndString);

        }



        String optionalMinuteEndString =

                   getRuntimeData(upId).getParameter(offeringMinuteEndParam);

        int optionalMinuteEndInt = 0;

        if (optionalMinuteEndString != null) {

            optionalMinuteEndInt =

                                  Integer.parseInt(optionalMinuteEndString);

        }



        String optionalAmPmEndString =

                   getRuntimeData(upId).getParameter(offeringAmPmEndParam);

        int optionalAmPmEndInt = 0;

        if (optionalAmPmEndString != null) {

            optionalAmPmEndInt =

                            Integer.parseInt(optionalAmPmEndString);

        }



        // OA 4.11-4.12 Offering Location

        String optionalLocationString =

                   getRuntimeData(upId).getParameter(offeringLocationParam);



        // *******************************************************************



        Topic topic = null;

        try {

            topic = TopicFactory.getTopic(topicId);

        } catch (ItemNotFoundException e) {

            String msg = "Failed to update offering. Topic no longer exists.";

            setErrorMsg(upId, msg);

            return ">";

        }



        try {

            id = Long.parseLong(idStr);

        } catch (NumberFormatException e) {

            return ">";

        }



        Offering offering = OfferingFactory.getOffering(id);



        // new code for Request/Approve Enrollment Model

        // we *do not* allow the enrollment model to be changed from

        // Request/Approve to something else (i.e. Open, Faciltator,

        // Invite-Only, etc.) if there are enrollment requests still

        // pending for the offering.  These pending requests will have to

        // be dealt with (rejected/approved) from the roster channel

        // before the Enrollment Model type can be changed from

        // Request/Approve.

        EnrollmentModel previousEnrollmentModel = offering.getEnrollmentModel();

        String prevEnrollmentModelString = previousEnrollmentModel.toString();

        String requestApproveString = EnrollmentModel.REQUESTAPPROVE.toString();

        if ((prevEnrollmentModelString.equals(requestApproveString)) &&

            (! modelStr.equals(requestApproveString)) &&

            (Memberships.hasMembership(offering,EnrollmentStatus.PENDING))) {

                String msg = "Failed to update offering.  There are " +

                             "enrollment requests pending.";

                setErrorMsg(upId, msg);

                return ">";

        }



        try {

            offering.setName(name);

            offering.setDescription(desc);

            offering.removeTopic(offering.getTopic());

            offering.addTopic(topic);

            offering.setEnrollmentModel(model);



            offering.setDefaultRole(roleId);



            // *********************************************************

            // Added for Requirements OA 4.1-4.12

            //

            offering.setOptionalId(optionalIdString);

            offering.setOptionalTerm(optionalTermString);

            offering.setOptionalMonthStart(optionalMonthStartInt);

            offering.setOptionalDayStart(optionalDayStartInt);

            offering.setOptionalYearStart(optionalYearStartInt);

            offering.setOptionalMonthEnd(optionalMonthEndInt);

            offering.setOptionalDayEnd(optionalDayEndInt);

            offering.setOptionalYearEnd(optionalYearEndInt);

            offering.setDaysOfWeek(daysOfWeek);

            offering.setOptionalHourStart(optionalHourStartInt);

            offering.setOptionalMinuteStart(optionalMinuteStartInt);

            offering.setOptionalAmPmStart(optionalAmPmStartInt);

            offering.setOptionalHourEnd(optionalHourEndInt);

            offering.setOptionalMinuteEnd(optionalMinuteEndInt);

            offering.setOptionalAmPmEnd(optionalAmPmEndInt);

            offering.setOptionalLocation(optionalLocationString);

            OfferingFactory.persist(offering);

            // *********************************************************



            // update the offering calendar

            CalendarServiceFactory.getService().updateCalendar(offering);


            /**
             * Detect presence of Ensemble and stores published and
             * buy now parameters.
             */
            boolean hasBuyNowEnabled = false;
            boolean published = false;

            if (ENSEMBLE_IS_ENABLED) {

                String buyNowParam = getRuntimeData(upId).
                        getParameter(BUY_NOW_PARAM);
                String publishedParam = getRuntimeData(upId).
                        getParameter(PUBLISHED_PARAM);

                if (buyNowParam != null) {
                    hasBuyNowEnabled = true;
                }

                if (publishedParam != null) {
                    published = true;
                }

                AdjunctOfferingData offeringData =
                        new AdjunctOfferingData(id, published, hasBuyNowEnabled);

                // Store additional meta data specific to Ensemble
                EnsembleService.updateAdjunctOfferingData(id, offeringData);
            }


        } catch (IllegalArgumentException e) {

            String msg = "Failed to edit offering: " +

                ExceptionUtils.getExceptionMessage(e);

            setErrorMsg(upId, msg);

            e.printStackTrace();

            return ">";

        } catch (OperationFailedException e) {

            String msg = "Failed to edit offering: " +

                ExceptionUtils.getExceptionMessage(e);

            setErrorMsg(upId, msg);

            e.printStackTrace();

            return ">";

        } catch (ItemNotFoundException e) {

            String msg = "Failed to edit offering: " +

                ExceptionUtils.getExceptionMessage(e);

            setErrorMsg(upId, msg);

            e.printStackTrace();

            return ">";

        } catch (DomainException e) {

            String msg = "Failed to edit offering: " +

                ExceptionUtils.getExceptionMessage(e);

            setErrorMsg(upId, msg);

            e.printStackTrace();

            return ">";

        }



        for (int i=0; channels != null && i<channels.length; i++) {

            ChannelClass cc = ChannelClassFactory.getChannelClass(channels[i]);

            offering.addChannel(cc);

        }

        broadcastUserDirtyChannel(getDomainUser(upId), "NavigationChannel");

        setSheetName(upId, searchCommand);

        getXSLParameters(upId).put("catCurrentCommand", searchCommand);



        return searchCommand(upId);

    }



    protected String searchCommand(String upId) throws Exception {

                // Gather modes to apply.

        setSheetName(upId, viewAllCommand);

        List sortModes = new ArrayList();

        List filterModes = new ArrayList();

        IPageMode pg = null;



        sortModes.add(new FDbSortMode("UPPER(T.NAME) ASC"));

        sortModes.add(new FDbSortMode("UPPER(O.NAME) ASC"));



        // Calculate which page to view.

        int pgSize = evaluatePageSize(upId);

        int pgNum = evaluateCurrentPage(upId);

        pg = new FDbPageMode(pgSize, pgNum);



        String topicName = getRuntimeData(upId).getParameter("topicName");

        String offName = getRuntimeData(upId).getParameter("offName");

        String optIdString = getRuntimeData(upId).getParameter("optId");

        filterModes.add(new FDbFilterMode("status = ?", new Object[] {Integer.toString(Offering.ACTIVE)}));



        if ( topicName != null && !"".equals(topicName)) {

            String sql = "UPPER(t.name) like UPPER(?)";

            filterModes.add(new FDbFilterMode(sql, new Object[] { topicName+"%" }));

        } else {

            topicName="";

        }



        if(offName != null && !"".equals(offName)) {

          String sql = "UPPER(o.name) like UPPER(?)";

          filterModes.add(new FDbFilterMode(sql, new Object[] { offName+"%" }));

        } else {

            offName="";

        }



        if (optIdString == null || "".equals(optIdString)) {

            getXSLParameters(upId).put("optId", "");

        }else {

            try {

                Long  optId=Long.valueOf(optIdString);

                getXSLParameters(upId).put("optId", optIdString);

                String sql = "o.opt_offeringid like ?";

                filterModes.add(new FDbFilterMode(sql, new Object[] {"%"+optIdString+"%"}));

            } catch ( NumberFormatException nfe) {

                System.out.println("Offering id can not be a string." + nfe);

                getXSLParameters(upId).put("optId", "");

            }

        }

        getXSLParameters(upId).put("topicName",topicName);
        getXSLParameters(upId).put("offName", offName);
        getXSLParameters(upId).put("catCurrentCommand", searchCommand);
        getXSLParameters(upId).put("catPageSize", "" + pgSize);


        // Is this user an Administrator
        boolean adminUser = PermissionsUtil.isPortalAdministrator(
            getStaticData(upId).getAuthorizationPrincipal());

        /* The superuser gets to see all of the active offerings.
           An administrator (non-super-user) gets to see all of the

           active offerings. A regular user gets to see all of

           the active offerings that they are enrolled in.

        */



        Catalog cat;

        if (getDomainUser(upId).isSuperUser() || adminUser) {

            cat = getSuperUserCatalog().subCatalog(

                        (ISortMode[]) sortModes.toArray(new ISortMode[0]),

                        (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),

                        pg);



        } else {

            String sql = "UPPER(m.user_name) = UPPER(?)";

            filterModes.add(new FDbFilterMode(sql, new Object[] { getDomainUser(upId).getUsername()}));

            cat = getUserCatalog().subCatalog(

                        (ISortMode[]) sortModes.toArray(new ISortMode[0]),

                        (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),

                        pg);

        }



        List elements = cat.elements();



        return toXML(elements, pg, upId);



    }



    protected String addSubmitCommand(String upId) throws Exception {

        long topicId =

            Long.parseLong(getRuntimeData(upId).getParameter("topicNameParam"));

        String name = getRuntimeData(upId).getParameter(offeringNameParam);

        String desc = getRuntimeData(upId).getParameter(offeringDescParam);

        String modelStr =

            getRuntimeData(upId).getParameter(userEnrollmentModelParam);

        long roleId =

            Long.parseLong(getRuntimeData(upId).getParameter(defaultRoleParam));

        String[] channels =

            getRuntimeData(upId).getParameterValues(channelParam);



        // Getting the creator's default role id specified by the form data

        long creatorRoleId =

            Long.parseLong(getRuntimeData(upId).getParameter(creatorRoleParam));


        User creator = getDomainUser(upId);


        // Determining if the creator needs to be enrolled.

        boolean enrollCreator = "true".equals(

            getRuntimeData(upId).getParameter(enrollCreatorParam));


        EnrollmentModel model = EnrollmentModel.getInstance(modelStr);


        // *******************************************************************

        // Added for Requirements OA 4.1-4.12

        //

        // OA 4.1-4.2 Offering ID

        // retrieve the optional string from the runtime data

        String optionalIdString =

                         getRuntimeData(upId).getParameter(offeringIdParam);



        // OA 4.3-4.4 Offering Term

        // retrieve the optional string from the runtime data

        String optionalTermString =

                         getRuntimeData(upId).getParameter(offeringTermParam);



        // OA 4.5-4.6 Offering Start Date and Offering End Date

        String optionalMonthStartString =

                   getRuntimeData(upId).getParameter(offeringMonthStartParam);

        int optionalMonthStartInt = 0;

        if (optionalMonthStartString != null) {

           optionalMonthStartInt = Integer.parseInt(optionalMonthStartString);

        }



        String optionalDayStartString =

                   getRuntimeData(upId).getParameter(offeringDayStartParam);

        int optionalDayStartInt = 0;

        if (optionalDayStartString != null) {

           optionalDayStartInt = Integer.parseInt(optionalDayStartString);

        }



        String optionalYearStartString =

                   getRuntimeData(upId).getParameter(offeringYearStartParam);

        int optionalYearStartInt = 0;

        if (optionalYearStartString != null) {

           optionalYearStartInt = Integer.parseInt(optionalYearStartString);

        }



        String optionalMonthEndString =

                   getRuntimeData(upId).getParameter(offeringMonthEndParam);

        int optionalMonthEndInt = 0;

        if (optionalMonthEndString != null) {

           optionalMonthEndInt = Integer.parseInt(optionalMonthEndString);

        }



        String optionalDayEndString =

                   getRuntimeData(upId).getParameter(offeringDayEndParam);

        int optionalDayEndInt = 0;

        if (optionalDayEndString != null) {

           optionalDayEndInt = Integer.parseInt(optionalDayEndString);

        }



        String optionalYearEndString =

                   getRuntimeData(upId).getParameter(offeringYearEndParam);

        int optionalYearEndInt = 0;

        if (optionalYearEndString != null) {

           optionalYearEndInt = Integer.parseInt(optionalYearEndString);

        }





        // OA 4.7-4.8 Offering Meeting Days of the Week

        int daysOfWeek = 0;

        String optionalMeetsMondayString =

                   getRuntimeData(upId).getParameter(offeringMtgMonParam);

        if (optionalMeetsMondayString != null) {

            daysOfWeek |= OfferingFactory.MONDAY;



        }



        String optionalMeetsTuesdayString =

                   getRuntimeData(upId).getParameter(offeringMtgTueParam);

        if (optionalMeetsTuesdayString != null) {

            daysOfWeek |= OfferingFactory.TUESDAY;

        }



        String optionalMeetsWednesdayString =

                   getRuntimeData(upId).getParameter(offeringMtgWedParam);

        if (optionalMeetsWednesdayString != null) {

            daysOfWeek |= OfferingFactory.WEDNESDAY;

        }



        String optionalMeetsThursdayString =

                   getRuntimeData(upId).getParameter(offeringMtgThuParam);

        if (optionalMeetsThursdayString != null) {

            daysOfWeek |= OfferingFactory.THURSDAY;

        }



        String optionalMeetsFridayString =

                   getRuntimeData(upId).getParameter(offeringMtgFriParam);

        if (optionalMeetsFridayString != null) {

            daysOfWeek |= OfferingFactory.FRIDAY;

        }



        String optionalMeetsSaturdayString =

                   getRuntimeData(upId).getParameter(offeringMtgSatParam);

        if (optionalMeetsSaturdayString != null) {

            daysOfWeek |= OfferingFactory.SATURDAY;

        }



        String optionalMeetsSundayString =

                   getRuntimeData(upId).getParameter(offeringMtgSunParam);

        if (optionalMeetsSundayString != null) {

            daysOfWeek |= OfferingFactory.SUNDAY;

        }



        // OA 4.9-4.10 Offering Start Time and Offering End Time

        String optionalHourStartString =

                   getRuntimeData(upId).getParameter(offeringHourStartParam);

        int optionalHourStartInt = 0;

        if (optionalHourStartString != null) {

            optionalHourStartInt = Integer.parseInt(optionalHourStartString);

        }



        String optionalMinuteStartString =

                   getRuntimeData(upId).getParameter(offeringMinuteStartParam);

        int optionalMinuteStartInt = 0;

        if (optionalMinuteStartString != null) {

            optionalMinuteStartInt =

                                  Integer.parseInt(optionalMinuteStartString);

        }



        String optionalAmPmStartString =

                   getRuntimeData(upId).getParameter(offeringAmPmStartParam);

        int optionalAmPmStartInt = 0;

        if (optionalAmPmStartString != null) {

            optionalAmPmStartInt =

                             Integer.parseInt(optionalAmPmStartString);

        }



        String optionalHourEndString =

                   getRuntimeData(upId).getParameter(offeringHourEndParam);

        int optionalHourEndInt = 0;

        if (optionalHourEndString != null) {

            optionalHourEndInt = Integer.parseInt(optionalHourEndString);

        }



        String optionalMinuteEndString =

                   getRuntimeData(upId).getParameter(offeringMinuteEndParam);

        int optionalMinuteEndInt = 0;

        if (optionalMinuteEndString != null) {

            optionalMinuteEndInt =

                                  Integer.parseInt(optionalMinuteEndString);

        }



        String optionalAmPmEndString =

                   getRuntimeData(upId).getParameter(offeringAmPmEndParam);

        int optionalAmPmEndInt = 0;

        if (optionalAmPmEndString != null) {

            optionalAmPmEndInt =

                            Integer.parseInt(optionalAmPmEndString);

        }



        // OA 4.11-4.12 Offering Location

        String optionalLocationString =

                   getRuntimeData(upId).getParameter(offeringLocationParam);



        // *******************************************************************



        Topic topic = null;

        try {

            topic = TopicFactory.getTopic(topicId);



            // check if the name already exists in this topic

            Offering offering;

            Iterator itr = OfferingFactory.getOfferings(topic).iterator();

            while (itr.hasNext()) {

                offering = (Offering)itr.next();

                if (name.equals(offering.getName())) {

                    StringBuffer sb = new StringBuffer();

                    sb.append("Offering named \"").append(name).append("\" ");

                    sb.append("already exists under topic \"");

                    sb.append(topic.getName()).append("\"");

                    setErrorMsg(upId, sb.toString());

                    return ">";

                }

            }



            Role defaultRole = RoleFactory.getRole(roleId);

            // ************************************************************

            // Code change for Requirements OA 4.1-4.12

            //

            // Changed code from:

            // Offering newOffering =

            //    OfferingFactory.createOffering(name,

            //                                   desc,

            //                                   topic,

            //                                   model,

            //                                   defaultRole,

            //                                   ChannelClassFactory.

            //                      getChannelClasses(ChannelMode.OFFERING));

            // to:



            Offering newOffering =

                OfferingFactory.createOffering(name,

                           desc,

                           topic,

                           model,



                           defaultRole,

                           ChannelClassFactory.getChannelClasses(ChannelMode.OFFERING),

                           creator,

                           optionalIdString,

                           optionalTermString,

                           optionalMonthStartInt,

                           optionalDayStartInt,

                           optionalYearStartInt,

                           optionalMonthEndInt,

                           optionalDayEndInt,

                           optionalYearEndInt,

                           daysOfWeek,

                           optionalHourStartInt,

                           optionalMinuteStartInt,

                           optionalAmPmStartInt,

                           optionalHourEndInt,

                           optionalMinuteEndInt,

                           optionalAmPmEndInt,

                           optionalLocationString);



            // create the offering calendar

            CalendarServiceFactory.getService().createCalendar(newOffering);



            // ************************************************************

            // Determines if the user who is adding the offering

            // is enrolled in the offering.

            if (enrollCreator) {

                Role creatorRole = RoleFactory.getRole(creatorRoleId);

                Memberships.add(getDomainUser(upId), newOffering, creatorRole,

                    EnrollmentStatus.ENROLLED);

                // Share the user with the offering's calendar

                CalendarServiceFactory.getService().addUser(newOffering, getDomainUser(upId));

            }

            /**
             * Detect presence of Ensemble and stores published and
             * buy now parameters.
             */
            boolean hasBuyNowEnabled = false;
            boolean published = false;

            if (ENSEMBLE_IS_ENABLED) {

                String buyNowParam = getRuntimeData(upId).
                        getParameter(BUY_NOW_PARAM);
                String publishedParam = getRuntimeData(upId).
                        getParameter(PUBLISHED_PARAM);

                if (buyNowParam != null) {
                    hasBuyNowEnabled = true;
                }

                if (publishedParam != null) {
                    published = true;
                }

                long offeringId = newOffering.getId();

                AdjunctOfferingData offeringData =
                        new AdjunctOfferingData(
                                offeringId, published, hasBuyNowEnabled);

                // Store additional meta data specific to Ensemble
                EnsembleService.createAdjunctOfferingData(
                        offeringId, offeringData);
            }

        } catch (IllegalArgumentException e) {

            String msg = "Failed to add offering: " +

                ExceptionUtils.getExceptionMessage(e);

            setErrorMsg(upId, msg);

            return ">";

        } catch (OperationFailedException e) {

        e.printStackTrace();

            String msg = "Failed to add offering: " +

            ExceptionUtils.getExceptionMessage(e);

            setErrorMsg(upId, msg);

            return ">";

        } catch (ItemNotFoundException e) {

            String msg = "Failed to add offering: " +

                ExceptionUtils.getExceptionMessage(e);

            setErrorMsg(upId, msg);

            return ">";

        }



        broadcastUserDirtyChannel(getDomainUser(upId), "NavigationChannel");



        if (ChannelDataManager.getChannelClass(upId).isCscrEnabled()) {

            getXSLParameters(upId).put("navigateAddMessage", "true");

        }



        return searchCommand(upId);

    }



    private int evaluatePageSize(String upId) {

        int pgSize = 10;    // Default.

        String strPageSize = getRuntimeData(upId).getParameter("catPageSize");

        if (strPageSize != null) {

            if (strPageSize.equals("All")) {

                pgSize = 0;

            } else if (strPageSize.trim().equals("")) {

                // Fall through...go w/ default.

            } else {

                pgSize = Integer.parseInt(strPageSize);

            }

        }

        return pgSize;

    }



    private int evaluateCurrentPage(String upId) {

        int pgNum = 1;      // Default.

        String strPageNum = getRuntimeData(upId).getParameter("catSelectPage");

        if (strPageNum != null) {

            pgNum = Integer.parseInt(strPageNum);

        }

        return pgNum;

    }



    private static Catalog getSuperUserCatalog() {

        if (superUserCatalog == null) {

            superUserCatalog = new FLazyCatalog(createSuperUserDataSource());

        }

        return superUserCatalog;

    }



    private static Catalog getUserCatalog() {

        if (userCatalog == null) {

            userCatalog = new FLazyCatalog(createUserDataSource());

        }

        return userCatalog;

    }



    private static IDataSource createUserDataSource() {

        StringBuffer qBase = new StringBuffer();

        String query = "select o.*, r.label, t.topic_id, t.name as topicName, t.type, t.description from membership m, offering o, role r, topic t, topic_offering ot where r.role_id = o.default_role_id and t.topic_id = ot.topic_id and ot.offering_id = o.offering_id and  m.offering_id=o.offering_id";



        qBase.append(query);

        return new FDbDataSource(qBase.toString(), createBaseEntryConvertor());

    }



    private static IDataSource createSuperUserDataSource() {

        StringBuffer qBase = new StringBuffer();

        String query = "SELECT o.*, r.label, t.topic_id, t.name as topicName, t.type, t.description  from offering o, role r, topic t, topic_offering ot where r.role_id = o.default_role_id and t.topic_id = ot.topic_id and ot.offering_id = o.offering_id";



        qBase.append(query);

        return new FDbDataSource(qBase.toString(), createBaseEntryConvertor());

    }



    private static IDbEntryConvertor createBaseEntryConvertor() {

        return new IDbEntryConvertor(){

            public Object convertRow(ResultSet rs)

                    throws SQLException, CatalogException {

                StringBuffer xmlSB = new StringBuffer();

                xmlSB.append("<offering id=\"").append(rs.getLong("offering_id")).append("\">");

                xmlSB.append("<name>")
                    .append("<![CDATA[")
                    .append(rs.getString("NAME"))
                    .append("]]>")
                    .append("</name>");

                xmlSB.append("<description>")
                    .append("<![CDATA[")
                    .append(rs.getString("description"))
                    .append("]]>")
                    .append("</description>");

                xmlSB.append("<enrollmentModel>")
                    .append("<![CDATA[")
                    .append(rs.getString("enrollment_model"))
                    .append("]]>")
                    .append("</enrollmentModel>");

                try {
                    Offering offering = OfferingFactory.getOffering(rs.getLong("offering_id"));

                    List channels = offering.getChannels();

                    Iterator itr = channels.iterator();

                    while (itr.hasNext()) {

                        ChannelClass channel = (ChannelClass)itr.next();

                        xmlSB.append("<channel id=\"")
                            .append(channel.getHandle())
                            .append("\">")
                            .append("<![CDATA[").append(channel.getLabel()).append("]]>")
                            .append("</channel>");
                    }
                } catch (Exception e) {
                    System.out.println("Opeartion getOffering() failed");
                }

                xmlSB.append("    <role id=\"")
                    .append(rs.getLong("default_role_id"))
                    .append("\">")
                    .append("<![CDATA[")
                    .append(rs.getString("LABEL"))
                    .append("]]>")
                    .append("</role>");

                xmlSB.append("<topic id=\"")
                    .append(rs.getLong("topic_id"))
                    .append("\">");

                xmlSB.append("<topicType>")
                    .append("<![CDATA[")
                    .append(rs.getString("type"))
                    .append("]]>")
                    .append("</topicType>");

                xmlSB.append("<name>")
                    .append("<![CDATA[")
                    .append(rs.getString("topicName"))
                    .append("]]>")
                    .append("</name>");

                xmlSB.append("<description>")
                    .append("<![CDATA[")
                    .append(rs.getString("description"))
                    .append("]]>")
                    .append("</description>");

                xmlSB.append("</topic>");

                xmlSB.append("<optionalOfferingId>");

                String optionalOfferingId =  rs.getString("opt_offeringid");

                if (optionalOfferingId != null || !"".equals(optionalOfferingId)) {
                    xmlSB.append("<![CDATA[")
                    .append(optionalOfferingId)
                    .append("]]>");
                }

                xmlSB.append("</optionalOfferingId>\n");

                // Optional Offering Term

                xmlSB.append("     <optionalOfferingTerm>");

                String optionalOfferingTerm =  rs.getString("opt_offeringterm");

                if (optionalOfferingTerm != null) {
                    xmlSB.append("<![CDATA[")
                    .append(optionalOfferingTerm)
                    .append("]]>");
                }

                xmlSB.append("</optionalOfferingTerm>\n");

                // Optional Offering Start Date

                xmlSB.append("     <optionalOfferingStartDate>\n");
                xmlSB.append("        <MonthStart>");
                xmlSB.append(rs.getInt("opt_offeringmonthstart"));
                xmlSB.append("</MonthStart>\n");
                xmlSB.append("        <DayStart>");
                xmlSB.append(rs.getInt("opt_offeringdaystart"));
                xmlSB.append("</DayStart>\n");
                xmlSB.append("        <YearStart>");
                xmlSB.append(rs.getInt("opt_offeringyearstart"));
                xmlSB.append("</YearStart>\n");
                xmlSB.append("     </optionalOfferingStartDate>\n");

                // Optional Offering End Date

                xmlSB.append("     <optionalOfferingEndDate>\n");
                xmlSB.append("        <MonthEnd>");
                xmlSB.append(rs.getInt("opt_offeringmonthend"));
                xmlSB.append("</MonthEnd>\n");
                xmlSB.append("        <DayEnd>");
                xmlSB.append(rs.getInt("opt_offeringdayend"));
                xmlSB.append("</DayEnd>\n");
                xmlSB.append("        <YearEnd>");
                xmlSB.append(rs.getInt("opt_offeringyearend"));
                xmlSB.append("</YearEnd>\n");

                xmlSB.append("     </optionalOfferingEndDate>\n");

                int daysOfWeek = rs.getInt("opt_daysofweek");

                // Optional Offering Meeting Days

                xmlSB.append("     <optionalOfferingMeetingDays>\n");
                xmlSB.append("          <MeetsMonday>");
                xmlSB.append((daysOfWeek & OfferingFactory.MONDAY) !=0 ? 1 : 0);
                xmlSB.append("</MeetsMonday>\n");
                xmlSB.append("          <MeetsTuesday>");
                xmlSB.append((daysOfWeek & OfferingFactory.TUESDAY) !=0 ? 1 : 0);
                xmlSB.append("</MeetsTuesday>\n");
                xmlSB.append("          <MeetsWednesday>");
                xmlSB.append((daysOfWeek & OfferingFactory.WEDNESDAY) !=0 ? 1 : 0);
                xmlSB.append("</MeetsWednesday>\n");
                xmlSB.append("          <MeetsThursday>");
                xmlSB.append((daysOfWeek & OfferingFactory.THURSDAY) !=0 ? 1 : 0);
                xmlSB.append("</MeetsThursday>\n");
                xmlSB.append("          <MeetsFriday>");
                xmlSB.append((daysOfWeek & OfferingFactory.FRIDAY) !=0 ? 1 : 0);
                xmlSB.append("</MeetsFriday>\n");
                xmlSB.append("          <MeetsSaturday>");

                xmlSB.append((daysOfWeek & OfferingFactory.SATURDAY) !=0 ? 1 : 0);

                xmlSB.append("</MeetsSaturday>\n");

                xmlSB.append("          <MeetsSunday>");

                xmlSB.append((daysOfWeek & OfferingFactory.SUNDAY) !=0 ? 1 : 0);

                xmlSB.append("</MeetsSunday>\n");

                xmlSB.append("     </optionalOfferingMeetingDays>\n");



                // Optional Offering Start Time

                xmlSB.append("     <optionalOfferingStartTime>\n");

                xmlSB.append("        <HourStart>");

                xmlSB.append(rs.getInt("opt_offeringhourstart"));

                xmlSB.append("</HourStart>\n");

                xmlSB.append("        <MinuteStart>");

                xmlSB.append(rs.getInt("opt_offeringminutestart"));

                xmlSB.append("</MinuteStart>\n");

                xmlSB.append("        <AmPmStart>");

                xmlSB.append(rs.getInt("opt_offeringampmstart"));

                xmlSB.append("</AmPmStart>\n");

                xmlSB.append("     </optionalOfferingStartTime>\n");





                // Optional Offering End Time

                xmlSB.append("     <optionalOfferingEndTime>\n");

                xmlSB.append("        <HourEnd>");

                xmlSB.append(rs.getInt("opt_offeringhourend"));

                xmlSB.append("</HourEnd>\n");

                xmlSB.append("        <MinuteEnd>");

                xmlSB.append(rs.getInt("opt_offeringminuteend"));

                xmlSB.append("</MinuteEnd>\n");

                xmlSB.append("        <AmPmEnd>");

                xmlSB.append(rs.getInt("opt_offeringampmend"));

                xmlSB.append("</AmPmEnd>\n");

                xmlSB.append("     </optionalOfferingEndTime>\n");



                // Optional Offering Location

                xmlSB.append("     <optionalOfferingLocation>");

                String optionalOfferingLocation =  rs.getString("opt_offeringlocation");

                if (optionalOfferingLocation != null || !"".equals(optionalOfferingLocation)) {
                    xmlSB.append("<![CDATA[")
                        .append(optionalOfferingLocation)
                        .append("]]>");

                }

                xmlSB.append("     </optionalOfferingLocation>\n");

            /***********************************************************/

                xmlSB.append("</offering>");

                return xmlSB.toString();

            }

        };

    }



    protected String toXML(List elements, IPageMode pg, String upId) throws Exception{



        // Calculate which page to view.

        int pgSize = evaluatePageSize(upId);

        int pgNum = evaluateCurrentPage(upId);



        // Share info w/ the xsl.

        Map xslParams = getXSLParameters(upId);

        xslParams.put("catCurrentPage", new Integer(pgNum));

        xslParams.put("catLastPage", new Integer(pg.getPageCount()));

        xslParams.put("catPageSize",  evaluatePageSize(upId) + "");

        //List offerings = OfferingFactory.getOfferings(topic, Offering.ACTIVE);



        StringBuffer xmlSB = new StringBuffer();

        if (pgSize == 0) {

            xmlSB.append("page=\"All\" ");

        } else {

            xmlSB.append("page=\"");

            xmlSB.append(pgNum);

            xmlSB.append("\" ");

        }

        xmlSB.append("maxPage=\"" + pg.getPageCount() + "\" ");

        xmlSB.append("enrolledCount=\"");

        xmlSB.append(elements.size());

        xmlSB.append("\" ");

        xmlSB.append(">");



        for (int x=0; x< elements.size(); x++){

            xmlSB.append((String)elements.get(x));

        }

        return xmlSB.toString();



    }



        /**
      * copySubmitCommand
      * Clones an offering and its associated roster.
      * @author M. Marquiz
      * @version 1.0
      * @param upId - the portal identifier (String)
      * @return null on success or an Exception on failure
      * @throws ItemNotFoundException, IllegalArgumentException, and
      *         OperationFailedException.
      */

    protected String copySubmitCommand(String upId) throws Exception {



        Topic topic = null;



        // Retrieve values from the copy.xsl form

        // topic ID

        long topicId =

            Long.parseLong(getRuntimeData(upId).getParameter(topicNameParam));



        try {

            topic = TopicFactory.getTopic(topicId);

        } catch (ItemNotFoundException e) {

            String msg = "Failed to create offering. Topic no longer exists.";

            setErrorMsg(upId, msg);

            return ">";

        }



        // Offering Name (for the offering being copied)

        String name = getRuntimeData(upId).getParameter(offeringNameParam);



        // check if the name already exists in this topic

        Offering offering;

        Iterator itr = OfferingFactory.getOfferings(topic).iterator();

        while (itr.hasNext()) {

            offering = (Offering)itr.next();



            if (name.equals(offering.getName())) {

                StringBuffer sb = new StringBuffer();

                sb.append("Offering named \"").append(name).append("\" ");

                sb.append("already exists under topic \"");

                sb.append(topic.getName()).append("\"");

                setErrorMsg(upId, sb.toString());

                return ">";

            }

        }



        // Offering Description

        String desc = getRuntimeData(upId).getParameter(offeringDescParam);



        Offering originalOffering = null;



        // Enrollment Model

        String modelStr =

            getRuntimeData(upId).getParameter(userEnrollmentModelParam);

        EnrollmentModel model = EnrollmentModel.getInstance(modelStr);



        // Default Offering Role

        long roleId =

            Long.parseLong(getRuntimeData(upId).getParameter(defaultRoleParam));



        // Default offering channels

        String[] channels =

            getRuntimeData(upId).getParameterValues(channelParam);



        // original offering ID

        long originalOfferingId =

            Long.parseLong(getRuntimeData(upId).getParameter("ID"));



        // Optional Offering ID

        // retrieve the optional string from the runtime data

        String optionalIdString =

                         getRuntimeData(upId).getParameter(offeringIdParam);



        // Optional Offering Term

        // retrieve the optional string from the runtime data

        String optionalTermString =

                         getRuntimeData(upId).getParameter(offeringTermParam);



        // Optional Offering Start Date and Offering End Date

        String optionalMonthStartString =
                   getRuntimeData(upId).getParameter(offeringMonthStartParam);

        int optionalMonthStartInt = 0;
        if (optionalMonthStartString != null) {
           optionalMonthStartInt = Integer.parseInt(optionalMonthStartString);
        }

        String optionalDayStartString =
                   getRuntimeData(upId).getParameter(offeringDayStartParam);

        int optionalDayStartInt = 0;

        if (optionalDayStartString != null) {
           optionalDayStartInt = Integer.parseInt(optionalDayStartString);
        }



        String optionalYearStartString =
                   getRuntimeData(upId).getParameter(offeringYearStartParam);

        int optionalYearStartInt = 0;

        if (optionalYearStartString != null) {
           optionalYearStartInt = Integer.parseInt(optionalYearStartString);
        }



        String optionalMonthEndString =
                   getRuntimeData(upId).getParameter(offeringMonthEndParam);

        int optionalMonthEndInt = 0;

        if (optionalMonthEndString != null) {
           optionalMonthEndInt = Integer.parseInt(optionalMonthEndString);
        }



        String optionalDayEndString =
                   getRuntimeData(upId).getParameter(offeringDayEndParam);

        int optionalDayEndInt = 0;

        if (optionalDayEndString != null) {
           optionalDayEndInt = Integer.parseInt(optionalDayEndString);
        }



        String optionalYearEndString =
                   getRuntimeData(upId).getParameter(offeringYearEndParam);
        int optionalYearEndInt = 0;

        if (optionalYearEndString != null) {
           optionalYearEndInt = Integer.parseInt(optionalYearEndString);
        }





        // Optional Offering Meeting Days of the Week

        int daysOfWeek = 0;

        String optionalMeetsMondayString =
                   getRuntimeData(upId).getParameter(offeringMtgMonParam);
        if (optionalMeetsMondayString != null 
            && "1".equals(optionalMeetsMondayString.trim())) {
            daysOfWeek |= OfferingFactory.MONDAY;
        }

        String optionalMeetsTuesdayString =
                   getRuntimeData(upId).getParameter(offeringMtgTueParam);
        if (optionalMeetsTuesdayString != null
            && "1".equals(optionalMeetsTuesdayString.trim())) {
            daysOfWeek |= OfferingFactory.TUESDAY;
        }

        String optionalMeetsWednesdayString =
                   getRuntimeData(upId).getParameter(offeringMtgWedParam);

        if (optionalMeetsWednesdayString != null
            && "1".equals(optionalMeetsWednesdayString.trim())) {
            daysOfWeek |= OfferingFactory.WEDNESDAY;
        }

        String optionalMeetsThursdayString =
                   getRuntimeData(upId).getParameter(offeringMtgThuParam);
        if (optionalMeetsThursdayString != null
            && "1".equals(optionalMeetsThursdayString.trim())) {
            daysOfWeek |= OfferingFactory.THURSDAY;
        }

        String optionalMeetsFridayString =
                   getRuntimeData(upId).getParameter(offeringMtgFriParam);
        if (optionalMeetsFridayString != null
            && "1".equals(optionalMeetsFridayString.trim())) {
            daysOfWeek |= OfferingFactory.FRIDAY;
        }

        String optionalMeetsSaturdayString =
                   getRuntimeData(upId).getParameter(offeringMtgSatParam);
        if (optionalMeetsSaturdayString != null
            && "1".equals(optionalMeetsSaturdayString.trim())) {
            daysOfWeek |= OfferingFactory.SATURDAY;
        }

        String optionalMeetsSundayString =
                   getRuntimeData(upId).getParameter(offeringMtgSunParam);
        if (optionalMeetsSundayString != null
            && "1".equals(optionalMeetsSundayString.trim())) {
            daysOfWeek |= OfferingFactory.SUNDAY;
        }


        // Optional Offering Start Time and Offering End Time

        String optionalHourStartString =

                   getRuntimeData(upId).getParameter(offeringHourStartParam);

        int optionalHourStartInt = 0;

        if (optionalHourStartString != null) {

            optionalHourStartInt = Integer.parseInt(optionalHourStartString);

        }



        String optionalMinuteStartString =

                   getRuntimeData(upId).getParameter(offeringMinuteStartParam);

        int optionalMinuteStartInt = 0;

        if (optionalMinuteStartString != null) {

            optionalMinuteStartInt =

                                  Integer.parseInt(optionalMinuteStartString);

        }



        String optionalAmPmStartString =

                   getRuntimeData(upId).getParameter(offeringAmPmStartParam);

        int optionalAmPmStartInt = 0;

        if (optionalAmPmStartString != null) {

            optionalAmPmStartInt =

                             Integer.parseInt(optionalAmPmStartString);

        }



        String optionalHourEndString =

                   getRuntimeData(upId).getParameter(offeringHourEndParam);

        int optionalHourEndInt = 0;

        if (optionalHourEndString != null) {

            optionalHourEndInt = Integer.parseInt(optionalHourEndString);

        }



        String optionalMinuteEndString =

                   getRuntimeData(upId).getParameter(offeringMinuteEndParam);

        int optionalMinuteEndInt = 0;

        if (optionalMinuteEndString != null) {



            optionalMinuteEndInt =

                                  Integer.parseInt(optionalMinuteEndString);

        }



        String optionalAmPmEndString =

                   getRuntimeData(upId).getParameter(offeringAmPmEndParam);

        int optionalAmPmEndInt = 0;

        if (optionalAmPmEndString != null) {

            optionalAmPmEndInt =

                            Integer.parseInt(optionalAmPmEndString);

        }



        // Optional Offering Location

        String optionalLocationString =

                   getRuntimeData(upId).getParameter(offeringLocationParam);



        try {

            Role defaultRole = RoleFactory.getRole(roleId);

        User creator = getDomainUser(upId); // new param

            // create the new offering

            Offering newOffering =

                OfferingFactory.createOffering(

                  name,

                  desc,

                  topic,

                  model,

                  defaultRole,

                  ChannelClassFactory.getChannelClasses(ChannelMode.OFFERING),

          creator,

                  optionalIdString,

                  optionalTermString,

                  optionalMonthStartInt,

                  optionalDayStartInt,



                  optionalYearStartInt,

                  optionalMonthEndInt,

                  optionalDayEndInt,

                  optionalYearEndInt,

                  daysOfWeek,

                  optionalHourStartInt,

                  optionalMinuteStartInt,

                  optionalAmPmStartInt,

                  optionalHourEndInt,

                  optionalMinuteEndInt,

                  optionalAmPmEndInt,

                  optionalLocationString);



            // create the offering calendar

            CalendarServiceFactory.getService().createCalendar(newOffering);



            // set the status of the new offering to the same value as the

            // original offering

            originalOffering =

                         OfferingFactory.getOffering(originalOfferingId);

            newOffering.setStatus(originalOffering.getStatus());

            OfferingFactory.persist(newOffering);



            // get the original offering roles - which includes the roles

            // for the offering in addition to the user-defined system

            // roles

            List originalRoles =

                      RoleFactory.getOfferingRoles(originalOffering);



            // copy the roles from the original offering

            // note: only user-defined offering specific roles are

            //       copied; system roles and generic offering roles are not

            if (originalRoles != null) {

              for (int i = 0; i < originalRoles.size(); i++) {

                 Role newOfferingRole = (Role) originalRoles.get(i);

                 String newOfferingLabel = newOfferingRole.getLabel();

                 int newOfferingRoleType = newOfferingRole.getRoleType();

                 if (newOfferingRole.isUserDefinedRole() &&

                     !newOfferingRole.isSystemWide()) {

                     RoleFactory.createRole(newOfferingLabel,

                                            newOffering,

                                            newOfferingRoleType,



                        creator);

                 }

              }

            }



            // copy the membership from the original offering

            List enrolledUserList =

                Memberships.getMembers(originalOffering,

                                       EnrollmentStatus.ENROLLED);

            // If the enrollment model for the cloned offering is

            // Request/Approve, then we get the list of pending users.

            // The list will be null/empty if the model is something other

            // then request/approve.

            List pendingUserList =

                Memberships.getMembers(originalOffering,

                                       EnrollmentStatus.PENDING);



            // loop through the enrolled members list and add them to

            // the new offering

            Role newOfferingRole = null;

            User newOfferingUser = null;

            if (enrolledUserList != null) {

              for (int i = 0; i < enrolledUserList.size(); i++) {

                newOfferingUser = (net.unicon.academus.domain.lms.User)

                                  enrolledUserList.get(i);

                newOfferingRole = Memberships.getRole(newOfferingUser,

                                                      originalOffering);

                Memberships.add(newOfferingUser,

                                newOffering,

                                newOfferingRole,

                                EnrollmentStatus.ENROLLED);



                // Share the user with the offering's calendar

                CalendarServiceFactory.getService().addUser(

                  newOffering, newOfferingUser);

              }

            }

            // loop through the pending members list and add them to

            // the new offering

            newOfferingRole = null;

            newOfferingUser = null;

            if (pendingUserList != null) {

              for (int i = 0; i < pendingUserList.size(); i++) {

                newOfferingUser = (net.unicon.academus.domain.lms.User)

                                  pendingUserList.get(i);

                newOfferingRole = Memberships.getRole(newOfferingUser,

                                                      originalOffering);

                Memberships.add(newOfferingUser,

                                newOffering,

                                newOfferingRole,

                                EnrollmentStatus.PENDING);

              }

             }



        } catch (IllegalArgumentException e) {

            String msg = "Failed to add offering: " +

                ExceptionUtils.getExceptionMessage(e);

            setErrorMsg(upId, msg);

            e.printStackTrace();

            return ">";

        } catch (OperationFailedException e) {

            String msg = "Failed to add offering: " +

                "This offering name is already in use.";

            setErrorMsg(upId, msg);

            e.printStackTrace();

            return ">";

        } catch (ItemNotFoundException e) {

            String msg = "Failed to add offering: " +

                ExceptionUtils.getExceptionMessage(e);

            setErrorMsg(upId, msg);

            e.printStackTrace();

            return ">";

        }



        if (originalOffering != null) {

            broadcastUserOfferingDirtyChannel(originalOffering,

                "NavigationChannel", true);

        }

        // need to also dirty this user's navigation manually because

        // they might not be enrolled in the offering (super user)

        broadcastUserDirtyChannel(getDomainUser(upId), "NavigationChannel");

        setSheetName(upId, mainCommand);



        return ">";

    }

}



