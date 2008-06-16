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
package net.unicon.portal.channels.subscription;

import java.util.*;
import java.sql.*;
import java.lang.NumberFormatException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.jasig.portal.services.LogService;

// UNICON.

import net.unicon.academus.domain.lms.*;
import net.unicon.academus.service.calendar.CalendarServiceFactory;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.sdk.catalog.Catalog;
import net.unicon.sdk.catalog.CatalogException;
import net.unicon.sdk.catalog.IDataSource;
import net.unicon.sdk.catalog.IFilterMode;
import net.unicon.sdk.catalog.FLazyCatalog;
import net.unicon.sdk.catalog.IPageMode;
import net.unicon.sdk.catalog.ISortMode;
import net.unicon.portal.util.db.FDbDataSource;
import net.unicon.sdk.catalog.db.IDbEntryConvertor;
import net.unicon.sdk.catalog.db.FDbFilterMode;
import net.unicon.sdk.catalog.db.FDbPageMode;
import net.unicon.sdk.catalog.db.FDbSortMode;
import net.unicon.portal.channels.BaseSubChannel;
import net.unicon.portal.common.cdm.ChannelDataManager;

import net.unicon.portal.common.PortalObject;
import net.unicon.portal.channels.gradebook.GradebookService;
import net.unicon.portal.channels.gradebook.GradebookServiceFactory;

public class SubscriptionChannel extends BaseSubChannel {

    protected static final String showSubscribedCommand          = "showSubscribed";
    protected static final String subscribeCommand               = "subscribe";
    protected static final String unsubscribeCommand             = "unsubscribe";
    protected static final String unsubscribeConfirmationCommand = "unenroll";
    protected static final String mainCommand                    = "main";
    protected static final String searchCommand                  = "search";
    protected static final String mainSearchCommand              = "mainSearch";
    protected static final String confirmParam                   = "confirmParam";
    
    private static Catalog baseCatalog = null;

    public SubscriptionChannel() { super(); }

    public void buildXML(String upId) throws Exception {

        net.unicon.portal.util.Debug.instance().markTime();

        net.unicon.portal.util.Debug.instance().timeElapsed(

        "BEGIN BUILDING SubscriptionChannel", true);

        setupParameters(upId);

        // Processing command

        String xmlBody = performCommand(upId,

        getRuntimeData(upId).getParameter("command"));

        StringBuffer xmlSB = new StringBuffer();

        xmlSB.append("<subscription ");

        xmlSB.append(xmlBody);

        xmlSB.append("</subscription>\n");

        setXML(upId, xmlSB.toString());

        net.unicon.portal.util.Debug.instance().timeElapsed(

        "DONE BUILDING SubscriptionChannel", true);

    }

    protected String performCommand(String upId, String command)

    throws Exception {

        // default to availableCommand

        String xmlBody = "";

        ArrayList subscribedOfferings = new ArrayList();

        // new for the request/approve enrollment model

        ArrayList requestedOfferings = new ArrayList();

        if (subscribeCommand.equals(command)) {

            xmlBody = subscribeCommand(upId);

            command = searchCommand;

        } else if (unsubscribeCommand.equals(command)) {

            xmlBody = unsubscribeCommand(upId);

            command = searchCommand;

        } else if (unsubscribeConfirmationCommand.equals(command)) {

            xmlBody = unsubscribeConfirmation(upId);

        }

        if (command == null || "".equals(command) || command.equals(unsubscribeCommand)) {

            setSheetName(upId, mainCommand);

            xmlBody = ">";

            return xmlBody;

        } else if (command.equals(searchCommand)) {

            xmlBody += searchCommand(upId);

        }

        setSheetName(upId, command);

        return xmlBody;

    }

