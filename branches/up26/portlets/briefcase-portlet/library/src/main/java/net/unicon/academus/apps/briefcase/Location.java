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

package net.unicon.academus.apps.briefcase;

import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.IResource;

public final class Location {

    // Instance Members.
    private final Object obj;
    private final Drive drive;

    /*
     * Public API.
     */

    public Location(Drive obj) {

        // Assertions.
        if (obj == null) {
            String msg = "Argument 'obj' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.obj = obj;
        this.drive = obj;

    }
    
    public Location(Drive drive, IResource obj) {

        // Assertions.
        if (drive == null) {
            String msg = "Argument 'drive' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (obj == null) {
            String msg = "Argument 'obj' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.obj = obj;
        this.drive = drive;

    }

    public Object getObject() {
        return obj;
    }
    
    public Drive getDrive(){
        return drive;
    }
    
    public boolean equals(Object o){
        if(!(o instanceof Location)){
            return false;
        }
        
        return ((Location)o).getObject().equals(this.getObject());
    }
    
    public int hashCode(){
        return this.getObject().hashCode();
    }
    
    public boolean before(Location l){
        if(this.getObject() instanceof Drive && !(l.getObject() instanceof Drive)){
            return true;
        }
        if(l.getObject() instanceof Drive ){
            return false ;
        }
        
        return ((IFolder)l.getObject()).getUrl().indexOf(((IFolder)this.getObject()).getUrl()) != -1;
    }
    

}