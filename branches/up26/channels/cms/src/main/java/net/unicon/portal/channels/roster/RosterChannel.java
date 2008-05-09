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
package net.unicon.portal.channels.roster;

// Java framework classes.

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

// Xml implementation classes.
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

// uPortal framework classes.
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.IServant;
import org.jasig.portal.MultipartDataSource;
import org.jasig.portal.PortalException;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.services.LogService;

// UNICON.
import org.jasig.portal.UploadStatus;
import net.unicon.sdk.catalog.Catalog;
import net.unicon.academus.common.XMLAbleEntity;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.academus.domain.lms.*;
import net.unicon.academus.service.calendar.CalendarServiceFactory;
import net.unicon.portal.channels.sendnotification.SendNotification;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.sdk.catalog.CatalogException;
import net.unicon.sdk.catalog.IDataSource;
import net.unicon.sdk.catalog.IFilterMode;
import net.unicon.sdk.catalog.FLazyCatalog;
import net.unicon.sdk.catalog.IPageMode;
import net.unicon.sdk.catalog.ISortMode;
import net.unicon.sdk.catalog.FSimplePageMode;
import net.unicon.portal.util.db.FDbDataSource;
import net.unicon.sdk.catalog.db.IDbEntryConvertor;
import net.unicon.sdk.catalog.db.FDbFilterMode;
import net.unicon.sdk.catalog.db.FDbSortMode;
import net.unicon.portal.domain.FColUserEntryDataSource;
import net.unicon.sdk.catalog.collection.IColEntryConvertor;
import net.unicon.sdk.catalog.collection.FColFilterMode;
import net.unicon.sdk.catalog.collection.FColSortMode;
import net.unicon.portal.common.BaseOfferingSubChannel;
import net.unicon.portal.domain.*;
import net.unicon.portal.permissions.Activity;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.permissions.PermissionsService;
import net.unicon.portal.permissions.ChannelClassPermissions;

import net.unicon.portal.channels.gradebook.GradebookService;
import net.unicon.portal.channels.gradebook.GradebookServiceFactory;
import net.unicon.portal.common.service.notification.NotificationService;
import net.unicon.portal.common.service.notification.NotificationServiceFactory;
import net.unicon.sdk.util.XmlUtils;
import net.unicon.sdk.util.ExceptionUtils;

public class RosterChannel extends BaseOfferingSubChannel {

    private final static String NAVIGATION_CHANNEL = "NavigationChannel";
    private final static String ROSTER_CHANNEL = "RosterChannel";
    private final static String SUBSCRIPTION_CHANNEL = "SubscriptionChannel";

    private static Catalog baseCatalog = null;

    private static final String ELEMENT_TAGNAME = "user";
    private static final int SEARCH_OR  = 1;
    private static final int SEARCH_AND = 2;
    private static final String FAILURES_KEY = "failures";
    private static final String SUCCESS_KEY = "success";
    private static final String ROSTER_MEMBERS = "ROSTER_MEMBERS";

    // Actions.
    private static final String actViewMember = "viewMember";
    private static final String actEnrollView = "enrollView";
    private static final String actEnroll = "enroll";
    private static final String actUnenroll = "unenroll";
    private static final String actSearch = "search";
    private static final String actSubmit = "submit";
    private static final String actImport = "import";
    private static final String actExecuteImport = "executeImport";
    private static final String actPage = "page";
    private static final String actEditUserPermissions = "editUserPermissions";
    private static final String actEditOfferingPermissions = "editOfferingPermissions";
    private static final String actUpdateUserPermissions = "updateUserPermissions";
    private static final String actUpdateOfferingPermissions = "updateOfferingPermissions";
    private static final String actConfirmUnenroll = "confirmUnenroll";
    private static final String actSendNotification = "sendNotification";
    private static final String actResolve = "resolve";
    private static final String actApprove = "approve";
    private static final String confirmParam = "confirmParam";

    //    private static final String userIdParam = "uid";
    public RosterChannel() {
        super();
    }

    public String exportChannel(Offering offering) throws Exception {
        StringBuffer sb = new StringBuffer("<rosterChannel>\n");

        User user = null;

        List enrolledUsers = Memberships.getMembers(offering, EnrollmentStatus.ENROLLED);
        List pendingUsers  = Memberships.getMembers(offering, EnrollmentStatus.PENDING);

        Role role = null;

        if (enrolledUsers != null) {
            Iterator itr = enrolledUsers.iterator();

            while (itr.hasNext()) {
                user = (User)itr.next();
                sb.append("<user id=\"" + user.getUsername() + "\">\n");
                sb.append("  <role>" + Memberships.getRole(user, offering).getLabel() + "</role>\n");
                sb.append("  <status>" + EnrollmentStatus.ENROLLED.toString() + "</status>\n");
                sb.append("</user>\n");
            }
        }

        if (pendingUsers != null) {
            Iterator itr = pendingUsers.iterator();

            while (itr.hasNext()) {
                user = (User)itr.next();
                sb.append("<user id=\"" + user.getUsername() + "\">\n");
                sb.append("  <role>" + Memberships.getRole(user, offering).getLabel() + "</role>\n");
                sb.append("  <status>" + EnrollmentStatus.PENDING.toString() + "</status>\n");
                sb.append("</user>\n");
            }
        }

        sb.append("</rosterChannel>\n");
        return sb.toString();
    }