    protected String searchCommand(String upId) throws Exception {

        // Gather modes to apply.

        List sortModes = new ArrayList();

        List filterModes = new ArrayList();

        IPageMode pg = null;

        // Calculate which page to view.

        int pgSize = evaluatePageSize(upId);

        int pgNum = evaluateCurrentPage(upId);

        pg = new FDbPageMode(pgSize, pgNum);

        String topicName   = getRuntimeData(upId).getParameter("topicName");

        String offName     = getRuntimeData(upId).getParameter("offName");

        String optIdString = getRuntimeData(upId).getParameter("optId");

        sortModes.add(new FDbSortMode("UPPER(t.name) ASC"));

        sortModes.add(new FDbSortMode("UPPER(o.name) ASC"));

        filterModes.add(new FDbFilterMode("o.status = ?",
            new Object[] { Integer.toString(Offering.ACTIVE) }));

        if ( topicName != null && ! "".equals(topicName)) {

            String sql = "UPPER(t.name) like UPPER(?)";

            filterModes.add(new FDbFilterMode(sql,
                new Object[] { topicName + "%" }));

        } else {

            topicName = "";

        }

        if (offName != null && ! "".equals(offName)) {

            String sql = "UPPER(o.name) like UPPER(?)";

            filterModes.add(new FDbFilterMode(sql,
                new Object[] { offName + "%" }));

        } else {

            offName = "";

        }

        if (optIdString == null || "".equals(optIdString)) {

            getXSLParameters(upId).put("optId", "");

        } else {

            try {

                Long  optId = Long.valueOf(optIdString);

                getXSLParameters(upId).put("optId", optIdString);

                String sql = "o.opt_offeringid like ?";

                filterModes.add(new FDbFilterMode(sql,
                    new Object[] { "%"+optIdString+"%" }));

            } catch ( NumberFormatException nfe) {

                System.out.println("Offering id can not be a string." + nfe);

                getXSLParameters(upId).put("optId", "");

            }

        }

        // Create the catalog.

        Catalog cat = getBaseCatalog().subCatalog(

        (ISortMode[]) sortModes.toArray(new ISortMode[0]),

        (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),

        pg);

        List elements = cat.elements();

        // Share info w/ the xsl.

        Map xslParams = getXSLParameters(upId);

        getXSLParameters(upId).put("topicName", topicName);

        getXSLParameters(upId).put("offName", offName);

        xslParams.put("catCurrentCommand", searchCommand);

        xslParams.put("catSelectPage", new Integer(pgNum));

        xslParams.put("catCurrentPage", new Integer(pgNum));

        xslParams.put("catLastPage", new Integer(pg.getPageCount()));

        xslParams.put("catPageSize", evaluatePageSize(upId) + "");

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

        xmlSB.append(sortElementsbyStatus(upId, elements));

        return xmlSB.toString();

    }

    protected String subscribeCommand(String upId) throws Exception {

        User targetUser = getDomainUser(upId);

        String offeringIdString = getRuntimeData(upId).getParameter("offeringId");

        // enrollmentStatusString will be either ENROLLED (for Open Enrollment

        // Model offerings) or PENDING (for Request/Approve Enrollment Model

        // offerings).

        String enrollmentStatusString = getRuntimeData(upId).getParameter("enrollmentStatus");

        if (offeringIdString != null) {

            long offeringID = Long.parseLong(offeringIdString);

            getXSLParameters(upId).put("catSelectPage", new Integer(getRuntimeData(upId).getParameter("catSelectPage")));

            Offering offering = OfferingFactory.getOffering(offeringID);

            Role role = offering.getDefaultRole();

            List offerings =

            Memberships.getOfferings(getDomainUser(upId), Offering.ACTIVE);

            boolean wasEmpty = (offerings == null || offerings.size() == 0);

            EnrollmentStatus enrollmentStatus =

            EnrollmentStatus.getInstance(enrollmentStatusString);

            Memberships.add(targetUser, offering, role, enrollmentStatus);

            // Only create gradebook related information for a user if they are

            // actually being ENROLLED into an offering.  PENDING enrollment

            // requests will not cause the gradebook related information to be

            // created.

            if (enrollmentStatusString.equals("ENROLLED")) {

                // Share the user with the offering's calendar

                CalendarServiceFactory.getService().addUser(offering, targetUser);

                Connection conn = null;
                try {
                conn = getDBConnection();

                GradebookService gb = GradebookServiceFactory.getService();

                gb.insertUserScores(offering, targetUser, conn);

                gb.recalculateStatistics(offering, conn);
                } finally {
                releaseDBConnection(conn);
                }

                super.broadcastUserOfferingDirtyChannel(offering,
                    "GradebookChannel", false);

            }

            // Set the Roster Channel to Dirty because the user has either

            // been enrolled into the offering or is pending approval for

            // enrollment into the offering.  In either case, the roster

            // channel will need to be re-rendered.

            super.broadcastUserOfferingDirtyChannel(offering,
                "RosterChannel", false);

            setChannelDirty(upId, "NavigationChannel", true);

        }

        return "";

    }

