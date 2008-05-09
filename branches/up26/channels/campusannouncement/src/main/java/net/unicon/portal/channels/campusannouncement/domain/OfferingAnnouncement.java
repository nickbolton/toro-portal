/*

 *******************************************************************************

 *

 * File:       OfferingAnnouncement.java

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

import org.jasig.portal.*;

public class OfferingAnnouncement extends AnnouncementImpl {

    public OfferingAnnouncement(Number announcementID, String categoryID, String categoryName, String body,
    Date submitDate, Date revisedDate) {

        super(announcementID, categoryID, categoryName, body, submitDate, revisedDate);

    }

    public String getType() {

        return "OFFERING";

    }

    public boolean isOfferingSpecific() {

        return true;

    }

}

