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
package net.unicon.portal.permissions;

import net.unicon.portal.util.ChannelDefinitionUtil;
import net.unicon.sdk.util.XmlUtils;

import org.jasig.portal.ChannelDefinition;

import java.util.Map;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class ActivityFactory {

    private static final Map cache = new HashMap();

    public static Activity createActivity(String handle, String label,
                                String description, Map defaults,
                                DirtyEvent[] dirtyEvents) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (label == null) {
            String msg = "Argument 'label' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (description == null) {
            String msg = "Argument 'description' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (defaults == null) {
            String msg = "Argument 'defaults' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (dirtyEvents == null) {
            String msg = "Argument 'dirtyEvents' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        return new Activity(handle, label, description, defaults, dirtyEvents);

    }

    public static synchronized Activity[] getActivities(ChannelDefinition cd)
    throws PermissionsException {
        String key = "class."+cd.getJavaClass();
        Activity[] activities = (Activity[])cache.get(key);
        if (activities == null) {
            activities = __getActivities(cd);
            cache.put(key, activities);
        }
        return activities;
    }

    private static Activity[] __getActivities(ChannelDefinition cd)
    throws PermissionsException {
        try {
            // lookup the permissions manifest through the channel's
            // published channel parameters
            String permissions = ChannelDefinitionUtil.
                getParameter(cd, "permissions");

            // the permissions manifest will be relative to the producer,
            // if it exists. Otherwise, it will be relative to the channel class
            String producerName = ChannelDefinitionUtil.
                getParameter(cd, "producer");

            String permissionsFilePath = null;
            if (producerName != null && !"".equals(producerName)) {
                permissionsFilePath = Class.forName(producerName).
                    getResource(permissions).toString();
            } else {
                permissionsFilePath = Class.forName(cd.getJavaClass()).
                    getResource(permissions).toString();
            }

            return getActivities(permissionsFilePath);
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    public static Activity getActivity(Element activityElement) {
        String handle = activityElement.getAttribute("handle");
        Element labelElement =
            (Element)activityElement.getElementsByTagName("label").item(0);
        String label = labelElement.getFirstChild().getNodeValue();
        Element descElement = (Element)activityElement.
            getElementsByTagName("description").item(0);
        String desc = descElement.getFirstChild().getNodeValue();
        Map defaults = evaluateDefaults(
            activityElement.getElementsByTagName("setting"));
        DirtyEvent[] dirtyEvents =
            evaluateDirtyEvents(activityElement.getElementsByTagName("dirty-event"));

        return new Activity(handle, label, desc, defaults, dirtyEvents);
    }

    public static Activity[] getActivities(Element activitiesElement) {

        NodeList nl = activitiesElement.getElementsByTagName("activity");
        Activity[] activities = new Activity[nl.getLength()];
        for (int i = 0; i < nl.getLength(); i++) {
            activities[i]=getActivity((Element)nl.item(i));
        }
        return activities;
    }

    public static synchronized Activity[] getActivities(String filename)
    throws Exception {
        String key = "file."+filename;
        Activity[] activities = (Activity[])cache.get(key);
        if (activities == null) {
            activities = __getActivities(filename);
            cache.put(key, activities);
        }
        return activities;
    }

    private static Activity[] __getActivities(String filename)
    throws Exception {
        return getActivities(
            XmlUtils.parse(filename, false).getDocumentElement());
    }

    private static Map evaluateDefaults(NodeList nl) {
        // The rtn.
        Map rtn = new HashMap();
        // Utilities.
        Element set = null;
        Boolean val = null;
        for (int i = 0; i < nl.getLength(); i++) {
            set = (Element)nl.item(i);
            if (set.getAttribute("value").equalsIgnoreCase("true")) {
                val = Boolean.TRUE;
            } else {
                val = Boolean.FALSE;
            }
            rtn.put(set.getAttribute("role"), val);
        }
        return rtn;
    }

    private static DirtyEvent[] evaluateDirtyEvents(NodeList nl) {
    int eventLen = 0;
    if (nl == null || (eventLen = nl.getLength()) == 0) {
        return null;
    }

    DirtyEvent[] rtn = new DirtyEvent[eventLen];
        // Utilities.
        Element set = null;
        Boolean val = null;
    String targets = null;
        for (int i = 0; i < eventLen; i++) {
            set = (Element)nl.item(i);
            if (set.getAttribute("flag").equalsIgnoreCase("yes")) {
                val = Boolean.TRUE;
            } else {
                val = Boolean.FALSE;
            }
        // process the target attribute if it is present
        String[] tokTargets = new String[0];
        targets = set.getAttribute("targets");
        if (targets != null && targets.length() > 0) {
        StringTokenizer tok = new StringTokenizer(targets);
        ArrayList alTok = new ArrayList();
        while (tok.hasMoreTokens()) {
            alTok.add(tok.nextToken());
        }
        tokTargets = (String[])alTok.toArray(tokTargets);
        }

            rtn[i] = new DirtyEvent(set.getAttribute("name"),
                    val,
                    set.getAttribute("scope"),
                    tokTargets);
        }
        return rtn;
    }


    private ActivityFactory() { }
}
