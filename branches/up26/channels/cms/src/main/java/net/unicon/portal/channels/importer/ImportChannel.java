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

package net.unicon.portal.channels.importer;

import net.unicon.sdk.util.StringUtils;
import java.util.*;
import java.io.*;
//import java.sql.Connection;
import javax.xml.parsers.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.apache.xpath.XPathAPI;
import org.xml.sax.helpers.DefaultHandler;

import net.unicon.academus.domain.*;
import net.unicon.academus.domain.lms.*;
import net.unicon.academus.service.calendar.CalendarServiceFactory;
import net.unicon.portal.domain.*;
import net.unicon.portal.permissions.*;

import org.jasig.portal.UploadStatus;
import net.unicon.portal.channels.BaseSubChannel;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.portal.common.properties.*;
import net.unicon.portal.domain.*;
import net.unicon.portal.permissions.*;

import org.jasig.portal.*;

public class ImportChannel extends BaseSubChannel {

    public ImportChannel() {
        super();
    }

    public void buildXML(String upId) throws Exception {

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

        //super.setSSLLocation(upId, "ImportChannel.ssl");
        ChannelDataManager.setSSLLocation(upId,
            ChannelDataManager.getChannelClass(upId).getSSLLocation());

        super.setSheetName(upId, "main");

        String command = runtimeData.getParameter("command");

        StringBuffer xmlSB = new StringBuffer();
        xmlSB.append("<import>");

        String msg = null;
        IPermissions p = getPermissions(upId);
        if (!p.canDo(getDomainUser(upId).getUsername(), "canImport")) {
            super.setXML(upId, "<import/>");
            super.setSheetName(upId, "unauthorized");
            return;
        }

        if ("importUsers".equals(command)) {
            super.setSheetName(upId, "user");
        } else if ("importOfferings".equals(command)) {
            super.setSheetName(upId, "offering");
        } else if ("importRoster".equals(command)) {
            super.setSheetName(upId, "roster");
        } else if ("import".equals(command)) {
            String type = runtimeData.getParameter("import-type");
            long parentTopicId = 0;
            if ("offerings".equals(type)) {
                parentTopicId =
                Long.parseLong(runtimeData.getParameter("topicId"));
            }
            Object[] x = runtimeData.getObjectParameterValues("import-file");
            if (x != null) {
                MultipartDataSource content = (MultipartDataSource) x[0];
                DocumentBuilderFactory builderFactory =
                    DocumentBuilderFactory.newInstance();
                builderFactory.setValidating(false);
                DocumentBuilder documentBuilder =
                    builderFactory.newDocumentBuilder();
                try {
                    Document doc =
                        documentBuilder.parse(content.getInputStream());
                    if ("users".equals(type)) {
                        msg = importUsers(doc);
                    } else if ("offerings".equals(type)) {
                        msg = importOfferings(doc, parentTopicId, upId);
                    } else {
                        msg = "<message>Import failed. Invalid import type: " +
                        type + ".</message>";
                    }
                } catch (Throwable t) {
                    msg = "<message>Invalid XML. Only well-formed XML files can be " +
                    		"imported through this channel.</message>";
                }
            } else {
                msg = "<message>Import failed. No import file specified.</message>";
            }
        }

        if (msg != null) {
            xmlSB.append(msg);
        }

        xmlSB.append(getTopicXML(TopicType.ACADEMICS));
        xmlSB.append(getTopicXML(TopicType.COMMUNITIES_OF_INTEREST));
        xmlSB.append("</import>");
        
        super.setXML(upId, xmlSB.toString());
    }

    protected void addError(Map errorMap, String msg, String username) {

        List userList = (List)errorMap.get(msg);
        if (userList == null) {
            userList = new ArrayList();
            errorMap.put(msg, userList);
        }
        userList.add(username);
    }

