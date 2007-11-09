/*
 *******************************************************************************
 *
 * File:       DateUtil.java
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

import java.util.*;

public class DateUtil {

    public static Date computeDateDaysAgo(int daysAgo) {

        return computeCalendarDaysAgo(daysAgo).getTime();

    }

    public static Calendar computeCalendarDaysAgo(int daysAgo) {

        Calendar answer = new GregorianCalendar();

        answer.set(Calendar.HOUR, 0);

        answer.set(Calendar.MINUTE, 0);

        answer.set(Calendar.SECOND, 0);

        answer.set(Calendar.MILLISECOND, 0);

        answer.add(Calendar.DAY_OF_MONTH, -1 * daysAgo);

        return answer;

    }

}

