/*
 *******************************************************************************
 *
 * File:       CollectionUtil.java
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

public class CollectionUtil {

    public static List combine(List list1, List list2) {

        if (list1 == null) {

            return list2;

        } else if (list2 == null) {

            return list1;

        }

        List answer = new ArrayList(list1);

        answer.addAll(list2);

        return answer;

    }

    public static String toString(List l, String delimiter) {

        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < l.size(); i++) {

            if (i > 0) {

                buffer.append(delimiter);

            }

            buffer.append(l.get(i));

        }

        return buffer.toString();

    }

    public static List arrayList() {

        List l = new ArrayList();

        return l;

    }

    public static List arrayList(Object o1) {

        List l = new ArrayList(1);

        l.add(o1);

        return l;

    }

    public static List arrayList(Object o1, Object o2) {

        List l = new ArrayList(2);

        l.add(o1);

        l.add(o2);

        return l;

    }

    public static List arrayList(Object o1, Object o2, Object o3) {

        List l = new ArrayList(3);

        l.add(o1);

        l.add(o2);

        l.add(o3);

        return l;

    }

    public static List arrayList(Object o1, Object o2, Object o3, Object o4) {

        List l = new ArrayList(4);

        l.add(o1);

        l.add(o2);

        l.add(o3);

        l.add(o4);

        return l;

    }

    public static List arrayList(Object o1, Object o2, Object o3, Object o4, Object o5) {

        List l = new ArrayList(5);

        l.add(o1);

        l.add(o2);

        l.add(o3);

        l.add(o4);

        l.add(o5);

        return l;

    }

}

