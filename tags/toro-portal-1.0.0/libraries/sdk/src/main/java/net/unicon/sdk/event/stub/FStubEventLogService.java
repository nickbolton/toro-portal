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
package net.unicon.sdk.event.stub;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import net.unicon.sdk.event.EventLogException;
import net.unicon.sdk.event.EventQueryException;
import net.unicon.sdk.event.IEvent;
import net.unicon.sdk.event.IEventListener;
import net.unicon.sdk.event.IEventLogService;
import net.unicon.sdk.event.IEventQueryFilter;
import net.unicon.sdk.event.IGUID;

/**
 * @author Kevin Gary
 *
 * This is a stub implementation to be used only for integration test.
 */
public final class FStubEventLogService implements IEventLogService {

    private Hashtable  __eventTable;
    private Hashtable  __observers;
    private IEvent     __lastEvent;
    
    private static FStubEventLogService __service = null;
    private static int __counter = 1;
    
    public static FStubEventLogService getEventService() {
        if (__service == null) {
            __service = new FStubEventLogService();
        }
        return __service;
    }
    
	/**
	 * 
	 */
	private FStubEventLogService() {
		super();
        
        __lastEvent = null;
        __eventTable = new Hashtable(100);
        __observers = new Hashtable(100);
	}

    private void __notifyListeners(Object indexer, IEvent event) {
        Enumeration observers = __observers.keys();
        IEventListener observer = null;
        IEventQueryFilter filter = null;
        
        for (; observers.hasMoreElements(); ) {
            observer = (IEventListener)observers.nextElement();
            filter = (IEventQueryFilter)__observers.get(observer);
            if (filter != null && filter.matches(indexer, event)) {
                observer.eventLogged(indexer, event);
            }
        }
    }

    private void __doLogEvent(Object indexer, IEvent event) {
        ArrayList al = (ArrayList)__eventTable.get(indexer);
        if (al == null) {
            al = new ArrayList();
            __eventTable.put(indexer, al);
        }
        al.add(event);
        __notifyListeners(indexer, event);
    }
    
	/* (non-Javadoc)
	 * @see net.unicon.sdk.event.IEventLogService#logEvent(net.unicon.sdk.event.IEvent)
	 */
	public void logEvent(IEvent event)
		throws EventLogException, UnsupportedOperationException {

        System.out.println("STUB ENTRY: FStubEventLogService.logEvent(IEvent");
                        
		__eventTable.put(new Integer(__counter++), event);
        __lastEvent = event;
        
        System.out.println("STUB ENTRY: FStubEventLogService.logEvent(IEvent");        
	}

	/* (non-Javadoc)
	 * @see net.unicon.sdk.event.IEventLogService#logEvent(java.io.Serializable, net.unicon.sdk.event.IEvent)
	 */
	public synchronized void logEvent(Serializable obj, IEvent event)
		throws EventLogException, UnsupportedOperationException {

        System.out.println("STUB ENTRY: FStubEventLogService.logEvent(Serializable, IEvent");
            
        __doLogEvent(obj, event);
        __lastEvent = event;
        
        System.out.println("STUB EXIT: FStubEventLogService.logEvent(Serializable, IEvent");         
	}

	/* (non-Javadoc)
	 * @see net.unicon.sdk.event.IEventLogService#logEvent(java.lang.Class, net.unicon.sdk.event.IEvent)
	 */
	public synchronized void logEvent(Class category, IEvent event)
		throws EventLogException, UnsupportedOperationException {

        System.out.println("STUB ENTRY: FStubEventLogService.logEvent(Class, IEvent");
            
        __doLogEvent(category, event);
        __lastEvent = event;
        
        System.out.println("STUB EXIT: FStubEventLogService.logEvent(Class, IEvent");         
	}

	/* (non-Javadoc)
	 * @see net.unicon.sdk.event.IEventLogService#logEvent(net.unicon.sdk.event.IGUID, net.unicon.sdk.event.IEvent)
	 */
	public synchronized void logEvent(IGUID guid, IEvent event)
		throws EventLogException, UnsupportedOperationException {

        System.out.println("STUB ENTRY: FStubEventLogService.logEvent(IGUID, IEvent");
            
        __doLogEvent(guid, event);
        __lastEvent = event;
        
        System.out.println("STUB EXIT: FStubEventLogService.logEvent(IGUID, IEvent");  
	}

	/* (non-Javadoc)
	 * @see net.unicon.sdk.event.IEventService#getLastEvent(net.unicon.sdk.event.IEventQueryFilter[])
	 */
	public IEvent getLastEvent(IEventQueryFilter queryFilter)
		throws EventQueryException {

        System.out.println("STUB ENTRY: FStubEventLogService.getLastEvent");
        
        // for the stub implementation, I don't care what the filter is...
		return __lastEvent;
	}

	/* (non-Javadoc)
	 * @see net.unicon.sdk.event.IEventService#getEvents(net.unicon.sdk.event.IEventQueryFilter)
	 */
	public IEvent[] getEvents(IEventQueryFilter queryFilter)
		throws EventQueryException {

        IEvent[] rval = null;
        
        System.out.println("STUB ENTRY: FStubEventLogService.getEvents");

        ArrayList al = (ArrayList)__eventTable.get(((FStubQueryFilter)queryFilter).getIndexer());
        
        System.out.println("STUB EXIT: FStubEventLogService.getEvents");
        
        // return the sorted set anyway
        if (al != null) {        
            rval = (IEvent[])al.toArray(new IEvent[0]);
        }
        else {
            rval = new IEvent[0];
        }
        return rval;
	}

	/* (non-Javadoc)
	 * @see net.unicon.sdk.event.IEventService#getSortedEvents(net.unicon.sdk.event.IEventQueryFilter, boolean)
	 */
	public IEvent[] getSortedEvents(IEventQueryFilter queryFilter, boolean lastToFirst)
		throws EventQueryException {

            System.out.println("STUB ENTRY: FStubEventLogService.getSortedEvents");
            
            IEvent[] rval = new IEvent[0];
        
            // as this is stub code, the only filter we'll accept is the bogus
            // FStubQueryFilter
            if (queryFilter instanceof FStubQueryFilter) {
                ArrayList al = (ArrayList)__eventTable.get(((FStubQueryFilter)queryFilter).getIndexer());
                if (al != null) {
                    // this is horribly inefficient
                    al = new ArrayList(al); //make a copy so no other threads worry about us sorting in-place
                    if (lastToFirst) {
                        Collections.sort(al, Collections.reverseOrder());                      
                        rval = (IEvent[])al.toArray(rval);
                    }
                    else {
                        Collections.sort(al);
                        rval = (IEvent[])al.toArray(rval);                        
                    }
                }
            }
            else {
              throw new EventQueryException("Stub EventService implementation must use stub query filter");
            }

            System.out.println("STUB EXIT: FStubEventLogService.getSortedEvents");
                        
            return rval;
	}

	/* (non-Javadoc)
	 * @see net.unicon.sdk.event.IEventService#registerListener(net.unicon.sdk.event.IEventListener[], net.unicon.sdk.event.IEventQueryFilter[])
	 */
	public void registerListener(IEventListener listener, IEventQueryFilter queryFilter) {
		__observers.put(listener, queryFilter);
	}

	/* (non-Javadoc)
	 * @see net.unicon.sdk.event.IEventService#deregisterListener(net.unicon.sdk.event.IEventListener)
	 */
	public void deregisterListener(IEventListener listener) {
        __observers.remove(listener);
	}

}
