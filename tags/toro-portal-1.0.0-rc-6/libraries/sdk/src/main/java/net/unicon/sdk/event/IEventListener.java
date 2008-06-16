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
 *  @author KG
 *  @version 1.0
 *  @since jdk1.2
 * 
 * IEventListener should be implemented by any object interested in observing
 * events logged by a particular IEventLogService. The listener should first
 * register interest in events with the service and then be prepared to have
 * its callback method invoked whenever an event satisfying the filter criteria
 * occurs. This model is single-threaded, so listeners should be used
 * selectively and have lightweight callback methods.
 */
public interface IEventListener {

	/**
	 * @param IEvent an event logged by the IEventLogService
	 * @return void
	 * 
	 * This method is invoked whenever an IEvent matching the filter criteria 
	 * provided by the listener at registration is logged by the IEventLogService.
	 */
	public void eventLogged(Object indexer, IEvent event);
}