    synchronized public Map importChannel(Offering offering, Document dom)
    throws Exception {
        Map results = new HashMap();
        List successList = new ArrayList();

        Map failureMap = new HashMap();

        results.put(SUCCESS_KEY, successList);
        results.put(FAILURES_KEY, failureMap);
        XPathAPI xpath = new XPathAPI();

        Node node = xpath.selectSingleNode(dom, "rosterChannel");

        if (node == null) {
            node = xpath.selectSingleNode(dom, "/offering/rosterChannel");
            if (node == null) {
                addError(failureMap, "XML does not contain any element 'rosterChannel'", "");
                return results;
            }
        }

        NodeList nl = xpath.selectNodeList(node, "user");
        
        if (nl == null) {
            addError(failureMap, "XML does not contain any users", "");
            return results;
        }
        
        Element element = null;
        
        String username = null;
        String roleLabel = null;

        User user = null;
        Role role = null;


        String reason = null;

        int pos = 0;

        String enrollmentModelString =
            offering.getEnrollmentModel().toString();

        String requestApproveEnrollmentModelString =
            EnrollmentModel.REQUESTAPPROVE.toString();
        String enrolledEnrollmentStatusString =
            EnrollmentStatus.ENROLLED.toString();
        String pendingEnrollmentStatusString =
            EnrollmentStatus.PENDING.toString();

        String enrollmentStatusString = null;
        EnrollmentStatus enrollmentStatus = null;

        Connection conn = null;

        try {
            conn = getDBConnection();

            for (int i = 0; i < nl.getLength(); i++) {
                element = (Element)nl.item(i);
                username = element.getAttribute("id");

                if (username == null || "".equals(username.trim())) {
                    addError(failureMap, "Parse error: No id attribute found.",
                            username != null ? username : "");
                    continue;
                }

                node = xpath.selectSingleNode(element, "role");

                if (node != null) {
                	Node roleLabelNode = node.getFirstChild();
                	if(roleLabelNode != null) {
                    	roleLabel = roleLabelNode.getNodeValue();
                    }
                }

                try {
                    user = UserFactory.getUser(username);
                } catch (ItemNotFoundException e) {
                    addError(failureMap, "User does not exist.", username);
                    continue;
                } catch (Exception e) {
                    addError(failureMap, "Failed to retrieve user:" +
                    getExceptionMessage(e), username);
                    continue;
                }

                // first try to get the role as a default, then offering, then
                // try user defined

                if (roleLabel == null) {
                    role = offering.getDefaultRole();
                } else {
                    try {
                        role = RoleFactory.getDefaultRole(roleLabel, Role.OFFERING);
                    } catch (ItemNotFoundException e) {
                        try {
                            role = RoleFactory.getRole(roleLabel,
                            Role.OFFERING, offering);
                        } catch (ItemNotFoundException e2) {
                            try {
                                role = RoleFactory.getRole(roleLabel,
                                        Role.OFFERING | Role.USER_DEFINED, offering);
                            } catch (ItemNotFoundException e3) {
                                addError(failureMap, "Role does not exist.",
                                username + " (" + roleLabel + ")");
                                continue;
                            } catch (Exception e3) {
                                e3.printStackTrace();
                                addError(failureMap, "Failed to retrieve role:" +
                                    getExceptionMessage(e3),
                                username);
                                continue;
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            addError(failureMap, "Failed to retrieve role:" +
                            getExceptionMessage(e2), username);
                            continue;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        addError(failureMap, "Failed to retrieve role:" +
                        getExceptionMessage(e), username);
                        continue;
                    }
                }

                // processing for the enrollment status <status>...</status> node

                node = xpath.selectSingleNode(element, "status");

                if (node != null) {                    if (node.getFirstChild() != null){
                    enrollmentStatusString = node.getFirstChild().getNodeValue();                    }                    if (enrollmentStatusString == null)                    {                      enrollmentStatusString = "enrolled";                      }
                }

            // Do we have a valid enrollment status value?
            // If so, we get an instance of the type-safe enumeration enrollment
            // status object.  Otherwise, we have an error and we continue processing
            // the next user node (with the next iteration of the for loop).
                try {
                    enrollmentStatus =
                        EnrollmentStatus.getInstance(enrollmentStatusString.toUpperCase());
                } catch (ItemNotFoundException e) {
                    e.printStackTrace();
                    addError(failureMap, "Failed to determine enrollment status: " +
                    getExceptionMessage(e), username);
                    continue;
                }

            // We have a valid enrollment status value and now we have to make sure that
            // the enrollment status value is actually supported by the enrollment
            // model for the offering.  For example, an enrollment status value of
            // PENDING only makes sense if the enrollment model for the offering is
            // Request/Approve.  Otherwise, we have an error.
                if ((enrollmentStatusString.toUpperCase().equals(pendingEnrollmentStatusString))  
                    && !(enrollmentModelString.equals(requestApproveEnrollmentModelString))) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("Invalid enrollment status: ").append(enrollmentModelString);
                    sb.append(" does not support a ").append(pendingEnrollmentStatusString);
                    sb.append(" value.");

                    addError(failureMap, sb.toString(), username);
                    continue;
                }

            // Make sure that the user being imported does not already exist in
            // the offering.  If they already exist in the offering, then they
            // will be skipped in the import operation.
                if (Memberships.isEnrolled(user, offering, EnrollmentStatus.ENROLLED) 
                    || Memberships.isEnrolled(user, offering, EnrollmentStatus.PENDING)) {
                    addError(failureMap, "User already enrolled: ", username);
                    continue;
                }

            // add the user to the appropriate database tables and cache
                try {

                Memberships.add(user, offering, role, enrollmentStatus);

                if (EnrollmentStatus.ENROLLED.toInt() == enrollmentStatus.toInt()) {
                    // Share the user with the offering's calendar
                    CalendarServiceFactory.getService().addUser(offering, user);
                }

                successList.add(username);

                GradebookService gb = GradebookServiceFactory.getService();
                gb.insertUserScores(offering, user, conn);
                gb.recalculateStatistics(offering, conn);

                // At this time we do not have a hook to determine the user who
                // has initiated the roster import.  Because we want to notify
                // each user that gets imported, we will send the notification
                // from the affected (imported) user to himself/herself.  It seems
                // more important that the imported user is notified that they
                // have in fact been imported, than who actually sent the
                // notification.

                NotificationService notification = NotificationServiceFactory.getService();
                StringBuffer msg = new StringBuffer();
                msg.append("You have been imported into topic:").append(offering.getTopic().getName());
                msg.append(", offering:").append(offering.getName());
                msg.append(" with an enrollment status of:").append(enrollmentStatus.toString());

                notification.sendNotification(user, user, msg.toString());
                super.broadcastUserOfferingDirtyChannel(user, offering, ROSTER_CHANNEL, false);

                } catch (Exception e) {
                    e.printStackTrace();
                    addError(failureMap, "Import failed: " +
                    getExceptionMessage(e), username);
                    continue;
                }
            } // end for loop
        } finally {
            releaseDBConnection(conn);
        }

        return results;

    }

    public void buildXML(String upId) throws Exception {
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

        //setSSLLocation(upId, "RosterChannel.ssl");
        ChannelDataManager.setSSLLocation(upId, 
          ChannelDataManager.getChannelClass(upId).getSSLLocation());

        User user = getDomainUser(upId);

        Offering offering = user.getContext().getCurrentOffering(TopicType.ACADEMICS);

        // Handle -- There is no offering.

        if (offering == null) {
            super.setSheetName(upId, "empty");
            setXML(upId, "<?xml version=\"1.0\"?><roster/>");
            return;
        }

        // See all parameters
        // spillParameters(getRuntimeData(upId));
        // Establish the action & sheetName once and for all.

        String action = getRuntimeData(upId).getParameter("command");

        if (action == null) action = actPage;

        super.setSheetName(upId, action);

        // DECISION TREE -- Choose an action.

        if (actSendNotification.equals(action)) { // Handle notifications separately.

            // attempt to call IServant
            IServant servant = getSendNotificationServant(upId, offering);

            getRuntimeData(upId).setParameter("upId", upId);

            renderServant(upId, servant);

            if (((IServant) servant).isFinished()) 
                getStaticData(upId).remove("SendNotificationServant");
            return;
        } else {
            getStaticData(upId).remove("SendNotificationServant");
        }

        String xml = null;

        Document doc = null;

        if (actPage.equals(action)) {
            xml = doPageAction(upId);

        } else if (actSearch.equals(action)) {
            xml = doEmptyRoster(upId);

        } else if (actEnrollView.equals(action)) {
            xml = doEnrollViewAction(upId);

        } else if (actEnroll.equals(action)) {
            xml = doEnrollAction(upId);

        } else if (actEditOfferingPermissions.equals(action)) {
            doc = doEditOfferingPermissionsAction(upId);

        } else if (actUpdateOfferingPermissions.equals(action)) {
            xml = doUpdateOfferingPermissionsAction(upId);

        } else if (actImport.equals(action)) {
            xml = doEmptyRoster(upId);

        } else if (actExecuteImport.equals(action)) {
            xml = doExecuteImportAction(upId);

        } else if (actViewMember.equals(action)) {
            xml = doViewMemberAction(upId);

        } else if (actEditUserPermissions.equals(action)) {
            xml = doEditUserPermissionsAction(upId);

        } else if (actUpdateUserPermissions.equals(action)) {
            xml = doUpdateUserPermissionsAction(upId);

        } else if (actConfirmUnenroll.equals(action)) {
            xml = doConfirmUnenrollAction(upId);

        } else if (actUnenroll.equals(action)) {
            xml = doUnenrollAction(upId);

        } else if (actResolve.equals(action)) {
            xml = doResolveAction(upId);

        } else if (actApprove.equals(action)) {
            xml = doApproveAction(upId);
        }

        setXML(upId, xml);
        setDocument(upId, doc);
    }

    private String doEmptyRoster(String upId) throws Exception {
        // Get the offering.
        Offering offering = getDomainUser(upId).getContext().
            getCurrentOffering(TopicType.ACADEMICS);

        // Build the Xml.
        Role defaultRole = offering.getDefaultRole();
        StringBuffer xml = new StringBuffer();
        xml.append("<roster>");
        xml.append(getRoleElements(offering));
        xml.append("</roster>");
        return xml.toString();
    }

    private String doPageAction(String upId) throws Exception {
        // Get xsl parameters.
        Map xslParams = getXSLParameters(upId);

        // Get the offering.
        Offering offering = getDomainUser(upId).getContext().
            getCurrentOffering(TopicType.ACADEMICS);

        // Gather modes to apply.
        List sortModes = new ArrayList();
        List filterModes = new ArrayList();
        IPageMode pg = null;

        // Filter by offering.
        Object[] oParams = new Object[] { new Long(offering.getId()) };
        filterModes.add(new FDbFilterMode("M.OFFERING_ID = ?", oParams));

        // Determine search pattern.
        int searchPattern = evaluateSearchAndOr(upId);
        boolean matchAllCriteria = false;
        switch (searchPattern) {
            case SEARCH_OR:
                matchAllCriteria = false;
                break;

            case SEARCH_AND:
                matchAllCriteria = true;
                break;
        }

        /*
            If doPageAction is called from (un)enroll action(s)
            then skip search filters.
        */
        String action = getRuntimeData(upId).getParameter("command");
        if (!actUnenroll.equals(action) || !actEnroll.equals(action)) {
            // Filter by name, where applicable.
            String fName = evaluateSearchFirstName(upId);
            String lName = evaluateSearchLastName(upId);

            if ((fName != null && !"".equals(fName.trim())) ||
                (lName != null && !"".equals(lName.trim()))) {
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
                // Share info w/ the xsl.
                if (fName != null) xslParams.put("firstName", fName);
                if (lName != null) xslParams.put("lastName", lName);
            }
        }

        // Calculate which page to view.
        int pgSize = evaluatePageSize(upId);
        int pgNum = evaluateCurrentPage(upId);
        pg = new FSimplePageMode(pgSize, pgNum);

        // Create the catalog.
        Catalog cat = getBaseCatalog().subCatalog(
            (ISortMode[]) sortModes.toArray(new ISortMode[0]),
            (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),
            pg);
        List elements = cat.elements();

        // Make sure we're not beyond the last page.
        if (pgNum > pg.getPageCount()) {
            pgNum = pg.getPageCount();
            pg = new FSimplePageMode(pgSize, pgNum);
            cat = getBaseCatalog().subCatalog(
                (ISortMode[]) sortModes.toArray(new ISortMode[0]),
                (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),
                pg);
            elements = cat.elements();
        }

        // Share info w/ the xsl.
        xslParams.put("enrollmentModel", offering.getEnrollmentModel().toString());
        xslParams.put("catCurrentCommand", "page");
        xslParams.put("catCurrentPage", new Integer(pgNum));
        xslParams.put("catLastPage", new Integer(pg.getPageCount()));
        if (searchPattern != 0) 
            xslParams.put("searchAndOr", new Integer(searchPattern));
        if (searchPattern != 0) 
            xslParams.put("emptyListMessage", 
                "No enrolled users match your search criteria.");

        // Build the Xml.
        Role defaultRole = offering.getDefaultRole();

        StringBuffer xml = new StringBuffer();
        xml.append("<roster ");

        if (pgSize == 0) {
            xml.append("page=\"All\" ");
        } else {
            xml.append("page=\"");
            xml.append(pgNum);
            xml.append("\" ");
        }

        xml.append("maxPage=\"" + pg.getPageCount() + "\" ");
        xml.append("enrolledCount=\"");
        xml.append(elements.size());
        xml.append("\" ");
        xml.append("defaultRole=\"" + defaultRole.getId() + "\" ");
        xml.append(">");

        // Get XML from elements.
        for (int i = 0; i < elements.size(); i++) {
            xml.append(((XMLAbleEntity)elements.get(i)).toXML());
        }

        xml.append(getRoleElements(offering));
        xml.append("</roster>");

        return xml.toString();
    }

    private String doEnrollViewAction(String upId) throws Exception {
        // Get the offering.
        Offering offering = getDomainUser(upId).getContext().
            getCurrentOffering(TopicType.ACADEMICS);

        // Gather modes to apply.
        List sortModes = new ArrayList();
        List filterModes = new ArrayList();
        IPageMode pg = null;

        // Sort by last name.
        sortModes.add(new FColSortMode(new UserComparator()));

        // Filter by offering.
        List enrolledUsers = Memberships.getMembers(
            offering, EnrollmentStatus.ENROLLED);
        filterModes.add(new FColFilterMode(enrolledUsers));

        // Calculate which page to view.
        int pgSize = evaluatePageSize(upId);
        int pgNum = evaluateCurrentPage(upId);
        pg = new FSimplePageMode(pgSize, pgNum);

        // Evaluate first name, last name, username.
        String fName = evaluateSearchFirstName(upId);
        String lName = evaluateSearchLastName(upId);
        String uName = evaluateSearchUserName(upId);

        // Get users by search criteria.
        MemberSearchCriteria criteria = new MemberSearchCriteria();
        criteria.matchAllCriteria(true);
        criteria.setUsername(uName);
        criteria.setFirstName(fName);
        criteria.setLastName(lName);
        List users = UserFactory.findUsers(criteria);

        // Create the catalog.
        Catalog cat = getEnrollCatalog(users).subCatalog(
            (ISortMode[]) sortModes.toArray(new ISortMode[0]),
            (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),
            pg);
        List elements = cat.elements();

        // Make sure we're not beyond the last page.
        if (pgNum > pg.getPageCount()) {
            pgNum = pg.getPageCount();
            pg = new FSimplePageMode(pgSize, pgNum);
            cat = getEnrollCatalog(users).subCatalog(
                (ISortMode[]) sortModes.toArray(new ISortMode[0]),
                (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),
                pg);
            elements = cat.elements();
        }

        // Share info w/ the xsl.
        Map xslParams = getXSLParameters(upId);
        xslParams.put("enrollmentModel", offering.getEnrollmentModel().toString());
        xslParams.put("catCurrentCommand", "enrollView");
        xslParams.put("catCurrentPage", new Integer(pgNum));
        xslParams.put("catLastPage", new Integer(pg.getPageCount()));
        xslParams.put("emptyListMessage", 
            "No unenrolled users match your search criteria.");
        if (fName != null) xslParams.put("firstName", fName);
        if (lName != null) xslParams.put("lastName", lName);
        if (uName != null) xslParams.put("userID", uName);

        // Build the Xml.
        Role defaultRole = offering.getDefaultRole();

        StringBuffer xml = new StringBuffer();
        xml.append("<roster ");

        if (pgSize == 0) {
            xml.append("page=\"All\" ");
        } else {
            xml.append("page=\"");
            xml.append(pgNum);
            xml.append("\" ");
        }

        xml.append("maxPage=\"" + pg.getPageCount() + "\" ");
        xml.append("enrolledCount=\"");
        xml.append(elements.size());
        xml.append("\" ");
        xml.append("defaultRole=\"" + defaultRole.getId() + "\" ");
        xml.append(">");

        for (int i = 0; i < elements.size(); i++) {
            xml.append((String)elements.get(i));
        }

        xml.append(getRoleElements(offering));
        xml.append("</roster>");

        return xml.toString();
    }

    private String doEnrollAction(String upId) throws Exception {

        // Offering, target user, & role.
        User user = getDomainUser(upId);
        Offering offering = user.getContext().
            getCurrentOffering(TopicType.ACADEMICS);
        User targetUser = UserFactory.getUser(
            getRuntimeData(upId).getParameter("uid"));
        String roleParam = getRuntimeData(upId).
            getParameter("roleId" + targetUser.getUsername());
        Role r = RoleFactory.getRole(Integer.parseInt(roleParam));

        // Add the new member.
        try {
            Memberships.add(targetUser, offering, r, EnrollmentStatus.ENROLLED);
            // Share the user with the offering's calendar
            CalendarServiceFactory.getService().addUser(offering, targetUser);
        } catch (OperationFailedException ofe) {
            setErrorMsg(upId, "Enrollment Failed:  " + ofe.getMessage());
            return doEmptyRoster(upId);
        }

        // Notify the new member.
        StringBuffer msg = new StringBuffer();
        msg.append("You have been enrolled in the following offering:  ");
        msg.append(offering.getName());
        msg.append(" [Topic:  ");
        msg.append(offering.getTopic().getName());
        msg.append("].");

        NotificationService notifier = NotificationServiceFactory.getService();
        notifier.sendNotification(targetUser, user, msg.toString());

        // Add him to the gradebook & dirty.
        Connection conn = null;

        try {
        conn = getDBConnection();
        GradebookService gb = GradebookServiceFactory.getService();
        gb.insertUserScores(offering, targetUser, conn);
        gb.recalculateStatistics(offering, conn);
        } finally {
        releaseDBConnection(conn);
        }

        dirtyGradebook(upId, offering);

        // Dirty the new member's navigation channel.
        super.broadcastUserDirtyChannel(targetUser, NAVIGATION_CHANNEL);
        super.broadcastUserOfferingDirtyChannel(
            user, offering, ROSTER_CHANNEL, false);

        return doEnrollViewAction(upId);
    }

    private void dirtyGradebook(String upId, Offering offering)
    throws PortalException {
        broadcastUserOfferingDirtyChannel(offering, "GradebookChannel", false);

        // if the user is not enrolled in this offering (admin), their
        // gradebook won't be dirtied. So we need to dirty it manually
        // No need to check if the user is enrolled since if they were,
        // they would have been dirtied anyway
        broadcastUserDirtyChannel(getDomainUser(upId), "GradebookChannel");
    }

    private Document doEditOfferingPermissionsAction(String upId) throws Exception {
        // Offering.
        User user = getDomainUser(upId);
        Offering offering = user.getContext().getCurrentOffering(TopicType.ACADEMICS);

        // Role
        int roleId = Integer.parseInt(getRuntimeData(upId).getParameter("roleId"));
        Role role = RoleFactory.getRole(roleId);

        // Share info w/ the xsl.
        getXSLParameters(upId).put("roleId", "" + roleId);
        getXSLParameters(upId).put("roleName", role.getLabel());
        return buildDocument(offering, getPermissionsList(role));
    }

    private String doUpdateOfferingPermissionsAction(String upId) throws Exception {
        // Offering.
        User user = getDomainUser(upId);
        Offering offering = user.getContext().getCurrentOffering(TopicType.ACADEMICS);
        
        // Role.
        int roleId = Integer.parseInt(getRuntimeData(upId).getParameter("roleId"));
        Role r = RoleFactory.getRole(roleId);

        if (r.isSystemWide()) {
            Role newRole = RoleFactory.createRole(r.getLabel(),
                          offering,
                          Role.OFFERING | Role.USER_DEFINED,
                          getDomainUser(upId));

            Memberships.changeRoles(offering, r, newRole);
            r = newRole;
        }

        // Update.
        updatePermissions(getPermissionsList(r), getRuntimeData(upId), r);

        // Gradebook must be dirtied since users are viewable based on permission
        dirtyGradebook(upId, offering);
        return doPageAction(upId);
    }

    private IPermissions[] getPermissionsList(Role role)
    throws Exception {
        ChannelClass[] channels = (ChannelClass[])ChannelClassFactory.
            getChannelClasses(ChannelType.OFFERING, true).
                toArray(new ChannelClass[0]);
        return PermissionsService.instance().getPermissions(
            channels, role.getGroup());
    }

    private String doExecuteImportAction(String upId) throws Exception {

        // Offering.

        User user = getDomainUser(upId);

        Offering offering = user.getContext().getCurrentOffering(TopicType.ACADEMICS);

        // Start the response.

        StringBuffer rslt = new StringBuffer("<roster><results>");

        // Flag for failure.

        boolean failed = false;

        // File

        Object[] x = getRuntimeData(upId).getObjectParameterValues("import-file");

        if (x == null) {

            rslt.append("<message>");

            rslt.append("Import failed. No import file specified.");

            rslt.append("</message>");

            failed = true;

        }

        // Parse the document.

        Document doc = null;

        MultipartDataSource content = (MultipartDataSource) x[0];

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        builderFactory.setValidating(false);

        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();

        try {

            doc = documentBuilder.parse(content.getInputStream());

        } catch (Exception e) {

            StringBuffer msg = new StringBuffer();

            msg.append("<message>");

            msg.append("Import failed due to parse error on input file.  ");

            msg.append("Please be sure the file is formatted correctly.");

            msg.append("</message>");

            msg.append("<message>");

            msg.append("The following error was reported:  ");

            msg.append(getExceptionMessage(e));

            msg.append("</message>");

            rslt.append(msg.toString());

            failed = true;

        }

        // Do the import if all is cool.

        Iterator itr = null;

        if (!failed) {

            // Do the import.

            Map results = importChannel(offering, doc);

            // Display the successes.

            rslt.append("<success>");

            List successList = (List)results.get(SUCCESS_KEY);

            itr = successList.iterator();

            while (itr.hasNext()) {

                String username = (String)itr.next();

                rslt.append("<entry>" + username + "</entry>");

            }

            rslt.append("</success>");

            // Display the failures.

            rslt.append("<failure>");

            Map failuresMap = (Map)results.get(FAILURES_KEY);

            String msg = null;

            itr = failuresMap.keySet().iterator();

            while (itr.hasNext()) {

                msg = (String)itr.next();

                rslt.append("<reason msg=\"" + msg + "\">");

                List users = (List)failuresMap.get(msg);

                Iterator userItr = users.iterator();

                while (userItr.hasNext()) {

                    String username = (String)userItr.next();

                    rslt.append("<entry>" + username + "</entry>");

                }

                rslt.append("</reason>");

            }

            rslt.append("</failure>");

            // When the import is successful we dirty the Gradebook
            dirtyGradebook(upId, offering);

        }

        // Close the results.

        rslt.append("</results>");

        // Do the roles.

        rslt.append(getRoleElements(offering));

        // Close the roster.

        rslt.append("</roster>");

        // Manage dirty states.

        super.broadcastUserOfferingDirtyChannel(user, offering,
            ROSTER_CHANNEL, false);

        setParentChannelDirty(upId, false);

        return rslt.toString();

    }

    private String doViewMemberAction(String upId) throws Exception {
        // Obtain the target user.
        String uid = getRuntimeData(upId).getParameter("uid");
        User u = UserFactory.getUser(uid);

        // Obtain the offering.
        User user = getDomainUser(upId);
        Offering offering = user.getContext().
            getCurrentOffering(TopicType.ACADEMICS);

        // Build the xml.
        StringBuffer rslt = new StringBuffer();

        // open.
        rslt.append("<roster>");
        rslt.append("<user id=\"");
        rslt.append(u.getUsername());
        rslt.append("\">");

        // first name.
        rslt.append("<firstname>");
        rslt.append(u.getFirstName());
        rslt.append("</firstname>");

        // last name.
        rslt.append("<lastname>");
        rslt.append(u.getLastName());
        rslt.append("</lastname>");

        // user type.
        Role r = Memberships.getRole(u, offering);
        rslt.append("<type>");
        rslt.append(r.getLabel());
        rslt.append("</type>");

        // status.
        EnrollmentStatus es = Memberships.getEnrollmentStatus(u, offering);
        String strEs = null;

        if (es == null) {
            strEs = "Unenrolled";
        } else {
            strEs = es.toStringInitialCapital();
        }

        rslt.append("<status>");
        rslt.append(strEs);
        rslt.append("</status>");

        // email.
        rslt.append("<email>");
        rslt.append(u.getEmail());
        rslt.append("</email>");

        // close.
        rslt.append("</user>");
        rslt.append(getRoleElements(offering));
        rslt.append("</roster>");

        return rslt.toString();
    }

    private String doEditUserPermissionsAction(String upId) throws Exception {

        // Obtain the target user.

        String uid = getRuntimeData(upId).getParameter("uid");

        User u = UserFactory.getUser(uid);

        // Obtain the offering.

        User user = getDomainUser(upId);

        Offering offering = user.getContext().getCurrentOffering(TopicType.ACADEMICS);

        // Obtain the role.

        Role r = Memberships.getRole(u, offering);

        // Share info w/ the xsl.

        getXSLParameters(upId).put("roleName", r.getLabel());

        getXSLParameters(upId).put("userId", u.getUsername());

        // Build the xml.

        StringBuffer rslt = new StringBuffer();

        rslt.append("<roster>");

        IPermissions[] perms = getPermissionsList(r);
        ChannelClass cc = null;

        for (int i = 0; perms != null && i < perms.length; i++) {
            if (!(perms[i] instanceof ChannelClassPermissions)) {
                LogService.log(LogService.ERROR,
                    "PermissionsChannel::buildDocument : wrong IPermissions " +
                    "implementation: " + perms[i].getClass().getName());
                continue;
            }

            cc = ((ChannelClassPermissions)perms[i]).getChannelClass();
            rslt.append("<channel handle=\"");
            rslt.append(cc.getHandle());
            rslt.append("\">");

            rslt.append("<className>");
        
            rslt.append(cc.getClassName());

            rslt.append("</className>");

            rslt.append("<label>");

            rslt.append(cc.getLabel());

            rslt.append("</label>");

            rslt.append("<permissions>");

            List func = cc.getActivities();

            for (int j = 0; j < func.size(); j++) {

                Activity f = (Activity) func.get(j);

                rslt.append("<activity handle=\"");

                rslt.append(f.getHandle());

                rslt.append("\" allowed=\"");

                rslt.append(perms[i].canDo(u.getUsername(), f.getHandle()) ?
                    IPermissions.YES : IPermissions.NO);

                rslt.append("\">");

                rslt.append("<label>");

                rslt.append(f.getLabel());

                rslt.append("</label>");

                rslt.append("<description>");

                rslt.append(f.getDescription());

                rslt.append("</description>");

                rslt.append("</activity>");

            }

            rslt.append("</permissions>");

            rslt.append("</channel>");

        }

        rslt.append(getRoleElements(offering));

        rslt.append("</roster>");

        return rslt.toString();

    }

    private String doUpdateUserPermissionsAction(String upId) throws Exception {

        // Obtain the target user.

        String uid = getRuntimeData(upId).getParameter("uid");

        User u = UserFactory.getUser(uid);

        // Obtain the offering.

        User user = getDomainUser(upId);

        Offering offering = user.getContext().getCurrentOffering(TopicType.ACADEMICS);

        // Parameters.

        ChannelRuntimeData formData = getRuntimeData(upId);

        Role r = Memberships.getRole(u, offering);

        // Make sure our role is user defined.

        if (!r.isUserUniqueRole()) {

            Role newRole = RoleFactory.createRole("User Defined: " + u.getUsername(),
                          offering,
                          Role.OFFERING | Role.USER_DEFINED | Role.USER_UNIQUE,
                          user);

            Memberships.changeRole(u, offering, newRole);

            r = newRole;

        }

        // Update.

        updatePermissions(getPermissionsList(r), formData, r);

        // Gradebook must be dirtied since users are viewable based on permission
        dirtyGradebook(upId, offering);

        // Return the user to the main roster view.

        return doPageAction(upId);

    }

    private String doConfirmUnenrollAction(String upId) throws Exception {

        // Target user.

        String userIdString = getRuntimeData(upId).getParameter("uid");

        User targetUser = UserFactory.getUser(userIdString);

        // Offering.

        User usr = getDomainUser(upId);

        String offeringIdParamString = String.valueOf(usr.getContext().getCurrentOffering(TopicType.ACADEMICS).getId());;

        // Make userIdParam and offeringIdParam available to resolve.xsl

        getXSLParameters(upId).put("uid", targetUser.getUsername());

        getXSLParameters(upId).put("offeringIdParam", offeringIdParamString);

        // Make xml.

        StringBuffer rslt = new StringBuffer();

        // open.
        rslt.append("<roster>");
        rslt.append("<user id=\"");
        rslt.append(targetUser.getUsername());
        rslt.append("\">");
        // first name.
        rslt.append("<firstname>");
        rslt.append(targetUser.getFirstName());
        rslt.append("</firstname>");
        // last name.
        rslt.append("<lastname>");
        rslt.append(targetUser.getLastName());
        rslt.append("</lastname>");
        // close.
        rslt.append("</user>");
        rslt.append("</roster>");

        return rslt.toString();

    }

    private String doUnenrollAction(String upId) throws Exception {

        String confirmationParam = 
            getRuntimeData(upId).getParameter(confirmParam);

        if (confirmationParam.equals("yes")) {
            String userIdString = getRuntimeData(upId).getParameter("uid");
            User targetUser = UserFactory.getUser(userIdString);
            String offeringIdParamString = 
                getRuntimeData(upId).getParameter("offeringIdParam");
            long offeringId = Long.parseLong(offeringIdParamString);
            Offering offering = OfferingFactory.getOffering(offeringId);

            Memberships.remove(targetUser, offering, EnrollmentStatus.ENROLLED);

            // Share the user with the offering's calendar
            CalendarServiceFactory.getService().removeUser(
                offering, targetUser);

            // send a notification to the unenrolled user alerting him/her that
            // they he/she has been unenrolled from an offering.
            NotificationService notifier = 
                NotificationServiceFactory.getService();

            String msg = "You have been unenrolled from topic:" +

            offering.getTopic().getName() + ", offering:" +
            offering.getName() + ".";

            User fromUser = getDomainUser(upId);

            notifier.sendNotification(targetUser, fromUser, msg);

            GradebookService gb = GradebookServiceFactory.getService();
            Connection conn = null;
            try {
            conn = getDBConnection();
            gb.recalculateStatistics(offering, conn);
            } finally {
            releaseDBConnection(conn);
            }

            // if the DomainUser is unenrolling himself/herself or
            // someone else from an offering then set the appropriate
            // channels as being dirty
            dirtyGradebook(upId, offering);

            // set the appropriate channels dirty for targetUser
            super.broadcastUserDirtyChannel(targetUser, SUBSCRIPTION_CHANNEL);
            super.broadcastUserDirtyChannel(targetUser, NAVIGATION_CHANNEL);
            super.broadcastUserOfferingDirtyChannel(fromUser, offering,
            ROSTER_CHANNEL, false);

            // updateCache(upId);

        }

        return doPageAction(upId);

    }

    private String doResolveAction(String upId) throws Exception {

        // Share info w/ the xsl.

        String uid = getRuntimeData(upId).getParameter("uid");

        getXSLParameters(upId).put("uid", uid);

        return doEmptyRoster(upId);

    }

    private String doApproveAction(String upId) throws Exception {

        // User.

        User user = getDomainUser(upId);

        // Offering.

        Offering offering = user.getContext().getCurrentOffering(TopicType.ACADEMICS);

        // Target user.

        String uid = getRuntimeData(upId).getParameter("uid");

        User u = UserFactory.getUser(uid);

        // Role.

        Role r = Memberships.getRole(u, offering);

        // Notification service.

        NotificationService notifier = NotificationServiceFactory.getService();

        // Check the confirmation.

        String confirm = getRuntimeData(upId).getParameter(confirmParam);

        if (confirm.equals("yes")) {

            // Enroll.

            Memberships.remove(u, offering, EnrollmentStatus.PENDING);

            Memberships.add(u, offering, r, EnrollmentStatus.ENROLLED);

            // Share the user with the offering's calendar

            CalendarServiceFactory.getService().addUser(offering, u);

            // Notify.

            String msg = "Your request to join " + offering.getName() + " has been approved.";

            notifier.sendNotification(u, user, msg);

            // Add to the gradebook.

            Connection conn = null;
            try {
            conn = getDBConnection();

            GradebookService gb = GradebookServiceFactory.getService();

            gb.insertUserScores(offering, u, conn);

            gb.recalculateStatistics(offering, conn);
            } finally {
            releaseDBConnection(conn);
            }

            // Manage dirty state.

            dirtyGradebook(upId, offering);

            super.broadcastUserDirtyChannel(u, NAVIGATION_CHANNEL);
        } else {

            // Remove.

            Memberships.remove(u, offering, EnrollmentStatus.PENDING);

            // Notify.

            String msg = "Your request to join " + offering.getName() + " has been rejected.";

            notifier.sendNotification(u, user, msg);

        }

        // Manage dirty state.

        super.broadcastUserDirtyChannel(u, SUBSCRIPTION_CHANNEL);
        super.broadcastUserOfferingDirtyChannel(user, offering,
            ROSTER_CHANNEL, false);

        setParentChannelDirty(upId, false);

        return doPageAction(upId);

    }

    private IServant getSendNotificationServant(String upId, Offering offering) throws PortalException {

        IServant servant = null;

        if (getStaticData (upId).get("SendNotificationServant") == null) {

            servant = (IServant) new SendNotification ();

            ChannelStaticData servantStatic = (ChannelStaticData) getStaticData (upId).clone();

            servantStatic.put("Offering", offering);

            ((IChannel) servant).setStaticData(servantStatic);

            getStaticData(upId).put("SendNotificationServant", servant);

        }

        else {

            servant = (IServant)  getStaticData(upId).get("SendNotificationServant");

        }

        return servant;

    }

    private String toXML(String upId, Offering offering, List users)

    throws Exception {

        StringBuffer sb = new StringBuffer();

        //        putChannelAttribute(upId, ROSTER_MEMBERS, new Integer(0));

        if (users != null) {

            Iterator itr = users.iterator();

            while (itr != null && itr.hasNext()) {

                sb.append(toXML(upId, offering, (User)itr.next()));

            }

        }

        return sb.toString();

    }

    private String toXML(String upId, Offering offering, User user)

    throws Exception {

        StringBuffer sb = new StringBuffer();

        List userOfferings = Memberships.getOfferings(user, Offering.ACTIVE,

        EnrollmentStatus.ENROLLED);

        userOfferings.addAll(Memberships.getOfferings(user, Offering.INACTIVE,

        EnrollmentStatus.ENROLLED));

        List pendingUserOfferings = Memberships.getOfferings(user, Offering.ACTIVE,

        EnrollmentStatus.PENDING);

        pendingUserOfferings.addAll(Memberships.getOfferings(user, Offering.INACTIVE,

        EnrollmentStatus.PENDING));

        String status = "Unenrolled";

        String roleLabel = "Not Enrolled";

        if (userOfferings != null && userOfferings.contains(offering)) {

            status = "Enrolled";

            roleLabel = Memberships.getRole(user, offering).getLabel();

            //            incrementRosterMembers(upId);

        }

        else if (pendingUserOfferings != null && pendingUserOfferings.contains(offering)) {

            status = "Pending";

            roleLabel = Memberships.getRole(user, offering).getLabel();

            //            incrementRosterMembers(upId);

        }

        Iterator itr = user.getAttributeKeys().iterator();

        // strip off the ':<username' portion of the role label

        // for user defined roles

        int pos = roleLabel.indexOf(":");

        if (pos >= 0) {

            roleLabel = roleLabel.substring(0, pos);

        }

        sb.append("<user id=\"" + user.getUsername() + "\">\n");
        sb.append("  <firstname>" + user.getFirstName() + "</firstname>\n");
        sb.append("  <lastname>" + user.getLastName() + "</lastname>\n");
        sb.append("  <username>" + user.getUsername() + "</username>\n");
        sb.append("  <email>" + user.getEmail() + "</email>\n");
        sb.append("  <role>" + roleLabel + "</role>\n");
        sb.append("  <status>" + status + "</status>\n");
        // User Attributes

        while (itr != null && itr.hasNext()) {
            String key = (String)itr.next();
            String val = user.getAttribute(key);
            sb.append("    <" + key + ">" + val + "</" + key + ">\n");
        }

        sb.append("</user>");
        return sb.toString();
    }

    private Catalog pageCommand(String upId) throws Exception {

        Offering offering = getDomainUser(upId).getContext().getCurrentOffering(TopicType.ACADEMICS);

        String firstName = getRuntimeData(upId).getParameter("searchFirstName");

        if (firstName == null || firstName.equalsIgnoreCase("First Name")) {

            firstName = "";

        }

        String lastName = getRuntimeData(upId).getParameter("searchLastName");

        if (lastName == null || lastName.equalsIgnoreCase("Last Name")) {

            lastName = "";

        }

        if (getRuntimeData(upId).getParameter("searchAndOr") != null) {

            // Search for a subset.

            int pattern = Integer.parseInt(getRuntimeData(upId).getParameter("searchAndOr"));

            throw new UnsupportedOperationException("Implement!");

        }

        throw new UnsupportedOperationException("Implement!");

    }

    // method which processes the reject/approve for the pending enrollment request

    private Catalog approveCommand(String upId) throws Exception {

        String confirmationParam = getRuntimeData(upId).getParameter(confirmParam);

        String userIdString = getRuntimeData(upId).getParameter("userIdParam");

        String offeringIdParamString = getRuntimeData(upId).getParameter("offeringIdParam");

        long offeringId = Long.parseLong(offeringIdParamString);

        Offering offering = OfferingFactory.getOffering(offeringId);

        User targetUser = UserFactory.getUser(userIdString);

        Role targetUserRole = Memberships.getRole(targetUser, offering);

        NotificationService notifier = NotificationServiceFactory.getService();

        User fromUser = getDomainUser(upId);

        if (confirmationParam.equals("yes")) {

            Memberships.remove(targetUser, offering, EnrollmentStatus.PENDING);

            String msg = "Your request to join " + offering.getName() + " has been approved.";

            notifier.sendNotification(targetUser, fromUser, msg);

            Memberships.add(targetUser, offering, targetUserRole, EnrollmentStatus.ENROLLED);

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

            addReloadParameter(upId, "command", actPage);

            addReloadParameter(upId, "page",

            getRuntimeData(upId).getParameter("page"));

            dirtyGradebook(upId, offering);

        }

        else {

            Memberships.remove(targetUser, offering, EnrollmentStatus.PENDING);

            String msg = "Your request to join " + offering.getName() + " has been rejected.";

            notifier.sendNotification(targetUser, fromUser, msg);
        }

        // set this channel dirty so the results don't remain in the cache

        super.broadcastUserOfferingDirtyChannel(fromUser, offering,
            ROSTER_CHANNEL, false);

        return pageCommand(upId);

    }

    private String getRoleElements(Offering o) throws Exception {

        StringBuffer rslt = new StringBuffer();

        Iterator itr = RoleFactory.getOfferingRoles(o).iterator();

        Role r = null;

        while (itr != null && itr.hasNext()) {
            r = (Role) itr.next();
            if (r.isUserUniqueRole()) 
                continue;
            
            rslt.append("<role id=\"");
            rslt.append(Long.toString(r.getId()));
            rslt.append("\">");
            rslt.append(r.getLabel());
            rslt.append("</role>");
        }

        return rslt.toString();

    }

    private Document buildDocument(Offering offering,
        IPermissions[] permissions) throws Exception {

        Document doc = DocumentFactory.getNewDocument();
        Element manifest = doc.createElement("manifest");
        Element node = null;
        ChannelClass cc = null;

        for (int ix = 0; permissions != null && ix < permissions.length; ++ix) {
            if (permissions[ix] instanceof ChannelClassPermissions) {
                node = ((ChannelClassPermissions)permissions[ix]).
                    getModifiedElement();
                manifest.appendChild(
                    manifest.getOwnerDocument().importNode(node, true));
            } else {
                LogService.log(LogService.ERROR,
                    "RosterChannel::buildDocument : wrong IPermissions " +
                    "implementation: " + permissions[ix].getClass().getName());
            }
        }
        doc.appendChild(manifest);
        return doc;
    }

    public static void updatePermissions(IPermissions[] permissions,
        ChannelRuntimeData formData, Role role) throws Exception {

        ChannelClass cc        = null;
        Activity activityObj   = null;
        String handle       = null;
        String activity        = null;
        String value           = null;
        List   activityList    = null;
        boolean willDo = false;

        if (permissions == null) return;

        for (int ix = 0; ix < permissions.length; ++ix) {
            if (!(permissions[ix] instanceof ChannelClassPermissions)) {
                LogService.log(LogService.ERROR,
                    "PermissionsChannel::updatePermissions : wrong " +
                    "IPermissions implementation: " +
                    permissions[ix].getClass().getName());
                continue;
            }

            cc = ((ChannelClassPermissions)permissions[ix]).getChannelClass();

            // Retrieving Data from the Channel List
            handle = cc.getHandle();
            activityList = cc.getActivities();

            for (int iy = 0; iy < activityList.size(); ++iy) {
                activityObj = (Activity) activityList.get(iy);
                activity = activityObj.getHandle();
                value = (String) formData.getParameter(handle + "-" + activity);

                // Setting values
                if (value != null) {
                    willDo = true;
                } else {
                    willDo = false;
                }

                permissions[ix].setActivity(activity, willDo);
            }
        }
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

    private String evaluateSearchFirstName(String upId) {
        String param = getRuntimeData(upId).getParameter("firstName");
        param = (param != null && !param.trim().equals("")) ? param:null;

        return param;
    }

    private String evaluateSearchLastName(String upId) {
        String param = getRuntimeData(upId).getParameter("lastName");
        param = (param != null && !param.trim().equals("")) ? param:null;
        
        return param;
    }

    private String evaluateSearchUserName(String upId) {
        String param = getRuntimeData(upId).getParameter("userID");
        param = (param != null && !param.trim().equals("")) ? param:null;

        return param;
    }

    private int evaluateSearchAndOr(String upId) {
        int rslt = 0;       // Default -- means no search.
        // Get out if there's no fName or lName.
        if (evaluateSearchFirstName(upId) != null ||
                evaluateSearchLastName(upId) != null) {
            try {
                String s = getRuntimeData(upId).getParameter("searchAndOr");
                rslt = Integer.parseInt(s);
            } catch (Exception e) { }
        }

        return rslt;
    }

    public void setupXSLParameters(String upId) throws PortalException {
        // Prep common stuff.
        super.setupXSLParameters(upId);

        // Channel specific.
        Map xslParams = getXSLParameters(upId);
        xslParams.put("enrollCommand", actEnroll);
        xslParams.put("enrollViewCommand", actEnrollView);
        xslParams.put("importCommand", actImport);
        xslParams.put("executeImportCommand", actExecuteImport);
        xslParams.put("viewMemberCommand", actViewMember);
        xslParams.put("unenrollCommand", actUnenroll);
        xslParams.put("searchCommand", actSearch);
        xslParams.put("submitCommand", actSubmit);
        xslParams.put("updateUserPermissionsCommand", actUpdateUserPermissions);
        xslParams.put("updateOfferingPermissionsCommand", 
            actUpdateOfferingPermissions);
        xslParams.put("editUserPermissionsCommand", actEditUserPermissions);
        xslParams.put("editOfferingPermissionsCommand", 
            actEditOfferingPermissions);
        xslParams.put("confirmUnenrollCommand", actConfirmUnenroll);
        xslParams.put("resolveCommand", actResolve);
        xslParams.put("approveCommand", actApprove);
        xslParams.put("pageCommand", actPage);

        // catPageSize.
        xslParams.put("catPageSize", evaluatePageSize(upId) + "");

        // showViewAll:  flag to show the 'home page' link or not.
        String action = getRuntimeData(upId).getParameter("command");
        if (action == null) action = actPage;
        if (action.equals(actPage) && evaluateSearchAndOr(upId) == 0) {
            xslParams.put("showViewAll", "false");
        } else {
            xslParams.put("showViewAll", "true");
        }

        // Show the parameters.
        //spillParameters(xslParams);
    }

    private static void addError(Map map, String msg, String username) {

        List list = (List)map.get(msg);

        if (list == null) {

            list = new ArrayList();

            map.put(msg, list);

        }

        list.add(username);

    }

    private static String getExceptionMessage(Exception e) {

        return XmlUtils.makeStringHTMLSafe(

        ExceptionUtils.getExceptionMessage(e));

    }

    private static Catalog getBaseCatalog() {
        if (baseCatalog == null) {
            baseCatalog = new FLazyCatalog(createBaseDataSource());
        }

        return baseCatalog;
    }

    private static Catalog getEnrollCatalog(List users) {
        return new FLazyCatalog(createEnrollDataSource(users));
    }

    private static IDataSource createBaseDataSource() {
        // queryBase.
        StringBuffer qBase = new StringBuffer();
        qBase.append("SELECT R.LABEL, M.USER_NAME, ");
        qBase.append("M.ROLE_ID, M.ENROLLMENT_STATUS ");
        qBase.append("FROM MEMBERSHIP M, ROLE R ");
        qBase.append("WHERE R.ROLE_ID = M.ROLE_ID ");

        // Return.
        return new RosterDbDataSource(
            qBase.toString(), createBaseEntryConvertor());
    }

    private static IDataSource createEnrollDataSource(List users) {
        return new FColUserEntryDataSource(users, createEnrollEntryConvertor());
    }

    private static IDbEntryConvertor createBaseEntryConvertor() {

        return new IDbEntryConvertor() {

            public Object convertRow(ResultSet rs)
            throws SQLException, CatalogException {

                // status.
                int intStatus = rs.getInt("ENROLLMENT_STATUS");
                String strStatus = null;
                EnrollmentStatus status = null; 
                try {
                    status = EnrollmentStatus.getInstance(intStatus);
                } catch (ItemNotFoundException infe) {
                    String msg = "Undefined enrollment status:  " + intStatus;
                    throw new CatalogException(msg, infe);
                }

                // User.
                String username = rs.getString("USER_NAME");
                User user = null;
                try {
                    user = UserFactory.getUser(username); 
                } catch (ItemNotFoundException infe) {
                    String msg = "User does not exist:  " + username;
                    throw new CatalogException(msg, infe);
                } catch (OperationFailedException ofe) {
                    String msg = "Unable to access User:  " + username;
                    throw new CatalogException(msg, ofe);
                }

                // roleLabel.
                String roleLabel = rs.getString("LABEL");

                // Create RosterMemberEntry
                RosterMemberEntry memberEntry = 
                    new RosterMemberEntry(user, status, roleLabel);
                
                return memberEntry; 
            }

        };

    }

    private static IColEntryConvertor createEnrollEntryConvertor() {

        return new IColEntryConvertor() {

            public Object convertEntry(Object entry)
            throws CatalogException {

                User user = (User)entry;
                StringBuffer rslt = new StringBuffer();

                // open.
                rslt.append("<user id=\"");
                rslt.append(user.getUsername());
                rslt.append("\">");

                // first name.
                rslt.append("<firstname>");
                rslt.append(user.getFirstName());
                rslt.append("</firstname>");

                // last name.
                rslt.append("<lastname>");
                rslt.append(user.getLastName());
                rslt.append("</lastname>");

                // status.
                rslt.append("<status>Unenrolled</status>");

                // close.
                rslt.append("</user>");

                // return.
                return rslt.toString();
            }

        };

    }

    private static void spillParameters(ChannelRuntimeData crd) {
        StringBuffer rslt = new StringBuffer();
        java.util.Enumeration keys = crd.getParameterNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) crd.getParameter(key);
            rslt.append("\t" + key + "=" + value + "\n");
        }
    }

    private static void spillParameters(Map m) {

        StringBuffer rslt = new StringBuffer();

    // to avoid ConcurrentModificationExceptions if the
    // Map is a Hashtable or other synch structure.
    Set keySet = m.keySet();
    Object[] keys = keySet.toArray();

    for (int i = 0; i < keys.length; i++) {
            String key = (String) keys[i];
            String value = m.get(key).toString();
            rslt.append("\t" + key + "=" + value + "\n");
        }
    }

}

