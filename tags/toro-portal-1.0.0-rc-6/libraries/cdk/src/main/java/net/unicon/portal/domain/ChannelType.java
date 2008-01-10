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

import net.unicon.academus.domain.DomainException;

/**
 * Represents the set of channel types in the system. This
 * class follows the type-safe enumeration pattern (see Effective Java).
 * Consequently, all possible instances of <code>ChannelType</code> are
 * static members of the class itself. Examples include:
 * <ul> <li>Offering</li> <li>Admin</li> <li>System</li></ul>
 */
public final class ChannelType {
    /* Static Members */
    public static final ChannelType OFFERING = new ChannelType("Offering");
    public static final ChannelType ADMIN = new ChannelType("Admin");
    public static final ChannelType SYSTEM = new ChannelType("System");
    /* Instance Members */
    private String label = null;
    private ChannelType(String label) {
        this.label = label;
    }
    public static ChannelType getChannelType(String channelType)
    throws DomainException {
        if (OFFERING.toString().equalsIgnoreCase(channelType)) {
            return OFFERING;
        }
        if (ADMIN.toString().equalsIgnoreCase(channelType)) {
            return ADMIN;
        }
        if (SYSTEM.toString().equalsIgnoreCase(channelType)) {
            return SYSTEM;
        }
        throw new DomainException(
        "Could not find Channel Type: " + channelType);
    }
    /**
     * Provides the label for this channel type. 
     * This information is useful for display purposes.
     * @return the channel type in string form.
     */
    public String toString() {
        return label;
    }
}
