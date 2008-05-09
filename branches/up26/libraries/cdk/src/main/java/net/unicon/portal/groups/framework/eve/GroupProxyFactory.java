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
package net.unicon.portal.groups.framework.eve;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Name;

import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.GroupsException;

public class GroupProxyFactory {
    
    private static GroupProxyFactory __instance = new GroupProxyFactory();

    //  keep a map of proxy objects to minimize the number
    // of objects .. *everyone* stays in memory
    private Map proxies = new HashMap();
    
    public static GroupProxyFactory getInstance() {
        return __instance;
    }
    
    public GroupProxy getProxy(IEntityGroup gr) throws GroupsException {
        return getProxy(gr.getKey());
    }
    
    public GroupProxy getProxy(String key) throws GroupsException {
        GroupProxy proxy = (GroupProxy)proxies.get(key);
        if (proxy == null) {
            proxy = new GroupProxy(key);
            proxies.put(key, proxy);
        }
        return proxy;
    }
    
    private GroupProxyFactory() {
        
    }
}
