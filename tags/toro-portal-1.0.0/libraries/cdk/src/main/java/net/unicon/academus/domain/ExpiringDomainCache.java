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

package net.unicon.academus.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExpiringDomainCache implements IDomainCache {

    // Statis Members.
    private static final long CACHE_DURATION = 3600000L;    // One hour...

    // Instance Members.
    private final Map cache;

    /*
     * Public API.
     */

    public ExpiringDomainCache() {

        // Instance Members.
        this.cache = Collections.synchronizedMap(new HashMap());

    }

    public void put(Object key, IDomainEntity e) {

        // Assertions.
        if (key == null) {
            String msg = "Argument 'key' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (e == null) {
            String msg = "Argument 'e [IDomainEntity]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        cache.put(key, new EntityWrapper(e));

    }

    public IDomainEntity get(Object key) {

        // Assertions.
        if (key == null) {
            String msg = "Argument 'key' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        IDomainEntity rslt = null;  // default...

        EntityWrapper r = (EntityWrapper) cache.get(key);
        if (r != null) {
            // Choose to return or expire...
            if (r.getExpires() > System.currentTimeMillis()) {
                rslt = r.getPayload();
            } else {
                cache.remove(key);
            }
        }

        return rslt;

    }

    public void remove(Object key) {

        // Assertions.
        if (key == null) {
            String msg = "Argument 'key' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        cache.remove(key);

    }

    /*
     * Nested Types.
     */

    private static final class EntityWrapper {

        // Instance Members.
        private final IDomainEntity payload;
        private final long expires;

        /*
         * Public API.
         */

        public EntityWrapper(IDomainEntity payload) {

            // Assertions.
            if (payload == null) {
                String msg = "Argument 'payload' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.payload = payload;
            this.expires = System.currentTimeMillis() + ExpiringDomainCache.CACHE_DURATION;

        }

        public IDomainEntity getPayload() {
            return payload;
        }

        public long getExpires() {
            return expires;
        }

    }

}
