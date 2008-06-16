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
package net.unicon.academus.apps;

import java.util.Map;

import net.unicon.academus.common.AcademusException;
import net.unicon.portal.permissions.Activity;

public abstract class AcademusBaseApp {
    protected String _appID     = null;
    protected String _username  = null;
    protected String _contextID = null;

    protected synchronized void init(String appID, String username,
        String contextID) {

        _appID     = appID;
        _username  = username;
        _contextID = contextID;
    }

    // package scope on purpose - provide default impl
    protected void destroy() throws AcademusException {
    }

    public abstract Activity[] getActivities() throws Exception;

    public String getApplicationID() {
        return _appID;
    }

    public String getUsername() {
        return _username;
    }

    public String getContextID() {
        return _contextID;
    }
    
    protected void addResultMapping(Map m, String name, Object value) {
        if (name != null && value != null) {
            m.put(name, value);
        }
    }

    public abstract boolean equals(AcademusBaseApp app);
}
