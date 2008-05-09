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
 
package net.unicon.sdk.time;

import java.sql.Timestamp;

import net.unicon.sdk.util.DateUtils;

/*  This allows user to obtain the current date and time from the database.
*/
public abstract class ADBTimeService implements TimeService {
    
    /** Constructor */
    public ADBTimeService() { }
    
    /* This method returns the current time.
     * @return <code>Timestamp</code> the current time off of the system.
     */
    public abstract Timestamp getTimestamp();
    
    /* This method returns the current time.
     * @return <code>String</code> that represents the current date in SimpleDateFormat("MM/dd/yyyy").
    */
    public String getCurrentDate() {
        return DateUtils.showDate(getTimestamp());     
    }
    
    /* This method returns the current time.
     * @return <code>String</code> that represents the current time in SimpleDateformat("H:mm").
    */
    public String getCurrentTime() {
        return DateUtils.showTime(getTimestamp());
    }
    
    /* This method returns the current date and time.
     * @return <code>String</code> that represents the current date and time in SimpleDateFormat("MM/dd/yyyy HH:mm"). 
    */ 
    public String getCurrentDateTime() {
        return DateUtils.showDateTime(getTimestamp());
    }    
}
