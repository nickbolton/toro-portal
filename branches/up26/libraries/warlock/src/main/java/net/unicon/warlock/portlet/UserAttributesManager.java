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

package net.unicon.warlock.portlet;

import java.util.Map;

/**
 * This class is used to propagate user attributes via a
 * <code>ThreadLocal</code> instance. Objects of this
 * class can be instantiated directly and used to store
 * or obtain user attributes in the calling thread of
 * execution.
 * 
 * @author Brad Szabo, bszabo@unicon.net
 */
public class UserAttributesManager {
    
    private static ThreadLocal threadLocal = new ThreadLocal();
    
    public void setAttributes(Map userAttributes) {
        threadLocal.set(userAttributes);
    }

    public Map getAttributes() {
        Object attributes = threadLocal.get();
        if (attributes == null) {
            throw new RuntimeException(
                    "ThreadLocal does not contain a Map of user attributes.");
        }
        return (Map)attributes;
    }
    
    public void clearAttributes() {
        threadLocal.set(null);
    }
}
