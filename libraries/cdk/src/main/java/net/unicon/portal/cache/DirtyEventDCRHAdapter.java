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
package net.unicon.portal.cache;

import net.unicon.portal.permissions.DirtyEvent;

import org.jasig.portal.PortalException;

/**
This adapter allows channels dealing with DirtyEvent encapsulations to use the
DirtyCacheRequestHandler (DCRH) to do the actual dirtying of channels. This
adapter will currently be used by the PortletMultithreadChannel as a means
to deal with dirty information passed back by a Producer. It may also be used
by any other channel that uses DirtyEvent objects (within Activity objects).

The methods that take an upId as an argument assume channel context info
is stored in the ChannelDataManager.

This interface Decorates the DCRH interface so it can provide pass-through
functionality.

@author KG
*/
public interface DirtyEventDCRHAdapter extends DirtyCacheRequestHandler {
    public void broadcastDirtyEvent(DirtyEvent de, String upId) 
	throws PortalException;

    public void broadcastDirtyEvents(DirtyEvent[] de, String upId) 
	throws PortalException;
}
