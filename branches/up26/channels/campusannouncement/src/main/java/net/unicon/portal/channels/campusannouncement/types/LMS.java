/*

 *******************************************************************************

 *

 * File:       LMS.java

 *

 * Copyright:  ©2002 Unicon, Inc. All Rights Reserved

 *

 * This source code is the confidential and proprietary information of Unicon.

 * No part of this work may be modified or used without the prior written

 * consent of Unicon.

 *

 *******************************************************************************

 */

package net.unicon.portal.channels.campusannouncement.types;

import java.util.*;

import org.jasig.portal.*;

import org.jasig.portal.security.*;

public interface LMS {

    public boolean lmsExists();

    public List getOfferingInfo(IPerson person) throws Exception;

    public List getAnnouncements(IPerson person, List offeringIDs, int ageLimitInDays) throws Exception;

}

