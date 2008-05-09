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

package net.unicon.portal.channels.notes;

import net.unicon.portal.common.PortalObject;

public interface Note extends PortalObject {

    /**
     * get the date the note was issued.
     * @return <code>String</code> the date it was issued
     */

    public String getDate();

    /**
     * get the id number of the note.
     * @return <code>int</code> id of the note
     */

    public int getId();

    /**
     * get the body of the note.
     * @return <code>String</code> the note body
     */

    public String getMessageBody();

    /**
     * get the xml presentation of the xml object
     * @return <code>String</code> the xml of the object
     */

    /**
     * get the user id to which this note belongs
     * @return <code>int</code> the user id
     */

    public int getUserID();

}

