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

package net.unicon.portal.channels.gradebook;

import net.unicon.portal.common.PortalObject;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Time;

public interface IGradebookFileInfo extends PortalObject {

    public int getID();

    public int getGradebookScoreID();

    public String getFilename();

    public int getFileSize();

    public String getComment();

    public Date getDate();

    public Time getTime();

    public Timestamp getTimestamp();

    public String getContentType();

    public void setFilename(String name);

    public void setFileSize(int size);

    public void setComment(String comment);

    public void setDate(Date date);

    public void setTime(Time time);

    public void setContentType(String contentType);

}

