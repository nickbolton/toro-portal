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
package net.unicon.portal.channels.navigation;

// Java framework classes.
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// uPortal framework classes.
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalException;

// UNICON classes.
import net.unicon.academus.domain.lms.Memberships;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.Role;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.channels.BaseSubChannel;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.portal.common.properties.*;
import net.unicon.portal.common.SubChannelFactory;
import net.unicon.portal.channels.SuperChannel;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.permissions.PermissionsFactory;
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
import net.unicon.portal.domain.ChannelMode;

public class NavigationChannel extends BaseSubChannel {

    private static Catalog cat = null;
    private static Catalog scat = null;

    public NavigationChannel() {
        super();
    }

    public void buildXML(String upId) throws Exception {
        // Set ssl & sheet.
        // setSSLLocation(upId, "NavigationChannel.ssl");
    // New way of getting SSL location - do we need this, seems
    // redundant in the CDM.
    ChannelDataManager.setSSLLocation(upId,
          ChannelDataManager.getChannelClass(upId).getSSLLocation());
        setSheetName(upId, "main");

        // User.
        User u = getDomainUser(upId);

        // See all parameters
        // spillParameters(getRuntimeData(upId));
        // Offering.

        Offering offering =
            u.getContext().getCurrentOffering(TopicType.ACADEMICS);
        // Gather modes to apply.
        List sortModes = new ArrayList();
        List filterModes = new ArrayList();
        IPageMode pg = null;

        // SuperUser sees everything, others must filter by enrollment.
        if (!u.isSuperUser()) {
            // Filter by user.
            String sqlUser = "M.USER_NAME = ?";
            filterModes.add(new FDbFilterMode(sqlUser,
                new Object[] { u.getUsername() }));
            // Also filter out inactive offerings that the user isn't supposed to see.
            List disallowed = new ArrayList();
            Iterator enrolled = Memberships.getAllOfferings(u).iterator();
            while (enrolled.hasNext()) {
                Offering o = (Offering) enrolled.next();
                IGroup g = Memberships.getRole(u, o).getGroup();
                IPermissions p = PermissionsFactory.getPermissions(g, ChannelDataManager.getChannelClass(upId), null);
                if (!p.canDo(u.getUsername(), "viewInactiveOfferings")) {
                    if (o.getStatus() == Offering.INACTIVE) disallowed.add(o);
                }
            }
            if (disallowed.size() > 0) {
                StringBuffer sqlInactive = new StringBuffer();
                sqlInactive.append("O.OFFERING_ID NOT IN ( ");
                for (int i=0; i < disallowed.size(); i++) {
                    Offering o = (Offering) disallowed.get(i);
                    sqlInactive.append(String.valueOf(o.getId()));
                    if (i + 1 > disallowed.size()) {
                        sqlInactive.append(", ");
                    }
                }
                sqlInactive.append(" )");
                filterModes.add(new FDbFilterMode(sqlInactive.toString(), new Object[0]));
            }
        }

        // Sort by topic, offering -- NB:  will have to change if we implement sortable columns.
        sortModes.add(new FDbSortMode("UPPER(T.NAME) ASC"));
        sortModes.add(new FDbSortMode("UPPER(O.NAME) ASC"));

        // Filter by sectionId, oName, tName where applicable.
        String sectionId = evaluateSearchSectionId(upId);
        String oName = evaluateSearchOfferingName(upId);
        String tName = evaluateSearchTopicName(upId);

        // catalog current command
        String catCurrentCommand = "page";

        if (sectionId != null) {
            catCurrentCommand = "search";
            String sql = "UPPER(OPT_OFFERINGID) = UPPER(?)";
            filterModes.add(new FDbFilterMode(sql,
                new Object[] { sectionId }));
        }

        if (oName != null) {
            catCurrentCommand = "search";
            String sql = "UPPER(O.NAME) LIKE UPPER(?)";
            filterModes.add(new FDbFilterMode(sql,
                new Object[] { oName + "%" }));
        }

        if (tName != null) {
            catCurrentCommand = "search";
            String sql = "UPPER(T.NAME) LIKE UPPER(?)";
            filterModes.add(new FDbFilterMode(sql,
                new Object[] { tName + "%" }));
        }

        // Calculate which page to view.
        int pgSize = evaluatePageSize(upId);
        int pgNum = evaluateCurrentPage(upId);
        pg = new FDbPageMode(pgSize, pgNum);

        // Create the catalog based on user type.
        Catalog c = null;

        if (u.isSuperUser()) {
            c = getSuperCatalog().subCatalog(
            (ISortMode[]) sortModes.toArray(new ISortMode[0]),
            (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),
            pg);
        } else {
            c = getCatalog().subCatalog(
            (ISortMode[]) sortModes.toArray(new ISortMode[0]),
            (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),
            pg);
        }

        List elements = c.elements();

        // Share info w/ the xsl.
        Map params = getXSLParameters(upId);
        params.put("adminMode", ChannelMode.ADMIN.toString());
        params.put("subscriptionMode", ChannelMode.SUBSCRIPTION.toString());
        params.put("offeringMode", ChannelMode.OFFERING.toString());
        params.put("currentMode", getChannelAttribute(SubChannelFactory.getParentUID(upId),
        SuperChannel.CHANNEL_MODE_KEY));

        if (offering != null) params.put("offeringID", String.valueOf(offering.getId()));
        params.put("catCurrentCommand", catCurrentCommand);
        params.put("catCurrentPage", new Integer(pgNum));
        params.put("catLastPage", new Integer(pg.getPageCount()));
        params.put("catPageSize", evaluatePageSize(upId) + "");

        if (sectionId != null) params.put("optId", sectionId);
        if (oName != null) params.put("offName", oName);
        if (tName != null) params.put("topicName", tName);

        // rslt.
        StringBuffer rslt = new StringBuffer();
        rslt.append("<navigation>\n");

        // Loop & load the offerings.
        rslt.append("<academics>\n");
        Iterator it = elements.iterator();

        while (it.hasNext()) rslt.append((String) it.next());

        rslt.append("</academics>\n");

        // Close.

        rslt.append("</navigation>\n");

        setXML(upId, rslt.toString());

    }