    protected String unsubscribeCommand(String upId) throws Exception {

        String confirmationParam = getRuntimeData(upId).getParameter(confirmParam);

        if (!confirmationParam.equals("yes")) {

            return "";

        }

        String offeringIdString = getRuntimeData(upId).getParameter("offeringId");

        // enrollmentStatusString will be either ENROLLED or PENDING

        String enrollmentStatusString = getRuntimeData(upId).getParameter("enrollmentStatus");

        if (offeringIdString != null) {

            long offeringID = Long.parseLong(offeringIdString);

            Offering offering = OfferingFactory.getOffering(offeringID);

            getXSLParameters(upId).put("catSelectPage", new Integer(getRuntimeData(upId).getParameter("catSelectPage")));

            // if the user's current offering was this one,

            // then remove it

            Context context = getDomainUser(upId).getContext();

            Offering currentOffering =

            context.getCurrentOffering(TopicType.ACADEMICS);

            if (currentOffering != null &&  currentOffering.getId() == offering.getId()) {

                context.setCurrentOffering(0, TopicType.ACADEMICS);

            }

            EnrollmentStatus enrollmentStatus =

            EnrollmentStatus.getInstance(enrollmentStatusString);

            Memberships.remove(getDomainUser(upId), offering, enrollmentStatus);

            // Only remove gradebook related information for a user who was

            // actually ENROLLED in an offering.  PENDING enrollment

            // requests will not cause the gradebook related information to be

            // created in the first place, and consequently will not have to

            // be deleted.

            if (enrollmentStatusString.equals("ENROLLED")) {

                // Share the user with the offering's calendar

                CalendarServiceFactory.getService().removeUser(offering, getDomainUser(upId));

                GradebookService gb = GradebookServiceFactory.getService();

                Connection conn = null;
                try {
                conn = getDBConnection();

                gb.recalculateStatistics(offering, conn);
                } finally {
                releaseDBConnection(conn);
                }

                super.broadcastUserOfferingDirtyChannel(offering,
                    "GradebookChannel", false);

            }

            super.broadcastUserOfferingDirtyChannel(offering,
                "RosterChannel", false);

            setChannelDirty(upId, "NavigationChannel", true);

        } else {

            LogService.instance().log(LogService.ERROR,

            "Offering ID is null or not valid, unable to unsubscribe");

        }

        return "";

    }

    protected String unsubscribeConfirmation(String upId) throws Exception {

        StringBuffer xmlSB = new StringBuffer();

        getXSLParameters(upId).put("catSelectPage", new Integer(getRuntimeData(upId).getParameter("catSelectPage")));

        ArrayList subscribedOfferings = new ArrayList();

        ArrayList requestedOfferings  = new ArrayList();

        xmlSB.append(searchCommand(upId));

        getXSLParameters(upId).put("offeringId",

        getRuntimeData(upId).getParameter("offeringId"));

        getXSLParameters(upId).put("enrollmentStatus",

        getRuntimeData(upId).getParameter("enrollmentStatus"));

        return xmlSB.toString();

    }