    protected String importUsers(Document doc) throws Exception {
        Element el = null;
        Node userNode = null;
        NodeList nl = doc.getElementsByTagName("user");

        String username = null;
        String firstname = null;
        String lastname = null;
        String roleLabel = null;
        String email = null;

        XPathAPI xpath = new XPathAPI();
        Node node = null;
        Map errorMap = new HashMap();
        List successList = new ArrayList();
        for (int i = 0; i < nl.getLength(); i++) {
            el = (Element)nl.item(i);
            node = xpath.selectSingleNode(el, "username");
            if (node == null) {
                addError(errorMap, "<username> tag not specified.", "");
                continue;
            }
            username = node.getFirstChild().getNodeValue();

            node = xpath.selectSingleNode(el, "firstname");
            if (node == null) {
                addError(errorMap, "<firstname> tag not specified.", username);
                continue;
            }
            firstname = node.getFirstChild().getNodeValue();

            node = xpath.selectSingleNode(el, "lastname");
            if (node == null) {
                addError(errorMap, "<lastname> tag not specified.", username);
                continue;
            }
            lastname = node.getFirstChild().getNodeValue();

            node = xpath.selectSingleNode(el, "role");
            if (node == null) {
                addError(errorMap, "<role> tag not specified.", username);
                continue;
            }
            roleLabel = node.getFirstChild().getNodeValue();

            node = xpath.selectSingleNode(el, "email");
            if (node == null) {
                addError(errorMap, "<email> tag not specified.", username);
                continue;
            }
            email = node.getFirstChild().getNodeValue();

            if ("".equals(username.trim())) {
                addError(errorMap, "<username> tag is empty.", "");
                continue;
            }

            if ("".equals(firstname.trim())) {
                addError(errorMap, "<firstname> tag is empty.", username);
                continue;
            }

            if ("".equals(lastname.trim())) {
                addError(errorMap, "<lastname> tag is empty.", username);
                continue;
            }

            if ("".equals(roleLabel.trim())) {
                addError(errorMap, "<role> tag is empty.", username);
                continue;
            }

            if ("".equals(email.trim())) {
                addError(errorMap, "<email> tag is empty.", username);
                continue;
            }

            if (UserFactory.usernameExists(username)) {
                addError(errorMap, "Username already exists.", username);
                continue;
            }

            Role role = null;
            try {
                role = RoleFactory.getDefaultRole(roleLabel, Role.SYSTEM);
            } catch (OperationFailedException e) {
                addError(errorMap,
                    "Invalid role \"" + roleLabel + "\" specified.", username);
                continue;
            }

            try {
                UserFactory.createUser(username,
                                       username,
                                       firstname,
                                       lastname,
                                       email,
                                       null);
                successList.add(username);
                if (role != null) {
                    role.getGroup().addMember(UserFactory.getUser(username).getGroupMember());
                }
            } catch (Exception e) {
                addError(errorMap, "Failed to create user.", username);
                continue;
            }
        } // end for loop

        StringBuffer ret = new StringBuffer();
        if (successList.size() <= 0 && errorMap.size() <= 0) {
            ret.append("<result msg=\"No Users Imported\">\n");
            ret.append("<entry>Import file contained no valid users.</entry>");
            ret.append("</result>\n");
        }
        ret.append("<message>User Import Results</message>");

        Iterator itr = successList.iterator();
        if (itr != null && itr.hasNext()) {
            ret.append("<result msg=\"Successful User Imports\">\n");
            while (itr != null && itr.hasNext()) {
                ret.append("<entry>" + makeStringHTMLSafe(
                    (String)itr.next()) + "</entry>\n");
            }
            ret.append("</result>\n");
        }

        itr = errorMap.keySet().iterator();
        while (itr != null && itr.hasNext()) {
            String msg = (String)itr.next();
            List users = (List)errorMap.get(msg);
            ret.append(
                "<result msg=\"" + makeStringHTMLSafe(msg) + "\">\n");

            Iterator itr2 = users.iterator();
            int nullCount = 0;
            while (itr2 != null && itr2.hasNext()) {
                String name = (String)itr2.next();
                if (name == null || "".equals(name)) {
                    nullCount++;
                    continue;
                }
                ret.append(
                    "<entry>" + makeStringHTMLSafe(name) +
                    "</entry>\n");
            }

            if (nullCount > 0) {
                ret.append("" + nullCount + " entries.");
            }
            ret.append("</result>\n");
        }

        return ret.toString();
    }