    private static Catalog getCatalog() {
        if (cat == null) {
            cat = new FLazyCatalog(createDataSource());
        }
        return cat;
    }

    private static Catalog getSuperCatalog() {
        if (scat == null) {
            scat = new FLazyCatalog(createSuperDataSource());
        }
        return scat;
    }

    private static IDataSource createDataSource() {
        // queryBase.
        StringBuffer qBase = new StringBuffer();
        qBase.append("SELECT O.OFFERING_ID, O.STATUS, ");
        qBase.append("O.NAME AS O_NAME, T.NAME AS T_NAME ");
        qBase.append("FROM OFFERING O, TOPIC T, MEMBERSHIP M, TOPIC_OFFERING OT ");
        qBase.append("WHERE OT.TOPIC_ID = T.TOPIC_ID AND OT.OFFERING_ID = O.OFFERING_ID ");
        qBase.append("AND O.OFFERING_ID = M.OFFERING_ID ");

        // Return.
        return new FDbDataSource(qBase.toString(), createEntryConvertor());
    }

    private static IDataSource createSuperDataSource() {
        // queryBase.
        StringBuffer qBase = new StringBuffer();
        qBase.append("SELECT O.OFFERING_ID, O.STATUS, ");
        qBase.append("O.NAME AS O_NAME, T.NAME AS T_NAME ");
        qBase.append("FROM OFFERING O, TOPIC T, TOPIC_OFFERING OT ");
        qBase.append("WHERE T.TOPIC_ID = OT.TOPIC_ID AND OT.OFFERING_ID = O.OFFERING_ID ");

        // Return.
        return new FDbDataSource(qBase.toString(), createEntryConvertor());
    }

    private static IDbEntryConvertor createEntryConvertor() {
        return new IDbEntryConvertor() {
            public Object convertRow(ResultSet rs)
            throws SQLException, CatalogException {
                StringBuffer rslt = new StringBuffer();

                // open.
                rslt.append("<offering id=\"");
                rslt.append(String.valueOf(rs.getLong("OFFERING_ID")));
                rslt.append("\" status=\"");
                rslt.append(String.valueOf(rs.getInt("STATUS")));
                rslt.append("\">");

                // offering name.
                rslt.append("<name>");
                rslt.append(rs.getString("O_NAME"));
                rslt.append("</name>");

                // topic & topic name.
                rslt.append("<topic><name>");
                rslt.append(rs.getString("T_NAME"));
                rslt.append("</name></topic>");

                // close.
                rslt.append("</offering>");

                // return.
                return rslt.toString();
            }
        };
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

    private String evaluateSearchSectionId(String upId) {
        String rslt = getRuntimeData(upId).getParameter("optId");
        if (rslt != null && rslt.trim().length() == 0) rslt = null;
        return rslt;
    }

    private String evaluateSearchOfferingName(String upId) {
        String rslt = getRuntimeData(upId).getParameter("offName");
        if (rslt != null && rslt.trim().length() == 0) rslt = null;
        return rslt;
    }

    private String evaluateSearchTopicName(String upId) {
        String rslt = getRuntimeData(upId).getParameter("topicName");
        if (rslt != null && rslt.trim().length() == 0) rslt = null;
        return rslt;
    }

    private static void spillParameters(ChannelRuntimeData crd) {
        StringBuffer rslt = new StringBuffer();
        java.util.Enumeration keys = crd.getParameterNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) crd.getParameter(key);
            rslt.append("\t" + key + "=" + value + "\n");
        }
        System.out.println("\n" + rslt.toString());
    }
}
