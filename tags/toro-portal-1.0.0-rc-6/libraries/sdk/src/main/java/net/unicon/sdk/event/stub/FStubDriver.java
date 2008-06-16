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

import net.unicon.sdk.event.IEvent;
import net.unicon.sdk.event.IEventLogService;
import net.unicon.sdk.event.IEventQueryFilter;

/**
 * @author Kevin Gary
 *
 */
public final class FStubDriver {

	public static void main(String[] args) {
        IEventLogService service = FStubEventLogService.getEventService();
        String indexer = args[0];
         
        System.out.println("*** FStubDriver::main ***");
        System.out.println("Creating filter and registering listener with indexer " + indexer);
        IEventQueryFilter filter = new FStubQueryFilter(indexer);
        IEventQueryFilter nonMatchingFilter = new FStubQueryFilter("nonMatching " + indexer);
         
        // log a few events
        for (int i = 1; i < args.length; i++)  {
            try {
                service.logEvent(indexer, new FStubEvent(args[i]));                
                System.out.println("logged and event, IEventService.getLastEvent returns: " + service.getLastEvent(filter));
                Thread.sleep(500);
            }
            catch (Exception exc) {
                System.out.println("Exception in logging event " + i);
                exc.printStackTrace();
            }
        }
        
        // retrieve a few events
        try {
            IEvent[] eventsReturned = null;
            System.out.println("Calling IEventService.getEvents(filter)");
            eventsReturned = service.getEvents(filter);
            for (int j = 0; j < eventsReturned.length; j++) {
                System.out.println("\tEvent matched: " + eventsReturned[j]);
            }
        
            System.out.println("Calling IEventService.getEvents(nonMatchingFilter)");
            eventsReturned = service.getEvents(nonMatchingFilter);
            for (int j = 0; j < eventsReturned.length; j++) {
                System.out.println("\tEvent matched: " + eventsReturned[j]);
            }
            System.out.println("END nonMatchingFilter (no results should have been displayed)");
        
            System.out.println("Calling IEventService.getSortedEvents(filter, true)");
            eventsReturned = service.getSortedEvents(filter, true);
            for (int j = 0; j < eventsReturned.length; j++) {
                System.out.println("\tEvent matched: " + eventsReturned[j]);
            }
        
            System.out.println("Calling IEventService.getSortedEvents(filter, false)");
            eventsReturned = service.getSortedEvents(filter, false);
            for (int j = 0; j < eventsReturned.length; j++) {
                System.out.println("\tEvent matched: " + eventsReturned[j]);
            }
        }
        catch (Exception exc2) {
            exc2.printStackTrace();
            System.exit(0);        
        }
        
        System.out.println("Normal termination");
        System.exit(0);
	}
}

class FStubGUID implements net.unicon.sdk.event.IGUID {
    private String guid;
    
    FStubGUID(String guid) {
        this.guid = guid;
    }
    public boolean equals(Object obj) {
         return (obj instanceof FStubGUID && this.guid.equalsIgnoreCase( ((FStubGUID)obj).guid) );
    }
}