    protected void setupParameters(String upId) throws Exception {

        //setSSLLocation(upId, "SubscriptionChannel.ssl");
	ChannelDataManager.setSSLLocation(upId, 
	      ChannelDataManager.getChannelClass(upId).getSSLLocation());

        User user = getDomainUser(upId);

        String subscribe;

        String unsubscribe;

        if (getPermissions(upId).canDo(user.getUsername(), "subscribe")) {
            subscribe = IPermissions.YES;
        } else {
            subscribe =  IPermissions.NO;
        }

        if (getPermissions(upId).canDo(user.getUsername(), "unsubscribe")) {
            unsubscribe = IPermissions.YES;
        } else {
            unsubscribe =  IPermissions.NO;
        }

        Hashtable params = getXSLParameters(upId);

        // Add the stylesheet parameters
        params.put("unsubscribe",            unsubscribe);
        params.put("subscribe", subscribe);
        params.put("showSubscribedCommand",  showSubscribedCommand);
        params.put("subscribeCommand",       subscribeCommand);
        params.put("unsubscribeCommand",     unsubscribeCommand);
        params.put("unsubscribeConfirmation", unsubscribeConfirmationCommand);
    }

    private static Catalog getBaseCatalog() {

        if (baseCatalog == null) {

            baseCatalog = new FLazyCatalog(createBaseDataSource());

        }

        return baseCatalog;

    }

    private static IDataSource createBaseDataSource() {

        StringBuffer qBase = new StringBuffer();

        qBase.append("SELECT o.OFFERING_ID, t.NAME AS TOPICNAME, o.NAME, ENROLLMENT_MODEL");

        qBase.append(" FROM OFFERING o, TOPIC t, TOPIC_OFFERING ot WHERE o.ENROLLMENT_MODEL <> 'Facilitator' AND t.TOPIC_ID = ot.TOPIC_ID AND ot.OFFERING_ID = o.OFFERING_ID");

        return new FDbDataSource(qBase.toString(), createBaseEntryConvertor());

    }

    private static IDbEntryConvertor createBaseEntryConvertor() {

        return new IDbEntryConvertor() {

            public Object convertRow(ResultSet rs)

            throws SQLException, CatalogException {

                StringBuffer xmlSB = new StringBuffer();

                xmlSB.append("      <offering id=\"" + rs.getLong("OFFERING_ID") + "\">\n");

                xmlSB.append("      <name>" + rs.getString("TOPICNAME") + "</name>\n");

                xmlSB.append("      <description>" +

                rs.getString("NAME") + "</description>\n");

                xmlSB.append("      <enrollmentModel>" + rs.getString("ENROLLMENT_MODEL") + "</enrollmentModel>\n");

                xmlSB.append("      </offering>\n");

                return xmlSB.toString();

            }

        };

    }

    protected String sortElementsbyStatus(String upId, List elementList) throws SAXException {

        StringBuffer requestedBuff = new StringBuffer();

        StringBuffer subscribedBuff = new StringBuffer();

        StringBuffer unenrolledBuff = new StringBuffer();

        User user = super.getDomainUser(upId);

        Offering offering = null;

        String rtStr;

        try {

            for (int x = 0; x < elementList.size(); x++) {

                InputStream is = new ByteArrayInputStream(((String) elementList.get(x)).getBytes());

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                DocumentBuilder db = dbf.newDocumentBuilder();

                Document doc = db.parse(is);

                Element element = doc.getDocumentElement();

                offering = OfferingFactory.getOffering(Long.parseLong(element.getAttribute("id")));

                if (Memberships.isEnrolled(user, offering, EnrollmentStatus.ENROLLED)) {

                    subscribedBuff.append((String)elementList.get(x));

                }

                else if (Memberships.isEnrolled(user, offering, EnrollmentStatus.PENDING)) {

                    requestedBuff.append((String)elementList.get(x));

                } else {

                    unenrolledBuff.append((String)elementList.get(x));

                }

            }

            rtStr = "    <available>\n" + unenrolledBuff.toString() + "    </available>\n" +

            "    <requested>\n" + requestedBuff.toString() + "    </requested>\n" +

            "    <subscribed>\n" + subscribedBuff.toString() + "    </subscribed>\n";

        } catch (Exception e) {

            String msg = "Unable to parse specified content.";

            throw new SAXException(msg);

        }

        return rtStr;

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

}

