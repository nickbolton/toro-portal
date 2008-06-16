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

/*  This allows user to obtain the current date and time off of the system.  We assume you have NTP setup and it is synchronized 
 *  with the NTP server. 
 *
 * Note:  
 * NTP is a protocol designed to synchronize the clocks of computers over a network. The NTP version 3 protocol is an internet draft
 * standard, formalized in RFC 1305. The Simple NTP (SNTP) version 4 protocol is described in RFC 2030 and is the current development
 * standard. This page is home for the NTP software package, the official reference implementation of the NTP protocol. The NTP
 * distribution is largely a volunteer effort, headed by Prof. David L. Mills, University of Delaware. A list of contributing developers is
 * included in the NTP copyright page, which also contains the licensing information for the NTP software distribution and all supporting
 * material.
 * 
 * You can go to www.ntp.org for more information about NTP.
*/
public class NTPTimeServiceImpl extends ADBTimeService {
    
    /** Constructor */
    public NTPTimeServiceImpl() { }
    
    /* This method returns the current time.
     * @return <code>Timestamp</code> the current time off of the system.
     */
    public Timestamp getTimestamp() {
        Timestamp timestamp= new Timestamp(System.currentTimeMillis());
        return timestamp;
    }
    
    /* This method returns the current time.
     * @return <code>String</code> that represents the current date in SimpleDateFormat("MM/dd/yyyy").
    */
    public String getCurrentDate() {
        return super.getCurrentDate();
    }
    
    /* This method returns the current time.
     * @return <code>String</code> that represents the current time in SimpleDateformat("H:mm").
    */
    public String getCurrentTime() {
        return super.getCurrentTime(); 
    }
    
    /* This method returns the current date and time.
     * @return <code>String</code> that represents the current date and time in SimpleDateFormat("MM/dd/yyyy HH:mm"). 
    */
    public String getCurrentDateTime() {
        return super.getCurrentDateTime();
    }
    
    
    
}
