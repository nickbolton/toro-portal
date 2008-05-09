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
package net.unicon.resource.thread;

import java.util.*;

public class SynchronizedQueue {
    protected List queue;

    public SynchronizedQueue() {
        this(20);
    }

    public SynchronizedQueue(int size) {
        queue = new ArrayList(size);
    }

    public int size() {
        return queue.size();
    }

    synchronized public void add(Object object) {
        if (object == null) {
            return;
        }

        queue.add(object);
        notify();
    }

    public boolean guaranteedAdd(Object object, int numberOfRetries) {
        if (numberOfRetries < 0) {
            return false;
        }

        boolean added = false;
        try {
            add(object);
            added = true;
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
        } finally {
            if (!added) {
                added = guaranteedAdd(object, numberOfRetries - 1);
            }
        }

        return added;
    }

    synchronized public Object next() {
        if (queue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                return null;
            }
        }

        if (queue.isEmpty()) {
            return next();
        } else {
            return queue.remove(0);
        }
    }

    synchronized public boolean remove(Object object) {
        return queue.remove(object);
    }

    synchronized public List contents() {
        List answer = new ArrayList(queue);
        return answer;
    }
}
