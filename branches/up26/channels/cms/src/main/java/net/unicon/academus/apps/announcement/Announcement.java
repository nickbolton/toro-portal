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
package net.unicon.academus.apps.announcement;

import net.unicon.academus.common.DOMAbleEntity;
import net.unicon.academus.common.EntityObject;
import net.unicon.academus.common.XMLAbleEntity;

public interface Announcement extends EntityObject, 
				      XMLAbleEntity, 
				      DOMAbleEntity,
				      java.io.Serializable {
    public String getDate();

    /**
     * get the group id
     * @return <code>String</code> group id
     */
    public String getGroupId();
    /**
     * get the id number of the announcement.
     * @return <code>int</code> id of the announcement
     */
    public int getId();
    /**
     * get the instructors id who issued the announcement.
     * @return <code>String</code> the instructor id
     */
    public String getInstructorID();
    /**
     * get the body of the announcement.
     * @return <code>String</code> the announcement body
     */
    public String getMessageBody();
}
