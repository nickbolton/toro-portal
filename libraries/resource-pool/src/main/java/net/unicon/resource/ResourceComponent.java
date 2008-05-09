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

abstract public class ResourceComponent extends ResourceThing {
    protected ResourceThing parent;
    protected Object rawResourceComponent;
    protected String description;
    private boolean closed;

    public ResourceComponent(ResourceThing parent, Object rawResourceComponent) {
        super();
        this.parent = parent;
        parent.addComponent(this);
        this.rawResourceComponent = rawResourceComponent;
        String rawResourceDescription = null;
        try {
            rawResourceDescription = rawResourceComponent.toString();
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
               throw (ThreadDeath) t;
            }
           rawResourceDescription = rawResourceComponent.getClass().getName();
        }
        description = parent + " ==> ResourceComponent #" + number + " on " + rawResourceDescription;

        closed = false;
    }

    abstract protected void closeRawResource();

    @Override
    public Resource getTopResource() {
        return (parent == null) ? null : parent.getTopResource();
    }

    @Override
    public boolean isActive() {
        return parent != null && parent.isActive();
    }

    @Override
    public boolean released() {
        return parent != null && parent.released();
    }

    @Override
    public boolean timedOut() {
        return parent != null && parent.timedOut();
    }

    public boolean isClosed() {
        return closed;
    }

    Object getRawResourceComponent() {
        return rawResourceComponent;
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    void clear() {
        clear(false);
    }

    protected void clear(boolean clientInitiated) {
        super.clear();
        if (!clientInitiated) {
            if (log.isInfoEnabled()) {
                log.info("Clearing " + this);
            }
        }
        closeRawResource();
        rawResourceComponent = null;
        // NOTE: Don't remove from parent--this method called from ResourceThing.closeComponents(), which iterates over its children list
        //       Don't want to make that unstable
        parent = null;
        description += " (cleared)";
        closed = true;
    }

    /**
     * Check if this ResourceComponent is active.  If it is not active,
     * throw a ResourceRevokedException representing the exceptional condition
     * causing inactivity.
     * @throws ResourceRevokedException if this ResourceComponent is not active
     */
    protected void checkActive() {
        if (!isActive()) {
            if (timedOut()) {
                throw new ResourceTimedOutException(this);
            } else if (released()) {
                throw new ResourceReleasedException(this);
            } else {
                throw new ResourceRevokedException(this);
            }
        }
    }
}
