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
package net.unicon.academus.service;

import java.io.Serializable;
import java.sql.Timestamp;



public class MeetingData
    extends TimeframeData 
    implements Serializable 
{

        // info fields
    private String location = null;
    private String days = null;
    private String recurrence = null;

    /** default constructor */
    public MeetingData() { super(); }

   /**
    * Constructor for a <code>MeetingData</code> object.
    *
    * @param  startDate    The startDate of the timeframe
    * @param  endDate      The endDate of the timeframe
    * @param  location    The location of the meeting or class
    * @param  days        The days to meet for the metting or class
    * @param  recurrence  The recurrence interval of the meeting or class
    *
    * @see    UCFImportAdapter
    */

    public MeetingData(String startDate, String endDate, 
                       String location, String days, String recurrence)
    {
        super(startDate, endDate, "");
        this.location = location;
        this.days = days;
        this.recurrence = recurrence;
    }

        /** Accessor for a MeetingData object's location field */
    public String getLocation() { return this.location; }
        /** Accessor for a MeetingData object's days field */
    public String getDays() { return this.days; }
        /** Accessor for a MeetingData object's recurrence field */
    public String getRecurrence() { return this.recurrence; }

        /** Mutator for a MeetingData object's location field */
    public void setLocation(String location) { this.location = location; }
        /** Mutator for a MeetingData object's days field */
    public void setDays(String days) { this.days = days; }
        /** Mutator for a MeetingData object's recurrence field */
    public void setRecurrence(String recurrence) {this.recurrence = recurrence;}

    /** 
     * Returns a string representation of a <code>MeetingData</code> 
     * 
     * @return Returns a string representation of a <code>MeetingData</code> 
     * in the format: 
     *   "recurrence:days:location:YYYY-MM-DD HH24:MI:SS<=>YYYY-MM-DD HH24:MI:SS"
     */

    public String toString() 
    {
        return recurrence + ":" + days + ":" + location + ":" 
               + this.getStartDate() + "<=>" + this.getEndDate(); 
    }


    /** 
     * Compares one <code>MeetingData</code> object to another.
     * 
     * @return true if both objects have the same values for their fields and
     *         false otherwise
     *        
     */

    public boolean equals(Object other) {
        if ( !(other instanceof MeetingData) ) return false;
        MeetingData castOther = (MeetingData) other;

        if (("" + this.getStartDate()).equals(castOther.getStartDate()) &&
            ("" + this.getEndDate()).equals(castOther.getEndDate()) &&
            ("" + location).equals(castOther.getLocation()) &&
            ("" + days).equals(castOther.getDays()) &&
            ("" + recurrence).equals(castOther.getRecurrence())) 
            return true;

        return false;
    }

}
