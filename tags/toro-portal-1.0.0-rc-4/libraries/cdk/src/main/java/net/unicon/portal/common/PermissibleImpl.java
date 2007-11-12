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
package net.unicon.portal.common;

import net.unicon.portal.permissions.Activity;

import org.jasig.portal.IPermissible;

import java.util.Map;
import java.util.HashMap;

public class PermissibleImpl implements IPermissible {
    String[] activities;
    Map activityNames;
    String[] targets;
    Map targetNames;
    String owner;
    String ownerName;

    PermissibleImpl(String owner, String ownerName,
        Activity[] activities, String[] targets) {

        this.owner = owner;
        this.ownerName = ownerName;

        if (activities == null) {
            this.activities = new String[0];
        } else {
            this.activities = new String[activities.length];
        }
        this.activityNames = new HashMap();
         
        if (targets == null) {
            this.targets = new String[0];
        } else {
            this.targets = new String[targets.length];
        }
        this.targetNames = new HashMap();

        int i=0;
        StringBuffer sb;
        for (i=0; activities!=null && i<activities.length; i++) {
            this.activities[i]=activities[i].getHandle();
            sb = new StringBuffer(512);
            sb.append(activities[i].getLabel());
            sb.append(Activity.activityNameSeparator);
            sb.append(activities[i].getDescription());
            this.activityNames.put(this.activities[i], sb.toString());
        }

        for (i=0; targets!=null && i<targets.length; i++) {
            this.targets[i]=targets[i];
            this.targetNames.put(this.targets[i], targets[i]);
        }
    }

    public String[] getActivityTokens() {
        return activities;
    }

    public String getActivityName(String token) {
        return (String)activityNames.get(token);
    }

    public String[] getTargetTokens() {
        return targets;
    }

    public String getTargetName(String token) {
        return (String)targetNames.get(token);
    }

    public String getOwnerToken() {
        return owner;
    }

    public String getOwnerName() {
        return owner;
    }
}

