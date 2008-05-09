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

public class Resource extends ResourceThing {
    protected ResourcePool pool;
    protected Object rawResourceID;
    protected Object rawResource;
    protected String requester;
    protected String previousRequester;
    protected long assignmentTime;
    protected long lastUseTime;
    protected boolean active = true;
    protected boolean released = false;
    protected boolean timedOut = false;

    public Resource(ResourcePool pool, String requester, Object rawResourceID, Object rawResource) {
        super();
        this.pool = pool;
        this.rawResourceID = rawResourceID;
        this.rawResource = rawResource;
        this.requester = requester;
        assignmentTime = System.currentTimeMillis();
        lastUseTime = System.currentTimeMillis();
    }

    @Override
    public Resource getTopResource() {
        return this;
    }

    @Override
    public ResourcePool getResourcePool() {
        return pool;
    }



    void copyFrom(Resource otherResource) {
        rawResourceID = otherResource.rawResourceID;
        rawResource = otherResource.rawResource;
        requester = otherResource.requester;
        previousRequester = otherResource.previousRequester;
        assignmentTime = otherResource.assignmentTime;
        lastUseTime = otherResource.lastUseTime;
    }

    public Object getRawResourceID() {
        return rawResourceID;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean timedOut() {
        return timedOut;
    }

    @Override
    public boolean released() {
        return released;
    }

    public String getRequester() {
        return requester;
    }

    public String getPreviousRequester() {
        return previousRequester;
    }

    public long getAssignmentTime() {
        return assignmentTime;
    }

    public long getLastUseTime() {
        return lastUseTime;
    }

    @Override
    public String toString() {
        return longString();
    }

    protected String shortString() {
        return "" + rawResourceID;
    }

    protected String longString() {
        if (requester != null) {
            return requester + " --> " + shortString();
        } else {
            return "<Prev> " + previousRequester + " --> " + shortString();
        }
    }

    protected String getRawResourceName() {
        return "Unknown";
    }

    protected String getTypeName() {
        return "Resource";
    }

    Object getRawResource() {
        return rawResource;
    }

    void setRawResource(Object rawResource) {
        this.rawResource = rawResource;
        rawResourceID = "Unknown";
    }

    void assign(String requesterArg) {
        if (this.requester != null) {
            previousRequester = this.requester;
        }

        this.requester = requesterArg;
        assignmentTime = System.currentTimeMillis();
        lastUseTime = System.currentTimeMillis();
        active = true;
    }

    void unassign() {
        active = false;
        previousRequester = requester;
        requester = null;
    }

    void release() {
        active = false;
        released = true;
    }

    void timeout() {
        active = false;
        timedOut = true;
    }

    @Override
    void clear() {
        super.clear();
        // NOTE: Don't close the rawResource--it will be re-used if possible within a new Resource object
        pool = null;
        rawResource = null;
    }

    boolean verify() throws Exception {
        lastUseTime = System.currentTimeMillis();
        return true;
    }

    void refresh() throws Exception {
        lastUseTime = System.currentTimeMillis();
    }

    void recycle() throws Exception {
        lastUseTime = System.currentTimeMillis();
        active = false;
        closeComponents();
    }

    void destroy() throws Exception {
        lastUseTime = System.currentTimeMillis();
        active = false;
        closeComponents();
    }

    protected void checkActive() {
        if (!isActive()) {
            if (timedOut) {
                throw new ResourceTimedOutException("Attempt to access a timed out resource: " + this);
            } else if (released) {
                throw new ResourceReleasedException("Attempt to access a released resource: " + this);
            } else {
                throw new ResourceRevokedException("Attempt to access a revoked resource: " + this);
            }
        }
    }
}
