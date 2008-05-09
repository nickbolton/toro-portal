/*
 *******************************************************************************
 *
 * File:       UPortalUtil.java
 *
 * Copyright:  ©2002 Unicon, Inc. All Rights Reserved
 *
 * This source code is the confidential and proprietary information of Unicon.
 * No part of this work may be modified or used without the prior written
 * consent of Unicon.
 *
 *******************************************************************************
 */

package net.unicon.portal.channels.campusannouncement.util;

import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;

import org.jasig.portal.security.IPerson;


public class UPortalUtil {

    public static String getUserName(IPerson person) {
        //String userNameKey = AcademusPropertiesFactory.getManager(PropertiesType.LMS).getProperty("org.jasig.portal.security.uidKey");
        String userNameKey = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("org.jasig.portal.security.uidKey");
        return (String) person.getAttribute(userNameKey);
    }
}
