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

import net.unicon.mercury.IRecipientType;

/**
 * Uses typesafe enum pattern.
 * @author bszabo
 *
 */
public class RdbmsRecipientType implements IRecipientType {

    /* 
     * Static members
     */
    private static final int TO_VALUE = 0;
    
    public static final RdbmsRecipientType TO = 
                        new RdbmsRecipientType("TO", TO_VALUE);
    
    public static final RdbmsRecipientType[] ALL_TYPES = { TO };
    
    /* 
     * Instance members
     */
    private final String label;
    private final int intValue;
    
    /* 
     * Public API
     */

    public static RdbmsRecipientType getType(int value) {
        RdbmsRecipientType rslt = null;
        
        switch(value) {
            case TO_VALUE:
                rslt = TO;
                break;
            default:
                String msg = "Invalid RdbmsRecipientType integer value.";
                throw new RuntimeException(msg);
        }
        
        return rslt;
    }
    
    public static RdbmsRecipientType getType(String label) {
        RdbmsRecipientType rslt = null;
        
        if (label.equalsIgnoreCase(TO.getLabel())) {
            rslt = TO;
        } else {
            String msg = "Invalid RdbmsRecipientType label.";
            throw new RuntimeException(msg);            
        }
        
        return rslt;
    }
    
    /**
     * @see net.unicon.mercury.IRecipientType#getLabel()
     */
    public String getLabel() {
        return this.label;
    }
    
    public int toInt() {
        return this.intValue;
    }
    
    /* 
     * Private API
     */
    
    private RdbmsRecipientType(String label, int value) {
        this.label = label;
        this.intValue = value;
    }
}
