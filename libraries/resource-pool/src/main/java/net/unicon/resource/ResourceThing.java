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
package net.unicon.resource;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract public class ResourceThing {

    protected final Log log = LogFactory.getLog(getClass());

    protected long number;
    protected List components = new ArrayList();

    public ResourceThing() {
        number = ResourceThing.nextNumber();
    }

    abstract public boolean isActive();
    abstract public boolean released();
    abstract public boolean timedOut();
    abstract public Resource getTopResource();

    public ResourcePool getResourcePool() {
        return (getTopResource() == null) ? null : getTopResource().getResourcePool();
    }


    public long getNumber() {
        return number;
    }

    void clear() {
        closeComponents();
        components = null;
    }

    protected void closeComponents() {
        for (int i = 0; i < components.size(); i++) {
            ResourceComponent component = (ResourceComponent) components.get(i);
            component.clear();
        }
        components.clear();
    }

    void addComponent(ResourceComponent component) {
        components.add(component);
    }

    void removeComponent(ResourceComponent component) {
        components.remove(component);
    }

    protected static long resourceThingCount = 1;

    protected synchronized static long nextNumber() {
        return resourceThingCount++;
    }
}
