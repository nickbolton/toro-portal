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

package net.unicon.penelope.store.rdbms;

import java.sql.Connection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IComplement;
import net.unicon.penelope.IComplementType;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.PenelopeException;

/**
 * Cached RDBMS backed data store for Penelope Choice and Decision collections.
 * Uses a fixed size cache with a Least Recently Used discard algorithm.
 *
 * @author eandresen
 * @version 2005-02-14
 */
public class CachedRdbmsEntityStore extends RdbmsEntityStore {
    // Static members.
    private static final int MAX_CACHE_ENTRIES = 256;
    private static final float CACHE_LOAD_FACTOR = 0.75F;

    // Instance Members.
    private Map cache;

    /*
     * Public API.
     */

    public CachedRdbmsEntityStore(DataSource ds) {
        super(ds);

        // Store up to MAX_CACHE_ENTRIES before discarding cache. The entry
        // discarded will be the Least Recently Used (LRU).
        // Using an initial capacity of MAX_CACHE_ENTRIES/CACHE_LOAD_FACTOR
        // guarantees that rehash operations will not be needed.
        this.cache = new LinkedHashMap(
                        (int)(MAX_CACHE_ENTRIES/CACHE_LOAD_FACTOR)+1,
                        CACHE_LOAD_FACTOR,
                        true) {
            /*
                public boolean containsKey(Object value) {
                    boolean rslt = super.containsKey(value);
                    if (rslt)
                        System.err.println("Cache hit: "+value);
                    return rslt;
                }

                public Object get(Object value) {
                    Object rslt = super.get(value);
                    if (rslt != null)
                        System.err.println("Cache hit: "+rslt.getClass().getName());
                    return rslt;
                }
            */

                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > MAX_CACHE_ENTRIES;
                }
            };

        // Ensure thread safety.
        this.cache = Collections.synchronizedMap(this.cache);

    }

    /*
     * Protected API.
     */

    protected IChoiceCollection getChoiceCollection(Handle handle, Connection conn) {
        IChoiceCollection rslt = null;

        // Try the cache first.
        rslt = (IChoiceCollection)cache.get(handle);
        if (rslt == null) {
            rslt = super.getChoiceCollection(handle, conn);

            if (rslt != null) {
                cache.put(new Long(rslt.getId()), rslt);
                cache.put(rslt.getHandle(), rslt);
            }
        }

        return rslt;
    }

    protected IChoiceCollection getChoiceCollection(long id, Connection conn) {
        IChoiceCollection rslt = null;

        // Try the cache first.
        rslt = (IChoiceCollection)cache.get(new Long(id));
        if (rslt == null) {
            rslt = super.getChoiceCollection(id, conn);
            if (rslt != null) {
                cache.put(new Long(rslt.getId()), rslt);
                cache.put(rslt.getHandle(), rslt);
            }
        }

        return rslt;
    }

    protected void storeChoiceCollection(IChoiceCollection c, Connection conn) {
        super.storeChoiceCollection(c, conn);
        cache.put(new Long(c.getId()), c);
        cache.put(c.getHandle(), c);
    }

    protected void deleteChoiceCollection(IChoiceCollection c, Connection conn) {
        cache.remove(new Long(c.getId()));
        cache.remove(c.getHandle());
        super.deleteChoiceCollection(c, conn);
    }

    protected boolean existsChoiceCollection(Handle h,
                                           Connection conn) {
        boolean rslt = false;

        if (cache.containsKey(h)) {
            rslt = true;
        } else {
            rslt = super.existsChoiceCollection(h, conn);
        }

        return rslt;
    }

    protected boolean existsChoiceCollection(IChoiceCollection c,
                                           Connection conn) {
        boolean rslt = false;

        if (cache.containsKey(new Long(c.getId()))) {
            rslt = true;
        } else {
            rslt = super.existsChoiceCollection(c, conn);
        }

        return rslt;
    }

    protected IDecisionCollection getDecisionCollection(long id, Connection conn) {
        IDecisionCollection rslt = null;

        // Try the cache first.
        rslt = (IDecisionCollection)cache.get(new Long(id));
        if (rslt == null) {
            rslt = super.getDecisionCollection(id, conn);

            if (rslt != null) {
                cache.put(new Long(rslt.getId()), rslt);
            }
        }

        return rslt;
    }

    protected void storeDecisionCollection(IDecisionCollection d, Connection conn) {
        super.storeDecisionCollection(d, conn);
        cache.put(new Long(d.getId()), d);
    }

    protected void deleteDecisionCollection(IDecisionCollection d, Connection conn) {
        cache.remove(new Long(d.getId()));
        super.deleteDecisionCollection(d, conn);
    }

    protected boolean existsDecisionCollection(IDecisionCollection dc,
                                             Connection conn) {
        boolean rslt = false;

        if (cache.containsKey(new Long(dc.getId()))) {
            rslt = true;
        } else {
            rslt = super.existsDecisionCollection(dc, conn);
        }

        return rslt;
    }
}
