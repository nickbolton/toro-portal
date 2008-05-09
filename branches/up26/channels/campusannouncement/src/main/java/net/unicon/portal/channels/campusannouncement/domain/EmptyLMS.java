/*

 *******************************************************************************

 *

 * File:       EmptyLMS.java

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

import java.util.ArrayList;
import java.util.List;

import org.jasig.portal.security.IPerson;

import net.unicon.portal.channels.campusannouncement.types.LMS;



public class EmptyLMS implements LMS {

    public boolean lmsExists() {

        return false;

    }

    public List getOfferingInfo(IPerson person) throws Exception {

        return new ArrayList();

    }

    public List getAnnouncements(IPerson person, List offeringIDs, int ageLimitInDays) throws Exception {

        return new ArrayList();

    }

}

