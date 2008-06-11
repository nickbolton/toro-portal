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

package net.unicon.civis;

/**
 * Represents an event within the Civis Subsystem.  External systems can
 * register event listeners to monitor changes in Civis state.
 */
public final class Event {

    /*
     * Public API.
     */

    /**
     * Triggers whenever a person is created.
     */
    public static final Event PERSON_CREATED = new Event();

    /**
     * Triggers whenever there is a change to a person's name or attributes.
     */
    public static final Event PERSON_MODIFIED = new Event();

    /**
     * Triggers whenever a person is deleted.
     */
    public static final Event PERSON_DELETED = new Event();

    /**
     * Triggers whenever a group is created.
     */
    public static final Event GROUP_CREATED = new Event();

    /**
     * Triggers whenever there is a change to a group's name or attributes.
     */
    public static final Event GROUP_MODIFIED = new Event();

    /**
     * Triggers whenever a group is deleted.
     */
    public static final Event GROUP_DELETED = new Event();

    /*
     * Implementation.
     */

    private Event() {}

}