    protected String importOfferings(Document doc, long parentTopicId, String upId)

    throws Exception {

        Element el = null;

        Node offeringNode = null;

        NodeList nl = doc.getElementsByTagName("offering");

        String name = null;

        String description = null;

        String enrollmentModelLabel = null;

        String roleLabel = null;

        // **********************************************************
        // Added for Requirements OA 4.1-4.12
        //

        String optionalIdString = null;
        String optionalTermString = null;

        int optionalMonthStartInt = 0;
        int optionalDayStartInt   = 0;
        int optionalYearStartInt  = 0;
        int optionalMonthEndInt   = 0;
        int optionalDayEndInt     = 0;
        int optionalYearEndInt    = 0;
        int optionalDaysOfWeek    = 0;
        int optionalHourStartInt  = 0;
        int optionalMinuteStartInt = 0;
        int optionalAmPmStartInt  = 0;
        int optionalHourEndInt    = 0;
        int optionalMinuteEndInt  = 0;
        int optionalAmPmEndInt    = 0;

        String optionalLocationString = null;

        String optionalString = null;

        // **********************************************************

        List channelList =

        ChannelClassFactory.getChannelClasses(

        ChannelMode.OFFERING);

        Map errorMap = new HashMap();

        List successList = new ArrayList();

        XPathAPI xpath = new XPathAPI();

        Node node = null;

        Node childNode = null;  // Added for Requirements OA 4.1-4.12
        
        // Moved Topic creation from inside the bottom of for-loop to 
        // improve performance and allow reuse
        Topic topic = null;
        try {
            topic = TopicFactory.getTopic(parentTopicId);
        } catch (ItemNotFoundException e) {
            addError(errorMap,
            "Failed to find topic \"" + parentTopicId +
            "\" specified.", name);
        }
        
        if (topic != null) {

            for (int i = 0; i < nl.getLength(); i++) {

                // **********************************************************
                // Added for Requirements OA 4.1-4.12
                //
                optionalIdString = null;
                optionalTermString = null;
                optionalMonthStartInt = 0;
                optionalDayStartInt = 0;
                optionalYearStartInt = 0;
                optionalMonthEndInt = 0;
                optionalDayEndInt = 0;
                optionalYearEndInt = 0;
                optionalDaysOfWeek = 0;
                optionalHourStartInt = 0;
                optionalMinuteStartInt = 0;
                optionalAmPmStartInt = 0;
                optionalHourEndInt = 0;
                optionalMinuteEndInt = 0;
                optionalAmPmEndInt = 0;
                optionalLocationString = null;

                // **********************************************************

                el = (Element) nl.item(i);

                node = xpath.selectSingleNode(el, "name");

                if (node == null) {
                    addError(errorMap, "<name> tag not specified.", "");
                    continue;
                }

                name = node.getFirstChild().getNodeValue();

                // TT 04651 - Import: Users are able to import the same offering
                // multiple times.
                // Fix - Allow an offering name to exist only once within a
                // topic.
                if (topic.hasOffering(name)) {

                    String msg = "Offering already exists in Topic: "
                            + topic.getName();
                    addError(errorMap, msg, name);
                    continue;

                }

                node = xpath.selectSingleNode(el, "description");

                if (node == null) {
                    addError(errorMap, "<description> tag not specified.", name);
                    continue;
                }

                description = node.getFirstChild().getNodeValue();

                node = xpath.selectSingleNode(el, "enrollmentModel");

                if (node == null) {
                    addError(errorMap, "<enrollmentModel> tag not specified.",
                            name);
                    continue;
                }

                enrollmentModelLabel = node.getFirstChild().getNodeValue();

                node = xpath.selectSingleNode(el, "role");

                if (node == null) {
                    addError(errorMap, "<role> tag not specified.", name);
                    continue;
                }

                roleLabel = node.getFirstChild().getNodeValue();

                // *********************************************************
                // Added for Requirements OA 4.1-4.12
                // Optional Offering ID

                node = xpath.selectSingleNode(el, "optionalOfferingId");
                if (node != null) {
                    optionalIdString = node.getFirstChild().getNodeValue();
                }

                // Optional Offering Term
                node = xpath.selectSingleNode(el, "optionalOfferingTerm");
                if (node != null) {
                    optionalTermString = node.getFirstChild().getNodeValue();
                }

                // Optional Offering Start Month, Day, and Year
                node = xpath.selectSingleNode(el, "optionalOfferingStartDate");
                if (node != null) {
                    childNode = xpath.selectSingleNode(node, "MonthStart");
                    if (childNode != null) {
                        optionalString = childNode.getFirstChild()
                                .getNodeValue();
                        if (optionalString != null) {
                            try {
                                optionalMonthStartInt = Integer
                                        .parseInt(optionalString);

                                if (optionalMonthStartInt < 1
                                        || optionalMonthStartInt > 12) {
                                    addError(errorMap,
                                            "<MonthStart> tag value "
                                                    + "out of range (1-12)!",
                                            name);
                                    optionalMonthStartInt = 0;
                                }
                            } catch (Exception e) {
                                // Catching an Integer Parse Message
                                addError(errorMap, "<MonthStart> tag does not "
                                        + "contain a valid number!", name);
                                optionalMonthStartInt = 0;
                            }
                        }
                    }

                    childNode = xpath.selectSingleNode(node, "DayStart");

                    if (childNode != null) {

                        optionalString = childNode.getFirstChild()
                                .getNodeValue();

                        if (optionalString != null) {

                            try {
                                optionalDayStartInt = Integer
                                        .parseInt(optionalString);

                                if (optionalDayStartInt < 1
                                        || optionalDayStartInt > 31) {
                                    addError(errorMap, "<DayStart> tag value "
                                            + "out of range (1-31)!", name);
                                    optionalDayStartInt = 0;
                                }
                            } catch (Exception e) {
                                addError(errorMap, "<DayStart> tag does not "
                                        + "contain a valid number!", name);
                                optionalDayStartInt = 0;
                            }
                        }
                    }

                    childNode = xpath.selectSingleNode(node, "YearStart");

                    if (childNode != null) {

                        optionalString = childNode.getFirstChild()
                                .getNodeValue();

                        if (optionalString != null) {

                            try {

                                optionalYearStartInt =

                                Integer.parseInt(optionalString);

                                if (optionalYearStartInt < 2002 ||

                                optionalYearStartInt > 2010) {

                                    addError(errorMap, "<YearStart> tag value "
                                            +

                                            "out of range (2002-2010)!", name);

                                    optionalYearStartInt = 0;

                                }

                            } catch (Exception e) {

                                addError(errorMap, "<YearStart> tag does not " +

                                "contain a valid number!", name);

                                optionalYearStartInt = 0;

                            }

                        }

                    }

                }

                // Optional Offering End Month, Day, and Year

                node = xpath.selectSingleNode(el, "optionalOfferingEndDate");

                if (node != null) {

                    childNode = xpath.selectSingleNode(node, "MonthEnd");

                    if (childNode != null) {

                        optionalString = childNode.getFirstChild()
                                .getNodeValue();

                        if (optionalString != null) {

                            try {

                                optionalMonthEndInt =

                                Integer.parseInt(optionalString);

                                if (optionalMonthEndInt < 1 ||

                                optionalMonthEndInt > 12) {

                                    addError(errorMap, "<MonthEnd> tag value " +

                                    "out of range (1-12)!", name);

                                    optionalMonthEndInt = 0;

                                }

                            } catch (Exception e) {
                                addError(errorMap, "<MonthEnd> tag does not "
                                        + "contain a valid number!", name);
                                optionalMonthEndInt = 0;
                            }
                        }
                    }

                    childNode = xpath.selectSingleNode(node, "DayEnd");

                    if (childNode != null) {

                        optionalString = childNode.getFirstChild()
                                .getNodeValue();

                        if (optionalString != null) {

                            try {

                                optionalDayEndInt =

                                Integer.parseInt(optionalString);

                                if (optionalDayEndInt < 1 ||

                                optionalDayEndInt > 31) {

                                    addError(errorMap, "<DayEnd> tag value " +

                                    "out of range (1-31)!", name);

                                    optionalDayEndInt = 0;

                                }

                            } catch (Exception e) {

                                addError(errorMap, "<DayEnd> tag does not " +

                                "contain a valid number!", name);

                                optionalDayEndInt = 0;

                            }

                        }

                    }

                    childNode = xpath.selectSingleNode(node, "YearEnd");

                    if (childNode != null) {

                        optionalString = childNode.getFirstChild()
                                .getNodeValue();

                        if (optionalString != null) {

                            try {

                                optionalYearEndInt =

                                Integer.parseInt(optionalString);

                                if (optionalYearEndInt < 2002 ||

                                optionalYearEndInt > 2010) {

                                    addError(errorMap, "<YearEnd> tag value " +

                                    "out of range (2002-2010)!", name);

                                    optionalYearEndInt = 0;

                                }

                            } catch (Exception e) {

                                addError(errorMap, "<YearEnd> tag does not " +

                                "contain a valid number!", name);

                                optionalYearEndInt = 0;

                            }

                        }

                    }

                }

                // Optional Offering Meeting Days
                node = xpath
                        .selectSingleNode(el, "optionalOfferingMeetingDays");

                if (node != null) {
                    optionalDaysOfWeek = this.getDaysOfWeek(node);

                }

                // Optional Offering Start Time (Hour, Minute, AM/PM)

                node = xpath.selectSingleNode(el, "optionalOfferingStartTime");

                if (node != null) {

                    childNode = xpath.selectSingleNode(node, "HourStart");

                    if (childNode != null) {

                        optionalString = childNode.getFirstChild()
                                .getNodeValue();

                        if (optionalString != null) {

                            try {

                                optionalHourStartInt =

                                Integer.parseInt(optionalString);

                                if (optionalHourStartInt < 1 ||

                                optionalHourStartInt > 12) {

                                    addError(errorMap, "<HourStart> tag value "
                                            +

                                            "out of range (1-12)!", name);

                                    optionalHourStartInt = 0;

                                }

                            } catch (Exception e) {

                                addError(errorMap, "<HourStart> tag does not " +

                                "contain a valid number!", name);

                                optionalHourStartInt = 0;

                            }

                        }

                    }

                    childNode = xpath.selectSingleNode(node, "MinuteStart");

                    if (childNode != null) {

                        optionalString = childNode.getFirstChild()
                                .getNodeValue();

                        if (optionalString != null) {

                            try {

                                optionalMinuteStartInt =

                                Integer.parseInt(optionalString);

                                if (optionalMinuteStartInt < 0 ||

                                optionalMinuteStartInt > 59) {

                                    addError(errorMap,
                                            "<MinuteStart> tag value " +

                                            "out of range (0-59)!", name);

                                    optionalMinuteStartInt = 0;

                                }

                            } catch (Exception e) {

                                addError(errorMap,
                                        "<MinuteStart> tag does not " +

                                        "contain a valid number!", name);

                                optionalMinuteStartInt = 0;

                            }

                        }

                    }

                    // Make sure that the MinuteStart is set to the closest

                    // multiple of 5 minutes (to match the GUI pull-down menu

                    // selections). This is accomplished by making sure that

                    // the value mod 5 == 0.

                    while ((optionalMinuteStartInt % 5) != 0) {

                        optionalMinuteStartInt--;

                    }

                    childNode = xpath.selectSingleNode(node, "AmPmStart");

                    if (childNode != null) {

                        optionalString = childNode.getFirstChild()
                                .getNodeValue();

                        if (optionalString != null) {

                            try {

                                optionalAmPmStartInt =

                                Integer.parseInt(optionalString);

                                if (optionalAmPmStartInt < 1 ||

                                optionalAmPmStartInt > 2) {

                                    addError(
                                            errorMap,
                                            "<AmPmStart> tag value " +

                                            "out of range (1 for AM, 2 for PM)!",

                                            name);

                                    optionalAmPmStartInt = 1;

                                }

                            } catch (Exception e) {

                                addError(errorMap, "<AmPmStart> tag does not " +

                                "contain a valid number!", name);

                                optionalAmPmStartInt = 1;

                            }

                        }

                    }

                }

                // Optional Offering End Time (Hour, Minute, AM/PM)

                node = xpath.selectSingleNode(el, "optionalOfferingEndTime");

                if (node != null) {

                    childNode = xpath.selectSingleNode(node, "HourEnd");

                    if (childNode != null) {

                        optionalString = childNode.getFirstChild()
                                .getNodeValue();

                        if (optionalString != null) {

                            try {
                                optionalHourEndInt = Integer
                                        .parseInt(optionalString);
                                if (optionalHourEndInt < 1
                                        || optionalHourEndInt > 12) {
                                    addError(errorMap, "<HourEnd> tag value "
                                            + "out of range (1-12)!", name);
                                    optionalHourEndInt = 0;
                                }
                            } catch (Exception e) {
                                addError(errorMap, "<HourEnd> tag does not "
                                        + "contain a valid number!", name);
                                optionalHourEndInt = 0;
                            }
                        }
                    }

                    childNode = xpath.selectSingleNode(node, "MinuteEnd");

                    if (childNode != null) {

                        optionalString = childNode.getFirstChild()
                                .getNodeValue();

                        if (optionalString != null) {

                            try {

                                optionalMinuteEndInt =

                                Integer.parseInt(optionalString);

                                if (optionalMinuteEndInt < 0 ||

                                optionalMinuteEndInt > 59) {

                                    addError(errorMap, "<MinuteEnd> tag value "
                                            +

                                            "out of range (0-59)!", name);

                                    optionalMinuteEndInt = 0;

                                }

                            } catch (Exception e) {

                                addError(errorMap, "<MinuteEnd> tag does not " +

                                "contain a valid number!", name);

                                optionalMinuteEndInt = 0;

                            }

                        }

                    }

                    // Make sure that the MinuteEnd is set to the closest
                    // multiple of 5 minutes (to match the GUI pull-down menu
                    // selections). This is accomplished by making sure that
                    // the value mod 5 == 0.

                    while ((optionalMinuteEndInt % 5) != 0) {
                        optionalMinuteEndInt--;
                    }

                    childNode = xpath.selectSingleNode(node, "AmPmEnd");

                    if (childNode != null) {
                        optionalString = childNode.getFirstChild()
                                .getNodeValue();
                        if (optionalString != null) {
                            try {
                                optionalAmPmEndInt = Integer
                                        .parseInt(optionalString);
                                if (optionalAmPmEndInt < 1
                                        || optionalAmPmEndInt > 2) {
                                    addError(
                                            errorMap,
                                            "<AmPmEnd> tag value "
                                                    + "out of range (1 for AM, 2 for PM)!",
                                            name);
                                    optionalAmPmEndInt = 1;
                                }
                            } catch (Exception e) {
                                addError(errorMap, "<AmPmEnd> tag does not "
                                        + "contain a valid number!", name);
                                optionalAmPmEndInt = 1;
                            }
                        }
                    }
                }

                // Optional Offering Location

                node = xpath.selectSingleNode(el, "optionalOfferingLocation");

                if (node != null) {
                    optionalLocationString = node.getFirstChild()
                            .getNodeValue();
                }

                // *********************************************************

                if ("".equals(name.trim())) {
                    addError(errorMap, "<name> tag is empty.", "");
                    continue;
                }

                if ("".equals(description.trim())) {
                    addError(errorMap, "<description> tag is empty.", name);
                    continue;

                }

                if ("".equals(enrollmentModelLabel.trim())) {
                    addError(errorMap, "<enrollmentModel> tag is empty.", name);
                    continue;
                }

                if ("".equals(roleLabel.trim())) {
                    addError(errorMap, "<roleLabel> tag is empty.", name);
                }

                EnrollmentModel enrollmentModel = null;

                try {
                    enrollmentModel = EnrollmentModel
                            .getInstance(enrollmentModelLabel);
                } catch (ItemNotFoundException e) {
                    addError(errorMap, "Invalid enrollment model \""
                            + enrollmentModelLabel + "\" specified.", name);
                    continue;
                }

                Role role = null;

                try {
                    role = RoleFactory.getDefaultRole(roleLabel, Role.OFFERING);
                } catch (OperationFailedException e) {
                    addError(errorMap, "Invalid role \"" + roleLabel
                            + "\" specified.", name);
                    continue;
                } catch (Exception e) {
                    throw e;
                }

                try {
                    // ****************************************************
                    // Modified for Requirements OA 4.1-4.12
                    //
                    // Changed from:
                    //
                    //OfferingFactory.createOffering(name, description, topic,
                    //    enrollmentModel, role, channelList);
                    //
                    // to:
                    //
                    Offering offering = OfferingFactory.createOffering(name,
                            description, topic, enrollmentModel, role,
                            channelList, getDomainUser(upId), // new creator field
                            optionalIdString, optionalTermString,
                            optionalMonthStartInt, optionalDayStartInt,
                            optionalYearStartInt, optionalMonthEndInt,
                            optionalDayEndInt, optionalYearEndInt,
                            optionalDaysOfWeek, optionalHourStartInt,
                            optionalMinuteStartInt, optionalAmPmStartInt,
                            optionalHourEndInt, optionalMinuteEndInt,
                            optionalAmPmEndInt, optionalLocationString);
                    // ****************************************************

                    // create the offering calendar
                    CalendarServiceFactory.getService()
                            .createCalendar(offering);

                    successList.add(name);
                } catch (Exception e) {
                    e.printStackTrace();
                    addError(errorMap, "Failed to create offering: "
                            + e.getMessage(), name);
                    continue;
                }

            }
        } // End - if topic was found and attempted to import offerings

        StringBuffer ret = new StringBuffer();

        if (successList.size() <= 0 && errorMap.size() <= 0) {
            ret.append("<result msg=\"No Offerings Imported\">\n");
            ret.append("<entry>Import file contained no valid offerings.</entry>");
            ret.append("</result>\n");
        }

        ret.append("<message>Offering Import Results</message>");

        Iterator itr = successList.iterator();

        if (itr != null && itr.hasNext()) {
            if (ChannelDataManager.getChannelClass(upId).isCscrEnabled() &&
                getDomainUser(upId).isSuperUser()) {

                ret.append("<result msg=\"Successful Offering Imports (Use Navigation to make visible)\">\n");

            } else {

                ret.append("<result msg=\"Successful Offering Imports\">\n");

            }

            while (itr != null && itr.hasNext()) {

                ret.append("<entry>" + makeStringHTMLSafe(

                (String)itr.next()) + "</entry>\n");

            }

            ret.append("</result>\n");

            // mark the Navigation and OfferingAdmin channels as being dirty

            broadcastDirtyChannel("OfferingAdminChannel");

            broadcastDirtyChannel("NavigationChannel");

        }

        itr = errorMap.keySet().iterator();

        while (itr != null && itr.hasNext()) {

            String msg = (String)itr.next();

            List offerings = (List)errorMap.get(msg);

            ret.append(

            "<result msg=\"" + makeStringHTMLSafe(msg) + "\">\n");

            Iterator itr2 = offerings.iterator();

            int nullCount = 0;

            while (itr2 != null && itr2.hasNext()) {

                String offeringName = (String)itr2.next();

                if (offeringName == null || "".equals(offeringName)) {

                    nullCount++;

                    continue;

                }

                ret.append(

                "<entry>" + makeStringHTMLSafe(offeringName) +

                "</entry>\n");

            }

            if (nullCount > 0) {

                ret.append("" + nullCount + " entries.");

            }

            ret.append("</result>\n");

        }

        return ret.toString();

    }

