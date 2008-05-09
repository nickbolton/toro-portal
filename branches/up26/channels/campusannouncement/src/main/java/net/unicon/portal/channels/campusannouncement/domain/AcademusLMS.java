/*
 *******************************************************************************
 *
 * File:       AcademusLMS.java
 *
 * Copyright:  ©2002 Unicon, Inc. All Rights Reserved
 *
 * This source code is the confidential and proprietary information of Unicon.
 * No part of this work may be modified or used without the prior written
 * consent of Unicon.
 *
 *******************************************************************************
 */
package net.unicon.portal.channels.campusannouncement.domain;

import java.util.*;
import java.util.Date;

import org.jasig.portal.security.*;

import net.unicon.academus.domain.lms.*;
import net.unicon.portal.channels.campusannouncement.types.*;
import net.unicon.portal.channels.campusannouncement.util.*;
import net.unicon.portal.util.db.AcademusDBUtil;

public class AcademusLMS implements LMS {
    public boolean lmsExists() {
        return true;
    }

    public List getOfferingInfo(IPerson person) throws Exception {
        return AcademusDBUtil.query("select offering.offering_id, offering.name, topic.name from membership, offering, topic, topic_offering where membership.user_name = ? and membership.offering_id = offering.offering_id and offering.offering_id = topic_offering.offering_id and topic_offering.topic_id = topic.topic_id",

        CollectionUtil.arrayList(UPortalUtil.getUserName(person)));
    }



    public List getAnnouncements(IPerson person, List offeringIDs, int ageLimitInDays) throws Exception {
        if (offeringIDs == null || offeringIDs.size() == 0) {
            return new ArrayList();
        }

        StringBuffer queryBuffer = new StringBuffer("select announcement.announcement_id, offering.offering_id, offering.name, topic.name, announcement.body, announcement.submit_date, announcement.revise_date from announcement, offering, topic_offering, topic where ");
        AcademusDBUtil.formatComparison("announcement.offering_id", offeringIDs, queryBuffer);

        queryBuffer.append("and (announcement.revise_date >= ? or announcement.submit_date >= ?) and announcement.offering_id = offering.offering_id and offering.status = ? and offering.offering_id = topic_offering.offering_id and topic_offering.topic_id = topic.topic_id");



        java.sql.Date minimumDate = new java.sql.Date(DateUtil.computeDateDaysAgo(ageLimitInDays).getTime());

        List parameters = new ArrayList(offeringIDs);

        parameters.add(minimumDate);
        parameters.add(minimumDate);
        parameters.add(new Integer(Offering.ACTIVE));

        List rows = AcademusDBUtil.query(queryBuffer.toString(), parameters);
        List answer = new ArrayList(rows.size());

        for (int i = 0; i < rows.size(); i++) {
            List row = (List) rows.get(i);
            Announcement announcement = new OfferingAnnouncement((Number) row.get(0), "" + row.get(1), row.get(3) + " -- " + row.get(2),

            (String) row.get(4), (Date) row.get(5), (Date) row.get(6));
            answer.add(announcement);
        }
        return answer;
    }
}
