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

package net.unicon.academus.apps.gateway;

import net.unicon.academus.apps.SsoEntry;
import net.unicon.academus.apps.SsoHandler;
import net.unicon.warlock.IApplicationContext;

public class GatewayApplicationContext implements IApplicationContext {
    
    // Instance Members.
    private SsoHandler ssoHandler;
    private SsoEntry[] ssoEntries;
    private String title;
    private String ajaxCallbackUrl = null;

    /*
     * Public API.
     */

    public GatewayApplicationContext(SsoHandler sh, SsoEntry[] entries,
        String title, String ajaxCallbackUrl) {

        // Instance Members.
        this.ssoHandler = sh;
        this.ssoEntries = entries;
        if (ssoEntries == null)
        	ssoEntries = new SsoEntry[0];
        this.title = title;
        this.ajaxCallbackUrl = ajaxCallbackUrl;
    }
    
    public boolean isAjaxFormPopulation() {
        return this.ajaxCallbackUrl != null;
    }
    
    public String getAjaxCallbackUrl() {
        return this.ajaxCallbackUrl;
    }

    public SsoEntry getSsoEntry(String handle) {
    	SsoEntry entry = null;
    	for (int i = 0; i < ssoEntries.length; i++) {
    		if (ssoEntries[i].getHandle().equals(handle)) {
    			entry = ssoEntries[i];
    			break;
    		}
    	}
    	return entry;
    }

    public SsoEntry[] getSsoEntries() {
    	SsoEntry[] rslt = new SsoEntry[ssoEntries.length];
    	rslt = ssoEntries.clone();
    	return rslt;
    }

    public SsoHandler getSsoHandler() {
        return this.ssoHandler;
    }
    
    public String getTitle() {
        return this.title;
    }

} // end GatewayApplicationContext class
