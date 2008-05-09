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

package net.unicon.mercury.fac.rdbms;

public class RdbmsDispatchMessageStatus {

    /*
     * Class members
     */
    
    private static final int UNDELIVERED_VALUE = 1;
    private static final int READ_VALUE = 2;
    private static final int UNREAD_VALUE = 3;
    
    public static final RdbmsDispatchMessageStatus UNDELIVERED =
                        new RdbmsDispatchMessageStatus("UNDELIVERED", UNDELIVERED_VALUE);
    public static final RdbmsDispatchMessageStatus READ =
                        new RdbmsDispatchMessageStatus("READ", READ_VALUE);
    public static final RdbmsDispatchMessageStatus UNREAD =
                        new RdbmsDispatchMessageStatus("UNREAD", UNREAD_VALUE);
    
    /*
     * Instance members
     */

    private final String label;
    private final int intValue;
    
    /*
     * Public API
     */
   
    public static RdbmsDispatchMessageStatus getStatus(int value) {
        RdbmsDispatchMessageStatus rslt = null;
        
        switch(value) {
            case UNDELIVERED_VALUE:
                rslt = UNDELIVERED;
                break;
            case READ_VALUE:
                rslt = READ;
                break;
            case UNREAD_VALUE:
                rslt = UNREAD;
                break;                
            default:
                String msg = "Invalid status integer. " +
                             "Unable to determine RdbmsMessageStatus.";
                throw new RuntimeException(msg);
        }
        
        return rslt;
    }
    
    public String getLabel() {
        return this.label;
    }
    
    public int toInt() {
        return this.intValue;
    }
    
    /*
     * Private API
     */
    
    private RdbmsDispatchMessageStatus(String label, int value) {
        this.label = label;
        this.intValue = value;
    }
}
