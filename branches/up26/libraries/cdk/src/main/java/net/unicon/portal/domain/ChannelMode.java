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
package net.unicon.portal.domain;

import java.util.List;
import java.util.ArrayList;

import net.unicon.academus.domain.ItemNotFoundException;

/**
 * Represents the set of channel modes.  This class follows the type-safe enumeration pattern (see Effective Java).
 * Consequently, all possible instances of <code>ChannelMode</code> are static members of the class itself. Examples include:
 * <ul> <li>Learning</li> <li>Administration</li> </ul>
 */
public final class ChannelMode {
    /* Static Members */
    public static final ChannelMode GLOBAL = new ChannelMode("Global");
    public static final ChannelMode OFFERING = new ChannelMode("Offering");
    public static final ChannelMode SUBSCRIPTION =
    new ChannelMode("Subscription");
    public static final ChannelMode ADMIN = new ChannelMode("Administration");
    public static List miscOfferingModes = null;
    /* Instance Members */
    private String label = null;
    private ChannelMode(String label) {
        this.label = label;
    }
    public static ChannelMode getChannelMode(String label)
    throws ItemNotFoundException {
        if (label.equalsIgnoreCase(OFFERING.toString())) return OFFERING;
        if (label.equalsIgnoreCase(ADMIN.toString())) return ADMIN;
        if (label.equalsIgnoreCase(GLOBAL.toString())) return GLOBAL;
        if (label.equalsIgnoreCase(SUBSCRIPTION.toString())) return SUBSCRIPTION;
        String msg = "Failed to get channel of mode: " + label;
        throw new ItemNotFoundException(msg);
    }
    /**
     * Provides the label for this channel mode.  This information is useful for display purposes.
     * @return the channel mode in string form.
     */
    public String toString() {
        return label;
    }
    public static synchronized List getMiscModes() {
        if (miscOfferingModes == null) {
            miscOfferingModes = new ArrayList();
            miscOfferingModes.add(SUBSCRIPTION);
        }
        return miscOfferingModes;
    }
    public boolean equals(ChannelMode mode) {
        if (label == null) {
            return mode.toString() == null;
        }
        return label.equals(mode.toString());
    }
}
