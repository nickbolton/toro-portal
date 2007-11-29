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

import java.util.HashMap;
import java.util.Map;

/**
 * Information about an activity associated with a permissions object.
 */
public final class Activity {
    /* Instance Members */
    private String handle = null;
    private String label = null;
    private String description = null;
    private Map defaults = null;
    private DirtyEvent[] dirtyEvents = null;

    // this is used for sending the Activity object through
    // an IPermissible object so we can specify both the label
    // and description in the name.
    public static final String activityNameSeparator = "|do=me|";

    Activity(String handle, String label, String description, Map defaults,
         DirtyEvent[] dirtyEvents) {
        this.handle = handle;
        this.label = label;
        this.description = description;
        this.defaults = defaults;
    this.dirtyEvents = dirtyEvents;
    }
    /**
     * Provides the handle (code-friendly name) for this activity.
     * @return the activity handle.
     */
    public String getHandle() {
        return handle;
    }
    /**
     * Provides the label (display-friendly name) for this activity.
     * @return the activity label.
     */
    public String getLabel() {
        return label;
    }
    /**
     * Checks whether a given default for a label exists.
     * @return boolean
     */
    public boolean containsLabel(String label) {
        return defaults.containsKey(label);
    }

    /**
     * Provides the description of this activity..
     * @return the activity description.
     */
    public String getDescription() {
        return description;
    }

    public Map getDefaults() {
        return new HashMap(defaults);
    }

    public boolean getDefaultPermissionsSetting(String label) {
        Boolean val = (Boolean) defaults.get(label);
        if (val == null) return false;
        return val.booleanValue();
    }

    public DirtyEvent[] getDirtyEvents() {
    return dirtyEvents;
    }
}
