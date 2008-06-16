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
package net.unicon.sdk.event;

import java.io.Serializable;

/**
 * @author Kevin Gary
 *
 * IEventLogService is the principle service interface for generating and logging
 * events. It extends the read-only interface for events so all event services
 * are automatically read-write.
 */
public interface IEventLogService extends IEventService {

	/**
	 * Logs an event with no indexing association.
	 * 
	 * @author Kevin Gary
	 * @return void
	 * @param event the event to log
	 */
	public void logEvent(IEvent event) throws EventLogException, UnsupportedOperationException;

    /**
     * Logs an event associated with a given serializable Object. This method is the
     * preferred way to log an event associated with an Object, since the indexing
     * Object may also be persisted.
     *  
     * @author KG
     * @return void
     * @param obj Object to which the event applies
     * @param event the event to log
     */
    public void logEvent(Serializable obj, IEvent event) throws EventLogException, UnsupportedOperationException;

	/**
     * This form of the logEvent method accepts a tagging type as a category
     * type parameter.
     * 
	 * @author KG
	 * @return void
	 * @param tagType a tagging type which may be used to categorize events
	 * @param event the event to log
	 */
	public void logEvent(Class category, IEvent event) throws EventLogException, UnsupportedOperationException;
	
	/**
	 * Logs an event indexed by a GUID. This is the preferred mechanism.
	 * 
	 * @author Kevin Gary
	 * @return void
	 * @param guid a globally unique identifer used to index events
	 * @param event the event to log
	 */
	public void logEvent(IGUID guid, IEvent event) throws EventLogException, UnsupportedOperationException;


}
