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

/**
 * @author Kevin Gary
 *
 * A read-only interface on an event service. This interface allows a client to
 * query for events in particular ways.  
 */
public interface IEventService {
	/**
	 * Retrieves the last event chronologically that satisfies the query filter.
	 * 
	 * @author KG
	 * @return IEvent most recently added event that satisfies the query filter
	 * @param IEventQueryFilter query filter
	 * @exception
	 */
	public IEvent getLastEvent(IEventQueryFilter queryFilter) throws EventQueryException;

	/**
	 * Retrieves an array of all IEvents that satisfy the IEventQueryFilter
	 * 
	 * @author KG
	 * @return IEvent[]
	 * @param IEventQuery Filter query filter
	 * @exception
	 */
	public IEvent[] getEvents(IEventQueryFilter queryFilter) throws EventQueryException;

	/**
	 * Retrieves events that satisfy the query filter in chronological order based on the 
	 * lastToFirst parameter.
	 * 
	 * @author KG
	 * @return IEvent[] a sorted array of events
	 * @param IEventQueryFilter a query filter
	 * @param boolean true if returning most recent first, false otherwise
	 * @exception
	 */
	public IEvent[] getSortedEvents(IEventQueryFilter queryFilter,
				    				boolean lastToFirst) throws EventQueryException;

	/**
	 * Registers an object interested in events satisfying a particular query filter.
	 * 
	 * @author KG
	 * @return void
	 * @param IEventListener listener implementing the callback interface
	 * @param IEventQueryFilter filter specifying the event(s) this listener is interested in.
	 */
	public void registerListener(IEventListener listener, IEventQueryFilter queryFilter);

	/**
	 * Deregisters this object as a listener for events.
	 */
	public void deregisterListener(IEventListener listener);

}
