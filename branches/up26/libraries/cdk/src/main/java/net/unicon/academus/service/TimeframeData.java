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



public class TimeframeData implements Serializable
{

        // info fields
    private Timestamp startDate;
    private Timestamp endDate;
    private String term;

    /** default constructor */
    public TimeframeData() { }

   /**
    * Constructor for a <code>TimeframeData</code> object.
    *
    * @param  startDate    The startDate of the timeframe
    * @param  endDate      The endDate of the timeframe
    * @param  term         The term of the timeframe
    *
    * @see    UCFImportAdapter
    */

    public TimeframeData(String startDate, String endDate, String term)
    {
        this.startDate = startDate == null?null:Timestamp.valueOf(startDate);
        this.endDate = endDate == null?null:Timestamp.valueOf(endDate);
        this.term = term;
    }

        /** Accessor for a TimeframeData object's startDate field */
    public Timestamp getStartDate() { return this.startDate; }
        /** Accessor for a TimeframeData object's endDate field */
    public Timestamp getEndDate() { return this.endDate; }
        /** Accessor for a TimeframeData object's term field */
    public String getTerm() { return this.term; }


        /** Mutator for a TimeframeData object's startDate field */
    public void setStartDate(String startDate)
    {
        this.startDate = Timestamp.valueOf(startDate);
    }
        /** Mutator for a TimeframeData object's startDate field */
    public void setStartDate(Timestamp startDate)
    {
        this.startDate = startDate;
    }
        /** Mutator for a TimeframeData object's endDate field */
    public void setEndDate(String endDate)
    {
        this.endDate = Timestamp.valueOf(endDate);
    }
        /** Mutator for a TimeframeData objects endDate field */
    public void setEndDate(Timestamp endDate)
    {
        this.endDate = endDate;
    }
        /** Mutator for a TimeframeData objects term field */
    public void setTerm(String term) { this.term = term; }


    /**
     * Returns a string representation of a <code>TimeframeData</code>
     *
     * @return Returns a string representation of a <code>TimeframeData</code>
     *         in the format:
     *             "term: YYYY-MM-DD HH24:MI:SS<=>YYYY-MM-DD HH24:MI:SS"
     */

    public String toString()
    {
        return term + ": " + startDate + "<=>" + endDate;
    }


    /**
     * Compares one <code>TimeframeData</code> object to another.
     *
     * @return true if both objects have the same values for their fields and
     *         false otherwise
     *
     */

    public boolean equals(Object other) {
        if ( !(other instanceof TimeframeData) ) return false;
        TimeframeData castOther = (TimeframeData) other;

        if (("" + startDate).equals(castOther.getStartDate()) &&
            ("" + endDate).equals(castOther.getEndDate()) &&
            ("" + term).equals(castOther.getTerm()) )
            return true;

        return false;
    }

}
