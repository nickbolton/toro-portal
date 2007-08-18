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

import net.unicon.resource.smalltalk.SimpleBlock;
import net.unicon.resource.thread.GuaranteedAction;
import net.unicon.resource.thread.GuaranteedActionStrict;

public class ResourcePoolSpawning extends ResourcePoolBasic {
    public ResourcePoolSpawning(ResourcePoolConfiguration config, ResourceFactory resourceFactory) {
        super(config, resourceFactory);
    }

    protected Throwable guaranteedRecycle(final Resource resource) {
        Thread t = new Thread() {
                public void run() {
                    guaranteedRecycleWork(resource);
                }};
        t.start();

        return null;
    }

    protected Throwable guaranteedRecycleWork(final Resource resource) {
        Throwable answer = null;
        boolean removed = true;

        try {
            SimpleBlock block = new SimpleBlock() {
                    public Object evaluate() throws Exception {
                        resource.recycle();
                        resource.verify();
                        return null;
                    }};

            log.log(5, this, "recycle", "begin", resource);
            long startTime = System.currentTimeMillis();

            GuaranteedAction action = new GuaranteedActionStrict("recycle " + resource, config.getRecycleLimit() + config.getVerifyLimit(), block, true);
            action.run();
            if (action.isSuccessful()) {
                freeResources.add(resource);
                removed = false;
            }

            guaranteedActionSupport.log(action, "recycle", resource);
            log.time(5, this, startTime, "recycle", "end", resource);
        } catch (Throwable t) {
            answer = t;
            log.error(this, t);
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
        } finally {
            if (removed) {
                createResources.add(resource);
                log.log(8, this, "recycle", "finally", resource);
            }
        }

        return answer;
    }
}
