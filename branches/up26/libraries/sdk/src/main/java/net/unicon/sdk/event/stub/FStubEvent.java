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

import java.util.Date;

import net.unicon.sdk.event.IEvent;

/**
 * @author Kevin Gary
 *
 */
public final class FStubEvent implements IEvent {

    private String message;
    private Date tstamp;
    
    public FStubEvent(String msg) {
        message = msg;
        tstamp = new Date();
    }
    
	/* (non-Javadoc)
	 * @see net.unicon.sdk.event.IEvent#getMessage()
	 */
	public String getMessage() {
		return message;
	}

	/* (non-Javadoc)
	 * @see net.unicon.sdk.event.IEvent#getCreationTimestamp()
	 */
	public Date getCreationDate() {
		// Usually one would go through a TimeService for this...
		return tstamp;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
        int rval = 0;
        // make the comparison based on Date/Time stamps
        if (arg0 instanceof IEvent) {
               rval = ((IEvent)arg0).getCreationDate().compareTo(tstamp);
        }
        else {
            throw new ClassCastException();
        }
        return rval;
	}

    public String toString() {
        return "FStubEvent:: " + tstamp + ", " + message;
    }
}