    protected String getTopicXML(TopicType topicType) throws Exception {

        StringBuffer sb = new StringBuffer();

        Iterator itr = TopicFactory.getTopics(topicType).iterator();

        while (itr != null && itr.hasNext()) {

            Topic topic = (Topic)itr.next();

            sb.append("<topic id=\"").append(topic.getId()).append("\">");

            sb.append("<name>").append(topic.getName()).append("</name>");

            sb.append("<description>").append("<![CDATA[")
				.append(topic.getDescription()).append("]]>").append("</description>");

            sb.append("<topicType>").append(topic.getTopicType().toString()).append("</topicType>");

            sb.append("</topic>");

        }

        return sb.toString();

    }

    protected int getDaysOfWeek(Node node) throws Exception {
        int numDaysPerWeek = 7;
        String dayNodeName = null;
        int day = 0;

        String optionalString = null;
        Node childNode = null;
        XPathAPI xpath = new XPathAPI();

        int rtnDaysOfWeek = 0;
        for (int ix = 0; ix < numDaysPerWeek; ++ix) {
            switch (ix) {
                case 0:
                    dayNodeName = "MeetsMonday";
                    day = OfferingFactory.MONDAY;
                    break;
                case 1:
                    dayNodeName = "MeetsTuesday";
                    day = OfferingFactory.TUESDAY;
                    break;
                case 2:
                    dayNodeName = "MeetsWedsnesday";
                    day = OfferingFactory.WEDNESDAY;
                    break;
                case 3:
                    dayNodeName = "MeetsThursday";
                    day = OfferingFactory.THURSDAY;
                    break;
                case 4:
                    dayNodeName = "MeetsFriday";
                    day = OfferingFactory.FRIDAY;
                    break;
                case 5:
                    dayNodeName = "MeetsSaturday";
                    day = OfferingFactory.SATURDAY;
                    break;
                case 6:
                    dayNodeName = "MeetsSunday";
                    day = OfferingFactory.SUNDAY;
                    break;
            }// End switch statement

            childNode = xpath.selectSingleNode(node, dayNodeName);

            if (childNode != null) {
                optionalString = childNode.getFirstChild().getNodeValue();

                if (optionalString != null && "1".equals(optionalString)) {
                   rtnDaysOfWeek |= day;
                } // End sub-if
            } // End if
        } // End for loop
        return rtnDaysOfWeek;
    }
    
    protected String makeStringHTMLSafe(String str) {
        StringBuffer s = new StringBuffer();
        char c;
        for (int pos = 0; pos < str.length(); ++pos) {
            switch ( c = str.charAt(pos)) {
                case '<' :
                    s.append("&lt;");
                    break;
                case '>' :
                    s.append("&gt;");
                    break;
                case '"' :
                    s.append("&quot;");
                    break;
                default :
                    s.append(c);
                    break;
            }
        }
        return (s.toString());
    }
}

