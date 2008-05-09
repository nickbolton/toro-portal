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

package net.unicon.academus.apps.messaging;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.unicon.alchemist.access.AccessType;
import net.unicon.mercury.IMessageFactory;

public class FactoryManager {
    /**
     */
    private transient Map factMap;

    public FactoryManager() {
        this.factMap = Collections.synchronizedMap(new HashMap());
    }

    public FactoryInfo[] listFactories() {
        return (FactoryInfo[])factMap.values().toArray(new FactoryInfo[0]);
    }

    /**
     * Retrieve the Message Factory identified by the given key.
     * @param key Identifier for the message factory instance.
     * @return The message factory identified by the given key
     */
    public FactoryInfo getFactory(String key) {
        if(key == null) {
        	throw new IllegalArgumentException(
        			"Argument 'key' cannot be null.");
        }
        if (key.equals("")) {
            throw new IllegalArgumentException(
                    "Argument 'key' cannot be an empty string.");
        }
        FactoryInfo fInfo = (FactoryInfo)factMap.get(key);

        if (fInfo == null) {
            throw new IllegalArgumentException(
                    "No factory exists with the given identifier: "+key);
        }
        
        return fInfo;
    }

    /**
     * Add a Message Factory to the session with an identifier.
     * @param factInfo FactoryInfo defining all of the factory information
     */
    public void addFactory(FactoryInfo factInfo) {
        if (factInfo == null)
            throw new IllegalArgumentException(
                    "Argument 'factInfo' cannot be null.");
        factMap.put(factInfo.getId(), factInfo);
    }

    public void removeFactory(String id) {
        factMap.remove(id);
    }

}
