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

package net.unicon.alchemist.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;


/**
 * A HashMap implementation that uses soft references, 
 * leaving memory management up to the gc.
 */
public final class SoftHashMap {

        // instance members
    	private final HashMap map = new HashMap();
        private final LinkedList fifo = new LinkedList();
        private final ReferenceQueue removeQueue = new ReferenceQueue();

        private int minSize;
        private int maxSize;
        
        /**
         * Construct a SoftHashMap
         * @param minSize minimum number of objects to keep (approximate)
         */
        public SoftHashMap(int minSize) {
            this.minSize=minSize;
        }

        public SoftHashMap() {
            this(10);
        }

        public Object put(Object key,Object value) {
	    	cleanMap();
	    	KeyReferencePair pair=new KeyReferencePair(value,key,removeQueue);
	    	// place the object into fifo
	    	addToFIFO(value);
	    	return map.put(key,pair);
        }

        public Object get(Object key) {
	    	SoftReference soft_ref=(SoftReference) map.get(key);
	    	if(soft_ref!=null) {
	    	    Object obj=soft_ref.get();
	    	    if(obj==null) {
		    		// object has been consumed by gc
		    		map.remove(key);
	    	    } else {
		    		// place the object into fifo
		    		addToFIFO(obj);
	    	    }
	    	    return obj;
	    	}
	    	return null;
        }

        public Object remove(Object key) {
	    	cleanMap();
	    	SoftReference soft_ref=(SoftReference) map.remove(key);
	    	if(soft_ref!=null) {
	    	    return soft_ref.get();
	    	} else {
	    	    return null;
	    	}
        }

        public int size() {
	    	cleanMap();
	    	return map.size();
        }

        public void clear() {
	    	synchronized(fifo) {
	    	    fifo.clear();
	    	}
	    	map.clear();
        }

        public Set entrySet() {
            throw new UnsupportedOperationException();
        }

        private void addToFIFO(Object o) {
	    	synchronized(fifo) {
	    	    fifo.addFirst(o);
	    	    if(fifo.size()>minSize) {
	    	        fifo.removeLast();
	    	    }	
	    	}
        }
        
        private void cleanMap() {
	    	KeyReferencePair pair;
	    	while((pair= (KeyReferencePair)removeQueue.poll())!=null) {
	    	    map.remove(pair.key);
	    	}
        }
    
        /**
         * An extension of a SoftReference that contains a key
         * by which it was mapped.
         */
        private final static class KeyReferencePair extends SoftReference {
    	
            private final Object key; 
            public KeyReferencePair (Object value, Object key, ReferenceQueue queue) {
                super(value, queue);
                this.key = key;
            }
        }

}
