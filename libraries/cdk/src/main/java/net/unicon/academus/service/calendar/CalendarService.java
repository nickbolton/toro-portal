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

package net.unicon.academus.service.calendar;



import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.common.service.activation.Activation;

public interface CalendarService {

    public CalendarServerInfo createCalendar(Offering offering)
    throws Exception;

    public void deleteCalendar(Offering offering)
    throws Exception;

    public CalendarServerInfo updateCalendar(Offering offering)
    throws Exception;

    public CalendarServerInfo getCalendar(Offering offering, User user)
    throws Exception;

    public String getCalendarId(Offering offering)
    throws Exception;

    public void addUser(Offering offering, User user)

    throws Exception;

    public void removeUser(Offering offering, User user)

    throws Exception;

    /**
    * <p>
    * A method that creates events in a calendar for an activation
    * <p>
    * @param newact - a newly created activation object
    * @param calid - identifier for the calendar that events will be created on
    * @param title - title for the event
    * @param description - description for the event
    * @param priority - the priority for the event (see Event.java and event.xsl)
    * @param location - the location of the event
    * @param category - a category to place the event under
    * @param organizer - the individual whom organized the event
    */


    public Activation pushToCalendar(Activation newact, String calid, String title, String description, int priority, String location, String category, String organizer)

    throws Exception;

    /**
    * <p>
    * Deletes events for an activation from the calendar.
    * <p>
    * @param oldact - the activity for which events should be removed
    * @param calid - identifier for the calendar which the events exist in
    * <p>
    * @throws Exception - if the calendar server cannot be accessed
    */


    public void delFromCalendar(Activation oldact, String calid)

    throws Exception;
    

}

