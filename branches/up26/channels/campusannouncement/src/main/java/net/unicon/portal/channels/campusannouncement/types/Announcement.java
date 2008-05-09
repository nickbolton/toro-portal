/*

 *******************************************************************************

 *

 * File:       Announcement.java

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

public interface Announcement extends Comparable {

    public Number getID();

    public String getCategoryID();

    public String getCategoryName();

    public boolean isCampusWide();

    public boolean isOfferingSpecific();

    public String getBody();

    public Date getDate();

    public int compareTo(Object other);

    public String toXML();

    public void toXML(StringBuffer buffer);

}

