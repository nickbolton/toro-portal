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

package net.unicon.academus.apps.permissions;

import java.util.Map;

import net.unicon.civis.ICivisFactory;
import net.unicon.warlock.IApplicationContext;

public class PermissionsApplicationContext implements IApplicationContext {

    // Instance Members.
    private final String id;
    private final Map portletMap;
    private final ICivisFactory[] factories;
    private final Map groupRestrictors;
    /*
     * Public API.
     */

    public PermissionsApplicationContext(String id, Map portletMap
            , ICivisFactory[] facs, Map restrictors)
    {
        // Assertions.
        if (id == null || "".equals(id))
            throw new IllegalArgumentException(
                    "Argument 'id' cannot be null or empty.");
        
        if (portletMap == null || portletMap.isEmpty())
            throw new IllegalArgumentException(
                    "Argument 'portletMap' cannot be null or empty.");
        
        // Instance Members.
        this.id = id;
        this.portletMap = portletMap;
        this.groupRestrictors = restrictors;
        this.factories = new ICivisFactory[facs.length];
        System.arraycopy(facs, 0, this.factories, 0, facs.length);
       
    }

    /** Retrieve the application context id. */
    public String getId() { return this.id; }
    
    public PortletHelper getPortlet(String handle) {
        Object portlet = portletMap.get(handle);
        if(portlet == null){
            throw new IllegalArgumentException("Portlet definition with the given handle was not found.");
        }
        
        return (PortletHelper)portlet; 
    }
    
    public PortletHelper[] listPortlets() {
        return (PortletHelper[])this.portletMap.values().toArray(new PortletHelper[0]); 
    }
    
    public ICivisFactory[] getCivisFactories() { 
        return this.factories; 
    }
    
    public Map getGroupRestrictors() { return this.groupRestrictors; } 
}
