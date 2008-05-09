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
package net.unicon.portal.channels.notepad;

import net.unicon.portal.common.PortalObject;

public interface OfferingNote extends PortalObject {

    /**
     * Get the user name for this note.
     * @return <code>String</code> the user name
     */
    public String getUsername();

    /**
     * Get the offering id for this note.
     * @return <code>int</code> the offering id for this note
     */
    public int getOfferingId();

    /**
     * get the date the annoucement was issued.
     * @return <code>String</code> the date it was issued
     */
    public String getDate();
    /**
     * get the id number of the announcement.
     * @return <code>int</code> id of the announcement
     */
    public int getId();
    /**
     * get the title od the announcement.
     * @return <code>String</code> title of the announcement
     */
    public String getTitle();
    /**
     * get the instructors id who issued the announcement.
     * @return <code>int</code> the instructor id
     */
    public int getInstructorID();
    /**
     * get the instructors name who issued the announcement.
     * @return <code>String</code> the instructor name
     */
    public String getInstructorName();
    /**
     * get the body of the announcement.
     * @return <code>String</code> the announcement body
     */
    public String getMessageBody();


}

