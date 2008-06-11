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

package net.unicon.mercury;

import java.util.HashMap;
import java.util.Map;

public final class SpecialFolder {
    
    // instance members
    private final String label;
    private final long id;
    
    // cache of special folders
    private static Map folders = new HashMap();
    
    public static final long SYSFOLDER_VALUE = 0;
    public static final long INBOX_VALUE = 1;
    public static final long SAVE_VALUE = 2;
    public static final long OUTBOX_VALUE = 4;
    
    public static final SpecialFolder SYSFOLDER = new SpecialFolder(SYSFOLDER_VALUE, "SYSTEM MESSAGES");
    public static final SpecialFolder INBOX = new SpecialFolder(INBOX_VALUE, "INBOX"); 
    public static final SpecialFolder SAVE 	= new SpecialFolder(SAVE_VALUE, "SAVE");
    public static final SpecialFolder OUTBOX = new SpecialFolder(OUTBOX_VALUE, "OUTBOX");
    
    public String getLabel(){
        return this.label;
    }
    
    public long toLong(){
        return this.id;
    }
    
    public static SpecialFolder getFolder(String label){
        Object rslt = folders.get(label);
        
        if(rslt == null){
            throw new IllegalArgumentException("Folder with given label does not exist. Label = " + label);
        }
        return (SpecialFolder)rslt;
    }
    
    private SpecialFolder(long id, String label){
        
        // assertions
        if(label == null){
            throw new IllegalArgumentException("Argument 'label' cannot be null.");
        }
        
        this.id = id;
        this.label = label;
        
        // add the instance to the cache
        folders.put(label, this);
    }

}
